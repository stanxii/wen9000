(function($){
	$(function(){
		socket = io.connect('http://localhost:3000');
		
		socket.emit('opt.con_success',"con_success");
		socket.emit('opt.con_failed',"con_failed");
		
		socket.on('opt.con_failed',fun_Confailed);
		socket.on('opt.con_success',fun_Consuccess)
		initTable();
	});
	
	function fun_Confailed(data){
		if(data != ""){
			$.each(data, function(key, itemv) {  					
  				$('#f_Table').dataTable().fnAddData( [
			        itemv.mac,
			        itemv.active,
			        itemv.label ] );
  								
			 	});
		}
	}
	
	function fun_Consuccess(data){
		if(data != ""){
			$.each(data, function(key, itemv) {  					
  				$('#s_Table').dataTable().fnAddData( [
			        itemv.mac,
			        itemv.active,
			        itemv.label ] );
  								
			 	});
		}
	}

	function initTable(){
		$('#s_Table').dataTable( {	  			 		
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
					      { "sTitle": "标识" , "sClass": "center"}
						],
			
	    } );
	    
	    $('#f_Table').dataTable( {	  			 		
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
	        	if ( aData[2] == "1" )
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
					      { "sTitle": "标识" , "sClass": "center"}
						],
			
	    } );
	}
})(jQuery);