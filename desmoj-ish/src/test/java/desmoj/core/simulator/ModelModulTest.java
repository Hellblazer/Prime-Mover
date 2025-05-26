package desmoj.core.simulator;

import desmoj.implementation.TestModel;
import desmoj.implementation.TestSubModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * this abstract class tests the Model Class on it's own.
 *
 * @author Sascha Winde, Clara Bluemm
 */
public abstract class ModelModulTest {

    Experiment   experiment;
    Experiment   experiment2;
    TestModel    model;
    TestSubModel subModel;

    @BeforeEach
    public void setUp() throws Exception {

        this.model = new TestModel();
        this.experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                         java.util.concurrent.TimeUnit.HOURS, null);
        model.connectToExperiment(experiment);

        this.subModel = new TestSubModel(model);
        this.experiment2 = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                          java.util.concurrent.TimeUnit.HOURS, null);
        subModel.connectToExperiment(experiment2);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * This test checks if the model it connected to an experiment.
     */
    @Test
    public void testExperiment() {
        assertTrue(experiment == model.getExperiment());
        assertTrue(model.isConnected());
    }

    /**
     * This test checks the model hierarchie to be correct.
     */
    @Test
    public void testModelHierarchie() {
        assertTrue(model.hasSubModels());
        assertTrue(model.isMainModel());
        assertFalse(model.isSubModel());
        assertTrue(subModel.isSubModel());
        assertFalse(subModel.isMainModel());
    }

}
