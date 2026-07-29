/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection.internal.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.ibm.di.api.connection.internal.proxy.InstanceCache.InstanceFactory;
import com.ibm.di.api.connection.internal.proxy.impl.ReadWriteLockInstanceCache;
import com.ibm.di.api.connection.internal.proxy.impl.SynchronizedInstanceCache;

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
public class ApiAdapter implements InvocationHandler {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static enum InstanceType {
		/**
		 * Marks that the adapter is adapting a local api instance to the
		 * corresponding Remote interface.
		 */
		LOCAL,
		/**
		 * Marks that the adapter is adapting a remote api instance to the
		 * corresponding Local interface.
		 */
		REMOTE;
	}

	private static final InstanceCache<InstanceCache<MethodAdapter>> methodAdaptersCache = new ReadWriteLockInstanceCache<InstanceCache<MethodAdapter>>(
			new InstanceFactory<InstanceCache<MethodAdapter>>() {
				public InstanceCache<MethodAdapter> newInstance(String key) {
					return new SynchronizedInstanceCache<MethodAdapter>(null);
				}
			});

	private final Object adaptedInstance;
	private final InstanceType type;
	private final ConverterFactory cf;

	public ApiAdapter(Object adaptedInstance, InstanceType type, ConverterFactory cf) {
		this.adaptedInstance = adaptedInstance;
		this.type = type;
		this.cf = cf;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			// Make sure two proxy instances are equal if the instances being
			// adapted are also equal.
			if ("hashCode".equals(method.getName())) {
				return adaptedInstance.hashCode();
			} else if ("equals".equals(method.getName())) {
				if (args.length == 1 && args[0] != null && Proxy.isProxyClass(args[0].getClass())) {
					InvocationHandler ih = Proxy.getInvocationHandler(args[0]);
					if (ih instanceof ApiAdapter) {
						return ((ApiAdapter) ih).adaptedInstance.equals(adaptedInstance);
					}
				}
				return false;
			}

			MethodAdapter methAdapter = getMethodAdapter(proxy, method);
			return methAdapter.invoke(adaptedInstance, args);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	private MethodAdapter getMethodAdapter(Object proxy, final Method method) {
		String methKey = getMethodKey(method);
		InstanceCache<MethodAdapter> methCache = methodAdaptersCache.getInstance(proxy.getClass().getName());
		return methCache.getInstance(methKey, new InstanceFactory<MethodAdapter>() {
			public MethodAdapter newInstance(String key) {
				return new MethodAdapter(method, cf);
			}
		});
	}

	private String getMethodKey(Method method) {
		int len = method.getName().length() + 1;
		Class<?>[] params = method.getParameterTypes();
		for (Class<?> param : params) {
			len += param.getClass().getName().length() + 1;
		}
		StringBuilder sb = new StringBuilder(len);
		sb.append(method.getName());
		sb.append('(');
		for (Class<?> param : params) {
			sb.append(param.getClass().getName());
			sb.append(',');
		}
		sb.setCharAt(sb.length() - 1, ')');

		return sb.toString();
	}

	public InstanceType getInstanceType() {
		return type;
	}

	public Object getAdaptedInstance() {
		return adaptedInstance;
	}

	private static class MethodAdapter {

		private static Converter[] EMPTY_CONVERTERS = new Converter[0];

		private final String methodName;
		private final Converter[] paramsConverters;
		private final Class<?>[] convertedParams;
		private final Converter returnConverter;
		private final Class<?> toReturn;

		public MethodAdapter(Method method, ConverterFactory cf) {
			this.methodName = method.getName();
			Class<?>[] params = method.getParameterTypes();
			this.paramsConverters = params.length == 0 ? EMPTY_CONVERTERS : new Converter[params.length];

			for (int i = 0; i < paramsConverters.length; i++) {
				paramsConverters[i] = cf.getInstance(params[i]);
			}

			if (params.length > 0) {
				convertedParams = new Class<?>[paramsConverters.length];
				for (int i = 0; i < convertedParams.length; i++) {
					convertedParams[i] = paramsConverters[i].convert(params[i]);
				}
			} else {
				convertedParams = params;
			}

			toReturn = method.getReturnType();
			// find the converter used for input parameters
			Converter inputConverter = cf.getInstance(toReturn);
			// see what would the type of the response be on invocation
			Class<?> convertedReturn = inputConverter.convert(toReturn); 
			// find the converter that can handle the actual response type
			returnConverter = cf.getInstance(convertedReturn);
		}

		private Object invoke(Object instance, Object[] args) throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException, SecurityException, NoSuchMethodException {
			Object[] converted = args == null || args.length == 0 ? args : new Object[args.length];
			for (int i = 0; i < paramsConverters.length; i++) {
				converted[i] = paramsConverters[i].convert(args[i], convertedParams[i]);
			}

			Method method = instance.getClass().getMethod(methodName, convertedParams);
			return returnConverter.convert(method.invoke(instance, converted), toReturn);
		}
	}
}
