package desmoj.core.dist;

import desmoj.core.report.EntityDistUniformReporter;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc

/**
 * Uniformly distributed stream of entities.
 *
 * @param <E> the element type
 * @author Tim Lechler, Philip Joschko, Johannes G&ouml;bel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @see desmoj.core.dist.Distribution
 */
public class EntityDistUniform<E extends Entity> extends EntityDist<E> {

    /** The range of entities that can be returned. */
    private ArrayList<E> _entities;

    /**
     * Shows if the empirical distribution has been properly initialized (i.e. at least one entity has been assigned).
     */
    private boolean _isInitialized;

    /**
     * Creates a stream of entities that are equally likely.
     *
     * @param owner        Model : The distribution's owner
     * @param name         java.lang.String : The distribution's name
     * @param showInReport boolean : Flag for producing reports
     * @param showInTrace  boolean : Flag for producing trace output
     */
    public EntityDistUniform(Model owner, String name, boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);

        this._entities = new ArrayList<>();
        this._isInitialized = false;
    }

    /**
     * Adds an entity (type E) to the range of this distribution, unless it is already present.
     *
     * @param e E : The entity to be added to the range of this distribution.
     * @return boolean : True, if the entity has been added to the range of this distribution (false, if it was already
     * present).
     */
    public boolean add(E e) {

        this._isInitialized = true;

        // entity already included?
        if (this.contains(e)) {
            sendWarning("Can't add empirical entry! Command ignored.",
                        "EntityDistUniform " + getName() + " Method: void add (E e)",
                        "The entity " + e + " is already included in the distribution.",
                        "Be sure to only add entries that are not yet present. "
                        + "Method contains(e) can be used to verify entity e " + "is not yet included.");
            return false;
        }

        this._entities.add(e);
        return true;
    }

    /**
     * Checks whether an entity is present (i.e. potentially sampled) by this distribution
     *
     * @param e E : The entity to be checked.
     * @return boolean : True, if the entity is included in the range of this distribution and false otherwise.
     */
    public boolean contains(E e) {
        return this._entities.contains(e);
    }

    /**
     * Creates the default reporter for the EntityDistUniform distribution.
     *
     * @return Reporter : The reporter for the EntityDistUniform distribution
     * @see EntityDistUniformReporter
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {

        return new desmoj.core.report.EntityDistUniformReporter(this);

    }

    /**
     * Removes an entity (type E) from the range of this distribution, if it is included.
     *
     * @param e E : The entity to be removed from the range of this distribution.
     * @return boolean : True, if the entity has been removed from the range of this distribution (false, if it was not
     * present).
     */
    public boolean remove(E e) {
        boolean result = this._entities.remove(e);
        if (this._entities.isEmpty()) {
            this._isInitialized = false;
        }
        return result;
    }

    /**
     * Returns the next empirically distributed entity sample.
     *
     * @return E : The next empirically distributed entity sample
     */
    @Override
    public E sample() {

        if (!_isInitialized) {
            sendWarning("Invalid sample returned!", "EntityDistEmpirical : " + getName() + " Method: E sample()",
                        "The distribution has not been initialized properly yet: " + "No entity provided yet!",
                        "Be sure to pass at least one entity to be returned " + "to an entityDistUniform distribution."
                        + "Note that calling method isInitialized() " + "which returns a boolean telling "
                        + "you whether the distribution is initialized or not.");
            return null; // prevent aborting the simulation
        }

        // Algorithm used here is the same as IntDistUniform except for
        // using results as index
        incrementObservations(); // increase count of samples

        int index; // aux variable
        int max = this._entities.size() - 1;

        do {
            if (isAntithetic()) // check if antithetic mode is on
            {
                index = (int) (Math.floor(max + 1) * (1 - randomGenerator.nextDouble()));
            } else {
                index = (int) (Math.floor(max + 1) * randomGenerator.nextDouble());
            }
        } while (nonNegative && index < 0 || index == (max + 1)); // get

        return this._entities.get(index);
    }

    /**
     * Overrides the same method of <code>desmoj.dist.Distribution</code>. A warning that it makes no sense to set a
     * <code>EntityDistUniform</code> to be (not) negative is printed since the distribution samples are not numerical.
     *
     * @param newValue boolean : No effect. A warning is issued.
     */
    @Override
    public void setNonNegative(boolean newValue) {
        this.nonNegative = newValue;
        sendWarning("Attempt to set a EntityDistUniform to " + (newValue ? "" : "not ")
                    + "nonNegative. This will be done, but doesn't make sense!",
                    "EntityDistUniform: " + this.getName() + " Method: public void "
                    + "setNonNegative(boolean newValue)", "The given distribution does not return numerical samples.",
                    "No necessity to set a non-numerical distribution to nonNegative.");
    }
}
