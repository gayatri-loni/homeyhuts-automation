package com.homeyhutz.playwright.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PwOtpPage {

    private final Page page;

    private final Locator otpDialog;
    private final Locator otpInput;
    private final Locator submitButton;
    private final Locator resendOtpText;
    private final Locator errorMessage;

    public PwOtpPage(Page page) {
        this.page = page;
        // Radix UI Dialog uses role="dialog" or role="alertdialog"
        this.otpDialog     = page.locator("[role='dialog'], [role='alertdialog']").first();
        // OTP input: inside the dialog, numeric input
        this.otpInput      = page.locator("[role='dialog'] input, [role='alertdialog'] input").first();
        // Submit button: inside the dialog
        this.submitButton  = page.locator("[role='dialog'] button:has-text('Submit'), [role='alertdialog'] button:has-text('Submit')").first();
        // Resend OTP text: has-text() is a valid Playwright CSS extension
        this.resendOtpText = page.locator("[role='dialog']:has-text('Resend'), [role='alertdialog']:has-text('Resend'), :has-text('Resend OTP')").first();
        // Error message inside dialog
        this.errorMessage  = page.locator("[role='dialog'] [class*='error'], [role='alertdialog'] [class*='error'], [role='alert']").first();
    }

    public void waitForOtpDialog() {
        otpDialog.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(30000));
        System.out.println("[OTP] OTP dialog appeared");
    }

    public void assertOtpUi() {
        assertThat(otpInput).isVisible();
        assertThat(submitButton).isVisible();
        System.out.println("[OTP] OTP UI validated: input and Submit button visible");
    }

    public void enterOtp(String otp) {
        otpInput.click();
        otpInput.fill(otp);
        System.out.println("[OTP] Entered OTP: " + otp);
    }

    public void clickSubmit() {
        submitButton.click();
        System.out.println("[OTP] Clicked Submit");
    }

    public boolean isOnOtpPage() {
        try {
            otpDialog.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isErrorVisible() {
        try {
            errorMessage.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        try {
            errorMessage.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            return errorMessage.textContent().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isResendTimerVisible() {
        try {
            resendOtpText.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            String text = resendOtpText.textContent();
            System.out.println("[OTP] Resend text: " + text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void assertSuccessfulRedirect() {
        page.waitForURL(url -> !url.contains("sign"), new Page.WaitForURLOptions().setTimeout(15000));
        System.out.println("[OTP] Redirect successful. URL: " + page.url());
    }
}
