package org.codegating.subversive.extension;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.internal.resources.File;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.team.svn.ui.extension.factory.ICommentDialogPanel;
import org.eclipse.team.svn.ui.extension.factory.ICommitActionFactory;
import org.eclipse.team.svn.ui.extension.factory.ICommitDialog;
import org.eclipse.team.svn.ui.extension.impl.DefaultCommitActionFactory;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Custom CommitAction Factory which acts like an interceptor to the
 * <code>DefaultCommitActionFactory</code>; to add the necessary pre-commit hook
 * inside Subversive.
 * 
 * @author Ganesh Kondal
 * @version 0.9
 * @since April 24, 2013
 * @see org.eclipse.team.svn.ui.extension.impl.DefaultCommitActionFactory
 * @see org.eclipse.team.svn.ui.extension.factory.ICommitActionFactory
 */
public class CommitActionFactoryHook implements ICommitActionFactory {
	private static final String className = CommitActionFactoryHook.class.getName();
	// -- log strings.
	// TODO remove all these once testing gets over.
	
	private static final String IN_GET_COMMIT_DIALOG = "In getCommitDialog()";
	
	/** The default commit action factory. */
	DefaultCommitActionFactory defaultCommitActionFactory = new DefaultCommitActionFactory();

	/**
	 * Commit action factory which returns 1 on open; essentially blocking the
	 * message dialog.
	 */
	BlockingCommitActionFactory blockingCommitActionFactory = new BlockingCommitActionFactory();
	AntPreCommitValidator commitValidator = new AntPreCommitValidator();

	/**
	 * Instantiates a new my commit action factory.
	 */
	public CommitActionFactoryHook() {
	}

	/**
	 * Opens up the commit dialog with the list of files to choose from.
	 * Downside of running PMD here is - the developer might have just touched a
	 * file which he is still working on; but we can differentiate which he
	 * chose to checkin of the changed lot.
	 * 
	 * @see org.eclipse.team.svn.ui.extension.factory.ICommitActionFactory#
	 *      getCommitDialog(org.eclipse.swt.widgets.Shell, java.util.Collection,
	 *      org.eclipse.team.svn.ui.extension.factory.ICommentDialogPanel)
	 */
	@SuppressWarnings("static-access")
	public ICommitDialog getCommitDialog(Shell shell, Collection allFilesToCommit,
			ICommentDialogPanel panel) {
		EclipseConsoleLogger.print(className, EclipseConsoleLogger.INFO, "PrecommitHook Version:" + Constants.PRECOMMIT_VERSION );
		boolean validationErrors = false;
		for (Iterator iterator = allFilesToCommit.iterator(); iterator.hasNext();) {
			Object commitFile = iterator.next();
			File eFile = null;
			if (commitFile instanceof File ) {
				eFile = (org.eclipse.core.internal.resources.File) commitFile;

				// validate each .java file with commit validator
				// System.out.println(eFile.getLocation());

				if (commitValidator.validate(getFullPath(eFile.getLocation().toOSString()),
						getSourceFileName(eFile.getLocation().toOSString())) && !validationErrors) {
					EclipseConsoleLogger.print(className, EclipseConsoleLogger.DEBUG, " AllFiles ToCommit:" + allFilesToCommit.size());
					validationErrors = true;
					break;
				}

			} else {
				// this can be removed once we are sure of all the 
				// class types being sent to this commit factory. 
				// for now, it makes sense to have this annoying print statement.
				// - Ganesh 04/30
				EclipseConsoleLogger.print(className, EclipseConsoleLogger.DEBUG, "CommitFile class: " + commitFile.getClass().getName());
			}

		}

		EclipseConsoleLogger.print(className, EclipseConsoleLogger.DEBUG, "About to check for pmd violations = " + validationErrors);
		if (validationErrors) {			
			//EclipseConsoleLogger.print(className, EclipseConsoleLogger.INFO, "You have violations !!! Please fix that - before you commit");
			return blockingCommitActionFactory.getCommitDialog(shell, allFilesToCommit, panel);
		} else {
			EclipseConsoleLogger.print(className, EclipseConsoleLogger.INFO, "You don’t have any violations. You can check-in your code");
			return defaultCommitActionFactory.getCommitDialog(shell, allFilesToCommit, panel);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.svn.ui.extension.factory.ICommitActionFactory#
	 * performAfterCommitTasks
	 * (org.eclipse.team.svn.core.operation.CompositeOperation,
	 * org.eclipse.team.svn.core.operation.IRevisionProvider,
	 * org.eclipse.team.svn.core.operation.IActionOperation[],
	 * org.eclipse.ui.IWorkbenchPart)
	 */
	public void performAfterCommitTasks(CompositeOperation operation,
			IRevisionProvider revisionProvider, IActionOperation[] dependsOn, IWorkbenchPart part) {

		defaultCommitActionFactory.performAfterCommitTasks(operation, revisionProvider, dependsOn,
				part);
	}

	private static String getFullPath(String fullSourceFilepath) {
		if (null == fullSourceFilepath)
			return null;

		return fullSourceFilepath.substring(0, fullSourceFilepath.lastIndexOf("\\"));

	}

	private static String getSourceFileName(String fullSourceFilepath) {
		if (null == fullSourceFilepath)
			return null;

		return fullSourceFilepath.substring(fullSourceFilepath.lastIndexOf("\\") + 1,
				fullSourceFilepath.length());

	}

	

}
