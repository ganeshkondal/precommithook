/**
 * 
 */
package org.codegating.subversive.extension;

import java.util.Collection;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.ui.extension.factory.ICommentDialogPanel;
import org.eclipse.team.svn.ui.extension.factory.ICommitDialog;
import org.eclipse.team.svn.ui.extension.impl.DefaultCommitActionFactory;

/**
 * A commit action factory implementation which essentially blocks the message
 * dialog from opening gracefully, even though there are files that are eligible
 * (in SVN context) to be checked in.
 * 
 * @author Ganesh Kondal
 * @version 0.2
 * 
 */
public class BlockingCommitActionFactory extends DefaultCommitActionFactory {
	private static final String className = BlockingCommitActionFactory.class.getName();
	@Override
	public ICommitDialog getCommitDialog(final Shell shell, Collection allFilesToCommit,
			final ICommentDialogPanel commentPanel) {

		return new ICommitDialog() {

			public String getMessage() {
				return commentPanel.getMessage();
			}

			public int open() {
				EclipseConsoleLogger.print(className, EclipseConsoleLogger.DEBUG, "About to return 1, so commit dialog won't come :) ");
				// anything but zero is supposed to be handled as a no-commit
				return 1;
			}

		};
	}

}
