package desmoj.core.simulator;

/**
 * Represents a code to be passed to interrupted SimProcesses to give information about the reason for the interruption.
 * Each new interrupt code instantiated will carry an individual internal integer codenumber to help identify different
 * interrupt code objects. These can be checked using the static <code>equals(InterruptCode a, InterruptCode b)</code>
 * method. It might come handy to clone an interrupt code object to make it known at different objects in a model. To
 * produce a clone, create a new interrupt code using the alternative constructor method giving the interrupt code to be
 * cloned as a parameter. That constructor will return a clone of the given interrupt code.
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
public class InterruptCode extends NamedObject {

    /**
     * Static counter to provide each new interrupt code with a unique internal serial number.
     */
    private static int irqCounter = 0;
    /**
     * The internal unique number to identify different interrupt codes.
     */
    private int _irqCode;

    /**
     * Constructs a new interrupt code to be an identical clone of the given interrupt code object. The constructor
     * provides the new interrupt code object with same name and internal codenumber as the interrupt code object given
     * as parameter.
     *
     * @param cloneMe desmoj.InterruptCode : The interrupt code object to produce a clone of
     */
    public InterruptCode(InterruptCode cloneMe) {

        super(cloneMe.getName());
        _irqCode = cloneMe.getCodeNumber();

    }

    /**
     * Produces a new interrupt code with a unique internal serial number and the given name.
     *
     * @param name java.lang.String : The interrupt code's name
     */
    public InterruptCode(String name) {

        super(name);

        _irqCode = ++irqCounter; // increment counter and set code

    }

    /**
     * Returns <code>true</code> if the two given interrupt codes have the same internal code, <code>false</code>
     * otherwise.
     *
     * @param a desmoj.InterruptCode : First comparand
     * @param b desmoj.InterruptCode : Second comparand
     * @return boolean : Is <code>true</code> if the two given interrupt codes have the same internal code,
     * <code>false</code> otherwise
     */
    public static boolean equals(InterruptCode a, InterruptCode b) {

        if ((a != null) && (b != null)) {
            return (a.getCodeNumber() == b.getCodeNumber());
        } else {
            return false;
        }

    }

    /**
     * Returns the internal unique number to identify different interrupt codes.
     *
     * @return int : The internal unique number of the interrupt code
     */
    public int getCodeNumber() {
        return _irqCode;
    }
}
