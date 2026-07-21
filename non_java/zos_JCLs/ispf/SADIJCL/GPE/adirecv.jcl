//ADIRECV  JOB 'ACCOUNTING INFORMATION','SMP/E RECEIVE',                        
//  CLASS=A,MSGCLASS=A,MSGLEVEL=(1,1)                                           
//********************************************************************          
//**                                                                **          
//** +----------------------------------------------------------+   **          
//** | Licensed Materials - Property of IBM                     |   **          
//** | 5698-B33 (C) COPYRIGHT IBM CORP. 2007, 2011              |   **          
//** | All rights reserved.                                     |   **          
//** |                                                          |   **          
//** | US Government Users Restricted Rights - Use, duplication |   **          
//** | or disclosure restricted by GSA ADP Schedule Contract    |   **          
//** | with IBM Corp.                                           |   **          
//** +----------------------------------------------------------+   **          
//**                                                                **          
//**   PROCEDURE:  ADIRECV                                          **          
//**                                                                **          
//**   FUNCTION:                                                    **          
//**      THE FMIDs ARE ADDED TO THE SMP/E DATABASE AND THE         **          
//**      REL FILES ASSOCIATED WITH THE FMIDs ARE UNLOADED          **          
//**      FROM THE TAPE AND PLACED ON THE TARGET VOLUME.            **          
//**                                                                **          
//**      RECEIVE HADI711 FMID                                      **          
//**     (IBM TIVOLI DIRECTORY INTEGRATOR GENERAL PURPOSE EDITION)  **          
//**                                                                **          
//**  NOTE:                                                         **          
//**        ARROWS "<==" POINT TO LINES WHICH ARE RECOGNIZED        **          
//**        AS REQUIRING CUSTOMIZATION.  PARAMETERS AND             **          
//**        KEYWORDS NEEDING TO BE CUSTOMIZED ARE ENTERED           **          
//**        IN LOWER CASE TO MAKE THEM EASIER TO FIND.  ALL         **          
//**        JCL MUST BE IN UPPER CASE BEFORE SUBMITTING THE         **          
//**        JOB TO AVOID A JCL ERROR.  SPECIFIC ITEMS NEEDING       **          
//**        CUSTOMIZATION INCLUDE:                                  **          
//**                    1. CHANGE:  adi.GLOBAL.CSI                  **          
//**                                adi.SMPLOG                      **          
//**                                adi.SMPPTS                      **          
//**                       TO THE DATASET NAMES OF YOUR             **          
//**                       INSTALLATION                             **          
//**                    2. YOUR TARGET VOLUME                       **          
//**                    3. YOUR UNIT DEVICE FOR TAPE DRIVES         **          
//**                                                                **          
//**                                                                **          
//**     EXPECTED COND CODE: 0000                                   **          
//**                                                                **          
//**     ACTIVITY:                                                  **          
//**                                                                **          
//********************************************************************          
//********************************************************************          
//ADIRECV  EXEC PGM=GIMSMP,REGION=0M                                            
//SMPCSI   DD   DSN=adi.GLOBAL.CSI,DISP=SHR          /* <==1 SMPCSI */          
//SMPLOG   DD   DSN=adi.SMPLOG,DISP=SHR              /* <==1 SMPLOG */          
//SMPPTS   DD   DSN=adi.SMPPTS,DISP=SHR              /* <==1 SMPPTS */          
//SYSUT1   DD   UNIT=SYSDA,SPACE=(1700,(1800,400))                              
//SYSUT2   DD   UNIT=SYSDA,SPACE=(1700,(1200,200))                              
//SYSUT3   DD   UNIT=SYSDA,SPACE=(1700,(1200,200))                              
//SYSUT4   DD   UNIT=SYSDA,SPACE=(1700,(1200,200))                              
//SMPTLIB  DD  UNIT=SYSALLDA,DISP=OLD,                                          
//             VOL=SER=ttttt1                   /* <==2 TLIB VOLUME */          
//SMPPTFIN DD  DSN=SMPMCS,VOL=SER=ADI720,                                       
//             UNIT=3480,LABEL=(1,SL),          /* <==3 UNIT DEVICE */          
//             DISP=OLD                                                         
//SMPCNTL  DD  *                                                                
  SET      BDY(GLOBAL)  .                                                       
  RECEIVE  S(                                                                   
             HADI711     /* IBM TIVOLI DIRECTORY INTEGRATOR         */          
            )                                                                   
           SYSMODS                                                              
           LIST                                                                 
           .                                                                    
/*                                                                              
//                                                                              
