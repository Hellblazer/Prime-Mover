package desmoj.core.report;

import java.awt.Color;

import desmoj.core.report.html5chart.Canvas;
import desmoj.core.simulator.Experiment;

/**
 * A table formatter class for writing simulation output to HTML tables.
 * It also supports the visualisation of 2D-charts based on HTML5's canvas-element.
 * This class implements the HTML formatting functionality of the deprecated class
 * desmoj.report.HTMLFileOuptut
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Johanna Djimandjaja
 * @author based on HTMLTableFormatter by Nicolas Knaak
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
public class HTMLTableChartFormatter extends AbstractTableFormatter {
	
	private long _freeCanvasIDNumber = 0;
	
	private JavaScriptFormatter _scriptFormatter = new JavaScriptFormatter();

	/**
	 * Inserts a reference to the top page and close the HTML file by writing the footing tags, flushing the buffer and
	 * closing the file.
	 */
	public void close() {
	
		if (rowOpen)
			closeRow();
		if (tableOpen)
			closeTable();
		
		out.writeln("<font size=\"-1\"><a href=\"#top\">top</a></font><p></p>");
	
		out.writeln("<font size=\"-2\">created using <a href=\"http://www.desmoj.de\">"
				+ "DESMO-J</A> Version " + Experiment.getDesmoJVersion() + " at "
				+ new java.util.Date() + " - DESMO-J is licensed under "
				+ Experiment.getDesmoJLicense(true)
				+ "</font>");
		out.write("</body></html>");
	}
	
	/**
	 * Writes the tag to close a row in a table to the file.
	 */
	public void closeRow() {
	
		if ((rowOpen) && (tableOpen)) {
			out.writeln("</tr>");
			rowOpen = false;
			this._currentReporter = null;
		}
	}
	
	/**
	 * Inserts the tags needed to close a HTML 3.2 table into the file.
	 */
	@Override
	public void closeTable() {
		this.closeTableNoTopTag();
	}

	/**
	 * Inserts the tags needed to close a HTML 3.2 table into the file. Also
	 * inserts a paragraph tag to add some space below the table. But omits the
	 * top tag as used in method <code>closeTable()</code>. This is needed if
	 * one reportable is generating a report that consists of more than one
	 * table (see <code>StockReporter</code> or <code>HistogramReporter</code>)
	 */
	public void closeTableNoTopTag() {
	
		if (!tableOpen)
			return; // do nothing if table not open, just return
	
		if (rowOpen) {
			closeRow(); // correct an open row if necessary
			rowOpen = false;
		}
	
		out.writeln("</table><p></p>"); // The table end tag
	
		tableOpen = false;
	
	}
	
	/**
	 * Creates a canvas area and draws a chart in this area.
	 * @param canvas
	 * 				The canvas containing the chart data to be drawn.
	 */
	public void drawChart(Canvas canvas) {
		if (tableOpen) return; // table still open
		
		this.createCanvas(canvas.getCanvasID(), canvas.getCanvasWidth(), canvas.getCanvasHeight());
		this.openScript();
		
		_scriptFormatter.writeDrawingScript(canvas);
		
		this.closeScript();		
	}
	
	/** @return The string <code>"html"</code> */
	public String getFileFormat() {
		return "html";
	}

	/**
	 * Returns an ID number for a canvas-element
	 * and increases the free ID number.
	 * @return
	 */
	public long getFreeCanvasIDNum() {
		_freeCanvasIDNumber++;
		return _freeCanvasIDNumber-1;
	}

	/**
	 * Opens a new file with the given fileName for writing a HTML table to. If
	 * no String is given, the default filename "unnamed_DESMOJ_file" is used.
	 * 
	 * @param name
	 *            java.lang.String : The name of the file to be created
	 */
	public void open(String name) {
		out.writeln("<!DOCTYPE html>");
		
		StringBuffer sb = new StringBuffer();
		sb.append("<html><head>" + FileOutput.getEndOfLine());
		sb.append("<meta http-equiv=\"Content-Type\" content=");
		sb.append("\"text/html; charset=iso-8859-1\">" + FileOutput.getEndOfLine());
		sb.append("<meta name=\"Author\" content=\"Tim Lechler\">"
				+ FileOutput.getEndOfLine());
		sb.append("<meta name=\"GENERATOR\" content=\"DESMO-J "
				+ Experiment.getDesmoJVersion() + "\">" + FileOutput.getEndOfLine());
		sb.append("<title>" + name + "</title></head>" + FileOutput.getEndOfLine());
		sb
				.append("<body text=\"#000000\" bgcolor=\"#FFFFFF\" link=\"#0000EE\"");
		sb.append(" vlink=\"#551A8B\" alink=\"#FF0000\">" + FileOutput.getEndOfLine());
		sb.append("<a name=\"top\"></a><br />" + FileOutput.getEndOfLine());
	
		out.write(sb.toString());
	
	}

	/**
	 * Writes the HTML 3.2 tags to open a new row in a table to the file. A new
	 * row can only be started, if the table has alerady been opened and the
	 * previous row has been closed.
	 */
	public void openRow() {
	
		if (tableOpen) {
	
			if (!rowOpen) {
				out.write("<tr valign=\"top\">");
				rowOpen = true; // keep a note to shut the row on closes
			}
	
		}
	
	}

	/**
	 * Inserts the tags needed to build a HTML 3.2 table heading into the file.
	 * The table's heading text is given with the parameter.
	 * 
	 * @param s
	 *            String : The heading for the table
	 */
	public void openTable(String s) {
	
		if (tableOpen)
			return; // table already opened
	
		StringBuffer sb = new StringBuffer();
	
		sb.append("<div align=\"center\"><h3>" + s + "</h3></div>"
						+ FileOutput.getEndOfLine());
		sb.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"3\" ");
		sb.append("width=\"100%\">" + FileOutput.getEndOfLine());
	
		out.write(sb.toString());
		tableOpen = true;
		rowOpen = false;
	
	}

	/**
	 * Returns the status of the current table row that is written to.
	 * 
	 * @return boolean : Is <code>true</code> if the method
	 *         <code>openRow()</code> has been called last, <code>false</code>
	 *         if the method <code>closeRow()</code> has been called last
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
		super.setOutput(out);
		_scriptFormatter.setOutput(this.out);
	}

	/**
	 * Returns the status of the current table that is written to.
	 * 
	 * @return boolean : Is <code>true</code> if the method
	 *         <code>openTable()</code> has been called last,
	 *         <code>false</code> if the method <code>closeTable()</code>
	 *         has been called last
	 */
	public boolean tableIsOpen() {
	
		return tableOpen;
	
	}

	/**
	 * Creates a new table cell and writes the given String into that cell. Note
	 * that there this is raw HTML code so there must not be any special
	 * language specific characters that might confuse any browser. A new cell
	 * can not be written, if neither a table nor a row have been opened yet.
	 * The method will simply return without action in that case.
	 * 
	 * @param s
	 *            java.lang.String : The text to be printed into a cell
	 * @param spanning
	 *            number of cells to span             
	 */
	public void writeCell(String s, int spanning) {
	
		if (s == null)
			return;
	
		if (!((rowOpen) && (tableOpen))) return;
		
		if (spanning == 1)
		    out.write("<td>" + s + "</td>");
		else
		    out.write("<td colspan=\"" + spanning + "\"><i>&ensp;&ensp;&#151; " + s + "<i></td>");
	}

	/**
	 * Creates a new table cell with the specified color.
	 * @param cellColor
	 * 			java.awt.Color : The color of the cell, that should be created.
	 */
	public void writeColoredCell(Color cellColor) {
		if (!((rowOpen) && (tableOpen))) return;
		out.write("<td width=\"20\" bgcolor=\"" + toHexString(cellColor) + "\"></td>");
	}

	/**
	 * Creates a newcentered heading row to print a title in. Note that there
	 * this is raw HTML code so the string given must not contain any special
	 * language specific characters that might confuse any browser. been opened
	 * yet. The method will simply return without action in that case. The
	 * number for the HTML heading style must be inside the range [1,6]. If not,
	 * it will be trimmed to the nearest legal heading style number.
	 * 
	 * @param style
	 *            int : The heading style format number for the text to be
	 *            printed in
	 * @param s
	 *            java.lang.String : The text to be printed as heading
	 */
	public void writeHeading(int style, String s) {
	
		// check parameter
		if (s == null)
			return;
	
		// check style number
		if (style > 6)
			style = 6;
		if (style < 1)
			style = 1;
	
		// check if no table is open, otherwise I can't write centered heading
		if (tableOpen)
			return;
	
		// now write heading
		out.write("<p></p><h" + style + "><div align=\"center\">" + s + "</div></h"
				+ style + "><p></p>");
	
	}

	/**
	 * Creates a new table cell and writes the given String into that cell as
	 * heading cells in bold letters and with centered text. Note that there
	 * this is raw HTML code so there must not be any special language specific
	 * characters that might confuse any browser. A new cell can not be written,
	 * if netiher a tbale nor a row have been opened yet. The method will simply
	 * return without action in that case.
	 * 
	 * @param s
	 *            java.lang.String : The text to be printed into a cell
	 */
	public void writeHeadingCell(String s) {
	
		if (s == null)
			return;
	
		if ((rowOpen) && (tableOpen))
			out.write("<td><b><div align=\"left\">" + s + "</div></b></td>");
	
	}

	/**
	 * Inserts a reference to the top page and writes the HTML tag for inserting a horizontal ruler into the file. Note
	 * that horizontal rulers are not written into table cells, thus this method
	 * simply returns, if a table is still open.
	 */
	@Override
	public void writeHorizontalRuler() {

		if (!tableOpen)
			out.writeln("<font size=\"-1\"><a href=\"#top\">top</a></font><p></p>");
			out.write("<hr>");

	}

	/**
	 * Writes the closing tag for the script-Element.
	 */
	private void closeScript() {
		out.writeln("//-->");//closing comments
		out.writeln("</script>");
	}

	/**
	 * Inserts a canvas element.
	 */
	private void createCanvas(String canvasID, int canvasWidth, int canvasHeight) {
		//create the canvas
		out.writeln("<div align=\"center\">");
		out.writeln("<canvas id=\"" + canvasID +
					"\" width=\"" + canvasWidth +
					"\" height=\"" + canvasHeight + "\">");
		out.writeln("Your browser is not able to display this graphic.<br />");
		out.writeln("This could be because it does not support the canvas-Element by html5<noscript> or because it does not support JavaScript</noscript>.");
		out.writeln("</canvas>");
		out.writeln("</div><p></p>");
		
	}
	
	/**
	 * Changes a given number into a 2-digit hexadecimal string.<br>
	 * Numbers less then 0 will be changed to the String 00 and numbers higher then 255 will be changed to FF.
	 * @param number
	 * 			The number, that should be changed into hexadecimal.
	 * @return
	 * 			String : The given number as a 2-digit hexadecimal string.
	 */
	private String intToHexString(int number) {
		if (number <= 0) return "00";
		if (number >= 255) return "FF";
		String hex;
		if (number < 16) {
			hex = "0" + Integer.toHexString(number);
		} else {
			hex = Integer.toHexString(number);
		}
		return hex;
	}
	
	/**
	 * Writes the opening tag for the script-Element.
	 */
	private void openScript() {
		out.writeln("<script type=\"text/javascript\">");
		out.writeln("<!--");
	}
	
	/**
	 * Returns the hexadecimal notation for the given color.<br>
	 * The notation combines the red, green, and blue color values (RGB) in hexadecimal form.
	 * The lowest value that can be given to one of the light sources is 0 (hex 00). The highest value is 255 (hex FF).<br>
	 * When the given color is <code>null</code> returns <code>#000000</code>.
	 * @param color
	 * 			The color to be coded.
	 * @return
	 * 			String : The hexadecimal notation for the given color.
	 */
	private String toHexString(Color color) {
		String hex = "";
		if (color == null) hex = "#000000";
		else {
			int r = color.getRed();
			int g = color.getGreen();
			int b = color.getBlue();
			hex = "#" + intToHexString(r) + intToHexString(g) + intToHexString(b);
		}	
		return hex;
	}	
}