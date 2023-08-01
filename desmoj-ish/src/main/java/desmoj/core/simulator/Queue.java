package desmoj.core.simulator;

import java.util.Iterator;

/**
 * Queue provides models with a ready-to-use modelling element to enqueue
 * entities in. The sort order of the queue is determined first
 * by the priorities of the enqueued entities and second by the given sort
 * order. The default sort order is FIFO (first in, first out) but others like
 * LIFO (last in, first out) can be chosen, too. See the constants in class
 * <code>QueueBased</code> and the derived classes from <code>QueueList</code>.
 * The capacity of the Queue, that is the maximum number of entities enqueued,
 * can be chosen, too.
 * <p>
 * For queueing <code>SimProcess</code>es the usage of class
 * <code>ProcessQueue</code> is recommended.  
 * 
 * 
 * @see QueueBased
 * @see QueueList
 * @see QueueListFifo
 * @see QueueListLifo
 * @see ProcessQueue
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
public class Queue<E extends Entity> extends QueueBased implements Iterable<E>{

	/**
	 * Private queue iterator, e.g. required for processing all queue elements in a 
	 * for-each-loop.
	 */
    private class QueueIterator implements Iterator<E> {
        
        Queue<E> clientQ; 
        E next, lastReturned;
        
        public QueueIterator(Queue<E> clientQ) {
            this.clientQ = clientQ;
            next = clientQ.first();
            lastReturned = null;
        }
        public boolean hasNext() {
            return next != null;
        }
        public E next() {
            lastReturned = next;
            next = clientQ.succ(next);
            return lastReturned;
        }
        public void remove() {
            clientQ.remove(lastReturned);
        }
    }

	/**
	 * The queue implementation (data structure) that actually stores the
	 * entities
	 */
	private QueueList<E> _ql;
	
	/**
	 * Counter for the entities which are refused to be enqueued, because the
	 * queue capacity is full.
	 */
	private long _refused;

	/**
	 * Constructs a simple priority and FIFO based waiting-queue for 
     * entities with a maximum capacity of 2,147,483,647 waiting 
     * entities, which should serve as an approximation of infinite 
     * queues sufficiently well for most purposes.Note that since SimProcesses 
     * are derived from Entities, they can be queued inside this queue, too.
     * <p>
     * The usage of the generic version <code>Queue&lt;Type&gt;</code> where 
     * <code>Type</code> is derived from <code>Entity</code> is recommended
     * for type safety. Using the raw type <code>Queue</code> yields a queue
     * in which any <code>Entity</code> can be enqueued, typically requiring
     * type casting on accessing entities enqueued.  
     * 
	 * @param owner
	 *            Model : The model this queue is associated to
	 * @param name
	 *            java.lang.String : The queue's name
	 * @param showInReport
	 *            boolean : Flag if queue should produce a report
	 * @param showInTrace
	 *            boolean : Flag for queue to produce trace messages
	 */
	public Queue(Model owner, String name, boolean showInReport,
			boolean showInTrace) {

		super(owner, name, showInReport, showInTrace); // create the QBased
		// object
		reset();

		// make the queue with Fifo queueing discipline and unlimited capacity
		_ql = new QueueListFifo<E>();
		_ql.setQueueBased(this);

	}

	/**
	 * Constructs a simple priority based waiting-queue for entities, the kind
	 * of queue sort order and the capacity of the queue can be chosen. 
	 * Note that since SimProcesses are derived from Entities, they can be 
	 * queued inside this queue, too.
	 * <p>
	 * Queues can be sorted as follows:<ul>
     * <li><code>QueueBased.FIFO</code> sorts entities by their priority,
     * highest priority first. Entities with the same priority are 
     * enqueued based on &quot;first in, first out&quot;.</li>
     * <li><code>QueueBased.LIFO</code> also sorts entities by their priority,
     * highest priority first. However, entities with the same priority are 
     * enqueued based on &quot;last in, first out&quot;.</li>
     * <li><code>QueueBased.Random</code> assigns a random position to each
     * entity entering the queue, disregarding priority.</li>
     * </ul>
	 * <p>
	 * The usage of the generic version <code>Queue&lt;Type&gt;</code> where 
	 * <code>Type</code> is derived from <code>Entity</code> is recommended
	 * for type safety. Using the raw type <code>Queue</code> yields a queue
	 * in which any <code>Entity</code> can be enqueued, typically requiring
	 * type casting on accessing entities enqueued.  
	 * 
	 * @param owner
	 *            Model : The model this queue is associated to
	 * @param name
	 *            java.lang.String : The queue's name
	 * @param sortOrder
	 *            int : determines the sort order of the underlying queue
	 *            implementation. Choose a constant from <code>QueueBased</code>:
	 *            <code>QueueBased.FIFO</code>, <code>QueueBased.LIFO</code> or
	 *            QueueBased.Random.
	 * @param qCapacity
	 *            int : The capacity of the Queue, that is how many entities can
	 *            be enqueued. Zero (0) can be used as shortcut for
	 *            for a capacity of <code>Integer.MAX_VALUE</code> = 2,147,483,647,
	 *            which should approximate an infinite queue sufficiently well
	 *            for most purposes.
	 * @param showInReport
	 *            boolean : Flag if queue should produce a report
	 * @param showInTrace
	 *            boolean : Flag for queue to produce trace messages
	 */
	public Queue(Model owner, String name, int sortOrder, int qCapacity,
			boolean showInReport, boolean showInTrace) {

		super(owner, name, showInReport, showInTrace); // create the QBased
		// object
		reset();
	    
		// determine the queueing strategy
	    switch (sortOrder) {
	    case QueueBased.FIFO :
	        _ql = new QueueListFifo<E>(); break;
        case QueueBased.LIFO :
            _ql = new QueueListLifo<E>(); break;
        case QueueBased.RANDOM :
            _ql = new QueueListRandom<E>(); break;
        default :
            sendWarning(
                    "The given sortOrder parameter " + sortOrder + " is not valid! "
                            + "A queue with Fifo sort order will be created.",
                    "Queue : "
                            + getName()
                            + " Constructor: Queue(Model owner, String name, "
                            + "int sortOrder, long qCapacity, boolean showInReport, "
                            + "boolean showInTrace)",
                    "A valid positive integer number must be provided to "
                            + "determine the sort order of the queue.",
                    "Make sure to provide a valid positive integer number "
                            + "by using the constants in the class QueueBased, like "
                            + "QueueBased.FIFO, QueueBased.LIFO or QueueBased.RANDOM.");
            _ql = new QueueListFifo<E>(); 
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
					"Queue : "
							+ getName()
							+ " Constructor: Queue(Model owner, String name, "
							+ "int sortOrder, long qCapacity, boolean showInReport, "
							+ "boolean showInTrace)",
					"A negative capacity for a queue does not make sense.",
					"Make sure to provide a valid positive capacity "
							+ "for the queue.");
			// set the capacity to the maximum value
			queueLimit = Integer.MAX_VALUE;
		}

		// check if qCapacity is zero (that means maximum capacity)
		if (qCapacity == 0) {
			// set the capacity to the maximum value
			queueLimit = Integer.MAX_VALUE;
		}

	}

	/**
     * Returns <code>true</code> if the given <code>Entity</code> is
     * contained in the queue; <code>false</code> otherwise.
     * 
     * @return boolean : <code>True</code> if the given
     *         <code>Entity</code> is contained in the queue;
     *         <code>false</code> otherwise.
     * @param e
     *            E : The <code>Entity</code> we are looking for
     *            in the queue.
     */
    public boolean contains(E e) {
        
        return _ql.contains(e); // delegated to the internal queue list
    }

	/**
	 * Returns a special queue reporter to produce a report about this queue.
	 * 
	 * @return desmoj.core.report.Reporter : The reporter for this queue
	 */
	public desmoj.core.report.Reporter createDefaultReporter() {

		return new desmoj.core.report.QueueReporter(this);

	}
	
	/**
	 * Returns the first entity queued in this queue or <code>null</code> in
	 * case the queue is empty.
	 * 
	 * @return E : The first entity in the queue or
	 *         <code>null</code> if the queue is empty
	 */
	public E first() {

		return _ql.first(); // straight design

	}
    
    /**
	 * Returns the first entity queued in this queue that fulfills the given
	 * condition. The queue is searched from front to end and the first entity
	 * that returns <code>true</code> when the condition is applied to it is
	 * returned by this method. If no Entity fulfills the given condition or the
	 * queue is empty, <code>null</code> will be returned.
	 * 
	 * @return E : The first entity queued in this queue applying to
	 *         the given condition or <code>null</code>
	 * @param c
	 *            Condition : The condition that the entity returned must fulfill
	 */
	public E first(Condition<E> c) {

		if (c == null) {
			sendWarning(
					"Can not return first entity fulfilling the condition!",
					"Queue : " + getName() + " Method: void first(Condition c)",
					"The Condition 'c' given as parameter is a null reference!",
					"Check to always have valid references when querying Queues.");
			return null; // no proper parameter
		}
		if (_ql.isEmpty())
			return null; // nobody home to be checked
		for (E tmp = _ql.first(); tmp != null; tmp = _ql.succ(tmp)) {
			if (c.check(tmp))
				return tmp;
		}
		// if no Entity fulfills to the condition just return null
		return null;

	}
	
    /**
     * Returns the queue index of a given <code>Entity</code>.
     * 
     * @return int :The position of the entity as an <code>int</code>. 
     *              Returns -1 if no such position exists.
     */
    public int get(E e) {
        
        return _ql.get(e);
        
    }

    /**
     * Returns the <code>Entity</code> queued at the named position.
     * The first position is 0, the last one size()-1.
     * 
     * @return E : The <code>Entity</code> at the position of
     *         <code>int</code> or <code>null</code> if no such position exists.
     */
    public E get(int index) {
        
        return _ql.get(index);
        
    }
	

	/**
	 * Returns the waiting time of an entity present in the queue.
	 * If the entity given is not queued, <code>null</code> will be returned.
	 * 
	 * @param e
	 *            Entity : The entity whose waiting time is looked for
	 *            
	 * @return TimeSpan : The waiting time of the entity or <code>null</code>.
	 */
	public TimeSpan getCurrentWaitTime(Entity e) {

		if (e == null) {
			sendWarning("Cannot query waiting time!", "Queue : "
					+ getName() + " Method:  void getCurrentWaitTime(Entity e)",
					"The Entity 'e' given as parameter is a null reference!",
					"Check to always have valid references when querying"
							+ "waiting durations");
			return null; // no proper parameter
		}
		TimeInstant i = _ql.timemap.get(e);
		
		if (i == null) {
			sendWarning("Cannot query waiting time!", "Queue : "
					+ getName() + " Method:  void getCurrentWaitTime(Entity e)",
					"The Entity 'e' ("+e.getQuotedName()+") given as parameter is not enqueued in this "
							+ "queue!",
					"Make sure the entity is inside the queue you want it to "
							+ "be queried.");
			return null; // not enqueued here
		}

		if (currentlySendDebugNotes()) {
			sendDebugNote("remove " + e.getQuotedName() + "<br>"
					+ _ql.toString());
		}

		return TimeOperations.diff(this.presentTime(), i);
	}

	/**
     * Returns the underlying queue implementation, providing access to the
     * QueueList implementation, e.g. to add PropertyChangeListeners.
     * 
     * @return QueueList : The underlying queue implementation of this Queue.
     */
    public QueueList<E> getQueueList() {

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
	 * Returns the number of entities refused to be enqueued in the queue.
	 * 
	 * @return long : The number of entities refused to be enqueued in the
	 *         queue.
	 */
	public long getRefused() {

		return _refused; // that's it
	}

	/**
	 * Enters a new Entity into the queue. If the capacity of the queue is full,
	 * the entity will not be enqueued and <code>false</code> will be
	 * returned. The Entity will be stored in the queue until method
	 * <code>remove(Entity e)</code> is called with this specific Entity.
	 * Entities inside the queue are ordered according to their priority. Higher
	 * priorities are sorted in front of lower priorities. Entities with same
	 * priority are orderer according to the specified strategy. The first
	 * Entity inside the queue will always be the one with the highest priority.
	 * 
	 * @return boolean : Is <code>true</code> if insertion was successful,
	 *         <code>false</code> otherwise (i.e. capacity limit is reached).
	 * @param e
	 *            E : The Entity to be added to the queue.
	 */
	public boolean insert(E e) {

		if (e == null) { // null returns with warning
			sendWarning("Can not insert entity into Queue! Command ignored.",
					"Queue : " + getName()
							+ " Method: boolean insert(E e)",
					"The Entity 'e' given as parameter is a null reference!",
					"Check to always have valid references when enqueueing Entities");
			return false; // no proper parameter
		}

		if (!isModelCompatible(e)) {
			sendWarning("Can not insert entity into Queue! Command ignored.",
					"Queue : " + getName() + " Method: boolean insert"
							+ "(E e)",
					"The Entity 'e' ("+e.getQuotedName()+") given as parameter is not compatible to "
							+ "the model this queue belongs to!",
					"Check if your submodels are allowed to mingle with other "
							+ "model's components.");
			return false; // not of my model type!!!
		}

		if (queueLimit <= length()) {

			if (currentlySendDebugNotes()) {
				sendDebugNote("refuses to insert " + e.getQuotedName()
					+ " because the " + "capacity limit is reached. Queue:<br>"
					+ _ql.toString());
			}

			if (currentlySendTraceNotes()) {
				sendTraceNote("is refused to be enqueued in "
					+ this.getQuotedName() + "because the capacity limit ("
					+ getQueueLimit() + ") of this queue is " + "reached");
			}

			_refused++; // count the refused ones

			return false; // capacity limit is reached
		}

		_ql.insert(e); // that's it

		if (currentlySendDebugNotes()) {
			sendDebugNote("inserts " + e.getQuotedName() + " in the queue:<br>"
					+ _ql.toString());
		}

		// produce trace output
		if (currentlySendTraceNotes()) {
			if (e == currentEntity() && currentEntityAll().size() == 1) {
				sendTraceNote("inserts itself into " + this.getQuotedName());
			} else {
				sendTraceNote("inserts " + e.getName() + " into "
						+ this.getQuotedName());
			}
		}

		return true;
	}

	/**
	 * Enters a new Entity into the queue and places it after the given Entity.
	 * If the capacity of the queue is full, the entity will not be enqueued and
	 * <code>false</code> will be returned. Make sure that the entity given as
	 * reference is already queued inside the queue, else the entity will not be
	 * enqueued and <code>false</code> will be returned. The Entity will be
	 * stored in the queue until method <code>remove(Entity e)</code> is
	 * called with this specific Entity.
	 * 
	 * @return boolean : Is <code>true</code> if insertion was successful,
	 *         <code>false</code> otherwise (i.e. capacity limit is reached).
	 * @param e
	 *            E : The Entity to be added to the queue
	 * @param after
	 *            E : The Entity after which the entity e is to be inserted
	 */
	public boolean insertAfter(E e, E after) {

		if (e == null) {
			sendWarning(
					"Can not insert entity into Queue! Command ignored.",
					"Queue : "
							+ getName()
							+ " Method: boolean insertAfter(E e, E after)",
					"The Entity 'e' given as parameter is a null reference!",
					"Check to always have valid references when enqueueing Entities");
			return false; // no proper parameter
		}

		if (after == null) {
			sendWarning(
					"Can not insert entity into Queue! Command ignored.",
					"Queue : "
							+ getName()
							+ " Method: boolean insertAfter(E e, E after)",
					"The Entity 'after' given as parameter is a null reference!",
					"Check to always have valid references when enqueueing Entities");
			return false; // no proper parameter
		}

		if (!isModelCompatible(e)) {
			sendWarning("Can not insert entity into Queue! Command ignored.",
					"Queue : " + getName() + " Method: boolean insertAfter"
							+ "(E e, E after)",
					"The Entity 'e' ("+e.getQuotedName()+") given as parameter is not compatible to "
							+ "the model this queue belongs to!",
					"Check if your submodels are allowed to mingle with other "
							+ "model's components.");
			return false; // not of my model type!!!
		}

		if (queueLimit <= length()) {

			if (currentlySendDebugNotes()) {
				sendDebugNote("refuses to insert " + e.getQuotedName()
					+ " because the " + "capacity limit is reached. Queue:<br>"
					+ _ql.toString());
			}

			if (currentlySendTraceNotes()) {
				sendTraceNote("is refused to be enqueued in "
					+ this.getQuotedName() + "because the capacity limit ("
					+ getQueueLimit() + ") of this queue is " + "reached");
			}

			_refused++; // count the refused ones

			return false; // capacity limit is reached
		}

		boolean successful = _ql.insertAfter(e, after); // that's the point

		if (currentlySendDebugNotes()) {
			sendDebugNote("inserts " + e.getQuotedName() + " after "
					+ after.getQuotedName() + "<br>" + _ql.toString());
		}

		// produce trace output
		if (currentlySendTraceNotes()) {
			if (e == currentEntity() && currentEntityAll().size() == 1) {
				sendTraceNote("inserts itself into " + this.getQuotedName()
						+ " after " + after.getName());
			} else {
				sendTraceNote("inserts " + e.getName() + " into "
						+ this.getQuotedName() + " after " + after.getName());
			}
		}

		return successful;

	}

	/**
	 * Enters a new Entity into the queue and places it in front of the given
	 * Entity. If the capacity of the queue is full, the entity will not be
	 * enqueued and <code>false</code> will be returned. Make sure that the
	 * Entity given as reference is already queued inside the queue, else the
	 * Entity will not be queued and <code>false</code> will be returned. The
	 * Entity will be stored in the queue until method
	 * <code>remove(Entity e)</code> is called with this specific Entity.
	 * 
	 * @return boolean : Is <code>true</code> if insertion was successful,
	 *         <code>false</code> otherwise (i.e. capacity limit is reached).
	 * @param e
	 *            E : The Entity to be added to the queue
	 * @param before
	 *            E : The Entity before which the entity e is to be
	 *            inserted
	 */
	public boolean insertBefore(E e, E before) {

		if (e == null) {
			sendWarning("Can not insert entity into Queue! Command ignored.",
					"Queue : " + getName()
							+ " Method: boolean insertBefore(E e, "
							+ "Entity before)",
					"The Entity 'e' given as parameter is a null reference!",
					"Check to always have valid references when enqueueing Entities");
			return false; // no proper parameter
		}
		if (before == null) {
			sendWarning(
					"Can not insert entity into Queue! Command ignored.",
					"Queue : " + getName()
							+ " Method: boolean insertBefore(E e, "
							+ "Entity before)",
					"The Entity 'before' given as parameter is a null reference!",
					"Check to always have valid references when enqueueing Entities");
			return false; // no proper parameter
		}

		if (!isModelCompatible(e)) {
			sendWarning("Can not insert entity into Queue! Command ignored.",
					"Queue : " + getName() + " Method: boolean insertBefore"
							+ "(E e, Entity before)",
					"The Entity 'e' ("+e.getQuotedName()+") given as parameter is not compatible to "
							+ "the model this queue belongs to!",
					"Check if your submodels are allowed to mingle with other "
							+ "model's components.");
			return false; // not of my model type!!!
		}

		if (queueLimit <= length()) {

			if (currentlySendDebugNotes()) { 
				sendDebugNote("refuses to insert " + e.getQuotedName()
					+ " because the " + "capacity limit is reached. Queue:<br>"
					+ _ql.toString());
			}
			
			if (currentlySendTraceNotes()) {
				sendTraceNote("is refused to be enqueued in "
					+ this.getQuotedName() + "because the capacity limit ("
					+ getQueueLimit() + ") of this queue is " + "reached");
			}

			_refused++; // count the refused ones

			return false; // capacity limit is reached
		}

		boolean successful = _ql.insertBefore(e, before); // that's the point

		if (currentlySendDebugNotes()) {
			sendDebugNote("inserts " + e.getQuotedName() + " before "
					+ before.getQuotedName() + "<br>" + _ql.toString());
		}

		// produce trace output
		if (currentlySendTraceNotes()) {
			if (e == currentEntity() && currentEntityAll().size() == 1) {
				sendTraceNote("inserts itself into " + this.getQuotedName()
						+ " before " + before.getName());
			} else {
				sendTraceNote("inserts " + e.getName() + " into "
						+ this.getQuotedName() + " before " + before.getName());
			}
		}
		
		return successful;

	}

	/**
	 * Returns a boolean value indicating if the queue is empty or if any number
	 * of entities is currently enqueued in it.
	 * 
	 * @return boolean : Is <code>true</code> if the Queue is empty,
	 *         <code>false</code> otherwise
	 */
	public boolean isEmpty() {

		return _ql.isEmpty();

	}

	/**
     * Returns an iterator over the entities enqueued.
     *
     * @return java.lang.Iterator&lt;E&gt; : An iterator over the entities enqueued.
     */
	public Iterator<E> iterator() {
        return new QueueIterator(this);
    }

	/**
	 * Returns the last Entity queued in this queue or <code>null</code> in
	 * case the queue is empty.
	 * 
	 * @return E : The last Entity in the queue or <code>null</code>
	 *         if the queue is empty
	 */
	public E last() {

		return _ql.last(); // straight design again

	}

	/**
	 * Returns the last Entity queued in this queue that fulfills the given
	 * condition. The queue is searched from end to front and the first entity
	 * that returns <code>true</code> when the condition is applied to it is
	 * returned. If no Entity fulfills the given condition or the queue is
	 * empty, <code>null</code> will be returned.
	 * 
	 * @return E : The last Entity queued in this queue applying to
	 *         the given condition or <code>null</code>
	 * @param c
	 *            Condition : The condition that the entity returned must fulfill
	 */
	public E last(Condition<E> c) {

		if (c == null) {
			sendWarning(
					"Can not insert entity!",
					"Queue : " + getName()
							+ " Method: Entity last(Condition c)",
					"The Condition -c- given as parameter is a null reference!",
					"Check to always have valid references when querying Queues.");
			return null; // no proper parameter
		}

		if (_ql.isEmpty())
			return null; // nobody home to be checked
		for (E tmp = _ql.last(); tmp != null; tmp = _ql.pred(tmp)) {
			if (c.check(tmp))
				return tmp;
		}

		// if no Entity fulfills to the condition just return null
		return null;

	}
	
	/**
	 * Returns the entity enqueued directly before the given Entity in the
	 * queue. If the given Entity is not contained in this queue or is at the
	 * first position thus having no possible predecessor, <code>null</code>
	 * is returned.
	 * 
	 * @return E : The Entity directly before the given Entity in
	 *         the queue or <code>null</code>.
	 * @param e
	 *         E : An Entity in the queue
	 */
	public E pred(E e) {

		if (e == null) {
			sendWarning("Can not find predecessor of Entity in Queue!",
					"Queue : " + getName() + " Method: Entity pred(E e)",
					"The Entity 'e' given as parameter is a null reference!",
					"Check to always have valid references when querying for Entities");
			return null; // no proper parameter
		}

		return _ql.pred(e);

	}
	
	
    /**
	 * Returns the entity enqueued before the given Entity in the queue that
	 * also fulfills the condition given. If the given Entity is not
	 * contained in this queue or is at the first position thus having no
	 * possible predecessor, <code>null</code> is returned. If no other Entity
	 * before the given one fulfills the condition, <code>null</code> is
	 * returned, too.
	 * 
	 * @return E : The Entity before the given Entity in the queue
	 *         fulfilling the condition or <code>null</code>.
	 * @param e
	 *            E : An Entity in the queue
	 * @param c
	 *            Condition : The condition that the preceeding Entity has to
	 *            fulfill
	 */
	public E pred(E e, Condition<E> c) {

		if (e == null) {
			sendWarning("Can not find predecessor of Entity in Queue!",
					"Queue : " + getName()
							+ " Method: Entity pred(E e, Condition c)",
					"The Entity 'e' given as parameter is a null reference!",
					"Check to always have valid references when querying "
							+ "for Entities");
			return null; // no proper parameter
		}

		if (c == null) {
			sendWarning(
					"Can not return previous Entity fulfilling the condition!",
					"Queue : " + getName()
							+ " Method: Entity pred(E e, Condition c)",
					"The Condition 'c' given as parameter is a null reference!",
					"Check to always have valid references when querying Queues.");
			return null; // no proper parameter
		}

		for (E tmp = pred(e); tmp != null; tmp = pred(tmp)) {
			if (c.check(tmp))
				return tmp;
		}

		return null; // obviously not found here, empty or doesn't fulfill
	}
	
	/**
	 * Removes the given Entity from the Queue. If the given Entity is not in
	 * the Queue, a warning will be issued but nothing else will be changed.
	 * 
	 * @param e
	 *            Entity : The Entity to be removed
	 */
	public void remove(Entity e) {

		if (e == null) {
			sendWarning("Can not remove Entity from Queue!", "Queue : "
					+ getName() + " Method:  void remove(Entity e)",
					"The Entity 'e' given as parameter is a null reference!",
					"Check to always have valid references when removing "
							+ "Entities");
			return; // no proper parameter
		}
		if (!_ql.remove((E)e)) { // watch out, already removed as a side
			// effect!!!
			sendWarning("Can not remove Entity from Queue!", "Queue : "
					+ getName() + " Method:  void remove(Entity e)",
					"The Entity 'e' ("+e.getQuotedName()+") given as parameter is not enqueued in this "
							+ "queue!",
					"Make sure the entity is inside the queue you want it to "
							+ "be removed.");
			return; // not enqueued here
		}

		if (currentlySendDebugNotes()) {
			sendDebugNote("remove " + e.getQuotedName() + "<br>"
					+ _ql.toString());
		}

		// produce trace output
		if (currentlySendTraceNotes()) {
			if (e == currentEntity() && currentEntityAll().size() == 1) {
				sendTraceNote("removes itself from " + this.getQuotedName());
			} else {
				sendTraceNote("removes " + e.getQuotedName() + " from "
						+ this.getQuotedName());
			}
		}

	}
    
    
    /**
     * Removes the entity queued at the given position.
     * The first position is 0, the last one length()-1.
     * 
     * @return : The method returns <code>true</code> if an <code>Entity</code>
     *           exists at the given position or <code>false></code> if otherwise.
     */
    public boolean remove(int index)  
    {
        if (index < 0 || index >= this.length()) return false;
        
        E e = get(index);
        if (e == null) {
            return false;
        } else {
            remove(e);
            return true;
        }
    }
    
    
    /**
     * Removes all entities from the Queue. Has no effect on empty queues.
     */
    public void removeAll() {
        
        while (!isEmpty()) {
            removeFirst();            
        }
    }

    /**
     * Removes the first entity from the queue and provides a reference to
     * this entity. If the queue is empty, <code>null</code> is returned.
     * 
     * @return E : The first entity in this queue, which has been removed,
     *      or <code>null</code> in case the queue was empty
     */
    public E removeFirst() {
        
        E first = this.first();
        
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
     * Removes the first entity from the queue that fulfills the given
     * condition. Also provides a reference to this entity. 
     * If the queue does not contain an entity that fulfills the condition
     * (e.g. if the queue is empty), <code>null</code> is returned.
     * 
     * @param c
     *      Condition : The condition that the entity returned must fulfill
     * 
     * @return E : The first entity in this queue fulfilling the condition, which 
     *      has been removed from the queue. <code>Null</code> in case no entity
     *      fulfills the condition.
     */
    public E removeFirst(Condition<E> c) {
        
        if (c == null) {
            sendWarning(
                    "Can not remove the first entity fulfilling a condition!",
                    "Queue : " + getName() + " Method: void removeFirst(Condition c)",
                    "The Condition 'c' given as parameter is a null reference!",
                    "Check to always have valid references when querying Queues.");
            return null; // no proper parameter
        }
        
        E first = this.first(c);
        
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
     * Removes the last entity from the queue and provides a reference to
     * this entity. If the queue is empty, <code>null</code> is returned.
     * 
     * @return E : The last entity in this queue, which has been removed,
     *      or <code>null</code> in case the queue was empty
     */
    public E removeLast() {
        
        E last = this.last();
        
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
     * Removes the last entity from the queue that fulfills to the given
     * condition, determined by traversing the queue from last to first until 
     * an entity fulfilling the condition is found. Also provides a reference 
     * to this entity. If the queue does not contain an entity that fulfills 
     * the condition (e.g. if the queue is empty), <code>null</code> is returned.
     * 
     * @param c
     *      Condition : The condition that the entity returned must fulfill
     * 
     * @return E : The last entity in this queue fulfilling the condition, which 
     *      has been removed from the queue. <code>Null</code> in case no entity
     *      fulfills the condition.
     */
    public E removeLast(Condition<E> c) {
        
        if (c == null) {
            sendWarning(
                    "Can not remove the last entity fulfilling a condition!",
                    "Queue : " + getName() + " Method: void removeLast(Condition c)",
                    "The Condition 'c' given as parameter is a null reference!",
                    "Check to always have valid references when querying Queues.");
            return null; // no proper parameter
        }
        
        E last = this.last(c);
        
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
     * Sets the sort order of this Queue to a new value and makes this
     * Queue use another <code>QueueList</code> with the specified
     * queueing discipline. Please choose a constant from
     * <code>QueueBased</code> (<code>QueueBased.FIFO</code>, 
     * <code>QueueBased.FIFO</code> or <code>QueueBased.Random</code>)
     * The sort order of a Queue can only be changed if the queue is empty.
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
                    "The Queue for which the queueing discipline should be "
                            + "changed is not empty. The queueing discipline will remain unchanged!",
                    getClass().getName() + ": " + getQuotedName()
                            + ", Method: "
                            + "void setQueueStrategy(int sortOrder)",
                    "The Queue already contains some entities ordered according a "
                            + "certain order.",
                    "Make sure to change the sort order only for an empty ProcessQueue.");

            return; // ignore that rubbish and just return
        }

        // determine the queueing strategy
        switch (sortOrder) {
        case QueueBased.FIFO :
            _ql = new QueueListFifo<E>(); break;
        case QueueBased.LIFO :
            _ql = new QueueListLifo<E>(); break;
        case QueueBased.RANDOM :
            _ql = new QueueListRandom<E>(); break;
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
     * Sets the seed of the underlying queue list's pseudo random number generator. 
     * Useful for queues with random sort order only; to other queues, calling
     * this method has no effect, resulting in a warning.  
     * 
     * @param newSeed
     *            long : new seed of the underlying queue list's pseudo 
     *            random number generator
     */
    public void setSeed(long newSeed) {
        if (_ql instanceof QueueListRandom<?>) {
            ((QueueListRandom<?>) _ql).setSeed(newSeed);
        } else {
            sendWarning(
                    "Cannot set seed of queue!",
                    "Queue : " + getName()
                            + " Method: setSeed(long newSeed)",
                    "The queue does not randomize entries.",
                    "Make sure to call setSeed(long newSeed) " +
                            "on queues with <tt>sortOrder == QueueBased.RANDOM</tt> only.");
        }
    }
	
	
    /**
     * Returns the current length of the Queue.
     * 
     * @return int : The number of entities enqueued.
     */
    public int size()
    {
        return _ql.size();
    }
    
	
    /**
	 * Returns the entity enqueued directly after the given Entity in the queue.
	 * If the given Entity is not contained in this queue or is at the last
	 * position thus having no possible successor, <code>null</code> is
	 * returned.
	 * 
	 * @return E : The Entity directly after the given Entity in the
	 *         queue or <code>null</code>.
	 * @param e
	 *         E : An Entity in the queue
	 */
	public E succ(E e) {

		if (e == null) {
			sendWarning("Can not find successor of Entity in Queue!",
					"Queue : " + getName() + " Method: Entity succ(E e)",
					"The Entity 'e' given as parameter is a null reference!",
					"Check to always have valid references when querying for Entities");
			return null; // no proper parameter
		}

		return _ql.succ(e);

	}

    /**
	 * Returns the entity enqueued after the given Entity in the queue that also
	 * fulfills the condition given. If the given Entity is not contained in
	 * this queue or is at the last position thus having no possible successor,
	 * <code>null</code> is returned. If no other Entity after the given one
	 * fulfills the condition, <code>null</code> is returned, too.
	 * 
	 * @return E : The Entity after the given Entity in the queue
	 *         fulfilling the condition or <code>null</code>.
	 * @param e
	 *            E : An Entity in the queue
	 * @param c
	 *            Condition : The condition that the succeeding Entity has to
	 *            fulfill
	 */
	public E succ(E e, Condition<E> c) {

		if (e == null) {
			sendWarning("Can not find predecessor of Entity in Queue!",
					"Queue : " + getName()
							+ " Method: Entity succ(E e, Condition c)",
					"The Entity 'e' given as parameter is a null reference!",
					"Check to always have valid references when querying for Entities");
			return null; // no proper parameter
		}

		if (c == null) {
			sendWarning(
					"Can not return previous Entity fulfilling the condition!",
					"Queue : " + getName()
							+ " Method: Entity succ(E e, Condition c)",
					"The Condition 'c' given as parameter is a null reference!",
					"Check to always have valid references when querying Queues.");
			return null; // no proper parameter
		}

		for (E tmp = succ(e); tmp != null; tmp = succ(tmp)) {
			if (c.check(tmp))
				return tmp;
		}

		return null; // obviously not found here, empty or doesn't fulfills

	}
}