package desmoj.core.simulator;

import java.util.Iterator;

/**
 * ProcessQueue provides models with a ready-to-use element to enqueue
 * <code>SimProcess</code>es in. The sort order of the ProcessQueue is
 * determined first by the priorities of the enqueued SimProcesses and second by
 * the given sort order. The default sort order is FIFO (first in, first out)
 * but others like LIFO (last in, first out) can be chosen, too. See the
 * constants in class <code>QueueBased</code> and the derived classes from
 * <code>QueueList</code>. The capacity of the ProcessQueue, that is the
 * maximum number of SimProcesses enqueued, can be chosen, too. Note that in
 * contrast to the 'plain' queue, this ProcessQueue always expects and returns
 * objects that are derived from class <code>SimProcess</code>. When
 * modelling using the process-, activity-, or transaction-oriented paradigm
 * where SimProcesses are used to represent the model's entities, this
 * ProcessQueue can be used instead of the standard Queue to reduce the amount
 * of casts needed otherwise.
 * 
 * @see QueueBased
 * @see QueueList
 * @see QueueListFifo
 * @see QueueListLifo
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
 * @author modified by Soenke Claassen
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
public class ProcessQueue<P extends SimProcess> extends QueueBased implements Iterable<P> {

	/**
     * Private queue iterator, e.g. required for processing all queue elements in a 
     * for-each-loop.
     */
    private class ProcessQueueIterator implements Iterator<P> {
        
        ProcessQueue<P> clientQ; 
        P next, lastReturned;
        
        public ProcessQueueIterator(ProcessQueue<P> clientQ) {
            this.clientQ = clientQ;
            next = clientQ.first();
            lastReturned = null;
        }
        public boolean hasNext() {
            return next != null;
        }
        public P next() {
            lastReturned = next;
            next = clientQ.succ(next);
            return lastReturned;
        }
        public void remove() {
            clientQ.remove(lastReturned);
        }
    }

	/**
	 * The queue implementation that actually stores the entities
	 */
	private QueueList<P> _ql;	

	/**
	 * Counter for the SimProcesses which are refused to be enqueued, because
	 * the queue capacity is full.
	 */
	private long _refused;

	/**
	 * Constructs a simple priority and FIFO based waiting-queue for 
	 * SimProcesses with a maximum capacity of 2,147,483,647 waiting 
	 * processes, which should serve as an approximation of infinite 
	 * queues sufficiently well for most purposes. 
     * <p>
     * The usage of the generic version <code>ProcessQueue&lt;Type&gt;</code> where 
     * <code>Type</code> is derived from <code>SimProcess</code> is recommended
     * for type safety. Using the raw type <code>ProcessQueue</code> yields a queue
     * in which any <code>SimProcess</code> can be enqueued, potentially requiring
     * type casting on accessing processes enqueued.  
	 * 
	 * @param owner
	 *            Model : The model this process-queue is associated to
	 * @param name
	 *            java.lang.String : The process-queue's name
	 * @param showInReport
	 *            boolean : Flag if process-queue should produce a report
	 * @param showInTrace
	 *            boolean : Flag for process-queue to produce trace messages
	 */
	public ProcessQueue(Model owner, String name, boolean showInReport,
			boolean showInTrace)
	{
		super(owner, name, showInReport, showInTrace); // create the QBased
		// object
		reset();

		// make the queue with Fifo queueing discipline and unlimited capacity
		_ql = new QueueListFifo<P>();
		_ql.setQueueBased(this);
		
	}

	/**
	 * Constructs a simple priority based waiting-queue for SimProcesses, the
	 * kind of queue implementation (FIFO or LIFO) and the capacity of the queue
	 * can be chosen.
	 * <p>
     * The usage of the generic version <code>ProcessQueue&lt;Type&gt;</code> where 
     * <code>Type</code> is derived from <code>SimProcess</code> is recommended
     * for type safety. Using the raw type <code>ProcessQueue</code> yields a queue
     * in which any <code>SimProcess</code> can be enqueued, potentially requiring
     * type casting on accessing processes enqueued.  
	 * 
	 * @param owner
	 *            Model : The model this ProcessQueue is associated to
	 * @param name
	 *            java.lang.String : The process-queue's name
     * @param sortOrder
     *            int : determines the sort order of the underlying queue
     *            implementation. Choose a constant from <code>QueueBased</code>:
     *            <code>QueueBased.FIFO</code>, <code>QueueBased.LIFO</code> or
     *            QueueBased.Random.
     * @param qCapacity
     *            int : The capacity of the Queue, that is how many processes can
     *            be enqueued. Zero (0) can be used as shortcut for
     *            for a capacity of <code>Integer.MAX_VALUE</code> = 2,147,483,647,
     *            which should approximate an infinite queue sufficiently well
     *            for most purposes.
	 * @param showInReport
	 *            boolean : Flag if process-queue should produce a report
	 * @param showInTrace
	 *            boolean : Flag for process-queue to produce trace messages
	 */
	public ProcessQueue(Model owner, String name, int sortOrder, int qCapacity,
			boolean showInReport, boolean showInTrace) {

		super(owner, name, showInReport, showInTrace); // create the QBased
		// object
		reset();

        // determine the queueing strategy
        switch (sortOrder) {
        case QueueBased.FIFO :
            _ql = new QueueListFifo<P>(); break;
        case QueueBased.LIFO :
            _ql = new QueueListLifo<P>(); break;
        case QueueBased.RANDOM :
            _ql = new QueueListRandom<P>(); break;
        default :
            sendWarning(
                    "The given sortOrder parameter " + sortOrder + " is not valid! "
                            + "A queue with Fifo sort order will be created.",
                    "ProcessQueueQueue : "
                            + getName()
                            + " Constructor: ProcessQueue(Model owner, String name, "
                            + "int sortOrder, long qCapacity, boolean showInReport, "
                            + "boolean showInTrace)",
                    "A valid positive integer number must be provided to "
                            + "determine the sort order of the queue.",
                    "Make sure to provide a valid positive integer number "
                            + "by using the constants in the class QueueBased, like "
                            + "QueueBased.FIFO, QueueBased.LIFO or QueueBased.RANDOM.");
            _ql = new QueueListFifo<P>(); 
        }
        
        // give the QueueList a reference to this QueueBased
        _ql.setQueueBased(this);

		// set the capacity of the queue
		queueLimit = qCapacity;

		// check if it the capacity does make sense
		if (qCapacity < 0) {
			sendWarning(
                    "The given capacity of the queue is negative! "
                            + "A queue with maximum capacity (2,147,483,647) will be created instead.",
					"ProcessQueue : "
							+ getName()
							+ " Constructor: ProcessQueue(Model owner, String name, "
							+ "int sortOrder, long qCapacity, boolean showInReport, "
							+ "boolean showInTrace)",
					"A negative capacity for a queue does not make sense.",
					"Make sure to provide a valid positive capacity "
							+ "for the queue.");
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
     * Returns <code>true</code> if the given <code>SimProcess</code> is
     * contained in the queue; <code>false</code> otherwise.
     * 
     * @return boolean : <code>True</code> if the given
     *         <code>SimProcess</code> is contained in the queue;
     *         <code>false</code> otherwise.
     * @param e
     *            E : The <code>Entity</code> we are looking for
     *            in the queue.
     */
    public boolean contains(P p) {
        
        return _ql.contains(p); // delegated to the internal queue list
    }

	/**
	 * Returns a process-queue-reporter to produce a report about this
	 * process-queue.
	 * 
	 * @return desmoj.report.Reporter : The reporter for this process-queue
	 */
	public desmoj.core.report.Reporter createDefaultReporter() 
	{

		return new desmoj.core.report.ProcessQueueReporter(this);

	}
	
	   /**
	 * Returns the first SimProcess queued in this process-queue or
	 * <code>null</code> in case the queue is empty.
	 * 
	 * @return P : The first SimProcess in the process-queue or
	 *         <code>null</code> if the process-queue is empty
	 */
	public P first() {

		return _ql.first(); // straight design

	}
    
    /**
	 * Returns the first SimProcess queued in this process-queue that applies to
	 * the given condition. The process-queue is searched from front to end and
	 * the first SimProcess that returns <code>true</code> when the condition
	 * is applied to it is returned by this method. If no SimProcess applies to
	 * the given condition or the process-queue is empty, <code>null</code>
	 * will be returned.
	 * 
	 * @return P :The first process queued in this
	 *         process-queue applying to the given condition or <code>null</code>
	 * @param c
	 *            Condition : The condition that the SimProcess returned must
	 *            confirm
	 */
	public P first(Condition<P> c) {

		if (c == null) {
			sendWarning(
					"Can not return first SimProcess fulfilling the condition!",
					"ProcessQueue : " + getName()
							+ " Method: void first(Condition c)",
					"The Condition 'c' given as parameter is a null reference!",
					"Check to always have valid references when querying Queues.");
			return null; // no proper parameter
		}
		if (_ql.isEmpty())
			return null; // nobody home to be checked
		for (P tmp = _ql.first(); tmp != null; tmp = _ql.succ(tmp)) {
			if (c.check(tmp))
				return tmp;
		}

		// if no SimProcess fulfills the condition just return null
		return null;

	}
	
	/**
	 * Returns the <code>SimProcess</code> queued at the named position.
	 * The first position is 0, the last one size()-1.
	 * 
	 * @return P :The <code>SimProcess</code> at the position of
	 *         <code>int</code> or <code>null</code> if no such position exists.
	 */
	public P get(int index) {
		return _ql.get(index);
	}

	/**
	 * Returns the queue index of a given <code>SimProcess</code>.
	 * 
	 * @return int :The position of the process as an <code>int</code>. 
	 * 				Returns -1 if no such position exists.
	 */
	public int get(P p)  
	{
		
		return _ql.get(p);
		
	}

    /**
	 * Returns the waiting time of a process present in the queue.
	 * If the process given is not queued, <code>null</code> will be returned.
	 * 
	 * @param p
	 *            P : The process whose waiting time is looked for
	 *            
	 * @return TimeSpan : The waiting time of the process or <code>null</code>.
	 */
	public TimeSpan getCurrentWaitTime(P p) {

		if (p == null) {
			sendWarning("Cannot query waiting time!", "Queue : "
					+ getName() + " Method:  void getCurrentWaitTime(SimProcess p)",
					"The Process 'p' given as parameter is a null reference!",
					"Check to always have valid references when querying"
							+ "waiting durations");
			return null; // no proper parameter
		}
		TimeInstant i = _ql.timemap.get(p);
		
		if (i == null) {
			sendWarning("Cannot query waiting time!", "Queue : "
					+ getName() + " Method:  void getCurrentWaitTime(SimProcess p)",
					"The Process 'p' ("+p.getQuotedName()+") given as parameter is not enqueued in this "
							+ "queue!",
					"Make sure the process is inside the queue you want it to "
							+ "be queried.");
			return null; // not enqueued here
		}

		return TimeOperations.diff(this.presentTime(), i);
	}

	/**
     * Returns the underlying queue implementation, providing access to the
     * QueueList implementation, e.g. to add PropertyChangeListeners.
     * 
     * @return QueueList : The underlying queue implementation of this
     *         ProcessQueue.
     */
    public QueueList<P> getQueueList() {

        return _ql; // that's all
    }

	/**
	 * Returns the implemented queueing discipline of the underlying queue as a
	 * String, so it can be displayed in the report.
	 * 
	 * @return String : The String indicating the queueing discipline.
	 */
	public String getQueueStrategy() {

		return _ql.getAbbreviation(); // that's it

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
	 * Enters a new SimProcess into the ProcessQueue. If the capacity of the
	 * ProcessQueue is full, the entity will not be enqueued and
	 * <code>false</code> will be returned. The SimProcess will be stored in
	 * the ProcessQueue until method <code>remove(SimProcess p)</code> is
	 * called with this specific SimProcess. Simprocesses are ordered according
	 * to their priority. Higher priorities are sorted in front of lower
	 * priorities. Simprocesses with same priority are orderer according to the
	 * strategy specified in the constructor. The first SimProcess inside the
	 * process-queue will always be the one with the highest priority.
	 * 
	 * @return boolean : Is <code>true</code> if insertion was successful,
	 *         <code>false</code> otherwise (i.e. capacity limit is reached).
	 * @param P
	 *            p : The SimProcess to be added to the
	 *            ProcessQueue
	 */
	public boolean insert(P p) {

		if (p == null) { // null returns with warning
			sendWarning("Can not insert SimProcess!", "ProcessQueue : "
					+ getName() + " Method: boolean insert" + "(P p)",
					"The SimProcess 'p' given as parameter is a null reference!",
					"Check to always have valid references when enqueueing "
							+ "Entities");
			return false; // no proper parameter
		}

		if (!isModelCompatible(p)) {
			sendWarning("Can not insert SimProcess!", "ProcessQueue : "
					+ getName() + " Method: boolean insert" + "(P p)",
					"The SimProcess 'p' ("+p.getQuotedName()+") given as parameter is not compatible to "
							+ "the model this process-queue belongs to!",
					"Check if your submodels are allowed to mingle with other "
							+ "model's components.");
			return false; // not of my model type!!!
		}

		if (queueLimit <= length()) {

			if (currentlySendDebugNotes()) { 
				sendDebugNote("refuses to insert " + p.getQuotedName()
					+ " because the "
					+ "capacity limit is reached. ProcessQueue:<br>"
					+ _ql.toString());
			}

			if (currentlySendTraceNotes()) {
				sendTraceNote("is refused to be enqueued in "
					+ this.getQuotedName() + "because the capacity limit ("
					+ getQueueLimit() + ") of this "
					+ "ProcessQueue is reached");
			}

			_refused++; // count the refused ones

			return false; // capacity limit is reached
		}

		_ql.insert(p); // that's it

		if (currentlySendDebugNotes()) {
			sendDebugNote("inserts " + p.getQuotedName()
					+ " in the ProcessQueue:<br>" + _ql.toString());
		}

		// produce trace output
		if (currentlySendTraceNotes()) {
			if (p == currentEntity() && currentEntityAll().size() == 1) {
				sendTraceNote("inserts itself into " + this.getQuotedName());
			} else {
				sendTraceNote("inserts " + p.getName() + " into "
						+ this.getQuotedName());
			}
		}

		return true;
	}

	/**
	 * Enters a new SimProcess into the process-queue and places it after the
	 * given SimProcess. If the capacity of the ProcessQueue is full, the entity
	 * will not be enqueued and <code>false</code> will be returned. Make sure
	 * that the SimProcess given as reference is already queued inside the
	 * process-queue, else the SimProcess will not be enqueued and
	 * <code>false</code> will be returned. The SimProcess will be stored in
	 * the ProcessQueue until method <code>remove(SimProcess p)</code> is
	 * called with this specific SimProcess.
	 * 
	 * @return boolean : Is <code>true</code> if insertion was successful,
	 *         <code>false</code> otherwise (i.e. capacity limit is reached).
	 * @param p
	 *            P :The SimProcess to be added to the process-queue
	 * @param after
	 *            P :The SimProcess after which SimProcess 'p' is to
	 *            be inserted
	 */
	public boolean insertAfter(P p, P after) {

		if (p == null) {
			sendWarning(
					"Can not insert SimProcess!",
					"ProcessQueue : "
							+ getName()
							+ " Method: boolean insertAfter(P p, P after)",
					"The SimProcess 'p' given as parameter is a null reference!",
					"Check to always have valid references when enqueueing Entities");
			return false; // no proper parameter
		}

		if (after == null) {
			sendWarning(
					"Can not insert SimProcess!",
					"ProcessQueue : "
							+ getName()
							+ " Method: boolean insertAfter(P e, P after)",
					"The SimProcess 'after' given as parameter is a null reference!",
					"Check to always have valid references when enqueueing Entities");
			return false; // no proper parameter
		}

		if (!isModelCompatible(p)) {
			sendWarning(
					"Can not insert SimProcess!",
					"ProcessQueue : "
							+ getName()
							+ " Method: boolean insertAfter(P p, P after)",
					"The SimProcess 'p' given as parameter is not compatible to "
							+ "the model this process-queue belongs to!",
					"Check if your submodels are allowed to mingle with other "
							+ "model's components.");
			return false; // not of my model type!!!
		}

		if (queueLimit <= length()) {

			if (currentlySendDebugNotes()) { 
				sendDebugNote("refuses to insert " + p.getQuotedName()
					+ " because the "
					+ "capacity limit is reached. ProcessQueue:<br>"
					+ _ql.toString());
			}

			if (currentlySendTraceNotes()) {
				sendTraceNote("is refused to be enqueued in "
					+ this.getQuotedName() + "because the capacity limit ("
					+ getQueueLimit() + ") of this "
					+ "ProcessQueue is reached");
			}

			_refused++; // count the refused ones

			return false; // capacity limit is reached
		}

		boolean successful = _ql.insertAfter(p, after); // elegantly done...
		
		if (currentlySendDebugNotes()) {
			sendDebugNote("inserts " + p.getQuotedName() + " after "
					+ after.getQuotedName() + " in the ProcessQueue:<br>"
					+ _ql.toString());
		}

		// produce trace output
		if (currentlySendTraceNotes()) {
			if (p == currentEntity() && currentEntityAll().size() == 1) {
				sendTraceNote("inserts itself into " + this.getQuotedName()
						+ " after " + after.getName());
			} else {
				sendTraceNote("inserts " + p.getName() + " into "
						+ this.getQuotedName() + " after " + after.getName());
			}
		}

		return successful;

	}

	/**
	 * Enters a new SimProcess into the ProcessQueue and places it in front of
	 * the given SimProcess. If the capacity of the ProcessQueue is full, the
	 * Entity will not be enqueued and <code>false</code> will be returned.
	 * Make sure that the SimProcess given as reference is already queued inside
	 * the ProcessQueue, else the SimProcess will not be enqueued and
	 * <code>false</code> will be returned. The SimProcess will be stored in
	 * the ProcessQueue until method <code>remove(SimProcess p)</code> is
	 * called with this specific SimProcess.
	 * 
	 * @return boolean : Is <code>true</code> if insertion was successful,
	 *         <code>false</code> otherwise (i.e. capacity limit is reached).
	 * @param p
	 *            P : The SimProcess to be added to the processqQueue
	 * @param before
	 *            P : The SimProcess before which the SimProcess 'p' is
	 *            to be inserted
	 */
	public boolean insertBefore(P p, P before) {

		if (p == null) {
			sendWarning(
					"Can not insert SimProcess!",
					"ProcessQueue : "
							+ getName()
							+ " Method: boolean insertBefore(P p, P before)",
					"The SimProcess 'p' given as parameter is a null reference!",
					"Check to always have valid references when enqueueing Entities");
			return false; // no proper parameter
		}

		if (before == null) {
			sendWarning(
					"Can not insert SimProcess!",
					"ProcessQueue : "
							+ getName()
							+ " Method: boolean insertBefore(P p, P before)",
					"The SimProcess 'before' given as parameter is a null reference!",
					"Check to always have valid references when enqueueing Entities");
			return false; // no proper parameter
		}

		if (!isModelCompatible(p)) {
			sendWarning(
					"Can not insert SimProcess!",
					"ProcessQueue : "
							+ getName()
							+ " Method: boolean insertBefore(P p, P before)",
					"The SimProcess 'p' given as parameter is not compatible to "
							+ "the model this process-queue belongs to!",
					"Check if your submodels are allowed to mingle with other "
							+ "model's components.");
			return false; // not of my model type!!!
		}

		if (queueLimit <= length()) {

			if (currentlySendDebugNotes()) {
				sendDebugNote("refuses to insert " + p.getQuotedName()
					+ " because the "
					+ "capacity limit is reached. ProcessQueue:<br>"
					+ _ql.toString());
			}

			if (currentlySendTraceNotes()) {
				sendTraceNote("is refused to be enqueued in "
					+ this.getQuotedName() + "because the capacity limit ("
					+ getQueueLimit() + ") of this "
					+ "ProcessQueue is reached");
			}

			_refused++; // count the refused ones

			return false; // capacity limit is reached
		}

		boolean successful = _ql.insertBefore(p, before); // elegantly done...
		
		if (currentlySendDebugNotes()) {
			sendDebugNote("inserts " + p.getQuotedName() + " before "
					+ before.getQuotedName() + " in the ProcessQueue:<br>"
					+ _ql.toString());
		}

		// produce trace output
		if (currentlySendTraceNotes()) {
			if (p == currentEntity() && currentEntityAll().size() == 1) {
				sendTraceNote("inserts itself into " + this.getQuotedName()
						+ " before " + before.getName());
			} else {
				sendTraceNote("inserts " + p.getName() + " into "
						+ this.getQuotedName() + " before " + before.getName());
			}
		}

		return successful;
	}

	/**
	 * Returns a boolean value indicating if the process-queue is empty or if any
	 * number of SimProcess is currently enqueued in it.
	 * 
	 * @return boolean : Is <code>true</code> if the process-queue is empty,
	 *         <code>false</code> otherwise
	 */
	public boolean isEmpty() {

		return _ql.isEmpty();

	}

	/**
     * Returns an iterator over the processes enqueued.
     *
     * @return java.lang.Iterator&lt;P&gt; : An iterator over the processes enqueued.
     */
    public Iterator<P> iterator() {
        return new ProcessQueueIterator(this);
    }

	/**
	 * Returns the last SimProcess queued in this process-queue or
	 * <code>null</code> in case the process-queue is empty.
	 * 
	 * @return P : The last SimProcess in the process-queue or
	 *         <code>null</code> if the process-queue is empty
	 */
	public P last() {

		return _ql.last(); // straight design again

	}

	/**
	 * Returns the last SimProcess queued in this process-queue that applies to
	 * the given condition. The process-queue is searched from end to front and
	 * the first SimProcess that returns <code>true</code> when the condition
	 * is applied to it is returned by this method. If no SimProcess applies to
	 * the given condition or the process-queue is empty, <code>null</code>
	 * will be returned.
	 * 
	 * @return P : The last SimProcess queued in this
	 *         process-queue applying to the given condition or <code>null</code>
	 * @param c
	 *            Condition : The condition that the SimProcess returned must
	 *            fulfill
	 */
	public P last(Condition<P> c) {

		if (c == null) {
			sendWarning(
					"Can not insert SimProcess!",
					"ProcessQueue : " + getName()
							+ " Method: SimProcess last(Condition c)",
					"The Condition -c- given as parameter is a null reference!",
					"Check to always have valid references when querying Queues.");
			return null; // no proper parameter
		}

		if (_ql.isEmpty())
			return null; // nobody home to be checked

		for (P tmp = _ql.last(); tmp != null; tmp = _ql.pred(tmp)) {
			if (c.check(tmp))
				return tmp;
		}

		// if no SimProcess fulfills the condition just return null
		return null;

	}

	/**
	 * Returns the SimProcess enqueued directly before the given SimProcess in
	 * the process-queue. If the given SimProcess is not contained in this
	 * process-queue or is at the first position thus having no possible
	 * predecessor, <code>null</code> is returned.
	 * 
	 * @return P : The SimProcess directly before the given
	 *         SimProcess in the process-queue or <code>null</code>.
	 * @param p
	 *            P : An SimProcess in the process-queue
	 */
	public P pred(P p) {

		if (p == null) {
			sendWarning(
					"Can not find predecessor of SimProcess in Queue!",
					"ProcessQueue : " + getName()
							+ " Method: SimProcess pred(P p)",
					"The SimProcess 'p' given as parameter is a null reference!",
					"Check to always have valid references when querying for Entities");
			return null; // no proper parameter
		}

		return _ql.pred(p);

	}
	
	/**
	 * Returns the SimProcess enqueued before the given SimProcess in the
	 * process-queue that also fulfills the condition given. If the given
	 * SimProcess is not contained in this process-queue or is at the first
	 * position thus having no possible predecessor, <code>null</code> is
	 * returned. If no other SimProcess before the given one fulfills the
	 * condition, <code>null</code> is returned, too.
	 * 
	 * @return P : The SimProcess before the given SimProcess in
	 *         the process-queue fulfilling to the condition or <code>null</code>.
	 * @param p
	 *            P : A SimProcess in the process-queue
	 * @param c
	 *            Condition : The condition that the preceeding SimProcess has
	 *            to fulfill
	 */
	public P pred(P p, Condition<P> c) {

		if (p == null) {
			sendWarning(
					"Can not find predecessor of SimProcess in Queue!",
					"ProcessQueue : "
							+ getName()
							+ " Method: SimProcess pred(P p, Condition c)",
					"The SimProcess 'p' given as parameter is a null reference!",
					"Check to always have valid references when querying for Entities");
			return null; // no proper parameter
		}

		if (c == null) {
			sendWarning(
					"Can not return previous SimProcess fulfilling condition!",
					"ProcessQueue : "
							+ getName()
							+ " Method: SimProcess pred(P p, Condition c)",
					"The Condition 'c' given as parameter is a null reference!",
					"Check to always have valid references when querying Queues.");
			return null; // no proper parameter
		}

		for (P tmp = pred(p); tmp != null; tmp = pred(tmp)) {
			if (c.check(tmp))
				return tmp;
		}

		return null; // obviously not found here, empty or doesn't fulfill

	}
	
    /**
     * Removes the process queued at the given position.
     * The first position is 0, the last one length()-1.
     * 
     * @return : The method returns <code>true</code> if a <code>SimProcess</code>
     *           exists at the given position or <code>false></code> if otherwise.
     */
	public boolean remove(int index)  
	{
		if (index < 0 || index >= this.length()) return false;
	    
	    P p = get(index);
		if (p == null) {
		    return false;
		} else {
		    remove(p);
		    return true;
		}
	}
		
	/**
	 * Removes the given SimProcess from the process-queue. If the given
	 * SimProcess is not in the process-queue, a warning will be issued but
	 * nothing else will be changed.
	 * 
	 * @param p
	 *            P :The SimProcess to be removed from the
	 *            process-queue
	 */
	public void remove(SimProcess p) {

		if (p == null) {
			sendWarning(
					"Can not remove SimProcess from Queue!",
					"ProcessQueue : " + getName()
							+ " Method:  void remove(SimProcess p)",
					"The SimProcess 'p' given as parameter is a null reference!",
					"Check to always have valid references when removing "
							+ "Entities");
			return; // no proper parameter
		}
		
		if (!_ql.remove((P)p)) 
		{ // watch out, removes SimProcess as a side
			// effect!!!
			sendWarning("Can not remove SimProcess from Queue!",
					"ProcessQueue : " + getName()
							+ " Method:  void remove(SimProcess p)",
					"The SimProcess 'p' ("+p.getQuotedName()+") given as parameter is not enqueued in "
							+ "this queue!",
					"Make sure the SimProcess is inside the queue you want it "
							+ "to be removed.");
			return; // not enqueued here
		}
		else // done
		{

		}

		if (currentlySendDebugNotes()) {
			sendDebugNote("remove " + p.getQuotedName() + "<br>"
					+ _ql.toString());
		}

		// produce trace output
		if (currentlySendTraceNotes()) {
			if (p == currentEntity() && currentEntityAll().size() == 1) {
				sendTraceNote("removes itself from " + this.getQuotedName());
			} else {
				sendTraceNote("removes " + p.getQuotedName() + " from "
						+ this.getQuotedName());
			}
		}

	}
    
    
    /**
     * Removes all processes from the Queue. Has no effect on empty queues.
     */
    public void removeAll() {
        
        while (!isEmpty()) {
            removeFirst();            
        }
    }	
    
   /**
 * Removes the first process from the queue and provides a reference to
 * this process. If the queue is empty, null is returned.
 * 
 * @return P : The first process in this queue, which has been removed,
 *      or <code>null</code> in case the queue was empty
 */
public P removeFirst() {
    
    P first = this.first();
    
    if (first != null) {

        _ql.remove(first); 

        // produce trace output
        if (currentlySendTraceNotes()) {
            if (first == currentEntity() && currentEntityAll().size() == 1) {
                sendTraceNote("removes itself from " + this.getQuotedName());
            } else {
                sendTraceNote("removes " + first.getQuotedName() + " from "
                        + this.getQuotedName());
            }
        }
    
    }
    
    return first;
}

   /**
 * Removes the first process from the queue that fulfills to the given
 * condition. Also provides a reference to this process. 
 * If the queue does not contain a process that fulfills the condition
 * (e.g. if the queue is empty), null is returned.
 * 
 * @param c
 *      Condition : The condition that the process returned must fulfill
 * 
 * @return P : The first process in this queue fulfilling the condition, which 
*      has been removed from the queue. <code>Null</code> in case no process
*      fulfills the condition.
 */
public P removeFirst(Condition<P> c) {
    
    if (c == null) {
        sendWarning(
                "Can not remove the first process fulfilling a condition!",
                "Queue : " + getName() + " Method: void removeFirst(Condition c)",
                "The Condition 'c' given as parameter is a null reference!",
                "Check to always have valid references when querying Queues.");
        return null; // no proper parameter
    }
    
    P first = this.first(c);
    
    if (first != null) {

        _ql.remove(first); 

        // produce trace output
        if (currentlySendTraceNotes()) {
            if (first == currentEntity() && currentEntityAll().size() == 1) {
                sendTraceNote("removes itself from " + this.getQuotedName());
            } else {
                sendTraceNote("removes " + first.getQuotedName() + " from "
                        + this.getQuotedName());
            }
        }
    
    }
    
    return first;
}    
	
    /**
        * Removes the last process from the queue and provides a reference to
        * this process. If the queue is empty, <code>null</code> is returned.
        * 
        * @return E : The last process in this queue, which has been removed,
        *      or <code>null</code> in case the queue was empty
        */
       public P removeLast() {
           
           P last = this.last();
           
           if (last != null) {
    
               _ql.remove(last); 
       
               // produce trace output
               if (currentlySendTraceNotes()) {
                   if (last == currentEntity() && currentEntityAll().size() == 1) {
                       sendTraceNote("removes itself from " + this.getQuotedName());
                   } else {
                       sendTraceNote("removes " + last.getQuotedName() + " from "
                               + this.getQuotedName());
                   }
               }
           
           }
           
           return last;
       }		

	/**
        * Removes the last process from the queue that fulfills to the given
        * condition, determined by traversing the queue from last to first until 
        * a process fulfilling the condition is found. Also provides a reference 
        * to this process. If the queue does not contain a process that fulfills 
        * the condition (e.g. if the queue is empty), <code>null</code> is returned.
        * 
        * @param c
        *      Condition : The condition that the entity returned must fulfill
        * 
        * @return P : The last process in this queue fulfilling the condition, which 
        *      has been removed from the queue. <code>Null</code> in case no process
        *      fulfills the condition.
        */
       public P removeLast(Condition<P> c) {
           
           if (c == null) {
               sendWarning(
                       "Can not remove the last entity fulfilling a condition!",
                       "Queue : " + getName() + " Method: void removeLast(Condition c)",
                       "The Condition 'c' given as parameter is a null reference!",
                       "Check to always have valid references when querying Queues.");
               return null; // no proper parameter
           }
           
           P last = this.last(c);
           
           if (last != null) {
    
               _ql.remove(last); 
       
               // produce trace output
               if (currentlySendTraceNotes()) {
                   if (last == currentEntity() && currentEntityAll().size() == 1) {
                       sendTraceNote("removes itself from " + this.getQuotedName());
                   } else {
                       sendTraceNote("removes " + last.getQuotedName() + " from "
                               + this.getQuotedName());
                   }
               }
           
           }
           
           return last;
        }

	/**
	 * Resets all statistical counters to their default values. The mininum and
	 * maximum length of the queue are set to the current number of queued
	 * objects. The counter for the entities refused to be enqueued will be
	 * reset.
	 */
	public void reset() {

		super.reset(); // reset of QueueBased

		_refused = 0;

	}

	/**
	 * Sets the queue capacity to a new value. Only if the new capacity is equal
	 * or larger than the current length of the queue!
	 * 
	 * @param newCapacity
	 *            int : The new capacity of this ProcessQueue.
	 */
	public void setQueueCapacity(int newCapacity) {

		// check if the new capacity is negative or larger than the current
		// length
		// of the queue
		if (newCapacity < length() || newCapacity < 0) {
			sendWarning(
					"The new capacity is negative or would be smaller than the "
							+ "number of entities already enqueued in this ProcessQueue. The capacity "
							+ "will remain unchanged!",
					getClass().getName() + ": " + getQuotedName()
							+ ", Method: "
							+ "void setQueueCapacity(int newCapacity)",
					"The ProcessQueue already contains more entities than the new capacity "
							+ "could hold. What should happen to the remaining entities?",
					"Make sure to change the capacity only to a non negative value larger "
							+ "than the current length of this ProcessQueue.");

			return; // ignore that rubbish and just return
		}

		// set the capacity of the queue to the new value
		queueLimit = newCapacity;

	}

	/**
	 * Sets the sort order of this ProcessQueue to a new value and makes this
	 * ProcessQueue use another <code>QueueList</code> with the specified
	 * queueing discipline. Please choose a constant from
	 * <code>QueueBased</code> (<code>QueueBased.FIFO</code>, 
	 * <code>QueueBased.FIFO</code> or <code>QueueBased.Random</code>)
	 * The sort order of a ProcessQueue can only be changed if the queue is empty.
	 * 
	 * @param sortOrder
	 *            int : determines the sort order of the underlying
	 *            <code>QueueList</code> implementation (<code>QueueBased.FIFO</code>, 
     * <code>QueueBased.FIFO</code> or <code>QueueBased.Random</code>)
	 */
	public void setQueueStrategy(int sortOrder) {

		// check if the queue is empty
		if (!isEmpty()) {
			sendWarning(
					"The ProcessQueue for which the queueing discipline should be "
							+ "changed is not empty. The queueing discipline will remain unchanged!",
					getClass().getName() + ": " + getQuotedName()
							+ ", Method: "
							+ "void setQueueStrategy(int sortOrder)",
					"The ProcessQueue already contains some processes ordered according a "
							+ "certain order.",
					"Make sure to change the sort order only for an empty ProcessQueue.");

			return; // ignore that rubbish and just return
		}

        // determine the queueing strategy
        switch (sortOrder) {
        case QueueBased.FIFO :
            _ql = new QueueListFifo<P>(); break;
        case QueueBased.LIFO :
            _ql = new QueueListLifo<P>(); break;
        case QueueBased.RANDOM :
            _ql = new QueueListRandom<P>(); break;
        default :
            sendWarning(
                    "The given sortOrder parameter is negative or too big! "
                            + "The sort order of the ProcessQueue will remain unchanged!",
                    getClass().getName() + ": " + getQuotedName()
                            + ", Method: "
                            + "void setQueueStrategy(int sortOrder)",
                    "A valid positive integer number must be provided to "
                            + "determine the sort order of the queue.",
                    "Make sure to provide a valid positive integer number "
                            + "by using the constants in the class QueueBased, like "
                            + "QueueBased.FIFO, QueueBased.LIFO or QueueBased.RANDOM.");
            return;
        }
        _ql.setQueueBased(this);

	}

	/**
	 * Sets the number of entities refused to be enqueued in the queue because
	 * the capacity limit is reached to a new value.
	 * 
	 * @param n
	 *            long : the new number of entities refused to be enqueued in
	 *            the queue because the capacity limit is reached.
	 */
	public void setRefused(long n) {
		// check if n is negative
		if (n < 0) {
			sendWarning(
					"Attempt to set the number of entities refused to enqueue in "
							+ "the ProcessQueue to a negative value. The attempted action "
							+ "is ignored!", "ProcessQueue : " + getName()
							+ " Method: void setRefused(long n)",
					"The number given as parameter n is negative! That makes no "
							+ "sense!",
					"Make sure to provide only positive numbers as parameter n.");
			return;
		}

		this._refused = n; // save the new value

	}

	/**
     * Returns the current length of the Queue.
     * 
     * @return int : The number of processes enqueued.
     */
    public int size()
    {
        return _ql.size();
    }
	
    /**
	 * Returns the SimProcess enqueued directly after the given SimProcess in
	 * the process-queue. If the given SimProcess is not contained in this
	 * process-queue or is at the last position thus having no possible
	 * successor, <code>null</code> is returned.
	 * 
	 * @return P : The SimProcess directly after the given
	 *         SimProcess in the ProcessQueue or <code>null</code>
	 * @param p
	 *            P : A SimProcess in the process-queue
	 */
	public P succ(P p) {

		if (p == null) {
			sendWarning(
					"Can not find successor of SimProcess in Queue!",
					"ProcessQueue : " + getName()
							+ " Method: SimProcess succ(P p)",
					"The SimProcess 'p' given as parameter is a null reference!",
					"Check to always have valid references when querying for "
							+ "Entities");
			return null; // no proper parameter
		}

		return _ql.succ(p);

	}

    /**
	 * Returns the SimProcess enqueued after the given SimProcess in the
	 * process-queue that also fulfills the condition given. If the given
	 * SimProcess is not contained in this process-queue or is at the last
	 * position thus having no possible successor, <code>null</code> is
	 * returned. If no other SimProcess after the given one fulfills the
	 * condition, <code>null</code> is returned, too.
	 * 
	 * @return P : The SimProcess after the given SimProcess in
	 *         the process-queue fulfilling the condition or <code>null</code>.
	 * @param p
	 *            P : A SimProcess in the process-queue
	 * @param c
	 *            Condition : The condition that the succeeding SimProcess has
	 *            to fulfill
	 */
	public P succ(P p, Condition<P> c) {

		if (p == null) {
			sendWarning(
					"Can not find predecessor of SimProcess in Queue!",
					"ProcessQueue : "
							+ getName()
							+ " Method: SimProcess succ(P p, Condition c)",
					"The SimProcess 'p' given as parameter is a null reference!",
					"Check to always have valid references when querying for Entities");
			return null; // no proper parameter
		}

		if (c == null) {
			sendWarning(
					"Can not return previous SimProcess fulfilling condition!",
					"ProcessQueue : "
							+ getName()
							+ " Method: SimProcess succ(P p, Condition c)",
					"The Condition 'c' given as parameter is a null reference!",
					"Check to always have valid references when querying Queues.");
			return null; // no proper parameter
		}

		for (P tmp = succ(p); tmp != null; tmp = succ(tmp)) {
			if (c.check(tmp))
				return tmp;
		}

		return null; // obviously not found here, empty or doesn't fulfill

	}
}