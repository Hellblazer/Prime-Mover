package desmoj.core.report;

import desmoj.core.simulator.Reportable;
import desmoj.core.statistic.DataListTally;

/**
 * Captures all relevant information about the DataListTally's DataList.
 *
 * @author Tim Janz
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */

public class DataListHistogramReporter extends Reporter {
    /**
     * Defines the maximal amount of classes found by the algorithm to calculate the histogram's dimensions.
     */
    private static final byte MAX_CLASSES = 15;
    /**
     * Counter for the instances of this class which are not already destructed.
     */
    private static int _activeReporterCount = 0;
    /**
     * Counter for the instances of this class
     */
    private static int _entireReporterCount = 0;
    /**
     * The histogram's dimensions, containing the lowest value, the greatest value, the interval-size and the count of
     * elements in the histogram.
     */
    private double[] _dimensions;

    /**
     * Constructor for a new DataListReporter. Note that although any Reportable is accepted you should make sure that
     * only subtypes of DataListTally.DataList are passed to this constructor. Otherwise an incorrect output will be
     * produced.
     *
     * @param informationSource desmoj.core.simulator.Reportable : The DataListTally to report about.
     * @param min               double : The DataList's minimal value.
     * @param max               double : The DataList's maximal value.
     */
    public DataListHistogramReporter(Reportable informationSource, double min, double max) {
        super(informationSource);

        // To produce a single table for each DataList, the reporters need to be
        // counted and groupID is calculated using this counters.
        _activeReporterCount++;
        _entireReporterCount++;

        groupID = 2011 + (_entireReporterCount * 100);
        groupHeading = "Histogram for " + informationSource.getName();

        _dimensions = getIntervalDimensions(min, max);

        if (source instanceof DataListTally.DataList) {
            groupHeading = "Histogram for " + ((DataListTally.DataList) informationSource).getTallyName();
            numColumns = ((int) _dimensions[3] <= 1) ? 1 : (int) _dimensions[3] - 1;
            columns = new String[numColumns];
            entries = new String[numColumns];

            for (int i = 0; i < numColumns; i++) {
                columns[i] = "[" + (_dimensions[0] + (i * _dimensions[2])) + " - " + (_dimensions[0] + ((i + 1)
                                                                                                        * _dimensions[2]))
                + (i == numColumns - 1 ? "]" : ")");
            }
        }

    }

    /**
     * Calculates the histogram's dimensions, containing the lowest value, the greatest value, the interval-size and the
     * count of elements in the histogram.
     *
     * @param min double : The list's minimal value.
     * @param max double : The list's maximal value.
     * @return the histogram's dimensions.
     */
    private static double[] getIntervalDimensions(double min, double max) {
        double interval = 1;
        double lower, upper;
        double elements;
        double f = 0;

        int factor = 1;

        if (min > 0) {
            while (min < 1) {
                min *= 10;
                max *= 10;
                factor *= 10;
            }
        }

        do {
            if (max % interval == 0) {
                upper = max;
            } else {
                upper = max + (interval - (max % interval));
            }

            if (min % interval == 0) {
                lower = min;
            } else {
                lower = min - (min % interval);
            }

            elements = (int) ((upper - lower) / interval) + 1;

            if (elements <= MAX_CLASSES) {
                break;
            }

            if (Math.log10(interval) % 1 == 0) {
                f = 1.25;
                interval *= f;
                continue;
            }

            if (Math.abs(f - 1.6) < 0.0001) {
                f = 1.25;
            } else {
                f = 1.6;
            }

            interval *= f;
        } while (true);

        while (factor != 1) {
            lower /= 10;
            upper /= 10;
            interval /= 10;
            factor /= 10;
        }

        return new double[] { lower, upper, interval, elements };
    }

    /**
     * Returns an array of Strings each containing the data for the corresponding column in array
     * <code>columns[]</code>. Implement this method in a way, that an array of the same length as the column titles is
     * produced containing the data at the point of time this method is called by someone else to produce up-to-date
     * information.
     *
     * @return java.lang.String[] : Array containing the data for reporting
     */
    @Override
    public String[] getEntries() {

        if (source instanceof DataListTally.DataList) {
            DataListTally.DataList.Element current = ((DataListTally.DataList) source).getFirst();
            int count = 0;
            int index = 0;

            while (current != null) {
                double value = current.getValue();

                if (value < _dimensions[0] + ((index + 1) * _dimensions[2]) || index == (numColumns - 1)) {
                    count++;
                    current = current.getNext();
                } else {
                    entries[index] = Integer.toString(count);
                    count = 0;
                    index++;
                }
            }

            entries[index] = Integer.toString(count);

        } else {
            for (int i = 0; i < numColumns; i++)
                entries[i] = "invalid source!";
        }

        return entries;
    }

    /**
     * Destructor for a DataListReporter
     */
    @Override
    protected void finalize() {
        _activeReporterCount--;

        if (_activeReporterCount == 0) {
            _entireReporterCount = 0;
        }
    }
}
