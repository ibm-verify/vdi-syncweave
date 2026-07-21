var profile = (function() {
	return {
		// source files for the build
		basePath : "./static.build",
		
		// releaseDir
		releaseDir : "../",

		// compressed and layered files are written to static
		releaseName : "static",

		action : "release",
		mini : true,
		selectorEngine : "lite",
		cssOptimize : "comments",

		htmlFiles : "index.html",

		defaultConfig : {
			hasCache : {
				"dojo-built" : 1,
				"dojo-loader" : 1,
				"dom" : 1,
				"host-browser" : 1,
				"config-selectorEngine" : "lite"
			},
			async : 1,
			packages : [ {
				name : "dijit",
				location : "../dijit"
			}, {
				name : "dojox",
				location : "../dojox"
			}, {
				name : "gridx",
				location : "../gridx"
			}, {
				name : "idx",
				location : "../idx"
			}, {
				name : "tdi",
				location : "../tdi"
			}, {
				name : "tdinls",
				location : "/webui/tdinls"
			} ]
		},

		packages : [ {
			name : "dojo",
			location : "dojo_1.8.1/dojo"
		}, {
			name : "dijit",
			location : "dojo_1.8.1/dijit"
		}, {
			name : "dojox",
			location : "dojo_1.8.1/dojox"
		}, {
			name : "idx",
			location : "ibmjs/idx"
		}, {
			name : "gridx",
			location : "dojo_1.8.1/gridx"
		}, {
			name : "codemirror",
			location : "codemirror"
		}, {
			name : "tdi",
			location : "tdi"
		} ],

		layers : {
			"tdi" : {
				include : [ "tdi/ldapsync/LDSMain" ]
			}
		}
	};
})();
