package desmoj.core.dist;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContDistEmpiricalTest {

    private ContDistEmpirical cde;

    // Sample cases

    @BeforeEach
    public void startup() {
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

        cde = new ContDistEmpirical(model, "cde", true, false);

    }

    @Test
    public void testBla() {
        cde.addEntry(4, 0.4);
        cde.addEntry(3, 0.3);
        cde.addEntry(8, 0.8);
        cde.addEntry(2, 0.2);
        cde.addEntry(9, 0.9);
        cde.addEntry(7, 0.6);
        cde.addEntry(1, 0.1);
        assertTrue(!cde.isInitialized());
        cde.addEntry(0.5, 0);
        assertTrue(!cde.isInitialized());
        cde.addEntry(9.5, 1);
        assertTrue(cde.isInitialized());

    }

}
