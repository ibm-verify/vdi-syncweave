var dojoConfig = {
      parseOnLoad: true,
      async: false,
      has: {
      	"dojo-firebug": true,
      	"dojo-debug-messages": true
      },
	  packages: [
       	{name: "dijit", location: "../dijit" },
      	{name: "dojox", location: "../dojox" },
      	{name: "gridx", location: "../gridx" },
      	{name: "idx", location: "../idx" },
      	{name: "tdi", location: "../tdi" },
      	{name: "tdinls", location: "/fds/tdinls" },
      	{name: "orion", location: "../orion" },
      	{name: "examples", location: "../examples" }
      ],
      aliases: [
		["i18n", "dojo/i18n"],
		["text", "dojo/text"],
		["domReady", "dojo/domReady"]
      ],
      gfxRenderer: "svg,canvas,silverlight,vml"
};