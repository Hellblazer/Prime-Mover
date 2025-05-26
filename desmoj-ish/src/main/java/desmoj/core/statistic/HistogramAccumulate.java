package desmoj.core.statistic;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;

import java.util.Observable;

/**
 * The <code>HistogramAccumulate</code> class is providing a statistic analysis about values. With each update an
 * observed value is changed. An interval is divided into sections with an under- and an overflow section. When a value
 * is updated it will be decided to which section it belongs to and the time where the observed value is in that section
 * will be updated. In the end the report will show how long values belong to which section. <br />
 *
 * @author Chr. M&uuml;ller (TH Wildau) 28.11.12
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */

public class HistogramAccumulate extends desmoj.core.statistic.Accumulate {

    // ****** attributes ******

    /**
     * The array holding the lower limits of the interval sections and the upper limit as the final value.
     */
    private double[] _range;

    /**
     * The array representing the sections of the interval.
     */
    private TimeSpan[] _table;

    private TimeInstant nextToLastTime;
    private Double      nextToLastValue;

    // ****** methods ******

    /**
     * Constructor for a Histogram object with equidistant interval cells that will NOT be connected to a
     * <code>ValueSupplier</code>.
     *
     * @param ownerModel   Model : The model this Histogram is associated to.
     * @param name         java.lang.String : The name of this Histogram object.
     * @param lowerEnd     double : The lower end of the interval.
     * @param upperEnd     double : The upper end of the interval.
     * @param cells        int : The number of cells (sections) the given interval will be divided into.
     * @param showInReport boolean : Flag for showing the report about this Histogram.
     * @param showInTrace  boolean : Flag for showing the trace output of this Histogram.
     */
    public HistogramAccumulate(Model ownerModel, String name, double lowerEnd, double upperEnd, int cells,
                               boolean showInReport, boolean showInTrace) {
        // call the constructor of Accumulate
        super(ownerModel, name, showInReport, showInTrace);

        // check the given parameters and initialize the range array
        checkParamAndInitRange(lowerEnd, upperEnd, cells);

        // initialize the table with zero for each cell
        initTable();
    }

    /**
     * Constructor for a Histogram object with user-defined interval cells that will NOT be connected to a
     * <code>ValueSupplier</code>.
     *
     * @param ownerModel   Model : The model this Histogram is associated to.
     * @param name         java.lang.String : The name of this Histogram object.
     * @param range        double[] : The interval with the lower limit of each cell and the upper limit of the interval
     *                     as the final value.
     * @param showInReport boolean : Flag for showing the report about this Histogram.
     * @param showInTrace  boolean : Flag for showing the trace output of this Histogram.
     */
    public HistogramAccumulate(Model ownerModel, String name, double[] range, boolean showInReport,
                               boolean showInTrace) {

        // call the constructor of Tally
        super(ownerModel, name, showInReport, showInTrace);

        // check the given parameters and initialize the range array
        checkParamAndInitRange(range);

        // initialize the table with zero for each cell
        initTable();
    }

