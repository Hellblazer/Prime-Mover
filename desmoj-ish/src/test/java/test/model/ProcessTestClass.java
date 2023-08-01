package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import desmoj.core.simulator.Experiment;
import desmoj.demo.balticSea.BalticSeaModel;
import desmoj.demo.balticSea.Ship;
/**
 * Einfaches Beispiel zum verdeutlichen des Problems mit den Nullpointern.
 * Das selbe Problem tritt auch in verschiedenen anderen 
 * Initialisierungsvarianten auf.
 * 
 * @author Sascha Winde
 *
 * @version 1.0	//	10.01.2011
 */
public class ProcessTestClass{

	BalticSeaModel model;
	Ship modelShip;

	@BeforeEach
	public void setUp() throws Exception {
		this.model = new BalticSeaModel();
	      // create model and experiment
        Experiment experiment = new Experiment("BalticSea", java.util.concurrent.TimeUnit.SECONDS, java.util.concurrent.TimeUnit.HOURS, null);

        // and link them to each other
        model.connectToExperiment(experiment);
        //Kommentiert man die naechste Zeile aus funktioniert nur der erste Test 
		this.modelShip = new Ship(model);	
	}

	public void testModelName()
	{
		String name = model.getName();
		assertTrue(name == "Container Shipment in the Baltic Sea");	
		System.out.println(name);
	}
	
	public void testShipName()
	{
		String name = modelShip.getName();
		assertEquals("ship#1", name);
		System.out.println(name);
	}
	
	@AfterEach
	public void tearDown() throws Exception {
	}

}
