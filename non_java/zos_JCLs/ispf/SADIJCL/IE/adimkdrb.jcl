/* REXX */                                                                      
/*                                                                  */          
/********************************************************************/          
/*                                                                  */          
/*    Licensed Materials - Property of IBM                          */          
/*    5698-B09 (C) . 2009, 2011                   */          
/*    ALL RIGHTS RESERVED.                                          */          
/*                                                                  */          
/*    /*    - USE, DUPLICATION OR  DISCLOSURE RESTRICTED BY               */          
/*      /*                                                                  */          
/*                                                                  */          
/********************************************************************/          
/*                                                                  */          
/* THIS REXX EXEC WILL CREATE THE MOUNT POINT DIRECTORY IN YOUR     */          
/* OS/390 UNIX SYSTEM SERVICES ENVIRONMENT TO MOUNT THE IBM TIVOLI  */          
/* DIRECTORY INTEGRATOR IDENTITY EDITION HFS-TYPE DATASET TO.       */          
/*                                                                  */          
/********************************************************************/          
                                                                                
idir='usr/lpp/itdi'          /* <== This is your ITDI directory     */          
                                                                                
parse arg $root .                                                               
If $root='' then                                                                
   $root = "/"                           /* Add a trailing slash    */          
 Else                                    /* if none exists on input */          
   $root=strip($root,'T','/')'/'                                                
                                                                                
/****************************************************************/              
/*     Beginning of main procedure:                             */              
/****************************************************************/              
                                                                                
firstchar = Substr($root,1,1)                                                   
If firstchar \= "/" then                                                        
 Do                                                                             
  say '  Directory name does NOT begin with a "/".'                             
  say '  Please correct and resubmit.'                                          
  Exit 12                                                                       
 End                                                                            
                                                                                
if syscalls(on)>4 then                                                          
 Do                                                                             
  say '  Initialization failure. Please correct and resubmit.'                  
  Exit 12                                                                       
 End                                                                            
                                                                                
$rc= '0'                                                                        
                                                                                
SINDEX.=0                                                                       
                                                                                
flag1="OFF"                                                                     
flag2="OFF"                                                                     
flag3="OFF"                                                                     
flag4="OFF"                                                                     
flag7="OFF"                                                                     
                                                                                
 say '  The EXEC to create the mount point directory has begun.'                
 say '  It will run for a couple of minutes.'                                   
                                                                                
call saym 1, '  The EXEC ran at ' TIME() ' on ' DATE()                          
call saym 1, '                   '                                              
                                                                                
address syscall 'getuid'                                                        
myuid=retval                                                                    
address syscall 'geteuid'                                                       
myeuid=retval                                                                   
privflag=0                         /* Change the Effective UID to  */           
If myeuid \= 0 then                /* be 0 since invoker should    */           
  Do                               /* be part of the BPX.SUPERUSER */           
   address syscall 'seteuid 0'     /* facility class               */           
   privflag=1                                                                   
  End                                                                           
                                                                                
address syscall 'access' $root F_OK                                             
                                                                                
If (rc=0 & retval\=0) | rc\= 0 then                                             
 Do                                                                             
  If errno = 6F then           /* EACCESS */                                    
  Do                                                                            
  call saym 1, '  Do not have appropriate permission to' $root                  
  call saym 1, '                                 '                              
  call saym 1, '  Please get appropriate permission to this directory',         
               '  and resubmit.'                                                
  call saym 1, '                                 '                              
  End                                                                           
Else                                                                            
 Do                                                                             
  call saym 1, '  Directory' $root 'does not exist.'                            
  call saym 1, '                                 '                              
  call saym 1, '  Please create this directory and resubmit.'                   
  call saym 1, '                                 '                              
 End                                                                            
$rc=12                                                                          
End                                                                             
Else                                                                            
 Do                                                                             
  flag1="ON"                                                                    
  call saym 2, '  Created the following directory(s):'                          
  call saym 2, '  ==================================='                          
                                                                                
  call saym 3, '  The following directory(s) already exist:'                    
  call saym 3, '  ========================================='                    
                                                                                
  call saym 4, '  Problems creating the following directory(s):'                
  call saym 4, '  ============================================='                
                                                                                
  call saym 5, '  '                                                             
                                                                                
  call saym 6, '  '                                                             
                                                                                
  call syscall0 mkdir $root || idir 755                                         
                                                                                
  call checkit                                                                  
                                                                                
End                                                                             
                                                                                
If privflag=1 then                                                              
  address syscall 'seteuid' myuid                                               
                                                                                
 say '  The EXEC has completed with Return Code' $rc                            
                                                                                
                                                                                
