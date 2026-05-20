package com.homeyhutz.playwright.pages;

import com.homeyhutz.constants.TestData;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PwHomePage {

    private final Page page;

    // Locators
    private final Locator logo;
    private final Locator searchInput;
    private final Locator startDatePicker;
    private final Locator endDatePicker;
    private final Locator guestSelector;
    private final Locator propertyCards;
    private final Locator loginIcon;

    public PwHomePage(Page page) {
        this.page = page;
        this.logo            = page.locator("img[alt='header-logo']");
        this.searchInput     = page.locator("input[placeholder*='Search'], input[placeholder*='Destination'], input[placeholder*='destination']").first();
        this.startDatePicker = page.locator("input[placeholder*='Start'], button:has-text('Start Date'), [placeholder*='Check-in']").first();
        this.endDatePicker   = page.locator("input[placeholder*='End'], button:has-text('End Date'), [placeholder*='Check-out']").first();
        this.guestSelector   = page.locator("text=Guest, button:has-text('Guest'), [placeholder*='Guest']").first();
        this.propertyCards   = page.locator(".property-card, [class*='property'], [class*='listing'], [class*='card']");
        this.loginIcon       = page.locator(".inline-flex > .flex").first();
    }

    public void openHomePage() {
        page.navigate(TestData.BASE_URL);
        System.out.println("[HOME] Navigated to: " + TestData.BASE_URL);
    }

    public void waitForLoad() {
        page.waitForLoadState();
        // Wait for at least one property card or the search input
        page.waitForSelector(
            "input[placeholder*='Search'], input[placeholder*='Destination'], [class*='property'], [class*='card']",
            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(30000)
        );
        System.out.println("[HOME] Page loaded. URL: " + page.url());
    }

    public void assertHomePageUi() {
        assertThat(searchInput).isVisible();
        System.out.println("[HOME] Search input visible");
        assertThat(loginIcon).isVisible();
        System.out.println("[HOME] Login icon visible");
    }

    public void assertPropertyCardsLoaded() {
        page.waitForSelector(
            "[class*='property'], [class*='listing'], [class*='card']",
            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(20000)
        );
        int count = propertyCards.count();
        System.out.println("[HOME] Property cards found: " + count);
        if (count == 0) {
            System.out.println("[HOME] WARN: No property cards detected with current locator — page may still be loading.");
        }
    }

    public void clickLoginIcon() {
        loginIcon.click();
        System.out.println("[HOME] Clicked login/profile icon");
    }

    public boolean searchForCity(String city) {
        try {
            searchInput.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            searchInput.click();
            searchInput.fill(city);
            searchInput.press("Enter");
            page.waitForLoadState();
            System.out.println("[HOME] Searched for city: " + city);
            return true;
        } catch (Exception e) {
            System.out.println("[HOME] Search input not available: " + e.getMessage());
            return false;
        }
    }

    public boolean clickPropertyByName(String name) {
        try {
            Locator card = page.locator(
                "[class*='property']:has-text('" + name + "'), " +
                "[class*='card']:has-text('" + name + "'), " +
                "[class*='listing']:has-text('" + name + "')"
            ).first();
            card.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
            card.click();
            System.out.println("[HOME] Clicked property: " + name);
            return true;
        } catch (Exception e) {
            System.out.println("[HOME] Property '" + name + "' not found in results: " + e.getMessage());
            return false;
        }
    }

    public void navigateToAuthPage() {
        // Click the login icon. On this build the icon click navigates directly to
        // /signup-signin — no dropdown step needed (matches Selenium openLoginPopup fallback).
        clickLoginIcon();

        // If a dropdown appears try to click the Sign Up / Sign In option,
        // but don't fail if it doesn't — the icon click may have already navigated.
        Locator signInOption = page.locator(".flex.font-semibold > .cursor-pointer > .text-sm").first();
        try {
            signInOption.waitFor(new Locator.WaitForOptions().setTimeout(3000));
            // Only click if input isn't already visible (i.e. we haven't navigated yet)
            if (page.locator("input[name='phoneOrEmail']").count() == 0) {
                signInOption.click();
                System.out.println("[HOME] Clicked Sign Up / Sign In from dropdown");
            }
        } catch (Exception ignored) {
            System.out.println("[HOME] No dropdown appeared — icon click navigated directly");
        }

        // Wait for the auth input — the definitive signal that the auth form is ready
        page.waitForSelector(
            "input[name='phoneOrEmail']",
            new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(20000)
        );
        System.out.println("[HOME] Auth form ready. URL: " + page.url());
    }
}
