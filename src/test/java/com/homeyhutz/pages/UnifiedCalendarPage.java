package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.List;

public class UnifiedCalendarPage extends BasePage {

    public UnifiedCalendarPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    public void clickCalendarNav() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (By loc : new By[]{
            By.xpath("//a[normalize-space()='Calendar']"),
            By.xpath("//span[normalize-space()='Calendar']/ancestor::a[1]"),
            By.xpath("//*[normalize-space()='Calendar' and (self::a or self::div or self::span)]")
        }) {
            for (WebElement e : driver.findElements(loc)) {
                try {
                    if (e.isDisplayed() && e.isEnabled()) {
                        js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", e);
                        try { e.click(); } catch (Exception ex) { js.executeScript("arguments[0].click();", e); }
                        pause(2000);
                        System.out.println("Clicked Calendar nav");
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }
        throw new RuntimeException("Calendar nav link not found. URL: " + driver.getCurrentUrl());
    }

    // ── Property search ──────────────────────────────────────────────────────

    /**
     * Types the property ID or name into the Unified Calendar search bar
     * and selects the first matching result from the dropdown.
     */
    public void searchAndSelectProperty(String propertyId, String propertyName) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Find the search input
        WebElement searchBox = null;
        for (By loc : new By[]{
            By.cssSelector("input[placeholder='Search by Property Name, Id, City']"),
            By.cssSelector("input[placeholder*='Search by Property']"),
            By.cssSelector("input[placeholder*='Search']")
        }) {
            searchBox = findVisible(loc);
            if (searchBox != null) break;
        }
        if (searchBox == null) {
            System.out.println("WARNING: Unified Calendar search box not found — skipping property search");
            return;
        }

        // Type property ID using React value setter (all chars at once)
        reactType(searchBox, propertyId, js);
        pause(1500);
        System.out.println("Unified Calendar: typed property ID " + propertyId);

        // Click the first matching dropdown result — the navigation to the property's calendar
        // page (/listing/properties/<id>?tab=calendar) IS the intended destination.
        for (By loc : new By[]{
            By.cssSelector(".flex.items-center.p-2.border-b.cursor-pointer"),
            By.xpath("//div[contains(@class,'cursor-pointer') and contains(normalize-space(),'" + propertyId + "')][1]"),
            By.xpath("//div[contains(@class,'cursor-pointer') and contains(normalize-space(),'" + propertyName + "')][1]"),
            By.xpath("//*[contains(normalize-space(),'" + propertyId + "') and (self::li or self::div or self::span)][1]")
        }) {
            WebElement item = findVisible(loc);
            if (item != null) {
                try { item.click(); } catch (Exception e) { js.executeScript("arguments[0].click();", item); }
                pause(2000);
                System.out.println("Unified Calendar: selected property " + propertyId + " → URL: " + driver.getCurrentUrl());
                return;
            }
        }
        System.out.println("Unified Calendar: dropdown item not found — continuing with typed search");
    }

    /**
     * Types a property ID/name into the search bar without clicking the dropdown result.
     * Use when the dropdown navigates away from the Unified Calendar page.
     */
    public void typePropertySearch(String query) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement searchBox = null;
        for (By loc : new By[]{
            By.cssSelector("input[placeholder='Search by Property Name, Id, City']"),
            By.cssSelector("input[placeholder*='Search by Property']"),
            By.cssSelector("input[placeholder*='Search']")
        }) {
            searchBox = findVisible(loc);
            if (searchBox != null) break;
        }
        if (searchBox == null) {
            System.out.println("WARNING: Unified Calendar search box not found — skipping");
            return;
        }
        reactType(searchBox, query, js);
        pause(1500);
        System.out.println("Unified Calendar: typed search query: " + query);
    }

    // ── Date picker ─────────────────────────────────────────────────────────

