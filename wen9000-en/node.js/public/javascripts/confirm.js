(function($){
	var count;
	$(function(){
		var user = localStorage.getItem('username');
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
			
			socket.emit('opt.send_config',user);
 			
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
			tmpdata.vlanen = "enable";
		}else{
			tmpdata.vlanen = "disable";
		}
		if(tmpdata.rxlimitsts=="1"){
			tmpdata.rxlimitsts = "enable";
		}else{
			tmpdata.rxlimitsts = "disable";
		}
		if(tmpdata.txlimitsts=="1"){
			tmpdata.txlimitsts = "enable";
		}else{
			tmpdata.txlimitsts = "disable";
		}
		if(tmpdata.authorization=="1"){
			tmpdata.authorization = "enable";
		}else{
			tmpdata.authorization = "disable";
		}	
		$("#profile_info").empty();	
		$("#profile_info").append('<h3>Config information</h3><div id="configinfo"><ul>'+
				'<li><a href="#tabs-1">Basic configuration</a></li>'+
				'<li><a href="#tabs-2">Downstream configuration</a></li>'+
				'<li><a href="#tabs-3">Upstream configuration</a></li></ul>'+
				'<div id="tabs-1">'+
					'<table id="optinfo"><tr><td><lable>Template name:&nbsp &nbsp &nbsp'+tmpdata.proname+'</lable></td>'+
					'<td><lable>Authorization: &nbsp &nbsp &nbsp  '+ tmpdata.authorization+'</lable></td></tr>'+
					'<tr><td><lable>VLAN_En: &nbsp &nbsp &nbsp  '+ tmpdata.vlanen+'</lable></td></tr>'+
					'<tr>'+
					'<tr><td><lable>ETH1 VLAN: &nbsp &nbsp &nbsp  '+tmpdata.vlan0id+'</lable></td>'+
					'<td><lable>ETH2 VLAN: &nbsp &nbsp &nbsp  '+tmpdata.vlan1id+'</lable></td>'+
					'<tr><td><lable>ETH3 VLAN: &nbsp &nbsp &nbsp  '+tmpdata.vlan2id+'</lable></td>'+
					'<td><lable>ETH4 VLAN: &nbsp &nbsp &nbsp  '+tmpdata.vlan3id+'</lable></td></tr></table>'+
				'</div>'+
				'<div id="tabs-2">'+
					'<table id="optinfo"><tr><td><lable>Down rate-limit en :&nbsp &nbsp &nbsp'+tmpdata.rxlimitsts+'</lable></td>'+
					'<td><lable>Down global rate-limit : &nbsp &nbsp &nbsp  '+ tmpdata.cpuportrxrate+'</lable></td></tr>'+
					'<tr><td><lable>ETH1 down rate-limit: &nbsp &nbsp &nbsp  '+tmpdata.port0txrate+'</lable></td>'+
					'<td><lable>ETH2 down rate-limit: &nbsp &nbsp &nbsp  '+tmpdata.port1txrate+'</lable></td>'+
					'<tr><td><lable>ETH3 down rate-limit: &nbsp &nbsp &nbsp  '+tmpdata.port2txrate+'</lable></td>'+
					'<td><lable>ETH4 down rate-limit: &nbsp &nbsp &nbsp  '+tmpdata.port3txrate+'</lable></td>'+
					'</table>'+
				'</div>'+
				'<div id="tabs-3">'+
					'<table id="optinfo"><tr><td><lable>Up rate-limit en :&nbsp &nbsp &nbsp'+tmpdata.txlimitsts+'</lable></td>'+
					'<td><lable>Up global rate-limit :&nbsp &nbsp &nbsp  '+ tmpdata.cpuporttxrate+'</lable></td></tr>'+
					'<tr><td><lable>ETH1 up rate-limit:&nbsp &nbsp &nbsp   '+tmpdata.port0rxrate+'</lable></td>'+
					'<td><lable>ETH2 up rate-limit: &nbsp &nbsp &nbsp  '+tmpdata.port1rxrate+'</lable></td>'+
					'<tr><td><lable>ETH3 up rate-limit: &nbsp &nbsp &nbsp  '+tmpdata.port2rxrate+'</lable></td>'+
					'<td><lable>ETH4 up rate-limit:  &nbsp &nbsp &nbsp '+tmpdata.port3rxrate+'</lable></td>'+
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
				"sLengthMenu": "Display _MENU_ strip record per page",
				"sZeroRecords": "No data was discoveryed",
				"sInfo": "Current data is from _START_ to _END_ strip data; has _TOTAL_ strip record in all",
				"sInfoEmtpy": "No data",
				"sProcessing": "Data Loading...",
				"oPaginate": {
					"sFirst": "Home",
					"sPrevious": "Previous",
					"sNext": "Next",
					"sLast": "End"
				}
			},
    		"fnRowCallback": function( nRow, aData, iDisplayIndex ) {
	        	if ( aData[1] == "1" )
	            {
	            	$('td:eq(1)', nRow).html( 'Online' );				               
	            }else{
	            	$('td:eq(1)', nRow).html( 'Offline' );
	            }     
	            
	        },		
			"aoColumns": [
						  { "sTitle": "MAC" , "sClass": "center"},
						  { "sTitle": "Status" , "sClass": "center"},
					      { "sTitle": "Identification" , "sClass": "center"},
					      { "sTitle": "Device type" , "sClass": "center"}
						],
			
	    } );
	}
})(jQuery);
