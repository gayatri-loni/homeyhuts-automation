package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ChannelManagerPage extends BasePage {

    public ChannelManagerPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    public void clickChannelManagerNav() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (By loc : new By[]{
            By.xpath("//*[normalize-space()='Channel Manager' and (self::a or self::span or self::div)]"),
            By.xpath("//a[contains(normalize-space(),'Channel Manager')]"),
            By.xpath("//span[contains(normalize-space(),'Channel Manager')]/ancestor::a[1]")
        }) {
            for (WebElement e : driver.findElements(loc)) {
                try {
                    if (e.isDisplayed() && e.isEnabled()) {
                        js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", e);
                        try { e.click(); } catch (Exception ex) { js.executeScript("arguments[0].click();", e); }
                        pause(2000);
                        System.out.println("Clicked Channel Manager nav");
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }
        throw new RuntimeException("Channel Manager nav not found. URL: " + driver.getCurrentUrl());
    }

    /**
     * Searches for a property by name in the left sidebar "Search by Property Name" input
     * and clicks the first matching result.
     */
    public void searchAndSelectProperty(String propertyName, String propertyId) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        WebElement searchBox = null;
        for (By loc : new By[]{
            By.cssSelector("input[placeholder='Search by Property Name']"),
            By.cssSelector("input[placeholder*='Search by Property']"),
            By.cssSelector("input[placeholder*='Property Name']")
        }) {
            searchBox = findVisible(loc);
            if (searchBox != null) break;
        }
        if (searchBox == null) throw new RuntimeException(
            "Channel Manager search box not found. URL: " + driver.getCurrentUrl());

        reactType(searchBox, propertyName, js);
        pause(1500);
        System.out.println("Channel Manager: typed property name: " + propertyName);

        // Click the first visible result from the list
        for (By loc : new By[]{
            By.cssSelector(".flex.items-center.p-2.border-b.cursor-pointer"),
            By.xpath("//div[contains(@class,'cursor-pointer') and contains(normalize-space(),'" + propertyName + "')][1]"),
            By.xpath("//div[contains(@class,'cursor-pointer') and contains(normalize-space(),'" + propertyId + "')][1]"),
            By.xpath("//*[contains(normalize-space(),'" + propertyName + "') and (self::li or self::div or self::span)][1]")
        }) {
            WebElement item = findVisible(loc);
            if (item != null) {
                try { item.click(); } catch (Exception e) { js.executeScript("arguments[0].click();", item); }
                pause(1500);
                System.out.println("Channel Manager: selected property " + propertyName);
                return;
            }
        }
        throw new RuntimeException(
            "Channel Manager property result not found for: " + propertyName + ". URL: " + driver.getCurrentUrl());
    }

    // ── Date picker ─────────────────────────────────────────────────────────

    /**
     * Selects a date using the Ant Design date picker.
     * Use 2-digit year: "25 Dec 26" (confirmed by BlockDateTest).
     */
    public void navigateToDate(String antDate) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement dateInput = null;
        for (By loc : new By[]{
            By.cssSelector(".ant-picker-input input[placeholder='Select date']"),
            By.cssSelector("input[placeholder='Select date']"),
            By.cssSelector("input.ant-picker-input"),
            By.cssSelector(".ant-picker input")
        }) {
            dateInput = findVisible(loc);
            if (dateInput != null) break;
        }
        if (dateInput == null) throw new RuntimeException("Channel Manager date input not found. URL: " + driver.getCurrentUrl());

        dateInput.click();
        pause(500);
        reactType(dateInput, antDate, js);
        dateInput.sendKeys(Keys.ENTER);
        pause(2000);
        System.out.println("Channel Manager date set: " + antDate);
    }

    // ── Price reading ────────────────────────────────────────────────────────

    /**
     * Reads the rate/price value displayed after selecting a date.
     * Tries Ant Design number inputs (rate field before inventory field).
     * Falls back to body text search.
     */
    public String getPriceDisplayed() {
        pause(1000);

        // Try rate-specific inputs first (id contains 'rate' or 'price')
        for (By loc : new By[]{
            By.cssSelector("input[id*='rate_'][type='number']"),
            By.cssSelector("input[id*='price_'][type='number']"),
            By.cssSelector("input[id*='Rate'][type='number']")
        }) {
            for (WebElement inp : driver.findElements(loc)) {
                try {
                    if (inp.isDisplayed()) {
                        String val = inp.getAttribute("value");
                        if (val != null && !val.isEmpty()) {
                            System.out.println("Channel Manager rate input value: " + val);
                            return val;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // Try all visible Ant Design number inputs — skip zero (inventory) values
        for (WebElement inp : driver.findElements(By.cssSelector("input.ant-input-number-input[type='number'], input[type='number']"))) {
            try {
                if (!inp.isDisplayed()) continue;
                String val = inp.getAttribute("value");
                if (val != null && !val.isEmpty() && !val.equals("0") && !val.equals("1")) {
                    System.out.println("Channel Manager number input value: " + val);
                    return val;
                }
            } catch (Exception ignored) {}
        }

        System.out.println("Channel Manager: no specific price input found — returning body text for assertion");
        return driver.findElement(By.tagName("body")).getText();
    }

    /**
     * Returns page body text — used for retry-based price assertion.
     */
    public String getPageBodyText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private WebElement findVisible(By loc) {
        for (WebElement e : driver.findElements(loc)) {
            try { if (e.isDisplayed()) return e; } catch (Exception ignored) {}
        }
        return null;
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

    protected void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
