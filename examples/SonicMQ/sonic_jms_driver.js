var brokerURL = env.get("jms.broker").trim();

var brokerHostName = brokerURL.substring(0, brokerURL.lastIndexOf(":")).trim();
var brokerPort = java.lang.Integer.parseInt( brokerURL.substring(brokerURL.lastIndexOf(":") + 1).trim() );

var useSSL = env.get("jms.sslUseFlag");
if (useSSL == null) {
	useSSL = "false";
}
var brokerProtocol = useSSL.equalsIgnoreCase("true") ? 
									progress.message.jclient.ConnectionFactory.SSL :
									progress.message.jclient.ConnectionFactory.TCP;

var connectID = env.get("jms.connectid");

var username = env.get("jms.username");
if (username == null) {
	username = "";
}

var password = env.get("jms.password");
if (password == null) {
	password = "";
}




ret.queueConnectionFactory = new progress.message.jclient.QueueConnectionFactory(brokerHostName, brokerPort, brokerProtocol, connectID, username, password);
ret.topicConnectionFactory = new progress.message.jclient.TopicConnectionFactory(brokerHostName, brokerPort, brokerProtocol, connectID, username, password);