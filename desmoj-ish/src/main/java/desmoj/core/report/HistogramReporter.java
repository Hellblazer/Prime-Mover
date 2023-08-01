package desmoj.core.report;

import java.util.ArrayList;

import desmoj.core.report.html5chart.ChartDataHistogramLong;
import desmoj.core.simulator.TimeSpan;
import desmoj.core.statistic.StatisticObject;

/**
 * Captures all relevant information about the Histogram.
 * Extended to show unit and description of reported object.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen based on ideas from Tim Lechler
 * @author based on DESMO-C from Thomas Schniewind, 1998
 * @author edited by Gunnar Kiesel (setting noOfCells at breakpoint)
 * @author modified by Chr. M&uuml;ller (TH Wildau) 28.11.2012
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License. 
 */

public class HistogramReporter extends desmoj.core.report.Reporter {

	// ****** attributes ******

	/**
	 * The column headings of the histogram part of this HistogramReporter.
	 * Entries should contain in the elements in the same order as the
	 * <code>histEntries[]</code>.
	 */
	private String[] _histColumns;

	/**
	 * The data entries of the histogram part of this HistogramReporter. The
	 * first (leftmost) dimension of this array is representing the number of
	 * cells the interval of the histogram is devided into (incl. under- and
	 * overflow). The second dimension of this array is representing each column
	 * entry of the specified cell. So the second dimension entries should
	 * contain the data elements in the same order as defined in the
	 * <code>histColumns[]</code> array.
	 */
	private String[][] _histEntries;

	/**
	 * The number of columns of the histogram part (table) of this
	 * HistogramReporter.
	 */
	private int _histNumColumns;

	/**
	 * The number of cells the interval of the given Histogram is devided into.
	 */
	private int _noOfCells;

	// ****** methods ******

	/**
	 * Constructor for a new HistogramReporter. Note that although any
	 * Reportable is accepted you should make sure that only subtypes of
	 * Histogram are passed to this constructor. Otherwise the number of column
	 * titles and their individual headings will differ from the actual content
	 * collected by this reporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The Histogram to report about.
	 */
	public HistogramReporter(desmoj.core.simulator.Reportable informationSource) {
		super(informationSource); // make a Reporter (source =
		// informationSource)

		// the Histogram we report about (source = informationSource)
		desmoj.core.statistic.Histogram hist 	= (desmoj.core.statistic.Histogram) source;

		groupID = 1411; // see Reporter for more information about groupID

		numColumns = 8;
		columns = new String[numColumns];
		columns[0] = "Title";
		columns[1] = "(Re)set";
		columns[2] = "Obs";
		columns[3] = "Mean";
		columns[4] = "Std.Dev";
		columns[5] = "Min";
		columns[6] = "Max";
		columns[7] = "Unit";
		groupHeading = "Histograms";

		entries = new String[numColumns];

		// *** histogram part ***

		_histNumColumns = 7;
		_noOfCells = hist.getCells();

		_histColumns = new String[_histNumColumns];
		_histColumns[0] = "Cell";
		_histColumns[1] = "Cell Range";
		_histColumns[2] = "n";
		_histColumns[3] = "%";
		_histColumns[4] = "Cum. %";
		_histColumns[5] = "|";
		_histColumns[6] = "Graph";

		_histEntries = new String[_noOfCells + 3][_histNumColumns];
	}

	/**
	 * Returns the data for the histogram chart.
	 * @return
	 */
	public ChartDataHistogramLong getChartData() {		
		// the Histogram we report about (source = informationSource)
		desmoj.core.statistic.Histogram hist = (desmoj.core.statistic.Histogram) source;
		
		boolean remainingEmpty = false;
		int i = 0;
		
		ArrayList<Double> lowerLimits = new ArrayList<Double>();
		ArrayList<Long> table = new ArrayList<Long>();
		
		while (!remainingEmpty && i < hist.getCells()+2) {
			lowerLimits.add(hist.getLowerLimit(i));
			table.add(hist.getObservationsInCell(i));			

			i++;
			
			int j = i;
			//check if remaining cells are empty
			boolean zeros = true;
			while (zeros && j < hist.getCells()+2) {
				if (hist.getObservationsInCell(j) != 0) zeros = false;
				j++;
			}
			if (zeros) remainingEmpty = true;			
		}
		
		if (table.size() < hist.getCells()+2) {
			//add one entry to represent the rest
			lowerLimits.add(hist.getLowerLimit(table.size()));
			table.add(0L);
		}
				
        String[] limits = new String[table.size()];
        Long[] entries = new Long[table.size()];
        
        //copy the values to array
        for (int k = 0; k < limits.length; k++) {
            if (k==0) {
                limits[k] = limits.length > 1 ? "< " + lowerLimits.get(k+1) : ""; 
            } else {
                limits[k] = limits.length > 1 ? "\\u2265 " + lowerLimits.get(k) : "";
            } 
            entries[k] = table.get(k);
        }
		
		return new ChartDataHistogramLong(hist.getModel(), limits, entries);		
	}

