/* START OF SPECIFICATIONS **************************************** */          
/*                                                                  */          
/*01* MACRO NAME: ATBCMC                                            */          
/*                                                                  */          
/*01* DESCRIPTIVE NAME: Interface Declaration File for CPI-C        */          
/*                      Protocol Boundary Interface - C             */          
/*                                                                  */          
/*01* COMPONENT: APPC Component (SCACB)                             */          
/*                                                                  */          
/*01* PROPRIETARY STATEMENT=                                        */          
/***PROPRIETARY_STATEMENT********************************************/          
/*                                                                  */          
/*                                                                  */          
/* LICENSED MATERIALS - PROPERTY OF IBM                             */          
/* THIS MACRO IS "RESTRICTED MATERIALS OF IBM"                      */          
/* 5647-A01 (C) COPYRIGHT IBM CORP. 1998                            */          
/*                                                                  */          
/* STATUS= HBB6606                                                  */          
/*                                                                  */          
/* EXTERNAL CLASSIFICATION:  GUPI                                   */          
/*                                                                  */          
/* END OF EXTERNAL CLASSIFICATION                                   */          
/*                                                                  */          
/***END_OF_PROPRIETARY_STATEMENT*************************************/          
/*                                                                  */          
/*01* FUNCTION:                                                     */          
/*02*  ATBCMC contains the C language declarations for the          */          
/*     parameter values and entry points of the CPI Communications  */          
/*     Protocol Boundary Interface services.                        */          
/*                                                                  */          
/*01* METHOD OF ACCESS:                                             */          
/*02*  #include <atbcmc.h> or #include <cmc.h> depending on         */          
/*     installation, See Writing Transaction Programs for APPC/MVS  */          
/*     GC28-1121 for installation suggestions.                      */          
/*                                                                  */          
/*01* DISTRIBUTION LIBRARY: AIEAHDR                                 */          
/*                                                                  */          
/*01* NOTES:                                                        */          
/*02*  APPC/MVS protocol boundary interface services require        */          
/*     standard linkage conventions.  The #pragma preprocessor      */          
/*     directive of IBM C is used to designate that these           */          
/*     functions should be invoked using OS linkage conventions.    */          
/*                                                                  */          
/*01* CHANGE-ACTIVITY:                                              */          
/*                                                                  */          
/* Flag LineItem  FMID    DATE  ID  Comment                         */          
/*  $L0=APPC1VS  HBB4420 891211 PDFP: Advanced Program-to-Program   */          
/*      communication for MVS/ESA, initial release.                 */          
/*  $D1=DH30068  HBB4420 900226 PDB1: New return codes              */          
/*  $P1=PKI0217  JBB4422 910510 PDFP: Corrected name of CMEPLN in   */          
/*                                   #PRAGMA statement.             */          
/*  $02=OY45436  JBB4422 910730 PDA7: Supporting the Pseudonym file */          
/*      declarations both in upper and lower case since "C" language*/          
/*      is case sensitive, Removed sequence numbers in              */          
/*      columns 73-80                                               */          
/*  $03=OY48120  JBB4422 911014 PDA7: Defining the constants CM_    */          
/*      SYNC_LVL_NOT_SUPPORTED_PGM and CM_TPN_NOT_RECOGNIZED in     */          
/*      decimal representation.                                     */          
/*  $P2=PKB0817  HBB4430 920729 PDI8: Support of CMC the C          */          
/*      Pseudonym File, SAA CPIC Reference SC26-4399-04, Fifth      */          
/*      Edition. Note this is a replacement of the JBB4422 part.    */          
/*      It contains APPC/MVS uppercase entry point support as well  */          
/*      as the CPIC lowercase support.                              */          
/*  $L1=APPCP    HBB6603 960105 PDE6: APPC/MVS PC Support.          */          
/*  $P3=PQC1198  HBB6603 960930 PDE4: Apply updates using CMC,      */          
/*      the latest definition file distributed on the CPIC diskette.*/          
/*  $04=OW26860  HBB6603 970613 PDB7: Comment out entries that      */          
/*      APPC/MVS does not support.                                  */          
/*      (TRSQ PTM PU70109)                                          */          
/*                                                                  */          
/*Note: When replacing the contents of this file, be sure to        */          
/*      add the following statements:                               */          
/*        #define CM_MVS                                            */          
/*        #pragma nomargins nosequence                              */          
/*                                                                  */          
/** END OF SPECIFICATIONS  ******************************************/          
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *          
 *                                                                   *          
 *       CPI Communications Reference Pseudonyms -- SC26-4399        *          
 *                                                                   *          
 * Copyright:                                                        *          
 *       (C) Copyright IBM Corp 1995                                 *          
 *       All Rights Reserved                                         *          
 *                                                                   *          
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *          
 * NOTE: This header file contains function prototypes for CPI-C calls          
 *       defined in the CPI Communications 2.1 Architecture.                    
 *       Each product using this header file does not necessarily               
 *       support all of these calls or only some of the parameters              
 *       on each call.  To determine which calls are supported for              
 *       the CPI Communications product you are using, refer to product         
 *       documentation for that particular product.                             
 *                                                                              
 * NOTE:                                                                        
 * Before you use this file, you must set a supported operating                 
 * system constant.  Search on SYSTEM to find the list of                       
 * supported constants and how to set the appropriate one.                      
 *                                                                              
 */                                                                             
                                                                                
                                                                                
/*                                                                              
 * May 11, 1994                                                                 
 *                                                                              
 * This file is organized as follows:                                           
 *  - product specific preprocessor directives that must be                     
 *    before all other declarations                                             
 *  - Base CPI-C constants and type definitions                                 
 *  - Base CPI-C function prototypes                                            
 *  - Product specific constants and type definitions (enclosed                 
 *    in #if/#endif)                                                            
 *  - Product specific function prototypes (enclosed in                         
 *    #if/#endif)                                                               
 */                                                                             
                                                                                
#ifndef _cpic_h                                                                 
#define _cpic_h                                                                 
                                                                                
/*                                                                              
 * The following system values are handled by this file:                        
 *    CM_AIX                                                                    
 *    CM_DOS                                                                    
 *    CM_MVS                                                                    
 *    CM_OS2                                                                    
 *    CM_OS400                                                                  
 *    CM_VM                                                                     
 *                                                                              
 * This is necessary for the proper setting of                                  
 * CM_ENTRY and CM_PTR below.                                                   
 */                                                                             
                                                                                
                                                                                
#if ! (defined(CM_AIX) || defined(CM_DOS) || defined(CM_MVS) || \
       defined(CM_OS2) || defined(CM_OS400) || defined(CM_VM)   \
      )                                                                         
/*                                                                              
 * Define the system ONLY if no system has been defined by the                  
 * program.  This avoids duplicate macro definition warnings and                
 * allows this include file to be moved to other platforms more                 
 * easily.                                                                      
 *                                                                              
 * If you choose to move this include file to another platform,                 
 * we recommend that you define the CM_ constant for that                       
 * platform externally on the compiler command line.                            
 * Also note that the services not supported by APPC/MVS have been              
 * commented out, therefore if these services are required by the               
 * other platform they need to be uncommented.                                  
 *                                                                              
 * When this file is shipped with a platform product, the                       
 * following line should be changed to match the platform CM_                   
 * constant.  For example, if this file were shipping on MVS/ESA,               
 * the following line would be changed to "#define CM_MVS"                      
 */                                                                             
                                                                                
#define CM_MVS                                                                  
#pragma nomargins nosequence                                                    
                                                                                
#endif                                                                          
                                                                                
                                                                                
/*                                                                              
 * CPI Communications Enumerated Constants                                      
 */                                                                             
                                                                                
/*                                                                              
 * CM_INT32 should be a 32-bit, signed integer.  The following                  
 * #define is system dependent and may need to be changed on                    
 * systems where signed long int does not define a 32-bit, signed               
 * integer.                                                                     
 */                                                                             
                                                                                
#define CM_INT32 signed long int                                                
                                                                                
                                                                                
#if   defined( CM_OS2 )                                                         
                                                                                
#   if (__IBMC__ >= 100 || __IBMCPP__ >=200)                                    
                                                                                
   /* __IBMC__   >= 100    indicates  C Set/2 or C Set++   */                   
   /* __IBMCPP__ >= 200    indicates  C Set++              */                   
                                                                                
#   define CM_ENTRY extern void _System                                         
#   define CM_PTR   *                                                           
                                                                                
#   elif defined(__BORLANDC__)                                                  
                                                                                
   /* Borland C++ for OS/2 */                                                   
#     define CM_ENTRY extern void __syscall                                     
#     define CM_PTR *                                                           
                                                                                
#   elif (_MSC_VER >= 600) || __IBMC__                                          
                                                                                
   /* Microsoft C 6.0 or IBM C/2 */                                             
#     define CM_ENTRY extern void pascal far _loadds                            
#     define CM_PTR far *                                                       
                                                                                
#   else                                                                        
                                                                                
/*                                                                              
 * If we have gotten this far, a supported compiler has not been                
 * recognized.  We will produce a syntax error on the next                      
 * line that should help the user identify the problem.                         
 */                                                                             
INTENTIONAL_SYNTAX_ERROR cpic_does_not_recognize_compiler();                    
                                                                                
#   endif                                                                       
                                                                                
#elif defined( CM_DOS)                                                          
                                                                                
/* Microsoft C 6.0 and IBM C/2 */                                               
#     define CM_ENTRY extern void pascal far _loadds                            
#     define CM_PTR far *                                                       
                                                                                
#elif defined( CM_MVS ) || defined( CM_OS400 ) || \                             
      defined( CM_VM ) || defined( CM_AIX )                                     
                                                                                
#     define CM_ENTRY extern void                                               
#     define CM_PTR *                                                           
                                                                                
                                                                                
#else                                                                           
                                                                                
/*                                                                              
 * If we have gotten this far, a supported system constant has                  
 * not been defined.  We will produce a syntax error on the next                
 * line that should help the user identify the problem.                         
 *                                                                              
 * To correct this problem, a system constant should be defined                 
 * either above in this file, or externally with a compiler                     
 * option.                                                                      
 */                                                                             
INTENTIONAL_SYNTAX_ERROR define_a_system_in_cpic_h();                           
                                                                                
#endif                                                                          
                                                                                
                                                                                
/*                                                                              
 * These macros allow you to write programs that are easier to read, since      
 * you can use the full name of the CPI-C call rather than its 6 character      
 * entry point.                                                                 
 *                                                                              
 * When porting code that uses these macros, you will have to ensure that       
 * the macros are defined on the target platform.                               
 */                                                                             
                                                                                
#ifdef READABLE_MACROS                                                          
#define Accept_Conversation                   cmaccp                            
#define Accept_Incoming                       cmacci                            
#define Allocate                              cmallc                            
#define Cancel_Conversation                   cmcanc                            
#define Confirm                               cmcfm                             
#define Confirmed                             cmcfmd                            
#define Convert_Incoming                      cmcnvi                            
#define Convert_Outgoing                      cmcnvo                            
#define Deallocate                            cmdeal                            
#define Deferred_Deallocate                   cmdfde                            
#define Extract_AE_Qualifier                  cmeaeq                            
#define Extract_AP_Title                      cmeapt                            
#define Extract_Application_Context_Name      cmeacn                            
#define Extract_Conversation_Context          cmectx                            
#define Extract_Conversation_State            cmecs                             
#define Extract_Conversation_Type             cmect                             
#define Extract_Initialization_Data           cmeid                             
#define Extract_Mapped_Initialization_Data    cmemid                            
#define Extract_Maximum_Buffer_Size           cmembs                            
#define Extract_Mode_Name                     cmemn                             
#define Extract_Partner_ID                    cmepid                            
#define Extract_Partner_LU_Name               cmepln                            
#define Extract_Secondary_Information         cmesi                             
#define Extract_Security_User_ID              cmesui                            
#define Extract_Send_Receive_Mode             cmesrm                            
#define Extract_Sync_Level                    cmesl                             
#define Extract_Transaction_Control           cmetc                             
#define Extract_TP_Name                       cmetpn                            
#define Flush                                 cmflus                            
#define Include_Partner_In_Transaction        cmincl                            
#define Initialize_Conversation               cminit                            
#define Initialize_For_Incoming               cminic                            
#define Prepare                               cmprep                            
#define Prepare_To_Receive                    cmptr                             
#define Receive                               cmrcv                             
#define Receive_Expedited_Data                cmrcvx                            
#define Receive_Mapped_Data                   cmrcvm                            
#define Release_Local_TP_Name                 cmrltp                            
#define Request_To_Send                       cmrts                             
#define Send_Data                             cmsend                            
#define Send_Error                            cmserr                            
#define Send_Expedited_Data                   cmsndx                            
#define Send_Mapped_Data                      cmsndm                            
#define Set_AE_Qualifier                      cmsaeq                            
#define Set_Allocate_Confirm                  cmsac                             
#define Set_AP_Title                          cmsapt                            
#define Set_Application_Context_Name          cmsacn                            
#define Set_Begin_Transaction                 cmsbt                             
#define Set_Confirmation_Urgency              cmscu                             
#define Set_Conversation_Security_Password    cmscsp                            
#define Set_Conversation_Security_Type        cmscst                            
#define Set_Conversation_Security_User_ID     cmscsu                            
#define Set_Conversation_Type                 cmsct                             
#define Set_Deallocate_Type                   cmsdt                             
#define Set_Error_Direction                   cmsed                             
#define Set_Fill                              cmsf                              
#define Set_Initialization_Data               cmsid                             
#define Set_Join_Transaction                  cmsjt                             
#define Set_Log_Data                          cmsld                             
#define Set_Mapped_Initialization_Data        cmsmid                            
#define Set_Mode_Name                         cmsmn                             
#define Set_Partner_ID                        cmspid                            
#define Set_Partner_LU_Name                   cmspln                            
#define Set_Prepare_Data_Permitted            cmspdp                            
#define Set_Prepare_To_Receive_Type           cmsptr                            
#define Set_Processing_Mode                   cmspm                             
#define Set_Queue_Callback_Function           cmsqcf                            
#define Set_Queue_Processing_Mode             cmsqpm                            
#define Set_Receive_Type                      cmsrt                             
#define Set_Return_Control                    cmsrc                             
#define Set_Send_Receive_Mode                 cmssrm                            
#define Set_Send_Type                         cmsst                             
#define Set_Sync_Level                        cmssl                             
#define Set_TP_Name                           cmstpn                            
#define Set_Transaction_Control               cmstc                             
#define Specify_Local_TP_Name                 cmsltp                            
#define Test_Request_To_Send_Received         cmtrts                            
#define Wait_For_Completion                   cmwcmp                            
#define Wait_For_Conversation                 cmwait                            
                                                                                
