package com.homeyhutz.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        try {
            if (result.getStatus() == ITestResult.FAILURE && driver != null)
                takeScreenshot(result.getName());
        } catch (Exception e) {
            System.out.println("Screenshot skipped: " + e.getMessage());
        } finally {
            if (driver != null) driver.quit();
        }
    }

    protected void takeScreenshot(String testName) {
        try {
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File dest = new File("screenshots/" + testName + "_" + System.currentTimeMillis() + ".png");
            dest.getParentFile().mkdirs();
            Files.copy(source.toPath(), dest.toPath());
            System.out.println("Screenshot saved: " + dest.getAbsolutePath());
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Logging ───────────────────────────────────────────────────────────────

    protected void logStep(String msg) {
        System.out.println("[" + getClass().getSimpleName() + "] " + msg);
    }

    protected void pause(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
    }

    // ── Element interaction ───────────────────────────────────────────────────

    protected void safeClick(WebElement e) {
        try { e.click(); } catch (Exception ex) { jsClick(e); }
    }

    protected void jsClick(WebElement e) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});arguments[0].click();", e);
    }

    protected void clearAndType(WebElement el, String value) {
        try { el.click(); } catch (WebDriverException ignored) {
            try { ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});arguments[0].click();", el);
            } catch (Exception ignored2) {}
        }
        try { el.clear(); } catch (WebDriverException ignored) {}
        try {
            el.sendKeys(value);
            if (value.equals(el.getAttribute("value"))) return;
        } catch (WebDriverException ignored) {}
        // React native value setter for controlled inputs
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var s=Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
                    "s.call(arguments[0],arguments[1]);" +
                    "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));" +
                    "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", el, value);
            pause(200);
        } catch (Exception e) { logStep("clearAndType JS failed: " + e.getMessage()); }
    }

    // ── Element finders ───────────────────────────────────────────────────────

    protected WebElement visibleElement(By locator) {
        return wait.until(d -> {
            for (WebElement e : d.findElements(locator)) {
                try { if (e.isDisplayed()) return e; } catch (Exception ignored) {}
            }
            return null;
        });
    }

    protected WebElement clickableElement(By locator) {
        return wait.until(d -> {
            for (WebElement e : d.findElements(locator)) {
                try { if (e.isDisplayed() && e.isEnabled()) return e; } catch (Exception ignored) {}
            }
            return null;
        });
    }

    protected WebElement tryFindElement(By locator, int timeoutSeconds) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
                for (WebElement e : d.findElements(locator)) {
                    try { if (e.isDisplayed() && e.isEnabled()) return e; } catch (Exception ignored) {}
                }
                return null;
            });
        } catch (TimeoutException e) { return null; }
    }

    protected boolean hasVisibleElement(By locator) {
        for (WebElement e : driver.findElements(locator)) {
            try { if (e.isDisplayed()) return true; } catch (Exception ignored) {}
        }
        return false;
    }

    protected void waitForDocumentReady() {
        wait.until(d -> "complete".equals(
                ((JavascriptExecutor) d).executeScript("return document.readyState")));
    }
}
