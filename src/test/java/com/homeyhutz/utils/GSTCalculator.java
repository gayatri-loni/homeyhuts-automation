package com.homeyhutz.utils;

import com.homeyhutz.constants.TestData;

public class GSTCalculator {

    /**
     * Returns GST rate based on Homeyhutz business rules:
     * Price >= 7500 → 18% GST
     * Price <  7500 → 5%  GST
     */
    public static double getGSTRate(int pricePerNight) {
        return pricePerNight >= TestData.GST_THRESHOLD
                ? TestData.GST_HIGH_RATE
                : TestData.GST_LOW_RATE;
    }

    public static int calculateGST(int pricePerNight, int discount) {
        int effectivePrice = pricePerNight - discount;
        double rate = getGSTRate(pricePerNight);
        return (int) Math.round(effectivePrice * rate);
    }

    public static int calculateFinalPrice(int pricePerNight, int discount) {
        int effectivePrice = pricePerNight - discount;
        int gst = calculateGST(pricePerNight, discount);
        return effectivePrice + gst;
    }

    public static void logBreakdown(int price, int discount) {
        int gst = calculateGST(price, discount);
        int finalPrice = calculateFinalPrice(price, discount);
        double rate = getGSTRate(price) * 100;

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║       PRICE BREAKDOWN SUMMARY        ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.printf("║  Base Price        : ₹%-15d ║%n", price);
        System.out.printf("║  Discount          : ₹%-15d ║%n", discount);
        System.out.printf("║  After Discount    : ₹%-15d ║%n", price - discount);
        System.out.printf("║  GST Rate          : %.0f%%-%-14s ║%n", rate, "");
        System.out.printf("║  GST Amount        : ₹%-15d ║%n", gst);
        System.out.printf("║  Final Price       : ₹%-15d ║%n", finalPrice);
        System.out.println("╚══════════════════════════════════════╝");
    }
}
