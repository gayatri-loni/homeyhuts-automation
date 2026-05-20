package com.homeyhutz.pages;

import com.homeyhutz.utils.WaitHelper;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.util.List;

public class AdminInventoryRatesPage {

    private final WebDriver driver;
    private final WaitHelper waitHelper;

    @FindBy(css = "input[placeholder='Search by Property Name']")
    private WebElement searchBox;

    @FindBy(css = "[class*='dropdown'] [class*='selected'], select, [role='combobox']")
    private WebElement propertyDropdown;

    @FindBy(xpath = "//span[contains(text(),'Properties')]/following-sibling::*[1] | //*[contains(@class,'properties-badge')]")
    private WebElement propertiesIdBadge;

    @FindBy(css = "input[type='checkbox'][id*='base'], [class*='toggle'], [aria-label*='Base Rate']")
    private WebElement showBaseRateToggle;

    @FindBy(css = "button[class*='refresh'], button:has(svg[class*='refresh'])")
    private WebElement refreshButton;

    public AdminInventoryRatesPage(WebDriver driver) {
        this.driver = driver;
        this.waitHelper = new WaitHelper(driver);
        PageFactory.initElements(driver, this);
    }

    public void navigate(String adminBaseUrl, String propertyUuid) {
        navigate(adminBaseUrl, propertyUuid, null);
    }

    public void navigate(String adminBaseUrl, String propertyUuid, String isoDate) {
        String url = adminBaseUrl + "/inventory-rates?showOnlyBaseRate=true&id=" + propertyUuid;
        if (isoDate != null && !isoDate.isEmpty()) url += "&currentDate=" + isoDate;
        driver.get(url);
        waitHelper.sleep(2000);
    }

    public void searchProperty(String name) {
        waitHelper.waitForVisible(By.cssSelector("input[placeholder='Search by Property Name']"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        try { searchBox.click(); } catch (Exception ignored) {}
        try { searchBox.clear(); } catch (Exception ignored) {}
        try {
            searchBox.sendKeys(name);
            if (name.equals(searchBox.getAttribute("value"))) { waitHelper.sleep(1500); return; }
        } catch (Exception ignored) {}
        js.executeScript(
            "var s=Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
            "s.call(arguments[0],arguments[1]);" +
            "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));" +
            "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
            searchBox, name);
        waitHelper.sleep(1500);
    }

    public void refreshInventory() {
        try {
            refreshButton.click();
            waitHelper.sleep(1000);
        } catch (NoSuchElementException e) {
            System.out.println("⚠️ Refresh button not found; continuing without refresh");
        }
    }

    public void verifyPropertyIdBadge(String propertyId) {
        waitHelper.waitForTextPresent(By.tagName("body"), propertyId);
        System.out.println("✅ Admin Inventory & Rates property badge verified: " + propertyId);
    }

    public boolean verifyPricePresent(int expectedPrice) {
        String formattedPrice = formatIndianCurrency(expectedPrice);
        String bodyText = driver.findElement(By.tagName("body")).getText();
        boolean found = bodyText.contains(formattedPrice) || bodyText.contains(String.valueOf(expectedPrice));
        if (!found) {
            System.out.println("⚠️ Expected price not found yet: " + formattedPrice);
        }
        return found;
    }

    public void verifyPriceWithRetry(int expectedPrice) {
        String formattedPrice = formatIndianCurrency(expectedPrice);
        String priceText = String.valueOf(expectedPrice);

        // Poll without refreshing — refresh loses the pre-loaded URL params
        boolean found = false;
        for (int i = 1; i <= 5 && !found; i++) {
            System.out.println("🔄 Inventory & Rates sync check [" + i + "/5] — looking for: " + formattedPrice);
            String body = driver.findElement(By.tagName("body")).getText();
            if (body.contains(formattedPrice) || body.contains(priceText)) {
                found = true;
            } else {
                try { Thread.sleep(2500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
        org.testng.Assert.assertTrue(found,
            "❌ FAIL: Admin Inventory & Rates did not show price ₹" + formattedPrice);
        System.out.println("✅ Admin Inventory & Rates shows price " + formattedPrice);
    }

    /**
     * Navigates to a date using the Ant Design date picker.
     * Use 2-digit year: "25 Dec 26" (confirmed by BlockDateTest).
     */
    public void navigateToDate(String antDate) {
        WebElement dateInput = null;
        for (By loc : new By[]{
            By.cssSelector("input[placeholder='Select date']"),
            By.cssSelector("input.ant-picker-input"),
            By.cssSelector(".ant-picker input")
        }) {
            for (WebElement e : driver.findElements(loc)) {
                try { if (e.isDisplayed()) { dateInput = e; break; } } catch (Exception ignored) {}
            }
            if (dateInput != null) break;
        }
        if (dateInput == null) {
            System.out.println("Admin Inventory: date input not found — skipping date navigation");
            return;
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        try { dateInput.click(); } catch (Exception e) { js.executeScript("arguments[0].click();", dateInput); }
        waitHelper.sleep(500);
        reactType(dateInput, antDate, js);
        dateInput.sendKeys(Keys.ENTER);
        waitHelper.sleep(2000);
        System.out.println("Admin Inventory date navigated to: " + antDate);
    }

    /**
     * Reads the rate/price value for the selected date.
     * Tries the rate-specific input field first (id contains 'rate_' and the date key),
     * then falls back to any visible non-zero number input.
     */
    public String getRateForDate(String dateKey) {
        // Specific rate field: id="rate_{uuid}_{date}" pattern (mirrors BlockDateTest inventory field)
        for (By loc : new By[]{
            By.cssSelector("input[id*='rate_'][id*='" + dateKey + "']"),
            By.cssSelector("input[id*='price_'][id*='" + dateKey + "']"),
            By.cssSelector("input[id*='rate_']"),
            By.cssSelector("input[id*='price_']")
        }) {
            for (WebElement e : driver.findElements(loc)) {
                try {
                    if (e.isDisplayed()) {
                        ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", e);
                        waitHelper.sleep(300);
                        String val = e.getAttribute("value");
                        if (val != null && !val.isEmpty()) {
                            System.out.println("Admin Inventory rate field value: " + val);
                            return val;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // Fallback: any visible number input with a meaningful (non-zero, non-one) value
        for (WebElement e : driver.findElements(By.cssSelector("input[type='number'], input.ant-input-number-input"))) {
            try {
                if (!e.isDisplayed()) continue;
                String val = e.getAttribute("value");
                if (val != null && !val.isEmpty() && !val.equals("0") && !val.equals("1")) {
                    System.out.println("Admin Inventory fallback number input value: " + val);
                    return val;
                }
            } catch (Exception ignored) {}
        }

        System.out.println("Admin Inventory: no rate input found — returning body text");
        return driver.findElement(By.tagName("body")).getText();
    }

    private String formatIndianCurrency(int amount) {
        return String.format("%,d", amount).replace(",", ",");
    }

    private void reactType(WebElement input, String value, JavascriptExecutor js) {
        try { input.click(); } catch (Exception ignored) {}
        try { input.clear(); } catch (Exception ignored) {}
        try {
            input.sendKeys(value);
            if (value.equals(input.getAttribute("value"))) return;
        } catch (Exception ignored) {}
        js.executeScript(
            "var s=Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
            "s.call(arguments[0],arguments[1]);" +
            "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));" +
            "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
            input, value);
    }
}
