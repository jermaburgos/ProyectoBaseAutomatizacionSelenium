package unit.report;

import report.GroupStatistics;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GroupStatisticsTest {

    @Test
    public void approvalPercentage_debeCalcularSoloPasadasSobreEjecutadas() {
        GroupStatistics statistics = new GroupStatistics();

        statistics.incrementPassed();
        statistics.incrementPassed();
        statistics.incrementFailed();
        statistics.incrementSkipped();

        Assert.assertEquals(statistics.getExecuted(), 3);
        Assert.assertEquals(statistics.getApprovalPercentage(), 66.66666666666667, 0.0001);
    }

    @Test
    public void approvalPercentage_sinEjecuciones_debeSerCero() {
        GroupStatistics statistics = new GroupStatistics();

        Assert.assertEquals(statistics.getExecuted(), 0);
        Assert.assertEquals(statistics.getApprovalPercentage(), 0.0, 0.0001);
    }

    @Test
    public void isApproved_debeRespetarElUmbral() {
        GroupStatistics statistics = new GroupStatistics();

        statistics.incrementPassed();
        statistics.incrementPassed();
        statistics.incrementFailed();

        Assert.assertTrue(statistics.isApproved(60.0));
        Assert.assertFalse(statistics.isApproved(80.0));
    }
}
