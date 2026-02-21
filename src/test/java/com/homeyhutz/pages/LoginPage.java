package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage extends BasePage {

    private By phoneInput = By.cssSelector(
            "input[name='phoneOrEmail'], input[type='text'], input[type='tel']"
    );

    private By continueButton = By.xpath("//button[contains(text(),'Continue')]");

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public void waitForLoginPage() {
        retryWaitUntilPresent(phoneInput, 5);
    }

    public void enterPhoneNumber(String phone) {
        type(phoneInput, phone);
    }

    public void clickContinue() {
        retryClick(continueButton, 3);
    }
}
