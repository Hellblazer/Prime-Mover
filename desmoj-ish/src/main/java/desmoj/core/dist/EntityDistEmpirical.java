package desmoj.core.dist;

import java.util.ArrayList;

import desmoj.core.report.EntityDistEmpiricalReporter;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

/**
 * Empirically distributed stream of entities.
 * 
 * @param <E> the element type
 * @see desmoj.core.dist.Distribution
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler, Philip Joschko, Johannes G&ouml;bel
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
public class EntityDistEmpirical<E extends Entity> extends EntityDist<E> {

	/**
	 * Inner class for entries.
	 */
	private class Entry {

		/**
		 * The entity assigned to this entry.
		 */
		private E entity;

		/**
		 * The relative probability of this entry.
		 */
		private double probability;

		/**
		 * Constructs an entry, containing an entity and a frequency.
		 * 
		 * @param e
		 *            the e
		 * @param prob
		 *            double : The relative probability of this distribution
		 *            sampling Entity e.
		 */
		private Entry(E e, double prob) {
			entity = e;
			probability = prob;
		}
	}

	/**
	 * The range of entities (and their relative probabilities) that can be
	 * returned.
	 */
	private ArrayList<Entry> _entries;

	/**
	 * Shows if the empirical distribution has been properly initialized (i.e.
	 * at least one entity has been assigned).
	 */
	private boolean _isInitialized;

	/**
	 * Auxiliary variable, containing the sum of the relative probabilities of
	 * all entities.
	 */
	private double _totalProbabilities;

	/**
	 * Creates a stream of entities, where an individual probability can be
	 * assigned to each entity.
	 * 
	 * @param owner
	 *            Model : The distribution's owner
	 * @param name
	 *            java.lang.String : The distribution's name
	 * @param showInReport
	 *            boolean : Flag for producing reports
	 * @param showInTrace
	 *            boolean : Flag for producing trace output
	 */
	public EntityDistEmpirical(Model owner, String name, boolean showInReport,
			boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);

		this._entries = new ArrayList<Entry>();
		this._isInitialized = false;
	}

	/**
	 * Adds an entity (type E) to the range of this distribution, unless it is
	 * already present.
	 * 
	 * @param e
	 *            E : The entity to be added to the range of this distribution.
	 * @param probability
	 *            double : The relative probability of this distribution
	 *            sampling Entity e.
	 * 
	 * @return boolean : True, if the entity has been added to the range of this
	 *         distribution (false, if it was already present).
	 */
	public boolean add(E e, double probability) {

		// probability must be positive
		if (probability < 0) {
			sendWarning("Can't add empirical entry! Command ignored.",
					"EntityDistEmpirical " + getName()
							+ " Method: void add (E e, double probability)",
					"The probability parameter given is invalid: "
							+ probability,
					"Be sure to add entries with nonnegative probability.");
			return false; // no proper parameter
		}

		// entity already included?
		if (this.contains(e)) {
			sendWarning(
					"Can't add empirical entry! Command ignored.",
					"EntityDistEmpirical " + getName()
							+ " Method: void add (E e, double probability)",
					"The entity " + e
							+ " is already included in the distribution.",
					"Be sure to only add entries that are not yet present. "
							+ "Method contains(e) can be used to verify entity e "
							+ "is not yet included.");
			return false;
		}

		// add entity
		this._entries.add(new Entry(e, probability));
		this._totalProbabilities += probability;
		if (this._totalProbabilities > 0.0)
			this._isInitialized = true;
		return true;
	}

	/**
	 * Adjusts the relative probability of an entity (type E from the range of
	 * this distribution being sampled.
	 * 
	 * @param e
	 *            E : The entity whose relative probability is adjusted.
	 * @param newProbability
	 *            double : The new relative probability of this distribution
	 *            sampling Entity e.
	 */
	public void changeProbability(E e, double newProbability) {

		// probability must be positive
		if (newProbability < 0) {
			sendWarning(
					"Can't change probability of entity " + e
							+ "! Command ignored.",
					"EntityDistEmpirical "
							+ getName()
							+ " Method: void changeProbability (E e, double newProbability)",
					"The probability parameter given is invalid: "
							+ newProbability,
					"Be sure to chose a nonnegative probability.");
			return;
		}

		// entity not included?
		if (!this.contains(e)) {
			sendWarning(
					"Can't change probability of entity " + e
							+ "! Command ignored.",
					"EntityDistEmpirical "
							+ getName()
							+ " Method: void changeProbability (E e, double probability)",
					"The entity " + e + " is not included in the distribution.",
					"Be sure to only change probabilities of entries that are present. "
							+ "Method contains(e) can be used to verify entity e "
							+ "is included.");
			return;
		}

		// change probability
		for (Entry entry : this._entries) {
			if (entry.entity == e) {
				this._totalProbabilities += newProbability - entry.probability;
				entry.probability = newProbability;
				if (this._totalProbabilities <= 0.0)
					this._isInitialized = false;
				return;
			}
		}
	}

	/**
	 * Checks whether an entity is present (i.e. potentially sampled) by this
	 * distribution
	 * 
	 * @param e
	 *            E : The entity to be checked.
	 * 
	 * @return boolean : True, if the entity is included in the range of this
	 *         distribution and false otherwise.
	 */
	public boolean contains(E e) {
		for (Entry entry : this._entries) {
			if (entry.entity == e)
				return true;
		}
		return false;
	}

	/**
	 * Creates the default reporter for the EntityDistEmpirical distribution.
	 * 
	 * @return Reporter : The reporter for the EntityDistEmpirical distribution
	 * @see EntityDistEmpiricalReporter
	 */
	public desmoj.core.report.Reporter createDefaultReporter() {

		return new desmoj.core.report.EntityDistEmpiricalReporter(this);

	}

	/**
	 * Removes an entity (type E) from the range of this distribution, if it is
	 * included.
	 * 
	 * @param e
	 *            E : The entity to be removed from the range of this
	 *            distribution.
	 * 
	 * @return boolean : True, if the entity has been removed from the range of
	 *         this distribution (false, if it was not present).
	 */
	public boolean remove(E e) {
		for (int i = 0; i < this._entries.size(); i++) {
			if (this._entries.get(i).entity == e) {
				this._totalProbabilities -= this._entries.get(i).probability;
				this._entries.remove(i);
				if (this._totalProbabilities <= 0.0)
					this._isInitialized = false;
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the next empirically distributed entity sample.
	 * 
	 * @return E : The next empirically distributed entity sample
	 */
	public E sample() {

		if (!_isInitialized) {
			sendWarning(
					"Invalid sample returned!",
					"EntityDistEmpirical : " + getName()
							+ " Method: E sample()",
					"The distribution has not been initialized properly yet: "
							+ "No entity provided yet!",
					"Be sure to pass at least one entity to be returned "
							+ "to an entityDistUniform distribution."
							+ "Note that calling method isInitialized() "
							+ "which returns a boolean telling "
							+ "you whether the distribution is initialized or not.");
			return null; // prevent aborting the simulation
		}

		// Algorithm used here is the same as IntDistUniform except for
		// using results as index
		incrementObservations(); // increase count of samples

		double q = randomGenerator.nextDouble() * this._totalProbabilities;
		if (antithetic)
			q = this._totalProbabilities - q; // check for antithetic

		double currentSum = 0;
		for (int i = 0; i < this._entries.size() - 1; i++) {
			currentSum += this._entries.get(i).probability;
			if (currentSum > q)
				return this._entries.get(i).entity;
		}
		return this._entries.get(this._entries.size() - 1).entity;
	}

	/**
	 * Overrides the same method of <code>desmoj.dist.Distribution</code>. A
	 * warning that it makes no sense to set a <code>EntityDistEmpirical</code>
	 * to be (not) negative is printed since the distribution samples are not
	 * numerical.
	 * 
	 * @param newValue
	 *            boolean : No effect. A warning is issued.
	 */
	public void setNonNegative(boolean newValue) {
		this.nonNegative = newValue;
		sendWarning("Attempt to set a EntityDistEmpirical to "
				+ (newValue ? "" : "not ")
				+ "nonNegative. This will be done, but doesn't make sense!",
				"EntityDistEmpirical: " + this.getName()
						+ " Method: public void "
						+ "setNonNegative(boolean newValue)",
				"The given distribution does not return numerical samples.",
				"No necessity to set a non-numerical distribution to nonNegative.");
	}
}