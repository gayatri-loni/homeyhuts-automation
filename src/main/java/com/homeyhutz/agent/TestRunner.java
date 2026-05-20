package com.homeyhutz.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestRunner {

    public record RunResult(boolean success, String output, String error, long elapsedSec) {}

    public static RunResult runSuite(String suiteXml) {
        List<String> cmd = List.of(
                "cmd", "/c", "mvn", "test",
                "-DsuiteXmlFile=" + suiteXml,
                "--no-transfer-progress"
        );
        return run(cmd, AgentConfig.PLAYWRIGHT_PROJECT.toFile(), 600);
    }

    public static RunResult runSingleTestClass(String className) {
        String xml = String.format(
                "<?xml version='1.0' encoding='UTF-8'?>" +
                "<!DOCTYPE suite SYSTEM 'https://testng.org/testng-1.0.dtd'>" +
                "<suite name='Single'><test name='Single'>" +
                "<classes><class name='%s'/></classes></test></suite>",
                className);

        File suiteFile = AgentConfig.PLAYWRIGHT_PROJECT.resolve("target/single-suite.xml").toFile();
        suiteFile.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(suiteFile)) {
            fw.write(xml);
        } catch (Exception e) {
            return new RunResult(false, "", e.getMessage(), 0);
        }
        return runSuite("target/single-suite.xml");
    }

    private static RunResult run(List<String> cmd, File workDir, int timeoutSec) {
        long start = System.currentTimeMillis();
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(workDir);
            pb.redirectErrorStream(true);

            Process proc = pb.start();
            StringBuilder out = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    out.append(line).append('\n');
                }
            }

            boolean finished = proc.waitFor(timeoutSec, TimeUnit.SECONDS);
            if (!finished) {
                proc.destroyForcibly();
                return new RunResult(false, out.toString(),
                        "Timed out after " + timeoutSec + "s", elapsed(start));
            }
            return new RunResult(proc.exitValue() == 0, out.toString(), "", elapsed(start));

        } catch (Exception e) {
            return new RunResult(false, "", e.getMessage(), elapsed(start));
        }
    }

    private static long elapsed(long start) {
        return (System.currentTimeMillis() - start) / 1000;
    }
}