#endif                                                                          
                                                                                
                                                                                
                                                                                
                                                                                
#if defined(CM_OS400) || defined(CM_MVS)                                        
#define cmacci CMACCI                                                           
#define cmaccp CMACCP                                                           
#define cmallc CMALLC                                                           
#define cmcanc CMCANC                                                           
#define cmcfm  CMCFM                                                            
#define cmcfmd CMCFMD                                                           
#define cmcnvi CMCNVI                                                           
#define cmcnvo CMCNVO                                                           
#define cmdeal CMDEAL                                                           
#define cmdfde CMDFDE                                                           
#define cmeaeq CMEAEQ                                                           
#define cmeapt CMEAPT                                                           
#define cmeacn CMEACN                                                           
#define cmecs  CMECS                                                            
#define cmect  CMECT                                                            
#define cmectx CMECTX                                                           
#define cmeid  CMEID                                                            
#define cmembs CMEMBS                                                           
#define cmemid CMEMID                                                           
#define cmemn  CMEMN                                                            
#define cmepid CMEPID                                                           
#define cmepln CMEPLN                                                           
#define cmesi  CMESI                                                            
#define cmesl  CMESL                                                            
#define cmesrm CMESRM                                                           
#define cmesui CMESUI                                                           
#define cmetpn CMETPN                                                           
#define cmetc  CMETC                                                            
#define cmflus CMFLUS                                                           
#define cmincl CMINCL                                                           
#define cminic CMINIC                                                           
#define cminit CMINIT                                                           
#define cmprep CMPREP                                                           
#define cmptr  CMPTR                                                            
#define cmrcv  CMRCV                                                            
#define cmrcvm CMRCVM                                                           
#define cmrcvx CMRCVX                                                           
#define cmrltp CMRLTP                                                           
#define cmrts  CMRTS                                                            
#define cmsac  CMSAC                                                            
#define cmsacn CMSACN                                                           
#define cmsaeq CMSAEQ                                                           
#define cmsapt CMSAPT                                                           
#define cmsbt  CMSBT                                                            
#define cmscsp CMSCSP                                                           
#define cmscst CMSCST                                                           
#define cmscsu CMSCSU                                                           
#define cmsct  CMSCT                                                            
#define cmscu  CMSCU                                                            
#define cmsdt  CMSDT                                                            
#define cmsed  CMSED                                                            
#define cmsend CMSEND                                                           
#define cmserr CMSERR                                                           
#define cmsf   CMSF                                                             
#define cmsid  CMSID                                                            
#define cmsjt  CMSJT                                                            
#define cmsld  CMSLD                                                            
#define cmsltp CMSLTP                                                           
#define cmsmid CMSMID                                                           
#define cmsmn  CMSMN                                                            
#define cmsndm CMSNDM                                                           
#define cmsndx CMSNDX                                                           
#define cmspdp CMSPDP                                                           
#define cmspln CMSPLN                                                           
#define cmspid CMSPID                                                           
#define cmspm  CMSPM                                                            
#define cmsptr CMSPTR                                                           
#define cmsqcf CMSQCF                                                           
#define cmsqpm CMSQPM                                                           
#define cmsrc  CMSRC                                                            
#define cmsrt  CMSRT                                                            
#define cmssl  CMSSL                                                            
#define cmssrm CMSSRM                                                           
#define cmsst  CMSST                                                            
#define cmstc  CMSTC                                                            
#define cmstpn CMSTPN                                                           
#define cmtrts CMTRTS                                                           
#define cmwait CMWAIT                                                           
#define cmwcmp CMWCMP                                                           
#endif                                                                          
                                                                                
/*                                                                              
 *  - Base CPI-C constants and type definitions                                 
 */                                                                             
                                                                                
typedef CM_INT32 CM_AE_QUAL_OR_AP_TITLE_FORMAT;                                 
typedef CM_INT32 CM_ALLOCATE_CONFIRM_TYPE;                                      
typedef CM_INT32 CM_BEGIN_TRANSACTION;                                          
typedef CM_INT32 CM_BUFFER_LENGTH;                                              
typedef CM_INT32 CM_CALL_ID;                                                    
typedef CM_INT32 CM_COMPLETED_OP_COUNT;                                         
typedef CM_INT32 CM_CONFIRMATION_URGENCY;                                       
typedef CM_INT32 CM_CONTEXT_ID_LENGTH;                                          
typedef CM_INT32 CM_CONTROL_INFORMATION_RECEIVED;                               
typedef CM_INT32 CM_CONVERSATION_QUEUE;                                         
typedef CM_INT32 CM_CONVERSATION_RETURN_CODE;                                   
typedef CM_INT32 CM_CONVERSATION_SECURITY_TYPE;                                 
typedef CM_INT32 CM_CONVERSATION_STATE;                                         
typedef CM_INT32 CM_CONVERSATION_TYPE;                                          
typedef CM_INT32 CM_DATA_RECEIVED_TYPE;                                         
typedef CM_INT32 CM_DEALLOCATE_TYPE;                                            
typedef CM_INT32 CM_DIRECTORY_ENCODING;                                         
typedef CM_INT32 CM_DIRECTORY_SYNTAX;                                           
typedef CM_INT32 CM_ERROR_DIRECTION;                                            
typedef CM_INT32 CM_FILL;                                                       
typedef CM_INT32 CM_JOIN_TRANSACTION_TYPE;                                      
typedef CM_INT32 CM_MAP_NAME_LENGTH;                                            
typedef CM_INT32 CM_MAXIMUM_BUFFER_SIZE;                                        
typedef CM_INT32 CM_OOID;                                                       
typedef CM_INT32 CM_OOID_LIST_COUNT;                                            
typedef CM_INT32 CM_PARTNER_ID_SCOPE;                                           
typedef CM_INT32 CM_PARTNER_ID_TYPE;                                            
typedef CM_INT32 CM_PREPARE_DATA_PERMITTED_TYPE;                                
typedef CM_INT32 CM_PREPARE_TO_RECEIVE_TYPE;                                    
typedef CM_INT32 CM_PROCESSING_MODE;                                            
typedef CM_INT32 CM_RECEIVE_TYPE;                                               
typedef CM_CONTROL_INFORMATION_RECEIVED CM_REQUEST_TO_SEND_RECEIVED;            
typedef CM_INT32 CM_RETURN_CODE;                                                
typedef CM_INT32 CM_RETURN_CONTROL;                                             
typedef unsigned char SECURITY_PASSWORD[10];                                    
typedef CM_INT32 CM_SECURITY_PASSWORD_LENGTH;                                   
typedef unsigned char SECURITY_USER_ID[10];                                     
typedef CM_INT32 CM_SECURITY_USER_ID_LENGTH;                                    
typedef CM_INT32 CM_SEND_RECEIVE_MODE;                                          
typedef CM_INT32 CM_SEND_TYPE;                                                  
typedef CM_INT32 CM_STATUS_RECEIVED;                                            
typedef CM_INT32 CM_SYNC_LEVEL;                                                 
typedef CM_INT32 CM_TIMEOUT;                                                    
typedef CM_INT32 CM_TRANSACTION_CONTROL;                                        
                                                                                
/* X/open typedefs for compatibilty  */                                         
#if defined(XOPEN_COMPATIBLE)                                                   
                                                                                
typedef unsigned char CONVERSATION_ID [8];                                      
typedef CM_INT32 CM_RETCODE;                                                    
typedef CM_INT32 CONVERSATION_SECURITY_TYPE;                                    
typedef CM_INT32 CONVERSATION_TYPE;                                             
typedef CM_INT32 DATA_RECEIVED;                                                 
typedef CM_INT32 DEALLOCATE_TYPE;                                               
typedef CM_INT32 ERROR_DIRECTION;                                               
typedef CM_INT32 PREPARE_TO_RECEIVE_TYPE;                                       
typedef CM_INT32 PROCESSING_MODE;                                               
typedef CM_INT32 RECEIVE_TYPE;                                                  
typedef CM_INT32 REQUEST_TO_SEND_RECEIVED;                                      
typedef CM_INT32 RETURN_CONTROL;                                                
typedef CM_INT32 SEND_TYPE;                                                     
typedef CM_INT32 STATUS_RECEIVED;                                               
typedef CM_INT32 SYNC_LEVEL;                                                    
                                                                                
#endif                                                                          
                                                                                
/*                                                                              
 * Enumerated data types (enum) have not been used for the                      
 * constant values because the default type for an enum                         
 * is 'int'.  This causes type conflicts on compilers where                     
 * int is not the same size as CM_INT32.                                        
 */                                                                             
                                                                                
/*  AE_qual_or_AP_title_format values, used for                                 
    AE_qualifier_format and AP_title_format parameters */                       
                                                                                
#define CM_DN                            (CM_AE_QUAL_OR_AP_TITLE_FORMAT) 0      
#define CM_OID                           (CM_AE_QUAL_OR_AP_TITLE_FORMAT) 1      
#define CM_INT_DIGITS                    (CM_AE_QUAL_OR_AP_TITLE_FORMAT) 2      
                                                                                
                                                                                
/*  allocate_confirm values  */                                                 
                                                                                
#define CM_ALLOCATE_NO_CONFIRM           (CM_ALLOCATE_CONFIRM_TYPE) 0           
#define CM_ALLOCATE_CONFIRM              (CM_ALLOCATE_CONFIRM_TYPE) 1           
                                                                                
                                                                                
/*  begin_transaction values  */                                                
                                                                                
#define CM_BEGIN_IMPLICIT                (CM_BEGIN_TRANSACTION) 0               
#define CM_BEGIN_EXPLICIT                (CM_BEGIN_TRANSACTION) 1               
                                                                                
                                                                                
/*  call_ID values  */                                                          
                                                                                
