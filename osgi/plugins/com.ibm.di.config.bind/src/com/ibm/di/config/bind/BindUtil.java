/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.naming.Binding;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchCondition;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.DeltaConfig;
import com.ibm.di.config.interfaces.ExposedProperty;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.InstanceConfig;
import com.ibm.di.config.interfaces.LibraryConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.LinkCriteriaItem;
import com.ibm.di.config.interfaces.LogConfig;
import com.ibm.di.config.interfaces.LogConfigItem;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.OperationsConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.PoolDefConfig;
import com.ibm.di.config.interfaces.PoolInstanceConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.ReconnectConfig;
import com.ibm.di.config.interfaces.ReconnectRuleConfig;
import com.ibm.di.config.interfaces.SandboxConfig;
import com.ibm.di.config.interfaces.SchedulerConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.SimulationConfig;
import com.ibm.di.config.interfaces.SolutionInterface;
import com.ibm.di.config.interfaces.TombstonesConfig;
import com.ibm.icu.util.StringTokenizer;

/**
 * Utility methods for working with the Binding objects <br>
 * <br>
 * 
 * @since 7.2
 */
public class BindUtil {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@SuppressWarnings("unchecked")
	public static SolutionBinding fromMetamergeConfig(MetamergeConfig mc) throws Exception {
		if (mc == null) {
			return null;
		}
		SolutionBinding sb = new SolutionBinding();
		sb.setCreated(System.currentTimeMillis());
		sb.setCreatedBy(System.getProperty("user.name"));
		sb.setIdiVersion("Created by SDI " + mc.getConfigVersion());
		sb.setVersion(mc.getConfigVersion());

		Enumeration<Binding> folders = mc.list();

		LibraryConfig jl = null;
		MetamergeFolder config = null;
		while (folders.hasMoreElements()) {
			BaseConfiguration cfg = (BaseConfiguration) folders.nextElement().getObject();
			if (cfg instanceof LibraryConfig) {
				// keep this for later usage.
				jl = (LibraryConfig) cfg;
			} else if ("Config".equals(cfg.getShortName())) {
				config = (MetamergeFolder) cfg;
			} else {
				if (cfg instanceof MetamergeFolder) {
					sb.getContainers().add(fromFolder((MetamergeFolder) cfg));
				}
			}
		}

		SolutionInterface i = null;
		LogConfig l = null;
		TombstonesConfig t = null;
		InstanceConfig ic = null;
		ContainerConfig sysStore = null;

		if (config != null) {
			Enumeration<Binding> ctx = config.list();
			while (ctx.hasMoreElements()) {
				BaseConfiguration b = (BaseConfiguration) ctx.nextElement().getObject();
				if (b instanceof SolutionInterface) {
					i = (SolutionInterface) b;
				} else if (b instanceof TombstonesConfig) {
					t = (TombstonesConfig) b;
				} else if (b instanceof LogConfig) {
					l = (LogConfig) b;
				} else if (b instanceof InstanceConfig) {
					ic = (InstanceConfig) b;
				} else if (b instanceof ContainerConfig && "SystemStore".equals(b.getShortName())) {
					sysStore = (ContainerConfig) b;
				}
			}
		}

		if (config != null || jl != null) {
			sb.setContext(fromContext(i, l, jl, t, ic, sysStore));
		}

		return sb;
	}

	@SuppressWarnings("unchecked")
	private static ContainerBinding fromFolder(MetamergeFolder f) throws Exception {
		ContainerBinding c = null;
		if (f instanceof PropertyManager) {
			PropertyStoresBinding p = new PropertyStoresBinding();
			PropertyStoreConfig def = ((PropertyManager) f).getDefaultPropertyStore();
			if (def != null) {
				p.setDefault(def.getName().toString());
			}

			PropertyStoreConfig pass = ((PropertyManager) f).getPasswordPropertyStore();
			if (pass != null) {
				p.setPassword(pass.getName().toString());
			}

			c = p;
		} else if ("Config".equals(f.getShortName())) {
			// Config folder is handled in the fromMetamergeConfig method.
			throw new IllegalArgumentException("Config");
		} else {
			c = new ContainerBinding();
		}

		c.setName(f.getShortName());
		Enumeration configs = f.list();
		NamedBinding b = null;
		while (configs.hasMoreElements()) {
			Object obj = configs.nextElement();
			if (obj instanceof BaseConfiguration) {
				b = fromBaseConfig((BaseConfiguration) obj);
			} else {
				b = fromBaseConfig((BaseConfiguration) ((Binding) obj).getObject());
			}
			// if (b instanceof ConnectorBinding) {
			// // connectors defined in the library are not supposed to define
			// // poolInst binding
			// ((ConnectorBinding) b).setPoolInst(null);
			// }
			c.getConfigs().add(b);
		}

		return c;
	}

	private static NamedBinding fromBaseConfig(BaseConfiguration object) throws Exception {
		NamedBinding b = null;

		if (object instanceof AssemblyLineConfig) {
			b = fromAssemblyLine((AssemblyLineConfig) object);

		} else if (object instanceof ALMappingConfig) {
			b = fromALMapping((ALMappingConfig) object);
		} else if (object instanceof FunctionConfig) {
			b = fromFunction((FunctionConfig) object);
		} else if (object instanceof ConnectorConfig) {
			b = fromConnector((ConnectorConfig) object);
		} else if (object instanceof ParserConfig) {
			b = fromParser((ParserConfig) object);
		} else if (object instanceof ScriptConfig) {
			b = fromScript((ScriptConfig) object);
		} else if (object instanceof LoopConfig) {
			b = fromLoop((LoopConfig) object);
		} else if (object instanceof BranchingConfig) {
			b = fromBranch((BranchingConfig) object);
		} else if (object instanceof SchemaConfig) {
			b = fromSchema((SchemaConfig) object);
		} else if (object instanceof PropertyStoreConfig) {
			b = fromPropertyStore((PropertyStoreConfig) object);
		} else if (object instanceof SchedulerConfig) {
			b = fromScheduler((SchedulerConfig) object);
		}

		return b;
	}

	/**
	 * @param object
	 * @return
	 */
	private static NamedBinding fromScheduler(SchedulerConfig cfg) {
		if (cfg == null) {
			return null;
		}
		ALStarterBinding starter;
		if (cfg.getType() == SchedulerConfig.TIMER) {
			starter = new ALExecutionScheduleBinding();
			((ALExecutionScheduleBinding) starter).setExecTimePattern(cfg.getStartTimes());
			((ALExecutionScheduleBinding) starter).setFailureAl(cfg.getStringParameter("FailureAL"));
			((ALExecutionScheduleBinding) starter).setSkipExecIfAlRunning(cfg.getBooleanParameter("NoStartIfRunning", true));
			((ALExecutionScheduleBinding) starter).setCancelScheduleOnAlFailure(cfg.getBooleanParameter("stopOnFailure", false));
			((ALExecutionScheduleBinding) starter).setEnabled(cfg.getEnabled());
		} else {
			starter = new ALReviverBinding();
			((ALReviverBinding) starter).setFailureAl(cfg.getStringParameter("FailureAL"));
			((ALReviverBinding) starter).setFailIfAlDiedIn(cfg.getIntegerParameter("WithinSeconds", 0));
			((ALReviverBinding) starter).setEnabled(cfg.getEnabled());
		}
		starter.setName(cfg.getShortName());
		starter.setAssemblyLine(cfg.getScheduledName());
		starter.setOperation(cfg.getStringParameter("operation"));
		starter.setConfigInstance(cfg.getStringParameter("config"));
		starter.setServer(cfg.getStringParameter("server"));

		ParametersBinding pb = new ParametersBinding();
		for (String key : cfg.getKeys(BaseConfiguration.ONE_LEVEL)) {
			if (key.startsWith("$initialize.")) {
				ParameterBinding p = new ParameterBinding();
				p.setName(key.substring(12));
				p.setValue(cfg.getStringParameter(key));
				pb.getParameters().add(p);
			}
		}
		starter.setInitParams(pb);

		return starter;
	}

	private static ParametersBinding fromParameters(BaseConfiguration cfg) {
		return fromParameters(cfg, ParametersBinding.class);
	}

	private static <I extends InheritingBinding> I fromInheritence(BaseConfiguration cfg, Class<I> inheritingType) {
		if (cfg == null) {
			return null;
		}

		I inst = null;
		try {
			// This method is private and we know only methods from this class
			// will be able to access it. Here is why only known classes with
			// accessible constructors will be passed in. Note: If this method
			// ever becomes public make sure you propagate the exceptions
			// properly.
			inst = inheritingType.newInstance();

			String sn = cfg.getShortName();
			if (sn != null && (sn = sn.trim()).length() > 0) {
				inst.setName(sn);
			}

			String inh = cfg.getInheritsFromRef();
			if (inh != null && (inh = inh.trim()).length() > 0) {
				inst.setInheritFrom(inh);
			}

		} catch (IllegalAccessException e) {
			// should not happen
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			// should not happen
			throw new RuntimeException(e);
		}
		return inst;
	}

	private static <P extends ParametersBinding> P fromParameters(BaseConfiguration cfg, Class<P> containerType) {
		P params = fromInheritence(cfg, containerType);

		List<String> keys = cfg.getKeys(BaseConfiguration.ONE_LEVEL);
		for (String key : keys) {
			if (!key.equals("inheritFrom") &&
				!key.equals("ProtectedParameters")) {
				ParameterBinding p = new ParameterBinding();
				p.setName(key);
				Object val = cfg.getParameterRaw(key);
				p.setValue(val != null ? val.toString().trim() : null);
				if (cfg.isProtectedParameter(key))
					p.setProtected(Boolean.TRUE);
				params.getParameters().add(p);
			}
		}

		return params;
	}

