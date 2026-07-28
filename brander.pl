#! /usr/bin/perl

$extension = ".orig";
$copyright = '/*
 * IBM Confidential
 *
 *  OCO Source Materials
 *
 * 5724-D49
 *
 * (C) oration. 2009, 2010
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     %I%, %G%
 * @owner       
 * @history
 */

';

while (<>) {
	changefile() if $ARGV ne $oldargv;
	alreadybranded() if (/ \* \(C\) oration\./);
	next if $skip;
	print;
	$writecopyright = 1 if /^\s*public\s+(abstract\s+)?class\b/;
	if ($writecopyright && /\{/) {
		print "\t@SuppressWarnings("unused")\n";
		print "\tprivate static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;\n";
		$writecopyright = 0;
		$haswritten = 1;
	}
}
		
sub changefile {
	if ($oldargv && ! $haswritten && ! $skip ) {
		select(STDOUT);
		print "No copyright: $oldargv\n";
	}
	$backup = $ARGV . $extension;
	rename($ARGV, $backup);
	open(ARGVOUT, ">$ARGV");
	select(ARGVOUT);
	$oldargv = $ARGV;

	print $copyright;
	$writecopyright = 0;
	$haswritten = 0;
	$skip = 0;
}

sub alreadybranded {
	return if $skip;
	close ARGVOUT;
	select (STDOUT);
	print "Already branded: $ARGV\n";
	rename($backup, $ARGV);
	$skip = 1;
}
