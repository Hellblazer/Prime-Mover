package desmoj.core.observer;

/**
 * Interface which an Subject can implement to notify possible Observers
 * 
 * @author Christian Mentz
 * 
 * @param <T>
 *            Class of an possible Subject
 * @param <X>
 *            Type of the Event which is passed in the update Method
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
public interface Subject<T, X> {
	/**
	 * This method adds an observer to the subject
	 * 
	 * @param observer
	 *            the observer which should be added to the subject
	 */
	public void addObserver(Observer<T, X> observer);

	/**
	 * This method deletes an observer from the subject
	 * 
	 * @param observer
	 *            the observer which should be deleted
	 */
	public void deleteObserver(Observer<T, X> observer);

	/**
	 * This method is used to notify the registerd observers
	 * 
	 * @param subject
	 *            reference to the actual subject
	 * @param eventObject
	 *            an eventtype to distinguish different notification types like itemAdded or itemRemoved
	 */
	public void notifyObservers(T subject, X eventObject);
}