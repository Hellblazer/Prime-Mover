package desmoj.core.statistic;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import desmoj.core.report.FileOutput;
import desmoj.core.report.TimeSeriesReporter;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * The <code>TimeSeries</code> class is recording data from a
 * <code>ValueSupplier</code> object and saving it to a file, making data
 * available to a TimeSeriesPlotter or doing both. Either every time the
 * <code>ValueSupplier</code> object has changed or at every tick of the
 * SimClock. See the automatic parameter in the constructor. If writing to a
 * file is claimed, the first line of the file contains the name of the file.
 * Then each line of the file contains a SimTime value and the value of the
 * <code>ValueSupplier</code> object at that point of time in the simulation,
 * separated by a ";". <br>
 * One can choose when the recording of the values will start and stop. See the
 * constructor for further information. <br>
 * Reseting the <code>TimeSeries</code> object will delete the contents of the
 * file. <br>
 * This <code>TimeSeries</code> object will be the observer of the
 * <code>ValueSupplier</code> object given in the constructor. When constructing
 * a new <code>TimeSeries</code> object it will be added to the list of
 * observers of the <code>ValueSupplier</code>. That means this
 * <code>TimeSeries</code> object will be updated automatically every time the
 * observed <code>ValueSupplier</code> has changed and has called its
 * <code>notifyStatistics()</code> method.
 *
 * As separator between different values (e.g. SimTime and value of the
 * Valuesupplier) ";" is used. See also FileOutput.
 *
 * @see desmoj.core.report.FileOutput
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen
 * @author modified by Philip Joschko
 * @author based on DESMO-C from Thomas Schniewind, 1998
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

public class TimeSeries extends desmoj.core.statistic.StatisticObjectSupportingTimeSpans {

    // ****** attributes ******

    /**
     * List for saving the data values in the memory, needed for the
     * GraphicalObserver.
     */
    ArrayList<Double> dataValues = null;

    /**
     * Boolean indicating whether to write data to file or not.
     */
    boolean hasToWriteToFile;

    /**
     * The graphical observer (ChartPlotter) this time series is connected to.
     * 'null' if there is no observer. If there is a connected plotter, the values
     * have to be kept in memory and not only written to a file.
     */
    java.util.Observer plotter = null;

    /**
     * List for saving the time values in the memory, needed for the
     * GraphicalObserver.
     */
    ArrayList<Double> timeValues = null;

    /**
     * Save the actual values of the ValueSupplier for all the time the simulation
     * is running? ( Is end <= start ?)
     */
    private boolean _always;

    /**
     * Save the actual value of the ValueSupplier at every tick of the SimClock?
     */
    private boolean _automatic;

    /**
     * From this TimeInstant on the TimeSeries will stop to save the data in the
     * file.
     */
    private TimeInstant _end;

    /**
     * The file where all the data will be saved to.
     */
    private FileOutput _file;

    /**
     * The name of the file where all the data will be saved to. Empty if no file
     * output is wanted.
     */
    private String _fileName;

    /**
     * Group for HTML output: Only TimeSeries with the same group entry will end up
     * in the same diagram.
     */
    private String _group = "default";

    /**
     * From this TimeInstant on the TimeSeries will start to save the data in the
     * file.
     */
    private TimeInstant _start;

    /**
     * The ValueSupplier which values will be recorded and saved in the file.
     */
    private ValueSupplier _valSuppl;

    // ****** methods ******

