package desmoj.core.simulator;

import java.util.HashMap;

/**
 * Keeps track of the names given for Schedulables within an experiment. To help
 * identify individual entities, events and all other types of Schedulables,
 * this class registers all names given to these objects. If an object is
 * created with the same name as some other object before, a number is added to
 * the object's name as a suffix. The number represents the amount of objects
 * already created with that name.
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
public class NameCatalog {

	/**
	 * Stores the names of all Schedulables within an experiment. The names are
	 * used as the indexing value via the string's method to produce a hashing
	 * key value. The numbers for each individual name are stored as objects of
	 * their wrapper class.
	 */
	private HashMap<String,Integer> _catalog;

	/**
	 * Constructs a single namecatalog for registering names of all Schedulables
	 * within an experiment.
	 */
	NameCatalog() {

		super();
		_catalog = new HashMap<String,Integer>();

	}

	/**
     * Returns the first part of name without added number as suffix. 
     * 
     * @return java.lang.String : The first part of the name without number suffix
     * @param name
     *            java.lang.String : The name obtain the first part from.
     */
	String getNameWithoutSuffix(String name) {
	    if (name == null || name.equals("unnamed") || name.indexOf("#") == -1)
	        return name; // no suffix to remove
	    else
	        return name.substring(0, name.lastIndexOf("#"));
	}
	
    /**
	 * Registers the given name in the namecatalog and returns the correct name
	 * with the added number if necessary. If a <code>null</code> reference is
	 * given as parameter, the returned name will be set to "unnamed" with the
	 * number of unnamed objects so far added as a suffix.
	 * 
	 * @return java.lang.String : The registered name including the number if
	 *         necessary
	 * @param name
	 *            java.lang.String : The name for a new Schedulable
	 */
	String registeredName(String name) {

		if (name == null)
			name = "unnamed";

		Integer number = _catalog.get(name);

		if (number != null) {
			int i = number.intValue();
			i++;
			_catalog.put(name, i);
			return name + "#" + i;
		} else {
			_catalog.put(name, 1);
			return name + "#1";
		}

	}
}