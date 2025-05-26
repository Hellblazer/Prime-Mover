package desmoj.core.simulator;

/**
 * This superclass for every DESMOJ class provides the ability to carry a name for identification. Gives objects the
 * ability to carry a name to be shown in reports. Each class in the desmoj-Framework is supposed to be able to carry a
 * name for identification and offer methods to reset and tell its name. Since all other desmoj classes inherit from
 * this class, it is ensured that any object, even the user's special objects, have a name to be used in a report.
 * Offering no default constructor enforces users to give each new object in their models a name. The class is set
 * abstract to prevent clients from accidentally creating otherwise functionless (but named) objects.
 *
 * @author Tim Lechler
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */
public abstract class NamedObject {

    /*
     * The name of the namedObject as a String.
     */
    private String _myName;

    /**
     * Constructs a named object with the given initial name. Note, that this allows clients to produce more than one
     * object with one name, thus being unable to distinguish those objects in reports!
     *
     * @param name java.lang.String : The given initial name for the named object
     */
    public NamedObject(String name) {

        if (name == null) {
            _myName = "unnamed";
        } else {
            _myName = name;
        }

    }

    /**
     * Returns the name of the named object. This is the same name displayed in reports and trace files when this named
     * object is shown in those reports or trace files.
     *
     * @return java.lang.String : The name of the named object
     */
    public String getName() {

        return _myName;

    }

    /**
     * Returns the quoted name of the named object. This is the name displayed in reports and trace files when this
     * named object is shown.
     *
     * @return java.lang.String : The quoted name of the named object
     */
    public String getQuotedName() {

        return "'" + _myName + "'";

    }

    /**
     * Overrides the java.lang.Object's toString method to return the named object's name when given as parameter to a
     * method that expects a string to be passed.
     *
     * @return java.lang.String : The named object's name
     */
    @Override
    public String toString() {

        return _myName;

    }

    /**
     * Changes the name of the named object. This might be necessary for automatically created named object of a user
     * defined model, but should not be used for elements of the framework or basic objects of the user model. Changing
     * names of objects while runtime will confuse any trace output and generally make traces more difficult if not
     * impossible to follow.
     *
     * @param newName java.lang.String : The new name for the named object
     */
    protected void rename(String newName) {

        _myName = newName;

    }
}
