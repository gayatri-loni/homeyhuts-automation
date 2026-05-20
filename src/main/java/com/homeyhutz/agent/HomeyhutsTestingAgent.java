package com.homeyhutz.agent;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.beta.messages.BetaContentBlock;
import com.anthropic.models.beta.messages.BetaContentBlockParam;
import com.anthropic.models.beta.messages.BetaMessage;
import com.anthropic.models.beta.messages.BetaTool;
import com.anthropic.models.beta.messages.BetaToolResultBlockParam;
import com.anthropic.models.beta.messages.BetaToolUseBlock;
import com.anthropic.models.beta.messages.BetaToolUseBlockParam;
import com.anthropic.models.beta.messages.MessageCreateParams;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HomeyhutsTestingAgent {

    private static final Map<String, String> TC_MAP = Map.of(
            "TC01", "com.homeyhutz.playwright.tests.TC01_HomePageTest",
            "TC02", "com.homeyhutz.playwright.tests.TC02_AuthInputValidationTest",
            "TC03", "com.homeyhutz.playwright.tests.TC03_LoginFlowTest",
            "TC04", "com.homeyhutz.playwright.tests.TC04_SignupFlowTest",
            "TC05", "com.homeyhutz.playwright.tests.TC05_OtpEdgeCasesTest"
    );

    private final AnthropicClient client;

    public HomeyhutsTestingAgent() {
        this.client = AnthropicOkHttpClient.fromEnv();
    }

    // ── Public entry points ────────────────────────────────────────────────────

    public void runAndAnalyze(String tc) {
        printBanner("Homeyhuts AI Testing Agent");

        TestRunner.RunResult run;
        if (tc != null) {
            String cls = TC_MAP.get(tc.toUpperCase());
            if (cls == null) {
                System.err.println("Unknown TC: " + tc + "  Valid: " + TC_MAP.keySet());
                return;
            }
            System.out.println("Running single test class: " + cls);
            run = TestRunner.runSingleTestClass(cls);
        } else {
            System.out.println("Running full Playwright auth suite...");
            run = TestRunner.runSuite(AgentConfig.SUITE_XML);
        }

        System.out.printf("%nMaven build: %s  (%ds)%n",
                run.success() ? "PASSED" : "FAILED", run.elapsedSec());
        analyze();
    }

    public void analyze() {
        System.out.println("\nParsing Surefire reports...");
        TestResult result = SurefireReportParser.parseAll();
        printResultTable(result);

        System.out.println("Sending results to Claude for AI analysis...\n");

        String prompt =
                "Here are the test results for the Homeyhuts Playwright auth test suite:\n\n" +
                "SUMMARY:\n" +
                "  Total:   " + result.total   + "\n" +
                "  Passed:  " + result.passed  + "\n" +
                "  Failed:  " + result.failed  + "\n" +
                "  Skipped: " + result.skipped + "\n\n" +
                "FAILURES:\n" + result.formatFailuresForClaude() + "\n\n" +
                "Please use the 'analyze_test_failures' tool to analyze these results, " +
                "then use 'generate_test_report' to produce an executive report.";

        List<BetaTool> tools = buildTools();

        MessageCreateParams.Builder builder = MessageCreateParams.builder()
                .model(AgentConfig.CLAUDE_MODEL)
                .maxTokens(AgentConfig.MAX_TOKENS)
                .system(AgentConfig.SYSTEM_PROMPT)
                .addTool(tools.get(0))
                .addTool(tools.get(1))
                .addUserMessage(prompt);

        BetaMessage response = client.beta().messages().create(builder.build());

        @SuppressWarnings("unchecked")
        Map<String, JsonValue>[] analysisData = new Map[1];
        @SuppressWarnings("unchecked")
        Map<String, JsonValue>[] reportData = new Map[1];

        while (hasToolUse(response)) {
            List<BetaContentBlockParam> assistantBlocks = new ArrayList<>();
            List<BetaContentBlockParam> toolResults     = new ArrayList<>();

            for (BetaContentBlock block : response.content()) {
                Optional<BetaToolUseBlock> tuOpt = block.toolUse();
                if (tuOpt.isEmpty()) continue;
                BetaToolUseBlock tu = tuOpt.get();

                // Capture the structured tool input
                Map<String, JsonValue> input = jsonObject(tu._input());
                if ("analyze_test_failures".equals(tu.name())) analysisData[0] = input;
                if ("generate_test_report".equals(tu.name()))  reportData[0]   = input;

                // Rebuild the assistant content block to add to history
                assistantBlocks.add(BetaContentBlockParam.ofToolUse(
                        BetaToolUseBlockParam.builder()
                                .id(tu.id())
                                .name(tu.name())
                                .input(tu._input())
                                .build()));

                // Tool result to send back
                toolResults.add(BetaContentBlockParam.ofToolResult(
                        BetaToolResultBlockParam.builder()
                                .toolUseId(tu.id())
                                .content("Tool executed successfully.")
                                .build()));
            }

            builder.addAssistantMessageOfBetaContentBlockParams(assistantBlocks);
            builder.addUserMessageOfBetaContentBlockParams(toolResults);
            response = client.beta().messages().create(builder.build());
        }

        if (analysisData[0] != null) printAnalysis(analysisData[0]);
        if (reportData[0]   != null) printReport(reportData[0]);

        printBanner("Analysis Complete");
    }

    // ── Tool definitions ──────────────────────────────────────────────────────

    private List<BetaTool> buildTools() {
        // analyze_test_failures schema
        Map<String, Object> failureItemProps = new LinkedHashMap<>();
        failureItemProps.put("test_name",     Map.of("type", "string"));
        failureItemProps.put("failure_type",  Map.of("type", "string",
                "enum", List.of("locator", "timing", "environment", "bug", "otp_dependency", "unknown")));
        failureItemProps.put("explanation",   Map.of("type", "string"));
        failureItemProps.put("suggested_fix", Map.of("type", "string"));

        Map<String, Object> failureItem = new LinkedHashMap<>();
        failureItem.put("type",       "object");
        failureItem.put("properties", failureItemProps);
        failureItem.put("required",   List.of("test_name", "failure_type", "explanation", "suggested_fix"));

        Map<String, Object> aProps = new LinkedHashMap<>();
        aProps.put("root_cause_summary", Map.of("type", "string",
                "description", "Overall summary of why tests are failing"));
        aProps.put("failure_analyses",   Map.of("type", "array", "items", failureItem));
        aProps.put("priority_fixes",     Map.of("type", "array",
                "items", Map.of("type", "string"), "description", "Top 3 most impactful fixes"));
        aProps.put("health_score",       Map.of("type", "integer", "minimum", 0, "maximum", 100));

        BetaTool analyzeTool = BetaTool.builder()
                .name("analyze_test_failures")
                .description("Analyze test failures from the Playwright Homeyhuts auth flow tests. " +
                             "Provide root cause analysis and concrete fix suggestions.")
                .inputSchema(BetaTool.InputSchema.builder()
                        .properties(JsonValue.from(aProps))
                        .putAdditionalProperty("required", JsonValue.from(
                                List.of("root_cause_summary", "failure_analyses",
                                        "priority_fixes", "health_score")))
                        .build())
                .build();

        // generate_test_report schema
        Map<String, Object> rProps = new LinkedHashMap<>();
        rProps.put("executive_summary",  Map.of("type", "string"));
        rProps.put("passed_highlights",  Map.of("type", "array", "items", Map.of("type", "string")));
        rProps.put("risk_areas",         Map.of("type", "array", "items", Map.of("type", "string")));
        rProps.put("next_steps",         Map.of("type", "array", "items", Map.of("type", "string")));

        BetaTool reportTool = BetaTool.builder()
                .name("generate_test_report")
                .description("Generate a structured executive test report with AI insights.")
                .inputSchema(BetaTool.InputSchema.builder()
                        .properties(JsonValue.from(rProps))
                        .putAdditionalProperty("required", JsonValue.from(
                                List.of("executive_summary", "passed_highlights",
                                        "risk_areas", "next_steps")))
                        .build())
                .build();

        return List.of(analyzeTool, reportTool);
    }

    // ── Display helpers ────────────────────────────────────────────────────────

    private void printAnalysis(Map<String, JsonValue> data) {
        String root  = str(data.get("root_cause_summary"));
        int    score = num(data.get("health_score"));
        String grade = score >= 70 ? "HEALTHY" : score >= 40 ? "MODERATE" : "CRITICAL";

        printBox("AI Root Cause Analysis",
                "Root Cause  : " + root + "\n" +
                "Health Score: " + score + "/100  [" + grade + "]");

        for (JsonValue item : jsonArray(data.get("failure_analyses"))) {
            Map<String, JsonValue> fm = jsonObject(item);
            printBox(str(fm.get("test_name")),
                    "Type : " + str(fm.get("failure_type")) + "\n" +
                    "Cause: " + str(fm.get("explanation"))  + "\n" +
                    "Fix  : " + str(fm.get("suggested_fix")));
        }

        List<JsonValue> prio = jsonArray(data.get("priority_fixes"));
        if (!prio.isEmpty()) {
            System.out.println("\nPriority Fixes:");
            for (int i = 0; i < prio.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + str(prio.get(i)));
            }
        }
    }

    private void printReport(Map<String, JsonValue> data) {
        printBox("Executive Report", str(data.get("executive_summary")));
        printList("What's Working:", data.get("passed_highlights"), "  + ");
        printList("Risk Areas:",     data.get("risk_areas"),        "  ! ");
        printList("Next Steps:",     data.get("next_steps"),        "  > ");
    }

    private void printList(String header, JsonValue json, String prefix) {
        List<JsonValue> items = jsonArray(json);
        if (items.isEmpty()) return;
        System.out.println("\n" + header);
        for (JsonValue v : items) System.out.println(prefix + str(v));
    }

    private void printResultTable(TestResult r) {
        System.out.println();
        System.out.println("  +------------------------+");
        System.out.println("  |      Test Results      |");
        System.out.println("  +------------------------+");
        System.out.printf( "  |  Total   : %-12d|%n", r.total);
        System.out.printf( "  |  Passed  : %-12d|%n", r.passed);
        System.out.printf( "  |  Failed  : %-12d|%n", r.failed);
        System.out.printf( "  |  Skipped : %-12d|%n", r.skipped);
        System.out.println("  +------------------------+");
        System.out.println();
    }

    private void printBanner(String text) {
        String bar = "=".repeat(text.length() + 4);
        System.out.println("\n+" + bar + "+");
        System.out.println("|  " + text + "  |");
        System.out.println("+" + bar + "+\n");
    }

    private void printBox(String title, String body) {
        int width = 60;
        System.out.println("\n+" + "-".repeat(width) + "+");
        System.out.printf("|  %-" + (width - 2) + "s|%n", title);
        System.out.println("+" + "-".repeat(width) + "+");
        for (String line : body.split("\n")) {
            System.out.printf("|  %-" + (width - 2) + "s|%n", line);
        }
        System.out.println("+" + "-".repeat(width) + "+");
    }

    // ── JsonValue helpers ──────────────────────────────────────────────────────

    private boolean hasToolUse(BetaMessage msg) {
        return msg.content().stream().anyMatch(b -> b.toolUse().isPresent());
    }

    private String str(JsonValue v) {
        if (v == null) return "";
        Optional<?> opt = v.asString();
        return opt.isPresent() ? opt.get().toString() : v.toString();
    }

    private int num(JsonValue v) {
        if (v == null) return 0;
        String raw = str(v);
        try { return Integer.parseInt(raw.trim()); } catch (Exception e) { return 0; }
    }

    @SuppressWarnings("unchecked")
    private List<JsonValue> jsonArray(JsonValue v) {
        if (v == null) return List.of();
        Optional<?> opt = v.asArray();
        return opt.isPresent() ? (List<JsonValue>) opt.get() : List.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, JsonValue> jsonObject(JsonValue v) {
        if (v == null) return Map.of();
        Optional<?> opt = v.asObject();
        return opt.isPresent() ? (Map<String, JsonValue>) opt.get() : Map.of();
    }
}
