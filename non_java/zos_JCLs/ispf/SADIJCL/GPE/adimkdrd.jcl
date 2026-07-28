/* REXX */                                                                      
/********************************************************************/          
/* Licensed Materials - Property of IBM                             */          
/* 5698-B33 (C) . 2007, 2011                      */          
/* All rights reserved.                                             */          
/*                                                                  */          
/* /* disclosure restricted by /********************************************************************/          
/*                                                                  */          
/* THIS REXX EXEC WILL CREATE THE NECESSARY DIRECTORIES AND OTHER   */          
/* FILES FOR PRODUCT IBM TIVOLI DIRECTORY INTEGRATOR GENERAL        */          
/* PURPOSE EDITION.                                                 */          
/*                                                                  */          
/********************************************************************/          
                                                                                
idir='usr/lpp/itdi/'         /* <== This is your install directory  */          
                                                                                
dirs=,
   'IBM 755',                                                                   
   'bin' 755,                                                                   
   'bin/IBM 755',                                                               
   'bin/amc' 755,                                                               
   'bin/amc/IBM 755',                                                           
   'bin/amc/ActionManager' 755,                                                 
   'bin/amc/ActionManager/IBM 755',                                             
   'bin/amc/ActionManager/jars' 755,                                            
   'bin/amc/ActionManager/jars/IBM 755',                                        
   'etc' 755,                                                                   
   'etc/IBM 755',                                                               
   'jars/ 755',                                                                 
   'jars/IBM 755',                                                              
   'jars/3rdparty/ 755',                                                        
   'jars/3rdparty/IBM 755',                                                     
   'jars/3rdparty/IBM/IBM 755',                                                 
   'jars/3rdparty/IBM/axis2 755',                                               
   'jars/3rdparty/IBM/axis2/IBM 755',                                           
   'jars/3rdparty/IBM/cdm 755',                                                 
   'jars/3rdparty/IBM/cdm/IBM 755',                                             
   'jars/3rdparty/IBM/gla 755',                                                 
   'jars/3rdparty/IBM/gla/IBM 755',                                             
   'jars/3rdparty/IBM/IT_registry 755',                                         
   'jars/3rdparty/IBM/IT_registry/IBM 755',                                     
   'jars/3rdparty/others 755',                                                  
   'jars/3rdparty/others/IBM 755',                                              
   'jars/3rdparty/others/ActiveMQ 755',                                         
   'jars/3rdparty/others/ActiveMQ/IBM 755',                                     
   'jars/3rdparty/others/emf' 755,                                              
   'jars/3rdparty/others/emf/IBM 755',                                          
   'jars/common 755',                                                           
   'jars/common/IBM 755',                                                       
   'jars/connectors 755',                                                       
   'jars/connectors/IBM 755',                                                   
   'jars/functions 755',                                                        
   'jars/functions/IBM 755',                                                    
   'jars/parsers 755',                                                          
   'jars/parsers/IBM 755',                                                      
   'jars/plugins' 755,                                                          
   'jars/plugins/IBM 755',                                                      
   'license' 755,                                                               
   'license/IBM 755',                                                           
   'osgi' 755,                                                                  
   'osgi/IBM 755',                                                              
   'osgi/plugins' 755,                                                          
   'osgi/plugins/IBM 755',                                                      
   'osgi/configuration' 755,                                                    
   'osgi/configuration/IBM 755',                                                
   'serverapi' 755,                                                             
   'serverapi/IBM 755',                                                         
   'tools' 755,                                                                 
   'tools/IBM 755',                                                             
   'tools/CSMigration' 755,                                                     
   'tools/CSMigration/IBM 755',                                                 
   'libs' 755,                                                                  
   'libs/IBM 755',                                                              
   'tso_fc' 755,                                                                
   'tso_fc/IBM 755',                                                            
   'xsd' 755,                                                                   
   'xsd/gla' 755,                                                               
   'xsd/gla/schema' 755,                                                        
   'xsd/gla/schema/IBM 755',                                                    

parse arg $root .
If $root='' then
   $root = '/'                         /* Add a trailing slash    */
 Else                                  /* if none exists on input */
   $root=strip($root,'T','/')'/'

/****************************************************************/
/*     Beginning of main procedure:                             */
/****************************************************************/

