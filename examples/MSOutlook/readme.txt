
Overview
This example shows how you can manipulate your Outlook Contacts using COMProxy.
You must have installed and configured Microsoft Outlook (not Outlook Express) for this example to work.

This is an example of an ibmdi.scriptconnector.  The script code is provided below if you would like to create your own script connector and input this data.  msoutlook.xml provides a connector defined as such.  

This example shows how you can create a script connector that supports add,iterate,update,lookup, and delete modes.

You could also copy the MSOutlook.jar file to the jars/connectors directory and it will appear in your list of available connectors to inherit from.  (this connector used to be part of IBM Security Verify Directory Integrator but has since moved into an example) 

If you open msoutlook.xml you will find a scriptconnector called msoutlook that contains this script information ("Connection" tab->"Edit script")

//
// This script implements all the necessary functions for accessing
// the Contacts register in MS Outlook.
// Assumes that the number of entries in contact folder is constant for the run

ol = system.createCOMInstance("Outlook.Application");

ns = COMProxy.call(ol,"GetNameSpace","MAPI");

contacts = COMProxy.call(ns.toObject(),"getDefaultFolder",10);

var item;
var counter = 0;
var oldstring="";
var decode="";

function selectEntries(){
	counter = 0;
}

function getNextEntry (){
	ol = system.createCOMInstance("Outlook.Application");

	ns = COMProxy.call(ol,"GetNameSpace","MAPI");

	contacts = COMProxy.call(ns.toObject(),"getDefaultFolder",10);

	items = COMProxy.call(contacts.toObject(),"Items");

	count = COMProxy.get(items.toObject(),"count");

	counter++;
	if(counter > count){ 
		result.setStatus(0);
		result.setMessage("End of input");
	}else{
		item = COMProxy.call(items.toObject(),"Item",counter);
		populateEntry();
	}
}

function findEntry (){
	main.logmsg("In findEntry");
	flt = "[" + search.getFirstCriteriaName() + "] = " + search.getFirstCriteriaValue();
	items = COMProxy.call(contacts.toObject(),"Items");
	item = COMProxy.call(items.toObject(),"Find",flt);
	if (item == null){
		result.setStatus(0)
		result.setMessage("Not found" + "--->["+ flt + "]");
	}
	else
		populateEntry();
}

function modEntry (){
	populateItem();
	COMProxy.call(item.toObject(),"Save");
}

function deleteEntry (){
	COMProxy.call(item.toObject(),"Delete");
}

function putEntry (){
	items = COMProxy.call(contacts.toObject(),"Items");
	item = COMProxy.get(items.toObject(),"Add");
	if(item==null){
		result.setStatus(2)
		result.setMessage("Unabled to create item");
		return;
	}
	oldString = entry.getString("FullName");
	COMProxy.put(item.toObject(),"FileAs",oldString);
	populateItem();
	COMProxy.call(item.toObject(),"Save");
}

function populateEntry (){
	entry.setAttribute("FileAs", COMProxy.get(item.toObject(),"FileAs"));
	entry.setAttribute("FullName", COMProxy.get(item.toObject(),"FullName"));
	entry.setAttribute("Email1Address", COMProxy.get(item.toObject(),"Email1Address"));
	entry.setAttribute("Birthday", COMProxy.get(item.toObject(),"Birthday"));

	entry.setAttribute("BusinessAddress", COMProxy.get(item.toObject(),"BusinessAddress"));
	entry.setAttribute("BusinessTelephoneNumber", COMProxy.get(item.toObject(),"BusinessTelephoneNumber"));
	entry.setAttribute("BusinessFaxNumber", COMProxy.get(item.toObject(),"BusinessFaxNumber"));
	entry.setAttribute("CompanyName", COMProxy.get(item.toObject(),"CompanyName"));
	entry.setAttribute("JobTitle", COMProxy.get(item.toObject(),"JobTitle"));

	entry.setAttribute("HomeAddress", COMProxy.get(item.toObject(),"HomeAddress"));
	entry.setAttribute("HomeTelephoneNumber", COMProxy.get(item.toObject(),"HomeTelephoneNumber"));
	entry.setAttribute("HomeFaxNumber", COMProxy.get(item.toObject(),"HomeFaxNumber"));

	entry.setAttribute("MobileTelephoneNumber", COMProxy.get(item.toObject(),"MobileTelephoneNumber"));

	entry.setAttribute("Categories", COMProxy.get(item.toObject(),"Categories"));
	entry.setAttribute("LastModificationTime", COMProxy.get(item.toObject(),"LastModificationTime"));
}

