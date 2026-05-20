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

public class UpdatePricingTest extends BaseTest {

    // ── URLs ──────────────────────────────────────────────────────────────────
    private static final String GUEST_BASE_URL  = "https://uat.homeyhutz.com";
    private static final String SIGNIN_URL      = GUEST_BASE_URL + "/signup-signin?redirectUrl=/&";
    private static final String HOST_PORTAL_URL = "https://uat.host.homeyhutz.com";
    private static final String ADMIN_BASE_URL  = "https://uat.admin.new.homeyhutz.com";
    private static final String ADMIN_LOGIN_URL = ADMIN_BASE_URL + "/login";

    // ── Credentials ───────────────────────────────────────────────────────────
    private static final String HOST_PHONE     = "9422121212";
    private static final String HOST_OTP       = "123456";
    private static final String ADMIN_EMAIL    = "homey@admin.com";
    private static final String ADMIN_PASSWORD = "Password1!";

    // ── Test data ─────────────────────────────────────────────────────────────
    private static final String PROPERTY_ID   = String.valueOf(TestData.PROPERTY_ID); // "5012"
    private static final String PROPERTY_NAME = TestData.PROPERTY_NAME;               // "test House"
    private static final String SYNC_DATE     = TestData.PRICE_SYNC_DATE;             // "2026-12-25"
    private static final String SYNC_DATE_ANT = TestData.PRICE_SYNC_DATE_ANT;         // "25 Dec 26"
    private static final int    SYNC_PRICE    = TestData.SYNC_UPDATED_PRICE;          // 9000

    private static final int GST_AMOUNT  = GSTCalculator.calculateGST(SYNC_PRICE, 0);
    private static final int FINAL_PRICE = GSTCalculator.calculateFinalPrice(SYNC_PRICE, 0);

    // Derived display strings — change SYNC_UPDATED_PRICE in TestData and these update too
    private static final String PRICE_STR = String.valueOf(SYNC_PRICE);
    private static final String PRICE_FMT = String.format("%,d", SYNC_PRICE);
    private static final String FINAL_FMT = String.format("%,d", FINAL_PRICE);
    private static final String GST_FMT   = String.format("%,d", GST_AMOUNT);
    private static final int    GST_PCT   = (int)(GSTCalculator.getGSTRate(SYNC_PRICE) * 100);

    // =========================================================================
    // Priority 1 — Phase 1: Host Calendar only
    // Login → select property → set price for Dec 25 → verify on panel.
    // =========================================================================
    @Test(priority = 1)
    public void verifyUserCanUpdatePropertyPricing() {
        System.out.println("=== Priority 1: Phase 1 — Host Calendar ===");

        loginAndSetPrice(SYNC_PRICE);

        PricingPage pricing = new PricingPage(driver, wait);
        String displayed = pricing.getPriceDisplayedForDate(SYNC_DATE);
        System.out.println("Panel price after save: " + displayed);
        Assert.assertTrue(
            displayed.contains(PRICE_FMT) || displayed.contains(PRICE_STR),
            "Phase 1: Expected ₹" + PRICE_FMT + " on host calendar panel. Found: " + displayed
        );
        System.out.println("✓ Phase 1 — Host Calendar: ₹" + PRICE_FMT + " verified on panel");
    }

