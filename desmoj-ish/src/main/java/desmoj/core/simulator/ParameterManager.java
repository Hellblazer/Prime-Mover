package desmoj.core.simulator;

import java.text.Collator;
import java.util.Collection;
import java.util.TreeMap;

/**
 * Class to manage parameters. Parameters are values, which affect a simulation-
 * model or an experiment. Model-parameters are model's constants. Experiment-
 * parameters are experiment-constants which can be used in the experiment's model.
 * 
 * This class is accessible from Model through the ModelParameterManager-interface to
 * restrict the accessible methods from a model's view. It is accessible from Experiment
 * through the ExperimentParameterManager-interface to restrict the accessible methods
 * from a experiment's view.
 * 
 * @see desmoj.core.simulator.ModelParameterManager
 * @see desmoj.core.simulator.ExperimentParameterManager
 * 
 * @author Tim Janz
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
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
public class ParameterManager implements ModelParameterManager, ExperimentParameterManager
{
	/**
	 * A map containing the parameter's name and the parameter itself
	 */
    //private IdentityHashMap<String, Parameter> parameters;
    private TreeMap<String, Parameter> _parameters;
    
    /**
     * Constructs a ParameterManager
     */
    public ParameterManager()
    {
        super();
        
        //parameters = new IdentityHashMap<String, Parameter>();
        _parameters = new TreeMap<String, Parameter>(Collator.getInstance());
    }

    /**
	 * Method assign a value to an experiment-parameter declared
	 * previously by calling the model's parameter-manager method
	 * to declare an experiment-parameter
	 * 
	 * @param name
	 * 			the parameter's name
	 * @param value
	 * 			the parameter's value
	 */
    public void assignExperimentParameter(String name, Object value)
    {
        Parameter param = _parameters.get(name);
        
        if (param.getParameterType() == Parameter.ParameterType.EXPERIMENTPARAMETER)
        {
            param.setValue(value);
        }
        else
        {
            //TODO: (Exception) param not found.
        }
    }

    /**
     * Method to assign a value to a model-parameter declared previously.
     * 
     * @param name
     * 			the model-parameter's name
     * @param value
     * 			the model-parameter's value
     */
    public void assignModelParameter(String name, Object value)
    {
        Parameter param = _parameters.get(name);
        
        if (param.getParameterType() == Parameter.ParameterType.MODELPARAMETER)
        {
            param.setValue(value);
        }
        else
        {
            //TODO: (Exception) param not found.
        }
    }

    /**
	 * Method to declare a experiment-parameter. The experiment-parameter's
	 * declarations are part of the simulation-model. The experiment-
	 * parameter's assignments are in contrast part of an experiment.
	 * 
	 * @param type
	 * 			the experiment-parameter's type
	 * @param name
	 * 			the experiment-parameter's name
	 */
    public void declareExperimentParameter(Class<?> type, String name)
    {
        _parameters.put(name, Parameter.createExperimentParameter(type, name));
    }

    /**
     * Method to declare a experiment-parameter. The experiment-parameter's
	 * declarations are part of the simulation-model. The experiment-
	 * parameter's assignments are in contrast part of an experiment.
	 * The default value is used, if an experiment dosen't assign a
	 * value to this parameter.
	 * 
     * @param type
     * 			the experiment-parameter's type
     * @param name
     * 			the experiment-parameter's name
     * @param defaultValue
     * 			the experiment-parameter's default value
     */
    public void declareExperimentParameter(Class<?> type, String name, Object defaultValue)
    {
        _parameters.put(name, Parameter.createExperimentParameter(type, name, defaultValue));
    }
 
    /**
     * Method to declare a model-parameter.
     * 
     * @param type
     * 			the model-parameter's type
     * @param name
     * 			the model-parameter's name
     */
    public void declareModelParameter(Class<?> type, String name)
    {
        _parameters.put(name, Parameter.createModelParameter(type, name));
    }

    /**
     * Returns all declared parameters.
     * 
     * @return
     * 			the parameters
     */
    public Collection<Parameter> getParameters()
    {
        return _parameters.values();
    }

    /**
     * Returns the value of an experiment- or model-parameter. Experiment-
     * and model-parameters are accessible through the model, to be able
     * to use the parameter's values at design-time.
     * 
     * @param name
     * 			the parameter's name
     * @return
     * 			the parameter's value
     */
    public Object getParameterValue(String name)
    {
        Object result = null;
        
        if (_parameters.containsKey(name))
        {
            result = _parameters.get(name).getValue();
        }
        else
        {
            //TODO: (Exception) param + name + not declared.
        }
        
        return result;
    }

    /**
     * Method to initialize (declare and assign) a model-parameter.
     * 
     * @param type
     * 			the model-parameter's type
     * @param name
     * 			the model-parameter's name
     * @param value
     * 			the model-parameter's value
     */
    public void initializeModelParameter(Class<?> type, String name, Object value)
    {
        _parameters.put(name, Parameter.createModelParameter(type, name, value));
    }
}