firstchar = Substr($root,1,1)
If firstchar <> '/' then
 Do
  say 'Directory name does NOT begin with a "/".'
  say 'Please correct and resubmit.'
  Exit 12
 End

if syscalls(on)>4 then
 Do
  say 'Initialization failure. Please correct and resubmit.'
  Exit 12
 End

verticalbar='|'                /* check integrity of this exec */
if C2X(verticalbar) <> 4F then
 Do
  say 'Vertical bar characters in this file are corrupted,'
  say 'probably caused by translation during upload or download.'
  say 'Dollar sign characters may also be corrupted.'
  Exit 12
 End

$rc= '0'

msgs. = ''

 x=outtrap(mm.,,'NOCONCAT')
 'PROFILE'
 If Pos(NOMSGID,mm.1) > 0 then num = '3'
 Else num = '4'
 PARSE SOURCE . . EXECNAME .
 x=outtrap(OFF)
  say 'The EXEC to create the directories has begun.'
  say 'It will run for a couple of minutes.'
 x=outtrap(mm.,,'NOCONCAT')
 say 'The' EXECNAME 'EXEC ran at ' TIME() ' on ' DATE()
 say
 $rc= '0'

address syscall 'getuid'
myuid=retval
address syscall 'geteuid'
myeuid=retval
privflag=0                         /* Change the Effective UID to  */
If myeuid <> 0 then                /* be 0 since invoker should    */
  Do                               /* be part of the BPX.SUPERUSER */
   address syscall 'seteuid 0'     /* facility class               */
   privflag=1
  End

address syscall 'access' $root F_OK

If (rc=0 & retval<>0) | rc<> 0 then
 Do
  If errno = 6F then           /* EACCESS */
   Do
    say 'Do not have appropriate permission to' $root
    say '                                 '
    say 'Please get appropriate permission to this directory',
               'and resubmit.'
    say
    Exit 12
   End
  Else
   Do
    say 'Directory' $root 'does not exist.'
    say
    say 'Please create this directory and resubmit.'
    say
    Exit 12
   End
  $rc= '12'
 End
Else
 Do                               /* OK to continue this exec */

  call msg errors,  ' '           /* Setup all headings */

  call msg dcreated, ' Created the following directories:'
  call msg dcreated, ' =================================='
 call msg dchmods, ' Changed permission bits of existing directories:'
 call msg dchmods, ' ================================================'
  call msg dexists,  ' Following directories already exist',
                     'with proper permissions:'
  call msg dexists,  ' ====================================',
                  ||'========================='
  call msg dproblems, ' Problems creating following directories:'
  call msg dproblems, ' ========================================'
  call msg lcreated, ' Created the following symlinks:'
  call msg lcreated, ' ==============================='
  call msg lexists,  ' Following symlinks already exist with correct',
                     'target:'
  call msg lexists,  ' =============================================',
                   ||'========'
  call msg lproblems, ' Problems creating following symlinks:'
  call msg lproblems, ' ====================================='
  call msg uMSGS, ' Verified that the following symlinks'
  call msg uMSGS, '  do not point to paths known to be obsolete.',
                  'No action taken.'
  call msg uMSGS, ' ============================================',
                ||'================='
  uMSGS_flag = 0
  call msg udeleted,  ' Deleted the following symlinks:'
  call msg udeleted,  ' ==============================='
  call msg uproblems,  ' Problems deleting the following symlinks:'
  call msg uproblems,  ' ========================================='
  call msg ADDMSG, ' Additional messages:'
  call msg ADDMSG, ' ===================='


  if idir <> '' then  /* this check prevents chmods to $root */
  call syscall0 mkdir $root || idir 755

  do while dirs<>''
     parse var dirs dir perm dirs
     call syscall0 mkdir $root || idir || strip(dir) strip(perm)
  end


  /************************************/
  /*                                  */
  /************************************/

End

If privflag=1 then
  address syscall 'seteuid' myuid

 if,
(msgs.dcreated.0 + msgs.dexists.0 + msgs.dproblems.0 + msgs.dchmods.0),
= 8 then
  Do
  call msg dcreated,  'No mkdir commands attempted'
  msgs.dexists.0 = 0       /* eliminate this message heading */
  msgs.dchmods.0 = 0       /* eliminate this message heading */
  msgs.dproblems.0 = 0     /* eliminate this message heading */
  End

