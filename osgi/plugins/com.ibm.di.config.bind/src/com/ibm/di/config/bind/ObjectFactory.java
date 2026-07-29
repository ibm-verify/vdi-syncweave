/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the com.ibm.di.config.bind package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private final static QName _PropertyStore_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "propertyStore");
	private final static QName _Connector_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "connector");
	private final static QName _CompositeConfig_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "compositeConfig");
	private final static QName _Script_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "script");
	private final static QName _Loop_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "loop");
	private final static QName _PropertyStores_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "propertyStores");
	private final static QName _Composite_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "composite");
    private final static QName _ReviveAl_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "reviveAl");
	private final static QName _Container_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "container");
	private final static QName _Schema_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "schema");
	private final static QName _Config_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "config");
    private final static QName _ScheduleAl_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "scheduleAl");
	private final static QName _Branch_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "branch");
	private final static QName _Map_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "map");
	private final static QName _Component_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "component");
	private final static QName _Parser_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "parser");
	private final static QName _Function_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "function");
	private final static QName _Solution_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "solution");
	private final static QName _AssemblyLine_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "assemblyLine");
	private final static QName _SimpleConfig_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "simpleConfig");
	private final static QName _Complex_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "complex");
	private final static QName _Simple_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "simple");
	private final static QName _ComplexConfig_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/config", "complexConfig");

	private static final Marshaller marshaller;
	private static final Unmarshaller unmarshaller;
	static {
		try {
			JAXBContext ctx = JAXBContext.newInstance(BindUtil.class.getPackage().getName());
			marshaller = ctx.createMarshaller();
			unmarshaller = ctx.createUnmarshaller();
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
		}
	}

	public static Marshaller getMarshaller() {
		return marshaller;
	}

	public static Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: com.ibm.di.config.bind
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link ConditionBinding }
	 * 
	 */
	public ConditionBinding createConditionBinding() {
		return new ConditionBinding();
	}

	/**
	 * Create an instance of {@link ALInitParamsBinding }
	 * 
	 */
	public ALInitParamsBinding createALInitParamsBinding() {
		return new ALInitParamsBinding();
	}

	/**
	 * Create an instance of {@link ParameterBinding }
	 * 
	 */
	public ParameterBinding createParameterBinding() {
		return new ParameterBinding();
	}

	/**
	 * Create an instance of {@link PoolInstanceBinding }
	 * 
	 */
	public PoolInstanceBinding createPoolInstanceBinding() {
		return new PoolInstanceBinding();
	}

	/**
	 * Create an instance of {@link SimpleALComponentBinding }
	 * 
	 */
	public SimpleALComponentBinding createSimpleALComponentBinding() {
		return new SimpleALComponentBinding();
	}

	/**
	 * Create an instance of {@link ConnectorModeBinding }
	 * 
	 */
	public ConnectorModeBinding createConnectorModeBinding() {
		return new ConnectorModeBinding();
	}

	/**
	 * Create an instance of {@link ExposedPropertyBinding }
	 * 
	 */
	public ExposedPropertyBinding createExposedPropertyBinding() {
		return new ExposedPropertyBinding();
	}

	/**
	 * Create an instance of {@link ParserBinding }
	 * 
	 */
	public ParserBinding createParserBinding() {
		return new ParserBinding();
	}

	/**
	 * Create an instance of {@link FunctionBinding }
	 * 
	 */
	public FunctionBinding createFunctionBinding() {
		return new FunctionBinding();
	}

	/**
	 * Create an instance of {@link AssemblyLineBinding }
	 * 
	 */
	public AssemblyLineBinding createAssemblyLineBinding() {
		return new AssemblyLineBinding();
	}

	/**
	 * Create an instance of {@link LinkCriteriaBinding }
	 * 
	 */
	public LinkCriteriaBinding createLinkCriteriaBinding() {
		return new LinkCriteriaBinding();
	}

	/**
	 * Create an instance of {@link ExposedAlBinding }
	 * 
	 */
	public ExposedAlBinding createExposedAlBinding() {
		return new ExposedAlBinding();
	}

	/**
	 * Create an instance of {@link SolutionBinding }
	 * 
	 */
	public SolutionBinding createSolutionBinding() {
		return new SolutionBinding();
	}

	/**
	 * Create an instance of {@link LinkCriteriaItemBinding }
	 * 
	 */
	public LinkCriteriaItemBinding createLinkCriteriaItemBinding() {
		return new LinkCriteriaItemBinding();
	}

	/**
	 * Create an instance of {@link AttributeMapBinding }
	 * 
	 */
	public AttributeMapBinding createAttributeMapBinding() {
		return new AttributeMapBinding();
	}

	/**
	 * Create an instance of {@link ReconnectBinding }
	 * 
	 */
	public ReconnectBinding createReconnectBinding() {
		return new ReconnectBinding();
	}

	/**
	 * Create an instance of {@link CollectionLoopBinding }
	 * 
	 */
	public CollectionLoopBinding createCollectionLoopBinding() {
		return new CollectionLoopBinding();
	}

	/**
	 * Create an instance of {@link SolutionContextBinding }
	 * 
	 */
	public SolutionContextBinding createSolutionContextBinding() {
		return new SolutionContextBinding();
	}

	/**
	 * Create an instance of {@link LoopBinding }
	 * 
	 */
	public LoopBinding createLoopBinding() {
		return new LoopBinding();
	}

	/**
	 * Create an instance of {@link DeltaBinding }
	 * 
	 */
	public DeltaBinding createDeltaBinding() {
		return new DeltaBinding();
	}

	/**
	 * Create an instance of {@link HooksBinding }
	 * 
	 */
	public HooksBinding createHooksBinding() {
		return new HooksBinding();
	}

	/**
	 * Create an instance of {@link ConditionItemBinding }
	 * 
	 */
	public ConditionItemBinding createConditionItemBinding() {
		return new ConditionItemBinding();
	}

	/**
	 * Create an instance of {@link ComplexALComponentBinding }
	 * 
	 */
	public ComplexALComponentBinding createComplexALComponentBinding() {
		return new ComplexALComponentBinding();
	}

	/**
	 * Create an instance of {@link SolutionLibraryBinding }
	 * 
	 */
	public SolutionLibraryBinding createSolutionLibraryBinding() {
		return new SolutionLibraryBinding();
	}

	/**
	 * Create an instance of {@link BranchBinding }
	 * 
	 */
	public BranchBinding createBranchBinding() {
		return new BranchBinding();
	}

	/**
	 * Create an instance of {@link ProxyALBinding }
	 * 
	 */
	public ProxyALBinding createProxyALBinding() {
		return new ProxyALBinding();
	}

	/**
	 * Create an instance of {@link SolutionInterfaceBinding }
	 * 
	 */
	public SolutionInterfaceBinding createSolutionInterfaceBinding() {
		return new SolutionInterfaceBinding();
	}

	/**
	 * Create an instance of {@link AttributeMapItemBinding }
	 * 
	 */
	public AttributeMapItemBinding createAttributeMapItemBinding() {
		return new AttributeMapItemBinding();
	}

	/**
	 * Create an instance of {@link ALComponentsBinding }
	 * 
	 */
	public ALComponentsBinding createALComponentsBinding() {
		return new ALComponentsBinding();
	}

	/**
	 * Create an instance of {@link ALSandboxBinding }
	 * 
	 */
	public ALSandboxBinding createALSandboxBinding() {
		return new ALSandboxBinding();
	}

	/**
	 * Create an instance of {@link ConnectorBinding }
	 * 
	 */
	public ConnectorBinding createConnectorBinding() {
		return new ConnectorBinding();
	}

	/**
	 * Create an instance of {@link NamedBinding }
	 * 
	 */
	public NamedBinding createNamedBinding() {
		return new NamedBinding();
	}

	/**
	 * Create an instance of {@link ALSimulationBinding }
	 * 
	 */
	public ALSimulationBinding createALSimulationBinding() {
		return new ALSimulationBinding();
	}

	/**
	 * Create an instance of {@link SchemaBinding }
	 * 
	 */
	public SchemaBinding createSchemaBinding() {
		return new SchemaBinding();
	}

	/**
	 * Create an instance of {@link HookBinding }
	 * 
	 */
	public HookBinding createHookBinding() {
		return new HookBinding();
	}

	/**
	 * Create an instance of {@link InheritingBinding }
	 * 
	 */
	public InheritingBinding createInheritingBinding() {
		return new InheritingBinding();
	}

	/**
	 * Create an instance of {@link LogBinding }
	 * 
	 */
	public LogBinding createLogBinding() {
		return new LogBinding();
	}

	/**
	 * Create an instance of {@link LogItemBinding }
	 * 
	 */
	public LogItemBinding createLogItemBinding() {
		return new LogItemBinding();
	}

	/**
	 * Create an instance of {@link SolutionInstanceBinding }
	 * 
	 */
	public SolutionInstanceBinding createSolutionInstanceBinding() {
		return new SolutionInstanceBinding();
	}

	/**
	 * Create an instance of {@link ALOperationsBinding }
	 * 
	 */
	public ALOperationsBinding createALOperationsBinding() {
		return new ALOperationsBinding();
	}

	/**
	 * Create an instance of {@link ParametersBinding }
	 * 
	 */
	public ParametersBinding createParametersBinding() {
		return new ParametersBinding();
	}

	/**
	 * Create an instance of {@link PoolDefinitionBinding }
	 * 
	 */
	public PoolDefinitionBinding createPoolDefinitionBinding() {
		return new PoolDefinitionBinding();
	}

	/**
	 * Create an instance of {@link ReconnectRuleBinding }
	 * 
	 */
	public ReconnectRuleBinding createReconnectRuleBinding() {
		return new ReconnectRuleBinding();
	}

	/**
	 * Create an instance of {@link ContainerBinding }
	 * 
	 */
	public ContainerBinding createContainerBinding() {
		return new ContainerBinding();
	}

	/**
	 * Create an instance of {@link ALOperationBinding }
	 * 
	 */
	public ALOperationBinding createALOperationBinding() {
		return new ALOperationBinding();
	}

	/**
	 * Create an instance of {@link SchemaItemBinding }
	 * 
	 */
	public SchemaItemBinding createSchemaItemBinding() {
		return new SchemaItemBinding();
	}

	/**
	 * Create an instance of {@link CompositeALComponentBinding }
	 * 
	 */
	public CompositeALComponentBinding createCompositeALComponentBinding() {
		return new CompositeALComponentBinding();
	}

	/**
	 * Create an instance of {@link ScriptBinding }
	 * 
	 */
	public ScriptBinding createScriptBinding() {
		return new ScriptBinding();
	}

	/**
	 * Create an instance of {@link PropertyStoresBinding }
	 * 
	 */
	public PropertyStoresBinding createPropertyStoresBinding() {
		return new PropertyStoresBinding();
	}

	/**
	 * Create an instance of {@link PropertyStoreBinding }
	 * 
	 */
	public PropertyStoreBinding createPropertyStoreBinding() {
		return new PropertyStoreBinding();
	}

	/**
	 * Create an instance of {@link NullBinding }
	 * 
	 */
	public NullBinding createNullBinding() {
		return new NullBinding();
	}

	/**
	 * Create an instance of {@link JavaClassBinding }
	 * 
	 */
	public JavaClassBinding createJavaClassBinding() {
		return new JavaClassBinding();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link PropertyStoreBinding }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "propertyStore", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "config")
	public JAXBElement<PropertyStoreBinding> createPropertyStore(PropertyStoreBinding value) {
		return new JAXBElement<PropertyStoreBinding>(_PropertyStore_QNAME, PropertyStoreBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link ConnectorBinding }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "connector", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "complexConfig")
	public JAXBElement<ConnectorBinding> createConnector(ConnectorBinding value) {
		return new JAXBElement<ConnectorBinding>(_Connector_QNAME, ConnectorBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link CompositeComponentBinding }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "compositeConfig", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "config")
	public JAXBElement<CompositeComponentBinding> createCompositeConfig(CompositeComponentBinding value) {
		return new JAXBElement<CompositeComponentBinding>(_CompositeConfig_QNAME, CompositeComponentBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link ScriptBinding }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "script", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "simpleConfig")
	public JAXBElement<ScriptBinding> createScript(ScriptBinding value) {
		return new JAXBElement<ScriptBinding>(_Script_QNAME, ScriptBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link LoopBinding }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "loop", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "compositeConfig")
	public JAXBElement<LoopBinding> createLoop(LoopBinding value) {
		return new JAXBElement<LoopBinding>(_Loop_QNAME, LoopBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link PropertyStoresBinding }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "propertyStores", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "container")
	public JAXBElement<PropertyStoresBinding> createPropertyStores(PropertyStoresBinding value) {
		return new JAXBElement<PropertyStoresBinding>(_PropertyStores_QNAME, PropertyStoresBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link CompositeALComponentBinding }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "composite", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "component")
	public JAXBElement<CompositeALComponentBinding> createComposite(CompositeALComponentBinding value) {
		return new JAXBElement<CompositeALComponentBinding>(_Composite_QNAME, CompositeALComponentBinding.class, null, value);
	}

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ALReviverBinding }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "reviveAl", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "config")
    public JAXBElement<ALReviverBinding> createReviveAl(ALReviverBinding value) {
        return new JAXBElement<ALReviverBinding>(_ReviveAl_QNAME, ALReviverBinding.class, null, value);
    }

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link ContainerBinding }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "container", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "config")
	public JAXBElement<ContainerBinding> createContainer(ContainerBinding value) {
		return new JAXBElement<ContainerBinding>(_Container_QNAME, ContainerBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link SchemaBinding }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "schema", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "config")
	public JAXBElement<SchemaBinding> createSchema(SchemaBinding value) {
		return new JAXBElement<SchemaBinding>(_Schema_QNAME, SchemaBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link NamedBinding }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "config")
	public JAXBElement<NamedBinding> createConfig(NamedBinding value) {
		return new JAXBElement<NamedBinding>(_Config_QNAME, NamedBinding.class, null, value);
	}

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ALExecutionScheduleBinding }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "scheduleAl", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "config")
    public JAXBElement<ALExecutionScheduleBinding> createScheduleAl(ALExecutionScheduleBinding value) {
        return new JAXBElement<ALExecutionScheduleBinding>(_ScheduleAl_QNAME, ALExecutionScheduleBinding.class, null, value);
    }

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link BranchBinding }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "branch", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "compositeConfig")
	public JAXBElement<BranchBinding> createBranch(BranchBinding value) {
		return new JAXBElement<BranchBinding>(_Branch_QNAME, BranchBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link AttributeMapBinding }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "map", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "simpleConfig")
	public JAXBElement<AttributeMapBinding> createMap(AttributeMapBinding value) {
		return new JAXBElement<AttributeMapBinding>(_Map_QNAME, AttributeMapBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link ALComponentBinding }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "component")
	public JAXBElement<ALComponentBinding> createComponent(ALComponentBinding value) {
		return new JAXBElement<ALComponentBinding>(_Component_QNAME, ALComponentBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link ParserBinding }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "parser", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "config")
	public JAXBElement<ParserBinding> createParser(ParserBinding value) {
		return new JAXBElement<ParserBinding>(_Parser_QNAME, ParserBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link FunctionBinding }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "function", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "complexConfig")
	public JAXBElement<FunctionBinding> createFunction(FunctionBinding value) {
		return new JAXBElement<FunctionBinding>(_Function_QNAME, FunctionBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link SolutionBinding }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "solution")
	public JAXBElement<SolutionBinding> createSolution(SolutionBinding value) {
		return new JAXBElement<SolutionBinding>(_Solution_QNAME, SolutionBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link AssemblyLineBinding }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "assemblyLine", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "config")
	public JAXBElement<AssemblyLineBinding> createAssemblyLine(AssemblyLineBinding value) {
		return new JAXBElement<AssemblyLineBinding>(_AssemblyLine_QNAME, AssemblyLineBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link SimpleComponentBinding }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "simpleConfig", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "config")
	public JAXBElement<SimpleComponentBinding> createSimpleConfig(SimpleComponentBinding value) {
		return new JAXBElement<SimpleComponentBinding>(_SimpleConfig_QNAME, SimpleComponentBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link ComplexALComponentBinding }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "complex", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "component")
	public JAXBElement<ComplexALComponentBinding> createComplex(ComplexALComponentBinding value) {
		return new JAXBElement<ComplexALComponentBinding>(_Complex_QNAME, ComplexALComponentBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link SimpleALComponentBinding }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "simple", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "component")
	public JAXBElement<SimpleALComponentBinding> createSimple(SimpleALComponentBinding value) {
		return new JAXBElement<SimpleALComponentBinding>(_Simple_QNAME, SimpleALComponentBinding.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link ComplexComponentBinding }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", name = "complexConfig", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", substitutionHeadName = "config")
	public JAXBElement<ComplexComponentBinding> createComplexConfig(ComplexComponentBinding value) {
		return new JAXBElement<ComplexComponentBinding>(_ComplexConfig_QNAME, ComplexComponentBinding.class, null, value);
	}

}
