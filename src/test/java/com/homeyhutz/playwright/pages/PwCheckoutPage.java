package com.homeyhutz.playwright.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class PwCheckoutPage {

    private final Page page;

    private final Locator confirmPayButton;
    private final Locator totalPrice;
    private final Locator gstRow;

    public PwCheckoutPage(Page page) {
        this.page = page;
        this.confirmPayButton = page.locator(
            "button:has-text('Request to Book'), [role='button']:has-text('Request to Book'), " +
            "a:has-text('Request to Book'), button:has-text('Confirm'), " +
            "button:has-text('Pay Now'), button:has-text('Proceed to Pay'), " +
            "button:has-text('Complete Booking'), button:has-text('Pay'), " +
            "button:has-text('Confirm & Pay')"
        ).first();
        this.totalPrice = page.locator(
            ":has-text('Total Price'), [class*='total'], :has-text('Total Amount'), " +
            ":has-text('Grand Total'), td:has-text('Total'), [class*='grand-total']"
        ).first();
        this.gstRow = page.locator(
            ":has-text('Enter GST Details'), :has-text('I have GST Number'), " +
            "[class*='gst'], [class*='tax']"
        ).first();
    }

    public boolean waitForCheckoutPage() {
        try {
            page.waitForURL(url ->
                url.contains("checkout") || url.contains("booking") ||
                url.contains("book") || url.contains("stays") ||
                url.contains("reservation") || url.contains("payment") ||
                url.contains("order") || url.contains("confirm") ||
                url.contains("summary") || url.contains("reserve") ||
                url.contains("pay"),
                new Page.WaitForURLOptions().setTimeout(15000)
            );
            System.out.println("[CHECKOUT] Reached checkout URL: " + page.url());
            // Wait for the booking widget content to hydrate
            try {
                page.locator(":has-text('Request to Book'), :has-text('Total Price')").first()
                    .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
            } catch (Exception ignored) {}
            return true;
        } catch (Exception e) {
            System.out.println("[CHECKOUT] URL did not change to checkout. Current: " + page.url());
            // Fallback: look for any pay/confirm button or summary text visible on page
            try {
                confirmPayButton.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE).setTimeout(8000));
                System.out.println("[CHECKOUT] Pay button visible (modal/drawer). URL: " + page.url());
                return true;
            } catch (Exception e2) {
                // Last resort: look for price summary / booking summary text
                try {
                    Locator summary = page.locator(
                        ":has-text('Booking Summary'), :has-text('Price Breakdown'), " +
                        ":has-text('Total Amount'), :has-text('Review your booking')"
                    ).first();
                    summary.waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
                    System.out.println("[CHECKOUT] Booking summary visible. URL: " + page.url());
                    return true;
                } catch (Exception e3) {
                    System.out.println("[CHECKOUT] Checkout not detected. URL: " + page.url());
                    return false;
                }
            }
        }
    }

    public boolean isTotalPriceVisible() {
        try {
            totalPrice.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
            System.out.println("[CHECKOUT] Total price element visible. URL: " + page.url());
            return true;
        } catch (Exception e) {
            System.out.println("[CHECKOUT] Total price not found: " + e.getMessage().split("\n")[0]);
            return false;
        }
    }

    public boolean isGstVisible() {
        try {
            gstRow.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            System.out.println("[CHECKOUT] GST: " + gstRow.textContent().trim());
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean clickConfirmAndPay() {
        try {
            confirmPayButton.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
            confirmPayButton.scrollIntoViewIfNeeded();
            confirmPayButton.click();
            System.out.println("[CHECKOUT] Clicked Confirm & Pay");
            return true;
        } catch (Exception e) {
            System.out.println("[CHECKOUT] Confirm/Pay button not found: " + e.getMessage().split("\n")[0]);
            // Dump buttons for diagnosis
            try {
                String btns = (String) page.evaluate(
                    "() => Array.from(document.querySelectorAll('button'))" +
                    ".map(b => b.innerText.trim() + ' | ' + b.className.substring(0,50))" +
                    ".filter(s => s.trim().length > 2).join(' || ')"
                );
                System.out.println("[CHECKOUT] Available buttons: " + btns);
            } catch (Exception ignored) {}
            return false;
        }
    }

    public boolean isBookingConfirmed() {
        // Primary check: URL-based (most reliable)
        String currentUrl = page.url();
        if (currentUrl.contains("confirmation") || currentUrl.contains("request-confirmation")
                || currentUrl.contains("booking-confirmed") || currentUrl.contains("success")) {
            System.out.println("[CHECKOUT] Confirmed via URL: " + currentUrl);
            return true;
        }
        // Secondary check: wait up to 10s for page redirect then re-check URL
        try {
            page.waitForURL(url ->
                url.contains("confirmation") || url.contains("request-confirmation")
                    || url.contains("booking-confirmed") || url.contains("success"),
                new Page.WaitForURLOptions().setTimeout(10000));
            System.out.println("[CHECKOUT] Confirmed via URL redirect: " + page.url());
            return true;
        } catch (Exception ignored) {}
        // Fallback: text-based check
        try {
            Locator confirmed = page.locator(
                "[class*='success'], [class*='confirmed'], [class*='confirmation'], " +
                ":has-text('Booking Confirmed'), :has-text('Successfully Booked'), " +
                ":has-text('Thank you'), :has-text('Booking ID'), :has-text('Booking successful')"
            ).first();
            confirmed.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
            System.out.println("[CHECKOUT] Confirmed via text: " + confirmed.textContent().trim().substring(0, 50));
            return true;
        } catch (Exception e) {
            System.out.println("[CHECKOUT] Confirmation not found. URL: " + page.url());
            return false;
        }
    }
}
