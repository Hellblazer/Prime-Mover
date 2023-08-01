package desmoj.core.simulator;

import java.util.Random;

/**
 * Contains the implementation with the java.util.LinkedList to represent queueing
 * functionality. The entities are queued in random order. The statistic data of the queue will be
 * stored in a <code>QueueBased</code> object. The <code>QueueListStandardFifo</code>
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
 * @author Johannes G&ouml;bel
 * @author based on ideas from Soenke Claassen, Tim Lechler, Justin Neumann
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
public class QueueListRandom<E extends Entity> extends QueueListStandard<E> implements
		java.beans.PropertyChangeListener
	
	{
    
    /**
     * Uses the java.util.WeakHashMap functionalities to link entities with their entry time.
     */
    private Random _positionGenerator;

	/**
	 * Constructs an empty <code>QueueListStandardFifo</code> with no reference to its
	 * client QueueBased. This no-arg constructor is necessary to instantiate an
	 * object of this class by calling the
	 * <code>java.lang.Class.newInstance()</code> method. The reference to the
	 * QueueBased object making use of this queue-functionality must be provided
	 * later by calling the setQueueBased() method. The initial length is always
	 * zero.
	 */
	public QueueListRandom()
	{	
		super();

		// set the abbreviation for this kind of queueing discipline
		this.abbreviation = "RANDOM";
		
		// set position generator
		this._positionGenerator = new Random();

		// we don't know the client queue yet.
		// Must be provided later by calling the setQueueBased() method
		clientQ = null;

	}

	/**
	 * Adds a new Entity to the QueueListStandardFifo. The position on which 
	 * the entity is inserted is randomly generated, disregarding priorities.
	 * 
	 * @param e
	 *            Entity : The Entity to add to the QueueListStandardFifo
	 */
	public void insert(E e) 
	{
		
 		if (e == null) { // check for null reference
			sendWarning(
					"Can not insert entity. Command ignored.",
					"Class: QueueListRandom Method: insert(Entity e).",
					"The Entity reference given as parameter is a null reference.",
					"Be sure to only use valid references.");
			return;
		}

		if (contains(e)) { // entity must not be contained twice in queue
			sendWarning("Can not insert entity. Command ignored.",
					"Class: QueueListRandom Method: insert(Entity e).",
					"The Entity given as parameter is already enqueued.",
					"Make sure the entity is not enqueued here by calling "
							+ "method 'contains(Entity e)'.");
			return;
		}
		
		int position = this._positionGenerator.nextInt(this.size() + 1);
	    // index between 0 (first) and size() (one position after the last Entity at index size()-1)
		
        queuelist.add(position, e);
        e.addQueueBased(this.clientQ); // sets entity's queue as this queued
		
		statisticalInsert(e); // update statistics
	
	}
	
    /**
     * Sets the seed of this queue list's pseudo random number generator. 
     * 
     * @param newSeed
     *            long : new seed this queue list's pseudo random number generator
     */
    public void setSeed(long newSeed) {
        this._positionGenerator.setSeed(newSeed);
    }
}