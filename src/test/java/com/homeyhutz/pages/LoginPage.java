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
        try { phoneInput.click(); } catch (Exception ignored) {}
        try { phoneInput.clear(); } catch (Exception ignored) {}
        try {
            phoneInput.sendKeys(phone);
            String val = phoneInput.getAttribute("value");
            if (phone.equals(val)) {
                System.out.println("Phone entered via sendKeys: " + val);
                return;
            }
        } catch (Exception ignored) {}

        // React native value setter — works with controlled inputs (React 16+)
        js.executeScript(
            "var s = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
            "s.call(arguments[0], arguments[1]);" +
            "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));" +
            "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
            phoneInput, phone);
        System.out.println("Phone entered via JS setter: " + js.executeScript("return arguments[0].value;", phoneInput));
    }

    public void clickContinue() {
        WebElement btn = waitForClickability(continueButton);
        // JS click is required for React-controlled forms on this SPA
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        System.out.println("[JS CLICK] Direct click on Continue button executed.");
    }
}
