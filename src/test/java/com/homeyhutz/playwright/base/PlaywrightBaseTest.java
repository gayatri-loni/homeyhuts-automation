package com.homeyhutz.playwright.base;

import com.microsoft.playwright.*;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PlaywrightBaseTest {

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;
    protected List<String> consoleErrors;

    @BeforeMethod
    public void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false));
        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1440, 900));
        page = context.newPage();
        page.setDefaultNavigationTimeout(60000);

        consoleErrors = new ArrayList<>();
        page.onConsoleMessage(msg -> {
            if ("error".equals(msg.type())) {
                consoleErrors.add(msg.text());
                System.out.println("[CONSOLE ERROR] " + msg.text());
            }
        });
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE && page != null) {
            takeScreenshot(result.getName());
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    protected void takeScreenshot(String testName) {
        try {
            String path = "screenshots/pw_" + testName + "_" + System.currentTimeMillis() + ".png";
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get(path))
                    .setFullPage(true));
            System.out.println("[SCREENSHOT] Saved: " + path);
        } catch (Exception e) {
            System.out.println("[SCREENSHOT] Failed to save: " + e.getMessage());
        }
    }
}
