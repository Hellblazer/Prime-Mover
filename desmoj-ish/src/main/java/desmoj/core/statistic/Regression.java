package desmoj.core.statistic;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimClock;

import java.util.Observable;

/**
 * The <code>Regression</code> class is producing a linear regression for two
 * <code>ValueSupplier</code> objects called x and y.
 *
 * @author Soenke Claassen
 * @author based on DESMO-C from Thomas Schniewind, 1998
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */

public class Regression extends desmoj.core.statistic.StatisticObject {

    // ****** attributes ******

    /**
     * Values have changed considerably only, if they have changed for more than this constant.
     */
    protected static final double C_EPSILON = 0.00001;

    /**
     * The sum of all x-squares so far
     */
    private double _sumSquareX;

    /**
     * The sum of all y-squares so far
     */
    private double _sumSquareY;

    /**
     * The sum of all x-values so far
     */
    private double _sumX;

    /**
     * The sum of x times y for all the pairs of x and y values so far
     */
    private double _sumXtimesY;

    /**
     * The sum of all y-values so far
     */
    private double _sumY;

    /**
     * The actual value of the x-variable
     */
    private double _x;

    /**
     * The name of the ValueSupplier for the x-Value
     */
    private String _xName;

    /**
     * The ValueSupplier for the x-Value.
     */
    private ValueSupplier _xSupplier;

    /**
     * The actual value of the y-variable
     */
    private double _y;

    /**
     * The name of the ValueSupplier for the y-Value
     */
    private String _yName;

    /**
     * The ValueSupplier for the y-Value.
     */
    private ValueSupplier _ySupplier;

    // ****** methods ******

    /**
     * Constructor for a Regression object that is NOT connected to any
     * <code>ValueSuppliers</code> for the x and y values.
     *
     * @param ownerModel   Model : The model this Regression object is associated to
     * @param name         java.lang.String : The name of this Regression object
     * @param xName        java.lang.String : The name for the x values.
     * @param yName        java.lang.String : The name for the y values.
     * @param showInReport boolean : Flag for showing the report about this Regression.
     * @param showInTrace  boolean : Flag for showing the trace output of this Regression.
     */
    public Regression(Model ownerModel, String name, String xName, String yName, boolean showInReport,
                      boolean showInTrace) {
        super(ownerModel, name, showInReport, showInTrace); // StatisticObject

        this._xName = xName;
        this._yName = yName;

        this._xSupplier = null;
        this._ySupplier = null;

        if ((xName != null && xName.length() == 0) || xName == null) // no proper name for x
        {
            sendWarning("The x-value of a regression analysis has no name. " + "It will be named: 'X'!",
                        "Regression: " + this.getName() + " Constructor: Regression(Model "
                        + "ownerModel, String name, String xName, String yName, "
                        + "boolean showInReport, boolean showInTrace)", "A x-value with no name is hard to find back.",
                        "Make sure to give the x-value a useful name.");

            this._xName = "X";
        }

        if ((yName != null && yName.length() == 0) || yName == null) // no proper name for y
        {
            sendWarning("The y-value of a regression analysis has no name. " + "It will be named: 'Y'!",
                        "Regression: " + this.getName() + " Constructor: Regression(Model "
                        + "ownerModel, String name, String xName, String yName, "
                        + "boolean showInReport, boolean showInTrace)", "A y-value with no name is hard to find back.",
                        "Make sure to give the y-value a useful name.");

            this._yName = "Y";
        }

        this._x = this._sumX = this._sumSquareX = 0.0;
        this._y = this._sumY = this._sumSquareY = this._sumXtimesY = 0.0;
    }

