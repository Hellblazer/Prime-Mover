package desmoj.core.simulator;

import static org.junit.jupiter.api.Assertions.assertNull;

import desmoj.implementation.TestEntity;
import desmoj.implementation.TestEventGeneric;
import desmoj.implementation.TestModel;
import desmoj.implementation.TestSimProcess;

public class ModelComponentTest{

	ModelComponent model;
	TestModel testModel;
	TestEntity entity1;
	TestEntity entity2;
	TestEventGeneric<TestEntity> event;
	TestSimProcess process;

	/**
	 * @throws Exception
	 * @BeforeEach
	 */
	public void setUp() throws Exception
	{
		this.testModel = new TestModel();
		Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS, java.util.concurrent.TimeUnit.HOURS, null);
		testModel.connectToExperiment(experiment);
		this.entity1 = new TestEntity(testModel, "First Test Entity", false);
		this.entity2 = new TestEntity(testModel, "Second Test Entity", false);
		this.event = new TestEventGeneric<>(testModel, "Event", false);
		this.model = new ModelComponent(null, "Model");
	}

	public void testModel()
	{
		assertNull(model.getModel());
	}



	/**
	 *
	 */
	public void tearDown()
	{

	}

}
