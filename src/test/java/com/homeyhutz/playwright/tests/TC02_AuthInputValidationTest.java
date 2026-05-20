package com.homeyhutz.playwright.tests;

import com.homeyhutz.playwright.base.PlaywrightBaseTest;
import com.homeyhutz.playwright.pages.PwAuthPage;
import com.homeyhutz.playwright.pages.PwHomePage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class TC02_AuthInputValidationTest extends PlaywrightBaseTest {

    private PwHomePage homePage;
    private PwAuthPage authPage;

    @BeforeMethod
    public void navigateToAuthPage() {
        // Call parent setup first (Playwright is already initialized by @BeforeMethod in base)
        homePage = new PwHomePage(page);
        authPage = new PwAuthPage(page);

        homePage.openHomePage();
        homePage.waitForLoad();
        homePage.navigateToAuthPage();
        authPage.waitForPage();
    }

    @DataProvider(name = "invalidMobileInputs")
    public Object[][] invalidMobileInputs() {
        return new Object[][] {
            {"123",           "less than 10 digits"},
            {"12345678901",   "more than 10 digits"},
            {"abcdefghij",   "alphabetic characters"},
            {"@#$%^&*()",    "special characters"},
            {"  1234  ",     "spaces only"},
        };
    }

    @DataProvider(name = "invalidEmailInputs")
    public Object[][] invalidEmailInputs() {
        return new Object[][] {
            {"invalidemail",         "missing @ symbol"},
            {"test@",                "missing domain"},
            {"test @yopmail.com",    "space in email"},
            {"@yopmail.com",         "missing local part"},
        };
    }

    @Test(description = "Empty field — clicking Continue should show error or keep button disabled")
    public void emptyFieldValidation() {
        System.out.println("\n===== TC02: Empty Field Validation =====");

        authPage.assertPageUi();

        // Don't enter anything — just click Continue
        authPage.clickContinue();

        boolean errorShown    = authPage.isValidationErrorVisible();
        boolean buttonEnabled = authPage.isContinueEnabled();

        System.out.println("[TC02] Validation error visible: " + errorShown);
        System.out.println("[TC02] Continue button enabled: " + buttonEnabled);

        Assert.assertTrue(
            errorShown || !buttonEnabled,
            "Either a validation error should appear or Continue should be disabled for empty input"
        );
        System.out.println("[TC02] PASSED: Empty field validation works correctly");
    }

    @Test(dataProvider = "invalidMobileInputs",
          description = "Invalid mobile number inputs should trigger validation")
    public void invalidMobileValidation(String input, String description) {
        System.out.println("\n===== TC02: Invalid Mobile — " + description + " =====");

        authPage.enterCredential(input);
        authPage.clickContinue();

        boolean errorShown = authPage.isValidationErrorVisible();
        boolean stillOnAuthPage = page.url().contains("signup") || page.url().contains("signin") || page.url().contains("sign");

        System.out.println("[TC02] Error visible: " + errorShown + " | Still on auth page: " + stillOnAuthPage);

        Assert.assertTrue(
            errorShown || stillOnAuthPage,
            "Expected validation error or no navigation for input: [" + input + "] (" + description + ")"
        );

        if (errorShown) {
            System.out.println("[TC02] PASSED: Validation error shown for " + description);
        } else {
            System.out.println("[TC02] PASSED: Did not navigate away for invalid input: " + description);
        }

        // Reset for next data row
        authPage.clearCredential();
    }

    @Test(dataProvider = "invalidEmailInputs",
          description = "Invalid email inputs should trigger validation")
    public void invalidEmailValidation(String input, String description) {
        System.out.println("\n===== TC02: Invalid Email — " + description + " =====");

        authPage.enterCredential(input);
        authPage.clickContinue();

        boolean errorShown = authPage.isValidationErrorVisible();
        boolean stillOnAuthPage = page.url().contains("signup") || page.url().contains("signin") || page.url().contains("sign");

        System.out.println("[TC02] Error visible: " + errorShown + " | Still on auth page: " + stillOnAuthPage);

        Assert.assertTrue(
            errorShown || stillOnAuthPage,
            "Expected validation error or no navigation for email: [" + input + "] (" + description + ")"
        );
        System.out.println("[TC02] PASSED: Validation handled for " + description);

        authPage.clearCredential();
    }

    @Test(description = "Auth page has correct UI: input field, Continue button, back button")
    public void authPageUiValidation() {
        System.out.println("\n===== TC02: Auth Page UI Validation =====");
        authPage.assertPageUi();
        System.out.println("[TC02] PASSED: Auth page UI elements are present");
    }

    @Test(description = "Tab key moves focus logically through auth page fields")
    public void tabKeyNavigation() {
        System.out.println("\n===== TC02: Tab Key Navigation (Accessibility) =====");

        // Focus the credential input
        page.locator("input[name='phoneOrEmail'], input[placeholder*='Email'], input[type='tel']")
                .first().click();

        // Press Tab — focus should move to the next focusable element (Continue button or similar)
        page.keyboard().press("Tab");

        String focusedTag = (String) page.evaluate(
                "() => document.activeElement ? document.activeElement.tagName.toLowerCase() : 'none'");
        System.out.println("[TC02] Focused element after Tab: " + focusedTag);

        Assert.assertTrue(
            List.of("button", "input", "a", "select", "textarea").contains(focusedTag),
            "Tab key should move focus to a focusable element, got: " + focusedTag
        );
        System.out.println("[TC02] PASSED: Tab key navigation works correctly");
    }
}
