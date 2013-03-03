package com.hellblazer.primeMover.eclipse.plugin;

import static com.hellblazer.primeMover.eclipse.plugin.Activator.PLUGIN_ID;
import static com.hellblazer.primeMover.eclipse.plugin.Activator.plugin;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.ExternalFoldersManager;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;
import org.eclipse.jdt.internal.core.builder.State;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;

import soot.G;
import soot.options.Options;

import com.hellblazer.primeMover.soot.SimulationTransform;

@SuppressWarnings("restriction")
public class IdeTransformer extends IncrementalProjectBuilder {
    private class BuildEnvironment {
        private final String classPath;
        private final ArrayList<ClasspathMultiDirectory> outputLocations;
        private final Map<IProject, ArrayList<ClasspathLocation>> projectBinaryLocations;

        BuildEnvironment(ArrayList<ClasspathLocation> binaryLocations,
                         ArrayList<ClasspathMultiDirectory> outputFolders,
                         Map<IProject, ArrayList<ClasspathLocation>> dependencies) {
            outputLocations = outputFolders;
            projectBinaryLocations = dependencies;
            classPath = buildClasspath(binaryLocations);
        }

        private String buildClasspath(ArrayList<ClasspathLocation> binaryClasspath) {
            StringBuffer classpath = new StringBuffer();
            for (ClasspathLocation binaryLocation : binaryClasspath) {
                classpath.append(binaryLocation.toOSString());
                classpath.append(File.pathSeparator);
            }
            return classpath.toString();
        }

        boolean hasClasspathChanged(BuildEnvironment newEnvironment) {
            for (IProject project : projectBinaryLocations.keySet()) {
                if (newEnvironment.projectBinaryLocations.get(project) == null) {
                    return true;
                }
            }
            for (IProject project : newEnvironment.projectBinaryLocations.keySet()) {
                ArrayList<ClasspathLocation> newClasspath = newEnvironment.projectBinaryLocations.get(project);
                ArrayList<ClasspathLocation> oldBinaryLocations = projectBinaryLocations.get(project);
                if (oldBinaryLocations == null) {
                    return true;
                }
                int newLength = newClasspath.size();
                int oldLength = oldBinaryLocations.size();
                int n, o;
                for (n = o = 0; n < newLength && o < oldLength; n++, o++) {
                    if (newClasspath.get(n).equals(oldBinaryLocations.get(o))) {
                        continue;
                    }
                    return true;
                }
                if (n < newLength || o < oldLength) {
                    return true;
                }
            }
            return false;
        }

        boolean hasStructuralDelta(BuildEnvironment newEnvironment) {
            for (IProject project : newEnvironment.projectBinaryLocations.keySet()) {
                IResourceDelta delta = getDelta(project);
                if (delta != null
                    && delta.getKind() != IResourceDelta.NO_CHANGE) {
                    ArrayList<ClasspathLocation> classFoldersAndJars = projectBinaryLocations.get(project);
                    if (classFoldersAndJars != null) {
                        for (ClasspathLocation classFolderOrJar : classFoldersAndJars) {
                            if (classFolderOrJar != null) {
                                IPath p = classFolderOrJar.getProjectRelativePath();
                                if (p != null) {
                                    IResourceDelta binaryDelta = delta.findMember(p);
                                    if (binaryDelta != null
                                        && binaryDelta.getKind() != IResourceDelta.NO_CHANGE) {
                                        return true;
                                    }
                                }
                            }
                        }
                    } else {
                        return true; // no past record == change
                    }
                }
            }
            return false;
        }
    }

