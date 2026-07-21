/**
 * Execute script in a block where you can catch
 * any error to avoid the assemblyline to terminate.
 *
 * @category Misc
 * @code Try/Catch
 */
function sys_code_snippet_1() {
	try {
		;
	} catch(err) {
		task.logmsg("Error occurred: " + err);
	}
}

/**
 * Retrieve the first value as a string from an attribute in the work entry.
 *
 * @category Entry & Attribute
 * @code Get work attribute value
 */
function sys_code_snippet_2() {
	if(work.getString("") != null) {
		value = work.getString("");
	}
}

/**
 * Loop over all attributes in the work entry
 *
 * @code Loop
 * @category Entry & Attribute
 */
function sys_code_snippet_3() {
	for(attr in work.getAttributes()) {
		value = attr.getString()
	}
}

/**
 * Function body
 * @code Function body
 */
function sys_code_snippet_4() {
/**
 * Description of function
 * 
 * @param paramName description of parameter
 * @return description of return value
 */
 function functionName(paramName:string) : string {
 	return "value of the function";
 }	
}

