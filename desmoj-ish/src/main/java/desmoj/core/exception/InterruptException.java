package desmoj.core.exception;

import desmoj.core.simulator.InterruptCode;

/**
 * InterruptExceptions can be caught when processes are interrupted. They
 * contain an {@link InterruptCode} which is used to provide the interrupted
 * processes with information about the reason for their interruption.
 *
 * @see InterruptCode InterruptCode
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Malte Unkrig
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
 */
public class InterruptException extends RuntimeException {
    private static final long serialVersionUID = 2298081652371353905L;

    /**
     * The {@link InterruptCode} transporting information about the reason for the
     * interrupt triggered by this InterruptException.
     */
    private final InterruptCode interruptCode;

    /**
     * Constructs an {@link InterruptException} containing the given
     * {@link InterruptCode}.
     *
     * @param interruptCode The InterruptCode which is to be transported inside of
     *                      this InterruptException.
     */
    public InterruptException(InterruptCode interruptCode) {
        this.interruptCode = interruptCode;
    }

    /**
     * Equals implementations which delegates to
     * {@link InterruptCode#equals(Object)}
     */
    @Override
    public boolean equals(Object obj) {
        return interruptCode.equals(obj);
    }

    /**
     * Returns the {@link InterruptCode} associated with this InterruptException.
     * The InterruptCode contains information on the reason for a process's
     * interruption.
     *
     * @return the InterruptCode
     */
    public InterruptCode getInterruptCode() {
        return interruptCode;
    }

    /**
     * HashCode implementations which delegates to {@link InterruptCode#hashCode()}
     */
    @Override
    public int hashCode() {
        return interruptCode.hashCode();
    }

}