	private static AssemblyLineBinding fromAssemblyLine(AssemblyLineConfig al) throws Exception {
		if (al == null) {
			return null;
		}
		AssemblyLineBinding alb = new AssemblyLineBinding();
		alb.setName(al.getShortName());
		alb.setSettings(fromParameters(al.getSettings()));

		alb.setThreading(fromParameters(al.getThreadOptions()));
		// the thread options are named JavaProperties... makes no sense so
		// unset the name.
		if ("JavaProperties".equals(alb.getThreading().getName())) {
			alb.getThreading().setName("ThreadOptions");
		}

		alb.setHooks(fromHooks(al.getHooks()));
		alb.setLogging(fromLog(al.getLogConfig()));

		// for some strange reason the null config is not on the al itself but
		// within the Settings
		alb.setNull(fromNull(al.getSettings()));
		// I suppose now is a good time to fix that
		Iterator<ParameterBinding> it = alb.getSettings().getParameters().iterator();
		while (it.hasNext()) {
			ParameterBinding p = it.next();
			if ("nullBehavior".equals(p.getName()) || "nullBehaviorValue".equals(p.getName())
					|| "nullDefinition".equals(p.getName()) || "nullDefinitionValue".equals(p.getName())) {
				it.remove();
			}
		}

		SandboxConfig sandbox = al.getSandboxConfig();
		if (sandbox != null) {
			ALSandboxBinding sb = new ALSandboxBinding();
			sb.setIdentifier(sandbox.getIdentifier());
			alb.setSandbox(sb);
		}

		SimulationConfig sim = al.getSimulationConfig();
		if (sim != null) {
			ALSimulationBinding simb = new ALSimulationBinding();

			ProxyALBinding pab = new ProxyALBinding();
			switch (sim.getProxyALMode()) {
			case 0:
				pab.setMode(ProxyALModeEnum.SYNC);
				break;
			case 1:
				pab.setMode(ProxyALModeEnum.ASYNC);
				break;
			case 2:
				pab.setMode(ProxyALModeEnum.MANUAL);
				break;
			default:
				pab.setMode(null);
				break;
			}
			pab.setServer(sim.getProxyALServer());
			pab.setConfigInstance(sim.getProxyALConfigInstance());
			pab.setAssemblyLine(sim.getProxyALName());
			pab.setDebug(sim.getProxyALDebug());
			simb.setProxy(pab);
			alb.setSimulation(simb);
			// components' simulation state is handled automatically during
			// alComponent conversation.
		}

		ContainerConfig ops = al.getOperations();
		if (ops != null) {
			ALOperationsBinding aosb = new ALOperationsBinding();

			for (int i = 0; i < ops.size(); i++) {
				BaseConfiguration bcfg = ops.getConfig(i);
				OperationConfig op = (OperationConfig) bcfg;
				if (op != null) {
					ALOperationBinding aob = new ALOperationBinding();
					aob.setName(op.getShortName());
					aob.getSchemas().add(fromSchema(op.getSchema(true)));
					aob.getSchemas().add(fromSchema(op.getSchema(false)));
					aob.getAttributeMaps().add(fromAttributeMap(op.getAttributeMap(true)));
					aob.getAttributeMaps().add(fromAttributeMap(op.getAttributeMap(false)));
					aosb.getOperations().add(aob);
				}
			}
			alb.setOperations(aosb);
		}

		SchemaConfig initParamsSchema = al.getPublishedInitParams();
		if (initParamsSchema != null) {
			ALInitParamsBinding ipb = new ALInitParamsBinding();
			ipb.setSchema(fromSchema(initParamsSchema));
			alb.setInitParams(ipb);
		}

		// handle components...
		ALComponentsBinding feedC = fromALContainer(al.getEntryFeedComponents());
		if (feedC != null) {
			alb.getContainers().add(feedC);
		}

		ALComponentsBinding flowC = fromALContainer(al.getDataFlowComponents());
		if (flowC != null) {
			alb.getContainers().add(flowC);
		}

		return alb;
	}

	private static ALComponentsBinding fromALContainer(ContainerConfig cc) throws Exception {
		if (cc == null) {
			return null;
		}

		ALComponentsBinding comps = new ALComponentsBinding();
		comps.setName(cc.getShortName());
		addChildALComponents(cc, comps.getComponents());

		return comps;
	}

	private static void addChildALComponents(ContainerConfig cc, List<ALComponentBinding> toList) throws Exception {
		for (int i = 0; i < cc.size(); i++) {
			BaseConfiguration config = cc.getConfig(i);
			ALComponentBinding comp = null;
			if (config instanceof ScriptConfig || config instanceof AttributeMapConfig || config instanceof ALMappingConfig) {
				comp = fromSimpleComponent(config);
			} else if (config instanceof BranchingConfig) {
				comp = fromCompositeComponent((BranchingConfig) config);
			} else if (config instanceof ConnectorConfig) {
				comp = fromComplexComponent((ConnectorConfig) config);
			}

			if (comp != null) {
				toList.add(comp);
			}
		}
	}

	private static ComplexALComponentBinding fromComplexComponent(ConnectorConfig cfg) throws Exception {
		if (cfg == null) {
			return null;
		}
		ComplexALComponentBinding comp = new ComplexALComponentBinding();
		comp.setName(cfg.getShortName());
		comp.setComplex((ComplexComponentBinding) fromBaseConfig(cfg));
		// if (comp.getComplexConfig() instanceof ConnectorBinding) {
		// // pool definition is only configured under the Connectors/Functions
		// // folder, not in an AL. Inherited values are manually resolved.
		// ((ConnectorBinding) comp.getComplexConfig()).setPoolDef(null);
		// }

		comp.setSimulateState(getSimulationState(cfg));
		comp.setSandboxPlayback(cfg.getSandboxConfig().getPlaybackEnabled());
		comp.setSandboxRecord(cfg.getSandboxConfig().getRecordEnabled());
		comp.setState(cfg.getState());

		switch (cfg.getInitializeOption()) {
		case ConnectorConfig.COMP_INIT_DEFAULT:
			comp.setInitialize(ALComponentInitializeEnum.ON_STARTUP);
			break;
		case ConnectorConfig.COMP_INIT_USE:
			comp.setInitialize(ALComponentInitializeEnum.ON_FIRST_USE);
			break;
		case ConnectorConfig.COMP_INIT_MODIFIED:
			comp.setInitialize(ALComponentInitializeEnum.ON_CONFIG_MODIFY);
			break;
		case ConnectorConfig.COMP_INIT_EVERYTIME:
			comp.setInitialize(ALComponentInitializeEnum.ON_EVERY_USE);
			break;
		default:
			comp.setInitialize(null);
			break;
		}

		// the al component name is what is used not the wrapped
		// configuration name, so remove duplicates.
		comp.getComplexConfig().setName(null);

		return comp;
	}

	private static String getSimulationState(BaseConfiguration cfg) throws Exception {
		String simState = null;
		String sn = cfg.getShortName();
		if (sn != null) {
			BaseConfiguration parent = cfg.getParent();
			while (parent != null) {
				if (parent instanceof AssemblyLineConfig) {
					simState = ((AssemblyLineConfig) parent).getSimulationConfig().getComponentSimState(sn);
					break;
				}
				parent = parent.getParent();
			}
		}

		return simState;
	}

	private static CompositeALComponentBinding fromCompositeComponent(BranchingConfig cfg) throws Exception {
		if (cfg == null) {
			return null;
		}
		CompositeALComponentBinding comp = new CompositeALComponentBinding();
		comp.setName(cfg.getShortName());
		comp.setComposite((CompositeComponentBinding) fromBaseConfig(cfg));
		comp.setState(cfg.getEnabled() ? ALComponentStateEnum.ENABLED : ALComponentStateEnum.DISABLED);
		addChildALComponents(cfg, comp.getComponents());

		// the al component name is what is used not the wrapped
		// configuration name, so remove duplicates.
		comp.getCompositeConfig().setName(null);

		return comp;
	}

	private static SimpleALComponentBinding fromSimpleComponent(BaseConfiguration cfg) throws Exception {
		if (cfg == null) {
			return null;
		}
		SimpleALComponentBinding comp = new SimpleALComponentBinding();
		comp.setName(cfg.getShortName());
		comp.setSimple((SimpleComponentBinding) fromBaseConfig(cfg));
		comp.setSimulateState("Enabled".equals(getSimulationState(cfg)) ? ALComponentStateEnum.ENABLED
				: ALComponentStateEnum.DISABLED);

		if (cfg instanceof ALMappingConfig) {
			comp.setState(ALComponentStateEnum.fromValue(((ALMappingConfig) cfg).getState()));

			String inh = ((ALMappingConfig) cfg).getAttributeMap().getInheritsFromRef();
			if (BaseConfiguration.INHERIT_PARENT.equals(inh)) {
				// if child AttributeMapConfig is inheriting from parent then
				// ALMapping must know where to inherit exactly.
				comp.getSimpleConfig().setInheritFrom(cfg.getInheritsFromRef());
			}
		} else {
			comp.setState(cfg.getEnabled() ? ALComponentStateEnum.ENABLED : ALComponentStateEnum.DISABLED);
		}

		// the al component name is what is used not the wrapped
		// configuration name, so remove duplicates.
		comp.getSimpleConfig().setName(null);

		return comp;
	}

	private static NullBinding fromNull(BaseConfiguration nCfg) {
		if (nCfg == null) {
			return null;
		}
		NullBinding nb = new NullBinding();

		nb.setBehavior(nCfg.getNullBehavior());
		nb.setBehaviorValue(nCfg.getNullBehaviorValue());
		nb.setDefinition(nCfg.getNullDefinition());
		nb.setDefinitionValue(nCfg.getNullDefinitionValue());

		return nb;
	}

	private static LogBinding fromLog(LogConfig log) {
		if (log == null) {
			return null;
		}

		LogBinding lb = new LogBinding();
		for (LogConfigItem item : log.getItems()) {
			LogItemBinding lib = fromParameters(item, LogItemBinding.class);
			lb.getItems().add(lib);
		}

		return lb;
	}

	private static HooksBinding fromHooks(HooksConfig cfg) {
		if (cfg == null) {
			return null;
		}
		HooksBinding hsb = fromInheritence(cfg, HooksBinding.class);

		List<String> list = cfg.getKeys(BaseConfiguration.RECURSIVE_SUBTREE);
		for (String str : list) {
			HookConfig hc = cfg.getHook(str);
			HookBinding hb = fromInheritence(hc, HookBinding.class);
			hb.setEnabled(hc.getEnabled());
			hb.setScript(hc.getScript());
			hsb.getHooks().add(hb);
		}

		return hsb;
	}

