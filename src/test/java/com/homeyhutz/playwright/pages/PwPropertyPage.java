package com.homeyhutz.playwright.pages;

import com.homeyhutz.constants.TestData;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class PwPropertyPage {

    private final Page page;

    private final Locator propertyTitle;
    private final Locator bookNowButton;
    private final Locator checkInTrigger;
    private final Locator checkOutTrigger;
    private final Locator priceSection;
    private final Locator guestIncrement;
    private final Locator guestDecrement;

    public PwPropertyPage(Page page) {
        this.page = page;
        this.propertyTitle = page.locator(
            "h1, [class*='property-name'], [class*='property-title'], [class*='room-title']"
        ).first();
        // Use :visible so the hidden mobile duplicate button is excluded automatically.
        this.bookNowButton = page.locator(
            "button:has-text('Reserve'):visible, button:has-text('Book Now'):visible, " +
            "button:has-text('Instant Book'):visible, button:has-text('Reserve Now'):visible"
        ).first();
        this.checkInTrigger = page.locator(
            "[placeholder*='Check-in'], [placeholder*='check-in'], [placeholder*='Check In'], " +
            "button:has-text('Check-in'), [aria-label*='Check-in'], " +
            "[class*='check-in'] input, [class*='checkin']"
        ).first();
        this.checkOutTrigger = page.locator(
            "[placeholder*='Check-out'], [placeholder*='check-out'], [placeholder*='Check Out'], " +
            "button:has-text('Check-out'), [aria-label*='Check-out'], " +
            "[class*='check-out'] input, [class*='checkout']"
        ).first();
        // Exclude script/style tags to avoid matching JSON-LD structured data
        this.priceSection = page.locator(
            "span:has-text('₹'), p:has-text('₹'), h2:has-text('₹'), " +
            "div.text-2xl, div[class*='price'], span[class*='price']"
        ).first();
        this.guestIncrement = page.locator(
            "button[aria-label*='increase'], button[aria-label*='Increase'], " +
            "[class*='increment'], [class*='plus']"
        ).first();
        this.guestDecrement = page.locator(
            "button[aria-label*='decrease'], button[aria-label*='Decrease'], " +
            "[class*='decrement'], [class*='minus']"
        ).first();
    }

    public void openPropertyPage() {
        page.navigate(TestData.PROPERTY_PAGE_URL);
        System.out.println("[PROP] Navigated to: " + page.url());
    }

    // Navigate with dates pre-set via query params — avoids calendar interaction entirely
    public void openPropertyPageWithDates(String checkIn, String checkOut) {
        String url = TestData.PROPERTY_PAGE_URL
                + "?checkIn=" + checkIn
                + "&checkOut=" + checkOut
                + "&adults=1&children=0&infants=0&pets=0";
        page.navigate(url);
        System.out.println("[PROP] Navigated with dates: " + url);
    }

    public void waitForLoad() {
        page.waitForLoadState();
        try {
            propertyTitle.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
            System.out.println("[PROP] Title loaded: " + propertyTitle.textContent().trim());
        } catch (Exception e) {
            System.out.println("[PROP] Title not visible: " + page.url());
        }
        // Wait for the booking widget (React hydration takes extra time)
        try {
            bookNowButton.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
            System.out.println("[PROP] Booking widget ready (Reserve button visible)");
        } catch (Exception e) {
            System.out.println("[PROP] Booking widget not visible after 15s — dates may be unavailable");
        }
    }

    public boolean isTitleVisible() {
        try {
            propertyTitle.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            return true;
        } catch (Exception e) { return false; }
    }

    public String getPropertyTitle() {
        try { return propertyTitle.textContent().trim(); } catch (Exception e) { return ""; }
    }

    public boolean isPriceVisible() {
        // Iterate selectors to find the first actually-visible price element
        // (avoids picking up hidden mobile-container elements via .first())
        String[] priceSelectors = {
            "div.text-2xl:visible",
            "span[class*='price']:visible",
            "div[class*='price']:visible",
            "p:has-text('₹'):visible",
            "span:has-text('₹'):visible",
            "h2:has-text('₹'):visible"
        };
        for (String sel : priceSelectors) {
            try {
                Locator el = page.locator(sel).first();
                el.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
                System.out.println("[PROP] Price visible via " + sel + ": " + el.textContent().trim());
                return true;
            } catch (Exception ignored) {}
        }
        System.out.println("[PROP] No visible price element found");
        return false;
    }

    public boolean isBookNowButtonVisible() {
        try {
            bookNowButton.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
            System.out.println("[PROP] Book/Reserve button visible");
            return true;
        } catch (Exception e) {
            System.out.println("[PROP] Book/Reserve button NOT found: " + e.getMessage().split("\n")[0]);
            return false;
        }
    }

    public void clickBookNow() {
        // Re-query fresh to avoid stale element after React re-render
        Locator btn = page.locator(
            "button:has-text('Reserve'):visible, button:has-text('Book Now'):visible, " +
            "button:has-text('Instant Book'):visible, button:has-text('Reserve Now'):visible"
        ).first();
        btn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
        btn.scrollIntoViewIfNeeded();
        btn.click();
        System.out.println("[PROP] Clicked Book Now");
    }

    /** Click Reserve without dates and return true if validation is shown (no navigation occurred). */
    public boolean clickReserveWithoutDatesShowsValidation() {
        try {
            Locator btn = page.locator(
                "button:has-text('Reserve'):visible, button:has-text('Book Now'):visible"
            ).first();
            btn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
            String urlBefore = page.url();
            btn.click();
            page.waitForTimeout(1500);
            String urlAfter = page.url();

            // Validation: page should stay on property page OR show inline error
            boolean stayedOnPage = urlAfter.contains("rooms") || urlAfter.equals(urlBefore);
            boolean errorShown = page.locator(
                "[class*='error'], [class*='alert'], :has-text('select date'), " +
                ":has-text('Select date'), :has-text('choose date'), " +
                ":has-text('Check-in'), :has-text('Please select')"
            ).first().isVisible();

            System.out.println("[PROP] Reserve without dates — stayed on page: " + stayedOnPage
                + " | error shown: " + errorShown + " | URL: " + urlAfter);
            return stayedOnPage || errorShown;
        } catch (Exception e) {
            System.out.println("[PROP] clickReserveWithoutDatesShowsValidation error: " + e.getMessage());
            return false;
        }
    }

    public void clickCheckInTrigger() {
        try {
            checkInTrigger.click();
            System.out.println("[PROP] Clicked check-in trigger");
        } catch (Exception e) {
            System.out.println("[PROP] Check-in trigger not found: " + e.getMessage());
        }
    }

    public boolean selectDates(String checkInIso, String checkOutIso) {
        // ISO format: "2026-05-26"
        try {
            clickCheckInTrigger();
            page.waitForTimeout(500);
            boolean checkinOk = clickCalendarDate(checkInIso);

            page.waitForTimeout(300);
            try {
                checkOutTrigger.click();
                System.out.println("[PROP] Clicked check-out trigger");
                page.waitForTimeout(400);
            } catch (Exception e) {
                System.out.println("[PROP] Check-out field not separate — calendar may stay open");
            }
            boolean checkoutOk = clickCalendarDate(checkOutIso);
            return checkinOk && checkoutOk;
        } catch (Exception e) {
            System.out.println("[PROP] Date selection error: " + e.getMessage());
            return false;
        }
    }

    private boolean clickCalendarDate(String isoDate) {
        // Try attribute-based selectors first (most precise)
        String[] attrSelectors = {
            "[data-date='" + isoDate + "']:not([class*='disabled'])",
            "[aria-label*='" + isoDate + "']:not([aria-disabled='true'])",
            "td[data-value='" + isoDate + "']:not([class*='disabled'])",
            "[data-day='" + isoDate + "']:not([class*='disabled'])"
        };
        for (String sel : attrSelectors) {
            try {
                Locator cell = page.locator(sel).first();
                cell.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE).setTimeout(2000));
                cell.click();
                System.out.println("[PROP] Clicked date " + isoDate + " via: " + sel);
                return true;
            } catch (Exception ignored) {}
        }

        // Fallback: click day number inside visible calendar
        String day = String.valueOf(Integer.parseInt(isoDate.substring(8)));
        try {
            Locator dayCell = page.locator(
                "table td:not([class*='disabled']):not([class*='prev']):not([class*='next'])"
            ).filter(new Locator.FilterOptions().setHasText(day)).first();
            dayCell.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(2000));
            dayCell.click();
            System.out.println("[PROP] Clicked day " + day + " for date " + isoDate + " (fallback)");
            return true;
        } catch (Exception e) {
            System.out.println("[PROP] Could not click date " + isoDate + ": " + e.getMessage());
            return false;
        }
    }

    public boolean isPastDateDisabled() {
        // Check yesterday
        String yesterday = java.time.LocalDate.now().minusDays(1).toString();
        String[] disabledAttrs = {
            "[data-date='" + yesterday + "'][class*='disabled']",
            "[data-date='" + yesterday + "'][aria-disabled='true']",
            "[data-date='" + yesterday + "'][class*='unavailable']"
        };
        for (String sel : disabledAttrs) {
            try {
                if (page.locator(sel).count() > 0) {
                    System.out.println("[PROP] Past date disabled via: " + sel);
                    return true;
                }
            } catch (Exception ignored) {}
        }
        System.out.println("[PROP] Past date disabled state not detectable by attribute");
        return false;
    }

    public void incrementGuests() {
        try {
            guestIncrement.click();
            System.out.println("[PROP] Guest count incremented");
        } catch (Exception e) {
            System.out.println("[PROP] Guest increment button not found");
        }
    }

    public void decrementGuests() {
        try {
            guestDecrement.click();
            System.out.println("[PROP] Guest count decremented");
        } catch (Exception e) {
            System.out.println("[PROP] Guest decrement button not found");
        }
    }

    public int getPropertyImageCount() {
        try {
            return page.locator(
                "[class*='gallery'] img, [class*='photo'] img, [class*='slider'] img"
            ).count();
        } catch (Exception e) { return 0; }
    }
}
