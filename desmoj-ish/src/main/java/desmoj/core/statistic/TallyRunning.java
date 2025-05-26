package desmoj.core.statistic;

import desmoj.core.simulator.Model;

import java.util.Observable;

/**
 * The <code>TallyRunning</code> class is extended from <code>Tally</code>. Same as <code>Tally</code>, it provides
 * statistic analysis about one value (mean, standard deviation, minimum, maximum). In addition, this class is able to
 * calculate this data based on the last <i>n</i> samples only, subject to
 * <i>n</i> passed to the constructor.
 *
 * @author Soenke Claassen, Johannes G&ouml;bel
 * @author based on DESMO-C from Thomas Schniewind, 1998
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @see Tally
 */

public class TallyRunning extends desmoj.core.statistic.Tally {

    // ****** attributes ******

    /**
     * Number of samples to include in running statistic data
     */
    private int _n;

    /**
     * The sum of the last n values so far
     */
    private double _sumLastN;

    /**
     * The sum of the last n values so far
     */
    private double _sumSquareLastN;

    /**
     * The last n values
     */
    private double[] _valuesLastN;

    /**
     * Constructor for a Tally object that has no connection to a
     * <code>ValueSupplier</code>.
     *
     * @param ownerModel   Model : The model this Tally is associated to
     * @param name         java.lang.String : The name of this Tally object
     * @param n            int : Number of samples to include in running statistic data.
     * @param showInReport boolean : Flag for showing the report about this Tally.
     * @param showInTrace  boolean : Flag for showing the trace output of this Tally.
     */
    public TallyRunning(Model ownerModel, String name, int n, boolean showInReport, boolean showInTrace) {

        // call the constructor of Tally
        super(ownerModel, name, showInReport, showInTrace);

        // n is less than 2
        if (n < 2) {
            sendWarning("Attempt to produce a TallyRunning based on the last n=" + +n
                        + " samples. Sample size will be increased to 2!",
                        "TallyRunning: " + this.getName() + " Constructor: TallyRunning"
                        + " (Model ownerModel, String name, ValueSupplier valSup, "
                        + "int n, boolean showInReport, boolean showInTrace)", "The given sample size is too small.",
                        "Make sure to pass a sample size greater or equal to 2 to a new " + "TallyRunning object.");

            n = 2;
        }

        // initialize storage of last n values
        this._n = n;
        this._valuesLastN = new double[n];
        this._sumLastN = 0;
        this._sumSquareLastN = 0;
    }

    // ****** methods ******

    /**
     * Constructor for a TallyRunning object that is connected to a
     * <code>ValueSupplier</code>.
     *
     * @param ownerModel   Model : The model this Tally is associated to
     * @param name         java.lang.String : The name of this Tally object
     * @param valSup       ValueSupplier : The ValueSupplier providing the value for this Tally. The given ValueSupplier
     *                     will be observed by this Tally object.
     * @param n            int : Number of samples to include in running statistic data.
     * @param showInReport boolean : Flag for showing the report about this Tally.
     * @param showInTrace  boolean : Flag for showing the trace output of this Tally.
     */
    public TallyRunning(Model ownerModel, String name, ValueSupplier valSup, int n, boolean showInReport,
                        boolean showInTrace) {

        // call the constructor of Tally
        super(ownerModel, name, valSup, showInReport, showInTrace);

        // n is less than 2
        if (n < 2) {
            sendWarning("Attempt to produce a TallyRunning based on the last n=" + +n
                        + " samples. Sample size will be increased to 2!",
                        "TallyRunning: " + this.getName() + " Constructor: TallyRunning"
                        + " (Model ownerModel, String name, ValueSupplier valSup, "
                        + "int n, boolean showInReport, boolean showInTrace)", "The given sample size is too small.",
                        "Make sure to pass a sample size greater or equal to 2 to a new " + "TallyRunning object.");

            n = 2;
        }

        // initialize storage of last n values
        this._n = n;
        this._valuesLastN = new double[n];
        this._sumLastN = 0;
        this._sumSquareLastN = 0;
    }

    /**
     * Returns a Reporter to produce a report about this Tally.
     *
     * @return desmoj.report.Reporter : The Reporter for this Tally.
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {
        return new desmoj.core.report.TallyReporter(this);
    }

    /**
     * Returns the maximum of the last n values.
     *
     * @return double : The maximum value of the last n values.
     */
    public double getMaximumLastN() {

        long obs = getObservations();
        if (obs == 0) {
            return 0;
        }

        double maximum = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < Math.min(this._n, obs); i++) {
            if (this._valuesLastN[i] > maximum) {
                maximum = this._valuesLastN[i];
            }
        }