    /**
     * Constructor for a TimeSeries object that has NO connection to a
     * <code>ValueSupplier</code> but will write data to the given file.
     *
     * @param ownerModel   Model : The model this TimeSeries object is associated to
     * @param name         java.lang.String : The name of this TimeSeries object
     * @param fileName     java.lang.String : The name of the file the values will
     *                     be saved to. Must comply to the naming rules of the
     *                     underlying Operating System. This name will be saved in
     *                     the first line of the file.
     * @param start        TimeInstant : The instant at which this TimeSeries will
     *                     start to save the values to the file.
     * @param end          TimeInstant : The instant at which this TimeSeries will
     *                     stop to save the values to the file. Choose an end time
     *                     that lies before the start time to record the values for
     *                     all the time.
     * @param showInReport boolean : Flag for showing this TimeSeries in report
     *                     files. Set it to <code>true</code> if TimeSeries should
     *                     show up in report. Set it to <code>false</code> if
     *                     TimeSeries should not be shown in report.
     * @param showInTrace  boolean : Flag for showing this TimeSeries in trace
     *                     files. Set it to <code>true</code> if TimeSeries should
     *                     show up in trace. Set it to <code>false</code> if
     *                     TimeSeries should not be shown in trace.
     */
    public TimeSeries(Model ownerModel, String name, String fileName, TimeInstant start, TimeInstant end,
                      boolean showInReport, boolean showInTrace) {
        this(ownerModel, name, start, end, showInReport, showInTrace);

        this._fileName = fileName; // get hold of the file name
        this._file = new FileOutput();
        hasToWriteToFile = true;
        // fileName contains no proper name
        if (fileName == null) {
            sendWarning("Attempt to write to a file which has no name. "
            + "The file will be named: 'unnamed_TimeSeries_File'!",
                        "TimeSeries: " + this.getName() + " Constructor: TimeSeries(Model "
                        + "ownerModel, String name, String fileName, ValueSupplier valSup,"
                        + " SimTime start, SimTime end, boolean automatic, boolean " + "showInTrace)",
                        "A file with no name will be lost in deep space of your harddisk.",
                        "Make sure to give output files a useful name.");

            this._fileName = "unnamed_TimeSeries_File";
        }
    }

    /**
     * Constructor for a TimeSeries object that will observe a
     * <code>ValueSupplier</code> and write data to the given file.
     *
     * @param ownerModel   Model : The model this TimeSeries object is associated to
     * @param name         java.lang.String : The name of this TimeSeries object
     * @param fileName     java.lang.String : The name of the file the values will
     *                     be saved to. Must comply to the naming rules of the
     *                     underlying Operating System. This name will be saved in
     *                     the first line of the file.
     * @param valSup       ValueSupplier : The values from this ValueSupplier will
     *                     be recorded and saved in the file. The given
     *                     ValueSupplier will be observed by this TimeSeries object.
     * @param start        TimeInstant : The instant at which this TimeSeries will
     *                     start to save the values to the file.
     * @param end          TimeInstant : The instant at which this TimeSeries will
     *                     stop to save the values to the file. Choose an end time
     *                     that lies before the start time to record the values for
     *                     all the time.
     * @param automatic    boolean : Shall the values be recorded automatically at
     *                     every tick of the SimClock?
     * @param showInReport boolean : Flag for showing this TimeSeries in report
     *                     files. Set it to <code>true</code> if TimeSeries should
     *                     show up in report. Set it to <code>false</code> if
     *                     TimeSeries should not be shown in report.
     * @param showInTrace  boolean : Flag for showing this TimeSeries in trace
     *                     files. Set it to <code>true</code> if TimeSeries should
     *                     show up in trace. Set it to <code>false</code> if
     *                     TimeSeries should not be shown in trace.
     */
    public TimeSeries(Model ownerModel, String name, String fileName, ValueSupplier valSup, TimeInstant start,
                      TimeInstant end, boolean automatic, boolean showInReport, boolean showInTrace) {
        this(ownerModel, name, valSup, start, end, automatic, showInReport, showInTrace);

        this._fileName = fileName; // get hold of the file name
        this._file = new FileOutput();
        hasToWriteToFile = true;

        // fileName contains no proper name
        if (fileName.equals("") || fileName == null) {
            sendWarning("Attempt to write to a file which has no name. "
            + "The file will be named: 'unnamed_TimeSeries_File'!",
                        "TimeSeries: " + this.getName() + " Constructor: TimeSeries(Model "
                        + "ownerModel, String name, String fileName, ValueSupplier valSup,"
                        + " SimTime start, SimTime end, boolean automatic, boolean " + "showInTrace)",
                        "A file with no name will be lost in deep space of your harddisk.",
                        "Make sure to give output files a useful name.");

            this._fileName = "unnamed_TimeSeries_File";
        }

    }

