(function($) {
	var socket;
	var isbusy = false;
  $(function() {
	//根据屏幕分辨率更改页面布局
//	  var ht1 = document.body.offsetHeight;
//	  var ht2 = document.body.scrollHeight;
//	  var ht3 = document.body.scrollTop;
//	  var ht4 = document.body.screenTop;
//	  var ht5 = window.screen.availHeight;
//	  var ht6 = document.body.clientHeight;
      if(window.screen.height >768){
    	  var ht = window.screen.availHeight - 345;
    	  $("#wapper").css("height",ht+"px");
    	  $("#menu").css("height",ht+"px");
      }else if(window.screen.height == 768){
			  $("#menu").css("overflow","auto");
   			  $("#menu").css("height","455px");
   			  $("#navtree").css("height","445px");
   			  $("#content").css("overflow","auto");
   			  $("#content").css("height","455px");
   			  $("#wapper").css("min-height","460px");
   			$("#alarm").css("height","130px");
   			$("#newAlarm").css("height","100x");
   		  }
      
	  socket = io.connect('http://localhost:3000');
                  socket.emit('initDynatree', 'init tree' );

                  socket.on('initDynatree', onInitTree);
                  socket.on('cbatdetail', fun_cbatdetail );
                  socket.on('cnudetail', fun_cnudetail );
                  socket.on('cbat_modify', fun_cbatmodify );
                  socket.on('cbat_sync', fun_CbatSync );
                  socket.on('cnu_sub', fun_CnuSub );
                  socket.on('cnusync', fun_CnuSync );
                  socket.on('statuschange', fun_Statuschange );
      
      
      $("#btn_cnusync").live('click', function(){
    	  if(isbusy != false){
				return;
			} 
			isbusy = true;
			document.body.style.cursor = 'wait';
    	  var mac = document.getElementById('cnu_mac').textContent;
    	  socket.emit('cnusync', mac );
      });
      
      $("#btn_cbsave").live('click', function(){
	  		var c_address = document.getElementById('c_address').value;
	  		var c_contact = document.getElementById('c_contact').value;
	  		var c_phone = document.getElementById('c_phone').value;
	  		var c_label = document.getElementById('c_label').value;
	  		var c_mac = document.getElementById('cnu_mac').textContent;
	  		
	  		var datastring = '{"address":"'+c_address+'","contact":"'+c_contact+'","phone":"'+c_phone+
	  			'","mac":"'+c_mac+'","label":"'+c_label+'"}';
	  		
	  		socket.emit('cnu_basesub',datastring);
	  		
	  		var node = $("#navtree").dynatree("getTree").getNodeByKey(c_mac);
			node.data.title = c_label;
			node.render();
			//成功提示对话框
			$( "#dialog:ui-dialog" ).dialog( "destroy" );

			$( "#dialog-message" ).dialog({
				autoOpen: false,
				show: "blind",
				modal: true,
				resizable: false,
				hide: "explode",
				buttons: {
					Ok: function() {
						$( this ).dialog( "close" );
					}
				}
			});
			$("#dialog-message").dialog("open");
	  		
	  	});
          
      $("#configinfo").livequery( function(){
	  		$(this).tabs();
      });
	 
	 $("#btn_cnusub").live('click', function() { 
		 if(isbusy != false){
				return;
			} 
			isbusy = true;
			document.body.style.cursor = 'wait';
			var proname = document.getElementById('proname').textContent;
			var mac = document.getElementById('cnu_mac').textContent;
	 		var vlanen = document.getElementById('vlan_en').value;
	    	//var vlanid = document.getElementById('vlanid').value;
	    	var vlan0id = document.getElementById('vlan0id').value;
	    	var vlan1id = document.getElementById('vlan1id').value;
	    	var vlan2id = document.getElementById('vlan2id').value;
	    	var vlan3id = document.getElementById('vlan3id').value;
	    	var rxlimitsts = document.getElementById('rxlimitsts').value;
	    	var cpuportrxrate = document.getElementById('cpuportrxrate').value;
	    	var port0txrate = document.getElementById('port0txrate').value;
	    	var port1txrate = document.getElementById('port1txrate').value;
	    	var port2txrate = document.getElementById('port2txrate').value;
	    	var port3txrate = document.getElementById('port3txrate').value;
	    	var txlimitsts = document.getElementById('txlimitsts').value;
	    	var cpuporttxrate = document.getElementById('cpuporttxrate').value;
	    	var port0rxrate = document.getElementById('port0rxrate').value;
	    	var port1rxrate = document.getElementById('port1rxrate').value;
	    	var port2rxrate = document.getElementById('port2rxrate').value;
	    	var port3rxrate = document.getElementById('port3rxrate').value;
			
			var datastring = '{"proname":"'+proname+'","mac":"'+mac+'","vlanen":"'+vlanen+
			'","vlan0id":"'+vlan0id+'","vlan1id":"'+vlan1id+'","vlan2id":"'+vlan2id+'","vlan3id":"'+vlan3id+
			'","rxlimitsts":"'+rxlimitsts+'","cpuportrxrate":"'+cpuportrxrate+'","port0txrate":"'+port0txrate+
			'","port1txrate":"'+port1txrate+'","port2txrate":"'+port2txrate+'","port3txrate":"'+port3txrate+
			'","txlimitsts":"'+txlimitsts+'","cpuporttxrate":"'+cpuporttxrate+'","port0rxrate":"'+port0rxrate+
			'","port1rxrate":"'+port1rxrate+'","port2rxrate":"'+port2rxrate+'","port3rxrate":"'+port3rxrate+'"}';
			
			socket.emit('cnu_sub',datastring);
	 });
	 
	 $("#btn_sub").live('click', function() { 	
			if(isbusy != false){
				return;
			} 
			isbusy = true;
			document.body.style.cursor = 'wait';
			var mac = document.getElementById('mac').textContent; 			
			var ip = document.getElementById('ip').value;
			var label = document.getElementById('label').value;
			var address = document.getElementById('address').value;
			var mvlanenable = document.getElementById('vlanen_e').value;
			var mvlanid = document.getElementById('mvlanid').value;
			var trapserver = document.getElementById('trapserver').value;
			var trap_port = document.getElementById('trap_port').value;
			var netmask = document.getElementById('netmask').value;
			var gateway = document.getElementById('gateway').value;
			var exp=/^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/; 
			var reg = ip.match(exp); 
			if(reg==null) 
			{ 
				alert("IP地址不合法！"); 
				document.body.style.cursor = 'default';
				isbusy = false;
				return;
			}  
			if(isNaN(mvlanid)){
				document.body.style.cursor = 'default';
				isbusy = false;
				return;
			}
			if(mvlanid>4095 || mvlanid <0){
				document.body.style.cursor = 'default';
				isbusy = false;
				return;
			}
			var datastring = '{"mac":"'+mac+'","ip":"'+ip+'","label":"'+label+'","address":"'+address+'","mvlanenable":"'+mvlanenable
			+'","mvlanid":"'+mvlanid+'","trapserver":"'+trapserver+'","trap_port":"'+trap_port+'","netmask":"'+netmask+'","gateway":"'
			+gateway+'"}';
			
			var node = $("#navtree").dynatree("getTree").getNodeByKey(mac);
			node.data.title = label;
			node.render();
			
			socket.emit('cbat_modify', datastring );
	  	
		 });
	 
	 $("#btn_sync").live('click', function() { 	
			if(isbusy != false){
				return;
			} 
			isbusy = true;
			document.body.style.cursor = 'wait';
			var mac = document.getElementById('mac').textContent;	
			socket.emit('cbat_sync',mac);

	 });
  }); 
      function onInitTree(treedata) {
          initTree(treedata);
     }
      
     function fun_Statuschange(itemv){
					
		var node = $("#navtree").dynatree("getTree").getNodeByKey(itemv.mac);
		//node.data.online = itemv.online;
		if(itemv.type == "cbat"){
			//	如果是新设备
			if(node == null){
				node = $("#navtree").dynatree("getTree").getNodeByKey("eocroot");
				var img;
				if(itemv.online == "1"){
					img = "cbaton.png";
				}else{
					img = "cbatoff.png";
				}
				node.addChild({
					title: itemv.ip,
					key: itemv.mac,
					online:itemv.online,
					tooltip:itemv.ip,
					type:"cbat",
					icon:img
				});
				return;
			}
			if(itemv.online == "1"){
				node.data.icon = "cbaton.png";
				node.data.online = "1";
			}else{
				node.data.icon = "cbatoff.png";
				node.data.online = "0";
			}
		}else if(itemv.type == "cnu"){
			//	如果是新设备
			if(node == null){
				node = $("#navtree").dynatree("getTree").getNodeByKey(itemv.cbatmac);
				var img;
				if(itemv.online == "1"){
					img = "online.gif";
				}else{
					img = "offline.png";
				}
				node.addChild({
						title: itemv.mac,
						key: itemv.mac,
						online:itemv.online,
						tooltip:itemv.mac,
						type:"cnu",
						icon:img
					});
				return;
			}		
			//移机
			if(node.getParent().data.key != itemv.cbatmac){
				//删除原节点
				node.remove();
				node = $("#navtree").dynatree("getTree").getNodeByKey(itemv.cbatmac);
				var img;
				if(itemv.online == "1"){
					img = "online.gif";
				}else{
					img = "offline.png";
				}
				node.addChild({
						title: itemv.mac,
						key: itemv.mac,
						online:itemv.online,
						tooltip:itemv.mac,
						type:"cnu",
						icon:img
					});
				return;
			}
			if(itemv.online == "1"){
				node.data.icon = "online.gif";
				node.data.online = "1";
			}else{
				node.data.icon = "offline.png";
				node.data.online = "0";
			}
		}else if(itemv.type == "hfc"){
			if(node == null){
				node = $("#navtree").dynatree("getTree").getNodeByKey("hfcroot");
				var img;
				if(itemv.online == "1"){
					img = "doc_with_children.gif";
				}else{
					img = "offline.png";
				}
				node.addChild({
						title: itemv.ip,
						key: itemv.mac,
						online:itemv.online,
						tooltip:itemv.mac,
						type:"hfc",
						icon:img
					});
				node = $("#navtree").dynatree("getTree").getNodeByKey(itemv.mac);
				node.addChild({
						title: itemv.hp,
						icon:"tp.png"
					});	
				node.addChild({
					title: itemv.sn,
					icon:"tp.png"
				});	
				node.addChild({
						title: itemv.id,
						icon:"tp.png"
					});	
				return false;
			}			  					
			if(itemv.online == "1"){
				node.data.icon = "doc_with_children.gif";
				node.data.online = "1";
			}else{
				node.data.icon = "offline.png";
				node.data.online = "0";
			}
		}		  					
		node.render();

     }
      
    function fun_CnuSync(data){
    	document.body.style.cursor = 'default';
		isbusy = false;
    	if(data==""){
    		//失败提示对话框					
			$( "#dialog-message-failed" ).dialog({
				autoOpen: false,
				show: "blind",
				modal: true,
				resizable: false,
				hide: "explode",
				buttons: {
					Ok: function() {
						$( this ).dialog( "close" );
					}
				}
			});
			$("#dialog-message-failed").dialog("open");
    	}else{
    		document.getElementById('vlan_en').value = data.vlanen;
        	//document.getElementById('vlanid').value = data.vlanid;
        	document.getElementById('vlan0id').value = data.vlan0id;
        	document.getElementById('vlan1id').value = data.vlan1id;
        	document.getElementById('vlan2id').value = data.vlan2id;
        	document.getElementById('vlan3id').value = data.vlan3id;
        	document.getElementById('rxlimitsts').value = data.rxlimitsts;
        	document.getElementById('cpuportrxrate').value = data.cpuportrxrate;
        	document.getElementById('port0txrate').value = data.port0txrate;
        	document.getElementById('port1txrate').value = data.port1txrate;
        	document.getElementById('port2txrate').value = data.port2txrate;
        	document.getElementById('port3txrate').value = data.port3txrate;
        	document.getElementById('txlimitsts').value = data.txlimitsts;
        	document.getElementById('cpuporttxrate').value = data.cpuporttxrate;
        	document.getElementById('port0rxrate').value = data.port0rxrate;
        	document.getElementById('port1rxrate').value = data.port1rxrate;
        	document.getElementById('port2rxrate').value = data.port2rxrate;
        	document.getElementById('port3rxrate').value = data.port3rxrate;
        	
        	if(document.getElementById('proname').textContent=="null"){
        		document.getElementById('proname').textContent="未知模板";
        	}else{
        		document.getElementById('proname').textContent=data.profilename;
        	}
        	if(data.active == "1"){
        		document.getElementById('cnusts_l').textContent = "设备连接正常";
        		$("#cnusts").css("color","#3ff83d");
        	}else{
        		document.getElementById('cnusts_l').textContent = "设备失去连接";
        		$("#cnusts").css("color","red");
        	}
        	
			
			
			
        	//成功提示对话框
			$( "#dialog:ui-dialog" ).dialog( "destroy" );

			$( "#dialog-message" ).dialog({
				autoOpen: false,
				show: "blind",
				modal: true,
				resizable: false,
				hide: "explode",
				buttons: {
					Ok: function() {
						$( this ).dialog( "close" );
					}
				}
			});
			$("#dialog-message").dialog("open");
    	}
    	
    }
      
  	function fun_CnuSub(data){
  		document.body.style.cursor = 'default';
		isbusy = false;
  		if(data == ""){
  		//失败提示对话框					
			$( "#dialog-message-failed" ).dialog({
				autoOpen: false,
				show: "blind",
				modal: true,
				resizable: false,
				hide: "explode",
				buttons: {
					Ok: function() {
						$( this ).dialog( "close" );
					}
				}
			});
			$("#dialog-message-failed").dialog("open");
  		}else{
  			//更新模板名称
  			document.getElementById('proname').textContent=data.profilename;
  			//成功提示对话框
			$( "#dialog:ui-dialog" ).dialog( "destroy" );

			$( "#dialog-message" ).dialog({
				autoOpen: false,
				show: "blind",
				modal: true,
				resizable: false,
				hide: "explode",
				buttons: {
					Ok: function() {
						$( this ).dialog( "close" );
					}
				}
			});
			$("#dialog-message").dialog("open");
  		}
  	}
  	
  	function fun_CbatSync(data){
  		document.body.style.cursor = 'default';
		isbusy = false;
		
		if(data == ""){
			//失败提示对话框					
			$( "#dialog-message-failed" ).dialog({
				autoOpen: false,
				show: "blind",
				modal: true,
				resizable: false,
				hide: "explode",
				buttons: {
					Ok: function() {
						$( this ).dialog( "close" );
					}
				}
			});
			$("#dialog-message-failed").dialog("open");
		}else{
			document.getElementById('vlanen_e').value = data.mvlanenable;
			document.getElementById('mvlanid').value = data.mvlanid;
			document.getElementById('netmask').value = data.netmask;
			document.getElementById('gateway').value = data.gateway;
			document.getElementById('trapserver').value = data.trapserverip;
			document.getElementById('trap_port').value = data.trap_port;
			document.getElementById('cbatsts_l').textContent = "设备连接正常";
			
			$("#cbatsts").css("color","#3ff83d");
			//成功提示对话框
			$( "#dialog:ui-dialog" ).dialog( "destroy" );

			$( "#dialog-message" ).dialog({
				autoOpen: false,
				show: "blind",
				modal: true,
				resizable: false,
				hide: "explode",
				buttons: {
					Ok: function() {
						$( this ).dialog( "close" );
					}
				}
			});
			$("#dialog-message").dialog("open");
		}
  	}

   function initTree(treedata) {
	$("#navtree").dynatree({
  			 	persist: true,
  			 	selectMode: 3,
  			 	activeVisible: true, 
  			 	autoFocus: false,  			 	
  			 	onPostInit: function(isReloading, isError) {
		               //logMsg("onPostInit(%o, %o) - %o", isReloading, isError, this);
		         this.reactivate();
		        }, 
		      	fx: { height: "toggle", duration: 200 },
                children: treedata,
		        imagePath: "http://localhost:8080/wen9000/css/images/",
		        minExpandLevel: 1,
				onDblClick: function(node, event) {
					var jsondata;
			        if(node.data.type=="cbat"){	
			        	socket.emit('cbatdetail', node.data.key );			        						        	
									       	          	
			          }else if(node.data.type == "cnu"){
			        	  socket.emit('cnudetail', node.data.key );				          			          		  	         	          		          	
			          	
			          }else if(node.data.type=="hfc"){
			          		var urltext="http://localhost:8080/wen9000/global/hfcs/"+node.data.key;
				          	$.ajax({url:urltext,dataType:"text",success:function(text){
				          		jsondata=$.parseJSON(text);			          		
				          		$("#main").empty();		
				          		$('.ui-dialog #dialog').remove();	          				          		
					          	$("#main").append('<div id="devinfo"><h6>HFC设备信息</h6>'+
					          	'<div style="float:left"><img src="/wen9000/css/images/Trans.jpg" style="width:200px;height:100px"/></div>'+
					        	'<div style=" height:100px;width:auto;margin:10px 0px 1px 210px"><lable>这里描述设备的功能信息</lable></div>'+
					        	'<h3 style="color:green">基本信息</h3><hr/>'+
					          	'<table id="baseinfo"><tr><td><lable>mac ='+ jsondata.mac+'</lable></td>'+
					          	'<td><lable>ip ='+ jsondata.ip+'</lable></td></tr></div>'

								);
				          	}
				          	});		    
			          }
			    },
			    strings: {
			        loading: "Loading…",
			        loadError: "Load error!"
			    },
		       // onDblClick: function(node, event) {
			   //     node.toggleExpand();
			   // },		                
		        onActivate: function(node) {
		        			        	       	
			    },
		    }); 	  		

   }
   
   function fun_cnudetail(jsondata) {
	   console.log(jsondata);
	   var active;
	   var style;
	   if(jsondata.active == "在线"){
		   active = "设备连接正常";
		   style = "color:#3ff83d";
	   }else{
		   active = "设备失去连接";
		   style = "color:red";
	   }
	   
		$("#content").empty();			          				          		
     	$("#content").append('<div id="devinfo"><h3 style="background-color:#ccc">终端设备信息</h3>'+
     	'<div style="float:left"><img src="http://localhost:8080/wen9000/css/images/3702I.jpg" style="width:100px;height:80px"/></div>'+
     	'<div id="cnusts" style="height:80px;width:200px;margin:10px 10px 1px 110px;'+style+'"><lable id="cnusts_l" style="font-size:30px;background-color:black;line-height:80px">'+active +'</lable></div>'+						
		'<br/><div id="configinfo"><ul>'+
			'<li><a href="#tabs-1">基本信息</a></li>'+
			'<li><a href="#tabs-2">配置信息</a></li></ul>'+
			'<div id="tabs-1">'+
				'<table id="baseinfo"><tr><td><lable>mac :&nbsp &nbsp &nbsp &nbsp</lable><lable id="cnu_mac" style="margin-left:0px">'+ jsondata.mac+'</lable></td>'+		     	
		     	'<td><lable>设备类型: '+jsondata.devicetype+'</lable></td></tr>'+
		     	'<tr><td><lable>设备标识: </lable>&nbsp<input type="text" id="c_label" style="width:150px" value='+jsondata.label+'></input></td>'+
				'<td><lable>地址: </lable><input type="text" id="c_address" style="width:150px" value='+jsondata.address+'></input></td>'+
				'<td><lable>联系方式 : </lable><input type="text" id="c_contact" style="width:150px" value='+jsondata.contact+'></input></td></tr>'+
				'<tr><td><lable>电话: &nbsp &nbsp &nbsp &nbsp</lable><input type="text" style="width:150px" id="c_phone" value='+jsondata.phone+'></input></td></tr>'+
				'</table><button id="btn_cbsave" style="margin-left:300px">修改</button>'+
			'</div>'+
			'<div id="tabs-2">'+
				'<table id="optinfo"><tr><td><lable>模板名称 :</lable></td><td><lable id="proname">'+jsondata.profilename+'</lable></td>'+
				'<td><lable>VLAN使能 : </lable></td><td><select id="vlan_en"><option value="1">启动</option><option value="2">禁止</option></select></td>'+
				'</tr>'+
				'<tr><td><lable>1端口VLAN: </lable></td><td><input type="text" id="vlan0id" value='+jsondata.vlan0id+'></input></td>'+
				'<td><lable>2端口VLAN: </lable></td><td><input type="text" id="vlan1id" value='+jsondata.vlan1id+'></input></td>'+
				'<td><lable>3端口VLAN: </lable></td><td><input type="text" id="vlan2id" value= '+jsondata.vlan2id+'></input></td></tr>'+
				'<tr><td><lable>4端口VLAN: </lable></td><td><input type="text" id="vlan3id" value='+jsondata.vlan3id+'></input></td></tr>'+
				
				'<tr><td><lable>下行限速使能 :</lable></td><td><select id="rxlimitsts"><option value="1">启动</option><option value="2">禁止</option></select></td>'+
				'<td><lable>下行全局限速 :</lable></td><td><input type="text" id="cpuportrxrate" value='+ jsondata.cpuportrxrate+'></input></td>'+
				'<td><lable>1端口下行限速:</lable></td><td><input type="text" id="port0txrate" value='+jsondata.port0txrate+'></input></td></tr>'+
				'<tr><td><lable>2端口下行限速:</lable></td><td><input type="text" id="port1txrate" value='+jsondata.port1txrate+'></input></td>'+
				'<td><lable>3端口下行限速:</lable></td><td><input type="text" id="port2txrate" value='+jsondata.port2txrate+'></input></td>'+
				'<td><lable>4端口下行限速:</lable></td><td><input type="text" id="port3txrate" value='+jsondata.port3txrate+'></input></td></tr>'+
				
				'<tr><td><lable>上行限速使能 :</lable></td><td><select id="txlimitsts"><option value="1">启动</option><option value="2">禁止</option></select></td>'+
				'<td><lable>上行全局限速 :</lable></td><td><input type="text" id="cpuporttxrate" value='+ jsondata.cpuporttxrate+'></input></td>'+
				'<td><lable>1端口上行限速:</lable></td><td><input type="text" id="port0rxrate" value='+jsondata.port0rxrate+'></input></td></tr>'+
				'<tr><td><lable>2端口上行限速:</lable></td><td><input type="text" id="port1rxrate" value='+jsondata.port1rxrate+'></input></td>'+
				'<td><lable>3端口上行限速:</lable></td><td><input type="text" id="port2rxrate" value='+jsondata.port2rxrate+'></input></td>'+
				'<td><lable>4端口上行限速:</lable></td><td><input type="text" id="port3rxrate" value='+jsondata.port3rxrate+'></input></td></tr>'+
				'</table><br/>'+
				'<button id="btn_cnusync" style="margin-left:200px">刷新</button><button id="btn_cnusub" style="margin-left:60px">提交</button>'+
			'</div>'+
		'</div>'
		);
		
		document.getElementById('vlan_en').value=jsondata.vlanen;
		document.getElementById('rxlimitsts').value=jsondata.rxlimitsts;
			
		document.getElementById('txlimitsts').value=jsondata.txlimitsts;
		
		var node = $("#navtree").dynatree("getActiveNode");
	   if(node.getParent().data.online != "1"){
		   $("#btn_cnusync")[0].disabled = "disabled";
		   $("#btn_cnusub")[0].disabled = "disabled";
	   }
		
   }
   
    	
   
   function fun_cbatmodify(jsondata){
	   document.body.style.cursor = 'default';
	   isbusy = false;

	   if(jsondata==""){
			//失败提示对话框					
			$( "#dialog-message-failed" ).dialog({
				autoOpen: false,
				show: "blind",
				modal: true,
				resizable: false,
				hide: "explode",
				buttons: {
					Ok: function() {
						$( this ).dialog( "close" );
					}
				}
			});
			$("#dialog-message-failed").dialog("open");
		}else{
			$( "#dialog:ui-dialog" ).dialog( "destroy" );

			$( "#dialog-message" ).dialog({
				autoOpen: false,
				show: "blind",
				modal: true,
				resizable: false,
				hide: "explode",
				buttons: {
					Ok: function() {
						$( this ).dialog( "close" );
					}
				}
			});
			$("#dialog-message").dialog("open");
		}
		
		
   }
   
   function fun_cbatdetail(jsondata) {
	   console.log(jsondata);
	   var active;
	   var style;
	   if(jsondata.active == "在线"){
		   active = "设备连接正常";
		   style = "color:#3ff83d";
	   }else{
		   active = "设备失去连接";
		   style = "color:red";
	   }
	   $("#content").empty();
	   	$("#content").append('<div id="devinfo"><h3 style="background-color:#ccc">头端设备信息</h3>'+
	   	'<div style="float:left"><img src="http://localhost:8080/wen9000/css/images/3501I.jpg" style="width:200px;height:100px"/></div>'+
	   	'<div id="cbatsts" style="height:100px;width:200px;margin:10px 10px 1px 210px;'+style+'"><lable id="cbatsts_l" style="font-size:30px;background-color:black;line-height:100px">'+active +'</lable></div>'+
	   	'<h3 style="background-color:#ccc">基本信息</h3>'+
	   	'<table id="baseinfo"><tr><td><lable>mac : </lable></td><td><lable style="margin-left:0px" id = "mac">'+jsondata.mac+'</lable></td>'+
	   		'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp设备类型 : </lable></td><td><lable>'+jsondata.devicetype+'</lable></td>'+
	   		'<tr><td><lable>软件版本 : </lable></td><td><lable>'+jsondata.appver+'</lable></td></tr>'+
			'<tr><td><lable>设备标识 : </lable></td><td><input type="text" id="label" value='+jsondata.label+'></input></td>'+
			'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp地址 : </lable></td><td><input type="text" id="address" value='+jsondata.address+'></input></td></tr>'+			
			'<tr><td><lable>ip : </lable></td><td><input type="text" id="ip" value='+jsondata.ip+'></input></td>'+
			'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp子网掩码 : </lable></td><td><input type="text" id="netmask" value='+jsondata.netmask+'></input></td></tr>'+
			'<tr><td><lable>网关 : </lable></td><td><input type="text" id="gateway" value='+jsondata.gateway+'></input></td></tr>'+						
			'<tr><td><lable>TrapServer : </lable></td><td><input type="text" id="trapserver" value='+jsondata.trapserver+'></input></td>'+
			'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp端口号 : </lable></td><td><input type="text" id="trap_port" value='+jsondata.agentport+'></input></td></tr>'+
			'<tr><td><lable>管理VLAN使能 : </lable></td><td><select name="vlanen_e" id="vlanen_e">'+
							'<option value="1">启动</option>'+
							'<option value="2">禁用</option>'+
						'</select></td>'+
			'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp管理VLAN ID : </lable></td><td><input type="text" id="mvlanid" value='+jsondata.mvlanid+'></input></td></tr>'+
			'</table></div><br/>'+
			'<div><hr/><button id="btn_sub" style="margin-left:60px">提交</button><button id="btn_sync" style="margin-left:190px">刷新</button></div>');
			
			document.getElementById('vlanen_e').value = jsondata.mvlanenable;
   }
})(jQuery);
