package com.homeyhutz.tests;

import com.homeyhutz.base.BaseTest;
import com.homeyhutz.pages.HomePage;
import com.homeyhutz.pages.LoginPage;
import com.homeyhutz.pages.OtpPage;
import com.homeyhutz.pages.UnifiedCalendarPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class BlockDateTest extends BaseTest {

    // ── Shared session (login once, reuse across all tests to avoid OTP rate-limit) ──
    private static WebDriver     sharedDriver;
    private static WebDriverWait sharedWait;

    @Override @BeforeMethod
    public void setUp() {
        if (sharedDriver != null) { driver = sharedDriver; wait = sharedWait; }
        else super.setUp();
    }

    @Override @AfterMethod
    public void tearDown(ITestResult result) {
        if (sharedDriver == null) { super.tearDown(result); return; }
        try {
            if (result.getStatus() == ITestResult.FAILURE) takeScreenshot(result.getName());
        } catch (Exception ignored) {}
    }

    @AfterClass(alwaysRun = true)
    public void closeSharedSession() {
        if (sharedDriver != null) {
            try { sharedDriver.quit(); } catch (Exception ignored) {}
            sharedDriver = null;
        }
    }

    // ── URLs ──────────────────────────────────────────────────────────────────
    private static final String HOST_PORTAL_URL = "https://uat.host.homeyhutz.com";
    private static final String ADMIN_LOGIN_URL  = "https://uat.admin.new.homeyhutz.com/login";
    private static final String ADMIN_BASE_URL   = "https://uat.admin.new.homeyhutz.com";
    private static final String SIGNIN_URL       = "https://uat.homeyhutz.com/signup-signin?redirectUrl=/&";

    // ── Credentials ───────────────────────────────────────────────────────────
    private static final String HOST_PHONE     = "9422121212";
    private static final String HOST_OTP       = "123456";
    private static final String ADMIN_EMAIL    = "homey@admin.com";
    private static final String ADMIN_PASSWORD = "Password1!";

    // ── Property ──────────────────────────────────────────────────────────────
    private static final String PROPERTY_NAME     = "test House";
    private static final String PROPERTY_ID       = "5012";
    private static final String ADMIN_PROPERTY_ID = "5012";

    // ── Booking form test data ────────────────────────────────────────────────
    private static final String CONTACT_NAME   = "Test User";
    private static final String CONTACT_EMAIL  = "testuser@example.com";
    private static final String CONTACT_PHONE  = "9234567804";
    private static final String BOOKING_AMOUNT = "2000";

    // ── Fallback date — used if dynamic scan cannot find an unblocked date ────
    private static final String SEED_DATE = "2026-11-27";

    // ── Set once in @BeforeClass — all @Test methods read from these ──────────
    private static String blockedDate  = "2026-11-27";
    private static String unifiedDate  = "27 Nov 26";
    private static String checkinDate  = "27 Nov 2026";
    private static String checkoutDate = "28 Nov 2026";

    // ── @BeforeClass: open host portal, block a date, keep session alive ──────
    @BeforeClass(alwaysRun = true)
    public void blockDateOnce() {
        logStep("SETUP: initialising shared session");
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--remote-allow-origins=*");
        opts.setPageLoadStrategy(PageLoadStrategy.EAGER);
        sharedDriver = new ChromeDriver(opts);
        sharedDriver.manage().window().maximize();
        sharedWait   = new WebDriverWait(sharedDriver, Duration.ofSeconds(30));
        driver = sharedDriver;
        wait   = sharedWait;

        try {
            openHostControlCenter();
            clickProperties();
            pause(2000);
            WebElement search = findHostPropertySearchInput();
            if (search != null) { clearAndType(search, PROPERTY_ID); pause(1500); }
            clickHostPropertyById(PROPERTY_ID);
            pause(1500);
            clickCalendarAndPricingTab();
            setDynamicDates(findAndBlockAvailableDate());
            logStep("SETUP: blocked date = " + blockedDate);
        } catch (Exception e) {
            logStep("SETUP WARNING: " + e.getMessage());
            boolean sessionDead = false;
            try { sharedDriver.getTitle(); } catch (Exception ignored) { sessionDead = true; }
            if (sessionDead) {
                logStep("SETUP: session dead — reinitialising browser");
                try { sharedDriver.quit(); } catch (Exception ignored) {}
                sharedDriver = new ChromeDriver(opts);
                sharedDriver.manage().window().maximize();
                sharedWait   = new WebDriverWait(sharedDriver, Duration.ofSeconds(30));
                driver = sharedDriver;
                wait   = sharedWait;
            }
        }
    }

    private String findAndBlockAvailableDate() {
        LocalDate seed = LocalDate.parse(SEED_DATE);
        String candidate = null;

        // Scan 1–3 months ahead; CSS check only (no clicks) to pick one candidate
        for (int months = 3; months >= 1; months--) {
            String date = seed.plusMonths(months).toString();
            if (driver.findElements(By.cssSelector("td[data-date='" + date + "']")).isEmpty()) continue;
            if (isHostCalendarDateBlocked(date)) continue;
            candidate = date;
            break;
        }

        if (candidate == null) return SEED_DATE;

        // Check if already blocked by looking for the block-event FullCalendar label
        if (isHostCalendarDateBlocked(candidate)) {
            logStep("SETUP: " + candidate + " is already blocked — using SEED_DATE");
            return SEED_DATE;
        }

        logStep("SETUP: clicking candidate date " + candidate);
        scrollToDateAndClick(candidate);
        pause(2500);

        // Block button on property calendar = a pill div with text-white (confirmed from Cypress test)
        // Selector: div.relative.z-10.flex-1.flex.items-center.justify-center.py-1.text-white
        WebElement blockBtn = null;
        for (By loc : new By[]{
            By.cssSelector("div.relative.z-10.flex-1.flex.items-center.justify-center.py-1.text-white"),
            By.xpath("//*[normalize-space()='Block' and (self::button or self::div or self::span)]")
        }) {
            blockBtn = tryFindElement(loc, 4);
            if (blockBtn != null) break;
        }
        if (blockBtn != null) {
            safeClick(blockBtn);
            pause(2000);
            logStep("SETUP: blocked " + candidate);
            return candidate;
        }

        logStep("SETUP: block button not found for " + candidate + " — falling back to SEED_DATE");
        try { driver.findElement(By.cssSelector("body")).sendKeys(Keys.ESCAPE); pause(1000); } catch (Exception ignored) {}
        return SEED_DATE;
    }

    private static void setDynamicDates(String iso) {
        LocalDate d = LocalDate.parse(iso);
        blockedDate  = iso;
        unifiedDate  = d.format(DateTimeFormatter.ofPattern("d MMM yy",   Locale.ENGLISH));
        checkinDate  = d.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH));
        checkoutDate = d.plusDays(1).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH));
    }

    // ── TC1: Host property calendar shows the blocked date ────────────────────
    @Test(priority = 1)
    public void hostCalendarShowsBlockedDate() {
        logStep("TC1: host calendar shows " + blockedDate);
        driver.get(HOST_PORTAL_URL + "/listing/properties/" + PROPERTY_ID);
        waitForDocumentReady();
        pause(1500);
        clickCalendarAndPricingTab();

        // Calendar is vertically scrollable — scroll to the date without clicking
        scrollToDateOnly(blockedDate);
        pause(1000);

        boolean isBlocked = isHostCalendarDateBlocked(blockedDate);
        Assert.assertTrue(isBlocked, "TC1 FAIL: " + blockedDate + " should be blocked on host calendar");
        logStep("TC1 PASS");
    }

    // ── TC2: Unified calendar shows the blocked date ──────────────────────────
    @Test(priority = 2)
    public void verifyBlockedDateInUnifiedCalendar() {
        logStep("TC2: unified calendar shows " + unifiedDate);
        // Navigate directly to the unified calendar — no Properties detour needed
        driver.get(HOST_PORTAL_URL + "/calendar");
        waitForDocumentReady();
        pause(2000);

        // Step 1: Navigate to blocked date via Ant DatePicker
        WebElement pickerContainer = tryFindElement(By.cssSelector("div.ant-picker-input"), 8);
        if (pickerContainer != null) { safeClick(pickerContainer); pause(800); }
        UnifiedCalendarPage calPage = new UnifiedCalendarPage(driver, wait);
        calPage.navigateToTargetDate(unifiedDate, blockedDate);
        pause(1500);

        // Step 2: Locate test House's row using its property link href — no search bar needed
        // Left panel: <a href="/listing/properties/5012?..."> and right panel date cells
        // share the same viewport Y-band because the grid syncs row heights across both panels
        WebElement propertyLink = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("a[href*='/listing/properties/" + PROPERTY_ID + "']")));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", propertyLink);
        pause(600);

        int rowTop    = propertyLink.getLocation().getY();
        int rowHeight = propertyLink.getSize().getHeight();
        logStep("TC2: test House row Y=" + rowTop + " h=" + rowHeight);

        // Step 3: Collect .uf_cal_date_box cells in the same Y-band as test House
        List<WebElement> rowCells = new java.util.ArrayList<>();
        for (WebElement cell : driver.findElements(By.cssSelector(".uf_cal_date_box"))) {
            try {
                int cellY = cell.getLocation().getY();
                if (cellY >= rowTop - 10 && cellY <= rowTop + rowHeight + 10)
                    rowCells.add(cell);
            } catch (Exception ignored) {}
        }
        logStep("TC2: " + rowCells.size() + " total cells in test House Y-band");

        // Step 4: Narrow to cells VISIBLE IN THE VIEWPORT — the calendar pre-renders all months
        // in the DOM; only the visible cells belong to the navigated date range (week of blockedDate)
        List<WebElement> visibleCells = new java.util.ArrayList<>();
        for (WebElement cell : rowCells) {
            try {
                Boolean inView = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var r=arguments[0].getBoundingClientRect();" +
                    "return r.width>0&&r.height>0&&r.left>0&&r.right<=window.innerWidth+10;",
                    cell);
                if (Boolean.TRUE.equals(inView)) visibleCells.add(cell);
            } catch (Exception ignored) {}
        }
        logStep("TC2: " + visibleCells.size() + " visible date cells in test House row");
        Assert.assertFalse(visibleCells.isEmpty(),
                "TC2 FAIL: no visible date cells found for property " + PROPERTY_ID);

        // Step 5: Count blocked cells only in the visible (navigated) date range
        long blockedInRow = 0;
        for (WebElement cell : visibleCells) {
            try {
                Object txt = ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].innerText;", cell);
                if (txt != null && txt.toString().contains("Blocked")) blockedInRow++;
            } catch (Exception ignored) {}
        }

        // Exactly 1 blocked cell expected — confirms the date is blocked AND
        // no adjacent date was accidentally blocked by this single blocking action
        Assert.assertTrue(blockedInRow >= 1,
                "TC2 FAIL: " + unifiedDate + " should be blocked in test House row");
        Assert.assertEquals(blockedInRow, 1L,
                "TC2 FAIL: exactly 1 date in visible range of test House row should be blocked, found " + blockedInRow);

        logStep("TC2 PASS");
    }

    // ── TC3: Offline booking is rejected for the blocked date ─────────────────
    @Test(priority = 3)
    public void verifyBlockedDateInManualBookingCalendar() {
        logStep("TC3: offline booking rejected for " + checkinDate);
        driver.get(HOST_PORTAL_URL + "/reservations");
        waitForDocumentReady();
        pause(2000);

        // Button label: "Create Manual Booking" (host portal) — restrict to <button> only
        WebElement offlineBtn = null;
        for (By loc : new By[]{
            By.xpath("//button[normalize-space()='Create Manual Booking']"),
            By.xpath("//button[contains(.,'Manual Booking')]"),
            By.xpath("//button[normalize-space()='Offline Booking']"),
            By.xpath("//button[contains(.,'Offline Booking')]")
        }) {
            offlineBtn = tryFindElement(loc, 5);
            if (offlineBtn != null) break;
        }
        Assert.assertNotNull(offlineBtn, "TC3 FAIL: 'Create Manual Booking' / 'Offline Booking' button not found");
        logStep("TC3: clicking '" + offlineBtn.getText().trim() + "' <" + offlineBtn.getTagName() + ">");
        safeClick(offlineBtn);
        pause(2000);

        WebElement propSearch = findManualBookingPropertySearch();
        Assert.assertNotNull(propSearch, "TC3 FAIL: property search input not found");
        clearAndType(propSearch, PROPERTY_ID);
        pause(1500);
        clickPropertyDropdownItem(PROPERTY_ID);
        pause(3000);

        clearAndType(visibleElement(By.id("bookingContactDetail_name")),  CONTACT_NAME);
        clearAndType(visibleElement(By.id("bookingContactDetail_email")), CONTACT_EMAIL);
        clearAndType(visibleElement(By.id("bookingContactDetail_phone")), CONTACT_PHONE);
        clearAndType(visibleElement(By.id("totalAmount")),                BOOKING_AMOUNT);
        clearAndType(visibleElement(By.id("advanceAmount")),              BOOKING_AMOUNT);

        // Range picker: id="checkInOut" placeholder="Start Date" date-range="start"
        WebElement checkIn = null;
        for (By loc : new By[]{
            By.cssSelector("input#checkInOut"),
            By.cssSelector("input[placeholder='Start Date']"),
            By.cssSelector("input[date-range='start']")
        }) {
            checkIn = tryFindElement(loc, 8);
            if (checkIn != null) break;
        }
        Assert.assertNotNull(checkIn, "TC3 FAIL: check-in (Start Date) input not found");
        // Open the range picker by clicking its container first
        try {
            WebElement rangeContainer = (WebElement) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].closest('.ant-picker-range, .ant-picker');", checkIn);
            if (rangeContainer != null) { safeClick(rangeContainer); pause(600); }
        } catch (Exception ignored) {}
        clearAndType(checkIn, checkinDate);
        pause(400);
        // TAB moves focus to the end input — Ant Design removes the disabled attribute when end input gains focus
        try { checkIn.sendKeys(Keys.TAB); } catch (Exception ignored) {}
        pause(800);

        // Check-out: wait for Ant to enable the end input after TAB
        WebElement checkOut = null;
        for (int i = 0; i < 6 && checkOut == null; i++) {
            // Prefer the active (focused) element if it's the end input
            try {
                WebElement active = driver.switchTo().activeElement();
                String ph = active == null ? "" : active.getAttribute("placeholder");
                if ("End Date".equals(ph)) { checkOut = active; break; }
            } catch (Exception ignored) {}
            // Poll for the end input to lose its disabled attribute
            for (By loc : new By[]{
                By.cssSelector("input[date-range='end']"),
                By.cssSelector("input[placeholder='End Date']")
            }) {
                for (WebElement e : driver.findElements(loc)) {
                    try { if (e.getAttribute("disabled") == null) { checkOut = e; break; } }
                    catch (Exception ignored) {}
                }
                if (checkOut != null) break;
            }
            if (checkOut == null) pause(400);
        }
        if (checkOut != null) {
            clearAndType(checkOut, checkoutDate);
            try {
                checkOut.sendKeys(Keys.ENTER);
            } catch (Exception ignored) {
                // End input still not interactable for native sendKeys — dispatch via JS
                try {
                    ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].dispatchEvent(new KeyboardEvent('keydown',{key:'Enter',keyCode:13,bubbles:true}));",
                        checkOut);
                } catch (Exception ignored2) {}
            }
            pause(1500);
        }

        WebElement submitBtn = tryFindElement(By.xpath("//button[contains(.,'Create Booking')]"), 8);
        Assert.assertNotNull(submitBtn, "TC3 FAIL: 'Create Booking' submit button not found");
        safeClick(submitBtn);
        pause(2500);

        boolean rejected = hasVisibleElement(By.cssSelector(".ant-message-error, .ant-notification-error, [class*='error']"))
                || (hasVisibleElement(By.id("bookingContactDetail_name"))
                    && !hasVisibleElement(By.cssSelector(".ant-message-success, .ant-notification-success")));

        Assert.assertTrue(rejected, "TC3 FAIL: booking on blocked date " + checkinDate + " should be rejected");
        logStep("TC3 PASS");
    }

    // ── TC4: Channel Manager shows inventory = 0 for the blocked date ─────────
    @Test(priority = 4)
    public void verifyBlockedDateInChannelManagerCalendar() {
        logStep("TC4: channel manager inventory = 0 for " + unifiedDate);
        driver.get(HOST_PORTAL_URL + "/channel-manager");
        waitForDocumentReady();
        pause(3000);

        // Left drawer: search input with placeholder "Search by Property Name"
        WebElement propSearch = null;
        for (By loc : new By[]{
            By.cssSelector("input[placeholder='Search by Property Name']"),
            By.cssSelector("input.ant-input[placeholder*='Property Name']")
        }) {
            propSearch = tryFindElement(loc, 8);
            if (propSearch != null) break;
        }
        Assert.assertNotNull(propSearch, "TC4 FAIL: property name search input not found");
        clearAndType(propSearch, PROPERTY_NAME);
        pause(1500);

        // Click the property card: div.flex.w-full.hover:border-brand.flex-col.cursor-pointer.rounded-lg.border.p-2
        boolean propClicked = false;
        for (By loc : new By[]{
            By.xpath("//span[contains(@class,'font-medium') and normalize-space()='" + PROPERTY_NAME + "']/ancestor::div[contains(@class,'cursor-pointer')][1]"),
            By.xpath("//span[normalize-space()='" + PROPERTY_NAME + "']/ancestor::div[contains(@class,'cursor-pointer')][1]"),
            By.xpath("//div[contains(@class,'cursor-pointer') and .//span[normalize-space()='" + PROPERTY_NAME + "']][1]")
        }) {
            WebElement card = tryFindElement(loc, 5);
            if (card != null) { safeClick(card); propClicked = true; break; }
        }
        Assert.assertTrue(propClicked, "TC4 FAIL: property card for '" + PROPERTY_NAME + "' not found");
        pause(2000);

        WebElement dateInput = tryFindElement(By.cssSelector("input[placeholder='Select date']"), 5);
        Assert.assertNotNull(dateInput, "TC4 FAIL: date input not found");
        dateInput.click();
        pause(500);
        clearAndType(dateInput, unifiedDate);
        dateInput.sendKeys(Keys.ENTER);
        pause(1500);

        WebElement inventoryInput = null;
        for (WebElement e : driver.findElements(By.cssSelector("input[type='number']"))) {
            if (e.isDisplayed()) { inventoryInput = e; break; }
        }
        Assert.assertNotNull(inventoryInput, "TC4 FAIL: inventory input not found");
        Assert.assertEquals(inventoryInput.getAttribute("value"), "0",
                "TC4 FAIL: inventory should be 0 for blocked date " + unifiedDate);
        logStep("TC4 PASS");
    }

    // ── TC5: Admin property calendar shows the blocked date ───────────────────
    @Test(priority = 5)
    public void verifyDateBlockedInPropertyCalendarAdminPanel() {
        logStep("TC5: admin calendar shows " + blockedDate + " as blocked");
        openAdminPanel();

        // Navigate directly to the property calendar — yearly multi-month FullCalendar view
        driver.get(ADMIN_BASE_URL + "/calendar-pricing?id=" + ADMIN_PROPERTY_ID);
        waitForDocumentReady();
        pause(2000);

        // Scroll to the blocked date's month section (each month has data-date='YYYY-MM')
        String monthKey = blockedDate.substring(0, 7); // "2026-11"
        WebElement monthSection = tryFindElement(
                By.cssSelector("div[data-date='" + monthKey + "']"), 10);
        if (monthSection != null) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", monthSection);
            pause(1000);
        }

        // Locate the exact date cell — td[data-date='2026-11-27']
        WebElement dateCell = tryFindElement(
                By.cssSelector("td[data-date='" + blockedDate + "']"), 10);
        Assert.assertNotNull(dateCell, "TC5 FAIL: date cell for " + blockedDate + " not found in admin calendar");

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", dateCell);
        pause(500);

        // Blocked cell carries class "blocked-date" and contains a.block-event with "Blocked" text
        String cellClass = dateCell.getAttribute("class");
        boolean hasBlockedClass = cellClass != null && cellClass.contains("blocked-date");
        boolean hasBlockEvent  = !dateCell.findElements(By.cssSelector("a.block-event")).isEmpty();
        boolean hasBlockedText = false;
        try {
            Object txt = ((JavascriptExecutor) driver).executeScript(
                    "return arguments[0].innerText;", dateCell);
            hasBlockedText = txt != null && txt.toString().contains("Blocked");
        } catch (Exception ignored) {}

        Assert.assertTrue(hasBlockedClass || hasBlockEvent || hasBlockedText,
                "TC5 FAIL: " + blockedDate + " not shown as blocked. Cell class: " + cellClass);
        logStep("TC5 PASS");
    }

    // ── TC6: Admin Inventory & Rates shows 0 for the blocked date ────────────
    @Test(priority = 6)
    public void verifyDateBlockedInInventoryRatesAdminPanel() {
        logStep("TC6: admin inventory = 0 for " + unifiedDate);
        openAdminPanel();
        clickAdminMenu("Inventory & Rates");
        pause(2000);

        WebElement searchInput = tryFindElement(By.cssSelector("input[placeholder*='Property Name']"), 5);
        Assert.assertNotNull(searchInput, "TC6 FAIL: inventory search input not found");
        clearAndType(searchInput, PROPERTY_NAME);
        pause(2000);

        boolean propClicked = false;
        for (By loc : new By[]{
            By.xpath("//span[contains(@class,'font-medium') and normalize-space()='" + PROPERTY_NAME + "']/ancestor::div[contains(@class,'cursor-pointer')][1]"),
            By.xpath("//span[normalize-space()='" + PROPERTY_NAME + "']/ancestor::div[contains(@class,'cursor-pointer')][1]"),
            By.xpath("//div[contains(@class,'cursor-pointer') and .//span[normalize-space()='" + PROPERTY_NAME + "']][1]"),
            By.xpath("//div[contains(@class,'cursor-pointer') and contains(normalize-space(),'" + PROPERTY_NAME + "')][1]")
        }) {
            WebElement prop = tryFindElement(loc, 5);
            if (prop != null) { safeClick(prop); propClicked = true; break; }
        }
        Assert.assertTrue(propClicked, "TC6 FAIL: property '" + PROPERTY_NAME + "' not found in inventory list");
        pause(3000);

        WebElement dateInput = tryFindElement(By.cssSelector("input[placeholder='Select date']"), 8);
        Assert.assertNotNull(dateInput, "TC6 FAIL: date input not found");
        // Click the picker container first so Ant registers the open state
        try {
            WebElement pickerContainer = (WebElement) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].closest('.ant-picker');", dateInput);
            if (pickerContainer != null) { safeClick(pickerContainer); pause(400); }
        } catch (Exception ignored) {}
        clearAndType(dateInput, unifiedDate);
        dateInput.sendKeys(Keys.ENTER);
        pause(3000);

        // Poll for inventory = 0 — the grid may take a moment to re-render after date selection
        String inventoryValue = "";
        for (int i = 0; i < 5; i++) {
            WebElement inventoryInput = null;
            for (WebElement e : driver.findElements(
                    By.cssSelector("input.ant-input-number-input, input[type='number']"))) {
                try { if (e.isDisplayed()) { inventoryInput = e; break; } } catch (Exception ignored) {}
            }
            if (inventoryInput != null) {
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'});", inventoryInput);
                inventoryValue = inventoryInput.getAttribute("value");
                if ("0".equals(inventoryValue)) break;
            }
            pause(1500);
        }
        Assert.assertEquals(inventoryValue, "0",
                "TC6 FAIL: inventory should be 0 for " + unifiedDate + ", found: " + inventoryValue);
        logStep("TC6 PASS");
    }

    // ── TC7: Admin direct booking calendar shows blocked date as disabled ──────
    @Test(priority = 7)
    public void verifyBlockedDateInDirectBookingCalendar() {
        logStep("TC7: admin direct booking shows " + checkinDate + " as disabled");
        openAdminPanel();
        clickAdminMenu("Reservations");
        pause(2000);

        WebElement createBtn = tryFindElement(By.xpath("//button[contains(.,'Create Booking')]"), 8);
        Assert.assertNotNull(createBtn, "TC7 FAIL: 'Create Booking' button not found");
        safeClick(createBtn);
        pause(1500);

        WebElement propSearch = findManualBookingPropertySearch();
        Assert.assertNotNull(propSearch, "TC7 FAIL: property search input not found");
        clearAndType(propSearch, PROPERTY_NAME);
        pause(1500);
        clickPropertyDropdownItem(PROPERTY_NAME);
        pause(1500);

        clearAndType(visibleElement(By.id("bookingContactDetail_name")),  CONTACT_NAME);
        clearAndType(visibleElement(By.id("bookingContactDetail_email")), CONTACT_EMAIL);
        clearAndType(visibleElement(By.id("bookingContactDetail_phone")), CONTACT_PHONE);
        clearAndType(visibleElement(By.id("totalAmount")),                BOOKING_AMOUNT);

        WebElement checkIn = tryFindElement(By.cssSelector("input[placeholder*='Check In']"), 8);
        Assert.assertNotNull(checkIn, "TC7 FAIL: check-in date input not found");
        clearAndType(checkIn, checkinDate);
        checkIn.sendKeys(Keys.ENTER);
        pause(1500);

        WebElement blockedCell = tryFindElement(By.cssSelector("td[title='" + checkinDate + "']"), 5);
        if (blockedCell == null)
            blockedCell = tryFindElement(By.cssSelector("td[title='" + blockedDate + "']"), 3);
        Assert.assertNotNull(blockedCell, "TC7 FAIL: date cell not found for " + checkinDate);

        String cellClass = blockedCell.getAttribute("class");
        Assert.assertTrue(cellClass != null && cellClass.contains("ant-picker-cell-disabled"),
                "TC7 FAIL: date should be disabled. Actual classes: " + cellClass);
        logStep("TC7 PASS");
    }

    // ── Login helpers ─────────────────────────────────────────────────────────

    private void openHostControlCenter() {
        driver.get(SIGNIN_URL);
        waitForDocumentReady();
        pause(1500);

        String url = driver.getCurrentUrl();
        boolean needsLogin = url != null && (url.contains("signup-signin") || url.contains("sign-in") || url.contains("login"));

        if (needsLogin) {
            HomePage  home  = new HomePage(driver, wait);
            LoginPage login = new LoginPage(driver, wait);
            OtpPage   otp   = new OtpPage(driver, wait);
            home.openLoginPopup();
            login.waitForLoginPage();
            login.enterPhoneNumber(HOST_PHONE);
            login.clickContinue();
            otp.waitForOtpPage();
            otp.enterOtp(HOST_OTP);
            otp.clickSubmit();
            wait.until(d -> {
                String u = d.getCurrentUrl();
                return u != null && !u.toLowerCase().contains("login") && !u.toLowerCase().contains("signup")
                        && d.findElements(By.cssSelector("input[name='pin'], input[name='otp']")).isEmpty();
            });
        }
        driver.get(HOST_PORTAL_URL);
        wait.until(ExpectedConditions.urlContains("host.homeyhutz.com"));
        waitForDocumentReady();
        pause(2000);
    }

    private void openAdminPanel() {
        driver.get(ADMIN_LOGIN_URL);
        waitForDocumentReady();
        pause(1500);

        boolean loginFormPresent = driver.findElements(By.id("email"))
                .stream().anyMatch(e -> { try { return e.isDisplayed(); } catch (Exception x) { return false; } });

        if (loginFormPresent) {
            clearAndType(visibleElement(By.id("email")),    ADMIN_EMAIL);
            clearAndType(visibleElement(By.id("password")), ADMIN_PASSWORD);
            clickableElement(By.cssSelector("button[type='submit']")).click();
            wait.until(d -> { String u = d.getCurrentUrl(); return u != null && !u.contains("/login"); });
            waitForDocumentReady();
            pause(2000);
        } else {
            String cur = driver.getCurrentUrl();
            if (cur == null || !cur.contains("admin.new.homeyhutz.com") || cur.contains("/login")) {
                driver.get(ADMIN_BASE_URL);
                waitForDocumentReady();
                pause(2000);
            }
        }
    }

    // ── Host portal helpers ───────────────────────────────────────────────────

    private void clickProperties() {
        for (By loc : new By[]{
            By.xpath("//a[normalize-space()='Properties']"),
            By.xpath("//button[normalize-space()='Properties']"),
            By.xpath("//*[normalize-space()='Properties' and (self::a or self::button or self::div or self::span)]")
        }) {
            WebElement e = tryFindElement(loc, 5);
            if (e != null) { safeClick(e); return; }
        }
        throw new RuntimeException("'Properties' menu item not found");
    }

    private WebElement findHostPropertySearchInput() {
        for (By loc : new By[]{
            By.cssSelector("input[placeholder='Search by Property Name, Id, City']"),
            By.cssSelector("input[placeholder*='Search by Property']")
        }) {
            WebElement e = tryFindElement(loc, 5);
            if (e != null) return e;
        }
        return null;
    }

    private void clickHostPropertyById(String id) {
        for (By loc : new By[]{
            By.xpath("//td[normalize-space()='" + id + "']"),
            By.xpath("//tr[.//td[contains(normalize-space(),'" + id + "')]][1]")
        }) {
            WebElement e = tryFindElement(loc, 8);
            if (e != null) { safeClick(e); return; }
        }
        throw new RuntimeException("Property row '" + id + "' not found");
    }

    private void clickCalendarAndPricingTab() {
        for (By loc : new By[]{
            By.xpath("//button[contains(.,'Calendar') and contains(.,'Pricing')]"),
            By.xpath("//a[contains(.,'Calendar') and contains(.,'Pricing')]"),
            By.xpath("//*[@role='tab'][contains(.,'Calendar')]"),
            By.xpath("//*[normalize-space()='Calendar & Pricing']")
        }) {
            WebElement tab = tryFindElement(loc, 5);
            if (tab != null) {
                safeClick(tab);
                try {
                    new WebDriverWait(driver, Duration.ofSeconds(10))
                            .until(d -> !d.findElements(By.cssSelector("td[data-date]")).isEmpty());
                    return;
                } catch (TimeoutException ignored) {}
            }
        }
        throw new RuntimeException("'Calendar & Pricing' tab not found");
    }

    private void scrollToDateAndClick(String date) {
        WebElement cell = wait.until(d -> {
            List<WebElement> cells = d.findElements(By.cssSelector("td[data-date='" + date + "']"));
            return cells.isEmpty() ? null : cells.get(0);
        });
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", cell);
        pause(600);
        try { cell.click(); } catch (Exception e) { jsClick(cell); }
    }

    private void scrollToDateOnly(String date) {
        WebElement cell = wait.until(d -> {
            List<WebElement> cells = d.findElements(By.cssSelector("td[data-date='" + date + "']"));
            return cells.isEmpty() ? null : cells.get(0);
        });
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", cell);
        pause(600);
    }

    private void clickPropertyDropdownItem(String term) {
        for (By loc : new By[]{
            By.xpath("//div[contains(@class,'flex') and contains(@class,'items-center') and contains(@class,'p-2') and contains(@class,'border-b') and contains(@class,'cursor-pointer') and contains(normalize-space(),'" + term + "')][1]"),
            By.xpath("//div[contains(@class,'cursor-pointer') and contains(normalize-space(),'" + term + "')][1]"),
            By.xpath("//*[contains(@class,'p-2') and contains(normalize-space(),'" + term + "')][1]")
        }) {
            WebElement e = tryFindElement(loc, 5);
            if (e != null) { safeClick(e); return; }
        }
        throw new RuntimeException("Property dropdown item not found for: " + term);
    }

    private WebElement findManualBookingPropertySearch() {
        for (By loc : new By[]{
            By.cssSelector("input[placeholder='Search by Property Id, Name']"),
            By.cssSelector("input[placeholder*='Search by Property']"),
            By.cssSelector("input.ant-input[placeholder]")
        }) {
            WebElement e = tryFindElement(loc, 5);
            if (e != null) return e;
        }
        return null;
    }

    // ── Admin helpers ─────────────────────────────────────────────────────────

    private void clickAdminMenu(String menu) {
        for (By loc : new By[]{
            By.xpath("//span[contains(@class,'ant-menu-title-content') and normalize-space()='" + menu + "']"),
            By.xpath("//li[contains(@class,'ant-menu') and .//span[normalize-space()='" + menu + "']]"),
            By.xpath("//*[normalize-space()='" + menu + "' and (self::a or self::span or self::li)]")
        }) {
            WebElement e = tryFindElement(loc, 8);
            if (e != null) { safeClick(e); return; }
        }
        throw new RuntimeException("Admin menu '" + menu + "' not found");
    }

    // ── Assertion helpers ─────────────────────────────────────────────────────

    private boolean isHostCalendarDateBlocked(String date) {
        List<WebElement> cells = driver.findElements(By.cssSelector("td[data-date='" + date + "']"));
        if (cells.isEmpty()) return false;
        WebElement cell = cells.stream()
                .filter(c -> { try { return c.isDisplayed(); } catch (Exception x) { return false; } })
                .findFirst().orElse(cells.get(0));
        // Blocked date renders as a FullCalendar event: <a class="... block-event ...">
        if (!cell.findElements(By.cssSelector("a.block-event")).isEmpty()) return true;
        // Fallback: any descendant span with text "Blocked"
        if (!cell.findElements(By.xpath(".//span[normalize-space()='Blocked']")).isEmpty()) return true;
        // JS fallback for cases where getText() returns empty on Ant/FC cells
        try {
            Object txt = ((JavascriptExecutor) driver).executeScript("return arguments[0].innerText;", cell);
            if (txt != null && txt.toString().contains("Blocked")) return true;
        } catch (Exception ignored) {}
        return false;
    }

    private boolean isUnifiedCalendarDateBlocked() {
        // Blocked cell: .uf_cal_date_box with bg-brand/10 ring-brand ring-inset classes
        // and an inner <span>Blocked</span> inside a bg-gray-400 rounded pill div
        for (WebElement box : driver.findElements(By.cssSelector(".uf_cal_date_box"))) {
            try {
                String cls = box.getAttribute("class");
                if (cls == null) continue;
                boolean hasBlockedStyle = cls.contains("bg-brand") || cls.contains("ring-brand");
                if (hasBlockedStyle) {
                    if (!box.findElements(By.xpath(".//span[normalize-space()='Blocked']")).isEmpty())
                        return true;
                    Object txt = ((JavascriptExecutor) driver)
                            .executeScript("return arguments[0].innerText;", box);
                    if (txt != null && txt.toString().contains("Blocked")) return true;
                }
            } catch (Exception ignored) {}
        }
        // Fallback: any uf_cal_date_box containing "Blocked" text regardless of class
        for (WebElement box : driver.findElements(By.cssSelector(".uf_cal_date_box"))) {
            try {
                Object txt = ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].innerText;", box);
                if (txt != null && txt.toString().contains("Blocked")) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }
}
