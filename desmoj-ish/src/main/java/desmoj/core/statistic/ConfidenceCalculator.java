package desmoj.core.statistic;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.OutOfRangeException;

import desmoj.core.simulator.Model;

/**
 * The <code>ConfidenceCalculator</code> class has the same functionality as the
 * <code>Tally</code> class. Additionally, it calculates a confidence interval
 * for the mean of the samples passed to the <code>ConfidenceCalculator</code>.
 * Note that the confidence interval computation assumes that the samples are
 * (approximately) identically and independently distributed. Observe that the
 * confidence interval estimations of this class are meaningless should this
 * assumption not hold!
 *
 * @see Tally
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
public class ConfidenceCalculator extends Tally {

    /**
     * The confidence level
     */
    private double _confidenceLevel;

    /**
     * Constructor for a ConfidenceCalculator object that has no connection to a
     * <code>ValueSupplier</code>. The confidence level is set to 0.95.
     *
     * @param ownerModel   Model : The model this ConfidenceCalculator is associated
     *                     to
     * @param name         java.lang.String : The name of this ConfidenceCalculator
     *                     object
     * @param showInReport boolean : Flag for showing the report about this
     *                     ConfidenceCalculator.
     * @param showInTrace  boolean : Flag for showing the trace output of this
     *                     ConfidenceCalculator.
     */
    public ConfidenceCalculator(Model ownerModel, String name, boolean showInReport, boolean showInTrace) {
        // call the constructor of ValueStatistics
        super(ownerModel, name, showInReport, showInTrace);
        _confidenceLevel = 0.95;
    }

    /**
     * Constructor for a ConfidenceCalculator object that is connected to a
     * <code>ValueSupplier</code>. The confidence level is set to 0.95.
     *
     * @param ownerModel   Model : The model this ConfidenceCalculator is associated
     *                     to
     * @param name         java.lang.String : The name of this ConfidenceCalculator
     *                     object
     * @param valSup       ValueSupplier : The ValueSupplier providing the value for
     *                     this ConfidenceCalculator. The given ValueSupplier will
     *                     be observed by this ConfidenceCalculator object.
     * @param showInReport boolean : Flag for showing the report about this
     *                     ConfidenceCalculator.
     * @param showInTrace  boolean : Flag for showing the trace output of this
     *                     ConfidenceCalculator.
     */
    public ConfidenceCalculator(Model ownerModel, String name, ValueSupplier valSup, boolean showInReport,
                                boolean showInTrace) {
        // call the constructor of ValueStatistics
        super(ownerModel, name, valSup, showInReport, showInTrace);
        _confidenceLevel = 0.95;
    }

    /**
     * Returns a Reporter to produce a report about this ConfidenceCalculator.
     *
     * @return desmoj.report.Reporter : The Reporter for this ConfidenceCalculator.
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {
        return new desmoj.core.report.ConfidenceCalculatorReporter(this);
    }

    /**
     * Calculates the lower bound of the confidence interval of the mean.
     *
     * @return double : The lower bound of the confidence interval of the mean
     */
    public double getConfidenceIntervalOfMeanLowerBound() {
        if (this.getObservations() < 2) {
            sendWarning("Attempt to determine a confidence interval, but there is no "
            + "sufficient data yet. UNDEFINED (-1.0) will be returned!",
                        "ConfidenceCalculator: " + this.getName()
                        + " Method: double getConfidenceIntervalOfMeanLowerBound()",
                        "You cannot obtain a confidence interval based on less than two observations.",
                        "Make sure to update the ConfidenceCalculator at least twice.");

            return UNDEFINED; // return UNDEFINED = -1.0

        } else {
            return round(getMean() - calcConfidenceIntervalHalfWidth());
        }
    }

    /**
     * Calculates the upper bound of the confidence interval of the mean.
     *
     * @return double : The upper bound of the confidence interval of the mean
     */
    public double getConfidenceIntervalOfMeanUpperBound() {
        if (this.getObservations() < 2) {
            sendWarning("Attempt to determine a confidence interval, but there is no "
            + "sufficient data yet. UNDEFINED (-1.0) will be returned!",
                        "ConfidenceCalculator: " + this.getName()
                        + " Method: double getConfidenceIntervalOfMeanUpperBound()",
                        "You cannot obtain a confidence interval based on less than two observations.",
                        "Make sure to update the ConfidenceCalculator at least twice.");

            return UNDEFINED; // return UNDEFINED = -1.0

        } else {
            return round(getMean() + calcConfidenceIntervalHalfWidth());
        }

    }

    /**
     * Returns the confidence level.
     * <p>
     *
     * @return double : the confidence level
     */
    public double getConfidenceLevel() {
        return round(_confidenceLevel);
    }

    /**
     * Sets the confidence level; default value is 0.95.
     * <p>
     *
     * Note: The confidence interval computation assumes that the input values are
     * (approximately) identically and independently distributed.
     *
     * @param level double : the confidence level
     */
    public void setConfidenceLevel(double level) {
        _confidenceLevel = level;
    }

    /**
     * Internal helper method to determine the confidence interval half width.
     *
     * @return double : The half width of the confidence interval
     */
    private double calcConfidenceIntervalHalfWidth() {

        if (this.getObservations() < 2)
            return UNDEFINED;

        double n = getObservations();
        double s = getStdDev();
        var _tDistribution = new TDistribution(n - 1);

        double z = Double.NaN;
        try {
            z = _tDistribution.inverseCumulativeProbability(0.5 + _confidenceLevel / 2);
        } catch (OutOfRangeException e) {
            return UNDEFINED;
        }

        return z * s / Math.sqrt(n);
    }
}
