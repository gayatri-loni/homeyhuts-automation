package com.homeyhutz.tests;

import com.homeyhutz.base.BaseTest;
import com.homeyhutz.pages.HomePage;
import com.homeyhutz.pages.HostReservationPage;
import com.homeyhutz.pages.LoginPage;
import com.homeyhutz.pages.OtpPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HostReservationNavigationTest extends BaseTest {

    private String readConfig(String key, String fallback) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return (value == null || value.isBlank()) ? fallback : value;
    }

    @Test
    public void hostCanOpenReservationSection() {
        String hostPhone = readConfig("HH_HOST_PHONE", "7558339843");
        String hostOtp = readConfig("HH_HOST_OTP", "123456");

        driver.get("https://uat.homeyhutz.com/");

        HomePage homePage = new HomePage(driver, wait);
        LoginPage loginPage = new LoginPage(driver, wait);
        OtpPage otpPage = new OtpPage(driver, wait);
        HostReservationPage hostReservationPage = new HostReservationPage(driver, wait);

        homePage.openLoginPopup();
        loginPage.waitForLoginPage();
        loginPage.enterPhoneNumber(hostPhone);
        loginPage.clickContinue();
        otpPage.waitForOtpPage();
        otpPage.enterOtp(hostOtp);
        otpPage.clickSubmit();

        boolean hostLoginCompleted;
        try {
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
        }

        Assert.assertTrue(
                hostLoginCompleted,
                "Host login did not complete after OTP submit. Current URL: " + driver.getCurrentUrl()
        );

        hostReservationPage.openReservationSection();
        hostReservationPage.clickBookingRequestsCard();

        String finalUrl = driver.getCurrentUrl().toLowerCase();
        Assert.assertTrue(
            finalUrl.contains("booking") || finalUrl.contains("request"),
            "Did not navigate to Booking Requests section. Current URL: " + driver.getCurrentUrl()
        );
    }
}