function populateItem (){
	COMProxy.put(item.toObject(),"FileAs", entry.getString("FileAs"));
	COMProxy.put(item.toObject(),"FullName", entry.getString("FullName"));
	COMProxy.put(item.toObject(),"Email1Address", entry.getString("Email1Address"));
	COMProxy.put(item.toObject(),"BusinessAddress", entry.getString("BusinessAddress"));
	COMProxy.put(item.toObject(),"BusinessTelephoneNumber", entry.getString("BusinessTelephoneNumber"));
	COMProxy.put(item.toObject(),"BusinessFaxNumber",entry.getString("BusinessFaxNumber"));
	COMProxy.put(item.toObject(),"JobTitle", entry.getString("JobTitle")) ;
	COMProxy.put(item.toObject(),"CompanyName", entry.getString("CompanyName")) ;
	COMProxy.put(item.toObject(),"HomeAddress", entry.getString("HomeAddress")) ;
	COMProxy.put(item.toObject(),"HomeTelephoneNumber", entry.getString("HomeTelephoneNumber")) ;
	COMProxy.put(item.toObject(),"HomeFaxNumber", entry.getString("HomeFaxNumber")) ;
	COMProxy.put(item.toObject(),"Categories", entry.getString("Categories"));
	if (entry.getString("Birthday")!=null && !entry.getString("Birthday").equals(" "))
		COMProxy.put(item.toObject(),"Birthday", entry.getString("Birthday"));
}

function querySchema(o){
	e = new Packages.com.ibm.di.entry.Entry();
	e.setAttribute("name", "FileAs");
	e.setAttribute("syntax", "java.lang.String");
	e.setAttribute("size", "");
	e.setAttribute("type", "");
	list.add(e);
	
	e = new Packages.com.ibm.di.entry.Entry();
	e.setAttribute("name", "FullName");
	e.setAttribute("syntax", "java.lang.String");
	e.setAttribute("size", "");
	e.setAttribute("type", "");
	list.add(e);

	e = new Packages.com.ibm.di.entry.Entry();
	e.setAttribute("name", "Email1Address");
	e.setAttribute("syntax", "java.lang.String");
	e.setAttribute("size", "");
	e.setAttribute("type", "");
	list.add(e);

	e = new Packages.com.ibm.di.entry.Entry();
	e.setAttribute("name", "BusinessAddress");
	e.setAttribute("syntax", "java.lang.String");
	e.setAttribute("size", "");
	e.setAttribute("type", "");
	list.add(e);

	e = new Packages.com.ibm.di.entry.Entry();
	e.setAttribute("name", "BusinessTelephoneNumber");
	e.setAttribute("syntax", "java.lang.String");
	e.setAttribute("size", "");
	e.setAttribute("type", "");
	list.add(e);

	e = new Packages.com.ibm.di.entry.Entry();
	e.setAttribute("name", "BusinessFaxNumber");
	e.setAttribute("syntax", "java.lang.String");
	e.setAttribute("size", "");
	e.setAttribute("type", "");
	list.add(e);

	e = new Packages.com.ibm.di.entry.Entry();
	e.setAttribute("name", "JobTitle");
	e.setAttribute("syntax", "java.lang.String");
	e.setAttribute("size", "");
	e.setAttribute("type", "");
	list.add(e);

	e = new Packages.com.ibm.di.entry.Entry();
	e.setAttribute("name", "CompanyName");
	e.setAttribute("syntax", "java.lang.String");
	e.setAttribute("size", "");
	e.setAttribute("type", "");
	list.add(e);

	e = new Packages.com.ibm.di.entry.Entry();
	e.setAttribute("name", "HomeAddress");
	e.setAttribute("syntax", "java.lang.String");
	e.setAttribute("size", "");
	e.setAttribute("type", "");
	list.add(e);

	e = new Packages.com.ibm.di.entry.Entry();
	e.setAttribute("name", "HomeTelephoneNumber");
	e.setAttribute("syntax", "java.lang.String");
	e.setAttribute("size", "");
	e.setAttribute("type", "");
	list.add(e);

	e = new Packages.com.ibm.di.entry.Entry();
	e.setAttribute("name", "HomeFaxNumber");
	e.setAttribute("syntax", "java.lang.String");
	e.setAttribute("size", "");
	e.setAttribute("type", "");
	list.add(e);

	e = new Packages.com.ibm.di.entry.Entry();
	e.setAttribute("name", "Categories");
	e.setAttribute("syntax", "java.lang.String");
	e.setAttribute("size", "");
	e.setAttribute("type", "");
	list.add(e);

	e = new Packages.com.ibm.di.entry.Entry();
	e.setAttribute("name", "Birthday");
	e.setAttribute("syntax", "java.lang.String");
	e.setAttribute("size", "");
	e.setAttribute("type", "");
	list.add(e);
}