if msgs.dcreated.0 = 2
  then call msg dcreated, 'No directories were created'
call msg dcreated, ' '

if msgs.dchmods.0 = 2
  then call msg dchmods, 'No permission bits were changed'
call msg dchmods, ' '

if msgs.dexists.0  = 2 then
  call msg dexists, 'No directories already existed with proper',
  'permission bits'
call msg dexists, ' '

if msgs.dproblems.0 = 2
  then call msg dproblems, 'No problems while creating directories'
call msg dproblems, ' '


if msgs.uMSGS.0 = 3
  then msgs.uMSGS.0 = 0    /* eliminate this message heading */
  else
  Do
   call msg uMSGS, 'If you recognize an existing path above',
                       'as one that was modified manually,'
   call msg uMSGS, 'then it may cause a problem during the apply.'
   call msg uMSGS, 'Otherwise it is acceptable.'
   call msg uMSGS, 'If this EXEC is being rerun after apply,',
                       'then this message is acceptable.'
   call msg uMSGS, ' '
  End

if msgs.ADDMSG.0 = 2
  then call msg ADDMSG, 'No additional messages'
call msg ADDMSG, ' '

 If $rc < 8  then
  call msg saylast, 'End of EXEC.'
 Else
  Do
   call msg saylast, '                                  '
   call msg saylast, 'Please refer to the UNIX System',
      'Services Messages and Codes book'
   call msg saylast, 'to interpret the Return and Reason Codes.'
   call msg saylast, 'Please correct and resubmit.'
  End


 x=outtrap(off)
 do i=1 to words(msgs.indices)             /* write out all msgs */
   index = word(msgs.indices,i)
 /*say 'index = ' index         */
   do j=1 to msgs.index.0
      say msgs.index.j
      end
   end

 say 'The' EXECNAME 'EXEC has completed with Return Code' $rc
Exit $rc

  /********************************/
  /*  Subroutine for mkdir calls  */
  /********************************/

syscall0:
 parse arg cmd
 address syscall cmd                        /* mkdir */
 parse arg . path permbits

 If (rc=0 & retval<>0) | rc<> 0 then
  Do
   If errno = 75 then           /* EEXIST */
    Do
     address syscall 'stat' path 'dirinf.'
     if dirinf.ST_MODE = permbits then call msg dEXISTS, path permbits
     else
       do
        address syscall 'chmod' path permbits
        call msg dCHMODS, path permbits
       end
    End
   Else
    If errno <> 75 then
     Do
      call msg dPROBLEMS, path permbits
      call msg dPROBLEMS,'  Not created. RC='errno '   RSN='errnojr
      call lookup(dPROBLEMS errno errnojr)
      $rc = '12'
     End
  End
 Else
  Do
   CALL MSG DCREATED, path permbits
  End

 If verify(permbits,'01234567') > 0 then
  Do
   call msg dPROBLEMS, path
   call msg dPROBLEMS,'  Not created. Invalid permission bits' permbits
   $rc = '12'
  End
Return


  /***************************************************/
  /*  Subroutine to lookup text for error numbers    */
  /*    note: does nothing on pre-os390 v2r7 systems */
  /***************************************************/

lookup: procedure expose reasons msgs.
 parse upper arg section msgno msgnojr  /* really errno and errnojr*/
  err.=''
  trace off                             /* supress clutter msgs     */
  address syscall 'strerror' msgno msgnojr 'err.'
  trace normal                          /* normal again             */
  if rc<> 0 then return                 /* quietly if syscall fails */

  call msg section, ' ' err.1           /* always list short text   */
  if wordpos(msgnojr,reasons) = 0       /* but only list long text  */
  then do                               /* if not shown previously  */
    reasons = reasons msgnojr
    if err.3<>'' then call msg section, '  'err.3
    if err.4<>'' then call msg section, '  'err.4
   end
  call msg section, ''
 return

  /*****************************************/
  /*  Subroutine to add messages to stems  */
  /*****************************************/

msg: procedure expose msgs.
 parse arg index, text
 if wordpos(index, msgs.indices) = 0
 then do
    msgs.indices = msgs.indices index
    msgs.index.0 = 0
    end
 i = msgs.index.0 + 1
 msgs.index.i = text
 msgs.index.0 = i
Return
