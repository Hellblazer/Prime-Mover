package desmoj.core.report;

import java.util.TreeMap;

import desmoj.core.advancedModellingFeatures.report.StockReporter;
import desmoj.core.report.html5chart.CanvasHistogramDouble;
import desmoj.core.report.html5chart.CanvasHistogramLong;
import desmoj.core.report.html5chart.CanvasTimeSeries;
import desmoj.core.report.html5chart.ChartDataHistogramDouble;
import desmoj.core.report.html5chart.ChartDataHistogramLong;
import desmoj.core.report.html5chart.ChartDataTimeSeries;

/**
 * ReportMultRowsFileOut is used to create a file to let the reporters write
 * their reports to. It receives a reporter and divides its individual text
 * information up to be displayed in HTML format on disc in the user's local
 * directory. The messages are displayed in a tabular design with columns for
 * each item displayed by the reporter. For each Reporter giving a new group-ID,
 * a new table is opened. This leads to a number of distinct tables displayed in
 * one HTML-page with all reporters grouped by their group-id in one table. This
 * special ReportFileOut is designed to write reports with more than one row for
 * one reporter. This is needed for the <code>WaitQueueReporter</code> where
 * there will be one row for the data of the waiting slaves and one row for the
 * data of the waiting masters and for the <code>HistogramReporter</code> where
 * there will be multiple rows, one for each segment (cell) of ther interval.
 * Note that if the framework is used as an Applet in webpages there is no
 * access to the disc and an alternative output on screen or into a graphics
 * window must be registered at the Experiment's MessageManager for this type of
 * messages. Errors affecting the java runtime are always displayed on the
 * system's standard output PrintStream.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author of ReportFileOut class : Tim Lechler, modified by Soenke Claassen and
 *         Nicolas Knaak, modified by Chr. M&uuml;ller (TH Wildau) 28.11.2012
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
public class ReportMultRowsFileOut extends ReportFileOut {

    /**
     * Flag to shift nested reporter descriptions one column if set to
     * <code>true</code>.
     */
    private static boolean offsetDescriptionsOneColumn = false;

    /**
     * Set report output such that nested Reporter descriptions are offset by one
     * column, thus improving readability at the expense of space.
     */
    public static void offsetDescriptionsOneColumn() {
        offsetDescriptionsOneColumn = true;
    }

    private TreeMap<String, CanvasTimeSeries> _timeSeriesCanvas;

    /**
     * Creates a file to print reports into a HTML page. By opening the file, the
     * necessary HTML tags to define a webpage are already inserted into the file.
     * The parameter given should reflect the experiment that produces this file.
     *
     * @param simTimeFloatingDigits int : The number of floating point digits of the
     *                              simulation time values to be displayed
     */
    public ReportMultRowsFileOut(int simTimeFloatingDigits, String formatter) {
        super(simTimeFloatingDigits, formatter); // make a ReportFileOut

        _timeSeriesCanvas = new TreeMap<>();
    }

    /**
     * Receives a reporter and writes its contents formatted to a HTML table into a
     * file in the user's default directory. In case of a ModelReporter it writes
     * the model's information into the file. After writing the model data, all
     * reportables registered with the ModelReporter are asked for a reporter and
     * those are also written to the report file. After processing the Reportables,
     * the registered subModels are asked to produce their reporters which in turn
     * are recursively sent to this method until no other submodels are available.
     *
     * @param r Reporter : The Reporter to be processed
     */
    @Override
    public void receive(desmoj.core.report.Reporter r) {
        // check parameters
        if (r == null)
            return; // invalid parameter

        // copy values in buffer variables for faster access
        String[] titleBuf = r.getColumnTitles();
        String[] entryBuf = r.getEntries();

        // in case lastReporter was a TimeSeriesReporter
        // and r is not a TimeSeriesReporter
        // and the formatter is a HTMLTableChartFormatter
        // then draw the TimeSeries in a canvas before the new reporter is displayed
        // ---added by Johanna Djimandjaja
        if (lastReporter instanceof desmoj.core.report.TimeSeriesReporter &&
            !(r instanceof desmoj.core.report.TimeSeriesReporter)) {

            HTMLTableChartFormatter chartFormatter = null;

            if (formatter instanceof HTMLTableChartFormatter) {
                // "Type-Cast"
                chartFormatter = (HTMLTableChartFormatter) formatter;
            }

            if (chartFormatter != null && _timeSeriesCanvas != null) {
                formatter.closeTableNoTopTag();
                for (CanvasTimeSeries c : _timeSeriesCanvas.values()) {
                    chartFormatter.drawChart(c);
                }
            }
        }

        // first Reporter to be printed here always writes heading and starts
        // table
        // (most likely a ModelReporter)
        if (lastReporter == null) {
            // open a table with the heading needed for this type of reporter
            formatter.openTable(r.getHeading());
            formatter.openRow();

            for (int i = 0; i < r.numColumns(); i++) {
                formatter.writeHeadingCell(titleBuf[i]);
            }

            formatter.closeRow();
            lastReporter = r;
            // trick the next if-clause to be avoided first time
        } else {
            // if two HistogramReporter or two StockReporter or two
            // WorkStationReporter
            // or two TransportReporter will be written one after the other
            // @modified by Nick Knaak 6.2.06: Condition 1: isContinuingReporter
            /*
             * if (r.isContinuingReporter() && lastReporter.isContinuingReporter() &&
             * (r.getClass().isAssignableFrom(lastReporter.getClass()) ||
             * lastReporter.getClass().isAssignableFrom(r.getClass()))) {
             */
            // modified if condition by Chr. M&uuml;ller (TH Wildau) 28.11.2012
            if (r.isContinuingReporter() && lastReporter.isContinuingReporter() &&
                Reporter.isSameGroup(r, lastReporter)) {
                formatter.closeTable();
                // open new table for new XYReporter with no special header
                formatter.openTable(" ");
                formatter.openRow();

                for (int i = 0; i < r.numColumns(); i++) {
                    formatter.writeHeadingCell(titleBuf[i]);
                }

                formatter.closeRow();
            } // end if
        } // end else

        // if new Reporter group type is received, close last table and open new
        // one
        // --- Modification by N. Knaak (27.11.01): Every table reporter has
        // it's own
        // headings.
        if (Reporter.isOtherGroup(r, lastReporter) && !(r instanceof TableReporter)) {

            formatter.closeTable();
            formatter.writeHorizontalRuler();
            formatter.openTable(r.getHeading());
            formatter.openRow();

            for (int i = 0; i < r.numColumns(); i++) {
                formatter.writeHeadingCell(titleBuf[i]);
            }

            // if formatter is an instance of HTMLTableChartFormatter
            // add a column for color
            // ---added by Johanna Djimandjaja
            if (formatter instanceof HTMLTableChartFormatter &&
                r.makeAdditionalColorEntryIfHTMLColorChartIsGenerated()) {
                formatter.writeHeadingCell("Color");
            }

            formatter.closeRow();
        }

        // the WaitQueueReporter, the TransportReporter and the
        // WorkStationReporter
        // produce two rows
        // @modified: Condition 2 - isTwoRowReporter
        // by Nick Knaak 6.2.06
        if (r.isTwoRowReporter()) {
            // open the row for the master queue data
            if (formatter instanceof AbstractTableFormatter) {
                ((AbstractTableFormatter) formatter).openRow(r);
            } else {
                formatter.openRow();
            }

            // write all data of the master queue in this row
            for (int i = 0; i < r.numColumns(); i++) {
                formatter.writeCell(entryBuf[i], 1);
            }

            formatter.closeRow(); // close the row for the master queue data

            // open the row for the slave queue data
            if (formatter instanceof AbstractTableFormatter) {
                ((AbstractTableFormatter) formatter).openRow(r);
            } else {
                formatter.openRow();
            }

            // write all data of the slave queue in this row
            for (int i = r.numColumns(); i < (r.numColumns() * 2); i++) {
                formatter.writeCell(entryBuf[i], 1);
            }

            formatter.closeRow(); // close the row for the slave queue data

            // if a WorkStation has more than one slave queue
            // @modified by Nick Knaak, 6.2.06
            if (r.getNumOfSlaveQueues() > 0) {
                int numberSlaveQueues = r.getNumOfSlaveQueues();

                // write all the slave queue rows
                for (int j = 1; j < numberSlaveQueues; j++) {
                    if (formatter instanceof AbstractTableFormatter) {
                        ((AbstractTableFormatter) formatter).openRow(r);
                    } else {
                        formatter.openRow();
                    }
                    // open the row for the slave queue data

                    // write all data of the slave queue in this row
                    for (int i = (r.numColumns() * (j + 1)); i < (r.numColumns() * (j + 2)); i++) {
                        formatter.writeCell(entryBuf[i], 1);
                    }

                    formatter.closeRow();
                    // close the row for the slave queue data
                }

            } else {

                // other overlong Reporters containing more than two rows
                int rows = entryBuf.length / r.numColumns;
                for (int row = 2; row < rows; row++) {

                    // skip if all cells are empty
                    boolean empty = true;
                    for (int i = row * r.numColumns(); i < r.numColumns() * (row + 1); i++) {
                        if (entryBuf[i] != null && entryBuf[i].length() > 0) {
                            empty = false;
                            break;
                        }
                    }
                    if (empty)
                        continue; // row empty

                    // at least one cell non-empty --> continue with output: open new row
                    if (formatter instanceof AbstractTableFormatter) {
                        ((AbstractTableFormatter) formatter).openRow(r);
                    } else {
                        formatter.openRow();
                    }

                    // write row data
                    for (int i = row * r.numColumns(); i < r.numColumns() * (row + 1); i++) {
                        formatter.writeCell(entryBuf[i], 1);
                    }

                    formatter.closeRow(); // close the row
                }

            }
        } // end of two row reporter
          // --- Modification by N. Knaak (27.11.01): TableReporters are
          // completely handled below.
        else if (!(r instanceof TableReporter))
        // normal handling of all other Reporters
        {
            // always write the reporter's content into a table row
            if (formatter instanceof AbstractTableFormatter) {
                ((AbstractTableFormatter) formatter).openRow(r);
            } else {
                formatter.openRow();
            }

            for (int i = 0; i < r.numColumns(); i++) {
                formatter.writeCell(entryBuf[i], 1);
            }

            // in case r is a TimeSeriesReporter
            // and the formatter instance of HTMLTableChartFormatter
            // add the chart data of the TimeSeries to the canvas
            // and add the color representing this TimeSeries in the chart
            // ---added by Johanna Djimandjaja
            if (r instanceof TimeSeriesReporter && formatter instanceof HTMLTableChartFormatter) {

                TimeSeriesReporter tsReporter = (TimeSeriesReporter) r;
                HTMLTableChartFormatter chartFormatter = (HTMLTableChartFormatter) formatter;

                java.awt.Color tsColor;
                ChartDataTimeSeries data = tsReporter.getChartData();
                if (_timeSeriesCanvas.get(data.getGroup()) == null) {
                    String yAxis = "value" + (data.getGroup().equals("default") ? "" : " (" + data.getGroup() + ")");
                    CanvasTimeSeries c = new CanvasTimeSeries("timeSeriesCanvas" + chartFormatter.getFreeCanvasIDNum(),
                                                              350, 500, tsReporter.getChartData(), "time", yAxis);
                    _timeSeriesCanvas.put(data.getGroup(), c);
                    tsColor = c.getDataColor(0);
                } else {
                    tsColor = _timeSeriesCanvas.get(data.getGroup()).addTimeSeries(tsReporter.getChartData());
                }
                chartFormatter.writeColoredCell(tsColor);
            }

            // the row's finished now
            formatter.closeRow();
        } // end of single row and no table reporter

        // ---------- description row, if set -------------------------------

        if (r.getDescription() != null && r.getDescription().length() > 0) {

            if (formatter instanceof AbstractTableFormatter) {
                ((AbstractTableFormatter) formatter).openRow(r);
            } else {
                formatter.openRow();
            }
            if (offsetDescriptionsOneColumn) {
                formatter.writeCell("", 1);
                formatter.writeCell(r.getDescription(), r.numColumns() - 1);
            } else {
                formatter.writeCell(r.getDescription(), r.numColumns());
            }
            formatter.closeRow(); // close the row
        }

        // ---------- inner reporter ----------------------------------------

        // the HistogramReporter produces a special histogram table
        if (r instanceof desmoj.core.report.HistogramReporter) {
            // "Type-Cast"
            desmoj.core.report.HistogramReporter hr = (desmoj.core.report.HistogramReporter) r;
            this.innerReport(hr);
        }

        // extended by Chr. M&uuml;ller (TH Wildau) 28.11.12
        // the HistogramAccumulateReporter produces a special histogram table
        if (r instanceof desmoj.core.report.HistogramAccumulateReporter) {
            // "Type-Cast"
            desmoj.core.report.HistogramAccumulateReporter har = (desmoj.core.report.HistogramAccumulateReporter) r;
            this.innerReport(har);
        }

        // the TextHistogramReporter produces a special histogram table
        if (r instanceof desmoj.core.report.TextHistogramReporter) {
            // "Type-Cast"
            desmoj.core.report.TextHistogramReporter thr = (desmoj.core.report.TextHistogramReporter) r;
            this.innerReport(thr);
        }

        // the StockReporter produces a special stock report about the two
        // queues for the producers and consumers
        // @TODO: Cond 3: isStockReporter
        if (r instanceof desmoj.core.advancedModellingFeatures.report.StockReporter) {
            // "Type-Cast"
            desmoj.core.advancedModellingFeatures.report.StockReporter sr = (desmoj.core.advancedModellingFeatures.report.StockReporter) r;
            this.innerReport(sr);
        }

        // --- The table reporter produces a table of arbitrary length.
        // (Modification: Nicolas Knaak 27.11.2001)
        if (r instanceof TableReporter)
            writeTableReporter((TableReporter) r, titleBuf);

        // remember the last reporter
        lastReporter = r;
    }

    private void innerReport(HistogramAccumulateReporter hr) {
        // copy values in buffer variables for faster access
        String[] histTitleBuf = hr.getHistColumnTitles();
        String[][] histEntryBuf = hr.getHistEntries();

        formatter.closeTableNoTopTag();
        // close the normal (Tally-like) Histogram table
        formatter.openTable(" ");
        // open table for histogram part with no special header
        formatter.openRow(); // open header row

        // in case there is not enough data collected
        if (hr.getObservations() < 3) {
            formatter.writeCell("Insufficient data for displaying histogram statistics", 1);
            formatter.closeRow();
        } else // enough data collected to display histogram part
        {
            HTMLTableChartFormatter chartFormatter = null;
            CanvasHistogramDouble histCanvas = null;

            if (formatter instanceof HTMLTableChartFormatter) {
                chartFormatter = (HTMLTableChartFormatter) formatter;
                ChartDataHistogramDouble histData = hr.getChartData();
                histCanvas = new CanvasHistogramDouble("histogramAccumulateCanvas"
                + chartFormatter.getFreeCanvasIDNum(), 350, 500, histData, hr.source.getName());
            }

            // write headings of the cells
            for (int i = 0; i < hr.getHistNumColumns() - 1; i++) {
                formatter.writeHeadingCell(histTitleBuf[i]);
            }
            // if (formatter instanceof HTMLTableChartFormatter) change the last heading to
            // "Color"
            if (chartFormatter != null)
                formatter.writeHeadingCell("Color");
            else
                formatter.writeHeadingCell(histTitleBuf[hr.getHistNumColumns() - 1]);

            formatter.closeRow();

            // write the HistrogramReporter's content into a table

            // loop through all cells
            for (int j = 0; j < hr.getNoOfCells() + 2; j++) {
                if (formatter instanceof AbstractTableFormatter) {
                    ((AbstractTableFormatter) formatter).openRow(hr);
                } else {
                    formatter.openRow();
                }

                for (int i = 0; i < hr.getHistNumColumns() - 1; i++) {
                    formatter.writeCell(histEntryBuf[j][i], 1);
                }
                // if (formatter instanceof HTMLTableChartFormatter) change the last content to
                // a colored cell
                if (chartFormatter != null)
                    chartFormatter.writeColoredCell(histCanvas.getDataColor(j));
                else
                    formatter.writeCell(histEntryBuf[j][hr.getHistNumColumns() - 1], 1);

                // the row is finished now
                formatter.closeRow();
            }

            if (chartFormatter != null) {
                formatter.closeTableNoTopTag();
                // draw the histogram chart
                chartFormatter.drawChart(histCanvas);
            }
        }
    }

    private void innerReport(HistogramReporter hr) {
        // copy values in buffer variables for faster access
        String[] histTitleBuf = hr.getHistColumnTitles();
        String[][] histEntryBuf = hr.getHistEntries();
        int numOfRows = hr.getNoOfCells() + 2;

        formatter.closeTableNoTopTag();
        // close the normal (Tally-like) Histogram table
        formatter.openTable(" ");
        // open table for histogram part with no special header
        formatter.openRow(); // open header row

        // in case there is not enough data collected
        if (hr.getObservations() < 3) {
            formatter.writeCell("Insufficient data for displaying histogram statistics", 1);
            formatter.closeRow();
        } else // enough data collected to display histogram part
        {
            HTMLTableChartFormatter chartFormatter = null;
            CanvasHistogramLong histCanvas = null;

            if (formatter instanceof HTMLTableChartFormatter) {
                chartFormatter = (HTMLTableChartFormatter) formatter;
                ChartDataHistogramLong histData = hr.getChartData();
                histCanvas = new CanvasHistogramLong("histogramCanvas" + chartFormatter.getFreeCanvasIDNum(), 350, 500,
                                                     histData, hr.source.getName());
            }

            // write headings of the cells
            for (int i = 0; i < hr.getHistNumColumns() - 1; i++) {
                formatter.writeHeadingCell(histTitleBuf[i]);
            }
            // if (formatter instanceof HTMLTableChartFormatter) change the last heading to
            // "Color"
            if (chartFormatter != null)
                formatter.writeHeadingCell("Color");
            else
                formatter.writeHeadingCell(histTitleBuf[hr.getHistNumColumns() - 1]);

            formatter.closeRow();

            // write the HistrogramReporter's content into a table

            // loop through all cells
            for (int j = 0; j < numOfRows; j++) {
                if (formatter instanceof AbstractTableFormatter) {
                    ((AbstractTableFormatter) formatter).openRow(hr);
                } else {
                    formatter.openRow();
                }

                for (int i = 0; i < hr.getHistNumColumns() - 1; i++) {
                    formatter.writeCell(histEntryBuf[j][i], 1);
                }
                // if (formatter instanceof HTMLTableChartFormatter) change the last content to
                // a colored cell
                if (chartFormatter != null)
                    chartFormatter.writeColoredCell(histCanvas.getDataColor(j));
                else
                    formatter.writeCell(histEntryBuf[j][hr.getHistNumColumns() - 1], 1);

                // the row is finished now
                formatter.closeRow();
            }

            if (chartFormatter != null) {
                formatter.closeTableNoTopTag();
                // draw the histogram chart
                chartFormatter.drawChart(histCanvas);
            }

        }

    }

    private void innerReport(StockReporter sr) {
        // copy values in buffer variables for faster access
        String[] stockTitleBuf = sr.getStockColumnTitles();
        String[] stockEntryBuf = sr.getStockEntries();

        formatter.closeTableNoTopTag();
        // close the normal Stock table (stock part)
        formatter.openTable(" ");
        // open table for queues part with no special header
        formatter.openRow(); // write header row

        // write queues heading
        for (int i = 0; i < sr.getStockNumColumns(); i++) {
            formatter.writeHeadingCell(stockTitleBuf[i]);
        }

        formatter.closeRow();

        // write the producer queue's content into a table
        formatter.openRow(); // open the row for the producer queue data

        // write all data of the producer queue in this row
        for (int i = 0; i < sr.getStockNumColumns(); i++) {
            formatter.writeCell(stockEntryBuf[i], 1);
        }

        formatter.closeRow(); // close the row for the producer queue data

        formatter.openRow(); // open the row for the consumer queue data

        // write all data of the consumer queue in this row
        for (int i = sr.getStockNumColumns(); i < (sr.getStockNumColumns() * 2); i++) {
            formatter.writeCell(stockEntryBuf[i], 1);
        }

        formatter.closeRow(); // close the row for the consumer queue data

    }

    private void innerReport(TextHistogramReporter thr) {
        // copy values in buffer variables for faster access
        String[] textHistTitleBuf = thr.getTextHistColumnTitles();
        String[][] histEntryBuf = thr.getTextHistEntries();
        int numOfRows = thr.getNoOfStrings();

        formatter.closeTableNoTopTag();
        // close the normal TextHistogram table
        formatter.openTable(" ");
        // open table for histogram part with no special header
        formatter.openRow(); // open header row

        // in case there is not enough data collected
        if (thr.getObservations() < 3) {
            formatter.writeCell("Insufficient data for displaying histogram statistics", 1);

            formatter.closeRow();
        } else // enough data collected to display histogram part
        {
            HTMLTableChartFormatter chartFormatter = null;
            CanvasHistogramLong textHistCanvas = null;

            if (formatter instanceof HTMLTableChartFormatter) {
                chartFormatter = (HTMLTableChartFormatter) formatter;
                ChartDataHistogramLong textHistData = thr.getChartData();
                textHistCanvas = new CanvasHistogramLong("textHistogramCanvas" + chartFormatter.getFreeCanvasIDNum(),
                                                         350, 500, textHistData, thr.source.getName());
            }

            // write headings of the cells
            for (int i = 0; i < thr.getTextHistNumColumns() - 1; i++) {
                formatter.writeHeadingCell(textHistTitleBuf[i]);
            }
            // if (formatter instanceof HTMLTableChartFormatter) change the last heading to
            // "Color"
            if (chartFormatter != null)
                formatter.writeHeadingCell("Color");
            else
                formatter.writeHeadingCell(textHistTitleBuf[thr.getTextHistNumColumns() - 1]);

            formatter.closeRow();

            // write the TextHistrogramReporter's content into a table

            // loop through all observed Strings
            for (int j = 0; j < numOfRows; j++) {
                if (formatter instanceof AbstractTableFormatter) {
                    ((AbstractTableFormatter) formatter).openRow(thr);
                } else {
                    formatter.openRow();
                }

                for (int i = 0; i < thr.getTextHistNumColumns() - 1; i++) {
                    formatter.writeCell(histEntryBuf[j][i], 1);
                }
                // if (formatter instanceof HTMLTableChartFormatter) change the last content to
                // a colored cell
                if (chartFormatter != null)
                    chartFormatter.writeColoredCell(textHistCanvas.getDataColor(j));
                else
                    formatter.writeCell(histEntryBuf[j][thr.getTextHistNumColumns() - 1], 1);

                // the row is finished now
                formatter.closeRow();
            }

            if (chartFormatter != null) {
                formatter.closeTableNoTopTag();
                // draw the histogram chart
                chartFormatter.drawChart(textHistCanvas);
            }
        }

    }

    /** Writes a table reporter */
    private void writeTableReporter(TableReporter tr, String[] titleBuf) {
        int cols = tr.numColumns();
        int rows = tr.numRows();
        String[][] entryTable = tr.getEntryTable();
        if (formatter.tableIsOpen())
            formatter.closeTable();

        formatter.writeHorizontalRuler();
        formatter.openTable(tr.getTitle());

        // Write additional info in table header

        String[][] header = tr.getHeader();
        if (header != null) {
            for (String[] element : header) {
                formatter.openRow();
                for (int j = 0; j < element.length; j++) {
                    formatter.writeCell(element[j], 1);
                }
                formatter.closeRow();
            }
            formatter.openRow();
            formatter.writeCell("___", 1);
            formatter.closeRow();
        }

        // Write Headings
        formatter.openRow();
        for (int i = 0; i < cols; i++) {
            formatter.writeHeadingCell(titleBuf[i]);
        }
        formatter.closeRow();

        formatter.openRow();
        formatter.closeRow();

        // Write Data Entries
        for (int i = 0; i < rows; i++) {
            formatter.openRow();
            for (int j = 0; j < cols; j++) {
                formatter.writeCell(entryTable[i][j], 1);
            }
            formatter.closeRow();
        }
        formatter.closeTable();
    }
} // end class ReportMultRowsFileOut
