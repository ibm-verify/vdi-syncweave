var brokerURL = env.get("jms.broker").trim();
var bus_name = env.get("jms.serverChannel").trim();

var username = env.get("jms.username");
if (username == null) {
	username = "";
}

var password = env.get("jms.password");
if (password == null) {
	password = "";
}
//Creating a JMS Connection for Topic/Queue

var jmsCFQ = new com.ibm.websphere.sib.api.jms.JmsFactoryFactory.getInstance().createQueueConnectionFactory();
var jmsCFT = new com.ibm.websphere.sib.api.jms.JmsFactoryFactory.getInstance().createTopicConnectionFactory();
jmsCFQ.setBusName(bus_name);
jmsCFT.setBusName(bus_name);
jmsCFQ.setProviderEndpoints(brokerURL);
jmsCFT.setProviderEndpoints(brokerURL);
jmsCFQ.createConnection(username,password).start();
jmsCFT.createConnection(username,password).start();

ret.queueConnectionFactory = this.jmsCFQ;
ret.topicConnectionFactory = this.jmsCFT;