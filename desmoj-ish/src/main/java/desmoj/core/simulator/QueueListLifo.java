package desmoj.core.simulator;

/**
 * Contains the implementation with the java.util.LinkedList to represent queueing
 * functionality. The entities are queued first according to their priority and
 * second in LIFO (last in first out) order. The statistic data of the queue will be
 * stored in a <code>QueueBased</code> object. The <code>QueueListLifo</code>
 * has a reference to its <code>QueueBased</code> object. This class needs a 
 * reference to a subclass of QueueBased to update the queue statistics.
 * It is used in many kinds of queue implementations i.e. in classes
 * <code>Queue</code> and <code>ProcessQueue</code>.
 *
 * @see QueueList
 * @see QueueBased
 * @see Queue
 * @see ProcessQueue
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Justin Neumann
 * @author based on ideas from Soenke Claassen, Tim Lechler, Johannes G&ouml;bel
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
public class QueueListLifo<E extends Entity> extends QueueListStandard<E> implements
		java.beans.PropertyChangeListener {
	

	/**
	 * Constructs an empty <code>QueueListStandardFifo</code> with no reference to its
	 * client QueueBased. This no-arg constructor is necessary to instantiate an
	 * object of this class by calling the
	 * <code>java.lang.Class.newInstance()</code> method. The reference to the
	 * QueueBased object making use of this queue-functionality must be provided
	 * later by calling the setQueueBased() method. The initial length is always
	 * zero.
	 */
	public QueueListLifo() {

		// set the abbreviation for this kind of queueing discipline
		this.abbreviation = "LIFO";
	}

	/**
	 * Adds a new Entity to the QueueListLifo. Entities are inserted according
	 * to their priority in descending order. The highest priority Entity will
	 * always be first in the queue. Entities with same priority are inserted in
	 * LiFo order.
	 * 
	 * @param e
	 *            Entity : The Entity to add to the QueueListStandardLifo
	 */
	@Override
	public void insert(E e) 
	{
		
		if (e == null) { // check for null reference
			sendWarning(
					"Can not insert entity. Command ignored.",
					"Class: QueueListStandardFifo Method: insert(Entity e).",
					"The Entity reference given as parameter is a null reference.",
					"Be sure to only use valid references.");
			return;
		}

		if (contains(e)) { // entity must not be contained twice in queue
			sendWarning("Can not insert entity. Command ignored.",
					"Class: QueueListStandardFifo Method: insert(Entity e).",
					"The Entity given as parameter is already enqueued.",
					"Make sure the entity is not enqueued here by calling "
							+ "method 'contains(Entity e)'.");
			return;
		}
		
		// if there are already entities in queue
		if (this.size() > 0)  
		{
			// continuously asks the succ (LIFO) entities if they have a higher priority;
			// finally inserts at correct position
			
			E swap = first(); // swap references the first element of the list
			while (Entity.isSmaller(e, swap)) // LIFO
			{
				swap = succ(swap); // swap reference to successor of itself
			}
			
			if (swap == null)
			{
				queuelist.addLast(e);
				statisticalInsert(e); // update statistics
				e.addQueueBased(this.clientQ); // sets entity's queue as this queued
			}
			else
			{
				this.insertBefore(e, swap); //inserts the entity at the correct position
				// with help of the java.util.LinkedList
			}
			
		}
		else
		{
			queuelist.add(e);
			e.addQueueBased(this.clientQ); // sets entity's queue as this queued
			
			statisticalInsert(e); // update statistics
		}
	}
}