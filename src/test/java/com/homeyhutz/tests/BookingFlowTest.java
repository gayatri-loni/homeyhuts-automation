package com.homeyhutz.tests;

import com.homeyhutz.base.BaseTest;
import com.homeyhutz.pages.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BookingFlowTest extends BaseTest {

    private void logAction(String message) {
        System.out.println("ACTION: " + message);
    }

    private String readConfig(String key, String fallback) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return (value == null || value.isBlank()) ? fallback : value;
    }

    @Test
    public void verifyBookingFlowTillHostAccept() {

        logAction("Test start: verifyBookingFlowTillHostAccept");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String guestPhone = readConfig("HH_GUEST_PHONE", "7558339846");
        String guestOtp = readConfig("HH_GUEST_OTP", "123456");
        String hostPhone = readConfig("HH_HOST_PHONE", "7558339843");
        String hostOtp = readConfig("HH_HOST_OTP", "123456");
        logAction("Loaded guest and host credentials from config/env with defaults");

        logAction("Opening web app URL: https://uat.homeyhutz.com/");
        driver.get("https://uat.homeyhutz.com/");

        logAction("Initializing page objects");
        HomePage homePage = new HomePage(driver, wait);
        LoginPage loginPage = new LoginPage(driver, wait);
        OtpPage otpPage = new OtpPage(driver, wait);
        PropertyPage propertyPage = new PropertyPage(driver, wait);
        BookingPage bookingPage = new BookingPage(driver, wait);
        PaymentPage paymentPage = new PaymentPage(driver, wait);
        HostReservationPage hostReservationPage = new HostReservationPage(driver, wait);

        // Guest Login Flow
        logAction("Guest login: opening login popup");
        homePage.openLoginPopup();
        logAction("Guest login: waiting for login page");
        loginPage.waitForLoginPage();
        logAction("Guest login: entering phone number");
        loginPage.enterPhoneNumber(guestPhone);
        logAction("Guest login: clicking Continue");
        loginPage.clickContinue();
        logAction("Guest login: waiting for OTP page");
        otpPage.waitForOtpPage();
        logAction("Guest login: entering OTP");
        otpPage.enterOtp(guestOtp);
        logAction("Guest login: submitting OTP");
        otpPage.clickSubmit();

        logAction("Guest login completed");

        int[][] dateOffsets = new int[][]{
            {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 7}, {7, 8},
            {10, 11}, {12, 13}, {14, 15}, {16, 17}, {18, 19}, {20, 21},
            {22, 23}, {24, 25}, {26, 27}, {28, 29}, {30, 31}, {35, 36}, {40, 41}
        };

        boolean bookingCtaClicked = false;
        String lastTriedUrl = null;

        for (int[] offsetPair : dateOffsets) {
            String checkInDate = LocalDate.now().plusDays(offsetPair[0]).format(dateFormat);
            String checkOutDate = LocalDate.now().plusDays(offsetPair[1]).format(dateFormat);

            String candidateUrl = "https://uat.homeyhutz.com/rooms/1685?&checkIn=" + checkInDate + "&checkOut=" + checkOutDate + "&adults=1&children=0&infants=0&pets=0";
            lastTriedUrl = candidateUrl;
            logAction("Guest flow: trying property URL " + candidateUrl);
            driver.get(candidateUrl);

            if (propertyPage.tryClickCompleteBooking()) {
                bookingCtaClicked = true;
                logAction("Guest flow: clicked Complete Booking CTA");
                break;
            }
        }

        if (!bookingCtaClicked) {
            throw new RuntimeException("Booking CTA not found for tried date ranges. Last URL: " + lastTriedUrl);
        }

        logAction("Guest flow: filling email if needed");
        bookingPage.fillEmailIfEmpty("test@example.com");
        logAction("Guest flow: clicking Confirm and Pay");
        bookingPage.clickConfirmAndPay();

        logAction("Guest flow: completing payment process");
        paymentPage.completePaymentProcess();

        logAction("Guest flow: waiting for booking completion page markers");
        boolean bookingCompletionDetected = paymentPage.waitForBookingCompletionPage();
        if (!bookingCompletionDetected) {
            System.out.println("WARN: Booking completion markers were not detected in time; continuing to booking ID extraction.");
        }

        logAction("Guest flow: extracting booking ID from current confirmation URL");
        String bookingId = paymentPage.extractBookingId();
        Assert.assertFalse(bookingId == null || bookingId.isBlank(), "Booking ID extraction failed from confirmation URL.");
        logAction("Captured booking ID: " + bookingId);

        logAction("Guest flow: opening booking request detail by booking ID");
        boolean detailsOpened = paymentPage.clickBookingRequestDetail(bookingId);
        if (!detailsOpened) {
            System.out.println("WARN: Booking Request Detail button was not clickable.");
        }

        logAction("Switching account: clearing cookies and local/session storage");
        driver.manage().deleteAllCookies();
        try {
            ((JavascriptExecutor) driver).executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
        } catch (Exception ignored) {
        }

        logAction("Opening host login URL: https://uat.homeyhutz.com/");
        driver.get("https://uat.homeyhutz.com/");

        // Host Login Flow
        logAction("Host login: opening login popup");
        homePage.openLoginPopup();
        logAction("Host login: waiting for login page");
        loginPage.waitForLoginPage();
        logAction("Host login: entering phone number");
        loginPage.enterPhoneNumber(hostPhone);
        logAction("Host login: clicking Continue");
        loginPage.clickContinue();
        logAction("Host login: waiting for OTP page");
        otpPage.waitForOtpPage();
        logAction("Host login: entering OTP");
        otpPage.enterOtp(hostOtp);
        logAction("Host login: submitting OTP");
        otpPage.clickSubmit();

        boolean hostLoginCompleted;
        try {
            logAction("Host login: waiting until auth screen exits");
            hostLoginCompleted = wait.until(d -> {
                String current = d.getCurrentUrl().toLowerCase();
                boolean stillAuthScreen = current.contains("login")
                        || current.contains("sign-up")
                        || current.contains("signup")
                        || d.findElements(By.cssSelector("input[name='phoneOrEmail']")).size() > 0
                        || d.findElements(By.cssSelector("input[name='pin'], input[name='otp']")).size() > 0;
                return !stillAuthScreen;
            });
        } catch (Exception e) {
            hostLoginCompleted = false;
            logAction("Host login wait failed with exception: " + e.getClass().getSimpleName());
        }

        Assert.assertTrue(
                hostLoginCompleted,
                "Host login did not complete after OTP submit. Current URL: " + driver.getCurrentUrl()
        );
        logAction("Host login completed");

        // Required host path: menu -> Host Control Center -> Reservations
        logAction("Host flow: opening reservation section via menu -> Host Control Center -> Reservations");
        hostReservationPage.openReservationSection();
        logAction("Host flow: validating Reservations page state");
        boolean reservationsLoaded = hostReservationPage.isOnReservationsPage();
        Assert.assertTrue(
            reservationsLoaded,
            "Reservations page validation failed after navigation. Current URL: " + driver.getCurrentUrl()
        );
        logAction("Host flow completed: Reservations page opened");

        logAction("Host flow: clicking Booking Requests tab");
        hostReservationPage.clickBookingRequestsCard();
        logAction("Host flow: validating Booking Requests tab/page state");
        boolean bookingRequestsLoaded = wait.until(d -> {
            String current = d.getCurrentUrl().toLowerCase();
            if (current.contains("booking") || current.contains("request")) {
                return true;
            }
            return d.findElements(By.xpath(
                    "//input[@placeholder='Search by Booking Id']"
                            + " | //input[contains(translate(@placeholder, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'search by booking id')]"
            )).stream().anyMatch(el -> {
                try {
                    return el.isDisplayed();
                } catch (Exception ignored) {
                    return false;
                }
            });
        });
        Assert.assertTrue(
                bookingRequestsLoaded,
                "Booking Requests tab/page validation failed. Current URL: " + driver.getCurrentUrl()
        );
        logAction("Host flow completed: Booking Requests tab opened");

        logAction("Host flow: clicking search bar and entering full captured booking ID: " + bookingId);
        hostReservationPage.openBookingRequestsAndSearch(bookingId);
        logAction("Host flow completed: booking ID entered in Booking Requests search");

        logAction("Host flow: clicking searched booking ID: " + bookingId);
        hostReservationPage.clickSearchedBookingId(bookingId);
        logAction("Host flow completed: searched booking ID clicked");

        logAction("Host flow: accepting booking request for booking ID: " + bookingId);
        hostReservationPage.clickAcceptOnCurrentBooking();

        logAction("Host flow: clicking All tab in Reservations sidebar");
        hostReservationPage.clickAllTab();
        logAction("Host flow completed: All tab clicked");

        logAction("Host flow: waiting 5 seconds after accept click");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting after accept click", e);
        }

        logAction("Host flow: searching same booking ID in Reservations All view: " + bookingId);
        hostReservationPage.searchBookingIdInCurrentReservationsView(bookingId);
        logAction("Host flow completed: searched same booking ID in Reservations All view");

        if (driver != null) {
            logAction("Closing driver at test end");
            driver.quit();
            driver = null;
        }

        logAction("Test end: verifyBookingFlowTillHostAccept");
    }
}