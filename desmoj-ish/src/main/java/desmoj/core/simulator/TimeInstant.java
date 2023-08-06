package desmoj.core.simulator;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Represents points in simulation time. Is used to indicate points in
 * simulation time at which the state of the model changes. Each point in
 * simulation time is represented by an individual object of this class and
 * offers its own methods for arithmetic operations.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Felix Klueckmann
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

public final class TimeInstant implements Comparable<TimeInstant> {

    /**
     * A short cut to the default time zone (thanks to Marcin Kawelski).
     */
    private final static TimeZone DEFAULT_PREFERRED_TIMEZONE = TimeZone.getTimeZone("UTC");

    /**
     * Checks if the first of two points of simulation time is after the second.
     * After means, that the time value of TimeInstant a is larger and hence "later"
     * than TimeInstant b. Note that this is a static method available through
     * calling the class <code>TimeInstant</code> i.e.
     * <code>TimeInstant.isAfter(a,b)</code> where a and b are valid TimeInstant
     * objects.
     *
     * @return boolean : True if a is after (later) than b
     * @param a TimeInstant : first comparand
     * @param b TimeInstant : second comparand
     */
    public static boolean isAfter(TimeInstant a, TimeInstant b) {
        return (a._timeInEpsilon > b._timeInEpsilon);
    }

    /**
     * Checks if the first of two points of simulation time is after the second or
     * equal to the second. After means, that the time value of TimeInstant a is
     * larger and hence after TimeInstant b. Equal means, that they both describe
     * the same point in simulation time. Note that this is a static method
     * available through calling the class <code>TimeInstant</code> i.e.
     * <code>TimeInstant.isAfterOrEqual(a,b)</code> where a and b are valid
     * timeInstant objects.
     *
     * @return boolean : True if a is after (later than )b or equal to b
     *
     * @param a TimeInstant : first comparand
     * @param b TimeInstant : second comparand
     */
    public static boolean isAfterOrEqual(TimeInstant a, TimeInstant b) {
        return (isAfter(a, b) || isEqual(a, b));
    }

    /**
     * Checks if the first of two points of simulation time is before the second.
     * Before means, that the time value of TimeInstant a is smaller and hence
     * "earlier" than TimeInstant b. Note that this is a static method available
     * through calling the class <code>TimeInstant</code> i.e.
     * <code>TimeInstant.isAfter(a,b)</code> where a and b are valid TimeInstant
     * objects.
     *
     * @return boolean : True if a is before (earlier) than b
     * @param a TimeInstant : first comparand
     * @param b TimeInstant : second comparand
     */
    public static boolean isBefore(TimeInstant a, TimeInstant b) {
        return (a._timeInEpsilon < b._timeInEpsilon);
    }

    /**
     * Checks if the first of two points of simulation time is before the second or
     * equal to the second. Before means, that the time value of TimeInstant a is
     * smaller and hence before TimeInstant b. Equal means, that they both describe
     * the same point in simulation time. Note that this is a static method
     * available through calling the class <code>TimeInstant</code> i.e.
     * <code>TimeInstant.isBeforeOrEqual(a,b)</code> where a and b are valid
     * timeInstant objects.
     *
     * @return boolean : True if a is before (earlier than )b or equal to b
     *
     * @param a TimeInstant : first comparand
     * @param b TimeInstant : second comparand
     */
    public static boolean isBeforeOrEqual(TimeInstant a, TimeInstant b) {
        return (isBefore(a, b) || isEqual(a, b));
    }

    /**
     * Checks if the two TimeInstant parameters describe the same point of
     * simulation time. Note that this is a static method available through calling
     * the class <code>TimeInstant</code> i.e. <code>TimeInstant.isEqual(a,b)</code>
     * where a and b are valid TimeInstant objects.
     *
     * @return boolean : True if both parameters describe same point of simulation
     *         time
     * @param a TimeInstant : first comparand
     * @param b TimeInstant : second comparand
     */
    public static boolean isEqual(TimeInstant a, TimeInstant b) {
        return (a._timeInEpsilon == b._timeInEpsilon);
    }

    /**
     * The preferred time zone for printing this time instant.
     */
    private TimeZone _preferredTimeZone;

    /**
     * The number of epsilons since the start of the epoch (January 1, 1970,
     * 00:00:00 GMT).
     */
    private final long _timeInEpsilon;

    /**
     * Constructs a TimeInstant object that represents the given instant of time
     * specified by the Calendar object. UTC will be used as default time zone for
     * output.
     *
     * @see java.util.Calendar
     */
    public TimeInstant(Calendar calendar) {
        this(calendar, false);
    }

