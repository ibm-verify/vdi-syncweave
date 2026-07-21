
@dojoreq = ();
$name = "";

while(<>) {

	if(/dojo.provide\("(.*)"\)/) {
		print "Converting $1\n";
		$name = $1;
	}
	if(/dojo.require\("(.*)"\)/ ) {
		push(@dojoreq, $1);
	}
	
}

print "/*\n";
print " * $name\n";
print " */\n";

print "define([\n";
print "\t\"dojo/_base/declare\",\n";
for ($i = 0; $i < $#dojoreq; $i++) {
	@dojoreq[$i] =~ s/\./\//g;
	print "\t\"" . @dojoreq[$i] . "\"";
	print "," if(($i+1) < $#dojoreq);
	print "\n";
}

# ], function(declare, _Widget, _TemplatedMixin, _WidgetsInTemplate, _Tree, ServerProjectsModel, tdiapi, template) {

print "], function(declare, ";
for ($i = 0; $i < $#dojoreq; $i++) {
	@arr = split("/", @dojoreq[$i]);
	print @arr[$#arr];
	print ", " if(($i+1) < $#dojoreq);
}
print ") {\n\n";
print "return declare(\n";
print "\t[";
print "],\n";
