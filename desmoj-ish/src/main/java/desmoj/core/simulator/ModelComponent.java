package desmoj.core.simulator;

import java.util.List;

import desmoj.core.report.DebugNote;
import desmoj.core.report.ErrorMessage;
import desmoj.core.report.Message;
import desmoj.core.report.TraceNote;

/**
 * Encapsulates all information relevant to each component of a model. Its basic
 * intention is to connect each modelcomponent to a single Model object as the
 * owner of this modelcomponent. Through this connection all relevant
 * information about that Model can be retrieved. It is part of the composite
 * design pattern as described in [Gamm97] page 163 in which it represents the
 * component class.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
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
public class ModelComponent extends NamedObject {

	/**
	 * Flag indicating if this modelcomponent should be listed in the debug
	 * output file.
	 */
	private boolean _debugMode;

	/**
	 * The reference to the model that this modelcomponent belongs to.
	 */
	private Model _owner;

	/**
	 * Flag indicating if this modelcomponent should be listed in the trace
	 * output file.
	 */
	private boolean _traceMode;

	/**
	 * Constructs a modelcomponent with the given String as name and the given
	 * model as the associated owner of this component. Components can only be
	 * created after the corresponding model object has been instantiated. The
	 * default preset for the showInTrace option is <code>false</code>.
	 * 
	 * @param name
	 *            java.lang.String : The name of the component
	 * @param ownerModel
	 *            Model : The model this component is associated to.
	 */
	public ModelComponent(Model ownerModel, String name) {

		// create a ModelComponent with the given name and no trace output
		this(ownerModel, name, false);
	}

	/**
	 * Constructs a modelcomponent with the given String as name and the given
	 * model as the associated owner of this component. Components can thus only
	 * be created after the corresponding model object has been instantiated.
	 * 
	 * @param name
	 *            java.lang.String : The name of the component
	 * @param ownerModel
	 *            Model : The model this component is associated to
	 * @param showInTrace
	 *            boolean : Flag for showing component in trace-files. Set it to
	 *            <code>true</code> if component should show up in trace. Set it
	 *            to <code>false</code> if component should not be shown in
	 *            trace.
	 */
	public ModelComponent(Model ownerModel, String name, boolean showInTrace) {

		super(name); // create the namedObject with given name
		_owner = ownerModel; // set the owner of this component
		_traceMode = showInTrace; // set the tracemode for this component

	}

	/**
	 * Returns the currently active Schedulable object that is handled by the
	 * scheduler.
	 * 
	 * @return Schedulable : The current Schedulable object.
	 */
	public Schedulable current() {

		return _owner.getExperiment().getScheduler().getCurrentSchedulable();

	}

	/**
     * Returns the currently active Entity. Returns <code>null</code> if the current
     * Schedulable happens to be an external event or a SimProcess.
     * Note that in case the current Event refers to more than one entity
     * (<code>EventTwoEntitties</code>, <code>EventThreeEntitties</code>),
     * only the first entity is returned; to obtain all such entities,
     * use <code>getAllCurrentEntities()</code> instead.
     * 
     * @return Entity : The currently active Entity or
     *         <code>null</code> in case of an external event or a SimProcess
     *         being the currently active Schedulable
     */
	public Entity currentEntity() {

		return _owner.getExperiment().getScheduler().getCurrentEntity();

	}
	
    /**
     * Returns the currently active entities. Returns an empty list 
     * if the current Schedulable happens to be an external event or a SimProcess.
     * 
     * @return List<Entity> : A list containing the currently active entities
     */
    public List<Entity> currentEntityAll() {

        return _owner.getExperiment().getScheduler().getAllCurrentEntities();

    }

	/**
	 * Returns the currently active Event that is handled by the scheduler. It
	 * returns <code>null</code> if a process Event is the current active
	 * Schedulable, thus no Event is active.
	 * 
	 * @return Event : The current active Event or <code>null</code> if the
	 *         current active Schedulable is a process
	 * 
	 */
	public EventAbstract currentEvent() {

		return _owner.getExperiment().getScheduler().getCurrentEvent();

	}

	/**
	 * Returns the model that the currently active Event or Entity handled by
	 * the scheduler belongs to or the main model connected to the experiment,
	 * if no model can be returned by the scheduler.
	 * 
	 * @return Model : The current active or the main model connected to the
	 *         experiment, if no model can be returned by the scheduler
	 */
	public Model currentModel() {

		Model mBuff = _owner.getExperiment().getScheduler().getCurrentModel();

		if (mBuff != null)
			return mBuff;
		else
			return _owner.getExperiment().getModel();

	}

	/**
	 * Returns the currently active SimProcess that is handled by the scheduler.
	 * 
	 * @return SimProcess : The current active SimProcess.
	 */
	public SimProcess currentSimProcess() {

		return _owner.getExperiment().getScheduler().getCurrentSimProcess();

	}

	/**
	 * Shows if this modelcomponent currently produces debug output.
	 * 
	 * @return boolean : true, if modelcomponent shows in debug, false if not
	 */
	public boolean debugIsOn() {

		return _debugMode; // has anybody ever returned from a debugMode...

	}

	/**
	 * Switches off debug output for this modelcomponent. Does nothing if trace
	 * is already switched off.
	 */
	public void debugOff() {

		_debugMode = false; // yep, that's it!

	}

	/**
	 * Switches on debug output for this modelcomponent. Does nothing if debug
	 * is already switched on.
	 */
	public void debugOn() {

		_debugMode = true; // yep, that's true!

	}

	/**
	 * Returns the model that owns this component.
	 * 
	 * @return Model : The model that this component is associated to
	 */
	public Model getModel() {

		return _owner; // "Make all things as simple as possible : but not
		// simpler!"
		// Albert Einstein

	}

	/**
	 * Tests if the modelcomponent given as parameter is a component of the same
	 * experiment as this modelcomponent.
	 * 
	 * @return boolean : true, if this modelcomponent belongs to the same
	 *         experiment as this modelcomponent, false otherwise
	 * @param other
	 *            ModelComponent : the other modelcomponent to check
	 *            compatibility with
	 */
	public boolean isExperimentCompatible(ModelComponent other) {

		// Checks if this modelcomponent has same experiment as other
		// modelcomponent
		return (_owner.getExperiment() == other.getModel().getExperiment());

	}

	/**
	 * Tests if the modelcomponent given as parameter is a component of the same
	 * model as this modelcomponent.
	 * 
	 * @return boolean :<code>true</code>, if this modelcomponent belongs to the
	 *         same model as the given modelcomponent, <code>false</code>
	 *         otherwise
	 * @param other
	 *            ModelComponent : the other modelcomponent to check
	 *            compatibility with
	 */
	public boolean isModelCompatible(ModelComponent other) {

		// since checking for compatibility is the models's responsibility,
		// we just pass checking on to our owner.
		return _owner.checkCompatibility(other);

	}

	/**
	 * Returns the current simulation time as displayed by the simulation clock
	 * responsible for this modelcomponent.
	 * 
	 * @return TimeInstant : The current point of simulation time
	 */
	public TimeInstant presentTime() {
		return _owner.getExperiment().getSimClock().getTime();
	}

	/**
	 * Creates and sends a debugnote to the experiment's messagedistributor.
	 * Debugnotes express the internal state of a modelcomponent to visualize
	 * the changes of state to help find bugs. Classes <code>Scheduler</code>
	 * and <code>Queue</code> both produce debugnotes if set to do so
	 * representing the data stored inside them. The information about the
	 * simulation time is extracted from the experiment and must not be given as
	 * a parameter.
	 * 
	 * @param description
	 *            java.lang.String : The description of a modelcomponent's
	 *            internal state to be passed with this debugnote
	 */
	public void sendDebugNote(String description) {
		// send debug message only if debug mode of this model component
		// and debug output is activated
		// This bugfix was contributed by Heine Kolltveit
		if (currentlySendDebugNotes()) {
			sendMessage(new DebugNote(getModel(), presentTime(), getName(),
					description));
		}
	}

	/**
	 * Sends a message to the messagedistributor handled by the experiment. This
	 * modelcomponent must already be connected to an experiment in order to
	 * have a messagedistributor available to send this message to and an
	 * appropriate messagereceiver must already be registered at the
	 * messagedistributor to receive that type of message passed on to it. If no
	 * messaging subsystem is available to this modelcomponent, then the mesage
	 * is printed to the standard <code>out</code> printstream as configured in
	 * the local Java runtime environment of the computer this simulation is
	 * running on. Note that there are shorthands for sending the standard
	 * DESMO-J messages. These methods create and send the appropriate Message
	 * on-the-fly:
	 * <ul>
	 * <li><code>sendTraceNote()</clode> to send a tracenote</li>
	 * <li><code>sendDebugNote()</code> to send the data needed to debug models</li>
	 * <li><code>sendWarning()</code> to send an errormessage that does not stop
	 * the experiment</li>
	 * </ul>
	 * 
	 * @param m
	 *            Message : The message to be transmitted
	 * @see ModelComponent#sendTraceNote
	 * @see ModelComponent#sendDebugNote
	 * @see ModelComponent#sendWarning
	 */
	public void sendMessage(Message m) {

		if (m == null) {
			sendWarning("Can't send Message!", "ModelComponent : " + getName()
					+ " Method: SendMessage(Message m)",
					"The Message given as parameter is a null reference.",
					"Be sure to have a valid Message reference.");
			return; // no proper parameter
		}

		if (_owner != null) { // is modelcomponent connected to model?

			if (_owner.getExperiment() != null) { // is model connected to
				// Experiment?

				getModel().getExperiment().getMessageManager().receive(m);
				return;
			}
		}

		// if not connected to messaging system, write to standard out
		System.out.println(m);

	}

	/**
	 * Creates and sends a tracenote to the experiment's messagedistributor. The
	 * information about the simulation time, model and component producing this
	 * tracenote is extracted from the experiment and must not be given as
	 * parameters.
	 * 
	 * @param description
	 *            java.lang.String : The description of the tracenote
	 */
	public void sendTraceNote(String description) {
		// send trace message only if trace mode of this model component
		// and trace output is activated
		// This bugfix was contributed by Heine Kolltveit
	    
	    String mode = ""; // no special mode
	    
	    if (currentModel().isConnected() && currentModel().getExperiment().isPreparing())
	        mode = "initially ";
	    
		if (currentlySendTraceNotes()) {
			sendMessage(new TraceNote(currentModel(), mode + description,
					presentTime(), currentEntityAll(), currentEvent()));
		}
	}

	/**
	 * Creates and sends an error message to warn about a erroneous condition in
	 * the DESMO-J framework to the experiment's messagedistributor. Be sure to
	 * have a correct location, since the object and method that the error
	 * becomes apparent is not necessary the location it was produced in. The
	 * information about the simulation time is extracted from the Experiment
	 * and must not be given as a parameter.
	 * 
	 * @param description
	 *            java.lang.String : The description of the error that occured
	 * @param location
	 *            java.lang.String : The class and method the error occured in
	 * @param reason
	 *            java.lang.String : The reason most probably responsible for
	 *            the error to occur
	 * @param prevention
	 *            java.lang.String : The measures a user should take to prevent
	 *            this warning to be issued again
	 */
	public void sendWarning(String description, String location, String reason,
			String prevention) {

		// comnpose the ErrorMessage and send it in one command
		sendMessage(new ErrorMessage(getModel(), description, location, reason,
				prevention, presentTime()));

	}

	/**
	 * Skips the next tracenote. The next tracenote produced by any object in
	 * the DESMO-J framework will not be distributed by the experiment's
	 * messagemanager. This is necessary for some operations to hide the
	 * framework's actions and thus not confuse the modeller.
	 */
	public void skipTraceNote() {

		skipTraceNote(1);

	}

	/**
	 * Skips the next number of tracenotes. The next -numSkipped - number of
	 * tracenotes produced by any object in the DESMO-J framework will not be
	 * distributed by the experiment's messagemanager. This is necessary for
	 * some operations to hide the framework's actions and thus not confuse the
	 * modeller.
	 * 
	 * @param numSkipped
	 *            int : The number of future tracenotes to be skipped
	 */
	public void skipTraceNote(int numSkipped) {

		if (numSkipped < 1)
			return; // nothing to do or negative (illegal) param.
		
		if (!currentlySendTraceNotes())
		    return; // not sending trance notes anyway

		try {
			getModel().getExperiment().getMessageManager().skip(
					Class.forName("desmoj.core.report.TraceNote"), numSkipped);
		} catch (ClassNotFoundException cnfx) {
			throw new desmoj.core.exception.SimAbortedException(
					new ErrorMessage(
							getModel(),
							"Can not skip tracenotes! Simulation aborted.",
							"ModelComponent : " + getName()
									+ " Method : skipTraceNote()",
							"The file for class desmoj.report.TraceNote can not be found by "
									+ "the Java runtime. Following exception was caught : "
									+ cnfx.toString(),
							"Check that all pathnames for DESMOJ are set in your environment.",
							presentTime()));

		}

	}

	/**
	 * Shows if this modelcomponent currently produces trace output.
	 * 
	 * @return boolean : true, if modelcomponent shows in trace, false if not
	 */
	public boolean traceIsOn() {

		return _traceMode;

	}

	/**
	 * Switches off trace output for this modelcomponent. Does nothing if trace
	 * is already switched off.
	 */
	public void traceOff() {

		_traceMode = false; // yep, that's it!

	}

	/**
	 * Switches on trace output for this modelcomponent. Does nothing if trace
	 * is already switched on.
	 */
	public void traceOn() {

		_traceMode = true; // yep, that's it!

	}

	/**
	 * returns true if this model component should currently send debug notes
	 * (i.e. experiment and the component are both in debug mode).
	 * 
	 * @return
	 */
	protected boolean currentlySendDebugNotes() {
		return debugIsOn() && getModel().getExperiment().debugIsOn();
	}

	/**
	 * returns true if this model component should currently send trace notes
	 * (i.e. experiment and the component are both in trace mode).
	 * 
	 * @return
	 */
	protected boolean currentlySendTraceNotes() {
		return traceIsOn() && getModel().getExperiment().traceIsOn();
	}

	/**
	 * Sets the owner of a modelcomponent to the given reference. This is
	 * exclusively needed to build a self-reference for the main model of an
	 * experiment. Has to be delegated to class <code>modelcomponent</code>
	 * since the owner attribute is encapsulated in this class.
	 * 
	 * @param newOwner
	 *            desmoj.Model : The modelcomponent's new owner
	 */
	void setOwner(Model newOwner) {

		_owner = newOwner;

	}
}