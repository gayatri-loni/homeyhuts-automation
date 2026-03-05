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

public class PropertyPage extends BasePage {

    public PropertyPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    private By propertyCard = By.xpath("//a[contains(@href,'/rooms/')]");
    private By propertyCardAltLink = By.xpath("//a[contains(@href,'/room/') or contains(@href,'/stay/') or contains(@href,'/property/')]");
    private By propertyCardAltContainer = By.cssSelector(".cursor-pointer");
        private By requestToBookBtn = By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'request to book')]");
    private By bookingActionButtons = By.xpath(
            "//button|//a[contains(@role,'button')]"
    );

    private void clickInViewport(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    private boolean clickFirstVisible(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        for (WebElement element : elements) {
            if (element.isDisplayed()) {
                clickInViewport(element);
                return true;
            }
        }
        return false;
    }

    public void selectFirstProperty() {
        wait.until(d ->
                !d.findElements(propertyCard).isEmpty() ||
                !d.findElements(propertyCardAltLink).isEmpty() ||
                !d.findElements(propertyCardAltContainer).isEmpty()
        );

        boolean clicked = clickFirstVisible(propertyCard)
                || clickFirstVisible(propertyCardAltLink)
                || clickFirstVisible(propertyCardAltContainer);

        if (!clicked) {
            throw new RuntimeException("Property card not found/clickable on search results page. URL: " + driver.getCurrentUrl());
        }
        
        // Wait for property details page to load
        wait.until(d -> !d.getCurrentUrl().contains("/search/"));
    }

    public void clickCompleteBooking() {
        if (!tryClickCompleteBooking()) {
            throw new RuntimeException("Booking CTA not found on property page. URL: " + driver.getCurrentUrl());
        }
    }

    public boolean tryClickCompleteBooking() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String[] bookingKeywords = {
                "complete booking", "book now", "reserve", "book", "check availability", "request to book"
        };

        List<WebElement> requestButtons = driver.findElements(requestToBookBtn);
        for (WebElement requestButton : requestButtons) {
            if (requestButton.isDisplayed()) {
                clickInViewport(requestButton);
                return true;
            }
        }

        for (int attempt = 0; attempt < 3; attempt++) {
            if (attempt == 1) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            } else if (attempt == 2) {
                js.executeScript("window.scrollTo(0, 0);");
            }

            List<WebElement> actions = driver.findElements(bookingActionButtons);
            for (WebElement action : actions) {
                try {
                    if (!action.isDisplayed()) {
                        continue;
                    }
                    String text = action.getText();
                    if (text == null || text.isBlank()) {
                        continue;
                    }
                    String normalized = text.toLowerCase();
                    for (String keyword : bookingKeywords) {
                        if (normalized.contains(keyword)) {
                            clickInViewport(action);
                            return true;
                        }
                    }
                } catch (StaleElementReferenceException ignored) {
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        return false;
    }
}