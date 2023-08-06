package desmoj.core.report;

/**
 * Provides the basic interface implemented by all classes receiving messages of
 * any type in the DESMO-J framework. This allows for flexible organisation of
 * the messaging subsystem, enabling different kinds of output paths for
 * different kinds of messages, including multiple outputs of the same type of
 * message.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
 *
 *         Licensed under the Apache License, Version 2.0 (the "License"); you
 *         may not use this file except in compliance with the License. You may
 *         obtain a copy of the License at
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied. See the License for the specific language governing
 *         permissions and limitations under the License.
 *
 */
public interface MessageReceiver {
    /**
     * Implement this method to define the behaviour of the messagereceiver when
     * reporters are sent to it.
     *
     * @param r Reporter : The reporter sent to the messagereceiver
     */
    void receive(desmoj.core.report.Reporter r);

    /**
     * Implement this method to define the behaviour of the messagereceiver when
     * messages are sent to it.
     *
     * @param m Message : The message sent to the messagereceiver
     */
    void receive(Message m);
}
