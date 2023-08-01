package desmoj.core.simulator;

/**
 * The model which shall be used to simulated coroutines on the JVM.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tobias Baum
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
public enum CoroutineModel {

    /**
     * Use Quasar fibers (lightweight threads) for every process.
     * Fibers can have performance benefits, but demand the use of bytecode manipulation.
     * To use fibers, the Quasar agent has to be activated at the JVM command line.
     * See http://docs.paralleluniverse.co/quasar/#instrumentation for further details.
     */
    FIBERS {
        @Override
        public SimStrandFactory createStrandFactory(String experimentName) {
            return new FiberSimStrandFactory();
        }
    },
    
    /**
     * Use a thread for every process.
     * This is the classic model used by DESMO-J.
     */
    THREADS {
        @Override
        public SimStrandFactory createStrandFactory(String experimentName) {
            return new ThreadSimStrandFactory(experimentName);
        }
    };
    
    abstract SimStrandFactory createStrandFactory(String experimentName);

}
