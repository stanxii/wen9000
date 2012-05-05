(function($) {
	var socket;
	var isbusy = false;
  $(function() {
  
	  socket = io.connect('http://localhost:3000');
                  socket.emit('initDynatree', 'init tree' );

                  socket.on('initDynatree', onInitTree);
                  socket.on('cbatdetail', fun_cbatdetail );
                  socket.on('cnudetail', fun_cnudetail );
                  socket.on('cbat_modify', fun_cbatmodify );
          function onInitTree(treedata) {
                console.log(treedata);
                initTree(treedata);
           }
          
          
  });



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
  			 		  			 			 	  			 	
  			 	//initAjax: { 
			    //type: "GET",
			    //dataType: "json",
		            //url: "http://192.168.1.250:8080/wen9000/global/eocs"
	
  			    //	},  			 	

                        children: treedata,
		        imagePath: "http://localhost:8080/wen9000/css/images/",
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
		       // onDblClick: function(node, event) {
			   //     node.toggleExpand();
			   // },		                
		        onActivate: function(node) {
		        			        	       	
			    },
			    // onLazyRead: function(node){
			    // 	node.appendAjax({
			    // 		url: "/wen9000/global/cnu_read/"+node.data.mac			     		
			    // 	});
			    // }
		    }); 	  		

   }
   
   function fun_cnudetail(jsondata) {
	   console.log(jsondata);
		$("#content").empty();			          				          		
     	$("#content").append('<div id="devinfo"><h3 style="color:green">终端设备信息</h3>'+
     	'<div style="float:left"><img src="http://localhost:8080/wen9000/css/images/Trans.jpg" style="width:200px;height:80px"/></div>'+
		'<div style="height:80px;width:auto;margin:10px 0px 1px 210px"><lable>这里描述设备的功能信息</lable></div>'+							
		'<div id="configinfo"><ul>'+
			'<li><a href="#tabs-1">基本信息</a></li>'+
			'<li><a href="#tabs-2">配置信息</a></li></ul>'+
			'<div id="tabs-1">'+
				'<table id="baseinfo"><tr><td><lable>mac :&nbsp &nbsp &nbsp &nbsp</lable><lable id="cnu_mac" style="margin-left:0px">'+ jsondata.mac+'</lable></td>'+
		     	'<td><lable>状态 :'+ jsondata.active+'</lable></td>'+
		     	'<td><lable>设备类型: '+"待定"+'</lable></td></tr>'+
		     	'<tr><td><lable>设备标识: </lable><input type="text" id="c_label" value='+jsondata.label+'></input></td>'+
				'<td><lable>地址: </lable><input type="text" id="c_address" value='+jsondata.address+'></input></td>'+
				'<td><lable>联系方式 : </lable><input type="text" id="c_contact" value='+jsondata.contact+'></input></td></tr>'+
				'<tr><td><lable>电话: &nbsp &nbsp &nbsp &nbsp</lable><input type="text" id="c_phone" value='+jsondata.phone+'></input></td></tr>'+
				'</table><button id="btn_cbsave" style="margin-left:60px">修改</button>'+
			'</div>'+
			'<div id="tabs-2">'+
				'<table id="optinfo"><tr><td><lable>模板名称 :&nbsp &nbsp &nbsp'+jsondata.profilename+'</lable></td>'+
				'<td><lable>VLAN使能 : </lable>&nbsp &nbsp &nbsp  <lable id="vlan_en" style="margin-left:0px">'+ jsondata.vlanen+'</lable></td>'+
				'<td><lable>VLAN ID: &nbsp &nbsp &nbsp  '+jsondata.vlanid+'</lable></td></tr>'+
				'<tr><td><lable>1端口VLAN: &nbsp &nbsp &nbsp  '+jsondata.vlan0id+'</lable></td>'+
				'<td><lable>2端口VLAN: &nbsp &nbsp &nbsp  '+jsondata.vlan1id+'</lable></td>'+
				'<td><lable>3端口VLAN: &nbsp &nbsp &nbsp  '+jsondata.vlan2id+'</lable></td></tr>'+
				'<tr><td><lable>4端口VLAN: &nbsp &nbsp &nbsp  '+jsondata.vlan3id+'</lable></td></tr>'+
				
				'<tr><td><lable>下行限速使能 :</lable>&nbsp &nbsp &nbsp<lable id="rxlimitsts" style="margin-left:0px">'+jsondata.rxlimitsts+'</lable></td>'+
				'<td><lable>下行全局限速 : &nbsp &nbsp &nbsp  '+ jsondata.cpuportrxrate+'</lable></td>'+
				'<td><lable>1端口下行限速: &nbsp &nbsp &nbsp  '+jsondata.port0txrate+'</lable></td></tr>'+
				'<tr><td><lable>2端口下行限速: &nbsp &nbsp &nbsp  '+jsondata.port1txrate+'</lable></td>'+
				'<td><lable>3端口下行限速: &nbsp &nbsp &nbsp  '+jsondata.port2txrate+'</lable></td>'+
				'<td><lable>4端口下行限速: &nbsp &nbsp &nbsp  '+jsondata.port3txrate+'</lable></td></tr>'+
				
				'<tr><td><lable>上行限速使能 :</lable>&nbsp &nbsp &nbsp<lable id="txlimitsts" style="margin-left:0px">'+jsondata.txlimitsts+'</lable></td>'+
				'<td><lable>上行全局限速 :&nbsp &nbsp &nbsp  '+ jsondata.cpuporttxrate+'</lable></td>'+
				'<td><lable>1端口上行限速:&nbsp &nbsp &nbsp   '+jsondata.port0rxrate+'</lable></td></tr>'+
				'<tr><td><lable>2端口上行限速: &nbsp &nbsp &nbsp  '+jsondata.port1rxrate+'</lable></td></tr>'+
				'<td><lable>3端口上行限速: &nbsp &nbsp &nbsp  '+jsondata.port2rxrate+'</lable></td>'+
				'<td><lable>4端口上行限速:  &nbsp &nbsp &nbsp '+jsondata.port3rxrate+'</lable></td></tr>'+
				'</table>'+
			'</div>'+
		'</div>'
		);
		
		if(document.getElementById('vlan_en').textContent== "2"){
			document.getElementById('vlan_en').textContent = "禁用";
		}else{
			document.getElementById('vlan_en').textContent = "启用";
		}
		if(document.getElementById('rxlimitsts').textContent== "2"){
			document.getElementById('rxlimitsts').textContent = "禁用";
		}else{
			document.getElementById('rxlimitsts').textContent = "启用";
		}
		if(document.getElementById('txlimitsts').textContent== "2"){
			document.getElementById('txlimitsts').textContent = "禁用";
		}else{
			document.getElementById('txlimitsts').textContent = "启用";
		}
		
   }
   
   $("#configinfo").livequery( function(){
	  		$(this).tabs();
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
		
		socket.emit('cbat_modify', datastring );
    	
   }); 	
   
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
	   $("#content").empty();
	   	$("#content").append('<div id="devinfo"><h3 style="color:green">头端设备信息</h3>'+
	   	'<div style="float:left"><img src="http://localhost:8080/wen9000/css/images/Trans.jpg" style="width:200px;height:100px"/></div>'+
	   	'<div style="height:100px;width:auto;margin:10px 0px 1px 210px"><lable></lable></div>'+
	   	'<h3 style="color:green">基本信息</h3>'+
	   	'<table id="baseinfo"><tr><td><lable>mac : </lable><lable style="margin-left:0px" id = "mac">'+jsondata.mac+'</lable></td>'+
			'<td><lable>状态 : '+jsondata.active+'</lable></td></tr>'+
			'<tr><td><lable>ip : </lable><input type="text" id="ip" value='+jsondata.ip+'></input></td>'+
			'<td><lable>子网掩码 : </lable><input type="text" id="netmask" value='+jsondata.netmask+'></input></td></tr>'+
			'<tr><td><lable>网关 : </lable><input type="text" id="gateway" value='+jsondata.gateway+'></input></td>'+
			'<td><lable>设备标识 : </lable><input type="text" id="label" value='+jsondata.label+'></input></td></tr>'+
			'<tr><td><lable>设备类型 : '+jsondata.devicetype+'</lable></td>'+
			'<td><lable>地址 : </lable><input type="text" id="address" value='+jsondata.address+'></input></td></tr>'+
			'<tr><td><lable>内核版本 : '+jsondata.bootver+'</lable></td>'+
			'<td><lable>软件版本 : '+jsondata.appver+'</lable></td></tr>'+
			'<tr><td><lable>TrapServer : </lable><input type="text" id="trapserver" value='+jsondata.trapserver+'></input></td>'+
			'<td><lable>端口号 : </lable><input type="text" id="trap_port" value='+jsondata.agentport+'></input></td></tr>'+
			'<tr><td><lable>管理VLAN使能 : </lable><select name="vlanen_e" id="vlanen_e">'+
							'<option value="1">启动</option>'+
							'<option value="2">禁用</option>'+
						'</select></td>'+
			'<td><lable>管理VLAN ID : </lable><input type="text" id="mvlanid" value='+jsondata.mvlanid+'></input></td></tr>'+
			'</table></div><br/>'+
			'<div><hr/><button id="btn_sub" style="margin-left:60px">提交</button><button id="btn_sync" style="margin-left:190px">同步数据</button></div>');
			
			document.getElementById('vlanen_e').value = jsondata.mvlanenable;
   }
})(jQuery);
