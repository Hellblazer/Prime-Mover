package desmoj.core.statistic;

import java.util.Observable;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeSpan;

/**
 * The <code>ValueStatistics</code> class is the super class for all the classes
 * providing a statistic analysis about one value (e.g. minimum and maximum
 * values). Derived classes are: Tally, Accumulate and Histogram (because it is
 * derived from Tally).
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen
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

public abstract class ValueStatistics extends desmoj.core.statistic.StatisticObjectSupportingTimeSpans {

    // ****** attributes ******

    /**
     * The last value we got from the ValueSupplier
     */
    private double _lastValue;

    /**
     * The maximum of all values so far
     */
    private double _max;

    /**
     * The minimum of all values so far
     */
    private double _min;

    /**
     * The ValueSupplier providing the values which will be processed.
     */
    private ValueSupplier _valSuppl;

    // ****** methods ******

    /**
     * Constructor for a ValueStatistics object that has NO connection to a
     * <code>ValueSupplier</code>.
     *
     * @param ownerModel   Model : The model this ValueStatistics is associated to
     * @param name         java.lang.String : The name of this ValueStatistics
     *                     object
     * @param showInReport boolean : Flag for showing the report about this
     *                     ValueStatistics.
     * @param showInTrace  boolean : Flag for showing the trace output of this
     *                     ValueStatistics.
     */
    public ValueStatistics(Model ownerModel, String name, boolean showInReport, boolean showInTrace) {
        super(ownerModel, name, showInReport, showInTrace);

        // no ValueSupplier will be observed
        this._valSuppl = null;
        this._max = Double.NaN;
        this._min = Double.NaN;
        this._lastValue = Double.NaN;

    }

    /**
     * Constructor for a ValueStatistics object that will be connected to a
     * <code>ValueSupplier</code>.
     *
     * @param ownerModel   Model : The model this ValueStatistics is associated to
     * @param name         java.lang.String : The name of this ValueStatistics
     *                     object
     * @param valSup       ValueSupplier : The ValueSupplier providing the value for
     *                     this ValueStatistics. The given ValueSupplier will be
     *                     observed by this ValueStatistics object.
     * @param showInReport boolean : Flag for showing the report about this
     *                     ValueStatistics.
     * @param showInTrace  boolean : Flag for showing the trace output of this
     *                     ValueStatistics.
     */
    public ValueStatistics(Model ownerModel, String name, ValueSupplier valSup, boolean showInReport,
                           boolean showInTrace) {
        super(ownerModel, name, showInReport, showInTrace);

        // valSup is no valid ValueSupplier
        if (valSup == null) {
            sendWarning("Attempt to produce a ValueStatistics about a non "
            + "existing ValueSupplier. The command will be ignored!",
                        "ValueStatistics: " + this.getName() + " Constructor: ValueStatistics"
                        + " (Model ownerModel, String name, ValueSupplier valSup, "
                        + "boolean showInReport, boolean showInTrace)",
                        "The given ValueSupplier: valSup is only a null pointer.",
                        "Make sure to pass a valid ValueSupplier when constructing a new " + "ValueStatistics object.");

            return; // just return
        }

        this._valSuppl = valSup;
        this._max = Double.NaN;
        this._min = Double.NaN;
        this._lastValue = Double.NaN;

        // this ValueStatistics will observe the valSuppl
        _valSuppl.addObserver(this);
    }

    /**
     * Returns the last observed value of this ValueStatistics object.
     *
     * @return double : The last value observed so far.
     */
    public double getLastValue() {
        return this._lastValue;
    }

    /**
     * Returns the maximum value observed so far.
     *
     * @return double : The maximum value observed so far.
     */
    public double getMaximum() {

        if (getObservations() == 0) {
            sendWarning("Attempt to get a maximum value, but there is not "
            + "sufficient data yet. UNDEFINED (-1.0) will be returned!",
                        "ValueStatistics: " + this.getName() + " Method: double getMaximum()",
                        "You can not obtain a maximum value as long as no data is collected.",
                        "Make sure to ask for a maximum value only after some data has been " + "collected already.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        // return the rounded maximum
        return round(this._max);
    }

    /**
     * Returns the mean value of all the values observed so far. Has to be
     * implemented in a derived class.
     *
     * @return double : The mean value of all the values observed so far.
     */
    public abstract double getMean();

    /**
     * Returns the minimum value observed so far.
     *
     * @return double : The minimum value observed so far.
     */
    public double getMinimum() {

        if (getObservations() == 0) {
            sendWarning("Attempt to get a minimum value, but there is not "
            + "sufficient data yet. UNDEFINED (-1.0) will be returned!",
                        "ValueStatistics: " + this.getName() + " Method: double getMinimum()",
                        "You can not obtain a minimum value as long as no data is collected.",
                        "Make sure to ask for a minimum value only after some data has been " + "collected already.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        // return the rounded minimum
        return round(this._min);
    }

    /**
     * Returns the standard deviation of all the values observed so far. Has to be
     * implemented in a derived class.
     *
     * @return double : The standard deviation of all the values observed so far.
     */
    public abstract double getStdDev();

    /**
     * Resets this ValueStatistics object by resetting all variables to 0.0 .
     */
    @Override
    public void reset() {
        super.reset(); // reset the StatisticObject, too.

        this._min = this._max = this._lastValue = Double.NaN;
    }

    /**
     * Updates this <code>ValueStatistics</code> object by fetching the actual value
     * of the <code>ValueSupplier</code> and processing it. The
     * <code>ValueSupplier</code> is passed in the constructor of this
     * <code>ValueStatistics</code> object. This <code>update()</code> method
     * complies with the one described in DESMO, see [Page91].
     */
    public void update() {
        if (this._valSuppl == null) {
            sendWarning("Attempt to update a ValueStatistics that is not "
            + "connected to a ValueSupplier. No value is provided with which "
            + "the statistic could be updated. The command will be ignored!",
                        "ValueStatistics: " + this.getName() + " Method: update()",
                        "The given ValueSupplier: valSuppl is only a null pointer.",
                        "Make sure to update a ValueStatistics only when it is connected "
                        + "to a valid ValueSupplier. Or use the update(double val) method.");

            return; // that's it
        }

        // get the actual value from the ValueSupplier
        _lastValue = _valSuppl.value();

        incrementObservations(); // use the method from the Reportable class

        if (getObservations() <= 1) // the first observation?
        {
            _min = _max = _lastValue; // update min and max
        }

        if (_lastValue < _min) {
            _min = _lastValue; // update min
        }

        if (_lastValue > _max) {
            _max = _lastValue; // update max
        }

        traceUpdate(); // leave a message in the trace
    }

    /**
     * Updates this <code>ValueStatistics</code> object with the double value given
     * as parameter. In some cases it might be more convenient to pass the value
     * this <code>ValueStatistics</code> will be updated with directly within the
     * <code>update(double val)</code> method instead of going via the
     * <code>ValueSupplier</code>.
     *
     * @param val double : The value with which this <code>ValueStatistics</code>
     *            will be updated.
     */
    public void update(double val) {
        _lastValue = val; // update lastValue

        incrementObservations(); // use the method from the Reportable class

        if (getObservations() <= 1) // the first onbservation?
        {
            _min = _max = _lastValue; // update min and max
        }

        if (_lastValue < _min) {
            _min = _lastValue; // update min
        }

        if (_lastValue > _max) {
            _max = _lastValue; // update max
        }

        traceUpdate(); // leave a message in the trace
    }

    /**
     * Implementation of the virtual <code>update(Observable, Object)</code> method
     * of the <code>Observer</code> interface. This method will be called
     * automatically from an <code>Observable</code> object within its
     * <code>notifyObservers()</code> method. <br>
     * If no Object (a<code>null</code> value) is passed as arg, the actual value of
     * the ValueSupplier will be fetched with the <code>value()</code> method of the
     * ValueSupplier. Otherwise it is expected that the actual value is passed in
     * the Object arg.
     *
     * @param o   java.util.Observable : The Observable calling this method within
     *            its own <code>notifyObservers()</code> method.
     * @param arg Object : The Object with which this <code>ValueStatistics</code>
     *            is updated. Normally a double number or TimeSpan which is added to
     *            the statistics or <code>null</code>.
     */
    @Override
    public void update(Observable o, Object arg) {
        // update was called with no arg Object OR from the SimClock
        if (arg == null || o instanceof SimClock) {
            if (_valSuppl == null) {
                sendWarning("Attempt to update a ValueStatistics that is not "
                + "connected to a ValueSupplier. No value is provided with which "
                + "the statistic could be updated. The command will be ignored!",
                            "ValueStatistics: " + this.getName() + " Method: update " + "(Observable o, Object arg)",
                            "The given ValueSupplier: valSuppl is only a null pointer.",
                            "Make sure to update a ValueStatistics only when it is connected "
                            + "to a valid ValueSupplier. Or use the update(double val) method.");

                return; // that's it
            }

            // get the actual value from the ValueSupplier
            _lastValue = _valSuppl.value();

            incrementObservations(); // use the method from the Reportable
            // class
        } else {
            if (arg instanceof Number) {
                // get the value out of the Object arg
                _lastValue = convertToDouble(arg);
                incrementObservations(); // use the method from the
                // Reportable class
            } else if (arg instanceof TimeSpan) {
                // get the value out of the Object arg
                _lastValue = ((TimeSpan) arg).getTimeAsDouble();
                incrementObservations(); // use the method from the
                // Reportable class
            } else {
                sendWarning("Attempt to update a ValueStatistics with an argument "
                + "arg, that can not be recognized. The attempted action is ignored!",
                            "ValueStatistics: " + this.getName() + " Method: update (Observable " + "o, Object arg)",
                            "The passed Object in the argument arg could not be recognized.",
                            "Make sure to pass null or a Number object as the arg argument.");

                return; // do nothing, just return
            }
        }

        if (getObservations() <= 1) // the first observation?
        {
            _min = _max = _lastValue; // update min and max
        }

        if (_lastValue < _min) {
            _min = _lastValue; // update min
        }

        if (_lastValue > _max) {
            _max = _lastValue; // update max
        }

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

    /**
     * Returns the ValueSupplier object providing all the values.
     *
     * @return ValueSupplier : The ValueSupplier object providing the values for
     *         this ValueStatistics.
     */
    protected ValueSupplier getValueSupplier() {
        return _valSuppl;
    }

} // end class ValueStatistics
