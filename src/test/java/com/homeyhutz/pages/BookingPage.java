package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class BookingPage extends BasePage {

    public BookingPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    private By emailInput = By.name("email");
    private By emailInputByType = By.cssSelector("input[type='email']");
    private By emailInputByPlaceholder = By.xpath("//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'email')]");
    private By actionButtons = By.xpath("//button|//a[contains(@role,'button')]");
    private By submitButtons = By.xpath("//button[@type='submit' and not(@disabled)]");

    private void clickInViewport(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    public void fillEmailIfEmpty(String email) {
        List<WebElement> candidates = driver.findElements(emailInput);
        if (candidates.isEmpty()) {
            candidates = driver.findElements(emailInputByType);
        }
        if (candidates.isEmpty()) {
            candidates = driver.findElements(emailInputByPlaceholder);
        }

        WebElement emailField = null;
        for (WebElement candidate : candidates) {
            if (candidate.isDisplayed()) {
                emailField = candidate;
                break;
            }
        }

        if (emailField == null) {
            System.out.println("INFO: Email field not present on booking page, continuing.");
            return;
        }

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                emailField
        );

        String currentValue = emailField.getDomProperty("value");
        if (currentValue == null || currentValue.isEmpty()) {
            emailField.sendKeys(email);
        }
    }

    public void clickConfirmAndPay() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String[] payKeywords = {
                "confirm & pay", "confirm and pay", "pay now", "confirm", "proceed to pay",
                "continue to pay", "continue", "book now", "reserve", "request to book", "pay"
        };
        String[] blockedKeywords = {"promo", "coupon", "code", "apply", "offer", "discount", "login", "log in", "sign up", "signup", "register"};

        for (int attempt = 0; attempt < 3; attempt++) {
            if (attempt == 1) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            } else if (attempt == 2) {
                js.executeScript("window.scrollTo(0, 0);");
            }

            List<WebElement> submitActions = driver.findElements(submitButtons);
            for (WebElement submitAction : submitActions) {
                try {
                    if (submitAction.isDisplayed()) {
                        String submitText = submitAction.getText();
                        String submitAria = submitAction.getDomAttribute("aria-label");
                        String submitTitle = submitAction.getDomAttribute("title");
                        String submitCombined = ((submitText == null ? "" : submitText) + " "
                                + (submitAria == null ? "" : submitAria) + " "
                                + (submitTitle == null ? "" : submitTitle)).toLowerCase();

                        boolean blocked = false;
                        for (String blockedKeyword : blockedKeywords) {
                            if (submitCombined.contains(blockedKeyword)) {
                                blocked = true;
                                break;
                            }
                        }

                        if (blocked) {
                            continue;
                        }

                        clickInViewport(submitAction);
                        return;
                    }
                } catch (StaleElementReferenceException ignored) {
                    // DOM refreshed; skip stale element and continue scanning
                }
            }

            List<WebElement> actions = driver.findElements(actionButtons);
            for (WebElement action : actions) {
                try {
                    if (!action.isDisplayed()) {
                        continue;
                    }

                    String text = action.getText();
                    String aria = action.getDomAttribute("aria-label");
                    String title = action.getDomAttribute("title");
                    String combined = ((text == null ? "" : text) + " "
                            + (aria == null ? "" : aria) + " "
                            + (title == null ? "" : title)).toLowerCase();

                    boolean blocked = false;
                    for (String blockedKeyword : blockedKeywords) {
                        if (combined.contains(blockedKeyword)) {
                            blocked = true;
                            break;
                        }
                    }

                    if (blocked) {
                        continue;
                    }

                    String normalized = combined;
                    for (String keyword : payKeywords) {
                        if (normalized.contains(keyword)) {
                            clickInViewport(action);
                            return;
                        }
                    }
                } catch (StaleElementReferenceException ignored) {
                    // DOM refreshed; skip stale element and continue scanning
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        throw new RuntimeException("Confirm/Pay CTA not found on booking page. URL: " + driver.getCurrentUrl());
    }
}