package com.homeyhutz.pages;

import com.homeyhutz.utils.WaitHelper;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AdminCalendarPage {

    private final WebDriver driver;
    private final WaitHelper waitHelper;

    @FindBy(css = "input[placeholder*='Search by Propert']")
    private WebElement searchBox;

    @FindBy(xpath = "//div[contains(text(),'Properties')] | //span[contains(text(),'Properties')]")
    private WebElement propertiesCountLabel;

    @FindBy(css = "button[aria-label='Previous'], .prev-btn, button:has(svg[data-icon='chevron-left'])")
    private WebElement prevArrow;

    @FindBy(css = "button[aria-label='Next'], .next-btn, button:has(svg[data-icon='chevron-right'])")
    private WebElement nextArrow;

    @FindBy(css = "input[type='date'], .date-picker-input, [class*='dateInput']")
    private WebElement dateInput;

    public AdminCalendarPage(WebDriver driver) {
        this.driver = driver;
        this.waitHelper = new WaitHelper(driver);
        PageFactory.initElements(driver, this);
    }

    public void navigate(String adminBaseUrl) {
        driver.get(adminBaseUrl + "/calendar-pricing");
        waitHelper.waitForVisible(By.cssSelector("input[placeholder*='Search by Propert']"));
    }

    public void searchProperty(String name) {
        waitHelper.waitForClickable(By.cssSelector("input[placeholder*='Search by Propert']"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        reactType(searchBox, name, js);
        waitHelper.sleep(1500);
    }

    /**
     * After searching, clicks the first visible dropdown result that matches the search term.
     * Handles Ant Design autocomplete and plain list results.
     */
    public void selectProperty(String searchTerm) {
        for (By loc : new By[]{
            By.xpath("//div[contains(@class,'cursor-pointer') and contains(normalize-space(),'" + searchTerm + "')][1]"),
            By.xpath("//li[contains(normalize-space(),'" + searchTerm + "')][1]"),
            By.xpath("//*[contains(@class,'ant-select-item') and contains(normalize-space(),'" + searchTerm + "')][1]"),
            By.xpath("//*[contains(@class,'option') and contains(normalize-space(),'" + searchTerm + "')][1]"),
            By.xpath("//*[contains(normalize-space(),'" + searchTerm + "') and (self::li or self::div or self::span)][1]")
        }) {
            for (WebElement e : driver.findElements(loc)) {
                try {
                    if (e.isDisplayed()) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", e);
                        waitHelper.sleep(1500);
                        System.out.println("Admin Calendar: selected property " + searchTerm);
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }
        System.out.println("Admin Calendar: property dropdown item not found for '" + searchTerm + "' — continuing");
    }

    /**
     * Navigates the Ant Design date picker to the target date.
     * Use 2-digit year format: "25 Dec 26" (confirmed by BlockDateTest).
     */
    public void navigateToDate(String antDate) {
        WebElement dateInput = null;
        for (By loc : new By[]{
            By.cssSelector("div.ant-picker-input input"),
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
            System.out.println("Admin Calendar: date input not found — skipping date navigation");
            return;
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        try { dateInput.click(); } catch (Exception e) { js.executeScript("arguments[0].click();", dateInput); }
        waitHelper.sleep(500);
        reactType(dateInput, antDate, js);
        dateInput.sendKeys(Keys.ENTER);
        waitHelper.sleep(2000);
        System.out.println("Admin Calendar date navigated to: " + antDate);
    }

    public void clearSearch() {
        try {
            WebElement clearBtn = driver.findElement(
                    By.cssSelector("[class*='clear'], button[aria-label='clear'], .search-clear"));
            clearBtn.click();
        } catch (NoSuchElementException e) {
            searchBox.clear();
        }
    }

    public void verifyPropertyVisible(String propertyName) {
        waitHelper.waitForTextPresent(By.tagName("body"), propertyName);
        System.out.println("✅ Property '" + propertyName + "' visible in Admin Calendar");
    }

    public void verifyPriceWithRetry(String propertyName, int expectedPrice) {
        String priceText = String.valueOf(expectedPrice);
        String formattedPrice = formatIndianCurrency(expectedPrice);

        // Poll without refreshing — refresh navigates away from the selected property + date.
        // Also check input field .value attributes, since the calendar grid renders prices
        // as Ant Design InputNumber fields (invisible to body.getText()).
        JavascriptExecutor js2 = (JavascriptExecutor) driver;
        boolean found = false;
        for (int i = 1; i <= 6 && !found; i++) {
            System.out.println("🔄 Admin Calendar sync check [" + i + "/6] — looking for: ₹" + formattedPrice);
            String body = driver.findElement(By.tagName("body")).getText();
            if (body.contains(formattedPrice) || body.contains(priceText)) {
                found = true;
                break;
            }
            try {
                Object res = js2.executeScript(
                    "return Array.from(document.querySelectorAll('input')).map(i=>i.value).filter(v=>v).join('|');");
                if (res != null && res.toString().contains(priceText)) {
                    System.out.println("✅ Admin Calendar price found in input field values: " + priceText);
                    found = true;
                    break;
                }
            } catch (Exception ignored) {}
            if (!found) {
                try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }

        org.testng.Assert.assertTrue(found, "❌ FAIL: Price ₹" + formattedPrice
                + " NOT found in Admin Calendar for property: " + propertyName);
        System.out.println("✅ Admin Calendar price ₹" + formattedPrice
                + " verified for " + propertyName);
    }

    public String getPropertyRowPrice(String propertyName) {
        try {
            WebElement row = driver.findElement(
                    By.xpath("//*[contains(text(),'" + propertyName + "')]/ancestor::tr | "
                            + "//*[contains(text(),'" + propertyName + "')]/ancestor::li | "
                            + "//*[contains(text(),'" + propertyName + "')]/ancestor::div[contains(@class,'row')]")
            );
            WebElement priceCell = row.findElement(
                    By.cssSelector("td, [class*='price'], [class*='cell']"));
            return priceCell.getText().trim();
        } catch (Exception e) {
            System.out.println("⚠️ Could not get row price: " + e.getMessage());
            return "";
        }
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
