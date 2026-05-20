package com.homeyhutz.agent;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AgentConfig {
    private AgentConfig() {}

    public static final String CLAUDE_MODEL = "claude-sonnet-4-6";
    public static final long   MAX_TOKENS   = 4096L;

    public static final Path   PLAYWRIGHT_PROJECT = Paths.get("c:\\Selenium\\HomeyHutz-Java-Automation");
    public static final Path   SUREFIRE_REPORTS   = PLAYWRIGHT_PROJECT.resolve("target/surefire-reports");
    public static final String SUITE_XML           = "src/test/resources/testng-playwright.xml";

    public static final String SYSTEM_PROMPT =
            "You are an expert QA engineer analyzing test results for the Homeyhuts guest " +
            "authentication flow. The flow has 4 screens:\n" +
            "  SCREEN 1 - Home page: search bar, date pickers, guest selector, property cards, login icon\n" +
            "  SCREEN 2 - Auth page: single input for mobile/email + Continue button.\n" +
            "             Existing user -> OTP popup, new user -> registration form.\n" +
            "  SCREEN 3 - OTP popup: 6-digit OTP field, Submit, Resend OTP timer (57 sec).\n" +
            "             Test OTP: 123456 (UAT environment).\n" +
            "  SCREEN 4 - Registration form: Full Name, Phone (pre-filled), Email, Send OTP.\n\n" +
            "Key business rules:\n" +
            "  - URL must NOT contain 'sign' after successful authentication.\n" +
            "  - New user phones are randomly generated each run (starts 6-9, 10 digits).\n" +
            "  - OTP '123456' is the fixed test OTP for UAT.\n" +
            "  - Base URL: https://uat.homeyhutz.com\n";
}