    // =========================================================================
    // Priority 2 — Phases 2-7: Full price sync across all downstream systems.
    // Price was already set by priority=1 — only login is needed here.
    // =========================================================================
    @Test(priority = 2)
    public void verifyUpdatedHostPriceSyncsToAdmin() {
        GSTCalculator.logBreakdown(SYNC_PRICE, 0);

        // Price was already set by priority=1 (verifyUserCanUpdatePropertyPricing).
        // Each TestNG method gets a fresh browser, so we only need to login for the
        // host-portal session — no need to set the price again.
        loginToHostPortalOnly();

        // ── Phase 2: Unified Calendar (Host Side) ────────────────────────────
        System.out.println("=== Phase 2: Unified Calendar ===");
        UnifiedCalendarPage unifiedCal = new UnifiedCalendarPage(driver, wait);
        // Step 1: Click Calendar nav from host portal
        unifiedCal.clickCalendarNav();
        waitForDocumentReady();
        pause(1500);
        // Step 2: Type property ID in search — filters the left panel to show property 5012.
        // We do NOT click the dropdown result (that would navigate to the property detail URL).
        // The unified calendar stays on /calendar and filters its grid view.
        unifiedCal.typePropertySearch(PROPERTY_ID);
        waitForDocumentReady();
        pause(1500);
        // Step 3: Navigate the date picker to Dec 25 2026
        // (tries Ant DatePicker input; falls back to appending currentDate to the URL)
        unifiedCal.navigateToTargetDate(SYNC_DATE_ANT, SYNC_DATE);
        // Step 4: Scroll to the Dec 25 cell and read the price directly from the grid
        // (no click — grid renders price inline; abbreviated as "₹2k" for ₹2,000)
        String phase2CellPrice = unifiedCal.readPriceFromCell(SYNC_DATE);
        System.out.println("Unified Calendar grid price for Dec 25: " + phase2CellPrice);
        // Step 5: Assert — abbreviated cell text (₹2k) or full format, fall back to body poll
        String abbrevK = (SYNC_PRICE % 1000 == 0) ? "₹" + (SYNC_PRICE / 1000) + "k" : "";
        boolean phase2ok = phase2CellPrice.contains(PRICE_FMT)
            || phase2CellPrice.contains(PRICE_STR)
            || (!abbrevK.isEmpty() && phase2CellPrice.contains(abbrevK));
        if (!phase2ok) {
            System.out.println("Price not directly in cell text — polling body/input values");
            unifiedCal.verifyPriceWithRetry(SYNC_PRICE);
        } else {
            System.out.println("✅ Unified Calendar cell shows " + phase2CellPrice + " for Dec 25");
        }
        System.out.println("✓ Phase 2 — Unified Calendar: ₹" + PRICE_FMT + " verified");

        // ── Phase 3: Channel Manager ──────────────────────────────────────────
        System.out.println("=== Phase 3: Channel Manager ===");
        ChannelManagerPage cmPage = new ChannelManagerPage(driver, wait);
        driver.get(HOST_PORTAL_URL);
        waitForDocumentReady();
        pause(2000);
        cmPage.clickChannelManagerNav();
        pause(1500);
        cmPage.searchAndSelectProperty(PROPERTY_NAME, PROPERTY_ID);
        pause(1000);
        cmPage.navigateToDate(SYNC_DATE_ANT);
        String cmPrice = cmPage.getPriceDisplayed();
        System.out.println("Channel Manager price text: " + cmPrice);
        boolean phase3 = cmPrice.contains(PRICE_STR) || cmPrice.contains(PRICE_FMT);
        if (!phase3) phase3 = bodyContains(PRICE_FMT) || bodyContains(PRICE_STR);
        Assert.assertTrue(phase3, "Phase 3: Channel Manager should show " + PRICE_STR + ". Found: " + cmPrice);
        System.out.println("✓ Phase 3 — Channel Manager: ₹" + PRICE_FMT + " verified");

        // ── Phase 4: Guest-Facing Property Page ───────────────────────────────
        System.out.println("=== Phase 4: Guest-Facing Page (base ₹" + PRICE_FMT + ", GST " + GST_PCT + "%, final ₹" + FINAL_FMT + ") ===");
        GuestPropertyPage guestPage = new GuestPropertyPage(driver, wait);
        guestPage.openPropertyWithDates(GUEST_BASE_URL, TestData.GUEST_ROOM_ID, SYNC_DATE, "2026-12-26");
        waitForDocumentReady();
        pause(2000);
        boolean phase4base  = guestPage.waitForPriceVisible(SYNC_PRICE, TestData.SYNC_RETRY_COUNT, TestData.SYNC_RETRY_WAIT_MS);
        if (!phase4base) guestPage.waitForPriceVisible(FINAL_PRICE, 3, 2000);
        guestPage.assertPriceBreakdown(SYNC_PRICE, FINAL_PRICE);
        System.out.println("✓ Phase 4 — Guest Page: base ₹" + PRICE_FMT + ", GST " + GST_PCT + "% ₹" + GST_FMT + ", final ₹" + FINAL_FMT + " verified");

        // ── Admin Login ───────────────────────────────────────────────────────
        AdminLoginPage adminLogin = new AdminLoginPage(driver);
        adminLogin.open(ADMIN_LOGIN_URL);
        adminLogin.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        waitForDocumentReady();
        pause(2000);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(d -> !d.getCurrentUrl().contains("/login"));
            System.out.println("✓ Admin login successful");
        } catch (TimeoutException e) {
            throw new RuntimeException("Admin login failed. URL: " + driver.getCurrentUrl());
        }

