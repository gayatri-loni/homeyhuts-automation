package com.homeyhutz.playwright.tests;

import com.homeyhutz.playwright.base.PlaywrightBaseTest;
import com.homeyhutz.playwright.pages.PwAuthPage;
import com.homeyhutz.playwright.pages.PwHomePage;
import com.homeyhutz.constants.TestData;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC01_HomePageTest extends PlaywrightBaseTest {

    @Test(description = "Home page loads successfully with all UI elements visible")
    public void homePageUiValidation() {
        PwHomePage homePage = new PwHomePage(page);

        System.out.println("\n===== TC01: Home Page UI Validation =====");

        homePage.openHomePage();
        homePage.waitForLoad();

        homePage.assertHomePageUi();
        System.out.println("[TC01] Search bar and login icon visible");

        homePage.assertPropertyCardsLoaded();
        System.out.println("[TC01] Property cards loaded");

        Assert.assertFalse(
            page.url().isEmpty(),
            "Page URL should not be empty"
        );
        System.out.println("[TC01] PASSED: Home page UI validation complete");
    }

    @Test(description = "No JS console errors on home page load")
    public void homePageNoConsoleErrors() {
        PwHomePage homePage = new PwHomePage(page);

        System.out.println("\n===== TC01: Home Page Console Error Check =====");

        homePage.openHomePage();
        homePage.waitForLoad();

        // Allow minor known errors (e.g. favicon 404), fail only on real JS errors
        long jsErrors = consoleErrors.stream()
            .filter(e -> !e.contains("favicon") && !e.contains("net::ERR"))
            .count();

        if (jsErrors > 0) {
            System.out.println("[TC01] Console errors found:");
            consoleErrors.forEach(e -> System.out.println("  - " + e));
        } else {
            System.out.println("[TC01] No JS console errors");
        }

        Assert.assertEquals(jsErrors, 0,
            "Expected no JS console errors, but found: " + consoleErrors);
    }

    @Test(description = "Clicking the login icon opens the authentication form")
    public void homePageNavigatesToAuthPage() {
        PwHomePage homePage = new PwHomePage(page);
        PwAuthPage authPage  = new PwAuthPage(page);

        System.out.println("\n===== TC01: Home Page → Auth Navigation =====");

        homePage.openHomePage();
        homePage.waitForLoad();
        homePage.navigateToAuthPage();

        // Assert the auth input is visible (auth may be inline modal — URL may not change)
        authPage.assertPageUi();
        System.out.println("[TC01] PASSED: Auth form opened. URL: " + page.url());
    }

    @Test(description = "Home page URL matches expected base URL")
    public void homePageUrlIsCorrect() {
        PwHomePage homePage = new PwHomePage(page);

        homePage.openHomePage();
        homePage.waitForLoad();

        Assert.assertTrue(
            page.url().startsWith(TestData.BASE_URL),
            "URL should start with " + TestData.BASE_URL + " but was: " + page.url()
        );
        System.out.println("[TC01] PASSED: Home page URL is correct");
    }
}
