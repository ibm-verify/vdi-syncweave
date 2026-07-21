env = new Packages.java.util.Hashtable();
env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
env.put("java.naming.provider.url", "ldap://192.168.113.54:389");
env.put("java.naming.security.principal", userdata.username);
env.put("java.naming.security.credentials", userdata.password);
env.put(Packages.javax.naming.Context.SECURITY_AUTHENTICATION, "simple");

main.logmsg("Authentication request for user: " +  userdata.username);

try
{
	mCtx = new Packages.javax.naming.directory.InitialDirContext(env);
	ret.auth = true;
}
catch(e)
{
	ret.auth = false;	
	ret.errordescr = e.toString();
//	ret.errorcode = "49";
}
