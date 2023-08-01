package desmoj.core.dist;

import java.util.concurrent.TimeUnit;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

/**
 * Base class for all distributions that produce numbers.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
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
public abstract class NumericalDist<N extends Number> extends Distribution {

	public NumericalDist(Model owner, String name, boolean showInReport,
			boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);
	}

	/**
     * Abstract method to map a double <code>p</code> from 0...1 to the 
     * distribution's domain by determining the value x that satisfies
     * <code>P(X &lt; x) = p</code>.
     * 
     * @param p double: A value between 0 and 1
     * 
     * @return N : The value x that satisfies <code>P(X &lt; x) = p</code>
     */
    public abstract N getInverseOfCumulativeProbabilityFunction(double p);
	
	/**
	 * Method to return the specific sample as a value of type N.
	 * Default implementation calls <code>getInverseCumulativeProbability()</code>,
	 * to which a random number from <code>randomGenerator</code> is passed.
	 * Subclasses may need to refine this method if <code>getInverseCumulativeProbability()</code>
	 * is not implemented as the distribution does not support inverse transformation.
	 * 
	 * @return N : The sample of type N to be drawn from this distribution
	 */
	public N sample() {
	    
	    this.incrementObservations();
	    
	    N sample;
	    int attempts = 0;
	    
	    do {
	    
    	    sample = this.antithetic ? 
    	            getInverseOfCumulativeProbabilityFunction(1 - randomGenerator.nextDouble()) :
    	                getInverseOfCumulativeProbabilityFunction(randomGenerator.nextDouble());
            attempts++;
    	            
    	            
	    } while (nonNegative && sample.doubleValue() < 0 && attempts < 100);
	    
	    if (nonNegative && sample.doubleValue() < 0) {
	        sendWarning(
                    "NumericalDist that was set non-negative faild to sample a non-negative value." + 
	                    "Returning a negative result.",
                    "NumericalDist: " + this.getName()
                            + " Method: public void sample()",
                    "100 Attempts of sampling the distribution yielded a negative value.",
                    "Make sure the range of the NumericalDist is at least partially non-negative.");
	    }
	    
	    
	    if (this.currentlySendTraceNotes()) {
	        this.sendTraceNote("samples " + sample + " from " + this);
	    }
	                
	    if (this.currentlySendDebugNotes())
	           this.traceLastSample(sample+"");
	    
	    return sample;
	}
    
	/**
     * Convenience method to return the distribution's sample as <code>Object</code>.
     * For type safety, method <code>sample()</code> should be preferred. However, 
     * this method is useful for environments requiring a non-genetic access point 
     * to obtain samples from any distribution. 
     * 
     * @return Object : A sample from this this distribution wrapped as <code>Object</code>.
     */
    public Object sampleObject() {
        return sample();
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
                    "NumericalDist: " + getName() + " Method: TimeSpan sampleTimeSpan()",
                    "Model not yet connected to an experiment, thus reference time unit and random number seeds not yet set.",
                    "Make sure your model is connected to an experiment before this method is called.");
            return new TimeSpan(0);
        }
        
        // Obtain a sample 
        N sample = this.sample();
        if (sample.doubleValue() < 0) {
            sendWarning(
                    "Failed to sample a TimeSpan. Returning TimeSpan(0) instead.",
                    "NumericalDist: " + getName() + " Method: TimeSpan sampleTimeSpan()",
                    "The distribution returned a negative sample (" + sample.doubleValue() + ").",
                    "Make sure to sample TimeSpans from non-negative distributions only.");
            return new TimeSpan(0);
        }
        
        // Return a TimeSpan using the appropriate constructor
        if (sample instanceof Double || sample instanceof Float) {
            return new TimeSpan(sample.doubleValue());
        } else {
            return new TimeSpan(sample.longValue());
        }
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
                            "NumericalDist: " + getName() + " Method: TimeSpan sampleTimeSpan(TimeUnit unit)",
                            "Time unit passed is null",
                            "Make sure to pass a non-null time unit.",
                            null)));
        }   
        
        if (!this.getModel().isConnected()) {
            sendWarning(
                    "Failed to sample a TimeSpan. Returning TimeSpan(0) instead.",
                    "NumericalDist: " + getName() + " Method: TimeSpan sampleTimeSpan(TimeUnit unit)",
                    "Model not yet connected to an experiment, thus random number seeds not yet set.",
                    "Make sure your model is connected to an experiment before this method is called.");
            return new TimeSpan(0);
        }
        
        // Obtain a sample 
        N sample = this.sample();
        if (sample.doubleValue() < 0) {
            sendWarning(
                    "Failed to sample a TimeSpan. Returning TimeSpan(0) instead.",
                    "NumericalDist: " + getName() + " Method: TimeSpan sampleTimeSpan(TimeUnit unit)",
                    "The distribution returned a negative sample (" + sample.doubleValue() + ").",
                    "Make sure to sample TimeSpans from non-negative distributions only.");
            return new TimeSpan(0);
        }
        
        // Return a TimeSpan using the appropriate constructor
        if (sample instanceof Double || sample instanceof Float) {
            return new TimeSpan(sample.doubleValue(), unit);
        } else {
            return new TimeSpan(sample.longValue(), unit);
        }
    }
}