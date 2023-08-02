package desmoj.core.advancedModellingFeatures;


import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
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
 * Stock is the place where a certain kind of product can be stored by producers
 * and retrieved by consumers. There is no difference between the units of one
 * kind of product, the units are indistinguishable. Producers are producing
 * products and store them using <code>store()</code> in the Stock. Consumers
 * make the Stock <code>retrieve()</code> the products to use them up. A Stock
 * is also providing a process synchronization between producers and consumers.
 * If no or not enough units of a product are available for the consumers, they
 * have to wait in a queue until new units are delivered by a producer. The
 * Stock has a certain capacity. If the Stock is filled to it's capacity
 * producers have to wait in a queue until consumers are arriving and taking
 * units out of the Stock. Then the producers can fill up the freed space. This
 * is the major difference to the Bin. 
 *  
 * The first sort criterion of the queues is highest queueing priorities first
 * (i.e. not using scheduling priorities - note that this is a somewhat arbitrary 
 * choice, as the <ode>Stock</code> combines queueing and scheduling features).
 * The second criterion, if a tie-breaker is needed, is the queueing discipline 
 * of the underlying queues, e.g. FIFO. The capacity limits can be determined by the user.
 * <code>Stock</code> is derived from <code>QueueBased</code>, which provides all the statistical functionality
 * for the consumer queue. The producer queue is managed by an internal ProcessQueue.
 * 
 * 
 * @see QueueBased
 * @see Bin
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
public class Stock extends QueueBased {

    /**
     * The queue, actually storing the consumer processes waiting for products
     */
    protected transient QueueList<SimProcess> _consumerQueue;

    /**
     * The queue, actually storing the producer processes waiting to store their
     * units in the stock
     */
    protected transient ProcessQueue<SimProcess> _producerQueue;

    protected transient java.beans.PropertyChangeSupport propertyChange;

    /**
     * Number of units available at the moment
     */
    private long _fieldAvail = 0;

    /**
     * The capacity of this stock. That is the number of units this stock can
     * hold at max. If set to zero the capacity is almost unlimited (=
     * 9223372036854775807).
     */
    private long _fieldCapacity;

    /**
     * The number of consumers having retrieved products from this Stock
     */
    private long _fieldConsumers = 0;

    /**
     * The initial number of units in the Stock
     */
    private long _fieldInitial = 0;

    /**
     * The maximum number of units in the Stock
     */
    private long _fieldMaximum = 0;

    /**
     * The minimum number of units in the Stock
     */
    private long _fieldMinimum = 0;

    /**
     * The number of producers having used this Stock to store products
     */
    private long _fieldProducers = 0;

    /**
     * Counter for the consumer SimProcesses which are refused to be enqueued,
     * because the queue capacity is full.
     */
    private long _fieldRefused = 0;

    /**
     * Indicates the method where something has gone wrong. Is passed as a
     * parameter to the method <code>checkProcess()</code>.
     */
    private transient java.lang.String _fieldWhere;

    /**
     * The time in the simultion this Stock was used for last.
     */
    private TimeInstant _lastUsage;

    /**
     * Flag to indicate whether an entity can pass by other entities which are
     * enqueued before that entity in the consumer queue. Is <code>false</code>
     * as default value.
     */
    private boolean _passByCons = false;

    /**
     * Flag to indicate whether an entity can pass by other entities which are
     * enqueued before that entity in the producer queue. Is <code>false</code>
     * as default value.
     */
    private boolean _passByProds = false;

    /**
     * Weighted sum of available units in the Stock over the time (must be
     * divided by the total time to get the average available units!)
     */
    private transient double _wSumAvail = 0.0;