    private static class GeneratedFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            int i1 = name.lastIndexOf('.');
            if (i1 == -1) {
                return false;
            }
            int i2 = name.lastIndexOf('$');
            if (i2 == -1) {
                return false;
            }
            if ("gen".equals(name.substring(i2 + 1, i1))) {
                return true;
            }
            return false;
        }
    }

    private static final String TRANSFORM_ERROR_MARKER = Activator.PLUGIN_ID
                                                         + ".transformError";
    static final String CONSOLE_NAME = "Prime Mover Transform";
    private static final ILog logger = plugin.getLog();
    public static final String BUILDER_ID = "com.hellblazer.primeMover.eclipse.transformer";
    private static final FilenameFilter filter = new GeneratedFilter();

    public static void removeProblemsAndTasksFor(IResource resource) {
        try {
            if (resource != null && resource.exists()) {
                resource.deleteMarkers(TRANSFORM_ERROR_MARKER, false,
                                       IResource.DEPTH_INFINITE);
            }
        } catch (CoreException e) {
            // assume there were no problems
        }
    }

    private BuildEnvironment lastState;
    private JavaProject javaProject;

    public IdeTransformer() {
        super();
    }

    private void build(BuildEnvironment newEnvironment) throws CoreException,
                                                       PartInitException {
        lastState = newEnvironment;
        String classPath = lastState.classPath;
        String pathSeparator = System.getProperty("path.separator");
        for (ClasspathLocation output : lastState.outputLocations) {
            String outputLocation = output.toOSString();
            classPath += pathSeparator;
            classPath += outputLocation;

            G.reset();
            PrintStream outputStream = new PrintStream(getConsoleStream());
            G.v().out = outputStream;
            clean(new File(outputLocation));
            Options.v().set_soot_classpath(classPath);
            Options.v().set_process_dir(asList(outputLocation));
            Options.v().set_output_dir(outputLocation);
            SimulationTransform transform = new SimulationTransform();
            try {
                transform.execute(null);
            } catch (Throwable e) {
                markProject(getProject(),
                            String.format("Unable to transform project %s",
                                          getProject().getName()));
                logger.log(new Status(
                                      IStatus.ERROR,
                                      PLUGIN_ID,
                                      String.format("Unable to transform project %s",
                                                    getProject().getName()), e));
                e.printStackTrace(outputStream);
            }
        }
    }

    private BuildEnvironment computeBuildEnvironment() throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        Map<IProject, ArrayList<ClasspathLocation>> binaryLocationsPerProject = new HashMap<IProject, ArrayList<ClasspathLocation>>();
        /* Update cycle marker */
        IMarker cycleMarker = javaProject.getCycleMarker();
        if (cycleMarker != null) {
            int severity = JavaCore.ERROR.equals(javaProject.getOption(JavaCore.CORE_CIRCULAR_CLASSPATH,
                                                                       true)) ? IMarker.SEVERITY_ERROR
                                                                             : IMarker.SEVERITY_WARNING;
            if (severity != cycleMarker.getAttribute(IMarker.SEVERITY, severity)) {
                cycleMarker.setAttribute(IMarker.SEVERITY, severity);
            }
        }

        ArrayList<ClasspathLocation> sLocations = new ArrayList<ClasspathLocation>();
        ArrayList<ClasspathLocation> bLocations = new ArrayList<ClasspathLocation>();
        nextEntry: for (IClasspathEntry abstractEntry : javaProject.getExpandedClasspath()) {
            ClasspathEntry entry = (ClasspathEntry) abstractEntry;
            IPath path = entry.getPath();
            Object target = JavaModel.getTarget(path, true);
            if (target == null) {
                continue nextEntry;
            }

            switch (entry.getEntryKind()) {
                case IClasspathEntry.CPE_SOURCE: {
                    if (target instanceof IContainer) {
                        sLocations.add(sourceLocation(root, entry,
                                                      (IContainer) target));
                    }
                    break;
                }
                case IClasspathEntry.CPE_PROJECT: {
                    if (target instanceof IProject) {
                        IProject prereqProject = (IProject) target;
                        if (JavaProject.hasJavaNature(prereqProject)) {
                            addPrereqProject(root, binaryLocationsPerProject,
                                             bLocations, entry, prereqProject);
                        }
                    }

                    break;
                }

                case IClasspathEntry.CPE_LIBRARY: {
                    addResource(binaryLocationsPerProject, bLocations, entry,
                                path, target);
                    break;
                }
            }
        }

        // now split the classpath locations... place the output folders ahead
        // of the other .class file folders & jars
        ArrayList<ClasspathMultiDirectory> outputFolders = new ArrayList<ClasspathMultiDirectory>(
                                                                                                  1);
        ClasspathMultiDirectory[] sourceLocations = new ClasspathMultiDirectory[sLocations.size()];
        if (!sLocations.isEmpty()) {
            sLocations.toArray(sourceLocations);

            // collect the output folders, skipping duplicates
            next: for (int i = 0, l = sourceLocations.length; i < l; i++) {
                ClasspathMultiDirectory md = sourceLocations[i];
                IPath outputPath = md.binaryFolder.getFullPath();
                for (int j = 0; j < i; j++) { // compare against previously
                                              // walked source folders
                    if (outputPath.equals(sourceLocations[j].binaryFolder.getFullPath())) {
                        md.hasIndependentOutputFolder = sourceLocations[j].hasIndependentOutputFolder;
                        continue next;
                    }
                }
                outputFolders.add(md);

                // also tag each source folder whose output folder is an
                // independent folder & is not also a source folder
                for (int j = 0, m = sourceLocations.length; j < m; j++) {
                    if (outputPath.equals(sourceLocations[j].sourceFolder.getFullPath())) {
                        continue next;
                    }
                }
                md.hasIndependentOutputFolder = true;
            }
        }

        return new BuildEnvironment(bLocations, outputFolders,
                                    binaryLocationsPerProject);
    }

    private ClasspathLocation sourceLocation(IWorkspaceRoot root,
                                             ClasspathEntry entry,
                                             IContainer target)
                                                               throws JavaModelException {
        IPath outputPath = entry.getOutputLocation() != null ? entry.getOutputLocation()
                                                            : javaProject.getOutputLocation();
        IContainer outputFolder;
        if (outputPath.segmentCount() == 1) {
            outputFolder = javaProject.getProject();
        } else {
            outputFolder = root.getFolder(outputPath);
        }
        ClasspathLocation sourceFolder = ClasspathLocation.forSourceFolder(target,
                                                                           outputFolder,
                                                                           entry.fullInclusionPatternChars(),
                                                                           entry.fullExclusionPatternChars());
        return sourceFolder;
    }

    private void addResource(Map<IProject, ArrayList<ClasspathLocation>> binaryLocationsPerProject,
                             ArrayList<ClasspathLocation> bLocations,
                             ClasspathEntry entry, IPath path, Object target) {
        if (target instanceof IResource) {
            IResource resource = (IResource) target;
            ClasspathLocation bLocation = null;
            if (resource instanceof IFile) {
                AccessRuleSet accessRuleSet = JavaCore.IGNORE.equals(javaProject.getOption(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE,
                                                                                           true))
                                              && JavaCore.IGNORE.equals(javaProject.getOption(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE,
                                                                                              true)) ? null
                                                                                                    : entry.getAccessRuleSet();
                bLocation = ClasspathLocation.forLibrary((IFile) resource,
                                                         accessRuleSet);
            } else if (resource instanceof IContainer) {
                AccessRuleSet accessRuleSet = JavaCore.IGNORE.equals(javaProject.getOption(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE,
                                                                                           true))
                                              && JavaCore.IGNORE.equals(javaProject.getOption(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE,
                                                                                              true)) ? null
                                                                                                    : entry.getAccessRuleSet();
                bLocation = ClasspathLocation.forBinaryFolder((IContainer) target,
                                                              false,
                                                              accessRuleSet); // is
                                                                              // library
                                                                              // folder
                                                                              // not
                                                                              // output
                                                                              // folder
            }
            bLocations.add(bLocation);
            if (binaryLocationsPerProject != null) { // normal
                                                     // builder mode
                IProject p = resource.getProject(); // can be the
                                                    // project being
                                                    // built
                ArrayList<ClasspathLocation> existingLocations = binaryLocationsPerProject.get(p);
                if (existingLocations == null) {
                    existingLocations = new ArrayList<ClasspathLocation>();
                }
                existingLocations.add(bLocation);
                binaryLocationsPerProject.put(p, existingLocations);
            }
        } else if (target instanceof File) {
            AccessRuleSet accessRuleSet = JavaCore.IGNORE.equals(javaProject.getOption(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE,
                                                                                       true))
                                          && JavaCore.IGNORE.equals(javaProject.getOption(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE,
                                                                                          true)) ? null
                                                                                                : entry.getAccessRuleSet();
            bLocations.add(ClasspathLocation.forLibrary(path.toString(),
                                                        accessRuleSet));
        }
    }

    private void addPrereqProject(IWorkspaceRoot root,
                                  Map<IProject, ArrayList<ClasspathLocation>> binaryLocationsPerProject,
                                  ArrayList<ClasspathLocation> bLocations,
                                  ClasspathEntry entry, IProject prereqProject)
                                                                               throws JavaModelException {

		JavaProject prereqJavaProject = (JavaProject) JavaCore.create(prereqProject);
        IClasspathEntry[] prereqClasspathEntries = prereqJavaProject.getRawClasspath();
        ArrayList<IContainer> seen = new ArrayList<IContainer>();
        for (IClasspathEntry prereqEntry : prereqClasspathEntries) {
            if (prereqEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                Object prereqTarget = JavaModel.getTarget(prereqEntry.getPath(),
                                                          true);
                if (prereqTarget instanceof IContainer) {
                    IPath prereqOutputPath = prereqEntry.getOutputLocation() != null ? prereqEntry.getOutputLocation()
                                                                                    : prereqJavaProject.getOutputLocation();
                    IContainer binaryFolder = prereqOutputPath.segmentCount() == 1 ? (IContainer) prereqProject
                                                                                  : (IContainer) root.getFolder(prereqOutputPath);
                    if (binaryFolder.exists() && !seen.contains(binaryFolder)) {
                        seen.add(binaryFolder);
                        ClasspathLocation bLocation = ClasspathLocation.forBinaryFolder(binaryFolder,
                                                                                        true,
                                                                                        entry.getAccessRuleSet());
                        bLocations.add(bLocation);
                        ArrayList<ClasspathLocation> existingLocations = binaryLocationsPerProject.get(prereqProject);
                        if (existingLocations == null) {
                            existingLocations = new ArrayList<ClasspathLocation>();
                        }
                        existingLocations.add(bLocation);
                        binaryLocationsPerProject.put(prereqProject,
                                                      existingLocations);
                    }
                }
            }
        }
    }

    private MessageConsole findConsole() {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager conMan = plugin.getConsoleManager();
        IConsole[] existing = conMan.getConsoles();
        for (IConsole element : existing) {
            if (CONSOLE_NAME.equals(element.getName())) {
                return (MessageConsole) element;
            }
        }
        // no console found, so create a new one
        MessageConsole myConsole = new MessageConsole(CONSOLE_NAME, null);
        conMan.addConsoles(new IConsole[] { myConsole });
        return myConsole;
    }

    private OutputStream getConsoleStream() throws PartInitException {
        MessageConsole console = findConsole();
        openConsole(console);
        return console.newMessageStream();
    }

    private State getLastState(IProject project) {
        return (State) JavaModelManager.getJavaModelManager().getLastBuiltState(project,
                                                                                null);
    }

    /*
     * Return the list of projects for which it requires a resource delta. This
     * builder's project is implicitly included and need not be specified.
     * Builders must re-specify the list of interesting projects every time they
     * are run as this is not carried forward beyond the next build. Missing
     * projects should be specified but will be ignored until they are added to
     * the workspace.
     */
    private IProject[] getRequiredProjects() {
        JavaProject javaProject = (JavaProject) JavaCore.create(getProject());
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

        ArrayList<IProject> projects = new ArrayList<IProject>();
        ExternalFoldersManager externalFoldersManager = JavaModelManager.getExternalManager();
        try {
            IClasspathEntry[] entries = javaProject.getExpandedClasspath();
            for (IClasspathEntry entry : entries) {
                IPath path = entry.getPath();
                IProject p = null;
                switch (entry.getEntryKind()) {
                    case IClasspathEntry.CPE_PROJECT:
                        p = workspaceRoot.getProject(path.lastSegment()); // missing
                                                                          // projects
                                                                          // are
                                                                          // considered
                                                                          // too
                        if (((ClasspathEntry) entry).isOptional()
                            && !JavaProject.hasJavaNature(p)) {
                            // is optional
                            p = null;
                        }
                        break;
                    case IClasspathEntry.CPE_LIBRARY:
                        if (path.segmentCount() > 0) {
                            // some binary resources on the class path can come
                            // from projects that are not included in the
                            // project references
                            IResource resource = workspaceRoot.findMember(path.segment(0));
                            if (resource instanceof IProject) {
                                p = (IProject) resource;
                            } else {
                                resource = externalFoldersManager.getFolder(path);
                                if (resource != null) {
                                    p = resource.getProject();
                                }
                            }
                        }
                }
                if (p != null && !projects.contains(p)) {
                    projects.add(p);
                }
            }
        } catch (JavaModelException e) {
            return new IProject[0];
        }
        IProject[] result = new IProject[projects.size()];
        projects.toArray(result);
        return result;
    }

    private boolean hasJavaBuilder(IProject project) throws CoreException {
        ICommand[] buildCommands = project.getDescription().getBuildSpec();
        for (ICommand buildCommand : buildCommands) {
            if (buildCommand.getBuilderName().equals(JavaCore.BUILDER_ID)) {
                return true;
            }
        }
        return false;
    }

    private boolean isClasspathBroken(IClasspathEntry[] classpath, IProject p)
                                                                              throws CoreException {
        IMarker[] markers = p.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER,
                                          false, IResource.DEPTH_ZERO);
        for (IMarker marker : markers) {
            if (marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) {

                return true;
            }
        }
        return false;
    }

    /**
     * Check to see if the class paths are valid
     * 
     * @param progressMonitor
     * @param project
     * @param requiredProjects
     * @return true if aspect, in, and class paths are valid. False if there is
     *         a problem
     * @throws CoreException
     */
    private boolean isWorthBuilding() throws CoreException {
        IProject project = getProject();
        removeProblemsAndTasksFor(project);
        if (isClasspathBroken(JavaCore.create(project).getRawClasspath(),
                              project)) {
            logger.log(new Status(IStatus.ERROR, PLUGIN_ID,
                                  "build: Abort due to missing classpath entries"));
            // make this the only problem for this project
            markProject(project,
                        String.format("Build prerequisites for project %s has problems",
                                      project.getName()));

            return false;
        }

        IJavaProject jp = JavaCore.create(getProject());
        if (JavaCore.WARNING.equals(jp.getOption(JavaCore.CORE_INCOMPLETE_CLASSPATH,
                                                 true))) {
            return true;
        }

        // make sure all prereq projects have valid build states... only when
        // aborting builds since projects in cycles do not have build states
        // except for projects involved in a 'warning' cycle (see below)
        IProject[] requiredProjects = getRequiredProjects();
        for (IProject requiredProject : requiredProjects) {
            IProject p = requiredProject;
            if (getLastState(p) == null) {
                // The prereq project has no build state: if this prereq project
                // has a 'warning' cycle marker then allow build (see bug id
                // 23357)
                JavaProject prereq = (JavaProject) JavaCore.create(p);
                if (prereq.hasCycleMarker()
                    && JavaCore.WARNING.equals(jp.getOption(JavaCore.CORE_CIRCULAR_CLASSPATH,
                                                            true))) {
                    System.out.println("Continued to build even though prereq project " + p.getName() //$NON-NLS-1$
                                       + " was not built since its part of a cycle"); //$NON-NLS-1$
                    continue;
                }
                if (!hasJavaBuilder(p)) {
                    System.out.println("Continued to build even though prereq project " + p.getName() //$NON-NLS-1$
                                       + " is not built by JavaBuilder"); //$NON-NLS-1$
                    continue;
                }
                System.out.println("Aborted build because prereq project " + p.getName() //$NON-NLS-1$
                                   + " was not built"); //$NON-NLS-1$

                IMarker marker = project.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
                marker.setAttributes(new String[] {
                                                   IMarker.MESSAGE,
                                                   IMarker.SEVERITY,
                                                   IJavaModelMarker.CATEGORY_ID,
                                                   IMarker.SOURCE_ID },
                                     new Object[] {
                                                   isClasspathBroken(prereq.getRawClasspath(),
                                                                     p) ? Messages.bind(Messages.build_prereqProjectHasClasspathProblems,
                                                                                        p.getName())
                                                                       : Messages.bind(Messages.build_prereqProjectMustBeRebuilt,
                                                                                       p.getName()),
                                                   new Integer(
                                                               IMarker.SEVERITY_ERROR),
                                                   new Integer(
                                                               CategorizedProblem.CAT_BUILDPATH),
                                                   JavaBuilder.SOURCE_ID });
                return false;
            }
        }
        return true;
    }

    private void markProject(IProject project, String errorMessage) {
        try {
            IMarker errorMarker = project.createMarker(TRANSFORM_ERROR_MARKER);
            errorMarker.setAttribute(IMarker.MESSAGE, errorMessage);
            errorMarker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        } catch (CoreException e) {
            logger.log(new Status(IStatus.ERROR, PLUGIN_ID,
                                  "build: Problem occured creating the error marker for project "
                                          + project.getName() + ": " + e));
        }
    }

    private void openConsole(IConsole msgConsole) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return;
        }
        IWorkbenchPage page = window.getActivePage();
        String id = IConsoleConstants.ID_CONSOLE_VIEW;
        IConsoleView view;
        try {
            view = (IConsoleView) page.showView(id, CONSOLE_NAME, 1);
        } catch (PartInitException e) {
            ConsolePlugin.log(e);
            return;
        }
        view.display(msgConsole);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
     * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected IProject[] build(int kind,
                               @SuppressWarnings("rawtypes") Map args,
                               IProgressMonitor monitor) throws CoreException {
        if (!isWorthBuilding()) {
            return getRequiredProjects();
        }

        BuildEnvironment newEnvironment = computeBuildEnvironment();
        if (kind == FULL_BUILD || kind == CLEAN_BUILD || lastState == null
            || projectHasChanged()
            || lastState.hasClasspathChanged(newEnvironment)
            || lastState.hasStructuralDelta(newEnvironment)) {
            build(newEnvironment);
        }
        return getRequiredProjects();
    }

    private boolean projectHasChanged() {
        IResourceDelta delta = getDelta(getProject());
        return delta != null && delta.getKind() != IResourceDelta.NO_CHANGE;
    }

    protected void clean(File directory) {
        for (File generated : directory.listFiles(filter)) {
            generated.delete();
        }
        for (File f : directory.listFiles()) {
            if (f.isDirectory()) {
                clean(f);
            }
        }
    }

    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        super.clean(monitor);
        for (ClasspathLocation output : computeBuildEnvironment().outputLocations) {
            clean(new File(output.toOSString()));
        }
        lastState = null;
    }

    @Override
    protected void startupOnInitialize() {
        super.startupOnInitialize();
        javaProject = (JavaProject) JavaCore.create(getProject());
    }
}