    /**
     * Navigates the unified calendar to the target date.
     * Tries two strategies in order:
     *   1. Focus the Ant DatePicker input directly via JS (no container click — avoids tab-switch side effect)
     *      and type the date.
     *   2. Append/replace the `currentDate` URL parameter (same effect as using the date picker).
     *
     * @param antDate  display format accepted by the date picker: "25 Dec 26"
     * @param isoDate  ISO format for URL fallback: "2026-12-25"
     */
    public void navigateToTargetDate(String antDate, String isoDate) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Strategy 1: find the date input directly and focus via JS (no click on the container
        // because clicking .ant-picker on this page can accidentally activate the Calendar & Pricing tab)
        WebElement dateInput = null;
        for (By loc : new By[]{
            By.cssSelector("div.ant-picker-input input"),
            By.cssSelector("input[placeholder='Select date']"),
            By.cssSelector("input.ant-picker-input"),
            By.cssSelector(".ant-picker input")
        }) {
            for (WebElement e : driver.findElements(loc)) {
                try {
                    // Accept visible OR off-screen inputs (Ant pickers are sometimes opacity:0)
                    String display = e.getCssValue("display");
                    if (!"none".equals(display)) { dateInput = e; break; }
                } catch (Exception ignored) {}
            }
            if (dateInput != null) break;
        }

        if (dateInput != null) {
            js.executeScript("arguments[0].focus();", dateInput);
            pause(500);
            reactType(dateInput, antDate, js);
            dateInput.sendKeys(Keys.ENTER);
            pause(2500);
            System.out.println("Unified calendar: navigated to " + antDate + " via date picker input");
            return;
        }

        // Strategy 2: append/replace currentDate in the URL — same result as selecting from the picker
        System.out.println("Unified calendar: date input not interactable — navigating via URL currentDate=" + isoDate);
        String currentUrl = driver.getCurrentUrl();
        String sep = currentUrl.contains("?") ? "&" : "?";
        String newUrl = currentUrl.contains("currentDate")
            ? currentUrl.replaceAll("currentDate=[^&]*", "currentDate=" + isoDate)
            : currentUrl + sep + "currentDate=" + isoDate;
        driver.get(newUrl);
        pause(2500);
        System.out.println("Unified calendar: navigated to " + isoDate + " via URL parameter");
    }

    // ── Date cell interaction ────────────────────────────────────────────────

    /**
     * Clicks the calendar cell for the given date.
     * Tries td[data-date] first (table-based calendar), then .uf_cal_date_box matching the day number.
     */
    public void clickDateCell(String isoDate, String dayNum) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Try td[data-date] (same pattern as admin property calendar)
        List<WebElement> dateCells = driver.findElements(By.cssSelector("td[data-date='" + isoDate + "']"));
        if (!dateCells.isEmpty()) {
            js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", dateCells.get(0));
            pause(500);
            try { dateCells.get(0).click(); }
            catch (Exception e) { js.executeScript("arguments[0].click();", dateCells.get(0)); }
            pause(1500);
            System.out.println("Unified Calendar: clicked td[data-date='" + isoDate + "']");
            return;
        }

