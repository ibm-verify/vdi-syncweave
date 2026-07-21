//ADIDDEF JOB 'ACCOUNTING INFORMATION','PROGRAMMER NAME',                       
//  CLASS=A,MSGCLASS=A,MSGLEVEL=(1,1)                                           
//********************************************************************          
//********************************************************************          
//**                                                                **          
//** +----------------------------------------------------------+   **          
//** | Licensed Materials - Property of IBM                     |   **          
//** | 5698-B33 (C) Copyright IBM Corp. 2007, 2011              |   **          
//** | All rights reserved.                                     |   **          
//** |                                                          |   **          
//** | US Government Users Restricted Rights - Use, duplication |   **          
//** | or disclosure restricted by GSA ADP Schedule Contract    |   **          
//** | with IBM Corp.                                           |   **          
//** +----------------------------------------------------------+   **          
//**                                                                **          
//**     PROCEDURE:  ADIDDEF                                        **          
//**                                                                **          
//**     FUNCTION:                                                  **          
//**        DEFINE IBM Tivoli Directory Integrator General Purpose  **          
//**        Edition DATA SETS TO YOUR SMP/E ENVIRONMENT             **          
//**                                                                **          
//**        ADD DD DEFINITIONS TO TARGET AND DIST. ZONES            **          
//**                                                                **          
//**     NOTE:                                                      **          
//**        ARROWS "<==" POINT TO LINES WHICH ARE RECOGNIZED        **          
//**        AS REQUIRING CUSTOMIZATION.  PARAMETERS AND             **          
//**        KEYWORDS NEEDING TO BE CUSTOMIZED ARE ENTERED           **          
//**        IN LOWER CASE TO MAKE THEM EASIER TO FIND.  ALL         **          
//**        JCL MUST BE IN UPPER CASE BEFORE SUBMITTING THE         **          
//**        JOB TO AVOID A JCL ERROR.  SPECIFIC ITEMS NEEDING       **          
//**        CUSTOMIZATION INCLUDE:                                  **          
//**           1. CHANGE #globalcsi TO THE DATASET NAME OF YOUR     **          
//**              GLOBAL CSI. CHANGE #smplog TO THE DATASET NAME    **          
//**              OF YOUR SMPLOG.                                   **          
//**           2. YOUR SMP/E TARGET ZONE                            **          
//**           3. YOUR SMP/E DLIB ZONE                              **          
//**           4. CHANGE THE <PathPrefix> STRING TO THE             **          
//**              APPROPRIATE HIGH LEVEL DIRECTORY NAME.            **          
//**           5. CHANGE "adi." TO THE APPROPRIATE HIGH-LEVEL       **          
//**              QUALIFIER IF YOU CHOOSE TO NOT USE THE DEFAULT.   **          
//**              IF YOU USE AN EDITOR CHANGE COMMAND BE SURE TO    **          
//**              INCLUDE THE PERIOD AT THE END OF THE HIGH-LEVEL   **          
//**              QUALIFIER.                                        **          
//**                                                                **          
//**                                                                **          
//**     NOTE:                                                      **          
//**       IF THE DATASETS ARE NOT CATALOGED, USE THE VOLUME        **          
//**       PARAMETER TO SPECIFY THE VOLSER OF THE DATASET           **          
//**       REFERENCED.                                              **          
//**                                                                **          
//**       IF YOU SPECIFY A VOLUME FOR ANY DATASET IN THIS          **          
//**       JOB, YOU MUST ALSO SPECIFY THE SAME VOLUME IN            **          
//**       THE CORRESPONDING DATASET ALLOCATION JOB,ADIALOC.        **          
//**                                                                **          
//**       IN THIS JOB, 'tvol1' SHOULD BE CHANGED TO THE VOLUME     **          
//**       SERIAL NUMBER WHERE YOU WILL PUT YOUR ITDI TARGET        **          
//**       LIBRARIES. 'dvol' SHOULD BE CHANGED TO THE VOLUME        **          
//**       SERIAL NUMBER WHERE YOU WILL PUT YOUR ITDI               **          
//**       DISTRIBUTION LIBRARIES.                                  **          
//**                                                                **          
//**                                                                **          
//**  EXPECTED COND CODE: 0000 IF DEFINITIONS DO NOT EXIST          **          
//**                      0008 IF DEFINITIONS ALREADY EXIST         **          
//**                                                                **          
//**                                                                **          
//**  IF SOME OR ALL OF THE DDDEF ENTRIES ALREADY EXIST, THEN THE   **          
//**  JOB WILL COMPLETE WITH A RETURN CODE 8. YOU WILL HAVE TO      **          
//**  EXAMINE THE OUTPUT AND DETERMINE WHETHER OR NOT THE EXISTING  **          
//**  ENTRIES SHOULD BE REPLACED. YOU CAN CHANGE THE 'ADD' TO       **          
//**  'REP' IN THIS JOB TO REPLACE EXISTING ENTRIES.  FOR EXAMPLE   **          
//**  ADD DDDEF(SADIJCL ) WOULD CHANGE TO REP DDDEF(SADIJCL ).      **          
//**                                                                **          
//********************************************************************          
//DDDEF1   EXEC PGM=GIMSMP,REGION=0M                                            
//SMPCSI   DD   DSN=#globalcsi,DISP=SHR /* <==1 SMPCSI */                       
//SMPLOG   DD   DSN=#smplog,DISP=MOD     /* <==1 SMPLOG */                      
//SMPCNTL  DD   *                                                               
  SET BDY(#tzone)                 /* <==2 YOUR SMP/E TARGET ZONE */             
  .                                                                             
  UCLIN.                                                                        
   ADD DDDEF(SADIJCL )                                                          
       DA(adi.SADIJCL )           /* <==5 HIGH-LEVEL QUALIFIER   */             
       UNIT(SYSALLDA)                                                           
       VOLUME(tvol1)                                                            
       WAITFORDSN                                                               
       SHR.                                                                     
   ADD DDDEF(AADIJCL )                                                          
       DA(adi.AADIJCL )           /* <==5 HIGH-LEVEL QUALIFIER   */             
       UNIT(SYSALLDA)                                                           
       VOLUME(dvol)                                                             
       WAITFORDSN                                                               
       SHR.                                                                     
   ADD DDDEF(AADIDIST)                                                          
       DA(adi.AADIDIST)           /* <==5 HIGH-LEVEL QUALIFIER   */             
       UNIT(SYSALLDA)                                                           
       VOLUME(dvol)                                                             
       WAITFORDSN                                                               
       SHR.                                                                     
   /*  DO NOT UPPER CASE ANY OF THE PATH VALUES SPECIFIED BELOW   */            
   ADD DDDEF(SADI0001)                                                          
       PATH('/usr/lpp/itdi/IBM/').                                              
   ADD DDDEF(SADI0002)                                                          
       PATH('/usr/lpp/itdi/bin/IBM/').                                          
   ADD DDDEF(SADI0003)                                                          
       PATH('/usr/lpp/itdi/etc/IBM/').                                          
   ADD DDDEF(SADI0004)                                                          
       PATH('/usr/lpp/itdi/jars/3rdparty/IBM/IBM/').                            
   ADD DDDEF(SADI0005)                                                          
       PATH('/usr/lpp/itdi/jars/3rdparty/others/IBM/').                         
   ADD DDDEF(SADI0006)                                                          
       PATH('/usr/lpp/itdi/jars/3rdparty/others/emf/IBM/').                     
   ADD DDDEF(SADI0007)                                                          
       PATH('/usr/lpp/itdi/jars/common/IBM/').                                  
   ADD DDDEF(SADI0008)                                                          
       PATH('/usr/lpp/itdi/jars/connectors/IBM/').                              
   ADD DDDEF(SADI0010)                                                          
       PATH('/usr/lpp/itdi/jars/functions/IBM/').                               
   ADD DDDEF(SADI0011)                                                          
       PATH('/usr/lpp/itdi/jars/parsers/IBM/').                                 
   ADD DDDEF(SADI0012)                                                          
       PATH('/usr/lpp/itdi/jars/plugins/IBM/').                                 
   ADD DDDEF(SADI0013)                                                          
       PATH('/usr/lpp/itdi/license/IBM/').                                      
   ADD DDDEF(SADI0014)                                                          
       PATH('/usr/lpp/itdi/tools/CSMigration/IBM/').                            
   ADD DDDEF(SADI0015)                                                          
       PATH('/usr/lpp/itdi/serverapi/IBM/').                                    
   ADD DDDEF(SADI0016)                                                          
       PATH('/usr/lpp/itdi/tools/IBM/').                                        
   ADD DDDEF(SADI0018)                                                          
       PATH('/usr/lpp/itdi/libs/IBM/').                                         
   ADD DDDEF(SADI0019)                                                          
       PATH('/usr/lpp/itdi/bin/amc/ActionManager/jars/IBM/').                   
   ADD DDDEF(SADI0020)                                                          
       PATH('/usr/lpp/itdi/bin/amc/IBM/').                                      
   ADD DDDEF(SADI0021)                                                          
       PATH('/usr/lpp/itdi/bin/amc/ActionManager/IBM/').                        
   ADD DDDEF(SADI0022)                                                          
       PATH('/usr/lpp/itdi/tso_fc/IBM/').                                       
   ADD DDDEF(SADI0023)                                                          
       PATH('/usr/lpp/itdi/jars/3rdparty/IBM/gla/IBM/').                        
   ADD DDDEF(SADI0024)                                                          
       PATH('/usr/lpp/itdi/xsd/gla/schema/IBM/').                               
   ADD DDDEF(SADI0025)                                                          
       PATH('/usr/lpp/itdi/jars/IBM/').                                         
   ADD DDDEF(SADI0026)                                                          
       PATH('/usr/lpp/itdi/jars/3rdparty/IBM/axis2/IBM/').                      
   ADD DDDEF(SADI0028)                                                          
       PATH('/usr/lpp/itdi/osgi/IBM/').                                         
   ADD DDDEF(SADI0029)                                                          
       PATH('/usr/lpp/itdi/osgi/plugins/IBM/').                                 
   ADD DDDEF(SADI0030)                                                          
       PATH('/usr/lpp/itdi/osgi/configuration/IBM/').                           
   ADD DDDEF(SADI0031)                                                          
       PATH('/usr/lpp/itdi/jars/3rdparty/IBM/cdm/IBM/').                        
   ADD DDDEF(SADI0032)                                                          
       PATH('/usr/lpp/itdi/jars/3rdparty/IBM/IT_registry/IBM/').                
   ADD DDDEF(SADI0033)                                                          
       PATH('/usr/lpp/itdi/jars/3rdparty/others/ActiveMQ/IBM/').                
   ENDUCL.                                                                      
/*                                                                              
//DDDEF2   EXEC PGM=GIMSMP,REGION=0M                                            
//SMPCSI   DD   DSN=#globalcsi,DISP=SHR          /* <==1 SMPCSI */              
//SMPLOG   DD   DSN=#smplog,DISP=MOD             /* <==1 SMPLOG */              
//SMPCNTL  DD   *                                                               
  SET BDY(#dzone)                /* <==3 YOUR SMP/E DLIB ZONE   */              
  .                                                                             
  UCLIN.                                                                        
   ADD DDDEF(AADIJCL )                                                          
       DA(adi.AADIJCL )          /* <==5 HIGH-LEVEL QUALIFIER   */              
       UNIT(SYSALLDA)                                                           
       VOLUME(dvol)                                                             
       WAITFORDSN                                                               
       SHR.                                                                     
   ADD DDDEF(AADIDIST)                                                          
       DA(adi.AADIDIST)          /* <==5 HIGH-LEVEL QUALIFIER   */              
       UNIT(SYSALLDA)                                                           
       VOLUME(dvol)                                                             
       WAITFORDSN                                                               
       SHR.                                                                     
  ENDUCL.                                                                       
/*                                                                              
/*                                                                              
//********************************************************************          
//**  Change the <PathPrefix> string to the appropriate             **          
//**  high level directory name.  If you are installing in          **          
//**  the path as defined, change "<PathPrefix>" to ""              **          
//**  (null).  If you are upgrading releases or installing          **          
//**  maintenance, change "<PathPrefix>" to "/Service" or a         **          
//**  more meaningful name.  Please note that the                   **          
//**  replacement string is case sensitive.                         **          
//**                                                                **          
//**  Please verify that the changed path statements do not         **          
//**  contain double slashes (such as //usr/lpp) prior to           **          
//**  running this step.                                            **          
//********************************************************************          
//DDDEF3   EXEC PGM=GIMSMP,REGION=0M                                            
//SMPCSI   DD   DSN=#globalcsi,DISP=SHR               /* <==1 SMPCSI */         
//SMPLOG   DD   DSN=#smplog,DISP=MOD                  /* <==1 SMPLOG */         
//SMPCNTL  DD   *                                                               
  SET BDY(#tzone)                     /* <==2 YOUR SMP/E TARGET ZONE */         
  .                                                                             
  ZONEEDIT DDDEF.                                                               
   CHANGE PATH('/usr/lpp/itdi'*,                                                
        '<PathPrefix>/usr/lpp/itdi'*).         /* <==4 <PathPrefix>  */         
  ENDZONEEDIT.                                                                  
/*                                                                              
//                                                                              
