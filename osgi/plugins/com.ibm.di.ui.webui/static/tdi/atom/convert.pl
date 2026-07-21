

$isopen = false;

while(<>) {
	next if (/dojo\.provide/);
	if(/declare\("tdi\.(.*)"/) {
		if($isopen) {
			print G "});\n";
			close G;
		}
		open G, ">" . $1 . ".js" || die $!;
		print G "define(\[\n\t\"dojo/_base/declare\",\n\t\"tdi/atom/tdiatom\"\n\], function(declare, tdiatom) {\nreturn declare(\n\t\[tdiatom\],\n\t{\n";
		print G "// " . $_;
		$isopen = true;
	} else {
		if($isopen) {
			print G;
		}
	}
}
