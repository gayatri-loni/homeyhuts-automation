package com.homeyhutz.playwright.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PwAuthPage {

    public enum FlowType { OTP_POPUP, REGISTRATION_PAGE, UNKNOWN }

    private final Page page;

    private final Locator credentialInput;
    private final Locator continueButton;

    public PwAuthPage(Page page) {
        this.page = page;
        this.credentialInput = page.locator(
            "input[name='phoneOrEmail'], input[placeholder*='Email'], input[placeholder*='Phone'], input[type='tel'], input[type='email']"
        ).first();
        this.continueButton = page.locator(
            "button:has-text('Continue'), button[type='submit']:has-text('Continue')"
        ).first();
    }

    public void waitForPage() {
        credentialInput.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        System.out.println("[AUTH] Auth input page ready");
    }

    public void assertPageUi() {
        assertThat(credentialInput).isVisible();
        assertThat(continueButton).isVisible();
        System.out.println("[AUTH] UI validated: input and Continue button visible");
    }

    public void enterCredential(String value) {
        credentialInput.click();
        credentialInput.fill(value);
        System.out.println("[AUTH] Entered credential: " + value);
    }

    public void clearCredential() {
        credentialInput.fill("");
    }

    public void clickContinue() {
        continueButton.click();
        System.out.println("[AUTH] Clicked Continue");
    }

    public boolean isContinueEnabled() {
        return continueButton.isEnabled();
    }

    public String getValidationMessage() {
        Locator error = page.locator(
            "[class*='error'], [class*='invalid'], [role='alert'], .helper-text, span[style*='color: red'], p[class*='error']"
        ).first();
        try {
            error.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            return error.textContent().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isValidationErrorVisible() {
        Locator error = page.locator(
            "[class*='error'], [class*='invalid'], [role='alert'], .helper-text, span[style*='color: red'], p[class*='error']"
        ).first();
        try {
            error.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(4000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * After clicking Continue, detect whether the system routed to:
     * - OTP popup  (existing user login flow)
     * - Registration page (new user signup flow)
     */
    public FlowType detectFlowAfterContinue() {
        // Wait up to 12s for any of the three possible outcomes
        try {
            page.waitForFunction(
                "() => window.location.href.includes('phone-sign-up')" +
                " || window.location.href.includes('registration')" +
                // URL gets &phone= param when OTP is triggered for existing user
                " || (window.location.href.includes('phone=') && !window.location.href.includes('phone-sign-up'))" +
                // OTP modal via Radix UI dialog role
                " || document.querySelector('[role=\"dialog\"] input, [role=\"alertdialog\"] input') !== null",
                null,
                new Page.WaitForFunctionOptions().setTimeout(12000)
            );
        } catch (Exception e) {
            System.out.println("[AUTH] detectFlowAfterContinue timed out waiting for redirect");
        }

        String url = page.url();

        // New user signup path
        if (url.contains("phone-sign-up") || url.contains("registration")) {
            System.out.println("[AUTH] Flow detected: REGISTRATION_PAGE");
            return FlowType.REGISTRATION_PAGE;
        }

        // Whether from phone= URL or otherwise, wait for the dialog input to actually appear
        Locator otpInput = page.locator("[role='dialog'] input, [role='alertdialog'] input").first();
        try {
            otpInput.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(12000));
            System.out.println("[AUTH] Flow detected: OTP_POPUP (dialog input visible)");
            return FlowType.OTP_POPUP;
        } catch (Exception e) {
            System.out.println("[AUTH] Flow detected: UNKNOWN. URL=" + page.url());
            return FlowType.UNKNOWN;
        }
    }
}
