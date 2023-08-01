package desmoj.core.simulator;

/**
 * Defines an interface to design the individual model options. This interface
 * does not define any special methods. It is used to clarify the API by
 * publishing a name for this type of objects.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
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
 *
 */
public interface ModelOptions {
	/**
	 * Sets the options for the specific model. The model has to call this
	 * method once the ModelOptions object has been passed to the model's
	 * <code>init(Modeloptions)</code> method. This is done automatically
	 * within the <code>init(Modeloptions)</code> method.
	 * 
	 * @param m
	 *            Model : The specific model for this options setting
	 */
	void setOptions(Model m);
}