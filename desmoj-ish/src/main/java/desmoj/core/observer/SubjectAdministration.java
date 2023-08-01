package desmoj.core.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for the subject interface to make the administration code reusable
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
public class SubjectAdministration<T, X> {
    
	/**
	 * a list of all registered observers of this subject
	 */
	private final List<Observer<T, X>> observers = new ArrayList<Observer<T, X>>();

	/**
	 * Adds an observer to the observer list
	 * 
	 * @param observer
	 *            the observer to be added
	 */
	public void addObserver(Observer<T, X> observer) {
		if (!observers.contains(observer)) {
			observers.add(observer);
		}
	}

	/**
	 * Deletes an observer from the observer list
	 * 
	 * @param observer
	 *            the observer to be deleted
	 */
	public void deleteObserver(Observer<T, X> observer) {
		observers.remove(observer);

	}

	/**
	 * Notifies all registered observers and passes a reference of the subject and a eventtype to the observer
	 * 
	 * @param subject
	 *            the reference of the actual subject
	 * @param eventObject
	 *            the event type of the notify call. This is optional to distinguish the events and can be null
	 */
	public void notifyObservers(T subject, X eventObject) {
		for (Observer<T, X> observer : observers) {
			observer.update(subject, eventObject);
		}
	}
}