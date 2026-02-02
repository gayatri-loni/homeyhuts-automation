# HomeyHutz Selenium Automation - AI Coding Assistant Instructions

## Project Overview
Maven-based Selenium WebDriver automation framework for testing HomeyHutz UAT environment (`https://uat.homeyhutz.com/`). Uses TestNG for test orchestration and WebDriverManager for automatic driver management.

## Architecture Pattern

### Base Test Structure
All test classes extend `BaseTest` (see [base/BaseTest.java](src/test/java/com/homeyhutz/base/BaseTest.java)):
- `BaseTest` handles WebDriver lifecycle with `@BeforeMethod` and `@AfterMethod`
- Chrome browser is auto-configured via WebDriverManager (no manual driver downloads)
- Browser maximizes on startup
- Protected `driver` and `wait` fields available to all test classes
- Default WebDriverWait timeout: 30 seconds

### Test Class Pattern
```java
public class YourTest extends BaseTest {
    @Test
    public void testName() {
        driver.get("https://uat.homeyhutz.com/...");
        // Use inherited 'driver' and 'wait' fields
    }
}
```

## Key Dependencies & Versions
- **Java 17** (source/target)
- **Selenium 4.18.1** - Modern Selenium API
- **TestNG 7.9.0** - Test framework (not JUnit)
- **WebDriverManager 5.7.0** - Automatic driver management

## Build & Test Commands

### Run All Tests
```bash
mvn clean test
```

### Run Specific Test
```bash
mvn test -Dtest=FirstTest
```

### View Test Reports
After execution, TestNG reports generate in `target/surefire-reports/index.html`

## Project-Specific Conventions

### Wait Strategy
- Use inherited `wait` field from BaseTest (30s timeout)
- Always wait for elements before interaction: `wait.until(ExpectedConditions.visibilityOfElementLocated(...))`
- Example from [FirstTest.java](src/test/java/com/homeyhutz/FirstTest.java#L17): Wait for `<body>` tag before proceeding

### Package Structure
- Base classes: `com.homeyhutz.base.*`
- Test classes: `com.homeyhutz.*` (root package for tests)
- All code under `src/test/java` (no production code)

### Browser Configuration
- Chrome is the default browser (configured in BaseTest)
- To add other browsers, modify BaseTest setup method
- No hardcoded driver paths - WebDriverManager handles all driver binaries

## Creating New Tests

1. Create class in `src/test/java/com/homeyhutz/`
2. Extend `BaseTest`
3. Use `@Test` annotation on test methods
4. Access `driver` and `wait` directly (inherited protected fields)
5. No need for `@BeforeMethod`/`@AfterMethod` - handled by BaseTest

## Critical Notes
- Tests run against UAT environment only (https://uat.homeyhutz.com/)
- Each test method gets fresh browser instance (via `@BeforeMethod` in BaseTest)
- Browser always quits after test (via `@AfterMethod`)
- Maven Surefire plugin runs TestNG tests automatically
