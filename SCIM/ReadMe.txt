The SCIM Service allows you to use a SDS server as a SCIM server.

To use the SCIM Service, go to the SCIM folder in your solution directory.
Configure the files there as necessary. These are the files that will need to be changed:

SCIM.properties: Contains several system specific properties
  LDAPServer - The URL for the SDS server that will be storing the user data
  LDAPServer.1 - The URL for the first failover LDAP server, if wanted.
  userSearchBase - The Search Base for users in the SDS server.
  groupSearchBase - The Search Base for groups in the SDS server.
  userSearchFilter - The Search Filter for users in the SDS server.
  groupSearchFilter - The Search Filter for groups in the SDS server.
  userObjectClass - The list of objectclasses used when creating a user in the SDS server.
  groupObjectClass - The list of objectclasses used when creating a group in the SDS server.
  dummyGroupMember - By default LDAP requires groups to have at least one member. When creating new
             groups, if this property is set and the new group does not have any members, this value
             will be added to avoid an Object class violation.
  LDAP.LookupLimit - The maximum number of resources that can be found by the SDS server. The default is
             only 20000, to avoid memory overflow.
  Location - Externally accessible URL of SCIM service. Only affects the Location headers
             in SCIM replies.
  httpPort - The port that the SCIM Service will use for listening. The SCIM Service will always use SSL.
  AuthenticationRealm - The realm presented to the user when asked for authentication.
  debugging - Set to true to get more information in the log file

  authenticationEndpoint - Set to true to add a new local extension to SCIM, that allows using SCIM as an authentication service.
	To use it, send a normal authenticated SCIM request that looks like
		GET authentication?filter=userName eq "Some User"
	and also add a header like
		Authentication-Password: MySecretPassword
	The filter should specify a unique user in the SCIM database, which has the password specified in the header.
	If both of these conditions are true, you will get a "204 No Content" back, otherwise you will get "403 Forbidden" back.

  Authentication:
    By default, the username and password provided as authentication to the SCIM Service must be a LDAP name and password,
    which will be sent to the LDAP server for verification. However, by setting the property
  mapTenantNames=true
    the name sent as authentication will be mapped. When this property is set to true, it will no longer be possible
    to use a LDAP name when authenticating.

    This functionality can be used for several reasons, maybe you do not want the users of the SCIM service to know directly the LDAP name
    that is allowed to change the data in the LDAP server, or maybe you want to check authorization or add
    filtering in the SCIM service to allow multiple domains to use the same SCIM service.

    There are several properties you can add to SCIM.properties for this, and not all are needed, you can use the ones that you
    feel are useful in your solution.
    Each property starts with the SCIM user name, in this example I use the name "someSCIMUser".
    But a SCIM name could also look like "domain.admin" or "domain.authenticator". Known limitations for the name:
        The name can not start or end with space.
        The name can not contain equal (=) or colon (:).
        The name can not start with hash (#),  single quote (') or exclamation mark (!).

   tenantBase
    A "superuser" is able to create new tenants. This property specifies where the tenants should be located. This must be
    an existing container in the LDAP server.
   
   usePasswordPolicy
    When set to true, this property enables the use of tenant specific password policies.
    tenantBase must also have a value, and mapTenantNames must be set to true, and the tenants must be created automatically using the "superuser".

  someSCIMUser.ldapName - The first property defines the name of the tenant of the SCIM service, and which LDAP name this should map to.
     This is needed for the other properties.

  {protect}-someSCIMUser.password
  {protect}-someSCIMUser.ldapPassword
    These two properties are most meaningful if both or none are defined. The first defines the password that should be used when trying to
    use the SCIM service, the other defines the password that will be sent to the LDAP server. They should both be prefixed with
    the {protect} flag, which will cause the password values to be encrypted in the properties file. 

  someSCIMUser.access - This property can be used to restrict the access for a tenant. The value is a comma separated list, which can include
	all - All access is allowed
	createUser - POST a user
	createGroup - POST a group
	modifyUser - PATCH or PUT a user
	modifyGroup - PATCH or PUT a group
	deleteUser - DELETE a user
	deleteGroup - DELETE a group
	readUser - GET on one or more users
	readGroup - GET on one or more groups
	auth - authenticate a user with the non-standard endpoint /authentication
	The default is no access.

	A special value is "superuser", which indicates a tenant that can create other tenants. This cannot be combined with other values.

  someSCIMUser.userSearchBase
  someSCIMUser.groupSearchBase
  someSCIMUser.userSearchFilter
  someSCIMUser.groupSearchFilter
    These properties can be used to restrict the view a tenant has of the underlying LDAP database.

  Logging:
    To log to a file, set the properties
	audit.log - true for logging
	audit.logFile - name of log file
	audit.logFileDatePattern - determines how often the file is rolled, and the extension of old files.
    To log to QRadar, set the properties
        audit.syslog - true for syslogging to QRadar
	audit.QRadarHost - the host where QRadar is located
	audit.QRadarPort - the port number
	audit.facility - The facility for the messages
	audit.eventID - The event ID
	audit.devTimeFormat - The date format to use


UserMapping.json: The mapping between SCIM attributes and LDAP attributes for a user.
Each entry in this file contains a SCIM attribute name and an LDAP attribute name.
The entry may also contain some extra attributes:
  "ReadOnly" - The value will only be mapped from LDAP to SCIM, not the other way.
  "WriteOnly" - The value will only be mapped from SCIM to LDAP, not the other way. Should be used for password.
  "CreateDN" - The value will also be used to create a Distinguished name in the SDS server,
		by appending the userSearchBase to the value. 
		To be able to create new resources, There must be one entry with CreateDN, using a SCIM
		attribute name that will always be provided.
  "Unique" - The value for this attribute must be unique (for that tenant).
  "Type": Provides the canonical type for a multi-valued attribute.
  "Conversion" - specifies a conversion of the attribute value. "Conversion" may have the following values
    "DateTime" - The value needs to be converted from LDAP date format to SCIM date format.
    "Group" - convert the values from an LDAP group to a SCIM group.
    "NewLines" - convert newlines in SCIM values to $ in LDAP Values.
    "Boolean" - convert from SCIM boolean to LDAP "TRUE" or "FALSE"
    "InverseBoolean" - as above, but true maps to "FALSE" and vice versa.
    "IsActive" - computes the active attribute from SDS specific password policy operational attributes
    "MultiValue" - A multi-valued attributes with no canonical type.


There should be only one map Entry for each SCIM name or name/type combination.
There should be only one entry for each LDAP name, unless the Entries are ReadOnly.

With the usual LDAP "person" objectClass, both the "cn" and "sn" attributes are required. By default
these are mapped from the "userName" and "name.familyName" in a SCIM user Entry.
If an attempt is made to create a user without these attributes, an Object violation error will be thrown.

GroupMapping.json: The mapping between SCIM attributes and LDAP attributes for a group.
  The entries in the file have the same format as for a user.

UserSchema.json - This is returned when the SCIM Service is asked for the User Schema.
    The specified attributes should match those defined in UserMapping.json.

GroupSchema.json - This is returned when the SCIM Service is asked for the Group Schema.

-----
Some other files found in this folder:

SCIM.xml - This is the configuration file that implements the SCIM Service.

ServiceProviderConfig.json - This file is returned when the SCIM Service is asked for the ServiceProviderConfig

QRadarLogging.map - Specifies values for attributes sent to the QRadar system when QRadar syslogging is enabled.

ReadMe.txt - This file.
--------
To start the SCIM Service, first make sure that the SDS server is running.
Then give this command
ibmdisrv -c SCIM/SCIM.xml -r SCIM_Service -w
