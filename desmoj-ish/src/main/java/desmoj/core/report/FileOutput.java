package desmoj.core.report;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * FileOutput is the class all other writer-based Output classes used within the DESMO-j framework are derived from. The
 * basic functionality needed for all FileOutput objects are methods to open and close the stream used for output and
 * set the two necessary types of separators for column output. Two methods for writing Strings into the file are
 * supported. The name is needed for opening a file to identify the output channel in terms of window titles or file
 * names, depending on the type of output the specific subclass relies on. Note that each object of this class supports
 * the creation of several files in a row. You can open and close as many files as needed with one object.
 *
 * Users of this class dont necessarily have to write to real files on disk, see {@link #setFileSystemAccess}.
 *
 * @author Tim Lechler
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */
public class FileOutput {

    /**
     * The standard method to write output: Plain files on disk.
     */
    public static final FileSystemAccess NORMAL_FILE_ACCESS = new FileSystemAccess() {
        @Override
        public Writer createWriter(String filename) throws IOException {
            return new FileWriter(filename);
        }
    };

    /**
     * The character used to indicate the end of a line.
     */
    private static String eol = System.getProperty("line.separator"); // standard OS eol

    /**
     * The strategy to get {@link Writer}s for filenames.
     */
    private static FileSystemAccess fileSystem = NORMAL_FILE_ACCESS;

    /**
     * The character used to separate individual entries within a line.
     */
    private static String sep = ";"; // standard String to separate entries
    /**
     * Remembers if any data has been written to it. If not, it might as well be deleted.
     */
    protected boolean empty;
    /**
     * The FileWriter used to open, close and write file and data.
     */
    protected Writer file;
    /**
     * The name of the file produced by this FileOutput.
     */
    protected String fileName;
    /**
     * The status of the current file.
     */
    protected boolean fileOpen;

    /**
     * Constructs a FileOutput object. A FileOuput object is capable of opening a file using method <code>open(String
     * fileName)</code>. Note that if the output stream used cannot be opened, the whole error handling is printed to
     * the system's standard output stream. The 'endOfLine' String is set to the underlying system's String used to
     * separate lines as given by the system property <code>line.separator</code>.
     */
    public FileOutput() {

        fileOpen = false; // no file open yet
        empty = true; // and nothing written to it either
        fileName = null; // name can only be given with method open(String)
    }

    /**
     * Returns the currently valid String for separating lines of output. This is a platform dependent String and is set
     * to the underlying system's String by default.
     *
     * @return String : The String used to separate lines
     */
    public static String getEndOfLine() {

        return eol;

    }

    /**
     * Returns the currently active String for separating individual entries in a line of output. This comes in handy,
     * when trying to write files in a certain format i.e. dbf format. Data from files complying to that format can
     * easily be read and imported to a variety of standard statistics packages for further manipulation and analysis.
     *
     * @return String : The String used to separate entries within a line
     */
    public static String getSeparator() {

        return sep;

    }

    /**
     * Sets the end-of-line separator String to the given parameter value. The default value is the end-of-line String
     * of the underlying platform. Use this method to create files of specific formats. Data from files complying to
     * that format can easily be read and imported to a variety of standard statistics packages for further manipulation
     * and analysis.
     *
     * @param eolString String : The String for separating lines
     */
    public static void setEndOfLine(String eolString) {

        eol = eolString;

    }

    /**
     * Enables the user to globally change the way files are created/opened. The standard is to create them on disk, but
     * using this method other options, like creating them in-memory, are available for the standard formatters.
     */
    public static void setFileSystemAccess(FileSystemAccess fileSystemAccess) {
        fileSystem = fileSystemAccess;
    }

    /**
     * Sets the entry separator String to the given parameter value. The default value is ';'. This is useful, when
     * trying to write files in a certain format i.e. dbf format. Data from files complying to that format can easily be
     * read and imported to a variety of standard statistics packages for further manipulation and analysis.
     *
     * @param sepString String : The String for separating entries within a line
     */
    public static void setSeparator(String sepString) {

        sep = sepString;

    }

    /**
     * Flushes the buffer and closes the file. note that if another file with the same name is opened and written to, it
     * will overwrite the previous file on the disc without notice. All users of Output objects have to call
     * <code>close()</code> to properly shut down the ouput stream after the last
     * call to the printing methods. Classes overriding this method should put all necessary actions to properly close
     * their file into the <code>close()</code> method and make a call to <code>super.close()</code> as their last
     * command to finally close buffers and files.
     */
    public void close() {

        if (!fileOpen) {
            return; // file not yet openend
        }

        try {
            file.flush();
            file.close();
            fileOpen = false;
            empty = true;
        } catch (IOException ioEx) {
            System.out.println("IOException thrown : " + ioEx);
            System.out.println("description: Can't flush and close " + fileName);
            System.out.println("origin     : Experiment auxiliaries");
            System.out.println("location   : FileOutput.close()");
            System.out.println(
            "hint       : Check access to the file and" + " that it is not in use by some other application.");
            System.out.println("The System will not be shut down. But the file " + fileName
                               + " can not be closed and may contain no data!");
            /*
             * the system will not be shut down, because this may disrupt other programs
             * like CoSim from Ralf Bachmann (University of Hamburg, germany).
             */
            // System.exit(-1); // radical but no time for fileselectors now
        }

    }

    /**
     * Returns the current fileName of this FileOutput or <code>null</code> if it has not been opened and named yet.
     *
     * @return java.lang.String : The name of the file or <code>null</code> if file has not been opened and named yet
     */
    public String getFileName() {

        return fileName;

    }

    /**
     * Returns a boolean value showing if relevant data has been written to the file. If only file headings have been
     * written, this method will return
     * <code>true</code> to indicate that it does not contain information and thus
     * might just as well be deleted. If data has been written to it, this method will return <code>false</code>
     * instead.
     *
     * @return boolean : Is <code>true</code> if no relevant data but the heading has been written to this file,
     * <code>false</code> otherwise
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * Returns the current state of the FileOutput. <code>True</code> is returned, if the file has been opened
     * successfully and can be written to. If the file has been closed or has not been opened yet, <code>false</code> is
     * returned.
     *
     * @return boolean : Is <code>true</code> if the file is currently open,
     * <code>false</code> if not
     */
    public boolean isOpen() {

        return fileOpen;

    }

    /**
     * Opens a new file with the given fileName for writing data to. If no String is given, the default filename
     * "unnamed_DESMOJ_file" is used. Note that opening a file with a name that already exists in the user's target
     * directory will overwrite that existing file without notice! So be careful when choosing file names and make sure
     * to use unique names.
     *
     * @param name java.lang.String : The name of the file to be created
     */
    public void open(String name) {

        if (fileOpen) {
            return; // file already opened
        }

        if (name != null) {
            fileName = name;// create the named object
        } else {
            fileName = "unnamed_DESMOJ_File";
        }

        // now try to create a new file in the user's standard directory
        try {
            file = new BufferedWriter(fileSystem.createWriter(fileName));

            fileOpen = true;
            empty = true;
        } catch (IOException ioEx) {
            System.out.println("IOException thrown : " + ioEx);
            System.out.println("description: Can't create file " + fileName);
            System.out.println("origin     : While creating the Experiment " + "auxiliaries.");
            System.out.println("location   : method open() in class FileOutput.");
            System.out.println(
            "hint       : Check access to the default path and " + "that no file of thesame name exists");
            System.out.println(
            "The System will not be shut down. But the file " + fileName + " can not be opened and may not exist as "
            + "expected!");
            /*
             * the system will not be shut down, because this may disrupt other programs
             * like CoSim from Ralf Bachmann (Universtiy of Hamburg, germany).
             */
            // System.exit(-1); // radical but no time for fileselectors now
        }

    }

    /**
     * Writes the given String to the open file. If the given String is empty, the method will simply return without
     * action. Override this method if you want your data to be written with specific tags as used in the
     * <code>HTMLFileOutput</code> class.
     *
     * @param s java.lang.String : The String to write to the file
     * @see HTMLFileOutput
     */
    public void write(String s) {
        // again nulls
        if ((s == null) || !fileOpen) {
            return; // file not yet openend
        }
        empty = false; // remember that something's written to file

        // try to write and handle Exception if needed
        try {
            file.write(s);
        } catch (IOException ioEx) {
            System.out.println("IOException thrown : " + ioEx);
            System.out.println("description: Can't write to file " + fileName);
            System.out.println("origin     : Experiment auxiliaries");
            System.out.println("location   : class FileOutput, method write()");
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

    /**
     * Writes the given String to the open file, adding a separator to the end of the String written. If the string
     * given is null or "" just a new line character will be written to the file. This method simply adds the
     * end-of-line String to the String given and calls the
     * <code>void write(String s)</code> method.
     *
     * @param s java.lang.String : The String to write to the file
     */
    public void writeSep(String s) {

        if ((s == null) || (s.length() == 0)) {
            write(sep);
            return;
        }

        write(s + sep);
    }

    /**
     * Writes the given String to the open file, adding a line separator to the end of the String written. If the string
     * given is null or "" just a new line character will be written to the file. This method simply adds the
     * end-of-line String to the String given and calls the
     * <code>void write(String s)</code> method.
     *
     * @param s java.lang.String : The String to write to the file
     */
    public void writeln(String s) {

        if ((s == null) || (s.length() == 0)) {
            write(eol);
            return;
        }

        write(s + eol);

    }
}