	private static JavaClassBinding fromRawConfig(BaseConfiguration cfg) {
		if (cfg == null) {
			return null;
		}

		JavaClassBinding jcb = fromParameters(cfg, JavaClassBinding.class);
		Iterator<ParameterBinding> it = jcb.getParameters().iterator();

		ParameterBinding pb = null;
		while (it.hasNext()) {
			pb = it.next();
			if (pb.getName() != null && pb.getName().equals("connectorType")) {
				jcb.setClassName(pb.getValue());
				it.remove();
				break;
			}
		}

		return jcb;
	}

	private static ComplexComponentBinding fromComplex(ConnectorConfig cfg) {
		if (cfg == null) {
			return null;
		}

		ComplexComponentBinding comp = fromInheritence(cfg, cfg instanceof FunctionConfig ? FunctionBinding.class
				: ConnectorBinding.class);
		comp.setHooks(fromHooks(cfg.getHooks()));
		comp.setRawConfig(fromRawConfig(cfg instanceof FunctionConfig ? ((FunctionConfig) cfg).getFunctionConfig() : cfg
				.getConnectionConfig()));
		comp.setParser(fromParser(cfg.getParserConfig()));

		comp.getSchemas().add(fromSchema(cfg.getSchema(false)));
		comp.getSchemas().add(fromSchema(cfg.getSchema(true)));

		comp.getMaps().add(fromAttributeMap(cfg.getAttributeMap(false)));
		comp.getMaps().add(fromAttributeMap(cfg.getAttributeMap(true)));

		return comp;
	}

	private static FunctionBinding fromFunction(FunctionConfig cfg) {
		return (FunctionBinding) fromComplex(cfg);
	}

	private static ConnectorBinding fromConnector(ConnectorConfig cfg) {
		ConnectorBinding conn = (ConnectorBinding) fromComplex(cfg);
		if (conn != null) {
			conn.setMode(ConnectorModeEnum.fromValue(cfg.getMode()));
			conn.setState(cfg.getState());

			switch (conn.getMode()) {
			case UPDATE:
			case DELETE:
			case DELTA:
			case LOOKUP:
				ConnectorModeBinding cmb = new ConnectorModeBinding();
				cmb.setComputeChanges(cfg.getComputeChanges());
				cmb.setSkipLookup(cfg.getSkipLookup());
				String lookupLimit = cfg.getStringParameter("findreturncount");
				if (lookupLimit != null) {
					try {
						cmb.setLookupLimit(Integer.valueOf(lookupLimit));
					} catch (NumberFormatException nfe) {
						; // just ignore
					}
				}

				cmb.setProcessDeltaEntryOnly(cfg.getDeltaStrict());
				cmb.setSkipDeltaEntryDelete(cfg.getDeltaBehavior() == 0);
				conn.setModeConfig(cmb);
				conn.setLinkCriteria(fromLinkCriteria(cfg.getLinkCriteria()));
				break;
			case ITERATOR:
				conn.setDeltaConfig(fromDelta(cfg.getDeltaConfig()));
				break;
			}

			conn.setPoolDef(fromPoolDef(cfg.getPoolDefConfig()));
			conn.setPoolInst(fromPoolInst(cfg.getPoolInstanceConfig()));
			conn.setReconnect(fromReconnect(cfg.getReconnectConfig()));
		}

		return conn;
	}

	private static ReconnectBinding fromReconnect(ReconnectConfig cfg) {
		if (cfg == null) {
			return null;
		}

		ReconnectBinding rb = new ReconnectBinding();
		rb.setAutoSkipForward(cfg.getAutoSkipForward());
		rb.setNumberOfRetries(cfg.getRetries());
		rb.setOnConnectionError(cfg.getAutoReconnect());
		rb.setOnInitializationError(cfg.getInitReconnect());
		rb.setRetryDelay(cfg.getDelay());

		ContainerConfig rules = cfg.getReconnectRules();
		for (int i = 0; i < rules.size(); i++) {
			BaseConfiguration bcfg = rules.getConfig(i);
			ReconnectRuleConfig rrc = (ReconnectRuleConfig) bcfg;
			ReconnectRuleBinding rrb = new ReconnectRuleBinding();
			rrb.setAction(rrc.getAction());
			rrb.setExceptionClass(rrc.getExceptionClass());
			rrb.setExceptionMsgRegEx(rrc.getExceptionMessageRegExp());
			rb.getRules().add(rrb);
		}

		return rb;
	}

	private static PoolInstanceBinding fromPoolInst(PoolInstanceConfig cfg) {
		if (cfg == null) {
			return null;
		}

		PoolInstanceBinding pib = new PoolInstanceBinding();
		pib.setEnabled(cfg.getPoolEnabled());
		switch (cfg.getExhaustedPoolBehavior()) {
		case PoolInstanceConfig.EXHAUSTED_POOL_FAIL:
			pib.setOnExhausted(PoolInstanceExhaustedEnum.FAIL);
			break;
		case PoolInstanceConfig.EXHAUSTED_POOL_WAIT:
			pib.setOnExhausted(PoolInstanceExhaustedEnum.WAIT);
			break;
		default:
			pib.setOnExhausted(null);
			break;
		}

		return pib;
	}

	private static PoolDefinitionBinding fromPoolDef(PoolDefConfig cfg) {
		if (cfg == null) {
			return null;
		}

		PoolDefinitionBinding pdb = new PoolDefinitionBinding();
		pdb.setEnabled(cfg.getPoolEnabled());
		pdb.setInitializeAttempts(cfg.getInitializeAttempts());
		pdb.setInitializeSleepInterval(cfg.getInitializeSleepInterval());
		pdb.setMaxSize(cfg.getMaxPoolSize());
		pdb.setMinSize(cfg.getMinPoolSize());
		pdb.setPurgeInterval(cfg.getPurgeInterval());

		return pdb;
	}

	private static LinkCriteriaBinding fromLinkCriteria(LinkCriteriaConfig cfg) {
		if (cfg == null) {
			return null;
		}

		LinkCriteriaBinding lcb = fromInheritence(cfg, LinkCriteriaBinding.class);
		lcb.setAdvanced(cfg.getAdvancedLinkMode());
		lcb.setScript(cfg.getAdvancedLinkCriteria());
		lcb.setMatchAny(cfg.getMatchAny());

		List<String> list = cfg.getCriteriaNames();
		for (int i = 0; i < list.size(); i++) {
			if (!cfg.isCriteriaLocal(list.get(i)))
				continue;
			LinkCriteriaItem lci = cfg.getCriteria(list.get(i));
			LinkCriteriaItemBinding lcib = new LinkCriteriaItemBinding();

			lcib.setKey(lci.getShortName());

			Object attr = lci.getAttribute();
			lcib.setAttribute(attr != null ? attr.toString() : null);

			Object op = lci.getOper();
			lcib.setOperator(op != null ? op.toString() : null);

			Object val = lci.getValue();
			lcib.setValue(val != null ? val.toString() : null);

			lcb.getItems().add(lcib);
		}

		return lcb;
	}

	private static DeltaBinding fromDelta(DeltaConfig d) {
		if (d == null) {
			return null;
		}

		DeltaBinding db = new DeltaBinding();
		db.setAllowDuplicateKeys(d.getAllowDuplicateDeltaKeys());
		db.setUniqueAttribute(d.getUniqueAttribute());
		db.setChangeDetectionAttributes(d.getAttributeList());
		db.setDeltaDb(d.getDeltaDB());
		db.setEnabled(d.getEnabled());
		db.setFasterAlgorithm(d.getFastAlgorithm());
		db.setReadDeleted(d.getIterateDeleted());
		db.setRemoveDeleted(d.getRemoveDeleted());
		db.setReturnUnchanged(d.getReturnUnchanged());

		String cdm = d.getChangeDetectionMode();
		if (cdm != null && (cdm = cdm.trim()).length() > 0) {
			DeltaChangeDetectionModeEnum cdmEnum = null;
			if ("IGNORE_ATTRIBUTES".equals(cdm)) {
				cdmEnum = DeltaChangeDetectionModeEnum.IGNORE_ATTRIBUTES;
			} else if ("DETECT_ATTRIBUTES".equals(cdm)) {
				cdmEnum = DeltaChangeDetectionModeEnum.DETECT_ATTRIBUTES;
			} else if ("DETECT_ALL".equals(cdm)) {
				cdmEnum = DeltaChangeDetectionModeEnum.DETECT_ALL;
			}
			db.setChangeDetectionMode(cdmEnum);
		}

		String commit = d.getWhenToCommit();
		if (commit != null && (commit = commit.trim()).length() > 0) {
			DeltaCommitEnum comEnum = null;
			if ("After every database operation".equals(commit)) {
				comEnum = DeltaCommitEnum.ON_EVERY_OP;
			} else if ("On end of AL cycle".equals(commit)) {
				comEnum = DeltaCommitEnum.ON_AL_CYCLE;
			} else if ("On Connector close".equals(commit)) {
				comEnum = DeltaCommitEnum.ON_AL_END;
			} else if ("No autocommit".equals(commit)) {
				comEnum = DeltaCommitEnum.CUSTOM;
			}
			db.setCommit(comEnum);
		}

		String rowLock = d.getRowLocking();
		if (rowLock != null && (rowLock = rowLock.trim()).length() > 0) {
			DeltaRowLockingEnum lockEnum = null;
			if ("READ_UNCOMMITTED".equals(rowLock)) {
				lockEnum = DeltaRowLockingEnum.READ_UNCOMMITED;
			} else if ("READ_COMMITTED".equals(rowLock)) {
				lockEnum = DeltaRowLockingEnum.READ_COMMITED;
			} else if ("REPEATABLE_READ".equals(rowLock)) {
				lockEnum = DeltaRowLockingEnum.REPEATABLE_READ;
			} else if ("SERIALIZABLE".equals(rowLock)) {
				lockEnum = DeltaRowLockingEnum.SERIALIZABLE;
			}
			db.setRowLocking(lockEnum);
		}

		return db;
	}

