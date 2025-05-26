package desmoj.core.simulator;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is the default Time Formatter. Use this class to format TimeSpan and TimeInstant objects like in the following
 * examples: 5.1 , 5.1 SECONDS, 12.035, 12.035 MINUTES.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Felix Klueckmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

/**
 * @author goebel
 *
 */
public class SingleUnitTimeFormatter implements TimeFormatter {
    private static Map<TimeUnit, Map<TimeUnit, Long>> timeConstants = new EnumMap<>(TimeUnit.class);

    static {

        Map<TimeUnit, Long> dayConstants = new EnumMap<>(TimeUnit.class);
        dayConstants.put(TimeUnit.HOURS, 24l);
        dayConstants.put(TimeUnit.MINUTES, 1440l);
        dayConstants.put(TimeUnit.SECONDS, 86400l);
        dayConstants.put(TimeUnit.MILLISECONDS, 86400000l);
        dayConstants.put(TimeUnit.MICROSECONDS, 86400000000l);
        dayConstants.put(TimeUnit.NANOSECONDS, 86400000000000l);
        Map<TimeUnit, Long> hourConstants = new EnumMap<>(TimeUnit.class);
        hourConstants.put(TimeUnit.MINUTES, 60l);
        hourConstants.put(TimeUnit.SECONDS, 3600l);
        hourConstants.put(TimeUnit.MILLISECONDS, 3600000l);
        hourConstants.put(TimeUnit.MICROSECONDS, 3600000000l);
        hourConstants.put(TimeUnit.NANOSECONDS, 3600000000000l);
        Map<TimeUnit, Long> minuteConstants = new EnumMap<>(TimeUnit.class);
        minuteConstants.put(TimeUnit.SECONDS, 60l);
        minuteConstants.put(TimeUnit.MILLISECONDS, 60000l);
        minuteConstants.put(TimeUnit.MICROSECONDS, 60000000l);
        minuteConstants.put(TimeUnit.NANOSECONDS, 60000000000l);
        Map<TimeUnit, Long> secondConstants = new EnumMap<>(TimeUnit.class);
        secondConstants.put(TimeUnit.MILLISECONDS, 1000l);
        secondConstants.put(TimeUnit.MICROSECONDS, 1000000l);
        secondConstants.put(TimeUnit.NANOSECONDS, 1000000000l);
        Map<TimeUnit, Long> milliConstants = new EnumMap<>(TimeUnit.class);
        milliConstants.put(TimeUnit.MICROSECONDS, 1000l);
        milliConstants.put(TimeUnit.NANOSECONDS, 1000000l);
        Map<TimeUnit, Long> microConstants = new EnumMap<>(TimeUnit.class);
        microConstants.put(TimeUnit.NANOSECONDS, 1000l);

        timeConstants.put(TimeUnit.DAYS, dayConstants);
        timeConstants.put(TimeUnit.HOURS, hourConstants);
        timeConstants.put(TimeUnit.MINUTES, minuteConstants);
        timeConstants.put(TimeUnit.SECONDS, secondConstants);
        timeConstants.put(TimeUnit.MILLISECONDS, milliConstants);
        timeConstants.put(TimeUnit.MICROSECONDS, microConstants);

    }

    /**
     * The number of digits after the decimal point which will be displayed for the
     * time values.
     */
    protected final long     _floats;
    /**
     * the value of epsilon used in this TimeFormatter
     */
    private final   TimeUnit _epsilon;
    /**
     * A Factor used for unit conversion
     */
    private final   long     _myFactor;
    /**
     * The TimeUnit that is used in this TimeFormatter
     */
    private final   TimeUnit _myTimeUnit;

    /**
     *
     */
    private final long _precisionFactor;

    /**
     * Flag that indicates if the time unit will be included in the time String.
     *
     */
    private final boolean _writeUnit;

    /**
     * Constructs a DecimalTimeFormatter.
     *
     * @param unit      java.util.concurrent.TimeUnit : The time values that will be
     *                  used
     * @param floats    int : The number of floating point digits to print
     * @param writeUnit boolean : Indicates if the time unit will be included in the
     *                  time String.
     */
    public SingleUnitTimeFormatter(TimeUnit unit, TimeUnit epsilon, int floats, boolean writeUnit) {
        this._myTimeUnit = unit;
        this._epsilon = epsilon;
        this._floats = floats;
        this._precisionFactor = (long) java.lang.Math.pow(10, floats);
        this._writeUnit = writeUnit;
        this._myFactor = epsilon.convert(1, _myTimeUnit);
    }

    /**
     * Returns the String-Representation of the given TimeInstant
     *
     */
    @Override
    public String buildTimeString(TimeInstant instant) {
        return buildSingleUnitTimeString(instant.getTimeInEpsilon());
    }

    /**
     * Returns the String-Representation of the given TimeSpan
     *
     */
    @Override
    public String buildTimeString(TimeSpan span) {
        return buildSingleUnitTimeString(span.getTimeInEpsilon());
    }

    /*
     * (non-Javadoc)
     *
     * @see desmoj.core.simulator.TimeFormatter#usesOnlySingleUnit()
     */
    @Override
    public String getUnit() {
        return _myTimeUnit.toString().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Returns the String-Representation of the given time object.
     *
     */
    private String buildSingleUnitTimeString(long timeValue) {
        StringBuffer timeStringBuffer = new StringBuffer();

        if (_myTimeUnit.compareTo(_epsilon) > 0) {
            // unit is a coarser granularity than epsilon
            timeStringBuffer.append('.');
            // append seperator
            long fractionTime = _myTimeUnit.convert((timeValue % _myFactor) * _precisionFactor, _epsilon);

            String fractionTimeString = Long.toString(fractionTime);
            char zero = '0';
            // append as many zeros as needed
            for (int i = fractionTimeString.length(); i < _floats; i++) {
                timeStringBuffer.append(zero);
            }

            timeStringBuffer.append(fractionTimeString);

        }
        timeStringBuffer.insert(0, Long.toString(_myTimeUnit.convert(timeValue, _epsilon)));
        if (_writeUnit) {
            // append the name of the time unit
            timeStringBuffer.append(' ');
            timeStringBuffer.append(_myTimeUnit.name());
        }
        return timeStringBuffer.toString();
    }
}
