package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

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
        // Wait up to 5 s for phone input (handles React render delay on signup-signin page)
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                ExpectedConditions.visibilityOfElementLocated(phoneInput));
            return;
        } catch (Exception ignored) {}

        clickLoginIcon();
        try {
            clickLoginText();
        } catch (Exception ignored) {
            // Some builds open login directly from icon click.
        }
        retryWaitUntilPresent(phoneInput, 5);
    }
}