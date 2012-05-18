(function($){
	$(function(){
		socket = io.connect('http://192.168.1.249:3000');
		
		socket.emit('opt.cnus', 'cnus' );
		
		socket.on('opt.allcnus', fun_Allcnus );
		socket.on('opt.allcheckedcnus', fun_Allcheckedcnus );
		
		$('.chk').live('click', function () {
	        var checkbox = $(this);
	        var mac = checkbox[0].parentElement.parentElement.cells[1].textContent;
	        var data = '{"cnumac":"'+mac+'","value":"'+checkbox[0].checked+'"}';
	        socket.emit('opt.checkedcnus', data );
	    } );
		
		//全选按钮点击事件
   		$("#checkall").click(function(){
   			var c_box = $(this);
   			var mac;
   			if(c_box[0].checked){
   				for(var i=1;i<cTable[0].rows.length;i++){
       				if(cTable[0].rows[i].firstChild.firstChild.checked){
       					continue;
       				}
       				cTable[0].rows[i].firstChild.firstChild.checked = true;
       				mac = cTable[0].rows[i].cells[1].textContent;
       				var data = '{"cnumac":"'+mac+'","value":"true"}';
       		        socket.emit('opt.checkedcnus', data );
       			}
   			}else{
   				for(var i=1;i<cTable[0].rows.length;i++){
       				if(!cTable[0].rows[i].firstChild.firstChild.checked){
       					continue;
       				}
       				cTable[0].rows[i].firstChild.firstChild.checked = false;
       				mac = cTable[0].rows[i].cells[1].textContent;
       				var data = '{"cnumac":"'+mac+'","value":"false"}';
       		        socket.emit('opt.checkedcnus', data );
       			}
   			}
			

   		}); 
   		
   		//下一步按钮点击事件
   		$("#btn_acount").click(function(){
   			//获取所有已选cnu在列表显示
   			socket.emit('opt.allcheckedcnus', "allcheckedcnus" );	

   		});
	});
	
	function fun_Allcheckedcnus(data){
		if(data == ""){
			//失败提示对话框		
			$( "#dialog:ui-dialog" ).dialog( "destroy" );			
			$( "#dialog-message-acount-failed" ).dialog({
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
			$("#dialog-message-acount-failed").dialog("open");
			}else{
				window.location.href="/opt/selectprofiles"; 
			}
	}
	
	function fun_Allcnus(data){
		var groupval=[];
		$.each(data, function(key, itemv) {  					
				var item = [itemv.check,itemv.mac,itemv.active,itemv.label,itemv.devicetype,itemv.proname];
				groupval[groupval.length] = item; 				
								
		 	}); 
		 	
		 	cTable = $('#cnuTable').dataTable( {	  			 		
			"bLengthChange": false,					//用户不可改变每页显示数量
			"iDisplayLength": 10,					//每页显示10条数据
			"aaData": groupval,
    		"bInfo": false,	
	        "sPaginationType": "full_numbers",				        
	        "oLanguage": {							//汉化
				"sLengthMenu": "每页显示 _MENU_ 条记录",
				"sZeroRecords": "没有检索到数据",
				"sInfo": "当前数据为从第 _START_ 到第 _END_ 条数据；总共有 _TOTAL_ 条记录",
				"sInfoEmtpy": "没有数据",
				"sProcessing": "正在加载数据...",
				"sSearch": "搜索设备:",
				"oPaginate": {
					"sFirst": "首页",
					"sPrevious": "前页",
					"sNext": "后页",
					"sLast": "尾页"
				}
			},
    		"fnRowCallback": function( nRow, aData, iDisplayIndex ) {
    			if(iDisplayIndex == 0){
    				$("#checkall")[0].checked = true;
    			}		        	

				if(nRow.outerHTML.indexOf("checked")<0)
				{
					$("#checkall")[0].checked = false;
				}
    			
	        	if ( aData[2] == "1" )
	            {
	            	$('td:eq(2)', nRow).html( '在线' );				               
	            }else{
	            	$('td:eq(2)', nRow).html( '离线' );
	            }     
	            
	        },		
	        "aaSorting": [[ 1, "asc" ]],
			"aoColumns": [	
						  { "sTitle": "选择" , "sClass": "center",type: "checkbox"},
						  { "sTitle": "MAC" , "sClass": "center"},
						  { "sTitle": "状态" , "sClass": "center"},
					      { "sTitle": "标识" , "sClass": "center"},
					      { "sTitle": "设备类型" , "sClass": "center"},
					      { "sTitle": "当前模板" , "sClass": "center"}
						],
			
	    } );
	}
})(jQuery);
