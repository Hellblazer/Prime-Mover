package desmoj.core.report;

import desmoj.core.simulator.TimeSpan;
import desmoj.core.statistic.DataListTally;
import desmoj.core.statistic.DataListTally.DataList;

import java.util.Arrays;

/**
 * Captures all relevant information about the DataListTally.
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

public class DataListTallyReporter extends desmoj.core.report.TallyReporter {

    // ****** methods ******

    /**
     * Constructor for a new DataListTallyReporter. Note that although any Reportable is accepted you should make sure
     * that only subtypes of DataListTally are passed to this constructor. Otherwise the number of column titles and
     * their individual headings will differ from the actual content collected by this reporter.
     *
     * @param informationSource desmoj.core.simulator.Reportable : The DataListTally to report about.
     */
    public DataListTallyReporter(desmoj.core.simulator.Reportable informationSource) {
        super(informationSource); // make a Reporter

        numColumns = numColumns + 3;
        columns = Arrays.copyOf(columns, numColumns);
        columns[numColumns - 3] = "Lower Quartile";
        columns[numColumns - 2] = "Median";
        columns[numColumns - 1] = "Upper Quartile";
        groupHeading = "Data-List Tallies";
        groupID = 1911; // see Reporter for more information about groupID
        entries = new String[numColumns];
    }

    /**
     * Calculates the quartiles for the given list of data. This list have to be sorted to produce correct results. This
     * method is called from the getEntries-Method to get all values needed for box-plots.
     *
     * @param list desmoj.core.statistic.DataListTally.DataList : The sorted list to calculate the quartiles for.
     * @return the quartiles from the given list.
     */
    private static double[] getQuartiles(desmoj.core.statistic.DataListTally.DataList list) {
        int n = list.getLength();

        if (n == 0) {
            return null;
        }

        if (n < 3) {
            return new double[] {
            (n == 2) ? (list.getFirst().getValue() + list.getLast().getValue()) / 2 : list.getFirst().getValue() };
        }

        boolean medianInteger = isIntegerNumber(n * 0.5d);
        int medianIndex = (int) (medianInteger ? n * 0.5d : Math.ceil(n * 0.5d)) - 1;
        boolean lowerQuartileInteger = isIntegerNumber(n * 0.25d);
        int lowerQuartileIndex = (int) (lowerQuartileInteger ? n * 0.25d : Math.ceil(list.getLength() * 0.25d)) - 1;
        boolean upperQuartileInteger = isIntegerNumber(n * 0.75d);
        int upperQuartileIndex = (int) (upperQuartileInteger ? n * 0.75d : Math.ceil(list.getLength() * 0.75d)) - 1;

        double median = 0;
        double lowerQuartile = 0;
        double upperQuartile = 0;

        DataList.Element current = list.getFirst();
        int i = 0;

        while (current != null) {
            if (i == medianIndex) {
                median = medianInteger ? (current.getValue() + current.getNext().getValue()) / 2 : current.getValue();
            }
            if (i == lowerQuartileIndex) {
                lowerQuartile = lowerQuartileInteger ? (current.getValue() + current.getNext().getValue()) / 2
                                                     : current.getValue();
            }
            if (i == upperQuartileIndex) {
                upperQuartile = upperQuartileInteger ? (current.getValue() + current.getNext().getValue()) / 2
                                                     : current.getValue();
            }

            current = current.getNext();
            i++;
        }

        return new double[] { lowerQuartile, median, upperQuartile };
    }

    /**
     * Checks if a double represents an interger number
     *
     * @param n double : the double number to check
     * @return true if the given double number is an integer number, false if not.
     */
    private static boolean isIntegerNumber(double n) {
        return (n - (int) n == 0.0);
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
        entries = super.getEntries();

        if (source instanceof DataListTally) {
            DataListTally dlt = (DataListTally) source;

            DataListTally.DataList list = dlt.getDataListSorted();

            double[] quartiles = getQuartiles(list);

            boolean showTimeSpansInReport = dlt.getShowTimeSpansInReport();

            if (quartiles == null) {
                for (int i = 1; i <= 3; i++)
                    entries[numColumns - i] = "Insufficient data";
            } else if (quartiles.length == 1) {
                entries[numColumns - 3] = "Insufficient data";
                entries[numColumns - 2] = showTimeSpansInReport ? new TimeSpan(quartiles[0]).toString()
                                                                : Double.toString(quartiles[0]);
                entries[numColumns - 1] = "Insufficient data";
            } else {
                for (int i = 0; i < 3; i++)
                    entries[numColumns - (3 - i)] = showTimeSpansInReport ? new TimeSpan(quartiles[i]).toString()
                                                                          : Double.toString(quartiles[i]);
            }
        }

        return entries;
    }
}
