package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.List;

public class RegistrationPage extends BasePage {

    // Updated locators based on actual HTML - field is labeled "Full Name"
    private By firstNameInput = By.cssSelector(
            "input[placeholder='Full Name'], input[placeholder='full name'], " +
            "input[name='fullName'], input[name='firstName'], " +
            "input[id*='form-item'], " +
            "input[type='text']:not([disabled])"
    );
    private By sendOtpButton = By.xpath("//button[contains(text(),'Send OTP')]");

    public RegistrationPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public boolean isOnRegistrationPage() {
        return getCurrentUrl().contains("phone-sign-up");
    }

    public void waitForRegistrationPage() {
        try {
            System.out.println("Waiting for Registration page to load...");
            // Wait up to 5 seconds for first name input to be present
            for (int i = 0; i < 10; i++) {
                List<WebElement> elements = driver.findElements(firstNameInput);
                if (elements.size() > 0 && elements.get(0).isDisplayed()) {
                    System.out.println("✓ Registration page loaded - Full Name input found");
                    return;
                }
                Thread.sleep(500);
            }
            System.out.println("✗ Full Name input not found after waiting");
            logDebugInfo("Failed to locate Full Name input field on registration page");
            throw new Exception("Full Name input field not found");
        } catch (Exception e) {
            System.out.println("✗ Error waiting for registration page: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void enterFirstName(String name) {
        try {
            System.out.println("Entering Full Name: " + name);
            List<WebElement> elements = driver.findElements(firstNameInput);
            if (elements.isEmpty()) {
                System.out.println("✗ Full Name input not found");
                logDebugInfo("Full Name input element not found");
                throw new Exception("Full Name input not found");
            }
            
            WebElement firstNameField = elements.get(0);
            System.out.println("Found input element. Clearing and entering text...");
            firstNameField.clear();
            firstNameField.sendKeys(name);
            System.out.println("✓ Full Name entered successfully: " + name);
        } catch (Exception e) {
            System.out.println("✗ Failed to enter Full Name: " + e.getMessage());
            logDebugInfo("Failed to interact with Full Name field");
            throw new RuntimeException(e);
        }
    }

    public void clickSendOtp() {
        try {
            System.out.println("Clicking Send OTP button...");
            retryClick(sendOtpButton, 3);
            System.out.println("✓ Send OTP button clicked");
        } catch (Exception e) {
            System.out.println("✗ Failed to click Send OTP button");
            logDebugInfo("Failed to click Send OTP button");
            throw e;
        }
    }
}
