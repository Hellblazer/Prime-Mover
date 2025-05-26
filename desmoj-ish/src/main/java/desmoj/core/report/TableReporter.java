package desmoj.core.report;

import desmoj.core.simulator.Reportable;

/**
 * A reporter with an arbitrary number of rows and columns.
 *
 * @author Nicolas Knaak
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */

public abstract class TableReporter extends Reporter {

    /** 2-dimensional table of entries */
    protected String[][] entryTable;

    /**
     * Header table printed before the data table stored in <tt>entryTable</tt>. This should contain additional
     * information like e.g. the number of observations.
     */
    protected String[][] header = null;

    /** The reporter's number of rows */
    protected int numRows;

    /** The reporter's title */
    private String _title;

    /**
     * Creates a new TableReporter for the given Reportable with the given title
     *
     * @param is    the source of information to be displayed in the table
     * @param title title of the table reporter
     */
    public TableReporter(Reportable is, String title) {
        super(is);
        this._title = title;
        this.groupHeading = "Data Tables";
    }

    /**
     * Returns a one-dimensional array of entries (Computed from getEntryTable() by entries[numColumns * i + j] =
     * entryTable[i][j]).
     *
     * @return field of table entries
     */
    @Override
    public String[] getEntries() {
        getEntryTable();
        entries = new String[numRows * numColumns];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                entries[numColumns * i + j] = entryTable[i][j];
            }
        }
        return entries;
    }

    /**
     * Should initialize and return the 2-dimensional entry table.
     *
     * @return field of entries
     */
    public abstract String[][] getEntryTable();

    /**
     * Returns the header table (or null if no header should be printed in report).
     *
     * @return header field of the table
     */

    public String[][] getHeader() {
        if (header == null) {
            return null;
        } else {
            return header.clone();
        }
    }

    /**
     * Returns the table reporter's title.
     *
     * @return the titel as a string
     */
    public String getTitle() {
        return _title;
    }

    /**
     * Returns the number of rows of this table reporter.
     *
     * @return integer number of rows
     */
    public int numRows() {
        return numRows;
    }

}
