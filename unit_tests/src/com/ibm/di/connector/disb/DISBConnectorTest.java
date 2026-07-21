package com.ibm.di.connector.disb;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.ibm.di.config.base.ConnectorConfigImpl;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.systemqueue.driver.IBMMQ;
import com.ibm.di.test.utils.RSMock;

public class DISBConnectorTest {

	public static final String JMS_BROKER = IBMMQ.PROP_MQ_BROKER;

	@Test
	public void test_putEntry_Opset_Create() throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put(JMS_BROKER, "tcp://localhost:61616");
		params.put("jms.connectionType", "Queue");
		params.put("jms.topic", "DisInboundQ");
		params.put("jms.driver", "ActiveMQ");
		params.put("jms.sslUseFlag", "false");

		ConnectorInterface disbc = createConnector(ConnectorConfig.ADDONLY_MODE, params);
		disbc.initialize(null);

		Entry e = new Entry();
		String msg = "{\"operationSet\":{\"opid\":\"1\",\"create\":{\"timeStamp\":\"2011-03-07T12:28:18Z\",\"modelObject\":{\"ComputerSystem\":[{\"guid\":\"923DE6F8DEF138F8AD0AFE3FF6E91E9B\",\"PrimaryMACAddress\":\"001C25740059\",\"SystemBoardUUID\":\"UUID-0003\",\"namingContext\":{\"source\":\"1234\",\"target\":\"3456\" }}],\"OperatingSystem\":[{\"guid\":\"D2C3F90AC23930E3B3F398012C68A210\",\"ManagedSystemName\":\"Win\",\"FQDN\":\"fqdn\" },{\"guid\":\"D2C3F90AC23930E3B3F398012C68A211\",\"ManagedSystemName\":\"Win2003\",\"FQDN\":\"fqdn\" }]},\"relationship\":{\"contains\":[{\"source\":\"923DE6F8DEF138F8AD0AFE3FF6E91E9B\",\"target\":\"D2C3F90AC23930E3B3F398012C68A210\"}]}}}}";
		e.setAttribute("message", msg);

		disbc.putEntry(e);
		disbc.terminate();

		// Check the output of putEntry() is same as what we created
		assertTrue(true);
	}

	@Test
	public void test_getNextEntry_InstanceTopic_Opset_Create() throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put(JMS_BROKER, "tcp://localhost:61616");
		params.put("jms.connectionType", "Topic");
		params.put("topicType", "Instance Topic");
		params.put("jms.topic", "DISInstanceTopic");
		params.put("jms.driver", "ActiveMQ");
		params.put("jms.sslUseFlag", "false");
		params.put("jms.getnextTimeout", "0");

		ConnectorInterface disbc = createConnector(ConnectorConfig.ITERATOR_MODE, params);
		disbc.initialize(null);

		disbc.selectEntries();
		Entry entry = new Entry();
		String msg = "{\"operationSet\":{\"opid\":\"1\",\"create\":{\"timeStamp\":\"2011-03-07T12:28:18Z\",\"modelObject\":{\"ComputerSystem\":[{\"guid\":\"923DE6F8DEF138F8AD0AFE3FF6E91E9B\",\"PrimaryMACAddress\":\"001C25740059\",\"SystemBoardUUID\":\"UUID-0003\",\"namingContext\":{\"source\":\"1234\",\"target\":\"3456\" }}],\"OperatingSystem\":[{\"guid\":\"D2C3F90AC23930E3B3F398012C68A210\",\"ManagedSystemName\":\"Win\",\"FQDN\":\"fqdn\" },{\"guid\":\"D2C3F90AC23930E3B3F398012C68A211\",\"ManagedSystemName\":\"Win2003\",\"FQDN\":\"fqdn\" }]},\"relationship\":{\"contains\":[{\"source\":\"923DE6F8DEF138F8AD0AFE3FF6E91E9B\",\"target\":\"D2C3F90AC23930E3B3F398012C68A210\"}]}}}}";
		int count = 0;
		while ((entry = disbc.getNextEntry()) != null) {
			count++;
			assertTrue(entry.getAttribute("message").getValue().equals(msg));
		}
		disbc.terminate();

		// Check the output of putEntry() is same as what we created
		assertTrue(true);
	}

	private ConnectorInterface createConnector(String mode, Map<String, String> params) throws Exception {
		ConnectorInterface conn = new DISBConnector();
		ConnectorConfig cc = new ConnectorConfigImpl();
		cc.init();
		cc.setState(ConnectorConfig.ENABLED_STATE);
		cc.setMode(mode);
		cc.getConnectionConfig().setJavaClass(DISBConnector.class.getName());
		for (Map.Entry<String, String> param : params.entrySet()) {
			cc.getConnectionConfig().setParameter(param.getKey(), param.getValue());
		}
		conn.setConfiguration(cc);

		conn.setLog(new Log(""));
		conn.setRSInterface(new RSMock());
		return conn;
	}

}
