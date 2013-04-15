(function($){
	var modal ;
	var pTable;
	$(function(){
		var user = localStorage.getItem('username');
		var socket = io.connect('http://localhost:3000');
		
		socket.emit('profile_all', 'profile_all' );
		socket.emit('preconfig_all','preconfig_all');
		socket.on('profileALL', fun_Allprofiles );
		socket.on('opt.preconfig_one', fun_PreconfigOne );
		socket.on('opt.preconfig_batch', fun_PreconfigBatch );
		socket.on('opt.preconfig_all', fun_PreconfigAll );
		
		var flag = getCookie("flag");
		fun_Checks();
		
		$(".smodal").click(function(){
			fun_Checks();
		});

		$("#pre_del").live('click',function(){
			if(flag == "3"){
	    		  alert("Read-Only, permission denied!");
	    		  return;
	    	  }
			var anRow = $(this);
	        var mac = anRow[0].parentNode.parentElement.cells[0].textContent;
	        var datastring = '{"mac":"'+mac+'","user":"'+user+'"}';
	        socket.emit('pre_del',datastring);
	        var anSelected = anRow[0].parentNode.parentElement;
	        pTable.fnDeleteRow( anSelected );
		});
		
		$("#btn_presub").click(function(){
			if(flag == "3"){
	    		  alert("Read-Only, permission denied!");
	    		  return;
	    	  }
			var objSelect = $("#combox_profiles");
			var reg_name=/^\w{2}(:\w{2}){5}$/; 
			var proid = objSelect[0].options[objSelect[0].options.selectedIndex].value;
			if(modal == "one"){	  
				var xxx = document.getElementById("cnumac").value;
            	if(!reg_name.test(document.getElementById("cnumac").value)){
            		alert("Not the right Mac!");
            		return;
            	}
            	var jsondata = '{"mac":"'+document.getElementById("cnumac").value+'","proid":"'+proid+'","user":"'+user+'"}';
            	socket.emit('opt.preconfig_one',jsondata);
            	
            }else if(modal == "batch"){
            	if(!reg_name.test(document.getElementById("smac").value)){
            		alert("Not the right Mac!");
            		return;
            	}
            	if(!reg_name.test(document.getElementById("emac").value)){
            		alert("Not the right Mac!");
            		return;
            	}
            	
            	var jsondata = '{"smac":"'+document.getElementById("smac").value+
            	'","emac":"'+document.getElementById("emac").value+'","proid":"'+proid+'","user":"'+user+'"}';
            	socket.emit('opt.preconfig_batch',jsondata);
            }
		});
	});
	
	function fun_PreconfigAll(data){
		var groupval=[];
		$.each(data, function(key, itemv) {  					
				var item = [itemv.mac,itemv.proname,itemv.tmp];
				groupval[groupval.length] = item; 				
								
		 	}); 
		 	
		    pTable = $('#precnus').dataTable( {
			"bFilter": false,						//不使用过滤功能
			"bLengthChange": false,					//用户不可改变每页显示数量
			"iDisplayLength": 5,					//每页显示5条数据
			"aaData": groupval,
    		"bInfo": false,	
	        "sPaginationType": "full_numbers",
	        "oLanguage": {							//汉化
				"sLengthMenu": "Display _MENU_ strip record per page",
				"sZeroRecords": "No data has been discoveryed",
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
	            if(aData[2] == "1"){
	            	$('td:eq(2)', nRow).html( '<button id="pre_del">Delete</button>' );
	            }
	            
	        },		
			"aoColumns": [							//设定各列宽度
			              { "sTitle": "MAC", "sClass": "center" },
						  { "sTitle": "Template name", "sClass": "center" },					     
					      { "sTitle": "Operate", "sClass": "center" }
						],
			
	    } );		
		
	}
	
	function fun_PreconfigBatch(data){
		if(data.code == "0"){
			//成功
			//成功提示对话框		
			$( "#dialog:ui-dialog" ).dialog( "destroy" );
				
			$( "#dialog-message" ).dialog({
				autoOpen: false,
				show: "blind",
				modal: true,
				resizable: false,
				hide: "explode",
				buttons: {
					Ok: function() {						
						$( this ).dialog( "close" );
						window.location.reload();
					}
				}
			});

			$("#dialog-message").dialog("open");
		}else if(data.code == "2"){
			$("#errorinfo").empty();
    		$("#errorinfo").append('<p>Failed-config.<br/>Starting MAC bigger than terminating MAC.</p>');
    		$( "#dialog:ui-dialog" ).dialog( "destroy" );

			$( "#dialog-message-preconfig-failed" ).dialog({
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
			$("#dialog-message-preconfig-failed").dialog("open");
		}else if(data.code == "3"){
			$("#errorinfo").empty();
    		$("#errorinfo").append('<p>Failed-config.<br/>Config more than 255.</p>');
    		$( "#dialog:ui-dialog" ).dialog( "destroy" );

			$( "#dialog-message-preconfig-failed" ).dialog({
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
			$("#dialog-message-preconfig-failed").dialog("open");
		}else{
			$("#errorinfo").empty();
			$("#errorinfo").append('<label>Following are the config failure device:</label><br/><br/>');
			$.each(data,function(key,itemv){
				$("#errorinfo").append('<label style="margin-left:30px">'+itemv.mac +'</label><br/>');
			});
			$( "#dialog:ui-dialog" ).dialog( "destroy" );

			$( "#dialog-message-preconfig-failed" ).dialog({
				autoOpen: false,
				show: "blind",
				modal: true,
				resizable: false,
				hide: "explode",
				height: 300,
				buttons: {
					Ok: function() {
						$( this ).dialog( "close" );
						window.location.reload();
					}
				}
			});
			$("#dialog-message-preconfig-failed").dialog("open");
		}
	}
	
    function fun_PreconfigOne(data){
    	if(data == ""){
    		$("#errorinfo").empty();
    		$("#errorinfo").append('<p>Failed-config.<br/>Device already exist or pre-config.</p>');
    		$( "#dialog:ui-dialog" ).dialog( "destroy" );

			$( "#dialog-message-preconfig-failed" ).dialog({
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
			$("#dialog-message-preconfig-failed").dialog("open");
    	}else{
    		$('#precnus').dataTable().fnAddData( [
               	        data.mac,
               	        data.profile,
               	        data.html] );
    		//成功提示对话框
			$( "#dialog:ui-dialog" ).dialog( "destroy" );

			$( "#dialog-message" ).dialog({
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
			$("#dialog-message").dialog("open");
    	}
    }
    
	function fun_Checks(){
		var radios=document.getElementsByName("choose");
        for(var i=0;i<radios.length;i++)
        {
            if(radios[i].checked==true)
            {
            	$("#pre_content").empty();
                modal = radios[i].id;
                if(modal == "one"){	                	
                	$("#pre_content").append('<label>Please input CNU MAC:</label>'+
                			'<input type="text" id="cnumac" value="30:71:b2:"></input>');
                }else if(modal == "batch"){
                	$("#pre_content").append('<table><tr><td>CNU starting MAC:</label></td>'+
        				'<td><input type="text" id="smac" value="30:71:b2:"></input></td></tr>'+
        				'<tr><td>CNU terminating MAC:</label></td>'+
        				'<td><input type="text" id="emac" value="30:71:b2:"></input></td></tr>');
                }
            }
        }
	}
	
	function fun_Allprofiles(data){
		var objSelect = $("#combox_profiles");
		$.each(data, function(key, itemv) {  					
				var item = itemv.proname;
				var varItem = new Option(item, itemv.id);      
			objSelect[0].options.add(varItem);  							
		 	});
		sortOption();
	}
	
	function sortRule(a,b) {
		 var x = a._value;
		 var y = b._value;
		 return x.localeCompare(y);
	}
	
	function op(){
		 var _value;
		 var _text;
	}
	function sortOption(){
		 var obj = document.getElementById("combox_profiles");
		 var tmp = new Array();
		 for(var i=0;i<obj.options.length;i++){
			  var ops = new op();
			  ops._value = obj.options[i].value;
			  ops._text = obj.options[i].text;
			  tmp.push(ops);
		 }
		 tmp.sort(sortRule);
		 for(var j=0;j<tmp.length;j++){
			  obj.options[j].value = tmp[j]._value;
			  obj.options[j].text = tmp[j]._text;
		 }
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