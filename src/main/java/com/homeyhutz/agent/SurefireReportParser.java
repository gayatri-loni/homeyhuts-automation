package com.homeyhutz.agent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;

public class SurefireReportParser {

    public static TestResult parseAll() {
        TestResult result = new TestResult();

        if (!Files.exists(AgentConfig.SUREFIRE_REPORTS)) {
            System.err.println("[AGENT] Surefire reports directory not found: " + AgentConfig.SUREFIRE_REPORTS);
            return result;
        }

        File[] xmlFiles = AgentConfig.SUREFIRE_REPORTS.toFile()
                .listFiles(f -> f.getName().endsWith(".xml"));
        if (xmlFiles == null || xmlFiles.length == 0) {
            System.err.println("[AGENT] No XML reports found. Run the tests first.");
            return result;
        }

        Arrays.sort(xmlFiles, Comparator.comparing(File::getName));
        for (File xml : xmlFiles) {
            parseSingle(xml, result);
        }

        result.passed = result.total - result.failed - result.skipped;
        return result;
    }

    private static void parseSingle(File xml, TestResult result) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xml);
            Element root = doc.getDocumentElement();

            int tests    = intAttr(root, "tests");
            int failures = intAttr(root, "failures");
            int errors   = intAttr(root, "errors");
            int skipped  = intAttr(root, "skipped");

            result.total   += tests;
            result.failed  += failures + errors;
            result.skipped += skipped;

            NodeList testcases = root.getElementsByTagName("testcase");
            for (int i = 0; i < testcases.getLength(); i++) {
                Element tc   = (Element) testcases.item(i);
                Element fail = firstChild(tc, "failure");
                Element err  = firstChild(tc, "error");
                Element node = fail != null ? fail : err;
                if (node == null) continue;

                TestResult.FailureDetail fd = new TestResult.FailureDetail();
                fd.classname  = tc.getAttribute("classname");
                fd.testName   = tc.getAttribute("name");
                fd.type       = node.getAttribute("type");
                fd.message    = node.getAttribute("message");
                fd.stacktrace = node.getTextContent().trim();
                result.failures.add(fd);
            }
        } catch (Exception e) {
            System.err.println("[AGENT] Could not parse " + xml.getName() + ": " + e.getMessage());
        }
    }

    private static int intAttr(Element el, String attr) {
        try { return Integer.parseInt(el.getAttribute(attr)); } catch (Exception e) { return 0; }
    }

    private static Element firstChild(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        return nl.getLength() > 0 ? (Element) nl.item(0) : null;
    }
}
