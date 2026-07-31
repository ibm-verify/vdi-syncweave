//******************************************************************************
//
// CMVC: @(#)49 1.7  src/dsml/com/ibm/ldap/dsml/LdapResult.java, ldap.dsml, idstools_81 10/9/03 15:26:15
//
// ORIGINS: 27
//
// IBM CONFIDENTIAL -- (IBM Confidential Restricted when
// combined with the aggregated modules for this product)
// OBJECT CODE ONLY SOURCE MATERIALS
// (C) COPYRIGHT International Business Machines Corp. 2002
// All Rights Reserved
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
//
// CHANGE HISTORY:
// ---------------
// 7/2/02   mehaberk              Base file version
// 8/6/02   mehaberk     D74209   Update entire component
//******************************************************************************
package com.ibm.ldap.dsml;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.CommunicationException;
import javax.naming.ContextNotEmptyException;
import javax.naming.InvalidNameException;
import javax.naming.LimitExceededException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;
import javax.naming.OperationNotSupportedException;
import javax.naming.ReferralException;
import javax.naming.ServiceUnavailableException;
import javax.naming.SizeLimitExceededException;
import javax.naming.TimeLimitExceededException;
import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.InvalidAttributeIdentifierException;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.InvalidSearchFilterException;
import javax.naming.directory.NoSuchAttributeException;
import javax.naming.directory.SchemaViolationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * Contains an LdapResult.  
 * 
 */
public class LdapResult extends DsmlMessage {
    private static final String CLASS = "LdapResult";


    /*	DSML Error Codes (same as LDAP)


    0	"success"
    1	"operationsError"
    2	"protocolError"
    3	"timeLimitExceeded"
    4	"sizeLimitExceeded"
    5	"compareFalse"
    6	"compareTrue"
    7	"authMethodNotSupported"
    8	"strongAuthRequired"
    
    9 	----   reserved   ---------------------
    
    10	"referral"
    11	"adminLimitExceeded"
    12	"unavailableCriticalExtension"
    13	"confidentialityRequired"
    14	"saslBindInProgress"
    
    15	----	not used	---------------------
    
    16	"noSuchAttribute"	
    17	"undefinedAttributeType"
    18	"inappropriateMatching"
    19	"constraintViolation"
    20	"attributeOrValueExists"
    21	"invalidAttributeSyntax"


    22-31 ----	not used	---------------------
    
    32	"noSuchObject"
    33	"aliasProblem"
    34	"invalidDNSyntax"


    35 	-----	reserved for undefined isLeaf	-----


    36	"aliasDereferencingProblem"


    37-47 ----	not used	----------------------


    48	"inappropriateAuthentication"
    49	"invalidCredentials"
    50	"insufficientAccessRights"
    51	"busy"
    52	"unavailable"
    53	"unwillingToPerform"
    54	"loopDetect"


    55-63 ----	not used	----------------------


    64	"namingViolation"
    65	"objectClassViolation"
    66	"notAllowedOnNonLeaf"
    67	"notAllowedOnRDN"
    68	"entryAlreadyExists"
    69	"objectClassModsProhibited"	


    70 	----	reserved for CLADAP	---------------


    71	"affectsMultipleDSAs"


    72-79 ----	not used	-------------------------


    80	"other"


    81-90 ----	reserved for APIs	-----------------


    */
    protected static final int COMPARE_FALSE    = 5;
    protected static final int COMPARE_TRUE     = 6;

