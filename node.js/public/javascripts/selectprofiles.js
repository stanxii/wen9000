(function($){
	$(function(){
		socket = io.connect('http://localhost:3000');
		
		socket.emit('profile_all', 'profile_all' );
		socket.on('profileALL', fun_Allprofiles );
		socket.on('profileGet', fun_Getprofile );
		socket.on('opt.selectedpro', fun_Selprofile );
		
		$("#btn_pre").click(function(){
 			window.location.href="/opt/accounts";
 		});
		
		$("#combox_profiles").change(function(){
			var proid = $(this)[0].options[$(this)[0].options.selectedIndex].value;
			socket.emit('profile_get',proid);
		});
		
		$("#btn_next").live("click",function(){
			socket.emit('opt.selectedpro',"selectedpro");
 			
 		});
	});
	
	function fun_Selprofile(data){
		if(data != ""){
			window.location.href="/opt/confirm";
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
	
	function fun_Getprofile(tmpdata){
		if(tmpdata.vlanen=="1"){
			tmpdata.vlanen = "启用";
		}else{
			tmpdata.vlanen = "禁用";
		}
		if(tmpdata.rxlimitsts=="1"){
			tmpdata.rxlimitsts = "启用";
		}else{
			tmpdata.rxlimitsts = "禁用";
		}
		if(tmpdata.txlimitsts=="1"){
			tmpdata.txlimitsts = "启用";
		}else{
			tmpdata.txlimitsts = "禁用";
		}
		if(tmpdata.authorization=="1"){
			tmpdata.authorization = "启用";
		}else{
			tmpdata.authorization = "禁用";
		}
		$("#profile_info").empty();	
		$("#profile_info").append('<br/><br/><h3 style="color:green">配置信息</h3><hr/><div id="configinfo"><ul>'+
			'<li><a href="#tabs-1">基本配置</a></li>'+
			'<li><a href="#tabs-2">下行配置</a></li>'+
			'<li><a href="#tabs-3">上行配置</a></li></ul>'+
			'<div id="tabs-1">'+
				'<table id="optinfo"><tr><td><lable>模板名称 :&nbsp &nbsp &nbsp'+tmpdata.proname+'</lable></td>'+
				'<td><lable>授权状态 : &nbsp &nbsp &nbsp  '+ tmpdata.authorization+'</lable></td></tr>'+
				'<tr><td><lable>VLAN使能 : &nbsp &nbsp &nbsp  '+ tmpdata.vlanen+'</lable></td></tr>'+
				'<tr>'+
				'<td><lable>ETH1VLAN: &nbsp &nbsp &nbsp  '+tmpdata.vlan0id+'</lable></td>'+
				'<td><lable>ETH2VLAN: &nbsp &nbsp &nbsp  '+tmpdata.vlan1id+'</lable></td></tr>'+
				'<tr><td><lable>ETH3VLAN: &nbsp &nbsp &nbsp  '+tmpdata.vlan2id+'</lable></td>'+
				'<td><lable>ETH4VLAN: &nbsp &nbsp &nbsp  '+tmpdata.vlan3id+'</lable></td></tr></table>'+
			'</div>'+
			'<div id="tabs-2">'+
				'<table id="optinfo"><tr><td><lable>下行限速使能 :&nbsp &nbsp &nbsp'+tmpdata.rxlimitsts+'</lable></td>'+
				'<td><lable>下行全局限速 : &nbsp &nbsp &nbsp  '+ tmpdata.cpuportrxrate+'</lable></td></tr>'+
				'<tr><td><lable>ETH1下行限速: &nbsp &nbsp &nbsp  '+tmpdata.port0txrate+'</lable></td>'+
				'<td><lable>ETH2下行限速: &nbsp &nbsp &nbsp  '+tmpdata.port1txrate+'</lable></td>'+
				'<tr><td><lable>ETH3下行限速: &nbsp &nbsp &nbsp  '+tmpdata.port2txrate+'</lable></td>'+
				'<td><lable>ETH4下行限速: &nbsp &nbsp &nbsp  '+tmpdata.port3txrate+'</lable></td>'+
				'</table>'+
			'</div>'+
			'<div id="tabs-3">'+
				'<table id="optinfo"><tr><td><lable>上行限速使能 :&nbsp &nbsp &nbsp'+tmpdata.txlimitsts+'</lable></td>'+
				'<td><lable>上行全局限速 :&nbsp &nbsp &nbsp  '+ tmpdata.cpuporttxrate+'</lable></td></tr>'+
				'<tr><td><lable>ETH1上行限速:&nbsp &nbsp &nbsp   '+tmpdata.port0rxrate+'</lable></td>'+
				'<td><lable>ETH2上行限速: &nbsp &nbsp &nbsp  '+tmpdata.port1rxrate+'</lable></td>'+
				'<tr><td><lable>ETH3上行限速: &nbsp &nbsp &nbsp  '+tmpdata.port2rxrate+'</lable></td>'+
				'<td><lable>ETH4上行限速:  &nbsp &nbsp &nbsp '+tmpdata.port3rxrate+'</lable></td>'+
				'</table>'+
			'</div></div>'+
			'<br/><br/><button id="btn_next" style="float:right">下一步</button>');
	}
	
	function fun_Allprofiles(data){
		var objSelect = $("#combox_profiles");
		objSelect.attr("sort","true");
		$.each(data, function(key, itemv) {  					
				var item = itemv.proname;
				var varItem = new Option(item, itemv.id);      
			objSelect[0].options.add(varItem);  							
		 	}); 
		sortOption();
		var proid = objSelect[0].options[objSelect[0].options.selectedIndex].value;
		socket.emit('profile_get',proid);
		
	}
	
	function sortRule(a,b) {
		 var x = a._value;
		 var y = b._value;
		 return x.localeCompare(y);
		}
		function op(){
		 var _value;
		 var _text;
		}
		function sortOption(){
		 var obj = document.getElementById("combox_profiles");
		 var tmp = new Array();
		 for(var i=0;i<obj.options.length;i++){
		  var ops = new op();
		  ops._value = obj.options[i].value;
		  ops._text = obj.options[i].text;
		  tmp.push(ops);
		 }
		 tmp.sort(sortRule);
		 for(var j=0;j<tmp.length;j++){
		  obj.options[j].value = tmp[j]._value;
		  obj.options[j].text = tmp[j]._text;
		 }
	}
})(jQuery);
