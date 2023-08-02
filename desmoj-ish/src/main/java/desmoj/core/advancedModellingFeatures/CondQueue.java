package desmoj.core.advancedModellingFeatures;


import desmoj.core.simulator.Condition;
import desmoj.core.simulator.QueueBased;
import desmoj.core.simulator.QueueList;
import desmoj.core.simulator.QueueListFifo;
import desmoj.core.simulator.QueueListLifo;
import desmoj.core.simulator.QueueListRandom;
import desmoj.core.simulator.SimProcess;

/**
 * In a CondQueue processes are waiting for a specific condition to become true.
 * Process synchronisation happens when processes are waiting in a queue for a
 * specific condition to become true. Each process which uses
 * <code>waitUntil(condition)</code> and does not find this condition to be
 * true, is inserted in a waiting-queue automatically. Whenever something
 * happens which might influence the condition to become <code>true</code>,
 * <code>signal()</code> should be used to check if the first entity (or, if
 * the attribute <code>checkAll</code> is set to true, all entities) in the
 * waiting-queue finds the desired condition now and therefore can continue. The
 * designer of the model is responsible that this check takes place! He also has
 * to implement the condition, this can easily be done by deriving it from the
 * interface <code>Condition</code>. The flag, if only the first or all
 * entities in the queue are checking their conditions again, can be checked and
 * changed using <code>getCheckAll()</code> and <code>setCheckAll()</code>.
 * 
 * The first sort criterion of the queue is highest queueing priorities first
 * (i.e. not using scheduling priorities - note that this is a somewhat arbitrary 
 * choice, as the <ode>CondQueue</code> combines queueing and scheduling features).
 * The second criterion, if a tie-breaker is needed, is the queueing discipline 
 * of the underlying queue, e.g. FIFO. The capacity limit can be determined by the user.
 * <code>CondQueue</code> is derived from <code>QueueBased</code>, which provides all the statistical functionality
 * for a queue.
 * 
 * @see QueueBased
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

public class CondQueue<P extends SimProcess> extends desmoj.core.simulator.QueueBased {

	// ****** attributes ******

	/**
	 * The queue, actually storing the processes waiting for the condition to
	 * become true.
	 */
	protected QueueList<P> _queue;

	/**
	 * Check all the entities in the queue?
	 */
	private boolean _checkAll;

	/**
	 * Counter for the SimProcesses which are refused to be enqueued, because
	 * the queue capacity is full.
	 */
	private long _refused;

	/**
	 * Indicates the method where something has gone wrong. Is passed as a
	 * parameter to the method <code>checkProcess()</code>.
	 */
	private String _where;

	/**
     * Constructor for a CondQueue where processes can wait for a certain
     * condition to become true. The processes are sorted (and thus activated)
     * according to their queueing priorities (highest first) and (if equal)
     * FIFO. The underlying queue has unlimited capacity.
	 * 
	 * @param owner
	 *            Model : The model this CondQueue is associated to.
	 * @param name
	 *            java.lang.String : The CondQueue's name
	 * @param showInReport
	 *            boolean : Flag, if CondQueue should produce a report or not.
	 * @param showInTrace
	 *            boolean : Flag for trace to produce trace messages.
	 */
	public CondQueue(desmoj.core.simulator.Model owner, String name,
			boolean showInReport, boolean showInTrace) {
		super(owner, name, showInReport, showInTrace); // construct QueueBased
		reset();

		_queue = new QueueListFifo<P>(); // make an actual queue and give it a
		_queue.setQueueBased(this); // reference of this "QueueBased"-CondQueue

		this._checkAll = false; // only the first entity in the queue is checked
	}

	// ****** methods ******

	/**
	 * Constructor for a CondQueue where processes can wait for a certain
	 * condition to become true. The processes are sorted (and thus activated)
	 * according to their queueing priorities (highest first) and (if equal) 
	 * by a queuing discipline as defined by <code>QueueBased</code>, e.g. 
	 * <code>QueueBased.FIFO</code> or <code>QueueBased.LIFO</code>. 
	 * The capacity limit of the underlying queue can be chosen. 
	 * 
	 * @param owner
	 *            Model : The model this CondQueue is associated to.
	 * @param name
	 *            java.lang.String : The CondQueue's name
	 * @param sortOrder
	 *            int : determines the sort order of the underlying queue
	 *            implementation to use for sorting processes with equal
	 *            queing priorities. Choose a constant from <code>QueueBased</code>
	 *            like <code>QueueBased.FIFO</code> or
	 *            <code>QueueBased.LIFO</code> or ...
	 * @param qCapacity
	 *            int : The capacity of the queue, that is how many processes
	 *            can be enqueued. Zero (0) means unlimited capacity.
	 * @param showInReport
	 *            boolean : Flag, if CondQueue should produce a report or not.
	 * @param showInTrace
	 *            boolean : Flag for trace to produce trace messages.
	 */
	public CondQueue(desmoj.core.simulator.Model owner, String name,
			int sortOrder, int qCapacity, boolean showInReport,
			boolean showInTrace) {
		super(owner, name, showInReport, showInTrace); // construct QueueBased
		reset();

		_refused = 0; // reset the statistics

        // determine the queueing strategy
        switch (sortOrder) {
        case QueueBased.FIFO :
            _queue = new QueueListFifo<P>(); break;
        case QueueBased.LIFO :
            _queue = new QueueListLifo<P>(); break;
        case QueueBased.RANDOM :
            _queue = new QueueListRandom<P>(); break;
        default :
            sendWarning(
                    "The given sortOrder parameter " + sortOrder + " is not valid! "
                            + "A queue with FIFO sort order will be created.",
                    "CondQueue : "
                            + getName()
                            + " Constructor: CondQueue (desmoj.Model owner, String name, "
                            + "int sortOrder, long qCapacity, boolean showInReport, "
                            + "boolean showInTrace)",
                    "A valid positive integer number must be provided to "
                            + "determine the sort order of the queue.",
                    "Make sure to provide a valid positive integer number "
                            + "by using the constants in the class QueueBased, like "
                            + "QueueBased.FIFO, QueueBased.LIFO or QueueBased.RANDOM.");
            _queue = new QueueListFifo<P>(); 
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
					"CondQueue : "
							+ getName()
							+ " Constructor: CondQueue (desmoj.Model owner, String name, "
							+ "int sortOrder, long qCapacity, boolean showInReport, "
							+ "boolean showInTrace)",
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

		this._checkAll = false; // only the first entity in the queue is checked
	}

	/**
	 * Returns a Reporter to produce a report about this CondQueue.
	 * 
	 * @return desmoj.report.Reporter : The Reporter for the queue inside this
	 *         CondQueue.
	 */
	public desmoj.core.report.Reporter createDefaultReporter() {
		return new desmoj.core.advancedModellingFeatures.report.CondQueueReporter(
				this);
	}

	/**
     * Returns the first process waiting in the queue. If there is no process 
     * waiting, <code>null</code> is returned.
     * 
     * @return SimProcess : Returns the first process in the queue
     *         (or <code>null</code> if no process is waiting).
     */
    public P first()
    {
        if (_queue.isEmpty()) { // nobody home to be checked
            return null;
        } else { 
            return _queue.first();
        }

    } // end method	

	/**
     * Returns the first process waiting in the queue that complies to
     * the given condition. If there is no such process waiting,
     * <code>null</code> is returned.
     * 
     * @return SimProcess : Returns the first process in the queue
     *         which complies to the given condition.
     * @param cond
     *            Condition : The Condition <code>cond</code> is describing the
     *            condition to which the process must comply to. This has
     *            to be implemented by the user in the class:
     *            <code>Condition</code> in the method: <code>check()</code>.
     */
    public P first(Condition<P> cond)
    {
        if (_queue.isEmpty()) { // nobody home to be checked
            return null;
        } // return null

        for (P process = _queue.first(); process != null; process = _queue
                .succ(process)) {
            if (cond.check(process))
                return process;
        }

        // if no SimProcess complies to the condition just return null
        return null;

    } // end method

	/**
	 * Returns if all entities or only the first one in the queue are getting a
	 * signal to check their conditions.
	 * 
	 * @return boolean : Are all entities in the queue checking their
	 *         conditions? Default is <code>false</code>, so that only the
	 *         first process in the queue is checking its condition.
	 */
	public boolean getCheckAll() {
		return this._checkAll;
	}

	/**
	 * Returns the implemented queueing discipline of the underlying queue 
	 * used for sorting (and, therefore, activation order) of processes 
	 * with equal queing priorities. Return type is <code>String</code>, 
	 * inteded to be displayed in the report.
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
	 * Sets the flag <code>checkAll</code>: if all entities or only the first
	 * one in the queue are getting a signal to check their conditions.
	 * 
	 * @param chckall
	 *            boolean : Flag if all entities in the queue should check their
	 *            conditions? Default is <code>false</code>, so that only the
	 *            first entity in the queue is checking its condition.
	 */
	public void setCheckAll(boolean chckall) {
		_checkAll = chckall;
	}

	/**
	 * A <code>signal ()</code> should be sent every time when a condition has
	 * changed and might be true now. It activates the first process in the
	 * queue to check its condition again. Other processes might follow.
	 * The order of process activation depends on the order of the internal
	 * <code>&#95;queue</code>, which is based on the processes' queueing 
	 * priorities and (if queueing priorities are equal, requiring
	 * a tie-breaker) by FIFO or a different discipline as defined in the
	 * constructor. 
	 */
	public void signal() {
		if (currentlySendTraceNotes()) {
			// tell in the trace that the CondQueue gets a signal
			sendTraceNote("signals '" + this.getName() + "'");
		}

		// activate the first process in the queue, others might follow...
		activateAsNext(_queue.first());
	}
	
    /**
     * Removes the given SimProcess from the Queue. 
     * The process no longer waits for its condition to become true
     * and resumes its lifecycle.
     * 
     * @param p
     *            P : The P to be removed from the queue
     */
    public void waitCancel(P p) {

        if (p == null) {
            sendWarning("Can not cancel waiting of SimProcess in Queue!", "CondQueue : "
                    + getName() + " Method:  void remove(P p)",
                    "The SimProcess 'p' given as parameter is a null reference!",
                    "Check to always have valid references when removing "
                            + "processes");
            return; // no proper parameter
        }
        if (!_queue.contains(p)) { 
            sendWarning("Can not cancel waiting of SimProcess in Queue!", "CondQueue : "
                    + getName() + " Method:  void remove(P p)",
                    "The SimProcess 'p' given as parameter is not enqueued in this "
                            + "CondQueue!",
                    "Make sure the process is inside the queue.");
            return; // not enqueued here
        }
        
        // make sure process is passive
        if (p.isScheduled()) // different from DESMO, see DESMO-C
        {
            p.skipTraceNote(); // don't tell the user, that we ...
            p.cancel(); // get the process from the event-list
        }
        
        // unblock
        p.setBlocked(false);
        p.skipTraceNote(); p.activate();  // don't tell we do an ordinary activation here

        // produce trace output
        if (currentlySendTraceNotes()) {
            sendTraceNote("cancels waiting of " + p.getQuotedName() + " in "
                    + this.getQuotedName());
        }
    }
	
	/**
	 * Lets the current process wait in the CondQueue until a certain condition,
	 * given as a parameter, has become true. When the process finds its
	 * condition to have become true or <code>checkAll</code> is set to
	 * <code>true</code> the next process in the queue will be activated, too.
	 * If the capacity limit of the queue is reached, the process will not be
	 * enqueued and <code>false</code> returned.
	 * 
	 * @return boolean : Is <code>true</code> if the process can be enqueued
	 *         successfully, <code>false</code> otherwise (i.e. capacity limit
	 *         of the queue is reached).
	 * @param cond
	 *            Condition : The condition that has to become true before the
	 *            process can continue.
	 * @see desmoj.core.simulator.Condition
	 */
	@SuppressWarnings("unchecked")
    public boolean waitUntil(Condition<P> cond)  {
		_where = "boolean waitUntil (desmoj.core.simulator.Condition cond)";

		P currentProcess = (P) currentSimProcess();

		if (!checkProcess(currentProcess, _where)) // check the current process
		{
			return false;
		} // if it is not valid just return

		if (!this.isModelCompatible(cond)) // if cond is not modelcompatible
		{
			sendWarning("Attempt to use a Condition object that does not "
					+ "belong to this model. The attempted action is ignored!",
					"CondQueue: " + getName()
							+ " Method: boolean waitUntil (Condition cond)",
					"The condition is not modelcompatible.",
					"Make sure that conditions given in a CondQueue waitUntil() method "
							+ "are modelcompatible with the CondQueue object.");
			return false;
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

		_queue.insert(currentProcess); // insert every process in the queue for
		// statistic reasons
		
		boolean waitingCanceled = false;

		if (!cond.check(currentProcess)) // condition is not true
		{
			if (currentlySendTraceNotes()) {
				sendTraceNote("waits in '" + getName() + "'" + " until '"
						+ cond.getName() + "' "); // send a traceNote
			}

			boolean proceed = false; // can we proceed? is the condition
			// true?

			do { // the process is stuck in here
				currentProcess.setBlocked(true); // as long as ...see while
				currentProcess.skipTraceNote(); // don't tell the user, that we
				// ...
				currentProcess.passivate(); // passivate the current process
				
				// waiting canceled? 
				if (!currentProcess.isBlocked()) {
				    waitingCanceled = true;
				    break;
				}

				proceed = cond.check(currentProcess); // has the condition
				// become true?

				if (proceed || _checkAll) // check also the next process in
				// the
				// q?
				{
					activateAsNext(_queue.succ(currentProcess));
					// activate the next process in the queue
				}
			} while (!proceed); // as long as the condition is not true
		} // end if

		// the condition is true now, so we are free...yeah!
		if (currentlySendTraceNotes()) {
		    if (waitingCanceled) {
                  sendTraceNote("resumes after waiting in '" + getName() 
                          + "' canceled"); // send a traceNote
		    } else {
		          sendTraceNote("leaves '" + getName() + "', because '"
		                    + cond.getName() + "' is true"); // send a traceNote
		    }
		}
		_queue.remove(currentProcess); // get the process out of the queue
		currentProcess.setBlocked(false); // we are not blocked (anymore),
		// yeah!

		return true;
	}
    
    /**
	 * Activates the given SimProcess and schedules him right after the current
	 * process. Then the process is responsible to check if the desired
	 * condition has become true. This is done in the method
	 * <code>waitUntil()</code>.
	 * 
	 * @param process
	 *            SimProcess : The process that will be activated now to check
	 *            its condition.
	 */
	protected void activateAsNext(SimProcess process) {
		_where = "protected void activateAsNext (SimProcess process)";

		if (process != null) // if queue is not empty or a successor is found
		{
			if (!checkProcess(process, _where)) // if next is a null pointer or
			{
				return;
			} // not modelcompatible just return

			if (process.isScheduled()) // different from DESMO, see DESMO-C
			{
				process.skipTraceNote(); // don't tell the user, that we ...
				process.cancel(); // get the process from the event-list
			}

			boolean wasBlocked = process.isBlocked();
			if (wasBlocked) {
				process.setBlocked(false); // the process is not blocked
				// anymore
				// and
			} // ready to become activated

			process.skipTraceNote(); // don't tell the user, that we ...
			process.activateAfter(current());// activate process after
			// current
			// process

			if (wasBlocked) {
				process.setBlocked(true); // the process status is still
				// "blocked"
			} // end inner if
		} // end outer if otherwise no one is to be activated
	}
	

	/**
	 * Checks whether the entity using the CondQueue is a valid process.
	 * 
	 * @return boolean : Returns whether the SimProcess is valid or not.
	 * @param p
	 *            SimProcess : Is this SimProcess a valid one?
	 */
	protected boolean checkProcess(SimProcess p, String where) {
		if (p == null) // if p is a null pointer instead of a process
		{
			sendWarning("A non existing process is trying to use a CondQueue  "
					+ "object. The attempted action is ignored!", "CondQueue: "
					+ getName() + " Method: " + where,
					"The process is only a null pointer.",
					"Make sure that only real SimProcesses are using CondQueues.");
			return false;
		}

		if (!isModelCompatible(p)) // if p is not modelcompatible
		{
			sendWarning(
					"The process trying to use a CondQueue object does not "
							+ "belong to this model. The attempted action is ignored!",
					"CondQueue: " + getName() + " Method: " + where,
					"The process is not modelcompatible.",
					"Make sure that processes are using only CondQueues within their model.");
			return false;
		}
		return true;
	}
} // end class CondQueue