	/**
	 * Returns an array of Strings each containing the data for the
	 * corresponding column in array <code>columns[]</code>. Implement this
	 * method in a way, that an array of the same length as the columntitles is
	 * produced containing the data at the point of time this method is called
	 * by someone else to produce up-to-date information.
	 * 
	 * @return java.lang.String[] : Array containing the data for reporting
	 */
	public String[] getEntries() {
		if (source instanceof desmoj.core.statistic.Histogram) {
			// the Histogram we report about (source = informationSource)
			desmoj.core.statistic.Histogram hist 	= (desmoj.core.statistic.Histogram) source;
			boolean _showTimeSpansInReport     = hist.getShowTimeSpansInReport();
			desmoj.core.statistic.Tally tl 			= (desmoj.core.statistic.Tally) source;
			desmoj.core.statistic.TallyRunning tlr = null;
			if (tl instanceof desmoj.core.statistic.TallyRunning) {
				tlr = (desmoj.core.statistic.TallyRunning) tl;
			}

			// Title
			entries[0] = hist.getName();
			// (Re)set
			entries[1] = hist.resetAt().toString();
			// Obs
			entries[2] = Long.toString(hist.getObservations());

			// Mean
			// no observations made, so Mean can not be calculated
			if (tl.getObservations() == 0) {
				entries[3] = "Insufficient data";
			} else // return mean value
			{
				entries[3] = this.format(_showTimeSpansInReport, tl.getMean());
				if (tlr != null)
					entries[3] += " (last "
							+ tlr.getSampleSizeN()
							+ " obs: "
							+ this.format(_showTimeSpansInReport, tlr.getMeanLastN())
							+ ")";
			}

			// Std.Dev
			// not enough observations are made, so Std.Dev can not be
			// calculated
			if (tl.getObservations() < 2) {
				entries[4] = "Insufficient data";
			} else // return standard deviation
			{
				entries[4] = this.format(_showTimeSpansInReport, tl.getStdDev());
				if (tlr != null)
					entries[4] += " (last "
							+ tlr.getSampleSizeN()
							+ " obs: "
							+ this.format(_showTimeSpansInReport, tlr.getStdDevLastN())
							+ ")";
			}

			// Min
			if (tl.getObservations() == 0) {
				entries[5] = "Insufficient data";
			} else {
				entries[5] = this.format(_showTimeSpansInReport, tl.getMinimum());
				if (tlr != null)
					entries[5] += " (last "
							+ tlr.getSampleSizeN()
							+ " obs: "
							+ this.format(_showTimeSpansInReport, tlr.getMinimumLastN())
							+ ")";
			}

			// Max
			if (tl.getObservations() == 0) {
				entries[6] = "Insufficient data";
			} else {
				entries[6] = this.format(_showTimeSpansInReport, tl.getMaximum());
				if (tlr != null)
					entries[6] += " (last "
							+ tlr.getSampleSizeN()
							+ " obs: "
							+ this.format(_showTimeSpansInReport, tlr.getMaximumLastN())
							+ ")";
			}

			//cm 21.11.12  Extension for viewing unit
            entries[7] = tl.getUnitText();

		} else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			} // end for
		} // end else

		return entries;
	}

	/**
	 * Returns an array of Strings each containing the title for the
	 * corresponding column of the histogram part (table).
	 * 
	 * @return java.lang.String[] : Array containing column titles of the
	 *         histogram part (table).
	 */
	public String[] getHistColumnTitles() {
		return _histColumns.clone();
	}

	/**
	 * Returns a two-dimensional array of Strings containing the data for the
	 * histogram part of this HistogramReporter. Implement this method in a way,
	 * that the data is collected at the point of time this method is called by
	 * someone else to produce up-to-date information.
	 * 
	 * @return java.lang.String[][] : Array containing the data for reporting
	 *         about the histogram part of this HistogramReporter.
	 */
	public String[][] getHistEntries() {
		// the Histogram we report about (source = informationSource)
		desmoj.core.statistic.Histogram hist = (desmoj.core.statistic.Histogram) source;
		boolean _showTimeSpansInReport     = hist.getShowTimeSpansInReport();

		// get hold of the accumulated percentage
		double cumPerc = 0.0;

		// flag if all remaining cells are empty
		boolean tailIsEmpty = false;

		for (int j = 0; j < _noOfCells + 2; j++) // loop through all cells
		{
			if (source instanceof desmoj.core.statistic.Histogram) {
				// Cell
				_histEntries[j][0] = Integer.toString(j);

				// Lower Limit
				String a = this.format(_showTimeSpansInReport, hist.getLowerLimit(j));
				String b = this.format(_showTimeSpansInReport, Double.POSITIVE_INFINITY);
				if(j+1 < _noOfCells+2) b = this.format(_showTimeSpansInReport, hist.getLowerLimit(j+1));
				if(j == 0) 	_histEntries[j][1] = "("+a+", "+b+")";
				else 		_histEntries[j][1] = "["+a+", "+b+")";
				//_histEntries[j][1] = Double.toString(hist.getLowerLimit(j));

				// n
				_histEntries[j][2] = Long
						.toString(hist.getObservationsInCell(j));

				// % rounded percentage

				// calculate the percentage with 4 digits after the decimal
				// point
				double perc = StatisticObject.round(100.0 * hist
						.getObservationsInCell(j) / hist
						.getObservations());

				cumPerc += perc; // update the accumulated percentage
				// to display the perc. round it to 2 digits after the decimal
				// point
				perc = StatisticObject.round(perc);

				_histEntries[j][3] = Double.toString(perc);

				// Cum. %

				// round the accumulated percentage
				double rdCumPerc = StatisticObject.round(cumPerc);

				_histEntries[j][4] = Double.toString(rdCumPerc);

				// check if the accumulated percentage has reached 100%
				// AND it is not the last cell
				if (rdCumPerc > 99.98 && j < (_noOfCells + 1)) {
					// flag if cells so far are empty
					boolean yetEmpty = true;
					// check if all the remaining cells are empty
					for (int k = j; k < _noOfCells + 2; k++) // loop thru
					// remaining cells
					{
						yetEmpty = (yetEmpty && (hist.getObservationsInCell(k) == 0));
					}

					tailIsEmpty = yetEmpty;
				}

				// |
				_histEntries[j][5] = "|";

				// Graph number of asterix's

				// if all remaining cells are empty
				if (tailIsEmpty) {
					_histEntries[j][6] = "the remaining cells are all empty";
					_noOfCells = j; // set the no. of cells to the actual value
					break; // the for-loop for all cells
				}

				// calculate the number of asterix's (one asterix per 2 percent)
				int ast = (int) (perc / 2.0);

				String lineOfAsterix = ""; // make an empty String

				// if percentage between zero and 2 percent
				if (hist.getObservationsInCell(j) > 0) {
					lineOfAsterix = "*"; // start with one asterix
				}

				for (int k = 0; k < ast; k++) // fill the String with
				// asterix's
				{
					lineOfAsterix = lineOfAsterix + "*";
				}

				_histEntries[j][6] = lineOfAsterix;
			} else {
				for (int i = 0; i < _histNumColumns; i++) {
					_histEntries[j][i] = "Invalid source!";
				} // end for
			} // end else
		} // end for

		return _histEntries;
	}

	/**
	 * Returns the number of columns of the histogram part (table) of this
	 * HistogramReporter.
	 * 
	 * @return int : The number of columns of the histogram part (table) of this
	 *         HistogramReporter
	 */
	public int getHistNumColumns() {
		return _histNumColumns;
	}

	/**
	 * Returns the number of cells the interval of the given Histogram is
	 * devided into.
	 * 
	 * @return int : The number of cells the interval of the given Histogram is
	 *         devided into.
	 */
	public int getNoOfCells() {
		return _noOfCells;
	}
	
	/**
	 * Returns the number of observations made by the Histogram object. This
	 * method call is passed on to the Histogram object.
	 * 
	 * @return long : The number of observations made by the Histogram object.
	 */
	public long getObservations() {

		return source.getObservations(); // that's all
	}
	
	/*@TODO: Comment */
	public boolean isContinuingReporter() {
		return true;
	}
		
	private String format(boolean showTimeSpans, double value){
		String out = Double.toString(value);
		if(showTimeSpans && value < 0.0) 					out += " (Invalid)";
		else if(showTimeSpans && value >= Long.MAX_VALUE) 	out += " (Invalid)";
		else if(showTimeSpans) 								out = new TimeSpan(value).toString();
		return out;
	}
} // end class HistogramReporter