    /**
     * Constructor for a <code>Stock</code> with a certain capacity and a 
     * certain number of initial units of a product in it.
     * Waiting producer and consumer processes are sorted (and thus activated) 
     * according to their queueing priorities (highest first) and (if equal) 
     * by a queuing discipline as defined by <code>QueueBased</code>, e.g. 
     * <code>QueueBased.FIFO</code> or <code>QueueBased.LIFO</code>. 
     * The capacities of the underlying queues for waiting processes
     * can be chosen, too.
     * 
     * 
     * @param owner
     *            desmoj.Model : The model this Stock is associated to.
     * @param name
     *            java.lang.String : The Stock's name, should indicate the kind
     *            of product stored in this Stock.
     * @param prodSortOrder
     *            int : determines the sort order of the underlying queue
     *            implementation for the producers. Choose a constant from
     *            <code>QueueBased</code> like <code>QueueBased.FIFO</code> or
     *            <code>QueueBased.LIFO</code> or ...
     * @param prodQCapacity
     *            int : The capacity of the producers queue, that is how many
     *            processes can be enqueued. Zero (0) means unlimited capacity.
     * @param consSortOrder
     *            int : determines the sort order of the underlying queue
     *            implementation for the consumers. Choose a constant from
     *            <code>QueueBased</code> like <code>QueueBased.FIFO</code> or
     *            <code>QueueBased.LIFO</code> or ...
     * @param consQCapacity
     *            int : The capacity of the consumers queue, that is how many
     *            processes can be enqueued. Zero (0) means unlimited capacity.
     * @param initialUnits
     *            long : The units of a product the Stock starts with. Must be
     *            positive!
     * @param capacity
     *            long : The maximum capacity of products this Stock can hold.
     * @param showInReport
     *            boolean : Flag, if this Stock should produce a report or not.
     * @param showInTrace
     *            boolean : Flag for trace to produce trace messages.
     */
    public Stock(Model owner, String name, int prodSortOrder,
            int prodQCapacity, int consSortOrder, int consQCapacity,
            long initialUnits, long capacity, boolean showInReport,
            boolean showInTrace) {
        super(owner, name, showInReport, showInTrace); // construct QueueBased

        // check the parameters for the producer queue
        // make a copy of them
        int pSortOrder = prodSortOrder;
        int pQCapacity = prodQCapacity;

        // check if a valid sortOrder is given
        if (prodSortOrder < 0 || prodSortOrder >= 3) {
            sendWarning(
                    "The given prodSortOrder parameter is negative or "
                            + "to big! "
                            + "A queue with Fifo sort order will be created instead.",
                    "Stock : "
                            + getName()
                            + " Constructor: Stock(Model owner, String name, int "
                            + "prodSortOrder, long prodQCapacity, int consSortOrder, "
                            + "long consQCapacity, long initialUnits, long capacity, "
                            + "boolean showInReport, boolean showInTrace)",
                    "A valid positive integer number must be provided to "
                            + "determine the sort order of the underlying queue.",
                    "Make sure to provide a valid positive integer number "
                            + "by using the constants in the class QueueBased, like "
                            + "QueueBased.FIFO or QueueBased.LIFO.");
            // make a Fifo queue
            pSortOrder = QueueBased.FIFO; // better than nothing
        }

        // check if it the queue capacity does make sense
        if (prodQCapacity < 0) {
            sendWarning(
                    "The given capacity of the producer queue is negative! "
                            + "A queue with unlimited capacity will be created instead.",
                    "Stock : "
                            + getName()
                            + " Constructor: Stock(Model owner, String name, int "
                            + "prodSortOrder, long prodQCapacity, int consSortOrder, "
                            + "long consQCapacity, long initialUnits, long capacity, "
                            + "boolean showInReport, boolean showInTrace)",
                    "A negative capacity for a queue does not make sense.",
                    "Make sure to provide a valid positive capacity "
                            + "for the underlying queue.");
            // set the capacity to the maximum value
            pQCapacity = Integer.MAX_VALUE;
        }

        // create the queue for the producers
        _producerQueue = new ProcessQueue<SimProcess>(owner, name + "_P", pSortOrder,
                pQCapacity, false, false);
        // add the underlying QueueList* of the producer queue to the
        // PropertyChangeListeners
        addPropertyChangeListener("avail", _producerQueue.getQueueList());

        // check the parameters for the consumer queue
        // check if a valid sortOrder is given
        switch (consSortOrder) {
        case QueueBased.FIFO :
            _consumerQueue = new QueueListFifo<SimProcess>(); break;
        case QueueBased.LIFO :
            _consumerQueue = new QueueListLifo<SimProcess>(); break;
        case QueueBased.RANDOM :
            _consumerQueue = new QueueListRandom<SimProcess>(); break;
        default :
            sendWarning(
                    "The given consSortOrder parameter " + consSortOrder + " is not valid! "
                            + "A queue with Fifo sort order will be created instead.",
                    "Stock : "
                            + getName()
                            + " Constructor: Stock(Model owner, String name, int "
                            + "prodSortOrder, long prodQCapacity, int consSortOrder, "
                            + "long consQCapacity, long initialUnits, long capacity, "
                            + "boolean showInReport, boolean showInTrace)",
                    "A valid positive integer number must be provided to "
                            + "determine the sort order of the underlying queue.",
                    "Make sure to provide a valid positive integer number "
                            + "by using the constants in the class QueueBased, like "
                            + "QueueBased.FIFO or QueueBased.LIFO.");
            _consumerQueue = new QueueListFifo<SimProcess>(); 
        }
        // give the QueueList a reference to this QueueBased
        _consumerQueue.setQueueBased(this);

        // add the QueueList* to the PropertyChangeListeners
        addPropertyChangeListener("avail", _consumerQueue);

        // set the capacity of the queue
        queueLimit = consQCapacity;

        // check if it the capacity does make sense
        if (consQCapacity < 0) {
            sendWarning(
                    "The given capacity of the consumer queue is negative! "
                            + "A queue with unlimited capacity will be created instead.",
                    "Stock : "
                            + getName()
                            + " Constructor: Stock(Model owner, String name, int "
                            + "prodSortOrder, long prodQCapacity, int consSortOrder, "
                            + "long consQCapacity, long initialUnits, long capacity, "
                            + "boolean showInReport, boolean showInTrace)",
                    "A negative capacity for a queue does not make sense.",
                    "Make sure to provide a valid positive capacity "
                            + "for the underlying queue.");
            // set the capacity to the maximum value
            queueLimit = Integer.MAX_VALUE;
        }

        // check if qCapacity is zero (that means unlimited capacity)
        if (consQCapacity == 0) {
            // set the capacity to the maximum value
            queueLimit = Integer.MAX_VALUE;
        }

        // set the capacity of the Stock
        _fieldCapacity = capacity;

        if (capacity == 0)
            _fieldCapacity = Long.MAX_VALUE;

        if (capacity < 0) {
            sendWarning(
                    "Attempt to construct a Stock with a negativ capacity."
                            + " The capacity will be converted to the positive value!",
                    "Stock: "
                            + getName()
                            + " Constructor: Stock(Model owner, String name, "
                            + "int prodSortOrder, long prodQCapacity, int consSortOrder, "
                            + "long consQCapacity, long initialUnits, long capacity, "
                            + "boolean showInReport, boolean showInTrace)",
                    "A negative capacity does not make sense for a stock.",
                    "Make sure to initialize a Stock always with a positive capacity.");

            // set it to the positive value of capacity
            _fieldCapacity = java.lang.Math.abs(capacity);
        }

        // set the number of initial units
        _fieldInitial = initialUnits;

        if (initialUnits < 0) // there can't be less than nothing
        {
            sendWarning(
                    "Attempt to construct a Stock with a negativ number of"
                            + " units. Initial number of units will be set to zero!",
                    "Stock: "
                            + getName()
                            + " Constructor: Stock(Model owner, String name, "
                            + "int prodSortOrder, long prodQCapacity, int consSortOrder, "
                            + "long consQCapacity, long initialUnits, long capacity, "
                            + "boolean showInReport, boolean showInTrace)",
                    "A negative number of units does not make sense here.",
                    "Make sure to initialize a Stock always with a positive number of "
                            + "initialUnits.");

            // set it to 0, that makes more sense
            _fieldInitial = 0;
        }

        // check if there should be more units stored than the capacity can hold
        if (_fieldInitial > _fieldCapacity) {
            sendWarning(
                    "Attempt to construct a Stock with initially more units"
                            + " in stock than the capacity can hold. The capacity will be increased "
                            + "to hold all the initial units!",
                    "Stock: "
                            + getName()
                            + " Constructor: Stock(Model owner, String name, "
                            + "int prodSortOrder, long prodQCapacity, int consSortOrder, "
                            + "long consQCapacity, long initialUnits, long capacity, "
                            + "boolean showInReport, boolean showInTrace)",
                    "A capacity lower than the initial number of units in the stock does not "
                            + "make sense.",
                    "Make sure to initialize a Stock always with a capacity greater or equal "
                            + "to the initial number of stored units.");

            // set the capacity to the initial stock
            _fieldCapacity = _fieldInitial;
        }

        // set the number of available and maximum units, so far
        _fieldAvail = _fieldMaximum = _fieldMinimum = _fieldInitial;

        // set the statistics
        this._wSumAvail = 0.0;
        this._lastUsage = presentTime();
        _fieldProducers = _fieldConsumers = 0;
        _fieldRefused = 0;
    }

