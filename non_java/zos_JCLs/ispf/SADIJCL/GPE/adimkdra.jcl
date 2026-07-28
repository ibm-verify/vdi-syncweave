//ADIMKDRA JOB 'ACCOUNTING INFORMATION','CREATE HFS DIRECTORY',                 
//  CLASS=A,MSGCLASS=A,MSGLEVEL=(1,1)                                           
//***************************************************************               
//** Licensed Materials - Property of IBM                      **               
//** 5698-B33 (C) . 2007, 2011               **               
//** All rights reserved.                                      **               
//**                                                           **               
//** //** or disclosure restricted by //** with IBM Corp.                                            **               
//***************************************************************               
//**                                                           **               
//**     PROCEDURE:  ADIMKDRA                                  **               
//**                                                           **               
//**     FUNCTION:                                             **               
//**        THIS JCL WILL EXECUTE THE ADIMKDRB EXEC TO CREATE  **               
//**        THE TIVOLI ITDI HIERARCHICAL FILE SYSTEM           **               
//**        (HFS) STRUCTURE FOR YOUR TARGET LIBRARIES FOR      **               
//**        OS/390 UNIX SYSTEM SERVICES RELATED COMPONENTS     **               
//**        (JAPANESE) AND WILL CREATE ANY SYMBOLIC LINKS AS   **               
//**        NEEDED                                             **               
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
//**           EXISTS PRIOR TO RUNNING THIS JOB.  DIRECTORIES  **               
//**           WILL BE CREATED UNDER <PathPrefix>/usr/lpp/.    **               
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
//SYSEXEC DD DSN=adi.HADI711.F1,      /* <==2 DSPREFIX VALUE   */               
//      DISP=SHR                                                                
//SYSTSPRT DD SYSOUT=H          /* <==5 YOUR HELD OUTPUT CLASS */               
//SYSTSIN DD *                                                                  
PROF MSGID                                                                      
ADIMKDRB <PathPrefix>                                                           
/*                                                                              
