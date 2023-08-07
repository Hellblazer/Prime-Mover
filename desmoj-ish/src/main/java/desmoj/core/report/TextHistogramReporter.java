package desmoj.core.report;

import desmoj.core.report.html5chart.ChartDataHistogramLong;
import desmoj.core.statistic.StatisticObject;

/**
 * Captures all relevant information about the TextHistogram.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Lorna Slawski based on the class HistogramReporter
 * @author Soenke Claassen based on ideas from Tim Lechler
 * @author based on DESMO-C from Thomas Schniewind, 1998
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
 */
public class TextHistogramReporter extends desmoj.core.report.Reporter {

    // ****** attributes ******

    /**
     * The column headings of the histogram part of this TextHistogramReporter.
     * Entries should contain in the elements in the same order as the
     * <codehistEntries[]</code.
     */
    private String[] _textHistColumns;

    /**
     * The data entries of the histogram part of this TextHistogramReporter. The
     * first (leftmost) dimension of this array represents the number of objects
     * which have been counted. The second dimension of this array represents each
     * column entry of the specified object. So the second dimension entries should
     * contain the data elements in the same order as defined in the
     * <codetextHistColumns[]</codearray.
     */
    private String[][] _textHistEntries;

    /**
     * The number of columns of the histogram part (table) of this.
     * TextHistogramReporter.
     */
    private int _textHistNumColumns;

    /**
     * The counted objects.
     */
    private String[] _textHistObjects;

    // ****** methods ******
    /**
     * Constructor for a new TextHistogramReporter. Note that although any
     * Reportable is accepted you should make sure that only subtypes of
     * TextHistogram are passed to this constructor. Otherwise the number of column
     * titles and their individual headings will differ from the actual content
     * collected by this reporter.
     *
     * @param informationSource desmoj.core.simulator.Reportable : The TextHistogram
     *                          to report about.
     */

    public TextHistogramReporter(desmoj.core.simulator.Reportable informationSource) {
        super(informationSource); // make a Reporter (source =
        // informationSource)

        groupID = 1561; // see Reporter for more information about groupID

        numColumns = 5;
        columns = new String[numColumns];
        columns[0] = "Title";
        columns[1] = "(Re)set";
        columns[2] = "Obs";
        columns[3] = "Least";
        columns[4] = "Most";
        groupHeading = "TextHistograms";

        entries = new String[numColumns];

        // *** histogram part ***

        _textHistNumColumns = 7;
        _textHistObjects = ((desmoj.core.statistic.TextHistogram) source).getStringsObserved();

        _textHistColumns = new String[_textHistNumColumns];
        _textHistColumns[0] = "Cell";
        _textHistColumns[1] = "Object";
        _textHistColumns[2] = "n";
        _textHistColumns[3] = "%";
        _textHistColumns[4] = "Cum. %";
        _textHistColumns[5] = "|";
        _textHistColumns[6] = "Graph";

        _textHistEntries = new String[_textHistObjects.length + 3][_textHistNumColumns];
    }

    /**
     * Returns the data for the TextHistogram chart.
     *
     * @return
     */
    public ChartDataHistogramLong getChartData() {
        // the Histogram we report about (source = informationSource)
        desmoj.core.statistic.TextHistogram textHist = (desmoj.core.statistic.TextHistogram) source;

        String[] strings = textHist.getStringsObserved();
        Long[] table = new Long[strings.length];

        for (int i = 0; i < table.length; i++) {
            table[i] = textHist.getObservationsOfString(strings[i]);
        }
        return new ChartDataHistogramLong(textHist.getModel(), strings, table);
    }

    /**
     * Returns an array of Strings each containing the data for the corresponding
     * column in array <codecolumns[]</code. Implement this method in a way, that an
     * array of the same length as the columntitles is produced containing the data
     * at the point of time this method is called by someone else to produce
     * up-to-date information.
     *
     * @return java.lang.String[] : Array containing the data for reporting.
     */
    @Override
    public String[] getEntries() {
        if (source instanceof desmoj.core.statistic.TextHistogram) {
            // the TextHistogram we report about (source = informationSource)
            desmoj.core.statistic.TextHistogram textHist = (desmoj.core.statistic.TextHistogram) source;

            // Title
            entries[0] = textHist.getName();
            // (Re)set
            entries[1] = textHist.resetAt().toString();
            // Obs
            entries[2] = Long.toString(textHist.getObservations());

            // Least
            // no observations made, so there is no least observed object
            if (textHist.getObservations() == 0) {
                entries[3] = "Insufficient data";
            } else // return least
            {
                entries[3] = textHist.getLeastFrequentedString();
            }

            // Most
            // no observations made, so there is no most observed object
            if (textHist.getObservations() == 0) {
                entries[4] = "Insufficient data";
            } else // return most
            {
                entries[4] = textHist.getMostFrequentedString();
            }
        } else {
            for (int i = 0; i < numColumns; i++) {
                entries[i] = "Invalid source!";
            } // end for
        } // end else

        return entries;
    }