    /**
     * Constructs a TimeInstant object that represents the given instant of time
     * specified by the Calendar object. The preferred time zone for output will can
     * either be adopted from the Calendar object or not be set.
     *
     * @param calendar                          Calendar : the instant of time that
     *                                          is represented by this TimeIstant
     * @param applyPrefferdTimeZoneFromCalendar boolean : Use the time zone as set
     *                                          in calendar for output (true) or use
     *                                          UTC as default time zone for output
     *                                          (false)
     *
     * @see java.util.Calendar
     */
    public TimeInstant(Calendar calendar, boolean applyPrefferdTimeZoneFromCalendar) {
        this(calendar.getTimeInMillis(), MILLISECONDS);
        if (applyPrefferdTimeZoneFromCalendar)
            this._preferredTimeZone = calendar.getTimeZone();
    }

    /**
     * Constructs a TimeInstant object that represents the given instant of time
     * specified by the Date object.
     *
     * @param date Date : the instant of time that is represented by this
     *             TimeInstant
     * @see java.util.Date
     */
    public TimeInstant(Date date) {
        this(date.getTime(), MILLISECONDS);
    }

    /**
     * Constructs a TimeInstant object with the given time value in the time unit of
     * the reference time. It represents a time Instant in simulation time.
     *
     * @param timeInReferenceUnit double : The time value of this TimeInstant in the
     *                            time unit of the reference time.
     */
    public TimeInstant(double timeInReferenceUnit) {
        this(timeInReferenceUnit, TimeOperations.getReferenceUnit());
    }

    /**
     * Constructs a TimeInstant object with the given time value in the time unit
     * given as second parameter. It represents a time Instant in simulation time.
     *
     * @param time double : The time value of this TimeInstant in the time unit of
     *             the reference time.
     *
     * @param unit TimeUnit : the time unit
     */
    public TimeInstant(double time, TimeUnit unit) {
        if (unit == null) { // no time unit given
            throw (new desmoj.core.exception.SimAbortedException(new desmoj.core.report.ErrorMessage(null,
                                                                                                     "Can't create TimeInstant object! Simulation aborted.",
                                                                                                     "Class : TimeInstant  Constructor : TimeInstant(double, TimeUnit)",
                                                                                                     "Time unit passed is null",
                                                                                                     "Make sure to pass a non-null time unit. \nNote that before "
                                                                                                     + "connecting model and experiment, TimeInstants must explicitly\n"
                                                                                                     + "refer to a time unit as the reference unit is not yet defined,"
                                                                                                     + "e.g. use \nTimeInstant(double time, TimeUnit unit) instead of"
                                                                                                     + "TimeInstant(double time).",
                                                                                                     null)));
        }
        // System.out.println(time + " " + unit);
        // System.out.println("Format: "
        // + new SimpleDateFormat().format(new Date(time)));
        if (unit.compareTo(TimeOperations.getEpsilon()) < 0) {
            // unit is a finer granularity than epsilon
            if (TimeOperations.getStartTime() != null) {// Start time has been
                // set
                System.out.println("Starttime: " + TimeOperations.getStartTime());
                double timeSinceStart = time // time since start...?
                - unit.convert(TimeOperations.getStartTime()// Achtung-negative
                                             // werden
                                             // positiv
                                             .getTimeInEpsilon(),
                               TimeOperations.getEpsilon());
                System.out.println("TimeSinceStart: " + timeSinceStart);
                if (timeSinceStart != 0) {
                    time = time - (timeSinceStart % unit.convert(1, TimeOperations.getEpsilon()));
                }
            }
        }
        this._timeInEpsilon = (long) (TimeOperations.getEpsilon().convert(1, unit) * time);
        TimeOperations.timeObjectsCreated = true;
        if (_timeInEpsilon == Long.MAX_VALUE) {
            /*
             * The timeInstant is too big. (The method
             * TimeUnit.convert(duration,unit)returns Long.MAX_VALUE if the result of the
             * conversion is to big
             */

            throw (new desmoj.core.exception.SimAbortedException(new desmoj.core.report.ErrorMessage(null,
                                                                                                     "Can't create TimeInstant object! Simulation aborted.",
                                                                                                     "Class : TimeInstant  Constructor : TimeInstant(long,TimeUnit)",
                                                                                                     "the TimeInstant is too big. ",
                                                                                                     "Can only create TimeInstant objects which are smaller than Long.MAX_VALUE (in the TimeUnit of epsilon).",
                                                                                                     null)));
        }

        this._preferredTimeZone = DEFAULT_PREFERRED_TIMEZONE;
    }

