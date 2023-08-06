package desmoj.core.advancedModellingFeatures;


import desmoj.core.simulator.QueueBased;
import desmoj.core.simulator.QueueList;
import desmoj.core.simulator.QueueListFifo;
import desmoj.core.simulator.QueueListLifo;
import desmoj.core.simulator.QueueListRandom;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import desmoj.core.statistic.StatisticObject;

/**
 * Bin is the place where producers can store their products for consumers to
 * come and use them up. Bin is used to implement process synchronization
 * between producers and consumers. Producers are producing products and store
 * them using <code>store()</code> in the bin. Consumers make the Bin
 * <code>deliver()</code> the products to use them up. If no or not enough units
 * of a product are available for the consumers, they have to wait in a queue
 * until new units are delivered by a producer. 
 * 
 * The first sort criterion of the queue is highest queueing priorities first
 * (i.e. not using scheduling priorities - note that this is a somewhat arbitrary 
 * choice, as the <ode>Bin</code> combines queueing and scheduling features).
 * The second criterion, if a tie-breaker is needed, is the queueing discipline 
 * of the underlying queue, e.g. FIFO. The capacity limit can be determined by the user.
 * <code>Bin</code> is derived from <code>QueueBased</code>, which provides all the statistical functionality
 * for a queue.
 * 
 * @see QueueBased
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

public class Bin extends desmoj.core.simulator.QueueBased {

	// ****** attributes ******

	/**
	 * The queue, actually storing the processes waiting for products
	 */
    protected QueueList<SimProcess> _queue;

	/**
	 * Number of products available at the moment
	 */
	private long _avail;

	/**
	 * Number of consumers having visited the Bin
	 */
	private long _consumers;

	/**
	 * The initial number of products in the Bin
	 */
	private long _initial;

	/**
	 * The last time the Bin has been used
	 */
	private TimeInstant _lastUsage;

	/**
	 * The maximum number of products in the Bin
	 */
	private long _maximum;

	/**
	 * Flag to indicate whether an entity can pass by other entities in the
	 * queue which are enqueued before that entity in the queue. Is
	 * <code>false</code> per default.
	 */
	private boolean _passBy = false;

	/**
	 * Number of producers having visited the Bin
	 */
	private long _producers;

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
	 * Weighted sum of available products in the Bin over the time (must be
	 * divided by the total time to get the average available units!)
	 */
	private double _wSumAvail;

	// ****** methods ******

	/**
	 * Constructor for a <code>Bin</code> with a number of initial units of a product in it.
     * Waiting consumer processes are sorted (and thus activated) according to their 
     * queueing priorities (highest first) and (if equal) 
     * by a queuing discipline as defined by <code>QueueBased</code>, e.g. 
     * <code>QueueBased.FIFO</code> or <code>QueueBased.LIFO</code>. 
	 * The capacity limit of the underlying queue can be chosen, too.
	 * 
	 * @param owner
	 *            Model : The model this Bin is associated to.
	 * @param name
	 *            java.lang.String : The Bin's name
	 * @param sortOrder
	 *            int : determines the sort order of the underlying queue
	 *            implementation. Choose a constant from <code>QueueBased</code>
	 *            like <code>QueueBased.FIFO</code> or
	 *            <code>QueueBased.LIFO</code> or ...
	 * @param qCapacity
	 *            int : The capacity of the queue, that is how many processes
	 *            can be enqueued. Zero (0) means unlimited capacity.
	 * @param initialUnits
	 *            long : The units of a product the Bin starts with. Must be
	 *            positive.
	 * @param showInReport
	 *            boolean : Flag, if Bin should produce a report or not.
	 * @param showInTrace
	 *            boolean : Flag for trace to produce trace messages.
	 */
	public Bin(desmoj.core.simulator.Model owner, String name, int sortOrder,
			int qCapacity, long initialUnits, boolean showInReport,
			boolean showInTrace) {
		super(owner, name, showInReport, showInTrace); // construct QueueBased
		reset();

		this._initial = initialUnits;
		this._maximum = initialUnits;
		this._avail = initialUnits;

		if (initialUnits < 0) // there can't be less than nothing
		{
			sendWarning("Attempt to construct a Bin with a negativ number of"
					+ " units. Initial number of units set to zero!", "Bin: "
					+ getName() + " Constructor: Bin (desmoj.Model owner, "
					+ "String name, long initialUnits, boolean showInReport, "
					+ "boolean showInTrace)",
					"A negative number of units does not make sense here.",
					"Make sure to initialize a Bin always with a positive number of "
							+ "initialUnits.");

			_initial = _maximum = _avail = 0; // set it to 0, that makes more
			// sense
		}

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
                    "Bin: "
                            + getName() + " Constructor: Bin (desmoj.Model owner, "
                            + "String name, long initialUnits, boolean showInReport, "
                            + "boolean showInTrace)",
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
					"Bin : "
							+ getName()
							+ " Constructor: Bin (desmoj.Model owner, String name, "
							+ "int sortOrder, long qCapacity, long initialUnits, "
							+ "boolean showInReport, boolean showInTrace)",
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

	}

    /**
     * Constructor for a <code>Bin</code> with a number of initial units of a product in it.
     * Waiting consumer processes are sorted (and thus activated) according to their 
     * queueing priorities (highest first) and (if equal) 
     * by FIFO. The capacity limit of the underlying queue is unlimited.
	 * 
	 * @param owner
	 *            Model : The model this Bin is associated to.
	 * @param name
	 *            java.lang.String : The Bin's name
	 * @param initialUnits
	 *            long : The units of a product the Bin starts with. Must be
	 *            positive.
	 * @param showInReport
	 *            boolean : Flag, if Bin should produce a report or not.
	 * @param showInTrace
	 *            boolean : Flag for trace to produce trace messages.
	 */
	public Bin(desmoj.core.simulator.Model owner, String name,
			long initialUnits, boolean showInReport, boolean showInTrace) {
		super(owner, name, showInReport, showInTrace); // construct QueueBased
		reset();

		_queue = new QueueListFifo<SimProcess>(); // make an actual queue and give it a
		_queue.setQueueBased(this); // reference of this "QueueBased"-Bin

		this._initial = initialUnits;
		this._maximum = initialUnits;
		this._avail = initialUnits;

		if (initialUnits < 0) // there can't be less than nothing
		{
			sendWarning("Attempt to construct a Bin with a negativ number of"
					+ " units. Initial number of units set to zero!", "Bin: "
					+ getName() + " Constructor: Bin (desmoj.Model owner, "
					+ "String name, long initialUnits, boolean showInReport, "
					+ "boolean showInTrace)",
					"A negative number of units does not make sense here.",
					"Make sure to initialize a Bin always with a positive number of "
							+ "initialUnits.");

			_initial = _maximum = _avail = 0; // set it to 0, that makes more
			// sense
		}
	}

    // ****** methods ******

	/**
	 * Returning the average number of products available in the bin over the
	 * time since the last reset of the bin.
	 * 
	 * @return double : The average number of products available in the bin over
	 *         the time since the last reset of the bin.
	 */
	public double avgAvail() {
		TimeInstant now = presentTime(); // what is the time?
		// how long since the last reset
		TimeSpan diff = TimeOperations.diff(now, resetAt());

		// update the weighted sum of available units
		double wSumAvl = _wSumAvail
				+ ((double) _avail * TimeOperations.diff(now, _lastUsage)
						.getTimeInEpsilon());

		if (diff.isZero()) // diff is not long enough
		{
			sendWarning("A Division-by-Zero error occured in a calculation. "
					+ "The UNDEFINED Value: -1.0 is returned as result.",
					"Bin: " + getName() + " Method: double avgAvail ()",
					"The Time difference is shorter than epsilon.",
					"Make sure not to use avgAvail() right after a reset.");
			return UNDEFINED; // see QueueBased: UNDEFINED = -1
		}
		// return the rounded average
		return StatisticObject.round(wSumAvl / diff.getTimeInEpsilon());
	}

	/**
	 * Returns a Reporter to produce a report about this Bin.
	 * 
	 * @return desmoj.report.Reporter : The Reporter for the queue inside this
	 *         bin
	 */
	public desmoj.core.report.Reporter createDefaultReporter() {
		return new desmoj.core.advancedModellingFeatures.report.BinReporter(
				this);
	}

	/**
	 * Returns the number of units available at the moment.
	 * 
	 * @return long : The number of units available at the moment.
	 */
	public long getAvail() {
		return this._avail;
	}

	/**
	 * Returns the number of consumers having visited the Bin.
	 * 
	 * @return long : The number of consumers having visited the bin.
	 */
	public long getConsumers() {
		return this._consumers;
	}

	/**
	 * Returns the initial number of products in the bin.
	 * 
	 * @return long : The number of products the bin started with.
	 */
	public long getInitial() {
		return this._initial;
	}

	/**
	 * Returns the maximum number of products in the bin.
	 * 
	 * @return long : The maximum number of products in the bin.
	 */
	public long getMaximum() {
		return this._maximum;
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
	 * Returns the number of producers having used the Bin.
	 * 
	 * @return long : The number of producers having put products in the bin.
	 */
	public long getProducers() {
		return this._producers;
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
	 * To reset the statistics of this bin. The number of available products at
	 * this moment and the processes waiting in the queue are not changed. But
	 * all statistic counters are reset. The <code>QueueBased</code> is also
	 * reset.
	 */
	public void reset() {
		super.reset(); // reset the QueueBased also

		_maximum = _avail;
		_producers = 0;
		_consumers = 0;
		_wSumAvail = 0.0;
		_lastUsage = presentTime();
		_refused = 0;
	}

	/**
	 * Method for consumers to make the Bin deliver a number of n products. When
	 * there are not enough products available or another consumer is first in
	 * the queue to be served (and it is not possible to pass by), the current
	 * consumer process will be blocked and inserted in the waiting-queue.
	 * 
     * The order of process activation depends on the order of the internal
     * queue, which is based on the consumers processes' queueing 
     * priorities and (if queueing priorities are equal, requiring
     * a tie-breaker) by FIFO or a different discipline as defined in the
     * constructor. 
	 * 
	 * @return boolean : Is <code>true</code> if the specified number of units
	 *         have been delivered successfully, <code>false</code> otherwise
	 *         (i.e. capacity limit of the queue is reached).
	 * @param n
	 *            long : The number of products the Bin is delivering to the
	 *            consumer.
	 */
	public boolean retrieve(long n) {
		_where = "boolean retrieve (long n)";

		SimProcess currentProcess = currentSimProcess();

		if (!checkProcess(currentProcess, _where)) // check the current process
		{
			return false;
		} // if it is not valid return false

		if (n <= 0) // if the process is taking nothing or less
		{
			sendWarning(
					"Attempt to take nothing or a negative number of units"
							+ " out of a Bin. The attempted action is ignored!",
					"Bin: " + getName() + " Method: " + _where,
					"It does not make sense to take nothing or less out of a Bin. "
							+ "The statistic will be corrupted with negative numbers!",
					"Make sure to take at least one unit out of the Bin.");
			return false; // go to where you came from; ignore that rubbish
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

		// insert every process in the queue for statistic reasons
		_queue.insert(currentProcess);

		// is it possible for this process to pass by?
		if (_passBy == false) {
			if (n > _avail || // not enough products available OR
					currentProcess != _queue.first()) // other process is
			// first
			// in the q
			{
				// tell in the trace what the process is waiting for
				if (currentlySendTraceNotes()) {
					sendTraceNote("awaits " + n + " of '" + this.getName()
							+ "'");
				}

				// for debugging purposes
				if (currentlySendDebugNotes()) {
					sendDebugNote("can not deliver " + n + " units for "
							+ currentProcess.getQuotedName() + "<br>"
							+ "because there are only " + getAvail()
							+ " units " + "right now.");
				}

				do { // the process is stuck in here
					currentProcess.setBlocked(true); // as long as ...see
					// while
					currentProcess.skipTraceNote(); // don't tell the user, that
					// we ...
					currentProcess.passivate(); // passivate the current process
				} while (n > _avail || // not enough products available OR
						currentProcess != _queue.first()); // other process is
				// first
			} // end if

		} // end if (passBy = false)

		else // the process can pass by other processes in the queue, passBy
		// =
		// true
		{
			if (n > _avail || // not enough products available OR
					currentProcess != _queue.first()) // other process is
			// first
			// in the q
			{
				// if this process is not the first in the queue
				if (currentProcess != _queue.first()) {
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
						sendTraceNote("awaits " + n + " of '" + this.getName()
								+ "'");
					}

					// for debugging purposes
					if (currentlySendTraceNotes()) {
						sendDebugNote("can not deliver " + n + " units for "
								+ currentProcess.getQuotedName() + "<br>"
								+ "because there are only " + getAvail()
								+ " units " + "right now.");
					}
				} // end if not enough units are available

				// block and passivate the process until enough products are
				// available
				do { // the process is stuck in here
					currentProcess.setBlocked(true); // as long as ...see
					// while
					currentProcess.skipTraceNote(); // don't tell the user, that
					// we ...
					currentProcess.passivate(); // passivate the current process

					// activate the next process in the queue to see what he can
					// do
					activateAsNext(_queue.succ(currentProcess));
				} while (n > _avail); // not enough products available
			}
		} // end else (passBy = true)

		// the current process has got the products he wanted ...

		// tell in the trace what the process is taking from the Bin
		if (currentlySendTraceNotes()) {
			sendTraceNote("takes " + n + " from '" + this.getName() + "'");
		}

		_queue.remove(currentProcess); // get the process out of the queue
		currentProcess.setBlocked(false); // we are not blocked (anymore),
		// yeah!

		// activate the new first process in the queue
		activateFirst();

		updateStatistics(-n); // statistics will be updated
		// with a negative n for deliver(), remember?!
		return true;
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
	 * Method for producers to put a number of n new products in the Bin. When
	 * producers <code>store()</code> new products n is positive. Producers can
	 * always store products into the Bin and will never be inserted in a queue
	 * and blocked, because the Bin has no capacity limit..
	 * 
	 * @param n
	 *            long : The number of products put into the Bin.
	 */
	public void store(long n) {
		_where = "void store (long n)";

		SimProcess currentProcess = currentSimProcess();

		if (!checkProcess(currentProcess, _where)) // if the current process
		{
			return;
		} // is not valid: just return

		if (n <= 0) // trying to store nothing or less, don't fool me!
		{
			sendWarning(
					"Attempt to store nothing or a negative number of units"
							+ " in a Bin. The attempted action is ignored!",
					"Bin: " + getName() + " Method: void store (long n)",
					"It does not make sense to put nothing or less into a Bin. "
							+ "The statistic will be corrupted with negative numbers!",
					"Make sure to store at least one unit in the Bin.");
			return; // ignore it and just return
		}

		if (currentlySendTraceNotes()) {
			sendTraceNote("stores " + n + " to '" + getName() + "'");
		} // tell in the trace what the process is giving into the Bin

		updateStatistics(n);

		// see if someone is in the queue waiting for products
		activateFirst();
	}

	/**
	 * Activates the SimProcess <code>process</code>, given as a parameter of
	 * this method, as the next process. This process should be a SimProcess
	 * waiting in the queue for some products.
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
	 * was trying to take products, but it could not get any because there were
	 * not enough products for it or another process was first in the queue to
	 * be served. This method is called every time a producer has given new
	 * products or a consumer in the waiting-queue is satisfied.
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
	 * Checks whether the entity using the bin is a valid process.
	 * 
	 * @return boolean : Returns whether the SimProcess is valid or not.
	 * @param p
	 *            SimProcess : Is this SimProcess a valid one?
	 * @param where
	 *            String : The method having called <code>checkProcess()</code>
	 *            as a String.
	 */
	protected boolean checkProcess(SimProcess p, String where) {
		if (p == null) // if p is a null pointer instead of a process
		{
			sendWarning(
					"A non existing process is trying to use a Bin object. "
							+ "The attempted action is ignored!", "Bin: "
							+ getName() + " Method: " + where,
					"The process is only a null pointer.",
					"Make sure that only real SimProcesses are using Bins.");
			return false;
		}

		if (!isModelCompatible(p)) // if p is not modelcompatible
		{
			sendWarning("The process trying to use a Bin object does not "
					+ "belong to this model. The attempted action is ignored!",
					"Bin: " + getName() + " Method: " + where,
					"The process is not modelcompatible.",
					"Make sure that processes are using only Bins within their model.");
			return false;
		}

		return true;
	}

	/**
	 * Updates the statistics for producers and consumers.
	 * 
	 * @param n
	 *            long : Is positive when producers <code>store()</code> new
	 *            products in the bin and negative when the Bin
	 *            <code>deliver()</code>'s products to the consumer process.
	 */
	protected void updateStatistics(long n) {
		TimeInstant now = presentTime();
		_wSumAvail = _wSumAvail
				+ ((double) _avail * TimeOperations.diff(now, _lastUsage)
						.getTimeInEpsilon());

		_lastUsage = now;
		_avail += n; // n can be positive or negative

		if (n > 0) // it is a real producer
		{
			_producers++;
			if (_avail > _maximum) {
				_maximum = _avail;
			}
		} else // it is a consumer
		{
			_consumers++;
		}
	}
}