package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class SearchPage extends BasePage {

    public SearchPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    private By locationInput = By.cssSelector("input[placeholder='Going To']");
    private By firstLocationOption = By.cssSelector("li.w-full.cursor-pointer");
    private By startDateBtn = By.xpath("//p[contains(text(),'Start')]");
    private By searchBtn = By.cssSelector("img.relative.z-10");
    private By adultsIncreaseBtns = By.name("adults");

    private void clickInViewport(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    public void searchProperty(String city) {

        type(locationInput, city);
        click(firstLocationOption);
        click(startDateBtn);

        // Select check-in date (25)
        click(By.xpath("//td[@role='button' and not(@aria-disabled='true') and text()='25']"));

        // Select check-out date (27)
        click(By.xpath("//td[@role='button' and not(@aria-disabled='true') and text()='27']"));

        // Increase adults twice
        List<WebElement> adultButtons = wait.until(d -> {
            List<WebElement> buttons = d.findElements(adultsIncreaseBtns);
            return buttons;
        });

        if (!adultButtons.isEmpty()) {
            WebElement incrementButton = adultButtons.get(adultButtons.size() - 1);
            for (int i = 0; i < 2; i++) {
                clickInViewport(incrementButton);
            }
        } else {
            System.out.println("INFO: Adult increment control not present, continuing with default guest count.");
        }

        clickInViewport(waitForClickability(searchBtn));
        
        // Wait for search results to load
        try {
            Thread.sleep(2000); // Allow time for page transition and results to load
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}