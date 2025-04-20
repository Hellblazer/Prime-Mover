package desmoj.core.report;

import java.text.DecimalFormat;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.Reportable;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;

/**
 * A reporter about a simulation run, including information like simulation
 * duration, computation duration and last reset.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler, Johannes G&ouml;bel
 *
 *         Licensed under the Apache License, Version 2.0 (the "License"); you
 *         may not use this file except in compliance with the License. You may
 *         obtain a copy of the License at
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied. See the License for the specific language governing
 *         permissions and limitations under the License.
 *
 */
public class SimulationRunReporter extends desmoj.core.report.TableReporter {

    /**
     * An inner class providing the simulation report.
     */
    public static class SimulationRunReporterProvider extends Reportable {

        SimulationRunReporter r;

        public SimulationRunReporterProvider(Model model) {
            super(model, "SimulationrunReportProvider", true, false);
            r = new SimulationRunReporter(model);
        }

        @Override
        public Reporter createDefaultReporter() {
            return r;
        }
    }

    /**
     * System time at which the simulation has started
     */
    protected long startedAtSystemMillis;

    /**
     * Creates a simulation run reporter for the given model.
     *
     * @param model Model : The model to report about.
     */
    public SimulationRunReporter(Model model) {

        super(model, "Simulation Run");

        startedAtSystemMillis = System.currentTimeMillis();
        groupID = 2000000000;
        numRows = 5;
        numColumns = 2;
        this.columns = new String[] { "Property", "Content" };
        this.groupHeading = "";
    }

    /**
     * Returns the output of the SimulationRunReporter.
     *
     * @return String[][] : The simulation data to be written into the report.
     */
    @Override
    public String[][] getEntryTable() {

        // Determine Computation duration
        long endAtSystemMillis = System.currentTimeMillis();
        int days_tot = 0;
        int hours_tot = 0;
        int minutes_tot = 0;
        int seconds_tot = 0;
        int elapsed_tot = (int) (endAtSystemMillis - startedAtSystemMillis) / 1000;
        seconds_tot = elapsed_tot % 60;
        elapsed_tot /= 60;
        minutes_tot = elapsed_tot % 60;
        elapsed_tot /= 60;
        hours_tot = elapsed_tot % 24;
        elapsed_tot /= 24;
        days_tot = elapsed_tot;
        DecimalFormat formatter = new DecimalFormat("00");
        String duration_tot = (days_tot > 0 ? days_tot + " day" + (days_tot > 1 ? "s " : " ") : "")
        + formatter.format(hours_tot) + ":" + formatter.format(minutes_tot) + ":" + formatter.format(seconds_tot);

        entryTable = new String[numRows][numColumns];
        Model m = (Model) source;

        entryTable[0][0] = "Simulation duration";
        entryTable[0][1] = "Experiment run from " + TimeOperations.getStartTime() + " until " + m.presentTime() + ".";

        entryTable[1][0] = "Computation duration (HH:MM:SS)";
        entryTable[1][1] = duration_tot;

        entryTable[2][0] = "Resets";
        entryTable[2][1] = TimeInstant.isAfter(m.resetAt(), TimeOperations.getStartTime()) ? "Last reset at "
        + m.resetAt() + "." : "No resets during the experiment run.";

        entryTable[3][0] = "Seed";
        entryTable[3][1] = Long.toString(getModel().getExperiment().getDistributionManager().getSeed());

        entryTable[4][0] = "Errors";
        entryTable[4][1] = m.getExperiment()
                            .hasError() ? "Attention: At least one error or warning has occurred. See error output for details."
                                        : "No errors or warnings have occurred.";

        return entryTable;
    }
}
