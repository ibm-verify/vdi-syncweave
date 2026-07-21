/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.webservice.axis2;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.extension.PackageFinder;
import org.apache.axis2.wsdl.codegen.extension.SimpleDBExtension;
import org.apache.axis2.wsdl.codegen.extension.WSDLValidatorExtension;
import org.apache.axis2.wsdl.codegen.extension.XMLBeansExtension;

import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;

/**
 * Command line interface to the Axis2 WSDL2Java utility.
 * This class replaces the original WSDL2JavaNoSystemExit class.
 */
public class WSDL2Java {
    /**
     * Copyright.
     */
    @SuppressWarnings("unused")
    private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
    
    /**
     * Component properties.
     */
    private static final String PROPERTIES_FILE = "webserviceutil";
    
    /**
     * NLS Property set holding name-value pairs for the resource.
     */
    private static final ResourceHash sResHash = new ResourceHash(PROPERTIES_FILE);
    
    /**
     * Prefix for error messages.
     */
    private static final String WSDL_TO_JAVA_PREFIX = "WSDL2Java: ";
    
    /**
     * WSDL URI.
     */
    private String wsdlURI;
    
    /**
     * Output directory.
     */
    private String outputDir;
    
    /**
     * Package name.
     */
    private String packageName;
    
    /**
     * Databinding type.
     */
    private String databindingType = "adb";
    
    /**
     * Generate server side code.
     */
    private boolean generateServerSide = false;
    
    /**
     * Generate test case.
     */
    private boolean generateTestCase = false;
    
    /**
     * Generate all.
     */
    private boolean generateAll = false;
    
    /**
     * Unwrap parameters.
     */
    private boolean unwrapParams = false;
    
    /**
     * Namespace to package mapping.
     */
    private List<String> namespaceToPackageMappings = new ArrayList<String>();
    
    /**
     * Username for authentication.
     */
    private String username;
    
    /**
     * Password for authentication.
     */
    private String password;
    
    /**
     * Logger.
     */
    private Log log;
    
