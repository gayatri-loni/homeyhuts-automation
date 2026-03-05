package com.homeyhutz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.homeyhutz.base.BasePage;

public class HostRegistrationPage extends BasePage {

    private By continueListingButton =
            By.xpath("//button[contains(text(),'Continue Listing')]");

    private By fullNameInput = By.name("fullName");
    private By emailInput = By.name("email");
    private By mobileInput = By.name("mobile");

    private By agreeTermsCheckbox =
            By.cssSelector(".mb-10 .text-sm");

    private By otpInput =
            By.xpath("//input[@inputmode='numeric' or @name='pin' or @type='password' or @name='otp']");

    private By submitButton =
            By.xpath("//button[contains(text(),'Continue')]");

    public HostRegistrationPage(WebDriver driver, WebDriverWait wait) {
    super(driver, wait);
    }
    public void clickContinueListing() {
        System.out.println("Clicking Continue Listing");
        // Wait for element visibility
        WebElement element = wait.until(
                ExpectedConditions.visibilityOfElementLocated(continueListingButton));
        
        // Add delay for page to fully settle
        sleep(2000);
        
        // Scroll element into view
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        
        sleep(1000);
        
        // Wait for element to be clickable
        wait.until(ExpectedConditions.elementToBeClickable(continueListingButton));
        
        sleep(1000);
        
        // Execute click via JavaScript to avoid interception
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", element);
        
        sleep(1000);
    }

    public void enterFullName(String name) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(fullNameInput)).clear();
        driver.findElement(fullNameInput).sendKeys(name);
    }

    public void enterEmail(String email) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).clear();
        driver.findElement(emailInput).sendKeys(email);
    }

    public void enterMobile(String mobile) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(mobileInput)).clear();
        driver.findElement(mobileInput).sendKeys(mobile);
    }

    public void agreeToTerms() {
        wait.until(ExpectedConditions.elementToBeClickable(agreeTermsCheckbox)).click();
    }

    public void enterOtp(String otp) {
        WebElement otpField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(otpInput));
        sleep(1000);
        otpField.click();
        otpField.clear();
        otpField.sendKeys(otp);
        sleep(1000);
    }

    public void submitRegistration() {
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();
    }

    public boolean isRedirectedToHostDashboard() {
        return driver.getCurrentUrl().contains("host.homeyhutz.com");
    }

    // Helper method for sleep
    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}