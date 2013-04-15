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
				"sLengthMenu": "Display _MENU_ strip record per page",
				"sZeroRecords": "No data was discoveryed",
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
					      { "sTitle": "Label" , "sClass": "center"}
						],
			
	    } );
	    
	    $('#f_Table').dataTable( {	  			 		
			"bFilter": false,						//不使用过滤功能
			"bLengthChange": false,					//用户不可改变每页显示数量
			"iDisplayLength": 5,					//每页显示5条数据
    		"bInfo": false,	
	        "sPaginationType": "full_numbers",				        
	        "oLanguage": {							//汉化
				"sLengthMenu": "Display _MENU_ strip record per page",
				"sZeroRecords": "No data was discoveryed",
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
	        	if ( aData[2] == "1" )
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
					      { "sTitle": "Label" , "sClass": "center"}
						],
			
	    } );
	}
})(jQuery);
