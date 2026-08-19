package report;

import com.aventstack.extentreports.ExtentTest;

public class ListenerReportHelper {

    public void safePass(ExtentTest test, String message) {
        if (test != null) {
            test.pass(message);
        } else {
            System.out.println(message);
        }
    }

    public void safeInfo(ExtentTest test, String message) {
        if (test != null) {
            test.info(message);
        } else {
            System.out.println(message);
        }
    }

    public void safeWarning(ExtentTest test, String message) {
        if (test != null) {
            test.warning(message);
        } else {
            System.out.println(message);
        }
    }

    public void safeSkip(ExtentTest test, String message) {
        if (test != null) {
            test.skip(message);
        } else {
            System.out.println(message);
        }
    }

    public void safeFail(ExtentTest test, Throwable throwable) {
        if (test != null) {
            test.fail(throwable);
        } else if (throwable != null) {
            System.out.println(throwable.getMessage());
        }
    }
}
