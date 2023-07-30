/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transform the module's classes to event driven simulation code.
 * 
 */
@Mojo(name = "transform", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, threadSafe = false, executionStrategy = "once-per-session", instantiationStrategy = InstantiationStrategy.PER_LOOKUP, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class CompileTransform extends AbstractTransform {
    private static final Logger log = LoggerFactory.getLogger(CompileTransform.class);

    @Parameter(property = "project")
    protected MavenProject project;

    @Parameter(property = "project.build.outputDirectory", readonly = true)
    File buildOutputDirectory;

    @Override
    protected String getCompileClasspath() throws MojoExecutionException {
        log.info(String.format("Maven project: %s", project));
        StringBuffer classpath = new StringBuffer();
        classpath.append(getBootClasspath());
        List<?> runtimeClasspathElements;
        try {
            runtimeClasspathElements = project.getRuntimeClasspathElements();
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Unable to do dependency resolution", e);
        }
        log.debug(String.format("Runtime classpath elements: %s", runtimeClasspathElements));
        for (Object element : runtimeClasspathElements) {
            classpath.append(':');
            classpath.append(element);
        }
        String pathString = classpath.toString();
        log.info(String.format("Compile transform classpath: %s", pathString));
        return pathString;
    }

    @Override
    String getOutputDirectory() {
        return buildOutputDirectory.getAbsolutePath();
    }

    @Override
    String getProcessDirectory() {
        return buildOutputDirectory.getAbsolutePath();
    }
}
