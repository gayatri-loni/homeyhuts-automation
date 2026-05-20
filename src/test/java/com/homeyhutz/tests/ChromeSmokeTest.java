package com.homeyhutz.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Locale;

public class ChromeSmokeTest {

    private void configureChromeDriverBinary() {
        String configuredPath = System.getProperty("webdriver.chrome.driver");
        if (configuredPath != null && !configuredPath.isBlank()) {
            System.out.println("Using ChromeDriver from webdriver.chrome.driver: " + configuredPath);
            return;
        }

        String envPath = System.getenv("HH_CHROME_DRIVER_PATH");
        if (envPath != null && !envPath.isBlank() && Files.exists(Paths.get(envPath))) {
            System.setProperty("webdriver.chrome.driver", envPath);
            System.out.println("Using ChromeDriver from HH_CHROME_DRIVER_PATH: " + envPath);
            return;
        }

        String[] candidatePaths = new String[]{
                "C:\\Drivers\\chromedriver.exe",
                "C:\\WebDriver\\chromedriver.exe",
                System.getProperty("user.dir") + "\\drivers\\chromedriver.exe",
                System.getProperty("user.dir") + "\\chromedriver.exe"
        };

        for (String candidatePath : candidatePaths) {
            if (Files.exists(Paths.get(candidatePath))) {
                System.setProperty("webdriver.chrome.driver", candidatePath);
                System.out.println("Using local ChromeDriver: " + candidatePath);
                return;
            }
        }

        System.out.println("Local ChromeDriver not found in configured locations, falling back to WebDriverManager.");
        WebDriverManager.chromedriver().setup();
    }

    @Test
    public void shouldOpenExampleDotComInChrome() {
        configureChromeDriverBinary();

        WebDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");

            if (shouldRunHeadless()) {
                options.addArguments("--headless=new");
            }

            driver = new ChromeDriver(options);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            driver.get("https://example.com");
            wait.until(ExpectedConditions.titleContains("Example Domain"));

            Assert.assertTrue(driver.getCurrentUrl().contains("example.com"));
            Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1"))).isDisplayed());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private boolean shouldRunHeadless() {
        String configuredValue = System.getProperty("hh.headless");
        if (configuredValue == null || configuredValue.isBlank()) {
            configuredValue = System.getenv("HH_HEADLESS");
        }

        if (configuredValue == null || configuredValue.isBlank()) {
            return true;
        }

        String normalizedValue = configuredValue.trim().toLowerCase(Locale.ROOT);
        return !("false".equals(normalizedValue) || "0".equals(normalizedValue) || "no".equals(normalizedValue));
    }
}
