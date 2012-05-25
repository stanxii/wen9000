(function($){
	var count;
	$(function(){
		socket = io.connect('http://localhost:3000');
		
		socket.emit('opt.allcheckedcnus', "allcheckedcnus" );
		socket.emit('opt.selectedpro',"selectedpro");
		
		socket.on('opt.allcheckedcnus', fun_Allcheckedcnus );
		socket.on('opt.selectedpro', fun_Selprofile );
		socket.on('opt.sendconfig', fun_Sendconfig );
		socket.on('opt.p_proc', fun_Proc );
		
		$("#btn_pre").click(function(){
 			window.history.back(-1);
 		});
		
		//下发配置
 		$("#btn_ok").click(function(){
 			$("#p_proc")[0].textContent = "0/"+count;
 			$( "#dialog:ui-dialog" ).dialog( "destroy" );
	
			$( "#dialog-confirm-warm" ).dialog({
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
			$("#dialog-confirm-warm").dialog("open");
			
			socket.emit('opt.send_config',"send_config");
 			
 		});
	});
	
	function fun_Proc(data){
		$("#p_proc")[0].textContent = data;
	}
	
	function fun_Sendconfig(data){
		window.location.href="/opt/config_results";
	}
	
	function fun_Selprofile(tmpdata){
		if(tmpdata.vlanen=="1"){
			tmpdata.vlanen = "启动";
		}else{
			tmpdata.vlanen = "禁止";
		}
		if(tmpdata.rxlimitsts=="1"){
			tmpdata.rxlimitsts = "启动";
		}else{
			tmpdata.rxlimitsts = "禁止";
		}
		if(tmpdata.txlimitsts=="1"){
			tmpdata.txlimitsts = "启动";
		}else{
			tmpdata.txlimitsts = "禁止";
		}
			
		$("#profile_info").empty();	
		$("#profile_info").append('<h3>配置信息</h3><div id="configinfo"><ul>'+
				'<li><a href="#tabs-1">基本配置</a></li>'+
				'<li><a href="#tabs-2">下行配置</a></li>'+
				'<li><a href="#tabs-3">上行配置</a></li></ul>'+
				'<div id="tabs-1">'+
					'<table id="optinfo"><tr><td><lable>模板名称 :&nbsp &nbsp &nbsp'+tmpdata.proname+'</lable></td>'+
					'<td><lable>VLAN使能 : &nbsp &nbsp &nbsp  '+ tmpdata.vlanen+'</lable></td>'+
					'</tr>'+
					'<tr><td><lable>1端口VLAN: &nbsp &nbsp &nbsp  '+tmpdata.vlan0id+'</lable></td>'+
					'<td><lable>2端口VLAN: &nbsp &nbsp &nbsp  '+tmpdata.vlan1id+'</lable></td>'+
					'<tr><td><lable>3端口VLAN: &nbsp &nbsp &nbsp  '+tmpdata.vlan2id+'</lable></td>'+
					'<td><lable>4端口VLAN: &nbsp &nbsp &nbsp  '+tmpdata.vlan3id+'</lable></td></tr></table>'+
				'</div>'+
				'<div id="tabs-2">'+
					'<table id="optinfo"><tr><td><lable>下行限速使能 :&nbsp &nbsp &nbsp'+tmpdata.rxlimitsts+'</lable></td>'+
					'<td><lable>下行全局限速 : &nbsp &nbsp &nbsp  '+ tmpdata.cpuportrxrate+'</lable></td></tr>'+
					'<tr><td><lable>1端口下行限速: &nbsp &nbsp &nbsp  '+tmpdata.port0txrate+'</lable></td>'+
					'<td><lable>2端口下行限速: &nbsp &nbsp &nbsp  '+tmpdata.port1txrate+'</lable></td>'+
					'<tr><td><lable>3端口下行限速: &nbsp &nbsp &nbsp  '+tmpdata.port2txrate+'</lable></td>'+
					'<td><lable>4端口下行限速: &nbsp &nbsp &nbsp  '+tmpdata.port3txrate+'</lable></td>'+
					'</table>'+
				'</div>'+
				'<div id="tabs-3">'+
					'<table id="optinfo"><tr><td><lable>上行限速使能 :&nbsp &nbsp &nbsp'+tmpdata.txlimitsts+'</lable></td>'+
					'<td><lable>上行全局限速 :&nbsp &nbsp &nbsp  '+ tmpdata.cpuporttxrate+'</lable></td></tr>'+
					'<tr><td><lable>1端口上行限速:&nbsp &nbsp &nbsp   '+tmpdata.port0rxrate+'</lable></td>'+
					'<td><lable>2端口上行限速: &nbsp &nbsp &nbsp  '+tmpdata.port1rxrate+'</lable></td>'+
					'<tr><td><lable>3端口上行限速: &nbsp &nbsp &nbsp  '+tmpdata.port2rxrate+'</lable></td>'+
					'<td><lable>4端口上行限速:  &nbsp &nbsp &nbsp '+tmpdata.port3rxrate+'</lable></td>'+
					'</table>'+
				'</div></div>');
	}
	
	function fun_Allcheckedcnus(data){
		var groupval=[];
		$.each(data, function(key, itemv) {  					
				var item = [itemv.mac,itemv.active,itemv.label,itemv.devicetype];
				groupval[groupval.length] = item; 				
								
		 	}); 
		 	count = groupval.length;
		 	$('#conTable').dataTable( {
			"bFilter": false,		//不使用过滤功能
			"bStateSave": true,				
			"bLengthChange": false,					//用户不可改变每页显示数量
			"iDisplayLength": 5,					//每页显示5条数据
			"aaData": groupval,
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
    		"fnRowCallback": function( nRow, aData, iDisplayIndex ) {
	        	if ( aData[1] == "1" )
	            {
	            	$('td:eq(1)', nRow).html( '在线' );				               
	            }else{
	            	$('td:eq(1)', nRow).html( '离线' );
	            }     
	            
	        },		
			"aoColumns": [
						  { "sTitle": "MAC" , "sClass": "center"},
						  { "sTitle": "状态" , "sClass": "center"},
					      { "sTitle": "标识" , "sClass": "center"},
					      { "sTitle": "设备类型" , "sClass": "center"}
						],
			
	    } );
	}
})(jQuery);
