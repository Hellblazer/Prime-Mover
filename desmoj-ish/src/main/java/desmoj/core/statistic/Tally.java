package desmoj.core.statistic;

import java.util.Observable;

import desmoj.core.report.TallyReporter;
import desmoj.core.simulator.Model;

/**
 * The <code>Tally</code> class is providing a statistic analysis about one
 * value. The mean value and the standard deviation is calculated on basis of
 * the total number of observations. <br />
 *
 * @see TallyRunning
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

public class Tally extends desmoj.core.statistic.ValueStatistics {

    // ****** attributes ******

    /**
     * The mean of all values so far
     */
    private double _mean;

    /**
     * The sum of the squares of the differences from the mean of all values so far
     */
    private double _sumOfSquaredDevsFromMean;

    /**
     * Constructor for a Tally object that has no connection to a
     * <code>ValueSupplier</code>.
     *
     * @param ownerModel   Model : The model this Tally is associated to
     * @param name         java.lang.String : The name of this Tally object
     * @param showInReport boolean : Flag for showing the report about this Tally.
     * @param showInTrace  boolean : Flag for showing the trace output of this
     *                     Tally.
     */
    public Tally(Model ownerModel, String name, boolean showInReport, boolean showInTrace) {
        // call the constructor of ValueStatistics
        super(ownerModel, name, showInReport, showInTrace);

        this._mean = this._sumOfSquaredDevsFromMean = 0.0;
    }

    // ****** methods ******

    /**
     * Constructor for a Tally object that is connected to a
     * <code>ValueSupplier</code>.
     *
     * @param ownerModel   Model : The model this Tally is associated to
     * @param name         java.lang.String : The name of this Tally object
     * @param valSup       ValueSupplier : The ValueSupplier providing the value for
     *                     this Tally. The given ValueSupplier will be observed by
     *                     this Tally object.
     * @param showInReport boolean : Flag for showing the report about this Tally.
     * @param showInTrace  boolean : Flag for showing the trace output of this
     *                     Tally.
     */
    public Tally(Model ownerModel, String name, ValueSupplier valSup, boolean showInReport, boolean showInTrace) {
        // call the constructor of ValueStatistics
        super(ownerModel, name, valSup, showInReport, showInTrace);

        // valSup is no valid ValueSupplier
        if (valSup == null) {
            sendWarning("Attempt to produce a Tally about a non existing "
            + "ValueSupplier. The command will be ignored!",
                        "Tally: " + this.getName() + " Constructor: Tally"
                        + " (Model ownerModel, String name, ValueSupplier valSup, "
                        + "boolean showInReport, boolean showInTrace)",
                        "The given ValueSupplier: valSup is only a null pointer.",
                        "Make sure to pass a valid ValueSupplier when constructing a new " + "Tally object.");

            return; // just return
        }

        this._mean = this._sumOfSquaredDevsFromMean = 0.0;
    }

    /**
     * Returns a Reporter to produce a report about this Tally.
     *
     * @return desmoj.report.Reporter : The Reporter for this Tally.
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {
        TallyReporter result = new TallyReporter(this);
        return result;
    }

    /**
     * Returns the mean value of all the values observed so far.
     *
     * @return double : The mean value of all the values observed so far.
     */
    @Override
    public double getMean() {
        if (getObservations() == 0) {
            sendWarning("Attempt to get a mean value, but there is not "
            + "sufficient data yet. UNDEFINED (-1.0) will be returned!",
                        "Tally: " + this.getName() + " Method: double getMean()",
                        "You can not calculate a mean value as long as no data is collected.",
                        "Make sure to ask for the mean value only after some data has been " + "collected already.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        // return the rounded mean value
        return round(_mean);
    }

    /**
     * Returns the standard deviation of all the values observed so far.
     *
     * @return double : The standard deviation of all the values observed so far.
     */
    @Override
    public double getStdDev() {
        long n = getObservations();

        if (n < 2) {
            sendWarning("Attempt to get a standard deviation, but there is not "
            + "sufficient data yet. UNDEFINED (-1.0) will be returned!",
                        "Tally: " + this.getName() + " Method: double getStdDev()",
                        "A standard deviation can not be calculated as long as no data is " + "collected.",
                        "Make sure to ask for the standard deviation only after some data "
                        + "has been collected already.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        // calculate the standard deviation
        double stdDev = Math.sqrt(_sumOfSquaredDevsFromMean / (n - 1));

        // return the rounded standard deviation
        return round(stdDev);
    }

    /**
     * Resets this Tally object by resetting all variables to 0.0 .
     */
    @Override
    public void reset() {
        super.reset(); // reset the ValueStatistics, too.
        this._mean = this._sumOfSquaredDevsFromMean = 0.0;
    }

    /**
     * Updates this <code>Tally</code> object by fetching the actual value of the
     * <code>ValueSupplier</code> and processing it. The <code>ValueSupplier</code>
     * is passed in the constructor of this <code>Tally</code> object. This
     * <code>update()</code> method complies with the one described in DESMO, see
     * [Page91].
     */
    @Override
    public void update() {
        super.update(); // call the update() method of ValueStatistics

        double lastVal = getLastValue();
        this.internalUpdate(lastVal);
    }

    /**
     * Updates this <code>Tally</code> object with the double value given as
     * parameter. In some cases it might be more convenient to pass the value this
     * <code>Tally</code> will be updated with directly within the
     * <code>update(double val)</code> method instead of going via the
     * <code>ValueSupplier</code>.
     *
     * @param val double : The value with which this <code>Tally</code> will be
     *            updated.
     */
    @Override
    public void update(double val) {
        // call the update(double val) method of ValueStatistics
        super.update(val);
        this.internalUpdate(val);
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
     * @param arg Object : The Object with which this <code>Tally</code> is updated.
     *            Normally a double number which is added to the statistics or
     *            <code>null</code>.
     */
    @Override
    public void update(Observable o, Object arg) {
        if (o == null) // null was passed instead of an Observable
        {
            sendWarning("Attempt to update a Tally with no reference to an " + "Observable. The actual value of '"
            + getValueSupplier().getName() + "' will be fetched and processed anyway.",
                        "Tally: " + this.getName() + " Method: update (Observable " + "o, Object arg)",
                        "The passed Observable: o in this method is only a null pointer.",
                        "The update()-method was not called via notifyObservers() from an "
                        + "Observable. Who was calling it? Why don't you let the Observable do" + " the work?");
        }

        super.update(o, arg); // call the update() method of ValueStatistics
        this.internalUpdate(getLastValue());
    }

    /**
     * Internal method to update the mean and sum of the squares of the differences
     * from the mean of values so far with a new sample.
     *
     * @param value double : The new sample.
     */
    private void internalUpdate(double value) {
        if (this.getObservations() == 1) { // First entry
            _mean = value;
            _sumOfSquaredDevsFromMean = 0.0;
        } else { // Further entries
            double _m_old = _mean;
            _mean += (value - _mean) / this.getObservations();
            _sumOfSquaredDevsFromMean += (value - _m_old) * (value - _mean);
        }
    }

} // end class Tally
