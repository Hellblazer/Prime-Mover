package desmoj.core.report;

import desmoj.core.simulator.Experiment;

/**
 * A table formatter class for writing simulation output to HTML tables. This
 * class implements the HTML formatting functionality of the deprecated class
 * desmoj.report.HTMLFileOuptut
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Nicolas Knaak
 * @author based on HTMLFileOutput by Tim Lechler
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
public class HTMLTableFormatter extends AbstractTableFormatter {

    /**
     * Close the HTML file by writing the footing tags, flushing the buffer and
     * closing the file.
     */
    @Override
    public void close() {

        if (rowOpen)
            closeRow();
        if (tableOpen)
            closeTable();
        out.writeln("<FONT SIZE=-2>created using <A HREF=http://www.desmoj.de>" + "DESMO-J</A> Version "
        + Experiment.getDesmoJVersion() + " at " + new java.util.Date() + " - DESMO-J is licensed under "
        + Experiment.getDesmoJLicense(true) + "</FONT>");
        out.write("</BODY></HTML>");
    }

    /**
     * Writes the tag to close a row in a table to the file.
     */
    @Override
    public void closeRow() {

        if ((rowOpen) && (tableOpen)) {
            out.writeln("</TR>");
            rowOpen = false;
            this._currentReporter = null;
        }
    }

    /**
     * Inserts the tags needed to close a HTML 3.2 table into the file. Also inserts
     * a paragraph tag to add some space below the table.
     */
    @Override
    public void closeTable() {

        if (!tableOpen)
            return; // do nothing if table not open

        if (rowOpen) {
            closeRow(); // correct an open row if necessary
            rowOpen = false;
        }

        out.writeln("</TABLE><P>"); // The table end tag
        out.writeln("<FONT SIZE=-1><A HREF=#top>top</A></FONT><P>");

        tableOpen = false;

    }

    /**
     * Inserts the tags needed to close a HTML 3.2 table into the file. Also inserts
     * a paragraph tag to add some space below the table. But omits the top tag as
     * used in method <code>closeTable()</code>. This is needed if one reportable is
     * generating a report that consists of more than one table (see
     * <code>StockReporter</code> or <code>HistogramReporter</code>)
     */
    @Override
    public void closeTableNoTopTag() {

        if (!tableOpen)
            return; // do nothing if table not open, just return

        if (rowOpen) {
            closeRow(); // correct an open row if necessary
            rowOpen = false;
        }

        out.writeln("</TABLE><P>"); // The table end tag
        // writeln("<FONT SIZE=-1><A HREF=#top>top</A></FONT><P>");

        tableOpen = false;

    }

    /** @return The string <code>"html"</code> */
    @Override
    public String getFileFormat() {
        return "html";
    }

    /**
     * Opens a new file with the given fileName for writing a HTML table to. If no
     * String is given, the default filename "unnamed_DESMOJ_file" is used.
     *
     * @param name java.lang.String : The name of the file to be created
     */
    @Override
    public void open(String name) {

        StringBuffer sb = new StringBuffer();
        sb.append("<HTML><HEAD>" + FileOutput.getEndOfLine());
        sb.append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=");
        sb.append("\"text/html; charset=iso-8859-1\">" + FileOutput.getEndOfLine());
        sb.append("<META NAME=\"Author\" CONTENT=\"Tim Lechler\">" + FileOutput.getEndOfLine());
        sb.append("<META NAME=\"GENERATOR\" CONTENT=\"DESMO-J " + Experiment.getDesmoJVersion() + "\">"
        + FileOutput.getEndOfLine());
        sb.append("<TITLE>" + name + "</TITLE></HEAD>" + FileOutput.getEndOfLine());
        sb.append("<BODY TEXT=\"#000000\" BGCOLOR=\"#FFFFFF\" LINK=\"#0000EE\"");
        sb.append(" VLINK=\"#551A8B\" ALINK=\"#FF0000\">" + FileOutput.getEndOfLine());
        sb.append("<A NAME=\"top\"></A><br>" + FileOutput.getEndOfLine());

        out.write(sb.toString());

    }

    /**
     * Writes the HTML 3.2 tags to open a new row in a table to the file. A new row
     * can only be started, if the table has alerady been opened and the previous
     * row has been closed.
     */
    @Override
    public void openRow() {

        if (tableOpen) {

            if (!rowOpen) {
                out.write("<TR VALIGN=TOP>");
                rowOpen = true; // keep a note to shut the row on closes
            }

        }

    }

    /**
     * Inserts the tags needed to build a HTML 3.2 table heading into the file. The
     * table's heading text is given with the parameter.
     *
     * @param s String : The heading for the table
     */
    @Override
    public void openTable(String s) {

        if (tableOpen)
            return; // table already opened

        StringBuffer sb = new StringBuffer();

        sb.append("<DIV align=center><H3>" + s + "</H3></DIV>" + FileOutput.getEndOfLine());
        sb.append("<TABLE BORDER=0 CELLSPACING=0 CELLPADDING=3 ");
        sb.append("WIDTH=\"100%\" >" + FileOutput.getEndOfLine());

        out.write(sb.toString());
        tableOpen = true;
        rowOpen = false;

    }

    /**
     * Returns the status of the current table row that is written to.
     *
     * @return boolean : Is <code>true</code> if the method <code>openRow()</code>
     *         has been called last, <code>false</code> if the method
     *         <code>closeRow()</code> has been called last
     */
    @Override
    public boolean rowIsOpen() {

        return rowOpen;

    }

    /**
     * Returns the status of the current table that is written to.
     *
     * @return boolean : Is <code>true</code> if the method <code>openTable()</code>
     *         has been called last, <code>false</code> if the method
     *         <code>closeTable()</code> has been called last
     */
    @Override
    public boolean tableIsOpen() {

        return tableOpen;

    }

    /**
     * Creates a new table cell and writes the given String into that cell. Note
     * that there this is raw HTML code so there must not be any special language
     * specific characters that might confuse any browser. A new cell can not be
     * written, if neither a table nor a row have been opened yet. The method will
     * simply return without action in that case.
     *
     * @param s        java.lang.String : The text to be printed into a cell
     * @param spanning number of cells to span
     */
    @Override
    public void writeCell(String s, int spanning) {

        if ((s == null) || !((rowOpen) && (tableOpen)))
            return;

        if (spanning == 1)
            out.write("<TD>" + s + "</TD>");
        else
            out.write("<TD colspan=\"" + spanning + "\"><i>&ensp;&ensp;&#151; " + s + "<i></TD>");
    }

    /**
     * Creates a newcentered heading row to print a title in. Note that there this
     * is raw HTML code so the string given must not contain any special language
     * specific characters that might confuse any browser. been opened yet. The
     * method will simply return without action in that case. The number for the
     * HTML heading style must be inside the range [1,6]. If not, it will be trimmed
     * to the nearest legal heading style number.
     *
     * @param style int : The heading style format number for the text to be printed
     *              in
     * @param s     java.lang.String : The text to be printed as heading
     */
    @Override
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
        out.write("<P><H" + style + "><DIV align=center>" + s + "</DIV></H" + style + "><P>");

    }

    /**
     * Creates a new table cell and writes the given String into that cell as
     * heading cells in bold letters and with centered text. Note that there this is
     * raw HTML code so there must not be any special language specific characters
     * that might confuse any browser. A new cell can not be written, if netiher a
     * tbale nor a row have been opened yet. The method will simply return without
     * action in that case.
     *
     * @param s java.lang.String : The text to be printed into a cell
     */
    @Override
    public void writeHeadingCell(String s) {

        if (s == null)
            return;

        if ((rowOpen) && (tableOpen))
            out.write("<TD><B><DIV align=left>" + s + "</DIV></B></TD>");

    }

    /**
     * Writes the HTML tag for inserting a horizontal ruler into the file. Note that
     * horizontal rulers are not written into table cells, thus this method simply
     * returns, if a table is still open.
     */
    @Override
    public void writeHorizontalRuler() {

        if (!tableOpen)
            out.write("<HR>");

    }

}
