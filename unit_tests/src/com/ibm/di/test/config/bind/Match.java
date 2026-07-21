
package com.ibm.di.test.config.bind;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import com.ibm.di.config.bind.ALComponentBinding;
import com.ibm.di.config.bind.ALComponentInitializeEnum;
import com.ibm.di.config.bind.ALComponentStateEnum;
import com.ibm.di.config.bind.ALComponentsBinding;
import com.ibm.di.config.bind.ALInitParamsBinding;
import com.ibm.di.config.bind.ALOperationBinding;
import com.ibm.di.config.bind.ALOperationsBinding;
import com.ibm.di.config.bind.ALSandboxBinding;
import com.ibm.di.config.bind.ALSimulationBinding;
import com.ibm.di.config.bind.AssemblyLineBinding;
import com.ibm.di.config.bind.AttributeMapBinding;
import com.ibm.di.config.bind.AttributeMapItemBinding;
import com.ibm.di.config.bind.AttributeMapItemTypeEnum;
import com.ibm.di.config.bind.BranchBinding;
import com.ibm.di.config.bind.BranchTypeEnum;
import com.ibm.di.config.bind.CollectionLoopBinding;
import com.ibm.di.config.bind.ComplexALComponentBinding;
import com.ibm.di.config.bind.ComplexComponentBinding;
import com.ibm.di.config.bind.CompositeALComponentBinding;
import com.ibm.di.config.bind.CompositeComponentBinding;
import com.ibm.di.config.bind.ConditionBinding;
import com.ibm.di.config.bind.ConditionItemBinding;
import com.ibm.di.config.bind.ConnectorBinding;
import com.ibm.di.config.bind.ConnectorLoopBinding;
import com.ibm.di.config.bind.ConnectorLoopInitializeEnum;
import com.ibm.di.config.bind.ConnectorLoopSelectEntriesEnum;
import com.ibm.di.config.bind.ConnectorModeBinding;
import com.ibm.di.config.bind.ConnectorModeEnum;
import com.ibm.di.config.bind.ContainerBinding;
import com.ibm.di.config.bind.DeltaBinding;
import com.ibm.di.config.bind.DeltaChangeDetectionModeEnum;
import com.ibm.di.config.bind.DeltaCommitEnum;
import com.ibm.di.config.bind.DeltaRowLockingEnum;
import com.ibm.di.config.bind.ExposedAlBinding;
import com.ibm.di.config.bind.ExposedPropertyBinding;
import com.ibm.di.config.bind.FunctionBinding;
import com.ibm.di.config.bind.HookBinding;
import com.ibm.di.config.bind.HooksBinding;
import com.ibm.di.config.bind.InheritingBinding;
import com.ibm.di.config.bind.JavaClassBinding;
import com.ibm.di.config.bind.LinkCriteriaBinding;
import com.ibm.di.config.bind.LinkCriteriaItemBinding;
import com.ibm.di.config.bind.LogBinding;
import com.ibm.di.config.bind.LogItemBinding;
import com.ibm.di.config.bind.LoopBinding;
import com.ibm.di.config.bind.NamedBinding;
import com.ibm.di.config.bind.NullBinding;
import com.ibm.di.config.bind.ParameterBinding;
import com.ibm.di.config.bind.ParametersBinding;
import com.ibm.di.config.bind.ParserBinding;
import com.ibm.di.config.bind.PoolDefinitionBinding;
import com.ibm.di.config.bind.PoolInstanceBinding;
import com.ibm.di.config.bind.PoolInstanceExhaustedEnum;
import com.ibm.di.config.bind.PropertyStoreBinding;
import com.ibm.di.config.bind.PropertyStoresBinding;
import com.ibm.di.config.bind.ProxyALBinding;
import com.ibm.di.config.bind.ProxyALModeEnum;
import com.ibm.di.config.bind.SchemaBinding;
import com.ibm.di.config.bind.SchemaItemBinding;
import com.ibm.di.config.bind.SchemaItemTypeEnum;
import com.ibm.di.config.bind.ScriptBinding;
import com.ibm.di.config.bind.SimpleALComponentBinding;
import com.ibm.di.config.bind.SimpleComponentBinding;
import com.ibm.di.config.bind.SolutionContextBinding;
import com.ibm.di.config.bind.SolutionInstanceBinding;
import com.ibm.di.config.bind.SolutionInterfaceBinding;
import com.ibm.di.config.bind.SolutionLibraryBinding;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class Match {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static Matcher<AttributeMapItemBinding> mItem(String name) {
		return new MapItemMatcher(name);
	}

	public static Matcher<AttributeMapItemBinding> mItem(String name, String inheritFrom) {
		return new MapItemMatcher(name, inheritFrom);
	}

	public static Matcher<AttributeMapItemBinding> mItem(String name, String inheritFrom, AttributeMapItemTypeEnum type,
			String mapsTo) {
		return new MapItemMatcher(name, inheritFrom, type, mapsTo);
	}

	public static Matcher<AttributeMapItemBinding> mItem(String name, String inheritFrom, AttributeMapItemTypeEnum type,
			String mapsTo, boolean enabled) {
		return new MapItemMatcher(name, inheritFrom, type, mapsTo, enabled);
	}

	public static Matcher<AttributeMapItemBinding> mItem(String name, String inheritFrom, AttributeMapItemTypeEnum type,
			String mapsTo, boolean enabled, boolean add, boolean modify) {
		return new MapItemMatcher(name, inheritFrom, type, mapsTo, enabled, add, modify);
	}

	public static Matcher<AttributeMapItemBinding> mItem(String name, String inheritFrom, AttributeMapItemTypeEnum type,
			String mapsTo, boolean enabled, boolean add, boolean modify, Matcher<NullBinding> n) {
		return new MapItemMatcher(name, inheritFrom, type, mapsTo, enabled, add, modify, n);
	}

	public static Matcher<AttributeMapBinding> map(String name) {
		return new MapMatcher(name);
	}

	public static Matcher<AttributeMapBinding> map(String name, String inheritFrom) {
		return new MapMatcher(name, inheritFrom);
	}

	public static Matcher<AttributeMapBinding> map(String name, String inheritFrom, Matcher<AttributeMapItemBinding>... items) {
		return new MapMatcher(name, inheritFrom, items);
	}

	public static Matcher<AttributeMapBinding> map(String name, String inheritFrom, Matcher<AttributeMapItemBinding>[] items,
			Matcher<NullBinding> _null) {
		return new MapMatcher(name, inheritFrom, items, _null);
	}

	public static Matcher<SchemaItemBinding> sItem(String name) {
		return new SchemaItemMatcher(name);
	}

	public static Matcher<SchemaItemBinding> sItem(String name, int min, String max) {
		return new SchemaItemMatcher(name, min, max);
	}

	public static Matcher<SchemaItemBinding> sItem(String name, int min, String max, Matcher<SchemaItemBinding>... items) {
		return new SchemaItemMatcher(name, min, max, items);
	}

	public static Matcher<SchemaItemBinding> sItem(String name, int min, String max, Matcher<SchemaItemBinding>[] items,
			SchemaItemTypeEnum type) {
		return new SchemaItemMatcher(name, min, max, items, type);
	}

	public static Matcher<SchemaItemBinding> sItem(String name, int min, String max, Matcher<SchemaItemBinding>[] items,
			SchemaItemTypeEnum type, String sample, String syntax, String nativeSyntax) {
		return new SchemaItemMatcher(name, min, max, items, type, sample, syntax, nativeSyntax);
	}

	public static Matcher<SchemaBinding> schema(String name) {
		return new SchemaMatcher(name);
	}

	public static Matcher<SchemaBinding> schema(String name, String inheritFrom) {
		return new SchemaMatcher(name, inheritFrom);
	}

	public static Matcher<SchemaBinding> schema(String name, String inheritFrom, Matcher<SchemaItemBinding>... items) {
		return new SchemaMatcher(name, inheritFrom, items);
	}

	public static Matcher<NamedBinding> name(String name) {
		return new NameMatcher<NamedBinding>(name);
	}

	public static Matcher<ParameterBinding> param(String name, String value) {
		return new ParameterMatcher(name, value);
	}

	public static Matcher<ParametersBinding> params(String name) {
		return new ParametersMatcher<ParametersBinding>(name);
	}

	public static Matcher<ParametersBinding> params(String name, String inheritFrom) {
		return new ParametersMatcher<ParametersBinding>(name, inheritFrom);
	}

	public static Matcher<ParametersBinding> params(String name, String inheritFrom, Matcher<ParameterBinding>... items) {
		return new ParametersMatcher<ParametersBinding>(name, inheritFrom, items);
	}

	public static Matcher<JavaClassBinding> jclass(String name, String inheritFrom, Class<?> cls,
			Matcher<ParameterBinding>... items) {
		return new JavaClassMatcher(name, inheritFrom, cls, items);
	}

	public static Matcher<ContainerBinding> container(String name) {
		return new ContainerMatcher(name);
	}

	public static Matcher<ContainerBinding> container(String name, Matcher<? extends NamedBinding>... items) {
		return new ContainerMatcher(name, items);
	}

	public static Matcher<ALComponentsBinding> alContainer(String name, Matcher<? extends ALComponentBinding>... components) {
		return new ALComponentsMatcher(name, components);
	}

	public static Matcher<HooksBinding> hooks(String name) {
		return new HooksMatcher(name);
	}

	public static Matcher<HooksBinding> hooks(String name, String inheritFrom) {
		return new HooksMatcher(name, inheritFrom);
	}

	public static Matcher<HooksBinding> hooks(String name, String inheritFrom, Matcher<HookBinding>... items) {
		return new HooksMatcher(name, inheritFrom, items);
	}

	public static Matcher<HookBinding> hook(String name) {
		return new HookMatcher(name);
	}

	public static Matcher<HookBinding> hook(String name, String inheritFrom, boolean enabled, String script) {
		return new HookMatcher(name, inheritFrom, enabled, script);
	}

	public static Matcher<ParserBinding> parser(String name) {
		return new ParserMatcher(name);
	}

	public static Matcher<ParserBinding> parser(String name, String inheritFrom) {
		return new ParserMatcher(name, inheritFrom);
	}

	public static Matcher<ParserBinding> parser(String name, String inheritFrom, Matcher<JavaClassBinding> jclass) {
		return new ParserMatcher(name, inheritFrom, jclass);
	}

	public static Matcher<ParserBinding> parser(String name, String inheritFrom, Matcher<JavaClassBinding> jclass,
			Matcher<SchemaBinding>[] schemas) {
		return new ParserMatcher(name, inheritFrom, jclass, schemas);
	}

	public static Matcher<ParserBinding> parser(String name, String inheritFrom, Matcher<JavaClassBinding> jclass,
			Matcher<SchemaBinding>[] schemas, String userComment) {
		return new ParserMatcher(name, inheritFrom, jclass, schemas, userComment);
	}

	public static Matcher<DeltaBinding> delta(Boolean enabled, String uniqueAttribute, String deltaDb, Boolean readDeleted,
			Boolean removeDeleted, Boolean returnUnchanged, Boolean fasterAlgorithm, Boolean allowDuplicateKeys,
			DeltaCommitEnum commit, DeltaRowLockingEnum rowLocking, DeltaChangeDetectionModeEnum changeDetectionMode,
			String changeDetectionAttributes) {
		return new DeltaMatcher(enabled, uniqueAttribute, deltaDb, readDeleted, removeDeleted, returnUnchanged, fasterAlgorithm,
				allowDuplicateKeys, commit, rowLocking, changeDetectionMode, changeDetectionAttributes);
	}

	public static Matcher<PoolDefinitionBinding> poolDef(Boolean enabled, Integer minSize, Integer maxSize, Integer purgeInterval,
			Integer initializeAttempts, Integer initializeSleepInterval) {
		return new PoolDefinitionMatcher(enabled, minSize, maxSize, purgeInterval, initializeAttempts, initializeSleepInterval);
	}

	public static Matcher<PoolInstanceBinding> poolInst(Boolean enabled, PoolInstanceExhaustedEnum onExhausted) {
		return new PoolInstanceMatcher(enabled, onExhausted);
	}

	public static Matcher<ConnectorBinding> connector(String name) {
		return new ConnectorMatcher(name);
	}

	public static Matcher<ConnectorBinding> connector(String name, String inheritFrom) {
		return new ConnectorMatcher(name, inheritFrom);
	}

	public static Matcher<ConnectorBinding> connector(String name, String inheritFrom, Matcher<JavaClassBinding> jclass) {
		return new ConnectorMatcher(name, inheritFrom, jclass);
	}

	public static Matcher<ConnectorBinding> connector(String name, String inheritFrom, Matcher<JavaClassBinding> jclass,
			Matcher<ParserBinding> parser) {
		return new ConnectorMatcher(name, inheritFrom, jclass, parser);
	}

	public static Matcher<ConnectorBinding> connector(String name, String inheritFrom, Matcher<JavaClassBinding> jclass,
			Matcher<ParserBinding> parser, Matcher<SchemaBinding>[] schemas) {
		return new ConnectorMatcher(name, inheritFrom, jclass, parser, schemas);
	}

	public static Matcher<ConnectorBinding> connector(String name, String inheritFrom, Matcher<JavaClassBinding> jclass,
			Matcher<ParserBinding> parser, Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps) {
		return new ConnectorMatcher(name, inheritFrom, jclass, parser, schemas, maps);
	}

	public static Matcher<ConnectorBinding> connector(String name, String inheritFrom, Matcher<JavaClassBinding> jclass,
			Matcher<ParserBinding> parser, Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps,
			Matcher<HooksBinding> hooks) {
		return new ConnectorMatcher(name, inheritFrom, jclass, parser, schemas, maps, hooks);
	}

	public static Matcher<ConnectorBinding> connector(String name, String inheritFrom, Matcher<JavaClassBinding> jclass,
			Matcher<ParserBinding> parser, Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps,
			Matcher<HooksBinding> hooks, ConnectorModeEnum mode, Matcher<ConnectorModeBinding> modeConfig) {
		return new ConnectorMatcher(name, inheritFrom, jclass, parser, schemas, maps, hooks, mode, modeConfig);
	}

	public static Matcher<ConnectorBinding> connector(String name, String inheritFrom, Matcher<JavaClassBinding> jclass,
			Matcher<ParserBinding> parser, Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps,
			Matcher<HooksBinding> hooks, ConnectorModeEnum mode, Matcher<ConnectorModeBinding> modeConfig,
			Matcher<LinkCriteriaBinding> link) {
		return new ConnectorMatcher(name, inheritFrom, jclass, parser, schemas, maps, hooks, mode, modeConfig, link);
	}

	public static Matcher<ConnectorBinding> connector(String name, String inheritFrom, Matcher<JavaClassBinding> jclass,
			Matcher<ParserBinding> parser, Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps,
			Matcher<HooksBinding> hooks, ConnectorModeEnum mode, Matcher<ConnectorModeBinding> modeConfig,
			Matcher<LinkCriteriaBinding> link, Matcher<DeltaBinding> delta) {
		return new ConnectorMatcher(name, inheritFrom, jclass, parser, schemas, maps, hooks, mode, modeConfig, link, delta);
	}

	public static Matcher<ConnectorBinding> connector(String name, String inheritFrom, Matcher<JavaClassBinding> jclass,
			Matcher<ParserBinding> parser, Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps,
			Matcher<HooksBinding> hooks, ConnectorModeEnum mode, Matcher<ConnectorModeBinding> modeConfig,
			Matcher<LinkCriteriaBinding> link, Matcher<DeltaBinding> delta, Matcher<PoolDefinitionBinding> poolDef,
			Matcher<PoolInstanceBinding> poolInst) {
		return new ConnectorMatcher(name, inheritFrom, jclass, parser, schemas, maps, hooks, mode, modeConfig, link, delta,
				poolDef, poolInst);
	}

	public static Matcher<LinkCriteriaBinding> link(String name) {
		return new LinkCriteriaMatcher(name);
	}

	public static Matcher<LinkCriteriaBinding> link(String name, String inheritFrom) {
		return new LinkCriteriaMatcher(name, inheritFrom);
	}

	public static Matcher<LinkCriteriaBinding> link(String name, String inheritFrom, Matcher<LinkCriteriaItemBinding>[] items) {
		return new LinkCriteriaMatcher(name, inheritFrom, items);
	}

	public static Matcher<LinkCriteriaBinding> link(String name, String inheritFrom, Matcher<LinkCriteriaItemBinding>[] items,
			boolean matchAny) {
		return new LinkCriteriaMatcher(name, inheritFrom, items, matchAny);
	}

	public static Matcher<LinkCriteriaBinding> link(String name, String inheritFrom, boolean advanced, String script) {
		return new LinkCriteriaMatcher(name, inheritFrom, advanced, script);
	}

	public static Matcher<LinkCriteriaItemBinding> lItem(String key, String attribute, String operator, String value) {
		return new LinkCriteriaItemMatcher(key, attribute, operator, value);
	}

	public static Matcher<ConnectorModeBinding> mode(boolean skipLookup) {
		return new ConnectorModeMatcher(skipLookup);
	}

	public static Matcher<ConnectorModeBinding> mode(boolean skipDeltaEntryDelete, boolean processDeltaEntryOnly) {
		return new ConnectorModeMatcher(skipDeltaEntryDelete, processDeltaEntryOnly);
	}

	public static Matcher<ScriptBinding> script(String name, String inheritFrom, String script, boolean autoInclude) {
		return new ScriptMatcher(name, inheritFrom, script, autoInclude);
	}

	public static Matcher<ScriptBinding> script(String name, String inheritFrom, Matcher<String>[] files, boolean autoInclude) {
		return new ScriptMatcher(name, inheritFrom, files, autoInclude);
	}

	public static Matcher<ScriptBinding> script(String name, String inheritFrom, String script, Matcher<String>[] files,
			boolean autoInclude) {
		return new ScriptMatcher(name, inheritFrom, script, files, autoInclude);
	}

	public static Matcher<FunctionBinding> function(String name) {
		return new ComplexMatcher<FunctionBinding>(name);
	}

	public static Matcher<FunctionBinding> function(String name, String inheritFrom) {
		return new ComplexMatcher<FunctionBinding>(name, inheritFrom);
	}

	public static Matcher<FunctionBinding> function(String name, String inheritFrom, Matcher<JavaClassBinding> jclass) {
		return new ComplexMatcher<FunctionBinding>(name, inheritFrom, jclass);
	}

	public static Matcher<FunctionBinding> function(String name, String inheritFrom, Matcher<JavaClassBinding> jclass,
			Matcher<ParserBinding> parser) {
		return new ComplexMatcher<FunctionBinding>(name, inheritFrom, jclass, parser);
	}

	public static Matcher<FunctionBinding> function(String name, String inheritFrom, Matcher<JavaClassBinding> jclass,
			Matcher<ParserBinding> parser, Matcher<SchemaBinding>[] schemas) {
		return new ComplexMatcher<FunctionBinding>(name, inheritFrom, jclass, parser, schemas);
	}

	public static Matcher<FunctionBinding> function(String name, String inheritFrom, Matcher<JavaClassBinding> jclass,
			Matcher<ParserBinding> parser, Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps) {
		return new ComplexMatcher<FunctionBinding>(name, inheritFrom, jclass, parser, schemas, maps);
	}

	public static Matcher<FunctionBinding> function(String name, String inheritFrom, Matcher<JavaClassBinding> jclass,
			Matcher<ParserBinding> parser, Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps,
			Matcher<HooksBinding> hooks) {
		return new ComplexMatcher<FunctionBinding>(name, inheritFrom, jclass, parser, schemas, maps, hooks);
	}

	public static Matcher<NullBinding> nullB(String behavior) {
		return new NullMatcher(behavior);
	}

	public static Matcher<NullBinding> nullB(String behavior, String behaviorValue) {
		return new NullMatcher(behavior, behaviorValue);
	}

	public static Matcher<NullBinding> nullB(String behavior, String behaviorValue, String definition) {
		return new NullMatcher(behavior, behaviorValue, definition);
	}

	public static Matcher<NullBinding> nullB(String behavior, String behaviorValue, String definition, String definitionValue) {
		return new NullMatcher(behavior, behaviorValue, definition, definitionValue);
	}

	public static Matcher<ALSandboxBinding> sandbox(String id) {
		return new ALSandboxMatcher(id);
	}

	public static Matcher<ProxyALBinding> proxyAl(String server, String ci, String al, ProxyALModeEnum mode, boolean debug) {
		return new ProxyALMatcher(server, ci, al, mode, debug);
	}

	public static Matcher<ALSimulationBinding> simulation(Matcher<ProxyALBinding> proxy) {
		return new ALSimulationMatcher(proxy);
	}

	public static Matcher<LogItemBinding> log(String name, String inheritFrom, Matcher<ParameterBinding>... params) {
		return new LogItemMatcher(name, inheritFrom, params);
	}

	public static Matcher<LogBinding> logs(Matcher<LogItemBinding>... items) {
		return new LogMatcher(items);
	}

	public static Matcher<ComplexALComponentBinding> complex(String name, Matcher<? extends ComplexComponentBinding> config,
			String state, ALComponentInitializeEnum init, String simState, boolean sandboxRecord, boolean sandboxPlayback) {
		return new ComplexALComponentMatcher(name, config, state, init, simState, sandboxRecord, sandboxPlayback);
	}

	public static Matcher<ConditionItemBinding> cItem(String left, String op, String right) {
		return new ConditionItemMatcher(left, op, right);
	}

	public static Matcher<ConditionItemBinding> cItem(String left, String op, String right, boolean negate, boolean matchAny) {
		return new ConditionItemMatcher(left, op, right, negate, matchAny);
	}

	public static Matcher<ConditionBinding> condition(boolean matchAny, Matcher<ConditionItemBinding>... items) {
		return new ConditionMatcher(matchAny, items);
	}

	public static Matcher<ConditionBinding> condition(String script) {
		return new ConditionMatcher(script);
	}

	public static Matcher<BranchBinding> branch(BranchTypeEnum type, Matcher<ConditionBinding> condition) {
		return new BranchMatcher(type, condition);
	}

	public static Matcher<CompositeALComponentBinding> composite(String name, Matcher<? extends CompositeComponentBinding> config,
			ALComponentStateEnum state, Matcher<? extends ALComponentBinding>... children) {
		return new CompositeALComponentMatcher(name, config, state, children);
	}

	public static Matcher<SimpleALComponentBinding> simple(String name, Matcher<? extends SimpleComponentBinding> config,
			ALComponentStateEnum state) {
		return new SimpleALComponentMatcher(name, config, state);
	}

	public static Matcher<SimpleALComponentBinding> simple(String name, Matcher<? extends SimpleComponentBinding> config,
			ALComponentStateEnum state, String simState) {
		return new SimpleALComponentMatcher(name, config, state, simState);
	}

	public static Matcher<LoopBinding> loop(CollectionLoopMatcher collection) {
		return new LoopMatcher(collection);
	}

	public static Matcher<LoopBinding> loop(ConnectorLoopMatcher connector) {
		return new LoopMatcher(connector);
	}

	public static Matcher<LoopBinding> loop(Matcher<ConditionBinding> condition) {
		return new LoopMatcher(condition);
	}

	public static CollectionLoopMatcher collection(String collectionAttr, String assignAttr) {
		return new CollectionLoopMatcher(collectionAttr, assignAttr);
	}

	public static ConnectorLoopMatcher loopConn(Matcher<ConnectorBinding> connector, ConnectorLoopInitializeEnum init,
			ConnectorLoopSelectEntriesEnum select) {
		return new ConnectorLoopMatcher(connector, init, select);
	}

	public static Matcher<ALOperationBinding> operation(String name, Matcher<SchemaBinding>[] schemas,
			Matcher<AttributeMapBinding>[] maps) {
		return new ALOperationMatcher(name, schemas, maps);
	}

	public static Matcher<ALOperationsBinding> operations(Matcher<ALOperationBinding>... ops) {
		return new ALOperationsMatcher(ops);
	}

	public static Matcher<ALInitParamsBinding> alInitParams(Matcher<SchemaBinding> schema) {
		return new ALInitParamsMatcher(schema);
	}

	public static Matcher<AssemblyLineBinding> al(String name, Matcher<ParametersBinding> params, Matcher<HooksBinding> hooks,
			Matcher<ALSandboxBinding> sandbox, Matcher<ALSimulationBinding> sim, Matcher<LogBinding> logs,
			Matcher<ALComponentsBinding>[] containers, Matcher<NullBinding> _null, Matcher<ParametersBinding> threading,
			Matcher<ALOperationsBinding> ops, Matcher<ALInitParamsBinding> initParams) {
		return new AssemblyLineMatcher(name, params, hooks, sandbox, sim, logs, containers, _null, threading, ops, initParams);
	}

	public static Matcher<SolutionContextBinding> context(Matcher<SolutionInterfaceBinding> iface, Matcher<LogBinding> log,
			Matcher<SolutionLibraryBinding> lib, Matcher<ParametersBinding> tombstone, Matcher<ParametersBinding> sysStore,
			Matcher<SolutionInstanceBinding> inst) {
		return new SolutionContextMatcher(iface, log, lib, tombstone, sysStore, inst);
	}

	public static Matcher<SolutionInterfaceBinding> iface(boolean enabled, String solName, String healthAl, Integer pollInterval,
			Matcher<ExposedAlBinding>[] als, Matcher<ExposedPropertyBinding>[] props) {
		return new SolutionInterfaceMatcher(enabled, solName, healthAl, pollInterval, als, props);
	}

	public static Matcher<SolutionLibraryBinding> library(Matcher<ParameterBinding>[] libs) {
		return new SolutionLibraryMatcher(libs);
	}

	public static Matcher<SolutionInstanceBinding> inst(Matcher<String>[] autoStartAls) {
		return new SolutionInstanceMatcher(autoStartAls);
	}

	public static NameMatcher<ExposedAlBinding> expAl(String name) {
		return new ExposedAlMatcher(name);
	}

	public static NameMatcher<ExposedPropertyBinding> expProp(String name, String label, String category, String storeName,
			String userComment) {
		return new ExposedPropertyMatcher(name, label, category, storeName, userComment);
	}

	public static Matcher<PropertyStoreBinding> propStore(String name, String nameFilter, String keyName, String valueName,
			boolean readOnly, boolean initialLoad, int cacheTimeout, Matcher<JavaClassBinding> conn,
			Matcher<JavaClassBinding> parser) {
		return new PropertyStoreMatcher(name, nameFilter, keyName, valueName, readOnly, initialLoad, cacheTimeout, conn, parser);
	}

	public static <T> Matcher<T>[] list(Matcher<T>... items) {
		return items;
	}

	public static Matcher<PropertyStoresBinding> propStores(String name, String defaultStore, String passStore,
			Matcher<PropertyStoreBinding>[] stores) {
		return new PropertyStoresMatcher(name, defaultStore, passStore, stores);
	}

	private static class PropertyStoresMatcher extends NameMatcher<PropertyStoresBinding> {

		private String desc = this.getClass().getSimpleName() + ": ";
		private boolean error;
		private String defaultStore;
		private String passStore;
		private Matcher<PropertyStoreBinding>[] stores;

		public PropertyStoresMatcher(String name, String defaultStore, String passStore, Matcher<PropertyStoreBinding>[] stores) {
			super(name);
			this.defaultStore = defaultStore;
			this.passStore = passStore;
			this.stores = stores;
		}

		public boolean matches(Object item) {
			PropertyStoresBinding b = (PropertyStoresBinding) item;

			try {
				assertEquals(b.getDefault(), defaultStore);
				assertEquals(b.getPassword(), passStore);
				assertContainsInAnyOrder(b.getConfigs(), (Matcher[]) stores);
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class PropertyStoreMatcher extends NameMatcher<PropertyStoreBinding> {
		private final String nameFilter;
		private final String keyName;
		private final String valueName;
		private final boolean readOnly;
		private final boolean initialLoad;
		private final Matcher<JavaClassBinding> conn;
		private final Matcher<JavaClassBinding> parser;

		private String desc = this.getClass().getSimpleName() + ": ";
		private boolean error;
		private final int cacheTimeout;

		public PropertyStoreMatcher(String name, String nameFilter, String keyName, String valueName, boolean readOnly,
				boolean initialLoad, int cacheTimeout, Matcher<JavaClassBinding> conn, Matcher<JavaClassBinding> parser) {
			super(name);
			this.nameFilter = nameFilter;
			this.keyName = keyName;
			this.valueName = valueName;
			this.readOnly = readOnly;
			this.initialLoad = initialLoad;
			this.cacheTimeout = cacheTimeout;
			this.conn = conn;
			this.parser = parser;
		}

		public boolean matches(Object item) {
			PropertyStoreBinding b = (PropertyStoreBinding) item;

			try {
				assertEquals(b.getNameFilters(), nameFilter);
				assertEquals(b.getKeyName(), keyName);
				assertEquals(b.getValueName(), valueName);
				assertEquals(b.isReadOnly(), readOnly);
				assertEquals(b.isInitialLoad(), initialLoad);
				assertEquals(b.getCacheTimeout(), cacheTimeout);
				assertThatSafe(b.getConnector(), conn);
				assertThatSafe(b.getParser(), parser);
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class ExposedPropertyMatcher extends NameMatcher<ExposedPropertyBinding> {

		private final String label;
		private final String category;
		private final String storeName;
		private final String userComment;

		private String desc = this.getClass().getSimpleName() + ": ";
		private boolean error;

		public ExposedPropertyMatcher(String name, String label, String category, String storeName, String userComment) {
			super(name);
			this.label = label;
			this.category = category;
			this.storeName = storeName;
			this.userComment = userComment;
		}

		public boolean matches(Object item) {
			ExposedPropertyBinding b = (ExposedPropertyBinding) item;

			try {
				assertEquals(b.getLabel(), label);
				assertEquals(b.getCategory(), category);
				assertEquals(b.getStoreName(), storeName);
				assertEquals(b.getUserComment(), userComment);
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class ExposedAlMatcher extends NameMatcher<ExposedAlBinding> {
		public ExposedAlMatcher(String name) {
			super(name);
		}
	}

	private static class SolutionInstanceMatcher extends BaseMatcher<SolutionInstanceBinding> {

		private String desc = this.getClass().getSimpleName() + ": ";

		private final Matcher<String>[] autoStartAls;

		public SolutionInstanceMatcher(Matcher<String>[] autoStartAls) {
			this.autoStartAls = autoStartAls;
		}

		public boolean matches(Object item) {
			SolutionInstanceBinding b = (SolutionInstanceBinding) item;

			try {
				assertContainsInAnyOrder(b.getAutostartALs(), autoStartAls);
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class SolutionLibraryMatcher extends BaseMatcher<SolutionLibraryBinding> {
		private final Matcher<ParameterBinding>[] libs;

		private String desc = this.getClass().getSimpleName() + ": ";

		public SolutionLibraryMatcher(Matcher<ParameterBinding>[] libs) {
			this.libs = libs;
		}

		public boolean matches(Object item) {
			SolutionLibraryBinding b = (SolutionLibraryBinding) item;

			try {
				assertContainsInAnyOrder(b.getLibraries(), libs);
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class SolutionInterfaceMatcher extends BaseMatcher<SolutionInterfaceBinding> {

		private final boolean enabled;
		private final String solName;
		private final String healthAl;
		private final Integer pollInterval;
		private final Matcher<ExposedAlBinding>[] als;
		private final Matcher<ExposedPropertyBinding>[] props;

		private String desc = this.getClass().getSimpleName() + ": ";

		public SolutionInterfaceMatcher(boolean enabled, String solName, String healthAl, Integer pollInterval,
				Matcher<ExposedAlBinding>[] als, Matcher<ExposedPropertyBinding>[] props) {
			this.enabled = enabled;
			this.solName = solName;
			this.healthAl = healthAl;
			this.pollInterval = pollInterval;
			this.als = als;
			this.props = props;
		}

		public boolean matches(Object item) {
			SolutionInterfaceBinding b = (SolutionInterfaceBinding) item;

			try {
				assertEquals(b.isEnabled(), enabled);
				assertEquals(b.getSolutionName(), solName);
				assertEquals(b.getHealthAl(), healthAl);
				assertEquals(b.getPollInterval(), pollInterval);
				assertContainsInAnyOrder(b.getAls(), als);
				assertContainsInAnyOrder(b.getProperties(), props);
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class SolutionContextMatcher extends BaseMatcher<SolutionContextBinding> {

		private final Matcher<SolutionInterfaceBinding> iface;
		private final Matcher<LogBinding> log;
		private final Matcher<SolutionLibraryBinding> lib;
		private final Matcher<ParametersBinding> tombstone;
		private final Matcher<ParametersBinding> sysStore;
		private final Matcher<SolutionInstanceBinding> inst;

		private String desc = this.getClass().getSimpleName() + ": ";

		public SolutionContextMatcher(Matcher<SolutionInterfaceBinding> iface, Matcher<LogBinding> log,
				Matcher<SolutionLibraryBinding> lib, Matcher<ParametersBinding> tombstone, Matcher<ParametersBinding> sysStore,
				Matcher<SolutionInstanceBinding> inst) {
			this.iface = iface;
			this.log = log;
			this.lib = lib;
			this.tombstone = tombstone;
			this.sysStore = sysStore;
			this.inst = inst;
		}

		public boolean matches(Object item) {
			SolutionContextBinding b = (SolutionContextBinding) item;

			try {
				assertThatSafe(b.getInterface(), iface);
				assertThatSafe(b.getLog(), log);
				assertThatSafe(b.getLibraries(), lib);
				assertThatSafe(b.getTombstone(), tombstone);
				assertThatSafe(b.getSystemStore(), sysStore);
				assertThatSafe(b.getInstance(), inst);
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}

	}

	private static class ALInitParamsMatcher extends BaseMatcher<ALInitParamsBinding> {
		private final Matcher<SchemaBinding> schema;

		private String desc = this.getClass().getSimpleName() + ": ";

		public ALInitParamsMatcher(Matcher<SchemaBinding> schema) {
			this.schema = schema;
		}

		public boolean matches(Object item) {
			ALInitParamsBinding b = (ALInitParamsBinding) item;

			try {
				assertThatSafe(b.getSchema(), schema);
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class ALComponentsMatcher extends NameMatcher<ALComponentsBinding> {

		private final Matcher<? extends ALComponentBinding>[] components;
		private String desc = this.getClass().getSimpleName() + ": ";
		private boolean error;

		public ALComponentsMatcher(String name, Matcher<? extends ALComponentBinding>[] components) {
			super(name);
			this.components = components;
		}

		public boolean matches(Object item) {
			ALComponentsBinding b = (ALComponentsBinding) item;

			try {
				assertContainsInAnyOrder(b.getComponents(), (Matcher[]) components);
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class AssemblyLineMatcher extends NameMatcher<AssemblyLineBinding> {
		private final Matcher<ParametersBinding> params;
		private final Matcher<HooksBinding> hooks;
		private final Matcher<ALSandboxBinding> sandbox;
		private final Matcher<ALSimulationBinding> sim;
		private final Matcher<LogBinding> logs;
		private final Matcher<ALComponentsBinding>[] containers;
		private final Matcher<NullBinding> _null;
		private final Matcher<ParametersBinding> threading;
		private final Matcher<ALOperationsBinding> ops;
		private final Matcher<ALInitParamsBinding> initParams;

		private String desc = this.getClass().getSimpleName() + ": ";
		private boolean error;

		public AssemblyLineMatcher(String name, Matcher<ParametersBinding> params, Matcher<HooksBinding> hooks,
				Matcher<ALSandboxBinding> sandbox, Matcher<ALSimulationBinding> sim, Matcher<LogBinding> logs,
				Matcher<ALComponentsBinding>[] containers, Matcher<NullBinding> _null, Matcher<ParametersBinding> threading,
				Matcher<ALOperationsBinding> ops, Matcher<ALInitParamsBinding> initParams) {
			super(name);
			this.params = params;
			this.hooks = hooks;
			this.sandbox = sandbox;
			this.sim = sim;
			this.logs = logs;
			this.containers = containers;
			this._null = _null;
			this.threading = threading;
			this.ops = ops;
			this.initParams = initParams;
		}

		public boolean matches(Object item) {
			AssemblyLineBinding b = (AssemblyLineBinding) item;

			try {
				assertThatSafe(b.getSettings(), params);
				assertThatSafe(b.getHooks(), hooks);
				assertThatSafe(b.getSandbox(), sandbox);
				assertThatSafe(b.getSimulation(), sim);
				assertThatSafe(b.getLogging(), logs);
				assertContainsInAnyOrder(b.getContainers(), containers);
				assertThatSafe(b.getNull(), _null);
				assertThatSafe(b.getThreading(), threading);
				assertThatSafe(b.getOperations(), ops);
				assertThatSafe(b.getInitParams(), initParams);
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class ALOperationsMatcher extends BaseMatcher<ALOperationsBinding> {

		private final Matcher<ALOperationBinding>[] ops;

		private String desc = this.getClass().getSimpleName() + ": ";

		public ALOperationsMatcher(Matcher<ALOperationBinding>[] ops) {
			this.ops = ops;
		}

		public boolean matches(Object item) {
			ALOperationsBinding b = (ALOperationsBinding) item;

			try {
				assertContainsInAnyOrder(b.getOperations(), ops);
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class ALOperationMatcher extends NameMatcher<ALOperationBinding> {

		private final Matcher<SchemaBinding>[] schemas;
		private final Matcher<AttributeMapBinding>[] maps;

		private boolean error;
		private String desc = this.getClass().getSimpleName() + ": ";

		public ALOperationMatcher(String name, Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps) {
			super(name);
			this.schemas = schemas;
			this.maps = maps;
		}

		public boolean matches(Object item) {
			ALOperationBinding b = (ALOperationBinding) item;

			try {
				assertContainsInAnyOrder(b.getSchemas(), schemas);
				assertContainsInAnyOrder(b.getAttributeMaps(), maps);
			} catch (AssertionError e) {

				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class CollectionLoopMatcher extends BaseMatcher<CollectionLoopBinding> {

		private final String collectionAttr;
		private final String assignAttr;

		private String desc = this.getClass().getSimpleName() + ": ";

		public CollectionLoopMatcher(String collectionAttr, String assignAttr) {
			this.collectionAttr = collectionAttr;
			this.assignAttr = assignAttr;
		}

		public boolean matches(Object item) {
			CollectionLoopBinding b = (CollectionLoopBinding) item;

			try {
				assertEquals(collectionAttr, b.getCollectionAttribute());
				assertEquals(assignAttr, b.getAssignAttribute());
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class ConnectorLoopMatcher extends BaseMatcher<ConnectorLoopBinding> {
		private final Matcher<ConnectorBinding> connector;
		private final ConnectorLoopInitializeEnum init;
		private final ConnectorLoopSelectEntriesEnum select;

		private String desc = this.getClass().getSimpleName() + ": ";

		public ConnectorLoopMatcher(Matcher<ConnectorBinding> connector, ConnectorLoopInitializeEnum init,
				ConnectorLoopSelectEntriesEnum select) {
			this.connector = connector;
			this.init = init;
			this.select = select;
		}

		public boolean matches(Object item) {
			ConnectorLoopBinding b = (ConnectorLoopBinding) item;

			try {
				assertThatSafe(b.getConnector(), connector);
				assertEquals(init, b.getInitialize());
				assertEquals(select, b.getSelectEntries());
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}

	}

	private static class LoopMatcher extends BaseMatcher<LoopBinding> {

		private final Matcher<ConditionBinding> condition;
		private final Matcher<CollectionLoopBinding> collection;
		private final Matcher<ConnectorLoopBinding> connector;

		private String desc = this.getClass().getSimpleName() + ": ";

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;
		private static final int CONSTRUCTOR_2 = 2;

		public LoopMatcher(CollectionLoopMatcher collection) {
			this.collection = collection;
			this.condition = null;
			this.connector = null;
			constFlag = CONSTRUCTOR_0;
		}

		public LoopMatcher(Matcher<ConditionBinding> condition) {
			this.condition = condition;
			this.connector = null;
			this.collection = null;
			constFlag = CONSTRUCTOR_1;
		}

		public LoopMatcher(ConnectorLoopMatcher connector) {
			this.connector = connector;
			this.condition = null;
			this.collection = null;
			constFlag = CONSTRUCTOR_2;
		}

		public boolean matches(Object item) {
			LoopBinding b = null;
			if (item instanceof LoopBinding) {
				b = (LoopBinding) item;
			} else {
				return false;
			}

			try {
				switch (constFlag) {
				case CONSTRUCTOR_2:
					assertThat(b.getConnectorCondition(), connector);
					assertThat(b.getCollectionCondition(), is(nullValue()));
					assertThat(b.getWhileCondition(), is(nullValue()));
					break;
				case CONSTRUCTOR_1:
					assertThat(b.getConnectorCondition(), is(nullValue()));
					assertThat(b.getCollectionCondition(), is(nullValue()));
					assertThat(b.getWhileCondition(), condition);
					break;
				case CONSTRUCTOR_0:
					assertThat(b.getConnectorCondition(), is(nullValue()));
					assertThat(b.getCollectionCondition(), collection);
					assertThat(b.getWhileCondition(), is(nullValue()));
					break;
				}
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class SimpleALComponentMatcher extends NameMatcher<SimpleALComponentBinding> {

		private Matcher<? extends SimpleComponentBinding> config;
		private final ALComponentStateEnum state;
		private final String simState;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;

		private boolean error;
		private String desc = this.getClass().getSimpleName() + ": ";

		public SimpleALComponentMatcher(String name, Matcher<? extends SimpleComponentBinding> config, ALComponentStateEnum state) {
			this(name, config, state, null);
			constFlag = CONSTRUCTOR_0;
		}

		public SimpleALComponentMatcher(String name, Matcher<? extends SimpleComponentBinding> config, ALComponentStateEnum state,
				String simState) {
			super(name);
			this.config = config;
			this.state = state;
			this.simState = simState;
			constFlag = CONSTRUCTOR_1;
		}

		public boolean matches(Object item) {
			SimpleALComponentBinding b;
			if (item instanceof SimpleALComponentBinding) {
				b = (SimpleALComponentBinding) item;
			} else {
				return false;
			}

			try {
				switch (constFlag) {
				case CONSTRUCTOR_1:
					assertEquals(ALComponentStateEnum.fromValue(simState), b.getSimulateState());
					// fall through
				case CONSTRUCTOR_0:
					assertThatSafe(b.getSimpleConfig(), (Matcher) config);
					assertEquals(state, b.getState());
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class CompositeALComponentMatcher extends NameMatcher<CompositeALComponentBinding> {

		private final Matcher<? extends CompositeComponentBinding> config;
		private final Matcher<? extends ALComponentBinding>[] children;
		private final ALComponentStateEnum state;

		private boolean error;
		private String desc = this.getClass().getSimpleName() + ": ";

		public CompositeALComponentMatcher(String name, Matcher<? extends CompositeComponentBinding> config,
				ALComponentStateEnum state, Matcher<? extends ALComponentBinding>[] children) {
			super(name);
			this.config = config;
			this.children = children;
			this.state = state;
		}

		public boolean matches(Object item) {
			CompositeALComponentBinding b;
			if (item instanceof CompositeALComponentBinding) {
				b = (CompositeALComponentBinding) item;
			} else {
				return false;
			}

			try {
				assertThatSafe(b.getCompositeConfig(), (Matcher) config);
				assertContainsInAnyOrder(b.getComponents(), (Matcher[]) children);
				assertEquals(state, b.getState());
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class BranchMatcher extends BaseMatcher<BranchBinding> {

		private final BranchTypeEnum type;
		private final Matcher<ConditionBinding> condition;

		private String desc = this.getClass().getSimpleName() + ": ";

		public BranchMatcher(BranchTypeEnum type, Matcher<ConditionBinding> condition) {
			this.type = type;
			this.condition = condition;
		}

		public boolean matches(Object item) {
			BranchBinding b = null;
			if (item instanceof BranchBinding) {
				b = (BranchBinding) item;
			} else {
				return false;
			}

			try {
				assertEquals(type, b.getType());
				assertThatSafe(b.getCondition(), condition);
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class ConditionMatcher extends BaseMatcher<ConditionBinding> {
		private final boolean matchAny;
		private final Matcher<ConditionItemBinding>[] items;
		private final String script;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;

		private String desc = this.getClass().getSimpleName() + ": ";

		public ConditionMatcher(boolean matchAny, Matcher<ConditionItemBinding>... items) {
			this.matchAny = matchAny;
			this.items = items;
			this.script = null;
			constFlag = CONSTRUCTOR_0;
		}

		public ConditionMatcher(String script) {
			this.script = script;
			this.matchAny = false;
			this.items = null;
			constFlag = CONSTRUCTOR_1;
		}

		public boolean matches(Object item) {
			ConditionBinding b = (ConditionBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_1:
					assertEquals(script, b.getScript());
					break;
				case CONSTRUCTOR_0:
					assertEquals(matchAny, b.isMatchAny());
					assertContainsInAnyOrder(b.getItems(), items);
				}
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class ConditionItemMatcher extends BaseMatcher<ConditionItemBinding> {

		private final String left;
		private final String op;
		private final String right;
		private final boolean negate;
		private final boolean matchAny;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;

		private String desc = this.getClass().getSimpleName() + ": ";

		public ConditionItemMatcher(String left, String op, String right) {
			this(left, op, right, false, false);
			constFlag = CONSTRUCTOR_0;
		}

		public ConditionItemMatcher(String left, String op, String right, boolean negate, boolean matchAny) {
			this.left = left;
			this.op = op;
			this.right = right;
			this.negate = negate;
			this.matchAny = matchAny;
			constFlag = CONSTRUCTOR_1;
		}

		public boolean matches(Object item) {
			ConditionItemBinding b = (ConditionItemBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_1:
					assertEquals(negate, b.isNegate());
					assertEquals(matchAny, b.isMatchAny());
					// fall through
				case CONSTRUCTOR_0:
					assertEquals(left, b.getLeftHand());
					assertEquals(op, b.getOperator());
					assertEquals(right, b.getRightHand());
				}
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class ComplexALComponentMatcher extends NameMatcher<ComplexALComponentBinding> {
		private final Matcher<? extends ComplexComponentBinding> config;
		private final String state;
		private final ALComponentInitializeEnum init;
		private final String simState;
		private final boolean sandboxRecord;
		private final boolean sandboxPlayback;

		private String desc = this.getClass().getSimpleName() + ": ";

		private boolean error;

		public ComplexALComponentMatcher(String name, Matcher<? extends ComplexComponentBinding> config, String state,
				ALComponentInitializeEnum init, String simState, boolean sandboxRecord, boolean sandboxPlayback) {
			super(name);
			this.config = config;
			this.state = state;
			this.init = init;
			this.simState = simState;
			this.sandboxRecord = sandboxRecord;
			this.sandboxPlayback = sandboxPlayback;
		}

		public boolean matches(Object item) {
			ComplexALComponentBinding b;
			if (item instanceof ComplexALComponentBinding) {
				b = (ComplexALComponentBinding) item;
			} else {
				return false;
			}

			try {
				assertThatSafe(b.getComplexConfig(), (Matcher) config);
				assertEquals(state, b.getState());
				assertEquals(init, b.getInitialize());
				assertEquals(simState, b.getSimulateState());
				assertEquals(sandboxRecord, b.isSandboxRecord());
				assertEquals(sandboxPlayback, b.isSandboxPlayback());
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}

	}

	public static class LogMatcher extends BaseMatcher<LogBinding> {

		private final Matcher<LogItemBinding>[] items;

		private String desc = this.getClass().getSimpleName() + ": ";

		public LogMatcher(Matcher<LogItemBinding>... items) {
			this.items = items;
		}

		public boolean matches(Object item) {
			LogBinding b = (LogBinding) item;

			try {
				assertContainsInAnyOrder(b.getItems(), items);
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class LogItemMatcher extends InheritMatcher<LogItemBinding> {

		private final Matcher<ParameterBinding>[] params;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;

		private String desc = this.getClass().getSimpleName() + ": ";

		private boolean error;

		public LogItemMatcher(String name) {
			super(name);
			params = null;
			constFlag = CONSTRUCTOR_0;
		}

		public LogItemMatcher(String name, String inheritFrom) {
			super(name, inheritFrom);
			params = null;
			constFlag = CONSTRUCTOR_0;
		}

		public LogItemMatcher(String name, String inheritFrom, Matcher<ParameterBinding>... params) {
			super(name, inheritFrom);
			this.params = params;
			constFlag = CONSTRUCTOR_1;
		}

		public boolean matches(Object item) {
			LogItemBinding b = (LogItemBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_1:
					assertContainsInAnyOrder(b.getParameters(), params);
					// fall through
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class ALSimulationMatcher extends BaseMatcher<ALSimulationBinding> {
		private final Matcher<ProxyALBinding> proxy;

		private String desc = this.getClass().getSimpleName() + ": ";

		public ALSimulationMatcher(Matcher<ProxyALBinding> proxy) {
			this.proxy = proxy;
		}

		public boolean matches(Object item) {
			ALSimulationBinding b = (ALSimulationBinding) item;

			try {
				assertThatSafe(b.getProxy(), proxy);
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class ProxyALMatcher extends BaseMatcher<ProxyALBinding> {
		private final String server;
		private final String ci;
		private final String al;
		private final ProxyALModeEnum mode;
		private final boolean debug;

		private String desc = this.getClass().getSimpleName() + ": ";

		public ProxyALMatcher(String server, String ci, String al, ProxyALModeEnum mode, boolean debug) {
			this.server = server;
			this.ci = ci;
			this.al = al;
			this.mode = mode;
			this.debug = debug;
		}

		public boolean matches(Object item) {
			ProxyALBinding b = (ProxyALBinding) item;

			try {
				assertEquals(server, b.getServer());
				assertEquals(ci, b.getConfigInstance());
				assertEquals(al, b.getAssemblyLine());
				assertEquals(mode, b.getMode());
				assertEquals(debug, b.isDebug());
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class ALSandboxMatcher extends BaseMatcher<ALSandboxBinding> {

		private final String id;

		private String desc = this.getClass().getSimpleName() + ": ";

		public ALSandboxMatcher(String id) {
			this.id = id;
		}

		public boolean matches(Object item) {
			ALSandboxBinding b = (ALSandboxBinding) item;

			try {
				assertEquals(id, b.getIdentifier());
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class NullMatcher extends BaseMatcher<NullBinding> {

		private final String behavior;
		private final String behaviorValue;
		private final String definition;
		private final String definitionValue;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;
		private static final int CONSTRUCTOR_2 = 2;
		private static final int CONSTRUCTOR_3 = 3;

		private String desc = this.getClass().getSimpleName() + ": ";

		public NullMatcher(String behavior) {
			this(behavior, null);
			constFlag = CONSTRUCTOR_0;
		}

		public NullMatcher(String behavior, String behaviorValue) {
			this(behavior, behaviorValue, null);
			constFlag = CONSTRUCTOR_1;
		}

		public NullMatcher(String behavior, String behaviorValue, String definition) {
			this(behavior, behaviorValue, definition, null);
			constFlag = CONSTRUCTOR_2;
		}

		public NullMatcher(String behavior, String behaviorValue, String definition, String definitionValue) {
			this.behavior = behavior;
			this.behaviorValue = behaviorValue;
			this.definition = definition;
			this.definitionValue = definitionValue;
			constFlag = CONSTRUCTOR_3;
		}

		public boolean matches(Object item) {
			NullBinding b = (NullBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_3:
					assertEquals(definitionValue, b.getDefinitionValue());
					// fall through
				case CONSTRUCTOR_2:
					assertEquals(definition, b.getDefinition());
					// fall through
				case CONSTRUCTOR_1:
					assertEquals(behaviorValue, b.getBehaviorValue());
					// fall through
				case CONSTRUCTOR_0:
					assertEquals(behavior, b.getBehavior());
					// fall through
				}
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class ScriptMatcher extends InheritMatcher<ScriptBinding> {

		protected String script;
		protected boolean autoInclude;
		private final Matcher<String>[] files;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;
		private static final int CONSTRUCTOR_2 = 2;
		private static final int CONSTRUCTOR_3 = 3;

		private String desc = this.getClass().getSimpleName() + ": ";
		private boolean error;

		public ScriptMatcher(String name) {
			super(name);
			this.script = null;
			this.files = null;
			this.autoInclude = false;
			constFlag = CONSTRUCTOR_0;
		}

		public ScriptMatcher(String name, String inheritFrom) {
			super(name, inheritFrom);
			this.script = null;
			this.files = null;
			this.autoInclude = false;
			constFlag = CONSTRUCTOR_0;
		}

		public ScriptMatcher(String name, String inheritFrom, String script, boolean autoInclude) {
			this(name, inheritFrom, script, null, autoInclude);
			constFlag = CONSTRUCTOR_1;
		}

		public ScriptMatcher(String name, String inheritFrom, Matcher<String>[] files, boolean autoInclude) {
			this(name, inheritFrom, null, files, autoInclude);
			constFlag = CONSTRUCTOR_2;
		}

		public ScriptMatcher(String name, String inheritFrom, String script, Matcher<String>[] files, boolean autoInclude) {
			super(name, inheritFrom);
			this.script = script;
			this.files = files;
			this.autoInclude = autoInclude;
			constFlag = CONSTRUCTOR_3;
		}

		public boolean matches(Object item) {
			ScriptBinding b = (ScriptBinding) item;

			try {
				if (constFlag == CONSTRUCTOR_1 || constFlag == CONSTRUCTOR_3) {
					assertEquals(script, b.getScript());
				}
				if (constFlag == CONSTRUCTOR_2 || constFlag == CONSTRUCTOR_3) {
					assertContainsInAnyOrder(b.getFiles(), files);
				}

				if (constFlag > CONSTRUCTOR_0) {
					assertEquals(autoInclude, b.isAutoInclude());
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class LinkCriteriaItemMatcher extends BaseMatcher<LinkCriteriaItemBinding> {

		private final String key;
		private final String attribute;
		private final String operator;
		private final String value;

		private String desc = this.getClass().getSimpleName() + ": ";

		public LinkCriteriaItemMatcher(String key, String attribute, String operator, String value) {
			this.key = key;
			this.attribute = attribute;
			this.operator = operator;
			this.value = value;
		}

		public boolean matches(Object item) {
			LinkCriteriaItemBinding b = (LinkCriteriaItemBinding) item;

			try {
				assertEquals(key, b.getKey());
				assertEquals(attribute, b.getAttribute());
				assertEquals(operator, b.getOperator());
				assertEquals(value, b.getValue());
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class LinkCriteriaMatcher extends InheritMatcher<LinkCriteriaBinding> {

		protected final String script;
		protected final Boolean matchAny;
		protected final Boolean advanced;
		private final Matcher<LinkCriteriaItemBinding>[] items;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;
		private static final int CONSTRUCTOR_2 = 2;
		private static final int CONSTRUCTOR_3 = 3;

		private boolean error = false;
		private String desc = this.getClass().getSimpleName() + ": ";

		public LinkCriteriaMatcher(String name) {
			super(name);
			this.advanced = false;
			this.script = null;
			this.items = null;
			this.matchAny = false;

			constFlag = CONSTRUCTOR_0;
		}

		public LinkCriteriaMatcher(String name, String inheritFrom) {
			this(name, inheritFrom, null);
			constFlag = CONSTRUCTOR_0;
		}

		public LinkCriteriaMatcher(String name, String inheritFrom, Matcher<LinkCriteriaItemBinding>[] items) {
			this(name, inheritFrom, items, false);
			constFlag = CONSTRUCTOR_1;
		}

		public LinkCriteriaMatcher(String name, String inheritFrom, Matcher<LinkCriteriaItemBinding>[] items, boolean matchAny) {
			super(name, inheritFrom);
			this.items = items;
			this.matchAny = matchAny;
			this.advanced = false;
			this.script = null;
			constFlag = CONSTRUCTOR_2;
		}

		public LinkCriteriaMatcher(String name, String inheritFrom, boolean advanced, String script) {
			super(name, inheritFrom);
			this.advanced = advanced;
			this.script = script;
			this.items = null;
			this.matchAny = false;
			constFlag = CONSTRUCTOR_3;
		}

		public boolean matches(Object item) {
			LinkCriteriaBinding b = (LinkCriteriaBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_3:
					assertEquals(advanced, b.isAdvanced());
					assertEquals(script, b.getScript());
					break;
				case CONSTRUCTOR_2:
					assertEquals(matchAny, b.isMatchAny());
					// fall through
				case CONSTRUCTOR_1:
					assertContainsInAnyOrder(b.getItems(), items);
					// fall through
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class ConnectorMatcher extends ComplexMatcher<ConnectorBinding> {

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;
		private static final int CONSTRUCTOR_2 = 2;
		private static final int CONSTRUCTOR_3 = 3;
		private static final int CONSTRUCTOR_4 = 4;

		private final ConnectorModeEnum mode;

		private final Matcher<ConnectorModeBinding> modeConfig;

		private final Matcher<DeltaBinding> delta;

		private final Matcher<LinkCriteriaBinding> link;

		private final Matcher<PoolDefinitionBinding> poolDef;

		private final Matcher<PoolInstanceBinding> poolInst;

		private boolean error = false;
		private String desc = this.getClass().getSimpleName() + ": ";

		public ConnectorMatcher(String name) {
			super(name);
			this.mode = null;
			this.modeConfig = null;
			this.link = null;
			this.delta = null;
			this.poolDef = null;
			this.poolInst = null;
			constFlag = CONSTRUCTOR_0;
		}

		public ConnectorMatcher(String name, String inheritFrom) {
			super(name, inheritFrom);
			this.mode = null;
			this.modeConfig = null;
			this.link = null;
			this.delta = null;
			this.poolDef = null;
			this.poolInst = null;
			constFlag = CONSTRUCTOR_0;
		}

		public ConnectorMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass) {
			super(name, inheritFrom, jclass);
			this.mode = null;
			this.modeConfig = null;
			this.link = null;
			this.delta = null;
			this.poolDef = null;
			this.poolInst = null;
			constFlag = CONSTRUCTOR_0;
		}

		public ConnectorMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass, Matcher<ParserBinding> parser) {
			super(name, inheritFrom, jclass, parser);
			this.mode = null;
			this.modeConfig = null;
			this.link = null;
			this.delta = null;
			this.poolDef = null;
			this.poolInst = null;
			constFlag = CONSTRUCTOR_0;
		}

		public ConnectorMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass, Matcher<ParserBinding> parser,
				Matcher<SchemaBinding>[] schemas) {
			super(name, inheritFrom, jclass, parser, schemas);
			this.mode = null;
			this.modeConfig = null;
			this.link = null;
			this.delta = null;
			this.poolDef = null;
			this.poolInst = null;
			constFlag = CONSTRUCTOR_0;
		}

		public ConnectorMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass, Matcher<ParserBinding> parser,
				Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps) {
			super(name, inheritFrom, jclass, parser, schemas, maps);
			this.mode = null;
			this.modeConfig = null;
			this.link = null;
			this.delta = null;
			this.poolDef = null;
			this.poolInst = null;
			constFlag = CONSTRUCTOR_0;
		}

		public ConnectorMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass, Matcher<ParserBinding> parser,
				Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps, Matcher<HooksBinding> hooks) {
			super(name, inheritFrom, jclass, parser, schemas, maps, hooks);
			this.mode = null;
			this.modeConfig = null;
			this.link = null;
			this.delta = null;
			this.poolDef = null;
			this.poolInst = null;
			constFlag = CONSTRUCTOR_0;
		}

		public ConnectorMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass, Matcher<ParserBinding> parser,
				Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps, Matcher<HooksBinding> hooks,
				ConnectorModeEnum mode, Matcher<ConnectorModeBinding> modeConfig) {
			this(name, inheritFrom, jclass, parser, schemas, maps, hooks);
			constFlag = CONSTRUCTOR_1;
		}

		public ConnectorMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass, Matcher<ParserBinding> parser,
				Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps, Matcher<HooksBinding> hooks,
				ConnectorModeEnum mode, Matcher<ConnectorModeBinding> modeConfig, Matcher<LinkCriteriaBinding> link) {
			this(name, inheritFrom, jclass, parser, schemas, maps, hooks, mode, modeConfig);
			constFlag = CONSTRUCTOR_2;
		}

		public ConnectorMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass, Matcher<ParserBinding> parser,
				Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps, Matcher<HooksBinding> hooks,
				ConnectorModeEnum mode, Matcher<ConnectorModeBinding> modeConfig, Matcher<LinkCriteriaBinding> link,
				Matcher<DeltaBinding> delta) {
			this(name, inheritFrom, jclass, parser, schemas, maps, hooks, mode, modeConfig);
			constFlag = CONSTRUCTOR_3;
		}

		public ConnectorMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass, Matcher<ParserBinding> parser,
				Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps, Matcher<HooksBinding> hooks,
				ConnectorModeEnum mode, Matcher<ConnectorModeBinding> modeConfig, Matcher<LinkCriteriaBinding> link,
				Matcher<DeltaBinding> delta, Matcher<PoolDefinitionBinding> poolDef, Matcher<PoolInstanceBinding> poolInst) {
			super(name, inheritFrom, jclass, parser, schemas, maps, hooks);
			this.mode = mode;
			this.modeConfig = modeConfig;
			this.link = link;
			this.delta = delta;
			this.poolDef = poolDef;
			this.poolInst = poolInst;
			constFlag = CONSTRUCTOR_4;
		}

		public boolean matches(Object item) {
			ConnectorBinding b = (ConnectorBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_4:
					assertThatSafe(b.getPoolDef(), poolDef);
					assertThatSafe(b.getPoolInst(), poolInst);
					// fall through
				case CONSTRUCTOR_3:
					assertThatSafe(b.getDeltaConfig(), delta);
					// fall through
				case CONSTRUCTOR_2:
					assertThatSafe(b.getLinkCriteria(), link);
					// fall through
				case CONSTRUCTOR_1:
					assertEquals(mode, b.getMode());
					assertThatSafe(b.getModeConfig(), modeConfig);
					// fall through
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class ComplexMatcher<T extends ComplexComponentBinding> extends InheritMatcher<T> {

		private final Matcher<JavaClassBinding> jclass;
		private final Matcher<ParserBinding> parser;
		private final Matcher<SchemaBinding>[] schemas;
		private final Matcher<AttributeMapBinding>[] maps;
		private final Matcher<HooksBinding> hooks;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;
		private static final int CONSTRUCTOR_2 = 2;
		private static final int CONSTRUCTOR_3 = 3;
		private static final int CONSTRUCTOR_4 = 4;
		private static final int CONSTRUCTOR_5 = 5;

		private boolean error = false;
		private String desc = this.getClass().getSimpleName() + ": ";

		public ComplexMatcher(String name) {
			super(name);
			this.jclass = null;
			this.parser = null;
			this.schemas = null;
			this.maps = null;
			this.hooks = null;
			constFlag = CONSTRUCTOR_0;
		}

		public ComplexMatcher(String name, String inheritFrom) {
			this(name, inheritFrom, null);
			constFlag = CONSTRUCTOR_0;
		}

		public ComplexMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass) {
			this(name, inheritFrom, jclass, null);
			constFlag = CONSTRUCTOR_1;
		}

		public ComplexMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass, Matcher<ParserBinding> parser) {
			this(name, inheritFrom, jclass, parser, null);
			constFlag = CONSTRUCTOR_2;
		}

		public ComplexMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass, Matcher<ParserBinding> parser,
				Matcher<SchemaBinding>[] schemas) {
			this(name, inheritFrom, jclass, parser, schemas, null);
			constFlag = CONSTRUCTOR_3;
		}

		public ComplexMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass, Matcher<ParserBinding> parser,
				Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps) {
			this(name, inheritFrom, jclass, parser, schemas, maps, null);
			constFlag = CONSTRUCTOR_4;
		}

		public ComplexMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass, Matcher<ParserBinding> parser,
				Matcher<SchemaBinding>[] schemas, Matcher<AttributeMapBinding>[] maps, Matcher<HooksBinding> hooks) {
			super(name, inheritFrom);
			constFlag = CONSTRUCTOR_5;
			this.jclass = jclass;
			this.parser = parser;
			this.schemas = schemas;
			this.maps = maps;
			this.hooks = hooks;
		}

		public boolean matches(Object item) {
			ComplexComponentBinding b = (ComplexComponentBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_5:
					assertThatSafe(b.getHooks(), hooks);
					// fall through
				case CONSTRUCTOR_4:
					assertContainsInAnyOrder(b.getMaps(), maps);
					// fall through
				case CONSTRUCTOR_3:
					assertContainsInAnyOrder(b.getSchemas(), schemas);
					// fall through
				case CONSTRUCTOR_2:
					assertThatSafe(b.getParser(), parser);
					// fall through
				case CONSTRUCTOR_1:
					assertThatSafe(b.getRawConfig(), jclass);
					// fall through
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class PoolDefinitionMatcher extends BaseMatcher<PoolDefinitionBinding> {

		protected Boolean enabled;
		protected Integer minSize;
		protected Integer maxSize;
		protected Integer purgeInterval;
		protected Integer initializeAttempts;
		protected Integer initializeSleepInterval;

		private String desc = this.getClass().getSimpleName() + ": ";

		public PoolDefinitionMatcher(Boolean enabled, Integer minSize, Integer maxSize, Integer purgeInterval,
				Integer initializeAttempts, Integer initializeSleepInterval) {
			this.enabled = enabled;
			this.minSize = minSize;
			this.maxSize = maxSize;
			this.purgeInterval = purgeInterval;
			this.initializeAttempts = initializeAttempts;
			this.initializeSleepInterval = initializeSleepInterval;
		}

		public boolean matches(Object item) {
			PoolDefinitionBinding b = (PoolDefinitionBinding) item;

			try {
				assertEquals(enabled, b.isEnabled());
				assertEquals(minSize, b.getMinSize());
				assertEquals(maxSize, b.getMaxSize());
				assertEquals(purgeInterval, b.getPurgeInterval());
				assertEquals(initializeAttempts, b.getInitializeAttempts());
				assertEquals(initializeSleepInterval, b.getInitializeSleepInterval());
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class PoolInstanceMatcher extends BaseMatcher<PoolInstanceBinding> {

		protected Boolean enabled;
		protected PoolInstanceExhaustedEnum onExhausted;

		private String desc = this.getClass().getSimpleName() + ": ";

		public PoolInstanceMatcher(Boolean enabled, PoolInstanceExhaustedEnum onExhausted) {
			this.enabled = enabled;
			this.onExhausted = onExhausted;
		}

		public boolean matches(Object item) {
			PoolInstanceBinding b = (PoolInstanceBinding) item;

			try {
				assertEquals(enabled, b.isEnabled());
				assertEquals(onExhausted, b.getOnExhausted());
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class ConnectorModeMatcher extends BaseMatcher<ConnectorModeBinding> {

		protected Boolean computeChanges;
		protected Boolean skipLookup;
		protected int lookupLimit;
		protected Boolean skipDeltaEntryDelete;
		protected Boolean processDeltaEntryOnly;

		private String desc = this.getClass().getSimpleName() + ": ";

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;
		private static final int CONSTRUCTOR_2 = 2;

		public ConnectorModeMatcher(Boolean computeChanges, int lookupLimit) {
			this.computeChanges = computeChanges;
			this.lookupLimit = lookupLimit;
			constFlag = CONSTRUCTOR_0;
		}

		public ConnectorModeMatcher(Boolean skipLookup) {
			this.skipLookup = skipLookup;
			constFlag = CONSTRUCTOR_1;
		}

		public ConnectorModeMatcher(Boolean skipDeltaEntryDelete, Boolean processDeltaEntryOnly) {
			this.skipDeltaEntryDelete = skipDeltaEntryDelete;
			this.processDeltaEntryOnly = processDeltaEntryOnly;
			constFlag = CONSTRUCTOR_2;
		}

		public boolean matches(Object item) {
			ConnectorModeBinding b = (ConnectorModeBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_2:
					assertEquals(skipDeltaEntryDelete, b.isSkipDeltaEntryDelete());
					assertEquals(processDeltaEntryOnly, b.isProcessDeltaEntryOnly());
					break;
				case CONSTRUCTOR_1:
					assertEquals(skipLookup, b.isSkipLookup());
					break;
				case CONSTRUCTOR_0:
					assertEquals(computeChanges, b.isComputeChanges());
					assertEquals(lookupLimit, b.getLookupLimit());
					break;
				}
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}

	}

	private static class DeltaMatcher extends BaseMatcher<DeltaBinding> {

		protected final Boolean enabled;
		protected final String uniqueAttribute;
		protected final String deltaDb;
		protected final Boolean readDeleted;
		protected final Boolean removeDeleted;
		protected final Boolean returnUnchanged;
		protected final Boolean fasterAlgorithm;
		protected final Boolean allowDuplicateKeys;
		protected final DeltaCommitEnum commit;
		protected final DeltaRowLockingEnum rowLocking;
		protected final DeltaChangeDetectionModeEnum changeDetectionMode;
		protected final String changeDetectionAttributes;

		private String desc = this.getClass().getSimpleName() + ": ";

		public DeltaMatcher(Boolean enabled, String uniqueAttribute, String deltaDb, Boolean readDeleted, Boolean removeDeleted,
				Boolean returnUnchanged, Boolean fasterAlgorithm, Boolean allowDuplicateKeys, DeltaCommitEnum commit,
				DeltaRowLockingEnum rowLocking, DeltaChangeDetectionModeEnum changeDetectionMode, String changeDetectionAttributes) {
			this.enabled = enabled;
			this.uniqueAttribute = uniqueAttribute;
			this.deltaDb = deltaDb;
			this.readDeleted = readDeleted;
			this.removeDeleted = removeDeleted;
			this.returnUnchanged = returnUnchanged;
			this.fasterAlgorithm = fasterAlgorithm;
			this.allowDuplicateKeys = allowDuplicateKeys;
			this.commit = commit;
			this.rowLocking = rowLocking;
			this.changeDetectionMode = changeDetectionMode;
			this.changeDetectionAttributes = changeDetectionAttributes;
		}

		public boolean matches(Object item) {
			DeltaBinding b = (DeltaBinding) item;

			try {
				assertEquals(enabled, b.isEnabled());
				assertEquals(uniqueAttribute, b.getUniqueAttribute());
				assertEquals(deltaDb, b.getDeltaDb());
				assertEquals(readDeleted, b.isReadDeleted());
				assertEquals(removeDeleted, b.isRemoveDeleted());
				assertEquals(returnUnchanged, b.isReturnUnchanged());
				assertEquals(fasterAlgorithm, b.isFasterAlgorithm());
				assertEquals(allowDuplicateKeys, b.isAllowDuplicateKeys());
				assertEquals(commit, b.getCommit());
				assertEquals(rowLocking, b.getRowLocking());
				assertEquals(changeDetectionMode, b.getChangeDetectionMode());
				assertEquals(changeDetectionAttributes, b.getChangeDetectionAttributes());
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class ParserMatcher extends InheritMatcher<ParserBinding> {

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;
		private static final int CONSTRUCTOR_2 = 2;
		private static final int CONSTRUCTOR_3 = 3;

		private final Matcher<JavaClassBinding> jclass;

		private boolean error = false;
		private String desc = this.getClass().getSimpleName() + ": ";

		private final Matcher<SchemaBinding>[] schemas;

		private final String userComment;

		public ParserMatcher(String name) {
			super(name);
			this.jclass = null;
			this.schemas = null;
			this.userComment = null;
			constFlag = CONSTRUCTOR_0;
		}

		public ParserMatcher(String name, String inheritFrom) {
			this(name, inheritFrom, null);
			constFlag = CONSTRUCTOR_0;
		}

		public ParserMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass) {
			this(name, inheritFrom, jclass, null);
			constFlag = CONSTRUCTOR_1;
		}

		public ParserMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass, Matcher<SchemaBinding>[] schemas) {
			this(name, inheritFrom, jclass, schemas, null);
			constFlag = CONSTRUCTOR_2;
		}

		public ParserMatcher(String name, String inheritFrom, Matcher<JavaClassBinding> jclass, Matcher<SchemaBinding>[] schemas,
				String userComment) {
			super(name, inheritFrom);
			this.jclass = jclass;
			this.schemas = schemas;
			this.userComment = userComment;
			constFlag = CONSTRUCTOR_3;
		}

		public boolean matches(Object item) {
			ParserBinding b = (ParserBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_3:
					assertEquals(userComment, b.getUserComment());
					// fall through
				case CONSTRUCTOR_2:
					assertContainsInAnyOrder(b.getSchemas(), schemas);
					// fall through
				case CONSTRUCTOR_1:
					assertThatSafe(b.getRawConfig(), jclass);
					// fall through
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class ContainerMatcher extends NameMatcher<ContainerBinding> {

		private final Matcher<? extends NamedBinding>[] items;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;

		private boolean error = false;
		private String desc = this.getClass().getSimpleName() + ": ";

		public ContainerMatcher(String name) {
			super(name);
			items = null;
			constFlag = CONSTRUCTOR_0;
		}

		public ContainerMatcher(String name, Matcher<? extends NamedBinding>[] items) {
			super(name);
			this.items = items;
			constFlag = CONSTRUCTOR_1;
		}

		public boolean matches(Object item) {
			ContainerBinding b = (ContainerBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_1:
					assertContainsInAnyOrder(b.getConfigs(), (Matcher[]) items);
					// fall through
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class MapMatcher extends InheritMatcher<AttributeMapBinding> {

		private final Matcher<AttributeMapItemBinding>[] items;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;
		private static final int CONSTRUCTOR_2 = 2;

		private boolean error = false;
		private String desc = this.getClass().getSimpleName() + ": ";

		private final Matcher<NullBinding> _null;

		public MapMatcher(String name) {
			super(name);
			items = null;
			_null = null;
			constFlag = CONSTRUCTOR_0;
		}

		public MapMatcher(String name, String inheritFrom) {
			this(name, inheritFrom, null);
			constFlag = CONSTRUCTOR_0;
		}

		public MapMatcher(String name, String inheritFrom, Matcher<AttributeMapItemBinding>[] items) {
			this(name, inheritFrom, items, null);
			constFlag = CONSTRUCTOR_1;
		}

		public MapMatcher(String name, String inheritFrom, Matcher<AttributeMapItemBinding>[] items, Matcher<NullBinding> _null) {
			super(name, inheritFrom);
			this.items = items;
			this._null = _null;
			constFlag = CONSTRUCTOR_2;
		}

		public boolean matches(Object item) {
			AttributeMapBinding b = (AttributeMapBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_2:
					assertThatSafe(b.getNull(), _null);
					// fall through
				case CONSTRUCTOR_1:
					assertContainsInAnyOrder(b.getItems(), items);
					// fall through
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class HookMatcher extends InheritMatcher<HookBinding> {

		private final Boolean enabled;
		private final String script;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;

		private boolean error = false;
		private String desc = this.getClass().getSimpleName() + ": ";

		public HookMatcher(String name) {
			super(name);
			constFlag = CONSTRUCTOR_0;
			this.script = null;
			this.enabled = false;
		}

		public HookMatcher(String name, String inheritFrom) {
			this(name, inheritFrom, true, null);
			constFlag = CONSTRUCTOR_0;
		}

		public HookMatcher(String name, String inheritFrom, boolean enabled, String script) {
			super(name, inheritFrom);
			this.enabled = enabled;
			this.script = script;
			constFlag = CONSTRUCTOR_1;
		}

		public boolean matches(Object item) {
			HookBinding b = (HookBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_1:
					assertEquals(enabled, b.isEnabled());
					assertEquals(script, b.getScript());
					// fall through
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class HooksMatcher extends InheritMatcher<HooksBinding> {

		private final Matcher<HookBinding>[] items;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;

		private boolean error = false;
		private String desc = this.getClass().getSimpleName() + ": ";

		public HooksMatcher(String name) {
			super(name);
			items = null;
			constFlag = CONSTRUCTOR_0;
		}

		public HooksMatcher(String name, String inheritFrom) {
			this(name, inheritFrom, null);
			constFlag = CONSTRUCTOR_0;
		}

		public HooksMatcher(String name, String inheritFrom, Matcher<HookBinding>[] items) {
			super(name, inheritFrom);
			this.items = items;
			constFlag = CONSTRUCTOR_1;
		}

		public boolean matches(Object item) {
			HooksBinding b = (HooksBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_1:
					assertContainsInAnyOrder(b.getHooks(), items);
					// fall through
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class MapItemMatcher extends InheritMatcher<AttributeMapItemBinding> {

		private final Boolean enabled;
		private final String mapsTo;
		private final AttributeMapItemTypeEnum type;
		private final Boolean add;
		private final Boolean modify;
		private final Matcher<NullBinding> _null;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;
		private static final int CONSTRUCTOR_2 = 2;
		private static final int CONSTRUCTOR_3 = 3;
		private static final int CONSTRUCTOR_4 = 4;

		private boolean error = false;
		private String desc = this.getClass().getSimpleName() + ": ";

		public MapItemMatcher(String name) {
			super(name);
			constFlag = CONSTRUCTOR_0;
			this.type = null;
			this.mapsTo = null;
			this.enabled = false;
			this.add = false;
			this.modify = false;
			this._null = null;
		}

		public MapItemMatcher(String name, String inheritFrom) {
			this(name, inheritFrom, null, null);
			constFlag = CONSTRUCTOR_0;
		}

		public MapItemMatcher(String name, String inheritFrom, AttributeMapItemTypeEnum type, String mapsTo) {
			this(name, inheritFrom, type, mapsTo, false, false, false);
			constFlag = CONSTRUCTOR_1;
		}

		public MapItemMatcher(String name, String inheritFrom, AttributeMapItemTypeEnum type, String mapsTo, boolean enabled) {
			this(name, inheritFrom, type, mapsTo, enabled, false, false);
			constFlag = CONSTRUCTOR_2;
		}

		public MapItemMatcher(String name, String inheritFrom, AttributeMapItemTypeEnum type, String mapsTo, boolean enabled,
				boolean add, boolean modify) {
			this(name, inheritFrom, type, mapsTo, enabled, add, modify, null);
			constFlag = CONSTRUCTOR_3;
		}

		public MapItemMatcher(String name, String inheritFrom, AttributeMapItemTypeEnum type, String mapsTo, boolean enabled,
				boolean add, boolean modify, Matcher<NullBinding> n) {
			super(name, inheritFrom);
			this.type = type;
			this.mapsTo = mapsTo;
			this.enabled = enabled;
			this.add = add;
			this.modify = modify;
			this._null = n;
			constFlag = CONSTRUCTOR_4;
		}

		public boolean matches(Object item) {
			AttributeMapItemBinding b = (AttributeMapItemBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_4:
					_null.matches(b.getNull());
					// fall through
				case CONSTRUCTOR_3:
					assertEquals(add, b.isAdd());
					assertEquals(modify, b.isModify());
					// fall through
				case CONSTRUCTOR_2:
					assertEquals(enabled, b.isEnabled());
					// fall through
				case CONSTRUCTOR_1:
					assertEquals(type, b.getType());
					assertEquals(mapsTo, b.getMapsTo());
					// fall through
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class SchemaMatcher extends InheritMatcher<SchemaBinding> {

		private final Matcher<SchemaItemBinding>[] items;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;

		private boolean error = false;
		private String desc = this.getClass().getSimpleName() + ": ";

		public SchemaMatcher(String name) {
			super(name);
			items = null;
			constFlag = CONSTRUCTOR_0;
		}

		public SchemaMatcher(String name, String inheritFrom) {
			this(name, inheritFrom, null);
			constFlag = CONSTRUCTOR_0;
		}

		public SchemaMatcher(String name, String inheritFrom, Matcher<SchemaItemBinding>[] items) {
			super(name, inheritFrom);
			this.items = items;
			constFlag = CONSTRUCTOR_1;
		}

		public boolean matches(Object item) {
			SchemaBinding b = (SchemaBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_1:
					assertContainsInAnyOrder(b.getItems(), items);
					// fall through
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}

	}

	private static class SchemaItemMatcher extends NameMatcher<SchemaItemBinding> {

		private final int min;
		private final String max;
		private final SchemaItemTypeEnum type;
		private Matcher<SchemaItemBinding>[] items;
		private final String sample;
		private final String syntax;
		private final String nativeSyntax;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;
		private static final int CONSTRUCTOR_2 = 2;
		private static final int CONSTRUCTOR_3 = 3;
		private static final int CONSTRUCTOR_4 = 4;

		private boolean error = false;
		private String desc = this.getClass().getSimpleName() + ": ";

		public SchemaItemMatcher(String name) {
			this(name, 0, null);
			constFlag = CONSTRUCTOR_0;
		}

		public SchemaItemMatcher(String name, int min, String max) {
			this(name, min, max, null);
			constFlag = CONSTRUCTOR_1;
		}

		public SchemaItemMatcher(String name, int min, String max, Matcher<SchemaItemBinding>[] items) {
			this(name, min, max, items, SchemaItemTypeEnum.ATTRIBUTE);
			constFlag = CONSTRUCTOR_2;
		}

		public SchemaItemMatcher(String name, int min, String max, Matcher<SchemaItemBinding>[] items, SchemaItemTypeEnum type) {
			this(name, min, max, items, type, null, null, null);
			constFlag = CONSTRUCTOR_3;
		}

		public SchemaItemMatcher(String name, int min, String max, Matcher<SchemaItemBinding>[] items, SchemaItemTypeEnum type,
				String sample, String syntax, String nativeSyntax) {
			super(name);
			this.min = min;
			this.max = max;
			this.items = items;
			this.type = type;
			this.sample = sample;
			this.syntax = syntax;
			this.nativeSyntax = nativeSyntax;
			constFlag = CONSTRUCTOR_4;
		}

		public boolean matches(Object item) {
			SchemaItemBinding b = (SchemaItemBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_4:
					assertEquals(sample, b.getSample());
					assertEquals(syntax, b.getSyntax());
					assertEquals(nativeSyntax, b.getNativeSyntax());
					// fall through
				case CONSTRUCTOR_3:
					assertEquals(type, b.getType());
					// fall through
				case CONSTRUCTOR_2:
					assertContainsInAnyOrder(b.getItems(), items);
					// fall through
				case CONSTRUCTOR_1:
					assertEquals(min, b.getMinOccurs());
					assertEquals(max, b.getMaxOccurs());
					// fall through
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class JavaClassMatcher extends ParametersMatcher<JavaClassBinding> {

		private final Class<?> cls;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;

		private boolean error = false;
		private String desc = this.getClass().getSimpleName() + ": ";

		public JavaClassMatcher(String name) {
			super(name);
			cls = null;
			constFlag = CONSTRUCTOR_0;
		}

		public JavaClassMatcher(String name, String inheritFrom) {
			super(name, inheritFrom, null);
			cls = null;
			constFlag = CONSTRUCTOR_0;
		}

		public JavaClassMatcher(String name, String inheritFrom, Matcher<ParameterBinding>[] items) {
			super(name, inheritFrom);
			cls = null;
			constFlag = CONSTRUCTOR_0;
		}

		public JavaClassMatcher(String name, String inheritFrom, Class<?> cls, Matcher<ParameterBinding>[] items) {
			super(name, inheritFrom, items);
			this.cls = cls;
			constFlag = CONSTRUCTOR_1;
		}

		public boolean matches(Object item) {
			JavaClassBinding b = (JavaClassBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_1:
					assertThatSafe(b.getClassName(), cls == null ? null : is(cls.getCanonicalName()));
					// fall through
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class ParametersMatcher<T extends ParametersBinding> extends InheritMatcher<T> {

		private final Matcher<ParameterBinding>[] items;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;

		private boolean error = false;
		private String desc = this.getClass().getSimpleName() + ": ";

		public ParametersMatcher(String name) {
			super(name);
			items = null;
			constFlag = CONSTRUCTOR_0;
		}

		public ParametersMatcher(String name, String inheritFrom) {
			this(name, inheritFrom, null);
			constFlag = CONSTRUCTOR_0;
		}

		public ParametersMatcher(String name, String inheritFrom, Matcher<ParameterBinding>[] items) {
			super(name, inheritFrom);
			this.items = items;
			constFlag = CONSTRUCTOR_1;
		}

		public boolean matches(Object item) {
			ParametersBinding b = (ParametersBinding) item;

			try {
				switch (constFlag) {
				case CONSTRUCTOR_1:
					assertContainsInAnyOrder(b.getParameters(), items);
					// fall through
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}
		}
	}

	private static class ParameterMatcher extends BaseMatcher<ParameterBinding> {

		private final String name;
		private final String value;

		private String desc = this.getClass().getSimpleName() + ": ";

		public ParameterMatcher(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public boolean matches(Object item) {
			ParameterBinding p = (ParameterBinding) item;
			try {
				assertEquals(name, p.getName());
				assertEquals(value, p.getValue());
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static class InheritMatcher<T extends InheritingBinding> extends NameMatcher<T> {

		private final String inheritFrom;

		private int constFlag = 0;

		private static final int CONSTRUCTOR_0 = 0;
		private static final int CONSTRUCTOR_1 = 1;

		private boolean error = false;
		private String desc = this.getClass().getSimpleName() + ": ";

		public InheritMatcher(String name) {
			this(name, null);
			constFlag = CONSTRUCTOR_0;
		}

		public InheritMatcher(String name, String inheritFrom) {
			super(name);
			this.inheritFrom = inheritFrom;
			constFlag = CONSTRUCTOR_1;
		}

		public boolean matches(Object item) {
			try {
				switch (constFlag) {
				case CONSTRUCTOR_1:
					assertEquals(inheritFrom, ((InheritingBinding) item).getInheritFrom());
					// fall through
				}
			} catch (AssertionError e) {
				error = true;
				desc += e.getMessage();
				return false;
			}
			return super.matches(item);
		}

		public void describeTo(Description description) {
			if (error) {
				description.appendValue(desc);
			} else {
				super.describeTo(description);
			}

		}
	}

	private static class NameMatcher<T extends NamedBinding> extends BaseMatcher<T> {

		private final String name;

		private String desc = this.getClass().getSimpleName() + ": ";

		public NameMatcher(String name) {
			this.name = name;
		}

		public boolean matches(Object item) {
			NamedBinding c = (NamedBinding) item;
			try {
				assertEquals(name, c.getName());
			} catch (AssertionError e) {
				desc += e.getMessage();
				return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendValue(desc);
		}
	}

	private static <T> void assertThatSafe(T item, Matcher<T> matcher) {
		if (matcher == null ^ item == null) {
			throw new AssertionError("matcher: " + matcher + " item: " + item);
		} else if (matcher != null && !matcher.matches(item)) {
			throw new AssertionError(matcher.toString());
		}
	}

	public static <T> void assertContainsInAnyOrder(Iterable<T> items, Matcher<T>[] matchers) {
		if (matchers == null ^ (items == null || !items.iterator().hasNext())) {
			throw new AssertionError("matchers: " + matchers + " item: " + items);
		} else if (matchers != null) {
                    if (!Matchers.containsInAnyOrder(matchers).matches(items)) {
			throw new AssertionError("matchers: " + matchers + " item: " + items);
                    }
		}
	}
}
