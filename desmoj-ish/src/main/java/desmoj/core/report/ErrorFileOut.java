package desmoj.core.report;

import java.io.IOException;

/**
 * ErrorFileOut is used to create a file to write the ErrorMessages to. It receives an ErrorMessage and divides its
 * individual text information up to be displayed in HTML format on disc in the user's local directory. The messages are
 * displayed in a tabular design with columns for:
 * <ul>
 * <li>The point of simulation time the ErrorMessage was created.</li>
 * <li>A textual description of what went wrong.</li>
 * <li>The location that issued the ErrorMessage.</li>
 * <li>The possible reason why that error condition occurred.</li>
 * <li>A hint for the user on how to prevent this error to happen again.</li>
 * </ul>
 * Note that if the framework is used as an Applet in webpages there is no
 * access to the disc and an alternative output on screen or into a graphics
 * window must be registered at the Experiment's MessageManager for this type of
 * messages. Errors affecting the java runtime are always displayed on the
 * system's standard output PrintStream.
 *
 * @author Tim Lechler, modified by Nicolas Knaak
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */
public class ErrorFileOut extends TableOutput implements MessageReceiver {

    /**
     * Buffers the last message received for formatting the HTML table to e printed
     * without repeating redundant information.
     */
    // field was never used
    // private ErrorMessage _lastNote;

    /**
     * Creates an ErrorOut to print ErrorMessages into a HTML page. By opening the file, the necessary HTML tags to
     * define a webpage are already inserted into the file. The name given should reflect the experiment that produces
     * this file.
     *
     * @param simTimeFloatingDigits int : The number of floating point digits of the simulation time values to be
     *                              displayed
     */
    public ErrorFileOut(int simTimeFloatingDigits, String format) {

        super(simTimeFloatingDigits, format);

    }

    /**
     * Closes this TraceOut. Writes the final necessary HTML-tags to close a table row, the table and finish the
     * HTML-page. Flushes and closes the FileOutputStream thereafter.
     */
    @Override
    public void close() {

        super.close();
        // _lastNote = null;

    }

    /**
     * Opens a new file with the given filename for writing ErrorMessages into a HTML table. The output path is set to
     * the working directory. If no String is given, the default filename "DESMOJ_errorfile.html" is used.
     *
     * @param name java.lang.String : The name of the file to be created
     */
    @Override
    public void open(String name) {

        this.open(null, name);

    }

    /**
     * Opens a new file with the given file- and pathname for writing ErrorMessages into a HTML table. If no String is
     * given, the default filename "DESMOJ_errorfile.html" is used. If no pathname is given the current working
     * directory (stored as property "user.dir") is used.
     *
     * @param name     java.lang.String : The name of the file to be created
     * @param pathname java.lang.String : The output path the file is written to
     */
    public void open(String pathname, String name) {

        super.open(createFileName(pathname, name, "error"));

        // write tags to open the table used to write the TraceNotes to
        // with the proper heading
        formatter.openTable(name + " - errors & warnings");
        formatter.openRow();
        formatter.writeHeadingCell("model");
        formatter.writeHeadingCell("time");
        formatter.writeHeadingCell("error");
        formatter.writeHeadingCell("content");
        formatter.closeRow();

    }

    /**
     * Reporters are not handled by this class so this method simply returns.
     *
     * @param r Reporter : The Reporter to be processed
     */
    @Override
    public void receive(desmoj.core.report.Reporter r) {
        return; // No reporters are handled here

    }

    /**
     * Receives a TraceNote and writes its contents formatted to a HTML table into a file in the user's default
     * directory. Note that although any type of message may be given to this method, only TraceNotes will be processed.
     * If other types of messages are given to this method, it will simply return doing nothing.
     *
     * @param m Message : The TraceNote to be written to file in HTML-table format
     */
    @Override
    public void receive(Message m) {

        // check parameters
        // again nulls
        if ((m == null) || !(m instanceof ErrorMessage)) {
            return; // got wrong message
        }
        ErrorMessage tmp = (ErrorMessage) m;
        // cast and buffer for easier access

        formatter.openRow();
        formatter.writeCell(tmp.getModelName(), 1);
        formatter.writeCell(formatter.writeTime(tmp.getTime()), 1);
        formatter.writeCell("description", 1);
        formatter.writeCell(tmp.getDescription(), 1);
        formatter.closeRow();
        formatter.openRow();
        formatter.writeCell(" ", 1);
        formatter.writeCell(" ", 1);
        formatter.writeCell("location", 1);
        formatter.writeCell(tmp.getLocation(), 1);
        formatter.closeRow();
        formatter.openRow();
        formatter.writeCell(" ", 1);
        formatter.writeCell(" ", 1);
        formatter.writeCell("reason", 1);
        formatter.writeCell(tmp.getReason(), 1);
        formatter.closeRow();
        formatter.openRow();
        formatter.writeCell(" ", 1);
        formatter.writeCell(" ", 1);
        formatter.writeCell("prevention", 1);
        formatter.writeCell(tmp.getPrevention(), 1);
        formatter.closeRow();
        try {
            file.flush();
        } catch (IOException ioEx) {
            System.out.println("IOException thrown : " + ioEx);
            System.out.println("description: Can't flush " + fileName);
            System.out.println("origin     : Experiment auxiliaries");
            System.out.println("location   : ErrorFileOut.receive(Message)");
            System.out.println(
            "hint       : Check access to the file and" + " that it is not in use by some other application.");
            System.out.println(
            "The System will not be shut down. But it can not be " + "written to the file " + fileName
            + ".  The file may " + "not contain all the important data!");
            /*
             * the system will not be shut down, because this may disrupt other programs
             * like CoSim from Ralf Bachmann (Universtiy of Hamburg, germany).
             */
            // System.exit(-1); // radical but no time for fileselectors now
        }
    }
}
