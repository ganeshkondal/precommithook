package org.codegating.subversive.extension;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class EclipseConsoleLogger {
	
	private static boolean isDebug = false;
	private static final String CONSOLE_NAME = "Commit Console";
	private static final MessageConsoleStream out = findConsole(CONSOLE_NAME).newMessageStream();

	// log levels
	public static final int DEBUG = 1;
	public static final int INFO = 0;
	
	/**
	 * Prints the string passed to sysout and to the eclipse console view..
	 * 
	 * @param o
	 *            the o
	 */
	public static void print(String className, int level, Object o) {
		if( level == INFO && null != o) { 
			out.println(o.toString());
			return;
		}
		
		if( level == DEBUG && isDebug && null != o ) { 
			out.println(className + ":" + o.toString());
			printToConsole( o.toString() );
			return;
		}
	}
	
	
	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	private static void printToConsole(String msg) { 
		if( isDebug ) System.out.println( msg );
	}
	
}