    protected static final String[] ERROR_MSGS = {"success",
                                                  "operationsError",
                                                  "protocolError",
                                                  "timeLimitExceeded",
                                                  "sizeLimitExceeded",
                                                  "compareFalse",
                                                  "compareTrue",
                                                  "authMethodNotSupported",
                                                  "strongAuthRequired",
                                                  null, // 9 reserved
                                                  "referral",
                                                  "adminLimitExceeded",
                                                  "unavailableCriticalExtension",
                                                  "confidentialityRequired",
                                                  "saslBindInProgress",
                                                  null, // 15 not used
                                                  "noSuchAttribute",
                                                  "undefinedAttributeType",
                                                  "inappropriateMatching",
                                                  "constraintViolation",
                                                  "attributeOrValueExists",
                                                  "invalidAttributeSyntax",
                                                  null, // 22 not used
                                                  null, // 23 not used
                                                  null, // 24 not used
                                                  null, // 25 not used
                                                  null, // 26 not used
                                                  null, // 27 not used
                                                  null, // 28 not used
                                                  null, // 29 not used
                                                  null, // 30 not used
                                                  null, // 31 not used
                                                  "noSuchObject",
                                                  "aliasProblem",
                                                  "invalidDNSyntax",
                                                  null, // 35 reserved for undefined isLeaf
                                                  "aliasDereferencingProblem",
                                                  null, // 37 not used
                                                  null, // 38 not used
                                                  null, // 39 not used
                                                  null, // 40 not used
                                                  null, // 41 not used
                                                  null, // 42 not used
                                                  null, // 43 not used
                                                  null, // 44 not used
                                                  null, // 45 not used
                                                  null, // 46 not used
                                                  null, // 47 not used
                                                  "inappropriateAuthentication",
                                                  "invalidCredentials",
                                                  "insufficientAccessRights",
                                                  "busy",
                                                  "unavailable",
                                                  "unwillingToPerform",
                                                  "loopDetect",
                                                  null, // 55 not used
                                                  null, // 56 not used
                                                  null, // 57 not used
                                                  null, // 58 not used
                                                  null, // 59 not used
                                                  null, // 60 not used
                                                  null, // 61 not used
                                                  null, // 62 not used
                                                  null, // 63 not used
                                                  "namingViolation",
                                                  "objectClassViolation",
                                                  "notAllowedOnNonLeaf",
                                                  "notAllowedOnRDN",
                                                  "entryAlreadyExists",
                                                  "objectClassModsProhibited",
                                                  null, // 70	reserved for CLADAP	
                                                  "affectsMultipleDSAs",
                                                  null, // 72 not used
                                                  null, // 73 not used
                                                  null, // 74 not used
                                                  null, // 75 not used
                                                  null, // 76 not used
                                                  null, // 77 not used
                                                  null, // 78 not used
                                                  null, // 79 not used
                                                  "other"
    };


    // resultCode contains:
    //    1. code (req.)
    //    2. descr (optional)
    private static final String ELEM_RC = "resultCode";
    private static final String ATTR_RC_CODE = "code";
    private static final String ATTR_RC_DESCR = "descr";
    private int m_resultCodeEC = -1;
    private String m_resultCodeMSG = null;

    // optional error message
    private static final String ELEM_ERRMSG = "errorMessage";
    private String m_errorMessage = null;

    // optional referral uri (0 or more occurrances)
    private static final String ELEM_REFERRAL = "referral";
    private Vector m_referrals = new Vector();

    // optional matchedDN
    private static final String ATTR_MATCHEDDN = "matchedDN";
    private String m_matchedDN = null;