	private static ParserBinding fromParser(ParserConfig cfg) {
		if (cfg == null) {
			return null;
		}
		ParserBinding pb = fromInheritence(cfg, ParserBinding.class);
		pb.setRawConfig(fromRawConfig(cfg));

		// we know the raw config of a parser is always inheriting from parent
		pb.getRawConfig().setInheritFrom(BaseConfiguration.INHERIT_PARENT);
		// the name of the raw config is actually the name of the parser itself.
		// must remove it to avoid confusion.
		pb.getRawConfig().setName(null);

		pb.setUserComment(cfg.getUserComment());

		pb.getSchemas().add(fromSchema(cfg.getSchema(true)));
		pb.getSchemas().add(fromSchema(cfg.getSchema(false)));

		return pb;
	}

	private static PropertyStoreBinding fromPropertyStore(PropertyStoreConfig cfg) {
		if (cfg == null) {
			return null;
		}

		PropertyStoreBinding psb = new PropertyStoreBinding();
		psb.setName(cfg.getShortName());
		psb.setCacheTimeout(cfg.getCacheTimeout());
		psb.setConnector(fromRawConfig(cfg.getConnectionConfig()));
		psb.setParser(fromRawConfig(cfg.getParserConfig()));
		psb.setInitialLoad(cfg.getInitialLoad());
		psb.setKeyName(cfg.getKeyAttribute());
		psb.setValueName(cfg.getValueAttribute());
		psb.setReadOnly(cfg.getReadOnly());
		psb.setNameFilters(cfg.getNameFilters());

		return psb;
	}

	private static ScriptBinding fromScript(ScriptConfig cfg) {
		if (cfg == null) {
			return null;
		}
		ScriptBinding sb = fromInheritence(cfg, ScriptBinding.class);
		// InternalSchema.SC_AUTO_INCLUDE
		if(cfg.hasParameter("autoInclude"))
			sb.setAutoInclude(cfg.getAutoInclude());
		if(cfg.hasParameter("script"))
			sb.setScript(cfg.getScript());

		// InternalSchema.SC_INCLUDE_FILES
		if(cfg.hasParameter("includeFiles")) {
			String files = cfg.getIncludeFiles();
			if (files != null && (files = files.trim()).length() > 0) {
				StringTokenizer st = new StringTokenizer(files, "\r\n");
				while (st.hasMoreTokens()) {
					String fileName = st.nextToken().trim();
					if (fileName.length() > 0) {
						sb.getFiles().add(fileName);
					}
				}
			}
		}

		return sb;
	}

	private static LoopBinding fromLoop(LoopConfig cfg) throws Exception {
		if (cfg == null) {
			return null;
		}
		LoopBinding lb = fromInheritence(cfg, LoopBinding.class);
		switch (cfg.getLoopType()) {
		case LoopConfig.LOOP_COLLECTION:
			CollectionLoopBinding collb = new CollectionLoopBinding();
			collb.setCollectionAttribute(cfg.getWorkAttributeName());
			collb.setAssignAttribute(cfg.getLoopAttributeName());
			lb.setCollectionCondition(collb);
			break;
		case LoopConfig.LOOP_CONDITIONS:
			lb.setWhileCondition(fromCondition(cfg));
			break;
		case LoopConfig.LOOP_CONNECTOR_FC:
			ConnectorLoopBinding connb = new ConnectorLoopBinding();
			connb.setConnector(fromConnector(cfg.getLoopConnector()));
			switch (cfg.getInitConnectorOption()) {
			case LoopConfig.OPTION_NONE:
				connb.setInitialize(ConnectorLoopInitializeEnum.ON_STARTUP);
				connb.setSelectEntries(ConnectorLoopSelectEntriesEnum.ON_INITIALIZE);
				break;
			case LoopConfig.OPTION_INITIALIZE:
				connb.setInitialize(ConnectorLoopInitializeEnum.ON_EVERY_USE);
				connb.setSelectEntries(ConnectorLoopSelectEntriesEnum.ON_INITIALIZE);
				break;
			case LoopConfig.OPTION_SELECT:
				connb.setInitialize(ConnectorLoopInitializeEnum.ON_STARTUP);
				connb.setSelectEntries(ConnectorLoopSelectEntriesEnum.ON_EVERY_USE);
				break;
			}
			lb.setConnectorCondition(connb);
			// connector name has no meaning here, remove duplicates
			connb.getConnector().setName(null);
			break;
		}
		return lb;
	}

	private static ConditionBinding fromCondition(BranchingConfig cfg) {
		if (cfg == null) {
			return null;
		}
		ConditionBinding cb = new ConditionBinding();
		cb.setMatchAny(cfg.getMatchAny());
		cb.setScript(cfg.getScript());

		ContainerConfig cont = cfg.getConditions();
		for (int i = 0; i < cont.size(); i++) {
			BaseConfiguration bcfg = cont.getConfig(i);
			BranchCondition bc = (BranchCondition) bcfg;
			ConditionItemBinding cib = new ConditionItemBinding();
			cib.setCaseSensitive(bc.getCaseSensitive());
			cib.setLeftHand(bc.getLeftHand());
			cib.setOperator(bc.getOperator());
			cib.setRightHand(bc.getRightHand());
			cib.setMatchAny(bc.getMatchAny());
			cib.setNegate(bc.getNegate());
			cb.getItems().add(cib);
		}

		return cb;
	}

	private static BranchBinding fromBranch(BranchingConfig cfg) {
		if (cfg == null) {
			return null;
		}

		BranchBinding bb = fromInheritence(cfg, BranchBinding.class);
		bb.setCondition(fromCondition(cfg));
		switch (cfg.getBranchType()) {
		case BranchingConfig.BRANCH_IF:
			bb.setType(BranchTypeEnum.IF);
			break;
		case BranchingConfig.BRANCH_ELSE:
			bb.setType(BranchTypeEnum.ELSE);
			break;
		case BranchingConfig.BRANCH_ELSEIF:
			bb.setType(BranchTypeEnum.ELSE_IF);
			break;
		case BranchingConfig.BRANCH_SWITCH:
			bb.setType(BranchTypeEnum.SWITCH);
			break;
		case BranchingConfig.BRANCH_CASE:
			bb.setType(BranchTypeEnum.CASE);
			break;
		}

		return bb;
	}

	private static SchemaBinding fromSchema(SchemaConfig cfg) {
		if (cfg == null) {
			return null;
		}

		SchemaBinding sb = fromInheritence(cfg, SchemaBinding.class);
		for (String name : cfg.getItemNames()) {
			sb.getItems().add(fromSchemaItem(cfg.getItem(name)));
		}

		return sb;
	}

	private static SchemaItemBinding fromSchemaItem(SchemaItemConfig cfg) {
		if (cfg == null) {
			return null;
		}

		SchemaItemBinding sib = new SchemaItemBinding();
		sib.setName(cfg.getShortName());
		sib.setMinOccurs(cfg.getMinOccurrences());
		sib.setMaxOccurs(cfg.getMaxOccurrences() == Integer.MAX_VALUE ? "unbounded" : Integer.toString(cfg.getMaxOccurrences()));
		sib.setNativeSyntax(cfg.getExternalSyntax());
		sib.setSyntax(cfg.getJavaClass());
		sib.setSample(cfg.getSample() != null ? cfg.getSample().toString() : null);
		sib.setType(cfg.isProperty() ? SchemaItemTypeEnum.PROPERTY : SchemaItemTypeEnum.ATTRIBUTE);
		sib.setComment(cfg.getUserComment());

		ContainerConfig children = cfg.getChildSchemaList();
		for (int i = 0; i < children.size(); i++) {
			BaseConfiguration bcfg = children.getConfig(i);
			sib.getItems().add(fromSchemaItem((SchemaItemConfig) bcfg));
		}

		return sib;
	}

	private static AttributeMapBinding fromALMapping(ALMappingConfig cfg) {
		if (cfg == null) {
			return null;
		}

		AttributeMapBinding map = fromAttributeMap(cfg.getAttributeMap());

		// when the AttributeMap is wrapped within an ALMap and when parent is a
		// MetamergeFolder the AL Map is carrying the name.
		if (cfg.getParent() == null || cfg.getParent() instanceof MetamergeFolder) {
			map.setName(cfg.getShortName());
		}

		return map;
	}

	private static AttributeMapBinding fromAttributeMap(AttributeMapConfig cfg) {
		if (cfg == null) {
			return null;
		}

		AttributeMapBinding amb = fromInheritence(cfg, AttributeMapBinding.class);
		amb.setNull(fromNull(cfg));

		List<String> list = cfg.getKeys(BaseConfiguration.SUBTREE);
		for (int idx = 0; idx < list.size(); idx++) {
			AttributeMapItem i = cfg.getAttributeMapItem(list.get(idx));
			AttributeMapItemBinding amib = fromInheritence(i, AttributeMapItemBinding.class);
			amib.setEnabled(i.getEnabled());
			amib.setAdd(i.getAdd());
			amib.setModify(i.getModify());
			String type = i.getType();
			if (AttributeMapItem.SIMPLE_MAPPING.equals(type)) {
				amib.setType(AttributeMapItemTypeEnum.SIMPLE);
				amib.setMapsTo(i.getSimple());
			} else if (AttributeMapItem.SUBSTITUTION_MAPPING.equals(type)) {
				amib.setType(AttributeMapItemTypeEnum.SUBSTITUTION);
				amib.setMapsTo(i.getSubstitution());
			} else if (AttributeMapItem.ADVANCED_MAPPING.equals(type)) {
				amib.setType(AttributeMapItemTypeEnum.ADVANCED);
				amib.setMapsTo(i.getScript());
			}
			amb.getItems().add(amib);
		}

		return amb;
	}