    /**
     * Constructor for a TimeSeries object that has NO connection to a
     * <code>ValueSupplier</code> and will NOT write data into a file. If you will
     * not connect this object to a TimeSeriesPlotter after using this constructor,
     * it will be useless. Choose another constructor for writing data to file.
     *
     * @param ownerModel   Model : The model this TimeSeries object is associated to
     * @param name         java.lang.String : The name of this TimeSeries object
     * @param start        TimeInstant : The instant at which this TimeSeries will
     *                     start to save the values to the file.
     * @param end          TimeInstant : The instant at which this TimeSeries will
     *                     stop to save the values to the file. Choose an end time
     *                     that lies before the start time to record the values for
     *                     all the time.
     * @param showInReport boolean : Flag for showing this TimeSeries in report
     *                     files. Set it to <code>true</code> if TimeSeries should
     *                     show up in report. Set it to <code>false</code> if
     *                     TimeSeries should not be shown in report.
     * @param showInTrace  boolean : Flag for showing this TimeSeries in trace
     *                     files. Set it to <code>true</code> if TimeSeries should
     *                     show up in trace. Set it to <code>false</code> if
     *                     TimeSeries should not be shown in trace.
     */
    public TimeSeries(Model ownerModel, String name, TimeInstant start, TimeInstant end, boolean showInReport,
                      boolean showInTrace) {
        super(ownerModel, name, showInReport, showInTrace);

        this._valSuppl = null; // there is no ValueSupplier to observe
        this._start = start;
        this._end = end;
        this._automatic = false; // the values can not be recorded automatically

        this._always = TimeInstant.isBefore(end, start);

        hasToWriteToFile = false;
    }

    /**
     * Constructor for a TimeSeries object that will observe a
     * <code>ValueSupplier</code> but will NOT write data into a file. If you will
     * not connect this object to a TimeSeriesPlotter after using this constructor,
     * it will be useless! Choose another constructor for writing data to file.
     *
     * @param ownerModel   Model : The model this TimeSeries object is associated to
     * @param name         java.lang.String : The name of this TimeSeries object
     * @param valSup       ValueSupplier : The values from this ValueSupplier will
     *                     be recorded and saved in the file. The given
     *                     ValueSupplier will be observed by this TimeSeries object.
     * @param start        TimeInstant : The point of SimTime this TimeSeries will
     *                     start to save the values to the file.
     * @param end          TimeInstant : The point of SimTime this TimeSeries will
     *                     stop to save the values to the file. Choose an end time
     *                     that lies before the start time to record the values for
     *                     all the time.
     * @param automatic    boolean : Shall the values be recorded automatically at
     *                     every tick of the SimClock?
     * @param showInReport boolean : Flag for showing this TimeSeries in report
     *                     files. Set it to <code>true</code> if TimeSeries should
     *                     show up in report. Set it to <code>false</code> if
     *                     TimeSeries should not be shown in report.
     * @param showInTrace  boolean : Flag for showing this TimeSeries in trace
     *                     files. Set it to <code>true</code> if TimeSeries should
     *                     show up in trace. Set it to <code>false</code> if
     *                     TimeSeries should not be shown in trace.
     */
    public TimeSeries(Model ownerModel, String name, ValueSupplier valSup, TimeInstant start, TimeInstant end,
                      boolean automatic, boolean showInReport, boolean showInTrace) {
        super(ownerModel, name, showInReport, showInTrace);

        // valSup is no valid ValueSupplier
        if (valSup == null) {
            sendWarning("Attempt to produce a TimeSeries about a non existing "
            + "ValueSupplier. The command will be ignored!",
                        "TimeSeries: " + this.getName() + " Constructor: TimeSeries(Model "
                        + "ownerModel, String name, String fileName, ValueSupplier valSup,"
                        + " SimTime start, SimTime end, boolean automatic, boolean " + "showInTrace)",
                        "The given ValueSupplier: valSup is only a null pointer.",
                        "Make sure to pass a valid ValueSupplier when constructing a new " + "TimeSeries object.");

            return; // just return
        }

        this._valSuppl = valSup;
        this._start = start;
        this._end = end;
        this._automatic = automatic;

        this._always = TimeInstant.isBefore(end, start);

        hasToWriteToFile = false;

        if (automatic) // update at every tick of the SimClock?
        {
            Observable simClock = this.getModel().getExperiment().getSimClock();

            simClock.addObserver(this); // observe the SimClock
        } else {
            _valSuppl.addObserver(this); // observe the valSuppl
        }
    }

