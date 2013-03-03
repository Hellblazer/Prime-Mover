package com.hellblazer.primeMover.eclipse.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.Util;

@SuppressWarnings("restriction")
public class ClasspathJar extends ClasspathLocation {

    static class PackageCacheEntry {
        long lastModified;
        long fileSize;
        SimpleSet packageSet;

        PackageCacheEntry(long lastModified, long fileSize, SimpleSet packageSet) {
            this.lastModified = lastModified;
            this.fileSize = fileSize;
            this.packageSet = packageSet;
        }
    }

    static SimpleLookupTable PackageCache = new SimpleLookupTable();

    /**
     * Calculate and cache the package list available in the zipFile.
     * @param jar The ClasspathJar to use
     * @return A SimpleSet with the all the package names in the zipFile.
     */
    static SimpleSet findPackageSet(ClasspathJar jar) {
        String zipFileName = jar.zipFilename;
        long lastModified = jar.lastModified();
        long fileSize = new File(zipFileName).length();
        PackageCacheEntry cacheEntry = (PackageCacheEntry) PackageCache.get(zipFileName);
        if (cacheEntry != null && cacheEntry.lastModified == lastModified
            && cacheEntry.fileSize == fileSize) {
            return cacheEntry.packageSet;
        }

        SimpleSet packageSet = new SimpleSet(41);
        packageSet.add(""); //$NON-NLS-1$
        nextEntry: for (Enumeration<? extends ZipEntry> e = jar.zipFile.entries(); e.hasMoreElements();) {
            String fileName = e.nextElement().getName();

            // add the package name & all of its parent packages
            int last = fileName.lastIndexOf('/');
            while (last > 0) {
                // extract the package name
                String packageName = fileName.substring(0, last);
                if (packageSet.addIfNotIncluded(packageName) == null) {
                    continue nextEntry; // already existed
                }
                last = packageName.lastIndexOf('/');
            }
        }

        PackageCache.put(zipFileName, new PackageCacheEntry(lastModified,
                                                            fileSize,
                                                            packageSet));
        return packageSet;
    }

    String zipFilename; // keep for equals
    IFile resource;
    ZipFile zipFile;
    long lastModified;
    boolean closeZipFileAtEnd;
    SimpleSet knownPackageNames;
    AccessRuleSet accessRuleSet;

    public ClasspathJar(ZipFile zipFile, AccessRuleSet accessRuleSet) {
        zipFilename = zipFile.getName();
        this.zipFile = zipFile;
        closeZipFileAtEnd = false;
        knownPackageNames = null;
        this.accessRuleSet = accessRuleSet;
    }

    ClasspathJar(IFile resource, AccessRuleSet accessRuleSet) {
        this.resource = resource;
        try {
            java.net.URI location = resource.getLocationURI();
            if (location == null) {
                zipFilename = ""; //$NON-NLS-1$
            } else {
                File localFile = Util.toLocalFile(location, null);
                zipFilename = localFile.getPath();
            }
        } catch (CoreException e) {
            // ignore
        }
        zipFile = null;
        knownPackageNames = null;
        this.accessRuleSet = accessRuleSet;
    }

    ClasspathJar(String zipFilename, long lastModified,
                 AccessRuleSet accessRuleSet) {
        this.zipFilename = zipFilename;
        this.lastModified = lastModified;
        zipFile = null;
        knownPackageNames = null;
        this.accessRuleSet = accessRuleSet;
    }

    @Override
    public void cleanup() {
        if (zipFile != null && closeZipFileAtEnd) {
            try {
                zipFile.close();
            } catch (IOException e) { // ignore it
            }
            zipFile = null;
        }
        knownPackageNames = null;
    }

    @Override
    public String debugPathString() {
        long time = lastModified();
        if (time == 0) {
            return zipFilename;
        }
        return zipFilename + '(' + new Date(time) + " : " + time + ')'; //$NON-NLS-1$
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClasspathJar)) {
            return false;
        }

        ClasspathJar jar = (ClasspathJar) o;
        if (accessRuleSet != jar.accessRuleSet) {
            if (accessRuleSet == null
                || !accessRuleSet.equals(jar.accessRuleSet)) {
                return false;
            }
        }
        return zipFilename.equals(jar.zipFilename)
               && lastModified() == jar.lastModified();
    }

    @Override
    public NameEnvironmentAnswer findClass(String binaryFileName,
                                           String qualifiedPackageName,
                                           String qualifiedBinaryFileName) {
        if (!isPackage(qualifiedPackageName)) {
            return null; // most common case
        }

        try {
            ClassFileReader reader = ClassFileReader.read(zipFile,
                                                          qualifiedBinaryFileName);
            if (reader != null) {
                if (accessRuleSet == null) {
                    return new NameEnvironmentAnswer(reader, null);
                }
                String fileNameWithoutExtension = qualifiedBinaryFileName.substring(0,
                                                                                    qualifiedBinaryFileName.length()
                                                                                            - SuffixConstants.SUFFIX_CLASS.length);
                return new NameEnvironmentAnswer(
                                                 reader,
                                                 accessRuleSet.getViolatedRestriction(fileNameWithoutExtension.toCharArray()));
            }
        } catch (IOException e) { // treat as if class file is missing
        } catch (ClassFormatException e) { // treat as if class file is missing
        }
        return null;
    }

    @Override
    public IPath getProjectRelativePath() {
        if (resource == null) {
            return null;
        }
        return resource.getProjectRelativePath();
    }

    @Override
    public int hashCode() {
        return zipFilename == null ? super.hashCode() : zipFilename.hashCode();
    }

    @Override
    public boolean isPackage(String qualifiedPackageName) {
        if (knownPackageNames != null) {
            return knownPackageNames.includes(qualifiedPackageName);
        }

        try {
            if (zipFile == null) {
                zipFile = new ZipFile(zipFilename);
                closeZipFileAtEnd = true;
            }
            knownPackageNames = findPackageSet(this);
        } catch (Exception e) {
            knownPackageNames = new SimpleSet(); // assume for this build the zipFile is empty
        }
        return knownPackageNames.includes(qualifiedPackageName);
    }

    public long lastModified() {
        if (lastModified == 0) {
            lastModified = new File(zipFilename).lastModified();
        }
        return lastModified;
    }

    @Override
    public String toOSString() {
        return zipFilename;
    }

    @Override
    public String toString() {
        String start = "Classpath jar file " + zipFilename; //$NON-NLS-1$
        if (accessRuleSet == null) {
            return start;
        }
        return start + " with " + accessRuleSet; //$NON-NLS-1$
    }

}