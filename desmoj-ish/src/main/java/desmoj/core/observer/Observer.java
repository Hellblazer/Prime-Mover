package desmoj.core.observer;

/**
 * Interface which an Observer class can implement to get Information from a Subject Class
 * 
 * @author Christian Mentz
 * 
 * @param <T>
 *            Class of an possible Subject
 * @param <X>
 *            Type of the Event which is passed in the update Method
 *            
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
public interface Observer<T, X> {

	/**
	 * This Method is called when a Subject calls his notify Method
	 * 
	 * @param subject
	 *            Reference to the calling Subject
	 * @param eventObject
	 *            An eventtype to have the chance of Making branches for the different eventtypes
	 */
	public void update(T subject, X eventObject);
}