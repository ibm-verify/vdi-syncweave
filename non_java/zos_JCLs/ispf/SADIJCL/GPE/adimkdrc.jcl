//ADIMKDRC JOB 'ACCOUNTING INFORMATION','CREATE MOUNT PT DIR',                  
//  CLASS=A,MSGCLASS=A,MSGLEVEL=(1,1)                                           
//***************************************************************               
//** Licensed Materials - Property of IBM                      **               
//** 5698-B33 (C) COPYRIGHT IBM CORP. 2007, 2011               **               
//** All rights reserved.                                      **               
//**                                                           **               
//** US Government Users Restricted Rights - Use, duplication  **               
//** or disclosure restricted by GSA ADP Schedule Contract     **               
//** with IBM Corp.                                            **               
//***************************************************************               
//**                                                           **               
//**     PROCEDURE:  ADIMKDRC                                  **               
//**                                                           **               
//**     FUNCTION:                                             **               
//**        THIS JCL WILL EXECUTE THE ADIMKDRD EXEC TO         **               
//**        CREATE THE HIERARCHICAL FILE SYSTEM (HFS)          **               
//**        MOUNT POINT DIRECTORY TO MOUNT YOUR IBM TIVOLI     **               
//**        DIRECTORY INTEGRATOR HFS DATA SET.                 **               
//**                                                           **               
//**     EXPECTED CONDITION CODE: 0000                         **               
//**                                                           **               
//**     NOTE:                                                 **               
//**        BEFORE RUNNING THIS JOB, CONSIDER THE FOLLOWING:   **               
//**                                                           **               
//**        1) CHANGE THE JOB CARD TO MEET YOUR SYSTEM         **               
//**           REQUIREMENTS                                    **               
//**                                                           **               
//**        2) CHANGE THE "//SYSEXEC DD" HIGH LEVEL QUALIFIER  **               
//**           TO YOUR INSTALLATION'S 'DSPREFIX'.  THE DEFAULT **               
//**           VALUE GIVE WHEN USING THE ITDI SUPPLIED         **               
//**           INSTALL SAMPLES IS "itdi".                      **               
//**                                                           **               
//**        3) CHANGE THE STRING "<PathPrefix>" TOWARDS THE    **               
//**           BOTTOM OF THIS JOB TO THE APPROPRIATE HIGH      **               
//**           LEVEL DIRECTORY NAME.  FOR USERS INSTALLING     **               
//**           IN THE ROOT, THIS WOULD BE "/" (WITHOUT THE     **               
//**           QUOTES).  FOR OTHERS, THE HIGH LEVEL DIRECTORY  **               
//**           MAY BE SOMETHING LIKE "/SERVICE" (WITHOUT THE   **               
//**           QUOTES) OR A MORE MEANINGFUL NAME.  PLEASE      **               
//**           NOTE THE REPLACEMENT STRING IS CASE SENSITIVE.  **               
//**           ENSURE THE <PathPrefix> IS AN ABSOLUTE PATH     **               
//**           NAME AND BEGINS WITH A SLASH (/).               **               
//**                                                           **               
//**        4) ENSURE THE DIRECTORY SPECIFIED BY <PathPrefix>  **               
//**           EXISTS PRIOR TO RUNNING THIS JOB.  THE MOUNT    **               
//**           POINT DIRECTORY WILL BE CREATED UNDER           **               
//**           <PathPrefix>/usr/lpp/.                          **               
//**                                                           **               
//**        5) CHANGE THE SYSOUT CLASS TO USE A HELD OUTPUT    **               
//**           CLASS.                                          **               
//**                                                           **               
//**        6) ENSURE YOU EXECUTE THIS JOB FROM A USERID THAT  **               
//**           IS UID=0 OR IS PERMITTED TO THE 'BPX.SUPERUSER' **               
//**           FACILITY CLASS.                                 **               
//**                                                           **               
//**        7) THIS JOB SHOULD END WITH RC=0. IF NOT THEN,     **               
//**           PLEASE CHECK OS/390 UNIX MESSAGES AND CODES     **               
//**           BOOK TO CORRECT THE PROBLEM AND RESUBMIT THIS   **               
//**           JOB.                                            **               
//**                                                           **               
//**     ACTIVITY:                                             **               
//***************************************************************               
//*                                                                             
//IKJEFT01 EXEC PGM=IKJEFT01                                                    
//SYSEXEC DD DSN=adi.HADI711.F1,      /* <==2 YOUR DSPREFIX    */               
//      DISP=SHR                                                                
//SYSTSPRT DD SYSOUT=H          /* <==5 YOUR HELD OUTPUT CLASS */               
//SYSTSIN DD *                                                                  
PROF MSGID                                                                      
ADIMKDRD <PathPrefix>                                                           
/*                                                                              
//                                                                              
