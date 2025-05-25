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

import com.hellblazer.primeMover.asm.SimulationTransformClassFileAPI;
import io.github.classgraph.ClassGraph;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class AbstractTransformClassFileAPI extends AbstractMojo {

    private static Logger logger = LoggerFactory.getLogger(AbstractTransformClassFileAPI.class);

    public AbstractTransformClassFileAPI() {
        super();
    }

    @Override
    public void execute() throws MojoExecutionException {
        var classpath = getCompileClasspath();
        logger.info(String.format("Using ClassFile API transform classpath: %s", Arrays.asList(classpath)));
        var graph = new ClassGraph();
        final var cpFile = new File(classpath);
        final URL cpUrl;
        try {
            cpUrl = cpFile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Unable to transform", e);
        }
        graph.addClassLoader(new URLClassLoader(new URL[] { cpUrl }));
        var failed = new ArrayList<String>();
        try (var txfm = new SimulationTransformClassFileAPI(graph)) {
            var out = getOutputDirectory();
            txfm.transformed(_ -> true).forEach((ci, bytes) -> {
                final var file = new File(out, ci.getName().replace('.', '/') + ".class");
                file.getParentFile().mkdirs();
                try (var fos = new FileOutputStream(file)) {
                    fos.write(bytes);
                    logger.info(String.format("ClassFile API Transformed: %s, written: %s", ci.getName(), file.getAbsoluteFile()));
                } catch (IOException e) {
                    failed.add(file.getAbsolutePath());
                }
            });
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to transform", e);
        }
        if (!failed.isEmpty()) {
            throw new MojoExecutionException("Unable to transform: " + failed);
        }
    }

    abstract File getOutputDirectory();

    abstract protected String getCompileClasspath() throws MojoExecutionException;

}