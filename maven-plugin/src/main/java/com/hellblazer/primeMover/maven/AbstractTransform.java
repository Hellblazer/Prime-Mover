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
import java.io.IOException;
import java.util.Enumeration;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTransform extends AbstractMojo {

    private static Logger logger = LoggerFactory.getLogger(AbstractTransform.class);

    public AbstractTransform() {
        super();
    }

    @Override
    public void execute() throws MojoExecutionException {
//        G.reset();
        String classpath = getCompileClasspath();
        logger.info(String.format("Using transform classpath: %s", classpath));
//        Options.v().set_soot_classpath(classpath);
//        Options.v().set_process_dir(asList(getProcessDirectory()));
//        Options.v().set_output_dir(getOutputDirectory());
//        SimulationTransform.main(null);
    }

    abstract protected String getCompileClasspath() throws MojoExecutionException;

    String getBootClasspath() throws MojoExecutionException {
        String cp = System.getProperty("sun.boot.class.path");
        if (cp != null) {
            logger.debug(String.format("Using[1] boot class path: %s", cp));
            return cp;
        }
        cp = System.getProperty("java.boot.class.path");
        if (cp != null) {
            logger.debug(String.format("Using[2] boot class path: %s", cp));
            return cp;
        }
        Enumeration<?> i = System.getProperties().propertyNames();
        String name = null;
        while (i.hasMoreElements()) {
            String temp = (String) i.nextElement();
            if (temp.indexOf(".boot.class.path") != -1) {
                if (name == null) {
                    name = temp;
                } else {
                    throw new MojoExecutionException("Cannot auto-detect boot class path "
                    + System.getProperty("java.version"));
                }
            }
        }
        if (name == null) {
            String version = System.getProperty("java.version");
            if (version.startsWith("1.1.")) {
                // by default, the current directory is added to the classpath
                // we therefore need to strip that out
                cp = System.getProperty("java.class.path");
                cp = removeAll(cp, ".");
                cp = removeAll(cp, new File(".").getAbsolutePath());
                try {
                    cp = removeAll(cp, new File(".").getCanonicalPath());
                } catch (IOException e) {
                    // ignore
                }
                cp = removeAll(cp, new File(".").getAbsolutePath() + System.getProperty("file.separator"));
                try {
                    cp = removeAll(cp, new File(".").getCanonicalPath() + System.getProperty("file.separator"));
                } catch (IOException e) {
                    // ignore
                }
                logger.debug(String.format("Using[3] boot class path: %s", cp));
                return cp;
            }
            logger.error("Cannot auto-detect boot class path " + System.getProperty("java.version") + " "
            + System.getProperty("java.class.path"));
            throw new MojoExecutionException("Cannot auto-detect boot class path ");
        }
        logger.info(String.format("Using[4] boot class path: %s", System.getProperty(name)));
        return System.getProperty(name);
    }

    abstract String getOutputDirectory();

    abstract String getProcessDirectory();

    boolean isWindows() {
        return System.getProperty("os.name").toUpperCase().startsWith("WINDOWS");
    }

    String removeAll(String cp, String prefix) {
        String pathSeparator = System.getProperty("path.separator");
        if (cp.startsWith(prefix + pathSeparator)) {
            cp = cp.substring(prefix.length() + pathSeparator.length());
        }
        int j;
        while (-1 != (j = cp.indexOf(pathSeparator + prefix + pathSeparator))) {
            cp = cp.substring(0, j) + cp.substring(j + prefix.length() + pathSeparator.length());
        }
        if (cp.endsWith(pathSeparator + prefix)) {
            cp = cp.substring(0, cp.length() - prefix.length() + pathSeparator.length());
        }
        if (isWindows()) {
            // we might have the prefix or the classpath case differing
            if (cp.toUpperCase().startsWith((prefix + pathSeparator).toUpperCase())) {
                cp = cp.substring(prefix.length() + pathSeparator.length());
            }
            while (-1 != (j = cp.toUpperCase().indexOf((pathSeparator + prefix + pathSeparator).toUpperCase()))) {
                cp = cp.substring(0, j) + cp.substring(j + prefix.length() + pathSeparator.length());
            }
            if (cp.toUpperCase().endsWith((pathSeparator + prefix).toUpperCase())) {
                cp = cp.substring(0, cp.length() - prefix.length() + pathSeparator.length());
            }
        }
        return cp;
    }

}