    /**
     * Constructor for a Histogram object with equidistant interval cells that will be connected to a
     * <code>ValueSupplier</code>.
     *
     * @param ownerModel   Model : The model this Histogram is associated to.
     * @param name         java.lang.String : The name of this Histogram object.
     * @param valSup       ValueSupplier : The ValueSupplier providing the value for this Histogram. The given
     *                     ValueSupplier will be observed by this Histogram object.
     * @param automatic    boolean : Flag for observing the ValueSupplier at every tick of the SimClock. Set it to
     *                     <code>true</code> to have it observed at every SimClock tick.
     * @param lowerEnd     double : The lower end of the interval.
     * @param upperEnd     double : The upper end of the interval.
     * @param cells        int : The number of cells (sections) the given interval will be divided into.
     * @param showInReport boolean : Flag for showing the report about this Histogram.
     * @param showInTrace  boolean : Flag for showing the trace output of this Histogram.
     */
    public HistogramAccumulate(Model ownerModel, String name, ValueSupplier valSup, boolean automatic, double lowerEnd,
                               double upperEnd, int cells, boolean showInReport, boolean showInTrace) {
        // call the constructor of Accumulate
        super(ownerModel, name, valSup, automatic, showInReport, showInTrace);

        // valsup is no valid ValueSupplier
        if (valSup == null) {
            sendWarning(
            "Attempt to produce a Histogram about a non existing " + "ValueSupplier. The command will be ignored!",
            "Histogram: " + this.getName() + " Constructor: Histogram"
            + " (Model ownerModel, String name, ValueSupplier valSup, "
            + "double lowerEnd, double upperEnd, int cells, " + "boolean showInReport, boolean showInTrace)",
            "The given ValueSupplier: valSup is only a null pointer.",
            "Make sure to pass a valid ValueSupplier when constructing a new " + "Histogram object.");

            return; // just return
        }

        // check the given parameters and initialize the range array
        checkParamAndInitRange(lowerEnd, upperEnd, cells);

        // initialize the table with zero for each cell
        initTable();
    }

    /**
     * Constructor for a Histogram object with user-defined interval cells that will be connected to a
     * <code>ValueSupplier</code>.
     *
     * @param ownerModel   Model : The model this Histogram is associated to.
     * @param name         java.lang.String : The name of this Histogram object.
     * @param valSup       ValueSupplier : The ValueSupplier providing the value for this Histogram. The given
     *                     ValueSupplier will be observed by this Histogram object.
     * @param automatic    boolean : Flag for observing the ValueSupplier at every tick of the SimClock. Set it to
     *                     <code>true</code> to have it observed at every SimClock tick.
     * @param range        double[] : The interval with the lower limit of each cell and the upper limit of the interval
     *                     as the final value.
     * @param showInReport boolean : Flag for showing the report about this Histogram.
     * @param showInTrace  boolean : Flag for showing the trace output of this Histogram.
     */
    public HistogramAccumulate(Model ownerModel, String name, ValueSupplier valSup, boolean automatic, double[] range,
                               boolean showInReport, boolean showInTrace) {

        // call the constructor of Tally
        super(ownerModel, name, valSup, automatic, showInReport, showInTrace);

        // valsup is no valid ValueSupplier
        if (valSup == null) {
            sendWarning(
            "Attempt to produce a Histogram about a non existing " + "ValueSupplier. The command will be ignored!",
            "Histogram: " + this.getName() + " Constructor: Histogram"
            + " (Model ownerModel, String name, ValueSupplier valSup, "
            + "double lowerEnd, double upperEnd, int cells, " + "boolean showInReport, boolean showInTrace)",
            "The given ValueSupplier: valSup is only a null pointer.",
            "Make sure to pass a valid ValueSupplier when constructing a new " + "Histogram object.");

            return; // just return
        }

        // check the given parameters and initialize the range array
        checkParamAndInitRange(range);

        // initialize the table with zero for each cell
        initTable();
    }

    /**
     * Changes the parameters of the interval and its number of segments. Can only be done after construction of a
     * Histogram or after a reset.
     *
     * @param low double : The lower end of the interval.
     * @param up  double : The upper end of the interval.
     * @param cel int : The number of cells (sections) the given interval will be divided into.
     */
    public void changeParameters(double low, double up, int cel) {
        if (getObservations() > 0) // Histogram has been used already
        {
            sendWarning("Attempt to change the parameters of a Histogram, but "
                        + "it has been used already. The command will be ignored!",
                        "Histogram: " + this.getName() + " Method: changeParameters( double "
                        + "low, double up, int cel )",
                        "The parameters of a Histogram can not be changed when the Histogram "
                        + "has been used already.",
                        "Make sure to reset the Histogram right before changing its " + "parameters.");

            return; // do nothing, just return
        }

        // check the given parameters and initialize the range array
        this.checkParamAndInitRange(low, up, cel);

        // initialize the table with zero for each cell
        initTable();
    }