    /**
     * Returns the number of Strings the given TextHistogram has saved.
     *
     * @return int : The number of cells the interval of the given Histogram is
     *         divided into.
     */
    public int getNoOfStrings() {
        return _textHistObjects.length;
    }

    /**
     * Returns the number of observations made by the TextHistogram object. This
     * method call is passed on to the TextHistogram object.
     *
     * @return long : The number of observations made by the TextHistogram object.
     */
    public long getObservations() {

        return source.getObservations(); // that's all
    }

    /**
     * Returns an array of Strings each containing the title for the corresponding
     * column of the histogram part (table).
     *
     * @return java.lang.String[] : Array containing column titles of the histogram
     *         part (table).
     */
    public String[] getTextHistColumnTitles() {
        return _textHistColumns.clone();
    }

    /**
     * Returns a two-dimensional array of Strings containing the data for the
     * histogram part of this TextHistogramReporter. Implement this method in a way,
     * that the data is collected at the point of time this method is called by
     * someone else to produce up-to-date information.
     *
     * @return java.lang.String[][] : Array containing the data for reporting about
     *         the histogram part of this TextHistogramReporter.
     */
    public String[][] getTextHistEntries() {
        // the Histogram we report about (source = informationSource)
        desmoj.core.statistic.TextHistogram textHist = (desmoj.core.statistic.TextHistogram) source;

        // get hold of the accumulated percentage
        double cumPerc = 0.0;

        // flag if all remaining cells are empty
        boolean tailIsEmpty = false;

        for (int j = 0; j < _textHistObjects.length; j++) // loop through all cells
        {
            if (source instanceof desmoj.core.statistic.TextHistogram) {
                // Cell
                _textHistEntries[j][0] = Integer.toString(j + 1);

                // Objectname
                _textHistEntries[j][1] = _textHistObjects[j];

                // n
                _textHistEntries[j][2] = Long.toString(textHist.getObservationsOfString(_textHistObjects[j]));

                // % rounded percentage
                // calculate the percentage with 4 digits after the decimal
                // point
                double perc = StatisticObject.round(100.0 * textHist.getObservationsOfString(_textHistObjects[j])
                / textHist.getObservations());

                cumPerc += perc; // update the accumulated percentage
                // to display the perc. round it to 2 digits after the decimal
                // point
                perc = StatisticObject.round(perc);

                _textHistEntries[j][3] = Double.toString(perc);

                // Cum. %
                // round the accumulated percentage
                double rdCumPerc = StatisticObject.round(cumPerc);

                _textHistEntries[j][4] = Double.toString(rdCumPerc);

                _textHistEntries[j][5] = "|";

                // Graph number of asterix's
                // if all remaining cells are empty
                if (tailIsEmpty) {
                    _textHistEntries[j][6] = "the remaining cells<brare all empty";
                    // = j; // set the no. of cells to the actual value
                    break; // the for-loop for all cells
                }

                // calculate the number of asterix's (one asterix per 2 percent)
                int ast = (int) (perc / 2.0);

                String lineOfAsterix = ""; // make an empty String

                // if percentage between zero and 2 percent
                if (textHist.getObservationsOfString(_textHistObjects[j]) > 0) {
                    lineOfAsterix = "*"; // start with one asterix
                }

                for (int k = 0; k < ast; k++) // fill the String with
                // asterix's
                {
                    lineOfAsterix = lineOfAsterix + "*";
                }

                _textHistEntries[j][6] = lineOfAsterix;
            } else {
                for (int i = 0; i < _textHistNumColumns; i++) {
                    _textHistEntries[j][i] = "Invalid source!";
                } // end for
            } // end else
        } // end for
        return _textHistEntries;
    }

    /**
     * Returns the number of columns of the histogram part (table) of this
     * TextHistogramReporter.
     *
     * @return int : The number of columns of the histogram part (table) of this
     *         TextHistogramReporter.
     */
    public int getTextHistNumColumns() {
        return _textHistNumColumns;
    }

    /* @TODO: Comment */
    @Override
    public boolean isContinuingReporter() {
        return true;
    }
}
