package com.homeyhutz.base;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    protected WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickability(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        waitForClickability(locator).click();
    }

    protected void type(By locator, String text) {
        WebElement element = waitForVisibility(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    protected void retryClick(By locator, int attempts) {
        for (int i = 0; i < attempts; i++) {
            try {
                click(locator);
                return;
            } catch (Exception e) {
                if (i == attempts - 1) {
                    throw e;
                }
            }
        }
    }

    protected void retryWaitUntilPresent(By locator, int attempts) {
        for (int i = 0; i < attempts; i++) {
            try {
                wait.until(d -> d.findElements(locator).size() > 0);
                return;
            } catch (Exception e) {
                if (i == attempts - 1) {
                    logDebugInfo("Element not found with locator: " + locator);
                    throw e;
                }
            }
        }
    }

    /**
     * Logs debug information including page title, URL, and all input fields
     * Useful for debugging locator issues
     */
    protected void logDebugInfo(String message) {
        System.out.println("\n=== DEBUG INFO ===");
        System.out.println("Message: " + message);
        try { System.out.println("Current URL: " + getCurrentUrl()); } catch (Exception ignored) {}
        try { System.out.println("Page Title: " + driver.getTitle()); } catch (Exception ignored) {}

        // Log all input fields on the page
        java.util.List<WebElement> inputs;
        try { inputs = driver.findElements(By.tagName("input")); } catch (Exception ignored) { System.out.println("===================\n"); return; }
        System.out.println("Found " + inputs.size() + " input fields on page:");
        for (int i = 0; i < inputs.size(); i++) {
            WebElement input = inputs.get(i);
            System.out.println("  Input " + (i + 1) + ":");
            System.out.println("    - name: " + input.getDomAttribute("name"));
            System.out.println("    - id: " + input.getDomAttribute("id"));
            System.out.println("    - type: " + input.getDomAttribute("type"));
            System.out.println("    - placeholder: " + input.getDomAttribute("placeholder"));
        }
        System.out.println("===================\n");
    }
}

