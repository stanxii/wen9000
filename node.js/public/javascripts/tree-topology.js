(function($){		
	var svg;
	var diagonal;
	var tree;
	var socket;
	var margin = {top: 20, right: 120, bottom: 20, left: 120},
		width = 960 - margin.right - margin.left,
		height = 800 - margin.top - margin.bottom;
	
	var i = 0,
		duration = 750,
		root = [];
	var nodes=[];			
	$(function(){
		
		var key = GetRequest()['key'];
		var type = GetRequest()['type'];
		socket = io.connect('http://localhost:3000');
		var datastring = '{"key":"'+key+'","type":"'+type+'"}';
		socket.emit('topdevices',datastring);
		socket.on('opt.distopology',fun_Displaytopology);
		socket.on('statuschange', fun_Statuschange );	

	});
			
	function GetRequest(){
		var url = location.search;
		var theRequest = new Object();
		if(url.indexOf("?") != -1){
			var str = url.substr(1);
			strs = str.split("&");
			for(var i=0;i<strs.length;i++){
				theRequest[strs[i].split("=")[0]] = unescape(strs[i].split("=")[1]);
			}
		}
		return theRequest;
	}
				
	function fun_Displaytopology(data){
		root = data;
		root.x0 = height / 2;
		root.y0 = 0;
	
		function collapse(d) {
			if (d.children) {
				d._children = d.children;
				d._children.forEach(collapse);
				d.children = null;
			}
		}
		
		tree = d3.layout.tree()
		.size([height, width]);
		
		var nodes = tree.nodes(root).reverse();
		var nodecounts = nodes.length - 1;
		
		

		diagonal = d3.svg.diagonal()
			.projection(function(d) { return [d.y, d.x]; });
		
		if(nodecounts * 48 > 800){
			tree = d3.layout.tree()
			.size([nodecounts * 48, width]);
			
			svg = d3.select(".topdiv").append("svg")
			.attr("width", width + margin.right + margin.left)
			.attr("height", nodecounts * 48 + margin.top + margin.bottom)
		  .append("g")
			.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
		}else{
			svg = d3.select(".topdiv").append("svg")
			.attr("width", width + margin.right + margin.left)
			.attr("height", height + margin.top + margin.bottom)
		  .append("g")
			.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
		}
		
	
		root.children.forEach(collapse);
		update(root);
		d3.select(self.frameElement).style("height", "1200px");		
		
	}
	
	function fun_Statuschange(data){
		var images = svg.selectAll(".circle")[0];
		for(var i=0;i<images.length;i++){
			if(images[i].id == data.mac){
				if(data.active == "1"){
					images[i].attributes[2].nodeValue = "http://localhost:3000/images/"+data.type+"_online.png";
				}else{
					images[i].attributes[2].nodeValue = "http://localhost:3000/images/"+data.type+"_offline.png";
				}
				
			}
		}
	}
	
	function update(source) {

		  // Compute the new tree layout.
		  var nodes = tree.nodes(root).reverse(),
			  links = tree.links(nodes);

		  // Normalize for fixed-depth.
		  nodes.forEach(function(d) { d.y = d.depth * 180; });
		  
		  // Update the nodes…
		  var node = svg.selectAll("g.node")
			  .data(nodes, function(d) { return d.mac; });

		  // Enter any new nodes at the parent's previous position.
		  var nodeEnter = node.enter().append("g")
			  .attr("class", "node")
			  .attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; });

		  nodeEnter.append("svg:image")
				.attr("id", function(d){return d.mac})
		      .attr("class", "circle")
		      .attr("xlink:href", function(d){
		         //根据类型来使用图片
		    	  if(d.type == "cbat"){
		    		  if(d.active == "1"){
		    			  return "http://localhost:3000/images/cbat_online.png";
		    		  }else{
		    			  return "http://localhost:3000/images/cbat_offline.png";
		    		  }	    		  
		    	  }else if(d.type == "cnu"){
		    		  if(d.active == "1"){
		    			  return "http://localhost:3000/images/cnu_online.png";
		    		  }else{
		    			  return "http://localhost:3000/images/cnu_offline.png";
		    		  }
		    		  
		    	  }else if(d.type == "folder"){
		    		  return "http://localhost:3000/images/region.png";
		    	  }else{
		    		  return "http://localhost:3000/images/center.png";
		    	  }
		         //return d.expand ? "http://localhost:3000/images/offline.png" : "http://localhost:3000/images/online.gif";
		      })
		      .attr("x", "-24px")
		      .attr("y", "-24px")
		      .attr("width", "48px")
		      .attr("height", "48px")
		      .on('click',click)
		      .on('dblclick',dblclick)

		  nodeEnter.append("text")
			  .attr("x", function(d) { return d.children || d._children ? 25 : 10; })
			  .attr("y","-15px")
			  .attr("dy", ".30em")
			  .attr("text-anchor", function(d) { return d.children || d._children ? "end" : "start"; })
			  .text(function(d) { return d.label; })
			  .style("fill-opacity", 1e-6);

		  // Transition nodes to their new position.
		  var nodeUpdate = node.transition()
			  .duration(duration)
			  .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });

		  nodeUpdate.select("circle")
			  .attr("r", 4.5)
			  .style("fill", function(d) { return d._children ? "lightsteelblue" : "#fff"; });

		  nodeUpdate.select("text")
			  .style("fill-opacity", 1);

		  // Transition exiting nodes to the parent's new position.
		  var nodeExit = node.exit().transition()
			  .duration(duration)
			  .attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
			  .remove();

		  nodeExit.select("circle")
			  .attr("r", 1e-6);

		  nodeExit.select("text")
			  .style("fill-opacity", 1e-6);

		  // Update the links…
		  var link = svg.selectAll("path.link")
			  .data(links, function(d) { return d.target.mac; });

		  // Enter any new links at the parent's previous position.
		  link.enter().insert("path", "g")
			  .attr("class", "link")
			  .attr("d", function(d) {
				var o = {x: source.x0, y: source.y0};
				return diagonal({source: o, target: o});
			  });

		  // Transition links to their new position.
		  link.transition()
			  .duration(duration)
			  .attr("d", diagonal);

		  // Transition exiting nodes to the parent's new position.
		  link.exit().transition()
			  .duration(duration)
			  .attr("d", function(d) {
				var o = {x: source.x, y: source.y};
				return diagonal({source: o, target: o});
			  })
			  .remove();

		  // Stash the old positions for transition.
		  nodes.forEach(function(d) {
			d.x0 = d.x;
			d.y0 = d.y;
		  });
		}

		// Toggle children on click.
		function click(d) {
		  if (d.children) {
			d._children = d.children;
			d.children = null;
		  } else {
			d.children = d._children;
			d._children = null;
		  }
		  update(d);
		}
		
		function dblclick(d) {
			var datastring = '{"mac":"'+d.mac+'","flag":"topology"}';
			if(d.type == "cbat"){
				socket.emit('cbatdetail', datastring );	
			}else if(d.type == "cnu"){
				socket.emit('cnudetail', datastring );	
			}else if(d.type=="hfc"){
	        	  socket.emit('hfcdetail', datastring );	 
			}			  
		}


})(jQuery);