    /**
     * Constructs a TimeInstant object with the given time value in the time unit of
     * the reference time. It represents a time Instant in simulation time.
     *
     * @param timeInReferenceUnit long : The time value of this TimeInstant in the
     *                            time unit of the reference time.
     */
    public TimeInstant(long timeInReferenceUnit) {
        this(timeInReferenceUnit, TimeOperations.getReferenceUnit());
    }

    /**
     * Constructs a TimeInstant object with the given time value in the time unit of
     * the given parameter. It represents a time instant in simulation time. Note
     * that trying to create a TimeInstant object The simulation will also stop
     * immediately if the TimeSpan is larger than Long.MAX_VALUE-1 (in the unit of
     * epsilon).
     *
     * @param time long : The time value of this TimeInstant in the time unit of
     *             unit.
     * @param unit TimeUnit: the TimeUnit
     */
    public TimeInstant(long time, TimeUnit unit) {
        if (unit == null) { // no time unit given
            throw (new desmoj.core.exception.SimAbortedException(new desmoj.core.report.ErrorMessage(null,
                                                                                                     "Can't create TimeInstant object! Simulation aborted.",
                                                                                                     "Class : TimeInstant  Constructor : TimeInstant(long, TimeUnit)",
                                                                                                     "Time unit passed is null",
                                                                                                     "Make sure to pass a non-null time unit. \nNote that before "
                                                                                                     + "connecting model and experiment, TimeInstants must explicitly\n"
                                                                                                     + "refer to a time unit as the reference unit is not yet defined,"
                                                                                                     + "e.g. use \nTimeInstant(long time, TimeUnit unit) instead of"
                                                                                                     + "TimeInstant(long time).",
                                                                                                     null)));
        }
        // System.out.println(time + " " + unit);
        // System.out.println("Format: "
        // + new SimpleDateFormat().format(new Date(time)));
        if (unit.compareTo(TimeOperations.getEpsilon()) < 0) {
            // unit is a finer granularity than epsilon
            if (TimeOperations.getStartTime() != null) {// Start time has been
                // set
                System.out.println("Starttime: " + TimeOperations.getStartTime());
                long timeSinceStart = time // time since start...?
                - unit.convert(TimeOperations.getStartTime()// Achtung-negative
                                             // werden
                                             // positiv
                                             .getTimeInEpsilon(),
                               TimeOperations.getEpsilon());
                System.out.println("TimeSinceStart: " + timeSinceStart);
                if (timeSinceStart != 0) {
                    time = time - (timeSinceStart % unit.convert(1, TimeOperations.getEpsilon()));
                }
            }
        }
        this._timeInEpsilon = TimeOperations.getEpsilon().convert(time, unit);
        TimeOperations.timeObjectsCreated = true;
        if (_timeInEpsilon == Long.MAX_VALUE) {
            /*
             * The timeInstant is too big. (The method
             * TimeUnit.convert(duration,unit)returns Long.MAX_VALUE if the result of the
             * conversion is to big
             */

            throw (new desmoj.core.exception.SimAbortedException(new desmoj.core.report.ErrorMessage(null,
                                                                                                     "Can't create TimeInstant object! Simulation aborted.",
                                                                                                     "Class : TimeInstant  Constructor : TimeInstant(long,TimeUnit)",
                                                                                                     "the TimeInstant is too big. ",
                                                                                                     "Can only create TimeInstant objects which are smaller than Long.MAX_VALUE (in the TimeUnit of epsilon).",
                                                                                                     null)));
        }

        this._preferredTimeZone = DEFAULT_PREFERRED_TIMEZONE;
    }

    /**
     * Compares the given TimeInstant to this TimeInstant. This method implements
     * the Comparable<TimeInstant> Interface
     *
     * @param anotherInstant : The TimeInstant to be compared to this TimeInstant
     *
     * @return: int: Returns a negative integer, zero, or a positive integer as this
     *          TimeInstant is before, at the same time, or after the given
     *          TimeInstant.
     */
    @Override
    public int compareTo(TimeInstant anotherInstant) {
        return Long.compare(getTimeInEpsilon(), anotherInstant.getTimeInEpsilon());
    }

