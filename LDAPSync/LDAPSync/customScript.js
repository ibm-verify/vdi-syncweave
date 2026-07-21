// This function returns the object class used to create new Person entries in the target
// Note that the Work Entry is passed in, but not used in this function, although the
// data on the current Person could be used to choose which object class to use.
//
function userObjectClass(workEntry) {
		var oc = system.newAttribute("objectClass")
		var parts = system.splitString(targetUserObjectClass,",")
		
		for (var p in parts)
			oc.addValue(p.trim())
			
		return oc
}

// This function returns the object class used to create new Group entries in the target
// Note that the Work Entry is passed in, but not used in this function, although the
// data on the current Group could be used to choose which object class to use.
//
function groupObjectClass(workEntry) {
		var oc = system.newAttribute("objectClass")
		var parts = system.splitString(targetGroupObjectClass,",")
		
		for (var p in parts)
			oc.addValue(p)
			
		return oc
}

// This function checks the AD attribute named userAccountControl to see if the bit flag 2
// is set, in which case it returns false; Otherwise true.
//
function userAccountDisabled(workEntry) {
	// only for SDS target
	if (typeof targetIsTDS !== "undefined" && !targetIsTDS) {
		return null;
	}
	
	var uaControl = work.getObject("userAccountControl"); // AD
	
	if (uaControl !== null) {
		var uacStr = new java.lang.String(uaControl);
		var uacInt = java.lang.Integer.parseInt(uacStr);
		if (deBug) logmsg("--##-> locking account based on userAccountControl: " + uacInt + " - " + ((uacInt & 2) == 2));
		return String((uacInt & 2) == 2);
	}
	
	uaControl = work.getString("nsAccountLock") // Sun/Oracle
	if (uaControl !== null) {
		if (deBug) logmsg("--##-> locking account based on nsAccountLock: " + uaControl);
		return String("true".equalsIgnoreCase(uaControl));
	}

	uaControl = work.getString("ibm-pwdAccountLocked"); // SDS
	if (uaControl !== null) {
		if (deBug) logmsg("--##-> locking account based on ibm-pwdAccountLocked: " + uaControl);
		return String("true".equalsIgnoreCase(uaControl));
	}

	if (deBug) logmsg("--##-> locking account as false by default");
	return "false";
}

// These next two function return the target Url and login username respectively.
// They are necessary since the SCIM target plugin uses different property names
// for Connection settings. These functions look for the SCIM properties, and if
// these are not found then they fall back on the default ones for standard
// Target SDS operations.
//
function getTargetUrl() {
	var propVal = getProperty("scim.url");
	if (propVal == null || propVal.trim().length == 0 || "ldap://".equalsIgnoreCase(propVal)) {
		propVal = getProperty("target.ldap.url");
	}
	java.lang.System.out.println("getTargetUrl() - got value: " + propVal)
	return propVal;
}

function getTargetUser() {
	var propVal = getProperty("scim.user");
	if (propVal == null || propVal.trim().length == 0) {
		propVal = getProperty("target.ldap.user");
	}
//	java.lang.System.out.println("getTargetUser() - got value: " + propVal)
	return propVal;
}

// These functions are used in the monitoring maps
//
// QRadar
//
// Returns the Hostname of the passed LDAP url
function getHostname(url) {
	var hostname = url
//	java.lang.System.out.println("getHostname() - passed in: " + url);
	if (hostname !== null) {
		var p = hostname.indexOf("://");
		if (p > 0) {
			hostname = hostname.substring(p+3);
		}
		
		p = hostname.lastIndexOf(":");
		if (p > 0) {
			hostname = hostname.substring(0, p);
		}
	}
//	java.lang.System.out.println("getHostname() - returning: " + hostname);
	return hostname;
}

// returns the port number of the passed LDAP url
function getPortNumber(url) {
	var port = url
//	java.lang.System.out.println("getPort() - passed in: " + url);
	if (port !== null) {
		var p = port.lastIndexOf(":");
		if (p < 0) {
			port = "389";
		} else {
			var p2 = port.indexOf("/", p);
			if (p2 > p) {
				port = port.substring(p+1, p2);
			} else {
				port = port.substring(p+1);
			}
		}
	}
//	java.lang.System.out.println("getPort() - returning: " + port);
	try {
		var portNum = system.toInt(port);
	} catch (ex) {
		portNum = 0;
	}
	return portNum;
}

// retuns the severity based on the writeStatus:
//     success = 1
//	   failed (e.g. other than success) = 5
function getSeverity() {
	if ("success".equals(work.getString("$writeStatus"))) {
		return 1;
	} else {
		return 5;
	}
}

// returns the write result
function getWriteResult() {
	var writeStatus = work.getString("$writeStatus");
	if ("success".equals(writeStatus)) {
		return "Success";
	} else
	if ("ok".equals(error.getString("status"))) {
		return writeStatus
	} else
	if (error.exception !== null) {
		return error.exception.toString();
	} else {
		return error.toString();
	}
}

// returns the comma-separated list of Attribute names for an add or modify
function getAttributeList() {
	var connEntry = work.getObject("$conn");
	if (connEntry === null || connEntry.size() == 0) {
		return "";
	}

	var attNames = connEntry.getAttributeNames();
	java.util.Arrays.sort(attNames);
	var attList = "";
	for (var name in attNames) {
		if (!name.startsWith("$") || name.equals("$dn")) {
			attList += name + ","
		}
	}
	return attList.substring(0, attList.length-1);
}

// returns the comma-separated list of Attribute operations for a modify
function getAttributeOperationList() {
	var connEntry = work.getObject("$conn");
	if (connEntry === null || connEntry.size() == 0) {
		return "";
	}

	var attNames = connEntry.getAttributeNames();
	java.util.Arrays.sort(attNames);
	var opList = "";
	for (var name in attNames) {
		if (!name.startsWith("$") || name.equals("$dn")) {
			opList += connEntry.getAttribute(name).getOperation() + ",";
		}
	}
	return opList.substring(0, opList.length-1);
}

// returns the specified property for the operation codes listed
// the oplist argument can be a single operation ('add') or a comma-separated list
function getValueForOperation(val, oplist) {
	if (typeof oplist === "undefined") { oplist = "" }
	var ops = system.splitString(oplist.toLowerCase(),",");
	var currentOp = work.getString("$operation");
	var opMatched = false;

	for (op in ops) {
		if (op.equals(currentOp)) {
			opMatched = true;
			break;
		}
	}
	
	if (!opMatched) {
		return null;
	}
	
	return val;
}

// returns the SDS Audit operation tag for QRadar events
function getAuditOperation(operation) {
	if ("add".equals(operation)) {
		return "V3 Add";
	} else
	if ("delete".equals(operation)) {
		return "V3 Delete";
	} else {
		return "V3 Modify";
	}
}

