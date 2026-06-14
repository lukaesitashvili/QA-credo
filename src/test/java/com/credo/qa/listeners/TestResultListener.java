package com.credo.qa.listeners;

import com.credo.qa.db.TestResult;
import com.credo.qa.db.TestResultRepository;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestResultListener implements ITestListener {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TestResultRepository repository = new TestResultRepository();

    @Override
    public void onTestSuccess(ITestResult result) {
        save(result, "PASSED");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        save(result, "FAILED");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        save(result, "SKIPPED");
    }

    private void save(ITestResult result, String status) {
        TestResult testResult = TestResult.builder()
            .testName(buildTestName(result))
            .status(status)
            .executionTime(LocalDateTime.now().format(FORMATTER))
            .build();

        repository.save(testResult);
    }

    // DataProvider პარამეტრებს ვამატებთ სახელში
    // მაგ: testFilterByAge_Positive(30, 200, 1)
    private String buildTestName(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        Object[] params = result.getParameters();

        if (params == null || params.length == 0) {
            return methodName;
        }

        StringBuilder sb = new StringBuilder(methodName).append("(");
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i]);
            if (i < params.length - 1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
}
