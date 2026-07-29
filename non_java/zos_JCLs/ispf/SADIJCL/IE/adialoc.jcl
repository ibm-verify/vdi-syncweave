//ADIALOC  JOB 'ACCOUNTING INFORMATION','ALLOC TARG/DIST LIBS',                 
//  CLASS=A,MSGCLASS=A,MSGLEVEL=(1,1)                                           
//********************************************************************          
//********************************************************************          
//**                                                                **          
//** +----------------------------------------------------------+   **          
//** | Licensed Materials - Property of IBM                     |   **          
//** | 5698-B09 (C) . 2009, 2011              |   **          
//** | All rights reserved.                                     |   **          
//** |                                                          |   **          
//** | //** | or disclosure restricted by //** | with IBM Corp.                                           |   **          
//** +----------------------------------------------------------+   **          
//**                                                                **          
//**     PROCEDURE: ADIALOC                                         **          
//**                                                                **          
//**     FUNCTION:                                                  **          
//**        ALLOCATE THE ADI TARGET AND DISTRIBUTION                **          
//**        DATA SETS FOR IBM TIVOLI ADI VERSION 7 RELEASE 2 MOD 0  **          
//**     NOTE:                                                      **          
//**        ARROWS "<==" POINT TO LINES WHICH ARE RECOGNIZED        **          
//**        AS REQUIRING CUSTOMIZATION.  PARAMETERS AND             **          
//**        KEYWORDS NEEDING TO BE CUSTOMIZED ARE ENTERED           **          
//**        IN LOWER CASE TO MAKE THEM EASIER TO FIND.  ALL         **          
//**        JCL MUST BE IN UPPER CASE BEFORE SUBMITTING THE         **          
//**        JOB TO AVOID A JCL ERROR.  SPECIFIC ITEMS NEEDING       **          
//**        CUSTOMIZATION INCLUDE:                                  **          
//**           1. HIGH LEVEL FOR TGT AND DIST DSETS                 **          
//**           2. HIGH LEVEL FOR TARGET HFS DATA SET                **          
//**           3. UNIT TYPE FOR TARGET VOLUME                       **          
//**           4. VOLUME SERIAL OF TARGET VOLUME                    **          
//**              IF DATASETS ARE ALLOCATED BUT NOT CATALOGED,      **          
//**              THIS VOLSER MUST ALSO BE SUPPLIED IN THE          **          
//**              DDDEF CONTROL STATEMENTS.                         **          
//**           5. UNIT TYPE FOR DISTRIBUTION VOLUME                 **          
//**           6. VOLUME SERIAL OF DISTRIBUTION VOLUME.             **          
//**              IF DATASETS ARE ALLOCATED BUT NOT CATALOGED,      **          
//**              THIS VOLSER MUST ALSO BE SUPPLIED IN THE          **          
//**              DDDEF CONTROL STATEMENTS.                         **          
//**              IF YOU SPECIFY A VOLUME FOR ANY DATASET IN        **          
//**              THIS JOB, YOU MUST ALSO SPECIFY THE SAME          **          
//**              VOLUME IN THE CORRESPONDING DDDEF ENTRY IN        **          
//**              THE DDDEF JOB, ADIDDEF.                           **          
//**           7. UNIT TYPE FOR HFS VOLUME                          **          
//**           8. VOLUME SERIAL OF HFS VOLUME                       **          
//**                                                                **          
//**     EXPECTED COND CODE: 0000                                   **          
//**                                                                **          
//**     ACTIVITY:                                                  **          
//********************************************************************          
//********************************************************************          
//ADIALOC  PROC HLQ=,TUNIT=,DUNIT=,TVOL1=,DVOL=                                 
//ALLOC1  EXEC PGM=IEFBR14                                                      
//***************************************************************               
//** TARGET LIBRARIES FOR IBM Tivoli Directory Integrator      **               
//***************************************************************               
//SADIJCL    DD DSN=&HLQ..SADIJCL,                                              
//           UNIT=&TUNIT,                                                       
//           VOL=SER=&TVOL1,                                                    
//           SPACE=(TRK,(4,5,5)),                                               
//           DCB=(LRECL=80,RECFM=FB,BLKSIZE=0),                                 
//           DISP=(NEW,CATLG)                                                   
//***************************************************************               
//* DISTRIBUTION LIBS FOR IBM Tivoli Directory Integrator      **               
//***************************************************************               
//AADIJCL    DD DSN=&HLQ..AADIJCL,                                              
//           UNIT=&TUNIT,                                                       
//           VOL=SER=&TVOL1,                                                    
//           SPACE=(TRK,(4,5,5)),                                               
//           DCB=(LRECL=80,RECFM=FB,BLKSIZE=0),                                 
//           DISP=(NEW,CATLG)                                                   
//AADIDIST   DD DSN=&HLQ..AADIDIST,                                             
//           UNIT=&DUNIT,                                                       
//           VOL=SER=&DVOL,                                                     
//           SPACE=(TRK,(2000,100,40)),                                         
//           DCB=(LRECL=3275,RECFM=VB,BLKSIZE=0),                               
//           DISP=(NEW,CATLG)                                                   
//         PEND                                                                 
//*                                                                             
//ALLOCATE EXEC ADIALOC,                                                        
//            HLQ=adi,                <==1 DATA SET HIGH LEVEL                  
//            TUNIT=sysallda,         <==3 TGT  LIB UNIT TYPE                   
//            TVOL1=tvol,             <==4 TGT  LIB VOLSER                      
//            DUNIT=sysallda,         <==5 DIST LIB UNIT TYPE                   
//            DVOL=dvol               <==6 DIST LIB VOLSER                      
//*                                                                             
//***************************************************************               
//** ALLOCATE A TARGET HFS DATASET TO MOUNT IN YOUR z/390      **               
//** UNIX SYSTEM SERVICES ENVIRONMENT.  IF YOU WANT TO         **               
//** ALLOCATE A TARGET HFS DATASET TO INSTALL YOUR ADI         **               
//** CODE INTO, UNCOMMENT THE FOLLOWING STEP.                  **               
//***************************************************************               
//*ALLOC2  EXEC PGM=IEFBR14                                                     
//*HFS001     DD DSN=adi.HFS,            <==2   TGT HFS DATASET                 
//*           UNIT=sysallda,             <==7   HFS UNIT TYPE                   
//*           VOL=SER=hfsvol,            <==8   HFS SMS-MANAGED VOLSER          
//*           SPACE=(TRK,(2200,5,0)),                                           
//*           DCB=(DSORG=PO),                                                   
//*           DISP=(NEW,CATLG),                                                 
//*           DSNTYPE=HFS,                                                      
//*           STORCLAS=classe             <==12 STORAGE CLASS                   

