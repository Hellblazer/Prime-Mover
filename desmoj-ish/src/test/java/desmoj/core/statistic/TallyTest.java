package desmoj.core.statistic;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TallyTest {

    private ConfidenceCalculator tally;

    @BeforeEach
    public void setUp() throws Exception {
        Experiment experiment = new Experiment("testcase");
        Model model = new Model(null, null, false, false) {

            @Override
            public String description() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void doInitialSchedules() {
                // TODO Auto-generated method stub

            }

            @Override
            public void init() {
                // TODO Auto-generated method stub

            }
        };
        model.connectToExperiment(experiment);
        tally = new ConfidenceCalculator(model, "test", false, false);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void testConfidence() {
        tally.update(1.55);
        for (int i = 0; i < 5; i++) {
            tally.update(1.65);
        }
        for (int i = 0; i < 49; i++) {
            tally.update(1.75);
        }
        for (int i = 0; i < 53; i++) {
            tally.update(1.85);
        }
        for (int i = 0; i < 15; i++) {
            tally.update(1.95);
        }
        tally.update(2.05);
        tally.setConfidenceLevel(0.9);
        assertEquals(tally.getConfidenceIntervalOfMeanLowerBound(), 1.803, 0.1);
        assertEquals(tally.getConfidenceIntervalOfMeanUpperBound(), 1.825, 0.1);
    }
}