    /**
     * Indicates whether this TimeInstant is equal to the given parameter. Returns
     * true if the obj argument is a TimeInstant and represents the same point of
     * time as this TimeInstant; false otherwise. This method overrides
     * java.lang.Object.equals()
     *
     * @param o the reference object with which to compare.
     * @return: true if the obj argument is a TimeInstant and represents the same
     *          point of time as this TimeInstant; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TimeInstant))
            return false;
        TimeInstant instant = (TimeInstant) o;
        return isEqual(this, instant);
    }

    /**
     * Determines that last instant prior to this instant at which a new period of
     * the unit provided "begins", which means smaller units are zero. <br/>
     * Examples: Assume this TimeInstant is 28.12.2011 11:23:45:678. <br\> Calling
     * <code>getBeginOf(TimeUnit.TimeUnit.DAYS)</code> yields 28.12.2011
     * 00:00:00:000 (begin of current hour).<br\> Calling
     * <code>getBeginOf(TimeUnit.TimeUnit.MINUTES)</code> yields 28.12.2011
     * 11:23:00:000 (begin of current day).<br\> Note that days are interpreted with
     * respect to this instant's preferred time zone.
     *
     * @param smallestUnit TimeUnit : the unit to begin (i.e. smaller units set to
     *                     zero); must be TimeUnit.DAYS, TimeUnit.HOURS,
     *                     TimeUnit.MINUTES, TimeUnit.SECONDS or
     *                     TimeUnit.MILLISECONDS.
     *
     * @return TimeInstant: The TimeInstant at which the
     *         day/hour/minute/second/millisecond of the this TimeInstant has begun.
     */
    public TimeInstant getBeginOf(TimeUnit smallestUnit) {

        Calendar cal = Calendar.getInstance(this._preferredTimeZone);
        cal.setTimeInMillis(this.getTimeTruncated(TimeUnit.MILLISECONDS));

        switch (smallestUnit) {
        case DAYS:
            cal.set(Calendar.HOUR_OF_DAY, 0);
        case HOURS:
            cal.set(Calendar.MINUTE, 0);
        case MINUTES:
            cal.set(Calendar.SECOND, 0);
        case SECONDS:
            cal.set(Calendar.MILLISECOND, 0);
        case MILLISECONDS:
        default:
            // nothing to as units smaller than milliseconds are truncated above
        }
        return new TimeInstant(cal, true);
    }

    /**
     * The preferred time zone for printing this time instant.
     *
     * @return TimeZone: the time zone to use for printing this TimeInstant.
     */
    public TimeZone getPreferredTimeZone() {
        return this._preferredTimeZone;
    }

