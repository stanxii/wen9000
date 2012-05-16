(function($){
	var pTable;
	$(function(){
		socket = io.connect('http://localhost:3000');
		
		socket.emit('historyalarm_all',"historyalarm_all");
		
		socket.on('historyalarm_all',fun_AllHistoryAlarms);
		

	function fun_AllHistoryAlarms(data){
		var groupval=[];
		$.each(data, function(key, itemv) {  					
				var item = [itemv.id,itemv.proname,itemv.vlanen,itemv.vlan0id,itemv.vlan1id,itemv.vlan2id,itemv.vlan3id,
				itemv.rxlimitsts,itemv.cpuportrxrate,itemv.port0txrate,itemv.port1txrate,itemv.port2txrate,itemv.port3txrate,
				itemv.txlimitsts,itemv.cpuporttxrate,itemv.port0rxrate];
				groupval[groupval.length] = item; 				
								
		 	}); 
		 	
		 	pTable = $('#historyAlarm').dataTable( {
			"bFilter": false,						//不使用过滤功能
			"bLengthChange": false,					//用户不可改变每页显示数量
			"iDisplayLength": 10,					//每页显示10条数据
			"aaData": groupval,
    		"bInfo": false,	
    		"sScrollX": "100%",
	        "sScrollXInner": "110%",
	        "bScrollCollapse": true,
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
	            	$('td:eq(2)', nRow).html( '启用' );				               
	            }else{
	            	$('td:eq(2)', nRow).html( '禁用' );
	            }      
	            if ( aData[8] == "1" )
	            {
	            	$('td:eq(8)', nRow).html( '启用' );				               
	            }else{
	            	$('td:eq(8)', nRow).html( '禁用' );
	            } 
	            if ( aData[14] == "1" )
	            {
	            	$('td:eq(14)', nRow).html( '启用' );				               
	            }else{
	            	$('td:eq(14)', nRow).html( '禁用' );
	            }     
	            
	        },		
			"aoColumns": [							//设定各列宽度
						  { "sTitle": "ID" },
						  { "sTitle": "模板名称" },
					      { "sTitle": "vlan使能" },
					      { "sTitle": "1端口vlan" },
					      { "sTitle": "2端口vlan" },
						  { "sTitle": "3端口vlan" },
					      { "sTitle": "4端口vlan" },
					      { "sTitle": "下行限速使能" },
					      { "sTitle": "下行全局限速" },
					      { "sTitle": "1端口限速" },
						  { "sTitle": "2端口限速" },
					      { "sTitle": "3端口限速" },
					      { "sTitle": "4端口限速" },
					      { "sTitle": "上行限速使能" },
					      { "sTitle": "上行全局限速" }
						],
			
	    } );
	}
  });
})(jQuery);