    /**
     * Constructor for a Regression object that is observing two
     * <code>ValueSuppliers</code> for the x and y values.
     *
     * @param ownerModel   Model : The model this Regression object is associated to
     * @param name         java.lang.String : The name of this Regression object
     * @param xName        java.lang.String : The name for the x values.
     * @param yName        java.lang.String : The name for the y values.
     * @param xValsup      ValueSupplier : The ValueSupplier providing the x values for the regression analysis. The
     *                     given ValueSupplier will be observed by this Regression object.
     * @param yValsup      ValueSupplier : The ValueSupplier providing the y values for the regression analysis. The
     *                     given ValueSupplier will be observed by this Regression object.
     * @param showInReport boolean : Flag for showing the report about this Regression.
     * @param showInTrace  boolean : Flag for showing the trace output of this Regression.
     */
    public Regression(Model ownerModel, String name, String xName, String yName, ValueSupplier xValsup,
                      ValueSupplier yValsup, boolean showInReport, boolean showInTrace) {
        super(ownerModel, name, showInReport, showInTrace); // StatisticObject

        // xValsup is no valid ValueSupplier
        if (xValsup == null) {
            sendWarning("Attempt to produce a Regression analysis about a non "
                        + "existing ValueSupplier for the x-value. The command will be ignored!",
                        "Regression: " + this.getName() + " Constructor: Regression(Model "
                        + "ownerModel, String name, String xName, String yName, "
                        + "ValueSupplier xValsup, ValueSupplier yValsup, boolean "
                        + "showInReport, boolean showInTrace)",
                        "The given ValueSupplier: xValsup is only a null pointer.",
                        "Make sure to pass a valid x-ValueSupplier when constructing a new " + "Regression object.");

            return; // just return
        }

        // yValsup is no valid ValueSupplier
        if (yValsup == null) {
            sendWarning("Attempt to produce a Regression analysis about a non "
                        + "existing ValueSupplier for the y-value. The command will be ignored!",
                        "Regression: " + this.getName() + " Constructor: Regression(Model "
                        + "ownerModel, String name, String xName, String yName, "
                        + "ValueSupplier xValsup, ValueSupplier yValsup, boolean "
                        + "showInReport, boolean showInTrace)",
                        "The given ValueSupplier: yValsup is only a null pointer.",
                        "Make sure to pass a valid y-ValueSupplier when constructing a new " + "Regression object.");

            return; // just return
        }

        this._xName = xName;
        this._yName = yName;

        this._xSupplier = xValsup;
        this._ySupplier = yValsup;

        // this Regression will observe the ValueSuppliers of the x and y values
        _xSupplier.addObserver(this);
        _ySupplier.addObserver(this);

        if ((xName != null && xName.length() == 0) || xName == null) // no proper name for x
        {
            sendWarning("The x-value of a regression analysis has no name. " + "It will be named: 'X'!",
                        "Regression: " + this.getName() + " Constructor: Regression(Model "
                        + "ownerModel, String name, String xName, String yName, "
                        + "ValueSupplier xValsup, ValueSupplier yValsup, boolean "
                        + "showInReport, boolean showInTrace)", "A x-value with no name is hard to find back.",
                        "Make sure to give the x-value a useful name.");

            this._xName = "X";
        }

        if ((yName != null && yName.length() == 0) || yName == null) // no proper name for y
        {
            sendWarning("The y-value of a regression analysis has no name. " + "It will be named: 'Y'!",
                        "Regression: " + this.getName() + " Constructor: Regression(Model "
                        + "ownerModel, String name, String xName, String yName, "
                        + "ValueSupplier xValsup, ValueSupplier yValsup, boolean "
                        + "showInReport, boolean showInTrace)", "A y-value with no name is hard to find back.",
                        "Make sure to give the y-value a useful name.");

            this._yName = "Y";
        }

        this._x = this._sumX = this._sumSquareX = 0.0;
        this._y = this._sumY = this._sumSquareY = this._sumXtimesY = 0.0;
    }

