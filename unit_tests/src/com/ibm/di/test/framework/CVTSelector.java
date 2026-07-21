package com.ibm.di.test.framework;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.ibm.di.test.CVTComponent;
import com.ibm.di.test.CVTTest;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class CVTSelector {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String RELEASE_PACKAGE_PREFIX = "com.ibm.di.cvt";

	private Map<String, Release> releases = new HashMap<String, Release>();

	public CVTSelector() {
		JUnitTestsClassLocator locator = new JUnitTestsClassLocator();
		buildReleasesTree(locator.getCVTClasses());
	}

	/**
	 * @param cvtClasses
	 */
	private void buildReleasesTree(Class<?>[] cvtClasses) {
		String packName = null;
		Release rel = null;
		for (Class<?> clazz : cvtClasses) {
			packName = clazz.getPackage().getName();
			int pos = packName.indexOf('.', RELEASE_PACKAGE_PREFIX.length() + 1);
			if (packName.startsWith(RELEASE_PACKAGE_PREFIX)) {
				String releaseName = packName.substring(RELEASE_PACKAGE_PREFIX.length(), pos == -1 ? packName.length() : pos);

				rel = releases.get(releaseName);
				if (rel == null) {
					rel = new Release();
					releases.put(releaseName, rel);
				}

				rel.addTestClass(clazz);
			}
		}
	}

	private static class Release {

		private Map<String, Component> components = new HashMap<String, Component>();

		/**
		 * @param clazz
		 */
		private void addTestClass(Class<?> clazz) {
			CVTComponent cvtComponentAnno = (CVTComponent) clazz.getAnnotation(CVTComponent.class);

			if (cvtComponentAnno != null) {
				Component comp = components.get(cvtComponentAnno.name());
				if (comp == null) {
					comp = new Component();
					components.put(cvtComponentAnno.name(), comp);
				}

				comp.addTestClass(clazz);
			}
		}

		/**
		 * @return
		 */
		public Set<String> getComponentsNames() {
			return components.keySet();
		}
	}

	private static class Component {

		private List<Class<?>> classes = new LinkedList<Class<?>>();

		private void addTestClass(Class<?> clazz) {
			classes.add(clazz);
		}

		/**
		 * @return
		 */
		public Set<String> getTestsNames() {
			Set<String> names = new HashSet<String>();

			for (Class<?> clazz : classes) {
				Method[] methods = clazz.getMethods();
				for (Method method : methods) {
					CVTTest cvtTest = method.getAnnotation(CVTTest.class);
					Test test = method.getAnnotation(Test.class);
					String cvtTestName = null;
					if (test != null && cvtTest != null) {
						cvtTestName = cvtTest.name();
						if (cvtTestName.trim().length() == 0 || CVTTest.UNDOCUMENTED.equalsIgnoreCase(cvtTestName)) {
							names.add(method.getName());
						} else {
							names.add(cvtTestName);
						}
					}
				}
			}

			return names;
		}
	}

	public Set<String> getReleasesNames() {
		return releases.keySet();
	}

	public Set<String> getComponentsNamesByRelease(String relName) {
		Release rel = releases.get(relName);
		return rel != null ? rel.getComponentsNames() : null;
	}

	public Set<String> getTestsNamesByReleaseAndComponent(String relName, String compName) {
		Release rel = releases.get(relName);
		Component comp = rel != null ? rel.components.get(compName) : null;
		return comp != null ? comp.getTestsNames() : null;
	}
}
