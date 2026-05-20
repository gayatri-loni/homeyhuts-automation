package com.homeyhutz.playwright.pages;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class PwRazorpayPage {

    private final Page page;

    public PwRazorpayPage(Page page) {
        this.page = page;
    }

    public boolean waitForRazorpayModal() {
        System.out.println("[RZPAY] Waiting for Razorpay...");
        // Razorpay standard checkout renders inside an iframe
        try {
            page.waitForSelector(
                "iframe[src*='razorpay'], iframe[name='razorpay'], " +
                "iframe[src*='checkout.razorpay']",
                new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.VISIBLE).setTimeout(15000)
            );
            System.out.println("[RZPAY] Razorpay iframe detected");
            return true;
        } catch (Exception e) {
            // Some integrations do a full-page redirect to Razorpay
            if (page.url().contains("razorpay")) {
                System.out.println("[RZPAY] Full-page Razorpay redirect: " + page.url());
                return true;
            }
            System.out.println("[RZPAY] Razorpay not detected: " + e.getMessage());
            return false;
        }
    }

    public boolean selectNetBankingAndPay(String bankName) {
        try {
            if (page.url().contains("razorpay")) {
                return handleFullPageRazorpay(bankName);
            }
            return handleIframeRazorpay(bankName);
        } catch (Exception e) {
            System.out.println("[RZPAY] selectNetBankingAndPay error: " + e.getMessage());
            return false;
        }
    }

    // Holds any bank portal popup that opened during payment; consumed by handleTestModeSimulation
    private Page bankPopupPage = null;

    private boolean handleIframeRazorpay(String bankName) {
        Frame rzFrame = page.frames().stream()
            .filter(f -> f.url().contains("razorpay"))
            .findFirst().orElse(null);

        if (rzFrame == null) {
            System.out.println("[RZPAY] Could not locate razorpay frame in page.frames()");
            return false;
        }
        System.out.println("[RZPAY] Found razorpay frame: " + rzFrame.url());
        page.waitForTimeout(2000);

        // Dump frame content for diagnosis
        try {
            String content = (String) rzFrame.evaluate(
                "() => Array.from(document.querySelectorAll('*'))" +
                ".filter(e => e.children.length === 0 && e.innerText && e.innerText.trim().length > 0 && e.innerText.trim().length < 50)" +
                ".map(e => e.tagName + '::[' + e.innerText.trim() + ']')" +
                ".join(' | ')"
            );
            System.out.println("[RZPAY] Frame leaf text: " + content.substring(0, Math.min(content.length(), 400)));
        } catch (Exception ignored) {}

        // Click Netbanking tab
        String[] netbankSelectors = {
            "text=Netbanking", "text=Net Banking", "[data-method='netbanking']",
            "li:has-text('Netbanking')", "a:has-text('Netbanking')", "div:has-text('Netbanking')"
        };
        boolean tabClicked = false;
        for (String sel : netbankSelectors) {
            try {
                rzFrame.locator(sel).first().click(new Locator.ClickOptions().setTimeout(3000));
                System.out.println("[RZPAY] Clicked Netbanking tab via: " + sel);
                tabClicked = true;
                break;
            } catch (Exception ignored) {}
        }
        if (!tabClicked) {
            System.out.println("[RZPAY] Could not click Netbanking tab");
        }
        page.waitForTimeout(2000);

        // Screenshot after Netbanking tab click
        page.screenshot(new Page.ScreenshotOptions()
            .setPath(java.nio.file.Paths.get("C:/Selenium/debug_razorpay_netbanking.png"))
            .setFullPage(false));
        System.out.println("[RZPAY] Screenshot after Netbanking click saved");

        // Dump buttons after Netbanking click
        try {
            String afterClick = (String) rzFrame.evaluate(
                "() => Array.from(document.querySelectorAll('button, [role=\"button\"], input[type=\"submit\"], a'))" +
                ".map(e => e.tagName + '::[' + (e.innerText || e.value || '').trim().substring(0,40) + '] CLASS=[' + e.className.substring(0,40) + ']')" +
                ".join(' | ')"
            );
            System.out.println("[RZPAY] Buttons after Netbanking click: " + afterClick.substring(0, Math.min(afterClick.length(), 500)));
        } catch (Exception ignored) {}

        // Register popup listener BEFORE bank click — live Razorpay opens bank portal in a popup
        bankPopupPage = null;
        BrowserContext ctx = page.context();
        ctx.onPage(newPage -> {
            System.out.println("[RZPAY] Popup opened: " + newPage.url());
            bankPopupPage = newPage;
        });

        // Click bank — banks are DIV elements, Razorpay shows "State Bank of India" not "SBI"
        String[] bankNames = { bankName, "State Bank of India", "HDFC Bank", "ICICI Bank" };
        boolean bankClicked = false;
        if (tabClicked) {
            for (String bank : bankNames) {
                try {
                    rzFrame.locator("text=" + bank).first()
                            .click(new Locator.ClickOptions().setTimeout(3000));
                    System.out.println("[RZPAY] Bank clicked: " + bank);
                    bankClicked = true;
                    break;
                } catch (Exception ignored) {}
            }
            if (!bankClicked) {
                try {
                    rzFrame.locator("div[class*='bg-surface']").first()
                            .click(new Locator.ClickOptions().setTimeout(3000));
                    System.out.println("[RZPAY] Clicked first bank in list (fallback)");
                    bankClicked = true;
                } catch (Exception ignored) {}
            }
        }

        if (bankClicked) {
            System.out.println("[RZPAY] Bank clicked — waiting for payment processing or popup...");
            // Wait up to 8s for popup to appear or processing to complete
            for (int i = 0; i < 8; i++) {
                page.waitForTimeout(1000);
                if (bankPopupPage != null) {
                    System.out.println("[RZPAY] Bank popup appeared: " + bankPopupPage.url());
                    break;
                }
                // Also take screenshot after bank click to capture state
                if (i == 4) {
                    page.screenshot(new Page.ScreenshotOptions()
                        .setPath(java.nio.file.Paths.get("C:/Selenium/debug_razorpay_bank_click.png"))
                        .setFullPage(false));
                    System.out.println("[RZPAY] Screenshot after bank click saved");
                }
            }
            return true;
        }

        System.out.println("[RZPAY] No bank could be clicked — giving up");
        return false;
    }

    private boolean handleFullPageRazorpay(String bankName) {
        // Full-page Razorpay — selectors without frameLocator
        page.locator(
            ":has-text('Net Banking'), [data-method='netbanking']"
        ).first().click();
        System.out.println("[RZPAY] Clicked Net Banking (full-page)");
        page.waitForTimeout(500);

        try {
            page.locator("select[name*='bank']").first().selectOption(bankName);
        } catch (Exception e) {
            page.locator(":has-text('" + bankName + "')").first().click();
        }
        page.waitForTimeout(300);

        page.locator("button:has-text('Pay'), button[type='submit']").first().click();
        System.out.println("[RZPAY] Clicked Pay (full-page)");
        return true;
    }

    public boolean handleTestModeSimulation() {
        System.out.println("[RZPAY] Waiting for test simulation / confirmation after payment...");
        page.waitForTimeout(3000);

        // If a bank portal popup opened, interact with it first
        if (bankPopupPage != null) {
            System.out.println("[RZPAY] Handling bank popup: " + bankPopupPage.url());
            try {
                bankPopupPage.waitForLoadState();
                // Screenshot the popup
                bankPopupPage.screenshot(new Page.ScreenshotOptions()
                    .setPath(java.nio.file.Paths.get("C:/Selenium/debug_razorpay_bank_popup.png"))
                    .setFullPage(false));
                // Dump popup buttons
                String popupBtns = (String) bankPopupPage.evaluate(
                    "() => Array.from(document.querySelectorAll('button,input[type=\"submit\"],a'))" +
                    ".map(e => e.tagName + '::[' + (e.innerText||e.value||'').trim().substring(0,40) + ']')" +
                    ".join(' | ')"
                );
                System.out.println("[RZPAY] Popup buttons: " + (popupBtns != null ? popupBtns.substring(0, Math.min(popupBtns.length(), 300)) : "none"));

                // Try to click success/pay button in popup
                String[] popupSelectors = {
                    "button:has-text('Success')", "input[value='Success']", "input[value='success']",
                    "button:has-text('Pay Now')", "input[value='Pay Now']", "button:has-text('Authorize')",
                    "button:has-text('Submit')", "input[type='submit']", "button[type='submit']"
                };
                for (String sel : popupSelectors) {
                    try {
                        bankPopupPage.locator(sel).first().click(new Locator.ClickOptions().setTimeout(3000));
                        System.out.println("[RZPAY] Clicked '" + sel + "' in bank popup");
                        page.waitForTimeout(4000);
                        return true;
                    } catch (Exception ignored) {}
                }
            } catch (Exception e) {
                System.out.println("[RZPAY] Popup interaction error: " + e.getMessage());
            }
        }

        page.waitForTimeout(2000);
        System.out.println("[RZPAY] Post-payment URL: " + page.url());

        // Screenshot to see what appeared
        try {
            page.screenshot(new Page.ScreenshotOptions()
                .setPath(java.nio.file.Paths.get("C:/Selenium/debug_razorpay_simulation.png"))
                .setFullPage(false));
            System.out.println("[RZPAY] Simulation screenshot saved");
        } catch (Exception ignored) {}

        // Dump all frame URLs to detect bank simulation iframe navigation
        page.frames().forEach(f -> System.out.println("[RZPAY] Frame URL: " + f.url()));

        // Check if auto-redirected to booking confirmation
        String url = page.url();
        if (!url.contains("razorpay") && !url.contains("book/stays")) {
            System.out.println("[RZPAY] Redirected away from checkout — success. URL: " + url);
            return true;
        }

        // Try each frame in turn — bank simulation may load in ANY frame
        for (Frame f : page.frames()) {
            String fUrl = f.url();
            System.out.println("[RZPAY] Checking frame: " + fUrl.substring(0, Math.min(fUrl.length(), 80)));
            try {
                String frameText = (String) f.evaluate(
                    "() => Array.from(document.querySelectorAll('button,input[type=\"submit\"],a'))" +
                    ".map(e => e.tagName + '::[' + (e.innerText||e.value||'').trim().substring(0,40) + ']')" +
                    ".join(' | ')"
                );
                if (frameText != null && !frameText.isEmpty()) {
                    System.out.println("[RZPAY] Frame elements: " + frameText.substring(0, Math.min(frameText.length(), 300)));
                }
            } catch (Exception ignored) {}

            // Try to click Success/Pay/Authorize inside this frame
            String[] simSelectors = {
                "button:has-text('Success')", "input[value='Success']", "input[value='success']",
                "a:has-text('Success')", "button:has-text('Authorize')", "button:has-text('Pay Now')",
                "button:has-text('Simulate Payment')", "button:has-text('Make Payment')",
                "input[value='Make Payment']", "input[value='Pay Now']",
                "button:has-text('Confirm')", "input[type='submit']"
            };
            for (String sel : simSelectors) {
                try {
                    f.locator(sel).first().click(new Locator.ClickOptions().setTimeout(2000));
                    System.out.println("[RZPAY] Clicked '" + sel + "' in frame: " + fUrl.substring(0, Math.min(fUrl.length(), 60)));
                    page.waitForTimeout(4000);
                    return true;
                } catch (Exception ignored) {}
            }
        }

        // Wait up to 15 more seconds for a simulation button on the main page or any frame
        long deadline = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < deadline) {
            // Check main page URL change
            String currentUrl = page.url();
            if (!currentUrl.contains("book/stays") && !currentUrl.contains("razorpay")) {
                System.out.println("[RZPAY] URL changed — success: " + currentUrl);
                return true;
            }
            // Check main page for success button
            try {
                Locator btn = page.locator(
                    "button:has-text('Success'), input[value='Success'], input[value='success'], " +
                    "button:has-text('Authorize'), button:has-text('Pay Now'), " +
                    "input[value='Make Payment'], button:has-text('Make Payment')"
                ).first();
                btn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(2000));
                btn.click();
                System.out.println("[RZPAY] Clicked success button on main page");
                page.waitForTimeout(4000);
                return true;
            } catch (Exception ignored) {}
            // Re-check all frames for new content
            for (Frame f : page.frames()) {
                try {
                    f.locator("button:has-text('Success'), input[value='success'], " +
                              "button:has-text('Pay Now'), input[value='Make Payment']")
                     .first().click(new Locator.ClickOptions().setTimeout(1000));
                    System.out.println("[RZPAY] Late-click success in frame: " + f.url().substring(0, Math.min(f.url().length(), 60)));
                    page.waitForTimeout(4000);
                    return true;
                } catch (Exception ignored) {}
            }
            page.waitForTimeout(1000);
        }

        System.out.println("[RZPAY] No simulation button found after extended wait. URL: " + page.url());
        return false;
    }

    public boolean isVisible() {
        try {
            page.locator("iframe[src*='razorpay'], iframe[name='razorpay']")
                    .first()
                    .waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
            return true;
        } catch (Exception e) {
            return page.url().contains("razorpay");
        }
    }
}