    /**
     * Constructor for a Regression object with no names for the x and y values. The names will be set to "X" and "Y".
     */
    public Regression(Model ownerModel, String name, ValueSupplier xValsup, ValueSupplier yValsup, boolean showInReport,
                      boolean showInTrace) {
        super(ownerModel, name, showInReport, showInTrace); // StatisticObject

        // xValsup is no valid ValueSupplier
        if (xValsup == null) {
            sendWarning("Attempt to produce a Regression analysis about a non "
                        + "existing ValueSupplier for the x-value. The command will be ignored!",
                        "Regression: " + this.getName() + " Constructor: Regression(Model "
                        + "ownerModel, String name, String xName, String yName, "
                        + "ValueSupplier xValsup, ValueSupplier yValsup, boolean "
                        + "showInReport, boolean showInTrace)",
                        "The given ValueSupplier: xValsup is only a null pointer.",
                        "Make sure to pass a valid x-ValueSupplier when constructing a new " + "Regression object.");

            return; // just return
        }

        // yValsup is no valid ValueSupplier
        if (yValsup == null) {
            sendWarning("Attempt to produce a Regression analysis about a non "
                        + "existing ValueSupplier for the y-value. The command will be ignored!",
                        "Regression: " + this.getName() + " Constructor: Regression(Model "
                        + "ownerModel, String name, String xName, String yName, "
                        + "ValueSupplier xValsup, ValueSupplier yValsup, boolean "
                        + "showInReport, boolean showInTrace)",
                        "The given ValueSupplier: yValsup is only a null pointer.",
                        "Make sure to pass a valid y-ValueSupplier when constructing a new " + "Regression object.");

            return; // just return
        }

        this._xName = "X";
        this._yName = "Y";

        this._xSupplier = xValsup;
        this._ySupplier = yValsup;

        // this Regression will observe the ValueSuppliers of the x and y values
        _xSupplier.addObserver(this);
        _ySupplier.addObserver(this);

        this._x = this._sumX = this._sumSquareX = 0.0;
        this._y = this._sumY = this._sumSquareY = this._sumXtimesY = 0.0;
    }

