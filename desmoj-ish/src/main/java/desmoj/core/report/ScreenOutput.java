package desmoj.core.report;

/**
 * A simple MessageReceiver that just prints the Message's description attribute
 * into a single line on screen using the <code>System.out</code> output
 * stream.
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
public class ScreenOutput implements MessageReceiver {

	/**
	 * Constructs a simple ScreenOutput object to print to
	 * <code>System.out</code> in lines.
	 */
	public ScreenOutput() {

		super();

	}

	/**
	 * Messages given as parameter to this method have their description
	 * attribute printed in one String on the screen.
	 * 
	 * @param m
	 *            desmoj.report.Message : The message to be printed on screen
	 */
	public void receive(Message m) {

		System.out.println(m.getDescription());

	}

	/**
	 * The method to print reports on screen is not implemented since reports
	 * need some grouping that is done by the classes that print to files. This
	 * method just returns without notice.
	 * 
	 * @param r
	 *            desmoj.report.Reporter : The reporter to be printed
	 */
	public void receive(Reporter r) {

		return;

	}
}