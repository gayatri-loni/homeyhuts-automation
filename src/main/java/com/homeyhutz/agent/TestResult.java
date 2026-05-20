package com.homeyhutz.agent;

import java.util.ArrayList;
import java.util.List;

public class TestResult {

    public int total;
    public int passed;
    public int failed;
    public int skipped;
    public List<FailureDetail> failures = new ArrayList<>();

    public static class FailureDetail {
        public String classname  = "";
        public String testName   = "";
        public String type       = "";
        public String message    = "";
        public String stacktrace = "";
    }

    public String formatFailuresForClaude() {
        if (failures.isEmpty()) return "No failures — all tests passed!";
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (FailureDetail f : failures) {
            String stack = f.stacktrace.length() > 500
                    ? f.stacktrace.substring(0, 500) + "..." : f.stacktrace;
            sb.append(String.format(
                    "FAILURE %d: %s#%s%n  Type   : %s%n  Message: %s%n  Stack  : %s%n%n",
                    i++, f.classname, f.testName, f.type, f.message, stack));
        }
        return sb.toString();
    }
}
