package com.homeyhutz.agent;

public class AgentMain {

    public static void main(String[] args) {
        HomeyhutsTestingAgent agent = new HomeyhutsTestingAgent();

        String command = args.length > 0 ? args[0] : "run";

        switch (command) {
            case "run" -> {
                String tc = null;
                for (int i = 1; i < args.length - 1; i++) {
                    if ("--tc".equals(args[i])) tc = args[i + 1];
                }
                agent.runAndAnalyze(tc);
            }
            case "analyze" -> agent.analyze();
            default -> {
                System.out.println("Homeyhuts AI Testing Agent (Java)");
                System.out.println();
                System.out.println("Usage:");
                System.out.println("  mvn exec:java                               # run full suite + AI analysis");
                System.out.println("  mvn exec:java -Dexec.args='run --tc TC03'   # run single test class");
                System.out.println("  mvn exec:java -Dexec.args='analyze'         # analyze last run only");
                System.out.println();
                System.out.println("Requires: ANTHROPIC_API_KEY environment variable");
            }
        }
    }
}
