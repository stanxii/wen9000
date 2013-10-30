(function() {
	var Cbat = function(label,mac, color) {
		this.initialize(label,mac, color);
	}
	var p = Cbat.prototype = new createjs.Container(); // inherit from Container
	p.label;
	p.mac;
	p.background;
	p.count = 0;
	p.Container_initialize = p.initialize;
	p.initialize = function(label,mac, color) {
		this.Container_initialize();
		this.label = label;
		this.mac = mac;
		if (!color) { color = "#CCC"; }
		var text = new createjs.Text(label, "10px Arial", "#000");
		text.textBaseline = "top";
		text.textAlign = "center";
		var width = 100;
		var height = 100;
		this.background = new createjs.Shape();
		this.background.graphics.beginFill(color).drawRoundRect(0,0,width,height,10);
		text.x = width/2;
		text.y = 40;
		this.addChild(this.background,text);
		this.addEventListener("click", this.handleClick);
	}
	p.handleClick = function (event) {
		var target = event.target;
		alert("You clicked on a button: "+target.label);
	}

	window.Cbat = Cbat;
}());