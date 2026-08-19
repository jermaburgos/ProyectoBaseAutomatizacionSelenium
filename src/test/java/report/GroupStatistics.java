package report;

public class GroupStatistics {

    private int total;
    private int passed;
    private int failed;
    private int skipped;

    public void incrementTotal() {
        total++;
    }

    public void incrementPassed() {
        passed++;
    }

    public void incrementFailed() {
        failed++;
    }

    public void incrementSkipped() {
        skipped++;
    }

    public int getTotal() {
        return total;
    }

    public int getPassed() {
        return passed;
    }

    public int getFailed() {
        return failed;
    }

    public int getSkipped() {
        return skipped;
    }

    public int getExecuted() {
        return passed + failed;
    }

    public double getApprovalPercentage() {
        int executed = getExecuted();

        if (executed == 0) {
            return 0.0;
        }

        return (passed * 100.0) / executed;
    }

    public boolean isApproved(double threshold) {
        return getApprovalPercentage() >= threshold;
    }
}
