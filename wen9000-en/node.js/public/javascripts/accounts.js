(function($){
	$(function(){
		socket = io.connect('http://localhost:3000');
		
		socket.emit('opt.cnus', 'cnus' );
		
		socket.on('opt.allcnus', fun_Allcnus );
		socket.on('opt.allcheckedcnus', fun_Allcheckedcnus );
		socket.on('checkallcnusres', fun_Checkallcnus );
		var flag = getCookie("flag");
		$('.chk').live('click', function () {
			if(flag == "3"){
	    		  alert("Read-Only, permission denied!");
	    		  return;
	    	  }
	        var checkbox = $(this);
	        var mac = checkbox[0].parentElement.parentElement.cells[1].textContent;
	        var data = '{"cnumac":"'+mac+'","value":"'+checkbox[0].checked+'"}';
	        socket.emit('opt.checkedcnus', data );
	        if(!checkbox[0].checked){
	        	$("#checkalllist")[0].checked = false;
	        	$("#checkall")[0].checked = false;
	        }else{
	        	for(var i=1;i<cTable[0].rows.length;i++){
      				if(!cTable[0].rows[i].firstChild.firstChild.checked){
      					$("#checkalllist")[0].checked = false;
      					return;
       				}       				
       			}
	        	$("#checkalllist")[0].checked = true;
	        }
	    } );
		
		//整列全选按钮点击事件
   		$("#checkalllist").click(function(){
   			if(flag == "3"){
	    		  alert("Read-Only, permission denied!");
	    		  return;
	    	  }
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
   		
   	//全选按钮点击事件
   		$("#checkall").click(function(){
   			if(flag == "3"){
	    		  alert("Read-Only, permission denied!");
	    		  return;
	    	  }
   			var c_box = $(this);
   			var mac;
   			if(c_box[0].checked){
   				for(var i=1;i<cTable[0].rows.length;i++){
       				if(cTable[0].rows[i].firstChild.firstChild.checked){
       					continue;
       				}
       				cTable[0].rows[i].firstChild.firstChild.checked = true;
       			}
   				$("#checkalllist")[0].checked = true;
       		    socket.emit('opt.checkallcnus', "true" );       			
   			}else{   
   				for(var i=1;i<cTable[0].rows.length;i++){
       				if(cTable[0].rows[i].firstChild.firstChild.checked){
       					cTable[0].rows[i].firstChild.firstChild.checked = false;
       				}       				
       			}
   				$("#checkalllist")[0].checked = false;
       		    socket.emit('opt.checkallcnus', "false" );       			
   			}			
				socket.emit('opt.cnus', 'cnus' );
   		}); 
   		
   		//下一步按钮点击事件
   		$("#btn_acount").click(function(){
   			if(flag == "3"){
	    		  alert("Read-Only, permission denied!");
	    		  return;
	    	  }
   			//获取所有已选cnu在列表显示
   			socket.emit('opt.allcheckedcnus', "allcheckedcnus" );	

   		});
	});
	
	function fun_Checkallcnus(data){
		//$('#cnuTable').dataTable();
		//socket.emit('opt.cnus', 'cnus' );
		//window.location.reload();
	}
	
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
		cTable = null;
		var groupval=[];
		$.each(data, function(key, itemv) {  					
				var item = [itemv.check,itemv.mac,itemv.active,itemv.label,itemv.devicetype,itemv.cbatip,itemv.proname];
				groupval[groupval.length] = item; 				
								
		 	}); 
		 	
		 	cTable = $('#cnuTable').dataTable( {	  			 		
			"bLengthChange": false,					//用户不可改变每页显示数量
			"iDisplayLength": 10,					//每页显示10条数据
			"aaData": groupval,
    		"bInfo": false,	
    		"bDestroy":true,
	        "sPaginationType": "full_numbers",				        
	        "oLanguage": {							//汉化
				"sLengthMenu": "Display _MENU_ strip record per page",
				"sZeroRecords": "No data was discoveryed",
				"sInfo": "Current data is from _START_ to _END_ strip data; has _TOTAL_ strip record in all",
				"sInfoEmtpy": "No data",
				"sProcessing": "Data loading...",
				"sSearch": "Search device:",
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
	            	$('td:eq(2)', nRow).html( 'Online' );				               
	            }else{
	            	$('td:eq(2)', nRow).html( 'Offline' );
	            }     
	            
	        },		
	        "aaSorting": [[ 1, "asc" ]],
			"aoColumns": [	
						  { "sTitle": "Choice" , "sClass": "center",type: "checkbox"},
						  { "sTitle": "MAC" , "sClass": "center"},
						  { "sTitle": "Status" , "sClass": "center"},
					      { "sTitle": "Lable" , "sClass": "center"},
					      { "sTitle": "Device type" , "sClass": "center"},
					      { "sTitle": "Cbat IP" , "sClass": "center"},
					      { "sTitle": "Current template" , "sClass": "center"}
						],
			
	    } );
	}
	
	function getCookie(objName)//获取指定名称的cookie的值
	{    
	    var arrStr = document.cookie.split(";");
	    
        for(var i = 0;i < arrStr.length;i++)
        {
            var temp = arrStr[i].split("=");
            if(objName.trim()==temp[0].trim()) //此处如果没有去掉字符串空格就不行,偶在这里折腾了半死,主要是这种错误不好跟踪啊
            {                
            	return temp[1];
            }                            
        }
	}
})(jQuery);
