/* --------------------------------------------------------------------- */
/*  This program begins the build process for the IBM Directory          */
/*  Integration (IBMDI) product.                                         */
/* --------------------------------------------------------------------- */

/* --------------------------------------------------------------------- */
/* Parse and save the input information.                                 */
/* --------------------------------------------------------------------- */
Parse Arg inputOptions

/* --------------------------------------------------------------------- */
/* Create and initialize the build script instance.                      */
/* --------------------------------------------------------------------- */
build = .IbmDiBuild~new()

rc = build~execute(inputOptions)

exit rc

/* ===================================================================== */
/* Program imbeds.                                                       */
/* ===================================================================== */
::REQUIRES Command
::REQUIRES GenericBuild

/* ===================================================================== */
/*   Class:  IbmDiBuild                                                  */
/*                                                                       */
/* Purpose:  Local subclass of GenericBuild used to build the IbmDi      */
/*           product.                                                    */
/* ===================================================================== */
::class IbmDiBuild public subclass GenericBuild

::method init                      /* Initialize the class instance.     */
  self~init:super('IbmDi', .Nil)

  self~release        = 'ibmdi_510'
  self~family         = 'integrat'

  self~dfsProjectDir  = 'ibmdi'
  self~dfsReleaseDir  = 'ibmdi5.1'

  self~product        = 'Ibmdi'
  self~version        = '5.1'

/*
  mutDrive = 'M:'
  self~networkConnections[self~networkConnections~items + 1] =,
       mutDrive '\\csbtool1\IbmDi1mut'

  .EnvVar['IbmDi1MUT']~value = mutDrive
*/

  return


::method performLocalSetup       /* Perform IbmDi-specific setup.   */

Parse Source . . callName .
parse value reverse(callName) with . '.' callName '\' callPath
callName = reverse(callName)

parse var callPath callRootDir '\' .
callPath    = reverse(callPath)
callRootDir = reverse(callRootDir)

  .Command~defaultLog = self~buildLog
  self~buildLog~write(''),
         ~writeWithBorder('method performLocalSetup ...')

  response = 'IbmDi-specific setup complete.'
  /* ----------------------------------------------------------------------- */
  /* Generate the alias.properties file as required by ISMP.  This must      */
  /* exist in the root of the ISMP home directory for the current version of */
  /* ISMP.  A future option is due in the 6/02 timeframe which allows the    */
  /* location of the file to be specified via the command line.              */
  /* ----------------------------------------------------------------------- */
  ismp = .EnvVar['ISMP45']~value

  lines = .Array~new()
  lines[lines~size + 1] = changeStr('\', '#ISMP Alias', '\\')
  lines[lines~size + 1] = changeStr('\', 'IS_HOME='ismp, '\\')
  lines[lines~size + 1] = changeStr('\', 'DI_FILES='self~extract, '\\')

  rc = 0
  if self~testMode = .false then do
    aliasFile = .Stream~new(ismp'\alias.properties')
    aliasFile~open('WRITE REPLACE')
    aliasFile~arrayOut(lines)
    aliasFile~close

    if aliasFile~state~translate = 'ERROR' then
      rc = 1
  end

  self~setupLog~writeWithRc('Writing' ismp'\alias.properties file.', rc)

  x = directory(callRootDir)
/*  Using the .Command object fails to cause the xml file to be derived
  'cd autogen'
    cmd = .Command['perl %s']
    rc = cmd~execute('ant.pl > ../build.xml')
  'cd ..'
*/
  x = directory("autogen")
  'perl ant.pl > ../build.xml' /* the return code is placed in RC */
  if rc <> 0 then
     response = response' but autogen had a non-zero return code!'
  x = directory("..")

  self~buildLog~write(''),
      ~writeWithBorder(response)
  return rc


/****************************************************************/
/* This method override was added to allow passing the level    */
/* name to the ziptime ant property                             */
/****************************************************************/

::method runBuild                       /* Run build step(s).                */
  .Command~defaultLog = self~buildLog
  self~buildLog~write(''),
         ~writeWithBorder('Begin basic build steps ...')

  rc = 0
  response = 'Basic build steps complete.'

  /* ----------------------------------------------------------------------- */
  /* Determine the CMVC level name.                                          */
  /* ----------------------------------------------------------------------- */
  if exists(self~extract'\build.properties') then do
    lines = queued()
    '@TYPE' self~extract'\build.properties | RXQUEUE'
    do while queued() > lines
      parse pull key '=' value
      if key~translate = 'LEVEL' then
        levelName = value
    end
    end
  else
    levelName = 'Unknown'

  target = self~options['b']~mod~value
/*  if target = '' then target = "release javadoc"   */

  if exists(self~extract'\build.xml') then do
    rc = .Ant[self~buildLog]~execute('-l build.log -Dziptime='levelName target)
    if rc = 0 then do
      cmd = .Command['type %s']
      cmd~redirection = 'RXQUEUE'
      cmd~scanForErrorText('BUILD SUCCESSFUL')

      rc = cmd~executeSilent('build.log')
      if rc = 0 then
        rc = 1
      else
        rc = 0
    end
  end

  if rc <> 0 then
    response = response'  There were errors!'

  self~buildLog~write(''),
         ~writeWithBorder(response)

  return rc

/*
::method performFTP                    /* Perform IbmDi FTP steps.            */
  start = .Command['start "FTP to %s" cmd /c "ant -l ftp%s.log dist_%s"']

  rc = start~execute('Israel', 'Israel', 'israel') +,
       start~execute('Egypt' , 'Egypt' , 'egypt') +,
       start~execute('GCG' , 'GCG' , 'gcg')

  return rc
*/
