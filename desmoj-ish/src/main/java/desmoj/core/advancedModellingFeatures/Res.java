package desmoj.core.advancedModellingFeatures;

//34567890123456789012345678901234567890123456789012345678901234567890123456

import java.util.Enumeration;
import java.util.Vector;


import desmoj.core.simulator.Model;
import desmoj.core.simulator.QueueBased;
import desmoj.core.simulator.QueueList;
import desmoj.core.simulator.QueueListFifo;
import desmoj.core.simulator.QueueListLifo;
import desmoj.core.simulator.QueueListRandom;
import desmoj.core.simulator.Resource;
import desmoj.core.simulator.ResourceDB;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import desmoj.core.statistic.StatisticObject;

/**
 * Res is the place where resources are stored in a pool. Processes can come by
 * and the resource pool will <code>provide()</code> resources to them. Each
 * process has to give back the same resources it once has acquired by calling
 * the <code>takeBack()</code> method of the Res. Res is used to implement
 * process synchronization between processes, which are using resources. The
 * resource pool has a limited capacity. A process can acquire one or more
 * resources and use them. After usage the process must release this or these
 * same resources to make them available to other processes. If a process can
 * not get the number of resources needed, it has to wait in a queue until
 * enough resources are released by other processes. A process can release its
 * resources anytime. After the resourcepool has <code>"takenBack"()</code> the
 * used resources the waiting-queue is checked for processes waiting for them.
 * The first sort criterion of the queue is highest queueing priorities first
 * (i.e. not using scheduling priorities - note that this is a somewhat arbitrary 
 * choice, as the <ode>Res</code> combines queueing and scheduling features).
 * The second criterion, if a tie-breaker is needed, is the queueing discipline 
 * of the underlying queue, e.g. FIFO. The capacity limit can be determined by the user.
 * <code>Res</code> is derived from <code>QueueBased</code>, which provides all the statistical functionality
 * for a queue.
 * 
 * @see QueueBased
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen
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

public class Res extends desmoj.core.simulator.QueueBased {

	// ****** attributes ******

	/**
	 * UsedResources is an inner class of Res to encapsulate the pairs of:
	 * Sim-process and an array of resources it holds. These pairs are stored in
	 * the vector <code>arrayOfUsedResources</code>.
	 */
	private static class UsedResources extends java.lang.Object {

		// ****** attributes of inner class ******

		/**
		 * The array of resources occupied by the SimProcess. In fact the array
		 * is a java.util.Vector because one does not know how many resources
		 * the SimProcess holds and the number of held resources might vary.
		 */
		private Vector<Resource> occupiedResources;

		/**
		 * The SimProcess using the resources at the moment.
		 */
		private SimProcess process;

		// ****** methods of inner class ******

		/**
		 * Constructor for a UsedResources object.
		 * 
		 * @param sProc
		 *            SimProcess : The SimProcess holding the resources.
		 * @param occupiedRes
		 *            java.util.Vector : The resources occupied by the
		 *            SimProcess.
		 */
		protected UsedResources(SimProcess sProc, Vector<Resource> occupiedRes) {
			// init variables
			this.process = sProc;
			this.occupiedResources = occupiedRes;
		}

		/**
		 * Returns the array of resources occupied by the SimProcess.
		 * 
		 * @return java.util.Vector : The array of resources occupied by the
		 *         SimProcess.
		 */
		protected Vector<Resource> getOccupiedResources() {
			return this.occupiedResources;
		}

		/**
		 * Returns the SimProcess which holds a number of resources.
		 * 
		 * @return SimProcess : The SimProcess which holds a number of
		 *         resources.
		 */
		protected SimProcess getProcess() {
			return this.process;
		}
	} // end inner class

	/**
	 * The number identifying a Res object. Because it is a class variable each
	 * <code>Res</code> will get its own ID number starting by zero.
	 */
	private static long resNumber = 0;

	/**
	 * The queue, actually storing the processes waiting for resources
	 */
	protected QueueList<SimProcess> _queue;

	/**
	 * The vector holding all the pairs (used resources of this Res, the
	 * Sim-process which holds the resources at the moment). See: inner class
	 * UsedResources
	 */
	private Vector<UsedResources> _arrayOfUsedResources;

	/**
	 * Number of resources available at the moment
	 */
	private int _avail;

	/**
	 * To indicate whether the check for deadlocks is active or not. Default is
	 * <code>true</code>= deadlock check enabled.
	 */
	private boolean _deadlockCheck = true;

	/**
	 * Is set to <code>true</code> if a deadlock is detected where this Res is
	 * involved in. Otherwise it remains <code>false</code>. Default is
	 * <code>false</code>.
	 */
	private boolean _deadlockDetected = false;

	/**
	 * The ID number of this <code>Res</code> object.
	 */
	private long _idNumber;

	/**
	 * The last time the Res has been used
	 */
	private TimeInstant _lastUsage;

	/**
	 * The number of resources in the Res (capacity)
	 */
	private int _limit;

	/**
	 * The minimum number of resources being available
	 */
	private int _minimum;

	/**
	 * Flag to indicate whether an entity can pass by other entities in the
	 * queue which are enqueued before that entity in the queue. Is
	 * <code>false</code> as default value.
	 */
	private boolean _passBy = false;

	/**
	 * Counter for the SimProcesses which are refused to be enqueued, because
	 * the queue capacity is full.
	 */
	private long _refused;

	/**
	 * The resource database keeping track of which SimProcesses holding which
	 * resources and SimPorcesses requesting resources.
	 */
	private ResourceDB _resourceDB;

	/**
	 * The vector holding all the resources of this resource pool not used at
	 * the moment.
	 */
	private Vector<Resource> _unUsedResources;

	/**
	 * Number of processes having acquired and released one or more resources
	 */
	private long _users;

	/**
	 * Indicates the method where something has gone wrong. Is passed as a
	 * parameter to the method <code>checkProcess()</code>.
	 */
	private String _where;

	// ****** inner class ******

	/**
	 * Weighted sum of available resources (in the Res over the time)
	 */
	private double _wSumAvail;

	/**
	 * Constructor for a Res with a number of initial resources in it. The
	 * underlying queue has a Fifo queueing discipline and unlimited capacity.
	 * 
	 * @param owner
	 *            Model : The model this Res is associated to.
	 * @param name
	 *            java.lang.String : The Res's name
	 * @param capacity
	 *            int : The number of resources the Res starts with. Must be
	 *            positive and greater than 0.
	 * @param showInReport
	 *            boolean : Flag, if Res should produce a report or not.
	 * @param showInTrace
	 *            boolean : Flag for trace to produce trace messages.
	 */
	public Res(Model owner, String name, int capacity, boolean showInReport,
			boolean showInTrace) {
		super(owner, name, showInReport, showInTrace); // construct QueueBased

		_idNumber = resNumber++; // increment the resNumber and get it as
		// IDNumber

		// make an actual queue and give it a reference of this "QueueBased"-Res
		_queue = new QueueListFifo<SimProcess>();
		_queue.setQueueBased(this);

		// construct a vector to hold all the UnUsedResources at the moment
		_unUsedResources = new Vector<Resource>();

		// construct a vector to hold the UsedResources (see the inner class)
		_arrayOfUsedResources = new Vector<UsedResources>();

		// get a reference to the resource database
		_resourceDB = owner.getExperiment().getResourceDB();

		this._limit = capacity;
		this._minimum = capacity;
		this._avail = capacity;
		this._users = 0;
		this._wSumAvail = 0.0;
		this._refused = 0;
		this._lastUsage = presentTime();

		if (capacity <= 0) // nothing or less in the resource pool, you fool!
		{
			sendWarning(
					"Attempt to construct a Res with nothing or a negativ"
							+ " number of resources. Initial number of resources is set to one!",
					"Res: "
							+ getName()
							+ " Constructor: Res (desmoj.Model owner, String name, "
							+ "int capacity, boolean showInReport, boolean showInTrace)",
					"A negative number of resources does not make sense here.",
					"Make sure to initialize the capacity of a Res always with"
							+ " a positive number of resources.");

			_limit = _minimum = _avail = 1; // set it to 1, that makes more
			// sense
		}

		// make the resource objects and store them in the vector of unused
		// resources
		for (int i = 0; i < capacity; i++) {
			// make the resources and give them the name of the Res pool
			Resource aResource = new Resource(owner, name, this, true);

			_unUsedResources.addElement(aResource);
		}
	}

	// ****** methods ******

	/**
	 * Constructor for a Res with a number of initial resources in it. The
	 * queueing discipline and the capacity limit of the underlying queue can be
	 * chosen, too.
	 * 
	 * @param owner
	 *            Model : The model this Res is associated to.
	 * @param name
	 *            java.lang.String : The Res's name
	 * @param sortOrder
	 *            int : determines the sort order of the underlying queue
	 *            implementation. Choose a constant from <code>QueueBased</code>
	 *            like <code>QueueBased.FIFO</code> or
	 *            <code>QueueBased.LIFO</code> or ...
	 * @param qCapacity
	 *            int : The capacity of the queue, that is how many processes
	 *            can be enqueued. Zero (0) means unlimited capacity.
	 * @param capacity
	 *            int : The number of resources the Res starts with. Must be
	 *            positive and greater than 0.
	 * @param showInReport
	 *            boolean : Flag, if Res should produce a report or not.
	 * @param showInTrace
	 *            boolean : Flag for trace to produce trace messages.
	 */
	public Res(Model owner, String name, int sortOrder, int qCapacity,
			int capacity, boolean showInReport, boolean showInTrace) {
		super(owner, name, showInReport, showInTrace); // construct QueueBased

		_idNumber = resNumber++; // increment the resNumber and get it as
		// IDNumber

        // determine the queueing strategy
        switch (sortOrder) {
        case QueueBased.FIFO :
            _queue = new QueueListFifo<SimProcess>(); break;
        case QueueBased.LIFO :
            _queue = new QueueListLifo<SimProcess>(); break;
        case QueueBased.RANDOM :
            _queue = new QueueListRandom<SimProcess>(); break;
        default :
            sendWarning(
                    "The given sortOrder parameter " + sortOrder + " is not valid! "
                            + "A queue with Fifo sort order will be created.",
                    "Res : "
                            + getName()
                            + " Constructor: Res (Model owner, String name, int "
                            + "sortOrder, long qCapacity, int capacity, boolean "
                            + "showInReport, boolean showInTrace)",
                    "A valid positive integer number must be provided to "
                            + "determine the sort order of the queue.",
                    "Make sure to provide a valid positive integer number "
                            + "by using the constants in the class QueueBased, like "
                            + "QueueBased.FIFO, QueueBased.LIFO or QueueBased.RANDOM.");
            _queue = new QueueListFifo<SimProcess>(); 
        }
        
        // give the QueueList a reference to this QueueBased
        _queue.setQueueBased(this);

		// set the capacity of the queue
		queueLimit = qCapacity;

		// check if it the capacity does make sense
		if (qCapacity < 0) {
			sendWarning(
					"The given capacity of the queue is negative! "
							+ "A queue with unlimited capacity will be created instead.",
					"Res : "
							+ getName()
							+ " Constructor: Res (Model owner, String name, int "
							+ "sortOrder, long qCapacity, int capacity,	boolean "
							+ "showInReport, boolean showInTrace)",
					"A negative capacity for a queue does not make sense.",
					"Make sure to provide a valid positive capacity "
							+ "for the underlying queue.");
			// set the capacity to the maximum value
			queueLimit = Integer.MAX_VALUE;
		}

		// check if qCapacity is zero (that means unlimited capacity)
		if (qCapacity == 0) {
			// set the capacity to the maximum value
			queueLimit = Integer.MAX_VALUE;
		}

		// construct a vector to hold all the UnUsedResources at the moment
		_unUsedResources = new Vector<Resource>();

		// construct a vector to hold the UsedResources (see the inner class)
		_arrayOfUsedResources = new Vector<UsedResources>();

		// get a reference to the resource database
		_resourceDB = owner.getExperiment().getResourceDB();

		this._limit = capacity;
		this._minimum = capacity;
		this._avail = capacity;
		this._users = 0;
		this._wSumAvail = 0.0;
		this._refused = 0;
		this._lastUsage = presentTime();

		if (capacity <= 0) // nothing or less in the resource pool, you fool!
		{
			sendWarning(
					"Attempt to construct a Res with nothing or a negativ"
							+ " number of resources. Initial number of resources is set to one!",
					"Res: "
							+ getName()
							+ " Constructor: Res (Model owner, String name, int sortOrder, "
							+ "long qCapacity, int capacity, boolean showInReport, "
							+ "boolean showInTrace)",
					"A negative number of resources does not make sense here.",
					"Make sure to initialize the capacity of a Res always with"
							+ " a positive number of resources.");

			_limit = _minimum = _avail = 1; // set it to 1, that makes more
			// sense
		}

		// make the resource objects and store them in the vector of unused
		// resources
		for (int i = 0; i < capacity; i++) {
			// make the resources and give them the name of the Res pool
			Resource aResource = new Resource(owner, name, this, true);

			_unUsedResources.addElement(aResource);
		}
	}

	/**
	 * Returns the average usage of the Res. That means: in average, which
	 * percentage of the resources were in use over the time?
	 * 
	 * @return double : the average usage of the resources in the Res.
	 */
	public double avgUsage() {
		TimeInstant now = presentTime(); // what is the time?

		// how long since the last reset
		TimeSpan diff = TimeOperations.diff(now, resetAt());

		double wSumAvl = _wSumAvail
				+ ((double) _avail * TimeOperations.diff(now, _lastUsage)
						.getTimeInEpsilon());
        if (diff.isZero()) // diff is not long enough
		{
			sendWarning(
					"A division by zero error occured.",
					"Res: " + this.getName() + " Method: double avgUsage ()",
					"The time difference between the last reset and now is zero.",
					"Do not reset any model component at the same time the simulation is "
							+ "over or will be stopped.");

			return UNDEFINED; // see QueueBased: UNDEFINED = -1
		}

		// calculate the average usage
		double avgUsg = 1.0 - ((wSumAvl / diff.getTimeInEpsilon()) / _limit);
		// return the rounded average usage
		return StatisticObject.round(avgUsg);
	}

	/**
	 * Changes the limit of the available resources in the Res. Sets the number
	 * of the maximum available resources to m. m must be positive. This is only
	 * allowed as long as the Res has not been used or the Res has just been
	 * reset.
	 * 
	 * @param m
	 *            int : The new limit (capacity) of the Res. Must be positive.
	 */
	public void changeLimit(int m) {
		if (_limit != _minimum || _users != 0) // if Res is already used
		{
			sendWarning(
					"Attempt to change the limit of a Res already"
							+ " in use. The limit will remain unchanged!",
					"Res: " + this.getName()
							+ " Method: void changeLimit (long m)",
					"The limit of a Res which has already be used can not"
							+ " be changed afterwards.",
					"Do not try to change the limit of a Res which might have been"
							+ " used already. Or reset the Res before changing its limit.");

			return; // without changing the limit
		}

		if (m <= 0) // Trying to set the limit to 0 or a negative value.
		{
			sendWarning(
					"Attempt to change the limit of a Res to zero"
							+ " or a negative number. The limit will remain unchanged!",
					"Res: " + this.getName() + " Method: void changeLimit "
							+ "(long m)",
					"The limit of a Res can not be set to zero or a negative"
							+ " number. That would make no sense.",
					"Do not try to change the limit of a Res to negative "
							+ "or zero. Choose a positive integer instead.");

			return; // ignore this rubbish
		}

		// adjust the number of resources stored in the array of unused
		// resources
		if (m > _limit) // the limit is increasing
		{
			for (int i = _limit; i < m; i++) {
				// make the resources and give them the name of the Res pool
				Resource aResource = new Resource(getModel(), getName(), this,
						true);
				_unUsedResources.addElement(aResource);
			}
		}

		if (m < _limit) // the limit is decreasing
			for (int i = m; i < _limit; i++) {
				_unUsedResources.removeElementAt(i);
			}

		// set the limit and the minimum to the new value
		_limit = _minimum = _avail = m;

	}

	/**
	 * Returns a Reporter to produce a report about this Res.
	 * 
	 * @return desmoj.report.Reporter : The Reporter for the queue inside this
	 *         Res.
	 */
	public desmoj.core.report.Reporter createDefaultReporter() {
		return new desmoj.core.advancedModellingFeatures.report.ResourceReporter(
				this);
	}

	/**
	 * Turns the deadlock check off. So whenever a SimProcess can not get the
	 * resources desired, there won't be checked if a deadlock situation might
	 * have occured.
	 */
	public void deadlockCheckOff() {

		_deadlockCheck = false; // that's all

		// send a warning if the resource pool has been used already
		if (_limit != _minimum || _users != 0) // if Res is already used
		{
			sendWarning(
					"The deadlock check for the resource pool: "
							+ this.getName() + " is turned off!",
					"Res: " + this.getName()
							+ " Method: void deadlockCheckOff()",
					"The deadlock check for this resource pool is turned off, but "
							+ "some resources are already in use.",
					"Make sure, that you really want to turn the deadlock check off "
							+ " even after some resources have been used already.");
		}

		// for debugging purposes
		if (currentlySendDebugNotes()) {
			sendDebugNote("The deadlock check for '" + getName()
					+ "' is turned " + "off now.");
		}
	}

	/**
	 * Turns the deadlock check on. So whenever a SimProcess can not get the
	 * resources desired, it will be checked if a deadlock situation might
	 * occur.
	 */
	public void deadlockCheckOn() {

		_deadlockCheck = true; // that's all

		// send a warning if the resource pool has been used already
		if (_limit != _minimum || _users != 0) // if Res is already used
		{
			sendWarning(
					"The deadlock check for the resource pool: "
							+ this.getName()
							+ " is turned on. But some resources have been "
							+ "used already!",
					"Res: " + this.getName()
							+ " Method: void deadlockCheckOn()",
					"The deadlock check for this resource pool is turned on again, "
							+ "but some resources are already in use. So the deadlock check can "
							+ "not be performed correctly!",
					"Make sure to turn the deadlock check on before the resources will "
							+ "be used.");
		}

		// for debugging purposes
		if (currentlySendDebugNotes()) {
			sendDebugNote("The deadlock check for '" + getName()
					+ "' is turned " + "on again from now on.");
		}
	}

	/**
	 * Returns the number of resources available in the pool at the moment.
	 * 
	 * @return int : The number of resources available at the moment.
	 */
	public int getAvail() {
		return this._avail;
	}

	/**
	 * Returns if the deadlock check is enabled (<code>true</code>) or not (
	 * <code>false</code>).
	 * 
	 * @return boolean :<code>true</code> if the deadlock check is enabled,
	 *         <code>false</code> if the deadlock check is not enabled
	 */
	public boolean getDeadlockCheck() {

		return _deadlockCheck; // that's all
	}

	/**
	 * Returns the ID number of this <code>Res</code> object.
	 * 
	 * @return long : The ID number of this <code>Res</code> object.
	 */
	public long getidNumber() {
		return _idNumber;
	}

	/**
	 * Returns the initial number of resources in the Res pool.
	 * 
	 * @return int : The initial number of resources in the Res pool at the
	 *         beginning.
	 */
	public int getLimit() {
		return this._limit;
	}

	/**
	 * Returns the minimum number of resources in the Res.
	 * 
	 * @return int : The minimum number of resources in the Res.
	 */
	public int getMinimum() {
		return this._minimum;
	}

	/**
	 * Returns whether entities can pass by other entities which are enqueued
	 * before them in the queue.
	 * 
	 * @return boolean : Indicates whether entities can pass by other entities
	 *         which are enqueued before them in the queue.
	 */
	public boolean getPassBy() {
		return _passBy;
	}

	/**
	 * Returns the <code>QueueList</code> actually storing the
	 * <code>SimProcesses</code> waiting for resources.
	 * 
	 * @return desmoj.QueueList : the queue actually storing the
	 *         <code>SimProcesses</code> waiting for resources.
	 */
	public QueueList<SimProcess> getQueue() {

		return _queue; // that's it
	}

	/**
	 * Returns the implemented queueing discipline of the underlying queue as a
	 * String, so it can be displayed in the report.
	 * 
	 * @return String : The String indicating the queueing discipline.
	 */
	public String getQueueStrategy() {

		return _queue.getAbbreviation(); // that's it

	}

	/**
	 * Returns the number of entities refused to be enqueued in the queue,
	 * because the capacity limit is reached.
	 * 
	 * @return long : The number of entities refused to be enqueued in the
	 *         queue.
	 */
	public long getRefused() {

		return _refused; // that's it
	}

	/**
	 * Returns the number of users.
	 * 
	 * @return long : The number of Users. That are processes having acquired
	 *         and released resources.
	 */
	public long getUsers() {
		return this._users;
	}

	/**
	 * Returns <code>true</code> if a deadlock is detected, <code>false</code>
	 * otherwise.
	 * 
	 * @return boolean : is <code>true</code> if a deadlock is detected,
	 *         <code>false</code> otherwise.
	 */
	public boolean isDeadlockDetected() {

		return _deadlockDetected; // that's it
	}

	/**
	 * Gets a number of n resources from the Res pool and provides them to the
	 * Sim-process to use them. Hint for developers: calls the private method
	 * <code>deliver()</code>. As not enough resources are available at the
	 * moment the SimProcess has to wait in a queue until enough products are
	 * available again.
	 * 
	 * The order of resource providing (and, thus, process re-activation) depends on the 
     * order of the internal queue, which is based on the processes' queueing 
     * priorities and (if queueing priorities are equal, requiring
     * a tie-breaker) by FIFO or a different discipline as defined in the
     * constructor. 
	 * 
	 * @return boolean : Is <code>true</code> if the specified number of
	 *         resources have been provided successfully, <code>false</code>
	 *         otherwise (i.e. capacity limit of the queue is reached).
	 * @param n
	 *            int : The number of resources the resourcepool will <code>
	 * provide()</code>
	 *            to the SimProcess.
	 */
	public boolean provide(int n)  {
		_where = " boolean provide (int n)";

		SimProcess currentProcess = currentSimProcess();

		if (!checkProcess(currentProcess, _where)) // if the current process
		{
			return false;
		} // is not valid: return

		if (n <= 0) // trying to provide nothing or less
		{
			sendWarning(
					"Attempt from a Res to provide nothing or a negative "
							+ "number of resources . The attempted action is ignored!",
					"Res: " + getName() + " Method: provide (int n)",
					"It does not make sense to provide nothing or a negative number "
							+ "of resources. The statistic will be corrupted with negative numbers!",
					"Make sure to provide at least one resource from the Res.");

			return false; // ignore that rubbish
		}

		// total of resources acquired and already held by the current
		// SimProcess
		int total = n + heldResources(currentProcess);

		if (total > _limit) // trying to provide (in total) more than the
		{ // capacity of the Res
			sendWarning(
					"Attempt from a Res to provide more resources than its "
							+ "capacity holds. The attempted action is ignored!",
					"Res: " + getName() + " Method: provide (int n)",
					"The requested resources [" + total
							+ "] could never be provided by the Res"
							+ ", because the capacity of this Res [" + _limit
							+ "] is not that big. <br>"
							+ "Therefore the process '"
							+ currentProcess.getName() + "' might be blocked "
							+ "for ever.",
					"Make sure never to let the Res provide more resources than its "
							+ "capacity.");

			return false; // ignore that rubbish
		}

		if (queueLimit <= length()) // check if capac. limit of queue is reached
		{
			if (currentlySendDebugNotes()) {
				sendDebugNote("refuses to insert "
						+ currentProcess.getQuotedName()
						+ " in waiting-queue, because the capacity limit is reached. ");
			}

			if (currentlySendTraceNotes()) {
				sendTraceNote("is refused to be enqueued in "
						+ this.getQuotedName() + "because the capacity limit ("
						+ getQueueLimit() + ") of the " + "queue is reached");
			}

			_refused++; // count the refused ones

			return false; // capacity limit is reached
		}

		// insert every process in the queue for statistical reasons
		_queue.insert(currentProcess);

		// is it possible for this process to pass by?
		if (_passBy == false) {
			// see if the SimProcess can be satisfied or has to wait in the
			// queue
			if (n > _avail || // not enough resources available OR
					currentProcess != _queue.first()) // other process is
			// first
			// in the q
			{
				// tell in the trace what the process is waiting for
				if (currentlySendTraceNotes()) {
					sendTraceNote("awaits " + n + " of ' " + this.getName()
							+ " '");
				}

				// tell in the debug output what the process is waiting for
				if (currentlySendDebugNotes()) {
					sendDebugNote("has not enough resources left to provide "
							+ n + " unit(s) to '" + currentProcess.getName()
							+ "'");
				}

				// check for deadlock?
				if (getDeadlockCheck()) {
					// update the resourceDB: this SimProcess is requesting
					// resources
					_resourceDB.noteResourceRequest(currentProcess, this, n);

					// check if this unsatisfied resource request has caused a
					// deadlock
					_deadlockDetected = _resourceDB
							.checkForDeadlock(currentProcess);
				}

				// the process is caught in this do-while loop as long as ...see
				// while
				do {
					currentProcess.setBlocked(true); // block the process
					currentProcess.skipTraceNote(); // don't tell the user, that
					// we ...
					currentProcess.passivate(); // passivate the current process
				} while (n > _avail || // not enough resources available OR
						currentProcess != _queue.first()); // other process is
				// first

				// check for deadlock?
				if (getDeadlockCheck()) {
					// delete the request of the resources in the resourceDB
					_resourceDB.deleteResRequest(currentProcess, this, n);
				}

			} // end if
		} else // the process can pass by other processes in the queue, passBy
		// =
		// true
		{
			if (n > _avail || // not enough resources available OR
					currentProcess != _queue.first()) // other process is
			// first
			// in the q
			{
				// is the current process the first in the queue?
				if (currentProcess != _queue.first()) // no it's not the first
				{
					// we have to make sure that no other process in front of
					// this current
					// process in the wait queue could be satisfied, so activate
					// the first Process in the queue to see what he can do. He
					// will pass
					// the activation on to his successors until this process
					// will be
					// activated again to get his products. (hopefully)
					activateFirst();
				}

				// only if not enough units are available the process has to
				// wait
				if (n > _avail) {
					// tell in the trace what the process is waiting for
					if (currentlySendTraceNotes()) {
						sendTraceNote("awaits " + n + " of ' " + this.getName()
								+ " '");
					}

					// tell in the debug output what the process is waiting for
					if (currentlySendDebugNotes()) {
						sendDebugNote("has not enough resources left to provide "
								+ n
								+ " unit(s) to '"
								+ currentProcess.getName() + "'");
					}
				} // end if not enough units are available

				// check for deadlock?
				if (getDeadlockCheck()) {
					// update the resourceDB: this SimProcess is requesting
					// resources
					_resourceDB.noteResourceRequest(currentProcess, this, n);

					// check if this unsatisfied resource request has caused a
					// deadlock
					_deadlockDetected = _resourceDB
							.checkForDeadlock(currentProcess);
				}

				// the process is caught in this do-while loop as long as ...see
				// while
				do {
					currentProcess.setBlocked(true); // block the process
					currentProcess.skipTraceNote(); // don't tell the user, that
					// we ...
					currentProcess.passivate(); // passivate the current process

					// activate the next process in the queue to see what he can
					// do
					activateAsNext(_queue.succ(currentProcess));
				} while (n > _avail); // not enough resources available

				// check for deadlock?
				if (getDeadlockCheck()) {
					// delete the request of the resources in the resourceDB
					_resourceDB.deleteResRequest(currentProcess, this, n);
				}

			}
		} // end else (passBy = true)

		// the current process has got the resources he wanted ...

		// the Res provides all the resources the SimProcess wants
		_queue.remove(currentProcess); // get the process out of the queue
		currentProcess.setBlocked(false); // we are not blocked (anymore),
		// yeah!

		// give the new first process in the queue a chance
		activateFirst();

		// hand the resources over to the SimProcess
		currentProcess.obtainResources(deliver(n));

		updateStatistics(-n); // statistics will be updated

		// check for deadlock?
		if (getDeadlockCheck()) {
			// update the resourceDB: resources are assigned to the SimProcess
			// now
			_resourceDB.noteResourceAllocation(this, currentProcess, n);
		}

		if (currentlySendTraceNotes()) {
			sendTraceNote("seizes " + n + " from " + this.getQuotedName());
		} // tell in the trace what the process is taking from the resources

		// a debug message is generated in the method deliver(), see some lines
		// above

		return true;
	}

	/**
	 * Resets the statistics of this Res. The number of available resources at
	 * this moment and the processes waiting in the queue are not changed. But
	 * all statistic counters are reset. The parent <code>QueueBased</code> is
	 * also reset.
	 */
	public void reset() {
		super.reset(); // reset the QueueBased also

		_minimum = _limit; // not quite correct, but needed for changeLimit()
		_users = 0;
		_wSumAvail = 0.0;
		_refused = 0;
		_lastUsage = presentTime();
	}

	/**
	 * Sets the boolean field <code>deadlockDetected</code> to the given value.
	 * If a deadlock for this <code>Res</code> is detected when an unsuccessfull
	 * seize statement for a resource has taken place, then the value of
	 * <code>deadlockDetected</code> will be set to <code>true</code>. The value
	 * will also been shown in the report of this <code>Res</code>.
	 * 
	 * @param dlDetected
	 *            boolean : the new value for the field
	 *            <code>deadlockDetected</code>. Should be <code>true</code> if
	 *            this <code>Res</code> is involved in a deadlock.
	 */
	public void setDeadlockDetected(boolean dlDetected) {

		_deadlockDetected = dlDetected; // that's all
	}

	/**
	 * Sets the flag passBy to a new value. PassBy is indicating whether
	 * entities can pass by other entities which are enqueued before them in the
	 * queue.
	 * 
	 * @param newPassBy
	 *            boolean : The new value of passBy. Set it to <code>true</code>
	 *            if you want entities to pass by other entities which are
	 *            enqueued before them in the queue. Set it to
	 *            <code>false</code> if you don't want entities to overtake
	 *            other entities in the queue.
	 */
	public void setPassBy(boolean newPassBy) {
		this._passBy = newPassBy; // that's all!
	}

	/**
	 * A process is using this method to put resources it has used back in the
	 * Res pool. The process can not put more resources back than it has
	 * acquired once. This method can be used as an alternative to the method
	 * <code>takeBack(Resource[] returnedRes)</code> in cases that the user does
	 * not want to provide an array of returning resources. This method is also
	 * compatible with older DESMO-J Versions.
	 * 
	 * @param n
	 *            int : The number of resources which should be returned to the
	 *            Res pool. Can't be more than once were acquired!
	 */
	public void takeBack(int n) {
		_where = "void takeBack (int n) ";

		SimProcess currentProcess = currentSimProcess();

		if (!checkProcess(currentProcess, _where)) // check the current process
		{
			return;
		} // if it is not valid just return

		if (n <= 0) // if the process is releasing nothing
		{
			sendWarning(
					"The number of returned resources is negative or zero! "
							+ "The attempted action is ignored!",
					"Res: " + this.getName() + " Method: void takeBack (int n)",
					"It makes no sense to take back nothing or a negative number of "
							+ "resources.",
					"Make sure to return at least one resource to the Res pool.");

			return; // go to where you came from
		}

		// the process is trying to release more resources than it holds
		if (n > heldResources(currentProcess)) {
			sendWarning("Attempt to make the Res take back more resources ["
					+ n + "] than the process '" + currentProcess.getName()
					+ "' is holding at the " + "moment ["
					+ heldResources(currentProcess) + "]. <br>"
					+ "The attempted action is ignored!", "Res: "
					+ this.getName() + " Method: void takeBack (int n)",
					"A process can not release more resources than it holds.",
					"Make sure not to bring back more resources than the process is holding.");

			return; // go to where you came from
		}

		// get the array of returned resources from the SimProcess
		Resource[] returnedRes = currentProcess.returnResources(this, n);

		// put the used resources back in the unused resources pool
		for (int i = 0; i < n; i++) {
			_unUsedResources.addElement(returnedRes[i]);
		}

		// update which SimProcess is holding which Resources
		updateTakenBackRes(currentProcess, returnedRes);

		updateStatistics(n); // statistics will be updated
		_users++; // update users

		// update the resource database / check for deadlock?
		if (getDeadlockCheck()) {
			_resourceDB.deleteResAllocation(this, currentProcess, n);
		}

		// tell in the trace what the process is returning to the Res pool
		if (currentlySendTraceNotes()) {
			sendTraceNote("releases " + n + " to " + this.getQuotedName());
		}

		// for debugging purposes
		if (currentlySendDebugNotes()) {
			// make a string including all elements of the array of returned
			// res.
		    StringBuilder s = new StringBuilder();
			s.append("SimProcess '" + currentProcess.getName() + "' <b>returns</b>: ");

			for (int j = 0; j < returnedRes.length; j++) {
			    s.append("<br>" + returnedRes[j].getName());
			}

			sendDebugNote(s.toString());
		}

		activateFirst(); // give the new first process in the queue a chance
	}

	/**
	 * A process is using this method to put resources it has used back in the
	 * Res pool. The process can not put more resources back than it has
	 * acquired once. The array of returning resources can be provided by the
	 * method <code>returnResources()</code> of the class
	 * <code>SimProcess</code>.
	 * 
	 * @param returnedRes
	 *            Resource[] : The array of resources a process is returning to
	 *            the resource pool. Can't be more resources than it once has
	 *            acquired!
	 */
	public void takeBack(Resource[] returnedRes) {
		_where = "void takeBack (Resource[] returnedRes)	";

		SimProcess currentProcess = currentSimProcess();

		if (!checkProcess(currentProcess, _where)) // check the current process
		{
			return;
		} // if it is not valid just return

		if (returnedRes.length <= 0) // if the process is releasing nothing
		{
			sendWarning(
					"The array of returned resources is empty! "
							+ "The attempted action is ignored!",
					"Res: " + this.getName()
							+ " Method: void takeBack (Resource[] "
							+ "returnedRes)",
					"It makes no sense to take back an empty array of resources.",
					"Make sure to return at least one resource to the Res pool.");

			return; // go to where you came from
		}

		// the process is trying to release more resources than it holds
		if (returnedRes.length > heldResources(currentProcess)) {
			sendWarning(
					"Attempt to make the Res take back more resources than "
							+ "the process is holding at the moment. The attempted action is "
							+ "ignored!", "Res: " + this.getName()
							+ " Method: void takeBack (Resource[] "
							+ "returnedRes)",
					"A process can not release more resources than it holds.",
					"Make sure not to take back more resources than the process is holding.");

			return; // go to where you came from
		}

		// put the used resources back in the unused resources pool
		for (int i = 0; i < returnedRes.length; i++) {
			_unUsedResources.addElement(returnedRes[i]);
		}

		// update which SimProcess is holding which Resources
		updateTakenBackRes(currentProcess, returnedRes);

		updateStatistics(returnedRes.length); // statistics will be updated
		_users++; // update users

		// update the resource database / check for deadlock?
		if (getDeadlockCheck()) {
			_resourceDB.deleteResAllocation(this, currentProcess,
					returnedRes.length);
		}

		// tell in the trace what the process is returning to the Res pool
		if (currentlySendTraceNotes()) {
			sendTraceNote("releases " + returnedRes.length + " to "
					+ this.getQuotedName());
		}

		// for debugging purposes
		if (currentlySendDebugNotes()) {
			// make a string including all elements of the array of returned
			// res.
			StringBuilder s = new StringBuilder();
			s.append("SimProcess '" + currentProcess.getName() + "' <b>returns</b>: ");

			for (int j = 0; j < returnedRes.length; j++) {
				s.append("<br>" + returnedRes[j].getName());
			}

			sendDebugNote(s.toString());
		}

		activateFirst(); // give the new first process in the queue a chance
	}

	/**
	 * Activates the SimProcess <code>process</code>, given as a parameter of
	 * this method, as the next process. This process should be a SimProcess
	 * waiting in the queue for some resources.
	 * 
	 * @param process
	 *            SimProcess : The process that is to be activated as next.
	 */
	protected void activateAsNext(SimProcess process) {
		_where = "protected void activateAsNext(SimProcess process)";

		if (process != null) {
			// if the given process is not valid just return
			if (!checkProcess(process, _where)) {
				return;
			}

			// if the process is scheduled (on the event-list) already
			if (process.isScheduled()) {
				process.skipTraceNote(); // don't tell the user, that we ...
				process.cancel(); // get the process from the event-list
			}

			// remember if the process is blocked at the moment
			boolean wasBlocked = process.isBlocked();

			// unblock the process to be able to activate him
			if (wasBlocked) {
				process.setBlocked(false); // the process is not blocked
				// anymore
				// and
			} // ready to become activated

			// don't tell the user, that we activate the process after the
			// current process
			process.skipTraceNote();
			process.activateAfter(current());

			// the process status is still "blocked"
			if (wasBlocked) {
				process.setBlocked(true);
			}
		} // end outer if
	}

	/**
	 * Activates the first process waiting in the queue. That is a process which
	 * was trying to acquire resources, but there were not enough left in the
	 * Res. Or another process was first in the queue to be served. This method
	 * is called every time a process returns resources or when a process in the
	 * waiting-queue is satisfied.
	 */
	protected void activateFirst() {
		_where = "protected void activateFirst()";

		// first is the first process in the queue (or null if none is in the
		// queue)
		SimProcess first = _queue.first();

		if (first != null) {
			// if first is not modelcompatible just return
			if (!checkProcess(first, _where)) {
				return;
			}

			// if first is scheduled (on the event-list) already
			if (first.isScheduled()) {
				first.skipTraceNote(); // don't tell the user, that we ...
				first.cancel(); // get the process from the event-list
			}

			// remember if first is blocked at the moment
			boolean wasBlocked = first.isBlocked();

			// unblock the process to be able to activate him
			if (wasBlocked) {
				first.setBlocked(false);
			}

			// don't tell the user, that we activate first after the current
			// process
			first.skipTraceNote();
			first.activateAfter(current());

			// the status of first is still "blocked"
			if (wasBlocked) {
				first.setBlocked(true);
			}
		} // end outer if
	}

	/**
	 * Checks whether the process using the Res is a valid process.
	 * 
	 * @return boolean : Returns whether the SimProcess is valid or not.
	 * @param p
	 *            SimProcess : Is this SimProcess a valid one?
	 */
	protected boolean checkProcess(SimProcess p, String where) {
		if (p == null) // if p is a null pointer instead of a process
		{
			sendWarning("A non existing process is trying to use a Res "
					+ "object. The attempted action is ignored!", "Res: "
					+ getName() + " Method: " + where,
					"The process is only a null pointer.",
					"Make sure that only real SimProcesses are using Res's.");
			return false;
		}

		if (!isModelCompatible(p)) // if p is not modelcompatible
		{
			sendWarning(
					"The process trying to use a Res object does"
							+ " not belong to this model. The attempted action is ignored!",
					"Res: " + getName() + " Method: " + where,
					"The process is not modelcompatible.",
					"Make sure that processes are using only Res's within"
							+ " their model.");
			return false;
		}
		return true;
	}

	/**
	 * Returns the number of resources held by the given SimProcess at this
	 * time.
	 * 
	 * @return int : The number of resources held by the given SimProcess at
	 *         this time.
	 * @param sProc
	 *            SimProcess : The SimProcess which is expected to hold some
	 *            Resources.
	 */
	protected int heldResources(SimProcess sProc) {
		int j = 0; // to count the resources held

		for (int i = 0; i < _arrayOfUsedResources.size(); i++) {
			// get hold of the UsedResources pair (SimProcess/number of
			// resources)
			UsedResources procHoldRes = _arrayOfUsedResources.elementAt(i);

			if (procHoldRes.getProcess() == sProc) {
				j += procHoldRes.getOccupiedResources().size();
			} // end if
		} // end for

		return j; // all the resources the SimProcess holds at the moment
	}

	/**
	 * Updates the arrayOfUsedResources for this Res whenever resources are
	 * <code>provided</code>.
	 * 
	 * @param crntProcess
	 *            SimProcess : The current SimProcess acquiring resources.
	 * @param providedRes
	 *            Resource[] : The array of resources the Res is providing to
	 *            the current SimProcess.
	 */
	protected void updateProvidedRes(SimProcess crntProcess,
			Resource[] providedRes) {
		// is the SimProcess already holding resources?
		boolean holdsResources = false; // not yet

		// search the whole vector
		for (int i = 0; i < _arrayOfUsedResources.size(); i++) {
			// get hold of the UsedResources pair (SimProcess/number of
			// resources)
			UsedResources procHoldRes = _arrayOfUsedResources.elementAt(i);

			// is the SimProcess already holding resources?
			if (procHoldRes.getProcess() == crntProcess) {
				// update the held resources of the current SimProcess
				for (int j = 0; j < providedRes.length; j++) {
					procHoldRes.getOccupiedResources().addElement(
							providedRes[j]);
				}

				holdsResources = true; // the SimProcess already holds
				// resources
			} // end if
		} // end for

		if (!holdsResources) // the process does not hold any resources yet
		{
			// make a new Vector
			Vector<Resource> occupiedRes = new Vector<Resource>();

			// copy all elements of the array to the Vector
			for (int i = 0; i < providedRes.length; i++) {
				occupiedRes.addElement(providedRes[i]);
			}

			// construct a new UsedResources object with the Vector
			UsedResources ur = new UsedResources(crntProcess, occupiedRes);

			// put ur in the arrayOfUsedResources
			_arrayOfUsedResources.addElement(ur);
		}
	}

	/**
	 * 
	 * Muss durch eine andere Methode in SimProcess ersetzt werden ???
	 * 
	 * Soenke ????
	 * 
	 * 
	 * A process is using this method to return all the resources it holds to
	 * the Res pool. The process can not put more resources back than it has
	 * acquired once. This method can be used if a Process is about to be
	 * terminated.
	 * 
	 * public void takeBackAll () { where = "void takeBackAll ()";
	 * 
	 * int n = 0; // how many resources will be taken back
	 * 
	 * Sim-process currentProcess = currentSimProcess();
	 * 
	 * if (!checkProcess(currentProcess, where)) //check the current process {
	 * return; } // if it is not valid just return // delete the entry of the
	 * currentSimProcess in the arrayOfUsedResources // search the whole vector
	 * for ( int i = 0; i < arrayOfUsedResources.size(); i++) { // get hold of
	 * the UsedResources pair (SimProcess/number of resources) UsedResources
	 * procHoldRes = (UsedResources)arrayOfUsedResources.elementAt(i);
	 * 
	 * if (procHoldRes.getProcess() == currentProcess) { // number of resources
	 * held by the currentProcess n = procHoldRes.getOccupiedResources ();
	 * 
	 * arrayOfUsedResources.removeElementAt(i); // delete the entry } } // end
	 * for
	 * 
	 * updateStatistics ( n ); // statistics will be updated
	 * 
	 * users++; // update users // tell in the trace that the process is
	 * releasing all its resources if ( traceIsOn() ) { sendTraceNote (
	 * "releases all its " + n + " " + this.getName() ); }
	 * 
	 * activateNext(); // give waiting process in the queue a chance }
	 */

	/**
	 * Updates the statistics for the Res whenever resources are
	 * <code>provided</code> or <code>"takenBack"</code>.
	 * 
	 * @param n
	 *            int : Is positive when the Res <code>takeBack()</code>
	 *            resources and negative when the Res <code>provides()</code>
	 *            resources.
	 */
	protected void updateStatistics(int n) {
		TimeInstant now = presentTime();

		_wSumAvail = _wSumAvail
				+ ((double) _avail * TimeOperations.diff(now, _lastUsage)
						.getTimeInEpsilon());
		_lastUsage = now;

		_avail += n; // n can be positive or negative (remember ?!)

		if (_avail < _minimum) // update minimum, if necessary
		{
			_minimum = _avail;
		}
	}

	/**
	 * Updates the arrayOfUsedResources for this Res whenever resources are
	 * taken back.
	 * 
	 * @param crntProcess
	 *            SimProcess : The current SimProcess releasing resources.
	 * @param returnedRes
	 *            Resource[] : The array of resources the Res will take back
	 *            from the current SimProcess.
	 */
	protected void updateTakenBackRes(SimProcess crntProcess,
			Resource[] returnedRes) {
		// search the whole vector
		for (int i = 0; i < _arrayOfUsedResources.size(); i++) {
			// get hold of the UsedResources pair (SimProcess/number of
			// resources)
			UsedResources procHoldRes = _arrayOfUsedResources.elementAt(i);

			if (procHoldRes.getProcess() == crntProcess) {
				// remove the resources from the Vector of used resources
				for (int j = 0; j < returnedRes.length; j++) {
					procHoldRes.getOccupiedResources().removeElement(
							returnedRes[j]);
				}

				// are all resources from this SimProcess taken back
				if (procHoldRes.getOccupiedResources().isEmpty()) {
					_arrayOfUsedResources.removeElementAt(i);
				}
			} // end if
		} // end for
	}

	/**
	 * Takes a number of n resources from the Res pool and delivers this array
	 * of resources to the Simprocess to use them. Is called from the method
	 * <code>provide (int n)</code>.
	 * 
	 * @param n
	 *            int : The number of resources the resourcepool will <code>
	 * deliver()</code>
	 *            to the SimProcess.
	 * @return Resource[] : the array of resources which will be delivered to
	 *         the SimProcess.
	 */
	private Resource[] deliver(int n) {
		SimProcess currentProcess = currentSimProcess();

		// get the resources from the unused resources pool
		Resource[] resArray = new Resource[n]; // make the array of resources

		// fill the array of resources
		for (int i = 0; i < n; i++) {
			// put first res in array
			resArray[i] = _unUsedResources.firstElement();
			// delete first res
			_unUsedResources.removeElement(_unUsedResources.firstElement());
		}

		// note which SimProcess is holding how many Resources
		updateProvidedRes(currentProcess, resArray);

		// for debugging purposes
		if (currentlySendDebugNotes()) {
			// make a string including all elements of the array of provided
			// res.
			StringBuilder s = new StringBuilder();
			s.append("delivers to SimProcess '" + currentProcess.getName() + "': ");

			for (int j = 0; j < n; j++) {
				s.append("<br>" + resArray[j].getName());
			}

			sendDebugNote(s.toString());

			// make a string including all the resource that are left in the Res
			StringBuilder t = new StringBuilder();
			t.append("In this Res pool are left: ");

			if (_unUsedResources.isEmpty()) // anything left ?
			{
				t.append("<br>none");
			}

			for (Enumeration<Resource> e = _unUsedResources.elements(); e
					.hasMoreElements();) {
				t.append("<br>" + e.nextElement().getName());
			}

			sendDebugNote(t.toString());
		}

		return resArray; // return the array of resources
	}
} // end class Res