        // ── Phase 5: Admin Property Calendar ─────────────────────────────────
        System.out.println("=== Phase 5: Admin Property Calendar ===");
        clickAdminMenu("Properties");
        pause(1500);

        WebElement searchInput = null;
        for (By loc : new By[]{
            By.cssSelector("input[placeholder='Search by Name, Property Id']"),
            By.cssSelector("input[placeholder*='Search by Name']"),
            By.cssSelector("input[placeholder*='Property Id']")
        }) {
            searchInput = tryFindElement(loc, 5);
            if (searchInput != null) break;
        }
        if (searchInput == null) throw new RuntimeException("Phase 5: Admin Properties search box not found");
        clearAndType(searchInput, PROPERTY_ID);
        pause(1500);

        boolean rowClicked = false;
        for (By loc : new By[]{
            By.xpath("//td[contains(@class,'ant-table-cell') and normalize-space()='" + PROPERTY_ID + "']"),
            By.xpath("//td[contains(@class,'ant-table-cell') and contains(normalize-space(),'" + PROPERTY_ID + "')]"),
            By.xpath("//*[contains(normalize-space(),'" + PROPERTY_ID + "') and (self::td or self::tr or self::div)]")
        }) {
            WebElement row = tryFindElement(loc, 5);
            if (row != null) { safeClick(row); rowClicked = true; break; }
        }
        if (!rowClicked) throw new RuntimeException("Phase 5: Admin property row not found for " + PROPERTY_ID);
        pause(3000);

