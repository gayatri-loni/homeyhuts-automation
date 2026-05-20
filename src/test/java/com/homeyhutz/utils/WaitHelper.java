package com.homeyhutz.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;

public class WaitHelper {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public WaitHelper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public boolean waitForTextPresent(By locator, String text) {
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public boolean waitForTextOnPage(String text) {
        return wait.until(driver ->
                driver.findElement(By.tagName("body")).getText().contains(text));
    }

    /**
     * Retry mechanism for async price sync across systems.
     * Reloads the page and checks for expected text.
     */
    public boolean retryUntilTextPresent(String expectedText, int maxRetries, int waitMs) {
        for (int i = 1; i <= maxRetries; i++) {
            System.out.println("🔄 Sync check attempt [" + i + "/" + maxRetries + "]"
                    + " — Looking for: " + expectedText);
            String bodyText = driver.findElement(By.tagName("body")).getText();
            if (bodyText.contains(expectedText)) {
                System.out.println("✅ Sync confirmed: " + expectedText);
                return true;
            }
            try {
                Thread.sleep(waitMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            driver.navigate().refresh();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    public void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
