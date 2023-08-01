package desmoj.core.simulator;

import desmoj.core.advancedModellingFeatures.Res;

//34567890123456789012345678901234567890123456789012345678901234567890123456

/**
 * Resources are objects needed by SimProcesses to perform certain tasks. Every
 * Resource has its unique ID number, so it can be identified. Each Resource
 * belongs to a certain resource category (resource pool) and has the status
 * ready (can be used) or <code>outOfOrder</code> if it is broken down.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen
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
public class Resource extends ModelComponent {

	// ****** attributes ******

	/**
	 * The number identifying a Resource. Because it is a class variable each
	 * Resource will get its own ID number starting by zero.
	 */
	private static long resourceNumber = 0;

	/**
	 * The ID number of this Resource object.
	 */
	private long _idNumber;

	/**
	 * Indicating if this resource is out of order (broken down) and therefore
	 * can not be used at the moment.
	 */
	private boolean _outOfOrder;

	/**
	 * The reference to the resource pool this resource belongs to.
	 */
	private Res _resPool;

	// ****** methods ******

	/**
	 * Constructs a resource object with the given String as name and the given
	 * model as the associated owner of this component. Components can only be
	 * created after the corresponding model object has been instantiated. The
	 * default preset for the showInTrace option is <code>false</code>.
	 * 
	 * @param ownerModel
	 *            Model : The model this resource is associated to.
	 * @param name
	 *            java.lang.String : The name of the resource.
	 * @param resPool
	 *            Res : The resource pool this resource belongs to.
	 * @param showInTrace
	 *            boolean : Flag for showing this resource in trace files. Set
	 *            it to <code>true</code> if resource should show up in trace.
	 *            Set it to <code>false</code> if resource should not be shown
	 *            in trace.
	 */
	public Resource(Model ownerModel, String name, Res resPool,
			boolean showInTrace) {
		super(ownerModel, name, showInTrace); // create the ModelComponent
		_idNumber = resourceNumber++; // increment the ID number
		rename(name + " resource No. " + _idNumber); // set the name
		this._resPool = resPool; // set the reference to the resource pool
		// this resource belongs to
		this._outOfOrder = false; // this resource can be used
	}

	/**
	 * Returns the ID number of this resource object.
	 * 
	 * @return long : The ID number of this resource object.
	 */
	public long getidNumber() {
		return _idNumber;
	}

	/**
	 * Returns the resource pool (Res) this resource belongs to.
	 * 
	 * @return Res : The resource pool (Res) this resource belongs to.
	 */
	public Res getResPool() {
		return _resPool;
	}

	/**
	 * Is this resource out of order at the moment and therefore can not be
	 * used?
	 * 
	 * @return boolean : Is this resource out of order at the moment?
	 */
	public boolean isOutOfOrder() {
		return _outOfOrder;
	}

	/**
	 * This resource can be set to out of order (<code>true</code>) if it is
	 * broken down and can not be used at the moment.
	 * 
	 * @param brokenDown
	 *            boolean : Flag for showing if this resource is out of order
	 *            and therefore is not available at the moment. Set it to
	 *            <code>true</code> if the resource is broken down. Set it to
	 *            <code>false</code> if the resource is ready to use.
	 */
	public void setOutOfOrder(boolean brokenDown) {
		_outOfOrder = brokenDown;
	}
}