    /**
     * Connects this statistic object with its GraphicalObserver. Advises this
     * object to keep the values in the memory, because the TimeSeriesPlotter
     * requires this.
     *
     * @param plotter The TimeSeriesPlotter which will display this TimeSeries
     *                object.
     */
    public void connectToPlotter(java.util.Observer plotter) {
        this.plotter = plotter;
    }

    /**
     * Returns a default Reporter! In addition, all data (values) can be saved in a
     * file or displayed in the GraphicalObserver. <br>
     * Overrides the method: createDefaultReporter() from
     * desmoj.core.simulator.Reportable.
     *
     * @return desmoj.report.Reporter :<code>null</code> will be returned
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {
        return new TimeSeriesReporter(this);
    }

    /**
     * Gets the end instant of this TimeSeries.
     *
     * @return The endinstant of this TimeSeries.
     */
    public TimeInstant get_end() {
        return this._end;
    }

    /**
     * Gets the start instant of this TimeSeries.
     *
     * @return The start instant of this TimeSeries.
     */
    public TimeInstant get_start() {
        return this._start;
    }

    /**
     * Gets the list of data values.
     *
     * @return The list with all the data values.
     */
    public List<Double> getDataValues() {
        return dataValues;
    }

    /**
     * Gets the group ID used for HTML report clustering.
     *
     * @return The group ID used for HTML report clustering.
     */
    public String getGroup() {
        return this._group;
    }

    /**
     * Gets the list of time values.
     *
     * @return The list of all the time values.
     */
    public List<Double> getTimeValues() {
        return timeValues;
    }

    /**
     * Returns the actual value of the <code>ValueSupplier</code> which values are
     * recorded from this TimeSeries object.
     *
     * @return double : The actual value of the observed <code>ValueSupplier</code>
     *         object.
     */
    public double getValue() {
        // there is no valid ValueSupplier
        if (_valSuppl == null) {
            sendWarning("Attempt to get a value for a TimeSeries from a non "
            + "existing ValueSupplier. UNDEFINED will be returned!",
                        "TimeSeries: " + this.getName() + " Method: getValue() ",
                        "The given ValueSupplier: valSuppl is only a null pointer. So no "
                        + "value can be fetched from it.",
                        "Make sure to pass a valid ValueSupplier when constructing a new " + "TimeSeries object.");

            return UNDEFINED; // just return
        }

        // get the value from the ValueSupplier
        double value = _valSuppl.value();

        // return the rounded value
        return round(value);
    }

    /**
     * Resets this TimeSeries object by deleting the contents of the file to which
     * all the data is saved to. The name of the file will remain the same. This is
     * done by closing and opening the existing file.
     */
    @Override
    public void reset() {
        super.reset(); // reset the Reportable, too.

        if (hasToWriteToFile) {
            if (_file.isOpen())
                _file.close();
            // register the FileOutput object at the experiment, so it will be
            // closed properly when the experiment is over
            this.getModel().getExperiment().registerFileOutput(_file);
            _file.open(_fileName); // this will overwrite the already
            // existing file
            // with the file name: fileName.
            // write the fileName in the first line of the file
            _file.writeln(_fileName);
        }
    }

    /**
     * Sets the group ID used for HTML report clustering.
     *
     * @param groupID The group ID used for HTML report clustering.
     */
    public void setGroup(String groupID) {
        this._group = groupID;
    }

