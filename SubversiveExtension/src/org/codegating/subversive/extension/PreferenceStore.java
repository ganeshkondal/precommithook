package org.codegating.subversive.extension;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * Holds preferences for the Subversive extension
 * 
 * @author Ganesh Kondal
 * 
 */
public class PreferenceStore {
	private static final String className = PreferenceStore.class.getName();
	private static Properties props = new Properties();
	private static boolean extracted = false;

	static {

		try {
			extractDependentFiles();
			loadProperties();
		} catch (Exception e) {
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG,
					"Exception loading properties, extracting dependent files..." + e.toString());
		}

	}

	public static String get(String key) {
		if( null == key ) return null;
		System.out.println(">>>>> In get..." + key );
		// switch on string is only available from JDK 1.7; so doing with multiple if blocks.
		if( props.get( key ) == null ) { 
			
			// case 1: Check for ANT within eclipse
			if( key.equalsIgnoreCase( Constants.PROPS_ANTHOME ) ) { 

				try {
					// case 1: Check for ANT within the eclipse installation					
					// get Eclipses' ANT plugin path
					Bundle antBundle = Platform.getBundle("org.apache.ant");
					if( null != antBundle && antBundle.getLocation() != null  ) { 
	
						EclipseConsoleLogger.print( className, EclipseConsoleLogger.INFO, "Found ANT binary at " + antBundle.getLocation() );
						URL buildUrl = FileLocator.find(antBundle, new Path("bin"), null);						
						
						// below line gets an exception 
						//File buildFile = new java.io.File( FileLocator.toFileURL( buildUrl).toString() );
						File buildFile = new java.io.File( FileLocator.resolve(buildUrl).getPath() );
						String eclipseAntPath = buildFile.getAbsolutePath();
						EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG, "buildFile.getAbsolutePath():" + eclipseAntPath );
						
						String eclipseAntPath2 = FileLocator.resolve(buildUrl).getPath();					
						EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG, "FileLocator.resolve(buildUrl).getPath():" + eclipseAntPath2 );
						//props.put( Constants.PROPS_ANTHOME, (eclipseAntPath.substring(1, eclipseAntPath.length())));
						props.put( Constants.PROPS_ANTHOME, eclipseAntPath );
						return props.getProperty(Constants.PROPS_ANTHOME);
					}



					// case 2: Check for ANT within the System via ANT_HOME Property
					String antHomePath = System.getenv(Constants.SYSPROP_ANT_HOME )  ;
					if(  antHomePath != null ) {
						String antBinPath = antHomePath + File.separator + Constants.ANT_BIN;
						EclipseConsoleLogger.print( className, EclipseConsoleLogger.INFO, "Found ANT_HOME binary at2 " + antBinPath );
						props.put( Constants.PROPS_ANTHOME, antBinPath );					
						return (String) props.get( Constants.PROPS_ANTHOME );
					} else { 
						// nothing is present - so expecting ant in a predetermined slot
						EclipseConsoleLogger.print( className, EclipseConsoleLogger.INFO, "Neither ANT_HOME is set, nor the antHome preference is given, so expecting ANT in " + Constants.DEFAULT_ANT_HOME );
						return Constants.DEFAULT_ANT_HOME;
					
					}		
				} catch (IOException e) {
					EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG, "Exception: " + e.toString() );
					return null;
				}					
	


			}
			
			// KEY 2: 
			if( key.equalsIgnoreCase( Constants.PROPS_PMDREPORTS_PATH ) ) {
				EclipseConsoleLogger.print( className, EclipseConsoleLogger.INFO, "Please find the PMD violations reported in the HTML file @ " + getDefaultReportsPath() );
				//TODO: get installLocation/ precommithook/
				return getDefaultReportsPath();
			}
			
			// KEY 3: 
			if( key.equalsIgnoreCase( Constants.PROPS_MAXVIOLATION ) ) { 
				return Constants.DEFAULT_MAXVIOLATION;
			} 
			
			// KEY 4:
			if( key.equalsIgnoreCase( Constants.PROPS_CSREPORTS_PATH ) ) {
				EclipseConsoleLogger.print( className, EclipseConsoleLogger.INFO, "Please find the CS violations reported in the XML file @ " + getDefaultCSReportsPath() );
				//TODO: get installLocation/ precommithook/
				return getDefaultCSReportsPath();
			}			
			
		} else { // 2nd run - when the data is expected to be filled within the properties file.
			//EclipseConsoleLogger.print( className, EclipseConsoleLogger.INFO, "Got " + (String)props.get( key ) + " for property " + key );
			return (String)props.get( key );
		}
		
		return null;
	}

	/**
	 * Extracts the build.xml file and sets
	 * 
	 * @return
	 * @throws Exception
	 */
	private static boolean extractDependentFiles() throws Exception {
		// check for director
		// platform:/configuration/precommithook/precommithook.pref
		// if it doesn't exist then call extractDepdendentPluginFiles
		// if they exist already - leave it and continue.
		URL installURL = Platform.getInstallLocation().getURL();

		// create reports directory
		String reportsPath = FileLocator.toFileURL(installURL).getPath() + "precommithook"
				+ File.separator + "pmdreports";
		File reportsDir = new File(reportsPath);
/*		if (reportsDir.exists()) {
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG,
					"extraction of files ha s already happened, so returning ");
		}
*/		reportsDir.mkdirs();
		// create reports directory for checkstyle
		String csReportsPath = FileLocator.toFileURL(installURL).getPath() + "precommithook"
				+ File.separator + "csreports";
		File csReportsDir = new File(csReportsPath);
		csReportsDir.mkdirs();
		// config filepath
		String configFilePath = FileLocator.toFileURL(installURL).getPath() + "configuration"
				+ File.separator + "precommithook" + File.separator;

		// 1. resources/ant/build.xml
		readAndWriteContent("resources/ant/build.xml", getBuildPath());

		// 2. resources/pmdruleset file
		String rulesetFilePath = configFilePath + File.separator + "ruleset" + File.separator
				+ "pmdruleset.xml";
		readAndWriteContent("resources/ruleset/PMDRuleset.xml", rulesetFilePath);

		// 3. resources/ant/lib jar files - jaxen.jar pmd.jar
		String pmdJarPath = configFilePath + File.separator + "ant" + File.separator + "lib"
				+ File.separator + "pmd.jar";
		readAndWriteContent("resources/ant/lib/pmd-5.0.2.jar", pmdJarPath);

		String jaxenJarPath = configFilePath + File.separator + "ant" + File.separator + "lib"
				+ File.separator + "jaxen.jar";
		readAndWriteContent("resources/ant/lib/jaxen-1.1.1.jar", jaxenJarPath);
		
		String asmJarPath = configFilePath + File.separator + "ant" + File.separator + "lib"
				+ File.separator + "asm.jar";
		readAndWriteContent("resources/ant/lib/asm-3.2.jar", asmJarPath);
		//4. resources/csruleset file for checkstyle
		String checkstyleRulesetFilePath = configFilePath + File.separator + "ruleset" + File.separator
				+ "csruleset.xml";
		readAndWriteContent("resources/ruleset/CSRuleset.xml", checkstyleRulesetFilePath);
		//5. checkstyle.jar
		String checkstyleJarPath = configFilePath + File.separator + "ant" + File.separator + "lib"
				+ File.separator + "checkstyle.jar";
		readAndWriteContent("resources/ant/lib/checkstyle-5.6-all.jar", checkstyleJarPath);		
		extracted = true;
		return extracted;
	}



	private static void readAndWriteContent(String resourcePath, String toPath) {
		EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG, "From:" + resourcePath + " toPath2:"
				+ toPath);
		URL url = Platform.getBundle("SubversiveExtension").getEntry(resourcePath);
		java.io.File file, destinationFile = null;
		BufferedInputStream buffInputStream = null;
		BufferedOutputStream writer = null;
		try {
			//file = new File(FileLocator.resolve(url).toURI());
			//buffInputStream = new BufferedInputStream(new FileInputStream(file));
			
			InputStream inputStream = url.openConnection().getInputStream();
			buffInputStream = new BufferedInputStream( inputStream );
			

			int readByte = 0;

			// destination file.
			// destinationFile = new File
			// (FileLocator.resolve(destinationURL).toURI());
			destinationFile = new File(toPath);

			if (destinationFile.exists()) {
				EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG,
						"Extraction has already happened for " + resourcePath + " so returning");
				return;
			}

			destinationFile.getParentFile().mkdirs();

			// create directory
			writer = new BufferedOutputStream(new FileOutputStream(destinationFile));

			while ((readByte = buffInputStream.read()) != -1) {
				// print( readByte );
				writer.write(readByte);
			}
		} catch (IOException ioe) {
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.INFO, "Error extracting " + toPath
					+ ":" + ioe.toString());
		} /*catch (URISyntaxException e) {
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.INFO, "Error extracting " + toPath
					+ ":" + e.toString());
		}*/ catch (Exception e) { 
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.INFO, "Exception extracting" + toPath
					+ ":" + e.getMessage());
		} finally {
			// close file handles.
			try {
				if (null != buffInputStream)
					buffInputStream.close();
				if (null != writer)
					writer.close();
			} catch (IOException e) {
				EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG,
						"Can Ignore this!!! Exception closing file handles :" + e.toString());
			}
		}
	}

	private static void loadProperties() {
		try {
			File propsFile = new File(Constants.PROPS_FILE_PATH);

			if (propsFile.exists()) {
				EclipseConsoleLogger.print( className, EclipseConsoleLogger.INFO,
						"Loading precommit hook properties from " + Constants.PROPS_FILE_PATH);
				props.load(new FileInputStream(new File(Constants.PROPS_FILE_PATH)));
			} else {
				EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG,
						"No precommit hook properties to load, about to use default values");
			}

			
		} catch (FileNotFoundException e) {
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG, e.toString());
		} catch (IOException e) {
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG, e.toString());
		}
	}

	// -- some test main code to do quick checks.
	public static void main(String[] args) {
		System.out.println(get(Constants.PROPS_ANTHOME));
		System.out.println(System.getenv(Constants.SYSPROP_ANT_HOME));
		System.out.println(System.getenv("PATH"));

	}

	/**
	 * Returns the reference to
	 * eclipseInstallLocation/configuration/precommithook/ant/build.xml
	 * 
	 * @return
	 */
	public static String getBuildPath() {
		if (props.getProperty(Constants.PROPS_ANT_BUILDXML_PATH) == null) {
			String buildXMLPath = getEclipseConfigPath() + Constants.DIR_ANT + File.separator
					+ Constants.DIR_BUILD + File.separator + Constants.PROPS_ANT_BUILDXML_FILE;
			
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG, "ANT BUILD PATH: " + buildXMLPath );
			props.put(Constants.PROPS_ANT_BUILDXML_PATH, buildXMLPath);
		}

		return props.getProperty(Constants.PROPS_ANT_BUILDXML_PATH);
	}

	/**
	 * Returns the reference to
	 * eclipseInstallLocation/configuration/precommithook/ruleset/pmdruleset.xml
	 * 
	 * @return
	 */
	public static String getRulesetPath() {
		if (props.getProperty(Constants.PROPS_RULESETPATH) == null) {
			String rulesetXMLPath = Constants.QUOTES + getEclipseConfigPath() +  Constants.DIR_RULESET + File.separator
					+ Constants.PROPS_RULESETXML_FILE + Constants.QUOTES; 
			props.put(Constants.PROPS_RULESETPATH, rulesetXMLPath);
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG, "rulesetXMLPath : " + rulesetXMLPath );
		}

		EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG,
				"rulesetPath: " + props.getProperty(Constants.PROPS_RULESETPATH));
		return props.getProperty(Constants.PROPS_RULESETPATH);
	}
	
	/**
	 * Returns the reference to
	 * eclipseInstallLocation/configuration/precommithook/ruleset/csruleset.xml
	 * 
	 * @return
	 */
	public static String getCSRulesetPath() {
		if (props.getProperty(Constants.PROPS_CSRULESETPATH) == null) {
			String csrulesetXMLPath = Constants.QUOTES + getEclipseConfigPath() +  Constants.DIR_RULESET + File.separator
					+ Constants.PROPS_CSRULESETXML_FILE + Constants.QUOTES; 
			props.put(Constants.PROPS_CSRULESETPATH, csrulesetXMLPath);
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG, "csrulesetXMLPath : " + csrulesetXMLPath );
		}

		EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG,
				"csrulesetPath: " + props.getProperty(Constants.PROPS_CSRULESETPATH));
		return props.getProperty(Constants.PROPS_CSRULESETPATH);
	}

	/**
	 * Returns the reference to
	 * eclipseInstallLocation/configuration/precommithook/ant/lib
	 * 
	 * @return
	 */
	public static String getLibraryPath() {
		if (props.getProperty(Constants.PROPS_ANT_LIB_PATH) == null) {
			String antLibPath = Constants.QUOTES + getEclipseConfigPath() +  Constants.DIR_ANT + File.separator
					+ Constants.DIR_LIB + Constants.QUOTES;
			props.put(Constants.PROPS_ANT_LIB_PATH, antLibPath);
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG, "antLibPath : " + antLibPath );
		}

		return props.getProperty(Constants.PROPS_ANT_LIB_PATH);
	}

	/**
	 * Returns the reference to eclipseInstallLocation/precommithook/reports
	 * 
	 * @return
	 */
	public static String getDefaultReportsPath() {
		if (props.getProperty(Constants.PROPS_PMDREPORTS_PATH) == null) {
			String pmdReportsPath = Constants.QUOTES + getEclipseInstallLocation() + File.separator
					+ Constants.DIR_PRECOMMITHOOK + File.separator + Constants.DIR_PMDREPORTS + Constants.QUOTES ;
			props.put(Constants.PROPS_PMDREPORTS_PATH, pmdReportsPath);
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG, "pmdReportsPath : " + pmdReportsPath );
		}

		return props.getProperty(Constants.PROPS_PMDREPORTS_PATH);
	}
	
	/**
	 * Returns the reference to eclipseInstallLocation/precommithook/csreports
	 * 
	 * @return
	 */
	public static String getDefaultCSReportsPath() {
		if (props.getProperty(Constants.PROPS_CSREPORTS_PATH) == null) {
			String csReportsPath = Constants.QUOTES + getEclipseInstallLocation() + File.separator
					+ Constants.DIR_PRECOMMITHOOK + File.separator + Constants.DIR_CSREPORTS + Constants.QUOTES ;
			props.put(Constants.PROPS_CSREPORTS_PATH, csReportsPath);
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG, "csReportsPath : " + csReportsPath );
		}

		return props.getProperty(Constants.PROPS_CSREPORTS_PATH);
	}

	private static String getEclipseConfigPath() {
		if (null != props && props.get(Constants.ECLIPSE_CONFIG_PATH) != null) {
			return props.getProperty(Constants.ECLIPSE_CONFIG_PATH);

		}
		try {
			URL installURL = Platform.getInstallLocation().getURL();
			URI uri = new URI(installURL.getProtocol(), installURL.getUserInfo(), installURL.getHost(), installURL.getPort(), installURL.getPath(), installURL.getQuery(), installURL.getRef());
			File configFile = new File( FileLocator.toFileURL( uri.toURL() ).toURI() );
			String configFilePath = configFile.getAbsolutePath() + File.separator + "configuration"
					+ File.separator + "precommithook" + File.separator;
			// String configFilePath = getPath( installURL ) + File.separator +
			// "configuration" + File.separator + "precommithook" +
			// File.separator;
			props.put(Constants.ECLIPSE_CONFIG_PATH, configFilePath );
			return configFilePath;
		} catch (Exception e) {
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG,
					"Exception getting eclipse.configuration path " + e.getMessage() );
			return null;
		}

	}

	private static String getEclipseInstallLocation() {
		if (null != props && props.get(Constants.ECLIPSE_INSTALL_PATH) != null) {
			return props.getProperty(Constants.ECLIPSE_INSTALL_PATH);

		}
		try {
			URL installURL = Platform.getInstallLocation().getURL();
		
	//		File installFolder = new File( FileLocator.toFileURL(installURL).toURI() );	// );
			File installFolder = new File( FileLocator.resolve(installURL).getPath() );	
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG,
					"EclipseInstall Path:" + installFolder.getAbsolutePath() );
			props.put(Constants.ECLIPSE_INSTALL_PATH, installFolder.getAbsolutePath() );
			return props.getProperty( Constants.ECLIPSE_INSTALL_PATH );
		} catch (Exception e) {
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG,
					"Exception getting eclipse.configuration path");
			return null;
		}

	}

	private static String getPath22(URL url) {
		File buildFile;
		try {
			buildFile = new java.io.File(FileLocator.toFileURL(url).toURI());
			return buildFile.getAbsolutePath();
		} catch (URISyntaxException e) {
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG,
					" Exception retrieving actual path:" + e.toString());
		} catch (IOException e) {
			EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG,
					" Exception retrieving actual path:" + e.toString());
		}
		return null;

	}

}

