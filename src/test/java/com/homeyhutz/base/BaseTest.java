package com.homeyhutz.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;

public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    // Runs BEFORE every test
    @BeforeMethod
    public void setUp() {

        // Setup ChromeDriver automatically
        WebDriverManager.chromedriver().setup();

        // Open Chrome browser
        driver = new ChromeDriver();

        // Maximize browser window
        driver.manage().window().maximize();

        // Create explicit wait object (30 seconds)
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // Runs AFTER every test
  @AfterMethod
public void tearDown(ITestResult result) {

    try {
        if (result.getStatus() == ITestResult.FAILURE) {
            if (driver != null) {
                takeScreenshot(result.getName());
            }
        }
    } catch (Exception e) {
        System.out.println("Screenshot skipped: " + e.getMessage());
    } finally {
        if (driver != null) {
            driver.quit();
        }
    }
}


    // Method to take screenshot
    private void takeScreenshot(String testName) {

        try {
            // Convert WebDriver to TakesScreenshot
            TakesScreenshot screenshot = (TakesScreenshot) driver;

            // Capture screenshot as file
            File source = screenshot.getScreenshotAs(OutputType.FILE);

            // Create screenshots folder & file name
            File destination = new File(
                    "screenshots/" + testName + "_" + System.currentTimeMillis() + ".png"
            );

            // Create folder if not exists
            destination.getParentFile().mkdirs();

            // Copy screenshot to destination
            Files.copy(source.toPath(), destination.toPath());

            // Print screenshot path in console
            System.out.println("Screenshot saved at: " + destination.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
