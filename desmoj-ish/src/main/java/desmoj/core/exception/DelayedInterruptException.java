package desmoj.core.exception;

import desmoj.core.simulator.InterruptCode;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * If a DelayedInterruptException is caught in the
 * {@link SimProcess#lifeCycle()} method of a process this means that the
 * process has been interrupted from "within" due to a previously scheduled
 * delayed interrupt. DelayedInterruptExceptions are used
 * in with {@link SimProcess#interruptDelayed(TimeInstant)} and
 * {@link SimProcess#interruptDelayed(TimeSpan)}.<br />
 * 
 * @see InterruptException
 * @see InterruptCode
 * @see SimProcess#interruptDelayed(TimeSpan)
 * @see SimProcess#interruptDelayed(TimeInstant)
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Malte Unkrig
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
 */
public class DelayedInterruptException extends InterruptException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a DelayedInterruptException with the given InterruptCode.
	 * 
	 * @param interruptCode
	 *            The InterruptCode to be contained within this
	 *            DelayedInterruptException.
	 */
	public DelayedInterruptException(InterruptCode interruptCode) {
		super(interruptCode);
	}
}
