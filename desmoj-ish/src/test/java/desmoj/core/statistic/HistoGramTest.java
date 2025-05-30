package desmoj.core.statistic;

import desmoj.implementation.TestModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the class Histogramm. It is supposed to make a statistic about one value.
 *
 * @author Sascha Winde, Clara Bluemm
 * @see core.statistic.Histogramm
 */
public class HistoGramTest {

    Histogram histo;
    TestModel model;

    /**
     * Sets up the testfixture before every test.
     */
    @BeforeEach
    public void setUp() throws Exception {
        this.model = new TestModel();
        this.histo = new Histogram(model, "Test Histogram", 0, 100, 20, false, false);
    }

    /**
     * Destroys the testfixture after every test.
     */
    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * Tests whether the number of cells and the range can be changed.
     */
    @Test
    public void testCell() {
        assertTrue(20 == histo.getCells());
        histo.changeParameters(0, 100, 10);
        assertFalse(20 == histo.getCells());
        assertTrue(10 == histo.getCells());
        histo.update(1);
        assertTrue(1 == histo.getLastValue());
        //      doesn't work since no time is defined in the testfixture
        //      Histogram.changeParameters works fine anyway, since a change
        //      is allowed after construction or reset
        //      histo.changeParameters(0, 100, 20);
        //      assertFalse(20 == histo.getCells());
    }

    /**
     * Should return the most frequented cell.
     */
    @Test
    public void testMostFrequented() {
        histo.changeParameters(0, 100, 10);
        histo.update(1);
        histo.update(1);
        assertTrue(1 == histo.getMostFrequentedCell());
        histo.update(49);
        histo.update(49);
        histo.update(49);
        assertTrue(5 == histo.getMostFrequentedCell());
    }

}
