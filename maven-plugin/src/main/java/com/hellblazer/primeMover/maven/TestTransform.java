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

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Transform the module's test classes to event driven simulation code.
 * 
 * @goal transform-test
 * 
 * @phase process-test-classes
 * 
 * @requiresDependencyResolution test
 */
public class TestTransform extends AbstractTransform {

    /**
     * The build output directory
     * 
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    File buildOutputDirectory;

    /**
     * Location of the file.
     * 
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
     * @readonly
     */
    File testOutputDirectory;

    /**
     * @parameter expression="${project.testClasspathElements}"
     * @readonly
     */
    protected List<String> testClasspathElements;

    @Override
    protected String getCompileClasspath() throws MojoExecutionException {
        StringBuffer classpath = new StringBuffer();
        classpath.append(getJavaClasspath());
        for (Object element : testClasspathElements) {
            classpath.append(':');
            classpath.append(element);
        }
        return classpath.toString();
    }

    @Override
    String getOutputDirectory() {
        return testOutputDirectory.getAbsolutePath();
    }

    @Override
    String getProcessDirectory() {
        return testOutputDirectory.getAbsolutePath();
    }

}
