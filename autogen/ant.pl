#!/usr/bin/perl

#  ===========================================================================
#  Change history:
#
#  Defect   Date     Who  Description
#  ...... .........  ...  ..................................................
#   nnnnn ddMMMyyyy  www
#   00871 20021108   BWR  move example jars
#   f4504 20050616   GSL  Added support for TMS XML files("tmsfiles" dir)
#  f12927 20090717   KGK  Added support for shared TMS XML files ("shared/tmsfiles") not put into jars.
#  ===========================================================================

# print "<project name=\"IBM Directory Integrator\" default=\"all\">\n\n";

$NoPII = 'false';

# Loop through the input parameters.
while ($_ = shift)
{
  # check if '-n' was specfied to not create PII rules.
  if( $_ eq "-n" )
  {
    $NoPII = "true";
    next;
  }
}

open (M, "main.xml");
while ( <M> ) {
	print;
}

%alljars = ();

checkDir ( "components" );

# export shared TMSFILES target
print "<!-- Handle shared components, e.g. transform shared TMS XML files -->\n";
print "<target name=\"shared\">\n\n";
transformTMSXML ("\${project.root.autogen.base}", "shared", "shared" );
print "</target>\n\n";

print "<!-- ALL JARS TARGET -->\n";
print "<target name=\"JARS\" depends=\"";
print join(",", (sort (keys %alljars)));
print ",shared\" />\n\n";

print "</project>\n";


sub	checkDir {
	local ($dir) = @_;
	local (@files);

	# Is this a distribution directory
	if ( -e "$dir/classfiles" || -e "$dir/tdi.xml" ) {
		makeJAR ( $dir );
	}

	# Get subdirectories to check
	opendir (S, "$dir") || die ("Cannot opendir: " . $dir . "\n");
	@files = readdir(S);
	closedir(S);

	foreach $f (@files) {

		next if ($f eq "." || $f eq "..");
		$path = "$dir/$f";

		if ( -d $path ) {
			&checkDir ("$dir/$f");
		}

	}
}

