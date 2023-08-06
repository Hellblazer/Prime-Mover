package desmoj.core.report;

import java.io.File;

/**
 * An output for tables containing simulation results. The table formatting is
 * done by an assigned TableFormatter (e.g. a HTMLTableFormatter or an
 * ASCIITableFormatter). Realizes the output functionality of the deprecated
 * class desmoj.report.HTMLFileOut. This class is the new base class for the
 * different Desmo-J output channels (ReportFileOut, etc.)
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Nicolas Knaak edited by Gunnar Kiesel, 5.1.2004
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
public class TableOutput extends FileOutput {

    /** The table formatter used if formatter is invalid */
    public static final String DEFAULT_FORMATTER = "desmoj.core.report.HTMLTableFormatter";

    /** The table formatter assigned to this output */
    protected TableFormatter formatter;

    /**
     * Creates a new TableOutput with the given time precision and TableFormatter
     *
     * @param timePrec precision for writing time values.
     * @param format   fully qualified class name of assigned TableFormatter
     */
    public TableOutput(int timePrec, String format) {
        FileOutput.setSeparator("\t");
        formatter = createFormatter(format);
        formatter.setOutput(this);
        formatter.setTimePrecision(timePrec);
    }

    /** Closes the table output (and the assigned file). */
    @Override
    public void close() {
        formatter.close();
        super.close();
    }

    /**
     * Opens the table output as a file with the given name
     *
     * @param name filename
     */
    @Override
    public void open(String name) {
        super.open(name);
        formatter.open(name);
    }

    /**
     * Creates a valid filename from the given information
     *
     * @param pathname name of path
     * @param name     name of file
     * @param type     kind of simulation output (e.g. <code>"report"</code>)
     */
    protected String createFileName(String pathname, String name, String type) {

        // check for proper path and filename
        if ((pathname == null) || (pathname.length() == 0))
            pathname = System.getProperty("user.dir", ".");
        if ((name == null) || (name.length() == 0))
            name = "DESMOJ";

        // check for proper suffix of the filename
        String appendix = "." + formatter.getFileFormat().toLowerCase();
        if ((!name.endsWith(appendix)) || (!name.endsWith(appendix.toUpperCase())))
            return pathname + File.separator + name + "_" + type + appendix;
        else
            return pathname + File.separator + name;
    }

    /**
     * Tries creating a TableFormatter from the given class name. If creation fails
     * (e.g. because the class name does not exist) the assigned experiments default
     * table formatter is created
     *
     * @param formatter fully qualified class name of TableFormatter subclass.
     * @return created TableFormatter object Since the Experiment class no longer
     *         contains a default formater, the default formatter is now located in
     *         this class
     */
    protected TableFormatter createFormatter(String formatter) {
        TableFormatter t;
        try {
            Class<?> formatterClass = Class.forName(formatter);
            t = (TableFormatter) formatterClass.newInstance();
            return t;
        } catch (Exception e) {
            // e.printStackTrace();
            return createFormatter(DEFAULT_FORMATTER);
        }
    }

}
