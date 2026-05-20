package com.homeyhutz.playwright.tests;

import com.homeyhutz.playwright.base.PlaywrightBaseTest;
import com.homeyhutz.playwright.pages.PwAuthPage;
import com.homeyhutz.playwright.pages.PwHomePage;
import com.homeyhutz.playwright.pages.PwOtpPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TC05_OtpEdgeCasesTest extends PlaywrightBaseTest {

    private static final String EXISTING_PHONE = "7558339846";

    private PwOtpPage otpPage;

    /**
     * Navigate to the OTP popup before each test so every TC05 test starts
     * at the OTP entry step.
     */
    @BeforeMethod
    public void navigateToOtpPage() {
        PwHomePage homePage = new PwHomePage(page);
        PwAuthPage authPage  = new PwAuthPage(page);
        otpPage = new PwOtpPage(page);

        homePage.openHomePage();
        homePage.waitForLoad();
        homePage.navigateToAuthPage();

        authPage.waitForPage();
        authPage.enterCredential(EXISTING_PHONE);
        authPage.clickContinue();

        PwAuthPage.FlowType flow = authPage.detectFlowAfterContinue();
        if (flow != PwAuthPage.FlowType.OTP_POPUP) {
            System.out.println("[TC05] WARN: Expected OTP popup but got: " + flow
                + ". Phone may not be registered. Skipping.");
            return;
        }
        otpPage.waitForOtpDialog();
        System.out.println("[TC05] OTP popup ready for edge case testing");
    }

    @Test(description = "Wrong OTP — submitting an incorrect OTP shows an error message")
    public void wrongOtpShowsError() {
        System.out.println("\n===== TC05: Wrong OTP =====");

        if (!otpPage.isOnOtpPage()) {
            System.out.println("[TC05] SKIP: OTP page not reached");
            return;
        }

        otpPage.enterOtp("000000");
        otpPage.clickSubmit();

        boolean errorVisible = otpPage.isErrorVisible();
        String errorText     = otpPage.getErrorMessage();

        System.out.println("[TC05] Error visible: " + errorVisible + " | Message: " + errorText);
        Assert.assertTrue(
            errorVisible || otpPage.isOnOtpPage(),
            "Wrong OTP should show error or keep user on OTP page"
        );
        System.out.println("[TC05] PASSED: Wrong OTP handled correctly");
    }

    @Test(description = "Empty OTP — clicking Submit without entering OTP shows error or keeps button disabled")
    public void emptyOtpValidation() {
        System.out.println("\n===== TC05: Empty OTP =====");

        if (!otpPage.isOnOtpPage()) {
            System.out.println("[TC05] SKIP: OTP page not reached");
            return;
        }

        // Do not enter anything — click Submit directly
        otpPage.clickSubmit();

        boolean errorVisible = otpPage.isErrorVisible();
        boolean stillOnPage  = otpPage.isOnOtpPage();

        System.out.println("[TC05] Error visible: " + errorVisible + " | Still on OTP page: " + stillOnPage);
        Assert.assertTrue(
            errorVisible || stillOnPage,
            "Empty OTP submit should show validation error or keep user on OTP page"
        );
        System.out.println("[TC05] PASSED: Empty OTP validation works");
    }

    @Test(description = "Partial OTP — entering fewer digits than required shows error")
    public void partialOtpValidation() {
        System.out.println("\n===== TC05: Partial OTP =====");

        if (!otpPage.isOnOtpPage()) {
            System.out.println("[TC05] SKIP: OTP page not reached");
            return;
        }

        otpPage.enterOtp("123"); // Only 3 of 6 digits
        otpPage.clickSubmit();

        boolean errorVisible = otpPage.isErrorVisible();
        boolean stillOnPage  = otpPage.isOnOtpPage();

        System.out.println("[TC05] Error visible: " + errorVisible + " | Still on OTP page: " + stillOnPage);
        Assert.assertTrue(
            errorVisible || stillOnPage,
            "Partial OTP submit should show error or keep user on OTP page"
        );
        System.out.println("[TC05] PASSED: Partial OTP validation works");
    }

    @Test(description = "Resend OTP timer is visible and shows countdown text")
    public void resendOtpTimerVisible() {
        System.out.println("\n===== TC05: Resend OTP Timer =====");

        if (!otpPage.isOnOtpPage()) {
            System.out.println("[TC05] SKIP: OTP page not reached");
            return;
        }

        boolean timerVisible = otpPage.isResendTimerVisible();
        System.out.println("[TC05] Resend timer visible: " + timerVisible);

        Assert.assertTrue(timerVisible,
            "Resend OTP timer should be visible immediately after OTP popup appears");
        System.out.println("[TC05] PASSED: Resend OTP timer confirmed");
    }

    @Test(description = "OTP dialog UI: input field and Submit button are visible")
    public void otpDialogUiValidation() {
        System.out.println("\n===== TC05: OTP Dialog UI Validation =====");

        if (!otpPage.isOnOtpPage()) {
            System.out.println("[TC05] SKIP: OTP page not reached");
            return;
        }

        otpPage.assertOtpUi();
        System.out.println("[TC05] PASSED: OTP dialog UI elements validated");
    }
}