    /**
     * Changes the parameters of the interval and its number of segments. Can only be done after construction of a
     * Histogram or after a reset.
     *
     * @param range double[] : The array holding the lower limits of the interval sections and the upper limit as the
     *              final value.
     */
    public void changeParameters(double[] range) {
        if (getObservations() > 0) // Histogram has been used already
        {
            sendWarning("Attempt to change the parameters of a Histogram, but "
                        + "it has been used already. The command will be ignored!",
                        "Histogram: " + this.getName() + " Method: changeParameters( double "
                        + "low, double up, int cel )",
                        "The parameters of a Histogram can not be changed when the Histogram "
                        + "has been used already.",
                        "Make sure to reset the Histogram right before changing its " + "parameters.");

            return; // do nothing, just return
        }

        // check the given parameters and initialize the range array
        this.checkParamAndInitRange(range);

        // initialize the table with zero for each cell
        initTable();
    }

    /**
     * Returns a Reporter to produce a report about this Histogram.
     *
     * @return desmoj.report.Reporter : The Reporter for this Histogram.
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {
        return new desmoj.core.report.HistogramAccumulateReporter(this);
    }

    /**
     * Returns the number of cells the interval is divided into.
     *
     * @return int : The number of cells the interval is divided into.
     */
    public int getCells() {
        return this._range.length - 1;
    }

    /**
     * Returns the lower limit of the given cell. If the given cell is negative,
     * <code>UNDEFINED</code> (-1) will be returned.
     *
     * @param cell int : The cell for which we want to know its lower limit. Should be zero or positive.
     * @return double : The lower limit of the given cell.
     */
    public double getLowerLimit(int cell) {
        if (cell < 0 || cell > this.getCells() + 1) {
            sendWarning("Attempt to get a lower limit of a not known cell. " + "UNDEFINED (-1) will be returned!",
                        "Histogram: " + this.getName() + " Method: getLowerLimit( int cell ).",
                        "The passed int: cell in this method is negative or greater than " + "the largest cell number.",
                        "Make sure to ask the lower limit only for valid cell numbers.");

            return UNDEFINED; // return UNDEFINED (-1)
        }

        if (cell == 0) {
            return Double.NEGATIVE_INFINITY;
        } else {
            return StatisticObject.round(this._range[cell - 1]);
        }
    }

    /**
     * Returns the mean width of all cells.
     *
     * @return double : The mean width of all cells.
     */
    public double getMeanWidth() {
        return round((this.getUpperLimit() - this.getLowerLimit(1)) / this.getCells());
    }

    /**
     * Returns the numbers of the most frequented cells, so far.
     *
     * @return int[] : An array with the numbers of the most frequented cells.
     */
    public int[] getMostFrequentedCells() {
        TimeSpan max = new TimeSpan(0); // max so far
        int numMaxCells = 0;
        int[] maxCellNo = new int[this.getCells() + 2];

        for (int i = 0; i < this.getCells() + 2; i++) // go through all cells
        {
            if (TimeSpan.isEqual(max, this._table[i])) {
                numMaxCells++;
                maxCellNo[numMaxCells - 1] = i;
            } else if (TimeSpan.isShorter(max, this._table[i])) // new maximum frequency found?
            {
                maxCellNo = new int[this.getCells()];
                numMaxCells = 1;
                maxCellNo[numMaxCells - 1] = i;
                max = this._table[i]; // set the new highest frequency
            }
        }

        int[] returnMaxCellNo = new int[numMaxCells];
        for (int i = 0; i < numMaxCells; i++) {
            returnMaxCellNo[i] = maxCellNo[i];
        }
        return returnMaxCellNo;
    }

