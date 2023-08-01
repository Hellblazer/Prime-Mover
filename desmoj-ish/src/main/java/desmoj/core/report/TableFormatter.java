package desmoj.core.report;

/**
 * An interface representing basic facilites for writing data into tables. The
 * specified operations are adapted from the deprecated class
 * demoj.report.HTMLFileOutput.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler (HTMLFileOutput), Nicolas Knaak
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
public interface TableFormatter {
    
	/** Should close the document */
	public void close();

	/** Should close a table row */
	public void closeRow();

	/** Should close a table */
	public void closeTable();

	/** Should close a table without writing a top tag (HTML specific) */
	public void closeTableNoTopTag();

	/**
	 * @return appendix of the file format the table is stored in (e.g. "html"
	 *         or "txt")
	 */
	public String getFileFormat();

	/**
	 * Should open a new document containing multiple tables with the given name
	 * 
	 * @param name
	 *            table name
	 */
	public void open(String name);

	/** Should open a row */
	public void openRow();

	/**
	 * Should open a table with the given heading
	 * 
	 * @param heading
	 *            table heading
	 */
	public void openTable(String heading);

	/**
	 * @return <code>true</code> if a row is currently open,
	 *         <code>false</code> otherwise
	 */
	public boolean rowIsOpen();

	/**
	 * Should set an output file to write the table to
	 * 
	 * @param out
	 *            a desmoj.report.FileOutput to write the table to
	 */
	public void setOutput(FileOutput out);

	/**
	 * Should set the required precision of time values.
	 * 
	 * @param tp
	 *            precision
	 */
	public void setTimePrecision(int tp);

	/**
	 * @return <code>true</code> if a table is currently open,
	 *         <code>false</code> otherwise
	 */
	public boolean tableIsOpen();

	/**
	 * Should return the precision used for time values.
	 * 
	 * @return precision
	 */
	public int timePrecision();

	/**
	 * Should write the given string into a new table cell
	 * 
	 * @param s
	 *            string to write
	 * @param spanning
	 *            number of cells to span 
	 */
	public void writeCell(String s, int spanning);

	/**
	 * Should write the given heading of size i into a new table cell
	 * 
	 * @param s
	 *            string to write
	 * @param i
	 *            size (must be interpreted in a sensible way).
	 */
	public void writeHeading(int i, String s);

	/**
	 * Should write the given heading of default size into a new table cell
	 * 
	 * @param s
	 *            string to write
	 */
	public void writeHeadingCell(String s);

	/** Writes a horizontal ruler */
	public void writeHorizontalRuler();

	/**
	 * Should format the given time String and write it into a cell
	 * 
	 * @param s
	 *            a string containing simulation time in float format.
	 */
	public String writeTime(String s);
}