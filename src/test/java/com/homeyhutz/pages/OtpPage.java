package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OtpPage extends BasePage {

    // 🔹 Locators
    private By otpInput = By.cssSelector("input[name='pin'], input[inputmode='numeric']");
    private By submitButton = By.cssSelector(".space-y-6 > .ring-offset-white");

    // 🔹 Constructor
    public OtpPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait); // 🔔 calls BasePage constructor
    }

    // 🔹 Wait until OTP screen is visible
   public void waitForOtpPage() {
    retryWaitUntilPresent(otpInput, 5);
    }

    // 🔹 Enter OTP
    public void enterOtp(String otp) {
        type(otpInput, otp);
    }

    // 🔹 Click Submit button
   public void clickSubmit() {
    retryClick(submitButton, 3);
    }

}