    /**
     * Returns the observations made for the given cell, so far.
     *
     * @param cell int : The cell of which want to get the number of observations made for.
     * @return long : The observations made for the given cell.
     */
    public TimeSpan getObservationsInCell(int cell) {
        TimeSpan out = new TimeSpan(0);
        if (cell < 0 || cell > this.getCells() + 1) { // cell 0: underflow, cell this.getCells() + 1: overflow
            sendWarning(
            "Attempt to get the number of observations from a not " + "known cell. Zero (0) will be returned!",
            "Histogram: " + this.getName() + " Method: getObservationsInCell" + "( int cell ).",
            "The passed int: cell in this method is negative or greater than " + "the largest cell number.",
            "Make sure to ask for the number of observations only for valid " + "cell numbers.");
            return out; // return zero (0)
        }
        out = _table[cell];
        // reflect the period since the last update
        if (this.nextToLastValue != null) {
            int n = this.findCellIndex(this.nextToLastValue);
            if (cell == n) {
                TimeSpan delta = TimeOperations.diff(this.presentTime(), this.nextToLastTime);
                out = TimeOperations.add(_table[cell], delta);
            }
        }
        return out;
    }

    /**
     * Returns the upper limit of the whole interval.
     *
     * @return double : The upper limit of the whole interval.
     */
    public double getUpperLimit() {
        return this._range[this._range.length - 1];
    }

    /**
     * Resets this Histogram object by resetting the counters for each cell to zero. That means the array of the cell
     * counters will be reset, but the interval and the number of sections this interval is divided into will remain the
     * same. The parameters of the interval can be changed with the
     * <code>changeParameters</code> method after the reset.
     */
    @Override
    public void reset() {
        super.reset(); // reset the Tally, too.

        // reset the array of cells #### only if the table already exists! Ruth
        // 24/01/2008
        if (_table != null) {
            initTable();
        }
    }

    /**
     * Updates this <code>Histogram</code> object by fetching the actual value of the <code>ValueSupplier</code> and
     * processing it. The
     * <code>ValueSupplier</code> is passed in the constructor of this
     * <code>Histogram</code> object. This <code>update()</code> method complies
     * with the one described in DESMO, see [Page91].
     */
    @Override
    public void update() {
        super.update(); // update Accumulate

        if (this.nextToLastValue != null) {
            int n = this.findCellIndex(this.nextToLastValue);
            TimeSpan delta = TimeOperations.diff(this.presentTime(), this.nextToLastTime);
            _table[n] = TimeOperations.add(_table[n], delta);
        }
        this.nextToLastTime = this.presentTime();
        this.nextToLastValue = this.getLastValue();
    }

    /**
     * Updates this <code>Histogram</code> object with the double value given as parameter. In some cases it might be
     * more convenient to pass the value this
     * <code>Histogram</code> will be updated with directly within the
     * <code>update(double val)</code> method instead of going via the
     * <code>ValueSupplier</code>.
     *
     * @param val double : The value with which this <code>Histogram</code> will be updated.
     */
    @Override
    public void update(double value) {
        super.update(value); // update Accumulate

        if (this.nextToLastValue != null) {
            int n = this.findCellIndex(this.nextToLastValue);
            TimeSpan delta = TimeOperations.diff(this.presentTime(), this.nextToLastTime);
            _table[n] = TimeOperations.add(_table[n], delta);
        }
        this.nextToLastTime = this.presentTime();
        this.nextToLastValue = value;

    }

