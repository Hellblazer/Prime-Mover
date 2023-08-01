package desmoj.core.dist;

import java.util.concurrent.TimeUnit;

import desmoj.core.simulator.TimeSpan;

/**
 * Superclass for all distributions returning (near-)continuous samples of type <code>Double</code>.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler, Johannes Goebel
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
public abstract class ContDist extends NumericalDist<Double> {

	/**
	 * Constructs a distribution returning continuously distributed double
	 * samples. Note that the method <code>Double sample()</code> returning the samples 
     * (inherited from <code>NumericalDist<N></code>) has to be
     * implemented in subclasses.
	 * 
	 * @param owner
	 *            Model : The distribution's owner
	 * @param name
	 *            java.lang.String : The distribution's name
	 * @param showInReport
	 *            boolean : Flag to show distribution in report
	 * @param showInTrace
	 *            boolean : Flag to show distribution in trace
	 */
	public ContDist(desmoj.core.simulator.Model owner, String name,
			boolean showInReport, boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);
	}

    /**
     * Convenience method to sample a period of random length by creating a
     * <code>TimeSpan</code> whose length is determined by sampling this
     * distribution (time unit is the experiment's reference units),
     * thus replacing the pattern <code>new TimeSpan(distribution.sample())</code>.
     * 
     * @return TimeSpan : The TimeSpan sampled from this distribution
     */
    public TimeSpan sampleTimeSpan() {
        
        if (!this.getModel().isConnected()) {
            sendWarning(
                    "Failed to sample a TimeSpan. Returning TimeSpan(0) instead.",
                    "ContDist: " + getName() + " Method: TimeSpan sampleTimeSpan()",
                    "Model not yet connected to an experiment, thus reference time unit and random number seeds not yet set.",
                    "Make sure your model is connected to an experiment before this method is called.");
            return new TimeSpan(0);
        }

        // Obtain a sample and return TimeSpan        
        double sample = sample();
        if (sample < 0) {
            sendWarning(
                    "Failed to sample a TimeSpan. Returning TimeSpan(0) instead.",
                    "ContDist: " + getName() + " Method: TimeSpan sampleTimeSpan()",
                    "The distribution returned a negative sample (" + sample + ").",
                    "Make sure to sample TimeSpans from non-negative distributions only.");
            return new TimeSpan(0);
        }
        
        return new TimeSpan(sample);
    }
    
    /**
     * Convenience method to sample a period of random length by creating a
     * <code>TimeSpan</code> whose length is determined by sampling this
     * distribution (time unit given explicitly as parameter), thus replacing 
     * the pattern <code>new TimeSpan(distribution.sample(), unit)</code>.
     * 
     * @param unit 
     *      TimeUnit: the TimeUnit to assign to the sampled value
     *      
     * @return TimeSpan : The TimeSpan sampled from this distribution
     */
    public TimeSpan sampleTimeSpan(TimeUnit unit) {
        
        if (unit == null) { // no time unit given
            throw (new desmoj.core.exception.SimAbortedException(
                    new desmoj.core.report.ErrorMessage(
                            null,
                            "Can't create TimeSpan object! Simulation aborted.",
                            "ContDist: " + getName() + " Method: TimeSpan sampleTimeSpan(TimeUnit unit)",
                            "Time unit passed is null",
                            "Make sure to pass a non-null time unit.",
                            null)));
        }   
        
        if (!this.getModel().isConnected()) {
            sendWarning(
                    "Failed to sample a TimeSpan. Returning TimeSpan(0) instead.",
                    "ContDist: " + getName() + " Method: TimeSpan sampleTimeSpan(TimeUnit unit)",
                    "Model not yet connected to an experiment, thus random number seeds not yet set.",
                    "Make sure your model is connected to an experiment before this method is called.");
            return new TimeSpan(0);
        }
        
        // Obtain a sample 
        double sample = sample();
        if (sample < 0) {
            sendWarning(
                    "Failed to sample a TimeSpan. Returning TimeSpan(0) instead.",
                    "ContDist: " + getName() + " Method: TimeSpan sampleTimeSpan(TimeUnit unit)",
                    "The distribution returned a negative sample (" + sample + ").",
                    "Make sure to sample TimeSpans from non-negative distributions only.");
            return new TimeSpan(0);
        }
        
        // Return a TimeSpan using the appropriate constructor
        return new TimeSpan(sample, unit);
    }
}