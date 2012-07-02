(function($) {
	var socket;
	var isbusy = false;
	var hfcactive = false;
	var Hfcclock;
  $(function() {
	//根据屏幕分辨率更改页面布局
//	  var ht1 = document.body.offsetHeight;
//	  var ht2 = document.body.scrollHeight;
//	  var ht3 = document.body.scrollTop;
//	  var ht4 = document.body.screenTop;
//	  var ht5 = window.screen.availHeight;
//	  var ht6 = document.body.clientHeight;
      if(window.screen.height >768){
    	  var ht = window.screen.availHeight - 348;
    	  $("#wapper").css("height",ht+"px");
    	  $("#menu").css("height",ht+"px");
    	  $("#navtree").css("height",ht+"px");    	  
      }else if(window.screen.height <= 768){
		  $("#menu").css("overflow","auto");
		  $("#menu").css("height","455px");
		  $("#navtree").css("height","445px");
		  $("#content").css("overflow","auto");
		  $("#content").css("height","455px");
		  $("#wapper").css("min-height","460px");
		  $("#alarm").css("height","130px");
		  $("#newAlarm").css("height","100x");
   	  }
      
      var user = getCookie("userName");
      var flag = getCookie("flag");
      $("#loginuser")[0].text = user;

      
	  socket = io.connect('http://localhost:3000');
                  socket.emit('initDynatree', 'init tree' );

                  socket.on('initDynatree', onInitTree);
                  socket.on('cbatdetail', fun_cbatdetail );
                  socket.on('cnudetail', fun_cnudetail );
                  socket.on('hfcdetail', fun_hfcdetail );
                  socket.on('cbat_modify', fun_cbatmodify );
                  socket.on('cbat_sync', fun_CbatSync );
                  socket.on('cnu_sub', fun_CnuSub );
                  socket.on('cnusync', fun_CnuSync );
                  socket.on('statuschange', fun_Statuschange );
                  socket.on('cbatreset', fun_Cbatreset );
                  socket.on('hfcbase', fun_HfcBase );
                  socket.on('hfcrealtime', fun_Hfcrealtime );                 
      
      
      $("#btn_hbase").live('click', function(){
    	  if(flag == "3"){
    		  alert("Read-Only, permission denied!");
    		  return;
    	  }
    	  if(isbusy != false){
				return;
			} 
			isbusy = true;
			document.body.style.cursor = 'wait';
    	  var hfcmac = document.getElementById('hfc_mac').textContent;
    	  var hfcip = document.getElementById('hfc_ip').value;
    	  var hfclable = document.getElementById('hfc_lable').value;
    	  var datastring = '{"hfcip":"'+hfcip+'","hfclable":"'+hfclable+'","hfcmac":"'+hfcmac+'"}';
    	  socket.emit('hfc_baseinfo',datastring);
    	  
    	  var node = $("#navtree").dynatree("getTree").getNodeByKey(hfcmac);
		  node.data.title = hfclable;
		  node.render();
      });
      
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
    	  if(flag == "3"){
    		  alert("Read-Only, permission denied!");
    		  return;
    	  }
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
	 
      $("#btn_reset").live('click', function() { 
    	  if(flag == "3"){
    		  alert("Read-Only, permission denied!");
    		  return;
    	  }
    	  if((confirm( "Load Fail-Safe Defaults? ")==true))
    	  {
    		  if(isbusy != false){
  					return;
	  			} 
	  			isbusy = true;
	  			document.body.style.cursor = 'wait';
    		  var mac = document.getElementById('mac').textContent;
    		  //alert("恢复出厂设置");
    		  socket.emit('cbatreset',mac);
    	  }
      });
      
	 $("#btn_cnusub").live('click', function() { 
		 if(flag == "3"){
   		  alert("Read-Only, permission denied!");
   		  return;
   	  	}
		 if(isbusy != false){
				return;
			} 
			isbusy = true;
			document.body.style.cursor = 'wait';
			var proname = document.getElementById('proname').textContent;
			var mac = document.getElementById('cnu_mac').textContent;
	 		var vlanen = document.getElementById('vlan_en').value;
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
			if((vlan0id>4095)||(vlan0id<0)||(vlan1id>4095)||(vlan1id<0)||(vlan2id>4095)||(vlan2id<0)||(vlan3id>4095)||(vlan3id<0)){
				document.body.style.cursor = 'default';
		 		isbusy = false;
				alert("VLAN ID value should between 0~4095!");
				return;
			}
			if(isNaN(cpuportrxrate)||isNaN(port0txrate)||isNaN(port1txrate)||isNaN(port2txrate)||isNaN(port3txrate)||isNaN(cpuporttxrate)||isNaN(port0rxrate)||isNaN(port1rxrate)||isNaN(port2rxrate)||isNaN(port3rxrate)){
				 document.body.style.cursor = 'default';
		 		 isbusy = false;
				 alert("Rate value must be numberal!");
				 return;
			}
			if((cpuportrxrate>102400)||(port0txrate>102400)||(port1txrate>102400)||(port2txrate>102400)||(port3txrate>102400)
					||(cpuporttxrate>102400)||(port0rxrate>102400)||(port1rxrate>102400)||(port2rxrate>102400)||(port3rxrate>102400)){
				 document.body.style.cursor = 'default';
		 		 isbusy = false;
				 alert("Rate value must between 0~102400!");
				 return;
			}
			if(checknull("vlan0id",vlan0id)&&checknull("vlan1id",vlan1id)&&checknull("vlan2id",vlan2id)&&checknull("vlan3id",vlan3id)
					&&checknull("cpuportrxrate",cpuportrxrate)&&checknull("port0txrate",port0txrate)
					&&checknull("port1txrate",port1txrate)&&checknull("port2txrate",port2txrate)
					&&checknull("port3txrate",port3txrate)&&checknull("cpuporttxrate",cpuporttxrate)
					&&checknull("port0rxrate",port0rxrate)&&checknull("port1rxrate",port1rxrate)
					&&checknull("port2rxrate",port2rxrate)&&checknull("port3rxrate",port3rxrate)){
				
			}else{
				document.body.style.cursor = 'default';
		 		isbusy = false;
				alert("Null is not allowed!");
				return;
			}
			
			var datastring = '{"proname":"'+proname+'","mac":"'+mac+'","vlanen":"'+vlanen+
			'","vlan0id":"'+vlan0id+'","vlan1id":"'+vlan1id+'","vlan2id":"'+vlan2id+'","vlan3id":"'+vlan3id+
			'","rxlimitsts":"'+rxlimitsts+'","cpuportrxrate":"'+cpuportrxrate+'","port0txrate":"'+port0txrate+
			'","port1txrate":"'+port1txrate+'","port2txrate":"'+port2txrate+'","port3txrate":"'+port3txrate+
			'","txlimitsts":"'+txlimitsts+'","cpuporttxrate":"'+cpuporttxrate+'","port0rxrate":"'+port0rxrate+
			'","port1rxrate":"'+port1rxrate+'","port2rxrate":"'+port2rxrate+'","port3rxrate":"'+port3rxrate+'"}';
			
			socket.emit('cnu_sub',datastring);
	 });
	 
	 $("#btn_sub").live('click', function() { 	
		    if(flag == "3"){
	   		  alert("Read-Only, permission denied!");
	   		  return;
   	  		}
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
				alert("IP address invalid!"); 
				document.body.style.cursor = 'default';
				isbusy = false;
				return;
			}  
			reg = netmask.match(exp);
			if(reg==null) 
			{ 
				alert("Subnet mask invalid!"); 
				document.body.style.cursor = 'default';
				isbusy = false;
				return;
			}
			if(gateway != ""){
				reg = gateway.match(exp);
				if(reg==null) 
				{ 
					alert("Gateway address invalid!"); 
					document.body.style.cursor = 'default';
					isbusy = false;
					return;
				}
			}
			
			reg = trapserver.match(exp);
			if(reg==null) 
			{ 
				alert("TrapServer invalid!"); 
				document.body.style.cursor = 'default';
				isbusy = false;
				return;
			}
			if(isNaN(trap_port)||(trap_port>65535)||(trap_port<0)){
				document.body.style.cursor = 'default';
				isbusy = false;
				alert("Port ID must be numberal between 0~65535"); 
				return;
			}
			if(isNaN(mvlanid)){
				document.body.style.cursor = 'default';
				isbusy = false;
				alert("VLAN ID value should between 0~4095!"); 
				return;
			}
			if(mvlanid>4095 || mvlanid <0){
				document.body.style.cursor = 'default';
				isbusy = false;
				alert("VLAN ID value should between 0~4095!"); 
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
	 
	//table
		$('#power').dataTable( {	  			 		
			"bFilter": false,						//不使用过滤功能
			"bLengthChange": false,					//用户不可改变每页显示数量
			"iDisplayLength": 5,					//每页显示5条数据
 		"bInfo": false,	
	        "sPaginationType": "full_numbers",				        
	        "oLanguage": {							//汉化
				"sLengthMenu": "Display _MENU_ strip record per page",
				"sZeroRecords": "No data has been discoveryed",
				"sInfo": "Current data is from _START_ to _END_ strip data; has _TOTAL_ strip record in all",
				"sInfoEmtpy": "No data",
				"sProcessing": "Data loading...",
				"oPaginate": {
					"sFirst": "Home",
					"sPrevious": "Previous",
					"sNext": "Next",
					"sLast": "End"
				}
			},
			"aoColumns": [	
						  { "sTitle": "Power source name" , "sClass": "center"},
						  { "sTitle": "Supply voltage" , "sClass": "center"}
						],
			
	    } );
  }); 
      function onInitTree(treedata) {
          initTree(treedata);
     }

     function hfcinterval(){       	
    	 if(hfcactive){
    		 //获取实时参数
    		 var mac = document.getElementById('hfc_mac').textContent;
    		 var node = $("#navtree").dynatree("getTree").getNodeByKey(mac);
    		 if(node.data.online == "1"){
    			 socket.emit('hfcrealtime',mac);
    		 }
    		 
    	 }
     }
     
     function fun_Hfcrealtime(data){
    	 if(data == ""){
    		 //alert("获取数据失败！");
    	 }else{
    		 
    	 }
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
					title: itemv.label,
					key: itemv.mac,
					online:itemv.online,
					tooltip:itemv.ip,
					type:"cbat",
					icon:img
				});
				return;
			}
			node.data.tooltip = itemv.ip;
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
					img = "online.gif";
					node.data.online = "1";
				}else{
					img = "offline.png";
					node.data.online = "0";
				}
				node.addChild({
						title: itemv.lable,
						key: itemv.mac,
						online:itemv.online,
						tooltip:itemv.ip,
						type:"hfc",
						icon:img
					});
				node = $("#navtree").dynatree("getTree").getNodeByKey(itemv.mac);
				node.addChild({
						title: itemv.hp,
						icon:"tp.png"
					});	
				node.addChild({
					title: itemv.mn,
					icon:"tp.png"
				});	
				node.addChild({
						title: itemv.id,
						icon:"tp.png"
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
		}		  					
		node.render();

     }
     
     function fun_HfcBase(data){
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
     
     function fun_Cbatreset(data){
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
      
    function fun_CnuSync(data){
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
    		document.getElementById('vlan_en').value = data.vlanen;
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
        	
        	if(document.getElementById('proname').textContent == "null"){
        		document.getElementById('proname').textContent = "Unknown template";
        	}else{
        		document.getElementById('proname').textContent = data.profilename;
        	}
        	if(data.active == "1"){
        		document.getElementById('cnusts_l').textContent = "Device connect normally";
        		$("#cnusts").css("color","#3ff83d");
        	}else{
        		document.getElementById('cnusts_l').textContent = "Device lose connection";
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
			document.getElementById('ip').value = data.cbatip;
			document.getElementById('vlanen_e').value = data.mvlanenable;
			document.getElementById('mvlanid').value = data.mvlanid;
			document.getElementById('netmask').value = data.netmask;
			document.getElementById('gateway').value = data.gateway;
			document.getElementById('trapserver').value = data.trapserverip;
			document.getElementById('trap_port').value = data.trap_port;
			document.getElementById('cbatsts_l').textContent = "Device connect normally";
			
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
			        	  socket.emit('hfcdetail', node.data.key );	    
			          }
			    },
			    onClick: function(node, event) {
			        // Close menu on click
			        if( $(".contextMenu:visible").length > 0 ){
			          $(".contextMenu").hide();
//			          return false;
			        }
			    },
			    onCreate: function(node, span){
			        bindContextMenu(span);
			    },
			    strings: {
			        loading: "Loading…",
			        loadError: "Load error!"
			    },		                
		        onActivate: function(node) {
		        			        	       	
			    },
		    }); 	  		

   }
   
   function bindContextMenu(span) {
	   var flag = getCookie("flag");
	   if(flag == "3"){
 		  //alert("只读用户，权限不足！");
 		  return;
 	   }
	    // Add context menu to this node:
	    $(span).contextMenu({menu: "myMenu"}, function(action, el, pos) {
	      // The event was bound to the <span> tag, but the node object
	      // is stored in the parent <li> tag
	      var node = $.ui.dynatree.getNode(el);
	      if((confirm( "Sure To Delete ? ")!=true))
    	  {
	    	  return;
    	  }
	      switch( action ) {
	      case "cut":
	      case "copy":
	      case "paste":
	        //copyPaste(action, node);
	        break;
	      case "quit":		        
		      break;
	      default:
	        //删除节点	    	  
	    	  var datastring = '{"mac":"'+node.data.key+'","type":"'+node.data.type+'"}';
	    	  socket.emit('delnode',datastring);
	    	  node.remove();
	    	  window.location.reload();
	      }
	    });
   };
   
   function fun_hfcdetail(jsondata){	   
	   var active;
	   var style;
	   hfcactive = true;
	   //hfc实时参数获取定时器
	   Hfcclock = setInterval(hfcinterval,5000);
	   
	   if(jsondata.active == "Online"){
		   active = "Device connect normally";
		   style = "color:#3ff83d";
	   }else{
		   active = "Device lose connection";
		   style = "color:red";
	   }
	   $("#content").empty();
	   if(jsondata.hfctype == "1310nm Optical Sender OS"){
		   $("#content").append('<div id="devinfo"><h3 style="background-color:#ccc">HFC device information</h3>'+
				   	'<div style="float:left"><img id="pg_dev" src="" style="width:200px;height:100px"/></div>'+
				   	'<div id="cbatsts" style="height:100px;width:300px;margin:10px 10px 1px 210px;'+style+'"><lable id="hfcsts_l" style="font-size:30px;background-color:black;line-height:100px">'+active +'</lable></div>'+
				   	'<br/><div id="configinfo"><ul>'+
					'<li><a href="#tabs-1">Basic information</a></li>'+
					'<li><a href="#tabs-2">relevant parameter</a></li>'+
					'<li><a href="#tabs-3">Trap information</a></li></ul>'+
					'<div id="tabs-1">'+
						'<table id="baseinfo"><tr><td><lable>Serial number : </lable></td><td><lable style="margin-left:0px">'+jsondata.serialnumber+'</lable></td>'+
					   		'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp Device type : </lable></td><td><lable>'+jsondata.hfctype+'</lable></td>'+
					   		'<tr><td><lable>MAC : </lable></td><td><lable id = "hfc_mac">'+jsondata.mac+'</lable></td>'+
							'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp logicID : </lable></td><td><lable>'+jsondata.logicalid+'</lable></td></tr>'+	
							'<tr><td><lable>Device identification : </lable></td><td><input id = "hfc_lable" value='+jsondata.lable+ '></input></td>'+
							'<tr><td><lable>IP : </lable></td><td><input id = "hfc_ip" value='+jsondata.ip+ '></input></td>'+
							'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp Gateway : </lable></td><td><input id = "hfc_gateway" value='+jsondata.gateway+ '></input></td></tr>'+	
						'</table>'+
						'<br/><button id="btn_hbase" style="margin-left:300px">Submit</button>'+
					'</div>'+
					'<div id="tabs-2">'+
						'<table id = "power"/>'+
					'<table id="baseinfo">'+
				   		'<tr><td><lable>current AGC offset : </lable></td><td><lable id="hfc_agc"></lable></td>'+
				   		'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp number of TV signal channel: </lable></td><td><lable id = "hfc_channelnum"></lable></td></tr>'+
						'<tr><td><lable>AGC control en : </lable></td><td><select id="agc_e">'+
							'<option value="1">enable</option>'+
							'<option value="2">disable</option>'+
						'</select></td>'+	
						'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp激光器制冷电流 : </lable></td><td><lable id = "hfc_liu"></lable></td></tr>'+
						'<tr><td><lable>激光器温度 : </lable></td><td><lable id = "hfc_jitemp"></lable></td>'+
						'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp输出光功率 : </lable></td><td><input id = "hfc_gonglv" value="0"></input></td></tr>'+
						'<tr><td><lable>激光器偏置电流 : </lable></td><td><lable id = "hfc_biasliu"></lable></td>'+	
						'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp当前MGC衰减量 : </lable></td><td><input id = "hfc_mgc" value="0"></input></td></tr>'+
						'<tr><td><lable>射频信号衰减量范围 : </lable></td><td><lable id = "hfc_jianrange"></lable></td>'+
						'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp激光器激励电平 : </lable></td><td><input id = "hfc_jilevel" value="0"></input></td></tr>'+
						'<tr><td><lable>激光器类型 : </lable></td><td><lable id = "hfc_jirange"></lable></td>'+
						'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp机内温度 : </lable></td><td><lable id = "hfc_innertemp"></lable></td></tr>'+
				'</table>'+			
					'</div>'+
					'<div id="tabs-3">'+
						'<lable>TrapIP : </lable></td><td><input id = "hfc_trapip" value='+jsondata.trapip+ '></input>'+
					'</div>'+
					'</div>');
	   }
	   	

		if(jsondata.hfctype == "WEC-3501I C22"){
			document.getElementById('pg_dev').src = "http://localhost:8080/wen9000/css/images/WEC-3501I C22.jpg";
		}else if(jsondata.hfctype == "WEC-3501I S220"){
			document.getElementById('pg_dev').src = "http://localhost:8080/wen9000/css/images/WEC-3501 S220.jpg";
		}else{
			document.getElementById('pg_dev').src = "http://localhost:8080/wen9000/css/images/Trans.jpg";
		}
	   
   }

   
   function fun_cnudetail(jsondata) {
	   //console.log(jsondata);
	   var active;
	   var style;
	   hfcactive = false;
	   if(jsondata.active == "Online"){
		   active = "Device connect normally";
		   style = "color:#3ff83d";
	   }else{
		   active = "Device lose connection";
		   style = "color:red";
	   }
	   
		$("#content").empty();			          				          		
     	$("#content").append('<div id="devinfo"><h3 style="background-color:#ccc">CNU information</h3>'+
     	'<div style="float:left"><img src="http://localhost:8080/wen9000/css/images/WEC-3702I C4.jpg" style="width:100px;height:80px"/></div>'+
     	'<div id="cnusts" style="height:80px;width:300px;margin:10px 10px 1px 110px;'+style+'"><lable id="cnusts_l" style="font-size:30px;background-color:black;line-height:80px">'+active +'</lable></div>'+						
		'<br/><div id="configinfo"><ul>'+
			'<li><a href="#tabs-1">Basic information</a></li>'+
			'<li><a href="#tabs-2">Config information</a></li></ul>'+
			'<div id="tabs-1">'+
				'<table id="baseinfo"><tr><td><lable>mac :&nbsp &nbsp</lable><lable id="cnu_mac" style="margin-left:0px">'+ jsondata.mac+'</lable></td>'+		     	
		     	'<td><lable>&nbsp &nbsp &nbsp &nbspDevice type: '+jsondata.devicetype+'</lable></td></tr>'+
		     	'<tr><td><lable>Label: </lable>&nbsp<input type="text" id="c_label" style="width:150px" value='+jsondata.label+'></input></td>'+
				'<td><lable>&nbsp &nbsp &nbsp &nbspAddress: </lable><input type="text" id="c_address" style="width:110px" value='+jsondata.address+'></input></td>'+
				'<td><lable>&nbsp &nbsp &nbsp &nbspContact: </lable><input type="text" id="c_contact" style="width:110px" value='+jsondata.contact+'></input></td></tr>'+
				'<tr><td><lable>Phone: </lable><input type="text" style="width:150px" id="c_phone" value='+jsondata.phone+'></input></td></tr>'+
				'</table><button id="btn_cbsave" style="margin-left:300px">Modify</button>'+
			'</div>'+
			'<div id="tabs-2">'+
				'<table id="optinfo"><tr><td><lable>Profile name :</lable></td><td><lable id="proname">'+jsondata.profilename+'</lable></td>'+
				'<td><lable>VLAN En : </lable></td><td><select id="vlan_en"><option value="1">enable</option><option value="2">disable</option></select></td>'+
				'</tr>'+
				'<tr><td><lable>ETH1VLAN: </lable></td><td><input type="text" id="vlan0id" value='+jsondata.vlan0id+'></input></td>'+
				'<td><lable>ETH2VLAN: </lable></td><td><input type="text" id="vlan1id" value='+jsondata.vlan1id+'></input></td>'+
				'<td><lable>ETH3VLAN: </lable></td><td><input type="text" id="vlan2id" value= '+jsondata.vlan2id+'></input></td></tr>'+
				'<tr><td><lable>ETH4VLAN: </lable></td><td><input type="text" id="vlan3id" value='+jsondata.vlan3id+'></input></td></tr>'+
				
				'<tr><td><lable>Down LimitEn:</lable></td><td><select id="rxlimitsts"><option value="1">enable</option><option value="2">disable</option></select></td>'+
				'<td><lable>Down Global Limit :</lable></td><td><input type="text" id="cpuportrxrate" value='+ jsondata.cpuportrxrate+'></input></td>'+
				'<td><lable>ETH1 down limit:</lable></td><td><input type="text" id="port0txrate" value='+jsondata.port0txrate+'></input></td></tr>'+
				'<tr><td><lable>ETH2 down limit:</lable></td><td><input type="text" id="port1txrate" value='+jsondata.port1txrate+'></input></td>'+
				'<td><lable>ETH3 down limit:</lable></td><td><input type="text" id="port2txrate" value='+jsondata.port2txrate+'></input></td>'+
				'<td><lable>ETH4 down limit:</lable></td><td><input type="text" id="port3txrate" value='+jsondata.port3txrate+'></input></td></tr>'+
				
				'<tr><td><lable>Up limitEn:</lable></td><td><select id="txlimitsts"><option value="1">enable</option><option value="2">disable</option></select></td>'+
				'<td><lable>Up Global Limit :</lable></td><td><input type="text" id="cpuporttxrate" value='+ jsondata.cpuporttxrate+'></input></td>'+
				'<td><lable>ETH1 up limit:</lable></td><td><input type="text" id="port0rxrate" value='+jsondata.port0rxrate+'></input></td></tr>'+
				'<tr><td><lable>ETH2 up limit:</lable></td><td><input type="text" id="port1rxrate" value='+jsondata.port1rxrate+'></input></td>'+
				'<td><lable>ETH3 up limit:</lable></td><td><input type="text" id="port2rxrate" value='+jsondata.port2rxrate+'></input></td>'+
				'<td><lable>ETH4 up limit:</lable></td><td><input type="text" id="port3rxrate" value='+jsondata.port3rxrate+'></input></td></tr>'+
				'</table><br/>'+
				'<button id="btn_cnusync" style="margin-left:200px">Refresh</button><button id="btn_cnusub" style="margin-left:60px">Submit</button>'+			
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
		}else if(jsondata=="ipconflict"){
			alert("Cbat IP is the same with other cbat IP!");
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
	   hfcactive = false;
	   if(jsondata.active == "Online"){
		   active = "Device connect";
		   style = "color:#3ff83d";
	   }else{
		   active = "Device lose";
		   style = "color:red";
	   }
	   $("#content").empty();
	   	$("#content").append('<div id="devinfo"><h3 style="background-color:#ccc">Cbat information</h3>'+
	   	'<div style="float:left"><img id="pg_dev" src="" style="width:200px;height:100px"/></div>'+
	   	'<div id="cbatsts" style="height:100px;width:200px;margin:10px 10px 1px 210px;'+style+'"><lable id="cbatsts_l" style="font-size:30px;background-color:black;line-height:100px">'+active +'</lable></div>'+
	   	'<h3 style="background-color:#ccc">Basic information</h3>'+
	   	'<table id="baseinfo"><tr><td><lable>mac : </lable></td><td><lable style="margin-left:0px" id = "mac">'+jsondata.mac+'</lable></td>'+
	   		'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp Device type : </lable></td><td><lable>'+jsondata.devicetype+'</lable></td>'+
	   		'<tr><td><lable>Software version : </lable></td><td><lable>'+jsondata.appver+'</lable></td></tr>'+
			'<tr><td><lable>Label : </lable></td><td><input type="text" id="label" value='+jsondata.label+'></input></td>'+
			'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp Address : </lable></td><td><input type="text" id="address" value='+jsondata.address+'></input></td></tr>'+			
			'<tr><td><lable>ip : </lable></td><td><input type="text" id="ip" value='+jsondata.ip+'></input></td>'+
			'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp Subnet mask : </lable></td><td><input type="text" id="netmask" value='+jsondata.netmask+'></input></td></tr>'+
			'<tr><td><lable>Gate way : </lable></td><td><input type="text" id="gateway" value='+jsondata.gateway+'></input></td></tr>'+						
			'<tr><td><lable>TrapServer : </lable></td><td><input type="text" id="trapserver" value='+jsondata.trapserver+'></input></td>'+
			'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp Port ID : </lable></td><td><input type="text" id="trap_port" value='+jsondata.agentport+'></input></td></tr>'+
			'<tr><td><lable>Management VLAN en : </lable></td><td><select name="vlanen_e" id="vlanen_e">'+
							'<option value="1">enable</option>'+
							'<option value="2">diable</option>'+
						'</select></td>'+
			'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp Management VLAN ID : </lable></td><td><input type="text" id="mvlanid" value='+jsondata.mvlanid+'></input></td></tr>'+
			'</table></div><br/>'+
			'<div><hr/><button id="btn_sub" style="margin-left:60px">Submit</button><button id="btn_sync" style="margin-left:190px">Refresh</button>'+
			'<button id="btn_reset" style="margin-left:160px">Load Fail-Safe Defaults</button>'+
			'</div>');
			
			document.getElementById('vlanen_e').value = jsondata.mvlanenable;
			if(jsondata.devicetype == "WEC-3501I C22"){
				document.getElementById('pg_dev').src = "http://localhost:8080/wen9000/css/images/WEC-3501I C22.jpg";
			}else if(jsondata.devicetype == "WEC-3501I S220"){
				document.getElementById('pg_dev').src = "http://localhost:8080/wen9000/css/images/WEC-3501 S220.jpg";
			}
   }
   
   function checknull(name,val){
	   if(val == ""){
		   return false;
	   }
	   return true;
   }
   
   function getCookie(objName)//获取指定名称的cookie的值
	{    
	    var arrStr = document.cookie.split(";");
	    
	        for(var i = 0;i < arrStr.length;i++)
	            {
	                var temp = arrStr[i].split("=");
	                if(objName.trim()==temp[0].trim()) //此处如果没有去掉字符串空格就不行,偶在这里折腾了半死,主要是这种错误不好跟踪啊
	                {                
	                	return temp[1];
	                }                            
	            }
	}
})(jQuery);
