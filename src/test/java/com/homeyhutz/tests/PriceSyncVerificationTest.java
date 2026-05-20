package com.homeyhutz.tests;

import com.homeyhutz.base.BaseTest;
import com.homeyhutz.constants.TestData;
import com.homeyhutz.pages.*;
import com.homeyhutz.utils.GSTCalculator;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

/**
 * Verifies that when a host updates the property price from the Host Calendar,
 * the change propagates correctly to all 6 downstream systems:
 *
 *   1. Unified Calendar  (host portal)
 *   2. Channel Manager   (host portal)
 *   3. Guest-facing property page  (with GST validation)
 *   4. Admin Property Calendar
 *   5. Admin Calendar & Pricing
 *   6. Admin Inventory & Rates
 */
public class PriceSyncVerificationTest extends BaseTest {

    // ── URLs ─────────────────────────────────────────────────────────────────
    private static final String GUEST_BASE_URL  = "https://uat.homeyhutz.com";
    private static final String SIGNIN_URL      = GUEST_BASE_URL + "/signup-signin?redirectUrl=/&";
    private static final String HOST_PORTAL_URL = "https://uat.host.homeyhutz.com";
    private static final String ADMIN_BASE_URL  = "https://uat.admin.new.homeyhutz.com";
    private static final String ADMIN_LOGIN_URL = ADMIN_BASE_URL + "/login";

    // ── Credentials ──────────────────────────────────────────────────────────
    private static final String HOST_PHONE      = "9422121212";
    private static final String HOST_OTP        = "123456";
    private static final String ADMIN_EMAIL     = "homey@admin.com";
    private static final String ADMIN_PASSWORD  = "Password1!";

    // ── Test data (from TestData constants) ──────────────────────────────────
    private static final String PROPERTY_ID   = String.valueOf(TestData.PROPERTY_ID);   // "5012"
    private static final String PROPERTY_NAME = TestData.PROPERTY_NAME;                 // "test House"
    private static final int    UPDATED_PRICE = TestData.SYNC_UPDATED_PRICE;            // 7000
    private static final String SYNC_DATE     = TestData.PRICE_SYNC_DATE;               // "2026-12-25"
    private static final String SYNC_DATE_ANT = TestData.PRICE_SYNC_DATE_ANT;           // "25 Dec 26"

    // GST: 7000 < 7500 → 5% → final = 7350
    private static final int GST_AMOUNT    = GSTCalculator.calculateGST(UPDATED_PRICE, 0);
    private static final int FINAL_PRICE   = GSTCalculator.calculateFinalPrice(UPDATED_PRICE, 0);

    // ── Main test ─────────────────────────────────────────────────────────────

    @Test
    public void verifyPriceSyncAcrossAllSystems() {

        GSTCalculator.logBreakdown(UPDATED_PRICE, 0);

        // ── Phase 1: Host login + price update ───────────────────────────────
        log("Phase 1: Host login and price update");
        loginToHostPortal();
        updatePriceOnHostCalendar();
        log("✓ Price updated to " + UPDATED_PRICE + " for " + SYNC_DATE);

        // ── Phase 2: Unified Calendar ────────────────────────────────────────
        log("Phase 2: Unified Calendar verification");
        verifyUnifiedCalendar();

        // ── Phase 3: Channel Manager ─────────────────────────────────────────
        log("Phase 3: Channel Manager verification");
        verifyChannelManager();

        // ── Phase 4: Guest-facing page (with GST) ────────────────────────────
        log("Phase 4: Guest-facing price verification");
        verifyGuestFacingPrice();

        // ── Phase 5–7: Admin systems ──────────────────────────────────────────
        log("Phase 5-7: Admin system verification");
        loginToAdmin();
        verifyAdminPropertyCalendar();
        verifyAdminCalendarPricing();
        verifyAdminInventoryRates();

        log("====================================================");
        log("✅ ALL 6 SYSTEMS VERIFIED — price " + UPDATED_PRICE + " synced correctly");
        log("====================================================");
    }

    // ── Phase 1: Host login + price update ───────────────────────────────────

