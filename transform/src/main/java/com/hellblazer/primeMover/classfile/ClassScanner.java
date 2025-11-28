/*
 * Copyright (C) 2023 Hal Hildebrand. All rights reserved.
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
package com.hellblazer.primeMover.classfile;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Native Java classpath scanner using java.nio.file and ClassFile API.
 * Replaces io.github.classgraph.ClassGraph for classpath scanning functionality.
 *
 * @author hal.hildebrand
 */
public class ClassScanner implements Closeable {
    private static final Logger log = Logger.getLogger(ClassScanner.class.getName());
    private static final ClassFile CLASS_FILE = ClassFile.of();

    private final List<Path> classpath = new ArrayList<>();
    private final Map<String, ClassMetadata> scannedClasses = new ConcurrentHashMap<>();
    private final Map<String, byte[]> classBytes = new ConcurrentHashMap<>();
    private URLClassLoader classLoader;
    private boolean scanned = false;

    /**
     * Add a classpath entry (directory or JAR file)
     */
    public ClassScanner addClasspathEntry(Path path) {
        classpath.add(path);
        return this;
    }

    /**
     * Add a classpath entry from a URL
     */
    public ClassScanner addClasspathEntry(URL url) {
        try {
            classpath.add(Path.of(url.toURI()));
        } catch (Exception e) {
            log.warning("Failed to add classpath entry: " + url + " - " + e.getMessage());
        }
        return this;
    }

    /**
     * Add a classpath entry from a string path
     */
    public ClassScanner addClasspathEntry(String path) {
        classpath.add(Path.of(path));
        return this;
    }

    /**
     * Perform the scan of all classpath entries
     */
    public ClassScanner scan() throws IOException {
        if (scanned) {
            return this;
        }

        // Create URLClassLoader for the classpath
        var urls = classpath.stream()
                            .map(p -> {
                                try {
                                    return p.toUri().toURL();
                                } catch (Exception e) {
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .toArray(URL[]::new);
        classLoader = new URLClassLoader(urls, getClass().getClassLoader());

        for (var path : classpath) {
            if (Files.isDirectory(path)) {
                scanDirectory(path);
            } else if (Files.isRegularFile(path) && path.toString().endsWith(".jar")) {
                scanJar(path);
            }
        }

        // Build class hierarchy after scanning
        buildClassHierarchy();
        scanned = true;
        return this;
    }

    /**
     * Get all scanned classes
     */
    public Collection<ClassMetadata> getAllClasses() {
        return Collections.unmodifiableCollection(scannedClasses.values());
    }

    /**
     * Get a class by name
     */
    public ClassMetadata getClass(String className) {
        return scannedClasses.get(className);
    }

    /**
     * Get classes with a specific annotation
     */
    public List<ClassMetadata> getClassesWithAnnotation(String annotationName) {
        return scannedClasses.values().stream()
                             .filter(cm -> cm.hasAnnotation(annotationName))
                             .toList();
    }

    /**
     * Get classes with a specific annotation, filtered
     */
    public List<ClassMetadata> getClassesWithAnnotation(String annotationName, Predicate<ClassMetadata> filter) {
        return scannedClasses.values().stream()
                             .filter(cm -> cm.hasAnnotation(annotationName))
                             .filter(filter)
                             .toList();
    }

    /**
     * Get classes that depend on a specific class (reference it in constant pool)
     */
    public List<ClassMetadata> getClassesDependingOn(String className) {
        return scannedClasses.values().stream()
                             .filter(cm -> cm.getDependencies().contains(className))
                             .toList();
    }

    /**
     * Get the raw bytecode for a class
     */
    public byte[] getClassBytes(String className) {
        return classBytes.get(className);
    }

    /**
     * Get the ClassLoader for the scanned classpath
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Stream all classes matching a filter
     */
    public Stream<ClassMetadata> stream() {
        return scannedClasses.values().stream();
    }

    /**
     * Stream all classes matching a filter
     */
    public Stream<ClassMetadata> stream(Predicate<ClassMetadata> filter) {
        return scannedClasses.values().stream().filter(filter);
    }

    @Override
    public void close() throws IOException {
        if (classLoader != null) {
            classLoader.close();
        }
        scannedClasses.clear();
        classBytes.clear();
    }

    private void scanDirectory(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".class")) {
                    try {
                        var bytes = Files.readAllBytes(file);
                        var relativePath = dir.relativize(file).toString();
                        var className = relativePath.replace('/', '.').replace('\\', '.')
                                                    .substring(0, relativePath.length() - 6);
                        processClassBytes(className, bytes);
                    } catch (Exception e) {
                        log.fine("Failed to scan class: " + file + " - " + e.getMessage());
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void scanJar(Path jarPath) throws IOException {
        try (var jarFile = new JarFile(jarPath.toFile())) {
            var entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    try (InputStream is = jarFile.getInputStream(entry)) {
                        var bytes = is.readAllBytes();
                        var className = entry.getName().replace('/', '.')
                                             .substring(0, entry.getName().length() - 6);
                        processClassBytes(className, bytes);
                    } catch (Exception e) {
                        log.fine("Failed to scan class: " + entry.getName() + " - " + e.getMessage());
                    }
                }
            }
        }
    }

    private void processClassBytes(String className, byte[] bytes) {
        try {
            ClassModel classModel = CLASS_FILE.parse(bytes);
            var metadata = new ClassMetadata(className, classModel, bytes);
            scannedClasses.put(className, metadata);
            classBytes.put(className, bytes);
        } catch (Exception e) {
            log.fine("Failed to parse class: " + className + " - " + e.getMessage());
        }
    }

    private void buildClassHierarchy() {
        // Link superclasses and interfaces
        for (var metadata : scannedClasses.values()) {
            var superClassName = metadata.getSuperclassName();
            if (superClassName != null) {
                var superClass = scannedClasses.get(superClassName);
                metadata.setSuperclass(superClass);
            }

            for (var interfaceName : metadata.getInterfaceNames()) {
                var interfaceClass = scannedClasses.get(interfaceName);
                if (interfaceClass != null) {
                    metadata.addResolvedInterface(interfaceClass);
                }
            }
        }
    }
}
