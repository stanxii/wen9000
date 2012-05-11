(function($){
	$(function(){
		socket = io.connect('http://localhost:3000');
		
		socket.emit('opt.onlinecbats',"onlinecbats");
		
		socket.on('opt.onlinecbats',fun_OnlineCbats);
		socket.on('opt.ftpfilelist',fun_Ftpfilelist);
		socket.on('opt.updateproc',fun_Updateproc);
		socket.on('opt.updateinfo',fun_Updateinfo);
		
		$('.chk').live('click', function () {
	        var checkbox = $(this);
	        var mac = checkbox[0].parentElement.parentElement.cells[1].textContent;
	        var data = '{"mac":"'+mac+'","value":"'+checkbox[0].checked+'"}';
	        socket.emit('opt.updatedcbats', data );
	    } );
		
		$("#ftp_connect").click(function(){
			var objSelect = $("#combox_files");		
			if(objSelect[0].options.length>0){
				var length = document.getElementById('combox_files').options.length;
				for(var i = 0;i<length;i++){
					objSelect[0].options.remove(i);
				}
			}
			var ftpip = document.getElementById("serverip").value;
			var ftpport = document.getElementById("serverport").value;
			var ftpuser = document.getElementById("username").value;
			var ftppassword = document.getElementById("password").value;
			var data = '{"ftpip":"'+ftpip+'","ftpport":"'+ftpport+'","username":"'+ftpuser+'","password":"'+ftppassword+'"}';
			socket.emit('opt.ftpconnet',data);
			
			
		});
		
		$("#showproc").click(function(){
			socket.emit('opt.updateinfo',"updateinfo");		
		});
		
		$("#btn_update").click(function(){
			var objSelect = $("#combox_files");
			var ftpip = document.getElementById("serverip").value;
			var ftpport = document.getElementById("serverport").value;
			var ftpuser = document.getElementById("username").value;
			var ftppassword = document.getElementById("password").value;
			var filename = objSelect[0].options[objSelect[0].options.selectedIndex].value;
			var data = '{"ftpip":"'+ftpip+'","ftpport":"'+ftpport+'","username":"'+ftpuser+'","password":"'+ftppassword+ 
			'","filename":"' + filename + '"}';
			if(filename == ""){
				Alert("没有选择升级文件!!!");
				return;
			}
			socket.emit('opt.ftpupdate',data);
			
			$( "#dialog:ui-dialog" ).dialog( "destroy" );
			
			$( "#dialog-message-proc" ).dialog({
				autoOpen: false,
				show: "blind",
				modal: true,
				resizable: false,
				hide: "explode"
			});
			$("#dialog-message-proc").dialog("open");
			
			$("#up_proc")[0].textContent = "正在初始化升级…………";
		});
		
		initTable();
	});
	
	function fun_Updateinfo(data){
		//获取进度信息
		if(data != ""){
			$("#up_proc")[0].textContent = data;
			
			$( "#dialog:ui-dialog" ).dialog( "destroy" );
			
			$( "#dialog-message-proc" ).dialog({
				autoOpen: false,
				show: "blind",
				modal: true,
				resizable: false,
				hide: "explode"
			});
			$("#dialog-message-proc").dialog("open");
		}
		
	}
	
	function fun_Updateproc(data){
		$("#up_proc")[0].textContent = data;
	}
	
	function fun_Ftpfilelist(data){
		if(data == ""){
			$( "#dialog:ui-dialog" ).dialog( "destroy" );
			
			$( "#dialog-message-ftp-failed" ).dialog({
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
			$("#dialog-message-ftp-failed").dialog("open");
		}else{
			var objSelect = $("#combox_files");
			$.each(data, function(key, itemv) {  					
				var item = itemv.filename;
				var varItem = new Option(item);      
				objSelect[0].options.add(varItem);  							
		 	});
			
			$("#btn_update").attr("disabled",false); 
		}			
	}
	
	function fun_OnlineCbats(data){
		if(data != ""){
			$.each(data, function(key, itemv) {  					
  				$('#upcbat_table').dataTable().fnAddData( [
			        itemv.check,
			        itemv.mac,
			        itemv.ip,
			        itemv.devicetype,
			        itemv.appver,
			        itemv.upgrade] );
  								
			 	});
		}
	}
	
	function initTable(){
		$('#upcbat_table').dataTable( {	  			 		
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
	        	if ( aData[1] == "3" )
	            {
	            	$('td:eq(1)', nRow).html( '在线' );				               
	            }else{
	            	$('td:eq(1)', nRow).html( '下线' );
	            } 
	        	
	        	if(aData[5] == "0"){
	        		$('td:eq(5)', nRow).html( 'successful' );	
	        	}else if(aData[5] == "1"){
	        		$('td:eq(5)', nRow).html( 'Upgrading' );	
	        	}else if(aData[5] == "2"){
	        		$('td:eq(5)', nRow).html( 'Open DB Error' );	
	        	}else if(aData[5] == "3"){
	        		$('td:eq(5)', nRow).html( 'Read DB Error' );	
	        	}else if(aData[5] == "4"){
	        		$('td:eq(5)', nRow).html( 'Init Network Error' );	
	        	}else if(aData[5] == "5"){
	        		$('td:eq(5)', nRow).html( 'File Not Exist' );	
	        	}else if(aData[5] == "6"){
	        		$('td:eq(5)', nRow).html( 'MD5File Not Exist' );	
	        	}else if(aData[5] == "7"){
	        		$('td:eq(5)', nRow).html( 'MD5 Error' );	
	        	}else if(aData[5] == "8"){
	        		$('td:eq(5)', nRow).html( 'FLASH Exhausted' );	
	        	}else if(aData[5] == "9"){
	        		$('td:eq(5)', nRow).html( 'Not Enough Memory' );	
	        	}else if(aData[5] == "10"){
	        		$('td:eq(5)', nRow).html( 'Server Unreachable' );	
	        	}else if(aData[5] == "11"){
	        		$('td:eq(5)', nRow).html( 'Invalid Parameter' );	
	        	}else if(aData[5] == "12"){
	        		$('td:eq(5)', nRow).html( 'FLASH Write Error' );	
	        	}else if(aData[5] == "13"){
	        		$('td:eq(5)', nRow).html( 'FLASH Erase Error' );	
	        	}else if(aData[5] == "14"){
	        		$('td:eq(5)', nRow).html( 'MTD Open Error' );	
	        	}else if(aData[5] == "15"){
	        		$('td:eq(5)', nRow).html( 'Image Test Error' );	
	        	}else if(aData[5] == "16"){
	        		$('td:eq(5)', nRow).html( 'Unkonw Error' );	
	        	}else if(aData[5] == "19"){
	        		$('td:eq(5)', nRow).html( 'Pending' );	
	        	}else if(aData[5] == "20"){
	        		$('td:eq(5)', nRow).html( 'A/N' );	
	        	}
	            
	        },		
	        "aaSorting": [[ 0, "asc" ]],
			"aoColumns": [	
			              { "sTitle": "选择" , "sClass": "center"},
						  { "sTitle": "MAC" , "sClass": "center"},
						  { "sTitle": "IP" , "sClass": "center"},
						  { "sTitle": "设备类型" , "sClass": "center"},
					      { "sTitle": "软件版本" , "sClass": "center"},
					      { "sTitle": "升级状态" , "sClass": "center"}
						],
			
	    } );
	}
})(jQuery);