#
# Add ANT build sections for directory
#
sub	makeJAR {
	local ($dir) = @_;
	local $mod = 1;
	local $jar = $dir;

	$jar =~ s/components\///;
	$name = $jar;
	$jar .= ".jar";
	$alljars{$name} = "1";

	# ANT source/build macros
	if ( -e "$dir/ant.txt" ) {
		open ( A, "$dir/ant.txt" );
		$antsrc = <A>; chop($antsrc);
		$antbuild = <A>; chop($antbuild);
		close (A);
	} else {
		$antsrc = "\${source}";
		$antbuild = "\${build}";
	}

	# ANT distribution configurations

        use File::Basename;
        ($jarname,$jarpath,$jarsuffix) = fileparse($name);

        if ( -e "$dir/jardist.txt" ) {
          open (CONFIG, "$dir/jardist.txt" );

          while(<CONFIG>) {         # parse input file
              next if ( /^#/ );     # ignore comment lines
              next if ( /^\s/ );    # ignore blank lines
          #    s/\s//g;	            # remove whitespace
              next if ( length == 0 );
                                    # parse in 'base' parm, if it exists
              if (s/base\W*=\W*//i) {
                $jarbase = $_;
                chomp($jarbase);
                next;
              }
                                    # parse in 'path' parm, if it exists
              if (s/path\W*=//i) {
                $jarpath = $_;
                chomp($jarpath);
                $jarpath .= "/";    # trailing slash not allowed in file
                next;
              }
                                    # parse in 'name' parm, if it exists
              if (s/name\W*=//i) {
                $jarname = $_;
                chomp($jarname);
                next;
              }
          }
          close (CONFIG);
        } # while

        if (length $jarbase != 0) {
            $jardist = $jarbase   . "/";
            $jarbase = "";
        } else {
            $jardist = "\${dist}" . "/"; # default value
        }
        $jarpath =~ s/\.\\//;      # fileparse can return "dot back-slash"
        if (length $jarpath  != 0) {
            $jardist .= $jarpath;
        }
        if (length $jarname != 0) {
            $jardist .= $jarname . ".jar\n";
        }

        chop($jardist);


	# Compose ANT sections

	print "<!-- Targets for $jar -->\n";

	# Create compose-<target> if there are class files
	$hasJava = 0;
	# bring in properties files for tmsxml if they existing during packaging of component.
	$hasProps = 0;
	if ( -e "$dir/classfiles" ) {
		open (F, "$dir/classfiles") || die ("Cannot open $dir/classfiles");
		print "<target depends=\"init\" name=\"compile-" . $name . "\">\n";
#    if ( -e "$dir/14srcrequired" ) {
		  print "  <javac srcdir=\"" . $antsrc . "\" destdir=\"" . $antbuild . "\" debug=\"\${root.javac_dbg}\" includeantruntime=\"false\" source=\"\${root.javac_src.vers}\" target=\"\${root.javac_tgt.vers}\">\n";
#		} else {
#		  print "  <javac debug=\"\${debug}\" srcdir=\"" . $antsrc . "\" destdir=\"" . $antbuild . "\">\n";
#		}
		print "    <classpath refid=\"project.class.path\" />\n";
		while ( <F> ) {
			chop if ( /\n$/ );
			next if ( /\$/ );
			$javasrc = $_;
			$javasrc =~ s/.class/.java/;
			print "    <include name=\"" . $javasrc . "\" />\n";
			$hasJava = 1;
		}

		# Excluded files
		if ( -e "$dir/excludes" ) {
			open (F, "$dir/excludes") || die ("Cannot open $dir/excludes");
			while ( <F> ) {
				chop if ( /\n$/ );
				next if ( /\$/ );
				$javasrc = $_;
				$javasrc =~ s/.class/.java/;
				print "    <exclude name=\"" . $javasrc . "\" />\n";
				$hasJava = 1;
			}
		}

		print "  </javac>\n";
		# If directory "tmsfiles" exist then pick each xml file and create a properties file 
		# (where ini files are present).
		# Any error is logged to project.root.tmsxml.log file during build
		
		if( -d "$dir/tmsfiles/") {
			transformTMSXML ("\${project.root.autogen.comp}", $dir, $name );
		}

		# END OF TMS XML related changes

		if( -d "$dir/NLS") {
			copyEnglish ("\${project.root.autogen.comp}/$name", "$dir/NLS");
		}

		print "</target>\n";
		print "\n";
	}

	if ( $hasJava == 1 ) {
		print "<target name=\"" . $name . "\" depends=\"compile-" . $name . "\">\n";
	} else {
		print "<target name=\"" . $name . "\">\n";
	}

	print "  <jar jarfile=\"" . $jardist . "\">\n";
	if ( -e "$dir/classfiles" ) {
		print "    <fileset dir=\"" . $antbuild . "\">\n";
		open (F, "$dir/classfiles") || die ("Cannot open $dir/classfiles");
		while ( <F> ) {
			chop if ( /\n$/ );
			print "      <include name=\"" . $_ . "\" />\n";
		}
		# Excluded files
		if ( -e "$dir/excludes" ) {
			open (F, "$dir/excludes") || die ("Cannot open $dir/excludes");
			while ( <F> ) {
				chop if ( /\n$/ );
				next if ( /\$/ );
				$javasrc = $_;
				print "      <exclude name=\"" . $javasrc . "\" />\n";
				$hasJava = 1;
			}
		}
		print "    </fileset>\n";
	}
	print "    <fileset dir=\"\${project.root.autogen.comp}/$name\">\n";
	print "      <include name=\"tdi.xml\" />\n";
	print "      <include name=\"NLS/**.properties\" />\n";
	print "    </fileset>\n";
	if ( $hasProps == 1 ) {
	print "    <fileset dir=\"\${project.root.tmsxml_props.export}/$name\">\n";
	print "      <include name=\"**.properties\" />\n";
	print "    </fileset>\n";
	}
	print "  </jar>\n";
	print "</target>\n";
	print "\n";
}

sub copyEnglish {
	local ($name, $dir) = @_;
	opendir(NLS, $dir) || return;
	foreach $file (readdir(NLS)) {
		next unless $file =~ /_fr\.properties/;
		local ($from, $to) = ( $` . ".properties", $` . "_en.properties"); 
		if (-f "$dir/$from" && ! -f "$dir/$to") {
			print "  <copy file=\"$name/NLS/$from\" tofile=\"$name/NLS/$to\"/>\n";
		}
		last;
	}
	closedir NLS;
}

sub transformTMSXML {

	local ($base, $dir, $name) = @_;

	if( -d "$dir/tmsfiles/") {
		  opendir(TMSDIR, "$dir/tmsfiles/" ) || die "Can't open dir $dir/tmsfiles/: $!";
		  @dirs_tms = readdir(TMSDIR);
		  print "  <mkdir dir=\"\${project.root.tmsxml_props.export}/$name/\"/>\n";
		  $hasProps = 1;
		  foreach $dirname (@dirs_tms){
			next if($dirname =~ /^\.+$/);
			if(($dirname ne 'en') and ($NoPII eq 'true')) {
			  next;
			}
			opendir(TMSFILES, "$dir/tmsfiles/$dirname/" ) || die "Can't open dir $dir/tmsfiles/$dirname: $!";
			@files_tms = readdir(TMSFILES);
			foreach $filename (@files_tms){
			  next if($filename =~ /^\.+$/);
			  $xmlFile = $filename;
			  $propFile = $filename;
			  $propFile2 = $filename;
			  $propFile3 = $filename;
			  $propFile =~ s/\.xml/\_$dirname.properties/;
			  $propFile2 =~ s/\.xml/\.properties/;
			  $propFile3 =~ s/\.xml/\_$dirname.in/;
			  
		  print "  <convertTMSXMLToProperties tmsxmlfile.loc=\"$base/$name/tmsfiles/$dirname/$xmlFile\" propsfile.loc=\"\${project.root.tmsxml_props.export}/$name/$propFile\" logfile.log=\"\${project.root.tmsxml.log}\"/>\n";
		  if($dirname eq "en") {
			print "  <copy file=\"\${project.root.tmsxml_props.export}/$name/$propFile\" tofile=\"\${project.root.tmsxml_props.export}/$name/$propFile2\"/>\n";
		  }

			 }
		  }
		closedir TMSDIR; 
	}
}
