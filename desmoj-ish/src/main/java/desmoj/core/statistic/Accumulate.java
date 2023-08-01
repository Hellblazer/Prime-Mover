package desmoj.core.statistic;

import java.util.Observable;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;

/**
 * The <code>Accumulate</code> class is providing a statistic analysis about
 * one value. The mean value and the standard deviation is weighted over time.<br />
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen
 * @author based on DESMO-C from Thomas Schniewind, 1998
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

public class Accumulate extends desmoj.core.statistic.ValueStatistics {

    // ****** attributes ******
    
    /**
     * The point of time this Accumulate was updated first
     */
    private TimeInstant _firstValueReadAt;

    /**
     * A value to assume initially and after each reset. 
     */
    private double _initialValue;
    
    /**
     * The point of time this Accumulate was updated last
     */
    private TimeInstant _lastUpdate;
    
    /**
     * The time-weighted mean of all values so far
     */
    private double _mean;

    /**
     * The length of the period during which this Accumulate was paused since the last reset  
     */
    private TimeSpan _pausedPeriodSinceLastReset;

    /**
     * Instant at which the current pause has started (<code>null</code> if not paused at the moment) 
     */
    private TimeInstant _pausedSince;

    /**
     * This flag indicates if the accumulate's last value is
     * retained on reset (true) or nullified (false)
     */
    private boolean _retainLastValueOnReset;
    
    /**
     * The time-weighted sum of the squares of the differences from the mean of all values so far
     */
    private double _sumOfSquaredDevsFromMean;

    /**
     * Constructor for a Accumulate object that will not be connected to a
     * <code>ValueSupplier</code> automatically. No specific initial value 
     * is assumed, so that the period covered effectively begins after the
     * first update.
     * 
     * @param ownerModel
     *            Model : The model this Accumulate is associated to
     * @param name
     *            java.lang.String : The name of this Accumulate object.
     * @param showInReport
     *            boolean : Flag for showing the report about this Accumulate.
     * @param showInTrace
     *            boolean : Flag for showing the trace output of this
     *            Accumulate.
     */
    public Accumulate(Model ownerModel, String name, boolean showInReport,
            boolean showInTrace) {
        // call the constructor of ValueStatistics without a connection to a
        // ValueSupplier
        super(ownerModel, name, showInReport, showInTrace);
        
        this._retainLastValueOnReset = true;
        this._pausedSince = null;
        this._pausedPeriodSinceLastReset = new TimeSpan(0);
        this._initialValue = Double.NaN;
        this._mean = Double.NaN;
        this._sumOfSquaredDevsFromMean = 0;

    }

    // ****** methods ******

    /**
     * Constructor for a Accumulate object that will not be connected to a
     * <code>ValueSupplier</code> automatically. An initial value stated
     * explicitly will be assumed after creation/reset.
     * 
     * @param ownerModel
     *            Model : The model this Accumulate is associated to
     * @param name
     *            java.lang.String : The name of this Accumulate object.
     * @param initialValue
     *            double : Value to assume initially and after resets.
     * @param showInReport
     *            boolean : Flag for showing the report about this Accumulate.
     * @param showInTrace
     *            boolean : Flag for showing the trace output of this
     *            Accumulate.
     */
    public Accumulate(Model ownerModel, String name, double initialValue, 
            boolean showInReport, boolean showInTrace) {
        // call the constructor of ValueStatistics without a connection to a
        // ValueSupplier
        this(ownerModel, name, showInReport, showInTrace);
        
        this._initialValue = initialValue;
        if (!Double.isNaN(_initialValue)) {
            this.update(_initialValue);
        }
    }
    
    /**
     * Constructor for a Accumulate object that will be connected to a
     * <code>ValueSupplier</code>, from which the values will be fetched. 
     * 
     * @param ownerModel
     *            Model : The model this Accumulate is associated to
     * @param name
     *            java.lang.String : The name of this Accumulate object
     * @param valSup
     *            ValueSupplier : The ValueSupplier providing the value for this
     *            Accumulate. The given ValueSupplier will be observed by this
     *            <code>Accumulate</code> object, if it is not observed
     *            automatically at every tick of the SimClock.
     * @param automatic
     *            boolean : Flag for observing the ValueSupplier at every tick
     *            of the SimClock. Set it to <code>true</code> to have it
     *            observed at every SimClock tick.
     * @param showInReport
     *            boolean : Flag for showing the report about this Accumulate.
     * @param showInTrace
     *            boolean : Flag for showing the trace output of this
     *            Accumulate.
     */
    public Accumulate(Model ownerModel, String name, ValueSupplier valSup,
            boolean automatic, boolean showInReport, boolean showInTrace) {
        // call the constructor of ValueStatistics
        super(ownerModel, name, valSup, showInReport, showInTrace);
        
        this._retainLastValueOnReset = true;
        this._pausedSince = null;
        this._pausedPeriodSinceLastReset = new TimeSpan(0);
        this._mean = Double.NaN;
        this._sumOfSquaredDevsFromMean = 0;

        // valSup is no valid ValueSupplier
        if (valSup == null) {
            sendWarning(
                    "Attempt to produce a Accumulate about a non existing "
                            + "ValueSupplier. The command will be ignored!",
                    "Accumulate: "
                            + this.getName()
                            + " Constructor: Accumulate"
                            + " (Model ownerModel, String name, ValueSupplier valSup, "
                            + "boolean showInReport, boolean showInTrace)",
                    "The given ValueSupplier: valSup is only a null pointer.",
                    "Make sure to pass a valid ValueSupplier when constructing a new "
                            + "Accumulate object.");

            return; // just return
        }

        if (automatic) // update at every tick of the SimClock?
        {
            // cancel the observation of the ValueSupplier
            getValueSupplier().deleteObserver(this);

            Observable simClock = this.getModel().getExperiment().getSimClock();

            simClock.addObserver(this); // observe the SimClock
        }
    }

	/**
     * Returns a Reporter to produce a report about this Accumulate.
     * 
     * @return desmoj.report.Reporter : The Reporter for this Accumulate.
     */
    public desmoj.core.report.Reporter createDefaultReporter() {
        return new desmoj.core.report.AccumulateReporter(this);
    }
    
    /**
     * This method determines whether if the accumulate's last value is
     * retained on reset (true) or nullified (false). The 
     * default value is true.
     * @return value of the retainLastValueOnReset flag
     */
    public boolean doesRetainLastValueOnReset() {
        return _retainLastValueOnReset;
    }    
    
    /**
     * Returns the mean value of all the values observed so far, weighted over
     * time.
     * 
     * @return double : The mean value of all the values observed so far,
     *         weighted over time.
     */
    public double getMean() {
        TimeInstant now = presentTime(); // what's the time?

        // has no time passed?
        if (_firstValueReadAt == null || TimeInstant.isEqual(now, _firstValueReadAt)
                || getObservations() == 0) // OR no observations are made
        {
            sendWarning(
                    "Attempt to get a mean value, but there is not "
                            + "sufficient data yet. UNDEFINED (-1.0) will be returned!",
                    "Accumulate: " + this.getName()
                            + " Method: double getMean()",
                    "You can not calculate a mean value as long as no data is collected.",
                    "Make sure to ask for the mean value only after some data has been "
                            + "collected already.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }
        
        // determine overall period measured, excluding all past and current pauses
        long periodMeasured = this.getPeriodMeasured().getTimeInEpsilon();

        // fetch current mean
        double current_mean = _mean;
        
        // update mean to reflect the period since the last update
        // (only necessary if not paused at the moment)
        if (this._pausedSince == null) {
            long periodCurrentValue = 
                TimeOperations.diff(this.presentTime(), _lastUpdate).getTimeInEpsilon();
            current_mean += (getLastValue() - _mean) / periodMeasured*periodCurrentValue;
        }
        
        // return the rounded mean value
        return round(current_mean);
    }

    /**
     * Returns the period measured (excluding pauses).
     * 
     * @return TimeSpan : The period measured (excluding pauses).
     */
    public TimeSpan getPeriodMeasured() {

        // has no time passed?
        if (_firstValueReadAt == null || getObservations() == 0) 
        {
            return new TimeSpan(0);
        }

        // determine overall period measured, excluding all past and current pauses
        TimeInstant start = _firstValueReadAt;
        TimeInstant end = (this._pausedSince == null ? this.presentTime() : this._pausedSince);
        
        TimeSpan period = TimeOperations.diff(start, end);
        period = TimeOperations.diff(period, this._pausedPeriodSinceLastReset);
        return period;
    }
    
    /**
     * Returns the standard deviation of all the values observed so far,
     * weighted over time.
     * 
     * @return double : The standard deviation of all the values observed so
     *         far, weighted over time.
     */
    public double getStdDev() {
        TimeInstant now = presentTime(); // what's the time?
        
        // is totalTimeDiff less than the minimum distinguishable span of
        // time?
        if (_firstValueReadAt == null || TimeInstant.isEqual(now, _firstValueReadAt)
                || getObservations() < 2) // OR not enough observations are
        {
            sendWarning(
                    "Attempt to get a standard deviation value, but there is "
                            + "insufficient data yet. UNDEFINED (-1.0) will be returned!",
                    "Accumulate: " + this.getName()
                            + " Method: double getStdDev()",
                    "You can not calculate a standard deviation as long as no data is "
                            + "collected.",
                    "Make sure to ask for the standard deviation only after some data "
                            + "has been collected already.");

            return UNDEFINED; // return UNDEFINED = -1.0
        }      


        // determine overall period measured, excluding all past and current pauses
        long periodMeasured = this.getPeriodMeasured().getTimeInEpsilon();
        double currentSumOfSquaredDevsFromMean = _sumOfSquaredDevsFromMean;
        
        // update mean and sum of squares of... to reflect the period since the last update
        // (only necessary if not paused at the moment)
        if (this._pausedSince == null) {
            long periodCurrentValue = 
                TimeOperations.diff(this.presentTime(), _lastUpdate).getTimeInEpsilon();
            double old_mean = _mean;
            double current_mean =  _mean + (getLastValue() - _mean)/periodMeasured*periodCurrentValue;
            currentSumOfSquaredDevsFromMean += 
                (getLastValue() - old_mean)*(getLastValue() - current_mean)*periodCurrentValue;
        }
        
        // calculate the standard deviation
        double stdDev = Math.sqrt(currentSumOfSquaredDevsFromMean/periodMeasured);
                
        // return the rounded standard deviation
        return round(stdDev);

    }
    
    /**
     * Temporarily suspend data collection to exclude a period, e.g. to disregard
     * the night period in which a plant is closed from machine utilization statistics.
     * Data collection will be resumed automatically on calling an update(...)-method.       
     */
    public void pause() {
        
        if (this._pausedSince != null) {
            sendWarning(
                    "Attempt to pause an Accumulate which is already paused."
                            + " Method call will be ignored.",
                    "Accumulate: " + this.getName()
                            + " Method: pause()",
                    "Multiple calls to pause this Accumulate.",
                    "Make sure to call pause() only once before resuming data collection "
                            + " by calling the appropriate update(...)-method.");
            return;
        }
        
        if (this._firstValueReadAt == null) {
            sendWarning(
                    "Attempt to pause an Accumulate which is not yet collecting data."
                            + " Method call will be ignored.",
                    "Accumulate: " + this.getName()
                            + " Method: pause()",
                    "Multiple calls to pause this Accumulate.",
                    "No need to pause an Accumulate that has not yet collected any data as"
                            + " data collection not will start before first update anyway.");
            return;           
        }
        
        // assume an Update now to reflect the period between last Update and now,
        // unless there has already been an update at this instant.
        if (!TimeInstant.isEqual(this._lastUpdate, this.presentTime())) {
            this.update(this.getLastValue());
            this.incrementObservations(-1); //...not counting as observation
        }
        
        // remember instant at which the pause has started 
        this._pausedSince = this.presentTime();
    }
    
    /**
     * Resets this Accumulate object by resetting all variables to 0.0.
     * If the flag retainLastValueOnReset is set to true, the last value
     * is not nullified but remains unchanged. 
     */
    public void reset() {
        double lastValue = this.getLastValue();
        boolean observationsPriorToReset = (getObservations() > 0); 
        
        super.reset(); // reset the ValueStatistics, too.
        
        this._mean = Double.NaN;
        this._sumOfSquaredDevsFromMean = 0;

        _firstValueReadAt = null;
        _lastUpdate = presentTime();
        _pausedSince = null;
        _pausedPeriodSinceLastReset = new TimeSpan(0);
        
        if (doesRetainLastValueOnReset() && observationsPriorToReset) {
            this.update(lastValue);
        } else if (!Double.isNaN(_initialValue)) {
            this.update(_initialValue);
        }
    }

    /**
     * Sets the value of the retainLastValueOnResetFlag.
     * @param retainValue new value of the flag.
     */
    public void setRetainLastValueOnReset(boolean retainValue) {
        this._retainLastValueOnReset = retainValue;
    }

    /**
     * Updates this <code>Accumulate</code> object by fetching the actual
     * value of the <code>ValueSupplier</code> and processing it. The
     * <code>ValueSupplier</code> can be passed in the constructor of this
     * <code>Accumulate</code> object. This <code>update()</code> method
     * complies with the one described in DESMO, see [Page91].
     */
    public void update() {
        // check if the experiment is already running
        if (!getModel().getExperiment().isRunning()) {
            return; // experiment is not running, don't update, just return
        }

        TimeInstant now = presentTime(); // what's the time?

        // how long since the last update or reset?
        long periodValueValidEps = TimeOperations.diff(now, _lastUpdate).getTimeInEpsilon();

        // get hold of the value that was valid until now
        double untilNowVal = getLastValue();

        // update the ValueStatistics
        super.update(); 
                
        // process update
        this.internalUpdate(untilNowVal, periodValueValidEps);
    }

    /**
     * Updates this <code>Accumulate</code> object with the double value given
     * as parameter. In some cases it might be more convenient to pass the value
     * this <code>Accumulate</code> will be updated with directly within the
     * <code>update(double val)</code> method instead of going via the
     * <code>ValueSupplier</code>.
     * 
     * @param val
     *            double : The value with which this <code>Accumulate</code>
     *            will be updated.
     */
    public void update(double val) {
        
        // check if the experiment is already running
        if (!getModel().getExperiment().isRunning()) {
            return; // experiment is not running, don't update, just return
        }

        TimeInstant now = presentTime(); // what's the time?

        // how long since the last update or reset?
        long periodValueValidEps = TimeOperations.diff(now, _lastUpdate).getTimeInEpsilon();

        // get hold of the value that was valid until now
        double untilNowVal = getLastValue();

        // update the ValueStatistics
        super.update(val);
        
        // process update
        this.internalUpdate(untilNowVal, periodValueValidEps);
    }
    
    /**
     * Implementation of the virtual <code>update(Observable, Object)</code>
     * method of the <code>Observer</code> interface. This method will be
     * called automatically from an <code>Observable</code> object within its
     * <code>notifyObservers()</code> method. <br>
     * If no Object (a<code>null</code> value) is passed as arg, the actual
     * value of the ValueSupplier will be fetched with the <code>value()</code>
     * method of the ValueSupplier. Otherwise it is expected that the actual
     * value is passed in the Object arg.
     * 
     * @param o
     *            java.util.Observable : The Observable calling this method
     *            within its own <code>notifyObservers()</code> method.
     * @param arg
     *            Object : The Object with which this <code>Accumulate</code>
     *            is updated. Normally a Double value which is added to the
     *            statistics or <code>null</code>.
     */
    public void update(Observable o, Object arg) {
        
        if (o == null) // null was passed instead of an Observable
        {
            sendWarning(
                    "Attempt to update a Accumulate with no reference to an "
                            + "Observable. The actual value of '"
                            + getValueSupplier().getName()
                            + "' will be fetched and processed anyway.",
                    "Accumulate: " + this.getName()
                            + " Method: update (Observable o," + " Object arg)",
                    "The passed Observable: o in this method is only a null pointer.",
                    "The update()-method was not called via notifyObservers() from an "
                            + "Observable. Who was calling it? Why don't you let the Observable do"
                            + " the work?");
        }

        // check if the experiment is already running
        if (!getModel().getExperiment().isRunning()) {
            return; // experiment is not running, don't update, just return
        }

        TimeInstant now = presentTime(); // what's the time?

        // how long since the last update or reset?
        long periodValueValidEps = TimeOperations.diff(now, _lastUpdate).getTimeInEpsilon();

        // get hold of the value that was valid until now
        double untilNowVal = getLastValue();

        // update the ValueStatistics
        super.update(o, arg); 
        
        // process update
        this.internalUpdate(untilNowVal, periodValueValidEps);
    }
    
    /**
     * Internal method to update the time-weighted mean and sum of the squares of the 
     * differences from the mean of values so far with a new sample.
     * 
     * @param value
     *            double : The new sample.
     * @param periodValueValid
     *            long : The length of the period in epsilon during which the sample was valid.
     */
    private void internalUpdate(double value, long periodValueValid) {
        
        // end of a pause?
        if (this._pausedSince != null) {
            
            // determine pause duration
            TimeSpan pauseDuration = TimeOperations.diff(_pausedSince, presentTime());
            
            // update total pause duration
            _pausedPeriodSinceLastReset = TimeOperations.add(_pausedPeriodSinceLastReset, pauseDuration);
            
            // no longer paused
            this._pausedSince = null;

        // no pause: maybe is the first update=     
        } else if (this._firstValueReadAt == null) {
            
            // just store the instant as there is no old value to apply to past period
            this._firstValueReadAt = this.presentTime();
        
        // normal case: second+ call, no pause    
        } else {
                    
            // ignore values valid for a zero period
            if (periodValueValid > 0L) {
            
                if (Double.isNaN(this._mean)) { // First entry
                    _mean = value;
                    _sumOfSquaredDevsFromMean = 0.0;
                } else { // Further entries
                    long periodMeasured = getPeriodMeasured().getTimeInEpsilon();
                    double _m_old = _mean;
                    _mean += (value - _mean)/periodMeasured*periodValueValid;
                    _sumOfSquaredDevsFromMean += (value - _m_old)*(value - _mean)*periodValueValid;
                }
            }
            
        }
        
        // store update instant
        _lastUpdate = this.presentTime(); // update the time of the last change
    }   
} // end class Accumulate