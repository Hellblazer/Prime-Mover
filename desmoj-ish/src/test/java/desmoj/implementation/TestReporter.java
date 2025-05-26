/**
 *
 */
package desmoj.implementation;

import desmoj.core.report.Reporter;
import desmoj.core.simulator.Schedulable;

/**
 * This class is the Implementation for having a test object of the typ Reporter.
 *
 * @author Clara Bluemm
 * @see desmoj.core.report
 */
public class TestReporter extends Reporter {

    /**
     * Invokes the Constructor of desmoj.core.report.Reporter
     */
    public TestReporter() {
        super((Schedulable) null);
    }

    /**
     * Returns the Entries of the superclass Reporter.
     *
     * @return string
     * @see desmoj.core.report.Reporter
     */
    @Override
    public String[] getEntries() {
        return super.entries;
    }

}
