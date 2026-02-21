package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OtpPage extends BasePage {

    // Multiple locator strategies for OTP input
    private By otpInput = By.cssSelector(
            "input[name='pin'], input[inputmode='numeric'], input[name='otp'], input[type='password'], input[data-testid='otp']"
    );
    private By submitButton = By.xpath("//button[contains(text(),'Submit')]");
    
    // Error banner locators
    private By errorBanner = By.xpath("//*[contains(text(),'Invalid Phone OTP')]");

    public OtpPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public boolean isOnOtpPage() {
        try {
            return driver.findElements(otpInput).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void waitForOtpPage() {
        try {
            System.out.println("Waiting for OTP page...");
            retryWaitUntilPresent(otpInput, 5);
            System.out.println("✓ OTP page loaded - OTP input field found");
        } catch (Exception e) {
            System.out.println("✗ Failed to find OTP input on OTP page");
            System.out.println("   Current URL: " + getCurrentUrl());
            System.out.println("   Page Title: " + driver.getTitle());
            logDebugInfo("Failed to locate OTP input field");
            throw e;
        }
    }

    public void enterOtp(String otp) {
        try {
            System.out.println("Entering OTP: " + otp);
            type(otpInput, otp);
            System.out.println("✓ OTP entered successfully: " + otp);
        } catch (Exception e) {
            System.out.println("✗ Failed to enter OTP: " + otp);
            logDebugInfo("Failed to interact with OTP field");
            throw e;
        }
    }

    public void clickSubmit() {
        try {
            System.out.println("Clicking Submit button...");
            retryClick(submitButton, 3);
            System.out.println("✓ Submit button clicked");
        } catch (Exception e) {
            System.out.println("✗ Failed to click Submit button");
            logDebugInfo("Failed to click Submit button");
            throw e;
        }
    }
    
    public void waitForInvalidOtpError() {
        try {
            System.out.println("Waiting for invalid OTP error...");
            retryWaitUntilPresent(errorBanner, 5);
            System.out.println("✓ Invalid OTP error appeared");
        } catch (Exception e) {
            System.out.println("✗ Failed to find invalid OTP error message");
            logDebugInfo("Failed to find invalid OTP error");
            throw e;
        }
    }
    
    public boolean isInvalidOtpErrorVisible() {
        try {
            return driver.findElements(errorBanner).size() > 0 && 
                   driver.findElement(errorBanner).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getErrorMessage() {
        try {
            if (isInvalidOtpErrorVisible()) {
                return driver.findElement(errorBanner).getText();
            }
        } catch (Exception e) {
            // Error banner may have auto-dismissed
        }
        return "";
    }
}
