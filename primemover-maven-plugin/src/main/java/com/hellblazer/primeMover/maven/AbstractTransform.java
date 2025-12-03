/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.maven;

import com.hellblazer.primeMover.classfile.SimulationTransform;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTransform extends AbstractMojo {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTransform.class);

    public AbstractTransform() {
        super();
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            logger.info("Skipping transformation");
            return;
        }

        var classpaths = getCompileClasspath();
        logger.info("Using ClassFile API transform classpath: {}", classpaths);

        var failed = new ArrayList<String>();
        try {
            var scanner = new com.hellblazer.primeMover.classfile.ClassScanner();
            for (var cp : classpaths) {
                scanner.addClasspathEntry(Path.of(cp));
            }
            scanner.scan();
            
            try (var txfm = new SimulationTransform(scanner)) {
                var out = getOutputDirectory();
                txfm.transformed(_ -> true).forEach((cm, bytes) -> {
                    var file = new File(out, cm.getName().replace('.', '/') + ".class");
                    file.getParentFile().mkdirs();
                    try (var fos = new FileOutputStream(file)) {
                        fos.write(bytes);
                        logger.info("ClassFile API Transformed: {}, written: {}", cm.getName(), file.getAbsoluteFile());
                    } catch (IOException e) {
                        logger.error("Failed to write transformed class to {}", file.getAbsolutePath(), e);
                        failed.add(file.getAbsolutePath());
                    }
                });
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to transform", e);
        }
        if (!failed.isEmpty()) {
            throw new MojoExecutionException("Unable to write transformed classes: " + failed);
        }
    }

    abstract protected boolean isSkip();

    abstract protected List<String> getCompileClasspath() throws MojoExecutionException;

    abstract File getOutputDirectory();

}
