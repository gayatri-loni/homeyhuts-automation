package com.homeyhutz.tests;

import com.homeyhutz.base.BaseTest;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EdgeSmokeTest extends BaseTest {

    @Test
    public void shouldOpenExampleDotCom() {
        driver.get("https://example.com");
        wait.until(ExpectedConditions.titleContains("Example Domain"));
        Assert.assertTrue(driver.getCurrentUrl().contains("example.com"), "Expected browser to open example.com");
    }
}
