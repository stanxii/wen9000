(function($){
	var proc = 0;
	var total = 0;
	$(function(){
		socket = io.connect('http://192.168.1.249:3000');
		
		socket.emit('dis.searchtotal',"searchtotal");
		socket.on('dis.searchtotal',fun_SearchTotal);
		socket.on('dis.proc',fun_Proc);
		socket.on('dis.findcbat',fun_FindCbat);
		//弹出进度窗口
		$( "#dialog:ui-dialog" ).dialog( "destroy" );
		$( "#dialog-dis-proc" ).dialog({
			show: "blind",
			modal: true,
			resizable: false,
			hide: "explode"
		});
		$("#dialog-dis-proc").dialog("open");
		
		//table
		$('#list').dataTable( {	  			 		
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
    		"fnRowCallback": function( nRow, aData, iDisplayIndex ) {
	        	if ( aData[1] == "1" )
	            {
	            	$('td:eq(1)', nRow).html( '在线' );				               
	            }else{
	            	$('td:eq(1)', nRow).html( '下线' );
	            }     
	            
	        },		
	        "aaSorting": [[ 0, "asc" ]],
			"aoColumns": [	
						  { "sTitle": "MAC" , "sClass": "center"},
						  { "sTitle": "状态" , "sClass": "center"},
						  { "sTitle": "IP" , "sClass": "center"},
						  { "sTitle": "设备类型" , "sClass": "center"}
						],
			
	    } );
	});
	
	function fun_Proc(data){
		//搜索进度监控器
		proc++;
		$("#dis_proc")[0].textContent= proc + "/"+total;
		if(proc == total){
			$("#dialog-dis-proc").dialog("close");
		}
	}
	
	function fun_SearchTotal(data){
		total = data.total;
		proc = data.proc;
	}
	
	function fun_FindCbat(data){
		$('#list').dataTable().fnAddData( [
	        data.mac,
	        data.active,
	        data.ip,
	        data.devtype] );
	}
})(jQuery);
