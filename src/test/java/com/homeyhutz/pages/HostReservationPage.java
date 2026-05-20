package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class HostReservationPage extends BasePage {

    private static final String UAT_WEB_BASE = "https://uat.homeyhutz.com";
    private static final String UAT_HOST_BASE = "https://uat.host.homeyhutz.com";
    private static final String UAT_HOST_DASHBOARD_URL = UAT_HOST_BASE + "/dashboard";
    private String lastAcceptedBookingId = "";
    private boolean lastAcceptClickSucceeded = false;

    private final By accountMenuIcon = By.cssSelector(".inline-flex > .flex");
    private final By hostControlCenterLink = By.xpath(
        "//*[self::a or self::button or self::div or self::span][normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))='host control center'" +
            " or normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))='host control centre']" +
            " | //*[self::a or self::button][.//*[self::span or self::div][normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))='host control center'" +
            " or normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))='host control centre']]"
    );
    private final By reservationSectionLink = By.xpath(
        "//a[contains(@href,'reservation') or contains(@href,'booking') or contains(@href,'request')]" +
            " | //button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'reservation')" +
            " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking')]" +
            " | //*[self::a or self::button or self::div or self::span][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'reservation')" +
            " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking')]"
    );
    private final By reservationsSidebarItem = By.xpath(
        "//*[self::a or self::button or self::div][.//span[normalize-space(.)='Reservations']]" +
            " | //span[normalize-space(.)='Reservations']/ancestor::*[self::a or self::button or self::div][1]"
    );
    private final By bookingRequestsLink = By.xpath(
        "//a[contains(@href,'bookingRequests') or contains(@href,'booking-requests') or contains(@href,'request')]" +
            " | //button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking request')" +
            " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'pending request')" +
            " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'requests')]" +
            " | //*[self::a or self::button or self::div or self::span][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking request')" +
            " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'pending request')" +
            " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'requests')]"
    );
    private final By bookingRequestsSidebarItem = By.xpath(
        "//*[self::a or self::button or self::div][contains(@class,'gap-x-2') and contains(@class,'items-center')" +
            " and contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking requests')]" +
            " | //*[self::a or self::button or self::div or self::span][starts-with(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking requests')]"
    );
    private final By bookingRequestsExactCard = By.xpath(
        "//div[contains(@class,'flex') and contains(@class,'gap-x-2') and contains(@class,'items-center')" +
            " and contains(@class,'px-2') and contains(@class,'py-1') and contains(@class,'mb-1')" +
            " and contains(@class,'rounded-md') and contains(@class,'hover:bg-gray-100') and contains(@class,'cursor-pointer')" +
            " and contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking requests')]"
    );
    private final By bookingRequestsActiveCard = By.xpath(
        "//div[contains(@class,'flex') and contains(@class,'gap-x-2') and contains(@class,'items-center')" +
            " and contains(@class,'px-2') and contains(@class,'py-1') and contains(@class,'mb-1')" +
            " and contains(@class,'rounded-md') and contains(@class,'cursor-pointer') and contains(@class,'bg-opacity-10')" +
            " and (contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking requests')" +
            " or starts-with(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking requests'))]"
    );
    private final By bookingRequestsCountCard = By.xpath(
        "//div[contains(@class,'flex') and contains(@class,'gap-x-2') and contains(@class,'items-center')" +
            " and contains(@class,'px-2') and contains(@class,'py-1') and contains(@class,'mb-1')" +
            " and contains(@class,'rounded-md') and contains(@class,'hover:bg-gray-100') and contains(@class,'cursor-pointer')" +
            " and starts-with(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking requests (')]"
    );
    private final By bookingIdSearchInput = By.xpath(
        "//input[@placeholder='Search by Booking Id']" +
            " | //input[contains(translate(@placeholder, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'search by booking id')]"
    );
    private final By reservationsExactSpan = By.xpath("//span[normalize-space(.)='Reservations']");
    private final By topReservationsNav = By.xpath(
        "//a[contains(@href,'/reservations') and contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'reservation')]" +
            " | //*[self::a or self::button or self::div or self::span][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'reservations')]"
    );
    private final By dashboardBookingRequestsTab = By.xpath(
        "//*[self::button or self::a or self::div or self::span][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking requests')]"
    );

    private final By reservationPageMarkers = By.xpath(
            "//*[self::h1 or self::h2 or self::h3 or self::a or self::button or self::div or self::span]" +
                    "[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'reservation')" +
                    " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking request')" +
                    " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'requests')]"
    );

    public HostReservationPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    private boolean isVisible(WebElement element) {
        try {
            return element != null && element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private void clickInViewport(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    private boolean waitForReservationPage(Duration timeout) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, timeout);
            return shortWait.until(d -> {
                String url = d.getCurrentUrl().toLowerCase();
                if (url.contains("reservation")
                        || url.contains("bookingrequest")
                        || url.contains("booking-request")
                        || (url.contains("uat.host.homeyhutz.com")
                        && (url.contains("request") || url.contains("booking") || url.contains("trip") || url.contains("reservation")))) {
                    return true;
                }

                List<WebElement> markers = d.findElements(reservationPageMarkers);
                for (WebElement marker : markers) {
                    if (marker.isDisplayed()) {
                        return true;
                    }
                }
                return false;
            });
        } catch (Exception e) {
            return false;
        }
    }

    private boolean clickFirstVisible(By locator, Duration timeout) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, timeout);
            return shortWait.until(d -> {
                List<WebElement> elements = d.findElements(locator);
                for (WebElement element : elements) {
                    if (!isVisible(element)) {
                        continue;
                    }
                    clickInViewport(element);
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            return false;
        }
    }

    private boolean clickByTextKeywords(String[] keywords, Duration timeout) {
        for (String keyword : keywords) {
            String lower = keyword.toLowerCase();
            By locator = By.xpath(
                    "//*[self::button or self::a or self::div or self::span or self::li or self::p or self::h1 or self::h2 or self::h3]" +
                            "[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + lower + "')]"
            );
            if (clickFirstVisible(locator, timeout)) {
                return true;
            }
        }
        return false;
    }

    private boolean clickReservationsTabExact(Duration timeout) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, timeout);
            return shortWait.until(d -> {
                try {
                    List<WebElement> spans = d.findElements(reservationsExactSpan);
                    for (WebElement span : spans) {
                        if (!isVisible(span)) {
                            continue;
                        }

                        WebElement clickable = span;
                        try {
                            clickable = span.findElement(By.xpath("ancestor::*[self::a or self::button or self::div[contains(@class,'cursor-pointer')]][1]"));
                        } catch (Exception ignored) {
                        }

                        clickInViewport(clickable);
                        return true;
                    }

                    JavascriptExecutor js = (JavascriptExecutor) d;
                    Object jsClicked = js.executeScript(
                            "const spans = Array.from(document.querySelectorAll('span'));"
                                    + "const target = spans.find(s => (s.textContent || '').trim().toLowerCase() === 'reservations');"
                                    + "if (!target) return false;"
                                    + "const clickable = target.closest('a,button,div.cursor-pointer,div') || target;"
                                    + "clickable.scrollIntoView({behavior:'smooth', block:'center'});"
                                    + "clickable.click();"
                                    + "return true;"
                    );
                    return Boolean.TRUE.equals(jsClicked);
                } catch (Exception ignored) {
                    return false;
                }
            });
        } catch (Exception e) {
            return false;
        }
    }

    private void dismissSignupInterruptionIfPresent() {
        By closeOrSkip = By.xpath(
                "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'close')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'skip')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'later')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'not now')]" +
                        " | //*[self::button or self::span][contains(normalize-space(.), '×') or normalize-space(.)='x' or normalize-space(.)='X']"
        );

        try {
            List<WebElement> actions = driver.findElements(closeOrSkip);
            for (WebElement action : actions) {
                if (!isVisible(action)) {
                    continue;
                }
                clickInViewport(action);
                Thread.sleep(300);
                return;
            }
        } catch (Exception ignored) {
        }
    }

    public void openReservationSection() {
        System.out.println("ACTION: HostReservationPage.openReservationSection - start navigation menu -> Host Control Center -> Reservations");

        System.out.println("ACTION: HostReservationPage.openReservationSection - checking and dismissing interruption popup if present");
        dismissSignupInterruptionIfPresent();

        System.out.println("ACTION: HostReservationPage.openReservationSection - clicking account menu icon");
        boolean menuOpened = clickFirstVisible(accountMenuIcon, Duration.ofSeconds(10));
        if (!menuOpened) {
            System.out.println("ACTION: HostReservationPage.openReservationSection - account menu icon not clickable, trying menu fallback locator");
            By menuTrigger = By.xpath(
                    "//button[contains(@aria-label,'menu') or contains(@aria-label,'Menu') or contains(@aria-label,'account') or contains(@aria-label,'Account')]"
                            + " | //*[self::button or self::a or self::div or self::span][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'menu')"
                            + " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'account')"
                            + " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'profile')"
                            + " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'my account')]"
            );
            menuOpened = clickFirstVisible(menuTrigger, Duration.ofSeconds(10));
        }

        if (!menuOpened) {
            throw new RuntimeException("Unable to open host menu. Current URL: " + driver.getCurrentUrl());
        }
        System.out.println("ACTION: HostReservationPage.openReservationSection - menu opened");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting before Host Control Center click", e);
        }

        System.out.println("ACTION: HostReservationPage.openReservationSection - clicking exact Host Control Center item");
        boolean hostControlCenterOpened = clickFirstVisible(hostControlCenterLink, Duration.ofSeconds(12));
        if (!hostControlCenterOpened) {
            System.out.println("ACTION: HostReservationPage.openReservationSection - exact Host Control Center click failed, trying JS exact text click");
            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                Object jsClicked = js.executeScript(
                        "const nodes = Array.from(document.querySelectorAll('a,button,div,span'));"
                                + "const target = nodes.find(n => {"
                                + "  const txt = (n.innerText || n.textContent || '').trim().toLowerCase();"
                                + "  return txt === 'host control center' || txt === 'host control centre';"
                                + "});"
                                + "if (!target) return false;"
                                + "target.scrollIntoView({behavior:'smooth', block:'center'});"
                                + "target.click();"
                                + "return true;"
                );
                hostControlCenterOpened = Boolean.TRUE.equals(jsClicked);
            } catch (Exception ignored) {
            }
        }

        if (!hostControlCenterOpened) {
            throw new RuntimeException("Unable to click Host Control Center from menu. Current URL: " + driver.getCurrentUrl());
        }
        System.out.println("ACTION: HostReservationPage.openReservationSection - Host Control Center clicked");

        String postHostClickUrl = driver.getCurrentUrl().toLowerCase();
        if (postHostClickUrl.contains("host-it-yourself")) {
            System.out.println("ACTION: HostReservationPage.openReservationSection - landed on host-it-yourself, retrying exact Host Control Center click");
            boolean menuReopened = clickFirstVisible(accountMenuIcon, Duration.ofSeconds(8));
            if (menuReopened) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while retrying Host Control Center click", e);
                }
                hostControlCenterOpened = clickFirstVisible(hostControlCenterLink, Duration.ofSeconds(10));
            }
        }

        for (String handle : driver.getWindowHandles()) {
            try {
                driver.switchTo().window(handle);
                String switchedUrl = driver.getCurrentUrl().toLowerCase();
                if (switchedUrl.contains("uat.host.homeyhutz.com") || switchedUrl.contains("host")) {
                    System.out.println("ACTION: HostReservationPage.openReservationSection - switched to host-related window/tab: " + switchedUrl);
                    break;
                }
            } catch (Exception ignored) {
            }
        }

        String currentUrlAfterHostCenter = driver.getCurrentUrl().toLowerCase();
        if (!currentUrlAfterHostCenter.contains("uat.host.homeyhutz.com") &&
                !currentUrlAfterHostCenter.contains("/dashboard") &&
                !currentUrlAfterHostCenter.contains("reservations")) {
            System.out.println("ACTION: HostReservationPage.openReservationSection - Host Control Center landed on non-host page, opening host dashboard URL");
            driver.get(UAT_HOST_DASHBOARD_URL);
        }

        System.out.println("ACTION: HostReservationPage.openReservationSection - clicking Reservations tab exact locator");
        boolean reservationSectionOpened = clickReservationsTabExact(Duration.ofSeconds(12));
        if (!reservationSectionOpened) {
            System.out.println("ACTION: HostReservationPage.openReservationSection - exact Reservations tab click failed, trying top nav locator");
            reservationSectionOpened = clickFirstVisible(topReservationsNav, Duration.ofSeconds(12));
        }
        if (!reservationSectionOpened) {
            System.out.println("ACTION: HostReservationPage.openReservationSection - top nav click failed, trying sidebar Reservations item");
            reservationSectionOpened = clickFirstVisible(reservationsSidebarItem, Duration.ofSeconds(12));
        }
        if (!reservationSectionOpened) {
            System.out.println("ACTION: HostReservationPage.openReservationSection - sidebar click failed, trying reservation section link locator");
            reservationSectionOpened = clickFirstVisible(reservationSectionLink, Duration.ofSeconds(12));
        }
        if (!reservationSectionOpened) {
            System.out.println("ACTION: HostReservationPage.openReservationSection - link locator failed, trying text keyword fallback for reservations");
            reservationSectionOpened = clickByTextKeywords(
                    new String[]{"reservations", "reservation"},
                    Duration.ofSeconds(12)
            );
        }

        if (!reservationSectionOpened || !waitForReservationPage(Duration.ofSeconds(12))) {
            throw new RuntimeException(
                    "Unable to open Reservations from Host Control Center. Current URL: " + driver.getCurrentUrl() +
                            ", title: " + driver.getTitle()
            );
        }

        System.out.println("ACTION: HostReservationPage.openReservationSection - Reservations opened successfully");
    }

    public boolean isOnReservationsPage() {
        System.out.println("ACTION: HostReservationPage.isOnReservationsPage - verifying reservations page markers");
        boolean loaded = waitForReservationPage(Duration.ofSeconds(10));
        System.out.println("ACTION: HostReservationPage.isOnReservationsPage - result=" + loaded + ", currentUrl=" + driver.getCurrentUrl());
        return loaded;
    }

    public void clickBookingRequestsCard() {
        System.out.println("ACTION: HostReservationPage.clickBookingRequestsCard - attempting to open Booking Requests card");
        boolean opened = clickExactBookingRequestsSidebarCard();
        if (!opened) {
            opened = clickFirstVisible(bookingRequestsActiveCard, Duration.ofSeconds(8));
        }
        if (!opened) {
            opened = clickFirstVisible(bookingRequestsExactCard, Duration.ofSeconds(8));
        }
        if (!opened) {
            opened = clickFirstVisible(dashboardBookingRequestsTab, Duration.ofSeconds(8));
        }
        if (!opened) {
            opened = clickFirstVisible(bookingRequestsSidebarItem, Duration.ofSeconds(8));
        }
        if (!opened) {
            opened = clickFirstVisible(bookingRequestsLink, Duration.ofSeconds(8));
        }
        if (!opened) {
            opened = clickByTextKeywords(new String[]{"booking requests", "booking request", "requests"}, Duration.ofSeconds(8));
        }

        if (!opened) {
            throw new RuntimeException("Unable to click Booking Requests card. Current URL: " + driver.getCurrentUrl());
        }

        System.out.println("ACTION: HostReservationPage.clickBookingRequestsCard - Booking Requests card click succeeded");

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            shortWait.until(d -> {
                String url = d.getCurrentUrl().toLowerCase();
                if (url.contains("booking") || url.contains("request")) {
                    return true;
                }
                List<WebElement> inputs = d.findElements(bookingIdSearchInput);
                for (WebElement input : inputs) {
                    if (input.isDisplayed()) {
                        return true;
                    }
                }
                return false;
            });
        } catch (Exception ignored) {
        }
    }

    public void openBookingRequestsAndSearch(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId cannot be blank");
        }

        System.out.println("ACTION: HostReservationPage.openBookingRequestsAndSearch - bookingId=" + bookingId);
        clickBookingRequestsCard();
        searchBookingRequestId(bookingId);
        System.out.println("ACTION: HostReservationPage.openBookingRequestsAndSearch - completed search for bookingId=" + bookingId);
    }

    public void clickSearchedBookingId(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId cannot be blank");
        }

        String normalized = bookingId.trim();
        String hhVariant = normalized.toUpperCase().startsWith("HH") ? normalized : "HH" + normalized;
        String safeId = normalized.replace("'", "\\'");
        String safeHh = hhVariant.replace("'", "\\'");
        System.out.println("ACTION: HostReservationPage.clickSearchedBookingId - attempting to click searched booking request row for booking ID=" + normalized);

        By exactRowByBookingId = By.xpath(
                "//tr[.//td[normalize-space(.)='" + safeId + "'] or .//span[normalize-space(.)='" + safeId + "'] or .//a[normalize-space(.)='" + safeId + "']]"
                + " | //tr[.//td[normalize-space(.)='" + safeHh + "'] or .//span[normalize-space(.)='" + safeHh + "'] or .//a[normalize-space(.)='" + safeHh + "']]"
                + " | //*[(self::div or self::li or self::article or self::section) and (.//*[normalize-space(.)='" + safeId + "'] or .//*[normalize-space(.)='" + safeHh + "'])]"
        );

        By exactIdElement = By.xpath(
            "//*[self::td or self::span or self::a or self::div][normalize-space(.)='" + safeId + "' or normalize-space(.)='" + safeHh + "']"
        );

        By filteredResultRows = By.xpath(
            "//tr[.//*[contains(normalize-space(.), '" + safeId + "') or contains(normalize-space(.), '" + safeHh + "')]]"
                + " | //*[(self::div or self::li or self::article or self::section) and .//*[contains(normalize-space(.), '" + safeId + "') or contains(normalize-space(.), '" + safeHh + "')]]"
        );

        boolean opened = false;
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(12));
            opened = shortWait.until(d -> {
                List<WebElement> rows = d.findElements(exactRowByBookingId);
                for (WebElement row : rows) {
                    try {
                        if (!row.isDisplayed()) {
                            continue;
                        }

                        List<WebElement> bookingDetailsCells = row.findElements(By.xpath(
                                ".//td[1] | .//*[self::td or self::div or self::span][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '#')][1]"
                        ));
                        if (!bookingDetailsCells.isEmpty() && bookingDetailsCells.get(0).isDisplayed()) {
                            clickInViewport(bookingDetailsCells.get(0));
                            return true;
                        }

                        List<WebElement> rowClickables = row.findElements(By.xpath(
                                ".//*[self::a or self::button or @role='button']"
                        ));
                        if (!rowClickables.isEmpty() && rowClickables.get(0).isDisplayed()) {
                            clickInViewport(rowClickables.get(0));
                            return true;
                        }

                        clickInViewport(row);
                        return true;
                    } catch (Exception ignored) {
                    }
                }

                List<WebElement> idElements = d.findElements(exactIdElement);
                for (WebElement idEl : idElements) {
                    try {
                        if (!idEl.isDisplayed()) {
                            continue;
                        }
                        clickInViewport(idEl);
                        return true;
                    } catch (Exception ignored) {
                    }
                }

                List<WebElement> filteredRows = d.findElements(filteredResultRows);
                for (WebElement row : filteredRows) {
                    try {
                        if (!row.isDisplayed()) {
                            continue;
                        }

                        List<WebElement> bookingDetailsCells = row.findElements(By.xpath(
                                ".//td[1] | .//*[self::td or self::div or self::span][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '#')][1]"
                        ));
                        if (!bookingDetailsCells.isEmpty() && bookingDetailsCells.get(0).isDisplayed()) {
                            clickInViewport(bookingDetailsCells.get(0));
                            return true;
                        }

                        List<WebElement> clickables = row.findElements(By.xpath(".//*[self::a or self::button or @role='button']"));
                        if (!clickables.isEmpty() && clickables.get(0).isDisplayed()) {
                            clickInViewport(clickables.get(0));
                            return true;
                        }

                        clickInViewport(row);
                        return true;
                    } catch (Exception ignored) {
                    }
                }
                return false;
            });
        } catch (Exception ignored) {
        }

        if (!opened) {
            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                Object jsClicked = js.executeScript(
                        "const bookingId = arguments[0].toLowerCase();"
                                + "const hh = ('hh' + bookingId).toLowerCase();"
                                + "const rows = Array.from(document.querySelectorAll('tr, div, li, article, section'));"
                                + "for (const row of rows) {"
                                + "  const txt = (row.innerText || row.textContent || '').toLowerCase();"
                                + "  if (!(txt.includes(bookingId) || txt.includes(hh))) continue;"
                        + "  const detailsCell = row.querySelector('td:first-child');"
                        + "  const clickable = detailsCell || row.querySelector('a,button,[role=\"button\"]') || row;"
                                + "  clickable.scrollIntoView({behavior:'smooth', block:'center'});"
                                + "  clickable.click();"
                                + "  return true;"
                                + "}"
                                + "return false;",
                        normalized
                );
                opened = Boolean.TRUE.equals(jsClicked);
            } catch (Exception ignored) {
            }
        }

        if (!opened) {
            throw new RuntimeException("Unable to click searched booking ID: " + normalized + ". Current URL: " + driver.getCurrentUrl());
        }

        System.out.println("ACTION: HostReservationPage.clickSearchedBookingId - clicked searched booking request row for booking ID=" + normalized);
    }

    public void clickAcceptOnCurrentBooking() {
        System.out.println("ACTION: HostReservationPage.clickAcceptOnCurrentBooking - attempting accept on current booking view");

        By acceptAction = By.xpath(
                "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')"
                        + " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'approve')"
                        + " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'confirm')]"
                        + " | //a[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')"
                        + " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'approve')]"
                        + " | //*[(self::button or self::a or self::div or self::span) and @data-testid and"
                        + " (contains(translate(@data-testid, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')"
                        + " or contains(translate(@data-testid, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'approve')"
                        + " or contains(translate(@data-testid, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'confirm'))]"
        );

        By confirmAction = By.xpath(
                "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'confirm')"
                        + " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'yes')"
                        + " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')]"
        );

        boolean clicked = false;
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(12));
            clicked = shortWait.until(d -> {
                List<WebElement> actions = d.findElements(acceptAction);
                for (WebElement action : actions) {
                    try {
                        if (!action.isDisplayed()) {
                            continue;
                        }
                        clickInViewport(action);
                        return true;
                    } catch (Exception ignored) {
                    }
                }
                return false;
            });
        } catch (Exception ignored) {
        }

        if (!clicked) {
            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                Object jsClicked = js.executeScript(
                        "const candidates = Array.from(document.querySelectorAll('button, a, [role=\"button\"], div, span'));"
                                + "for (const el of candidates) {"
                                + "  const txt = ((el.innerText || el.textContent || '') + ' ' + (el.getAttribute('aria-label') || '') + ' ' + (el.getAttribute('data-testid') || '')).toLowerCase();"
                                + "  const ok = txt.includes('accept') || txt.includes('approve') || txt.includes('confirm');"
                                + "  if (!ok) continue;"
                                + "  const style = window.getComputedStyle(el);"
                                + "  const visible = style && style.display !== 'none' && style.visibility !== 'hidden' && el.offsetParent !== null;"
                                + "  if (!visible) continue;"
                                + "  el.scrollIntoView({behavior:'smooth', block:'center'});"
                                + "  el.click();"
                                + "  return true;"
                                + "}"
                                + "return false;"
                );
                clicked = Boolean.TRUE.equals(jsClicked);
            } catch (Exception ignored) {
            }
        }

        if (!clicked) {
            throw new RuntimeException("Unable to click Accept on current booking view. URL: " + driver.getCurrentUrl());
        }

        System.out.println("ACTION: HostReservationPage.clickAcceptOnCurrentBooking - accept clicked");

        try {
            WebDriverWait confirmWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            confirmWait.until(d -> {
                List<WebElement> buttons = d.findElements(confirmAction);
                for (WebElement button : buttons) {
                    try {
                        if (!button.isDisplayed()) {
                            continue;
                        }
                        clickInViewport(button);
                        System.out.println("ACTION: HostReservationPage.clickAcceptOnCurrentBooking - confirm clicked");
                        return true;
                    } catch (Exception ignored) {
                    }
                }
                return false;
            });
        } catch (Exception ignored) {
        }
    }

    public void clickAllTab() {
        System.out.println("ACTION: HostReservationPage.clickAllTab - attempting to open All tab");
        By leftSidebarAllTab = By.xpath(
            "//div[contains(@class,'cursor-pointer') and .//span[starts-with(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'all (')]]"
                + " | //*[self::a or self::button or self::div][starts-with(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'all (')]"
        );

        By allTab = By.xpath(
            "//*[self::button or self::a or self::div or self::span][normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))='all']"
                + " | //*[self::button or self::a or self::div][.//*[self::span or self::div][normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))='all']]"
        );

        boolean clicked = clickFirstVisible(leftSidebarAllTab, Duration.ofSeconds(8));
        if (!clicked) {
            clicked = clickFirstVisible(allTab, Duration.ofSeconds(8));
        }
        if (!clicked) {
            clicked = clickByTextKeywords(new String[]{"all"}, Duration.ofSeconds(8));
        }

        if (!clicked) {
            throw new RuntimeException("Unable to click All tab. Current URL: " + driver.getCurrentUrl());
        }

        System.out.println("ACTION: HostReservationPage.clickAllTab - All tab clicked");
    }

    public void searchBookingIdInCurrentReservationsView(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId cannot be blank");
        }

        String normalized = bookingId.trim();
        System.out.println("ACTION: HostReservationPage.searchBookingIdInCurrentReservationsView - searching bookingId=" + normalized);

        By reservationsSearchInput = By.xpath(
                "//input[contains(translate(@placeholder, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'search by booking')]"
                        + " | //input[contains(translate(@placeholder, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'search by bookin')]"
                        + " | //input[contains(translate(@name, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'search')]"
        );

        boolean searched = false;
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement searchInput = shortWait.until(d -> {
                List<WebElement> inputs = d.findElements(reservationsSearchInput);
                for (WebElement input : inputs) {
                    if (input.isDisplayed() && input.isEnabled()) {
                        return input;
                    }
                }
                return null;
            });

            for (int attempt = 0; attempt < 3; attempt++) {
                try {
                    if (attempt > 0) {
                        searchInput = driver.findElements(reservationsSearchInput).stream()
                                .filter(el -> el.isDisplayed() && el.isEnabled())
                                .findFirst()
                                .orElse(searchInput);
                    }

                    clickInViewport(searchInput);
                    searchInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                    searchInput.sendKeys(Keys.DELETE);
                    for (char ch : normalized.toCharArray()) {
                        searchInput.sendKeys(String.valueOf(ch));
                        Thread.sleep(80);
                    }

                    String actual = searchInput.getAttribute("value");
                    if (normalized.equals(actual)) {
                        triggerBookingSearchSubmit(searchInput);
                        searched = true;
                        break;
                    }

                    System.out.println("WARN: Reservations All-tab search value mismatch on attempt " + (attempt + 1)
                            + ". Expected=" + normalized + ", actual=" + actual);
                } catch (Exception ignored) {
                }
            }

            if (!searched) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                Object jsSearched = js.executeScript(
                        "const val = arguments[0];"
                                + "const inputs = Array.from(document.querySelectorAll('input'));"
                                + "const target = inputs.find(i => {"
                                + "  const p = (i.getAttribute('placeholder') || '').toLowerCase();"
                                + "  const n = (i.getAttribute('name') || '').toLowerCase();"
                                + "  return p.includes('search by booking') || p.includes('search by bookin') || n.includes('search');"
                                + "});"
                                + "if (!target) return false;"
                                + "target.scrollIntoView({behavior:'smooth', block:'center'});"
                                + "target.focus();"
                                + "const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;"
                                + "setter.call(target, '');"
                                + "target.dispatchEvent(new Event('input', {bubbles:true}));"
                                + "setter.call(target, val);"
                                + "target.dispatchEvent(new Event('input', {bubbles:true}));"
                                + "target.dispatchEvent(new KeyboardEvent('keydown', {key:'Enter', code:'Enter', keyCode:13, which:13, bubbles:true}));"
                                + "target.dispatchEvent(new KeyboardEvent('keyup', {key:'Enter', code:'Enter', keyCode:13, which:13, bubbles:true}));"
                                + "target.dispatchEvent(new Event('change', {bubbles:true}));"
                                + "return (target.value || '') === val;",
                        normalized
                );
                searched = Boolean.TRUE.equals(jsSearched);
            }
        } catch (Exception ignored) {
        }

        if (!searched) {
            throw new RuntimeException("Unable to search booking ID in current reservations view: " + normalized + ". Current URL: " + driver.getCurrentUrl());
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting after searching booking ID in reservations view", e);
        }

        System.out.println("ACTION: HostReservationPage.searchBookingIdInCurrentReservationsView - search submitted for bookingId=" + normalized);
    }

    private boolean clickExactBookingRequestsSidebarCard() {
        boolean clicked = clickFirstVisible(bookingRequestsCountCard, Duration.ofSeconds(6));
        if (clicked) {
            return true;
        }

        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object jsClicked = js.executeScript(
                    "const nodes = Array.from(document.querySelectorAll('div.flex.gap-x-2.items-center.px-2.py-1.mb-1.rounded-md.cursor-pointer, div.flex.gap-x-2.items-center.px-2.py-1.mb-1.rounded-md.hover\\:bg-gray-100.cursor-pointer'));"
                            + "const target = nodes.find(n => ((n.innerText || n.textContent || '').trim().toLowerCase().startsWith('booking requests (')));"
                            + "if (!target) return false;"
                            + "target.scrollIntoView({behavior:'smooth', block:'center'});"
                            + "target.click();"
                            + "return true;"
            );
            return Boolean.TRUE.equals(jsClicked);
        } catch (Exception ignored) {
            return false;
        }
    }

    private void openBookingRequestsOrPendingTabIfPresent() {
        System.out.println("ACTION: HostReservationPage.openBookingRequestsOrPendingTabIfPresent - checking Booking Requests/Pending tab");
        if (clickExactBookingRequestsSidebarCard()) {
            System.out.println("ACTION: HostReservationPage.openBookingRequestsOrPendingTabIfPresent - clicked exact Booking Requests sidebar card");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting after Booking Requests card click", e);
            }
            return;
        }

        By requestsOrPendingTab = By.xpath(
                "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking request')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'request')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'pending')]" +
                        " | //a[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking request')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'request')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'pending')]"
        );

        try {
            List<WebElement> tabs = driver.findElements(requestsOrPendingTab);
            for (WebElement tab : tabs) {
                if (!tab.isDisplayed()) {
                    continue;
                }
                clickInViewport(tab);
                System.out.println("ACTION: HostReservationPage.openBookingRequestsOrPendingTabIfPresent - clicked tab candidate");
                Thread.sleep(500);
                return;
            }
        } catch (Exception ignored) {
        }
    }

    private void searchBookingRequestId(String bookingId) {
        System.out.println("ACTION: HostReservationPage.searchBookingRequestId - start bookingId=" + bookingId);
        List<String> variants = bookingIdVariants(bookingId);
        if (variants.isEmpty()) {
            System.out.println("ACTION: HostReservationPage.searchBookingRequestId - no bookingId variants, skipping");
            return;
        }

        try {
            boolean bookingRequestsReady = openBookingRequestsSectionBeforeSearch();
            if (!bookingRequestsReady) {
                throw new RuntimeException("Booking Requests tab/input not ready before search for ID: " + bookingId);
            }
            System.out.println("ACTION: HostReservationPage.searchBookingRequestId - Booking Requests tab/input ready");

            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement searchInput = shortWait.until(d -> {
                List<WebElement> inputs = d.findElements(bookingIdSearchInput);
                for (WebElement input : inputs) {
                    if (input.isDisplayed() && input.isEnabled()) {
                        return input;
                    }
                }
                return null;
            });

            clickInViewport(searchInput);
            typeSearchValueReliably(searchInput, variants.get(0));
            triggerBookingSearchSubmit(searchInput);
            System.out.println("ACTION: HostReservationPage.searchBookingRequestId - search submitted for value=" + variants.get(0));
            Thread.sleep(800);

            String actual = searchInput.getAttribute("value");
            if (actual == null || !actual.trim().equals(variants.get(0))) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript(
                        "arguments[0].focus();"
                                + "arguments[0].dispatchEvent(new KeyboardEvent('keydown', {key:'Enter', code:'Enter', keyCode:13, which:13, bubbles:true}));"
                                + "arguments[0].dispatchEvent(new KeyboardEvent('keyup', {key:'Enter', code:'Enter', keyCode:13, which:13, bubbles:true}));"
                                + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                        searchInput
                );
                Thread.sleep(900);
            }
            System.out.println("ACTION: HostReservationPage.searchBookingRequestId - completed bookingId search cycle");
        } catch (Exception e) {
            throw new RuntimeException("Failed to search Booking Request ID after opening Booking Requests tab: " + bookingId, e);
        }
    }

    private void triggerBookingSearchSubmit(WebElement searchInput) {
        try {
            System.out.println("ACTION: HostReservationPage.triggerBookingSearchSubmit - submitting with ENTER key");
            searchInput.sendKeys(Keys.ENTER);
            return;
        } catch (Exception ignored) {
        }

        try {
            System.out.println("ACTION: HostReservationPage.triggerBookingSearchSubmit - ENTER key failed, using JS keyboard events");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    "arguments[0].focus();"
                            + "arguments[0].dispatchEvent(new KeyboardEvent('keydown', {key:'Enter', code:'Enter', keyCode:13, which:13, bubbles:true}));"
                            + "arguments[0].dispatchEvent(new KeyboardEvent('keyup', {key:'Enter', code:'Enter', keyCode:13, which:13, bubbles:true}));"
                            + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                    searchInput
            );
        } catch (Exception ignored) {
            System.out.println("ACTION: HostReservationPage.triggerBookingSearchSubmit - JS submit fallback failed");
        }
    }

    private void typeSearchValueReliably(WebElement input, String value) {
        String expected = value == null ? "" : value.trim();
        if (expected.isBlank()) {
            return;
        }

        System.out.println("INFO: Booking ID search expected value: " + expected);

        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                WebElement activeInput = input;
                if (attempt > 0) {
                    activeInput = driver.findElements(bookingIdSearchInput).stream()
                            .filter(el -> el.isDisplayed() && el.isEnabled())
                            .findFirst()
                            .orElse(input);
                    clickInViewport(activeInput);
                }

                activeInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                activeInput.sendKeys(Keys.DELETE);
                for (char ch : expected.toCharArray()) {
                    activeInput.sendKeys(String.valueOf(ch));
                    Thread.sleep(80);
                }

                String actual = activeInput.getAttribute("value");
                if (expected.equals(actual)) {
                    System.out.println("INFO: Booking ID typed successfully on attempt " + (attempt + 1) + ". Actual value: " + actual);
                    return;
                }
                System.out.println("WARN: Booking ID typing mismatch on attempt " + (attempt + 1) + ". Expected: " + expected + ", actual: " + actual);
            } catch (Exception ignored) {
            }
        }

        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
            "const el = arguments[0];"
                + "const value = arguments[1];"
                + "el.focus();"
                + "const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;"
                + "setter.call(el, '');"
                + "el.dispatchEvent(new Event('input', {bubbles:true}));"
                + "setter.call(el, value);"
                + "el.dispatchEvent(new Event('input', {bubbles:true}));"
                + "el.dispatchEvent(new Event('change', {bubbles:true}));"
                + "el.dispatchEvent(new KeyboardEvent('keydown', {key:'Enter', code:'Enter', keyCode:13, which:13, bubbles:true}));"
                + "el.dispatchEvent(new KeyboardEvent('keyup', {key:'Enter', code:'Enter', keyCode:13, which:13, bubbles:true}));",
            input, expected
        );

            String finalValue = input.getAttribute("value");
            System.out.println("INFO: Booking ID value after JS fallback: " + finalValue);

        if (!expected.equals(finalValue)) {
        WebElement activeInput = driver.findElements(bookingIdSearchInput).stream()
            .filter(el -> el.isDisplayed() && el.isEnabled())
            .findFirst()
            .orElse(input);
        clickInViewport(activeInput);
        activeInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        activeInput.sendKeys(Keys.DELETE);
        for (char ch : expected.toCharArray()) {
            activeInput.sendKeys(String.valueOf(ch));
            Thread.sleep(120);
        }
        System.out.println("INFO: Booking ID value after slow typing fallback: " + activeInput.getAttribute("value"));
        }
        } catch (Exception ignored) {
        }
    }

    private boolean openBookingRequestsSectionBeforeSearch() {
        try {
            for (int attempt = 0; attempt < 3; attempt++) {
                System.out.println("ACTION: HostReservationPage.openBookingRequestsSectionBeforeSearch - attempt " + (attempt + 1));
                boolean bookingRequestsOpened = clickExactBookingRequestsSidebarCard();
                if (!bookingRequestsOpened) {
                    bookingRequestsOpened = clickFirstVisible(bookingRequestsCountCard, Duration.ofSeconds(8));
                }
                if (!bookingRequestsOpened) {
                    bookingRequestsOpened = clickFirstVisible(bookingRequestsExactCard, Duration.ofSeconds(8));
                }
                if (!bookingRequestsOpened) {
                    bookingRequestsOpened = clickFirstVisible(dashboardBookingRequestsTab, Duration.ofSeconds(8));
                }
                if (!bookingRequestsOpened) {
                    bookingRequestsOpened = clickFirstVisible(bookingRequestsSidebarItem, Duration.ofSeconds(8));
                }
                if (!bookingRequestsOpened) {
                    bookingRequestsOpened = clickFirstVisible(bookingRequestsLink, Duration.ofSeconds(8));
                }
                if (!bookingRequestsOpened) {
                    bookingRequestsOpened = clickByTextKeywords(
                            new String[]{"booking requests", "booking request", "requests"},
                            Duration.ofSeconds(8)
                    );
                }

                if (!bookingRequestsOpened) {
                    System.out.println("ACTION: HostReservationPage.openBookingRequestsSectionBeforeSearch - could not open Booking Requests on attempt " + (attempt + 1));
                    continue;
                }

                openBookingRequestsOrPendingTabIfPresent();

                try {
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));
                    boolean inputReady = shortWait.until(d -> {
                        List<WebElement> inputs = d.findElements(bookingIdSearchInput);
                        for (WebElement input : inputs) {
                            if (input.isDisplayed() && input.isEnabled()) {
                                clickInViewport(input);
                                return true;
                            }
                        }
                        return false;
                    });

                    if (inputReady) {
                        System.out.println("ACTION: HostReservationPage.openBookingRequestsSectionBeforeSearch - booking search input ready on attempt " + (attempt + 1));
                        return true;
                    }
                } catch (Exception ignored) {
                }

                try {
                    System.out.println("ACTION: HostReservationPage.openBookingRequestsSectionBeforeSearch - input not ready, refreshing page");
                    driver.navigate().refresh();
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            }

            System.out.println("ACTION: HostReservationPage.openBookingRequestsSectionBeforeSearch - failed after max attempts");
            return false;
        } catch (Exception e) {
            System.out.println("ACTION: HostReservationPage.openBookingRequestsSectionBeforeSearch - exception: " + e.getClass().getSimpleName());
            return false;
        }
    }

    private List<String> bookingIdVariants(String bookingId) {
        java.util.LinkedHashSet<String> variants = new java.util.LinkedHashSet<>();
        String normalized = bookingId == null ? "" : bookingId.trim();
        if (normalized.isBlank()) {
            return java.util.Collections.emptyList();
        }

        variants.add(normalized);
        variants.add(normalized.toUpperCase());

        if (normalized.matches("^\\d+$")) {
            variants.add("HH" + normalized);
        }

        if (normalized.toUpperCase().startsWith("HH") && normalized.length() > 2) {
            variants.add(normalized.substring(2));
        }

        return new java.util.ArrayList<>(variants);
    }

    private void nudgeBookingRequestsListScroll() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    "window.scrollBy(0, Math.max(window.innerHeight * 0.55, 220));"
                            + "const nodes = Array.from(document.querySelectorAll('*'));"
                            + "for (const node of nodes) {"
                            + "  const style = window.getComputedStyle(node);"
                            + "  const overflowY = style.overflowY;"
                            + "  const canScroll = (overflowY === 'auto' || overflowY === 'scroll') && node.scrollHeight > node.clientHeight + 8;"
                            + "  if (canScroll) {"
                            + "    node.scrollTop = Math.min(node.scrollTop + Math.max(node.clientHeight * 0.55, 180), node.scrollHeight);"
                            + "  }"
                            + "}"
            );
        } catch (Exception ignored) {
        }
    }

    private boolean clickAcceptActionInMatchingRow(String bookingId) {
        System.out.println("ACTION: HostReservationPage.clickAcceptActionInMatchingRow - start bookingId=" + bookingId);
        String normalized = bookingId == null ? "" : bookingId.trim();
        if (normalized.isBlank()) {
            System.out.println("ACTION: HostReservationPage.clickAcceptActionInMatchingRow - no variants available");
            return false;
        }

        By rowAcceptAction = By.xpath(
                ".//*[self::button or self::a or @role='button' or self::div or self::span]"
                        + "[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')"
                        + " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'approve')"
                        + " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'confirm')"
                        + " or contains(translate(@aria-label, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')"
                        + " or contains(translate(@aria-label, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'approve')"
                        + " or contains(translate(@aria-label, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'confirm')"
                        + " or contains(translate(@data-testid, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')"
                        + " or contains(translate(@data-testid, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'approve')"
                        + " or contains(translate(@data-testid, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'confirm')]"
        );

        List<String> exactOnly = java.util.Collections.singletonList(normalized);
        for (String idVariant : exactOnly) {
            String safeId = idVariant.replace("'", "\\'");
            By matchingRows = By.xpath(
                "//tr[.//*[normalize-space(.)='" + safeId + "']]"
                            + " | //*[(self::div or self::li or self::article or self::section)"
                    + " and (.//*[normalize-space(.)='" + safeId + "'])]"
            );

            List<WebElement> rows = driver.findElements(matchingRows);
            for (WebElement row : rows) {
                try {
                    if (!row.isDisplayed()) {
                        continue;
                    }

                    List<WebElement> actions = row.findElements(rowAcceptAction);
                    for (WebElement action : actions) {
                        if (!action.isDisplayed()) {
                            continue;
                        }
                        clickInViewport(action);
                        System.out.println("ACTION: HostReservationPage.clickAcceptActionInMatchingRow - clicked row action by label/attribute for id=" + idVariant);
                        return true;
                    }

                    List<WebElement> candidateButtons = row.findElements(By.xpath(".//*[self::button or self::a or @role='button']"));
                    for (WebElement candidate : candidateButtons) {
                        if (!candidate.isDisplayed()) {
                            continue;
                        }

                        String descriptor = (
                                (candidate.getText() == null ? "" : candidate.getText()) + " "
                                        + (candidate.getAttribute("aria-label") == null ? "" : candidate.getAttribute("aria-label")) + " "
                                        + (candidate.getAttribute("data-testid") == null ? "" : candidate.getAttribute("data-testid")) + " "
                                        + (candidate.getAttribute("class") == null ? "" : candidate.getAttribute("class"))
                        ).toLowerCase();

                        boolean positive = descriptor.contains("accept")
                                || descriptor.contains("approve")
                                || descriptor.contains("confirm")
                                || descriptor.contains("success")
                                || descriptor.contains("green")
                                || descriptor.contains("check")
                                || descriptor.contains("tick");

                        boolean negative = descriptor.contains("reject")
                                || descriptor.contains("decline")
                                || descriptor.contains("cancel")
                                || descriptor.contains("delete")
                                || descriptor.contains("remove");

                        if (positive && !negative) {
                            clickInViewport(candidate);
                            System.out.println("ACTION: HostReservationPage.clickAcceptActionInMatchingRow - clicked row action by heuristic descriptor for id=" + idVariant);
                            return true;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        System.out.println("ACTION: HostReservationPage.clickAcceptActionInMatchingRow - no row-level accept action found");
        return false;
    }

    private boolean openBookingDetailsByBookingId(String bookingId) {
        System.out.println("ACTION: HostReservationPage.openBookingDetailsByBookingId - start bookingId=" + bookingId);
        String normalized = bookingId == null ? "" : bookingId.trim();
        if (normalized.isBlank()) {
            System.out.println("ACTION: HostReservationPage.openBookingDetailsByBookingId - no exact bookingId available");
            return false;
        }

        List<String> variants = java.util.Collections.singletonList(normalized);

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(15));
            return shortWait.until(d -> {
                for (String idVariant : variants) {
                    String safeId = idVariant.replace("'", "\\'");

                    By exactRowByBookingId = By.xpath(
                        "//tr[.//td[normalize-space(.)='" + safeId + "'] or .//span[normalize-space(.)='" + safeId + "'] or .//a[normalize-space(.)='" + safeId + "']]"
                    );

                    By exactIdCell = By.xpath(
                            "//tr[.//*[normalize-space(.)='" + safeId + "']]//*[normalize-space(.)='" + safeId + "']"
                                    + " | //*[(self::td or self::div or self::span or self::a) and normalize-space(.)='" + safeId + "']"
                    );

                    List<WebElement> exactRows = d.findElements(exactRowByBookingId);
                    for (WebElement row : exactRows) {
                        try {
                            if (!row.isDisplayed()) {
                                continue;
                            }

                            List<WebElement> idAnchors = row.findElements(By.xpath(
                                    ".//*[self::a or self::button or self::td or self::span][normalize-space(.)='" + safeId + "' or contains(normalize-space(.), '" + safeId + "')]"
                            ));
                            if (!idAnchors.isEmpty()) {
                                clickInViewport(idAnchors.get(0));
                            } else {
                                clickInViewport(row);
                            }
                            System.out.println("ACTION: HostReservationPage.openBookingDetailsByBookingId - opened detail from exact row for id=" + idVariant);
                            Thread.sleep(600);
                            return true;
                        } catch (Exception ignored) {
                        }
                    }

                    List<WebElement> exactCells = d.findElements(exactIdCell);
                    for (WebElement cell : exactCells) {
                        try {
                            if (!cell.isDisplayed()) {
                                continue;
                            }
                            clickInViewport(cell);
                            System.out.println("ACTION: HostReservationPage.openBookingDetailsByBookingId - opened detail from exact cell for id=" + idVariant);
                            Thread.sleep(600);
                            return true;
                        } catch (Exception ignored) {
                        }
                    }
                }
                return false;
            });
        } catch (Exception e) {
            System.out.println("ACTION: HostReservationPage.openBookingDetailsByBookingId - failed with exception: " + e.getClass().getSimpleName());
            return false;
        }
    }

    private void ensureBookingRequestsListingPage() {
        try {
            System.out.println("ACTION: HostReservationPage.ensureBookingRequestsListingPage - ensuring host booking requests listing page");
            String currentUrl = driver.getCurrentUrl().toLowerCase();
            if (!currentUrl.contains("uat.host.homeyhutz.com") || !currentUrl.contains("/dashboard")) {
                driver.get(UAT_HOST_DASHBOARD_URL);
                Thread.sleep(900);
                System.out.println("ACTION: HostReservationPage.ensureBookingRequestsListingPage - navigated to host dashboard");
            }

            boolean reservationSectionOpened = clickReservationsTabExact(Duration.ofSeconds(8));
            if (!reservationSectionOpened) {
                reservationSectionOpened = clickFirstVisible(reservationsExactSpan, Duration.ofSeconds(8));
            }
            if (!reservationSectionOpened) {
                reservationSectionOpened = clickFirstVisible(topReservationsNav, Duration.ofSeconds(8));
            }
            if (!reservationSectionOpened) {
                reservationSectionOpened = clickFirstVisible(reservationsSidebarItem, Duration.ofSeconds(8));
            }
            if (!reservationSectionOpened) {
                reservationSectionOpened = clickByTextKeywords(
                        new String[]{"reservations", "reservation"},
                        Duration.ofSeconds(8)
                );
            }

            Thread.sleep(400);

            boolean bookingRequestsOpened = clickFirstVisible(bookingRequestsExactCard, Duration.ofSeconds(8));
            if (!bookingRequestsOpened) {
                bookingRequestsOpened = clickFirstVisible(dashboardBookingRequestsTab, Duration.ofSeconds(8));
            }
            if (!bookingRequestsOpened) {
                bookingRequestsOpened = clickFirstVisible(bookingRequestsSidebarItem, Duration.ofSeconds(8));
            }
            if (!bookingRequestsOpened) {
                clickByTextKeywords(
                        new String[]{"booking requests", "booking request", "requests"},
                        Duration.ofSeconds(8)
                );
            }
            System.out.println("ACTION: HostReservationPage.ensureBookingRequestsListingPage - reservation/requests section navigation attempted");
        } catch (Exception ignored) {
        }
    }

    private boolean openBookingDetailsDirectById(String bookingId) {
        System.out.println("ACTION: HostReservationPage.openBookingDetailsDirectById - start bookingId=" + bookingId);
        String normalized = bookingId == null ? "" : bookingId.trim();
        if (normalized.isBlank()) {
            System.out.println("ACTION: HostReservationPage.openBookingDetailsDirectById - no exact bookingId available");
            return false;
        }

        List<String> variants = java.util.Collections.singletonList(normalized);

        for (String idVariant : variants) {
            String[] detailUrls = new String[]{
                    UAT_HOST_BASE + "/trips/bookingRequests/" + idVariant,
                    UAT_HOST_BASE + "/host/trips/bookingRequests/" + idVariant,
                    UAT_HOST_BASE + "/trips/booking-requests/" + idVariant,
                    UAT_HOST_BASE + "/bookingRequests/" + idVariant,
                    UAT_HOST_BASE + "/booking-requests/" + idVariant
            };

            for (String url : detailUrls) {
                try {
                    System.out.println("ACTION: HostReservationPage.openBookingDetailsDirectById - trying URL: " + url);
                    driver.get(url);
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
                    boolean pageOpened = shortWait.until(d -> {
                        String current = d.getCurrentUrl().toLowerCase();
                        if (!current.contains("uat.host.homeyhutz.com")) {
                            return false;
                        }
                        String bodyText = d.findElement(By.tagName("body")).getText().toLowerCase();
                        return bodyText.contains(idVariant.toLowerCase()) && bodyText.contains("booking");
                    });

                    if (pageOpened) {
                        System.out.println("ACTION: HostReservationPage.openBookingDetailsDirectById - detail page opened for id=" + idVariant);
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        System.out.println("ACTION: HostReservationPage.openBookingDetailsDirectById - failed to open direct detail URL for bookingId=" + bookingId);
        return false;
    }

    private boolean isCurrentBookingAlreadyConfirmed(String bookingId) {
        String normalizedId = bookingId == null ? "" : bookingId.trim();
        By confirmedMarker = By.xpath(
                "//*[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'confirmed')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accepted')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booked')]"
        );

        try {
            List<WebElement> markers = driver.findElements(confirmedMarker);
            for (WebElement marker : markers) {
                if (!marker.isDisplayed()) {
                    continue;
                }

                String text = marker.getText() == null ? "" : marker.getText().toLowerCase();
                if (normalizedId.isBlank() || text.contains(normalizedId.toLowerCase()) || text.contains("booking") || text.contains("reservation")) {
                    return true;
                }
            }

            String bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase();
            return bodyText.contains("booking confirmed")
                    || bodyText.contains("reservation confirmed")
                    || bodyText.contains("request accepted");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasAcceptedOrConfirmedStatus(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String normalized = text.toLowerCase();
        return normalized.contains("accepted")
                || normalized.contains("confirmed")
                || normalized.contains("booked")
                || normalized.contains("reservation confirmed")
                || normalized.contains("request accepted")
                || normalized.contains("approved");
    }

    private boolean isBookingAcceptedInRequestsList(String bookingId) {
        List<String> variants = bookingIdVariants(bookingId);
        if (variants.isEmpty()) {
            return false;
        }

        try {
            ensureBookingRequestsListingPage();
            openBookingRequestsSectionBeforeSearch();
            searchBookingRequestId(bookingId);

            for (String idVariant : variants) {
                String safeId = idVariant.replace("'", "\\'");
                By matchingRows = By.xpath(
                        "//tr[.//*[contains(normalize-space(.), '" + safeId + "')]]"
                                + " | //*[(self::div or self::li or self::article or self::section) and .//*[contains(normalize-space(.), '" + safeId + "')]]"
                );

                List<WebElement> rows = driver.findElements(matchingRows);
                for (WebElement row : rows) {
                    try {
                        if (!row.isDisplayed()) {
                            continue;
                        }

                        String rowText = row.getText();
                        if (hasAcceptedOrConfirmedStatus(rowText)) {
                            return true;
                        }

                        List<WebElement> rowAcceptButtons = row.findElements(By.xpath(
                                ".//*[self::button or self::a][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')"
                                        + " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'approve')"
                                        + " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'confirm')]"
                        ));

                        boolean hasVisibleAcceptAction = false;
                        for (WebElement action : rowAcceptButtons) {
                            if (action.isDisplayed()) {
                                hasVisibleAcceptAction = true;
                                break;
                            }
                        }

                        if (!hasVisibleAcceptAction) {
                            return true;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void acceptBookingRequest(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId cannot be blank");
        }

        System.out.println("ACTION: HostReservationPage.acceptBookingRequest - start bookingId=" + bookingId);

        lastAcceptedBookingId = bookingId.trim();
        lastAcceptClickSucceeded = false;

        Instant deadline = Instant.now().plusSeconds(150);
        boolean bookingOpened = false;

        while (Instant.now().isBefore(deadline) && !bookingOpened) {
            System.out.println("ACTION: HostReservationPage.acceptBookingRequest - searching/opening booking details for bookingId=" + bookingId);
            ensureBookingRequestsListingPage();
            openBookingRequestsSectionBeforeSearch();
            searchBookingRequestId(bookingId);
            bookingOpened = openBookingDetailsByBookingId(bookingId);
            if (!bookingOpened) {
                System.out.println("ACTION: HostReservationPage.acceptBookingRequest - booking detail not opened yet, retrying with refresh/scroll");
                try {
                    openBookingRequestsOrPendingTabIfPresent();
                    nudgeBookingRequestsListScroll();
                    Thread.sleep(500);
                    driver.navigate().refresh();
                    Thread.sleep(1400);
                } catch (Exception ignored) {
                }
            }
        }

        if (!bookingOpened) {
            System.out.println("ACTION: HostReservationPage.acceptBookingRequest - trying direct booking detail URLs");
            bookingOpened = openBookingDetailsDirectById(bookingId);
        }

        if (!bookingOpened) {
            throw new RuntimeException("Booking ID not found in reservation list: " + bookingId);
        }

        By acceptAction = By.xpath(
                "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'approve')" +
                " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept booking')" +
                " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept request')" +
                " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'approve request')" +
                " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'approve booking')" +
                " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'confirm request')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'confirm booking')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'confirm')]" +
                        " | //a[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')" +
                " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'approve')]" +
                " | //*[(self::button or self::a or self::div or self::span) and " +
                "(@data-testid and (contains(translate(@data-testid, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept') or contains(translate(@data-testid, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'approve') or contains(translate(@data-testid, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'confirm'))) ]"
        );

        By confirmAction = By.xpath(
                "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'confirm')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'yes')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')]"
        );

        boolean acceptClicked = false;
        for (int cycle = 0; cycle < 3 && !acceptClicked; cycle++) {
            System.out.println("ACTION: HostReservationPage.acceptBookingRequest - accept cycle " + (cycle + 1));
            boolean clicked = false;
            for (int attempt = 0; attempt < 6 && !clicked; attempt++) {
                try {
                    System.out.println("ACTION: HostReservationPage.acceptBookingRequest - generic accept locator attempt " + (attempt + 1));
                    List<WebElement> actions = driver.findElements(acceptAction);
                    for (WebElement action : actions) {
                        if (!action.isDisplayed()) {
                            continue;
                        }
                        clickInViewport(action);
                        clicked = true;
                        System.out.println("ACTION: HostReservationPage.acceptBookingRequest - clicked accept action with generic locator");
                        break;
                    }

                    if (!clicked) {
                        nudgeBookingRequestsListScroll();
                        Thread.sleep(400);
                    }
                } catch (Exception ignored) {
                }
            }

            if (!clicked) {
                try {
                    System.out.println("ACTION: HostReservationPage.acceptBookingRequest - attempting row-level accept action");
                    ensureBookingRequestsListingPage();
                    openBookingRequestsSectionBeforeSearch();
                    searchBookingRequestId(bookingId);
                    clicked = clickAcceptActionInMatchingRow(bookingId);
                    if (clicked) {
                        System.out.println("ACTION: HostReservationPage.acceptBookingRequest - row-level accept action clicked");
                    }
                } catch (Exception ignored) {
                }
            }

            if (!clicked) {
                try {
                    System.out.println("ACTION: HostReservationPage.acceptBookingRequest - attempting page-wide JS accept fallback");
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    Object jsClicked = js.executeScript(
                            "const candidates = Array.from(document.querySelectorAll('button, a, [role=\"button\"], input[type=\"button\"], input[type=\"submit\"], div, span'));"
                                    + "const isAction = (el) => {"
                                    + "  const txt = ((el.innerText || el.textContent || '') + ' ' + (el.value || '') + ' ' + (el.getAttribute('aria-label') || '') + ' ' + (el.getAttribute('data-testid') || '')).toLowerCase();"
                                    + "  return txt.includes('accept') || txt.includes('approve') || txt.includes('confirm booking') || txt.includes('confirm request') || txt.includes('accept booking') || txt.includes('accept request') || txt.includes('approve request') || txt.includes('approve booking');"
                                    + "};"
                                    + "for (const el of candidates) {"
                                    + "  const style = window.getComputedStyle(el);"
                                    + "  const visible = style && style.display !== 'none' && style.visibility !== 'hidden' && el.offsetParent !== null;"
                                    + "  if (!visible || !isAction(el)) continue;"
                                    + "  el.scrollIntoView({behavior: 'smooth', block: 'center'});"
                                    + "  el.click();"
                                    + "  return true;"
                                    + "}"
                                    + "return false;"
                    );
                    clicked = Boolean.TRUE.equals(jsClicked);
                    if (clicked) {
                        System.out.println("ACTION: HostReservationPage.acceptBookingRequest - JS accept fallback clicked an action");
                    }
                } catch (Exception ignored) {
                }
            }

            if (!clicked) {
                if (cycle < 2) {
                    ensureBookingRequestsListingPage();
                    openBookingRequestsSectionBeforeSearch();
                    searchBookingRequestId(bookingId);
                    if (!openBookingDetailsByBookingId(bookingId)) {
                        openBookingDetailsDirectById(bookingId);
                    }
                    continue;
                }

                throw new RuntimeException(
                        "Accept button not found for booking ID: " + bookingId + ". URL: " + driver.getCurrentUrl()
                );
            }

            acceptClicked = true;

            try {
                WebDriverWait confirmWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                confirmWait.until(d -> {
                    List<WebElement> buttons = d.findElements(confirmAction);
                    for (WebElement button : buttons) {
                        if (!button.isDisplayed()) {
                            continue;
                        }
                        clickInViewport(button);
                        System.out.println("ACTION: HostReservationPage.acceptBookingRequest - clicked confirm modal action");
                        return true;
                    }
                    return false;
                });
            } catch (Exception ignored) {
            }
        }

        lastAcceptClickSucceeded = acceptClicked;
        System.out.println("ACTION: HostReservationPage.acceptBookingRequest - end bookingId=" + bookingId + ", acceptClicked=" + lastAcceptClickSucceeded);
    }

    public boolean isBookingConfirmedForId(String bookingId) {
        return isBookingConfirmedForId(bookingId, Duration.ofSeconds(35));
    }

    public boolean isBookingConfirmedForId(String bookingId, Duration timeout) {
        List<String> variants = bookingIdVariants(bookingId);
        if (variants.isEmpty()) {
            return false;
        }

        Duration effectiveTimeout = (timeout == null || timeout.isNegative() || timeout.isZero())
                ? Duration.ofSeconds(10)
                : timeout;

        By genericSuccessMarkers = By.xpath(
                "//*[self::div or self::span or self::p or self::h1 or self::h2 or self::h3]" +
                        "[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'request accepted')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking confirmed')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'reservation confirmed')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accepted successfully')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'successfully accepted')]"
        );

        By acceptAction = By.xpath(
                "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'approve')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept booking')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept request')]"
        );

        try {
            WebDriverWait statusWait = new WebDriverWait(driver, effectiveTimeout);
            return statusWait.until(d -> {
                try {
                    String url = d.getCurrentUrl().toLowerCase();
                    if (url.contains("confirmed") || url.contains("accepted")) {
                        return true;
                    }

                    List<WebElement> successMarkers = d.findElements(genericSuccessMarkers);
                    for (WebElement marker : successMarkers) {
                        if (marker.isDisplayed()) {
                            return true;
                        }
                    }

                    String bodyText = d.findElement(By.tagName("body")).getText().toLowerCase();
                    for (String idVariant : variants) {
                        String idLower = idVariant.toLowerCase();
                        boolean hasId = bodyText.contains(idLower);
                        boolean hasAcceptedStatus = bodyText.contains("accepted")
                                || bodyText.contains("confirmed")
                                || bodyText.contains("booked")
                                || bodyText.contains("reserved");
                        if (hasId && hasAcceptedStatus) {
                            return true;
                        }
                    }

                    boolean onBookingDetailPage = url.contains("bookingrequests") || url.contains("booking-requests");
                    List<WebElement> visibleAcceptButtons = d.findElements(acceptAction);
                    boolean hasVisibleAccept = false;
                    for (WebElement button : visibleAcceptButtons) {
                        if (button.isDisplayed()) {
                            hasVisibleAccept = true;
                            break;
                        }
                    }
                    if (onBookingDetailPage && !hasVisibleAccept) {
                        return true;
                    }

                    if (isCurrentBookingAlreadyConfirmed(bookingId)) {
                        return true;
                    }

                    if (isBookingAcceptedInRequestsList(bookingId)) {
                        return true;
                    }

                    return false;
                } catch (Exception ignored) {
                    return false;
                }
            });
        } catch (Exception e) {
            return false;
        }
    }
}