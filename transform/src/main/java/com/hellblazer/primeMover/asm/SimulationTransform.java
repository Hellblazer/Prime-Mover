/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.hellblazer.primeMover.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

/**
 * @author hal.hildebrand
 *
 */
public class SimulationTransform {

    public byte[] transform(byte[] classBits) {
        final var writer = new ClassWriter(0);
        final var reader = new ClassReader(classBits);
        reader.accept(transform(writer), ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    private ClassVisitor transform(final ClassWriter writer) {
        final var apiTransform = new ClassRemapper(writer,
                                                   new SimpleRemapper("com/hellblazer/primeMover/Kronos",
                                                                      "com/hellblazer/primeMover/runtime/Kairos"));
        return apiTransform;
    }
}
