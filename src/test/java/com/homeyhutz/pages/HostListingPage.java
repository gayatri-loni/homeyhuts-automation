package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HostListingPage extends BasePage {

    // Constructor
    public HostListingPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    private By listYourPropertyLink = By.cssSelector("a[href='/why-list-with-homeyhuts/host-it-yourself']");
    private By continueListingBtn = By.xpath("//button[contains(.,'Continue Listing')]");
    private By fullNameInput = By.xpath("//input[@name='fullName']");
    private By emailInput = By.xpath("//input[@name='email']");
    private By mobileInput = By.xpath("//input[@name='mobile']");
    private By agreeTermsCheckbox = By.cssSelector(".mb-10 > :nth-child(2) > .text-sm");
    private By otpInputs = By.cssSelector("input[inputmode='numeric'], input[name='pin']");
    private By continueListingAfterOtpBtn =
            By.xpath("//button[contains(@class,'bg-brand') and contains(text(),'Continue Listing')]");

    private By createNewListingBtn =
            By.xpath("//button[contains(@class,'ant-btn-primary') and contains(.,'Create New Listing')]");

    private By nextBtnGeneric =
            By.xpath("//button[contains(@class,'bg-brand') and contains(@class,'rounded') and contains(.,'Next')]");
    private By nextBtnTextOnly = By.xpath("//button[contains(.,'Next')]");

    private By propertyTypeHut = By.xpath("//*[contains(text(),'Hut')]");
    private By roomTypeSharedRoom = By.xpath("//*[contains(text(),'Shared Room')]");
    private By propertyOwner = By.xpath("//*[contains(text(),'Property Owner')]");
    private By addressInput = By.xpath("//input[@placeholder='Enter Your Address']");
    private By addressOptionMaharashtra =
            By.xpath("//span[contains(text(),'Maharashtra, India')]");
    private By houseNameInput = By.xpath("//input[@name='propertyAddress']");

    private By fileInput = By.cssSelector("input[type='file']");
        private By documentUploadText =
            By.xpath("//p[contains(@class,'text-xs') and contains(.,'Drag & drop a document')]" );
        private By documentBrowseSpan =
            By.xpath("//p[contains(@class,'text-xs') and contains(.,'Drag & drop a document')]/span[contains(.,'Browse')]" );
        private By documentFileInput =
            By.xpath("//p[contains(@class,'text-xs') and contains(.,'Drag & drop a document')]/ancestor::div[1]//input[@type='file']");

    private By guestsAllowedPlus =
            By.xpath("//div[contains(text(), 'Guests allowed')]/parent::div//div[@class='cursor-pointer'][last()]");
    private By bedsPlus =
            By.xpath("//div[contains(text(), 'Beds')]/parent::div//div[@class='cursor-pointer'][last()]");
    private By bedroomsPlus =
            By.xpath("//div[contains(text(), 'Bedrooms')]/parent::div//div[@class='cursor-pointer'][last()]");
    private By bathroomsPlus =
            By.xpath("//div[contains(text(), 'Bathrooms')]/parent::div//div[@class='cursor-pointer'][last()]");

    private By amenityTv = By.xpath("//*[text()='TV']");
    private By amenityKitchen = By.xpath("//*[text()='Kitchen']");
    private By amenityAc = By.xpath("//*[text()='AC']");

    private By featurePrivatePool = By.xpath("//*[text()='Private Pool']");
    private By featureLakeFront = By.xpath("//*[text()='Lake Front']");
    private By featureCityView = By.xpath("//*[text()='City View']");
    private By featureGlamping = By.xpath("//*[text()='Glamping']");

    private By activityBirdWatching = By.xpath("//*[contains(text(),'Bird Watching')]");
    private By activityJungleSafari = By.xpath("//*[contains(text(),'Jungle Safari')]");
    private By activityVillageTour = By.xpath("//*[contains(text(),'Village Tour')]");
    private By activityTempleVisit = By.xpath("//*[contains(text(),'Temple Visit')]");

    private By titleTextarea = By.cssSelector("textarea[placeholder='Enter title']");
    private By descriptionDiv =
            By.cssSelector("div[contenteditable='true'][data-placeholder='Enter Description']");
    private By neighbourhoodTextarea =
            By.cssSelector("textarea[placeholder='Enter description']");

    private By priceInput = By.cssSelector("input[name='price'], .price-input");

    // Property Address form locators
    private By houseStreetNameInput = By.xpath("//input[@placeholder='House/Flat No. and Street Name *']");
    private By landmarkInput = By.xpath("//input[@placeholder='Nearby Landmark']");

    // Checkin-Checkout page - typically has default values, just need to proceed
    
    private By securityYes = By.xpath("//*[text()='Yes']");

    private By ruleChildFriendly = By.xpath("//label[contains(normalize-space(.),'Child-Friendly Property')]");
    private By ruleInfantFriendly = By.xpath("//label[contains(normalize-space(.),'Infant-Friendly Property')]");
    private By rulePetsAllowed = By.xpath("//label[contains(normalize-space(.),'Pets Allowed')]");

    private By hostGreetsYou = By.xpath("//*[contains(normalize-space(.),'Host greets you')]");

    private By finishBtn = By.xpath("//button[contains(.,'Finish')]");
    private By goToManagePropertiesBtn =
            By.xpath("//button[contains(.,'Go to Manage Properties')]");

    // Admin portal locators (inside same page object for now)
    private By adminEmail = By.id("email");
    private By adminPassword = By.id("password");
    private By adminLoginBtn = By.xpath("//button[contains(.,'Login')]");
    private By adminPropertiesMenu = By.xpath("//span[contains(.,'Properties')]");
    private By adminVerificationMenu = By.xpath("//span[contains(.,'Verification')]");
    private By firstPropertyRow =
            By.cssSelector("tr.ant-table-row.ant-table-row-level-0.cursor-pointer");
    private By pendingApprovalBadge =
            By.xpath("//span[contains(@class,'capitalize') and contains(.,'pending approval')]");
    private By publishedRadio =
            By.xpath("//span[contains(@class,'ant-radio-label') and contains(.,'Published')]");
    private By confirmBtn = By.xpath("//span[contains(.,'Confirm')]");

    // ----- High level actions -----

    public void openHomeAndClickListProperty() {
        driver.get("https://uat.homeyhutz.com/");
        waitForVisibility(listYourPropertyLink).click();
    }

    public void clickContinueListing() {
        try {
            // Wait for button to be clickable
            WebElement button = waitForClickability(continueListingBtn);
            
            // Scroll element into center of view (prevents excessive scrolling)
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", button);
            
            // Wait for any overlays to disappear
            Thread.sleep(1000);
            
            // Try normal click first
            try {
                button.click();
            } catch (ElementClickInterceptedException e) {
                // If normal click fails, use JavaScript click
                System.out.println("Normal click intercepted, using JavaScript click");
                js.executeScript("arguments[0].click();", button);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void fillRegistrationForm(String fullName, String email, String mobile) {
        type(fullNameInput, fullName);
        type(emailInput, email);
        type(mobileInput, mobile);
        click(agreeTermsCheckbox);
    }

    public void enterOtp(String otp) {
        WebElement otpField = waitForVisibility(otpInputs);
        otpField.clear();
        otpField.sendKeys(otp);
    }

    public void clickContinueListingAfterOtp() {
        try {
            // Wait for button to be clickable (modal is already centered, no scroll needed)
            WebElement button = waitForClickability(continueListingAfterOtpBtn);
            
            // Wait briefly for modal to be fully interactive
            Thread.sleep(500);
            
            // Click the button directly - modal is already visible and centered
            try {
                button.click();
            } catch (ElementClickInterceptedException e) {
                // If normal click fails, use JavaScript click
                System.out.println("Button click intercepted, using JavaScript click");
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].click();", button);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void clickCreateNewListing() {
        waitForClickability(createNewListingBtn).click();
    }

    public void clickNext() {
        try {
            click(nextBtnGeneric);
        } catch (Exception e) {
            click(nextBtnTextOnly);
        }
    }

    public void selectPropertyBasics() {
        waitForClickability(propertyTypeHut).click();
        click(By.xpath("//button[contains(@class,'bg-brand') and contains(.,'Next')]"));

        waitForClickability(roomTypeSharedRoom).click();
        clickNext();

        waitForClickability(propertyOwner).click();
        clickNext();
    }

    public void selectLocationAndHouseName() {
        type(addressInput, "Latur");
        waitForClickability(addressOptionMaharashtra).click();
        clickNext();

        type(houseNameInput, "102, Cozy hut near Latur market");
        clickNext();
    }

    public void uploadPhotos(String imagesDirPath) {
        try {
            // Wait for file input to be present
            WebElement file = waitForVisibility(fileInput);
            
            // Get the first 10 image files from the directory
            java.io.File dir = new java.io.File(imagesDirPath);
            java.io.File[] files = dir.listFiles((d, name) -> 
                name.toLowerCase().endsWith(".jpg") || 
                name.toLowerCase().endsWith(".jpeg") || 
                name.toLowerCase().endsWith(".png") || 
                name.toLowerCase().endsWith(".webp"));
            
            if (files == null || files.length == 0) {
                throw new RuntimeException("No image files found in: " + imagesDirPath);
            }
            
            // Build paths for up to 10 images
            StringBuilder filePaths = new StringBuilder();
            int count = Math.min(10, files.length);
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    filePaths.append("\n");
                }
                filePaths.append(files[i].getAbsolutePath());
            }
            
            // Send all file paths at once
            file.sendKeys(filePaths.toString());
            System.out.println("✓ Uploaded " + count + " images from: " + imagesDirPath);
            
            // Wait for upload to complete
            Thread.sleep(3000);
            
            // Click Next button twice
            clickNext();
            Thread.sleep(2000);
            clickNext();
            System.out.println("✓ Clicked Next button twice after upload");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void setGuestBedCounts() {
        try {
            // Click 4 times on each field to add 4 guests, 4 beds, 4 bedrooms, 4 bathrooms
            // Click guests 4 times
            for (int i = 0; i < 4; i++) {
                click(guestsAllowedPlus);
                Thread.sleep(300);
            }
            
            // Click beds 4 times
            for (int i = 0; i < 4; i++) {
                click(bedsPlus);
                Thread.sleep(300);
            }
            
            // Click bedrooms 4 times
            for (int i = 0; i < 4; i++) {
                click(bedroomsPlus);
                Thread.sleep(300);
            }
            
            // Click bathrooms 4 times
            for (int i = 0; i < 4; i++) {
                click(bathroomsPlus);
                Thread.sleep(300);
            }
            
            clickNext();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void selectAmenitiesAndFeatures() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Amenities section
            WebElement tvElement = waitForVisibility(amenityTv);
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'end'});", tvElement);
            Thread.sleep(1000);
            js.executeScript("arguments[0].click();", tvElement);
            WebElement kitchenElement = waitForVisibility(amenityKitchen);
            js.executeScript("arguments[0].click();", kitchenElement);
            WebElement acElement = waitForVisibility(amenityAc);
            js.executeScript("arguments[0].click();", acElement);
            clickNext();
            Thread.sleep(3000);

            // Features section
            WebElement poolElement = waitForVisibility(featurePrivatePool);
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'end'});", poolElement);
            Thread.sleep(1000);
            js.executeScript("arguments[0].click();", poolElement);
            WebElement lakefrontElement = waitForVisibility(featureLakeFront);
            js.executeScript("arguments[0].click();", lakefrontElement);
            WebElement cityElement = waitForVisibility(featureCityView);
            js.executeScript("arguments[0].click();", cityElement);
            WebElement glampingElement = waitForVisibility(featureGlamping);
            js.executeScript("arguments[0].click();", glampingElement);
            clickNext();
            Thread.sleep(3000);

            // Activities section
            WebElement birdElement = waitForVisibility(activityBirdWatching);
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", birdElement);
            Thread.sleep(800);
            js.executeScript("arguments[0].click();", birdElement);

            WebElement safariElement = waitForVisibility(activityJungleSafari);
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", safariElement);
            Thread.sleep(800);
            js.executeScript("arguments[0].click();", safariElement);

            WebElement villageElement = waitForVisibility(activityVillageTour);
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", villageElement);
            Thread.sleep(800);
            js.executeScript("arguments[0].click();", villageElement);

            WebElement templeElement = waitForVisibility(activityTempleVisit);
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", templeElement);
            Thread.sleep(800);
            js.executeScript("arguments[0].click();", templeElement);
            clickNext();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void fillDescriptionsAndPrice() {
        type(titleTextarea, "Beautiful Hut in Latur 1");
        waitForVisibility(descriptionDiv).sendKeys("A cozy shared room hut with amazing views.");
        clickNext();

        type(neighbourhoodTextarea, "Latur is a vibrant town with temples and markets nearby.");
        clickNext();

        type(priceInput, "5000");
        clickNext();
        
        // Debug: Print current URL to see what page we're on
        try {
            Thread.sleep(2000);
            System.out.println("DEBUG: Current URL after price: " + driver.getCurrentUrl());
            System.out.println("DEBUG: Page title: " + driver.getTitle());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void handleCheckinCheckout() {
        try {
            Thread.sleep(2000);
            System.out.println("DEBUG: URL before checkin-checkout: " + driver.getCurrentUrl());
            
            // The checkin-checkout page typically has default times already set
            // Just click next to proceed
            clickNext();
            
            Thread.sleep(2000);
            System.out.println("DEBUG: URL after checkin-checkout: " + driver.getCurrentUrl());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void fillPropertyAddress() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Thread.sleep(2000);
            
            WebElement houseStreetElement = waitForVisibility(houseStreetNameInput);
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", houseStreetElement);
            Thread.sleep(800);
            type(houseStreetNameInput, "Plot No 123, MG Road");
            
            type(landmarkInput, "Near City Mall");
            clickNext();
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void handleSecurityAndRules() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            System.out.println("DEBUG: URL at start of handleSecurityAndRules: " + driver.getCurrentUrl());
            
            // Step 1: Click "Yes" on security-camera page
            WebElement securityElement = waitForVisibility(securityYes);
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", securityElement);
            Thread.sleep(800);
            js.executeScript("arguments[0].click();", securityElement);
            System.out.println("DEBUG: Clicked security Yes");
            clickNext();
            Thread.sleep(2000);
            
            // Step 2: Now on cancellation-policy page, click next
            System.out.println("DEBUG: On cancellation-policy page: " + driver.getCurrentUrl());
            clickNext();
            Thread.sleep(2000);
            
            // Step 3: Now on house-rules page - fill the checkboxes
            System.out.println("DEBUG: On house-rules page: " + driver.getCurrentUrl());
            
            WebElement childFriendlyElement = waitForVisibility(ruleChildFriendly);
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", childFriendlyElement);
            Thread.sleep(800);
            js.executeScript("arguments[0].click();", childFriendlyElement);
            System.out.println("DEBUG: Clicked Child-Friendly");

            WebElement infantFriendlyElement = waitForVisibility(ruleInfantFriendly);
            js.executeScript("arguments[0].click();", infantFriendlyElement);
            System.out.println("DEBUG: Clicked Infant-Friendly");

            WebElement petsAllowedElement = waitForVisibility(rulePetsAllowed);
            js.executeScript("arguments[0].click();", petsAllowedElement);
            System.out.println("DEBUG: Clicked Pets Allowed");
            
            // Step 4: Click next to proceed to checkin-method page
            clickNext();
            Thread.sleep(2000);
            System.out.println("DEBUG: After house-rules, URL: " + driver.getCurrentUrl());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void clickHostGreetsYouAndNext() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            WebElement hostGreetsElement = waitForVisibility(hostGreetsYou);
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", hostGreetsElement);
            Thread.sleep(800);
            js.executeScript("arguments[0].click();", hostGreetsElement);
            clickNext();
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    } 

    public void uploadDocuments(String imagesDirPath) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Thread.sleep(2000);
            
            System.out.println("DEBUG: uploadDocuments - Current URL: " + driver.getCurrentUrl());
            
            // Check if we're on account-verification page (skip documents)
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("account-verification")) {
                System.out.println("DEBUG: On account-verification page, skipping document upload");
                clickNext();
                Thread.sleep(2000);
                return;
            }
            
            // Check if document upload element exists on this page
            if (driver.findElements(documentUploadText).isEmpty()) {
                System.out.println("DEBUG: No document upload element found, skipping document upload");
                clickNext();
                Thread.sleep(2000);
                return;
            }
            
            WebElement uploadText = waitForVisibility(documentUploadText);
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", uploadText);
            Thread.sleep(800);

            if (!driver.findElements(documentBrowseSpan).isEmpty()) {
                WebElement browseElement = waitForVisibility(documentBrowseSpan);
                js.executeScript("arguments[0].click();", browseElement);
            }

            java.io.File dir = new java.io.File(imagesDirPath);
            java.io.File[] files = dir.listFiles((d, name) ->
                    name.toLowerCase().endsWith(".jpg") ||
                    name.toLowerCase().endsWith(".jpeg") ||
                    name.toLowerCase().endsWith(".png") ||
                    name.toLowerCase().endsWith(".webp"));

            if (files == null || files.length == 0) {
                throw new RuntimeException("No image files found in: " + imagesDirPath);
            }

            int count = Math.min(2, files.length);
            StringBuilder filePaths = new StringBuilder();
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    filePaths.append("\n");
                }
                filePaths.append(files[i].getAbsolutePath());
            }

            WebElement documentInput = waitForVisibility(documentFileInput);
            documentInput.sendKeys(filePaths.toString());
            Thread.sleep(3000);
            clickNext();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

   

    public void clickFinishAndWait() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            WebElement finishButton = waitForVisibility(finishBtn);
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", finishButton);
            Thread.sleep(800);
            js.executeScript("arguments[0].click();", finishButton);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void goToManageProperties() {
        try {
            WebElement button = waitForClickability(goToManagePropertiesBtn);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", button);
            Thread.sleep(700);

            try {
                button.click();
            } catch (ElementClickInterceptedException e) {
                System.out.println("Go to Manage Properties click intercepted, using JavaScript click");
                js.executeScript("arguments[0].click();", button);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    // ----- Admin portal actions -----

    public void approveListingInAdmin() {
        driver.get("https://uat.admin.new.homeyhutz.com/login");
        type(adminEmail, "homey@admin.com");
        type(adminPassword, "Password1!");
        click(adminLoginBtn);

        click(adminPropertiesMenu);
        click(adminVerificationMenu);

        click(firstPropertyRow);
        click(pendingApprovalBadge);
        click(publishedRadio);
        click(confirmBtn);
    }
}