    /**
     * Implementation of the virtual <code>update(Observable, Object)</code> method of the <code>Observer</code>
     * interface. This method will be called automatically from an <code>Observable</code> object within its
     * <code>notifyObservers()</code> method. <br>
     * If no Object (a<code>null</code> value) is passed as arg, the actual value of the ValueSupplier will be fetched
     * with the <code>value()</code> method of the ValueSupplier. Otherwise it is expected that the actual value is
     * passed in the Object arg.
     *
     * @param o   java.util.Observable : The Observable calling this method within its own
     *            <code>notifyObservers()</code> method.
     * @param arg Object : The Object with which this <code>Tally</code> is updated. Normally a double number which is
     *            added to the statistics or
     *            <code>null</code>.
     */
    @Override
    public void update(Observable o, Object arg) {
        if (o == null) // null was passed instead of an Observable
        {
            sendWarning("Attempt to update a Histogram with no reference to an " + "Observable. The actual value of '"
                        + getValueSupplier().getName() + "' will be fetched and processed anyway.",
                        "Histogram: " + this.getName() + " Method: update (Observable " + "o, Object arg)",
                        "The passed Observable: o in this method is only a null pointer.",
                        "The update()-method was not called via notifyObservers() from an "
                        + "Observable. Who was calling it? Why don't you let the Observable do" + " the work?");
        }

        super.update(o, arg); // update Accumulate

        if (this.nextToLastValue != null) {
            int n = this.findCellIndex(this.nextToLastValue);
            TimeSpan delta = TimeOperations.diff(this.presentTime(), this.nextToLastTime);
            _table[n] = TimeOperations.add(_table[n], delta);
        }
        this.nextToLastTime = this.presentTime();
        this.nextToLastValue = this.getLastValue();
    }

    /**
     * Checks the segmentation of the given equidistant interval and then initializes the range array.
     */
    protected void checkParamAndInitRange(double lowerLimit, double upperLimit, int cells) {
        if (cells <= 0) // the interval will not be divided into segments
        {
            sendWarning("Attempt to produce a Histogram about an interval, "
                        + "which is not divided into segments. The number of segments will be " + "set to one!",
                        "Histogram: " + this.getName() + " Constructor: Histogram"
                        + " (Model ownerModel, String name, ... int cells, ...) "
                        + "or Method: changeParameters( ..., int cel ).",
                        "The given number of cells is zero or negative.",
                        "Make sure to pass a valid number of cells when constructing a new "
                        + "Histogram object or changing its parameters.");

            cells = 1;
        }

        if (lowerLimit > upperLimit) // lowerLimit is greater than upperLimit
        {
            sendWarning("Attempt to produce a Histogram about an interval, "
                        + "which lower end is greater than its upper end. The lower and " + "upper end are exchanged!",
                        "Histogram: " + this.getName() + " Constructor: Histogram"
                        + " (Model ownerModel, ... double lowerEnd, double upperEnd, ...) "
                        + "or Method: changeParameters( double low, double up, int cel ).",
                        "The given lower end is greater than the upper end of the interval.",
                        "Make sure that the lower limit of the interval is smaller than the "
                        + "upper limit when constructing a new Histogram object or changing " + "its parameters.");

            double temp = lowerLimit;
            lowerLimit = upperLimit;
            upperLimit = temp;
        } else {
            // there is no segmentation (all is one segment)
            if (lowerLimit == upperLimit && cells != 1) {
                sendWarning("Attempt to produce a Histogram about an interval, "
                            + "which lower and upper limit are the same. The number of cells is " + "set to one!",
                            "Histogram: " + this.getName() + " Constructor: Histogram"
                            + " (Model ownerModel, ... double lowerEnd, double upperEnd, ...) "
                            + "or Method: changeParameters( double low, double up, int cel ).",
                            "The given lower and upper end of the interval are the same, but "
                            + "the interval is divided into more than one segment.",
                            "Make sure that in case the lower and upper limit of the interval "
                            + "are the same the number of cells is one.");

                cells = 1;

            } // end inner if
        } // end else
        // Init range array
        this._range = new double[cells + 1];
        for (int i = 0; i < this._range.length - 1; i++)
            this._range[i] = lowerLimit + i * (upperLimit - lowerLimit) / cells;
        this._range[this._range.length - 1] = upperLimit;
    }