    /**
     * Returns the value of this TimeInstant object as a Calender object. Note that
     * the TimeZone of the Calender object returend is set to this TimeInstant's
     * preferred TimeZone (which defaults to UTC unless set differently).
     *
     * @return Calendar: a Calendar representation of this TimeInstant
     */
    public Calendar getTimeAsCalender() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(this.getTimeRounded(TimeUnit.MILLISECONDS));
        cal.setTimeZone(this._preferredTimeZone);
        return cal;
    }

    /**
     * Returns the value of this TimeInstant object as a Date object.
     *
     * @return Date: a Date representation of this TimeInstant
     */
    public Date getTimeAsDate() {
        return new Date(this.getTimeRounded(TimeUnit.MILLISECONDS));
    }

    /**
     * Returns the value of this TimeInstant object as a double type in the time
     * unit of the reference time.
     *
     * @return double: the time value of the TimeInstant object as a double type in
     *         the time unit given as a parameter
     */
    public double getTimeAsDouble() {
        return getTimeAsDouble(TimeOperations.getReferenceUnit());
    }

    /**
     * Returns the value of this TimeInstant object as a double type in the time
     * unit given as a parameter.
     *
     * @return double: the time value of the TimeInstant object as a double type in
     *         the time unit given as a parameter
     */
    public double getTimeAsDouble(TimeUnit unit) {
        return _timeInEpsilon / (double) TimeOperations.getEpsilon().convert(1, unit);
    }

    /**
     * Returns the value of the TimeInstant object as a long type in the time unit
     * of epsilon
     *
     * @return long: the time value of the TimeInstant object as a long type in the
     *         time unit of epsilon
     */
    public long getTimeInEpsilon() {
        return _timeInEpsilon;
    }

    /**
     * Returns the value of this TimeInstant object as a long type in the time unit
     * of the reference time. If the parameter has a coarser granularity than
     * epsilon the returned value will be rounded, so lose precision.
     *
     *
     * @return long: the time value of the TimeInstant object as a long type in the
     *         time unit given as a parameter or Long.MIN_VALUE if conversion would
     *         negatively overflow, or Long.MAX_VALUE if it would positively
     *         overflow.
     */
    public long getTimeRounded() {
        return getTimeRounded(TimeOperations.getReferenceUnit());
    }

    /**
     * Returns the value of this TimeInstant object as a long type in the time unit
     * given as a parameter. If the parameter has a coarser granularity than epsilon
     * the returned value will be rounded, so lose precision.
     *
     * @param unit the TimeUnit
     *
     * @return long: the time value of the TimeInstant object as a long type in the
     *         time unit given as a parameter or Long.MIN_VALUE if conversion would
     *         negatively overflow, or Long.MAX_VALUE if it would positively
     *         overflow.
     */
    public long getTimeRounded(TimeUnit unit) {
        if (unit.compareTo(TimeOperations.getEpsilon()) > 0) {
            // unit has a coarser granularity than epsilon
            long halfAUnitInEpsilon = TimeOperations.getEpsilon().convert(1, unit) / 2;
            long durationInUnitTruncated = getTimeTruncated(unit);
            long difference = _timeInEpsilon - TimeOperations.getEpsilon().convert(durationInUnitTruncated, unit);
            // if the time value in the unit Epsilon is bigger than
            if (difference >= halfAUnitInEpsilon) {
                return durationInUnitTruncated + 1;
            }
            return durationInUnitTruncated;
        } else {
            // unit has a finer granularity or is equal than epsilon
            return getTimeTruncated(unit);
        }

    }

    /**
     * Returns the value of this TimeInstant object as a long type in the time unit
     * of the reference time. If the parameter has a coarser granularity than
     * epsilon the returned value will be truncated, so lose precision.
     *
     *
     * @return long: the time value of the TimeInstant object as a long type in the
     *         time unit given as a parameter or Long.MIN_VALUE if conversion would
     *         negatively overflow, or Long.MAX_VALUE if it would positively
     *         overflow.
     */
    public long getTimeTruncated() {
        return getTimeTruncated(TimeOperations.getReferenceUnit());
    }

    /**
     * Returns the value of this TimeInstant object as a long type in the time unit
     * given as a parameter. If the parameter has a coarser granularity than epsilon
     * the returned value will be truncated, so lose precision.
     *
     *
     * @return long: the time value of the TimeInstant object as a long type in the
     *         time unit given as a parameter or Long.MIN_VALUE if conversion would
     *         negatively overflow, or Long.MAX_VALUE if it would positively
     *         overflow.
     */
    public long getTimeTruncated(TimeUnit unit) {
        return unit.convert(_timeInEpsilon, TimeOperations.getEpsilon());
    }

    /**
     * Returns a hash code value for the object. This method overrides
     * java.lang.Object.hashCode() to support efficient treatment in HashMaps.
     *
     * @return: int: A hash code value for this TimeInstant.
     */
    @Override
    public int hashCode() {
        return (int) (this._timeInEpsilon ^ (this._timeInEpsilon >>> 32));
    }

    /**
     * Sets the preferred time zone for printing this time instant. Note that this
     * time zone does not affect the internal representation of this TimeInstant
     * (stored as multiple of Experiment's epsilon since the start of the epoch,
     * i.e. January 1, 1970, 00:00:00 GMT); the preferred TimeZone just servers
     * TimeFormatters generating a convenient output.
     *
     * @param preferredTimeZone java.util.TimeZone : The time value of this
     *                          TimeInstant in the time unit of the reference time.
     */
    public void setPreferredTimeZone(TimeZone preferredTimeZone) {
        this._preferredTimeZone = preferredTimeZone;
    }

    /**
     * Returns the String Representation of this TimeInstant according to the
     * TimeFormatter.
     *
     * @see java.lang.Object#toString()
     * @see desmoj.core.simulator.TimeFormatter
     */
    @Override
    public String toString() {
        return TimeOperations.formatTimeInstant(this);
    }

    /**
     * Returns the String Representation of this TimeInstant according to the
     * TimeFormatter, truncating digits after the decimal point if necessary.
     *
     * @param digits Maximum number of digits after decimal point
     *
     * @see java.lang.Object#toString()
     * @see desmoj.core.simulator.TimeFormatter
     */
    public String toString(int digits) {

        String result = TimeOperations.formatTimeInstant(this);

        if (result.lastIndexOf(".") >= 0) {
            result = result.substring(0, Math.max(result.length() - 1, result.lastIndexOf(".") + digits));
        }
        return result;
    }
}
