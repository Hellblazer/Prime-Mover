package desmoj.core.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Enumeration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import desmoj.core.simulator.Experiment;
import desmoj.implementation.TestComplexSimProcess;
import desmoj.implementation.TestModel;
import desmoj.implementation.TestSimProcess;

/**
 * This class tests the functionality of the class ComplexSimProcess.
 * 
 * @author Sascuemm
 *
 * 
 */
public abstract class ComplexSimProcessModulTest {

    public TestComplexSimProcess complex;
    public TestModel             model;
    public TestSimProcess        simProcess1;
    public TestSimProcess        simProcess2;

    @BeforeEach
    public void setUp() throws Exception {

        this.model = new TestModel();
        Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                               java.util.concurrent.TimeUnit.HOURS, null);
        model.connectToExperiment(experiment);

        this.simProcess1 = new TestSimProcess(model, "First Test SimProcess", false);
        this.simProcess2 = new TestSimProcess(model, "Second Test SimProcess", false);
        this.complex = new TestComplexSimProcess(model, "First Test ComplexSimProcess", false);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * This Test checks the functionality to add, remove or check for components,
     * given to a ComplexSimProcess
     *
     * @
     */
    public void testComponents() {
        assertNotNull(complex.getComponents());
        assertFalse(complex.hasComponents());
        complex.addComponent(simProcess1);
        complex.addComponent(simProcess2);
        assertTrue(complex.hasComponents());
        assertTrue(complex.contains(simProcess1));
        assertTrue(complex.contains(simProcess2));
        Enumeration e = complex.getComponents();
        assertNotNull(e);
        assertFalse(e == complex.getComponents());
        complex.removeComponent(simProcess1);
        assertFalse(complex.contains(simProcess1));
        assertTrue(complex.hasComponents());
        assertTrue(complex.contains(simProcess2));
        complex.removeAllComponents();
        assertFalse(complex.contains(simProcess2));
        assertFalse(complex.hasComponents());
    }

    /**
     * This Test checks the given Name of the ComplexSimProcess
     */
    public void testName() {
        assertEquals("First Test ComplexSimProcess#1", complex.getName());
    }

}
