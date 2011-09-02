package com.hellblazer.primeMover.eclipse.plugin;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.util.Util;

@SuppressWarnings("restriction")
class ClasspathMultiDirectory extends ClasspathDirectory {

    IContainer sourceFolder;
    char[][] inclusionPatterns; // used by builders when walking source folders
    char[][] exclusionPatterns; // used by builders when walking source folders
    boolean hasIndependentOutputFolder; // if output folder is not equal to any of the source folders

    ClasspathMultiDirectory(IContainer sourceFolder, IContainer binaryFolder,
                            char[][] inclusionPatterns,
                            char[][] exclusionPatterns) {
        super(binaryFolder, true, null);

        this.sourceFolder = sourceFolder;
        this.inclusionPatterns = inclusionPatterns;
        this.exclusionPatterns = exclusionPatterns;
        hasIndependentOutputFolder = false;

        // handle the case when a state rebuilds a source folder
        if (this.inclusionPatterns != null
            && this.inclusionPatterns.length == 0) {
            this.inclusionPatterns = null;
        }
        if (this.exclusionPatterns != null
            && this.exclusionPatterns.length == 0) {
            this.exclusionPatterns = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClasspathMultiDirectory)) {
            return false;
        }

        ClasspathMultiDirectory md = (ClasspathMultiDirectory) o;
        return sourceFolder.equals(md.sourceFolder)
               && binaryFolder.equals(md.binaryFolder)
               && CharOperation.equals(inclusionPatterns, md.inclusionPatterns)
               && CharOperation.equals(exclusionPatterns, md.exclusionPatterns);
    }

    @Override
    public String toString() {
        return "Source classpath directory " + sourceFolder.getFullPath().toString() + //$NON-NLS-1$
               " with " + super.toString(); //$NON-NLS-1$
    }

    @Override
    protected boolean isExcluded(IResource resource) {
        if (exclusionPatterns != null || inclusionPatterns != null) {
            if (sourceFolder.equals(binaryFolder)) {
                return Util.isExcluded(resource, inclusionPatterns,
                                       exclusionPatterns);
            }
        }
        return false;
    }
}