	private static SolutionContextBinding fromContext(SolutionInterface i, LogConfig l, LibraryConfig jl, TombstonesConfig t,
			InstanceConfig ic, ContainerConfig sysStore) {

		SolutionContextBinding scb = new SolutionContextBinding();
		scb.setInterface(fromSolutionInterface(i));
		scb.setLog(fromLog(l));
		scb.setLibraries(fromSolutionLibrary(jl));
		scb.setTombstone(fromParameters(t));
		scb.setInstance(fromSolutionInstance(ic));
		if (sysStore != null && sysStore.size() > 0) {
			scb.setSystemStore(fromParameters(sysStore.getConfig(0)));
			// the parameters name is "Default" which says nothing when
			// serialized... set the name of the container instead
			scb.getSystemStore().setName(sysStore.getShortName());
		}

		return scb;
	}

	private static SolutionInstanceBinding fromSolutionInstance(InstanceConfig cfg) {
		if (cfg == null) {
			return null;
		}

		SolutionInstanceBinding sib = new SolutionInstanceBinding();
		for (String alName : cfg.getStartupItems().getChildNames()) {
			sib.getAutostartALs().add(alName);
		}

		return sib;
	}

	private static SolutionLibraryBinding fromSolutionLibrary(LibraryConfig cfg) {
		if (cfg == null) {
			return null;
		}

		SolutionLibraryBinding slb = new SolutionLibraryBinding();

		for (String key : cfg.getKeys(BaseConfiguration.ONE_LEVEL)) {
			if (!key.equals("inheritFrom")) {
				ParameterBinding p = new ParameterBinding();
				p.setName(key);
				Object val = cfg.getParameter(key);
				p.setValue(val != null ? val.toString().trim() : null);
				slb.getLibraries().add(p);
			}
		}

		return slb;
	}

	private static SolutionInterfaceBinding fromSolutionInterface(SolutionInterface cfg) {
		if (cfg == null) {
			return null;
		}

		SolutionInterfaceBinding sib = new SolutionInterfaceBinding();
		sib.setUserComment(cfg.getUserComment());
		sib.setEnabled(cfg.getEnabled());
		sib.setPollInterval(cfg.getHealthPollInterval());
		sib.setHealthAl(cfg.getHealthAssemblyLine());
		sib.setSolutionName("".equals(cfg.getInstanceID()) ? null : cfg.getInstanceID());

		ContainerConfig exAls = cfg.getExposedAssemblyLines();
		if (exAls != null) {
			for (String alName : exAls.getChildNames()) {
				ExposedAlBinding eab = new ExposedAlBinding();
				eab.setName(alName);
				sib.getAls().add(eab);
			}
		}

		ContainerConfig exPs = cfg.getExposedProperties();
		if (exPs != null) {
			for (int i = 0; i < exPs.size(); i++) {
				BaseConfiguration bcfg = exPs.getConfig(i);
				ExposedProperty prop = (ExposedProperty) bcfg;
				ExposedPropertyBinding epb = new ExposedPropertyBinding();
				epb.setName(prop.getPropertyName());
				epb.setCategory(prop.getCategory());
				epb.setLabel(prop.getLabel());
				epb.setStoreName(prop.getStoreName());
				epb.setUserComment(prop.getUserComment());
				sib.getProperties().add(epb);
			}
		}

		return sib;
	}

	@SuppressWarnings("unchecked")
	public static MetamergeConfig toMetamergeConfig(SolutionBinding sb) throws Exception {
		if (sb == null) {
			return null;
		}

		Hashtable env = new Hashtable();
		env.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.xml.MetamergeConfigXML");
		env.put(MetamergeConfigFactory.MC_CREATE, false);
		env.put(MetamergeConfigFactory.MC_URL, "");
		MetamergeConfig cfg = MetamergeConfigFactory.getInstance(env);

		for (ContainerBinding cb : sb.getContainers()) {
			toFolder(cb, cfg);
		}

		toContext(sb.getContext(), cfg);

		return cfg;
	}

	private static void toFolder(ContainerBinding cfg, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return;
		}

		MetamergeFolder f = cfg.getName() != null ? (MetamergeFolder) dest.lookup(cfg.getName()) : null;

