package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.List;

public class PricingPage extends BasePage {

    public PricingPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    // Locators for Host Portal Navigation
    private static final By THREE_DOT_MENU = By.xpath("//button[contains(@aria-label, 'menu') or contains(., '⋮')]");
    private static final By HOST_CONTROL_CENTER = By.xpath("//a[contains(text(), 'Host Control Center') or contains(., 'Host Control Center')]");
    // Broad locator matching the actual host portal sidebar — mirrors BlockDateTest.clickProperties()
    private static final By PROPERTIES_SIDEBAR = By.xpath(
        "//a[normalize-space()='Properties'] | " +
        "//button[normalize-space()='Properties'] | " +
        "//*[normalize-space()='Properties' and (self::a or self::button or self::div or self::span)] | " +
        "//div[contains(@class,'cursor-pointer') and .//span[normalize-space()='Properties']]"
    );
    
    // Locators for Property Management — most specific placeholder first (host portal confirmed)
    private static final By PROPERTY_SEARCH_BOX = By.cssSelector(
        "input[placeholder='Search by Property Name, Id, City']," +
        "input[placeholder*='Search by Property Name']," +
        "input[placeholder*='Search by Property Id']," +
        "input[placeholder*='Search by Property']"
    );
    private static final By PROPERTY_CARD = By.xpath("//div[contains(@class, 'property')] | //li[contains(@class, 'property-row')]");
    // contains(., ...) matches nested text nodes — same approach as BlockDateTest.clickCalendarAndPricingTab()
    private static final By PRICING_TAB = By.xpath(
        "//button[contains(.,'Calendar') and contains(.,'Pricing')] | " +
        "//a[contains(.,'Calendar') and contains(.,'Pricing')] | " +
        "//*[@role='tab'][contains(.,'Calendar')] | " +
        "//*[normalize-space()='Calendar & Pricing']"
    );
    
    // Locators for Pricing Updates
    private static final By DATE_CELL = By.xpath("//div[contains(@class, 'date-cell')] | //td[contains(@class, 'date')]");
    private static final By UPDATE_PRICE_BUTTON = By.xpath(
        "//button[normalize-space()='Change Price'] | " +
        "//button[contains(.,'Change Price')] | " +
        "//div[normalize-space()='Change Price'] | " +
        "//*[normalize-space()='Change Price' and (self::button or self::div or self::span or self::a)]"
    );
    private static final By SAVE_BUTTON = By.xpath(
        "//button[normalize-space()='Update price'] | " +
        "//button[normalize-space()='Update Price'] | " +
        "//button[normalize-space()='Update'] | " +
        "//button[normalize-space()='Save'] | " +
        "//button[contains(.,'Update price')] | " +
        "//button[contains(.,'Update Price')] | " +
        "//button[contains(.,'Save')] | " +
        "//*[@type='submit']"
    );

    /**
     * Click on the three-dot menu button
     */
    public void clickThreeDotMenu() {
        click(THREE_DOT_MENU);
        pause(500);
    }

    /**
     * Click on Host Control Center menu item
     */
    public void clickHostControlCenter() {
        click(HOST_CONTROL_CENTER);
        waitForPageToLoad();
    }

    /**
     * Click on Properties in the sidebar
     */
    public void clickPropertiesSidebar() {
        click(PROPERTIES_SIDEBAR);
        pause(500);
    }