    /**
     * Constructor for a <code>Stock</code> with a certain capacity and a 
     * certain number of initial units of a product in it.
     * Waiting producer and consumer processes are sorted (and thus activated) 
     * according to their queueing priorities (highest first) and (if equal) 
     * by FIFO. The capacities of the underlying queues for waiting processes
     * are unlimited.
     * 
     * @param owner
     *            desmoj.Model : The model this Stock is associated to.
     * @param name
     *            java.lang.String : The Stock's name, should indicate the kind
     *            of product stored in this Stock.
     * @param initialUnits
     *            long : The units of a product the Stock starts with. Must be
     *            positive!
     * @param capacity
     *            long : The maximum capacity of products this Stock can hold.
     * @param showInReport
     *            boolean : Flag, if this Stock should produce a report or not.
     * @param showInTrace
     *            boolean : Flag for trace to produce trace messages.
     */
    public Stock(Model owner, String name, long initialUnits, long capacity,
            boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace); // construct QueueBased

        // make the actual consumer queue and
        // give it a reference to this "QueueBased"-Stock
        _consumerQueue = new QueueListFifo<SimProcess>();
        _consumerQueue.setQueueBased(this);

        // add the QueueList* to the PropertyChangeListeners
        addPropertyChangeListener("avail", _consumerQueue);

        // make the queue for the producers
        _producerQueue = new ProcessQueue<SimProcess>(owner, name + "_P", false, false);

        // add the underlying QueueList* of the producer queue to the
        // PropertyChangeListeners
        addPropertyChangeListener("avail", _producerQueue.getQueueList());

        // set the capacity
        _fieldCapacity = capacity;

        if (capacity == 0)
            _fieldCapacity = Long.MAX_VALUE;

        if (capacity < 0) {
            sendWarning(
                    "Attempt to construct a Stock with a negativ capacity."
                            + " The capacity will be converted to the positive value!",
                    "Stock: "
                            + getName()
                            + " Constructor: Stock(Model owner, String name, "
                            + "long initialUnits, long capacity, boolean showInReport, "
                            + "boolean showInTrace)",
                    "A negative capacity does not make sense for a stock.",
                    "Make sure to initialize a Stock always with a positive capacity.");

            // set it to the positive value of capacity
            _fieldCapacity = java.lang.Math.abs(capacity);
        }

        // set the number of initial units
        _fieldInitial = initialUnits;

        if (initialUnits < 0) // there can't be less than nothing
        {
            sendWarning(
                    "Attempt to construct a Stock with a negativ number of"
                            + " units. Initial number of units will be set to zero!",
                    "Stock: "
                            + getName()
                            + " Constructor: Stock(Model owner, String name, "
                            + "long initialUnits, long capacity, boolean showInReport, "
                            + "boolean showInTrace)",
                    "A negative number of units does not make sense here.",
                    "Make sure to initialize a Stock always with a positive number of "
                            + "initialUnits.");

            // set it to 0, that makes more sense
            _fieldInitial = 0;
        }