		if (f instanceof PropertyManager) {
			PropertyStoresBinding p = (PropertyStoresBinding) cfg;
			PropertyManager pm = (PropertyManager) f;

			for (NamedBinding b : p.getConfigs()) {
				if (b instanceof PropertyStoreBinding && b.getName() != null) {
					BaseConfiguration child = toBaseConfig(b, dest);

					if (child instanceof PropertyStoreConfig) {
						if (b.getName().equals(p.getDefault())) {
							pm.setDefaultPropertyStore((PropertyStoreConfig) child);
						}
						if (b.getName().equals(p.getPassword())) {
							pm.setDefaultPasswordStore((PropertyStoreConfig) child);
						}
						pm.addPropertyStore((PropertyStoreConfig) child);
					}
				}
			}
		} else if (f != null) {
			BaseConfiguration bcfg = null;
			for (NamedBinding child : cfg.getConfigs()) {
				if (child instanceof AttributeMapBinding) {
					bcfg = toALMapping(child.getName(), (AttributeMapBinding) child, dest);
				} else {
					bcfg = toBaseConfig(child, dest);
				}

				dest.bind(cfg.getName() + "/" + child.getName(), bcfg);
			}
		}
	}

	private static BaseConfiguration toBaseConfig(NamedBinding cfg, MetamergeConfig dest) throws Exception {
		BaseConfiguration b = null;

		if (cfg instanceof AssemblyLineBinding) {
			b = toAssemblyLine((AssemblyLineBinding) cfg, dest);
		} else if (cfg instanceof ComplexALComponentBinding) {
			b = toComplexComponent((ComplexALComponentBinding) cfg, null, dest);
		} else if (cfg instanceof CompositeALComponentBinding) {
			b = toCompositeComponent((CompositeALComponentBinding) cfg, dest);
		} else if (cfg instanceof SimpleALComponentBinding) {
			b = toSimpleComponent((SimpleALComponentBinding) cfg, dest);
		} else if (cfg instanceof ParserBinding) {
			b = toParser((ParserBinding) cfg, dest);
		} else if (cfg instanceof ConnectorBinding) {
			b = toConnector((ConnectorBinding) cfg, dest);
		} else if (cfg instanceof FunctionBinding) {
			b = toFunction((FunctionBinding) cfg, dest);
		} else if (cfg instanceof ScriptBinding) {
			b = toScript((ScriptBinding) cfg, dest);
		} else if (cfg instanceof SchemaBinding) {
			b = toSchema((SchemaBinding) cfg, dest);
		} else if (cfg instanceof PropertyStoreBinding) {
			b = toPropertyStore((PropertyStoreBinding) cfg, dest);
		} else if (cfg instanceof ALExecutionScheduleBinding) {
			b = toAlExecutionSchedule((ALExecutionScheduleBinding) cfg, dest);
		} else if (cfg instanceof ALReviverBinding) {
			b = toAlReviver((ALReviverBinding) cfg, dest);
		}

		return b;
	}

	private static SchedulerConfig toScheduler(ALStarterBinding cfg, int type, MetamergeConfig dest) throws Exception {
		SchedulerConfig sched = dest.newInstanceOf(SchedulerConfig.class);
		sched.setName(cfg.getName());
		sched.init();
		sched.setType(type);
		sched.setScheduledName(cfg.getAssemblyLine());
		if (cfg.getOperation() != null) {
			sched.setStringParameter("operation", cfg.getOperation());
		}
		if (cfg.getConfigInstance() != null) {
			sched.setStringParameter("config", cfg.getConfigInstance());
		}
		if (cfg.getServer() != null) {
			sched.setStringParameter("server", cfg.getServer());
		}
		for (ParameterBinding p : cfg.getInitParams().getParameters()) {
			sched.setStringParameter("$initialize." + p.getName(), p.getValue());
		}
		return sched;
	}

	private static SchedulerConfig toAlReviver(ALReviverBinding cfg, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return null;
		}
		SchedulerConfig sched = toScheduler(cfg, SchedulerConfig.KEEP_ALIVE, dest);
		if (cfg.getFailureAl() != null) {
			sched.setStringParameter("FailureAL", cfg.getFailureAl());
		}
		if (cfg.getFailIfAlDiedIn() != null) {
			sched.setParameter("WithinSeconds", cfg.getFailIfAlDiedIn());
		}
		sched.setEnabled(cfg.isEnabled());
		return sched;
	}

	private static SchedulerConfig toAlExecutionSchedule(ALExecutionScheduleBinding cfg, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return null;
		}

		SchedulerConfig sched = toScheduler(cfg, SchedulerConfig.TIMER, dest);
		sched.setStartTimes(cfg.getExecTimePattern());
		if (cfg.getFailureAl() != null) {
			sched.setStringParameter("FailureAL", cfg.getFailureAl());
		}
		sched.setParameter("NoStartIfRunning", cfg.isSkipExecIfAlRunning());
		sched.setParameter("stopOnFailure", cfg.isCancelScheduleOnAlFailure());
		sched.setEnabled(cfg.isEnabled());

		return sched;
	}

	private static BaseConfiguration toParameters(ParametersBinding cfg, MetamergeConfig dest) throws Exception {
		return toParameters(cfg, BaseConfiguration.class, dest);
	}

	private static <B extends BaseConfiguration> B toInheritence(InheritingBinding cfg, Class<B> cls, MetamergeConfig dest)
			throws Exception {
		if (cfg == null) {
			return null;
		}

		B b = dest.newInstanceOf(cls);
		b.setName(cfg.getName());
		if (cfg.getInheritFrom() != null) {
			b.setInheritsFromRef(cfg.getInheritFrom());
		}
		b.init();
		return b;
	}

	private static <B extends BaseConfiguration> B toParameters(ParametersBinding cfg, Class<B> cls, MetamergeConfig dest)
			throws Exception {
		if (cfg == null) {
			return null;
		}
		B b = toInheritence(cfg, cls, dest);

		for (ParameterBinding p : cfg.getParameters()) {
			if (p.isProtected())
				b.setProtectedParameter(p.getName(), p.getValue());
			else
				b.setParameter(p.getName(), p.getValue());
		}

		return b;
	}

	private static AssemblyLineConfig toAssemblyLine(AssemblyLineBinding cfg, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return null;
		}

		AssemblyLineConfig al = (AssemblyLineConfig) dest.newInstanceOf(MetamergeConfig.ASSEMBLYLINE_FOLDER);
		al.setName(cfg.getName());
		al.init();

		al.setSettings(toParameters(cfg.getSettings(), dest));
		toHooks(cfg.getHooks(), al.getHooks());
		toLog(cfg.getLogging(), al.getLogConfig(), dest);
		toNull(cfg.getNull(), al.getSettings());
		toSandbox(cfg.getSandbox(), al.getSandboxConfig());
		toSimulation(cfg.getSimulation(), al.getSimulationConfig());
		toOperations(cfg.getOperations(), al, dest);

		if (cfg.getThreading() != null) {
			for (ParameterBinding param : cfg.getThreading().getParameters()) {
				al.getThreadOptions().setParameter(param.getName(), param.getValue());
			}
		}

		if (cfg.getInitParams() != null) {
			toSchema(cfg.getInitParams().getSchema(), al.getPublishedInitParams(), dest);
		}

		for (ALComponentsBinding b : cfg.getContainers()) {
			if (al.getEntryFeedComponents().getShortName().equals(b.getName())) {
				toALContainer(b, al.getEntryFeedComponents(), dest);
			} else if (al.getDataFlowComponents().getShortName().equals(b.getName())) {
				toALContainer(b, al.getDataFlowComponents(), dest);
			}
		}

		return al;
	}

	private static void toOperations(ALOperationsBinding cfg, OperationsConfig dest, MetamergeConfig mc) throws Exception {
		if (cfg == null) {
			return;
		}

		ContainerConfig ops = dest.getOperations();

		if (ops != null) {
			for (ALOperationBinding b : cfg.getOperations()) {

				OperationConfig c = mc.newInstanceOf(OperationConfig.class);
				c.setName(b.getName());
				c.init();

				for (SchemaBinding s : b.getSchemas()) {
					toSchema(s, c.getSchema("Input".equalsIgnoreCase(s.getName())), mc);
				}
				for (AttributeMapBinding a : b.getAttributeMaps()) {
					toAttributeMap(a, c.getAttributeMap("Input".equalsIgnoreCase(a.getName())), mc);
				}
				ops.addConfig(c);
			}
		}
	}

	private static void toSimulation(ALSimulationBinding cfg, SimulationConfig dest) {
		if (cfg == null) {
			return;
		}

		ProxyALBinding proxy = cfg.getProxy();
		if (proxy != null) {
			dest.setProxyALServer(proxy.getServer());
			dest.setProxyALConfigInstance(proxy.getConfigInstance());
			dest.setProxyALName(proxy.getAssemblyLine());
			dest.setProxyALDebug(proxy.isDebug());
			switch (proxy.getMode()) {
			case SYNC:
				dest.setProxyALMode(0);
				break;
			case ASYNC:
				dest.setProxyALMode(1);
				break;
			case MANUAL:
				dest.setProxyALMode(2);
				break;
			default:
				break;
			}
		}

		// components' simulation state is handled automatically during
		// alComponent conversation.
	}

	private static void toSandbox(ALSandboxBinding cfg, SandboxConfig sbox) {
		if (cfg == null) {
			return;
		}
		sbox.setIdentifier(cfg.getIdentifier());
	}

	private static void toALContainer(ALComponentsBinding cfg, ContainerConfig cc, MetamergeConfig mc) throws Exception {
		if (cc == null) {
			return;
		}
		toChildALComponents(cfg.getComponents(), cc, mc);
	}

	private static void toChildALComponents(List<ALComponentBinding> fromList, ContainerConfig cc, MetamergeConfig dest)
			throws Exception {
		for (ALComponentBinding b : fromList) {
			BaseConfiguration c = null;
			if (b instanceof SimpleALComponentBinding) {
				c = toSimpleComponent((SimpleALComponentBinding) b, dest);
			} else if (b instanceof CompositeALComponentBinding) {
				c = toCompositeComponent((CompositeALComponentBinding) b, dest);
			} else if (b instanceof ComplexALComponentBinding) {
				c = toComplexComponent((ComplexALComponentBinding) b, (AssemblyLineConfig) cc.getParent(), dest);
			}
			cc.addConfig(c);
		}
	}

	private static ConnectorConfig toComplexComponent(ComplexALComponentBinding cfg, AssemblyLineConfig parent, MetamergeConfig dest)
			throws Exception {
		if (cfg == null) {
			return null;
		}

		ConnectorConfig cc;
		if (cfg.getComplexConfig() instanceof FunctionBinding) {
			cc = toFunction((FunctionBinding) ((ComplexALComponentBinding) cfg).getComplexConfig(), dest);
		} else {
			cc = toConnector((ConnectorBinding) ((ComplexALComponentBinding) cfg).getComplexConfig(), dest);
		}

		// ALComponentBinding contains the name...
		cc.setName(cfg.getName());

		if (parent != null) {
			parent.getSimulationConfig().setComponentSimState(cfg.getName(), cfg.getSimulateState());
		}
		cc.getSandboxConfig().setPlaybackEnabled(cfg.isSandboxPlayback());
		cc.getSandboxConfig().setRecordEnabled(cfg.isSandboxRecord());
		cc.setState(cfg.getState());

		switch (cfg.getInitialize()) {
		case ON_STARTUP:
			cc.setInitializeOption(ConnectorConfig.COMP_INIT_DEFAULT);
			break;
		case ON_FIRST_USE:
			cc.setInitializeOption(ConnectorConfig.COMP_INIT_USE);
			break;
		case ON_CONFIG_MODIFY:
			cc.setInitializeOption(ConnectorConfig.COMP_INIT_MODIFIED);
			break;
		case ON_EVERY_USE:
			cc.setInitializeOption(ConnectorConfig.COMP_INIT_EVERYTIME);
			break;
		}

		return cc;
	}

	private static BranchingConfig toCompositeComponent(CompositeALComponentBinding cfg, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return null;
		}

		BranchingConfig bc;
		if (cfg.getCompositeConfig() instanceof LoopBinding) {
			bc = dest.newInstanceOf(LoopConfig.class);
			bc.setName(cfg.getName());
			if (cfg.getCompositeConfig().getInheritFrom() != null) {
				bc.setInheritsFromRef(cfg.getCompositeConfig().getInheritFrom());
			}
			bc.init();
			toBranching((LoopBinding) cfg.getCompositeConfig(), (LoopConfig) bc, dest);
		} else {
			bc = dest.newInstanceOf(BranchingConfig.class);
			bc.setName(cfg.getName());
			if (cfg.getCompositeConfig().getInheritFrom() != null) {
				bc.setInheritsFromRef(cfg.getCompositeConfig().getInheritFrom());
			}
			bc.init();
			toBranching((BranchBinding) cfg.getCompositeConfig(), bc, dest);
		}

		bc.setEnabled(cfg.getState() == ALComponentStateEnum.ENABLED);
		toChildALComponents(cfg.getComponents(), bc, dest);
		return bc;
	}

	private static BaseConfiguration toSimpleComponent(SimpleALComponentBinding cfg, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return null;
		}

		BaseConfiguration simple = null;
		if (cfg.getSimpleConfig() instanceof ScriptBinding) {
			simple = toScript((ScriptBinding) cfg.getSimpleConfig(), dest);
			simple.setName(cfg.getName());
		} else if (cfg.getSimpleConfig() instanceof AttributeMapBinding) {
			simple = toALMapping(cfg.getName(), (AttributeMapBinding) cfg.getSimpleConfig(), dest);
		}
		
		if (simple != null) {
			simple.setEnabled(cfg.getState() == ALComponentStateEnum.ENABLED);
		}

		return simple;
	}

	private static void toNull(NullBinding cfg, BaseConfiguration dest) {
		if (cfg == null) {
			return;
		}

		dest.setNullBehavior(cfg.getBehavior());
		dest.setNullBehaviorValue(cfg.getBehaviorValue());
		dest.setNullDefinition(cfg.getDefinition());
		dest.setNullDefinitionValue(cfg.getDefinitionValue());
	}

	private static LogConfig toLog(LogBinding cfg, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return null;
		}

		LogConfig log = dest.newInstanceOf(LogConfig.class);
		log.init();
		return toLog(cfg, log, dest);
	}

	private static LogConfig toLog(LogBinding cfg, LogConfig log, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return null;
		}

		for (LogItemBinding item : cfg.getItems()) {
			log.addItem(toParameters(item, LogConfigItem.class, dest));
		}

		return log;
	}

	private static void toHooks(HooksBinding cfg, HooksConfig dest) {
		if (cfg == null) {
			return;
		}

		dest.setInheritsFromRef(cfg.getInheritFrom());
		for (HookBinding hook : cfg.getHooks()) {
			HookConfig h = dest.getHook(hook.getName(), true);
			h.setInheritsFromRef(hook.getInheritFrom());
			h.setEnabled(hook.isEnabled());
			h.setScript(hook.getScript());
		}
	}

	private static void toRawConfig(JavaClassBinding cfg, BaseConfiguration dest) {
		if (cfg == null) {
			return;
		}

		if (cfg.getInheritFrom() != null) {
			dest.setInheritsFromRef(cfg.getInheritFrom());
		}
		for (ParameterBinding p : cfg.getParameters()) {
			if (p.isProtected()) {
				try {
					dest.setProtectedParameter(p.getName(), p.getValue());
				} catch (Exception e) {
					dest.setParameter(p.getName(), p.getValue());
				}
			} else {
				dest.setParameter(p.getName(), p.getValue());
			}
		}
		if (cfg.getClassName() != null) {
			dest.setParameter("connectorType", cfg.getClassName());
		}
	}

	private static void toComplex(ComplexComponentBinding cfg, ConnectorConfig cc, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return;
		}

		toHooks(cfg.getHooks(), cc.getHooks());
		toRawConfig(cfg.getRawConfig(), cc instanceof FunctionConfig ? ((FunctionConfig) cc).getFunctionConfig() : cc
				.getConnectionConfig());
		if (cfg.getParser() != null) {
			cc.setParserConfig(toParser(cfg.getParser(), dest));
		}

		for (SchemaBinding s : cfg.getSchemas()) {
			toSchema(s, cc.getSchema("Input".equalsIgnoreCase(s.getName())), dest);
		}
		for (AttributeMapBinding a : cfg.getMaps()) {
			toAttributeMap(a, cc.getAttributeMap("Input".equalsIgnoreCase(a.getName())), dest);
		}
	}

	private static FunctionConfig toFunction(FunctionBinding cfg, MetamergeConfig dest) throws Exception {
		FunctionConfig fc = toInheritence(cfg, FunctionConfig.class, dest);
		toFunction(cfg, fc, dest);
		return fc;
	}

	private static void toFunction(FunctionBinding cfg, FunctionConfig fc, MetamergeConfig dest) throws Exception {
		toComplex(cfg, fc, dest);
	}

	private static ConnectorConfig toConnector(ConnectorBinding cfg, MetamergeConfig dest) throws Exception {
		ConnectorConfig cc = toInheritence(cfg, ConnectorConfig.class, dest);
		toConnector((ConnectorBinding) cfg, (ConnectorConfig) cc, dest);
		return cc;
	}

	private static void toConnector(ConnectorBinding cfg, ConnectorConfig cc, MetamergeConfig dest) throws Exception {
		toComplex(cfg, cc, dest);

		if (cfg != null) {
			cc.setMode(cfg.getMode().value());
			cc.setState(cfg.getState());

			if (cfg.getModeConfig() != null) {
				cc.setComputeChanges(cfg.getModeConfig().isComputeChanges());
				cc.setSkipLookup(cfg.getModeConfig().isSkipLookup());
				cc.setParameter("findreturncount", cfg.getModeConfig().getLookupLimit());
				cc.setDeltaStrict(cfg.getModeConfig().isProcessDeltaEntryOnly());
				cc.setDeltaBehavior(cfg.getModeConfig().isSkipDeltaEntryDelete() ? 0 : 1);
			}

			toDelta(cfg.getDeltaConfig(), cc.getDeltaConfig());
			toLinkCriteria(cfg.getLinkCriteria(), cc.getLinkCriteria(), dest);
			toPoolDef(cfg.getPoolDef(), cc.getPoolDefConfig());
			toPoolInst(cfg.getPoolInst(), cc.getPoolInstanceConfig());
			toReconnect(cfg.getReconnect(), cc.getReconnectConfig(), dest);
		}
	}

	private static void toReconnect(ReconnectBinding cfg, ReconnectConfig dest, MetamergeConfig mc) throws Exception {
		if (cfg == null) {
			return;
		}

		dest.setBooleanParameter("skipForwardAfterReconnect", cfg.isAutoSkipForward());
		dest.setIntegerParameter("numberOfRetries", cfg.getNumberOfRetries());
		dest.setBooleanParameter("autoreconnect", cfg.isOnConnectionError());
		dest.setBooleanParameter("initreconnect", cfg.isOnInitializationError());
		dest.setIntegerParameter("retryDelay", cfg.getRetryDelay());

		for (ReconnectRuleBinding b : cfg.getRules()) {
			ReconnectRuleConfig r = mc.newInstanceOf(ReconnectRuleConfig.class);
			r.init();
			r.setStringParameter("action", b.getAction());
			r.setStringParameter("exceptionClass", b.getExceptionClass());
			r.setStringParameter("exceptionMessageRegExp", b.getExceptionMsgRegEx());
			dest.getReconnectRules().addConfig(r);
		}
	}

	private static void toPoolInst(PoolInstanceBinding cfg, PoolInstanceConfig dest) throws Exception {
		if (cfg == null) {
			return;
		}

		dest.setPoolEnabled(cfg.isEnabled());
		switch (cfg.getOnExhausted()) {
		case FAIL:
			dest.setExhaustedPoolBehavior(PoolInstanceConfig.EXHAUSTED_POOL_FAIL);
			break;
		case WAIT:
			dest.setExhaustedPoolBehavior(PoolInstanceConfig.EXHAUSTED_POOL_WAIT);
			break;
		}
	}

	private static void toPoolDef(PoolDefinitionBinding cfg, PoolDefConfig dest) throws Exception {
		if (cfg == null) {
			return;
		}

		dest.setPoolEnabled(cfg.isEnabled());
		dest.setInitializeAttempts(cfg.getInitializeAttempts());
		dest.setInitializeSleepInterval(cfg.getInitializeSleepInterval());
		dest.setMaxPoolSize(cfg.getMaxSize());
		dest.setMinPoolSize(cfg.getMinSize());
		dest.setPurgeInterval(cfg.getPurgeInterval());
	}

	private static void toLinkCriteria(LinkCriteriaBinding cfg, LinkCriteriaConfig lc, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return;
		}

		if (cfg.getInheritFrom() != null) {
			lc.setInheritsFromRef(cfg.getInheritFrom());
		}
		lc.setAdvancedLinkMode(cfg.isAdvanced());
		lc.setAdvancedLinkCriteria(cfg.getScript());
		lc.setMatchAny(cfg.isMatchAny());

		for (LinkCriteriaItemBinding b : cfg.getItems()) {
			LinkCriteriaItem i = dest.newInstanceOf(LinkCriteriaItem.class);

			i.setName(b.getKey());
			i.setAttribute(b.getAttribute());
			i.setOper(b.getOperator());
			i.setValue(b.getValue());
			lc.setCriteria(i);
		}
	}

	private static void toDelta(DeltaBinding cfg, DeltaConfig dest) {
		if (cfg == null) {
			return;
		}

		dest.setAllowDuplicateDeltaKeys(cfg.isAllowDuplicateKeys());
		dest.setUniqueAttribute(cfg.getUniqueAttribute());
		dest.setAttributeList(cfg.getChangeDetectionAttributes());
		dest.setDeltaDB(cfg.getDeltaDb());
		dest.setEnabled(cfg.isEnabled());
		dest.setFastAlgorithm(cfg.isFasterAlgorithm());
		dest.setIterateDeleted(cfg.isReadDeleted());
		dest.setRemoveDeleted(cfg.isRemoveDeleted());
		dest.setReturnUnchanged(cfg.isReturnUnchanged());

		switch (cfg.getChangeDetectionMode()) {
		case IGNORE_ATTRIBUTES:
			dest.setChangeDetectionMode("IGNORE_ATTRIBUTES");
			break;
		case DETECT_ATTRIBUTES:
			dest.setChangeDetectionMode("DETECT_ATTRIBUTES");
			break;
		case DETECT_ALL:
			dest.setChangeDetectionMode("DETECT_ALL");
			break;
		}

		switch (cfg.getCommit()) {
		case ON_EVERY_OP:
			dest.setWhenToCommit("After every database operation");
			break;
		case ON_AL_CYCLE:
			dest.setWhenToCommit("On end of AL cycle");
			break;
		case ON_AL_END:
			dest.setWhenToCommit("On Connector close");
			break;
		case CUSTOM:
			dest.setWhenToCommit("No autocommit");
			break;
		}

		switch (cfg.getRowLocking()) {
		case READ_UNCOMMITED:
			dest.setRowLocking("READ_UNCOMMITTED");
			break;
		case READ_COMMITED:
			dest.setRowLocking("READ_COMMITTED");
			break;
		case REPEATABLE_READ:
			dest.setRowLocking("REPEATABLE_READ");
			break;
		case SERIALIZABLE:
			dest.setRowLocking("SERIALIZABLE");
			break;
		}
	}

	private static ParserConfig toParser(ParserBinding cfg, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return null;
		}

		ParserConfig pc = toInheritence(cfg, ParserConfig.class, dest);
		toRawConfig(cfg.getRawConfig(), pc);
		// above call will override the inherit from ref...
		if (cfg.getInheritFrom() != null
				&& (pc.getInheritsFromRef() == null || BaseConfiguration.INHERIT_PARENT.equals(pc.getInheritsFromRef()) || BaseConfiguration.INHERIT_NONE
						.equals(pc.getInheritsFromRef()))) {
			pc.setInheritsFromRef(cfg.getInheritFrom());
		}

		if (cfg.getUserComment() != null) {
			pc.setUserComment(cfg.getUserComment());
		}
		for (SchemaBinding s : cfg.getSchemas()) {
			toSchema(s, pc.getSchema("Input".equalsIgnoreCase(s.getName())), dest);
		}

		return pc;
	}

	private static PropertyStoreConfig toPropertyStore(PropertyStoreBinding cfg, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return null;
		}

		PropertyStoreConfig ps = dest.newInstanceOf(PropertyStoreConfig.class);
		ps.setName(cfg.getName());
		ps.init();
		ps.setCacheTimeout(cfg.getCacheTimeout());
		toRawConfig(cfg.getConnector(), ps.getConnectionConfig());
		toRawConfig(cfg.getParser(), ps.getParserConfig());
		ps.setInitialLoad(cfg.isInitialLoad());
		ps.setKeyAttribute(cfg.getKeyName());
		ps.setValueAttribute(cfg.getValueName());
		ps.setReadOnly(cfg.isReadOnly());
		ps.setNameFilters(cfg.getNameFilters());

		return ps;
	}

	private static ScriptConfig toScript(ScriptBinding cfg, MetamergeConfig mc) throws Exception {
		if (cfg == null) {
			return null;
		}

		ScriptConfig dest = toInheritence(cfg, ScriptConfig.class, mc);

		dest.setAutoInclude(cfg.isAutoInclude());
		dest.setScript(cfg.getScript());

		StringBuilder files = new StringBuilder();
		for (String fileName : cfg.getFiles()) {
			files.append(fileName);
			files.append("\r\n");
		}

		if (files.length() > 0) {
			files.deleteCharAt(files.length() - 1);
			files.deleteCharAt(files.length() - 1);
		}

		dest.setIncludeFiles(files.toString());
		return dest;
	}

	private static void toBranching(LoopBinding lb, LoopConfig dest, MetamergeConfig mc) throws Exception {
		if (lb == null) {
			return;
		}

		if (lb.getCollectionCondition() != null) {
			dest.setWorkAttributeName(lb.getCollectionCondition().getCollectionAttribute());
			dest.setLoopAttributeName(lb.getCollectionCondition().getAssignAttribute());
			dest.setLoopType(LoopConfig.LOOP_COLLECTION);
		}

		if (lb.getWhileCondition() != null) {
			toCondition(lb.getWhileCondition(), dest, mc);
			dest.setLoopType(LoopConfig.LOOP_CONDITIONS);
		}

		if (lb.getConnectorCondition() != null) {
			toConnector(lb.getConnectorCondition().getConnector(), dest.getLoopConnector(), mc);
			if (lb.getConnectorCondition().getConnector() != null
					&& lb.getConnectorCondition().getConnector().getInheritFrom() != null) {
				dest.getLoopConnector().setInheritsFromRef(lb.getConnectorCondition().getConnector().getInheritFrom());
			}

			switch (lb.getConnectorCondition().getInitialize()) {
			case ON_STARTUP:
				switch (lb.getConnectorCondition().getSelectEntries()) {
				case ON_INITIALIZE:
					dest.setInitConnectorOption(LoopConfig.OPTION_NONE);
					break;
				case ON_EVERY_USE:
					dest.setInitConnectorOption(LoopConfig.OPTION_SELECT);
					break;
				}
				break;
			case ON_EVERY_USE:
				dest.setInitConnectorOption(LoopConfig.OPTION_INITIALIZE);
				break;
			}
			dest.setLoopType(LoopConfig.LOOP_CONNECTOR_FC);
		}
	}

	private static void toCondition(ConditionBinding cfg, BranchingConfig dest, MetamergeConfig mc) throws Exception {
		if (cfg == null) {
			return;
		}

		dest.setMatchAny(cfg.isMatchAny());
		dest.setScript(cfg.getScript());

		for (ConditionItemBinding b : cfg.getItems()) {
			BranchCondition i = mc.newInstanceOf(BranchCondition.class);
			i.init();
			i.setCaseSensitive(b.isCaseSensitive());
			i.setLeftHand(b.getLeftHand());
			i.setOperator(b.getOperator());
			i.setRightHand(b.getRightHand());
			i.setMatchAny(b.isMatchAny());
			i.setNegate(b.isNegate());
			dest.getConditions().addConfig(i);
		}
	}

	private static void toBranching(BranchBinding bb, BranchingConfig dest, MetamergeConfig mc) throws Exception {
		if (bb == null) {
			return;
		}

		toCondition(bb.getCondition(), dest, mc);

		switch (bb.getType()) {
		case IF:
			dest.setBranchType(BranchingConfig.BRANCH_IF);
			break;
		case ELSE:
			dest.setBranchType(BranchingConfig.BRANCH_ELSE);
			break;
		case ELSE_IF:
			dest.setBranchType(BranchingConfig.BRANCH_ELSEIF);
			break;
		case SWITCH:
			dest.setBranchType(BranchingConfig.BRANCH_SWITCH);
			break;
		case CASE:
			dest.setBranchType(BranchingConfig.BRANCH_CASE);
			break;
		}
	}

	private static SchemaConfig toSchema(SchemaBinding cfg, MetamergeConfig dest) throws Exception {
		SchemaConfig sc = toInheritence(cfg, SchemaConfig.class, dest);
		toSchema(cfg, sc, dest);
		return sc;
	}

	private static void toSchema(SchemaBinding cfg, SchemaConfig dest, MetamergeConfig mc) throws Exception {
		if (cfg == null) {
			return;
		}

		dest.setInheritsFromRef(cfg.getInheritFrom());
		for (SchemaItemBinding b : cfg.getItems()) {
			SchemaItemConfig i = toSchemaItem(b, mc);
			if (i != null) {
				dest.setItem(i.getName(), i);
			}
		}
	}

	private static SchemaItemConfig toSchemaItem(SchemaItemBinding cfg, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return null;
		}

		SchemaItemConfig i = dest.newInstanceOf(SchemaItemConfig.class);
		i.setName(cfg.getName());
		i.init();

		i.setMinOccurrences(cfg.getMinOccurs());
		i.setMaxOccurrences("unbounded".equals(cfg.getMaxOccurs()) ? Integer.MAX_VALUE : Integer.parseInt(cfg.getMaxOccurs()));
		i.setExternalSyntax(cfg.getNativeSyntax());
		i.setJavaClass(cfg.getSyntax());
		i.setSample(cfg.getSample());
		i.setProperty(cfg.getType() == SchemaItemTypeEnum.PROPERTY);
		i.setUserComment(cfg.getComment());

		ContainerConfig children = i.getChildSchemaList();
		for (SchemaItemBinding b : cfg.getItems()) {
			SchemaItemConfig child = toSchemaItem(b, dest);
			if (child != null) {
				children.addConfig(child);
			}
		}

		return i;
	}

	private static ALMappingConfig toALMapping(String name, AttributeMapBinding cfg, MetamergeConfig dest) throws Exception {
		ALMappingConfig mapping = dest.newInstanceOf(ALMappingConfig.class);
		mapping.setName(name);
		mapping.setInheritsFromRef(cfg.getInheritFrom());
		mapping.init();

		AttributeMapConfig map = mapping.getAttributeMap();
		map.setName("Input");
		map.setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		map.init();
		toAttributeMap(cfg, map, dest);
		mapping.setAttributeMap(map);

		return mapping;
	}

	private static void toAttributeMap(AttributeMapBinding cfg, AttributeMapConfig dest, MetamergeConfig mc) throws Exception {
		if (cfg == null) {
			return;
		}

		dest.setInheritsFromRef(cfg.getInheritFrom());
		toNull(cfg.getNull(), dest);

		for (AttributeMapItemBinding b : cfg.getItems()) {
			AttributeMapItem i = toInheritence(b, AttributeMapItem.class, mc);
			i.setEnabled(b.isEnabled());
			i.setAdd(b.isAdd());
			i.setModify(b.isModify());
			switch (b.getType()) {
			case SIMPLE:
				i.setType(AttributeMapItem.SIMPLE_MAPPING);
				i.setSimple(b.getMapsTo());
				break;
			case SUBSTITUTION:
				i.setType(AttributeMapItem.SUBSTITUTION_MAPPING);
				i.setSubstitution(b.getMapsTo());
				break;
			case ADVANCED:
				i.setType(AttributeMapItem.ADVANCED_MAPPING);
				i.setScript(b.getMapsTo());
				break;
			}
			dest.setAttributeMapItem(i);
		}
	}

	private static void toContext(SolutionContextBinding scb, MetamergeConfig dest) throws Exception {
		if (scb == null) {
			return;
		}

		MetamergeFolder config = (MetamergeFolder) dest.lookup(MetamergeConfig.DEFAULT_CONFIG_FOLDER);
		config.setName("Config");

		SolutionInterface si = toSolutionInterface(scb.getInterface(), dest);
		if (si != null) {
			dest.rebind("Config/" + si.getShortName(), si);
		}

		InstanceConfig ic = toSolutionInstance(scb.getInstance(), dest);
		if (ic != null) {
			dest.rebind("Config/" + ic.getShortName(), ic);
		}

		toSolutionLibrary(scb.getLibraries(), dest);

		LogConfig log = toLog(scb.getLog(), dest);
		if (log != null) {
			dest.rebind("Config/" + log.getShortName(), log);
		}

		TombstonesConfig tc = toParameters(scb.getTombstone(), TombstonesConfig.class, dest);
		if (tc != null) {
			dest.rebind("Config/" + tc.getShortName(), tc);
		}

		if (scb.getSystemStore() != null) {
			BaseConfiguration ss = toParameters(scb.getSystemStore(), dest);
			ContainerConfig sysStore = dest.newInstanceOf(ContainerConfig.class);
			sysStore.setName("SystemStore");
			sysStore.setParent(dest);
			sysStore.init();
			sysStore.addConfig(ss);
			dest.bind("Config/" + sysStore.getShortName(), sysStore);
		}
	}

	private static InstanceConfig toSolutionInstance(SolutionInstanceBinding cfg, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return null;
		}

		InstanceConfig ic = dest.newInstanceOf(InstanceConfig.class);
		ic.setName("AutoStart");
		ic.init();

		ContainerConfig c = ic.getStartupItems();
		for (String alName : cfg.getAutostartALs()) {
			BaseConfiguration bc = dest.newInstanceOf(BaseConfiguration.class);
			bc.setName(alName);
			bc.setParameter("Name", alName);
			c.addConfig(bc);
		}

		return ic;
	}

	private static void toSolutionLibrary(SolutionLibraryBinding cfg, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return;
		}

		BaseConfiguration libFolder = (BaseConfiguration) dest.lookup(MetamergeConfig.DEFAULT_LIBRARY_FOLDER);

		for (ParameterBinding p : cfg.getLibraries()) {
			if (p.getName() != null) {
				libFolder.setParameter(p.getName(), p.getValue());
			}
		}
	}

	private static SolutionInterface toSolutionInterface(SolutionInterfaceBinding cfg, MetamergeConfig dest) throws Exception {
		if (cfg == null) {
			return null;
		}

		SolutionInterface si = dest.getSolutionInterface();
		si.setName("SolutionInterface");
		si.init();

		if (cfg.getUserComment() != null) {
			si.setUserComment(cfg.getUserComment());
		}
		si.setEnabled(cfg.isEnabled());
		Integer interval = cfg.getPollInterval();
		if (interval != null) {
			si.setHealthPollInterval(interval);
		}

		if (cfg.getHealthAl() != null) {
			si.setHealthAssemblyLine(cfg.getHealthAl());
		}
		if (cfg.getSolutionName() != null) {
			si.setInstanceID(cfg.getSolutionName());
		}

		for (ExposedAlBinding al : cfg.getAls()) {
			si.addExposedAssemblyLine(al.getName());
		}

		for (ExposedPropertyBinding prop : cfg.getProperties()) {
			ExposedProperty p = si.addExposedProperty(prop.getName(), prop.getStoreName());
			p.setCategory(prop.getCategory());
			p.setLabel(prop.getLabel());
			p.setStoreName(prop.getStoreName());
			p.setUserComment(prop.getUserComment());
		}

		return si;
	}
}
