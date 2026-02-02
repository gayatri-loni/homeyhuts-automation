package com.homeyhutz.tests;

import com.homeyhutz.base.BaseTest;
import com.homeyhutz.pages.SignupPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SignupFlowTest extends BaseTest {

    @Test
    public void signupFlowTest() {

        SignupPage signupPage = new SignupPage(driver, wait);

        signupPage.openHomePage();
        signupPage.clickHeaderLogin();
        signupPage.clickSignupButton();

        signupPage.enterPhoneNumber("7798339846");
        signupPage.clickContinue();

        // Handle conditional signup / login flow
        if (signupPage.isPhoneSignupFlow()) {
            signupPage.enterFirstName("John");
            signupPage.clickSendOtp();
        }

        signupPage.enterOtp("123456");
        signupPage.submitOtp();

        Assert.assertTrue(
                signupPage.isSignupSuccessful(),
                "Signup/Login flow failed"
        );
    }
}