    /** 
     * Constructor that sets the message type to msgType 
     * @param msgType the message type to use for this LdapResult
     * 
     */
    public LdapResult(int msgType)
    {
        super(msgType);
        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.BOTH, CLASS, "constructor(int)");

    } // end ctor LdapResult(int)
    /** 
     * Constructor that sets the message type to msgType and resultCode.
     * Some problems with differentiating the various resultCodes since
     * some NamingExceptions are used for multiple codes.
     * @param msgType the message type to use for this LdapResult
     * @param e the NamingException used to set the resultCode
     * 
     */
    public LdapResult(int msgType, NamingException e)
    {
        super(msgType);
        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.ENTER, CLASS, "constructor(int, NamingException)");
        int code = 80;

        if (e != null) {
            if (e instanceof CommunicationException) {
                code = 2;
            } else if (e instanceof TimeLimitExceededException) {
                code = 3;
            } else if (e instanceof SizeLimitExceededException) {
                code = 4;
            } else if (e instanceof AuthenticationNotSupportedException) {
                // same AuthenticationNotSupportedException applies to
                // LDAP code 7, 8, 13, 48
                // cannot differentiate
                // we are going to try to parse the error message and look
                // for the code number there
                StringTokenizer st = new StringTokenizer(e.getExplanation());
                boolean notFound = true;
                while (st.hasMoreTokens() && notFound) {
                    String str = st.nextToken();
                    if (str.equalsIgnoreCase("7")) {
                        code = 7;
                        notFound = false;
                    } else if (str.equalsIgnoreCase("8")) {
                        code = 8;
                        notFound = false;
                    } else if (str.equalsIgnoreCase("13")) {
                        code = 13;
                        notFound = false;
                    } else if (str.equalsIgnoreCase("48")) {
                        code = 48;
                        notFound = false;
                    }
                }
            } else if (e instanceof ReferralException) {
                code = 10;
            } else if (e instanceof LimitExceededException) {
                code = 11;
            } else if (e instanceof OperationNotSupportedException) {
                // cannot differentiate LDAP code 12, 53
                // we are going to try to parse the error message and look
                // for the code number there
                StringTokenizer st = new StringTokenizer(e.getExplanation());
                boolean notFound = true;
                while (st.hasMoreTokens() && notFound) {
                    String str = st.nextToken();
                    if (str.equalsIgnoreCase("12")) {
                        code = 12;
                        notFound = false;
                    } else if (str.equalsIgnoreCase("53")) {
                        code = 53;
                        notFound = false;
                    }
                }
            } else if (e instanceof NoSuchAttributeException) {
                code = 16;
            } else if (e instanceof InvalidAttributeIdentifierException) {
                code = 17;
            } else if (e instanceof InvalidSearchFilterException) {
                code = 18;
            } else if (e instanceof InvalidAttributeValueException) {
                // cannot differentiate LDAP code 19, 21
                // we are going to try to parse the error message and look
                // for the code number there
                StringTokenizer st = new StringTokenizer(e.getExplanation());
                boolean notFound = true;
                while (st.hasMoreTokens() && notFound) {
                    String str = st.nextToken();
                    if (str.equalsIgnoreCase("19")) {
                        code = 19;
                        notFound = false;
                    } else if (str.equalsIgnoreCase("21")) {
                        code = 21;
                        notFound = false;
                    }
                }
            } else if (e instanceof AttributeInUseException) {
                code = 20;
            } else if (e instanceof NameNotFoundException) {
                code = 32;
            } else if (e instanceof AuthenticationException) {
                code = 49;
            } else if (e instanceof NoPermissionException) {
                code = 50;
            } else if (e instanceof ServiceUnavailableException) {
                // cannot differentiate LDAP code 51, 52
                // we are going to try to parse the error message and look
                // for the code number there
                StringTokenizer st = new StringTokenizer(e.getExplanation());
                boolean notFound = true;
                while (st.hasMoreTokens() && notFound) {
                    String str = st.nextToken();
                    if (str.equalsIgnoreCase("51")) {
                        code = 51;
                        notFound = false;
                    } else if (str.equalsIgnoreCase("52")) {
                        code = 52;
                        notFound = false;
                    }
                }
            } else if (e instanceof InvalidNameException) {
                code = 64;
            } else if (e instanceof SchemaViolationException) {
                // cannot differentiate LDAP code 65, 67, 69
                // we are going to try to parse the error message and look
                // for the code number there
                StringTokenizer st = new StringTokenizer(e.getExplanation());
                boolean notFound = true;
                while (st.hasMoreTokens() && notFound) {
                    String str = st.nextToken();
                    if (str.equalsIgnoreCase("65")) {
                        code = 65;
                        notFound = false;
                    } else if (str.equalsIgnoreCase("67")) {
                        code = 67;
                        notFound = false;
                    } else if (str.equalsIgnoreCase("69")) {
                        code = 69;
                        notFound = false;
                    }
                }
            } else if (e instanceof ContextNotEmptyException) {
                code = 66;
            } else if (e instanceof NameAlreadyBoundException) {
                code = 68;
            } else {
                // cannot differentiate LDAP code 1, 36, 54, 71, 80
                // we are going to try to parse the error message and look
                // for the code number there
                StringTokenizer st = new StringTokenizer(e.getExplanation());
                boolean notFound = true;
                while (st.hasMoreTokens() && notFound) {
                    String str = st.nextToken();
                    if (str.equalsIgnoreCase("1")) {
                        code = 1;
                        notFound = false;
                    } else if (str.equalsIgnoreCase("36")) {
                        code = 36;
                        notFound = false;
                    } else if (str.equalsIgnoreCase("54")) {
                        code = 54;
                        notFound = false;
                    } else if (str.equalsIgnoreCase("71")) {
                        code = 71;
                        notFound = false;
                    } else if (str.equalsIgnoreCase("80")) {
                        code = 80;
                        notFound = false;
                    }
                }
            }

            setErrorMessage(e.getMessage());
        }

        setResultCodeEC(code);
        setResultCodeMSG(ERROR_MSGS[code]);
        //System.out.println(DSMLMessages.getMessage(DSMLMessageKeys.LDAPRESULT_EXC_CLASS_NAME) + e.toString());

        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "constructor(int, NamingException)");
    } // end ctor LdapResult(int, NamingException)
    /** 
     * Constructor that takes a message type and an Element  
     * and populates the variables.
     * @param msgType the message type
     * @param elem the Element to be deconstructed
     * @exception DsmlIllegalArgumentException if Element contains an illegal value
     * 
     */
    public LdapResult(int msgType, Element elem) throws DsmlIllegalArgumentException
    {
        super(msgType, elem);
        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.ENTER, CLASS, "constructor(int, Element)");

        Element elemTmp = null;

        // only one required ResultCode
        NodeList nodes = elem.getElementsByTagName(ELEM_RC);
        if ((nodes != null) && (nodes.getLength() != 0)) {
            elemTmp = (Element) nodes.item(0);
            setResultCodeEC(elemTmp.getAttribute(ATTR_RC_CODE));
            setResultCodeMSG(elemTmp.getAttribute(ATTR_RC_DESCR));
        }


        // optional errorMessage
        nodes = elem.getElementsByTagName(ELEM_ERRMSG);
        if ((nodes != null) && (nodes.getLength() > 0)) {
            // only one error message element
            elemTmp = (Element) nodes.item(0);
            elemTmp.normalize();
            setErrorMessage(elemTmp.getFirstChild().getNodeValue());
        }

        nodes = elem.getElementsByTagName(ELEM_REFERRAL);
        if (nodes != null) {
            int num = nodes.getLength();        
            for (int i=0; i<num; i++) {
                elemTmp = (Element) nodes.item(i);
                elemTmp.normalize();
                setReferral(elemTmp.getFirstChild().getNodeValue());
            }
        }

        setMatchedDN(elem.getAttribute(ATTR_MATCHEDDN));

        // check to see all required elements and attributes are there
        if (!isClassRequiredSet()) {
            if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "constructor(int, Element)", "Exception)");
            throw new DsmlIllegalArgumentException(DSMLMessageKeys.LDAPRESULT_NOT_REQUIRED_SET);
        }

        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "constructor(int, Element)");
    } // end ctor LdapResult(int, Element)
    /**
     * Gets the optional LDAPResult error message.
     * @return errorMessage optional error message
     * 
     */
    public String getErrorMessage()
    {
        return m_errorMessage;

    } // end getErrorMessage()
    /**
     * Gets the optional matchedDN.
     * @return matchedDN
     * 
     */
    public String getMatchedDN()
    {
        return m_matchedDN;

    } // end getmatchedDN()
    /**
     * Gets Vector containing all referral URIs.
     * @return Vector of referral URIs
     * 
     */
    public Vector getReferrals()
    {
        return m_referrals;

    } // end getReferrals()
    /**
     * Gets the required resultCode error code.
     * @return resultCode error code
     * 
     */
    public int getResultCodeEC()
    {
        return m_resultCodeEC;

    } // end getresultCodeEC()
    /**
     * Gets the optional resultCode error message.
     * @return resultCode error message
     * 
     */
    public String getResultCodeMSG()
    {
        return m_resultCodeMSG;

    } // end getresultCodeMSG()
    /**
     * Checks to see whether this class's required elements are set.
     * @return true/false
     * 
     */
    private boolean isClassRequiredSet()
    {
        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.ENTER, CLASS, "isClassRequiredSet()");
        boolean allset = true;

