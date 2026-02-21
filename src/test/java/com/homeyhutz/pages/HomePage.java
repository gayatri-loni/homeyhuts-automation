package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HomePage extends BasePage {

    private By logo = By.cssSelector("img[alt='header-logo']");
    private By loginIcon = By.cssSelector(".inline-flex > .flex");
    private By loginTextButton = By.cssSelector(".flex.font-semibold > .cursor-pointer > .text-sm");
    private By phoneInput = By.cssSelector("input[name='phoneOrEmail']");

    public HomePage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public void openHomePage() {
        driver.get("https://uat.homeyhutz.com/");
        waitForVisibility(By.tagName("body"));
    }

    public void waitForHomePageToLoad() {
        waitForVisibility(logo);
    }

    public void clickLoginIcon() {
        retryClick(loginIcon, 3);
    }

    public void clickLoginText() {
        retryClick(loginTextButton, 3);
    }

    public void waitForLoginUiToOpen() {
        retryWaitUntilPresent(phoneInput, 5);
    }

    // Used in SignupFlowTest
    public void openLoginPopup() {
        clickLoginIcon();
        clickLoginText();
        retryWaitUntilPresent(phoneInput, 5);
    }
}
