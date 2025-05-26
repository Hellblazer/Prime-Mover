package desmoj.core.simulator;

import java.util.Observable;

/**
 * The simulation clock shows the actual simulation time. The actual simulation time can be polled by any object but can
 * only be set by the scheduler responsible for the actual model. The simulation clock is extending class
 * <code>java.util.Observable</code> thus representing the 'observable' part in
 * a 'observer'-design pattern as described in [Gamm95] page 107. This enables observers to register themselves at the
 * simulation clock to be notified whenever the simulation time changes. This can be easily used to provide fully
 * automatic statistical counters. Each time the simulation time changes, a counter registered at the simulation clock
 * is notofied and can poll the value it is observing (most likely from a special <code>ValueSupplier</code> object).
 * This way, no explicit calls for the counter to update its observed value are needed. Note that on the other hand this
 * might reduce performance in comparison to explicit update call since the value under observation might not change
 * each time the simulation time is changed.
 *
 * @author Tim Lechler
 * @author modified by Soenke Claassen
 * @author modified by Felix Klueckmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @see java.util.Observable
 */
public class SimClock extends Observable {

    /**
     * The Simclock's Name
     */
    String name;

    /**
     * Stores internally the actual simulation time.
     */
    private TimeInstant _timeNow;

    /**
     * Constructs a simulation clock with no parameters given. By default the actual simulation time is set to zero.
     *
     * @param name String : The name of the simulation clock
     * @author Tim Lechler
     * @author modified by Felix Klueckmann
     */
    public SimClock(String name) {

        this.name = name + "_clock";
        // set the simulation clock to 0
        _timeNow = new TimeInstant(0); // the birth of time ;-)

    }

    /**
     * Returns the clock's name as string. This method has become necessary since the simulation clock does not extend
     * class <code>NamedObjectImp</code>.
     *
     * @return java.lang.String : The clock's name
     */
    public String getName() {

        return name;

    }

    /**
     * Returns the actual simulation time.
     *
     * @return TimeInstant : The actual simulation time
     */
    public TimeInstant getTime() {
        return _timeNow;
    }

    /**
     * Returns the clock's name as string. This method has become necessary since the simulation clock does not extend
     * class <code>NamedObjectImp</code>.
     *
     * @return java.lang.String : The clock's name
     */
    @Override
    public String toString() {

        return name;

    }

    /**
     * Sets the initial simulation time, overriding potential previous calls to this method. Allows negative values.
     * This method has to be protected from user access since it must not be manipulated by anyone but the scheduler.
     *
     * @param initTime TimeInstant : The initial simulation time
     */
    void setInitTime(TimeInstant initTime) {
        _timeNow = initTime;
    }

    /**
     * Sets the actual simulation time to a new value. This method has to be protected from user access since it must
     * not be manipulated by anyone but the scheduler.
     *
     * @param newTime TimeInstant : The new simulation time
     */
    void setTime(TimeInstant newTime) {
        // check if newTime is in the future
        if (TimeInstant.isBeforeOrEqual(newTime, _timeNow)) {
            // check for legal parameter (newTime>oldTime)
            if (TimeInstant.isBefore(newTime, _timeNow)) {
                // TODO Exception (Wrong Time)
            }
            return;
        }

        // note all observers of change before setting the new time!!!!
        setChanged(); // set the status to changed

        // tell every Observer registered the actual TimeInstant which will be
        // changed now
        notifyObservers(_timeNow);

        _timeNow = newTime; // now make the move for the next time change.
    }
}
