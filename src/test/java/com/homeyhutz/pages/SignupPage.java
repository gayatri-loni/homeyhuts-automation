package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SignupPage extends BasePage {

    // 🔹 Locators (converted from Cypress selectors)
    private By headerLoginBtn = By.cssSelector(".inline-flex > .flex");
    private By signupBtn = By.cssSelector(".flex.font-semibold > .cursor-pointer > .text-sm");

    private By phoneInput = By.cssSelector(
            "input[name='phoneOrEmail'], input[type='text'], input[type='tel']"
    );

    private By submitBtn = By.cssSelector("button[type='submit']");
    private By firstNameInput = By.cssSelector("form input");
    private By sendOtpBtn = By.xpath("//button[contains(text(),'Send OTP')]");
    private By otpInput = By.cssSelector("input[inputmode='numeric'], input[name='pin']");
    private By submitOtpBtn = By.xpath("//button[contains(text(),'Submit')]");

    // 🔹 Constructor
    public SignupPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    // 🔹 Actions (business methods)

    public void openHomePage() {
        driver.get("https://uat.homeyhutz.com/");
    }

    public void clickHeaderLogin() {
        click(headerLoginBtn);
    }

    public void clickSignupButton() {
        click(signupBtn);
    }

    public void enterPhoneNumber(String phone) {
        type(phoneInput, phone);
    }

    public void clickContinue() {
        click(submitBtn);
    }

    public boolean isPhoneSignupFlow() {
        return driver.getCurrentUrl().contains("phone-sign-up");
    }

    public void enterFirstName(String name) {
        type(firstNameInput, name);
    }

    public void clickSendOtp() {
        click(sendOtpBtn);
    }

    public void enterOtp(String otp) {
        type(otpInput, otp);
    }

    public void submitOtp() {
        click(submitOtpBtn);
    }

    public boolean isSignupSuccessful() {
        return !driver.getCurrentUrl().contains("sign");
    }
}