'ALLOC FI(SYSTSPRT) SYSOUT HOLD REUSE'                                          
                                                                                
 call saym 1, '  This EXEC completed with Return Code' $rc                      
 call saym 1, '                 '                                               
                                                                                
 If flag2 = 'OFF' then                                                          
  Do                                                                            
   call saym 2, '  None. No directory was created.'                             
   call saym 2, '                               '                               
  End                                                                           
 Else                                                                           
   call saym 2, '                               '                               
                                                                                
 If flag3 = 'OFF' then                                                          
  Do                                                                            
   call saym 3, '  None. No existing directory.'                                
   call saym 3, '                  '                                            
  End                                                                           
 Else                                                                           
   call saym 3, '                               '                               
                                                                                
 If flag4 = 'OFF' then                                                          
  Do                                                                            
   call saym 4, '  No problems while creating the directory.'                   
   call saym 4, '                               '                               
  End                                                                           
 Else                                                                           
   call saym 4, '                               '                               
                                                                                
 If flag7 = 'OFF' then                                                          
   call saym 7, '  End of EXEC.'                                                
 Else                                                                           
  Do                                                                            
   call saym 7, '                               '                               
   call saym 7, '  Please refer to the OS/390 UNIX Messages and Codes',         
                '  book to interpret'                                           
   call saym 7, '  the Return and Reason Codes.'                                
   call saym 7, '  Please correct and resubmit.'                                
  End                                                                           
                                                                                
If flag1 = 'OFF' then                                                           
  'EXECIO' SINDEX.1 'DISKW SYSTSPRT (STEM STEM.1. FINIS'                        
Else                                                                            
 Do                                                                             
  'EXECIO' SINDEX.1 'DISKW SYSTSPRT (STEM STEM.1.'                              
  'EXECIO' SINDEX.2 'DISKW SYSTSPRT (STEM STEM.2.'                              
  'EXECIO' SINDEX.3 'DISKW SYSTSPRT (STEM STEM.3.'                              
  'EXECIO' SINDEX.4 'DISKW SYSTSPRT (STEM STEM.4.'                              
  'EXECIO' SINDEX.5 'DISKW SYSTSPRT (STEM STEM.5.'                              
  'EXECIO' SINDEX.6 'DISKW SYSTSPRT (STEM STEM.6.'                              
  'EXECIO' SINDEX.7 'DISKW SYSTSPRT (STEM STEM.7. FINIS'                        
 End                                                                            
'FREE FI(SYSTSPRT)'                                                             
                                                                                
Exit $rc                                                                        
                                                                                
                                                                                
  /********************************/                                            
  /*  Subroutine for mkdir calls  */                                            
  /********************************/                                            
                                                                                
syscall0:                                                                       
 parse arg cmd                                                                  
 address syscall cmd                                                            
 parse arg . path permbits                                                      
                                                                                
 If (rc=0 & retval\=0) | rc\= 0 then                                            
  Do                                                                            
   If errno = 75 then           /* EEXIST */                                    
    Do                                                                          
     flag3 = 'ON'                                                               
     call saym 3, ' ' path                                                      
     address syscall 'chmod' path permbits                                      
    End                                                                         
   Else                                                                         
    If errno \= 75 then                                                         
     Do                                                                         
      flag4 = 'ON'                                                              
      flag7 = 'ON'                                                              
      call saym 4, ' ' path                                                     
      call saym 4,'    Not created. RC='errno '   RSN='errnojr                  
      $rc = '12'                                                                
     End                                                                        
  End                                                                           
 Else                                                                           
  Do                                                                            
   flag2 = 'ON'                                                                 
   call saym 2, ' ' path                                                        
  End                                                                           
Return                                                                          
                                                                                
  /********************************/                                            
  /*  Subroutine to check mkdir   */                                            
  /********************************/                                            
                                                                                
checkit:                                                                        
 If flag2 = 'OFF' then                                                          
  Do                                                                            
   call saym 2, '  None. No directory was created: ' path                       
   call saym 2, '                               '                               
  End                                                                           
 Else                                                                           
   call saym 2, '                               '                               
                                                                                
 If flag3 = 'OFF' then                                                          
  Do                                                                            
   call saym 3, '  None. No existing directory: ' path                          
   call saym 3, '                  '                                            
  End                                                                           
 Else                                                                           
   call saym 3, '                               '                               
                                                                                
 If flag4 = 'OFF' then                                                          
  Do                                                                            
   call saym 4, '  No problems while creating the directory: ' path             
   call saym 4, '                               '                               
  End                                                                           
 Else                                                                           
   call saym 4, '                               '                               
                                                                                
Return                                                                          
                                                                                
  /*****************************************/                                   
  /*  Subroutine to add messages to STEMS  */                                   
  /*****************************************/                                   
                                                                                
saym:                                                                           
 parse arg s,msg                                                                
 SINDEX.s=SINDEX.s+1                                                            
 ix=SINDEX.s                                                                    
 STEM.s.ix=msg                                                                  
Return                                                                          
