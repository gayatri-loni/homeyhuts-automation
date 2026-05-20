package com.homeyhutz.pages;

import com.homeyhutz.utils.WaitHelper;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AdminLoginPage {

    private final WebDriver driver;
    private final WaitHelper waitHelper;

    @FindBy(id = "email")
    private WebElement emailField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(xpath = "//button[contains(.,'Login') or contains(.,'Sign In')]")
    private WebElement loginButton;

    public AdminLoginPage(WebDriver driver) {
        this.driver = driver;
        this.waitHelper = new WaitHelper(driver);
        PageFactory.initElements(driver, this);
    }

    public void open(String adminLoginUrl) {
        driver.get(adminLoginUrl);
        waitHelper.waitForVisible(By.id("email"));
    }

    public void login(String email, String password) {
        waitHelper.waitForVisible(By.id("email")).clear();
        emailField.sendKeys(email);
        passwordField.clear();
        passwordField.sendKeys(password);
        loginButton.click();
    }
}
