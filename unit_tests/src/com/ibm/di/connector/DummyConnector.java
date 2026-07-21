package com.ibm.di.connector;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.SearchCriteria;

/**
 * A simple lightweight Connector that is used for performance testing. The only
 * point for the Connector is to be used in Iterator mode and return simple
 * Entries in order to avoid any interaction with external sources. The
 * Connector could be set the total number of Entries to return and the size of
 * the Entry.
 */
public class DummyConnector extends Connector implements ConnectorInterface {

	/**
	 * name of Total Iterations parameter
	 */
	public static final String PARAM_TOTAL_ITERATIONS = "totalIterations";

	/**
	 * name of the Total Attributes parameter
	 */

	public static final String PARAM_TOTAL_ATTRIBUTES = "totalAttributes";

	/**
	 * Message for missing or incorrect connector parameters.
	 */
	private static final String MSG_MISSING_OR_INCORRECT_PARAMS = "Missing or incorrect parameter";

	/**
	 * component name
	 */
	private static final String myName = "Dummy Connector";

	/**
	 * Entry that will be returned when getNextEntry() is invoked. Since this is
	 * a Dummy Connector in iterator mode the entry will be one and the same all
	 * the time so this Entry is formed in the selectEntries() method.
	 */
	private Entry entry;

	/**
	 * total number of iterations
	 */
	private Integer totalIterations;

	/**
	 * total number of attributes that are returned with the Entry when
	 * getNextEntry() is invoked.
	 */
	private Integer totalAttributes;

	/**
	 * counter to determine how many Entries have been returned when
	 * getNextEntry() method was invoked.
	 */
	private int counter;

	/**
	 * Constructor.
	 */
	public DummyConnector() {
		super();
		setName(myName);
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE, ConnectorConfig.ADDONLY_MODE, ConnectorConfig.DELETE_MODE,
				ConnectorConfig.UPDATE_MODE, ConnectorConfig.LOOKUP_MODE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(Object o) throws Exception {
		totalIterations = null;
		totalAttributes = null;

		totalIterations = new Integer(getParam(PARAM_TOTAL_ITERATIONS));
		if (totalIterations == null || totalIterations < 1)
			throw new Exception(MSG_MISSING_OR_INCORRECT_PARAMS);

		totalAttributes = new Integer(getParam(PARAM_TOTAL_ATTRIBUTES));
		if (totalAttributes == null || totalAttributes < 1)
			throw new Exception(MSG_MISSING_OR_INCORRECT_PARAMS);

		entry = new Entry();

		String attrName;
		// set the attributes to the Entry
		for (int i = 0; i < totalAttributes; i++) {
			attrName = "attr" + i;
			entry.setAttribute(attrName, attrName);
		}
	}

	/**
	 * Returns the next Entry.
	 */
	@Override
	public Entry getNextEntry() throws Exception {
		if (counter >= totalIterations)
			return null;
		counter++;

		return entry;
	}

	/**
	 * For the purpose of the performance testing this method needn't do
	 * anything in particular.
	 */
	@Override
	public void putEntry(Entry entry) throws Exception {
		// Nothing special to do. For the performance testing no functionality
		// is required for add only mode
	}

	/**
	 * Return the dummy Entry. Simulate that this is the Entry that was found
	 */
	@Override
	public Entry findEntry(SearchCriteria search) throws Exception {
		return entry;
	}

	/**
	 * Delete entry. No communication with external sources is available as this
	 * is a simple dummy Connector. For this reason no implementation for this
	 * method is provided.
	 */
	@Override
	public void deleteEntry(Entry entry, SearchCriteria search) throws Exception {
		// Nothing special to do.
	}

	/**
	 * This method updates the Entry. However, no communication with external
	 * sources is available for performance reasons. As the method doesn't
	 * return anything no implementation is provided
	 */
	@Override
	public void modEntry(Entry entry, SearchCriteria search) throws Exception {
		// Nothing special to do.
	}

	/**
	 * Return version information
	 * 
	 * @return version info
	 */
	public String getVersion() {
		return "1";
	}
}
