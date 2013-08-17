package org.codegating.subversive.extension;

/**
 * Constants class (intead of a interface). Constants Interface are a no-no; as classes will implement them
 * to get hold of the constants and even get unwanted constants. In addition, such unwanted constants will be 
 * exposed outside by the public classes along with their APIs (unintentional exposure) - goes against 
 * encapsulation desired.
 *  
 * @author Ganesh Kondal
 *
 */
public final class Constants {
	
	
	public static final String PRECOMMIT_VERSION = "v1.0 Built June 01, 2013";
	
	/**
	 * Default configuration values
	 */
	public static final String DEFAULT_ANT_HOME = "c:\\svnhooks\\ant\\bin\\";
	//public static final String DEFAULT_REPORTS_DIR = "c:\\svnhooks\\reports\\";
	public static final String DEFAULT_MAXVIOLATION = "0";
	//public static final String PROPS_FILE_PATH = "c:\\svnhooks\\precommithook.pref";
	public static final String PROPS_FILE_PATH = "platform:/configuration/precommithook/precommithook.pref";
	
	/**
	 * Constant string names, identifying the property names 
	 */
	public static final String PROPS_PRECOMMITPREF_HOME = "platform:/configuration/precommithook/";
	public static final String SYSPROP_ANT_HOME = "ANT_HOME";
	public static final String QUOTES = "\"";
	
	public final static String BUILD_FAILURE = "BUILD FAILED";
	public static final String JAVA = "java";
	public static final String PMD = "PMD";
	public static final String GOT = "Got";
	public static final String SCRIPT_PATH = "platform:/plugin/SubversiveExtension/ant/build.xml";
	
	
	public static final String CMD_COMMAND = "cmd \\/c ";
	public static final String ANT_COMMAND = "ant\" -f ";
	public static final String ANT_SOURCE_DIR = "-Dsource.dir=";
	public static final String ANT_FILE_NAME = "-Dfile.name=";
	public static final String ANT_MAXVIOLATION = "-Dmaxviolation=";
	public static final String ANT_PMDREPORTSDIR = "-Dpmdreports.dir=";
	public static final String ANT_CSREPORTSDIR = "-Dcsreports.dir=";
	public static final String ANT_LIB_OPTION = " -lib ";
	public static final Object ANT_RULESETPATH = "-Druleset.path=";
	public static final Object ANT_CSRULESETPATH = "-Dcsruleset.path=";
	public static final String ANT_DIR = "\\ant";
	public static final String ANT_LIB_DIR = "/lib";
	public static final String ANT_BIN = "bin";
	
	
	// path and/or directories
	public static final String ECLIPSE_CONFIG_PATH = "eclipse.configuration";
	public static final String DIR_PRECOMMITHOOK = "precommithook";
	public static final String DIR_CONFIGURATION = "configuration";
	public static final String DIR_ANT = "ant";
	public static final String DIR_RULESET = "ruleset";
	public static final String DIR_BUILD = "build";
	public static final String DIR_LIB = "lib";
	public static final String DIR_PMDREPORTS = "pmdreports";
	public static final String DIR_CSREPORTS = "csreports";
	
	
	// preferencestore keys
	public static final String PROPS_MAXVIOLATION = "maxViolation"; 
	public static final String PROPS_REPORTSDIR = "reportsDir";
	public static final String PROPS_ANTHOME = "antHome";
	public static final String PROPS_ANT_BUILDXML_PATH = "antBuild.xml";
	public static final String PROPS_ANT_BUILDXML_FILE = "build.xml";
	public static final String PROPS_ANT_LIB_PATH = "ant.lib";
	public static final String PROPS_PMDREPORTS_PATH = "pmdreports.path";
	public static final String PROPS_CSREPORTS_PATH = "csreports.path";
	public static final String PROPS_RULESETPATH = "ruleset.path";
	public static final String PROPS_CSRULESETPATH = "csruleset.path";
	public static final String ECLIPSE_INSTALL_PATH = "eclipse.installpath";
	public static final String PROPS_RULESETXML_FILE = "pmdruleset.xml";
	public static final String PROPS_CSRULESETXML_FILE = "csruleset.xml";
	
	
	
}