#define CM_CMACCI                        (CM_CALL_ID) 1                         
#define CM_CMACCP                        (CM_CALL_ID) 2                         
#define CM_CMALLC                        (CM_CALL_ID) 3                         
#define CM_CMCANC                        (CM_CALL_ID) 4                         
#define CM_CMCFM                         (CM_CALL_ID) 5                         
#define CM_CMCFMD                        (CM_CALL_ID) 6                         
#define CM_CMCNVI                        (CM_CALL_ID) 7                         
#define CM_CMCNVO                        (CM_CALL_ID) 8                         
#define CM_CMDEAL                        (CM_CALL_ID) 9                         
#define CM_CMDFDE                        (CM_CALL_ID) 10                        
#define CM_CMEACN                        (CM_CALL_ID) 11                        
#define CM_CMEAEQ                        (CM_CALL_ID) 12                        
#define CM_CMEAPT                        (CM_CALL_ID) 13                        
#define CM_CMECS                         (CM_CALL_ID) 14                        
#define CM_CMECT                         (CM_CALL_ID) 15                        
#define CM_CMECTX                        (CM_CALL_ID) 16                        
#define CM_CMEID                         (CM_CALL_ID) 17                        
#define CM_CMEMBS                        (CM_CALL_ID) 18                        
#define CM_CMEMN                         (CM_CALL_ID) 19                        
#define CM_CMEPID                        (CM_CALL_ID) 20                        
#define CM_CMEPLN                        (CM_CALL_ID) 21                        
#define CM_CMESI                         (CM_CALL_ID) 22                        
#define CM_CMESL                         (CM_CALL_ID) 23                        
#define CM_CMESRM                        (CM_CALL_ID) 24                        
#define CM_CMESUI                        (CM_CALL_ID) 25                        
#define CM_CMETC                         (CM_CALL_ID) 26                        
#define CM_CMETPN                        (CM_CALL_ID) 27                        
#define CM_CMFLUS                        (CM_CALL_ID) 28                        
#define CM_CMINCL                        (CM_CALL_ID) 29                        
#define CM_CMINIC                        (CM_CALL_ID) 30                        
#define CM_CMINIT                        (CM_CALL_ID) 31                        
#define CM_CMPREP                        (CM_CALL_ID) 32                        
#define CM_CMPTR                         (CM_CALL_ID) 33                        
#define CM_CMRCV                         (CM_CALL_ID) 34                        
#define CM_CMRCVX                        (CM_CALL_ID) 35                        
#define CM_CMRLTP                        (CM_CALL_ID) 36                        
#define CM_CMRTS                         (CM_CALL_ID) 37                        
#define CM_CMSAC                         (CM_CALL_ID) 38                        
#define CM_CMSACN                        (CM_CALL_ID) 39                        
#define CM_CMSAEQ                        (CM_CALL_ID) 40                        
#define CM_CMSAPT                        (CM_CALL_ID) 41                        
#define CM_CMSBT                         (CM_CALL_ID) 42                        
#define CM_CMSCSP                        (CM_CALL_ID) 43                        
#define CM_CMSCST                        (CM_CALL_ID) 44                        
#define CM_CMSCSU                        (CM_CALL_ID) 45                        
#define CM_CMSCT                         (CM_CALL_ID) 46                        
#define CM_CMSCU                         (CM_CALL_ID) 47                        
#define CM_CMSDT                         (CM_CALL_ID) 48                        
#define CM_CMSED                         (CM_CALL_ID) 49                        
#define CM_CMSEND                        (CM_CALL_ID) 50                        
#define CM_CMSERR                        (CM_CALL_ID) 51                        
#define CM_CMSF                          (CM_CALL_ID) 52                        
#define CM_CMSID                         (CM_CALL_ID) 53                        
#define CM_CMSLD                         (CM_CALL_ID) 54                        
#define CM_CMSLTP                        (CM_CALL_ID) 55                        
#define CM_CMSMN                         (CM_CALL_ID) 56                        
#define CM_CMSNDX                        (CM_CALL_ID) 57                        
#define CM_CMSPDP                        (CM_CALL_ID) 58                        
#define CM_CMSPID                        (CM_CALL_ID) 59                        
#define CM_CMSPLN                        (CM_CALL_ID) 60                        
#define CM_CMSPM                         (CM_CALL_ID) 61                        
#define CM_CMSPTR                        (CM_CALL_ID) 62                        
#define CM_CMSQCF                        (CM_CALL_ID) 63                        
#define CM_CMSQPM                        (CM_CALL_ID) 64                        
#define CM_CMSRC                         (CM_CALL_ID) 65                        
#define CM_CMSRT                         (CM_CALL_ID) 66                        
#define CM_CMSSL                         (CM_CALL_ID) 67                        
#define CM_CMSSRM                        (CM_CALL_ID) 68                        
#define CM_CMSST                         (CM_CALL_ID) 69                        
#define CM_CMSTC                         (CM_CALL_ID) 70                        
#define CM_CMSTPN                        (CM_CALL_ID) 71                        
#define CM_CMTRTS                        (CM_CALL_ID) 72                        
#define CM_CMWAIT                        (CM_CALL_ID) 73                        
#define CM_CMWCMP                        (CM_CALL_ID) 74                        
#define CM_CMSJT                         (CM_CALL_ID) 75                        
#define CM_CMEMID                        (CM_CALL_ID) 76                        
#define CM_CMSMID                        (CM_CALL_ID) 77                        
#define CM_CMSNDM                        (CM_CALL_ID) 78                        
#define CM_CMRCVM                        (CM_CALL_ID) 79                        
                                                                                
                                                                                
/*  confirmation_urgency values  */                                             
                                                                                
#define CM_CONFIRMATION_NOT_URGENT       (CM_CONFIRMATION_URGENCY) 0            
#define CM_CONFIRMATION_URGENT           (CM_CONFIRMATION_URGENCY) 1            
                                                                                
                                                                                
/*  control_information_received, request_to_send_received values  */           
/*  control_information_received is a superset of the old          */           
/*  request_to_send_received parameter.                            */           
                                                                                
#define CM_NO_CONTROL_INFO_RECEIVED      (CM_CONTROL_INFORMATION_RECEIVED)  0   
#define CM_REQ_TO_SEND_NOT_RECEIVED      (CM_CONTROL_INFORMATION_RECEIVED)  0   
#define CM_REQ_TO_SEND_RECEIVED          (CM_CONTROL_INFORMATION_RECEIVED)  1   
#define CM_ALLOCATE_CONFIRMED            (CM_CONTROL_INFORMATION_RECEIVED)  2   
#define CM_ALLOCATE_CONFIRMED_WITH_DATA  (CM_CONTROL_INFORMATION_RECEIVED)  3   
#define CM_ALLOCATE_REJECTED_WITH_DATA   (CM_CONTROL_INFORMATION_RECEIVED)  4   
#define CM_EXPEDITED_DATA_AVAILABLE      (CM_CONTROL_INFORMATION_RECEIVED)  5   
#define CM_RTS_RCVD_AND_EXP_DATA_AVAIL   (CM_CONTROL_INFORMATION_RECEIVED)  6   
                                                                                
                                                                                
/*  conversation_queue values  */                                               
                                                                                
#define CM_INITIALIZATION_QUEUE          (CM_CONVERSATION_QUEUE) 0              
#define CM_SEND_QUEUE                    (CM_CONVERSATION_QUEUE) 1              
#define CM_RECEIVE_QUEUE                 (CM_CONVERSATION_QUEUE) 2              
#define CM_SEND_RECEIVE_QUEUE            (CM_CONVERSATION_QUEUE) 3              
#define CM_EXPEDITED_SEND_QUEUE          (CM_CONVERSATION_QUEUE) 4              
#define CM_EXPEDITED_RECEIVE_QUEUE       (CM_CONVERSATION_QUEUE) 5              
                                                                                
                                                                                
/*  conversation_state values  */                                               
                                                                                
#define CM_INITIALIZE_STATE              (CM_CONVERSATION_STATE) 2              
#define CM_SEND_STATE                    (CM_CONVERSATION_STATE) 3              
#define CM_RECEIVE_STATE                 (CM_CONVERSATION_STATE) 4              
#define CM_SEND_PENDING_STATE            (CM_CONVERSATION_STATE) 5              
#define CM_CONFIRM_STATE                 (CM_CONVERSATION_STATE) 6              
#define CM_CONFIRM_SEND_STATE            (CM_CONVERSATION_STATE) 7              
#define CM_CONFIRM_DEALLOCATE_STATE      (CM_CONVERSATION_STATE) 8              
#define CM_DEFER_RECEIVE_STATE           (CM_CONVERSATION_STATE) 9              
#define CM_DEFER_DEALLOCATE_STATE        (CM_CONVERSATION_STATE) 10             
#define CM_SYNC_POINT_STATE              (CM_CONVERSATION_STATE) 11             
#define CM_SYNC_POINT_SEND_STATE         (CM_CONVERSATION_STATE) 12             
#define CM_SYNC_POINT_DEALLOCATE_STATE   (CM_CONVERSATION_STATE) 13             
#define CM_INITIALIZE_INCOMING_STATE     (CM_CONVERSATION_STATE) 14             
#define CM_SEND_ONLY_STATE               (CM_CONVERSATION_STATE) 15             
#define CM_RECEIVE_ONLY_STATE            (CM_CONVERSATION_STATE) 16             
#define CM_SEND_RECEIVE_STATE            (CM_CONVERSATION_STATE) 17             
#define CM_PREPARED_STATE                (CM_CONVERSATION_STATE) 18             
                                                                                
                                                                                
/*  conversation_type values  */                                                
                                                                                
#define CM_BASIC_CONVERSATION            (CM_CONVERSATION_TYPE) 0               
#define CM_MAPPED_CONVERSATION           (CM_CONVERSATION_TYPE) 1               
                                                                                
                                                                                
/*  data_received values  */                                                    
                                                                                
#define CM_NO_DATA_RECEIVED              (CM_DATA_RECEIVED_TYPE) 0              
#define CM_DATA_RECEIVED                 (CM_DATA_RECEIVED_TYPE) 1              
#define CM_COMPLETE_DATA_RECEIVED        (CM_DATA_RECEIVED_TYPE) 2              
#define CM_INCOMPLETE_DATA_RECEIVED      (CM_DATA_RECEIVED_TYPE) 3              
                                                                                
                                                                                
/*  deallocate_type values  */                                                  
                                                                                
#define CM_DEALLOCATE_SYNC_LEVEL         (CM_DEALLOCATE_TYPE) 0                 
#define CM_DEALLOCATE_FLUSH              (CM_DEALLOCATE_TYPE) 1                 
#define CM_DEALLOCATE_CONFIRM            (CM_DEALLOCATE_TYPE) 2                 
#define CM_DEALLOCATE_ABEND              (CM_DEALLOCATE_TYPE) 3                 
                                                                                
                                                                                
/*  directory_encoding values */                                                
                                                                                
#define CM_DEFAULT_ENCODING              (CM_DIRECTORY_ENCODING) 0              
#define CM_UNICODE_ENCODING              (CM_DIRECTORY_ENCODING) 1              
                                                                                
                                                                                
/*  directory_syntax values */                                                  
                                                                                
#define CM_DEFAULT_SYNTAX                (CM_DIRECTORY_SYNTAX) 0                
#define CM_DCE_SYNTAX                    (CM_DIRECTORY_SYNTAX) 1                
#define CM_XDS_SYNTAX                    (CM_DIRECTORY_SYNTAX) 2                
#define CM_NDS_SYNTAX                    (CM_DIRECTORY_SYNTAX) 3                
                                                                                
                                                                                
/*  error_direction values  */                                                  
                                                                                
#define CM_RECEIVE_ERROR                 (CM_ERROR_DIRECTION) 0                 
#define CM_SEND_ERROR                    (CM_ERROR_DIRECTION) 1                 
                                                                                
                                                                                
/*  fill values  */                                                             
                                                                                
#define CM_FILL_LL                       (CM_FILL) 0                            
#define CM_FILL_BUFFER                   (CM_FILL) 1                            
                                                                                
                                                                                
/*  join transaction values  */                                                 
                                                                                
#define CM_JOIN_IMPLICIT                 (CM_JOIN_TRANSACTION_TYPE) 0           
#define CM_JOIN_EXPLICIT                 (CM_JOIN_TRANSACTION_TYPE) 1           
                                                                                
                                                                                
/*  partner_ID_scope values */                                                  
                                                                                
#define CM_EXPLICIT                      (CM_PARTNER_ID_SCOPE) 0                
#define CM_REFERENCE                     (CM_PARTNER_ID_SCOPE) 1                
                                                                                
                                                                                
/*  partner_ID_type values */                                                   
                                                                                
#define CM_DISTINGUISHED_NAME            (CM_PARTNER_ID_TYPE) 0                 
#define CM_LOCAL_DISTINGUISHED_NAME      (CM_PARTNER_ID_TYPE) 1                 
#define CM_PROGRAM_FUNCTION_ID           (CM_PARTNER_ID_TYPE) 2                 
#define CM_OSI_TPSU_TITLE_OID            (CM_PARTNER_ID_TYPE) 3                 
#define CM_PROGRAM_BINDING               (CM_PARTNER_ID_TYPE) 4                 
                                                                                
                                                                                
/*  prepare_data_permitted values  */                                           
                                                                                
#define CM_PREPARE_DATA_NOT_PERMITTED    (CM_PREPARE_DATA_PERMITTED_TYPE) 0     
#define CM_PREPARE_DATA_PERMITTED        (CM_PREPARE_DATA_PERMITTED_TYPE) 1     
                                                                                
                                                                                
/*  prepare_to_receive_type values  */                                          
                                                                                
