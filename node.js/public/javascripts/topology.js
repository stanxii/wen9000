(function($){		
	var topology;
	var socket;
	var nodes=[];
	var links=[];
	var childNodes=[
			    {mac:'10.4.43.2',type:'switch',status:1},
			    {mac:'10.4.43.3',type:'switch',status:1}

			];
	var childLinks=[
			    {source:'10.4.43.1',target:'10.4.43.2'},
			    {source:'10.4.43.1',target:'10.4.43.3'},
			    {source:'10.4.43.2',target:'10.4.43.3'}
			]
			
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
		$.each(data, function(key, itemv) {  					
			if(itemv.type == "nodes"){
				var snodes = itemv.nodes;
				$.each(snodes, function(skey, sitemv) { 
					var itemstring = '{"mac":"'+sitemv.mac+'","active":"'+sitemv.active+'","label":"'
						+sitemv.label+'","type":"'+sitemv.type+'"}';
					nodes[nodes.length] =JSON.parse(itemstring);
				});
			}else{
				var slinks = itemv.links;
				$.each(slinks, function(skey, sitemv) { 
					var itemstring = '{"source":"'+sitemv.source+'","target":"'+sitemv.target+'"}';
					links[links.length] =JSON.parse(itemstring);
				});
			}
	 	}); 
		topology=new Topology('topcontainer');

		topology.addNodes(nodes);
		topology.addLinks(links);
		//节点的点击事件
		topology.setNodeClickFn(function(node){
		    //TODO
			
		});
		topology.update();
//		var index = topology.findNodeIndex("30:71:b2:00:21:30");
//		nodes[index].active = 1;		
//		var images = topology.vis.selectAll(".circle")[0];
//		for(var i=0;i<images.length;i++){
//			if(images[i].id == "30:71:b2:00:21:30"){
//				var xxx = images[i];
//				images[i].attributes[2].nodeValue = "http://localhost:3000/images/cnu_online.png";;
//			}
//		}
	}
	
	function fun_Statuschange(data){
		var index = topology.findNodeIndex(data.mac);
		nodes[index].active = data.active;
		var images = topology.vis.selectAll(".circle")[0];
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
	
	function expandNode(mac){
	    topology.addNodes(childNodes);
	    topology.addLinks(childLinks);
	    topology.update();
	}

	function collapseNode(mac){
	    topology.removeChildNodes(mac);
	    topology.update();
	}
	
	function Topology(ele){
	    typeof(ele)=='string' && (ele=document.getElementById(ele));
	    var w=ele.clientWidth,
	        h=ele.clientHeight,
	        self=this;
	    this.force = d3.layout.force().gravity(.05).distance(50).charge(-800).size([w, h]);
	    this.nodes=this.force.nodes();
	    this.links=this.force.links();
	    this.clickFn=function(){};
	    this.vis = d3.select(ele).append("svg:svg")
	                 .attr("width", w).attr("height", h).attr("pointer-events", "all");

	    this.force.on("tick", function(x) {
	      self.vis.selectAll("g.node")
	          .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });

	      self.vis.selectAll("line.link")
	          .attr("x1", function(d) { return d.source.x; })
	          .attr("y1", function(d) { return d.source.y; })
	          .attr("x2", function(d) { return d.target.x; })
	          .attr("y2", function(d) { return d.target.y; });
	    });
	}


	Topology.prototype.doZoom=function(){
	    d3.select(this).select('g').attr("transform","translate(" + d3.event.translate + ")"+ " scale(" + d3.event.scale + ")");

	}


	//增加节点
	Topology.prototype.addNode=function(node){
	    this.nodes.push(node);
	}

	Topology.prototype.addNodes=function(nodes){
	    if (Object.prototype.toString.call(nodes)=='[object Array]' ){
	        var self=this;
	        nodes.forEach(function(node){
	            self.addNode(node);
	        });

	    }
	}

	//增加连线
	Topology.prototype.addLink=function(source,target){
	    this.links.push({source:this.findNode(source),target:this.findNode(target)});
	}

	//增加多个连线
	Topology.prototype.addLinks=function(links){
	    if (Object.prototype.toString.call(links)=='[object Array]' ){
	        var self=this;
	        links.forEach(function(link){
	            self.addLink(link['source'],link['target']);
	        });
	    }
	}


	//删除节点
	Topology.prototype.removeNode=function(mac){
	    var i=0,
	        n=this.findNode(mac),
	        links=this.links;
	    while ( i < links.length){
	        links[i]['source']==n || links[i]['target'] ==n ? links.splice(i,1) : ++i;
	    }
	    this.nodes.splice(this.findNodeIndex(mac),1);
	}

	//删除节点下的子节点，同时清除link信息
	Topology.prototype.removeChildNodes=function(mac){
	    var node=this.findNode(mac),
	        nodes=this.nodes;
	        links=this.links,
	        self=this;

	    var linksToDelete=[],
	        childNodes=[];
	    
	    links.forEach(function(link,index){
	        link['source']==node 
	            && linksToDelete.push(index) 
	            && childNodes.push(link['target']);
	    });

	    linksToDelete.reverse().forEach(function(index){
	        links.splice(index,1);
	    });

	    var remove=function(node){
	        var length=links.length;
	        for(var i=length-1;i>=0;i--){
	            if (links[i]['source'] == node ){
	               var target=links[i]['target'];
	               links.splice(i,1);
	               nodes.splice(self.findNodeIndex(node.mac),1);
	               remove(target);
	               
	            }
	        }
	    }

	    childNodes.forEach(function(node){
	        remove(node);
	    });

	    //清除没有连线的节点
	    for(var i=nodes.length-1;i>=0;i--){
	        var haveFoundNode=false;
	        for(var j=0,l=links.length;j<l;j++){
	            ( links[j]['source']==nodes[i] || links[j]['target']==nodes[i] ) && (haveFoundNode=true) 
	        }
	        !haveFoundNode && nodes.splice(i,1);
	    }
	}



	//查找节点
	Topology.prototype.findNode=function(mac){
	    var nodes=this.nodes;
	    for (var i in nodes){
	        if (nodes[i]['mac']==mac ) return nodes[i];
	    }
	    return null;
	}


	//查找节点所在索引号
	Topology.prototype.findNodeIndex=function(mac){
	    var nodes=this.nodes;
	    for (var i in nodes){
	        if (nodes[i]['mac']==mac ) return i;
	    }
	    return -1;
	}

	//节点点击事件
	Topology.prototype.setNodeClickFn=function(callback){
	    this.clickFn=callback;
	}

	//更新拓扑图状态信息
	Topology.prototype.update=function(){
	  var link = this.vis.selectAll("line.link")
	      .data(this.links, function(d) { return d.source.mac + "-" + d.target.mac; })
	      .attr("class", function(d){
	            return d['source']['active'] && d['target']['active'] ? 'link' :'link link_error';
	      });

	  link.enter().insert("svg:line", "g.node")
	      .attr("class", function(d){
	         return d['source']['active'] && d['target']['active'] ? 'link' :'link link_error';
	      });

	  link.exit().remove();

	  var node = this.vis.selectAll("g.node")
	      .data(this.nodes, function(d) { return d.mac;});

	  var nodeEnter = node.enter().append("svg:g")
	      .attr("class", "node")
	      .call(this.force.drag);

	  //增加图片，可以根据需要来修改
	  var self=this;
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
	      .on('click',function(d){ d.expand && self.clickFn(d);})

	  nodeEnter.append("svg:text")
	      .attr("class", "nodetext")
	      .attr("dx", -12)
	      .attr("dy", -28)
	      .text(function(d) { return d.label });

	  
	  node.exit().remove();

	  this.force.start();
	}


})(jQuery);