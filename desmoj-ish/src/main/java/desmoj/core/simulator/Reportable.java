package desmoj.core.simulator;

import java.lang.reflect.Constructor;

import desmoj.core.report.Reporter;
import desmoj.core.report.StandardReporter;

/**
 * All classes that want to publish their information have to extend this class
 * in order to provide the necessary functions to represent their information in
 * reports.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler, modified by Chr. M&uuml;ller (TH Wildau) 28.11.2012
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
public abstract class Reportable extends ModelComponent {

    /**
     * schedulable (e.g. entity) corresponding to this reportable maybe null if no
     * schedulable corresponds to this reportable
     */
    private Schedulable _correspondingSchedulable = null;

    /**
     * stores the last time that this reportable object was reset.
     */
    private TimeInstant _lastReset;

    /**
     * This Reportable's Reporter
     */
    private Reporter _myReporter = null;

    /**
     * The number of observations that this reportable can report about
     */
    private long _observations;

    /**
     * A class to instantiate a custom reporter from
     */
    private Class<? extends Reporter> _reporter = null;

    /**
     * Flag indicating if this reportable should produce a report.
     */
    private boolean _reportMode;

    /**
     * A description (optional) / 21.11.12 extended bx cm
     */
    private String description;

    /**
     * Creates a reportable object with all parameters required. The reportable
     * registers itself at the given model
     * 
     * @param name         java.lang.String : The name of this reportable
     * @param owner        Model : The model this reportable is associated to
     * @param showInReport boolean : Flag for showing the report. Set it to
     *                     <code>true</code> if reportable should show up in report.
     *                     Set it to <code>false</code> if reportable should not be
     *                     shown in report.
     * @param showInTrace  boolean : Flag for showing this reportable in trace
     *                     files. Set it to <code>true</code> if reportable should
     *                     show up in trace. Set it to <code>false</code> if
     *                     reportable should not be shown in trace.
     */
    public Reportable(Model owner, String name, boolean showInReport, boolean showInTrace) {

        super(owner, name, showInTrace); // create the ModelComponent
        _reportMode = showInReport; // set report flag
        _observations = 0; // reset observations counter

        // Registration even if showInReport=false!
        if ((owner != null)) {
            owner.register(this);
        }

        // register current time as reset
        // in case experiment running
        if (owner != null && owner.getExperiment() != null && owner.getExperiment().isRunning()) {
            _lastReset = presentTime();
        }
    }

    /**
     * This method provides a reporter to generate report data about this
     * reportable. In this general implementation, a <code>StandardReporter</code>
     * is returned. Lacking further knowledge about a specific reportable, the
     * <code>StandardReporter</code> is only able to read out the reportable's name,
     * number of observations and time of last reset.<br/>
     * 
     * Thus, it is highly recommended that subclasses override this method to return
     * an appropriate default reporter for the specific reportable object. All
     * reportable objects in DESMO-J like <code>Accumulate</code>,
     * <code>Tally</code>, <code>Count</code>, <code>Queue</code>, and many others
     * provide such reporters. <br/>
     * 
     * If you want to extend the framework with new reportable objects, provide a
     * suitable reporter as well and override this method to return an instance of
     * the reporter. <br/>
     * 
     * If you just need to replace the reporter used by an existing reportable
     * (while not requiring any changes to the reportable itself), there is no need
     * to create a subclass of the reportable: Instead of overriding this method,
     * just call <code>setReporter()</code> to have this reportable use a custom
     * reporter instead of the default reporter.
     * 
     * @return desmoj.core.report.Reporter : A specific reporter for this reportable
     */
    public Reporter createDefaultReporter() {

        return new StandardReporter(this);
    }

    /**
     * Gets the schedulable (e.g. entity) corresponding to this reportable; may be
     * null if no schedulable corresponds to this reportable.
     * 
     * @return Schedulable : The schedulable (e.g. entity) corresponding to this
     *         reportable (may be null if no schedulable corresponds to this
     *         reportable!)
     */
    public Schedulable getCorrespondingSchedulable() {
        return _correspondingSchedulable;
    }

    /**
     * Get an optional description of reported value. Default is null. This value is
     * shown in description reports, only when set.
     *
     * @return
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the number of observations made by the reportable object.
     * 
     * @return long : The number of observations made by the reportable object.
     */
    public long getObservations() {

        return _observations;

    }

    /**
     * Returns an instance of a reporter for this reportable. Note that every
     * reportable has to provide a default reporter, see method
     * <code>createDefaultReporter()</code>. The reporter returned by this method is
     * such a default reporter.
     * 
     * @return desmoj.core.report.Reporter : A reporter for this reportable
     */
    public final Reporter getReporter() {

        if (_myReporter != null)
            return _myReporter;

        if (_reporter == null) {
            _myReporter = createDefaultReporter();
            return _myReporter;
        }

        try {
            Constructor<?> constructor = _reporter.getConstructor(new Class[] { Reportable.class });
            constructor.setAccessible(true);
            _myReporter = (Reporter) constructor.newInstance(new Object[] { this });

        } catch (Exception e) {
            this.sendWarning("Instanciating the user-specified reporter for this reportable caused an exception. Using the default reporter instead.",
                             "Reportable : " + getName() + " Method: Reporter getReporter()",
                             "User-specified class is not accessible or constructor cannot be invoked.",
                             "Make sure provide an appropriate reporter class if you want to replace the default reporter. "
                             + "Such a reporter has to provide a constructor requiring a reference to this reportable as only parameter");
            _myReporter = createDefaultReporter();
        }
        return _myReporter;
    }

    /**
     * Increments the number of observations made by this reportable by one (1).
     */
    public void incrementObservations() {

        _observations++;

    }

    /**
     * Increments the number of observations by the amount given as parameter.
     * 
     * @param multiObservations long : The number to increase the number of
     *                          observations by
     */
    public void incrementObservations(long multiObservations) {

        _observations += multiObservations;

    }

    /**
     * Checks if this reportable produces a report.
     * 
     * @return boolean : true if report will be produced, false otherwise
     */
    public boolean reportIsOn() {

        return _reportMode;

    }

    /**
     * Switches report mode to prevent this reportable to produce reports.
     */
    public void reportOff() {

        _reportMode = false;

    }

    /**
     * Switches report mode of this reportable on to produce a report.
     */
    public void reportOn() {

        _reportMode = true;

    }

    /**
     * Resets the counter for observations made by this reportable. The point of
     * simulation time this method was called will be stored and can be retrieved
     * using method <code>resetAt()</code>.
     */
    public void reset() {

        _observations = 0; // reset observations
        _lastReset = presentTime(); // register the reset time
    }

    /**
     * Shows the point in simulation time when the last reset of this reportable was
     * made.
     * 
     * @return TimeInstant : The point of simulation time of the last reset.
     */
    public TimeInstant resetAt() {

        return _lastReset;
    }

    /**
     * Sets the schedulable (e.g. entity) corresponding to this Reportable. May be
     * null if no schedulable corresponds to this reportable. If set, the
     * schedulable must have the same model as this reportable! A Reportable needs
     * not have a corresponding schedulable.
     *
     * @param correspondingSchedulable Schedulable : The Schedulable corresponding
     *                                 to this Reportable.
     */
    public void setCorrespondingSchedulable(Schedulable correspondingSchedulable) {

        if (this instanceof Model) {
            this.sendWarning("A Model may not have a corresponding schedulable. Method call ignored.",
                             "Reportable.setCorrespondingSchedulable(Schedulable)",
                             "A Model may not have a corresponding schedulable, because Model contains many corresponding schedulables.",
                             "Do not set corresponding schedulable to a model!");
            return;
        }

        if (correspondingSchedulable != null && this.getModel() != correspondingSchedulable.getModel()) {
            this.sendWarning("Schedulable to correspond to this Reportable must belong to the same model!",
                             "Reportable.setCorrespondingSchedulable(Schedulable)",
                             "Model of Reportable and corresponding schedulable must be identical.",
                             "Do not set a corresponding schedulable to another model's Schedulable.");
            return;
        }
        this._correspondingSchedulable = correspondingSchedulable;
    }

    // Extension by Chr. M&uuml;ller (TH Wildau) 28.11.12
    // -------------------------------------

    /**
     * Set an optional description of reported value. Default is null. This value is
     * shown in description reports, only when set.
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Specifies a Reporter-Class to be used as reporter by this reportable. Note
     * that such a Reporter has to provide a constructor requiring a reference to
     * this object (i.e. the reportable to report about) as only parameter.<br/>
     * If this method is never called, a default reporter as obtained from
     * <code>createDefaultReporter()</code> will be used.
     * 
     * @param reporterClass the reporter's class
     */
    public void setReporter(Class<? extends Reporter> reporterClass) {
        _reporter = reporterClass;
    }

}
