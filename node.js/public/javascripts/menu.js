(function($) {
	var socket;
	var isbusy = false;
	var hfcactive = false;
	//var Hfcclock;
	var flag;
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
    	  $("#menu").css("height",ht-30+"px");
    	  $("#navtree").css("height",ht-40+"px");    	  
      }else if(window.screen.height <= 768){
		  $("#menu").css("overflow","auto");
		  $("#menu").css("height","425px");
		  $("#navtree").css("height","415px");
		  $("#content").css("overflow","auto");
		  $("#content").css("height","455px");
		  $("#wapper").css("min-height","460px");
		  $("#alarm").css("height","130px");
		  $("#newAlarm").css("height","100x");
   	  }
      //db = openDatabase("Userdb", "1", "Login users", 1000);
      var user = localStorage.getItem('username');
      flag = localStorage.getItem('flag');
      //var user = getCookie("userName");
      //var flag = getCookie("flag");
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
                  socket.on('hfcsubresponse', fun_Hfcresponse );    
                  socket.on('devsearch', fun_Devsearch ); 
      
      $("#btn_hbase").live('click', function(){
    	  if(flag == "3"){
    		  alert("只读用户，权限不足！");
    		  return;
    	  }
    	  if(isbusy != false){
				return;
		  } 
		  isbusy = true;
		  document.body.style.cursor = 'wait';
    	  var hfcmac = document.getElementById('hfc_mac').textContent;
    	  var hfcip = document.getElementById('hfc_ip').textContent;
    	  var hfclable = document.getElementById('hfc_lable').value;
    	  var datastring = '{"hfcip":"'+hfcip+'","hfclable":"'+hfclable+'","user":"'+user+'","hfcmac":"'+hfcmac+'"}';
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
    		  alert("只读用户，权限不足！");
    		  return;
    	  }
	  		var c_address = document.getElementById('c_address').value;
	  		var c_contact = document.getElementById('c_contact').value;
	  		var c_phone = document.getElementById('c_phone').value;
	  		var c_label = document.getElementById('c_label').value;
	  		var c_mac = document.getElementById('cnu_mac').textContent;
	  		
	  		var datastring = '{"address":"'+c_address+'","user":"'+user+'","contact":"'+c_contact+'","phone":"'+c_phone+
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
    		  alert("只读用户，权限不足！");
    		  return;
    	  }
    	  if((confirm( "要恢复出厂设置吗？ ")==true))
    	  {
    		  if(isbusy != false){
  					return;
	  			} 
	  			isbusy = true;
	  			document.body.style.cursor = 'wait';
    		  var mac = document.getElementById('mac').textContent;
    		  var datastring = '{"user":"'+user+'","mac":"'+mac+'"}';
    		  socket.emit('cbatreset',datastring);
    	  }
      });
      
      $("#btn_reboot").live('click', function() { 
    	  if(flag == "3"){
    		  alert("只读用户，权限不足！");
    		  return;
    	  }    	  
		  if(isbusy != false){
				return;
  			} 
  			isbusy = true;
  			document.body.style.cursor = 'wait';
		  var mac = document.getElementById('mac').textContent;
		  var datastring = '{"user":"'+user+'","mac":"'+mac+'"}';
		  socket.emit('cbatreboot',datastring);
    	  
      });
      
      $("#btn_search").click(function(){
    	  var search_val = document.getElementById('searchbox').value;
    	  var exp=/^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/; 
		  var reg = search_val.match(exp); 
		  if(reg==null) 
		  { 
			  var node = $("#navtree").dynatree("getTree").getNodeByKey(search_val.toLocaleLowerCase());
			  if(node == null){
				  alert("无法查询到设备!");
			  }
			  node.activate()
		  }else{
			  socket.emit('devsearch',search_val);
		  } 
    	  
      });
      
      $("#searchbox").keydown(function(event){ 
    	   if(event.keyCode==13){ 
    		   var search_val = document.getElementById('searchbox').value;
    	    	  var exp=/^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/; 
    			  var reg = search_val.match(exp); 
    			  if(reg==null) 
    			  { 
    				  var node = $("#navtree").dynatree("getTree").getNodeByKey(search_val);
    				  if(node == null){
    					  alert("无法查询到设备!");
    				  }
    				  node.activate()
    			  }else{
    				  socket.emit('devsearch',search_val);
    			  } 
    	      } 
    	   });  
      
	 $("#btn_cnusub").live('click', function() { 
		 if(flag == "3"){
   		  alert("只读用户，权限不足！");
   		  return;
   	  	}
		 if(isbusy != false){
				return;
			} 
			isbusy = true;
			document.body.style.cursor = 'wait';
			var proname = document.getElementById('proname').textContent;
			var mac = document.getElementById('cnu_mac').textContent;
			var authorization = document.getElementById('authorization_en').value;
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
				alert("VLAN值应在0~4095之间！");
				return;
			}
			if(isNaN(cpuportrxrate)||isNaN(port0txrate)||isNaN(port1txrate)||isNaN(port2txrate)||isNaN(port3txrate)||isNaN(cpuporttxrate)||isNaN(port0rxrate)||isNaN(port1rxrate)||isNaN(port2rxrate)||isNaN(port3rxrate)){
				 document.body.style.cursor = 'default';
		 		 isbusy = false;
				 alert("速率值必须是数字！");
				 return;
			}
			if((cpuportrxrate>102400)||(port0txrate>102400)||(port1txrate>102400)||(port2txrate>102400)||(port3txrate>102400)
					||(cpuporttxrate>102400)||(port0rxrate>102400)||(port1rxrate>102400)||(port2rxrate>102400)||(port3rxrate>102400)){
				 document.body.style.cursor = 'default';
		 		 isbusy = false;
				 alert("速率值必须在0~102400之间！");
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
				alert("不能存在空值！");
				return;
			}
			
			var datastring = '{"proname":"'+proname+'","mac":"'+mac+'","authorization":"'+authorization+'","vlanen":"'+vlanen+
			'","vlan0id":"'+vlan0id+'","vlan1id":"'+vlan1id+'","vlan2id":"'+vlan2id+'","vlan3id":"'+vlan3id+
			'","rxlimitsts":"'+rxlimitsts+'","cpuportrxrate":"'+cpuportrxrate+'","port0txrate":"'+port0txrate+
			'","port1txrate":"'+port1txrate+'","port2txrate":"'+port2txrate+'","port3txrate":"'+port3txrate+
			'","txlimitsts":"'+txlimitsts+'","cpuporttxrate":"'+cpuporttxrate+'","port0rxrate":"'+port0rxrate+
			'","port1rxrate":"'+port1rxrate+'","port2rxrate":"'+port2rxrate+'","port3rxrate":"'+port3rxrate+'","user":"'+user+'"}';
			
			socket.emit('cnu_sub',datastring);
	 });
	 
	 $("#btn_sub").live('click', function() { 	
		    if(flag == "3"){
	   		  alert("只读用户，权限不足！");
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
			var dns = document.getElementById('dns').value;
			var telnet = document.getElementById('telnet_timeout').value;
			var exp=/^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/; 
			var reg = ip.match(exp); 
			if(reg==null) 
			{ 
				alert("IP地址不合法！"); 
				document.body.style.cursor = 'default';
				isbusy = false;
				return;
			}  
			reg = netmask.match(exp);
			if(reg==null) 
			{ 
				alert("子网掩码不合法！"); 
				document.body.style.cursor = 'default';
				isbusy = false;
				return;
			}
			if(gateway != ""){
				reg = gateway.match(exp);
				if(reg==null) 
				{ 
					alert("网关地址不合法！"); 
					document.body.style.cursor = 'default';
					isbusy = false;
					return;
				}
			}
			
			reg = trapserver.match(exp);
			if(reg==null) 
			{ 
				alert("TrapServer不合法！"); 
				document.body.style.cursor = 'default';
				isbusy = false;
				return;
			}
			reg = dns.match(exp);
			if(reg==null) 
			{ 
				alert("DNS不合法！"); 
				document.body.style.cursor = 'default';
				isbusy = false;
				return;
			}
			if(isNaN(trap_port)||(trap_port>65535)||(trap_port<0)){
				document.body.style.cursor = 'default';
				isbusy = false;
				alert("端口号必须是数字,且在0~65535之间"); 
				return;
			}
			if(isNaN(mvlanid)){
				document.body.style.cursor = 'default';
				isbusy = false;
				alert("VLAN值应在0~4095之间！"); 
				return;
			}
			if(mvlanid>4095 || mvlanid <0){
				document.body.style.cursor = 'default';
				isbusy = false;
				alert("VLAN值应在0~4095之间！"); 
				return;
			}
			var datastring = '{"mac":"'+mac+'","ip":"'+ip+'","label":"'+label+'","address":"'+address+'","mvlanenable":"'+mvlanenable
			+'","mvlanid":"'+mvlanid+'","trapserver":"'+trapserver+'","trap_port":"'+trap_port+'","netmask":"'+netmask+'","gateway":"'
			+gateway+'","dns":"'+dns+'","telnet":"'+telnet+'","user":"'+user+'"}';
			
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
	 
	 $(".hfcbasesub").live('click', function(){
		 if(flag == "3"){
	   		  alert("只读用户，权限不足！");
	   		  return;
  	  		}
		 if(isbusy != false){
				return;
			} 
			isbusy = true;
			document.body.style.cursor = 'wait';
			var key = $(this)[0].id;
			var value;
			var name;
			if(key == "trap1sub"){
				value = document.getElementById('trapip1').value;
				name = "trapip1";
			}else if(key == "trap2sub"){
				value = document.getElementById('trapip2').value;
				name = "trapip2";
			}else if(key == "trap3sub"){
				value = document.getElementById('trapip3').value;
				name = "trapip3";
			}else if(key == "hfcreboot"){
				value = "";
				name = "hfcreboot";
			}
			var ip = document.getElementById('hfc_ip').textContent;
			var mac = document.getElementById('hfc_mac').textContent;
			
			var datastring = '{"code":"1","key":"'+name+'","val":"' + value + '","mac":"' + mac +'","ip":"'+ ip +'","user":"'+user+'"}';
			socket.emit('hfc_sub',datastring);
			
	 });
	 
	 $(".hfcalarmthreshold").live('dblclick', function(){
		 var mac = document.getElementById('hfc_mac').textContent;
		 var node = $("#navtree").dynatree("getTree").getNodeByKey(mac);
		 if(node.data.online == "0"){
			 alert("设备不在线！");
			 return;
		 }
		 if(isbusy != false){
				return;
			} 
			isbusy = true;
			document.body.style.cursor = 'wait';
			var key = $(this)[0].id;
			var type = document.getElementById('hfctype').textContent;
			var ip = document.getElementById('hfc_ip').textContent;
			var mac = document.getElementById('hfc_mac').textContent;
			
			var datastring = '{"type":"'+type+'","key":"'+key+'","cmd":"1","mac":"' + mac +'","ip":"'+ ip +'"}';
			socket.emit('hfc_alarmthresholdsub',datastring);
			
	 });
	 
	//table
		$('#power').dataTable( {	  			 		
			"bFilter": false,						//不使用过滤功能
			"bLengthChange": false,					//用户不可改变每页显示数量
			"iDisplayLength": 5,					//每页显示5条数据
 		"bInfo": false,	
	        "sPaginationType": "full_numbers",				        
	        "oLanguage": {							//汉化
				"sLengthMenu": "每页显示 _MENU_ 条记录",
				"sZeroRecords": "没有检索到数据",
				"sInfo": "当前数据为从第 _START_ 到第 _END_ 条数据；总共有 _TOTAL_ 条记录",
				"sInfoEmtpy": "没有数据",
				"sProcessing": "正在加载数据...",
				"oPaginate": {
					"sFirst": "首页",
					"sPrevious": "前页",
					"sNext": "后页",
					"sLast": "尾页"
				}
			},
			"aoColumns": [	
						  { "sTitle": "电源名称" , "sClass": "center"},
						  { "sTitle": "电源电压" , "sClass": "center"}
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
     
     function fun_Devsearch(data){
    	 if(data == ""){
    		 alert("无法查询到设备!")
    	 }else{
    		 var node = $("#navtree").dynatree("getTree").getNodeByKey(data);
			  if(node == null){
				  alert("无法查询到设备!");
			  }
			  node.activate()
    	 }
     }
     
     function fun_Hfcrealtime(data){
    	 if(data == ""){
    		 //alert("获取数据失败！");
    	 }else{
    		 if(data.hfctype == "掺铒光纤放大器"){
    			 $("#hfc_powerv1")[0].textContent = data.power_v1;
    			 $("#hfc_powerv2")[0].textContent = data.power_v2;
    			 $("#hfc_innertemp")[0].textContent = data.temp;
    			 $("#hfc_ingonglv")[0].textContent = data.inpower;
    			 $("#hfc_gonglv")[0].textContent = data.outpower;
    			 $("#hfc_bias_c1")[0].textContent = data.bias_c1;
    			 $("#hfc_ref_c1")[0].textContent = data.ref_c1;
    			 $("#hfc_pump_t1")[0].textContent = data.pump_t1;
    			 $("#hfc_bias_c2")[0].textContent = data.bias_c2;
    			 $("#hfc_ref_c2")[0].textContent = data.ref_c2;
    			 $("#hfc_pump_t2")[0].textContent = data.pump_t2;
    		 }else if(data.hfctype == "1310nm光发射机"){
    			 $("#hfc_powerv1")[0].textContent = data.power_v1;
    			 $("#hfc_powerv2")[0].textContent = data.power_v2;
    			 $("#hfc_powerv3")[0].textContent = data.power_v3;
    			 $("#hfc_drivelevel")[0].textContent = data.drivelevel;
    			 $("#hfc_rfattrange")[0].textContent = data.rfattrange;
    			 $("#hfc_outputpower")[0].textContent = data.outputpower;
    			 $("#hfc_lasercurrent")[0].textContent = data.lasercurrent;
    			 $("#hfc_temp")[0].textContent = data.temp;
    			 $("#hfc_teccurrent")[0].textContent = data.teccurrent;
    			 $("#hfc_innertemp")[0].textContent = data.innertemp;
    			 var xx = $("#hfcagccontrol");
    			 if(data.agccontrol == "1"){
    				 $("#hfcagccontrol")[0].textContent = "当前状态:AGC";
    			 }else{
    				 $("#hfcagccontrol")[0].textContent = "当前状态:MGC";
    			 }
    		 }
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
				if($("#cbatsts_l").length >0){
					$("#cbatsts_l")[0].textContent = "设备连接正常";
					$("#cbatsts").css("color","#3ff83d");
				}				
			}else{
				node.data.icon = "cbatoff.png";
				node.data.online = "0";
				if($("#cbatsts_l").length >0){
					$("#cbatsts_l")[0].textContent = "设备失去连接";
					$("#cbatsts").css("color","red");
				}				
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
				if($("#cnusts_l").length >0){
					$("#cnusts_l")[0].textContent = "设备连接正常";
					$("#cnusts").css("color","#3ff83d");
				}				
			}else{
				node.data.icon = "offline.png";
				node.data.online = "0";
				if($("#cnusts_l").length >0){
					$("#cnusts_l")[0].textContent = "设备失去连接";
					$("#cnusts").css("color","red");
				}				
			}
		}else if(itemv.type == "hfc"){
			if(node == null){
				node = $("#navtree").dynatree("getTree").getNodeByKey("hfcroot");
				var img;
				if(itemv.online == "1"){
					img = "cbaton.png";
					node.data.online = "1";
				}else{
					img = "cbatoff.png";
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
				if(itemv.online == "1"){
					node.addChild({
						title: itemv.hp,
						icon:"tp.png",
						key:"hfctype"
					});	
					node.addChild({
						title: itemv.mn,
						icon:"tp.png",
						key:"modelnumber"
					});	
					node.addChild({
							title: itemv.id,
							icon:"tp.png",
							key:"logicalid"
					});	
				}else{
					node.addChild({
						title: itemv.hp,
						icon:"disable.png",
						key:"hfctype"
					});	
					node.addChild({
						title: itemv.mn,
						icon:"disable.png",
						key:"modelnumber"
					});	
					node.addChild({
							title: itemv.id,
							icon:"disable.png",
							key:"logicalid"
					});	
				}
				
				return;
			}			  					
			if(itemv.online == "1"){
				node.data.icon = "cbaton.png";
				node.data.online = "1";
				if($("#hfcsts_l").length >0){
					$("#hfcsts_l")[0].textContent = "设备连接正常";
					$("#hfcsts").css("color","#3ff83d");
				}
				node.render();
				node = $("#navtree").dynatree("getTree").getNodeByKey("hfctype");
				node.data.icon = "tp.png";
				node.render();
				node = $("#navtree").dynatree("getTree").getNodeByKey("modelnumber");
				node.data.icon = "tp.png";
				node.render();
				node = $("#navtree").dynatree("getTree").getNodeByKey("logicalid");
				node.data.icon = "tp.png";
				node.render();
			}else{
				node.data.icon = "cbatoff.png";
				node.data.online = "0";
				if($("#hfcsts_l").length >0){
					$("#hfcsts_l")[0].textContent = "设备失去连接";
					$("#hfcsts").css("color","red");
				}	
				node.render();
				node = $("#navtree").dynatree("getTree").getNodeByKey("hfctype");
				node.data.icon = "disable.png";
				node.render();
				node = $("#navtree").dynatree("getTree").getNodeByKey("modelnumber");
				node.data.icon = "disable.png";
				node.render();
				node = $("#navtree").dynatree("getTree").getNodeByKey("logicalid");
				node.data.icon = "disable.png";
				node.render();
			}
		}	
		node.render();
     }
     
     function fun_Hfcresponse(data){
    	 document.body.style.cursor = 'default';
 		 isbusy = false;
    	 if(data.result == "ok"){
    		 if(data.code == "1"){
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
    		 }else if(data.code == "2"){
    			 //告警门限显示
    			 $("#hihi")[0].value = data.HIHIOid;
    			 $("#hi")[0].value = data.HIOid;
    			 $("#lo")[0].value = data.LOOid;
    			 $("#lolo")[0].value = data.LOLOOid;
    			 $("#deadb")[0].value = data.DeadBOid;

    			 var type = document.getElementById('hfctype').textContent;
    			 var ip = document.getElementById('hfc_ip').textContent;
    			 var mac = document.getElementById('hfc_mac').textContent;
    			 $("#dialog-alarmThreshold").dialog({
    					autoOpen: false,
    					resizable: false,
    					show: "blind",
    					hide: "explode",
    					modal: true,
    					height: 300,
    					width: 400,
    					buttons: {					
    						"确定": function() {
    							if(flag == "3"){
    								  $( "#dialog-alarmThreshold" ).dialog("close");
    						   		  alert("只读用户，权限不足！");
    						   		  return;
    					   	  		}
    							var datastring = '{"type":"'+type+'","key":"'+data.key+'","cmd":"2","mac":"' + mac 
    							+'","ip":"'+ ip+'","hihi":"'+ $("#hihi")[0].value+'","hi":"'+ $("#hi")[0].value 
    							+'","lo":"'+ $("#lo")[0].value+'","lolo":"'+ $("#lolo")[0].value
    							+'","deadb":"'+ $("#deadb")[0].value+'"}';
    							socket.emit('hfc_alarmthresholdsub',datastring);
    							$( "#dialog-alarmThreshold" ).dialog("close");						
    						},
        					"取消": function() {
        						$( this ).dialog("close");
        					}
    					},
    					close: function() {

    					}
    				});	
    			
    				$("#dialog-alarmThreshold").dialog("open");
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
    	 }else{
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
    	 }
    	 
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
    		document.getElementById('authorization_en').value = data.authorization;
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
        		document.getElementById('proname').textContent = "未知模板";
        	}else{
        		document.getElementById('proname').textContent = data.profilename;
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
			document.getElementById('ip').value = data.cbatip;
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
	      if((confirm( "确定要删除吗？ ")!=true))
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
   }
   
   function fun_hfcdetail(jsondata){	   
	   var active;
	   var style;
	   hfcactive = true;
	   //hfc实时参数获取定时器
	   //Hfcclock = setInterval(hfcinterval,5000);
	   
	   if(jsondata.active == "在线"){
		   active = "设备连接正常";
		   style = "color:#3ff83d";
	   }else{
		   active = "设备失去连接";
		   style = "color:red";
	   }
	   $("#content").empty();
	   if(jsondata.hfctype == "掺铒光纤放大器"){
		   $("#content").append('<div id="devinfo"><h3 style="background-color:#ccc">HFC设备信息</h3>'+
				   	'<div style="float:left"><img id="pg_dev" src="" style="width:500px;height:100px"/></div>'+
				   	'<div id="hfcsts" style="height:100px;width:200px;margin:10px 10px 1px 510px;'+style+'"><lable id="hfcsts_l" style="font-size:30px;background-color:black;line-height:100px">'+active +'</lable></div>'+
				   	'<br/><div id="configinfo"><ul>'+
					'<li><a href="#tabs-1">基本信息</a></li>'+
					'<li><a href="#tabs-2">相关参数</a></li>'+
					'<li><a href="#tabs-3">Trap信息</a></li></ul>'+
					'<div id="tabs-1">'+
						'<table id="baseinfo"><tr><td><lable>序列号 : </lable></td><td><lable style="margin-left:0px">'+jsondata.serialnumber+'</lable></td>'+
					   		'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp设备类型 : </lable></td><td><lable id="hfctype">'+jsondata.hfctype+'</lable></td>'+
					   		'<tr><td><lable>MAC : </lable></td><td><lable id = "hfc_mac">'+jsondata.mac+'</lable></td>'+
							'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp逻辑ID : </lable></td><td><lable>'+jsondata.logicalid+'</lable></td></tr>'+	
							'<tr><td><lable>IP : </lable></td><td><lable id = "hfc_ip">'+jsondata.ip+'</lable></td>'+
							'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp设备标识 : </lable></td><td><input id = "hfc_lable" value="'+jsondata.lable+ '"></input></td></tr>'+							
						'</table>'+
						'<br/><button id="btn_hbase" style="margin-left:100px">提交</button><button style="margin-left:160px" class="hfcbasesub" id="hfcreboot">重启设备</button>'+
					'</div>'+
					'<div id="tabs-2">'+
						'<table>'+
				   		'<tr><td><lable>电源名称 : </lable></td><td><lable id="hfc_dcpower">'+jsondata.power1+'</lable></td>'+
				   		'<td><lable>&nbsp &nbsp &nbsp &nbsp电源电压 : </lable></td><td><lable id = "hfc_powerv1" class="hfcalarmthreshold">'+jsondata.power_v1+'</lable></td></tr>'+
				   		'<tr><td><lable>电源名称 : </lable></td><td><lable id="hfc_dcpower">'+jsondata.power2+'</lable></td>'+
				   		'<td><lable>&nbsp &nbsp &nbsp &nbsp电源电压 : </lable></td><td><lable id = "hfc_powerv2" class="hfcalarmthreshold">'+jsondata.power_v2+'</lable></td></tr>'+
						'<tr><td><lable>输入光功率 : </lable></td><td><lable id = "hfc_ingonglv" class="hfcalarmthreshold"></lable></td>'+
						'<td><lable>&nbsp &nbsp &nbsp &nbsp输出光功率 : </lable></td><td><lable id = "hfc_gonglv" class="hfcalarmthreshold"></input></td></tr>'+
						'<tr><td><lable>机内温度 : </lable></td><td><lable id = "hfc_innertemp"></lable></td></tr>'+
						'</table>'+
						'<label style="color:green">泵浦参数</label><br/><hr/>'+
						'<table>'+
						'<tr><td><lable>偏置电流1 : </lable></td><td><lable id = "hfc_bias_c1" class="hfcalarmthreshold">'+jsondata.bias_c1+'</lable></td>'+	
						'<td><lable>&nbsp &nbsp &nbsp &nbsp制冷电流1 : </lable></td><td><lable id = "hfc_ref_c1" class="hfcalarmthreshold">'+jsondata.ref_c1+'</lable></td>'+
						'<td><lable>&nbsp &nbsp &nbsp &nbsp泵浦温度1 : </lable></td><td><lable id = "hfc_pump_t1" class="hfcalarmthreshold">'+jsondata.pump_t1+'</lable></td></tr>'+
						'<tr><td><lable>偏置电流2 : </lable></td><td><lable id = "hfc_bias_c2" class="hfcalarmthreshold">'+jsondata.bias_c2+'</lable></td>'+	
						'<td><lable>&nbsp &nbsp &nbsp &nbsp制冷电流2 : </lable></td><td><lable id = "hfc_ref_c2" class="hfcalarmthreshold">'+jsondata.ref_c2+'</lable></td>'+
						'<td><lable>&nbsp &nbsp &nbsp &nbsp泵浦温度2 : </lable></td><td><lable id = "hfc_pump_t2" class="hfcalarmthreshold">'+jsondata.pump_t2+'</lable></td></tr>'+					
						'</table>'+			
					'</div>'+
					'<div id="tabs-3">'+
						'<lable>Trap1IP : </lable></td><td><input id = "trapip1" value='+jsondata.trapip1+ '></input><button class="hfcbasesub" id="trap1sub">修改</button><br/>'+
						'<lable>Trap2IP : </lable></td><td><input id = "trapip2" value='+jsondata.trapip2+ '></input><button class="hfcbasesub" id="trap2sub">修改</button><br/>'+
						'<lable>Trap3IP : </lable></td><td><input id = "trapip3" value='+jsondata.trapip3+ '></input><button class="hfcbasesub" id="trap3sub">修改</button>'+
					'</div>'+
					'</div>');
	   }else if(jsondata.hfctype == "1310nm光发射机"){
		   $("#content").append('<div id="devinfo"><h3 style="background-color:#ccc">HFC设备信息</h3>'+
				   	'<div style="float:left"><img id="pg_dev" src="" style="width:500px;height:100px"/></div>'+
				   	'<div id="hfcsts" style="height:100px;width:200px;margin:10px 10px 1px 510px;'+style+'"><lable id="hfcsts_l" style="font-size:30px;background-color:black;line-height:100px">'+active +'</lable></div>'+
				   	'<br/><div id="configinfo"><ul>'+
					'<li><a href="#tabs-1">基本信息</a></li>'+
					'<li><a href="#tabs-2">相关参数</a></li>'+
					'<li><a href="#tabs-3">Trap信息</a></li></ul>'+
					'<div id="tabs-1">'+
						'<table id="baseinfo"><tr><td><lable>序列号 : </lable></td><td><lable style="margin-left:0px">'+jsondata.serialnumber+'</lable></td>'+
					   		'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp设备类型 : </lable></td><td><lable id="hfctype">'+jsondata.hfctype+'</lable></td>'+
					   		'<tr><td><lable>MAC : </lable></td><td><lable id = "hfc_mac">'+jsondata.mac+'</lable></td>'+
							'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp逻辑ID : </lable></td><td><lable>'+jsondata.logicalid+'</lable></td></tr>'+	
							'<tr><td><lable>IP : </lable></td><td><lable id = "hfc_ip">'+jsondata.ip+'</lable></td>'+
							'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp设备标识 : </lable></td><td><input id = "hfc_lable" value="'+jsondata.lable+ '"></input></td></tr>'+							
						'</table>'+
						'<br/><button id="btn_hbase" style="margin-left:100px">提交</button><button style="margin-left:160px" class="hfcbasesub" id="hfcreboot">重启设备</button>'+
					'</div>'+
					'<div id="tabs-2">'+
						'<table>'+
				   		'<tr><td><lable>电源名称 : </lable></td><td><lable>'+jsondata.power1+'</lable></td>'+
				   		'<td><lable>&nbsp &nbsp &nbsp &nbsp电源电压 : </lable></td><td><lable id = "hfc_powerv1" class="hfcalarmthreshold">'+jsondata.power_v1+'</lable></td></tr>'+
				   		'<tr><td><lable>电源名称 : </lable></td><td><lable>'+jsondata.power2+'</lable></td>'+
				   		'<td><lable>&nbsp &nbsp &nbsp &nbsp电源电压 : </lable></td><td><lable id = "hfc_powerv2" class="hfcalarmthreshold">'+jsondata.power_v2+'</lable></td></tr>'+
				   		'<tr><td><lable>电源名称 : </lable></td><td><lable>'+jsondata.power3+'</lable></td>'+
				   		'<td><lable>&nbsp &nbsp &nbsp &nbsp电源电压 : </lable></td><td><lable id = "hfc_powerv3" class="hfcalarmthreshold">'+jsondata.power_v3+'</lable></td></tr>'+
						'<tr><td><lable>电视信号频道数 : </lable></td><td><input id = "hfc_channelnum" value ="'+jsondata.channelnum +'"></input></td>'+
						'<td><lable>&nbsp &nbsp &nbsp &nbsp激光器激励电平 : </lable></td><td><lable id = "hfc_drivelevel" class="hfcalarmthreshold">'+jsondata.drivelevel+'</lable></td></tr>'+
						'<tr><td><lable>激光器波长(nm) : </lable></td><td><lable id = "hfc_wavelength">'+jsondata.wavelength+'</lable></td>'+
						'<td><lable>&nbsp &nbsp &nbsp &nbsp射频信号衰减量范围 : </lable></td><td><lable id = "hfc_rfattrange" class="hfcalarmthreshold">'+jsondata.rfattrange+'</lable></td></tr>'+
						'<tr><td><lable>激光器类型 : </lable></td><td><lable id = "hfc_lasertype">'+jsondata.lasertype+'</lable></td>'+	
						'<td><lable>&nbsp &nbsp &nbsp &nbsp输出光功率 : </lable></td><td><lable id = "hfc_outputpower" class="hfcalarmthreshold">'+jsondata.outputpower+'</lable></td>'+
						'<td><lable>&nbsp &nbsp &nbsp &nbsp激光器偏置电流 : </lable></td><td><lable id = "hfc_lasercurrent" class="hfcalarmthreshold">'+jsondata.lasercurrent+'</lable></td></tr>'+
						'<tr><td><lable>AGC控制使能 : </lable></td><td><button id = "hfcagccontrol"></button></td>'+	
						'<td><lable>&nbsp &nbsp &nbsp &nbsp激光器温度 : </lable></td><td><lable id = "hfc_temp" class="hfcalarmthreshold">'+jsondata.temp+'</lable></td>'+
						'<td><lable>&nbsp &nbsp &nbsp &nbsp当前MGC衰减量 : </lable></td><td><lable id = "hf_cmgc" class="hfcset">'+jsondata.mgc+'</lable></td></tr>'+	
						'<tr><lable>激光器制冷电流 : </lable></td><td><lable id = "hfc_teccurrent" class="hfcalarmthreshold">'+jsondata.teccurrent+'</lable></td>'+
						'<td><lable>&nbsp &nbsp &nbsp &nbsp当前AGC偏移量 : </lable></td><td><lable id = "hfc_agc" class="hfcset">'+jsondata.agc+'</lable></td>'+
						'<td><lable>&nbsp &nbsp &nbsp &nbsp机内温度 : </lable></td><td><lable id = "hfc_innertemp">'+jsondata.innertemp+'</lable></td></tr>'+	
						'</table>'+			
					'</div>'+
					'<div id="tabs-3">'+
						'<lable>Trap1IP : </lable></td><td><input id = "trapip1" value='+jsondata.trapip1+ '></input><button class="hfcbasesub" id="trap1sub">修改</button><br/>'+
						'<lable>Trap2IP : </lable></td><td><input id = "trapip2" value='+jsondata.trapip2+ '></input><button class="hfcbasesub" id="trap2sub">修改</button><br/>'+
						'<lable>Trap3IP : </lable></td><td><input id = "trapip3" value='+jsondata.trapip3+ '></input><button class="hfcbasesub" id="trap3sub">修改</button>'+
					'</div>'+
					'</div>');
	   }	   
		document.getElementById('pg_dev').src = "http://localhost:8080/wen9000/css/images/Trans.jpg";
		if(jsondata.agccontrol=="1"){
			document.getElementById('hfcagccontrol').textContent = "当前状态:AGC";
		}else{
			document.getElementById('hfcagccontrol').textContent = "当前状态:MGC";
		}
		
   }

   
   function fun_cnudetail(jsondata) {
	   //console.log(jsondata);
	   var active;
	   var style;
	   hfcactive = false;
	   if(jsondata.active == "在线"){
		   active = "设备连接正常";
		   style = "color:#3ff83d";
	   }else{
		   active = "设备失去连接";
		   style = "color:red";
	   }
	   
		$("#content").empty();			          				          		
     	$("#content").append('<div id="devinfo"><h3 style="background-color:#ccc">终端设备信息</h3>'+
     	'<div style="float:left"><img src="http://localhost:8080/wen9000/css/images/WEC-3702I C4.jpg" style="width:100px;height:80px"/></div>'+
     	'<div id="cnusts" style="height:80px;width:200px;margin:10px 10px 1px 110px;'+style+'"><lable id="cnusts_l" style="font-size:30px;background-color:black;line-height:80px">'+active +'</lable></div>'+						
		'<br/><div id="configinfo"><ul>'+
			'<li><a href="#tabs-1">基本信息</a></li>'+
			'<li><a href="#tabs-2">配置信息</a></li>'+
			'<li><a href="#tabs-3">状态信息</a></li></ul>'+
			'<div id="tabs-1">'+
				'<table id="baseinfo"><tr><td><lable>mac :&nbsp &nbsp &nbsp &nbsp</lable><lable id="cnu_mac" style="margin-left:0px">'+ jsondata.mac+'</lable></td>'+		     	
		     	'<td><lable>设备类型: '+jsondata.devicetype+'</lable></td></tr>'+
		     	'<tr><td><lable>设备标识: </lable>&nbsp<input type="text" id="c_label" style="width:150px" value="'+jsondata.label+'"></input></td>'+
				'<td><lable>地址: </lable><input type="text" id="c_address" style="width:150px" value="'+jsondata.address+'"></input></td>'+
				'<td><lable>联系方式 : </lable><input type="text" id="c_contact" style="width:150px" value="'+jsondata.contact+'"></input></td></tr>'+
				'<tr><td><lable>电话: &nbsp &nbsp &nbsp &nbsp</lable><input type="text" style="width:150px" id="c_phone" value="'+jsondata.phone+'"></input></td></tr>'+
				'</table><button id="btn_cbsave" style="margin-left:300px">修改</button>'+
			'</div>'+
			'<div id="tabs-2">'+
				'<table id="optinfo"><tr><td><lable>模板名称 :</lable></td><td><lable id="proname">'+jsondata.profilename+'</lable></td>'+
				'<td><lable>授权状态 : </lable></td><td><select id="authorization_en"><option value="1">启用</option><option value="2">禁用</option></select></td>'+
				'<tr><td><lable>VLAN使能 : </lable></td><td><select id="vlan_en"><option value="1">启用</option><option value="2">禁用</option></select></td>'+
				'</tr>'+
				'<tr><td><lable>ETH1VLAN: </lable></td><td><input type="text" id="vlan0id" value='+jsondata.vlan0id+'></input></td>'+
				'<td><lable>ETH2VLAN: </lable></td><td><input type="text" id="vlan1id" value='+jsondata.vlan1id+'></input></td>'+
				'<td><lable>ETH3VLAN: </lable></td><td><input type="text" id="vlan2id" value= '+jsondata.vlan2id+'></input></td></tr>'+
				'<tr><td><lable>ETH4VLAN: </lable></td><td><input type="text" id="vlan3id" value='+jsondata.vlan3id+'></input></td></tr>'+
				
				'<tr><td><lable>下行限速使能 :</lable></td><td><select id="rxlimitsts"><option value="1">启用</option><option value="2">禁用</option></select></td>'+
				'<td><lable>下行全局限速 :</lable></td><td><input type="text" id="cpuportrxrate" value='+ jsondata.cpuportrxrate+'></input></td>'+
				'<td><lable>ETH1下行限速:</lable></td><td><input type="text" id="port0txrate" value='+jsondata.port0txrate+'></input></td></tr>'+
				'<tr><td><lable>ETH2下行限速:</lable></td><td><input type="text" id="port1txrate" value='+jsondata.port1txrate+'></input></td>'+
				'<td><lable>ETH3下行限速:</lable></td><td><input type="text" id="port2txrate" value='+jsondata.port2txrate+'></input></td>'+
				'<td><lable>ETH4下行限速:</lable></td><td><input type="text" id="port3txrate" value='+jsondata.port3txrate+'></input></td></tr>'+
				
				'<tr><td><lable>上行限速使能 :</lable></td><td><select id="txlimitsts"><option value="1">启用</option><option value="2">禁用</option></select></td>'+
				'<td><lable>上行全局限速 :</lable></td><td><input type="text" id="cpuporttxrate" value='+ jsondata.cpuporttxrate+'></input></td>'+
				'<td><lable>ETH1上行限速:</lable></td><td><input type="text" id="port0rxrate" value='+jsondata.port0rxrate+'></input></td></tr>'+
				'<tr><td><lable>ETH2上行限速:</lable></td><td><input type="text" id="port1rxrate" value='+jsondata.port1rxrate+'></input></td>'+
				'<td><lable>ETH3上行限速:</lable></td><td><input type="text" id="port2rxrate" value='+jsondata.port2rxrate+'></input></td>'+
				'<td><lable>ETH4上行限速:</lable></td><td><input type="text" id="port3rxrate" value='+jsondata.port3rxrate+'></input></td></tr>'+				
				'</table><br/>'+
				'<button id="btn_cnusync" style="margin-left:200px">刷新</button><button id="btn_cnusub" style="margin-left:60px">提交</button>'+			
			'</div>'+
			'<div id="tabs-3">'+
				'<table id="statusinfo"><tr><td><lable>上行链路:</lable></td><td><lable id="txinfo">'+jsondata.txinfo+'</lable></td></tr>'+
				'<tr><td><lable>下行链路:</lable></td><td><lable id="rxinfo">'+jsondata.rxinfo+'</lable></td></tr>'+
				'<tr><td><lable>端口1连接状态:</lable></td><td><img src="http://localhost:8080/wen9000/css/images/offline.png" id="p1sts"></img></td></tr>'+
				'<tr><td><lable>端口2连接状态:</lable></td><td><img src="http://localhost:8080/wen9000/css/images/offline.png" id="p2sts"></img></td></tr>'+
				'<tr><td><lable>端口3连接状态:</lable></td><td><img src="http://localhost:8080/wen9000/css/images/offline.png" id="p3sts"></img></td></tr>'+
				'<tr><td><lable>端口4连接状态:</lable></td><td><img src="http://localhost:8080/wen9000/css/images/offline.png" id="p4sts"></img></td></tr>'+
			'</div>'+
		'</div>'
		);
		
     	document.getElementById('authorization_en').value=jsondata.authorization;
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
			alert("头端IP与其它头端冲突！");
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
	   if(jsondata.active == "在线"){
		   active = "设备连接正常";
		   style = "color:#3ff83d";
	   }else{
		   active = "设备失去连接";
		   style = "color:red";
	   }
	   $("#content").empty();
	   	$("#content").append('<div id="devinfo"><h3 style="background-color:#ccc">头端设备信息</h3>'+
	   	'<div style="float:left"><img id="pg_dev" src="" style="width:200px;height:100px"/></div>'+
	   	'<div id="cbatsts" style="height:100px;width:200px;margin:10px 10px 1px 210px;'+style+'"><lable id="cbatsts_l" style="font-size:30px;background-color:black;line-height:100px">'+active +'</lable></div>'+
	   	'<h3 style="background-color:#ccc">基本信息</h3>'+
	   	'<table id="baseinfo"><tr><td><lable>mac : </lable></td><td><lable style="margin-left:0px" id = "mac">'+jsondata.mac+'</lable></td>'+
	   		'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp设备类型 : </lable></td><td><lable>'+jsondata.devicetype+'</lable></td>'+
	   		'<tr><td><lable>软件版本 : </lable></td><td><lable>'+jsondata.appver+'</lable></td></tr>'+
			'<tr><td><lable>设备标识 : </lable></td><td><input type="text" id="label" value="'+jsondata.label+'"></input></td>'+
			'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp地址 : </lable></td><td><input type="text" id="address" value="'+jsondata.address+'"></input></td></tr>'+			
			'<tr><td><lable>ip : </lable></td><td><input type="text" id="ip" value='+jsondata.ip+'></input></td>'+
			'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp子网掩码 : </lable></td><td><input type="text" id="netmask" value='+jsondata.netmask+'></input></td></tr>'+
			'<tr><td><lable>网关 : </lable></td><td><input type="text" id="gateway" value='+jsondata.gateway+'></input></td></tr>'+						
			'<tr><td><lable>TrapServer : </lable></td><td><input type="text" id="trapserver" value='+jsondata.trapserver+'></input></td>'+
			'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp端口号 : </lable></td><td><input type="text" id="trap_port" value='+jsondata.agentport+'></input></td></tr>'+
			'<tr><td><lable>管理VLAN使能 : </lable></td><td><select name="vlanen_e" id="vlanen_e">'+
							'<option value="1">启用</option>'+
							'<option value="2">禁用</option>'+
						'</select></td>'+
			'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp管理VLAN ID : </lable></td><td><input type="text" id="mvlanid" value='+jsondata.mvlanid+'></input></td></tr>'+
			'<tr><td><lable>DNS : </lable></td><td><input type="text" id="dns" value='+jsondata.dns+'></input></td>'+
			'<td><lable>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbspTELNET超时(s) : </lable></td><td><input type="text" id="telnet_timeout" value='+jsondata.telnet+'></input></td></tr>'+
			'</table></div><br/>'+
			'<div><hr/><button id="btn_sub" style="margin-left:60px">提交</button><button id="btn_sync" style="margin-left:140px">刷新</button>'+
			'<button id="btn_reboot" style="margin-left:140px">设备重启</button>'+
			'<button id="btn_reset" style="margin-left:140px">恢复出厂设置</button>'+
			'</div>');

			document.getElementById('vlanen_e').value = jsondata.mvlanenable;
			if(jsondata.devicetype == "WEC-3501I C22"){
				document.getElementById('pg_dev').src = "http://localhost:8080/wen9000/css/images/WEC-3501I C22.jpg";
			}else if(jsondata.devicetype == "WEC-3501I S220"){
				document.getElementById('pg_dev').src = "http://localhost:8080/wen9000/css/images/WEC-3501I S220.jpg";
			}else if(jsondata.devicetype == "WEC9720EK C22"){
				document.getElementById('pg_dev').src = "http://localhost:8080/wen9000/css/images/WEC-3501I C22.jpg";
			}else if(jsondata.devicetype == "WEC9720EK E31"){
				document.getElementById('pg_dev').src = "http://localhost:8080/wen9000/css/images/WEC-3501I C22.jpg";
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
