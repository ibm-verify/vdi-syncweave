/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
// kkolev: this code has been auto-generated with the following command:
// 
//   "xjc <XSDs_dir> -nv -p com.ibm.di.config.bind -no-header -d <output_dir>"
// 
// After the generation the code was modified to get rid of the JAXBElements.
// In order to do so the root XML elements were explicitly defined using the
// @XmlRootElement annotation for the below types. 
// com.ibm.di.bind.config.NamedConfig
// com.ibm.di.bind.config.ALComponentConfig
// com.ibm.di.bind.config.ContainerConfig
// com.ibm.di.bind.config.BranchConfig
// com.ibm.di.bind.config.LoopConfig
// com.ibm.di.bind.config.AttributeMapConfig
// com.ibm.di.bind.config.ConnectorConfig
// com.ibm.di.bind.config.ContextConfig
// com.ibm.di.bind.config.FunctionConfig
// com.ibm.di.bind.config.ParserConfig
// com.ibm.di.bind.config.PropertyStoreConfig
// com.ibm.di.bind.config.SchemaConfig
// com.ibm.di.bind.config.ScriptConfig
// com.ibm.di.bind.config.PropertyStoreRelationsConfig
//
// If recompiling the XSDs again, make sure to use file diff utility to avoid
// overwriting some of changes.
// Some readability enhancements were made to the following files:
// com.ibm.di.bind.config.ALComponentsConfig
// com.ibm.di.bind.config.AssemblyLineConfig
// com.ibm.di.bind.config.AttributeMapConfig
// com.ibm.di.bind.config.ConditionConfig
// com.ibm.di.bind.config.ConnectorConfig
// com.ibm.di.bind.config.NamedConfig
// com.ibm.di.bind.config.ExposedALsConfig
// com.ibm.di.bind.config.ExposedPropsConfig
// com.ibm.di.bind.config.FunctionConfig
// com.ibm.di.bind.config.HooksConfig
// com.ibm.di.bind.config.JavaLibraryConfig
// com.ibm.di.bind.config.LinkCriteriaConfig
// com.ibm.di.bind.config.LogConfig
// com.ibm.di.bind.config.OperationConfig
// com.ibm.di.bind.config.OperationsConfig
// com.ibm.di.bind.config.ParametersConfig
// com.ibm.di.bind.config.ParserConfig
// com.ibm.di.bind.config.ReconnectConfig
// com.ibm.di.bind.config.SchemaConfig
// com.ibm.di.bind.config.SchemaItemConfig
// com.ibm.di.bind.config.ScriptConfig
// com.ibm.di.bind.config.SimulationConfig
// com.ibm.di.bind.config.SolutionConfig
// com.ibm.di.bind.config.StartupConfig
// 
// All binding objects must implement java.lang.Serializable na comply with
// TDI's coding guidelines.

@javax.xml.bind.annotation.XmlSchema(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)
package com.ibm.di.config.bind;