        // return the rounded maximum
        return round(maximum);
    }

    /**
     * Returns the mean value of the last <i>n</n> values observed. Note that if less than n samples values are observed
     * this far, the value returned will only include fewer (i.e. all) samples.
     *
     * @return double : The mean value of the last <i>n</n> values observed so far.
     */
    public double getMeanLastN() {
        if (getObservations() == 0) {
            sendWarning("Attempt to get a mean value, but there is not "
                        + "sufficient data yet. UNDEFINED (-1.0) will be returned!",
                        "TallyRunning: " + this.getName() + " Method: double getMean()",
                        "You can not calculate a mean value as long as no data is collected.",
                        "Make sure to ask for the mean value only after some data has been " + "collected already.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        // calculate the mean value
        double meanValue = this._sumLastN / Math.min(this.getObservations(), this._n);
        // return the rounded mean value
        return round(meanValue);
    }

    /**
     * Returns the minimum of the last n values.
     *
     * @return double : The minimum value of the last n values.
     */
    public double getMinimumLastN() {

        long obs = getObservations();
        if (obs == 0) {
            return 0;
        }

        double minimum = Double.POSITIVE_INFINITY;
        for (int i = 0; i < Math.min(this._n, obs); i++) {
            if (this._valuesLastN[i] < minimum) {
                minimum = this._valuesLastN[i];
            }
        }
        // return the rounded minimum
        return round(minimum);
    }

    /**
     * Returns the sample size n.
     *
     * @return int : The sample size n.
     */
    public int getSampleSizeN() {
        return this._n;
    }

    /**
     * Returns the standard deviation of the last <i>n</n> values observed. Note that if less than n samples values are
     * observed this far, the value returned will only include fewer (i.e. all) samples.
     *
     * @return double : The standard deviation of the last <i>n</n> values observed so far.
     */
    public double getStdDevLastN() {
        long obs = Math.min(this.getObservations(), this._n);

        if (obs < 2) {
            sendWarning("Attempt to get a standard deviation, but there is not "
                        + "sufficient data yet. UNDEFINED (-1.0) will be returned!",
                        "TallyRunning: " + this.getName() + " Method: double getStdDev()",
                        "A standard deviation can not be calculated as long as no data is " + "collected.",
                        "Make sure to ask for the standard deviation only after some data "
                        + "has been collected already.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        // calculate the standard deviation
        double stdDev = Math.sqrt(
        Math.abs(obs * this._sumSquareLastN - this._sumLastN * this._sumLastN) / (obs * (obs - 1)));
        // return the rounded standard deviation
        return round(stdDev);
    }

    /**
     * Resets this TallyRunning object by resetting all variables to 0.0 .
     */
    @Override
    public void reset() {
        super.reset(); // reset the Tally, too.

        this._sumLastN = this._sumSquareLastN = 0.0;
    }

    /**
     * Updates this <code>TallyRunning</code> object by fetching the actual value of the <code>ValueSupplier</code> and
     * processing it. The
     * <code>ValueSupplier</code> is passed in the constructor of this
     * <code>TallyRunning</code> object. This <code>update()</code> method complies
     * with the one described in DESMO, see [Page91].
     */
    @Override
    public void update() {
        super.update(); // call the update() method of Tally

        long obs = getObservations();
        double lastVal = getLastValue();
        int index = (int) ((obs - 1) % this._n);

        this._sumLastN += lastVal - this._valuesLastN[index];
        this._sumSquareLastN += lastVal * lastVal - this._valuesLastN[index] * this._valuesLastN[index];
        // note: in case obs < n, this.valuesLastN[index] will be zero

        this._valuesLastN[index] = lastVal;
    }

    /**
     * Updates this <code>TallyRunning</code> object with the double value given as parameter. In some cases it might be
     * more convenient to pass the value this
     * <code>TallyRunning</code> will be updated with directly within the
     * <code>update(double val)</code> method instead of going via the
     * <code>ValueSupplier</code>.
     *
     * @param val double : The value with which this <code>TallyRunning</code> will be updated.
     */
    @Override
    public void update(double val) {
        // call the update(double val) method of Tally
        super.update(val);

        long obs = getObservations();
        double lastVal = getLastValue();
        int index = (int) ((obs - 1) % this._n);

        this._sumLastN += lastVal - this._valuesLastN[index];
        this._sumSquareLastN += lastVal * lastVal - this._valuesLastN[index] * this._valuesLastN[index];
        // note: in case obs < n, this.valuesLastN[index] will be zero

        this._valuesLastN[index] = lastVal;
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
     * @param arg Object : The Object with which this <code>TallyRunning</code> is updated. Normally a double number
     *            which is added to the statistics or <code>null</code>.
     */
    @Override
    public void update(Observable o, Object arg) {
        if (o == null) // null was passed instead of an Observable
        {
            sendWarning(
            "Attempt to update a TallyRunning with no reference to an " + "Observable. The actual value of '"
            + getValueSupplier().getName() + "' will be fetched and processed anyway.",
            "TallyRunning: " + this.getName() + " Method: update (Observable " + "o, Object arg)",
            "The passed Observable: o in this method is only a null pointer.",
            "The update()-method was not called via notifyObservers() from an "
            + "Observable. Who was calling it? Why don't you let the Observable do" + " the work?");
        }

        super.update(o, arg); // call the update() method of Tally

        long obs = getObservations();
        double lastVal = getLastValue();
        int index = (int) ((obs - 1) % this._n);

        this._sumLastN += lastVal - this._valuesLastN[index];
        this._sumSquareLastN += lastVal * lastVal - this._valuesLastN[index] * this._valuesLastN[index];
        // note: in case obs < n, this.valuesLastN[index] will be zero

        this._valuesLastN[index] = lastVal;
    }
} // end class TallyRunning
