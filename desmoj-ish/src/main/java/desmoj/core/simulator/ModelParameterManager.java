package desmoj.core.simulator;

import java.util.Collection;

/**
 * Interface to restrict the functionalities of a parameter-manager to manage
 * model-parameters. This Interface should be used as return- type of the
 * Model's methods which return the parameter- manager.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Janz
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
public interface ModelParameterManager {
    /**
     * Method to assign a value to a model-parameter declared previously.
     *
     * @param name  the model-parameter's name
     * @param value the model-parameter's value
     */
    public void assignModelParameter(String name, Object value);

    /**
     * Method to declare a experiment-parameter. The experiment-parameter's
     * declarations are part of the simulation-model. The experiment- parameter's
     * assignments are in contrast part of an experiment.
     *
     * @param type the experiment-parameter's type
     * @param name the experiment-parameter's name
     */
    public void declareExperimentParameter(Class<?> type, String name);

    /**
     * Method to declare a experiment-parameter. The experiment-parameter's
     * declarations are part of the simulation-model. The experiment- parameter's
     * assignments are in contrast part of an experiment. The default value is used,
     * if an experiment dosen't assign a value to this parameter.
     *
     * @param type         the experiment-parameter's type
     * @param name         the experiment-parameter's name
     * @param defaultValue the experiment-parameter's default value
     */
    public void declareExperimentParameter(Class<?> type, String name, Object defaultValue);

    /**
     * Method to declare a model-parameter.
     *
     * @param type the model-parameter's type
     * @param name the model-parameter's name
     */
    public void declareModelParameter(Class<?> type, String name);

    /**
     * Returns all declared parameters.
     *
     * @return the parameters
     */
    public Collection<Parameter> getParameters();

    /**
     * Returns the value of an experiment- or model-parameter. Experiment- and
     * model-parameters are accessible through the model, to be able to use the
     * parameter's values at design-time.
     *
     * @param name the parameter's name
     * @return the parameter's value
     */
    public Object getParameterValue(String name);

    /**
     * Method to initialize (declare and assign) a model-parameter.
     *
     * @param type  the model-parameter's type
     * @param name  the model-parameter's name
     * @param value the model-parameter's value
     */
    public void initializeModelParameter(Class<?> type, String name, Object value);
}
