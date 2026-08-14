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
}