        // check if there should be more units stored than the capacity can hold
        if (_fieldInitial > _fieldCapacity) {
            sendWarning(
                    "Attempt to construct a Stock with initially more units"
                            + " in stock than the capacity can hold. The capacity will be increased "
                            + "to hold all the initial units!",
                    "Stock: "
                            + getName()
                            + " Constructor: Stock(Model owner, String name, "
                            + "long initialUnits, long capacity, boolean showInReport, "
                            + "boolean showInTrace)",
                    "A capacity lower than the initial number of units in the stock does not "
                            + "make sense.",
                    "Make sure to initialize a Stock always with a capacity greater or equal "
                            + "to the initial number of stored units.");

            // set the capacity to the initial stock
            _fieldCapacity = _fieldInitial;
        }

        // set the number of available and maximum units, so far
        _fieldAvail = _fieldMaximum = _fieldMinimum = _fieldInitial;

        // set the statistics
        this._wSumAvail = 0.0;
        this._lastUsage = presentTime();
    }

    /**
     * The addPropertyChangeListener method was generated to support the
     * propertyChange field.
     */
    public synchronized void addPropertyChangeListener(
            java.beans.PropertyChangeListener listener) {
        getPropertyChange().addPropertyChangeListener(listener);
    }

    /**
     * The addPropertyChangeListener method was generated to support the
     * propertyChange field.
     */
    public synchronized void addPropertyChangeListener(String propertyName,
            java.beans.PropertyChangeListener listener) {
        getPropertyChange().addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Returning the average number of units available in the Stock over the
     * time since the last reset of the Stock.
     * 
     * @return double : The average number of units available in the Stock over
     *         the time since the last reset of the Stock.
     */
    public double avgAvail() {
        /* Perform the avgAvail method. */

        TimeInstant now = presentTime(); // what is the time?
        // how long since the last reset
        TimeSpan diff = TimeOperations.diff(now, resetAt());

        // update the weighted sum of available units
        double wSumAvl = _wSumAvail
                + ((double) _fieldAvail * TimeOperations.diff(now, _lastUsage)
                        .getTimeInEpsilon());
        if (diff.isZero()) // diff is not long enough
        {
            sendWarning("A Division-by-Zero error occured in a calculation. "
                    + "The UNDEFINED Value: -1.0 is returned as result.",
                    "Stock: " + getName() + " Method: double avgAvail ()",
                    "The Time difference is zero.",
                    "Make sure not to use avgAvail() right after a reset.");
            return UNDEFINED; // see QueueBased: UNDEFINED = -1
        }
        // return the rounded average
        return StatisticObject.round(wSumAvl / diff.getTimeInEpsilon());
    }

    /**
     * Returns a Reporter to produce a report about this Stock.
     * 
     * @return desmoj.report.Reporter : The Reporter reporting about the
     *         statistics of the two queues (producer and consumer) of this
     *         Stock.
     */
    public desmoj.core.report.Reporter createDefaultReporter() {
        /* Perform the createReporter method. */
        return new desmoj.core.advancedModellingFeatures.report.StockReporter(
                this);
    }

    /**
     * The firePropertyChange method was generated to support the propertyChange
     * field.
     */
    public void firePropertyChange(java.beans.PropertyChangeEvent evt) {
        getPropertyChange().firePropertyChange(evt);
    }

    /**
     * The firePropertyChange method was generated to support the propertyChange
     * field.
     */
    public void firePropertyChange(String propertyName, boolean oldValue,
            boolean newValue) {
        getPropertyChange()
                .firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * The firePropertyChange method was generated to support the propertyChange
     * field.
     */
    public void firePropertyChange(String propertyName, int oldValue,
            int newValue) {
        getPropertyChange()
                .firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * The firePropertyChange method was generated to support the propertyChange
     * field.
     */
    public void firePropertyChange(String propertyName, Object oldValue,
            Object newValue) {
        getPropertyChange()
                .firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Gets the available number of products in the Stock at the moment.
     * 
     * @return long : The available number of products in the Stock at the
     *         moment.
     */
    public long getAvail() {
        return _fieldAvail;
    }

    /**
     * Gets the capacity property (long) value of this Stock, that is the number
     * of units this stock can hold at max.
     * 
     * @return long : The capacity property value, that is the number of units
     *         this stock can hold at max.
     */
    public long getCapacity() {
        return _fieldCapacity;
    }

    /**
     * Returns the implemented queueing discipline of the underlying consumer
     * queue as a String, so it can be displayed in the report.
     * 
     * @return String : The String indicating the queueing discipline.
     */
    public String getConsQueueStrategy() {

        return _consumerQueue.getAbbreviation(); // that's it

    }

    /**
     * Returns the number of consumers that were refused to be enqueued because
     * the queue for consumers was full.
     * 
     * @return long : The number of consumers that were refused to be enqueued
     *         because the queue for consumers was full.
     */
    public long getConsRefused() {
        /* Perform the getConsRefused method. */
        return getRefused(); // get this refused
    }

    /**
     * Gets the consumers property (long) value.
     * 
     * @return The consumers property value.
     */
    public long getConsumers() {
        return _fieldConsumers;
    }

    /**
     * Gets the initial number of products the Stock starts with.
     * 
     * @return long : The initial number of products the Stock starts with.
     */
    public long getInitial() {
        return _fieldInitial;
    }

    /**
     * Gets the maximum number of products in the Stock.
     * 
     * @return long : The maximum number of products in the Stock.
     */
    public long getMaximum() {
        return _fieldMaximum;
    }

    /**
     * Gets the minimum number of units in the Stock so far (since the last
     * reset).
     * 
     * @return long : The minimum number of units in the Stock since the last
     *         reset.
     */
    public long getMinimum() {
        return _fieldMinimum;
    }

    /**
     * Returns whether entities can pass by other entities which are enqueued
     * before them in the queue.
     * 
     * @return boolean : Indicates whether entities can pass by other entities
     *         which are enqueued before them in the queue.
     */
    public boolean getPassByConsumers() {
        return _passByCons;
    }

    /**
     * Returns whether entities can pass by other entities which are enqueued
     * before them in the queue.
     * 
     * @return boolean : Indicates whether entities can pass by other entities
     *         which are enqueued before them in the queue.
     */
    public boolean getPassByProducers() {
        return _passByProds;
    }

    /**
     * Returns the implemented queueing discipline of the underlying queue for
     * producers as a String, so it can be displayed in the report.
     * 
     * @return String : The String indicating the queueing discipline.
     */
    public String getProdQueueStrategy() {
        /* Perform the getProdQueueStrategy method. */

        return _producerQueue.getQueueStrategy(); // ask the producer queue
    }

    /**
     * Returns the number of producers that were refused to be enqueued because
     * the queue for producers was full.
     * 
     * @return long : The number of producers that were refused to be enqueued
     *         because the queue for producers was full.
     */
    public long getProdRefused() {
        /* Perform the getProdRefused method. */
        return getProducerQueue().getRefused();
    }

    /**
     * Returns the queue where the producers are waiting to deliver their units.
     * 
     * @return desmoj.ProcessQueue : the queue where the producers are waiting
     *         to deliver their units.
     */
    public ProcessQueue<SimProcess> getProducerQueue() {
        /* Perform the getProducerQueue method. */

        return this._producerQueue; // that's all
    }

    /**
     * Gets the producers property (long) value.
     * 
     * @return The producers property value.
     */
    public long getProducers() {
        return _fieldProducers;
    }

    /**
     * Gets the refused property (long) value.
     * 
     * @return The refused property value.
     */
    public long getRefused() {
        return _fieldRefused;
    }

    /**
     * Gets the where property (String) value denoting the method, where
     * something has gone wrong.
     * 
     * @return java.lang.String : The where property value denoting the method,
     *         where something has gone wrong.
     */
    public String getWhere() {
        return _fieldWhere;
    }

    /**
     * The hasListeners method was generated to support the propertyChange
     * field.
     */
    public synchronized boolean hasListeners(String propertyName) {
        return getPropertyChange().hasListeners(propertyName);
    }

    /**
     * The removePropertyChangeListener method was generated to support the
     * propertyChange field.
     */
    public synchronized void removePropertyChangeListener(
            java.beans.PropertyChangeListener listener) {
        getPropertyChange().removePropertyChangeListener(listener);
    }

    /**
     * The removePropertyChangeListener method was generated to support the
     * propertyChange field.
     */
    public synchronized void removePropertyChangeListener(String propertyName,
            java.beans.PropertyChangeListener listener) {
        getPropertyChange()
                .removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Resets all statistical counters of this Stock to their default values.
     * Both, producer queue and consumer queue are reset. The number of
     * available units at this moment and the consumer and producer processes
     * waiting in their queues remain unchanged. The maximum length of the
     * queues are set to the current number of enqueued processes.
     */
    public void reset() {
        /* Perform the reset method. */

        super.reset(); // reset the QueueBased statistics of this Stock

        _producerQueue.reset(); // reset the statistics for the producer queue

        _fieldMaximum = _fieldMinimum = _fieldAvail;
        _fieldProducers = _fieldConsumers = 0;
        _wSumAvail = 0.0;
        _lastUsage = presentTime();
        _fieldRefused = 0;
    }

    /**
     * Method for consumers to make the Stock retrieve a number of n units. When
     * there are not enough units available or another consumer is first in the
     * queue to be served (and it is not possible to pass by), the current
     * consumer process will be blocked and inserted in the waiting-queue (for
     * consumers).
     * 
     * The order of retrieving (and, thus, process re-activation) depends on the 
     * order of the internal queue, which is based on the consumer processes' queueing 
     * priorities and (if queueing priorities are equal, requiring
     * a tie-breaker) by FIFO or a different discipline as defined in the
     * constructor. 
     * 
     * @return boolean : Is <code>true</code> if the specified number of units
     *         can been retrieved successfully, <code>false</code> otherwise
     *         (i.e. capacity limit of the queue is reached).
     * @param n
     *            long : The number of units the Stock is retrieving for the
     *            consumer; must be positive.
     */
    public boolean retrieve(long n)  {
        /* Perform the retrieve method. */

        _fieldWhere = "boolean retrieve(long n)";

        SimProcess currentProcess = currentSimProcess();

        if (!checkProcess(currentProcess, _fieldWhere)) // check the current
        // process
        {
            return false;
        } // if it is not valid return false

        if (n <= 0) // if the process is taking nothing or less
        {
            sendWarning(
                    "Attempt to take nothing or a negative number of units"
                            + " out of a Stock. The attempted action is ignored!",
                    "Stock: " + getName() + " Method: " + _fieldWhere,
                    "It does not make sense to take nothing or less out of a Stock. "
                            + "The statistic will be corrupted with negative numbers!",
                    "Make sure to take at least one unit out of the Stock.");
            return false; // go to where you came from; ignore that rubbish
        }

        if (n > _fieldCapacity) // if the process is trying to take more than
        // the
        // capacity
        {
            sendWarning(
                    "Attempt to take more units than the capacity of this Stock"
                            + " can ever hold. The attempted action is ignored!",
                    "Stock: " + getName() + " Method: " + _fieldWhere,
                    "The Stock can never retrieve more units than its capacity can hold.",
                    "Make sure not to take more units out of the Stock than its capacity.");
            return false; // go to where you came from; ignore that rubbish
        }

        // check if capacity limit of the consumer queue is reached
        if (queueLimit <= length()) {
            if (currentlySendDebugNotes()) {
                sendDebugNote("refuses to insert "
                        + currentProcess.getQuotedName()
                        + " in consumer waiting-queue, because the capacity limit is reached. ");
            }

            if (currentlySendTraceNotes()) {
                sendTraceNote("is refused to be enqueued in "
                        + this.getQuotedName() + "because the capacity limit ("
                        + getQueueLimit() + ") of the "
                        + "consumer queue is reached");
            }

            _fieldRefused++; // count the refused ones

            return false; // capacity limit is reached
        }

        // insert every process in the queue for statistic reasons
        _consumerQueue.insert(currentProcess);

        // is it possible for this process to pass by?
        if (_passByCons == false) {
            // not enough products available OR other process is first in the
            // queue
            if (n > _fieldAvail || currentProcess != _consumerQueue.first()) {
                // tell in the trace what the process is waiting for
                if (currentlySendTraceNotes()) {
                    sendTraceNote("awaits " + n + " of " + this.getQuotedName());
                }

                // for debugging purposes
                if (currentlySendDebugNotes()) {
                    sendDebugNote("can not retrieve " + n + " units for "
                            + currentProcess.getQuotedName() + "<br>"
                            + "because stock (" + getAvail()
                            + " units) is too low.");
                }

                do { // the process is stuck in here
                    currentProcess.setBlocked(true); // as long as ...see
                    // while
                    currentProcess.skipTraceNote(); // don't tell the user, that
                    // we ...
                    currentProcess.passivate(); // passivate the current process
                }
                // not enough products available OR other process is first in
                // the queue
                while (n > _fieldAvail
                        || currentProcess != _consumerQueue.first());

            } // end if

        } // end if (passBy = false)

        else // the process is allowed to pass by other processes (passBy =
        // true)
        {
            // not enough products available OR other process is first in the
            // queue
            if (n > _fieldAvail || currentProcess != _consumerQueue.first()) {
                // if this consumer is not the first in the consumer queue
                if (currentProcess != _consumerQueue.first()) {
                    // we have to make sure that no other process in front of
                    // this current
                    // process in the wait queue could be satisfied, so activate
                    // the first Process in the queue to see what he can do. He
                    // will pass
                    // the activation on to his successors until this process
                    // will be
                    // activated again to get his products. (hopefully)
                    activateFirstConsumer();
                }

                // only if not enough products are available the process has to
                // wait
                if (n > _fieldAvail) {
                    // tell in the trace what the process is waiting for
                    if (currentlySendTraceNotes()) {
                        sendTraceNote("awaits " + n + " of "
                                + this.getQuotedName());
                    }

                    // for debugging purposes
                    if (currentlySendDebugNotes()) {
                        sendDebugNote("can not retrieve " + n + " units for "
                                + currentProcess.getQuotedName() + "<br>"
                                + "because stock (" + getAvail()
                                + " units) is too low.");
                    }
                } // end if not enough products are available

                // block and passivate the process until enough products are
                // available
                do { // the process is stuck in here
                    currentProcess.setBlocked(true); // as long as ...see
                    // while
                    currentProcess.skipTraceNote(); // don't tell the user, that
                    // we ...
                    currentProcess.passivate(); // passivate the current process

                    // activate the next process in the consumer queue to see
                    // what he can do
                    activateAsNext(_consumerQueue.succ(currentProcess));
                } while (n > _fieldAvail); // not enough products available

            } // end if
        } // end else (passBy = true)

        // the current process has got the products he wanted ...

        _consumerQueue.remove(currentProcess); // get the process out of the
        // queue
        currentProcess.setBlocked(false); // we are not blocked (anymore),
        // yeah!

        /*
         * this will be done with a PropertyChangeEvent when the fieldAvail is
         * updated. Hopefully ... activateNext(); // give waiting successors a
         * chance
         */

        updateStatistics(-n); // statistics will be updated
        // with a negative n for deliver(), remember?!

        // tell in the trace what the process is taking from the Stock
        if (currentlySendTraceNotes()) {
            sendTraceNote("takes " + n + " units from " + this.getQuotedName());
        }

        // for debugging purposes
        if (currentlySendDebugNotes()) {
            sendDebugNote("retrieves " + n + " units for "
                    + currentProcess.getQuotedName() + "<br>" + "and has now "
                    + getAvail() + " units on stock.");
        }

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
    public void setPassByConsumers(boolean newPassBy) {
        this._passByCons = newPassBy; // that's all!
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
    public void setPassByProducers(boolean newPassBy) {
        this._passByProds = newPassBy; // that's all!
    }

    /**
     * Method for producers to make the Stock store a number of n units. When
     * the capacity of the stock can not hold the additional incoming units or
     * another producer is already waiting at first position in the queue (and
     * it is not possible to pass by), the current producer process will be
     * blocked and inserted in the queue (for producers).
     * 
     * The order of storing (and, thus, process re-activation) depends on the 
     * order of the internal queue, which is based on the producers processes' queueing 
     * priorities and (if queueing priorities are equal, requiring
     * a tie-breaker) by FIFO or a different discipline as defined in the
     * constructor. 
     * 
     * @return boolean : Is <code>true</code> if the specified number of units
     *         can been stored successfully, <code>false</code> otherwise (i.e.
     *         capacity limit of the queue is reached).
     * @param n
     *            long : The number of units the Stock is receiving from the
     *            producer to store. n must be positive.
     */
    public boolean store(long n)  {
        /* Perform the store method. */

        _fieldWhere = "boolean store(long n)";

        SimProcess currentProcess = currentSimProcess();

        if (!checkProcess(currentProcess, _fieldWhere)) // check the current
        // process
        {
            return false;
        } // if it is not valid return false

        if (n <= 0) // if the process is giving nothing or less
        {
            sendWarning(
                    "Attempt to store nothing or a negative number of units"
                            + " in a Stock. The attempted action is ignored!",
                    "Stock: " + getName() + " Method: " + _fieldWhere,
                    "It does not make sense to store nothing or less in a Stock.",
                    "Make sure to store at least one unit in the Stock.");
            return false; // go to where you came from; ignore that rubbish
        }

        // is the process trying to store more than the capacity can ever hold?
        if (n > _fieldCapacity) {
            sendWarning(
                    "Attempt to store more units than the capacity of this Stock"
                            + " can hold. The attempted action is ignored!",
                    "Stock: " + getName() + " Method: " + _fieldWhere,
                    "The Stock can never store more units than its capacity. "
                            + "Units to store: " + n
                            + " exceeds the capacity of: " + getCapacity(),
                    "Make sure not to store more units in a Stock than its capacity "
                            + "can hold.");
            return false; // go to where you came from; ignore that rubbish
        }

        // check if capacity limit of the producer queue is reached
        if (_producerQueue.getQueueLimit() <= _producerQueue.length()) {
            if (currentlySendDebugNotes()) {
                sendDebugNote("refuses to insert "
                        + currentProcess.getQuotedName()
                        + " in producer waiting-queue, because the capacity limit is reached. ");
            }

            if (currentlySendTraceNotes()) {
                sendTraceNote("is refused to be enqueued in "
                        + this.getQuotedName() + "because the capacity limit ("
                        + getQueueLimit() + ") of the "
                        + "producer queue is reached");
            }

            // count the refused producers
            _producerQueue.setRefused(_producerQueue.getRefused() + 1);

            return false; // capacity limit is reached
        }

        // insert every process in the queue for statistic reasons
        _producerQueue.insert(currentProcess);

        // is it possible for this process to pass by?
        if (_passByProds == false) {

            // not enough space for the new units left OR other process is first
            // in the q
            if (n + _fieldAvail > _fieldCapacity
                    || currentProcess != _producerQueue.first()) {
                // tell in the trace what the process is waiting for
                if (currentlySendTraceNotes()) {
                    sendTraceNote("is waiting to store " + n + " units to '"
                            + this.getName() + "'");
                }

                // for debugging purposes
                if (currentlySendDebugNotes()) {
                    sendDebugNote("can not store " + n + " units from "
                            + currentProcess.getQuotedName() + "<br>"
                            + "because capacity limit (" + getCapacity()
                            + ") is reached.");
                }

                do { // the process is stuck in here
                    currentProcess.setBlocked(true); // as long as ...see
                    // while
                    currentProcess.skipTraceNote(); // don't tell the user, that
                    // we ...
                    currentProcess.passivate(); // passivate the current process
                }
                // not enough space available OR other process is first in the
                // queue
                while (n + _fieldAvail > _fieldCapacity
                        || currentProcess != _producerQueue.first());

            } // end if

        } // end if (passBy = false)

        else // the process is allowed to pass by other processes (passBy =
        // true)
        {
            // not enough space for the new units left OR other process is first
            // in the q
            if (n + _fieldAvail > _fieldCapacity
                    || currentProcess != _producerQueue.first()) {
                // if this producer is not the first in the producer queue
                if (currentProcess != _producerQueue.first()) {
                    // we have to make sure that no other process in front of
                    // this current
                    // process in the wait queue could be satisfied, so activate
                    // the first Process in the queue to see what he can do. He
                    // will pass
                    // the activation on to his successors until this process
                    // will be
                    // activated again to get his products. (hopefully)
                    activateFirstProducer();
                }

                // only if not enough space is left for the units the process
                // has to wait
                if (n + _fieldAvail > _fieldCapacity) {
                    // tell in the trace what the process is waiting for
                    if (currentlySendTraceNotes()) {
                        sendTraceNote("is waiting to store " + n
                                + " units to '" + this.getName() + "'");
                    }

                    // for debugging purposes
                    if (currentlySendDebugNotes()) {
                        sendDebugNote("can not store " + n + " units from "
                                + currentProcess.getQuotedName() + "<br>"
                                + "because capacity limit (" + getCapacity()
                                + ") is reached.");
                    }
                } // end if not enough space is left for the units

                // block and passivate the process until enough space is
                // available
                do { // the process is stuck in here
                    currentProcess.setBlocked(true); // as long as ...see
                    // while
                    currentProcess.skipTraceNote(); // don't tell the user, that
                    // we ...
                    currentProcess.passivate(); // passivate the current process
                } while (n + _fieldAvail > _fieldCapacity); // not enough space
                // available

            }
        } // end else (passBy = true)

        // the currrent process can store his products in the stock ...

        _producerQueue.remove(currentProcess); // get the process out of the
        // queue
        currentProcess.setBlocked(false); // we are not blocked (anymore),
        // yeah!

        /*
         * this will be done with a PropertyChangeEvent when the fieldAvail is
         * updated. Hopefully ... activateNext(); // give waiting successors a
         * chance
         */

        updateStatistics(n); // statistics will be updated

        // tell in the trace what the process is storing in the Stock
        if (currentlySendTraceNotes()) {
            sendTraceNote("stores " + n + " units to '" + this.getName() + "'");
        }

        // for debugging purposes
        if (currentlySendDebugNotes()) {
            sendDebugNote("stores " + n + " units from "
                    + currentProcess.getQuotedName() + "<br>" + "and has now "
                    + getAvail() + " units on stock.");
        }

        return true;
    }

    /**
     * Activates the SimProcess <code>process</code>, given as a parameter of
     * this method, as the next process. This process should be a SimProcess
     * waiting in a queue for some products.
     * 
     * @param process
     *            SimProcess : The process that is to be activated as next.
     */
    protected void activateAsNext(SimProcess process) {
        String where = "protected void activateAsNext(SimProcess process)";

        if (process != null) {
            // if the given process is not valid just return
            if (!checkProcess(process, where)) {
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
     * Activates the first process waiting in the consumer queue. That is a
     * process which was trying to take products, but it could not get any
     * because there were not enough products for it or another process was
     * first in the queue to be served. This method is called every time a
     * consumer is arriving at the waiting-queue and it is possible for him to
     * pass other processes in front of him in the queue. Then we have to check
     * if one of the other process first in the queue could be satisfied before
     * the newly arrived one.
     */
    protected void activateFirstConsumer() {
        String where = "protected void activateFirstConsumer()";

        // first is the first process in the queue (or null if none is in the
        // queue)
        SimProcess first = _consumerQueue.first();

        if (first != null) {
            // if first is not modelcompatible just return
            if (!checkProcess(first, where)) {
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
     * Activates the first process waiting in the producer queue. That is a
     * process which was trying to store products, but it could store them
     * because the capacity limit of this stock is reached. This method is
     * called every time a producer is arriving at the waiting-queue and it is
     * possible for him to pass other processes in front of him in the queue.
     * Then we have to check if one of the other process first in the queue
     * could store his units before the newly arrived one.
     */
    protected void activateFirstProducer() {
        String where = "protected void activateFirstProducer()";

        // first is the first process in the queue (or null if none is in the
        // queue)
        SimProcess first = _producerQueue.first();

        if (first != null) {
            // if first is not modelcompatible just return
            if (!checkProcess(first, where)) {
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
     * Checks whether the SimProcess using the Stock is a valid process.
     * 
     * @return boolean : Returns whether the SimProcess is valid or not.
     * @param p
     *            SimProcess : Is this SimProcess a valid one?
     */
    protected boolean checkProcess(SimProcess p, String where) {
        if (p == null) // if p is a null pointer instead of a process
        {
            sendWarning(
                    "A non existing process is trying to use a Stock object. "
                            + "The attempted action is ignored!", "Stock: "
                            + getName() + " Method: " + where,
                    "The process is only a null pointer.",
                    "Make sure that only real SimProcesses are using Stocks.");
            return false;
        }

        if (!isModelCompatible(p)) // if p is not modelcompatible
        {
            sendWarning("The process trying to use a Stock object does not "
                    + "belong to this model. The attempted action is ignored!",
                    "Stock: " + getName() + " Method: " + where,
                    "The process is not modelcompatible.",
                    "Make sure that processes are using only Stocks within their model.");
            return false;
        }

        return true;
    }

    /**
     * Accessor for the propertyChange field.
     */
    protected java.beans.PropertyChangeSupport getPropertyChange() {
        if (propertyChange == null) {
            propertyChange = new java.beans.PropertyChangeSupport(this);
        }
        ;
        return propertyChange;
    }

    /**
     * Updates the statistics when producers or consumers access the Stock.
     * Changes the fieldAvail and fires the PropertyChangeEvent(s).
     * 
     * @param n
     *            long : Is positive when producers <code>store()</code> new
     *            units in the Stock and negative when the Stock
     *            <code>retrieve()</code>'s units for the consumer process.
     */
    protected void updateStatistics(long n) {
        TimeInstant now = presentTime(); // what's the time?
        _wSumAvail = _wSumAvail
                + ((double) _fieldAvail * TimeOperations.diff(now, _lastUsage)
                        .getTimeInEpsilon());
        _lastUsage = now;

        // remember old number of available units
        long oldAvail = _fieldAvail;
        // update number of available units
        _fieldAvail += n; // n can be positive or negative
        // fire PropertyChange to get all listeners (QueueList*) informed
        firePropertyChange("avail", Long.valueOf(oldAvail), Long
                .valueOf(_fieldAvail));

        if (n > 0) // it is a real producer
        {
            _fieldProducers++;
            if (_fieldAvail > _fieldMaximum) {
                _fieldMaximum = _fieldAvail;
            }
        } else // it is a consumer
        {
            _fieldConsumers++;
            if (_fieldAvail < _fieldMinimum) {
                _fieldMinimum = _fieldAvail;
            }
        }

    }
}