    /**
     * Returns the correlation coefficient.
     *
     * @return double : The correlation coefficient.
     */
    public double correlationCoeff() {
        long n = getObservations(); // get the number of observations so far

        if (n <= 5) // not enough data yet
        {
            sendWarning("Attempt to get the correlation coefficient, but there "
                        + "is insufficient data yet to calculate it. UNDEFINED (-1.0) " + "will be returned!",
                        "Regression: " + this.getName() + " Method: double " + "correlationCoeff().",
                        "The correlation coefficient can not be calculated, because there "
                        + "is insufficient data collected so far.",
                        "Make sure to ask for the correlation coefficient only after "
                        + "enough data has been collected.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        double dx = Math.abs(n * _sumSquareX - _sumX * _sumX);
        double dy = Math.abs(n * _sumSquareY - _sumY * _sumY);

        if (dx < C_EPSILON || dy < C_EPSILON) // not changed considerably
        {
            sendWarning("The x or y values have not changed considerably. The "
                        + "data seems to be degenerated. UNDEFINED (-1.0) will be returned!",
                        "Regression: " + this.getName() + " Method: double " + "correlationCoeff().",
                        "The x or y values have not changed considerably. Some failure " + "might have occured.",
                        "One or both values are almost constant. It seems that nothing "
                        + "really happens. Check that!");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        double squareDiff = (n * _sumXtimesY - _sumX * _sumY);

        // calculate the rounded result
        double rndResult = round(squareDiff * squareDiff / (dx * dy));
        return rndResult;
    }

    /**
     * Returns a Reporter to produce a report about this Regression analysis.
     *
     * @return desmoj.report.Reporter : The Reporter for this Regression.
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {
        return new desmoj.core.report.RegressionReporter(this);
    }

    /**
     * Returns the mean x-value.
     *
     * @return double : The mean x-value.
     */
    public double getXMean() {
        long n = getObservations(); // get the number of observations so far

        if (n == 0) // nothing observed yet
        {
            sendWarning("Attempt to get a mean x-value, but there is not "
                        + "sufficient data yet. UNDEFINED (-1.0) will be returned!",
                        "Regression: " + this.getName() + " Method: double getXMean()",
                        "You can not calculate a mean value as long as no data is " + "collected.",
                        "Make sure to ask for the mean value only after some data " + "has been collected already.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        // calculate the rounded result
        double rndResult = StatisticObject.round(_sumX / n);

        return rndResult;
    }

    /**
     * Returns the name of the x-value.
     *
     * @return String : The name of the x-value.
     */
    public String getXName() {
        return this._xName;
    }

    /**
     * Returns the actual value of the <code>ValueSupplier</code> supplying the x-value.
     *
     * @return double : The actual value of the observed <code>ValueSupplier</code> object supplying the x-value.
     */
    public double getXValue() {
        if (_xSupplier == null) // no x-Supplier observed
        {
            sendWarning("Attempt to get a x-value, but there is no "
                        + "ValueSupplier for x values. UNDEFINED (-1.0) will be returned!",
                        "Regression: " + this.getName() + " Method: double getXValue()",
                        "A x value can not be provided, because there is no x "
                        + "ValueSupplier observed from which the value could be fetched.",
                        "Make sure to ask for the x value only if there is a x " + "ValueSupplier observed.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        return _xSupplier.value();
    }

    /**
     * Returns the mean y-value.
     *
     * @return double : The mean y-value.
     */
    public double getYMean() {
        long n = getObservations(); // get the number of observations so far

        if (n == 0) // nothing observed yet
        {
            sendWarning("Attempt to get a mean y-value, but there is not "
                        + "sufficient data yet. UNDEFINED (-1.0) will be returned!",
                        "Regression: " + this.getName() + " Method: double getYMean()",
                        "You can not calculate a mean value as long as no data is " + "collected.",
                        "Make sure to ask for the mean value only after some data " + "has been collected already.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        // calculate the rounded result
        double rndResult = StatisticObject.round(_sumY / n);

        return rndResult;
    }

    /**
     * Returns the name of the y-value.
     *
     * @return String : The name of the y-value.
     */
    public String getYName() {
        return this._yName;
    }

    /**
     * Returns the actual value of the <code>ValueSupplier</code> supplying the y-value.
     *
     * @return double : The actual value of the observed <code>ValueSupplier</code> object supplying the y-value.
     */
    public double getYValue() {
        if (_ySupplier == null) // no y-Supplier observed
        {
            sendWarning("Attempt to get a y-value, but there is no "
                        + "ValueSupplier for y values. UNDEFINED (-1.0) will be returned!",
                        "Regression: " + this.getName() + " Method: double getYValue()",
                        "A y value can not be provided, because there is no y "
                        + "ValueSupplier observed from which the value could be fetched.",
                        "Make sure to ask for the y value only if there is a y " + "ValueSupplier observed.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        return _ySupplier.value();
    }

    /**
     * Returns the interception of the X-axis.
     *
     * @return double : The interception of the X-axis.
     */
    public double intercept() {
        long n = getObservations(); // get the number of observations so far

        if (n <= 5) // not enough data yet
        {
            sendWarning("Attempt to get the interception of the X-axis, but "
                        + "there is insufficient data yet to calculate it. UNDEFINED " + "(-1.0) will be returned!",
                        "Regression: " + this.getName() + " Method: double intercept()",
                        "The interception of the X-axis can not be calculated, because "
                        + "there is insufficient data collected so far.",
                        "Make sure to ask for the interception of the X-axis only after "
                        + "enough data has been collected.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        double dx = Math.abs(n * _sumSquareX - _sumX * _sumX);
        double dy = Math.abs(n * _sumSquareY - _sumY * _sumY);

        if (dx < C_EPSILON || dy < C_EPSILON) // not changed considerably
        {
            sendWarning("The x or y values have not changed considerably. The "
                        + "data seems to be degenerated. UNDEFINED (-1.0) will be returned!",
                        "Regression: " + this.getName() + " Method: double intercept()",
                        "The x or y values have not changed considerably. Some failure " + "might have occured.",
                        "One or both values are almost constant. It seems that nothing "
                        + "really happens. Check that!");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        // calculate the rounded result
        double rndResult = round((_sumY * _sumSquareX - _sumX * _sumXtimesY) / dx);
        return rndResult;
    }

    /**
     * Returns the regression coefficient.
     *
     * @return double : The regression coefficient.
     */
    public double regCoeff() {
        long n = getObservations(); // get the number of observations so far

        if (n <= 5) // not enough data yet
        {
            sendWarning("Attempt to get the regression coefficient, but there "
                        + "is insufficient data yet to calculate it. UNDEFINED (-1.0) " + "will be returned!",
                        "Regression: " + this.getName() + " Method: double regCoeff()",
                        "The regression coefficient can not be calculated, because there "
                        + "is insufficient data collected so far.",
                        "Make sure to ask for the regression coefficient only after "
                        + "enough data has been collected.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        double dx = Math.abs(n * _sumSquareX - _sumX * _sumX);
        double dy = Math.abs(n * _sumSquareY - _sumY * _sumY);

        if (dx < C_EPSILON || dy < C_EPSILON) // not changed considerably
        {
            sendWarning("The x or y values have not changed considerably. The "
                        + "data seems to be degenerated. UNDEFINED (-1.0) will be returned!",
                        "Regression: " + this.getName() + " Method: double regCoeff()",
                        "The x or y values have not changed considerably. Some failure " + "might have occured.",
                        "One or both values are almost constant. It seems that nothing "
                        + "really happens. Check that!");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        // calculate the rounded result
        double rndResult = round((n * _sumXtimesY - _sumX * _sumY) / dx);
        return rndResult;
    }

    /**
     * Resets this Regression object by resetting all variables of x and y to 0.0 . The names remain the same.
     */
    @Override
    public void reset() {
        super.reset(); // reset the StatisticObject, too.

        this._x = this._sumX = this._sumSquareX = 0.0;
        this._y = this._sumY = this._sumSquareY = this._sumXtimesY = 0.0;
    }

    /**
     * Returns the residuale standard deviation.
     *
     * @return double : The residuale standard deviation.
     */
    public double residualStdDev() {
        long n = getObservations(); // get the number of observations so far

        if (n <= 5) // not enough data yet
        {
            sendWarning("Attempt to get the residual standard deviation, but "
                        + "there is insufficient data yet to calculate it. UNDEFINED " + "(-1.0) will be returned!",
                        "Regression: " + this.getName() + " Method: double " + "residualStdDev()",
                        "You can not calculate the residual standard deviation as long "
                        + "as no sufficient data is collected.",
                        "Make sure to ask for the residual standard deviation only "
                        + "after enough data has been collected.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        double dx = Math.abs(n * _sumSquareX - _sumX * _sumX);
        double dy = Math.abs(n * _sumSquareY - _sumY * _sumY);

        if (dx < C_EPSILON || dy < C_EPSILON) // not changed considerably
        {
            sendWarning("The x or y values have not changed considerably. The "
                        + "data seems to be degenerated. UNDEFINED (-1.0) will be returned!",
                        "Regression: " + this.getName() + " Method: double " + "residualStdDev()",
                        "The x or y values have not changed considerably. Some failure " + "might have occured.",
                        "One or both values are almost constant. It seems that nothing "
                        + "really happens. Check that!");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        // calculate the residual std. deviation
        double result = Math.sqrt(Math.abs(_sumSquareY - intercept() * _sumY - regCoeff() * _sumXtimesY) / (n - 2));
        // calculate the rounded result
        double rndResult = round(result);
        return rndResult;
    }

    /**
     * Returns the standard deviation of the regression coefficients.
     *
     * @return double : The standard deviation of the regression coefficients.
     */
    public double stdDevRegCoeff() {
        long n = getObservations(); // get the number of observations so far

        if (n <= 5) // not enough data yet
        {
            sendWarning("Attempt to get the standard deviation of the "
                        + "regression coefficients, but there is insufficient data yet "
                        + "to calculate it. UNDEFINED (-1.0) will be returned!",
                        "Regression: " + this.getName() + " Method: double stdDevRegCoeff()",
                        "The standard deviation of the regression coefficients can not be "
                        + "calculated, because there is insufficient data collected so far.",
                        "Make sure to ask for the standard deviation of the regression "
                        + "coefficients only after enough data has been collected.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        double dx = Math.abs(n * _sumSquareX - _sumX * _sumX);
        double dy = Math.abs(n * _sumSquareY - _sumY * _sumY);

        if (dx < C_EPSILON || dy < C_EPSILON) // not changed considerably
        {
            sendWarning("The x or y values have not changed considerably. The "
                        + "data seems to be degenerated. UNDEFINED (-1.0) will be returned!",
                        "Regression: " + this.getName() + " Method: double stdDevRegCoeff()",
                        "The x or y values have not changed considerably. Some failure " + "might have occured.",
                        "One or both values are almost constant. It seems that nothing "
                        + "really happens. Check that!");

            return UNDEFINED; // return UNDEFINED = -1.0
        }

        // calculate the standard deviation Reg. Coeff.
        double result = n * residualStdDev() / Math.sqrt((n - 2) * dx);

        // calculate the rounded result
        double rndResult = round(result);
        return rndResult;
    }

    /**
     * Updates this <code>Regression</code> object by fetching the actual values of the <code>ValueSupplier</code> s and
     * processing them. The
     * <code>ValueSupplier</code> s are passed in the constructor of this
     * <code>Regression</code> object. This <code>update()</code> method complies
     * with the one described in DESMO, see [Page91].
     */
    public void update() {
        // not connected to a x- OR y-ValueSupplier
        if (_xSupplier == null || _ySupplier == null) {
            sendWarning("Attempt to update a Regression analysis without "
                        + "providing any x- or y-value. Which value(s) should be used to "
                        + "update the Regression statistic? The command will be ignored!",
                        "Regression: " + this.getName() + " Method: update () ",
                        "The given ValueSupplier: xSupplier or ySupplier is only a null " + "pointer.",
                        "Make sure to pass a valid x-ValueSupplier and y-ValueSupplier "
                        + "when constructing a new Regression object. Or use the update "
                        + "(double xVal, double yVal) method.");

            return; // just return
        }

        this._x = _xSupplier.value(); // get the x-value

        this._y = _ySupplier.value(); // get the y-value

        _sumX += _x; // update the variables...
        _sumY += _y;
        _sumSquareX += _x * _x;
        _sumSquareY += _y * _y;
        _sumXtimesY += _x * _y;

        incrementObservations(); // increment the observations (see
        // Reportable)

        traceUpdate(); // leave a message in the trace
    }

    /**
     * Updates this <code>Regression</code> object with the x and y double values given as parameters. In some cases it
     * might be more convenient to pass the values this <code>Regression</code> will be updated with directly within
     * the
     * <code>update(double xVal, double yVal)</code> method instead of going via the
     * <code>ValueSupplier</code>.
     *
     * @param xVal double : The x value with which this <code>Regression</code> will be updated.
     * @param yVal double : The y value with which this <code>Regression</code> will be updated.
     */
    public void update(double xVal, double yVal) {

        this._x = xVal; // get the x-value

        this._y = yVal; // get the y-value

        _sumX += _x; // update the variables...
        _sumY += _y;
        _sumSquareX += _x * _x;
        _sumSquareY += _y * _y;
        _sumXtimesY += _x * _y;

        incrementObservations(); // increment the observations (see
        // Reportable)

        traceUpdate(); // leave a message in the trace
    }

    /**
     * Implementation of the virtual <code>update(Observable, Object)</code> method of the <code>Observer</code>
     * interface. This method will be called automatically from an <code>Observable</code> object within its
     * <code>notifyObservers()</code> method. <br>
     * Whenever either of the two <code>ValueSupplier</code> objects providing the x or y value is updated and calling
     * <code>notifyStatistics()</code> the x and the y value will be fetched and a new regression will be calculated.
     *
     * @param o   java.util.Observable : The Observable calling this method within its own
     *            <code>notifyObservers()</code> method.
     * @param arg Object : The Object with which this <code>Regression</code> is updated.
     */
    @Override
    public void update(Observable o, Object arg) {
        if (o == null) // null was passed instead of an Observable
        {
            sendWarning("Attempt to update a Regression with no reference to " + "an Observable. The x-value of '"
                        + _xSupplier.getName() + "' and " + "the y-value of '" + _ySupplier.getName()
                        + "'will be fetched and " + "processed anyway!",
                        "Regression: " + this.getName() + " Method: update (Observable o," + " Object arg)",
                        "The passed Observable: o in this method is only a null pointer.",
                        "The update()-method was not called via notifyObservers() from an "
                        + "Observable. Who was calling it? Why don't you let the Observable " + "do the work?");
        }

        // update was called from xSupplier
        if (o == _xSupplier) {
            if (arg == null) // notifyStatistics() was called with no arg
            {
                this._x = _xSupplier.value(); // get the x-value
            } else {
                if (arg instanceof Number) // actual x value passed in arg?
                {
                    this._x = convertToDouble(arg); // get the x-value out of
                    // the
                    // arg
                } else {
                    sendWarning("Attempt to update a Regression with an argument arg,"
                                + " that could not be recognized. The attempted action is ignored!",
                                "Regression: " + this.getName() + " Method: update (Observable " + "o, Object arg)",
                                "The passed Object in the argument arg could not be recognized.",
                                "Make sure to pass null or a Number object as the arg argument");

                    return; // do nothing, just return
                }
            }

            this._y = _ySupplier.value(); // get the y-value
        } // end update was called from xSupplier

        // update was called from ySupplier
        if (o == _ySupplier) {
            if (arg == null) // notifyStatistics() was called with no arg
            {
                this._y = _ySupplier.value(); // get the y-value
            } else {
                if (arg instanceof Number) // actual y value passed in arg?
                {
                    this._y = convertToDouble(arg); // get the y-value out of
                    // the
                    // arg
                } else {
                    sendWarning("Attempt to update a Regression with an argument arg,"
                                + " that could not be recognized. The attempted action is ignored!",
                                "Regression: " + this.getName() + " Method: update (Observable " + "o, Object arg)",
                                "The passed Object in the argument arg could not be recognized.",
                                "Make sure to pass null or a Number object as the arg argument");

                    return; // do nothing, just return
                }
            }

            this._x = _xSupplier.value(); // get the x-value
        } // end update was called from ySupplier

        // update was called from the SimClock
        if (o instanceof SimClock) {
            this._x = _xSupplier.value(); // get the x-value

            this._y = _ySupplier.value(); // get the y-value

        } // end update was called from SimClock

        _sumX += _x; // update the variables...
        _sumY += _y;
        _sumSquareX += _x * _x;
        _sumSquareY += _y * _y;
        _sumXtimesY += _x * _y;

        incrementObservations(); // increment the observations (see
        // Reportable)

        traceUpdate(); // leave a message in the trace
    }

    /**
     * Returns <code>true</code> when the x-values are constant. That means no considerable change can be recognized.
     *
     * @return boolean :<code>true</code> is returned if no considerable change of the x-values can be recognized.
     * <code>false</code> otherwise.
     */
    public boolean xIsConstant() {
        double dx = Math.abs(getObservations() * _sumSquareX - _sumX * _sumX);

        return (dx < C_EPSILON);
    }

    /**
     * Returns <code>true</code> when the y-values are constant. That means no considerable change can be recognized.
     *
     * @return boolean :<code>true</code> is returned if no considerable change of the y-values can be recognized.
     * <code>false</code> otherwise.
     */
    public boolean yIsConstant() {
        double dy = Math.abs(getObservations() * _sumSquareY - _sumY * _sumY);

        return (dy < C_EPSILON);
    }
} // end class Regression