#define CM_PREP_TO_RECEIVE_SYNC_LEVEL    (CM_PREPARE_TO_RECEIVE_TYPE) 0         
#define CM_PREP_TO_RECEIVE_FLUSH         (CM_PREPARE_TO_RECEIVE_TYPE) 1         
#define CM_PREP_TO_RECEIVE_CONFIRM       (CM_PREPARE_TO_RECEIVE_TYPE) 2         
                                                                                
                                                                                
/*  processing_mode values  */                                                  
                                                                                
#define CM_BLOCKING                      (CM_PROCESSING_MODE) 0                 
#define CM_NON_BLOCKING                  (CM_PROCESSING_MODE) 1                 
                                                                                
                                                                                
/*  receive_type values  */                                                     
                                                                                
#define CM_RECEIVE_AND_WAIT              (CM_RECEIVE_TYPE) 0                    
#define CM_RECEIVE_IMMEDIATE             (CM_RECEIVE_TYPE) 1                    
                                                                                
                                                                                
/*  return_code values  */                                                      
                                                                                
#define CM_OK                            (CM_RETURN_CODE) 0                     
#define CM_ALLOCATE_FAILURE_NO_RETRY     (CM_RETURN_CODE) 1                     
#define CM_ALLOCATE_FAILURE_RETRY        (CM_RETURN_CODE) 2                     
#define CM_CONVERSATION_TYPE_MISMATCH    (CM_RETURN_CODE) 3                     
#define CM_PIP_NOT_SPECIFIED_CORRECTLY   (CM_RETURN_CODE) 5                     
#define CM_SECURITY_NOT_VALID            (CM_RETURN_CODE) 6                     
#define CM_SYNC_LVL_NOT_SUPPORTED_LU     (CM_RETURN_CODE) 7                     
#define CM_SYNC_LVL_NOT_SUPPORTED_SYS    (CM_RETURN_CODE) 7    /* CPIC 2.0 */   
#define CM_SYNC_LVL_NOT_SUPPORTED_PGM    (CM_RETURN_CODE) 8                     
#define CM_TPN_NOT_RECOGNIZED            (CM_RETURN_CODE) 9                     
#define CM_TP_NOT_AVAILABLE_NO_RETRY     (CM_RETURN_CODE) 10                    
#define CM_TP_NOT_AVAILABLE_RETRY        (CM_RETURN_CODE) 11                    
#define CM_DEALLOCATED_ABEND             (CM_RETURN_CODE) 17                    
#define CM_DEALLOCATED_NORMAL            (CM_RETURN_CODE) 18                    
#define CM_PARAMETER_ERROR               (CM_RETURN_CODE) 19                    
#define CM_PRODUCT_SPECIFIC_ERROR        (CM_RETURN_CODE) 20                    
#define CM_PROGRAM_ERROR_NO_TRUNC        (CM_RETURN_CODE) 21                    
#define CM_PROGRAM_ERROR_PURGING         (CM_RETURN_CODE) 22                    
#define CM_PROGRAM_ERROR_TRUNC           (CM_RETURN_CODE) 23                    
#define CM_PROGRAM_PARAMETER_CHECK       (CM_RETURN_CODE) 24                    
#define CM_PROGRAM_STATE_CHECK           (CM_RETURN_CODE) 25                    
#define CM_RESOURCE_FAILURE_NO_RETRY     (CM_RETURN_CODE) 26                    
#define CM_RESOURCE_FAILURE_RETRY        (CM_RETURN_CODE) 27                    
#define CM_UNSUCCESSFUL                  (CM_RETURN_CODE) 28                    
#define CM_DEALLOCATED_ABEND_SVC         (CM_RETURN_CODE) 30                    
#define CM_DEALLOCATED_ABEND_TIMER       (CM_RETURN_CODE) 31                    
#define CM_SVC_ERROR_NO_TRUNC            (CM_RETURN_CODE) 32                    
#define CM_SVC_ERROR_PURGING             (CM_RETURN_CODE) 33                    
#define CM_SVC_ERROR_TRUNC               (CM_RETURN_CODE) 34                    
#define CM_OPERATION_INCOMPLETE          (CM_RETURN_CODE) 35   /* CPIC 1.2 */   
#define CM_SYSTEM_EVENT                  (CM_RETURN_CODE) 36   /* CPIC 1.2 */   
#define CM_OPERATION_NOT_ACCEPTED        (CM_RETURN_CODE) 37   /* CPIC 1.2 */   
#define CM_CONVERSATION_ENDING           (CM_RETURN_CODE) 38   /* CPIC 2.0 */   
#define CM_SEND_RCV_MODE_NOT_SUPPORTED   (CM_RETURN_CODE) 39   /* CPIC 2.0 */   
#define CM_BUFFER_TOO_SMALL              (CM_RETURN_CODE) 40   /* CPIC 2.0 */   
#define CM_EXP_DATA_NOT_SUPPORTED        (CM_RETURN_CODE) 41   /* CPIC 2.0 */   
#define CM_DEALLOC_CONFIRM_REJECT        (CM_RETURN_CODE) 42   /* CPIC 2.0 */   
#define CM_ALLOCATION_ERROR              (CM_RETURN_CODE) 43   /* CPIC 2.0 */   
#define CM_RETRY_LIMIT_EXCEEDED          (CM_RETURN_CODE) 44   /* CPIC 2.0 */   
#define CM_NO_SECONDARY_INFORMATION      (CM_RETURN_CODE) 45   /* CPIC 2.0 */   
#define CM_SECURITY_NOT_SUPPORTED        (CM_RETURN_CODE) 46   /* CPIC 2.0 */   
#define CM_SECURITY_MUTUAL_FAILED        (CM_RETURN_CODE) 47   /* CPIC 2.0 */   
#define CM_CALL_NOT_SUPPORTED            (CM_RETURN_CODE) 48   /* CPIC 2.0 */   
#define CM_PARM_VALUE_NOT_SUPPORTED      (CM_RETURN_CODE) 49   /* CPIC 2.0 */   
#define CM_UNKNOWN_MAP_NAME_REQUESTED    (CM_RETURN_CODE) 50   /* CPIC 2.1 */   
#define CM_UNKNOWN_MAP_NAME_RECEIVED     (CM_RETURN_CODE) 51   /* CPIC 2.1 */   
#define CM_MAP_ROUTINE_ERROR             (CM_RETURN_CODE) 52   /* CPIC 2.1 */   
#define CM_CONVERSATION_CANCELLED        (CM_RETURN_CODE) 53   /* CPIC 2.1 */   
#define CM_TAKE_BACKOUT                  (CM_RETURN_CODE) 100                   
#define CM_DEALLOCATED_ABEND_BO          (CM_RETURN_CODE) 130                   
#define CM_DEALLOCATED_ABEND_SVC_BO      (CM_RETURN_CODE) 131                   
#define CM_DEALLOCATED_ABEND_TIMER_BO    (CM_RETURN_CODE) 132                   
#define CM_RESOURCE_FAIL_NO_RETRY_BO     (CM_RETURN_CODE) 133                   
#define CM_RESOURCE_FAILURE_RETRY_BO     (CM_RETURN_CODE) 134                   
#define CM_DEALLOCATED_NORMAL_BO         (CM_RETURN_CODE) 135                   
#define CM_CONV_DEALLOC_AFTER_SYNCPT     (CM_RETURN_CODE) 136  /* CPIC 2.0 */   
#define CM_INCLUDE_PARTNER_REJECT_BO     (CM_RETURN_CODE) 137  /* CPIC 2.0 */   
                                                                                
                                                                                
/*  return_control values  */                                                   
                                                                                
#define CM_WHEN_SESSION_ALLOCATED        (CM_RETURN_CONTROL) 0                  
#define CM_IMMEDIATE                     (CM_RETURN_CONTROL) 1                  
#define CM_WHEN_CONWINNER_ALLOCATED      (CM_RETURN_CONTROL) 2                  
#define CM_WHEN_SESSION_FREE             (CM_RETURN_CONTROL) 3                  
                                                                                
                                                                                
/*  send_receive_mode values  */                                                
                                                                                
#define CM_HALF_DUPLEX                   (CM_SEND_RECEIVE_MODE) 0               
#define CM_FULL_DUPLEX                   (CM_SEND_RECEIVE_MODE) 1               
                                                                                
                                                                                
/*  send_type values  */                                                        
                                                                                
#define CM_BUFFER_DATA                   (CM_SEND_TYPE) 0                       
#define CM_SEND_AND_FLUSH                (CM_SEND_TYPE) 1                       
#define CM_SEND_AND_CONFIRM              (CM_SEND_TYPE) 2                       
#define CM_SEND_AND_PREP_TO_RECEIVE      (CM_SEND_TYPE) 3                       
#define CM_SEND_AND_DEALLOCATE           (CM_SEND_TYPE) 4                       
                                                                                
                                                                                
/*  status_received values  */                                                  
                                                                                
#define CM_NO_STATUS_RECEIVED            (CM_STATUS_RECEIVED) 0                 
#define CM_SEND_RECEIVED                 (CM_STATUS_RECEIVED) 1                 
#define CM_CONFIRM_RECEIVED              (CM_STATUS_RECEIVED) 2                 
#define CM_CONFIRM_SEND_RECEIVED         (CM_STATUS_RECEIVED) 3                 
#define CM_CONFIRM_DEALLOC_RECEIVED      (CM_STATUS_RECEIVED) 4                 
#define CM_TAKE_COMMIT                   (CM_STATUS_RECEIVED) 5                 
#define CM_TAKE_COMMIT_SEND              (CM_STATUS_RECEIVED) 6                 
#define CM_TAKE_COMMIT_DEALLOCATE        (CM_STATUS_RECEIVED) 7                 
#define CM_TAKE_COMMIT_DATA_OK           (CM_STATUS_RECEIVED) 8                 
#define CM_TAKE_COMMIT_SEND_DATA_OK      (CM_STATUS_RECEIVED) 9                 
#define CM_TAKE_COMMIT_DEALLOC_DATA_OK   (CM_STATUS_RECEIVED) 10                
#define CM_PREPARE_OK                    (CM_STATUS_RECEIVED) 11                
#define CM_JOIN_TRANSACTION              (CM_STATUS_RECEIVED) 12                
                                                                                
                                                                                
/* sync_level values  */                                                        
                                                                                
#define CM_NONE                          (CM_SYNC_LEVEL) 0                      
#define CM_CONFIRM                       (CM_SYNC_LEVEL) 1                      
#define CM_SYNC_POINT                    (CM_SYNC_LEVEL) 2                      
#define CM_SYNC_POINT_NO_CONFIRM         (CM_SYNC_LEVEL) 3                      
                                                                                
/* conversation_security_type values */                                         
                                                                                
#define CM_SECURITY_NONE                 (CM_CONVERSATION_SECURITY_TYPE) 0      
#define CM_SECURITY_SAME                 (CM_CONVERSATION_SECURITY_TYPE) 1      
#define CM_SECURITY_PROGRAM              (CM_CONVERSATION_SECURITY_TYPE) 2      
#define CM_SECURITY_DISTRIBUTED          (CM_CONVERSATION_SECURITY_TYPE) 3      
#define CM_SECURITY_MUTUAL               (CM_CONVERSATION_SECURITY_TYPE) 4      
#define CM_SECURITY_PROGRAM_STRONG       (CM_CONVERSATION_SECURITY_TYPE) 5      
                                                                                
/* transaction_control values */                                                
                                                                                
#define CM_CHAINED_TRANSACTIONS          (CM_TRANSACTION_CONTROL) 0             
#define CM_UNCHAINED_TRANSACTIONS        (CM_TRANSACTION_CONTROL) 1             
                                                                                
                                                                                
/* maximum sizes of strings and buffers */                                      
                                                                                
#define CM_CID_SIZE   (8)         /* conversation ID           */               
#define CM_CTX_SIZE   (32)        /* context ID                */               
#define CM_LD_SIZE    (512)       /* log data                  */               
#define CM_MAP_SIZE   (64)        /* map name                  */               
#define CM_MN_SIZE    (8)         /* mode name                 */               
#define CM_PLN_SIZE   (17)        /* partner LU name           */               
#define CM_PW_SIZE    (10)        /* password                  */               
#define CM_SDN_SIZE   (8)         /* symbolic destination name */               
#define CM_TPN_SIZE   (64)        /* TP name                   */               
#define CM_UID_SIZE   (10)        /* userid ID                 */               
                                                                                
                                                                                
                                                                                
/*                                                                              
 *  - Base CPI-C function prototypes                                            
 */                                                                             
                                                                                
