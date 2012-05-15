(function($){
	var pTable;
	$(function(){
		socket = io.connect('http://localhost:3000');
		
		socket.emit('profile_all',"profile_all");
		
		socket.on('profileALL',fun_Allprofiles);
		socket.on('profileDEL',fun_Delprofile);
		socket.on('profileisedit',fun_Editprofile);
		
		var proname = $( "#proname" ),
		vlanen = $( "#vlanen" ),
		//vlanid = $( "#vlanid" ),
		vlan0id = $( "#vlan0id" ),
		vlan1id = $( "#vlan1id" ),
		vlan2id = $( "#vlan2id" ),
		vlan3id = $( "#vlan3id" ),
		rxlimitsts = $( "#rxlimitsts" ),
		cpuportrxrate = $( "#cpuportrxrate" ),
		port0txrate = $( "#port0txrate" ),
		port1txrate = $( "#port1txrate" ),
		port2txrate = $( "#port2txrate" ),
		port3txrate = $( "#port3txrate" ),
		txlimitsts = $( "#txlimitsts" ),
		cpuporttxrate = $( "#cpuporttxrate" ),
		port0rxrate = $( "#port0rxrate" ),
		port1rxrate = $( "#port1rxrate" ),
		port2rxrate = $( "#port2rxrate" ),
		port3rxrate = $( "#port3rxrate" ),
		
		
		allFields = $( [] ).add( proname ).add( vlanen ).add( vlan0id ).add( vlan1id ).add( vlan2id )
		.add( vlan3id ).add( rxlimitsts ).add( cpuportrxrate ).add( port0txrate ).add( port1txrate ).add( port2txrate )
		.add( port3txrate ).add( txlimitsts ).add( cpuporttxrate ).add( port0rxrate ).add( port1rxrate ).add( port2rxrate )
		.add( port3rxrate ),
		
		tips = $( ".validateTips" );
		
		$('#proTable tbody tr').live('click', function( e ) {
	        if ( $(this).hasClass('row_selected') ) {
	            $(this).removeClass('row_selected');
	        }
	        else {
	            pTable.$('tr.row_selected').removeClass('row_selected');
	            $(this).addClass('row_selected');
	        }
	    });
		
		function updateTips( t ) {
			tips
				.text( t )
				.addClass( "ui-state-highlight" );
			setTimeout(function() {
				tips.removeClass( "ui-state-highlight", 1500 );
			}, 500 );
		}

		function checkvalue( o, n, min, max){
			if(isNaN(o.val())){
				o.addClass( "ui-state-error" );
				updateTips( n + "必须是数字!" );
				return false;
			}
			if(o.val()> max || o.val()<min)
			{
				o.addClass( "ui-state-error" );
				updateTips( n+"的值必须在"+min + "到" + max+ "之间!" );
				return false;
			}else{
				return true;
			}
		}
		
		function checknum( o, n){
			if(isNaN(o.val()))
			{
				o.addClass( "ui-state-error" );
				updateTips( n + "必须是数字!");
				return false;
			}else{
				return true;
			}
		}
		
		/* Add a click handler for the delete row */
	    $('#btn_delete').click( function() {
	        var anSelected = fnGetSelected( pTable );
	        socket.emit('profile_del',anSelected[0].firstChild.textContent);
	        
	    } );    	
	    
	    $("#btn_create").click( function() { 
	    	$( "#dialog:ui-dialog" ).dialog( "destroy" );	
			
			$("#dialog-form").dialog({
				autoOpen: false,
				resizable: false,
				show: "blind",
				hide: "explode",
				modal: true,
				height: 550,
				width: 600,
				buttons: {
					"保存": function() {
						var bValid = true;
						allFields.removeClass( "ui-state-error" );
						//验证
						if(proname.val() == ""){
							proname.addClass( "ui-state-error" );
							updateTips( "模板名称 不能为空! ");
							bValid = false;
						}
						bValid = bValid && checknum(cpuportrxrate,"全局下行限速")&& checknum(port0txrate,"1端口限速")&& checknum(port1txrate,"2端口限速")&& checknum(port2txrate,"3端口限速")&& checknum(port3txrate,"4端口限速")
							&& checknum(cpuporttxrate,"全局上行限速")&& checknum(port0rxrate,"1端口限速")&& checknum(port1rxrate,"2端口限速")&& checknum(port2rxrate,"3端口限速")&& checknum(port3rxrate,"4端口限速"); 
							
						bValid = bValid&& checkvalue(vlanen,"Vlan使能",1,2)&& checkvalue(vlan0id,"vlan0id",0,4095)&& checkvalue(vlan1id,"vlan1id",0,4095)
						&& checkvalue(vlan2id,"vlan2id",0,4095)&& checkvalue(vlan3id,"vlan3id",0,4095)&& checkvalue(rxlimitsts,"rxlimitsts",1,2)&& checkvalue(txlimitsts,"txlimitsts",1,2);
						
						if(bValid){
							var datastring = '{"proname":"'+proname.val()+'","vlanen":"'+vlanen.val()+
							'","vlan0id":"'+vlan0id.val()+'","vlan1id":"'+vlan1id.val()+'","vlan2id":"'+vlan2id.val()+'","vlan3id":"'+vlan3id.val()+
							'","rxlimitsts":"'+rxlimitsts.val()+'","cpuportrxrate":"'+cpuportrxrate.val()+'","port0txrate":"'+port0txrate.val()+
							'","port1txrate":"'+port1txrate.val()+'","port2txrate":"'+port2txrate.val()+'","port3txrate":"'+port3txrate.val()+
							'","txlimitsts":"'+txlimitsts.val()+'","cpuporttxrate":"'+cpuporttxrate.val()+'","port0rxrate":"'+port0rxrate.val()+
							'","port1rxrate":"'+port1rxrate.val()+'","port2rxrate":"'+port2rxrate.val()+'","port3rxrate":"'+port3rxrate.val()+'"}';
							
							socket.emit('profile_create',datastring);
							$("#dialog-form").dialog("close");						
						
					}},
					"取消": function() {
						$( this ).dialog("close");
					}
				},
				close: function() {
					allFields.val( "" ).removeClass( "ui-state-error" );
				}
			});	

			$("#dialog-form").dialog("open");
		  });
	    
	    /* Add a click handler for the edit row */
	    $('#btn_edit').click( function() {
	        var anSelected = fnGetSelected( pTable );
	        socket.emit('profile_isedit',anSelected[0].firstChild.textContent);
	      }); 
	});

	
	function updateTips( t ) {
		tips
			.text( t )
			.addClass( "ui-state-highlight" );
		setTimeout(function() {
			tips.removeClass( "ui-state-highlight", 1500 );
		}, 500 );
	}

	function checkvalue( o, n, min, max){
		if(isNaN(o.val())){
			o.addClass( "ui-state-error" );
			updateTips( n + "必须是数字!" );
			return false;
		}
		if(o.val()> max || o.val()<min)
		{
			o.addClass( "ui-state-error" );
			updateTips( n+"的值必须在"+min + "到" + max+ "之间!" );
			return false;
		}else{
			return true;
		}
	}
	
	function checknum( o, n){
		if(isNaN(o.val()))
		{
			o.addClass( "ui-state-error" );
			updateTips( n + "必须是数字!");
			return false;
		}else{
			return true;
		}
	}
	
	/* Get the rows which are currently selected */
	function fnGetSelected( oTableLocal )
	{
	    return oTableLocal.$('tr.row_selected');
	}
	
	function fun_Editprofile(tmpdata){
		if(tmpdata != ""){
			$( "#dialog:ui-dialog" ).dialog( "destroy" );
			
			var proname = $( "#proname" ),
			vlanen = $( "#vlanen" ),
			//vlanid = $( "#vlanid" ),
			vlan0id = $( "#vlan0id" ),
			vlan1id = $( "#vlan1id" ),
			vlan2id = $( "#vlan2id" ),
			vlan3id = $( "#vlan3id" ),
			rxlimitsts = $( "#rxlimitsts" ),
			cpuportrxrate = $( "#cpuportrxrate" ),
			port0txrate = $( "#port0txrate" ),
			port1txrate = $( "#port1txrate" ),
			port2txrate = $( "#port2txrate" ),
			port3txrate = $( "#port3txrate" ),
			txlimitsts = $( "#txlimitsts" ),
			cpuporttxrate = $( "#cpuporttxrate" ),
			port0rxrate = $( "#port0rxrate" ),
			port1rxrate = $( "#port1rxrate" ),
			port2rxrate = $( "#port2rxrate" ),
			port3rxrate = $( "#port3rxrate" ),
			
			
			allFields = $( [] ).add( proname ).add( vlanen ).add( vlan0id ).add( vlan1id ).add( vlan2id )
			.add( vlan3id ).add( rxlimitsts ).add( cpuportrxrate ).add( port0txrate ).add( port1txrate ).add( port2txrate )
			.add( port3txrate ).add( txlimitsts ).add( cpuporttxrate ).add( port0rxrate ).add( port1rxrate ).add( port2rxrate )
			.add( port3rxrate ),
			
			tips = $( ".validateTips" );
			//组装弹出窗口html	
			$("#dialog-edit").empty();
			$("#dialog-edit").append('<p class="validateTips">All form fields are required.</p>'+	
				'<fieldset>'+
					'<legend>基本配置</legend>'+
					'<form>'+
						'<table>'+
							'<tr><td><label for="proname_e" >模板名称：</label></td>'+
								'<td><input type="text" name="proname_e" id="proname_e" value='+tmpdata.proname+' class="text ui-widget-content ui-corner-all" /></td>'+
								'<td><label for="vlanen_e">Vlan使能:</label></td>'+
								'<td><select name="vlanen_e" id="vlanen_e">'+
									'<option value="1">启动</option>'+
									'<option value="2">禁用</option>'+
								'</select></td>	</tr>'+
							'<tr>'+
							'<td><label for="vlan0id_e">Vlan0id:</label></td>'+
							'<td><input type="text" name="vlan0id_e" id="vlan0id_e" value='+tmpdata.vlan0id+ ' class="text ui-widget-content ui-corner-all" /></td>'+
							'<td><label for="vlan1id_e">Vlan1id:</label></td>'+
							'<td><input type="text" name="vlan1id_e" id="vlan1id_e" value='+tmpdata.vlan1id+ ' class="text ui-widget-content ui-corner-all" /></td></tr>'+
							'<tr><td><label for="vlan2id_e">Vlan2id:</label></td>'+
							'<td><input type="text" name="vlan2id_e" id="vlan2id_e" value='+tmpdata.vlan2id+ ' class="text ui-widget-content ui-corner-all" /></td>'+
							'<td><label for="vlan3id_e">Vlan3id:</label></td>'+
							'<td><input type="text" name="vlan3id_e" id="vlan3id_e" value='+tmpdata.vlan3id+ ' class="text ui-widget-content ui-corner-all" /></td></tr>'+
						'</table>'+
					'</form></fieldset>'+
					'<fieldset><legend>下行配置(KB)</legend>'+
					'<form><table>'+
						'<tr><td><label for="rxlimitsts_e">下行限速使能：</label></td>'+
						'<td><select name="rxlimitsts_e" id="rxlimitsts_e">'+
						'<option value="1">启动</option><option value="2">禁用</option>'+
						'</select></td>'+
						'<td><label for="cpuportrxrate_e">全局下行限速:</label></td>'+
						'<td><input type="text" name="cpuportrxrate_e" id="cpuportrxrate_e" value='+tmpdata.cpuportrxrate+ ' class="text ui-widget-content ui-corner-all" /></td></tr>'+
						'<tr><td><label for="port0txrate_e">1端口限速:</label></td>'+
						'<td><input type="text" name="port0txrate_e" id="port0txrate_e" value='+tmpdata.port0txrate+ ' class="text ui-widget-content ui-corner-all" /></td>'+
						'<td><label for="port1txrate_e">2端口限速:</label></td>'+
						'<td><input type="text" name="port1txrate_e" id="port1txrate_e" value='+tmpdata.port1txrate+ ' class="text ui-widget-content ui-corner-all" /></td></tr>'+
						'<tr><td><label for="port2txrate_e">3端口限速:</label></td>'+
						'<td><input type="text" name="port2txrate_e" id="port2txrate_e" value='+tmpdata.port2txrate+ ' class="text ui-widget-content ui-corner-all" /></td>'+
						'<td><label for="port3txrate_e">4端口限速:</label></td>'+
						'<td><input type="text" name="port3txrate_e" id="port3txrate_e" value='+tmpdata.port3txrate+ ' class="text ui-widget-content ui-corner-all" /></td></tr>'+
					'</table></form></fieldset>'+
					'<fieldset><legend>上行配置(KB)</legend>'+
					'<form><table>'+
						'<tr><td><label for="txlimitsts_e">上行限速使能：</label></td>'+
						'<td><select name="txlimitsts_e" id="txlimitsts_e">'+
						'<option value="1">启动</option><option value="2">禁用</option>'+
						'</select></td>'+
						'<td><label for="cpuporttxrate_e">全局上行限速:</label></td>'+
						'<td><input type="text" name="cpuporttxrate_e" id="cpuporttxrate_e" value='+tmpdata.cpuporttxrate+ ' class="text ui-widget-content ui-corner-all" /></td></tr>'+
						'<tr><td><label for="port0rxrate_e">1端口限速:</label></td>'+
						'<td><input type="text" name="port0rxrate_e" id="port0rxrate_e" value='+tmpdata.port0rxrate+ ' class="text ui-widget-content ui-corner-all" /></td>'+
						'<td><label for="port1txrate_e">2端口限速:</label></td>'+
						'<td><input type="text" name="port1rxrate_e" id="port1rxrate_e" value='+tmpdata.port1rxrate+ ' class="text ui-widget-content ui-corner-all" /></td></tr>'+
						'<tr><td><label for="port2rxrate_e">3端口限速:</label></td>'+
						'<td><input type="text" name="port2rxrate_e" id="port2rxrate_e" value='+tmpdata.port2rxrate+ ' class="text ui-widget-content ui-corner-all" /></td>'+
						'<td><label for="port3txrate_e">4端口限速:</label></td>'+
						'<td><input type="text" name="port3rxrate_e" id="port3rxrate_e" value='+tmpdata.port3rxrate+ ' class="text ui-widget-content ui-corner-all" /></td></tr>'+
					'</table></form></fieldset>');

				document.getElementById('vlanen_e').value = tmpdata.vlanen;
				document.getElementById('rxlimitsts_e').value = tmpdata.rxlimitsts;
				document.getElementById('txlimitsts_e').value = tmpdata.txlimitsts;
				
				proname = $( "#proname_e" ),
				vlanen = $( "#vlanen_e" ),
				//vlanid = $( "#vlanid_e" ),
				vlan0id = $( "#vlan0id_e" ),
				vlan1id = $( "#vlan1id_e" ),
				vlan2id = $( "#vlan2id_e" ),
				vlan3id = $( "#vlan3id_e" ),
				rxlimitsts = $( "#rxlimitsts_e" ),
				cpuportrxrate = $( "#cpuportrxrate_e" ),
				port0txrate = $( "#port0txrate_e" ),
				port1txrate = $( "#port1txrate_e" ),
				port2txrate = $( "#port2txrate_e" ),
				port3txrate = $( "#port3txrate_e" ),
				txlimitsts = $( "#txlimitsts_e" ),
				cpuporttxrate = $( "#cpuporttxrate_e" ),
				port0rxrate = $( "#port0rxrate_e" ),
				port1rxrate = $( "#port1rxrate_e" ),
				port2rxrate = $( "#port2rxrate_e" ),
				port3rxrate = $( "#port3rxrate_e" ),
		
				//弹出窗口
				$("#dialog-edit").dialog({
				autoOpen: false,
				resizable: false,
				show: "blind",
				hide: "explode",
				modal: true,
				height: 550,
				width: 600,
				buttons: {
					"保存": function() {
						var bValid = true;
						allFields.removeClass( "ui-state-error" );
						//验证
						if(proname.val() == ""){
							proname.addClass( "ui-state-error" );
							updateTips( "模板名称 不能为空! ");
							bValid = false;
						}
						bValid = bValid && checknum(cpuportrxrate,"全局下行限速")&& checknum(port0txrate,"1端口限速")&& checknum(port1txrate,"2端口限速")&& checknum(port2txrate,"3端口限速")&& checknum(port3txrate,"4端口限速")
							&& checknum(cpuporttxrate,"全局上行限速")&& checknum(port0rxrate,"1端口限速")&& checknum(port1rxrate,"2端口限速")&& checknum(port2rxrate,"3端口限速")&& checknum(port3rxrate,"4端口限速"); 
							
						bValid = bValid&& checkvalue(vlanen,"Vlan使能",1,2)&& checkvalue(vlan0id,"vlan0id",0,4095)&& checkvalue(vlan1id,"vlan1id",0,4095)
						&& checkvalue(vlan2id,"vlan2id",0,4095)&& checkvalue(vlan3id,"vlan3id",0,4095)&& checkvalue(rxlimitsts,"rxlimitsts",1,2)&& checkvalue(txlimitsts,"txlimitsts",1,2);
						
						if(bValid){
							var anSelected = fnGetSelected( pTable );
							var datastring = '{"proid":"'+anSelected[0].firstChild.textContent+'","proname":"'+proname.val()+'","vlanen":"'+vlanen.val()+
							'","vlan0id":"'+vlan0id.val()+'","vlan1id":"'+vlan1id.val()+'","vlan2id":"'+vlan2id.val()+'","vlan3id":"'+vlan3id.val()+
							'","rxlimitsts":"'+rxlimitsts.val()+'","cpuportrxrate":"'+cpuportrxrate.val()+'","port0txrate":"'+port0txrate.val()+
							'","port1txrate":"'+port1txrate.val()+'","port2txrate":"'+port2txrate.val()+'","port3txrate":"'+port3txrate.val()+
							'","txlimitsts":"'+txlimitsts.val()+'","cpuporttxrate":"'+cpuporttxrate.val()+'","port0rxrate":"'+port0rxrate.val()+
							'","port1rxrate":"'+port1rxrate.val()+'","port2rxrate":"'+port2rxrate.val()+'","port3rxrate":"'+port3rxrate.val()+'"}';
							socket.emit('profile_edit',datastring);
							$("#dialog-edit").dialog("close");
							pTable=$('#proTable').dataTable();

						
					}},
					"取消": function() {
						$( this ).dialog("close");
					}
				},
				close: function() {
					allFields.val( "" ).removeClass( "ui-state-error" );
				}
			});	
		
			$("#dialog-edit").dialog("open");
		}else{
			//失败提示对话框					
			$( "#dialog-pro-failed" ).dialog({
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
			$("#dialog-pro-failed").dialog("open");
		}
		
	}
	
	function fun_Delprofile(data){
		if(data != ""){
			//成功
			var anSelected = fnGetSelected( pTable );
			pTable.fnDeleteRow( anSelected[0] );
		}else{
			//失败
			//失败提示对话框					
			$( "#dialog-pro-failed" ).dialog({
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
			$("#dialog-pro-failed").dialog("open");
		}
	}
	
	function fun_Allprofiles(data){
		var groupval=[];
		$.each(data, function(key, itemv) {  					
				var item = [itemv.id,itemv.proname,itemv.vlanen,itemv.vlan0id,itemv.vlan1id,itemv.vlan2id,itemv.vlan3id,
				itemv.rxlimitsts,itemv.cpuportrxrate,itemv.port0txrate,itemv.port1txrate,itemv.port2txrate,itemv.port3txrate,
				itemv.txlimitsts,itemv.cpuporttxrate,itemv.port0rxrate,];
				groupval[groupval.length] = item; 				
								
		 	}); 
		 	
		 	pTable = $('#proTable').dataTable( {
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
})(jQuery);