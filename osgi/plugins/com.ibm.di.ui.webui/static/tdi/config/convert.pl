

$isopen = false;

while(<>) {
	next if (/dojo\.provide/);
	if(/declare\("tdi\.(.*)"/) {
		if($isopen) {
			print G "});\n";
			close G;
		}
		open G, ">" . $1 . ".js" || die $!;
		print G "define(\[\n\t\"dojo/_base/declare\",\n\t\"tdi/config/basecfg\"\n\], function(declare, basecfg) {\nreturn declare(\n\t\[basecfg\],\n\t{\n";
		print G "// " . $_;
		$isopen = true;
	} else {
		if($isopen) {
			print G;
		}
	}
}