    /**
     * Default constructor.
     */
    public WSDL2Java() {
        try {
            log = new Log("WSDL2Java");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Generates Java code from WSDL.
     * 
     * @param args Command line arguments
     * @throws Exception if an error occurs
     */
    public void generate(String[] args) throws Exception {
        parseCommandLineOptions(args);
        validateOptions();
        generateCode();
    }
    
    /**
     * Parses command line options.
     * 
     * @param args Command line arguments
     * @throws Exception if an error occurs
     */
    private void parseCommandLineOptions(String[] args) throws Exception {
        CommandLineOptionParser parser = new CommandLineOptionParser(args);
        // In Axis2 1.7.6, getAllOptions() returns a Map instead of a List
        Map<String, CommandLineOption> commandLineOptions = parser.getAllOptions();
        
        for (CommandLineOption option : commandLineOptions.values()) {
            String optionType = option.getOptionType();
            
            if (CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION.equals(optionType)) {
                wsdlURI = option.getOptionValue();
            } else if (CommandLineOptionConstants.WSDL2JavaConstants.OUTPUT_LOCATION_OPTION.equals(optionType)) {
                outputDir = option.getOptionValue();
            } else if (CommandLineOptionConstants.WSDL2JavaConstants.PACKAGE_OPTION.equals(optionType)) {
                packageName = option.getOptionValue();
            } else if ("db".equals(optionType) || "-db".equals(optionType)) {
                // Using string literals instead of constants that might have changed
                databindingType = option.getOptionValue();
            } else if (CommandLineOptionConstants.WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION.equals(optionType)) {
                generateServerSide = true;
            } else if (CommandLineOptionConstants.WSDL2JavaConstants.GENERATE_TEST_CASE_OPTION.equals(optionType)) {
                generateTestCase = true;
            } else if (CommandLineOptionConstants.WSDL2JavaConstants.GENERATE_ALL_OPTION.equals(optionType)) {
                generateAll = true;
            } else if (CommandLineOptionConstants.WSDL2JavaConstants.UNWRAP_PARAMETERS.equals(optionType)) {
                unwrapParams = true;
            } else if (CommandLineOptionConstants.WSDL2JavaConstants.NAME_SPACE_TO_PACKAGE_OPTION.equals(optionType)) {
                // Handle ArrayList return type in Axis2 1.7.6
                ArrayList<String> valuesList = option.getOptionValues();
                if (valuesList != null && valuesList.size() >= 2) {
                    namespaceToPackageMappings.add(valuesList.get(0) + "=" + valuesList.get(1));
                }
            } else if ("u".equals(optionType) || "-u".equals(optionType)) {
                // Using string literals instead of constants that might have changed
                username = option.getOptionValue();
            } else if ("p".equals(optionType) || "-p".equals(optionType)) {
                // Using string literals instead of constants that might have changed
                password = option.getOptionValue();
            }
        }
        
        // Check for authentication info in the URL
        checkForAuthInfo(wsdlURI);
    }
    
    /**
     * Validates command line options.
     * 
     * @throws Exception if validation fails
     */
    private void validateOptions() throws Exception {
        if (wsdlURI == null) {
            throw new Exception(WSDL_TO_JAVA_PREFIX + sResHash.getString("WSUTIL.WSDL2JAVA.WSDL.URI.REQUIRED"));
        }
        
        if (outputDir == null) {
            outputDir = ".";
        }
        
        if (!databindingType.equals("adb") && !databindingType.equals("xmlbeans") &&
                !databindingType.equals("jibx") && !databindingType.equals("none")) {
            throw new Exception(WSDL_TO_JAVA_PREFIX + sResHash.getString("WSUTIL.WSDL2JAVA.INVALID.DATABINDING.TYPE", databindingType));
        }
    }
    
    /**
     * Generates Java code from WSDL.
     * 
     * @throws Exception if code generation fails
     */
    private void generateCode() throws Exception {
        try {
            // Create the options list for code generation
            List<CommandLineOption> optionList = new ArrayList<CommandLineOption>();
            
            // Add WSDL URI
            CommandLineOption wsdlOption = new CommandLineOption(
                    CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION,
                    new String[] { wsdlURI });
            optionList.add(wsdlOption);
            
            // Add output directory
            CommandLineOption outOption = new CommandLineOption(
                    CommandLineOptionConstants.WSDL2JavaConstants.OUTPUT_LOCATION_OPTION,
                    new String[] { outputDir });
            optionList.add(outOption);
            
            // Add package name if specified
            if (packageName != null) {
                CommandLineOption packageOption = new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.PACKAGE_OPTION,
                        new String[] { packageName });
                optionList.add(packageOption);
            }
            
            // Add databinding type
            CommandLineOption databindingOption = new CommandLineOption(
                    "db", // Using string literal instead of constant that might have changed
                    new String[] { databindingType });
            optionList.add(databindingOption);
            
            // Add server-side code generation option if requested
            if (generateServerSide) {
                CommandLineOption serverSideOption = new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION,
                        new String[0]);
                optionList.add(serverSideOption);
            }
            
            // Add test case generation option if requested
            if (generateTestCase) {
                CommandLineOption testCaseOption = new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.GENERATE_TEST_CASE_OPTION,
                        new String[0]);
                optionList.add(testCaseOption);
            }
            
            // Add generate all option if requested
            if (generateAll) {
                CommandLineOption allOption = new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.GENERATE_ALL_OPTION,
                        new String[0]);
                optionList.add(allOption);
            }
            
