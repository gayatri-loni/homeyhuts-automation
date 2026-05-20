package com.homeyhutz.constants;

import java.time.LocalDate;
import java.util.Random;

public class TestData {

    // ── Playwright ───────────────────────────────────────────────────────────
    public static final String BASE_URL        = "https://uat.homeyhutz.com";
    public static final String EXISTING_PHONE    = "7558339846";
    public static final String PROPERTY_PAGE_URL = BASE_URL + "/rooms/4943";
    // Shared dates (30–59 days out) — used by all tests EXCEPT fullBookingWithNetbanking.
    // These tests only navigate to checkout; they never create real bookings, so dates stay clean.
    private static final long BROWSE_OFFSET = 30 + (LocalDate.now().getDayOfYear() % 30);
    public static final String BOOKING_CHECK_IN  = LocalDate.now().plusDays(BROWSE_OFFSET).toString();
    public static final String BOOKING_CHECK_OUT = LocalDate.now().plusDays(BROWSE_OFFSET + 1).toString();

    // Unique booking dates for the actual payment test (100–139 days out, never overlaps browse range).
    // Stays well within the property's advance-booking window (~6 months).
    private static final long BOOK_OFFSET = 100 + ((System.currentTimeMillis() / 1000) % 40);
    public static final String NETBANKING_CHECK_IN  = LocalDate.now().plusDays(BOOK_OFFSET).toString();
    public static final String NETBANKING_CHECK_OUT = LocalDate.now().plusDays(BOOK_OFFSET + 1).toString();

    public static String generateTestEmail() {
        return "testqa_" + System.currentTimeMillis() + "@yopmail.com";
    }

    public static String generateRandomPhone() {
        // Valid Indian mobile: starts with 6-9, followed by 9 random digits
        int[] startDigits = {6, 7, 8, 9};
        int start = startDigits[new Random().nextInt(startDigits.length)];
        StringBuilder sb = new StringBuilder(String.valueOf(start));
        Random rng = new Random();
        for (int i = 0; i < 9; i++) {
            sb.append(rng.nextInt(10));
        }
        return sb.toString();
    }

    // ── Property Details ────────────────────────────────────────────────────
    public static final int PROPERTY_ID   = 5012;
    public static final String PROPERTY_NAME = "test House";
    public static final String PROPERTY_CITY = "Latur";
    public static final String PROPERTY_UUID = "5532293e-31af-479f-a2cc-7e72f3680187";

    // ── Test Dates ───────────────────────────────────────────────────────────
    public static final String CHECK_IN_DATE       = "2026-04-29";  // URL format
    public static final String CHECK_OUT_DATE      = "2026-04-30";  // URL format
    public static final String CHECK_IN_DISPLAY    = "29/04/2026";  // UI format
    public static final String CHECK_OUT_DISPLAY   = "30/04/2026";  // UI format
    public static final String CALENDAR_DATE_LABEL = "29";           // Calendar cell

    // ── Prices ───────────────────────────────────────────────────────────────
    public static final int NEW_PRICE     = 10000; // price set from host calendar
    public static final int DEFAULT_PRICE = 9000;  // default price for other dates

    // ── GST Boundary ────────────────────────────────────────────────────────
    public static final int GST_THRESHOLD    = 7500;
    public static final double GST_HIGH_RATE = 0.18;  // 18% GST for price >= 7500
    public static final double GST_LOW_RATE  = 0.05;  // 5% GST for price < 7500

    // ── Sync Retry Config ────────────────────────────────────────────────────
    public static final int SYNC_RETRY_COUNT   = 6;
    public static final int SYNC_RETRY_WAIT_MS = 3000;

    // ── Price Sync Verification Test (Dec 25 2026, price = 9000) ─────────────
    public static final String PRICE_SYNC_DATE         = "2026-12-25";       // ISO / data-date attr
    public static final String PRICE_SYNC_DATE_ANT     = "25 Dec 26";        // Ant Design 2-digit year
    public static final String PRICE_SYNC_DATE_DISPLAY = "25 Dec 2026";      // 4-digit display
    public static final String PRICE_SYNC_ARIA_LABEL   = "December 25, 2026"; // FullCalendar aria-label
    public static final String PRICE_SYNC_INV_KEY      = "2026-12-25";       // inventory/rate field id key
    public static final int    SYNC_UPDATED_PRICE      = 9000;
    public static final String ADMIN_PROPERTY_ID       = "4967";             // admin-side property id
    public static final String GUEST_ROOM_ID           = "4943";             // /rooms/{id} in guest URL
}
