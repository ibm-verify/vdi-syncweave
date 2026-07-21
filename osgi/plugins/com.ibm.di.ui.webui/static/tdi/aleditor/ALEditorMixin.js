define("tdi/aleditor/ALEditorMixin", [
    	"dojo/_base/declare"
    ], function(declare) {

	return declare([], {
		
		onResize: function(box) {
		},
		
		getBoundingBox: function() {
			// summary:
			//		Returns the x,y,width,height for the current domNode
			var box = {
				y: parseFloat(this.domNode.style.top.replace("px", "")),
				x: parseFloat(this.domNode.style.left.replace("px", "")),
				width: parseFloat(this.domNode.style.width.replace("px", "")),
				height: parseFloat(this.domNode.style.height.replace("px", ""))
			};
			return box;
		},
		
		setBoundingBox: function(box) {
			// summary:
			//		Sets the bounding for the UI component.
			//		Box must contains numbers only (e.g. no 10px etc)
			var style = {
					position:"absolute",
					top:box.y + "px",
					left:box.x + "px",	
					width:box.width + "px",
					height:box.height + "px"
			};
			for(var f in style) {
				this.domNode.style[f] = style[f];
			}
			this.onResize(box);
		},

	});
});

