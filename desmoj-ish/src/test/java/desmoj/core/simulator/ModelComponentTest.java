package desmoj.core.simulator;

import desmoj.implementation.TestEntity;
import desmoj.implementation.TestEventGeneric;
import desmoj.implementation.TestModel;
import desmoj.implementation.TestSimProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class ModelComponentTest {

    TestEntity                   entity1;
    TestEntity                   entity2;
    TestEventGeneric<TestEntity> event;
    ModelComponent               model;
    TestSimProcess               process;
    TestModel                    testModel;

    /**
     * @throws Exception
     * @BeforeEach
     */
    @BeforeEach
    public void setUp() throws Exception {
        this.testModel = new TestModel();
        Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                               java.util.concurrent.TimeUnit.HOURS, null);
        testModel.connectToExperiment(experiment);
        this.entity1 = new TestEntity(testModel, "First Test Entity", false);
        this.entity2 = new TestEntity(testModel, "Second Test Entity", false);
        this.event = new TestEventGeneric<>(testModel, "Event", false);
        this.model = new ModelComponent(null, "Model");
    }

    /**
     *
     */
    public void tearDown() {

    }

    @Test
    public void testModel() {
        assertNull(model.getModel());
    }

}
