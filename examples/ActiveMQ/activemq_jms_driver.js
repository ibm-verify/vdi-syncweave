var brokerURL = env.get("jms.broker").trim();
var connectionFactory = new org.apache.activemq.ActiveMQConnectionFactory();
connectionFactory.setBrokerURL(brokerURL);

ret.queueConnectionFactory = connectionFactory;
ret.topicConnectionFactory = connectionFactory;
