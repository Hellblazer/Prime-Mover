/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package soot;

/**
 * @author hal.hildebrand
 *
 */
public class Hack {
    public static void writeClass(SootClass generatedClass, PackManager v) {
        v.writeClass(generatedClass);
    }
}