    /**
     * Updates this <code>TimeSeries</code> object by fetching the actual value of
     * the <code>ValueSupplier</code> and processing it. The
     * <code>ValueSupplier</code> is passed in the constructor of this
     * <code>TimeSeries</code> object. This <code>update()</code> method complies
     * with the one described in DESMO, see [Page91].
     */
    public void update() {
        if (hasToWriteToFile) {
            if (!_file.isOpen()) // the file is opened for the first time
            {
                // register the FileOutput object at the experiment, so it will be
                // closed properly when the experiment is over
                this.getModel().getExperiment().registerFileOutput(_file);

                // open the file to write data to it and give it a name
                _file.open(_fileName);

                // write the fileName in the first line of the file
                _file.writeln(_fileName);
            }
        }

        TimeInstant actualTime = presentTime(); // get hold of the current time

        if (!_always) // if not always
        { // check boundaries of interval (start - end)

            // start of interval is not reached yet
            if (TimeInstant.isBefore(actualTime, _start)) {
                return;
            } // do nothing, just return

            // already beyond end of interval
            if (TimeInstant.isBefore(_end, actualTime)) {
                if (_automatic) // is the SimClock observed?
                {
                    Observable simClock = this.getModel().getExperiment().getSimClock();

                    simClock.deleteObserver(this); // stop observing the
                    // SimClock
                }

                return; // do nothing, just return

            } // end beyond end of interval

        } // end always

        // there is no valid ValueSupplier
        if (_valSuppl == null) {
            sendWarning("Attempt to update a TimeSeries with a non existing "
            + "ValueSupplier. Which value should be written to the file? " + "The command will be ignored!",
                        "TimeSeries: " + this.getName() + " Method: update() ",
                        "The given ValueSupplier: valSuppl is only a null pointer.",
                        "Make sure to update the TimeSeries only if there is a valid "
                        + "ValueSupplier. Or use the other update(double val) method.");

            return; // just return
        }

        // get the value from the ValueSupplier
        double actualValue = getValue();

        // get double representation of current time (rounding!)
        double actTime = actualTime.getTimeAsDouble();

        if (hasToWriteToFile) {
            // make the string which will be saved in the file
            String record = actTime + FileOutput.getSeparator() + actualValue;
            _file.writeln(record); // write the String: record to the file
        }

        if (timeValues == null)
            timeValues = new ArrayList<>();
        if (dataValues == null)
            dataValues = new ArrayList<>();
        dataValues.add(actualValue);
        timeValues.add(actTime);
        if (plotter != null)
            plotter.update(null, null);

        incrementObservations(); // increment the observations (see Reportable)
        traceUpdate(); // leave a message in the trace
    }

    /**
     * Updates this <code>TimeSeries</code> object with the double value given as
     * parameter. In some cases it might be more convenient to pass the value this
     * <code>ValueStatistics</code> will be updated with directly within the
     * <code>update(double val)</code> method instead of going via the
     * <code>ValueSupplier</code>.
     *
     * @param val double : The value with which this <code>TimeSeries</code> will be
     *            updated.
     */
    public void update(double val) {
        if (hasToWriteToFile) {
            if (!_file.isOpen()) // the file is opened for the first time
            {
                // register the FileOutput object at the experiment, so it will be
                // closed properly when the experiment is over
                this.getModel().getExperiment().registerFileOutput(_file);

                // open the file to write data to it and give it a name
                _file.open(_fileName);

                // write the fileName in the first line of the file
                _file.writeln(_fileName);
            }
        }

        TimeInstant actualTime = presentTime(); // get hold of the current time

        if (!_always) // if not always
        { // check boundaries of interval (start - end)

            // start of interval is not reached yet
            if (TimeInstant.isBefore(actualTime, _start)) {
                return;
            } // do nothing, just return

            // already beyond end of interval
            if (TimeInstant.isBefore(_end, actualTime)) {
                if (_automatic) // is the SimClock observed?
                {
                    Observable simClock = this.getModel().getExperiment().getSimClock();

                    simClock.deleteObserver(this); // stop observing the
                    // SimClock
                }

                return; // do nothing, just return

            } // end beyond end of interval

        } // end always

        // get double representation of current time (rounding!)
        double actTime = round(actualTime.getTimeAsDouble());

        if (hasToWriteToFile) {
            // make the string which will be saved in the file
            String record = actTime + FileOutput.getSeparator() + val;
            _file.writeln(record); // write the String: record to the file
        }

        if (timeValues == null)
            timeValues = new ArrayList<>();
        if (dataValues == null)
            dataValues = new ArrayList<>();
        dataValues.add(new Double(val));
        timeValues.add(new Double(actTime));
        if (plotter != null)
            plotter.update(null, null);

        incrementObservations(); // increment the observations (see Reportable)
        traceUpdate(); // leave a message in the trace
    }

