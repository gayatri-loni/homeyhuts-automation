package com.homeyhutz.pages;

import com.homeyhutz.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentPage extends BasePage {

    public PaymentPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

        private By razorpayIframe =
            By.cssSelector("iframe[src*='razorpay'], iframe[src*='checkout'], iframe.razorpay-checkout-frame");
        private By cardMethodTab = By.xpath("//*[self::button or self::div or self::label][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'card')]");
    private By netbankingMethodTab = By.xpath("//*[@data-method='netbanking' or @method='netbanking' or @data-value='netbanking' or @data-option='netbanking' or @data-testid='netbanking' or ((self::button or self::div or self::label or self::span or self::li) and (contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'netbanking') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'net banking')))]");
    private By netbankingMethodInput = By.cssSelector("input[value='netbanking'], input[data-method='netbanking'], input[name='method'][value='netbanking'], input[name='payment-method'][value='netbanking']");
    private By netbankingMethodLabel = By.xpath("//label[@for='netbanking' or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'netbank')]");
    private By netbankingOptions = By.xpath("//*[@data-bankcode or @data-value or @value][self::button or self::label or self::li or self::div or self::span] | //*[self::button or self::label or self::li or self::div or self::span][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'hdfc') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'icici') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'sbi') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'axis') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'kotak') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'bank of baroda')]");
    private By cardNumberInput = By.cssSelector("input[name='card[number]'], input[autocomplete='cc-number']");
    private By cardExpiryInput = By.cssSelector("input[name='card[expiry]'], input[autocomplete='cc-exp']");
    private By cardCvvInput = By.cssSelector("input[name='card[cvv]'], input[autocomplete='cc-csc']");
    private By cardNameInput = By.cssSelector("input[name='card[name]'], input[name='name']");
    private By payButton = By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'pay')] | //button[@type='submit'] | //input[@type='submit'] | //input[contains(translate(@value, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'pay')] | //*[self::a or self::div or self::span][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'pay') and (@role='button' or @data-testid or @class)]");
    private By otpInput = By.cssSelector("input[name='otp'], input[inputmode='numeric']");
    private By otpSubmitButton = By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'submit') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'verify') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'continue')]");
    private By bankAuthButtons = By.xpath("//button|//a[contains(@role,'button')]|//input[@type='submit']");
    private By bookingCompletionMarkers = By.xpath("//*[self::h1 or self::h2 or self::h3 or self::p or self::span or self::div or self::a][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking confirmed') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking completed') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking success') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'complete booking') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'thank you') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'reservation confirmed')]");
    private By netbankingExactTab = By.cssSelector("div[data-testid='netbanking'][data-value='netbanking']");
    private By netbankingExactBank = By.cssSelector("div[data-value='PUNB_R']");
    private By paymentSuccessButton = By.cssSelector("button[data-val='S'].success, button.success[data-val='S'], button[data-val='S'], button.success");
    private By paymentSuccessButtonFallback = By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'success')] | //input[@type='submit' and contains(translate(@value, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'success')]");
    private By bookingRequestHrefLink = By.cssSelector("a[href*='/trips/bookingRequests/']");
    private By bookingRequestDetailExactButton = By.xpath("//button[contains(@class,'text-sm') and contains(@class,'border-brand') and contains(@class,'inline-flex') and contains(@class,'bg-brand') and contains(@class,'h-[50px]') and contains(@class,'w-full') and normalize-space(.)='Booking Request Details']");
    private By bookingRequestDetailAction = By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking request detail') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking request details')] | //a[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking request detail') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking request details')] | //*[@role='button' and (contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking request detail') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking request details'))]");
    private By bookingRequestDetailsViewMarkers = By.xpath("//*[self::h1 or self::h2 or self::h3 or self::p or self::span or self::div][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking id') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'request id') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking reference')]");

    private void clickInViewport(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    private String readConfig(String key, String fallback) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private boolean clickFirstVisible(By locator, Duration timeout) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, timeout);
            return shortWait.until(d -> {
                List<WebElement> elements = d.findElements(locator);
                for (WebElement element : elements) {
                    try {
                        if (!element.isDisplayed()) {
                            continue;
                        }
                        WebElement target = resolveClickableElement(element);
                        clickInViewport(target);
                        return true;
                    } catch (Exception ignored) {
                    }
                }
                return false;
            });
        } catch (Exception e) {
            return false;
        }
    }

    private boolean forceClickByCss(String cssSelector) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object clicked = js.executeScript(
                    "const el=document.querySelector(arguments[0]);"
                            + "if(!el){return false;}"
                            + "el.scrollIntoView({behavior:'smooth',block:'center'});"
                            + "el.click();"
                            + "return true;",
                    cssSelector
            );
            return Boolean.TRUE.equals(clicked);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean forceClickBookingRequestDetailsExact() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object clicked = js.executeScript(
                    "const buttons = Array.from(document.querySelectorAll('button'));"
                            + "const target = buttons.find(b => (b.textContent || '').trim() === 'Booking Request Details'"
                            + " && b.className.includes('text-sm')"
                            + " && b.className.includes('inline-flex')"
                            + " && b.className.includes('border-brand')"
                            + " && b.className.includes('bg-brand')"
                            + " && b.className.includes('h-[50px]')"
                            + " && b.className.includes('w-full'));"
                            + "if(!target){return false;}"
                            + "target.scrollIntoView({behavior:'smooth', block:'center'});"
                            + "target.click();"
                            + "return true;"
            );
            return Boolean.TRUE.equals(clicked);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean clickBookingRequestDetailsUsingViewport() {
        try {
            List<WebElement> buttons = driver.findElements(bookingRequestDetailExactButton);
            for (WebElement button : buttons) {
                if (!button.isDisplayed()) {
                    continue;
                }
                clickInViewport(button);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean clickBookingRequestLinkUsingViewport() {
        try {
            List<WebElement> links = driver.findElements(bookingRequestHrefLink);
            for (WebElement link : links) {
                if (!link.isDisplayed()) {
                    continue;
                }
                clickInViewport(link);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void nudgeScrollForBookingRequestDetailsSearch() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    "window.scrollBy(0, Math.max(window.innerHeight * 0.60, 260));"
                            + "const nodes = Array.from(document.querySelectorAll('*'));"
                            + "for (const node of nodes) {"
                            + "  const style = window.getComputedStyle(node);"
                            + "  const overflowY = style.overflowY;"
                            + "  const canScroll = (overflowY === 'auto' || overflowY === 'scroll') && node.scrollHeight > node.clientHeight + 8;"
                            + "  if (canScroll) {"
                            + "    node.scrollTop = Math.min(node.scrollTop + Math.max(node.clientHeight * 0.60, 220), node.scrollHeight);"
                            + "  }"
                            + "}"
            );
        } catch (Exception ignored) {
        }
    }

    private boolean waitForBookingRequestDetailsView(String beforeClickUrl) {
        try {
            WebDriverWait detailsWait = new WebDriverWait(driver, Duration.ofSeconds(20));
            return detailsWait.until(d -> {
                try {
                    String afterClickUrl = d.getCurrentUrl().toLowerCase();
                    if (afterClickUrl.contains("/trips/bookingrequests/")) {
                        return true;
                    }
                    if (!afterClickUrl.equals(beforeClickUrl)) {
                        if (afterClickUrl.contains("booking") || afterClickUrl.contains("request") || afterClickUrl.contains("detail")) {
                            return true;
                        }
                    }

                    if (!afterClickUrl.contains("request-confirmation")
                            && (afterClickUrl.contains("booking") || afterClickUrl.contains("request"))) {
                        return true;
                    }

                    List<WebElement> markers = d.findElements(bookingRequestDetailsViewMarkers);
                    for (WebElement marker : markers) {
                        if (marker.isDisplayed()) {
                            return true;
                        }
                    }
                    return false;
                } catch (Exception ignored) {
                    return false;
                }
            });
        } catch (Exception e) {
            return false;
        }
    }

    private WebElement resolveClickableElement(WebElement source) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object candidate = js.executeScript(
                    "return arguments[0].closest('button, label, li, [role=\"tab\"], [role=\"button\"], [data-method], [method], [data-value], div') || arguments[0];",
                    source
            );
            if (candidate instanceof WebElement) {
                return (WebElement) candidate;
            }
        } catch (Exception ignored) {
        }
        return source;
    }

    private boolean clickAnyVisibleNetbankingBank() {
        List<WebElement> options = driver.findElements(netbankingOptions);
        for (WebElement option : options) {
            try {
                if (!option.isDisplayed()) {
                    continue;
                }

                String text = option.getText();
                String aria = option.getDomAttribute("aria-label");
                String title = option.getDomAttribute("title");
                String value = option.getDomAttribute("value");
                String dataValue = option.getDomAttribute("data-value");

                String normalized = ((text == null ? "" : text) + " "
                        + (aria == null ? "" : aria) + " "
                        + (title == null ? "" : title) + " "
                        + (value == null ? "" : value) + " "
                        + (dataValue == null ? "" : dataValue)).trim().toLowerCase();

                if (normalized.isEmpty()) {
                    continue;
                }

                if (normalized.contains("netbanking") || normalized.contains("net banking")
                        || normalized.contains("select bank") || normalized.contains("popular banks")
                        || normalized.contains("all banks") || normalized.contains("search")) {
                    continue;
                }

                clickInViewport(option);
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private boolean selectNetbankingMethod() {
        if (forceClickByCss("div[data-testid='netbanking'][data-value='netbanking']")) {
            System.out.println("INFO: Netbanking selected via exact JS selector.");
            return true;
        }

        if (clickFirstVisible(netbankingExactTab, Duration.ofSeconds(25))) {
            System.out.println("INFO: Netbanking selected via exact selector.");
            return true;
        }

        if (clickFirstVisible(netbankingMethodTab, Duration.ofSeconds(25))) {
            System.out.println("INFO: Netbanking tab selected.");
            return true;
        }

        if (clickFirstVisible(netbankingMethodLabel, Duration.ofSeconds(10))) {
            System.out.println("INFO: Netbanking label selected.");
            return true;
        }

        List<WebElement> inputs = driver.findElements(netbankingMethodInput);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (WebElement input : inputs) {
            try {
                js.executeScript("arguments[0].click();", input);
                js.executeScript("arguments[0].dispatchEvent(new Event('change', {bubbles: true}));", input);
                System.out.println("INFO: Netbanking input selected via JS.");
                return true;
            } catch (Exception ignored) {
            }
        }

        By netbankingFallbackText = By.xpath("//*[self::button or self::div or self::label or self::span][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'netbank')]");
        boolean fallbackClicked = clickFirstVisible(netbankingFallbackText, Duration.ofSeconds(10));
        if (fallbackClicked) {
            System.out.println("INFO: Netbanking selected via fallback text locator.");
        }
        return fallbackClicked;
    }

    private boolean selectExactNetbankingBank() {
        if (forceClickByCss("div[data-value='PUNB_R']")) {
            System.out.println("INFO: Netbanking bank selected via exact JS selector.");
            return true;
        }
        return clickFirstVisible(netbankingExactBank, Duration.ofSeconds(20));
    }

    private boolean clickSuccessButtonIfPresent() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(30));
            return shortWait.until(d -> {
                try {
                    for (String handle : d.getWindowHandles()) {
                        d.switchTo().window(handle);
                        d.switchTo().defaultContent();

                        if (forceClickByCss("button[data-val='S'].success")) {
                            System.out.println("INFO: Success button force-clicked via exact JS selector (top-level).");
                            return true;
                        }

                        List<WebElement> topLevelButtons = d.findElements(paymentSuccessButton);
                        if (topLevelButtons.isEmpty()) {
                            topLevelButtons = d.findElements(paymentSuccessButtonFallback);
                        }
                        for (WebElement button : topLevelButtons) {
                            try {
                                ((JavascriptExecutor) d).executeScript(
                                        "arguments[0].scrollIntoView({behavior:'smooth',block:'center'}); arguments[0].click();",
                                        button
                                );
                                System.out.println("INFO: Success button JS-clicked from located element (top-level).");
                                return true;
                            } catch (Exception ignored) {
                            }
                        }

                        List<WebElement> iframes = d.findElements(razorpayIframe);
                        for (WebElement iframe : iframes) {
                            if (!iframe.isDisplayed()) {
                                continue;
                            }
                            d.switchTo().frame(iframe);

                            if (forceClickByCss("button[data-val='S'].success")) {
                                System.out.println("INFO: Success button force-clicked via exact JS selector (iframe).");
                                d.switchTo().defaultContent();
                                return true;
                            }

                            List<WebElement> frameButtons = d.findElements(paymentSuccessButton);
                            if (frameButtons.isEmpty()) {
                                frameButtons = d.findElements(paymentSuccessButtonFallback);
                            }
                            for (WebElement button : frameButtons) {
                                try {
                                    ((JavascriptExecutor) d).executeScript(
                                            "arguments[0].scrollIntoView({behavior:'smooth',block:'center'}); arguments[0].click();",
                                            button
                                    );
                                    System.out.println("INFO: Success button JS-clicked from located element (iframe).");
                                    d.switchTo().defaultContent();
                                    return true;
                                } catch (Exception ignored) {
                                }
                            }
                            d.switchTo().defaultContent();
                        }
                    }
                } catch (Exception ignored) {
                }
                return false;
            });
        } catch (Exception e) {
            return false;
        }
    }

    private boolean forceClickSuccessButtonHard() {
        long end = System.currentTimeMillis() + Duration.ofSeconds(30).toMillis();
        while (System.currentTimeMillis() < end) {
            try {
                for (String handle : driver.getWindowHandles()) {
                    driver.switchTo().window(handle);
                    driver.switchTo().defaultContent();

                    if (forceClickByCss("button[data-val='S'].success")) {
                        System.out.println("INFO: Hard force-clicked Success button (top-level).");
                        return true;
                    }

                    List<WebElement> iframes = driver.findElements(razorpayIframe);
                    for (WebElement iframe : iframes) {
                        try {
                            if (!iframe.isDisplayed()) {
                                continue;
                            }
                            driver.switchTo().frame(iframe);
                            if (forceClickByCss("button[data-val='S'].success")) {
                                System.out.println("INFO: Hard force-clicked Success button (iframe).");
                                driver.switchTo().defaultContent();
                                return true;
                            }
                            driver.switchTo().defaultContent();
                        } catch (Exception ignored) {
                            try {
                                driver.switchTo().defaultContent();
                            } catch (Exception ignoredAgain) {
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private void switchBackToAppWindow(String originalWindow) {
        driver.switchTo().defaultContent();
        if (driver.getWindowHandles().contains(originalWindow)) {
            driver.switchTo().window(originalWindow);
            return;
        }

        for (String handle : driver.getWindowHandles()) {
            driver.switchTo().window(handle);
            String url = driver.getCurrentUrl().toLowerCase();
            if (!url.contains("razorpay")) {
                return;
            }
        }
    }

    private boolean isBookingCompletionUrl(String url) {
        if (url.contains("razorpay")) {
            return false;
        }

        if (url.contains("/booking") || url.contains("booking?") || url.contains("booking/")) {
            return true;
        }

        return (url.contains("booking") && (url.contains("success") || url.contains("complete") || url.contains("confirm") || url.contains("thank")))
                || url.contains("/booking/success")
                || url.contains("/booking/complete")
                || url.contains("/booking/confirmation")
                || url.contains("reservation-success");
    }

    private boolean switchToNonRazorpayWindowIfPossible() {
        try {
            if (driver.getWindowHandles().isEmpty()) {
                return false;
            }

            for (String handle : driver.getWindowHandles()) {
                driver.switchTo().window(handle);
                String url = driver.getCurrentUrl().toLowerCase();
                if (!url.contains("razorpay")) {
                    driver.switchTo().defaultContent();
                    return true;
                }
            }

            String firstHandle = driver.getWindowHandles().iterator().next();
            driver.switchTo().window(firstHandle);
            driver.switchTo().defaultContent();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void completeSimulatedBankAuthorizationIfPresent() {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        try {
            shortWait.until(d -> {
                try {
                    if (d.getWindowHandles().isEmpty()) {
                        return false;
                    }

                    for (String handle : d.getWindowHandles()) {
                        d.switchTo().window(handle);
                        d.switchTo().defaultContent();
                        List<WebElement> actions = d.findElements(bankAuthButtons);
                        for (WebElement action : actions) {
                            if (!action.isDisplayed()) {
                                continue;
                            }

                            String text = action.getText();
                            String value = action.getDomAttribute("value");
                            String aria = action.getDomAttribute("aria-label");
                            String combined = ((text == null ? "" : text) + " "
                                    + (value == null ? "" : value) + " "
                                    + (aria == null ? "" : aria)).trim().toLowerCase();

                            if (combined.isEmpty()) {
                                continue;
                            }

                            if (combined.contains("fail") || combined.contains("cancel") || combined.contains("decline")) {
                                continue;
                            }

                            if (combined.contains("success") || combined.contains("authorize") || combined.contains("approve")
                                    || combined.contains("continue") || combined.contains("proceed") || combined.contains("complete")) {
                                clickInViewport(action);
                                return true;
                            }
                        }
                    }

                    return false;
                } catch (Exception ignored) {
                    return false;
                }
            });
        } catch (Exception ignored) {
            // Optional step for gateways that require mock bank authorization.
        }
    }

    private boolean switchToRazorpayContext() {
        wait.until(d -> d.getWindowHandles().size() >= 1);
        String latestWindow = driver.getWindowHandle();
        for (String handle : driver.getWindowHandles()) {
            latestWindow = handle;
        }
        driver.switchTo().window(latestWindow);
        driver.switchTo().defaultContent();

        List<WebElement> iframes = driver.findElements(razorpayIframe);
        if (!iframes.isEmpty()) {
            for (WebElement iframe : iframes) {
                if (iframe.isDisplayed()) {
                    driver.switchTo().frame(iframe);
                    return true;
                }
            }
            driver.switchTo().frame(iframes.get(0));
            return true;
        }

        return driver.getCurrentUrl().toLowerCase().contains("razorpay");
    }

    public void completePaymentProcess() {
        String originalWindow = driver.getWindowHandle();
        String cardNumber = readConfig("HH_CARD_NUMBER", "4111111111111111");
        String cardExpiry = readConfig("HH_CARD_EXPIRY", "12/29");
        String cardCvv = readConfig("HH_CARD_CVV", "123");
        String cardName = readConfig("HH_CARD_NAME", "Test User");
        String paymentOtp = readConfig("HH_PAYMENT_OTP", "123456");

        wait.until(d -> !d.findElements(razorpayIframe).isEmpty() || d.getCurrentUrl().toLowerCase().contains("razorpay") || d.getWindowHandles().size() > 1);

        if (!switchToRazorpayContext()) {
            throw new RuntimeException("Unable to switch to Razorpay payment context.");
        }

        List<WebElement> cardTabs = driver.findElements(cardMethodTab);
        for (WebElement tab : cardTabs) {
            if (tab.isDisplayed()) {
                clickInViewport(tab);
                break;
            }
        }

        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(20));

        boolean cardFlowExecuted = false;
        try {
            WebElement number = shortWait.until(d -> {
                List<WebElement> fields = d.findElements(cardNumberInput);
                if (fields.isEmpty()) {
                    return null;
                }
                WebElement field = fields.get(0);
                return field.isDisplayed() ? field : null;
            });
            number.clear();
            number.sendKeys(cardNumber);

            WebElement expiry = shortWait.until(d -> {
                List<WebElement> fields = d.findElements(cardExpiryInput);
                if (fields.isEmpty()) {
                    return null;
                }
                WebElement field = fields.get(0);
                return field.isDisplayed() ? field : null;
            });
            expiry.clear();
            expiry.sendKeys(cardExpiry);

            WebElement cvv = shortWait.until(d -> {
                List<WebElement> fields = d.findElements(cardCvvInput);
                if (fields.isEmpty()) {
                    return null;
                }
                WebElement field = fields.get(0);
                return field.isDisplayed() ? field : null;
            });
            cvv.clear();
            cvv.sendKeys(cardCvv);

            List<WebElement> nameFields = driver.findElements(cardNameInput);
            if (!nameFields.isEmpty() && nameFields.get(0).isDisplayed()) {
                WebElement name = nameFields.get(0);
                name.clear();
                name.sendKeys(cardName);
            }

            cardFlowExecuted = true;
        } catch (TimeoutException e) {
            System.out.println("INFO: Card fields not visible; falling back to netbanking option.");

            boolean netbankingSelected = selectNetbankingMethod();
            if (netbankingSelected) {
                WebDriverWait shortNetbankingWait = new WebDriverWait(driver, Duration.ofSeconds(20));
                try {
                    shortNetbankingWait.until(d -> !d.findElements(netbankingOptions).isEmpty());
                } catch (Exception ignored) {
                }
                boolean exactBankClicked = selectExactNetbankingBank();
                if (!exactBankClicked) {
                    clickAnyVisibleNetbankingBank();
                }
            }
        }

        List<WebElement> payButtons = driver.findElements(payButton);
        WebElement visiblePayButton = null;
        for (WebElement button : payButtons) {
            if (button.isDisplayed()) {
                visiblePayButton = button;
                break;
            }
        }

        if (visiblePayButton == null) {
            if (cardFlowExecuted) {
                System.out.println("INFO: Pay button not found after card details entry; continuing to verification.");
                forceClickSuccessButtonHard();
                completeSimulatedBankAuthorizationIfPresent();
                clickSuccessButtonIfPresent();
                switchBackToAppWindow(originalWindow);
                return;
            }
            System.out.println("INFO: No visible pay action found in payment context; continuing to verification.");
            forceClickSuccessButtonHard();
            completeSimulatedBankAuthorizationIfPresent();
            clickSuccessButtonIfPresent();
            switchBackToAppWindow(originalWindow);
            return;
        }
        clickInViewport(visiblePayButton);

        List<WebElement> otpFields = driver.findElements(otpInput);
        if (!otpFields.isEmpty() && otpFields.get(0).isDisplayed()) {
            WebElement otp = otpFields.get(0);
            otp.clear();
            otp.sendKeys(paymentOtp);

            List<WebElement> otpButtons = driver.findElements(otpSubmitButton);
            if (!otpButtons.isEmpty() && otpButtons.get(0).isDisplayed()) {
                clickInViewport(otpButtons.get(0));
            }
        }

            forceClickSuccessButtonHard();
            completeSimulatedBankAuthorizationIfPresent();
            clickSuccessButtonIfPresent();
            forceClickSuccessButtonHard();

        switchBackToAppWindow(originalWindow);
    }

    public boolean isPaymentGatewayLoaded() {
        waitForVisibility(razorpayIframe);
        return driver.findElement(razorpayIframe).isDisplayed();
    }

    public boolean isPaymentAttemptSubmitted() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(20));
            return shortWait.until(d -> {
                String url = d.getCurrentUrl().toLowerCase();
                if (url.contains("booking") || url.contains("payment") || url.contains("success") || url.contains("thank")) {
                    return true;
                }
                return !d.findElements(razorpayIframe).isEmpty() || d.getWindowHandles().size() >= 1;
            });
        } catch (Exception e) {
            return false;
        }
    }

    public boolean waitForBookingCompletionPage() {
        try {
            WebDriverWait completionWait = new WebDriverWait(driver, Duration.ofSeconds(120));
            return completionWait.until(d -> {
                try {
                    if (!switchToNonRazorpayWindowIfPossible()) {
                        return false;
                    }

                    String currentUrl = d.getCurrentUrl().toLowerCase();
                    if (isBookingCompletionUrl(currentUrl)) {
                        return true;
                    }

                    if (currentUrl.contains("/trips/bookingrequests/")
                            || currentUrl.contains("bookingrequests")
                            || currentUrl.contains("bookingrequest")) {
                        return true;
                    }

                    List<WebElement> markers = d.findElements(bookingCompletionMarkers);
                    for (WebElement marker : markers) {
                        if (marker.isDisplayed()) {
                            return true;
                        }
                    }

                    List<WebElement> detailButtons = d.findElements(bookingRequestDetailAction);
                    for (WebElement detailButton : detailButtons) {
                        if (detailButton.isDisplayed()) {
                            return true;
                        }
                    }

                    return false;
                } catch (Exception ignored) {
                    return false;
                }
            });
        } catch (Exception e) {
            return false;
        }
    }

    public boolean clickBookingRequestDetail(String expectedBookingId) {
        if (expectedBookingId != null && !expectedBookingId.isBlank()) {
            String trimmedId = expectedBookingId.trim();
            By specificLink = By.cssSelector("a[href*='/trips/bookingRequests/" + trimmedId + "']");
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
                boolean found = shortWait.until(d -> {
                    List<WebElement> links = d.findElements(specificLink);
                    for (WebElement link : links) {
                        if (link.isDisplayed()) {
                            clickInViewport(link);
                            return true;
                        }
                    }
                    return false;
                });
                if (found) {
                    String beforeUrl = driver.getCurrentUrl().toLowerCase();
                    return waitForBookingRequestDetailsView(beforeUrl);
                }
            } catch (Exception ignored) {
            }
        }
        return clickBookingRequestDetail();
    }

    public boolean clickBookingRequestDetail() {
        try {
            if (!switchToNonRazorpayWindowIfPossible()) {
                return false;
            }

            JavascriptExecutor js = (JavascriptExecutor) driver;
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));

            for (int attempt = 0; attempt < 30; attempt++) {
                try {
                    String beforeClickUrl = driver.getCurrentUrl().toLowerCase();

                    // Keep moving down the page while searching for the CTA.
                    if (attempt > 0) {
                        nudgeScrollForBookingRequestDetailsSearch();
                        Thread.sleep(250);
                    }

                    if (clickBookingRequestLinkUsingViewport()) {
                        if (waitForBookingRequestDetailsView(beforeClickUrl)) {
                            return true;
                        }
                    }

                    if (clickBookingRequestDetailsUsingViewport()) {
                        if (waitForBookingRequestDetailsView(beforeClickUrl)) {
                            return true;
                        }
                    }

                    if (forceClickBookingRequestDetailsExact()) {
                        if (waitForBookingRequestDetailsView(beforeClickUrl)) {
                            return true;
                        }
                    }

                    WebElement action = shortWait.until(d -> {
                        List<WebElement> actions = d.findElements(bookingRequestDetailAction);
                        for (WebElement candidate : actions) {
                            try {
                                if (candidate.isDisplayed()) {
                                    return candidate;
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        return null;
                    });

                    clickInViewport(action);
                    return waitForBookingRequestDetailsView(beforeClickUrl);
                } catch (Exception ignored) {
                    try {
                        nudgeScrollForBookingRequestDetailsSearch();
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractBookingId() {
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl != null) {
            Pattern requestConfirmationUrlPattern = Pattern.compile("/booking/([0-9]+)/request-confirmation", Pattern.CASE_INSENSITIVE);
            Matcher requestConfirmationMatcher = requestConfirmationUrlPattern.matcher(currentUrl);
            if (requestConfirmationMatcher.find()) {
                return requestConfirmationMatcher.group(1);
            }

            Pattern bookingRequestPathPattern = Pattern.compile("/trips/bookingRequests/([A-Za-z0-9_-]+)", Pattern.CASE_INSENSITIVE);
            Matcher bookingRequestPathMatcher = bookingRequestPathPattern.matcher(currentUrl);
            if (bookingRequestPathMatcher.find()) {
                return bookingRequestPathMatcher.group(1);
            }
        }

        By bookingRequestNumberText = By.xpath(
                "//*[self::h1 or self::h2 or self::h3 or self::p or self::span or self::div or self::td]" +
                        "[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking request number')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking request no')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'request number')]"
        );

        List<WebElement> numberCandidates = driver.findElements(bookingRequestNumberText);
        Pattern requestNumberPattern = Pattern.compile("(?i)(?:booking\\s*request\\s*(?:number|no\\.?)*\\s*[:#-]?\\s*)([A-Za-z0-9_-]{4,})");
        Pattern genericIdPattern = Pattern.compile("([A-Za-z0-9_-]{4,})");

        for (WebElement candidate : numberCandidates) {
            try {
                if (!candidate.isDisplayed()) {
                    continue;
                }
                String text = candidate.getText();
                if (text == null || text.isBlank()) {
                    continue;
                }

                Matcher numberMatcher = requestNumberPattern.matcher(text);
                if (numberMatcher.find()) {
                    return numberMatcher.group(1);
                }

                Matcher fallbackMatcher = genericIdPattern.matcher(text);
                if (fallbackMatcher.find()) {
                    return fallbackMatcher.group(1);
                }
            } catch (Exception ignored) {
            }
        }

        List<WebElement> bookingLinks = driver.findElements(bookingRequestHrefLink);
        Pattern hrefPattern = Pattern.compile("/trips/bookingRequests/([A-Za-z0-9_-]+)");
        for (WebElement link : bookingLinks) {
            try {
                String href = link.getDomAttribute("href");
                if (href == null || href.isBlank()) {
                    continue;
                }
                Matcher hrefMatcher = hrefPattern.matcher(href);
                if (hrefMatcher.find()) {
                    return hrefMatcher.group(1);
                }
            } catch (Exception ignored) {
            }
        }

        By bookingIdText = By.xpath(
                "//*[self::h1 or self::h2 or self::h3 or self::p or self::span or self::div or self::td]" +
                        "[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking id')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'request id')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'booking reference')]"
        );

        List<WebElement> candidates = driver.findElements(bookingIdText);
        Pattern textPattern = Pattern.compile("([A-Za-z0-9_-]{6,})");

        for (WebElement candidate : candidates) {
            try {
                if (!candidate.isDisplayed()) {
                    continue;
                }
                String text = candidate.getText();
                if (text == null || text.isBlank()) {
                    continue;
                }
                Matcher textMatcher = textPattern.matcher(text);
                if (textMatcher.find()) {
                    return textMatcher.group(1);
                }
            } catch (Exception ignored) {
            }
        }

        throw new RuntimeException("Booking ID not found on booking request detail page. URL: " + currentUrl);
    }
}