            // Add unwrap parameters option if requested
            if (unwrapParams) {
                CommandLineOption unwrapOption = new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.UNWRAP_PARAMETERS,
                        new String[0]);
                optionList.add(unwrapOption);
            }
            
            // Add namespace to package mappings
            for (String mapping : namespaceToPackageMappings) {
                String[] parts = mapping.split("=");
                if (parts.length == 2) {
                    CommandLineOption nsOption = new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.NAME_SPACE_TO_PACKAGE_OPTION,
                            new String[] { parts[0], parts[1] });
                    optionList.add(nsOption);
                }
            }
            
            // Add authentication options if specified
            if (username != null) {
                CommandLineOption userOption = new CommandLineOption(
                        "u", // Using string literal instead of constant that might have changed
                        new String[] { username });
                optionList.add(userOption);
            }
            
            if (password != null) {
                CommandLineOption passwordOption = new CommandLineOption(
                        "p", // Using string literal instead of constant that might have changed
                        new String[] { password });
                optionList.add(passwordOption);
            }
            
            // Convert our options to a String[] array for the CommandLineOptionParser
            List<String> argsList = new ArrayList<String>();
            for (CommandLineOption option : optionList) {
                argsList.add("-" + option.getOptionType());
                // In Axis2 1.7.6, getOptionValues() returns ArrayList<Object>
                ArrayList<?> values = option.getOptionValues();
                if (values != null) {
                    for (Object value : values) {
                        if (value != null) {
                            argsList.add(value.toString());
                        }
                    }
                }
            }
            String[] argsArray = argsList.toArray(new String[0]);
            
            // Create a new CommandLineOptionParser with our options
            CommandLineOptionParser parser = new CommandLineOptionParser(argsArray);
            
            // Create the code generation configuration
            // In Axis2 1.8.2, CodeGenConfiguration has a no-arg constructor
            // and we populate it using the parser's options
            CodeGenConfiguration config = new CodeGenConfiguration();
            
            // Populate the configuration from the parser
            Map<String, CommandLineOption> optionsMap = parser.getAllOptions();
            for (Map.Entry<String, CommandLineOption> entry : optionsMap.entrySet()) {
                config.putProperty(entry.getKey(), entry.getValue());
            }
            
            // Create the code generation engine with the configuration
            CodeGenerationEngine codegenEngine = new CodeGenerationEngine(config);
            // Note: JiBX and other databinding types may require additional extensions
            // that are not included here
            
            // Generate the code
            codegenEngine.generate();
            
            if (log != null) {
                log.loginfo(sResHash.getString("WSUTIL.WSDL2JAVA.CODE.GENERATION.COMPLETED"));
            }
        } catch (CodeGenerationException e) {
            if (log != null) {
                log.logerror(sResHash.getString("WSUTIL.WSDL2JAVA.CODE.GENERATION.FAILED"), e);
            }
            throw e;
        }
    }
    
    /**
     * Checks for authentication information in the URI.
     * 
     * @param uri The URI to check
     */
    private void checkForAuthInfo(String uri) {
        if (uri == null) {
            return;
        }
        
        URL url = null;
        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            return;
        }
        
        String userInfo = url.getUserInfo();
        if (userInfo != null) {
            int i = userInfo.indexOf(':');
            if (i >= 0) {
                username = userInfo.substring(0, i);
                password = userInfo.substring(i + 1);
            } else {
                username = userInfo;
            }
        }
    }
    
    /**
     * Sets the WSDL URI.
     * 
     * @param wsdlURI The WSDL URI
     */
    public void setWsdlURI(String wsdlURI) {
        this.wsdlURI = wsdlURI;
    }
    
    /**
     * Sets the output directory.
     * 
     * @param outputDir The output directory
     */
    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
    
    /**
     * Sets the package name.
     * 
     * @param packageName The package name
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    /**
     * Sets the databinding type.
     * 
     * @param databindingType The databinding type
     */
    public void setDatabindingType(String databindingType) {
        this.databindingType = databindingType;
    }
    
    /**
     * Sets whether to generate server-side code.
     * 
     * @param generateServerSide Whether to generate server-side code
     */
    public void setGenerateServerSide(boolean generateServerSide) {
        this.generateServerSide = generateServerSide;
    }
    
    /**
     * Sets whether to generate test cases.
     * 
     * @param generateTestCase Whether to generate test cases
     */
    public void setGenerateTestCase(boolean generateTestCase) {
        this.generateTestCase = generateTestCase;
    }
    
    /**
     * Sets whether to generate all code.
     * 
     * @param generateAll Whether to generate all code
     */
    public void setGenerateAll(boolean generateAll) {
        this.generateAll = generateAll;
    }
    
    /**
     * Sets whether to unwrap parameters.
     * 
     * @param unwrapParams Whether to unwrap parameters
     */
    public void setUnwrapParams(boolean unwrapParams) {
        this.unwrapParams = unwrapParams;
    }
    
    /**
     * Adds a namespace to package mapping.
     * 
     * @param namespace The namespace
     * @param packageName The package name
     */
    public void addNamespaceToPackageMapping(String namespace, String packageName) {
        namespaceToPackageMappings.add(namespace + "=" + packageName);
    }
    
    /**
     * Sets the username for authentication.
     * 
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Sets the password for authentication.
     * 
     * @param password The password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Static method to generate code from WSDL.
     *
     * @param args Command line arguments
     * @throws Exception if an error occurs
     */
    public static void generateFromArgs(String[] args) throws Exception {
        WSDL2Java wsdl2Java = new WSDL2Java();
        wsdl2Java.generate(args);
    }
    
    /**
     * Main method for command-line invocation.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            generateFromArgs(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