    /**
     * Implementation of the virtual <code>update(Observable, Object)</code> method
     * of the <code>Observer</code> interface. This method will be called
     * automatically from an <code>Observable</code> object within its
     * <code>notifyObservers()</code> method. <br>
     *
     * @param o   java.util.Observable : The Observable calling this method within
     *            its own <code>notifyObservers()</code> method.
     * @param arg Object : The Object with which this <code>TimeSeries</code> is
     *            updated.
     */
    @Override
    public void update(Observable o, Object arg) {
        if (o == null) // null was passed instead of an Observable
        {
            sendWarning("Attempt to update a TimeSeries with no reference to an " + "Observable. The value of '"
            + _valSuppl.getName() + "' will be " + "recorded and saved in the file anyway.",
                        "TimeSeries: " + this.getName() + " Method: update (Observable o, " + "Object arg)",
                        "The passed Observable: o in this method is only a null pointer.",
                        "The update()-method was not called via notifyObservers() from an "
                        + "Observable. Who was calling it? Why don't you let the Observable do" + " the work?");
        }
        if (hasToWriteToFile) {
            if (!_file.isOpen()) // the file is opened for the first time
            {
                // register the FileOutput object at the experiment, so it will be
                // closed properly when the experiment is over
                this.getModel().getExperiment().registerFileOutput(_file);

                // open the file to write data to it and give it a name
                _file.open(_fileName);

                // write the fileName in the first line of the file
                _file.writeln(_fileName);
            }
        }

        // get hold of the current time
        TimeInstant actualTime = presentTime(); // get hold of the current time

        if (!_always) // if not always
        { // check boundaries of interval (start - end)

            // start of interval is not reached yet
            if (TimeInstant.isBefore(actualTime, _start)) {
                return;
            } // do nothing, just return

            // already beyond end of interval
            if (TimeInstant.isBefore(_end, actualTime)) {
                if (_automatic) // is the SimClock observed?
                {
                    Observable simClock = this.getModel().getExperiment().getSimClock();

                    simClock.deleteObserver(this); // stop observing the
                    // SimClock
                }

                return; // do nothing, just return

            } // end beyond end of interval

        } // end always

        double actualValue = -1.0; // get hold of the actual value of the
        // ValueSupplier. undefined so far

        // notified by a ValueSupplier
        if (arg == null) // update was called with no arg Object
        {
            actualValue = getValue(); // get the value from the ValueSupplier
        } else {
            // update was called from a ValueSupplier
            if (arg instanceof Number) {
                // convert the object given as arg to a double value
                double actVal = convertToDouble(arg);

                // round the value
                actualValue = round(actVal);
            } else {
                // notified by the SimClock
                if (arg instanceof TimeInstant) // time instant passed
                {
                    // get the value from the ValueSupplier
                    actualValue = getValue();

                    // get the time provided by an instant
                    actualTime = (TimeInstant) arg;

                } else {
                    sendWarning("Attempt to update a TimeSeries with an argument arg, "
                    + "that can not be recognized. The attempted action is ignored!",
                                "TimeSeries: " + this.getName() + " Method: update (Observable o, " + "Object arg)",
                                "The passed Object in the argument arg could not be recognized.",
                                "Make sure to pass null, an TimeInstant, a SimTime or a Number object as the arg "
                                + "argument.");

                    return; // do nothing, just return
                }
            }
        }

        // round the time reasonably
        double actTime = round(actualTime.getTimeAsDouble());

        if (hasToWriteToFile) {
            // make the string which will be saved in the file
            String record = actTime + FileOutput.getSeparator() + actualValue;
            _file.writeln(record); // write the String: record to the file
        }

        if (timeValues == null)
            timeValues = new ArrayList<>();
        if (dataValues == null)
            dataValues = new ArrayList<>();
        dataValues.add(new Double(actualValue));
        timeValues.add(new Double(actTime));
        if (plotter != null)
            plotter.update(null, null);

        incrementObservations(); // increment the observations (see Reportable)
        traceUpdate(); // leave a message in the trace
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void update(TimeSpan t) {
        this.setShowTimeSpansInReport(true);
        this.update(t.getTimeAsDouble());
    }

} // end class TimeSeries
