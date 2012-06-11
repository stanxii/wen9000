(function($){
	var modal ;
	var pTable;
	$(function(){
		var socket = io.connect('http://localhost:3000');
		
		socket.emit('profile_all', 'profile_all' );
		socket.emit('preconfig_all','preconfig_all');
		socket.on('profileALL', fun_Allprofiles );
		socket.on('opt.preconfig_one', fun_PreconfigOne );
		socket.on('opt.preconfig_batch', fun_PreconfigBatch );
		socket.on('opt.preconfig_all', fun_PreconfigAll );
		
		fun_Checks();
		
		$(".smodal").click(function(){
			fun_Checks();
		});

		$("#pre_del").live('click',function(){
			var anRow = $(this);
	        var mac = anRow[0].parentNode.parentElement.cells[0].textContent;
	        socket.emit('pre_del',mac);
	        var anSelected = anRow[0].parentNode.parentElement;
	        pTable.fnDeleteRow( anSelected );
		});
		
		$("#btn_presub").click(function(){
			var objSelect = $("#combox_profiles");
			var reg_name=/^\w{2}(:\w{2}){5}$/; 
			var proid = objSelect[0].options[objSelect[0].options.selectedIndex].value;
			if(modal == "one"){	  
				var xxx = document.getElementById("cnumac").value;
            	if(!reg_name.test(document.getElementById("cnumac").value)){
            		alert("Mac地址不正确!");
            		return;
            	}
            	var jsondata = '{"mac":"'+document.getElementById("cnumac").value+'","proid":"'+proid+'"}';
            	socket.emit('opt.preconfig_one',jsondata);
            	
            }else if(modal == "batch"){
            	if(!reg_name.test(document.getElementById("smac").value)){
            		alert("Mac地址不正确!");
            		return;
            	}
            	if(!reg_name.test(document.getElementById("emac").value)){
            		alert("Mac地址不正确!");
            		return;
            	}
            	
            	var jsondata = '{"smac":"'+document.getElementById("smac").value+
            	'","emac":"'+document.getElementById("emac").value+'","proid":"'+proid+'"}';
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
	            if(aData[2] == "1"){
	            	$('td:eq(2)', nRow).html( '<button id="pre_del">删除</button>' );
	            }
	            
	        },		
			"aoColumns": [							//设定各列宽度
			              { "sTitle": "MAC", "sClass": "center" },
						  { "sTitle": "模板名称", "sClass": "center" },					     
					      { "sTitle": "操作", "sClass": "center" }
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
    		$("#errorinfo").append('<p>配置失败.<br/>起始MAC大于终止MAC.</p>');
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
    		$("#errorinfo").append('<p>配置失败.<br/>配置数量多于255个.</p>');
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
			$("#errorinfo").append('<label>以下是配置失败的设备：</label><br/><br/>');
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
    		$("#errorinfo").append('<p>配置失败.<br/>设备已存在或已预开户.</p>');
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
                	$("#pre_content").append('<label>请输入终端MAC地址:</label>'+
                			'<input type="text" id="cnumac" value="30:71:b2:"></input>');
                }else if(modal == "batch"){
                	$("#pre_content").append('<table><tr><td>起始终端MAC地址:</label></td>'+
        				'<td><input type="text" id="smac" value="30:71:b2:"></input></td></tr>'+
        				'<tr><td>终止终端MAC地址:</label></td>'+
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
})(jQuery);