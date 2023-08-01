package test.core.simulator;

import static org.junit.jupiter.api.Assertions.assertNull;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.ModelComponent;
import test.implementation.TestEntity;
import test.implementation.TestEventGeneric;
import test.implementation.TestModel;
import test.implementation.TestSimProcess;

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
		this.event = new TestEventGeneric<TestEntity>(testModel, "Event", false);
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
