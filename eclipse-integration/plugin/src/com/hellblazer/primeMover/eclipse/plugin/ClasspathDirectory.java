package com.hellblazer.primeMover.eclipse.plugin;

import java.io.IOException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.Util;

@SuppressWarnings("restriction")
public class ClasspathDirectory extends ClasspathLocation {

    IContainer binaryFolder; // includes .class files for a single directory
    boolean isOutputFolder;
    SimpleLookupTable directoryCache;
    String[] missingPackageHolder = new String[1];
    AccessRuleSet accessRuleSet;

    ClasspathDirectory(IContainer binaryFolder, boolean isOutputFolder,
                       AccessRuleSet accessRuleSet) {
        this.binaryFolder = binaryFolder;
        this.isOutputFolder = isOutputFolder
                              || binaryFolder.getProjectRelativePath().isEmpty(); // if binaryFolder == project, then treat it as an outputFolder
        directoryCache = new SimpleLookupTable(5);
        this.accessRuleSet = accessRuleSet;
    }

    @Override
    public void cleanup() {
        directoryCache = null;
    }

    @Override
    public String debugPathString() {
        return binaryFolder.getFullPath().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClasspathDirectory)) {
            return false;
        }

        ClasspathDirectory dir = (ClasspathDirectory) o;
        if (accessRuleSet != dir.accessRuleSet) {
            if (accessRuleSet == null
                || !accessRuleSet.equals(dir.accessRuleSet)) {
                return false;
            }
        }
        return binaryFolder.equals(dir.binaryFolder);
    }

    @Override
    public NameEnvironmentAnswer findClass(String binaryFileName,
                                           String qualifiedPackageName,
                                           String qualifiedBinaryFileName) {
        if (!doesFileExist(binaryFileName, qualifiedPackageName,
                           qualifiedBinaryFileName)) {
            return null; // most common case
        }

        ClassFileReader reader = null;
        try {
            reader = Util.newClassFileReader(binaryFolder.getFile(new Path(
                                                                           qualifiedBinaryFileName)));
        } catch (CoreException e) {
            return null;
        } catch (ClassFormatException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
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
        return null;
    }

    @Override
    public IPath getProjectRelativePath() {
        return binaryFolder.getProjectRelativePath();
    }

    @Override
    public int hashCode() {
        return binaryFolder == null ? super.hashCode()
                                   : binaryFolder.hashCode();
    }

    @Override
    public boolean isOutputFolder() {
        return isOutputFolder;
    }

    @Override
    public boolean isPackage(String qualifiedPackageName) {
        return directoryList(qualifiedPackageName) != null;
    }

    @Override
    public void reset() {
        directoryCache = new SimpleLookupTable(5);
    }

    @Override
    public String toOSString() {
        return binaryFolder.getLocation().toOSString();
    }

    @Override
    public String toString() {
        String start = "Binary classpath directory " + binaryFolder.getFullPath().toString(); //$NON-NLS-1$
        if (accessRuleSet == null) {
            return start;
        }
        return start + " with " + accessRuleSet; //$NON-NLS-1$
    }

    protected boolean isExcluded(IResource resource) {
        return false;
    }

    String[] directoryList(String qualifiedPackageName) {
        String[] dirList = (String[]) directoryCache.get(qualifiedPackageName);
        if (dirList == missingPackageHolder) {
            return null; // package exists in another classpath directory or jar
        }
        if (dirList != null) {
            return dirList;
        }

        try {
            IResource container = binaryFolder.findMember(qualifiedPackageName); // this is a case-sensitive check
            if (container instanceof IContainer) {
                IResource[] members = ((IContainer) container).members();
                dirList = new String[members.length];
                int index = 0;
                for (IResource m : members) {
                    if (m.getType() == IResource.FILE
                        && org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(m.getName())) {
                        // add exclusion pattern check here if we want to hide .class files
                        dirList[index++] = m.getName();
                    }
                }
                if (index < dirList.length) {
                    System.arraycopy(dirList, 0, dirList = new String[index],
                                     0, index);
                }
                directoryCache.put(qualifiedPackageName, dirList);
                return dirList;
            }
        } catch (CoreException ignored) {
            // ignore
        }
        directoryCache.put(qualifiedPackageName, missingPackageHolder);
        return null;
    }

    boolean doesFileExist(String fileName, String qualifiedPackageName,
                          String qualifiedFullName) {
        String[] dirList = directoryList(qualifiedPackageName);
        if (dirList == null) {
            return false; // most common case
        }

        for (int i = dirList.length; --i >= 0;) {
            if (fileName.equals(dirList[i])) {
                return true;
            }
        }
        return false;
    }

}