    /**
     * Search for a property by name or ID.
     * Uses the React native value setter to type all characters atomically —
     * avoids the single-character drop caused by React re-rendering after each keystroke.
     */
    public void searchProperty(String query) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement searchBox = waitForVisibility(PROPERTY_SEARCH_BOX);
        js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", searchBox);
        try { searchBox.click(); } catch (Exception ignored) {}
        try { searchBox.clear(); } catch (Exception ignored) {}
        try {
            searchBox.sendKeys(query);
            String val = searchBox.getAttribute("value");
            if (query.equals(val)) {
                System.out.println("Property search typed: " + val);
                pause(1500);
                return;
            }
        } catch (Exception ignored) {}
        // React native value setter — sets all characters at once without per-keystroke re-render
        js.executeScript(
            "var s = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
            "s.call(arguments[0], arguments[1]);" +
            "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));" +
            "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
            searchBox, query);
        System.out.println("Property search typed via JS: " + js.executeScript("return arguments[0].value;", searchBox));
        pause(1500);
    }

    /**
     * Select a property row by identifier (ID, name, or any visible text).
     * Tries exact td match first (confirmed by BlockDateTest), then broad element scan.
     */
    public void selectPropertyByName(String identifier) {
        By[] locators = {
            By.xpath("//td[normalize-space()='" + identifier + "']"),
            By.xpath("//tr[.//td[contains(normalize-space(),'" + identifier + "')]][1]"),
            By.xpath("//td[contains(normalize-space(),'" + identifier + "')][1]"),
            By.xpath("//div[contains(text(),'" + identifier + "')]"),
            By.xpath("//span[contains(text(),'" + identifier + "')]")
        };
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (By loc : locators) {
            for (WebElement e : driver.findElements(loc)) {
                try {
                    if (e.isDisplayed() && e.isEnabled()) {
                        js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", e);
                        pause(300);
                        try { e.click(); } catch (Exception ex) { js.executeScript("arguments[0].click();", e); }
                        waitForPageToLoad();
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }
        // Broad fallback — scan every visible element containing the identifier
        for (WebElement e : driver.findElements(By.xpath("//*[contains(normalize-space(),'" + identifier + "')]"))) {
            try {
                if (e.isDisplayed()) {
                    js.executeScript("arguments[0].click();", e);
                    waitForPageToLoad();
                    return;
                }
            } catch (Exception ignored) {}
        }
        throw new RuntimeException("Property '" + identifier + "' not found. URL: " + driver.getCurrentUrl());
    }

    /**
     * Click on the Calendar & Pricing tab
     */
    public void clickPricingTab() {
        click(PRICING_TAB);
        pause(500);
    }

    /**
     * Get all date cells in the calendar
     */
    public List<WebElement> getDateCells() {
        return driver.findElements(DATE_CELL);
    }

    /**
     * Click on a specific date cell by date string (e.g., "2026-12-25").
     * Uses DOM presence + scroll — mirrors BlockDateTest.scrollToDateAndClick() so future-month
     * cells that are in the DOM but off-screen are scrolled into view before clicking.
     */
    public void clickDateCell(String dateString) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        By primary = By.cssSelector("td[data-date='" + dateString + "']");
        try {
            WebElement cell = wait.until(d -> {
                List<WebElement> cells = d.findElements(primary);
                return cells.isEmpty() ? null : cells.get(0);
            });
            js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", cell);
            pause(600);
            try { cell.click(); } catch (Exception e) { js.executeScript("arguments[0].click();", cell); }
            System.out.println("Clicked date cell: " + dateString);
        } catch (Exception e) {
            By fallback = By.xpath("//div[@data-date='" + dateString + "']");
            try {
                click(fallback);
            } catch (Exception ex) {
                throw new RuntimeException("Date cell not found for: " + dateString + ". URL: " + driver.getCurrentUrl());
            }
        }
        pause(500);
    }

    /**
     * Click the "Change Price" button that appears after selecting a date cell.
     * This opens the price edit panel/modal.
     */
    public void clickUpdatePriceButton() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        pause(1200);

        // First pass: try standard locators without strict display check
        for (WebElement e : driver.findElements(UPDATE_PRICE_BUTTON)) {
            try {
                js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", e);
                pause(300);
                try { e.click(); } catch (Exception ex) { js.executeScript("arguments[0].click();", e); }
                System.out.println("Clicked Change Price button");
                pause(800);
                return;
            } catch (Exception ignored) {}
        }

        // Second pass: JS text-search for any element whose visible text is "Change Price"
        try {
            Object result = js.executeScript(
                "var all = document.querySelectorAll('button, a, span, div, p, li');" +
                "for(var i=0;i<all.length;i++){" +
                "  var t=all[i].innerText ? all[i].innerText.trim() : '';" +
                "  if(t==='Change Price'||t==='Change price'){" +
                "    all[i].click(); return 'clicked:'+t;" +
                "  }" +
                "}" +
                "return 'not_found';");
            if (result != null && result.toString().startsWith("clicked")) {
                System.out.println("Clicked Change Price button via JS text search: " + result);
                pause(800);
                return;
            }
        } catch (Exception ignored) {}

        System.out.println("WARNING: 'Change Price' button not found — attempting direct price input");
    }

    /**
     * Enter price for a date.
     * Tries multiple locator strategies (placeholder, aria-label, name, id, type=number)
     * and uses the React native value setter so all characters are typed atomically.
     */
    public void enterPrice(String price) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        By[] locators = {
            // Ant Design InputNumber (uses type="text", class contains ant-input-number-input)
            By.cssSelector("input.ant-input-number-input"),
            By.cssSelector("input[class*='ant-input-number']"),
            By.cssSelector(".ant-input-number input"),
            // Standard number inputs
            By.cssSelector("input[type='number']"),
            // Label/placeholder/attribute based
            By.xpath("//input[contains(translate(@placeholder,'PRICE','price'),'price')]"),
            By.xpath("//input[contains(translate(@aria-label,'PRICE','price'),'price')]"),
            By.xpath("//input[contains(translate(@name,'PRICE','price'),'price')]"),
            By.xpath("//input[contains(translate(@id,'PRICE','price'),'price')]"),
            By.xpath("//*[contains(translate(normalize-space(),'PRICE','price'),'price')]//following::input[1]"),
            By.xpath("//label[contains(translate(.,'PRICE','price'),'price')]/..//input")
        };
        // First pass: strict check (displayed & enabled)
        for (By loc : locators) {
            for (WebElement inp : driver.findElements(loc)) {
                try {
                    if (inp.isDisplayed() && inp.isEnabled()) {
                        js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", inp);
                        pause(300);
                        setInputValueWithFallback(inp, price, js);
                        System.out.println("Price entered: " + price);
                        pause(200);
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }

        // Second pass: lenient check — try to type into any matching input even if display state is uncertain
        for (By loc : locators) {
            for (WebElement inp : driver.findElements(loc)) {
                try {
                    js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", inp);
                    pause(200);
                    setInputValueWithFallback(inp, price, js);
                    String val = inp.getAttribute("value");
                    if (val != null && val.contains(price)) {
                        System.out.println("Price entered (lenient pass): " + price);
                        pause(200);
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }

        logVisibleInputs("PRICE INPUT NOT FOUND");
        throw new RuntimeException("Price input not found. URL: " + driver.getCurrentUrl());
    }

    /**
     * Enter discount for a date if available.
     * Silent skip when no discount field exists on the page.
     */
    public void enterDiscount(String discountPercentage) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        By[] locators = {
            By.xpath("//input[contains(translate(@placeholder,'DISCOUNT','discount'),'discount')]"),
            By.xpath("//input[contains(translate(@aria-label,'DISCOUNT','discount'),'discount')]"),
            By.xpath("//input[contains(translate(@name,'DISCOUNT','discount'),'discount')]"),
            By.xpath("//input[contains(translate(@id,'DISCOUNT','discount'),'discount')]"),
            By.xpath("//*[contains(translate(normalize-space(),'DISCOUNT','discount'),'discount')]//following::input[1]"),
            By.xpath("//label[contains(translate(.,'DISCOUNT','discount'),'discount')]/..//input")
        };
        for (By loc : locators) {
            for (WebElement inp : driver.findElements(loc)) {
                try {
                    if (inp.isDisplayed() && inp.isEnabled()) {
                        js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", inp);
                        pause(300);
                        setInputValueWithFallback(inp, discountPercentage, js);
                        System.out.println("Discount entered: " + discountPercentage);
                        pause(200);
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }
        System.out.println("Discount input not available for this property");
    }

    /**
     * Sets an input value via sendKeys first; falls back to React native value setter
     * so all characters are written atomically (avoids React SPA single-char drops).
     */
    private void setInputValueWithFallback(WebElement input, String value, JavascriptExecutor js) {
        try { input.click(); } catch (Exception ignored) {}
        try { input.clear(); } catch (Exception ignored) {}
        try {
            input.sendKeys(value);
            if (value.equals(input.getAttribute("value"))) return;
        } catch (Exception ignored) {}
        js.executeScript(
            "var s = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
            "s.call(arguments[0], arguments[1]);" +
            "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));" +
            "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
            input, value);
        System.out.println("Value set via JS setter: " + js.executeScript("return arguments[0].value;", input));
    }

    /**
     * Prints all visible inputs to stdout for debugging when a locator fails.
     */
    private void logVisibleInputs(String context) {
        System.out.println("=== " + context + " — visible inputs on page ===");
        for (WebElement inp : driver.findElements(By.tagName("input"))) {
            try {
                if (inp.isDisplayed()) {
                    System.out.println("  type=" + inp.getAttribute("type")
                        + " name=" + inp.getAttribute("name")
                        + " id=" + inp.getAttribute("id")
                        + " placeholder=" + inp.getAttribute("placeholder")
                        + " aria-label=" + inp.getAttribute("aria-label")
                        + " class=" + inp.getAttribute("class"));
                }
            } catch (Exception ignored) {}
        }
    }

    /**
     * Click Save/Update button to save pricing changes
     */
    public void clickSaveButton() {
        click(SAVE_BUTTON);
        pause(500);
    }

    /**
     * Update pricing for a specific date
     */
    public void updatePricingForDate(String dateString, String price, String discount) {
        clickDateCell(dateString);
        clickUpdatePriceButton();
        enterPrice(price);
        if (discount != null && !discount.isEmpty()) {
            enterDiscount(discount);
        }
        clickSaveButton();
    }

    /**
     * Re-clicks the date cell and reads the price displayed in the right panel.
     * Used to verify the saved price after an update.
     * Returns the raw text e.g. "₹7,000".
     */
    public String getPriceDisplayedForDate(String dateString) {
        clickDateCell(dateString);
        pause(1000);

        // The right panel shows "Pricing   ₹X,XXX" — find the ₹ value near "Pricing" heading
        By[] locators = {
            By.xpath("//*[normalize-space()='Pricing']/following-sibling::*[contains(text(),'₹')]"),
            By.xpath("//*[normalize-space()='Pricing']/following-sibling::*[contains(text(),'Rs')]"),
            By.xpath("//*[contains(@class,'pricing') or contains(@class,'Pricing')]//*[contains(text(),'₹')]"),
            By.xpath("//*[contains(text(),'₹') and string-length(normalize-space()) < 15]")
        };

        for (By loc : locators) {
            for (WebElement e : driver.findElements(loc)) {
                try {
                    if (e.isDisplayed()) {
                        String text = e.getText().trim();
                        if (!text.isEmpty() && (text.contains("₹") || text.contains("Rs"))) {
                            System.out.println("Price displayed on panel: " + text);
                            return text;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        System.out.println("WARNING: Could not read price from panel. Dumping visible text elements:");
        for (WebElement e : driver.findElements(By.xpath("//*[contains(text(),'₹')]"))) {
            try {
                if (e.isDisplayed()) System.out.println("  " + e.getText().trim());
            } catch (Exception ignored) {}
        }
        return "";
    }

    /**
     * Wait for page to load
     */
    private void waitForPageToLoad() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Generic pause utility
     */
    protected void pause(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
