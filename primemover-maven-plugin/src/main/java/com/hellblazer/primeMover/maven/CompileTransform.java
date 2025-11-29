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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Transform the module's classes to event driven simulation code.
 */
@Mojo(name = "transform", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, threadSafe = true, executionStrategy = "once-per-session", instantiationStrategy = InstantiationStrategy.PER_LOOKUP, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class CompileTransform extends AbstractTransform {

    @Parameter(property = "project.build.outputDirectory", readonly = true)
    private File buildOutputDirectory;

    @Parameter(property = "project")
    private MavenProject project;

    @Parameter(property = "primemover.skip", defaultValue = "false")
    private boolean skip;

    @Override
    protected String getCompileClasspath() throws MojoExecutionException {
        return project.getBuild().getOutputDirectory();
    }

    @Override
    File getOutputDirectory() {
        return buildOutputDirectory;
    }

    @Override
    protected boolean isSkip() {
        return skip;
    }
}
