package desmoj.core.simulator;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.TimeUnit;

/**
 * Represents spans of simulation time. Each span of simulation time is
 * represented by an individual object of this class and offers its own methods
 * for arithmetic operations and comparison. Ensures that only valid spans of
 * time are generated.
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
public final class TimeSpan implements Comparable<TimeSpan> {

	/**
	 *Use the Builder to create TimeSpans specified as the sum of durations
	 * with different TimeUnits Example (timeSpan with the length of one hour
	 * and 30 minutes): new TimeSpan.Builder().hours(1).minutes(30).build();
	 * 
	 */
	public static class Builder {
		private long durationInEpsilon;

		public Builder() {
			durationInEpsilon = 0L;
		}

		/**
		 * Use this method to create TimeSpan objects with the builder pattern.
		 * 
		 */
		public TimeSpan build() {
			return new TimeSpan(this);
		}

		public Builder days(long days) {
			if (days < 0) { // points of time must be postive
				throw (new desmoj.core.exception.SimAbortedException(
						new desmoj.core.report.ErrorMessage(
								null,
								"Can't set the value of days of the Builder for TimeSpan object! Simulation aborted.",
								"Class : TimeSpan  Builder : Builder.days(long)",
								"the value passed for the setting of days is negative : "
										+ days,
								"Negative values for simulation time are illegal.",
								null)));
			}
			durationInEpsilon += TimeOperations.getEpsilon()
					.convert(days, DAYS);
			return this;
		}

		public Builder hours(long hours) {
			if (hours < 0) { // points of time must be postive
				throw (new desmoj.core.exception.SimAbortedException(
						new desmoj.core.report.ErrorMessage(
								null,
								"Can't set the value of hours of the Builder for TimeSpan object! Simulation aborted.",
								"Class : TimeSpan  Builder : Builder.hours(long)",
								"the value passed for the setting of hours is negative : "
										+ hours,
								"Negative values for simulation time are illegal.",
								null)));
			}
			durationInEpsilon += TimeOperations.getEpsilon().convert(hours,
					HOURS);
			return this;
		}

		public Builder microseconds(long microseconds) {
			if (microseconds < 0) { // points of time must be positive
				throw (new desmoj.core.exception.SimAbortedException(
						new desmoj.core.report.ErrorMessage(
								null,
								"Can't set the value of microseconds of the Builder for TimeSpan object! Simulation aborted.",
								"Class : TimeSpan  Builder : Builder.microseconds(long)",
								"the value passed for the setting of microseconds is negative : "
										+ microseconds,
								"Negative values for simulation time are illegal.",
								null)));
			}
			durationInEpsilon += TimeOperations.getEpsilon().convert(
					microseconds, MICROSECONDS);
			return this;
		}

		public Builder milliseconds(long milliseconds) {
			if (milliseconds < 0) { // points of time must be postive
				throw (new desmoj.core.exception.SimAbortedException(
						new desmoj.core.report.ErrorMessage(
								null,
								"Can't set the value of milliseconds of the Builder for TimeSpan object! Simulation aborted.",
								"Class : TimeSpan  Builder : Builder.milliseconds(long)",
								"the value passed for the setting of milliseconds is negative : "
										+ milliseconds,
								"Negative values for simulation time are illegal.",
								null)));
			}
			durationInEpsilon += TimeOperations.getEpsilon().convert(
					milliseconds, MILLISECONDS);
			return this;
		}

		public Builder minutes(long minutes) {
			if (minutes < 0) { // points of time must be postive
				throw (new desmoj.core.exception.SimAbortedException(
						new desmoj.core.report.ErrorMessage(
								null,
								"Can't set the value of minutes of the Builder for TimeSpan object! Simulation aborted.",
								"Class : TimeSpan  Builder : Builder.minutes(long)",
								"the value passed for the setting of minutes is negative : "
										+ minutes,
								"Negative values for simulation time are illegal.",
								null)));
			}
			durationInEpsilon += TimeOperations.getEpsilon().convert(minutes,
					MINUTES);
			return this;
		}

		public Builder nanoseconds(long nanoseconds) {
			if (nanoseconds < 0) { // points of time must be postive
				throw (new desmoj.core.exception.SimAbortedException(
						new desmoj.core.report.ErrorMessage(
								null,
								"Can't set the value of nanoseconds of the Builder for TimeSpan object! Simulation aborted.",
								"Class : TimeSpan  Builder : Builder.nanoseconds(long)",
								"the value passed for the setting of nanoseconds is negative : "
										+ nanoseconds,
								"Negative values for simulation time are illegal.",
								null)));
			}
			durationInEpsilon += TimeOperations.getEpsilon().convert(
					nanoseconds, NANOSECONDS);
			return this;
		}
		public Builder seconds(long seconds) {
			if (seconds < 0) { // points of time must be postive
				throw (new desmoj.core.exception.SimAbortedException(
						new desmoj.core.report.ErrorMessage(
								null,
								"Can't set the value of seconds of the Builder for TimeSpan object! Simulation aborted.",
								"Class : TimeSpan  Builder : Builder.seconds(long)",
								"the value passed for the setting of seconds is negative : "
										+ seconds,
								"Negative values for simulation time are illegal.",
								null)));
			}
			durationInEpsilon += TimeOperations.getEpsilon().convert(seconds,
					SECONDS);
			return this;
		}
	}

	/**
	 * Indicates whether TimeSpan a is equal to TimeSpan b, i.e. they are of
	 * equal length.
	 * 
	 * @param a TimeSpan: first comparand
	 * @param b TimeSpan: second comparand
	 * @return true if a is equal to b; false otherwise.
	 */
	public static boolean isEqual(TimeSpan a, TimeSpan b) {
		return (a._durationInEpsilon == b._durationInEpsilon);
	}

	/**
	 * Checks if the first of two spans of simulation time is longer than the
	 * second. Note that this is a static method available through calling the
	 * class <code>TimeSpan</code> i.e. <code>TimeSpan.isLonger(a,b)</code>
	 * where a and b are valid TimeSpan objects.
	 * 
	 * @return boolean : True if a is longer than b
	 * @param a
	 *            TimeSpan : first comparand
	 * @param b
	 *            TimeSpan : second comparand
	 */
	public static boolean isLonger(TimeSpan a, TimeSpan b) {
		return (a._durationInEpsilon > b._durationInEpsilon);
	}

	/**
	 * Checks if the first of two spans of simulation time is longer than the
	 * second or equal to the second. Note that this is a static method
	 * available through calling the class <code>TimeSpan</code> i.e.
	 * <code>TimeSpan.isLonger(a,b)</code> where a and b are valid TimeSpan
	 * objects.
	 * 
	 * @return boolean : True if a is longer than b or equal to b.
	 * @param a
	 *            TimeSpan : first comparand
	 * @param b
	 *            TimeSpan : second comparand
	 */
	public static boolean isLongerOrEqual(TimeSpan a, TimeSpan b) {
		return (isLonger(a, b) || isEqual(a, b));
	}

	/**
	 * Checks if the first of two spans of simulation time is shorter than the
	 * second. Note that this is a static method available through calling the
	 * class <code>TimeSpan</code> i.e. <code>TimeSpan.isShorter(a,b)</code>
	 * where a and b are valid TimeSpan objects.
	 * 
	 * @return boolean : True if a is shorter than b
	 * @param a
	 *            TimeSpan : first comparand
	 * @param b
	 *            TimeSpan : second comparand
	 */
	public static boolean isShorter(TimeSpan a, TimeSpan b) {
		return (a._durationInEpsilon < b._durationInEpsilon);
	}

	/**
	 * Checks if the first of two spans of simulation time is shorter than the
	 * second or equal to the second. Note that this is a static method
	 * available through calling the class <code>TimeSpan</code> i.e.
	 * <code>TimeSpan.isShorterOrEqual(a,b)</code> where a and b are valid
	 * TimeSpan objects.
	 * 
	 * @return boolean : True if a is shorter than b or equal to b.
	 * @param a
	 *            TimeSpan : first comparand
	 * @param b
	 *            TimeSpan : second comparand
	 */
	public static boolean isShorterOrEqual(TimeSpan a, TimeSpan b) {
		return (isShorter(a, b) || isEqual(a, b));
	}

	/**
	 * The span of time in the unit of epsilon
	 */
	private final long _durationInEpsilon;

	/**
	 * Constructs a TimeSpan object with the given time value in the time unit
	 * of the reference time. It represents a time span in simulation time. Note
	 * that trying to create a TimeSpan object with a negative value will stop
	 * the simulation immediately.The simulation will also stop immediately if
	 * the TimeSpan is larger than Long.MAX_VALUE-1 (in the unit of epsilon).
	 * 
	 * @param duration
	 *            double : The time value of this TimeSpan in the time unit of
	 *            the reference time.
	 */
	public TimeSpan(double duration) {
	    this(duration, TimeOperations.getReferenceUnit());
	}

	/**
	 * Constructs a TimeSpan object with the given time value in the given
	 * timeUnit. It represents a time span in simulation time. Note that trying
	 * to create a TimeSpan object with a negative value will stop the
	 * simulation immediately. The simulation will also stop immediately if the
	 * TimeSpan is larger than Long.MAX_VALUE-1 (in the unit of epsilon).
	 * 
	 * @param duration
	 *            double : The time value of this TimeSpan in the time unit of
	 *            the reference time.
	 * @param unit
	 *            TimeUnit : the time unit
	 */
	public TimeSpan(double duration, TimeUnit unit) {
	    
        if (unit == null) { // no time unit given
            throw (new desmoj.core.exception.SimAbortedException(
                    new desmoj.core.report.ErrorMessage(
                            null,
                            "Can't create TimeSpan object! Simulation aborted.",
                            "Class : TimeSpan  Constructor : TimeSpan(double, TimeUnit)",
                            "Time unit passed is null",
                            "Make sure to pass a non-null time unit. \nNote that before " +
                              "connecting model and experiment, TimeSpans must explicitly\n" +
                              "refer to a time unit as the reference unit is not yet defined," +
                              "e.g. use \nTimeSpan(double time, TimeUnit unit) instead of" +
                              "TimeInstant(double time).",
                            null)));
        }         
        _durationInEpsilon = (long) (duration * TimeOperations.getEpsilon().convert(1, unit));
        TimeOperations.timeObjectsCreated = true;
        
        if (_durationInEpsilon < 0) { // points of time must be postive
            throw (new desmoj.core.exception.SimAbortedException(
                    new desmoj.core.report.ErrorMessage(
                            null,
                            "Can't create TimeSpan object! Simulation aborted.",
                            "Class : TimeSpan  Constructor : TimeSpan(long, TimeUnit)",
                            "the value passed for instantiation is negative : "
                                    + _durationInEpsilon,
                            "Negative values for simulation time are illegal.",
                            null)));
        }
        if (_durationInEpsilon == Long.MAX_VALUE) {
            /*The timeSpan is too big. 
            (The method TimeUnit.convert(duration,unit)returns Long.MAX_VALUE if
            the result of the conversion is to big*/
            
            throw (new desmoj.core.exception.SimAbortedException(
                    new desmoj.core.report.ErrorMessage(
                            null,
                            "Can't create TimeSpan object! Simulation aborted.",
                            "Class : TimeSpan  Constructor : TimeSpan(long,TimeUnit)",
                            "the TimeSpan is too big. ",
                            "Can only create TimeSpan objects which are smaller than Long.MAX_VALUE (in the TimeUnit of epsilon).",
                            null)));
        }
	}
	
	/**
	 * Constructs a TimeSpan object with the given time value in the time unit
	 * of the reference time. It represents a time span in simulation time. Note
	 * that trying to create a TimeSpan object with a negative value will stop
	 * the simulation immediately. The simulation will also stop immediately if
	 * the TimeSpan is larger than Long.MAX_VALUE-1 (in the unit of epsilon).
	 * 
	 * @param duration
	 *            long : The time value of this TimeSpan in the time unit of the
	 *            reference time.
	 */
	public TimeSpan(long duration) {
		this(duration, TimeOperations.getReferenceUnit());
	}
	
	/**
	 * Constructs a TimeSpan object with the given time value in the time unit
	 * of the given parameter. It represents a time span in simulation time.
	 * Note that trying to create a TimeSpan object with a negative value will
	 * stop the simulation immediately. The simulation will also stop
	 * immediately if the TimeSpan is larger than Long.MAX_VALUE-1 (in the unit
	 * of epsilon).
	 * 
	 * @param duration
	 *            long : The time value of this TimeSpan 
	 * @param unit
	 *            TimeUnit: the TimeUnit
	 */
	public TimeSpan(long duration, TimeUnit unit) {
        if (unit == null) { // no time unit given
            throw (new desmoj.core.exception.SimAbortedException(
                    new desmoj.core.report.ErrorMessage(
                            null,
                            "Can't create TimeSpan object! Simulation aborted.",
                            "Class : TimeSpan  Constructor : TimeSpan(long, TimeUnit)",
                            "Time unit passed is null",
                            "Make sure to pass a non-null time unit. \nNote that before " +
                              "connecting model and experiment, TimeSpans must explicitly\n" +
                              "refer to a time unit as the reference unit is not yet defined," +
                              "e.g. use \nTimeSpan(long time, TimeUnit unit) instead of" +
                              "TimeInstant(long time).",
                            null)));
        } 	    
		if (duration < 0) { // points of time must be postive
			throw (new desmoj.core.exception.SimAbortedException(
					new desmoj.core.report.ErrorMessage(
							null,
							"Can't create TimeSpan object! Simulation aborted.",
							"Class : TimeSpan  Constructor : TimeSpan(long, TimeUnit)",
							"the value passed for instantiation is negative : "
									+ duration,
							"Negative values for simulation time are illegal.",
							null)));
		}
		_durationInEpsilon = TimeOperations.getEpsilon().convert(duration, unit);
		TimeOperations.timeObjectsCreated = true;
		if (_durationInEpsilon == Long.MAX_VALUE) {
			/*The timeSpan is too big. 
			(The method TimeUnit.convert(duration,unit)returns Long.MAX_VALUE if
			the result of the conversion is to big*/
			
			throw (new desmoj.core.exception.SimAbortedException(
					new desmoj.core.report.ErrorMessage(
							null,
							"Can't create TimeSpan object! Simulation aborted.",
							"Class : TimeSpan  Constructor : TimeSpan(long,TimeUnit)",
							"the TimeSpan is too big. ",
							"Can only create TimeSpan objects which are smaller than Long.MAX_VALUE (in the TimeUnit of epsilon).",
							null)));
		}
	}
	
	/**
	 * private constructor for the Builder pattern
	 */
	private TimeSpan(Builder builder) {
		_durationInEpsilon = builder.durationInEpsilon;
	}
	
	/**
	 * Compares the given TimeSpan to this TimeSpan. This method implements the
	 * Comparable<TimeSpan> Interface
	 * 
	 * @param anotherTimeSpan The TimeSpan to be compared to this TimeSpan
	 * 
	 * @return int: Returns a negative integer, zero, or a positive integer as
	 *          this TimeSpan is shorter than, equal to, or longer than the
	 *          given parameter.
	 */
	public int compareTo(TimeSpan anotherTimeSpan) {
		long difference = this.getTimeInEpsilon()
				- anotherTimeSpan.getTimeInEpsilon();
		// if the given parameter is longer than this return -1
		if (difference < 0)
			return -1;
		// if the given parameter is shorter than this return 1
		if (difference > 0)
			return 1;
		// if they are equal return 0
		return 0;
	}
	
	/**
	 * Indicates whether this TimeSpan is equal to the given parameter. Returns
	 * true if the obj argument is a TimeSpan and is of equal length as this
	 * TimeSpan; false otherwise. This method overrides
	 * java.lang.Object.equals()
	 * 
	 * @param obj the reference object with which to compare.
	 * @return true if the obj argument is a TimeSpan and is of equal length as
	 *          this TimeSpan; false otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TimeSpan))
			return false;
		TimeSpan span = (TimeSpan) obj;
		return isEqual(this, span);
	}

	
	/**
	 * Returns the value of this TimeSpan object as a double type in the time unit
	 * of the reference time. 
	 * 
	 * @return double: the time value of the TimeSpan object as a double type in the
	 *         time unit given as a parameter
	 */
	public double getTimeAsDouble() {
		return getTimeAsDouble(TimeOperations.getReferenceUnit());
	}

	/**
	 * Returns the value of this TimeSpan object as a double type in the time unit
	 * given as a parameter. 
	 * 
	 * @return double: the time value of the TimeSpan object as a double type in the
	 *         time unit given as a parameter
	 */
	public double getTimeAsDouble(TimeUnit unit) {
		return _durationInEpsilon
				/ (double) TimeOperations.getEpsilon().convert(1, unit);
	}

	/**
	 * Returns the value of the TimeSpan object as a long type in the time unit
	 * of epsilon
	 * 
	 * @return long: the time value of the TimeSpan object as a long type in the
	 *         time unit of epsilon
	 */
	public long getTimeInEpsilon() {
		return _durationInEpsilon;
	}

	/**
	 * Returns the value of this TimeSpan object as a long type in the time unit
	 * of the reference time. If the parameter has a coarser granularity than
	 * epsilon the returned value will be rounded, so lose precision.
	 *  
	 * 
	 * @return long: the time value of the TimeSpan object as a long type in the
	 *         time unit given as a parameter or Long.MIN_VALUE if conversion
	 *         would negatively overflow, or Long.MAX_VALUE if it would
	 *         positively overflow.
	 */
	public long getTimeRounded() {
		return getTimeRounded(TimeOperations.getReferenceUnit());
	}

	/**
	 * Returns the value of this TimeSpan object as a long type in the time unit
	 * given as a parameter. If the parameter has a coarser granularity than
	 * epsilon the returned value will be rounded, so lose precision.
	 * 
	 * @param unit
	 *            : the TimeUnit
	 * 
	 * @return long: the time value of the TimeSpan object as a long type in the
	 *         time unit given as a parameter or Long.MIN_VALUE if conversion
	 *         would negatively overflow, or Long.MAX_VALUE if it would
	 *         positively overflow.
	 */
	public long getTimeRounded(TimeUnit unit) {		
		if(unit.compareTo(TimeOperations.getEpsilon())>0){
			//unit has a coarser granularity than epsilon
			long halfAUnitInEpsilon = TimeOperations.getEpsilon().convert(1, unit) / 2;
			long durationInUnitTruncated = getTimeTruncated(unit);
			long difference = _durationInEpsilon
					- TimeOperations.getEpsilon().convert(durationInUnitTruncated,
							unit);
			// if the time value in the unit Epsilon is bigger than
			if (difference >= halfAUnitInEpsilon) {
				return durationInUnitTruncated + 1;
			}
			return durationInUnitTruncated;
		}
		else {
			//unit has a finer granularity or is equal than epsilon
			return getTimeTruncated(unit);
		}
		
	}

	/**
	 * Returns the value of this TimeSpan object as a long type in the time unit
	 * of the reference time. If the parameter has a coarser granularity than
	 * epsilon the returned value will be truncated, so lose precision.
	 *  
	 * 
	 * @return long: the time value of the TimeSpan object as a long type in the
	 *         time unit given as a parameter or Long.MIN_VALUE if conversion
	 *         would negatively overflow, or Long.MAX_VALUE if it would
	 *         positively overflow.
	 */
	public long getTimeTruncated() {
		return getTimeTruncated(TimeOperations.getReferenceUnit());
	}
	
	/**
	 * Returns the value of this TimeSpan object as a long type in the time unit
	 * given as a parameter. If the parameter has a coarser granularity than
	 * epsilon the returned value will be truncated, so lose precision.
	 * 
	 * @param unit
	 *            : the TimeUnit
	 * 
	 * @return long: the time value of the TimeSpan object as a long type in the
	 *         time unit given as a parameter or Long.MIN_VALUE if conversion
	 *         would negatively overflow, or Long.MAX_VALUE if it would
	 *         positively overflow.
	 */
	public long getTimeTruncated(TimeUnit unit) {
		return unit.convert(_durationInEpsilon, TimeOperations.getEpsilon());
	}

	/**
	 * Returns a hash code value for the object. This methode overides
	 * java.lang.Object.hashCode().The method is supported for the benefit of
	 * hashtables such as those provided by java.util.Hashtable.
	 * 
	 * @return int: a hash code value for this TimeSpan.
	 */
	@Override
	public int hashCode() {
		return (int) (this._durationInEpsilon ^ (this._durationInEpsilon >>> 32));
	}

	/**
     * Indicates whether this TimeSpan is equal to 0.
     * 
     * @return true if this TimeSpan has a length of 0; false otherwise.
     */
    public boolean isZero() {
        return _durationInEpsilon == 0l;
    }

	/**Returns the String Representation of this TimeSpan.
	 *  
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return TimeOperations.formatTimeSpan(this);
	}
	
    /**
     * Returns the String Representation of this TimeSpan according to the
     * TimeFormatter, truncating digits after the decimal point if necessary.
     * 
     * @param digits Maximum number of digits after decimal point 
     * 
     * @see java.lang.Object#toString()
     * @see desmoj.core.simulator.TimeFormatter
     */
    public String toString(int digits) {
        
        String result = TimeOperations.formatTimeSpan(this);
        
        if (result.lastIndexOf(".") >= 0) {
            result = result.substring(0, Math.max(result.length()-1, result.lastIndexOf(".") + digits)); 
        }
        return result;
    }
}
