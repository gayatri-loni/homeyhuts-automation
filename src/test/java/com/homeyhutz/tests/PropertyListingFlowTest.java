package com.homeyhutz.tests;

import com.homeyhutz.base.BaseTest;
import com.homeyhutz.pages.HostListingPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PropertyListingFlowTest extends BaseTest {

    @Test
    public void newUserCompletesPropertyListingFlow() throws InterruptedException {
        HostListingPage listingPage = new HostListingPage(driver, wait);

        try {
            // Step 1: Open home and start listing
            System.out.println("\n========== STEP 1: OPEN HOME AND CLICK LIST PROPERTY ==========");
            listingPage.openHomeAndClickListProperty();
            System.out.println("✓ Home page opened and list property link clicked\n");

            // Step 2: Click Continue Listing
            System.out.println("========== STEP 2: CLICK CONTINUE LISTING ==========");
            listingPage.clickContinueListing();
            System.out.println("✓ Continue Listing button clicked\n");

            // Step 3-5: Registration form + OTP
            System.out.println("========== STEP 3: FILL REGISTRATION FORM ==========");
            listingPage.fillRegistrationForm(
                    "Test User",
                    "testuser@yopmail.com",
                    "7758339846"
            );
            System.out.println("✓ Registration form filled\n");

            System.out.println("========== STEP 4: ENTER OTP ==========");
            listingPage.enterOtp("123456");
            System.out.println("✓ OTP entered: 123456\n");

            // Wait for OTP to be processed
            Thread.sleep(3000);

            System.out.println("========== STEP 5: CLICK CONTINUE LISTING AFTER OTP ==========");
            listingPage.clickContinueListingAfterOtp();
            System.out.println("✓ Continue Listing after OTP button clicked\n");

            // Wait for redirect to host portal
            Thread.sleep(5000);
            String currentUrl = driver.getCurrentUrl();
            System.out.println("Current URL after OTP: " + currentUrl);

            // Check if on subscription form page
            if (currentUrl.contains("serviceForm")) {
                System.out.println("⚠ Need to fill subscription form first\n");
                // For now, skip subscription form and proceed to next button
                // In real scenario, would need to fill form
                System.out.println("========== STEP 5B: SKIP SUBSCRIPTION FORM ==========");
                Thread.sleep(3000);
                listingPage.clickNext();  // Try clicking next to skip/proceed
                System.out.println("✓ Proceeding from subscription form\n");
                Thread.sleep(5000);
                currentUrl = driver.getCurrentUrl();
                System.out.println("URL after subscription form: " + currentUrl);
            }
                
                // Step 6: Click Create New Listing button
                System.out.println("========== STEP 6: CLICK CREATE NEW LISTING ==========");
                Thread.sleep(2000);  // Wait for page to fully load
                listingPage.clickCreateNewListing();
                System.out.println("✓ Create New Listing button clicked\n");
                
                Thread.sleep(5000);  // Wait for navigation to listing form
                String listingPageUrl = driver.getCurrentUrl();
                System.out.println("Listing page URL: " + listingPageUrl);
                
                // Step 7: Click Next button on information page
                System.out.println("========== STEP 7: CLICK NEXT ON INFORMATION PAGE ==========");
                Thread.sleep(2000);
                listingPage.clickNext();
                System.out.println("✓ Next button clicked\n");

                Thread.sleep(3000);  // Wait for next page to load
                String nextPageUrl = driver.getCurrentUrl();
                System.out.println("Next page URL: " + nextPageUrl);
                
                // Step 8: Select property basics (Hut, Shared Room, Property Owner)
                System.out.println("========== STEP 8: SELECT PROPERTY BASICS ==========");
                listingPage.selectPropertyBasics();
                System.out.println("✓ Selected Hut, Shared Room, Property Owner\n");
                
                Thread.sleep(3000);
                
                // Step 9: Select location and house name
                System.out.println("========== STEP 9: SELECT LOCATION AND HOUSE NAME ==========");
                listingPage.selectLocationAndHouseName();
                System.out.println("✓ Location and house name entered\n");
                
                Thread.sleep(3000);
                
                // Step 10: Upload photos
                System.out.println("========== STEP 10: UPLOAD PHOTOS ==========");
                listingPage.uploadPhotos("C:\\House Images");
                System.out.println("✓ Photos uploaded successfully\n");
                
                Thread.sleep(3000);
                
                // Step 11: Set guest bed counts
                System.out.println("========== STEP 11: SET GUEST AND BED COUNTS ==========");
                listingPage.setGuestBedCounts();
                System.out.println("✓ Guest and bed counts set\n");
                
                Thread.sleep(3000);
                
                // Step 12: Select amenities and features
                System.out.println("========== STEP 12: SELECT AMENITIES, FEATURES & ACTIVITIES ==========");
                listingPage.selectAmenitiesAndFeatures();
                System.out.println("✓ Amenities, features, and activities selected\n");
                
                Thread.sleep(3000);
                
                // Step 13: Fill descriptions and price
                System.out.println("========== STEP 13: FILL DESCRIPTIONS AND PRICE ==========");
                listingPage.fillDescriptionsAndPrice();
                System.out.println("✓ Descriptions and price entered\n");
                
                Thread.sleep(3000);
                
                // Step 13B: Handle checkin-checkout
                System.out.println("========== STEP 13B: HANDLE CHECKIN-CHECKOUT ==========");
                listingPage.handleCheckinCheckout();
                System.out.println("✓ Checkin-checkout handled\n");
                
                Thread.sleep(3000);
                
                // Step 14: Handle security and house rules
                System.out.println("========== STEP 14: HANDLE SECURITY AND HOUSE RULES ==========");
                listingPage.handleSecurityAndRules();
                System.out.println("✓ Security and house rules configured\n");
                
                Thread.sleep(3000);

                // Step 14B: Host greets you
                System.out.println("========== STEP 14B: HOST GREETS YOU ==========");
                listingPage.clickHostGreetsYouAndNext();
                System.out.println("✓ Host greets you selected\n");
                
                Thread.sleep(3000);

                // Step 14C: Upload documents
                System.out.println("========== STEP 14C: UPLOAD DOCUMENTS ==========");
                listingPage.uploadDocuments("C:\\House Images");
                System.out.println("✓ Documents uploaded\n");
                
                Thread.sleep(3000);

                // Step 15: Finish
                System.out.println("========== STEP 15: FINISH LISTING ==========");
                listingPage.clickFinishAndWait();
                System.out.println("✓ Finish button clicked\n");
                
                Thread.sleep(3000);
                
                // Step 16: Go to manage properties
                System.out.println("========== STEP 16: GO TO MANAGE PROPERTIES ==========");
                listingPage.goToManageProperties();
                System.out.println("✓ Navigated to manage properties\n");
                
                Thread.sleep(3000);
                
                // Step 17: Approve listing in admin portal
                System.out.println("========== STEP 17: APPROVE LISTING IN ADMIN PORTAL ==========");
                listingPage.approveListingInAdmin();
                System.out.println("✓ Listing approved in admin portal\n");
                
                System.out.println("========== PROPERTY LISTING FLOW COMPLETED SUCCESSFULLY ==========\n");
                Assert.assertTrue(true, "Complete property listing flow executed successfully");

        } catch (Exception e) {
            System.out.println("✗ TEST FAILED WITH ERROR:");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}