//        if ((m_resultCodeEC < 0) || (m_resultCodeEC >= ERROR_MSGS.length)) {
//            allset = false;
//        }

        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "isClassRequiredSet()", "returning " + allset);
        return allset;
    } // end isClassRequiredSet()
    /**
     * Checks to see whether all required elements are set.
     * @return true/false
     * 
     */
    public boolean isRequiredSet()
    {
        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.ENTER, CLASS, "isRequiredSet()");
        boolean allset = super.isRequiredSet();

        if (!allset || !isClassRequiredSet()) {
            allset = false;
        }

        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "isRequiredSet()", "returning " + allset);
        return allset;
    } // end isRequiredSet()
    /**
     * Sets the optional LDAPResult error message.
     * @param errorMessage optional error message
     * @return true if set, false otherwise
     * 
     */
    public boolean setErrorMessage(String errorMsg)
    {
        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.ENTER, CLASS, "setErrorMessage(String)");
        if ((errorMsg != null) && (errorMsg.length() > 0)) {
            m_errorMessage = errorMsg;
            if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "setErrorMessage(String)", "returning true");
            return true;
        }

        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "setErrorMessage(String)", "returning false");
        return false;

    } // end setErrorMessage(String)
    /**
     * Sets the optional matchedDN.
     * @param matchedDN optional matchedDN
     * @return true if set, false otherwise
     * 
     */
    public boolean setMatchedDN(String matchedDN)
    {
        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.ENTER, CLASS, "setMatchedDN(String)");
        if ((matchedDN != null) && (matchedDN.length() > 0)) {
            m_matchedDN = matchedDN;
            if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "setMatchedDN(String)", "returning true");
            return true;
        }

        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "setMatchedDN(String)", "returning false");
        return false;

    } // end setmatchedDN(String)
    /**
     * Adds referral URI to Vector containing all referrals.
     * @param referral referral URI to be added.
     * @return true if set, false otherwise
     * 
     */
    public boolean setReferral(String uri)
    {
        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.ENTER, CLASS, "setReferral(String)");
        if ((uri != null) && (uri.length() > 0)) {
            boolean returning = m_referrals.add(uri);
            if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "setReferral(String)", "returning " + returning);
            return returning;
        }

        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "setReferral(String)", "returning false");
        return false;

    } // end setReferral(String)
    /**
     * Sets the required resultCode error code and the corresponding
     * resultCode error message.
     * @param resultCodeEC resultCode error code
     * @return true if set, false otherwise
     * 
     */
    public boolean setResultCode(int resultCodeEC)
    {
        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.ENTER, CLASS, "setResultCodeCode(int)");
        if (setResultCodeEC(resultCodeEC)) {
            if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "setResultCodeCode(int)");
            return setResultCodeMSG(ERROR_MSGS[resultCodeEC]);
        }

        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "setResultCode(int)", "returning false");
        return false;

    } // end setResultCode(int)
    /**
     * Sets the required resultCode error code.
     * @param resultCodeEC resultCode error code
     * @return true if set, false otherwise
     * 
     */
    public boolean setResultCodeEC(int resultCodeEC)
    {
        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.ENTER, CLASS, "setResultCodeEC(int)");
        m_resultCodeEC = resultCodeEC;
        if ((resultCodeEC >= 0) && (resultCodeEC < ERROR_MSGS.length)) {
            if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "setResultCodeEC(int)", "returning true");
            return true;
        }

        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "setResultCodeEC(int)", "returning false");
        return false;

    } // end setResultCodeEC(int)
    /**
     * Sets the required resultCode error code.
     * @param resultCodeEC resultCode error code as String
     * @return true if set, false otherwise
     * 
     */
    public boolean setResultCodeEC(String resultCodeEC)
    {
        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.ENTER, CLASS, "setResultCodeEC(String)");
        if ((resultCodeEC != null ) && (resultCodeEC.length() > 0)) {
            boolean returning = setResultCodeEC(Integer.parseInt(resultCodeEC));
            if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "setResultCodeEC(String)", "returning " + returning);
            return returning;
        }

        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "setResultCodeEC(String)", "returning false");
        return false;

    } // end setResultCodeEC(String)
    /**
     * Sets the optional resultCode error message.
     * @param resultCodeEC resultCode error message
     * @return true if set, false otherwise
     * 
     */
    public boolean setResultCodeMSG(String resultCodeMsg)
    {
        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.ENTER, CLASS, "setResultCodeMsg(String)");
        if ((resultCodeMsg != null) && (resultCodeMsg.length() > 0)) {
            m_resultCodeMSG = resultCodeMsg;
            if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "setResultCodeMsg(String)", "returning true");
            return true;
        }

        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "setResultCodeMSG(String)", "returning false");
        return false;

    } // end setResultCodeMSG(String)
    /**
     * Create an Element node.
     * @param doc Document used to create the Element
     * @return the Element created
     * 
     */
    public Element toElement(Document doc)
    {
        // first check that all required elements and attributes
        // are set.
        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.ENTER, CLASS, "toElement(Document)");
        if (!isRequiredSet()) {

            if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "toElement(Document)", "returning null");
            return null;
        }

        // send to super class for it to create the Element node
        Element elem = super.toElement(doc);

        // create an element for the ResultCode
        // attach attributes code and descr (if set)
        Element elemTmp = doc.createElement(ELEM_RC);
        elemTmp.setAttribute(ATTR_RC_CODE, Integer.toString(m_resultCodeEC));
        if (m_resultCodeMSG != null) {
            elemTmp.setAttribute(ATTR_RC_DESCR, m_resultCodeMSG);
        }
        elem.appendChild(elemTmp);

        // create element for errorMessage if exists
        if (m_errorMessage != null) {
            elemTmp = doc.createElement(ELEM_ERRMSG);
            Text text = doc.createTextNode(m_errorMessage);
            elemTmp.appendChild(text);
            elem.appendChild(elemTmp);          
        }

        // loop through the referrals, create an Element node for each
        // and add to elem
        for (Enumeration e = m_referrals.elements(); e.hasMoreElements();) {
            elemTmp = doc.createElement(ELEM_REFERRAL);
            Text text = doc.createTextNode((String) e.nextElement());
            elemTmp.appendChild(text);
            elem.appendChild(elemTmp);
        }

        // if attribute matchedDN exists, set it
        if (m_matchedDN != null) {
            elem.setAttribute(ATTR_MATCHEDDN, m_matchedDN);
        }

        if (DSMLTrace.isDiagnosticTraceEnabled()) DSMLTrace.logDiagnostic(DSMLTrace.EXIT, CLASS, "toElement(Document)");
        return elem;
    } // end toElement(Document, String)
} // end class LdapResult
