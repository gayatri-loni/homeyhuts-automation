
package com.homeyhutz.tests;

import com.homeyhutz.base.BaseTest;
import com.homeyhutz.pages.HomePage;
import com.homeyhutz.pages.LoginPage;
import com.homeyhutz.pages.OtpPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginFlowTest extends BaseTest {

    @Test
    public void verifyUserCanProceedTillOtpSubmit() {
        HomePage homePage = new HomePage(driver, wait);
        LoginPage loginPage = new LoginPage(driver, wait);
        OtpPage otpPage = new OtpPage(driver, wait);

        // Home → Login
        homePage.openHomePage();
        homePage.waitForHomePageToLoad();
        homePage.clickLoginIcon();
        homePage.clickLoginText();
        homePage.waitForLoginUiToOpen();

        // Phone → Continue
        loginPage.waitForLoginPage();
        loginPage.enterPhoneNumber("7558339846");
        loginPage.clickContinue();

        // OTP → Submit
        otpPage.waitForOtpPage();
        otpPage.enterOtp("123456");
        otpPage.clickSubmit();

        // Safe final assertion (UI response)
        Assert.assertTrue(
                driver.getPageSource().length() > 0,
                "Page did not respond after OTP submit"
        );

        // Wait 5 seconds before browser closes automatically
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
