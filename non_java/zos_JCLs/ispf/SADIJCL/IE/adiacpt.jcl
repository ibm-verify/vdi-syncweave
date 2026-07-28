//ADIACPT  JOB 'ACCOUNTING INFORMATION','SMP/E ACCEPT',                         
//  CLASS=A,MSGCLASS=A,MSGLEVEL=(1,1)                                           
//********************************************************************          
//**                                                                **          
//** +----------------------------------------------------------+   **          
//** | Licensed Materials - Property of IBM                     |   **          
//** | 5698-B09 (C) . 2009, 2011              |   **          
//** | All rights reserved.                                     |   **          
//** |                                                          |   **          
//** | US Government Users Restricted Rights - Use, duplication |   **          
//** | or disclosure restricted by GSA ADP Schedule Contract    |   **          
//** | with IBM Corp.                                           |   **          
//** +----------------------------------------------------------+   **          
//**                                                                **          
//**     PROCEDURE:  ADIACPT                                        **          
//**                                                                **          
//**     FUNCTION:                                                  **          
//**        UPDATE THE DLIB ZONE WITH NEW FUNCTIONS                 **          
//**        AND POPULATE THE DISTRIBUTION LIBRARIES.                **          
//**                                                                **          
//**        ACCEPT ALL APPLIED FUNCTIONS                            **          
//**        ACCEPT THE HADI711 AND JADI71Y FMID                     **          
//**        (IBM TIVOLI DIRECTORY INTEGRATOR IDENTITY EDITION)      **          
//**                                                                **          
//**     NOTE:                                                      **          
//**        ARROWS "<==" POINT TO LINES WHICH ARE RECOGNIZED        **          
//**        AS REQUIRING CUSTOMIZATION.  PARAMETERS AND             **          
//**        KEYWORDS NEEDING TO BE CUSTOMIZED ARE ENTERED           **          
//**        IN LOWER CASE TO MAKE THEM EASIER TO FIND.  ALL         **          
//**        JCL MUST BE IN UPPER CASE BEFORE SUBMITTING THE         **          
//**        JOB TO AVOID A JCL ERROR.  SPECIFIC ITEMS NEEDING       **          
//**        CUSTOMIZATION INCLUDE:                                  **          
//**                    1. CHANGE adi.GLOBAL.CSI TO                 **          
//**                       THE DATASET NAME OF YOUR GLOBAL          **          
//**                       CSI.                                     **          
//**                    2. YOUR TARGET ZONE NAME                    **          
//**                    3. ALL OF THE ITDI FMIDS FOR                **          
//**                       THIS COMPONENT ARE LISTED ON THE         **          
//**                       ACCEPT  BELOW.                           **          
//**                    4. UNCOMMENT THE CHECK OPERAND IF           **          
//**                       YOU WANT TO DO AN ACCEPT CHECK           **          
//**                       BEFORE THE ACCEPT.                       **          
//**                                                                **          
//**      >>>> BE SURE TO CHECK THAT THE FMIDS THAT YOU             **          
//**      >>>> RECEIVED MATCH THE ONES THAT YOU ARE ABOUT           **          
//**      >>>> TO ACCEPT.                                           **          
//**                                                                **          
//**                                                                **          
//**     EXPECTED COND CODE: 0000                                   **          
//**                                                                **          
//**     ACTIVITY:                                                  **          
//**                                                                **          
//********************************************************************          
//********************************************************************          
//ADIACPT  EXEC PGM=GIMSMP,REGION=0M                                            
//SMPCSI   DD   DSN=adi.GLOBAL.CSI,DISP=SHR          /* <==1 SMPCSI */          
//SYSUT1   DD   UNIT=SYSDA,SPACE=(1700,(900,200))                               
//SYSUT2   DD   UNIT=SYSDA,SPACE=(1700,(600,100))                               
//SYSUT3   DD   UNIT=SYSDA,SPACE=(1700,(600,100))                               
//SYSUT4   DD   UNIT=SYSDA,SPACE=(1700,(600,100))                               
//SMPCNTL  DD  *                                                                
  SET      BDY(#dzone)  .        /*  <==2 YOUR DISTRIBUTION ZONE    */          
  ACCEPT SELECT                                                                 
     (                           /*  <==3 FMIDS                     */          
     HADI711,                    /* IBM TIVOLI DIRECTORY INTEGRATOR */          
     JADI71Y                     /* IDENTITY EDITION                */          
     )                                                                          
     /* CHECK                    /*  <==4  CHECK OPERAND            */          
  .                                                                             
/*                                                                              
//                                                                              
                                                                                