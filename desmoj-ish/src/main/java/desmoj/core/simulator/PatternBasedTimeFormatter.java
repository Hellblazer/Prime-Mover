package desmoj.core.simulator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Use this class to format TimeInstant objects using a java.text.DateFormat object.
 * The PatternBasedTimeFormatter uses a MultiUnitTimeFormatter to format TimeSpans.
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
public class PatternBasedTimeFormatter implements TimeFormatter {
	/**
	 * The DateFormat used by this TimeFormatter
	 */
	private DateFormat _myDateFormat;
	private MultiUnitTimeFormatter _myMultiUnitTimeFormatter;

	/**
	 * Constructor for a GeneralTimeFormatter. This shortcut constructor uses the
	 * pattern "dd.MM.yyyy HH:mm:ss:SSS" (no time zone output). Default Locale is Germany.
	 */
	public PatternBasedTimeFormatter() {
		this(false);
	}
	
    /**
     * Constructor for a GeneralTimeFormatter. This shortcut constructor uses the
     * pattern "dd.MM.yyyy HH:mm:ss:SSS", followed by a five digits time zone
     * identifier (e.g. "-0800"), if requested. Default Locale is Germany.
     * 
     * @param timeZoneIncluded
     *            boolean : inclusion of time zone identifiers (true) or not (false).
     */
    public PatternBasedTimeFormatter(boolean timeZoneIncluded) {
        this(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS" + (timeZoneIncluded ? " Z" : ""), Locale.GERMANY),TimeUnit.DAYS, TimeUnit.MILLISECONDS);
    }

	/**
	 * Constructor for a GeneralTimeFormatter.
	 * 
	 * @param dateFormat
	 *            DateFormat : the dateFormat used by this TimeFormatter.
	 * @param coarsestUnit
	 *          		TimeUnit: The coarsest Unit for the TimeSpan
	 * @param finestUnit
	 *   				TimeUnit: The finest Unit for the TimeSpan
	 */
	public PatternBasedTimeFormatter(DateFormat dateFormat, TimeUnit coarsestUnit, TimeUnit finestUnit ) {
		this._myDateFormat = dateFormat;
		this._myMultiUnitTimeFormatter = new MultiUnitTimeFormatter(coarsestUnit, finestUnit);
	}

	/**
	 * Constructor for a GeneralTimeFormatter. Default Locale is Germany.
	 * 
	 * @param pattern
	 *            String : the pattern used by this UTCTimeFormatter to format
	 *            TimeSpan and TimeInstant objects. See 
	 *            http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html
	 *            for a description of the syntax of such patterns. 
	 *             @param coarsestUnit
	 *          		TimeUnit: The coarsest Unit for the TimeSpan
	 * @param finestUnit
	 *   				TimeUnit: The finest Unit for the TimeSpan
	 */
	public PatternBasedTimeFormatter(String pattern, TimeUnit coarsestUnit, TimeUnit finestUnit ) {
		this(new SimpleDateFormat(pattern, Locale.GERMANY), coarsestUnit, finestUnit);
	}
	
	/**
	 * Returns the String-Representation of the given TimeInstant.
	 */
	public String buildTimeString(TimeInstant instant) {
	    _myDateFormat.setTimeZone(instant.getPreferredTimeZone());
		return _myDateFormat.format(new Date(instant.getTimeTruncated(TimeUnit.MILLISECONDS))) + " " + instant.getPreferredTimeZone().getID();
	}
	
	/**
	 * Returns the String-Representation of the given TimeSpan.
	 */
	public String buildTimeString(TimeSpan span) {
		return _myMultiUnitTimeFormatter.buildTimeString(span);
	}
	
    /* (non-Javadoc)
     * @see desmoj.core.simulator.TimeFormatter#usesOnlySingleUnit()
     */
    public String getUnit() {
        return _myMultiUnitTimeFormatter.getUnit();
    }
}
