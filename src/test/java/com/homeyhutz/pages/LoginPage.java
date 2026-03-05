package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class LoginPage extends BasePage {

    private By phoneInputPrimary = By.cssSelector("input[name='phoneOrEmail']");
    private By phoneInputFallback = By.cssSelector("input[type='tel'], input[inputmode='numeric'], input[type='text']");
    private By continueButton = By.xpath("//button[contains(text(),'Continue')]");

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public void waitForLoginPage() {
        retryWaitUntilPresent(phoneInputPrimary, 5);
    }

    private WebElement resolveVisiblePhoneInput() {
        List<WebElement> primary = driver.findElements(phoneInputPrimary);
        for (WebElement element : primary) {
            if (element.isDisplayed() && element.isEnabled()) {
                return element;
            }
        }

        List<WebElement> fallback = driver.findElements(phoneInputFallback);
        for (WebElement element : fallback) {
            if (element.isDisplayed() && element.isEnabled()) {
                return element;
            }
        }

        throw new RuntimeException("Login phone input not found on page. URL: " + getCurrentUrl());
    }

    public void enterPhoneNumber(String phone) {
        WebElement phoneInput = resolveVisiblePhoneInput();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", phoneInput);
        phoneInput.click();
        phoneInput.clear();
        phoneInput.sendKeys(phone);

        String actualValue = phoneInput.getDomProperty("value");
        if (actualValue == null || !actualValue.contains(phone)) {
            js.executeScript("arguments[0].value = arguments[1];", phoneInput, phone);
            js.executeScript("arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", phoneInput);
            js.executeScript("arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", phoneInput);
        }
    }

    public void clickContinue() {
        retryClick(continueButton, 3);
    }
}
