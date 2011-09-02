package com.hellblazer.primeMover.eclipse.plugin;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleFactory;

public class TransformConsoleFactory implements IConsoleFactory {

    int counter = 1;
    @Override
    public void openConsole() { 
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                try { 
                    String secondaryId = IdeTransformer.CONSOLE_NAME + " #" + counter; //$NON-NLS-1$
                    page.showView(IConsoleConstants.ID_CONSOLE_VIEW, secondaryId, 1);
                    counter++;
                } catch (PartInitException e) {
                    ConsolePlugin.log(e);
                }
            }
        }
    }

}