/*
 * private static void readAndWriteContent2(String resourcePath, String
 * toPath ) { EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG, "From:"
 * + resourcePath + " toPath:" + toPath ); URL url =
 * Platform.getBundle("SubversiveExtension").getEntry( resourcePath ); //URL
 * destinationURL = Platform.getBundle("SubversiveExtension").getEntry(
 * toPath );
 * 
 * java.io.File file, destinationFile = null; BufferedReader buffReader =
 * null; BufferedWriter writer = null; try { file = new File
 * (FileLocator.resolve(url).toURI() ); buffReader = new BufferedReader( new
 * FileReader( file ) );
 * 
 * String fileContent = null;
 * 
 * // destination file. //destinationFile = new File
 * (FileLocator.resolve(destinationURL).toURI()); destinationFile = new File
 * ( toPath ); // create directory writer = new BufferedWriter( new
 * FileWriter( destinationFile ));
 * 
 * while( (fileContent = buffReader.readLine()) != null ) {
 * EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG, fileContent );
 * writer.write( fileContent ); } } catch(IOException ioe) {
 * EclipseConsoleLogger.print( className, EclipseConsoleLogger.INFO, "Error extracting "
 * + toPath + ":" + ioe.toString() ); } catch (URISyntaxException e) {
 * EclipseConsoleLogger.print( className, EclipseConsoleLogger.INFO, "Error extracting "
 * + toPath + ":" + e.toString() );
 * 
 * } finally { // close file handles. try { if( null != buffReader)
 * buffReader.close(); if( null != writer ) writer.close(); } catch
 * (IOException e) { EclipseConsoleLogger.print( className, EclipseConsoleLogger.DEBUG,
 * "Can Ignore this!!! Exception closing file handles :" + e.toString() ); }
 * } }
 */
