package com.homeyhutz.playwright.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PwRegistrationPage {

    private final Page page;

    private final Locator fullNameField;
    private final Locator phoneField;
    private final Locator emailField;
    private final Locator sendOtpButton;
    private final Locator loginLink;
    private final Locator termsLink;

    public PwRegistrationPage(Page page) {
        this.page = page;
        this.fullNameField = page.locator(
            "input[placeholder*='Full Name'], input[name='fullName'], input[placeholder*='Name']"
        ).first();
        this.phoneField = page.locator(
            "input[type='tel'], input[placeholder*='Phone'], input[name='phone']"
        ).first();
        this.emailField = page.locator(
            "input[type='email'], input[placeholder*='Email'], input[name='email']"
        ).first();
        this.sendOtpButton = page.locator(
            "button:has-text('Send OTP'), button[type='submit']:has-text('OTP')"
        ).first();
        this.loginLink = page.locator("a:has-text('Login'), a:has-text('already have an account')").first();
        this.termsLink = page.locator(
            "a:has-text('Terms'), a:has-text('Conditions'), a:has-text('T&C'), " +
            "a:has-text('Privacy'), span:has-text('Terms'), button:has-text('Terms')"
        ).first();
    }

    public boolean isOnRegistrationPage() {
        try {
            fullNameField.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void waitForPage() {
        fullNameField.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
        System.out.println("[REG] Registration page loaded. URL: " + page.url());
    }

    public void assertRegistrationFormUi() {
        assertThat(fullNameField).isVisible();
        assertThat(emailField).isVisible();
        assertThat(sendOtpButton).isVisible();
        System.out.println("[REG] Registration form UI validated");
    }

    public void enterFullName(String name) {
        fullNameField.click();
        fullNameField.fill(name);
        System.out.println("[REG] Full name entered: " + name);
    }

    public void enterEmail(String email) {
        emailField.click();
        emailField.fill(email);
        System.out.println("[REG] Email entered: " + email);
    }

    public String getPhoneFieldValue() {
        return phoneField.inputValue();
    }

    public void clearPhoneField() {
        phoneField.click();
        phoneField.press("Control+a");
        phoneField.press("Delete");
        System.out.println("[REG] Phone field cleared");
    }

    public void enterPhone(String phone) {
        phoneField.click();
        phoneField.press("Control+a");
        phoneField.fill(phone);
        System.out.println("[REG] Phone entered: " + phone);
    }

    public void clearEmailField() {
        emailField.click();
        emailField.press("Control+a");
        emailField.press("Delete");
        System.out.println("[REG] Email field cleared");
    }

    public boolean isValidationErrorVisible() {
        Locator err = page.locator(
            "[class*='error'], [class*='invalid'], [role='alert'], " +
            "p[class*='error'], span[class*='error'], [class*='text-red']"
        ).first();
        try {
            err.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(4000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getValidationErrorText() {
        Locator err = page.locator(
            "[class*='error'], [class*='invalid'], [role='alert'], " +
            "p[class*='error'], span[class*='error'], [class*='text-red']"
        ).first();
        try {
            err.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(4000));
            return err.textContent().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public void clickTermsLink() {
        termsLink.click();
        System.out.println("[REG] Clicked Terms & Conditions link");
    }

    public void clickSendOtp() {
        sendOtpButton.click();
        System.out.println("[REG] Clicked Send OTP");
    }

    public boolean isLoginLinkVisible() {
        try {
            loginLink.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTermsLinkVisible() {
        try {
            termsLink.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
