package com.homeyhutz.tests;

import com.homeyhutz.base.BaseTest;
import com.homeyhutz.pages.HomePage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HomePageTest extends BaseTest {

    @Test
    public void verifyHomePageLoginNavigation() {

        HomePage homePage = new HomePage(driver, wait);

        // Open home page
        homePage.openHomePage();
        homePage.waitForHomePageToLoad();

        // Click login
        homePage.clickLoginIcon();
        homePage.clickLoginText();

        // ✅ WAIT like Cypress (retry until UI appears)
        homePage.waitForLoginUiToOpen();

        // ✅ Assertion AFTER wait
        Assert.assertTrue(
                driver.findElements(By.cssSelector("input[name='phoneOrEmail']")).size() > 0,
                "Login UI did not open after clicking login"
        );
    }
}
