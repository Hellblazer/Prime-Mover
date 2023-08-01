package desmoj.core.report;

/**
 * A basic implementation of the TableFormatter interface realizing common
 * properties of table formatters.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Nicolas Knaak
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
 *
 */
public abstract class AbstractTableFormatter implements TableFormatter {

	/** The Reporter displayed in current row. */
	protected Reporter _currentReporter = null;

	/** The FileOutput this table writer writes to. */
	protected FileOutput out = null;

	/** Flag indicating if table row is open */
	protected boolean rowOpen = false;

	/** Flag indicating if table is open */
	protected boolean tableOpen = false;

	/** Precision of time values printed to table */
	private int _timeFloats = 0;
	
	/**
	 * Opens a new row. Calls openRow() and saves reference on
	 * reporter which will we displayed in that row.
	 * Reference on reporter is not necessary for getting content of reporter!!!
	 * But maybe its useful for some kind of extended reports.
	 * 
	 * @param rep Reporter which will be displayed in new row
	 */
	public void openRow(Reporter rep) {
		this._currentReporter = rep;
		this.openRow();
	}

	/**
	 * Sets tableOpen flag to true. Should always be called from subclass
	 * implementations of this method.
	 * 
	 * @return value of row open flag
	 * @see desmoj.core.report.TableFormatter#rowIsOpen()
	 */
	public boolean rowIsOpen() {
		return rowOpen;
	}

	/**
	 * Sets an output file to write the table to
	 * 
	 * @param out
	 *            desmoj.report.FileOutput
	 */
	public void setOutput(FileOutput out) {
		this.out = out;
	}

	/**
	 * Sets the time precision
	 * 
	 * @param tf
	 *            time precision
	 */
	public void setTimePrecision(int tf) {
		this._timeFloats = tf;
	}

	/**
	 * @return value of table open flag;
	 * @see desmoj.core.report.TableFormatter#tableIsOpen()
	 */
	public boolean tableIsOpen() {
		return false;
	}	
	
	/**
	 * @return time precision
	 * @see desmoj.core.report.TableFormatter#timePrecision()
	 */
	public int timePrecision() {
		return _timeFloats;
	}
	
	/**
	 * Returns a formatted time string
	 * 
	 * @param t
	 *            time string
	 * @return formatted version of time string
	 * @see desmoj.core.report.TableFormatter#writeTime(java.lang.String)
	 */
	public String writeTime(String t) {
	    /*
		if (t == null)
			return "None";

		if (t.lastIndexOf(".") == -1)
			return t; // no decimal point -> just print it

		int decPoint = t.lastIndexOf(".");

		if (t.length() - decPoint <= timeFloats + 1)
			return t; // less floats than specified

		if (t.lastIndexOf("E") == -1)
		    return t.substring(0, decPoint + timeFloats + 1);  // no scientific notation
		else
		    return t.substring(0, decPoint + timeFloats + 1) + t.substring(t.lastIndexOf("E")); // scientific notation
		*/
		return t;
	}
	
	/**
	 * Returns the reporter which is displayed in current row.
	 * Maybe null, if openRow() insted of openRow(Reporter) was called.
	 * @return the reporter which is displayed in current row.
	 */
	protected Reporter getReporterForCurrentRow() {
		return _currentReporter;
	}
}