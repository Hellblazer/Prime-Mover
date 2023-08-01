package desmoj.core.report;

/**
 * DebugOut is used to create a file to write the DebugNotes to. It receives a
 * DebugNotes and divides its individual text information up to be displayed in
 * HTML format on disc in the user's local directory. The messages are displayed
 * in a tabular design with columns for:
 * <ul>
 * <li>The point of simulation time the DebugNote was created</li>
 * <li>The ModelComponent's name responsible for sending the DebugNote</li>
 * <li>The textual description of the ModelComponent's current state</li>
 * </ul>
 * Note that if the framework is used as an Applet in webpages there is no
 * access to the disc and an alternative output on screen or into a graphics
 * window must be registered at the Experiment's MessageManager for this type of
 * messages. Errors affecting the java runtime are always displayed on the
 * system's standard output PrintStream.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler, modified by Nicolas Knaak
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
public class DebugFileOut extends TableOutput implements MessageReceiver {

	/**
	 * Buffers the last message received for formatting the HTML table to e
	 * printed without repeating redundant information.
	 */
	private DebugNote _lastNote;

	/**
	 * Creates a DebugOut to print DebugNotes into a HTML page. By opening the
	 * file, the necessary HTML tags to define a webpage are already inserted
	 * into the file. The parameter given should reflect the experiment that
	 * produces this file.
	 * 
	 * @param simTimeFloatingDigits
	 *            int : The number of floating point digits of the simulation
	 *            time values to be displayed
	 */
	public DebugFileOut(int simTimeFloatingDigits, String format) {

		super(simTimeFloatingDigits, format);

		_lastNote = null;

	}

	/**
	 * Closes this DebugOut. Writes the final necessary HTML-tags to close a
	 * table row, the table and finish the HTML-page. Flushes and closes the
	 * FileOutputStream thereafter.
	 */
	public void close() {

		super.close();
		_lastNote = null;

	}

	/**
	 * Opens a new file with the given filename for writing DebugNotes into a
	 * HTML table. The output path is set to the working directory. If no
	 * filename is given, the default filename "DESMOJ_debugfile.html" is used.
	 * 
	 * @param name
	 *            java.lang.String : The name of the file to be created
	 */
	public void open(String name) {

		this.open(null, name);

	}

	/**
	 * Opens a new file with the given file- and pathname for writing DebugNotes
	 * into a HTML table. If no filename is given, the default filename
	 * "DESMOJ_debugfile.html" is used. If no pathname is given the current
	 * working directory (stored as property "user.dir") is used.
	 * 
	 * @param name
	 *            java.lang.String : The name of the file to be created
	 * @param pathname
	 *            java.lang.String : The file's output path
	 */
	public void open(String pathname, String name) {

		super.open(createFileName(pathname, name, "debug"));

		// write tags to open the table used to write the TraceNotes to
		// with the proper heading
		formatter.openTable(name + " - debug notes");
		formatter.openRow();
		formatter.writeHeadingCell("model");
		formatter.writeHeadingCell("time");
		formatter.writeHeadingCell("origin");
		formatter.writeHeadingCell("debug information");
		formatter.closeRow();

	}

	/**
	 * Reporters are not handled by this class so this method simply returns.
	 * 
	 * @param r
	 *            Reporter : The reporter to be processed
	 */
	public void receive(desmoj.core.report.Reporter r) {

		return; // No reporters are handled here

	}

	/**
	 * Receives a debugnote and writes its contents formatted to a HTML table
	 * into a file in the user's default directory. Note that although any type
	 * of message may be given to this method, only debugnotes will be
	 * processed. If other types of messages are given to this method, it will
	 * simply return doing nothing.
	 * 
	 * @param m
	 *            Message : The DebugNote to be written to file in HTML-table
	 *            format
	 */
	public void receive(Message m) {

		// check parameters
		if (m == null)
			return; // again nulls
		if (!(m instanceof DebugNote))
			return; // got wrong message
		DebugNote tmp = (DebugNote) m; // cast and buffer for easier access

		formatter.openRow();

		if (_lastNote == null) {

			formatter.writeCell(tmp.getModelName(), 1);
			formatter.writeCell(formatter.writeTime(tmp.getTime()), 1);
			formatter.writeCell(tmp.getOrigin(), 1);
			formatter.writeCell(tmp.getDescription(), 1);
		} else {

			// now write the modelname to debug
			if (tmp.getModelName().equals(_lastNote.getModelName()))
				formatter.writeCell(" ", 1);
			else
				formatter.writeCell(formatter.writeTime(tmp.getModelName()), 1);

			// now write the time to debug
			if (tmp.getTime().equals(_lastNote.getTime()))
				formatter.writeCell(" ", 1);
			else
				formatter.writeCell(formatter.writeTime(tmp.getTime()), 1);

			// now write the Origin to debug
			if (tmp.getOrigin().equals(_lastNote.getOrigin()))
				formatter.writeCell(" ", 1);
			else
				formatter.writeCell(tmp.getOrigin(), 1);

			// always write the description to debug
			formatter.writeCell(tmp.getDescription(), 1);
		}

		// remember the last note for future formatting
		_lastNote = tmp; // set new note to be last

		formatter.closeRow();

	}
}