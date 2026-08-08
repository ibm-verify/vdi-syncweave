# Downloads

The IBM Eclise SDK (IES) is avilable from [http://ies1.fyre.ibm.com/iss/ies/index.php](http://ies1.fyre.ibm.com/iss/ies/index.php).

You don't need to download any files directly, but instead need to install the SDK and then use the IDE to install the required software.

# Encapsulation

Instructions for how to install the IBM Eclipse SDK are available at: [http://ies1.fyre.ibm.com/iss/ies/downloads/packager/index.php](http://ies1.fyre.ibm.com/iss/ies/downloads/packager/index.php).

The commands which were used to install the v4.27 SDK IDE for the various supported platforms were:

```
./eclipse -noSplash -application org.eclipse.equinox.p2.director \
   -repository http://ies-build1.fyre.ibm.com/IESRepos/4.27.0/I20230503-2000/all \
   -installIU oorg.eclipse.sdk.ide,org.eclipse.sdk,org.eclipse.e4.core.tools.feature.feature.group \
   -tag InitialState \
   -destination /home/exton/ies \
   -profile SDKProfile \
   -profileProperties org.eclipse.update.install.features=true \
   -bundlepool /home/exton/ies \
   -p2.os linux \
   -p2.ws gtk \
   -p2.arch x86_64 \
   -roaming
   
./eclipse -noSplash -application org.eclipse.equinox.p2.director \
        -repository http://ies-build1.fyre.ibm.com/IESRepos/4.27.0/I20230503-2000/all \
        -installIU org.eclipse.sdk.ide \
        -tag InitialState \
        -destination $install_path \
        -profile SDKProfile \
        -profileProperties org.eclipse.update.install.features=true \
        -bundlepool /home/exton/ies.macosx \
        -p2.os macosx \
        -p2.ws cocoa \
        -p2.arch x86_64 \
        -roaming
        
./eclipse -noSplash -application org.eclipse.equinox.p2.director \
        -repository http://ies-build1.fyre.ibm.com/IESRepos/4.27.0/I20230503-2000/all \
        -installIU org.eclipse.sdk.ide \
        -tag InitialState \
        -destination $install_path \
        -profile SDKProfile \
        -profileProperties org.eclipse.update.install.features=true \
        -bundlepool /home/exton/ies.win32 \
        -p2.os win32 \
        -p2.ws win32 \
        -p2.arch x86_64 \
        -roaming
```

The following downloads are required for v4.27:

1. Equinox
2. EMF
3. NL


In order to encapsulate the Eclipse build files you need to:

1. Download and install the 'public' Eclipse SDK;
2. Use the P2 installer to install the IES for each of the supported platforms;
3. Copy any files which are present in the Windows or MacOSX Eclipse SDK back to the Linux Eclipse SDK;
3. Start the IES eclipse binary;
4. Add the update sites for Platform, Equinox, EMF and NL.  This can be achieved in the Eclipse UI by accessing: `Help -> Install New Software -> Manage`.  The repository URLs are avilable from: [http://ies1.fyre.ibm.com/iss/ies/downloads/buildpage.php?build=R-4.27.0-202305032000&os=any](http://ies1.fyre.ibm.com/iss/ies/downloads/buildpage.php?build=R-4.27.0-202305032000&os=any).
3. Install the software from the additional sites (`Help -> Install New Software`);
4. Copy the full eclipse directory;

**NB:** A temporary hack is to also grab the javax.servlet plugin and add this to the plugins directory.

**NB**: Some of the platform specific translated files will be missing from the eclipse area.  These plugin files will need to be source directly from the NL zip file, available from: [http://ies-build1.fyre.ibm.com/IESRepos/4.27.0/I20230503-2000/zips/nl_IES4.27.0.zip](http://ies-build1.fyre.ibm.com/IESRepos/4.27.0/I20230503-2000/zips/nl_IES4.27.0.zip).

# Source Code Changes

The following changes are also required to SDI source code:

1. The `root.eclipse_runtime_vers`, `root.eclipse_rt_scripts` and `root.eclipse_rt_launcher` properties within the `rules_mk/tools_setup.xml` file needs to be updated;
2. The list of required features and plugins need to be updated.  The 'easiest' way to do this is to copy the `export/ce_rcp` directory from a build machine to a Linux box which has the Eclipse IDE installed.  You can then open the `ce_rcp` directory as a project, build the project and then export the project (using the `ce_rcp/plugins/com.ibm.tdi.rcp/tdieclipse.product` file in the IDE).  There are a couple of things which then need to be completed:
	1. The list of features needs to be updated in the product file;
	2. Ensure that any plugins which are missing are either added via a new feature, or specified directly within the `ce_rcp/features/com.ibm.tdi.feature/feature.xml` file.
	3. Copy the bundles.info file from the exported directory to `ce_rcp/plugins/com.ibm.tdi.rcp/bundles.info`. 