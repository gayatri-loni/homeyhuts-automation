package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

public class GuestPropertyPage extends BasePage {

    public GuestPropertyPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    /**
     * Loads the guest-facing property page with check-in/check-out dates pre-set in the URL.
     * Pattern confirmed by BlockDateTest.verifyBlockedDateInGuestFacingCalendar.
     */
    public void openPropertyWithDates(String guestBaseUrl, String roomId,
                                       String checkInDate, String checkOutDate) {
        String url = guestBaseUrl + "/rooms/" + roomId
            + "?checkIn=" + checkInDate
            + "&checkOut=" + checkOutDate
            + "&adults=1&children=0&infants=0&pets=0";
        driver.get(url);
        pause(3000);
        System.out.println("Loaded guest property page: " + url);
    }

    // ── Price reading ─────────────────────────────────────────────────────────

    /**
     * Reads the per-night base price shown on the property page (e.g. "₹7,000/night").
     */
    public String getDisplayedNightlyPrice() {
        for (By loc : new By[]{
            By.xpath("//*[contains(text(),'₹') and (contains(text(),'/night') or contains(text(),'per night'))]"),
            By.xpath("//*[contains(@class,'price') and contains(text(),'₹')]"),
            By.cssSelector("[class*='price'][class*='night']"),
            By.cssSelector("[class*='perNight']")
        }) {
            for (WebElement e : driver.findElements(loc)) {
                try {
                    if (e.isDisplayed()) {
                        String t = e.getText().trim();
                        if (!t.isEmpty()) {
                            System.out.println("Guest nightly price: " + t);
                            return t;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        return "";
    }

    /**
     * Reads the final total price shown in the booking summary / price breakdown widget.
     */
    public String getDisplayedTotalPrice() {
        for (By loc : new By[]{
            By.xpath("//*[contains(translate(normalize-space(),'TOTAL','total'),'total')]/following-sibling::*[contains(text(),'₹')][1]"),
            By.xpath("//*[contains(translate(normalize-space(),'TOTAL','total'),'total') and contains(text(),'₹')]"),
            By.xpath("//div[contains(@class,'total') or contains(@class,'Total')]//*[contains(text(),'₹')]"),
            By.xpath("//div[contains(@class,'booking') or contains(@class,'summary')]//*[contains(text(),'₹')][last()]")
        }) {
            for (WebElement e : driver.findElements(loc)) {
                try {
                    if (e.isDisplayed()) {
                        String t = e.getText().trim();
                        if (!t.isEmpty()) {
                            System.out.println("Guest total price: " + t);
                            return t;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        return "";
    }

    // ── Price verification with retry ─────────────────────────────────────────

    /**
     * Retries page reloads until the formatted price appears in the page body.
     * Handles async CDN/cache propagation delays.
     */
    public boolean waitForPriceVisible(int price, int maxRetries, int waitMs) {
        String formatted = formatIndian(price);
        for (int i = 1; i <= maxRetries; i++) {
            System.out.println("Guest price check [" + i + "/" + maxRetries + "] looking for: ₹" + formatted);
            String body = driver.findElement(By.tagName("body")).getText();
            if (body.contains(formatted) || body.contains(String.valueOf(price))) {
                System.out.println("✅ Guest page shows price: ₹" + formatted);
                return true;
            }
            pause(waitMs);
            driver.navigate().refresh();
            pause(2000);
        }
        System.out.println("❌ Guest page did not show ₹" + formatted + " after " + maxRetries + " attempts");
        return false;
    }

    /**
     * Checks if both the base price and the GST-inclusive final price are visible on the page.
     * Logs the full price breakdown for debugging.
     */
    public void assertPriceBreakdown(int basePrice, int finalPrice) {
        String baseFormatted  = formatIndian(basePrice);
        String finalFormatted = formatIndian(finalPrice);
        String body = driver.findElement(By.tagName("body")).getText();

        boolean baseFound  = body.contains(baseFormatted)  || body.contains(String.valueOf(basePrice));
        boolean finalFound = body.contains(finalFormatted) || body.contains(String.valueOf(finalPrice));

        System.out.println("--- Guest Price Breakdown Assertion ---");
        System.out.println("  Base  ₹" + baseFormatted  + " : " + (baseFound  ? "FOUND ✅" : "NOT FOUND ❌"));
        System.out.println("  Final ₹" + finalFormatted + " : " + (finalFound ? "FOUND ✅" : "NOT FOUND ❌"));

        if (!baseFound && !finalFound) {
            // Dump visible ₹ elements for debugging
            System.out.println("  Visible ₹ elements on page:");
            for (WebElement e : driver.findElements(By.xpath("//*[contains(text(),'₹')]"))) {
                try {
                    if (e.isDisplayed()) System.out.println("    " + e.getText().trim());
                } catch (Exception ignored) {}
            }
        }

        org.testng.Assert.assertTrue(baseFound || finalFound,
            "Guest page should show base price ₹" + baseFormatted
            + " or final price ₹" + finalFormatted
            + ". URL: " + driver.getCurrentUrl());
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    public static String formatIndian(int amount) {
        return String.format("%,d", amount);
    }

    /**
     * Strips ₹, Rs, commas, spaces and handles "k" suffix → returns numeric value.
     * e.g. "₹7,000" → 7000,  "7k" → 7000,  "7,350" → 7350
     */
    public static int parsePriceValue(String text) {
        if (text == null || text.isEmpty()) return -1;
        String cleaned = text.replaceAll("[₹Rs,\\s]", "").trim();
        if (cleaned.toLowerCase().endsWith("k")) {
            try {
                return (int)(Double.parseDouble(cleaned.substring(0, cleaned.length() - 1)) * 1000);
            } catch (NumberFormatException e) { return -1; }
        }
        try {
            return (int) Double.parseDouble(cleaned);
        } catch (NumberFormatException e) { return -1; }
    }

    protected void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
