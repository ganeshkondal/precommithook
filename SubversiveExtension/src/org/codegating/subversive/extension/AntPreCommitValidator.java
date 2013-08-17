package org.codegating.subversive.extension;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Validates the source files about to checked in, using ant pmd task. In this
 * version of the validator, the ant files along with rulesets are expected
 * within c:\svnhooks directory.
 * 
 * In the upcoming updates, a Window-Preferences or a property setting for the
 * plugin will be exposed.
 * 
 * @author Ganesh Kondal
 * @version 0.3
 * 
 * 
 */
public class AntPreCommitValidator {
	
	private static final String className = AntPreCommitValidator.class.getName();
		
	/**
	 * Copy the dependent jars of this plugins' PMD task (pmd-5.0.2.jar & jaxen..jar) to 
	 * platform:/configuration/precommithook/
	 * 										precommithook.pref
	 * 										ant/lib/build.xml
	 * 										pmd/pmdruleset.xml
	 * 										
	 * @return
	 */
	private static boolean extractDependentPluginFiles() { 
		URL url;
		try {
		        url = new URL("platform:/plugin/SubversiveExtension/ant/build.xml");
		    InputStream inputStream = url.openConnection().getInputStream();
		    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		    String inputLine;
		 
		    while ((inputLine = in.readLine()) != null) {
		        System.out.println(inputLine);
		    }
		 
		    in.close();
		 
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		return true;
	}
	

	public static boolean validate(String filePath, String fileName) {
		//EclipseConsoleLogger.print(EclipseConsoleLogger.DEBUG, "SCRIPT_PATH is: " + Constants.SCRIPT_PATH);
		Process buildResults = null;
		try { 
			if (null == fileName || null == filePath || !fileName.contains(Constants.JAVA))
				return false;
								
			// stringbuilder is non synchronized version of StringBuffer. Provides better performance.
			StringBuilder antCmd = new StringBuilder().append( Constants.CMD_COMMAND ) 
					.append( Constants.QUOTES )
					.append( Constants.QUOTES )
					.append( PreferenceStore.get( Constants.PROPS_ANTHOME ))                  // ant bin path
					.append( File.separator )                                                 // separator
					.append(Constants.ANT_COMMAND ).append( Constants.QUOTES )
					.append( PreferenceStore.getBuildPath())   // ant" -f build.xml path
					.append( Constants.QUOTES )
					.append(" ")
					.append(Constants.ANT_SOURCE_DIR).append( Constants.QUOTES ).append( filePath ) // filepath for PMD
					.append( Constants.QUOTES )
					.append(" ")
					.append(Constants.ANT_FILE_NAME).append( fileName ) // file to be analyzed by PMD
					.append(" ")
					.append(Constants.ANT_MAXVIOLATION ).append( PreferenceStore.get( Constants.PROPS_MAXVIOLATION ))
					.append(" ")
					.append(Constants.ANT_RULESETPATH ).append( PreferenceStore.getRulesetPath())
					.append(" ")
					.append(Constants.ANT_CSRULESETPATH ).append( PreferenceStore.getCSRulesetPath()) // for checkstyle rules reference
					.append(" ")
					.append(Constants.ANT_PMDREPORTSDIR)
					//.append( Constants.QUOTES )
					.append( PreferenceStore.get( Constants.PROPS_PMDREPORTS_PATH ) )
					//.append( Constants.QUOTES )
					.append(" ")
					.append(Constants.ANT_CSREPORTSDIR)
					//.append( Constants.QUOTES )
					.append( PreferenceStore.get( Constants.PROPS_CSREPORTS_PATH ) )
					//.append( Constants.QUOTES )
					.append(" ")					
					.append( Constants.ANT_LIB_OPTION )
					.append( PreferenceStore.getLibraryPath() )
					.append( Constants.QUOTES );
					

			EclipseConsoleLogger.print(className, EclipseConsoleLogger.DEBUG, antCmd.toString() );
			// call the ant script with the filepath and filename
			buildResults = Runtime.getRuntime().exec( antCmd.toString() );
					
			return hasPMDViolations(buildResults);

		} catch (IOException e) {
			EclipseConsoleLogger.print(className, EclipseConsoleLogger.DEBUG, "IOException :" + e.toString());
			return false;
		}
	}
	
	private static String removeFirstSlashChar(String path) { 
		EclipseConsoleLogger.print(className, EclipseConsoleLogger.DEBUG, "PRE>> " + path );
		if( null != path && path.length() > 1 && path.charAt(0)=='/' ) {
			int end = path.length()-1;
			int start = 1;
			String postRemovingSlash = new String( path.substring(start, end ));
			return postRemovingSlash;
		}
		
		return path;
		
	}

	/**
	 * Check the build results for PMD violations. If there are violations, then
	 * return false, else return true;
	 * 
	 * @param buildResults
	 * @return
	 */
	private static boolean hasPMDViolations(Process buildResults) {
		
		boolean hasPMDViolations = false;
		if (null == buildResults) {
			EclipseConsoleLogger.print(className, EclipseConsoleLogger.DEBUG, "build results is null, so returning true for violations. Check why there is no build result");
			hasPMDViolations = true;
			return hasPMDViolations;
		}

		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					buildResults.getInputStream()));

			String line = reader.readLine();
			// without going through this stream, unable to review the error
			// stream directly
			while ((line = reader.readLine()) != null) {
				EclipseConsoleLogger.print(className, EclipseConsoleLogger.DEBUG, line);
			}

			// read and parse the error stream.
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(
					buildResults.getErrorStream()));

			String errorLines = errorReader.readLine();
			while (errorLines != null) {
				
				if (errorLines.contains( Constants.BUILD_FAILURE )) {
					EclipseConsoleLogger.print(className, EclipseConsoleLogger.DEBUG, "About to send true for hasPMDViolations");
					hasPMDViolations = true;
					//break;
				}
				errorLines = errorReader.readLine();
				EclipseConsoleLogger.print(className, EclipseConsoleLogger.DEBUG, "errorlines : " + errorLines);
				EclipseConsoleLogger.print(className, EclipseConsoleLogger.INFO, errorLines);
				
				if (errorLines != null && errorLines.contains( Constants.PMD )) {
					EclipseConsoleLogger.print(className, EclipseConsoleLogger.INFO, "You have PMD violations !!! Please fix that - before you commit");
				}
				
				if (errorLines != null && errorLines.contains( Constants.GOT )) {
					EclipseConsoleLogger.print(className, EclipseConsoleLogger.INFO, "You have Checkstyle violations !!! Please fix that - before you commit");
				}
			}

		} catch (IOException ioe) {
			EclipseConsoleLogger.print(className, EclipseConsoleLogger.DEBUG, ioe.toString());
		}
		EclipseConsoleLogger.print(className, EclipseConsoleLogger.DEBUG, "About to return " + hasPMDViolations);
		return hasPMDViolations;

	}

	/**
	 * some test main code.,
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		validate("D:\\SVNRepo\\PMDExecute\\src\\com\\cts\\pmd\\", "PMDExecutor.java");
	}
}