#ifdef __cplusplus                                                              
#ifdef CM_MVS                                                                   
extern "OS" {                                                                   
#else                                                                           
extern "C" {                                                                    
#endif                                                                          
#endif /* __cplusplus */                                                        
                                                                                
CM_ENTRY cmaccp(unsigned char CM_PTR,              /* conversation_ID         */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmallc(unsigned char CM_PTR,              /* conversation_ID         */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmcfm( unsigned char CM_PTR,              /* conversation_ID         */
                CM_CONTROL_INFORMATION_RECEIVED CM_PTR,                         
                                              /* control_information_received */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmcfmd(unsigned char CM_PTR,              /* conversation_ID         */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmdeal(unsigned char CM_PTR,              /* conversation_ID         */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmecs( unsigned char CM_PTR,              /* conversation_ID         */
                CM_CONVERSATION_STATE CM_PTR,      /* conversation_state      */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmect( unsigned char CM_PTR,              /* conversation_ID         */
                CM_CONVERSATION_TYPE CM_PTR,       /* conversation_type       */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmemn( unsigned char CM_PTR,              /* conversation_ID         */
                unsigned char CM_PTR,              /* mode_name               */
                CM_INT32 CM_PTR,                   /* mode_name_length        */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmepln(unsigned char CM_PTR,              /* conversation_ID         */
                unsigned char CM_PTR,              /* partner_LU_name         */
                CM_INT32 CM_PTR,                   /* partner_LU_name_length  */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmesl( unsigned char CM_PTR,              /* conversation_ID         */
                CM_SYNC_LEVEL CM_PTR,              /* sync_level              */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmflus(unsigned char CM_PTR,              /* conversation_ID         */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cminit(unsigned char CM_PTR,              /* conversation_ID         */
                unsigned char CM_PTR,              /* sym_dest_name           */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmptr( unsigned char CM_PTR,              /* conversation_ID         */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmrcv( unsigned char CM_PTR,              /* conversation_ID         */
                unsigned char CM_PTR,              /* buffer                  */
                CM_INT32 CM_PTR,                   /* requested_length        */
                CM_DATA_RECEIVED_TYPE CM_PTR,      /* data_received           */
                CM_INT32 CM_PTR,                   /* received_length         */
                CM_STATUS_RECEIVED CM_PTR,         /* status_received         */
                CM_CONTROL_INFORMATION_RECEIVED CM_PTR,                         
                                              /* control_information_received */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmrts( unsigned char CM_PTR,              /* conversation_ID         */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmsct( unsigned char CM_PTR,              /* conversation_ID         */
                CM_CONVERSATION_TYPE CM_PTR,       /* conversation_type       */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmsdt( unsigned char CM_PTR,              /* conversation_ID         */
                CM_DEALLOCATE_TYPE CM_PTR,         /* deallocate_type         */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmsed( unsigned char CM_PTR,              /* conversation_ID         */
                CM_ERROR_DIRECTION CM_PTR,         /* error_direction         */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmsend(unsigned char CM_PTR,              /* conversation_ID         */
                unsigned char CM_PTR,              /* buffer                  */
                CM_INT32 CM_PTR,                   /* send_length             */
                CM_CONTROL_INFORMATION_RECEIVED CM_PTR,                         
                                              /* control_information_received */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmserr(unsigned char CM_PTR,              /* conversation_ID         */
                CM_CONTROL_INFORMATION_RECEIVED CM_PTR,                         
                                              /* control_information_received */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmsf(  unsigned char CM_PTR,              /* conversation_ID         */
                CM_FILL CM_PTR,                    /* fill                    */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmsld( unsigned char CM_PTR,              /* conversation_ID         */
                unsigned char CM_PTR,              /* log_data                */
                CM_INT32 CM_PTR,                   /* log_data_length         */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmsmn( unsigned char CM_PTR,              /* conversation_ID         */
                unsigned char CM_PTR,              /* mode_name               */
                CM_INT32 CM_PTR,                   /* mode_name_length        */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmspln(unsigned char CM_PTR,              /* conversation_ID         */
                unsigned char CM_PTR,              /* partner_LU_name         */
                CM_INT32 CM_PTR,                   /* partner_LU_name_length  */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmsptr(unsigned char CM_PTR,              /* conversation_ID         */
                CM_PREPARE_TO_RECEIVE_TYPE CM_PTR, /* prepare_to_receive_type */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmsrc( unsigned char CM_PTR,              /* conversation_ID         */
                CM_RETURN_CONTROL CM_PTR,          /* return_control          */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmsrt( unsigned char CM_PTR,              /* conversation_ID         */
                CM_RECEIVE_TYPE CM_PTR,            /* receive_type            */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmssl( unsigned char CM_PTR,              /* conversation_ID         */
                CM_SYNC_LEVEL CM_PTR,              /* sync_level              */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmsst( unsigned char CM_PTR,              /* conversation_ID         */
                CM_SEND_TYPE CM_PTR,               /* send_type               */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmstpn(unsigned char CM_PTR,              /* conversation_ID         */
                unsigned char CM_PTR,              /* TP_name                 */
                CM_INT32 CM_PTR,                   /* TP_name_length          */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
CM_ENTRY cmtrts(unsigned char CM_PTR,              /* conversation_ID         */
                CM_CONTROL_INFORMATION_RECEIVED CM_PTR,                         
                                              /* control_information_received */
                CM_RETURN_CODE CM_PTR);            /* return_code             */
                                                                                
                                                                                
                                                                                
/*********************************************************************          
 *                                                                              
 * The following Entries are not supported by APPC/MVS.  They are               
 * commented out.  If this header file is used for another platform             
 * that uses any of these services, they can be moved and uncommented.          
 *                                                                              
 *****************************************************************@04A          
 *                                                                              
 *  CM_ENTRY cmacci(unsigned char CM_PTR,                                       
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmcanc(unsigned char CM_PTR,                                       
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmcnvi(unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmcnvo(unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmdfde(unsigned char CM_PTR,                                       
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmeaeq(unsigned char CM_PTR,                                       
 *                  unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_AE_QUAL_OR_AP_TITLE_FORMAT CM_PTR,                       
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmeapt(unsigned char CM_PTR,                                       
 *                  unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_AE_QUAL_OR_AP_TITLE_FORMAT CM_PTR,                       
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmeacn(unsigned char CM_PTR,                                       
 *                  unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmectx(unsigned char CM_PTR,                                       
 *                  unsigned char CM_PTR,                                       
 *                  CM_CONTEXT_ID_LENGTH CM_PTR,                                
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmeid( unsigned char CM_PTR,                                       
 *                  unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmembs(CM_INT32 CM_PTR,                                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmemid(unsigned char CM_PTR,                                       
 *                  unsigned char CM_PTR,                                       
 *                  CM_MAP_NAME_LENGTH CM_PTR,                                  
 *                  unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmepid(unsigned char CM_PTR,                                       
 *                  CM_PARTNER_ID_TYPE CM_PTR,                                  
 *                  unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_PARTNER_ID_SCOPE CM_PTR,                                 
 *                  CM_DIRECTORY_SYNTAX CM_PTR,                                 
 *                  CM_DIRECTORY_ENCODING CM_PTR,                               
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmesi( unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_DATA_RECEIVED_TYPE CM_PTR,                               
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmesrm(unsigned char CM_PTR,                                       
 *                  CM_SEND_RECEIVE_MODE CM_PTR,                                
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmesui(unsigned char  CM_PTR,                                      
 *                  unsigned char  CM_PTR,                                      
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmetc( unsigned char CM_PTR,                                       
 *                  CM_TRANSACTION_CONTROL CM_PTR,                              
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmetpn(unsigned char CM_PTR,                                       
 *                  unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmincl(unsigned char CM_PTR,                                       
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cminic(unsigned char CM_PTR,                                       
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmprep(unsigned char CM_PTR,                                       
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmrcvm(unsigned char CM_PTR,                                       
 *                  unsigned char CM_PTR,                                       
 *                  CM_MAP_NAME_LENGTH CM_PTR,                                  
 *                  unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_DATA_RECEIVED_TYPE CM_PTR,                               
 *                  CM_STATUS_RECEIVED CM_PTR,                                  
 *                  CM_CONTROL_INFORMATION_RECEIVED CM_PTR,                     
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmrcvx(unsigned char CM_PTR,                                       
 *                  unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_CONTROL_INFORMATION_RECEIVED CM_PTR,                     
 *                  CM_RECEIVE_TYPE CM_PTR,                                     
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmrltp(unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmsaeq(unsigned char  CM_PTR,                                      
 *                  unsigned char  CM_PTR,                                      
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_AE_QUAL_OR_AP_TITLE_FORMAT CM_PTR,                       
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmsac( unsigned char CM_PTR,                                       
 *                  CM_ALLOCATE_CONFIRM_TYPE CM_PTR,                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmsacn(unsigned char  CM_PTR,                                      
 *                  unsigned char  CM_PTR,                                      
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmsapt(unsigned char  CM_PTR,                                      
 *                  unsigned char  CM_PTR,                                      
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_AE_QUAL_OR_AP_TITLE_FORMAT CM_PTR,                       
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmsbt( unsigned char CM_PTR,                                       
 *                  CM_BEGIN_TRANSACTION CM_PTR,                                
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmscsp(unsigned char  CM_PTR,                                      
 *                  unsigned char  CM_PTR,                                      
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmscst(unsigned char  CM_PTR,                                      
 *                  CM_CONVERSATION_SECURITY_TYPE CM_PTR,                       
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmscsu(unsigned char  CM_PTR,                                      
 *                  unsigned char  CM_PTR,                                      
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmscu( unsigned char CM_PTR,                                       
 *                  CM_CONFIRMATION_URGENCY CM_PTR,                             
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmsid( unsigned char CM_PTR,                                       
 *                  unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmsjt( unsigned char CM_PTR,                                       
 *                  CM_JOIN_TRANSACTION_TYPE CM_PTR,                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmsltp(unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmsmid(unsigned char CM_PTR,                                       
 *                  unsigned char CM_PTR,                                       
 *                  CM_MAP_NAME_LENGTH CM_PTR,                                  
 *                  unsigned char CM_PTR,                                       
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmsndm(unsigned char CM_PTR,                                       
 *                  unsigned char CM_PTR,                                       
 *                  CM_MAP_NAME_LENGTH CM_PTR,                                  
 *                  unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_CONTROL_INFORMATION_RECEIVED CM_PTR,                     
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmsndx(unsigned char CM_PTR,                                       
 *                  unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_CONTROL_INFORMATION_RECEIVED CM_PTR,                     
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmspdp(unsigned char CM_PTR,                                       
 *                  CM_PREPARE_DATA_PERMITTED_TYPE CM_PTR,                      
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmspid(unsigned char CM_PTR,                                       
 *                  CM_PARTNER_ID_TYPE CM_PTR,                                  
 *                  unsigned char CM_PTR,                                       
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_PARTNER_ID_SCOPE CM_PTR,                                 
 *                  CM_DIRECTORY_SYNTAX CM_PTR,                                 
 *                  CM_DIRECTORY_ENCODING CM_PTR,                               
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmspm( unsigned char CM_PTR,                                       
 *                  CM_PROCESSING_MODE CM_PTR,                                  
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmsqcf(unsigned char CM_PTR,                                       
 *                  CM_CONVERSATION_QUEUE CM_PTR,                               
 *                  void CM_PTR,                                                
 *                  unsigned char CM_PTR,                                       
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmsqpm(unsigned char CM_PTR,                                       
 *                  CM_CONVERSATION_QUEUE CM_PTR,                               
 *                  CM_PROCESSING_MODE CM_PTR,                                  
 *                  unsigned char CM_PTR,                                       
 *                  CM_OOID CM_PTR,                                             
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmssrm(unsigned char CM_PTR,                                       
 *                  CM_SEND_RECEIVE_MODE CM_PTR,                                
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmstc( unsigned char CM_PTR,                                       
 *                  CM_TRANSACTION_CONTROL CM_PTR,                              
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmwait(unsigned char CM_PTR,                                       
 *                  CM_RETURN_CODE CM_PTR,                                      
 *                  CM_RETURN_CODE CM_PTR);                                     
 *  CM_ENTRY cmwcmp(CM_OOID CM_PTR,                                             
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_TIMEOUT CM_PTR,                                          
 *                  CM_INT32 CM_PTR,                                            
 *                  CM_INT32 CM_PTR,                                            
 *                  unsigned char CM_PTR,                                       
 *                  CM_RETURN_CODE CM_PTR);                                     
 *                                                                              
 ********** End of Services not Supported by APPC/MVS ****************          
 *****************************************************************@04A*/        
                                                                                
                                                                                
                                                                                
/*                                                                              
 * X/Open provides security calls in the base.                                  
 * Since some platforms already support the security calls through              
 * product specific extensions, we'll provide macros here to help in            
 * porting X/Open applications to these platforms.                              
 */                                                                             
#if defined(CM_VM) || defined(CM_DOS)                                           
#define cmscsu(v1,v2,v3,v4) xcscsu(v1,v2,v3,v4)                                 
#define cmscsp(v1,v2,v3,v4) xcscsp(v1,v2,v3,v4)                                 
#define cmscst(v1,v2,v3)    xcscst(v1,v2,v3)                                    
#    if !defined(CM_DOS)                                                        
/*                                                                              
 * Networking Services/DOS does not provide extract security calls.             
 */                                                                             
#    define cmecsu(v1,v2,v3,v4) xcecsu(v1,v2,v3,v4)                             
#    endif                                                                      
#endif                                                                          
                                                                                
                                                                                
                                                                                
                                                                                
#ifdef __cplusplus                                                              
}                                                                               
#endif /* __cplusplus */                                                        
                                                                                
/*                                                                              
 *  - Product specific constants and type definitions                           
 */                                                                             
                                                                                
/*                                                                              
 *    #pragma linkage directives                                                
 *                                                                              
 * Note: For OS/400, routine names must be all uppercase.                       
 */                                                                             
#if defined(CM_VM)                                                              
#     pragma linkage (cmacci, OS)                                               
#     pragma linkage (cmaccp, OS)                                               
#     pragma linkage (cmallc, OS)                                               
#     pragma linkage (cmcanc, OS)                                               
#     pragma linkage (cmcfm,  OS)                                               
#     pragma linkage (cmcfmd, OS)                                               
#     pragma linkage (cmcnvi, OS)                                               
#     pragma linkage (cmcnvo, OS)                                               
#     pragma linkage (cmdeal, OS)                                               
#     pragma linkage (cmdfde, OS)                                               
#     pragma linkage (cmeaeq, OS)                                               
#     pragma linkage (cmeapt, OS)                                               
#     pragma linkage (cmeacn, OS)                                               
#     pragma linkage (cmecs,  OS)                                               
#     pragma linkage (cmect,  OS)                                               
#     pragma linkage (cmectx, OS)                                               
#     pragma linkage (cmeid,  OS)                                               
#     pragma linkage (cmembs, OS)                                               
#     pragma linkage (cmemid, OS)                                               
#     pragma linkage (cmemn,  OS)                                               
#     pragma linkage (cmepid, OS)                                               
#     pragma linkage (cmepln, OS)                                               
#     pragma linkage (cmesi,  OS)                                               
#     pragma linkage (cmesl,  OS)                                               
#     pragma linkage (cmesrm, OS)                                               
#     pragma linkage (cmesui, OS)                                               
#     pragma linkage (cmetpn, OS)                                               
#     pragma linkage (cmetc,  OS)                                               
#     pragma linkage (cmflus, OS)                                               
#     pragma linkage (cmincl, OS)                                               
#     pragma linkage (cminic, OS)                                               
#     pragma linkage (cminit, OS)                                               
#     pragma linkage (cmprep, OS)                                               
#     pragma linkage (cmptr,  OS)                                               
#     pragma linkage (cmrcv,  OS)                                               
#     pragma linkage (cmrcvm, OS)                                               
#     pragma linkage (cmrcvx, OS)                                               
#     pragma linkage (cmrltp, OS)                                               
#     pragma linkage (cmrts,  OS)                                               
#     pragma linkage (cmsaeq, OS)                                               
#     pragma linkage (cmsac,  OS)                                               
#     pragma linkage (cmsacn, OS)                                               
#     pragma linkage (cmsapt, OS)                                               
#     pragma linkage (cmsbt,  OS)                                               
#     pragma linkage (cmscsp, OS)                                               
#     pragma linkage (cmscst, OS)                                               
#     pragma linkage (cmscsu, OS)                                               
#     pragma linkage (cmsct,  OS)                                               
#     pragma linkage (cmscu,  OS)                                               
#     pragma linkage (cmsdt,  OS)                                               
#     pragma linkage (cmsed,  OS)                                               
#     pragma linkage (cmsend, OS)                                               
#     pragma linkage (cmserr, OS)                                               
#     pragma linkage (cmsf,   OS)                                               
#     pragma linkage (cmsid,  OS)                                               
#     pragma linkage (cmsjt,  OS)                                               
#     pragma linkage (cmsld,  OS)                                               
#     pragma linkage (cmsltp, OS)                                               
#     pragma linkage (cmsmid, OS)                                               
#     pragma linkage (cmsmn,  OS)                                               
#     pragma linkage (cmsndm, OS)                                               
#     pragma linkage (cmsndx, OS)                                               
#     pragma linkage (cmspdp, OS)                                               
#     pragma linkage (cmspid, OS)                                               
#     pragma linkage (cmspln, OS)                                               
#     pragma linkage (cmspm , OS)                                               
#     pragma linkage (cmsptr, OS)                                               
#     pragma linkage (cmsqcf, OS)                                               
#     pragma linkage (cmsqpm, OS)                                               
#     pragma linkage (cmsrc,  OS)                                               
#     pragma linkage (cmsrt,  OS)                                               
#     pragma linkage (cmssl,  OS)                                               
#     pragma linkage (cmssrm, OS)                                               
#     pragma linkage (cmsst,  OS)                                               
#     pragma linkage (cmstc,  OS)                                               
#     pragma linkage (cmstpn, OS)                                               
#     pragma linkage (cmtrts, OS)                                               
#     pragma linkage (cmwait, OS)                                               
#     pragma linkage (cmwcmp, OS)                                               
#endif                                                                          
                                                                                
#if defined(CM_OS400) || defined(CM_MVS)                                        
#if !defined(__cplusplus) || defined(CM_OS400)                                  
/* Pragma linkage statements are not needed for C++ on MVS           */         
/*    pragma linkage (CMACCI, OS)                                @04C*/         
#     pragma linkage (CMACCP, OS)                                               
#     pragma linkage (CMALLC, OS)                                               
/*    pragma linkage (CMCANC, OS)                                @04C*/         
#     pragma linkage (CMCFM,  OS)                                               
#     pragma linkage (CMCFMD, OS)                                               
/*    pragma linkage (CMCNVI, OS)                                @04C*/         
/*    pragma linkage (CMCNVO, OS)                                @04C*/         
#     pragma linkage (CMDEAL, OS)                                               
/*    pragma linkage (CMDFDE, OS)                                @04C*/         
/*    pragma linkage (CMEAEQ, OS)                                @04C*/         
/*    pragma linkage (CMEAPT, OS)                                @04C*/         
/*    pragma linkage (CMEACN, OS)                                @04C*/         
#     pragma linkage (CMECS,  OS)                                               
#     pragma linkage (CMECT,  OS)                                               
/*    pragma linkage (CMECTX, OS)                                @04C*/         
/*    pragma linkage (CMEID,  OS)                                @04C*/         
/*    pragma linkage (CMEMBS, OS)                                @04C*/         
/*    pragma linkage (CMEMID, OS)                                @04C*/         
#     pragma linkage (CMEMN,  OS)                                               
/*    pragma linkage (CMEPID, OS)                                @04C*/         
#     pragma linkage (CMEPLN, OS)                                               
/*    pragma linkage (CMESI,  OS)                                @04C*/         
#     pragma linkage (CMESL,  OS)                                               
/*    pragma linkage (CMESRM, OS)                                @04C*/         
/*    pragma linkage (CMESUI, OS)                                @04C*/         
/*    pragma linkage (CMETC,  OS)                                @04C*/         
/*    pragma linkage (CMETPN, OS)                                @04C*/         
#     pragma linkage (CMFLUS, OS)                                               
/*    pragma linkage (CMINCL, OS)                                @04C*/         
/*    pragma linkage (CMINIC, OS)                                @04C*/         
#     pragma linkage (CMINIT, OS)                                               
/*    pragma linkage (CMPREP, OS)                                @04C*/         
#     pragma linkage (CMPTR,  OS)                                               
#     pragma linkage (CMRCV,  OS)                                               
/*    pragma linkage (CMRCVM, OS)                                @04C*/         
/*    pragma linkage (CMRCVX, OS)                                @04C*/         
/*    pragma linkage (CMRLTP, OS)                                @04C*/         
#     pragma linkage (CMRTS,  OS)                                               
/*    pragma linkage (CMSAEQ, OS)                                @04C*/         
/*    pragma linkage (CMSAC,  OS)                                @04C*/         
/*    pragma linkage (CMSACN, OS)                                @04C*/         
/*    pragma linkage (CMSAPT, OS)                                @04C*/         
/*    pragma linkage (CMSBT,  OS)                                @04C*/         
/*    pragma linkage (CMSCSP, OS)                                @04C*/         
/*    pragma linkage (CMSCST, OS)                                @04C*/         
/*    pragma linkage (CMSCSU, OS)                                @04C*/         
#     pragma linkage (CMSCT,  OS)                                               
/*    pragma linkage (CMSCU,  OS)                                @04C*/         
#     pragma linkage (CMSDT,  OS)                                               
#     pragma linkage (CMSED,  OS)                                               
#     pragma linkage (CMSEND, OS)                                               
#     pragma linkage (CMSERR, OS)                                               
#     pragma linkage (CMSF,   OS)                                               
/*    pragma linkage (CMSID,  OS)                                @04C*/         
/*    pragma linkage (CMSJT,  OS)                                @04C*/         
#     pragma linkage (CMSLD,  OS)                                               
/*    pragma linkage (CMSLTP, OS)                                @04C*/         
/*    pragma linkage (CMSMID, OS)                                @04C*/         
#     pragma linkage (CMSMN,  OS)                                               
/*    pragma linkage (CMSNDM, OS)                                @04C*/         
/*    pragma linkage (CMSNDX, OS)                                @04C*/         
/*    pragma linkage (CMSPDP, OS)                                @04C*/         
/*    pragma linkage (CMSPID, OS)                                @04C*/         
#     pragma linkage (CMSPLN, OS)                                               
/*    pragma linkage (CMSPM , OS)                                @04C*/         
#     pragma linkage (CMSPTR, OS)                                               
/*    pragma linkage (CMSQCF, OS)                                @04C*/         
/*    pragma linkage (CMSQPM, OS)                                @04C*/         
#     pragma linkage (CMSRC,  OS)                                               
#     pragma linkage (CMSRT,  OS)                                               
#     pragma linkage (CMSSL,  OS)                                               
/*    pragma linkage (CMSSRM, OS)                                @04C*/         
#     pragma linkage (CMSST,  OS)                                               
/*    pragma linkage (CMSTC,  OS)                                @04C*/         
#     pragma linkage (CMSTPN, OS)                                               
#     pragma linkage (CMTRTS, OS)                                               
/*    pragma linkage (CMWAIT, OS)                                @04C*/         
/*    pragma linkage (CMWCMP, OS)                                @04C*/         
#endif                                                                          
#endif                                                                          
                                                                                
                                                                                
#if defined(CM_OS2) || defined(CM_DOS) || defined(CM_AIX)                       
                                                                                
/*                                                                              
 * Conversation security extensions                                             
 */                                                                             
                                                                                
typedef CM_INT32 XC_CONVERSATION_SECURITY_TYPE;                                 
                                                                                
/* conversation_security_type values */                                         
                                                                                
#define XC_SECURITY_NONE                 (XC_CONVERSATION_SECURITY_TYPE) 0      
#define XC_SECURITY_SAME                 (XC_CONVERSATION_SECURITY_TYPE) 1      
#define XC_SECURITY_PROGRAM              (XC_CONVERSATION_SECURITY_TYPE) 2      
                                                                                
/* Set Signal Behavior extensions and type values */                            
                                                                                
#if defined(CM_AIX)                                                             
typedef CM_INT32 XC_SIGNAL_BEHAVIOR_TYPE;                                       
#define XC_SIGNAL_BEHAVIOR_NO_RETRY       (XC_SIGNAL_BEHAVIOR_TYPE) 0           
#define XC_SIGNAL_BEHAVIOR_INFINITE_RETRY (XC_SIGNAL_BEHAVIOR_TYPE) 1           
#endif                                                                          
                                                                                
#ifdef __cplusplus                                                              
extern "C" {                                                                    
#endif /* __cplusplus */                                                        
                                                                                
#    if !defined(CM_DOS) /* extract calls are not supported in DOS */           
                                                                                
                                                                                
/* Extract_Conversation_Security_Type */                                        
CM_ENTRY xcecst(unsigned char  CM_PTR,   /* conversation_ID                 */  
                CM_INT32 CM_PTR,         /* conversation_security_type      */  
                CM_RETURN_CODE CM_PTR);  /* return_code                     */  
                                                                                
/* Extract_Conversation_Security_User_ID */                                     
CM_ENTRY xcecsu(unsigned char  CM_PTR,   /* conversation_ID                 */  
                unsigned char  CM_PTR,   /* user_ID                         */  
                CM_INT32 CM_PTR,         /* user_ID_length                  */  
                CM_RETURN_CODE CM_PTR);  /* return_code                     */  
#    endif                                                                      
                                                                                
/* Set_Conversation_Security_Password */                                        
CM_ENTRY xcscsp(unsigned char  CM_PTR,   /* conversation_ID                 */  
                unsigned char  CM_PTR,   /* password                        */  
                CM_INT32 CM_PTR,         /* password_length                 */  
                CM_RETURN_CODE CM_PTR);  /* return_code                     */  
                                                                                
/* Set_Conversation_Security_Type */                                            
CM_ENTRY xcscst(unsigned char  CM_PTR,   /* conversation_ID                 */  
                CM_CONVERSATION_SECURITY_TYPE CM_PTR,                           
                                         /* conversation_security_type      */  
                CM_RETURN_CODE CM_PTR);  /* return_code                     */  
                                                                                
/* Set_Conversation_Security_User_ID */                                         
CM_ENTRY xcscsu(unsigned char  CM_PTR,   /* conversation_ID                 */  
                unsigned char  CM_PTR,   /* user_ID                         */  
                CM_INT32 CM_PTR,         /* user_ID_length                  */  
                CM_RETURN_CODE CM_PTR);  /* return_code                     */  
                                                                                
/* Set_Signal_Behavior */                                                       
#if defined(CM_AIX)                                                             
CM_ENTRY xcssb(unsigned char  CM_PTR,          /* conversation_ID           */  
               XC_SIGNAL_BEHAVIOR_TYPE CM_PTR, /* signal behavior           */  
               CM_RETURN_CODE CM_PTR);         /* return_code               */  
                                                                                
#endif                                                                          
                                                                                
#ifdef __cplusplus                                                              
}                                                                               
#endif /* __cplusplus */                                                        
                                                                                
#endif                                                                          
                                                                                
                                                                                
#if defined(CM_OS2)                                                             
/*                                                                              
 * Constants and prototypes for OS/2 side information calls                     
 */                                                                             
                                                                                
typedef CM_INT32 XC_TP_NAME_TYPE;                                               
/* TP_name_type values */                                                       
#define XC_APPLICATION_TP        (XC_TP_NAME_TYPE) 0                            
#define XC_SNA_SERVICE_TP        (XC_TP_NAME_TYPE) 1                            
                                                                                
typedef CM_INT32 XC_TP_END_TYPE;                                                
/*  tp ended values  */                                                         
#define XC_SOFT                          (XC_TP_END_TYPE) 0                     
#define XC_HARD                          (XC_TP_END_TYPE) 1                     
                                                                                
/* Used to release all TP names using CMRLTP */                                 
#define XC_RELEASE_ALL                   "**"                                   
                                                                                
/*  call_IDs  */                                                                
#define XC_XCECST                        (CM_CALL_ID) 10001                     
#define XC_XCECSU                        (CM_CALL_ID) 10002                     
#define XC_XCSCSP                        (CM_CALL_ID) 10003                     
#define XC_XCSCST                        (CM_CALL_ID) 10004                     
#define XC_XCSCSU                        (CM_CALL_ID) 10005                     
#define XC_XCMSSI                        (CM_CALL_ID) 10006                     
#define XC_XCMESI                        (CM_CALL_ID) 10007                     
#define XC_XCMDSI                        (CM_CALL_ID) 10008                     
#define XC_XCENDT                        (CM_CALL_ID) 10009                     
#define XC_XCETI                         (CM_CALL_ID) 10010                     
#define XC_XCINCT                        (CM_CALL_ID) 10011                     
#define XC_XCSTP                         (CM_CALL_ID) 10012                     
#define XC_XCDEFTP                       (CM_CALL_ID) 10013                     
#define XC_XCDELTP                       (CM_CALL_ID) 10014                     
#define XC_XCREGMEM                      (CM_CALL_ID) 10015                     
#define XC_XCRELMEM                      (CM_CALL_ID) 10016                     
                                                                                
/* Conversation Type */                                                         
#define XC_EITHER_CONVERSATION_TYPE      (CM_INT32) 2003                        
                                                                                
/* Send Receive Mode */                                                         
#define XC_EITHER_SEND_RECEIVE_MODE      (CM_INT32) 2004                        
                                                                                
/* Sync Level */                                                                
#define XC_EITHER_SYNC_LEVEL             (CM_INT32) 2007                        
                                                                                
/* TP operation */                                                              
#define XC_QUEUED_OPERATOR_STARTED       (CM_INT32) 2008                        
#define XC_QUEUED_AM_STARTED             (CM_INT32) 2009                        
#define XC_NONQUEUED_AM_STARTED          (CM_INT32) 2010                        
#define XC_QUEUED_OPERATOR_PRELOADED     (CM_INT32) 2011                        
                                                                                
/* Program type */                                                              
#define XC_BACKGROUND                    (CM_INT32) 2012                        
#define XC_FULL_SCREEN                   (CM_INT32) 2013                        
#define XC_PRESENTATION_MANAGER          (CM_INT32) 2014                        
#define XC_VIO_WINDOWABLE                (CM_INT32) 2015                        
                                                                                
/* Values for conv security required */                                         
#define XC_NO                            (CM_INT32) 2005                        
#define XC_YES                           (CM_INT32) 2006                        
                                                                                
/* Infinite timeout value */                                                    
#define XC_HOLD_FOREVER                  (CM_TIMEOUT) -1                        
                                                                                
/* Return codes for XCDEFTP and XCDELTP */                                      
#define XC_PARM1_CHECK                         (CM_RETURN_CODE) 2000            
#define XC_PARM2_CHECK                         (CM_RETURN_CODE) 2001            
#define XC_PARM3_CHECK                         (CM_RETURN_CODE) 2002            
#define XC_PARM4_CHECK                         (CM_RETURN_CODE) 2003            
#define XC_TP_ALREADY_ACTIVE                   (CM_RETURN_CODE) 2004            
#define XC_COMM_SUBSYSTEM_ABENDED              (CM_RETURN_CODE) 2006            
#define XC_COMM_SUBSYSTEM_NOT_LOADED           (CM_RETURN_CODE) 2007            
#define XC_STACK_TOO_SMALL                     (CM_RETURN_CODE) 2008            
#define XC_UNEXPECTED_DOS_ERROR                (CM_RETURN_CODE) 2009            
#define XC_INCONSISTENT_TP_OPERATION           (CM_RETURN_CODE) 2010            
#define XC_INVALID_CONV_SECURITY_RQD           (CM_RETURN_CODE) 2011            
#define XC_INVALID_CONVERSATION_TYPE           (CM_RETURN_CODE) 2012            
#define XC_INVALID_SEND_RCV_MODE               (CM_RETURN_CODE) 2013            
#define XC_INVALID_INCOM_ALLOC_TIMEOUT         (CM_RETURN_CODE) 2014            
#define XC_INVALID_PROGRAM_TYPE                (CM_RETURN_CODE) 2015            
#define XC_INVALID_INCOM_ALLOC_Q_LIMIT         (CM_RETURN_CODE) 2016            
#define XC_INVALID_ACCEPT_TIMEOUT              (CM_RETURN_CODE) 2017            
#define XC_INVALID_SYNC_LEVEL                  (CM_RETURN_CODE) 2018            
#define XC_INVALID_TP_NAME                     (CM_RETURN_CODE) 2019            
#define XC_INVALID_TP_NAME_TYPE                (CM_RETURN_CODE) 2020            
#define XC_INVALID_TP_OPERATION                (CM_RETURN_CODE) 2021            
#define XC_TP_NAME_NOT_RECOGNIZED              (CM_RETURN_CODE) 2022            
                                                                                
/* Return codes for XCRMO and XCURMO */                                         
#define XC_MEMORY_OBJECT_IN_USE                (CM_RETURN_CODE) 2030            
#define XC_MEMORY_OBJECT_NOT_REG               (CM_RETURN_CODE) 2031            
                                                                                
/* side info structure used by xcmssi to define side info */                    
typedef struct side_info_entry {                                                
    unsigned char    sym_dest_name[8];    /* symbolic destination name    */    
    unsigned char    partner_LU_name[17];                                       
    unsigned char    reserved[3];         /* currently not used           */    
    XC_TP_NAME_TYPE  TP_name_type;        /* set to XC_APPLICATION_TP     */    
                                          /*     or XC_SNA_SERVICE_TP     */    
    unsigned char    TP_name[64];                                               
    unsigned char    mode_name[8];                                              
    XC_CONVERSATION_SECURITY_TYPE                                               
                     conversation_security_type;                                
                                          /* set to XC_SECURITY_NONE      */    
                                          /*   or   XC_SECURITY_SAME      */    
                                          /*   or   XC_SECURITY_PROGRAM   */    
    unsigned char    security_user_ID[8];                                       
    unsigned char    security_password[8];                                      
} SIDE_INFO;                                                                    
                                                                                
/* extended side info structure with 10 byte user_id and password */            
/* NOTE: If using the extended side info entry with XCMSSI or     */            
/*       XCMESI, typecast the pointer to (SIDE_INFO *) to match   */            
/*       the function prototype.                                  */            
typedef struct extended_side_info_entry {                                       
    unsigned char    sym_dest_name[8];     /* symbolic destination name    */   
    unsigned char    partner_LU_name[17];                                       
    unsigned char    reserved[3];          /* currently not used           */   
    XC_TP_NAME_TYPE  TP_name_type;         /* set to XC_APPLICATION_TP     */   
                                           /*     or XC_SNA_SERVICE_TP     */   
    unsigned char    TP_name[64];                                               
    unsigned char    mode_name[8];                                              
    XC_CONVERSATION_SECURITY_TYPE                                               
                     conversation_security_type;                                
                                           /* set to XC_SECURITY_NONE      */   
                                           /*   or   XC_SECURITY_SAME      */   
                                           /*   or   XC_SECURITY_PROGRAM   */   
    unsigned char    security_user_ID[10]; /* 10 byte user id              */   
    unsigned char    security_password[10];/* 10 byte password             */   
    unsigned char    reserved2[12];        /* reserved set to X'00'        */   
} EXTENDED_SIDE_INFO;                                                           
                                                                                
/* Secondary information additional information area - format types */          
#define SDARYA_TYPE_APPC1             "AP01"                                    
                                                                                
typedef struct sdarya_type_def {        /* Secondary Information - additional */
                                        /*  information.  This structure used */
                                        /*  to access just the type field.    */
    unsigned char     type[4];                                                  
} XC_SDARYA_TYPE;                                                               
                                                                                
typedef struct sdarya_appc1_def {        /* Secondary Information-  additional *
                                        /*  information                       */
    unsigned char     type[4];          /* type=SDARYS_TYPE_APPC1             */
    unsigned char     opcode[4];        /* APPC verb opcode                   */
    unsigned char     primary_rc[4];    /* APPC Primary Return Code           */
    unsigned char     secondary_rc[8];  /* APPC Secondary Return Code         */
    unsigned char     sense_data[8];    /* APPC Sense Data if available       */
} XC_SDARYA_APPC1;                                                              
                                                                                
/* TP definition structure used by XCDEFTP */                                   
typedef struct tp_definition_entry {                                            
    unsigned char    tp_name[64];            /* TP name                      */ 
    XC_TP_NAME_TYPE  tp_name_type;           /* set to XC_APPLICATION_TP     */ 
                                             /*     or XC_SNA_SERVICE_TP     */ 
    unsigned char filespec[80];              /* TP Filespec (ASCII)          */ 
    unsigned char icon_filespec[80];         /* Icon Filespec (ASCII)        */ 
    unsigned char parm_string[128];          /* TP Parameters (ASCII)        */ 
    CM_INT32 conversation_type;              /* Conversation Type            */ 
                                             /*  CM_BASIC                    */ 
                                             /*  CM_MAPPED                   */ 
                                             /*  XC_EITHER                   */ 
    CM_INT32 send_receive_mode;              /* Send-Receive Mode            */ 
                                             /*  CM_HALF_DUPLEX              */ 
                                             /*  CM_FULL_DUPLEX              */ 
                                             /*  XC_EITHER_SEND_RECEIVE_MODE */ 
    CM_INT32 reserv1;                        /* reserved field               */ 
                                             /*  set to 0's                  */ 
    CM_INT32 conversation_security_required; /* Conversation Security Rqd    */ 
                                             /*  XC_NO                       */ 
                                             /*  XC_YES                      */ 
    CM_INT32 sync_level;                     /* Sync Level                   */ 
                                             /*  CM_NONE                     */ 
                                             /*  CM_CONFIRM                  */ 
                                             /*  XC_EITHER_SYNC_LEVEL        */ 
    CM_INT32 tp_operation;                   /* TP Operation                 */ 
                                             /*  XC_QUEUED_OPERATOR_STARTED  */ 
                                             /*  XC_QUEUED_AM_STARTED        */ 
                                             /*  XC_NONQUEUED_AM_STARTED     */ 
                                             /*  XC_QUEUED_OPERATOR_PRELOADED*/ 
    CM_INT32 program_type;                   /* Program Type                 */ 
                                             /*  XC_BACKGROUND               */ 
                                             /*  XC_FULL_SCREEN              */ 
                                             /*  XC_PRESENTATION_MANAGER     */ 
                                             /*  XC_VIO_WINDOWABLE           */ 
    CM_INT32 incoming_allocate_queue_limit;  /* Queue Limit  1-255           */ 
    CM_INT32 incoming_allocate_timeout;      /* Incoming Allocate Timeout    */ 
                                             /*    0-32767 or XC_HOLD_FOREVER*/ 
    CM_INT32 accept_timeout;                 /* Accept Timeout               */ 
                                             /*    0-32767 or XC_HOLD_FOREVER*/ 
    unsigned char reserv2[12];               /* Reserved - set to 0's        */ 
} XC_TP_DEFINITION;                                                             
                                                                                
                                                                                
#ifdef __cplusplus                                                              
extern "C" {                                                                    
#endif /* __cplusplus */                                                        
/* Set_CPIC_Side_Information     */                                             
CM_ENTRY xcmssi(unsigned char CM_PTR,  /* key lock                          */  
                SIDE_INFO CM_PTR,      /* side info_entry                   */  
                CM_INT32 CM_PTR,       /* side_info length                  */  
                CM_RETURN_CODE CM_PTR);/* return_code                       */  
                                                                                
/* Extract_CPIC_Side_Information */                                             
CM_ENTRY xcmesi(CM_INT32 CM_PTR,       /* entry_number                      */  
                unsigned char CM_PTR,  /* symbolic destination name 8 chars */  
                SIDE_INFO CM_PTR,      /* side_info_entry                   */  
                CM_INT32 CM_PTR,       /* side_info_length                  */  
                CM_RETURN_CODE CM_PTR);/* return_code                       */  
                                                                                
/* Delete_CPIC_Side_Information  */                                             
CM_ENTRY xcmdsi(unsigned char CM_PTR,  /* key_lock                          */  
                unsigned char CM_PTR,  /* symbolic destination name 8 chars */  
                CM_RETURN_CODE CM_PTR);/* return_code                       */  
                                                                                
/* End_TP   note: cpic_tp_id length is 12 bytes */                              
CM_ENTRY xcendt(unsigned char CM_PTR,  /* cpic_tp_id               */           
                CM_INT32 CM_PTR,       /* type-XC_SOFT or XC_HARD  */           
                CM_INT32 CM_PTR);      /* return_code              */           
                                                                                
/* Extract_TP_ID   note:cpic_tp_id length is 12 bytes */                        
CM_ENTRY xceti(unsigned char CM_PTR,   /* conversation_id          */           
               unsigned char CM_PTR,   /* cpic_tp_id               */           
               CM_INT32 CM_PTR);       /* return_code              */           
                                                                                
/* Initialize_Conv_For_TP  note: cpic_tp_id length is 12 bytes */               
CM_ENTRY xcinct(unsigned char CM_PTR,  /* conversation_ID          */           
                unsigned char CM_PTR,  /* sym_dest_name            */           
                unsigned char CM_PTR,  /* cpic_tp_id               */           
                CM_INT32 CM_PTR);      /* return_code              */           
                                                                                
/* Start_TP      note: cpic_tp_id length is 12 bytes   */                       
CM_ENTRY xcstp(unsigned char CM_PTR,   /* local_lu_alias           */           
               CM_INT32 CM_PTR,        /* local_lu_alias_length    */           
               unsigned char CM_PTR,   /* tp_name                  */           
               CM_INT32 CM_PTR,        /* tp_name_length           */           
               unsigned char CM_PTR,   /* cpic_tp_id               */           
               CM_INT32 CM_PTR);       /* return_code              */           
                                                                                
/* Define_TP                     */                                             
CM_ENTRY xcdeftp(unsigned char CM_PTR, /* input key                */           
                 XC_TP_DEFINITION CM_PTR, /* tp definition         */           
                 CM_INT32 CM_PTR,      /* tp definition length     */           
                 CM_RETURN_CODE CM_PTR);/* return code             */           
                                                                                
/* Delete_TP                     */                                             
CM_ENTRY xcdeltp(unsigned char CM_PTR, /* input key                */           
                 unsigned char CM_PTR, /* tp name                  */           
                 CM_INT32 CM_PTR,      /* tp name length           */           
                 CM_INT32 CM_PTR,      /* tp name type             */           
                 CM_RETURN_CODE CM_PTR);/* return code             */           
                                                                                
/* Register_Memory_Object        */                                             
CM_ENTRY xcrmo(void CM_PTR,             /* memory object           */           
               CM_RETURN_CODE CM_PTR);  /* return code             */           
                                                                                
/* Unregister_Memory_Object        */                                           
CM_ENTRY xcurmo(void CM_PTR,              /* memory object         */           
                CM_RETURN_CODE CM_PTR);   /* return code           */           
                                                                                
#ifdef __cplusplus                                                              
}                                                                               
#endif /* __cplusplus */                                                        
#endif                                                                          
                                                                                
#ifdef CM_VM                                                                    
                                                                                
/*                                                                              
 * XC_INT32 should be a 32-bit, signed integer.  The following #define is       
 * system dependent and may need to be changed on systems where signed long     
 * int does not define a 32-bit, signed integer.                                
 */                                                                             
                                                                                
#define XC_INT32 signed long int                                                
#define XC_ENTRY extern void                                                    
#define XC_PTR *                                                                
                                                                                
                                                                                
typedef XC_INT32 CMINT;                                                         
typedef CMINT *PCMINT;                                                          
typedef unsigned char CMCHAR;                                                   
typedef CMCHAR *PCMCHAR;                                                        
                                                                                
                                                                                
typedef XC_INT32 XC_RESOURCE_MANAGER_TYPE;                                      
/*  resource_manager_type values  */                                            
#define XC_PRIVATE                    (XC_RESOURCE_MANAGER_TYPE) 0              
#define XC_LOCAL                      (XC_RESOURCE_MANAGER_TYPE) 1              
#define XC_GLOBAL                     (XC_RESOURCE_MANAGER_TYPE) 2              
#define XC_SYSTEM                     (XC_RESOURCE_MANAGER_TYPE) 3              
                                                                                
typedef XC_INT32 XC_SERVICE_MODE;                                               
/*  service_mode values  */                                                     
#define XC_SINGLE                     (XC_SERVICE_MODE) 0                       
#define XC_SEQUENTIAL                 (XC_SERVICE_MODE) 1                       
#define XC_MULTIPLE                   (XC_SERVICE_MODE) 2                       
                                                                                
typedef XC_INT32 XC_SECURITY_LEVEL_FLAG;                                        
/*  security_level_flag values  */                                              
#define XC_REJECT_SECURITY_NONE       (XC_SECURITY_LEVEL_FLAG) 0                
#define XC_ACCEPT_SECURITY_NONE       (XC_SECURITY_LEVEL_FLAG) 1                
                                                                                
typedef XC_INT32 XC_CONVERSATION_SECURITY_TYPE;                                 
/*  conversation_security_type values  */                                       
#define XC_SECURITY_NONE              (XC_CONVERSATION_SECURITY_TYPE) 0         
#define XC_SECURITY_SAME              (XC_CONVERSATION_SECURITY_TYPE) 1         
#define XC_SECURITY_PROGRAM           (XC_CONVERSATION_SECURITY_TYPE) 2         
                                                                                
typedef XC_INT32 XC_EVENT_TYPE;                                                 
/*  event_type values  */                                                       
#define XC_ALLOCATION_REQUEST         (XC_EVENT_TYPE) 1                         
#define XC_INFORMATION_INPUT          (XC_EVENT_TYPE) 2                         
#define XC_RESOURCE_REVOKED           (XC_EVENT_TYPE) 3                         
#define XC_CONSOLE_INPUT              (XC_EVENT_TYPE) 4                         
#define XC_REQUEST_ID                 (XC_EVENT_TYPE) 5                         
#define XC_USER_EVENT                 (XC_EVENT_TYPE) 6                         
                                                                                
CM_ENTRY xcecl( unsigned char CM_PTR,   /* conversation_ID          */          
                unsigned char CM_PTR,   /* luwid                    */          
                CM_INT32 CM_PTR,        /* luwid_length             */          
                CM_RETURN_CODE CM_PTR); /* return_code              */          
CM_ENTRY xcecsu(unsigned char CM_PTR,   /* conversation_ID          */          
                unsigned char CM_PTR,   /* security_user_ID         */          
                CM_INT32 CM_PTR,        /* security_user_ID_length  */          
                CM_RETURN_CODE CM_PTR); /* return_code              */          
CM_ENTRY xcecwu(unsigned char CM_PTR,   /* conversation_ID          */          
                CM_INT32 CM_PTR,        /* workunitid               */          
                CM_RETURN_CODE CM_PTR); /* return_code              */          
CM_ENTRY xcelfq(unsigned char CM_PTR,   /* conversation_ID          */          
                unsigned char CM_PTR,   /* local_fq_LU_name         */          
                CM_INT32 CM_PTR,        /* local_fq_LU_name_length  */          
                CM_RETURN_CODE CM_PTR); /* return_code              */          
CM_ENTRY xcerfq(unsigned char CM_PTR,   /* conversation_ID          */          
                unsigned char CM_PTR,   /* remote_fq_LU_name        */          
                CM_INT32 CM_PTR,        /* remote_fq_LU_name_length */          
                CM_RETURN_CODE CM_PTR); /* return_code              */          
CM_ENTRY xcetpn(unsigned char CM_PTR,   /* conversation_ID          */          
                unsigned char CM_PTR,   /* TP_name                  */          
                CM_INT32 CM_PTR,        /* TP_name_length           */          
                CM_RETURN_CODE CM_PTR); /* return_code              */          
CM_ENTRY xcidrm(unsigned char CM_PTR,   /* resource_ID              */          
                CM_INT32 CM_PTR,        /* resource_manager_type    */          
                CM_INT32 CM_PTR,        /* service_mode             */          
                CM_INT32 CM_PTR,        /* security_level_flag      */          
                CM_RETURN_CODE CM_PTR); /* return_code              */          
CM_ENTRY xcscsp(unsigned char CM_PTR,   /* conversation_ID          */          
                unsigned char CM_PTR,   /* security_password        */          
                CM_INT32 CM_PTR,        /* security_password_length */          
                CM_RETURN_CODE CM_PTR); /* return_code              */          
CM_ENTRY xcscst(unsigned char CM_PTR,   /* conversation_ID          */          
                CM_INT32 CM_PTR,        /* conv_security_type       */          
                CM_RETURN_CODE CM_PTR); /* return_code              */          
CM_ENTRY xcscsu(unsigned char CM_PTR,   /* conversation_ID          */          
                unsigned char CM_PTR,   /* security_user_ID         */          
                CM_INT32 CM_PTR,        /* security_user_ID_length  */          
                CM_RETURN_CODE CM_PTR); /* return_code              */          
CM_ENTRY xcscui(unsigned char CM_PTR,   /* conversation_ID          */          
                unsigned char CM_PTR,   /* client_user_ID           */          
                CM_RETURN_CODE CM_PTR); /* return_code              */          
CM_ENTRY xcsue( unsigned char CM_PTR,   /* event_ID                 */          
                unsigned char CM_PTR,   /* user_data                */          
                CM_INT32 CM_PTR,        /* user_data_length         */          
                CM_RETURN_CODE CM_PTR); /* return_code              */          
CM_ENTRY xctrrm(unsigned char CM_PTR,   /* resource_ID              */          
                CM_RETURN_CODE CM_PTR); /* return_code              */          
CM_ENTRY xcwoe( unsigned char CM_PTR,   /* resource_ID              */          
                unsigned char CM_PTR,   /* conversation_ID          */          
                CM_INT32 CM_PTR,        /* event_type               */          
                CM_INT32 CM_PTR,        /* info_input_length        */          
                unsigned char CM_PTR,   /* console_input_buffer     */          
                CM_RETURN_CODE CM_PTR); /* return_code              */          
                                                                                
#     pragma linkage (xcecl,  OS)                                               
#     pragma linkage (xcecsu, OS)                                               
#     pragma linkage (xcecwu, OS)                                               
#     pragma linkage (xcelfq, OS)                                               
#     pragma linkage (xcerfq, OS)                                               
#     pragma linkage (xcetpn, OS)                                               
#     pragma linkage (xcidrm, OS)                                               
#     pragma linkage (xcscsp, OS)                                               
#     pragma linkage (xcscst, OS)                                               
#     pragma linkage (xcscsu, OS)                                               
#     pragma linkage (xcscui, OS)                                               
#     pragma linkage (xcsue,  OS)                                               
#     pragma linkage (xctrrm, OS)                                               
#     pragma linkage (xcwoe,  OS)                                               
                                                                                
#endif                                                                          
                                                                                
#endif                                                                          
                                                                                
/* ********************* End of Pseudonyms ********************** */            
