/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

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
 * element interface generated in the com.ibm.di.api.bind package.
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

	private final static QName _ServerInfo_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "serverInfo");
	private final static QName _CiEvent_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "ciEvent");
	private final static QName _DiEvent_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "diEvent");
	private final static QName _Property_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "property");
	private final static QName _Shutdown_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "shutdown");
	private final static QName _LogListener_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "logListener");
	private final static QName _Event_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "event");
	private final static QName _CreateConfig_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "createConfig");
	private final static QName _Attribute_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "attribute");
	private final static QName _BatchEvent_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "batchEvent");
	private final static QName _AssemblyLineEvent_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "assemblyLineEvent");
	private final static QName _Channel_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "channel");
	private final static QName _PollChannel_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "pollChannel");
	private final static QName _StartCI_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "startCI");
	private final static QName _TaskStatistics_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "taskStatistics");
	private final static QName _Error_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "error");
	private final static QName _PushChannel_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "pushChannel");
	private final static QName _TaskCallBlock_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "taskCallBlock");
	private final static QName _StartAL_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "startAL");
	private final static QName _AssemblyLineListener_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api",
			"assemblyLineListener");
	private final static QName _ConfigLock_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "configLock");
	private final static QName _Entry_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "entry");
    private final static QName _CiData_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "ciData");
	private final static QName _Properties_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "properties");
	private final static QName _LogEvent_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "logEvent");

	private final static QName _AlHandle_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "alHandle");
	private final static QName _CustomNotification_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api",
			"customNotification");
	private final static QName _DIEventListener_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "diEventListener");
	private final static QName _AlEvent_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "alEvent");
    private final static QName _TsData_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "tsData");
	private final static QName _Value_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "value");
	private final static QName _ConfigFileListener_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api",
			"configFileListener");
	private final static QName _ConfigFileEvent_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "configFileEvent");
    private final static QName _AlData_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/72/api", "alData");

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

    /**
     * Create an instance of {@link Tombstone }
     * 
     */
    public Tombstone createTombstone() {
        return new Tombstone();
    }

	public static Marshaller getMarshaller() {
		return marshaller;
	}

	public static Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: com.ibm.di.api.bind
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link LogListener }
	 * 
	 */
	public LogListener createLogListener() {
		return new LogListener();
	}

	/**
	 * Create an instance of {@link AttributeProperty }
	 * 
	 */
	public AttributeProperty createAttributeProperty() {
		return new AttributeProperty();
	}

	/**
	 * Create an instance of {@link TcbComponent }
	 * 
	 */
	public TcbComponent createTcbComponent() {
		return new TcbComponent();
	}

	/**
	 * Create an instance of {@link AttributeValue }
	 * 
	 */
	public AttributeValue createAttributeValue() {
		return new AttributeValue();
	}

	/**
	 * Create an instance of {@link Properties }
	 * 
	 */
	public Properties createProperties() {
		return new Properties();
	}

	/**
	 * Create an instance of {@link Error.Details }
	 * 
	 */
	public Error.Details createErrorDetails() {
		return new Error.Details();
	}

	/**
	 * Create an instance of {@link TaskCallBlock }
	 * 
	 */
	public TaskCallBlock createTaskCallBlock() {
		return new TaskCallBlock();
	}

	/**
	 * Create an instance of {@link AssemblyLineListener }
	 * 
	 */
	public AssemblyLineListener createAssemblyLineListener() {
		return new AssemblyLineListener();
	}

	/**
	 * Create an instance of {@link ALEvent }
	 * 
	 */
	public ALEvent createALEvent() {
		return new ALEvent();
	}

	/**
	 * Create an instance of {@link Entry }
	 * 
	 */
	public Entry createEntry() {
		return new Entry();
	}

	/**
	 * Create an instance of {@link StartAL }
	 * 
	 */
	public StartAL createStartAL() {
		return new StartAL();
	}

	/**
	 * Create an instance of {@link StartCI }
	 * 
	 */
	public StartCI createStartCI() {
		return new StartCI();
	}

	/**
	 * Create an instance of {@link DIEventListener }
	 * 
	 */
	public DIEventListener createDIEventListener() {
		return new DIEventListener();
	}

	/**
	 * Create an instance of {@link CustomNotification }
	 * 
	 */
	public CustomNotification createCustomNotification() {
		return new CustomNotification();
	}

	/**
	 * Create an instance of {@link TaskStatistics }
	 * 
	 */
	public TaskStatistics createTaskStatistics() {
		return new TaskStatistics();
	}

	/**
	 * Create an instance of {@link Error }
	 * 
	 */
	public Error createError() {
		return new Error();
	}

	/**
	 * Create an instance of {@link TcbRuntime }
	 * 
	 */
	public TcbRuntime createTcbRuntime() {
		return new TcbRuntime();
	}

	/**
	 * Create an instance of {@link CIEvent }
	 * 
	 */
	public CIEvent createCIEvent() {
		return new CIEvent();
	}

	/**
	 * Create an instance of {@link DIEvent }
	 * 
	 */
	public DIEvent createDIEvent() {
		return new DIEvent();
	}

	/**
	 * Create an instance of {@link CreateConfig }
	 * 
	 */
	public CreateConfig createCreateConfig() {
		return new CreateConfig();
	}

	/**
	 * Create an instance of {@link AssemblyLineEvent }
	 * 
	 */
	public AssemblyLineEvent createAssemblyLineEvent() {
		return new AssemblyLineEvent();
	}

	/**
	 * Create an instance of {@link TcbInitParam }
	 * 
	 */
	public TcbInitParam createTcbInitParam() {
		return new TcbInitParam();
	}

	/**
	 * Create an instance of {@link ServerInfo }
	 * 
	 */
	public ServerInfo createServerInfo() {
		return new ServerInfo();
	}

	/**
	 * Create an instance of {@link Exception }
	 * 
	 */
	public Exception createException() {
		return new Exception();
	}

	/**
	 * Create an instance of {@link Stat }
	 * 
	 */
	public Stat createStat() {
		return new Stat();
	}

	/**
	 * Create an instance of {@link ALHandle }
	 * 
	 */
	public ALHandle createALCycle() {
		return new ALHandle();
	}

	/**
	 * Create an instance of {@link EntryProperty }
	 * 
	 */
	public EntryProperty createEntryProperty() {
		return new EntryProperty();
	}

	/**
	 * Create an instance of {@link Shutdown }
	 * 
	 */
	public Shutdown createShutdown() {
		return new Shutdown();
	}

	/**
	 * Create an instance of {@link TcbParameter }
	 * 
	 */
	public TcbParameter createTcbParameter() {
		return new TcbParameter();
	}

	/**
	 * Create an instance of {@link Property }
	 * 
	 */
	public Property createProperty() {
		return new Property();
	}

	/**
	 * Create an instance of {@link ConfigLock }
	 * 
	 */
	public ConfigLock createConfigLock() {
		return new ConfigLock();
	}

	/**
	 * Create an instance of {@link ErrorDetail }
	 * 
	 */
	public ErrorDetail createErrorDetail() {
		return new ErrorDetail();
	}

	/**
	 * Create an instance of {@link Data }
	 * 
	 */
	public Data createData() {
		return new Data();
	}

	/**
	 * Create an instance of {@link EntryAttribute }
	 * 
	 */
	public EntryAttribute createEntryAttribute() {
		return new EntryAttribute();
	}

	/**
	 * Create an instance of {@link TcbComponents }
	 * 
	 */
	public TcbComponents createTcbComponents() {
		return new TcbComponents();
	}

	/**
	 * Create an instance of {@link LogEvent }
	 * 
	 */
	public LogEvent createLogEvent() {
		return new LogEvent();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link ServerInfo }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "serverInfo")
	public JAXBElement<ServerInfo> createServerInfo(ServerInfo value) {
		return new JAXBElement<ServerInfo>(_ServerInfo_QNAME, ServerInfo.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link CIEvent }{@code
	 * >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "ciEvent")
	public JAXBElement<CIEvent> createCiEvent(CIEvent value) {
		return new JAXBElement<CIEvent>(_CiEvent_QNAME, CIEvent.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link DIEvent }{@code
	 * >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "diEvent")
	public JAXBElement<DIEvent> createDiEvent(DIEvent value) {
		return new JAXBElement<DIEvent>(_DiEvent_QNAME, DIEvent.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Property }{@code
	 * >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "property")
	public JAXBElement<Property> createProperty(Property value) {
		return new JAXBElement<Property>(_Property_QNAME, Property.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Shutdown }{@code
	 * >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "shutdown")
	public JAXBElement<Shutdown> createShutdown(Shutdown value) {
		return new JAXBElement<Shutdown>(_Shutdown_QNAME, Shutdown.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link LogListener }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "logListener")
	public JAXBElement<LogListener> createLogListener(LogListener value) {
		return new JAXBElement<LogListener>(_LogListener_QNAME, LogListener.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Event }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "event")
	public JAXBElement<Event> createEvent(Event value) {
		return new JAXBElement<Event>(_Event_QNAME, Event.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link CreateConfig }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "createConfig")
	public JAXBElement<CreateConfig> createCreateConfig(CreateConfig value) {
		return new JAXBElement<CreateConfig>(_CreateConfig_QNAME, CreateConfig.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link EntryAttribute }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "attribute")
	public JAXBElement<EntryAttribute> createAttribute(EntryAttribute value) {
		return new JAXBElement<EntryAttribute>(_Attribute_QNAME, EntryAttribute.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link AssemblyLineEvent }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "assemblyLineEvent")
	public JAXBElement<AssemblyLineEvent> createAssemblyLineEvent(AssemblyLineEvent value) {
		return new JAXBElement<AssemblyLineEvent>(_AssemblyLineEvent_QNAME, AssemblyLineEvent.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link BatchEvent }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "batchEvent")
	public JAXBElement<BatchEvent> createBatchEvent(BatchEvent value) {
		return new JAXBElement<BatchEvent>(_BatchEvent_QNAME, BatchEvent.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link TransportChannel } {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "channel")
	public JAXBElement<TransportChannel> createChannel(TransportChannel value) {
		return new JAXBElement<TransportChannel>(_Channel_QNAME, TransportChannel.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link StartCI }{@code
	 * >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "startCI")
	public JAXBElement<StartCI> createStartCI(StartCI value) {
		return new JAXBElement<StartCI>(_StartCI_QNAME, StartCI.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link PollChannel }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "pollChannel", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", substitutionHeadName = "channel")
	public JAXBElement<PollChannel> createPollChannel(PollChannel value) {
		return new JAXBElement<PollChannel>(_PollChannel_QNAME, PollChannel.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link TaskStatistics }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "taskStatistics")
	public JAXBElement<TaskStatistics> createTaskStatistics(TaskStatistics value) {
		return new JAXBElement<TaskStatistics>(_TaskStatistics_QNAME, TaskStatistics.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Error }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "error")
	public JAXBElement<Error> createError(Error value) {
		return new JAXBElement<Error>(_Error_QNAME, Error.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link PushChannel }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "pushChannel", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", substitutionHeadName = "channel")
	public JAXBElement<PushChannel> createPushChannel(PushChannel value) {
		return new JAXBElement<PushChannel>(_PushChannel_QNAME, PushChannel.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link TaskCallBlock }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "taskCallBlock")
	public JAXBElement<TaskCallBlock> createTaskCallBlock(TaskCallBlock value) {
		return new JAXBElement<TaskCallBlock>(_TaskCallBlock_QNAME, TaskCallBlock.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link StartAL }{@code
	 * >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "startAL")
	public JAXBElement<StartAL> createStartAL(StartAL value) {
		return new JAXBElement<StartAL>(_StartAL_QNAME, StartAL.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link AssemblyLineListener }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "assemblyLineListener", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", substitutionHeadName = "logListener")
	public JAXBElement<AssemblyLineListener> createAssemblyLineListener(AssemblyLineListener value) {
		return new JAXBElement<AssemblyLineListener>(_AssemblyLineListener_QNAME, AssemblyLineListener.class, null, value);
	}

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CITombstoneData }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "ciData", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", substitutionHeadName = "tsData")
    public JAXBElement<CITombstoneData> createCiData(CITombstoneData value) {
        return new JAXBElement<CITombstoneData>(_CiData_QNAME, CITombstoneData.class, null, value);
    }

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Entry }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "entry")
	public JAXBElement<Entry> createEntry(Entry value) {
		return new JAXBElement<Entry>(_Entry_QNAME, Entry.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Properties }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "properties")
	public JAXBElement<Properties> createProperties(Properties value) {
		return new JAXBElement<Properties>(_Properties_QNAME, Properties.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link LogEvent }{@code
	 * >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "logEvent")
	public JAXBElement<LogEvent> createLogEvent(LogEvent value) {
		return new JAXBElement<LogEvent>(_LogEvent_QNAME, LogEvent.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link ALHandle }{@code
	 * >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "alCycle")
	public JAXBElement<ALHandle> createAlCycle(ALHandle value) {
		return new JAXBElement<ALHandle>(_AlHandle_QNAME, ALHandle.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link CustomNotification }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "customNotification")
	public JAXBElement<CustomNotification> createCustomNotification(CustomNotification value) {
		return new JAXBElement<CustomNotification>(_CustomNotification_QNAME, CustomNotification.class, null, value);
	}

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TombstoneData }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "tsData")
    public JAXBElement<TombstoneData> createTsData(TombstoneData value) {
        return new JAXBElement<TombstoneData>(_TsData_QNAME, TombstoneData.class, null, value);
    }

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link AttributeValue }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "value")
	public JAXBElement<AttributeValue> createValue(AttributeValue value) {
		return new JAXBElement<AttributeValue>(_Value_QNAME, AttributeValue.class, null, value);
	}
	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link DIEventListener }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "diEventListener")
	public JAXBElement<DIEventListener> createDIEventListener(DIEventListener value) {
		return new JAXBElement<DIEventListener>(_DIEventListener_QNAME, DIEventListener.class, null, value);
	}

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ALTombstoneData }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "alData", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", substitutionHeadName = "tsData")
    public JAXBElement<ALTombstoneData> createAlData(ALTombstoneData value) {
        return new JAXBElement<ALTombstoneData>(_AlData_QNAME, ALTombstoneData.class, null, value);
    }

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link ALEvent }{@code
	 * >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "alEvent")
	public JAXBElement<ALEvent> createAlEvent(ALEvent value) {
		return new JAXBElement<ALEvent>(_AlEvent_QNAME, ALEvent.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link ConfigLock }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "configLock")
	public JAXBElement<ConfigLock> createConfigLock(ConfigLock value) {
		return new JAXBElement<ConfigLock>(_ConfigLock_QNAME, ConfigLock.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link DIEventListener }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "configFileListener")
	public JAXBElement<ConfigFileListener> createConfigFileListener(ConfigFileListener value) {
		return new JAXBElement<ConfigFileListener>(_ConfigFileListener_QNAME, ConfigFileListener.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link ConfigFileEvent }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", name = "configFileEvent")
	public JAXBElement<ConfigFileEvent> createAlEvent(ConfigFileEvent value) {
		return new JAXBElement<ConfigFileEvent>(_ConfigFileEvent_QNAME, ConfigFileEvent.class, null, value);
	}

}