    /**
     * Checks the segmentation of the given user-defined interval and then initializes the range array.
     */
    protected void checkParamAndInitRange(double[] range) {
        double temp;
        if (range.length == 0 || range.length == 1) // the interval will not be divided into segments of length > 0
        {
            temp = 0;
            sendWarning("Attempt to produce a Histogram only containing of the underflow and the overflow. "
                        + "There will an interval of the length of 0 between these. The number of segments will be "
                        + "set to one!", "Histogram: " + this.getName() + " Constructor: Histogram"
                        + " (Model ownerModel, String name, double[] range, ...) "
                        + "or Method: changeParameters( double[] range ).", "There are no given cells. ",
                        "Make sure to pass a valid number of cells when constructing a new "
                        + "Histogram object or changing its parameters.");

            // correct and initialize the range array
            if (range.length == 1) {
                temp = range[0];
            }
            range = new double[2];
            range[0] = range[1] = temp;
        }

        // check the order of the given limits
        boolean errorMsgAsc = false;
        // using bubblesort as we can expect the given array to be reasonably sorted
        for (int i = 0; i < range.length - 1; i++) {
            for (int j = i + 1; j < range.length; j++) {
                if (range[i] > range[j]) {
                    errorMsgAsc = true;
                    temp = range[i];
                    range[i] = range[j];
                    range[j] = temp;
                }
            }
        }
        if (errorMsgAsc) // the limits were not ascending
        {
            sendWarning("Attempt to produce a Histogram about an interval, "
                        + "which has at least one section whose upper is smaller than its lower limit. "
                        + "The lower and upper limits are being exchanged!",
                        "Histogram: " + this.getName() + " Constructor: Histogram"
                        + " (Model ownerModel, ... double[] range, ...) "
                        + "or Method: changeParameters( double[] range ).",
                        "The given lower end is greater than the upper end of the interval.",
                        "Make sure to have a strictly ascending order "
                        + "when constructing a new Histogram object or changing " + "its parameters.");
        }

        // check for equal entries
        boolean errorMsgEq = false;
        double[] rangetemp = new double[range.length];
        int count = 0;
        for (int i = 0; i < range.length - 2; i++) {
            if (!(range[i] == range[i + 1])) {
                rangetemp[count] = range[i];
                count++;
            } else {
                errorMsgEq = true;
            }
        }
        rangetemp[count] = range[range.length - 2];
        count++;
        rangetemp[count] = range[range.length - 1];
        range = new double[count + 1];
        for (int i = 0; i < range.length; i++) {
            range[i] = rangetemp[i];
        }
        if (errorMsgEq) {
            sendWarning("Attempt to produce a Histogram about an interval, "
                        + "which has at least two sections in a row whose lower limits are the same. At least one "
                        + "lower limit is being deleted!", "Histogram: " + this.getName() + " Constructor: Histogram"
                        + " (Model ownerModel, ... double[] range, ...) "
                        + "or Method: changeParameters( double[] range ).",
                        "The given interval has at least two sections with the same lower limit. ",
                        "Make sure to have a strictly ascending order "
                        + "when constructing a new Histogram object or changing " + "its parameters.");
        }

        // Init range array;
        _range = range;

    }

    /**
     * Initializes the table by setting each cell counter to zero.
     */
    protected void initTable() {

        TimeSpan zero = new TimeSpan(0);

        // make a new table; each cell is set to zero
        this._table = new TimeSpan[this.getCells() + 2];
        for (int i = 0; i < this._table.length; i++)
            this._table[i] = zero;
        this.nextToLastTime = null;
        this.nextToLastValue = null;
    }

    private int findCellIndex(double val) {
        int n = 0; // to which cell does the value belong to?
        if (val < this._range[0]) {
            n = 0; // underflow
            // }else if(val > this._range[this._range.length - 1]){ // overflow
        } else if (val >= this._range[this._range.length - 1]) { // overflow
            n = this.getCells() + 1;
            /*
             * }else if(val >= this._range[this._range.length - 2] && val <=
             * this._range[this._range.length - 1]) { // last segment n = this.getCells();
             */
        } else {
            for (int i = 0; i < this._range.length - 1; i++) {
                if (val >= this._range[i] && val < this._range[i + 1]) {
                    n = i + 1;
                }
            }
        }
        return n;
    }

} // end class Histogram