        // Fallback: .uf_cal_date_box whose first line matches dayNum
        for (WebElement box : driver.findElements(By.cssSelector(".uf_cal_date_box"))) {
            try {
                if (!box.isDisplayed()) continue;
                String[] lines = box.getText().trim().split("\\n");
                if (lines.length > 0 && lines[0].trim().equals(dayNum)) {
                    js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", box);
                    pause(500);
                    try { box.click(); } catch (Exception e) { js.executeScript("arguments[0].click();", box); }
                    pause(1500);
                    System.out.println("Unified Calendar: clicked .uf_cal_date_box for day " + dayNum);
                    return;
                }
            } catch (Exception ignored) {}
        }
        System.out.println("WARNING: Unified Calendar date cell not found for " + isoDate + " — continuing");
    }

    /**
     * Polls the page for the expected price.
     * Checks body text, abbreviated grid format (₹2k for 2000), and input field .value attrs.
     * Asserts with TestNG if not found within the retry window.
     */
    public void verifyPriceWithRetry(int expectedPrice) {
        String priceStr = String.valueOf(expectedPrice);
        String priceFmt = String.format("%,d", expectedPrice);
        // Compact form used in the calendar grid: 2000 → "₹2k", 9000 → "₹9k"
        String abbrevK  = (expectedPrice % 1000 == 0)
            ? "₹" + (expectedPrice / 1000) + "k" : "";
        JavascriptExecutor js = (JavascriptExecutor) driver;

        boolean found = false;
        for (int i = 1; i <= 6 && !found; i++) {
            System.out.println("🔄 Unified Calendar sync check [" + i + "/6] — looking for: ₹" + priceFmt
                + (abbrevK.isEmpty() ? "" : " or " + abbrevK));
            String body = driver.findElement(By.tagName("body")).getText();
            if (body.contains(priceFmt) || body.contains(priceStr)
                    || (!abbrevK.isEmpty() && body.contains(abbrevK))) {
                System.out.println("✅ Unified Calendar price found in body text");
                found = true;
                break;
            }
            try {
                Object res = js.executeScript(
                    "return Array.from(document.querySelectorAll('input')).map(i=>i.value).filter(v=>v).join('|');");
                if (res != null && (res.toString().contains(priceFmt) || res.toString().contains(priceStr))) {
                    System.out.println("✅ Unified Calendar price found in input field values");
                    found = true;
                    break;
                }
            } catch (Exception ignored) {}
            if (!found) {
                try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
        org.testng.Assert.assertTrue(found,
            "❌ FAIL: Price ₹" + priceFmt + " (or " + abbrevK + ") NOT found in Unified Calendar");
        System.out.println("✅ Unified Calendar price ₹" + priceFmt + " verified");
    }

    // ── Price reading ────────────────────────────────────────────────────────

    /**
     * Scrolls to the calendar grid cell for isoDate and reads the price displayed
     * directly inside the cell (the grid shows prices like "₹2,000" in each date cell).
     * Does NOT click — just reads what is already rendered.
     * Returns the raw price text, or empty string if not found.
     */
    public String readPriceFromCell(String isoDate) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        pause(1000);

        List<WebElement> cells = driver.findElements(By.cssSelector("td[data-date='" + isoDate + "']"));
        if (cells.isEmpty()) {
            System.out.println("Unified Calendar: td[data-date='" + isoDate + "'] not found in DOM");
            return "";
        }
        WebElement cell = cells.get(0);
        js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", cell);
        pause(800);

        // getText() often returns empty for Ant Design table cells — use JS to collect
        // all visible child text nodes (day number + price abbreviated, e.g. "25\n₹2k")
        String cellText = "";
        try { cellText = cell.getText().trim(); } catch (Exception ignored) {}

        if (cellText.isEmpty()) {
            try {
                Object inner = js.executeScript(
                    "var c=arguments[0];" +
                    "var parts=[];" +
                    "c.querySelectorAll('*').forEach(function(el){" +
                    "  var t=(el.innerText||el.textContent||'').trim();" +
                    "  if(t && el.children.length===0) parts.push(t);" +
                    "});" +
                    "return parts.join('\\n');",
                    cell);
                if (inner != null) cellText = inner.toString().trim();
            } catch (Exception ignored) {}
        }
        System.out.println("Unified Calendar cell [" + isoDate + "] text: \"" + cellText + "\"");

        // Extract the price line: look for ₹, Rs, or a standalone number
        for (String line : cellText.split("\\n")) {
            String l = line.trim();
            if (l.contains("₹") || l.contains("Rs") || l.matches("^[\\d,]+$")) {
                System.out.println("Unified Calendar cell price: " + l);
                return l;
            }
        }
        return cellText; // return full text if no specific price line found
    }

    /**
     * Reads the price displayed inside the .uf_cal_date_box for the given day number (e.g. "25").
     * Returns the raw price text such as "₹7,000" or "7k".
     */
    public String getPriceForDay(String dayNum) {
        pause(1000);
        for (WebElement box : driver.findElements(By.cssSelector(".uf_cal_date_box"))) {
            try {
                if (!box.isDisplayed()) continue;
                String text = box.getText().trim();
                String[] lines = text.split("\\n");
                if (lines.length > 0 && lines[0].trim().equals(dayNum)) {
                    for (String line : lines) {
                        String l = line.trim();
                        if (!l.equals(dayNum) && (l.contains("₹") || l.contains("Rs") || l.matches("\\d.*"))) {
                            System.out.println("Unified calendar day " + dayNum + " price text: " + l);
                            return l;
                        }
                    }
                    System.out.println("Unified calendar day " + dayNum + " full text: " + text);
                    return text;
                }
            } catch (Exception ignored) {}
        }

        // Fallback: check body text for the price near the date context
        System.out.println("WARNING: .uf_cal_date_box for day " + dayNum + " not matched. Dumping all boxes:");
        for (WebElement box : driver.findElements(By.cssSelector(".uf_cal_date_box"))) {
            try { if (box.isDisplayed()) System.out.println("  box: " + box.getText().trim()); }
            catch (Exception ignored) {}
        }
        return "";
    }

    /**
     * Returns page body text — fallback when specific cell locators fail.
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

    /**
     * Types using sendKeys first; falls back to React native value setter so all
     * characters are written atomically (avoids single-char drop on React re-render).
     */
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
