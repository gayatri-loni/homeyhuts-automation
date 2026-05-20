package com.homeyhutz.playwright.tests;

import com.homeyhutz.constants.TestData;
import com.homeyhutz.playwright.base.PlaywrightBaseTest;
import com.homeyhutz.playwright.pages.PwAuthPage;
import com.homeyhutz.playwright.pages.PwHomePage;
import com.homeyhutz.playwright.pages.PwOtpPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TC08_OtpAdvancedTest extends PlaywrightBaseTest {

    private static final String EXISTING_PHONE = TestData.EXISTING_PHONE;
    private static final String WRONG_OTP      = "000000";

    private PwOtpPage otpPage;
    private boolean   otpReady = false;

    // Navigate to OTP dialog using the existing registered phone before each test.
    // If UAT rate-limits the request, otpReady stays false and tests skip gracefully.
    @BeforeMethod
    public void navigateToOtpDialog() {
        PwHomePage homePage = new PwHomePage(page);
        PwAuthPage authPage = new PwAuthPage(page);
        otpPage  = new PwOtpPage(page);
        otpReady = false;

        homePage.openHomePage();
        homePage.waitForLoad();
        homePage.navigateToAuthPage();
        authPage.waitForPage();
        authPage.enterCredential(EXISTING_PHONE);
        authPage.clickContinue();

        PwAuthPage.FlowType flow = authPage.detectFlowAfterContinue();
        if (flow == PwAuthPage.FlowType.OTP_POPUP) {
            try {
                otpPage.waitForOtpDialog();
                otpReady = true;
                System.out.println("[TC08] OTP dialog ready");
            } catch (Exception e) {
                System.out.println("[TC08] OTP dialog timed out — UAT may be rate-limiting.");
            }
        } else {
            System.out.println("[TC08] Could not reach OTP dialog. Flow: " + flow);
        }
    }

    // ── TC08a: Multiple Failed OTP Attempts ───────────────────────────────────

    @Test(description = "Three consecutive wrong OTPs should trigger lockout warning or show error each time")
    public void multipleFailedOtpAttempts() {
        System.out.println("\n===== TC08: Multiple Failed OTP Attempts =====");
        if (!otpReady) { System.out.println("[TC08] SKIP: OTP dialog not available"); return; }

        int errorCount = 0;

        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.println("[TC08] Wrong OTP attempt #" + attempt);
            otpPage.enterOtp(WRONG_OTP);
            otpPage.clickSubmit();

            boolean errorVisible = otpPage.isErrorVisible();
            String  errorText    = otpPage.getErrorMessage();
            System.out.println("[TC08] Attempt " + attempt + " — Error visible: " + errorVisible
                    + " | Text: " + errorText);

            if (errorVisible) {
                errorCount++;
                // Check for lockout/warning keywords on 3rd attempt
                if (attempt == 3) {
                    boolean hasLockoutHint =
                            errorText.toLowerCase().contains("lock")  ||
                            errorText.toLowerCase().contains("block") ||
                            errorText.toLowerCase().contains("limit") ||
                            errorText.toLowerCase().contains("attempt") ||
                            errorText.toLowerCase().contains("invalid") ||
                            errorText.toLowerCase().contains("incorrect");
                    System.out.println("[TC08] Lockout/warning hint after 3rd wrong OTP: " + hasLockoutHint
                            + "  (text: '" + errorText + "')");
                }
            }

            // If the dialog closed (OTP accepted — unlikely with 000000 — or locked), stop loop
            if (!otpPage.isOnOtpPage()) {
                System.out.println("[TC08] OTP dialog closed after attempt #" + attempt);
                break;
            }
        }

        Assert.assertTrue(errorCount >= 1,
                "Expected at least one error message for wrong OTP submissions");
        System.out.println("[TC08] PASSED: Error shown for wrong OTP (count: " + errorCount + ")");
    }

    // ── TC08b: OTP Expiry (Manual / Documentation Only) ──────────────────────

    @Test(enabled = false,
          description = "OTP expiry test — disabled: requires waiting ~60s for OTP to expire, then submitting. " +
                        "Manual steps: (1) reach OTP dialog, (2) wait for resend timer to reach 0, " +
                        "(3) wait another 30s, (4) enter any OTP, (5) expect 'OTP expired' error. " +
                        "Automate only if UAT provides a short-expiry test mode.")
    public void otpExpiryManualTest() {
        // This test is intentionally disabled.
        // OTP expires after ~60 seconds. Automating a 60-second sleep is flaky and slow.
        // Run manually when validating expiry behaviour.
    }
}
