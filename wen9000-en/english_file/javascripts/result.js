(function($){
	var proc = 0;
	var total = 0;
	$(function(){
		socket = io.connect('http://localhost:3000');
		
		socket.emit('dis.searchtotal',"searchtotal");

		socket.on('dis.searchtotal',fun_SearchTotal);
		socket.on('dis.proc',fun_Proc);
		socket.on('dis.findcbat',fun_FindCbat);		
		
		//table
		$('#list').dataTable( {	  			 		
			"bFilter": false,						//不使用过滤功能
			"bLengthChange": false,					//用户不可改变每页显示数量
			"iDisplayLength": 5,					//每页显示5条数据
    		"bInfo": false,	
	        "sPaginationType": "full_numbers",				        
	        "oLanguage": {							//汉化
				"sLengthMenu": "Display _MENU_ strip record per page",
				"sZeroRecords": "No data has been searched",
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
    		"fnRowCallback": function( nRow, aData, iDisplayIndex ) {
	        	if ( aData[1] == "1" )
	            {
	            	$('td:eq(1)', nRow).html( 'Online' );				               
	            }else{
	            	$('td:eq(1)', nRow).html( 'Offline' );
	            }     
	            
	        },		
	        "aaSorting": [[ 0, "asc" ]],
			"aoColumns": [	
						  { "sTitle": "MAC" , "sClass": "center"},
						  { "sTitle": "Status" , "sClass": "center"},
						  { "sTitle": "IP" , "sClass": "center"},
						  { "sTitle": "Device type" , "sClass": "center"}
						],
			
	    } );
	});
	
	function fun_FindCbat(data){
		$('#list').dataTable().fnAddData( [
	        data.mac,
	        data.active,
	        data.ip,
	        data.devicetype] );
	}
	
	function fun_Proc(data){
		//搜索进度监控器
		proc++;
		$("#dis_proc")[0].textContent= proc + "/"+total;
		if(proc == total){
			$("#dialog-dis-proc").dialog("close");
			$("#dialog-dis-proc img").css("display","none");
			$("#dis_proc")[0].textContent="Complete search!!!";
		}
	}
	
	function fun_SearchTotal(data){
		total = data.total;
		proc = data.proc;
		if(proc == total){
			$("#dialog-dis-proc img").css("display","none");
			$("#dis_proc")[0].textContent="Complete search!!!";
		}
		//弹出进度窗口
		$( "#dialog:ui-dialog" ).dialog( "destroy" );
		$( "#dialog-dis-proc" ).dialog({
			show: "blind",
			modal: true,
			resizable: false,
			hide: "explode"
		});
		$("#dialog-dis-proc").dialog("open");
	}	
	
})(jQuery);
