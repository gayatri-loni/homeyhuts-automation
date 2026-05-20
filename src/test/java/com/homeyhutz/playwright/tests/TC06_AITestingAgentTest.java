package com.homeyhutz.playwright.tests;

import com.homeyhutz.agent.HomeyhutsTestingAgent;
import org.testng.annotations.Test;

/**
 * TC06 — AI-powered analysis of the Playwright test results.
 * Runs after TC01–TC05 and sends results to Claude via Anthropic API.
 * Requires ANTHROPIC_API_KEY environment variable.
 * Skips gracefully if the key is not set.
 */
public class TC06_AITestingAgentTest {

    @Test(description = "AI analysis of Playwright auth suite results via Claude API")
    public void aiAnalysisOfTestResults() {
        System.out.println("\n===== TC06: AI Testing Agent — Claude Analysis =====");

        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("[TC06] SKIP: ANTHROPIC_API_KEY is not set.");
            System.out.println("[TC06] Set it with: $env:ANTHROPIC_API_KEY = 'sk-ant-...'");
            System.out.println("[TC06] Then re-run: mvn test -DsuiteXmlFile=src/test/resources/testng-full.xml");
            return;
        }

        System.out.println("[TC06] API key found. Starting Claude analysis...");
        new HomeyhutsTestingAgent().analyze();
        System.out.println("[TC06] AI analysis complete.");
    }
}