        // Open Calendar & Pricing tab
        for (By loc : new By[]{
            By.xpath("//button[contains(.,'Calendar') and contains(.,'Pricing')]"),
            By.xpath("//a[contains(.,'Calendar') and contains(.,'Pricing')]"),
            By.xpath("//*[@role='tab'][contains(.,'Calendar')]"),
            By.xpath("//*[normalize-space()='Calendar & Pricing']")
        }) {
            WebElement tab = tryFindElement(loc, 8);
            if (tab != null) {
                safeClick(tab);
                try { new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> !d.findElements(By.cssSelector("td[data-date]")).isEmpty());
                } catch (TimeoutException ignored) {}
                break;
            }
        }
        pause(2000);

        // Scroll directly to Dec 25 — no month navigation needed (all months in DOM)
        JavascriptExecutor js = (JavascriptExecutor) driver;
        List<WebElement> cells = driver.findElements(By.cssSelector("td[data-date='" + SYNC_DATE + "']"));
        if (!cells.isEmpty()) {
            js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", cells.get(0));
            pause(600);
            safeClick(cells.get(0));
            pause(1500);
            System.out.println("Clicked admin calendar cell for " + SYNC_DATE);
        } else {
            System.out.println("Phase 5 WARNING: td[data-date='" + SYNC_DATE + "'] not found — checking body text");
        }

        // Poll body text (no page refresh — refresh navigates away from property detail)
        boolean phase5 = false;
        long deadline5 = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < deadline5 && !phase5) {
            phase5 = bodyContains(PRICE_FMT) || bodyContains(PRICE_STR);
            if (!phase5) pause(1500);
        }
        Assert.assertTrue(phase5, "Phase 5: Admin Property Calendar should show ₹" + PRICE_FMT + " for " + SYNC_DATE);
        System.out.println("✓ Phase 5 — Admin Property Calendar: ₹" + PRICE_FMT + " verified");

        // ── Phase 6: Admin Calendar & Pricing ────────────────────────────────
        System.out.println("=== Phase 6: Admin Calendar & Pricing ===");
        AdminCalendarPage adminCalPage = new AdminCalendarPage(driver);
        adminCalPage.navigate(ADMIN_BASE_URL);
        // Search by property ID — proven pattern (searching by name fails to load Dec data)
        adminCalPage.searchProperty(PROPERTY_ID);
        adminCalPage.selectProperty(PROPERTY_ID);
        adminCalPage.navigateToDate(SYNC_DATE_ANT);
        adminCalPage.verifyPriceWithRetry(PROPERTY_NAME, SYNC_PRICE);
        System.out.println("✓ Phase 6 — Admin Calendar & Pricing: ₹" + PRICE_FMT + " verified");

        // ── Phase 7: Admin Inventory & Rates ─────────────────────────────────
        System.out.println("=== Phase 7: Admin Inventory & Rates ===");
        AdminInventoryRatesPage adminInvPage = new AdminInventoryRatesPage(driver);
        adminInvPage.navigate(ADMIN_BASE_URL, TestData.PROPERTY_UUID, SYNC_DATE);
        adminInvPage.verifyPropertyIdBadge(PROPERTY_ID);
        // Prices render as input .value (not visible text) — read via JS
        String rawPrice = String.valueOf(SYNC_PRICE);
        boolean phase7 = inputValuesContain(rawPrice);
        for (int i = 1; i <= TestData.SYNC_RETRY_COUNT && !phase7; i++) {
            System.out.println("Phase 7 retry [" + i + "/" + TestData.SYNC_RETRY_COUNT + "] looking for " + rawPrice);
            pause(TestData.SYNC_RETRY_WAIT_MS);
            driver.navigate().refresh();
            waitForDocumentReady();
            pause(2000);
            phase7 = inputValuesContain(rawPrice);
        }
        Assert.assertTrue(phase7, "Phase 7: Admin Inventory & Rates should show " + SYNC_PRICE + " for " + SYNC_DATE);
        System.out.println("✓ Phase 7 — Admin Inventory & Rates: " + PRICE_STR + " verified");

        System.out.println("====================================================");
        System.out.println("✅ ALL 7 PHASES VERIFIED — ₹" + PRICE_FMT + " synced across every system");
        System.out.println("====================================================");
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    /** Logs in and navigates to the host portal WITHOUT setting a price. */
    private void loginToHostPortalOnly() {
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
        waitForLoginAndNavigateToHostPortal();
        System.out.println("✓ Logged in to host portal (price not changed)");
    }

    /** Logs in, navigates to the host portal, selects property and sets the price. */
    private void loginAndSetPrice(int price) {
        HomePage  homePage  = new HomePage(driver, wait);
        LoginPage loginPage = new LoginPage(driver, wait);
        OtpPage   otpPage   = new OtpPage(driver, wait);
        PricingPage pricing  = new PricingPage(driver, wait);

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
        waitForLoginAndNavigateToHostPortal();

        pricing.clickPropertiesSidebar();
        pause(1000);
        pricing.searchProperty(PROPERTY_ID);
        pause(500);
        pricing.selectPropertyByName(PROPERTY_ID);
        pause(1000);
        pricing.clickPricingTab();
        pause(1000);
        pricing.updatePricingForDate(SYNC_DATE, String.valueOf(price), null);
        pause(2000);
    }

    // ── Low-level helpers ─────────────────────────────────────────────────────

    private boolean bodyContains(String text) {
        try { return driver.findElement(By.tagName("body")).getText().contains(text); }
        catch (Exception e) { return false; }
    }

    private boolean inputValuesContain(String value) {
        try {
            JavascriptExecutor jsExec = (JavascriptExecutor) driver;
            Object res = jsExec.executeScript(
                "return Array.from(document.querySelectorAll('input')).map(i=>i.value).filter(v=>v).join('|');");
            if (res == null) return false;
            String all = res.toString();
            System.out.println("Input values snapshot: " + (all.length() > 200 ? all.substring(0, 200) + "…" : all));
            return all.contains(value) || bodyContains(value);
        } catch (Exception e) { return false; }
    }

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

    private void waitForLoginAndNavigateToHostPortal() {
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
        if (!driver.getCurrentUrl().contains("host.homeyhutz.com")) {
            pause(3000);
            driver.get(HOST_PORTAL_URL);
            waitForDocumentReady();
            pause(1000);
        }
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.urlContains("host.homeyhutz.com"));
            System.out.println("✓ Navigated to host portal");
        } catch (TimeoutException e) {
            throw new RuntimeException("Could not reach host portal. URL: " + driver.getCurrentUrl());
        }
    }


}
