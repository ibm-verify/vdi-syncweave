
Password Decryption (pwsync_decryption)  Example:
Note: If viewing this file with Notepad, set Format->Word Wrap from the toolbar.

This example demonstrates the use of the password decryption function provided with the IDIPasswordSynchronizer Plugin provided with SyncWeave.  Specifically, the assemblyline demonstrates how to use the 
cryptography helper class, com.ibm.di.function.UserFunctions.getRsaDecrypted which is packaged in miserver.jar and is accessible by the assemblyline.
The GetNextSuccessful hook of the getPasswords connector uses the following static method call to decrypt the passwords captured by the PasswordSynchronizer: 
 
   system.getRsaDecrypted(encpw,                      // the ecrypted pw from work
   			"c:/sync/idicryptotest.jks",  // the test Keystore file created by idicryptokeys.bat
			"secret",        // the keystore password, maps to -storepass argument in idicryptokeys.bat
			"idicryptotest", // certificate alias
			"secret");       // the keypassword, maps to the -keypass argument in idicryptokeys.bat

Files included in this example:
"pwsync_decryption.xml", "pwsync_decryption_expected_output.txt", "readme.txt".
The pwsync_decryption.xml IDI configuration demonstrates how the IDIPasswordCrypto.decrypt method can be used in a simple scenario.  Passwords are retrieved from the directory server, decrpyted and written to a file along with the userids and encrypted passwords.

Use of this demo, requires that you have already installed and configured the IDIPasswordSynchronizer plugin and populated an LDAP directory (eg. IBM Directory Server, SunOne, Active Directory) with instance of the ibm-diPerson object with password encryption enabled.  Please refer to the readme documentation provided with
the IDIPasswordSynchronizer plugin.  The java.security file which is located in the <idihome>/_jvm/lib/security directory) must be updated to allow for the following security provider:
com.ibm.crypto.provider.IBMJCE.



The output of the example is stored in the "examples\pwsync_decryption\pwsync_decryption_out.txt" text file. Its contents should be similar to the contents of the "examples\pwsync_decryption\pwsync_decryption_expected_output.txt" file provided with this package.



