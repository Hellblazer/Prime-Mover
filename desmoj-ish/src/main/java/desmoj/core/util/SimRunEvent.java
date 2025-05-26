package desmoj.core.util;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

import java.util.EventObject;

/**
 * Base class for AWT events occurring while a simulation runs.
 *
 * @author Nicolas Knaak
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */

public class SimRunEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new sim run event
     *
     * @param source the experiment the SimRunEvent is assigned to
     */
    public SimRunEvent(Experiment source) {
        super(source);
    }

    /** @return the source experiment's current simulation time */
    public TimeInstant getCurrentTime() {
        return getModel().presentTime();
    }

    /** @return the source ExperimentRunner that emitted this event */
    public Experiment getExperiment() {
        return (Experiment) this.getSource();
    }

    /** @return the model connected to the assigned experiment */
    public Model getModel() {
        return getExperiment().getModel();
    }
}