    private void loginToHostPortal() {
        HomePage  homePage  = new HomePage(driver, wait);
        LoginPage loginPage = new LoginPage(driver, wait);
        OtpPage   otpPage   = new OtpPage(driver, wait);

        driver.get(SIGNIN_URL);
        waitForDocumentReady();
        pause(1500);

        homePage.openLoginPopup();
        loginPage.waitForLoginPage();
        loginPage.enterPhoneNumber(HOST_PHONE);
        loginPage.clickContinue();

        otpPage.waitForOtpPage();
        otpPage.enterOtp(HOST_OTP);
        otpPage.clickSubmit();
        pause(2000);

        // Wait until sign-in page is left, then navigate to host portal
        try {
            wait.until(d -> {
                String url = d.getCurrentUrl();
                return url != null && !url.contains("signup-signin");
            });
        } catch (TimeoutException ignored) {}
        pause(2000);

        driver.get(HOST_PORTAL_URL);
        waitForDocumentReady();
        pause(2000);

        // Retry once if redirected away
        if (!driver.getCurrentUrl().contains("host.homeyhutz.com")) {
            pause(3000);
            driver.get(HOST_PORTAL_URL);
            waitForDocumentReady();
            pause(1000);
        }

        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.urlContains("host.homeyhutz.com"));
        } catch (TimeoutException e) {
            throw new RuntimeException("Could not reach host portal. URL: " + driver.getCurrentUrl());
        }

        // Verify actual portal content loaded (not a Chrome error page)
        for (int attempt = 0; attempt < 4; attempt++) {
            String bodyText = "";
            try { bodyText = driver.findElement(By.tagName("body")).getText(); } catch (Exception ignored) {}
            boolean errorPage = bodyText.contains("can't be reached") || bodyText.contains("ERR_")
                || bodyText.contains("took too long") || bodyText.trim().isEmpty();
            if (!errorPage) { log("✓ Host portal reached"); return; }
            log("Host portal error page detected (attempt " + (attempt + 1) + ") — retrying in 5s");
            pause(5000);
            driver.navigate().refresh();
            waitForDocumentReady();
            pause(3000);
        }
        throw new RuntimeException("Host portal content never loaded. URL: " + driver.getCurrentUrl());
    }

    private void updatePriceOnHostCalendar() {
        PricingPage pricingPage = new PricingPage(driver, wait);

        pricingPage.clickPropertiesSidebar();
        pause(1000);
        pricingPage.searchProperty(PROPERTY_ID);
        pause(500);
        pricingPage.selectPropertyByName(PROPERTY_ID);
        pause(1000);
        pricingPage.clickPricingTab();
        pause(1000);

        pricingPage.updatePricingForDate(SYNC_DATE, String.valueOf(UPDATED_PRICE), null);
        pause(2000);

        // Re-open the date panel and read the price displayed there
        // (same approach as UpdatePricingTest which is confirmed working)
        String displayed = pricingPage.getPriceDisplayedForDate(SYNC_DATE);
        log("Host calendar price displayed after save: " + displayed);

        boolean confirmed = priceContains(displayed, UPDATED_PRICE);
        if (!confirmed) {
            // Fallback: refresh and re-read panel
            driver.navigate().refresh();
            waitForDocumentReady();
            pause(3000);
            displayed = pricingPage.getPriceDisplayedForDate(SYNC_DATE);
            log("Host calendar price after refresh: " + displayed);
            confirmed = priceContains(displayed, UPDATED_PRICE);
        }
        if (!confirmed) {
            // Last resort: body text search
            confirmed = retryBodyTextCheck(
                driver.findElement(By.tagName("body")).getText(),
                UPDATED_PRICE, 3, 2000, "Host Calendar");
        }

        Assert.assertTrue(confirmed,
            "Host calendar should show ₹" + formatIndian(UPDATED_PRICE) + " for " + SYNC_DATE
            + ". Displayed: " + displayed);
        log("✓ Host calendar confirmed: ₹" + formatIndian(UPDATED_PRICE) + " for " + SYNC_DATE);
    }

    // ── Phase 2: Unified Calendar ─────────────────────────────────────────────

    private void verifyUnifiedCalendar() {
        // Navigate directly to the unified calendar URL with property search + date pre-applied.
        // URL format confirmed from browser: /calendar?search={id}&currentDate={date}
        String calendarUrl = HOST_PORTAL_URL + "/calendar?search=" + PROPERTY_ID + "&currentDate=" + SYNC_DATE;
        driver.get(calendarUrl);
        waitForDocumentReady();
        pause(3000);
        log("Unified Calendar URL: " + driver.getCurrentUrl());

        // The grid shows ₹7,000 in the first column (Fri 25) for property 5012.
        // Check body text for the updated price.
        String bodyText = driver.findElement(By.tagName("body")).getText();
        boolean found = priceContains(bodyText, UPDATED_PRICE);
        if (!found) {
            found = retryBodyTextCheck(bodyText, UPDATED_PRICE, 4, 3000, "Unified Calendar");
        }
        Assert.assertTrue(found,
            "Unified Calendar should show ₹" + formatIndian(UPDATED_PRICE) + " for " + SYNC_DATE);
        log("✓ Unified Calendar verified: ₹" + formatIndian(UPDATED_PRICE));
    }

    // ── Phase 3: Channel Manager ──────────────────────────────────────────────

    private void verifyChannelManager() {
        ChannelManagerPage cmPage = new ChannelManagerPage(driver, wait);

        // Always navigate to portal root so the top nav with Channel Manager is fully rendered
        driver.get(HOST_PORTAL_URL);
        waitForDocumentReady();
        pause(2000);

        cmPage.clickChannelManagerNav();
        pause(1500);

        // Search by property name in the left sidebar and select the property
        cmPage.searchAndSelectProperty(PROPERTY_NAME, PROPERTY_ID);
        pause(1000);

        cmPage.navigateToDate(SYNC_DATE_ANT);

        String priceText = cmPage.getPriceDisplayed();
        log("Channel Manager price text: " + priceText);

        boolean found = priceContains(priceText, UPDATED_PRICE);
        if (!found) {
            found = retryBodyTextCheck(cmPage.getPageBodyText(), UPDATED_PRICE, 4, 3000, "Channel Manager");
        }
        Assert.assertTrue(found,
            "Channel Manager should show ₹" + formatIndian(UPDATED_PRICE) + " for " + SYNC_DATE
            + ". Found: " + priceText);
        log("✓ Channel Manager verified: ₹" + formatIndian(UPDATED_PRICE));
    }

    // ── Phase 4: Guest-facing page ────────────────────────────────────────────

    private void verifyGuestFacingPrice() {
        GuestPropertyPage guestPage = new GuestPropertyPage(driver, wait);

        // Load property page with Dec 25 as check-in, Dec 26 as check-out (1 night)
        guestPage.openPropertyWithDates(
            GUEST_BASE_URL,
            TestData.GUEST_ROOM_ID,
            SYNC_DATE,
            "2026-12-26"
        );
        waitForDocumentReady();
        pause(2000);

        log("GST breakdown — base: " + UPDATED_PRICE
            + ", GST(" + (int)(GSTCalculator.getGSTRate(UPDATED_PRICE) * 100) + "%): " + GST_AMOUNT
            + ", final: " + FINAL_PRICE);

        // Retry with page refresh to handle CDN/cache propagation
        boolean baseVisible = guestPage.waitForPriceVisible(UPDATED_PRICE, TestData.SYNC_RETRY_COUNT, TestData.SYNC_RETRY_WAIT_MS);
        if (!baseVisible) {
            // Also accept the GST-inclusive final price as evidence of correct base
            baseVisible = guestPage.waitForPriceVisible(FINAL_PRICE, 3, 2000);
        }

        // Full breakdown assertion (logs expected vs actual, asserts at least one price visible)
        guestPage.assertPriceBreakdown(UPDATED_PRICE, FINAL_PRICE);

        log("✓ Guest facing verified — base: ₹" + formatIndian(UPDATED_PRICE)
            + ", GST 5%: ₹" + formatIndian(GST_AMOUNT)
            + ", final: ₹" + formatIndian(FINAL_PRICE));
    }

    // ── Phase 5: Admin login ──────────────────────────────────────────────────

    private void loginToAdmin() {
        AdminLoginPage adminLoginPage = new AdminLoginPage(driver);
        adminLoginPage.open(ADMIN_LOGIN_URL);
        adminLoginPage.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        waitForDocumentReady();
        pause(2000);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(d -> !d.getCurrentUrl().contains("/login"));
            log("✓ Admin login successful");
        } catch (TimeoutException e) {
            throw new RuntimeException("Admin login failed. URL: " + driver.getCurrentUrl());
        }
    }

    // ── Phase 5: Admin Property Calendar ─────────────────────────────────────

    private void verifyAdminPropertyCalendar() {
        // Admin: Properties → search 5012 → open property → Calendar & Pricing tab
        // → navigate to Dec 2026 → click Dec 25 → verify price in panel
        // (mirrors the host-side flow exactly)
        clickAdminMenu("Properties");
        pause(1500);

        WebElement searchInput = findAdminSearchInput(
            "input[placeholder='Search by Name, Property Id']",
            "input[placeholder*='Search by Name']",
            "input[placeholder*='Property Id']",
            "input[placeholder*='Search']"
        );
        if (searchInput == null) throw new RuntimeException("Admin Properties search input not found");
        clearAndType(searchInput, PROPERTY_ID);  // "5012"
        pause(1500);

        // Click the property row
        boolean rowClicked = false;
        for (By loc : new By[]{
            By.xpath("//td[contains(@class,'ant-table-cell') and normalize-space()='" + PROPERTY_ID + "']"),
            By.xpath("//td[contains(@class,'ant-table-cell') and contains(normalize-space(),'" + PROPERTY_ID + "')]"),
            By.xpath("//td[contains(@class,'ant-table-cell') and contains(normalize-space(),'" + PROPERTY_NAME + "')]"),
            By.xpath("//*[contains(normalize-space(),'" + PROPERTY_ID + "') and (self::td or self::tr or self::div)]")
        }) {
            WebElement row = tryFindElement(loc, 5);
            if (row != null) { safeClick(row); rowClicked = true; break; }
        }
        if (!rowClicked) throw new RuntimeException("Admin property row not found for " + PROPERTY_ID);
        pause(3000);

        // Calendar & Pricing tab
        clickCalendarAndPricingTab();
        pause(2000);

        // Scroll to Dec 25 — same as host side (td[data-date] is in DOM, just scroll into view)
        WebElement dayCell = scrollToDateCell("2026-12-25");
        if (dayCell != null) {
            safeClick(dayCell);
            pause(1500);
            log("Clicked admin calendar day 2026-12-25");
        } else {
            log("WARNING: Admin calendar day cell not found — checking body text anyway");
        }

        // Poll body text without refreshing — refresh would navigate away from the property
        // detail page back to the Properties list, making retry attempts search the wrong page.
        boolean found = false;
        long deadline = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < deadline && !found) {
            String body = driver.findElement(By.tagName("body")).getText();
            found = priceContains(body, UPDATED_PRICE);
            if (!found) pause(1500);
        }
        Assert.assertTrue(found,
            "Admin Property Calendar should show ₹" + formatIndian(UPDATED_PRICE) + " for " + SYNC_DATE);
        log("✓ Admin Property Calendar verified: ₹" + formatIndian(UPDATED_PRICE));
    }

    /**
     * Waits up to 10 s for td[data-date=isoDate] to appear in the DOM,
     * then scrolls it into the centre of the viewport and returns it.
     * The calendar renders all months in the DOM — no navigation needed, just scroll.
     */
    private WebElement scrollToDateCell(String isoDate) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        By loc = By.cssSelector("td[data-date='" + isoDate + "']");
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline) {
            List<WebElement> cells = driver.findElements(loc);
            if (!cells.isEmpty()) {
                WebElement cell = cells.get(0);
                js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", cell);
                pause(600);
                return cell;
            }
            pause(500);
        }
        return null;
    }

    // ── Phase 6: Admin Calendar & Pricing ────────────────────────────────────

    private void verifyAdminCalendarPricing() {
        AdminCalendarPage adminCalPage = new AdminCalendarPage(driver);

        adminCalPage.navigate(ADMIN_BASE_URL);
        pause(1000);

        adminCalPage.searchProperty(PROPERTY_ID);
        pause(500);
        adminCalPage.selectProperty(PROPERTY_ID);
        pause(1000);

        adminCalPage.navigateToDate(SYNC_DATE_ANT);

        // verifyPriceWithRetry checks body text with retries + page refresh
        adminCalPage.verifyPriceWithRetry(PROPERTY_NAME, UPDATED_PRICE);
        log("✓ Admin Calendar & Pricing verified: ₹" + formatIndian(UPDATED_PRICE));
    }

    // ── Phase 7: Admin Inventory & Rates ─────────────────────────────────────

    private void verifyAdminInventoryRates() {
        // Navigate directly with UUID + currentDate pre-applied — property and date are pre-loaded
        String url = ADMIN_BASE_URL + "/inventory-rates?showOnlyBaseRate=true&id="
                     + TestData.PROPERTY_UUID + "&currentDate=" + SYNC_DATE;
        driver.get(url);
        waitForDocumentReady();
        pause(3000);
        log("Admin Inventory URL: " + driver.getCurrentUrl());

        // The grid renders prices as input field values (not visible text) — read via JS
        String raw = String.valueOf(UPDATED_PRICE);
        boolean found = inventoryGridContainsPrice(raw);
        if (!found) {
            for (int i = 1; i <= TestData.SYNC_RETRY_COUNT; i++) {
                log("Admin Inventory & Rates sync retry [" + i + "/" + TestData.SYNC_RETRY_COUNT + "] looking for ₹" + formatIndian(UPDATED_PRICE));
                pause(TestData.SYNC_RETRY_WAIT_MS);
                driver.navigate().refresh();
                waitForDocumentReady();
                pause(2000);
                if (inventoryGridContainsPrice(raw)) { found = true; break; }
            }
        }
        if (!found) log("Admin Inventory & Rates did not show ₹" + formatIndian(UPDATED_PRICE) + " after " + TestData.SYNC_RETRY_COUNT + " retries");
        Assert.assertTrue(found,
            "Admin Inventory & Rates should show " + UPDATED_PRICE + " for " + SYNC_DATE);
        log("✓ Admin Inventory & Rates verified: ₹" + formatIndian(UPDATED_PRICE));
    }

    /** Checks all visible input field values in the Inventory & Rates grid for the target price. */
    private boolean inventoryGridContainsPrice(String raw) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        // Use JS to collect all input values on page — avoids body.getText() which misses input values
        Object result = js.executeScript(
            "return Array.from(document.querySelectorAll('input')).map(i=>i.value).filter(v=>v).join('|');");
        String allValues = result != null ? result.toString() : "";
        log("Admin Inventory input values snapshot: " + (allValues.length() > 200 ? allValues.substring(0, 200) + "…" : allValues));
        if (allValues.contains(raw)) {
            log("Admin Inventory & Rates sync confirmed: input value " + raw + " found");
            return true;
        }
        // Also check body text as secondary fallback
        String body = driver.findElement(By.tagName("body")).getText();
        return body.contains(raw) || body.contains(formatIndian(UPDATED_PRICE));
    }

    // ── Admin navigation helpers (mirrors BlockDateTest) ─────────────────────

    private void clickAdminMenu(String menuText) {
        for (By loc : new By[]{
            By.xpath("//span[contains(@class,'ant-menu-title-content') and normalize-space()='" + menuText + "']"),
            By.xpath("//li[contains(@class,'ant-menu') and .//span[normalize-space()='" + menuText + "']]"),
            By.xpath("//*[normalize-space()='" + menuText + "' and (self::a or self::span or self::li)]")
        }) {
            WebElement e = tryFindElement(loc, 8);
            if (e != null) { safeClick(e); return; }
        }
        throw new RuntimeException("Admin menu '" + menuText + "' not found. URL: " + driver.getCurrentUrl());
    }

    private void clickCalendarAndPricingTab() {
        for (By loc : new By[]{
            By.xpath("//button[contains(.,'Calendar') and contains(.,'Pricing')]"),
            By.xpath("//a[contains(.,'Calendar') and contains(.,'Pricing')]"),
            By.xpath("//*[@role='tab'][contains(.,'Calendar')]"),
            By.xpath("//*[normalize-space()='Calendar & Pricing']")
        }) {
            WebElement tab = tryFindElement(loc, 8);
            if (tab != null) {
                safeClick(tab);
                try {
                    new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(d -> !d.findElements(By.cssSelector("td[data-date]")).isEmpty());
                } catch (TimeoutException ignored) {}
                return;
            }
        }
        throw new RuntimeException("Calendar & Pricing tab not found. URL: " + driver.getCurrentUrl());
    }

    private WebElement findAdminSearchInput(String... cssSelectors) {
        for (String css : cssSelectors) {
            for (WebElement e : driver.findElements(By.cssSelector(css))) {
                try { if (e.isDisplayed()) return e; } catch (Exception ignored) {}
            }
        }
        // Last resort: first visible text input
        for (WebElement e : driver.findElements(By.cssSelector("input[type='text']"))) {
            try { if (e.isDisplayed()) return e; } catch (Exception ignored) {}
        }
        return null;
    }

    // ── Retry + assertion helpers ─────────────────────────────────────────────

    /**
     * Retries page refresh until price appears in body text.
     * Handles async sync delays across CDN / message queue propagation.
     */
    private boolean retryBodyTextCheck(String initialBody, int price, int maxRetries,
                                        int waitMs, String systemName) {
        String formatted = formatIndian(price);
        String raw = String.valueOf(price);
        String kAbbrev = (price % 1000 == 0) ? (price / 1000) + "k" : null;
        if (bodyContainsPrice(initialBody, formatted, raw, kAbbrev)) {
            System.out.println("[PriceSyncTest] " + systemName + " sync confirmed on first check: ₹" + formatted);
            return true;
        }
        for (int i = 1; i <= maxRetries; i++) {
            System.out.println("[PriceSyncTest] " + systemName + " sync retry [" + i + "/" + maxRetries + "] looking for ₹" + formatted);
            pause(waitMs);
            driver.navigate().refresh();
            pause(2000);
            String body = driver.findElement(By.tagName("body")).getText();
            if (bodyContainsPrice(body, formatted, raw, kAbbrev)) {
                System.out.println("[PriceSyncTest] " + systemName + " sync confirmed: ₹" + formatted);
                return true;
            }
        }
        System.out.println("[PriceSyncTest] " + systemName + " did not show ₹" + formatted + " after " + maxRetries + " retries");
        return false;
    }

    private boolean bodyContainsPrice(String body, String formatted, String raw, String kAbbrev) {
        if (body == null) return false;
        if (body.contains(formatted) || body.contains(raw)) return true;
        if (kAbbrev != null && body.contains(kAbbrev)) return true;
        return false;
    }

    /**
     * Checks if the given text contains the price in any common format:
     * raw number, Indian comma-formatted, or "k" abbreviated.
     */
    private boolean priceContains(String text, int price) {
        if (text == null || text.isEmpty()) return false;
        String formatted = formatIndian(price);
        if (text.contains(formatted) || text.contains(String.valueOf(price))) return true;
        // Check k-notation (7000 → "7k")
        if (price % 1000 == 0 && text.contains((price / 1000) + "k")) return true;
        // Parse input field value directly
        try { return Integer.parseInt(text.trim()) == price; } catch (NumberFormatException ignored) {}
        return false;
    }

    private static String formatIndian(int amount) {
        return String.format("%,d", amount);
    }

    private void log(String message) {
        System.out.println("[PriceSyncTest] " + message);
    }

}
