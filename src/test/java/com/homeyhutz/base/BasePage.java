package com.homeyhutz.base;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    // 🔹 Wait until element is visible
    protected WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // 🔹 Wait until element is clickable
    protected WebElement waitForClickability(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    // 🔹 Click element safely
    protected void click(By locator) {
        waitForClickability(locator).click();
    }

    // 🔹 Type text safely
    protected void type(By locator, String text) {
        WebElement element = waitForVisibility(locator);
        element.clear();
        element.sendKeys(text);
    }

    // 🔹 Cypress-style retry (for dynamic UI)
    protected void waitUntilElementsPresent(By locator) {
        wait.until(driver ->
                driver.findElements(locator).size() > 0
        );
    }

    // 🔹 Get current URL
    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    // 🔹 Retry click (handles flaky clicks)
protected void retryClick(By locator, int attempts) {
    for (int i = 0; i < attempts; i++) {
        try {
            click(locator);
            return; // success, exit method
        } catch (Exception e) {
            if (i == attempts - 1) {
                throw e; // fail after last attempt
            }
        }
    }
}

// 🔹 Retry wait for element (Cypress-like auto retry)
protected void retryWaitUntilPresent(By locator, int attempts) {
    for (int i = 0; i < attempts; i++) {
        try {
            waitUntilElementsPresent(locator);
            return;
        } catch (Exception e) {
            if (i == attempts - 1) {
                throw e;
            }
        }
    }
}

}
