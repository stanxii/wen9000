(function($){
	var pTable;
	$(function(){
		user = localStorage.getItem('username');
		socket = io.connect('http://localhost:3000');
		
		socket.emit('profile_all',"profile_all");
		
		socket.on('profileALL',fun_Allprofiles);
		socket.on('profileDEL',fun_Delprofile);
		socket.on('profileisedit',fun_Editprofile);
		socket.on('profiledetail',fun_Detail);
		
		var flag = getCookie("flag");
		var proname = $( "#proname" ),
		authorization = $( "#authorization" ),
		vlanen = $( "#vlanen" ),
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
		port3rxrate = $( "#port3rxrate" );		
		
		allFields = $( [] ).add( proname ).add( authorization ).add( vlanen ).add( vlan0id ).add( vlan1id ).add( vlan2id )
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
			if(o.val()==""){
				o.val() = 2;
			}
			if(isNaN(o.val())){
				o.addClass( "ui-state-error" );
				updateTips( n + "must be numberal!" );
				return false;
			}
			if(o.val()> max || o.val()<min)
			{
				o.addClass( "ui-state-error" );
				updateTips( n+"value must in the range from"+min + "to" + max );
				return false;
			}else{
				return true;
			}
		}
		
		function checknum( o, n){
			if(o.val()==""){
				o[0].value = 0;
			}
			if(isNaN(o.val()))
			{
				o.addClass( "ui-state-error" );
				updateTips( n + "must be numberal!");
				return false;
			}else{
				if(o.val()>102400 || o.val()<0){
					o.addClass( "ui-state-error" );
					updateTips( n + "value must between 0~102400!");
					return false;
				}
				return true;
			}
		}
		
		/* Add a click handler for the delete row */
	    $('#btn_delete').click( function() {
	    	if(flag == "3"){
	    		  alert("Read-Only, permission denied!");
	    		  return;
	    	  }
	        var anSelected = fnGetSelected( pTable );
	        if(anSelected.length == 0){
	        	alert("Please choose template!");
	        	return;
	        }
	        var proid = anSelected[0].firstChild.textContent;
	        if(proid <5){
	        	alert("Default template can not delete!");
	        	return;
	        }
	        var datastring = '{"proid":"'+proid+'","user":"'+user+'"}';
	        socket.emit('profile_del',datastring);
	        
	    } );    	
	    
	    $('#pro_detail').live('click', function() {
	        var anSelected = $(this);	        
	        var proid = anSelected[0].parentNode.parentElement.cells[0].textContent;

	        socket.emit('profile_detail',proid);
	        
	    } ); 
	    
	    $("#btn_create").click( function() { 
	    	if(flag == "3"){
	    		  alert("Read-Only, permission denied!");
	    		  return;
	    	  }
	    	$( "#dialog:ui-dialog" ).dialog( "destroy" );	
	    	$( "#proname" )[0].value = "";
			$( "#vlan0id" )[0].value = 0;
			$( "#vlan1id" )[0].value = 0;
			$( "#vlan2id" )[0].value = 0;
			$( "#vlan3id" )[0].value = 0;
			$( "#cpuportrxrate" )[0].value = 0;
			$( "#port0txrate" )[0].value = 0;
			$( "#port1txrate" )[0].value = 0;
			$( "#port2txrate" )[0].value = 0;
			$( "#port3txrate" )[0].value = 0;
			$( "#cpuporttxrate" )[0].value = 0;
			$( "#port0rxrate" )[0].value = 0;
			$( "#port1rxrate" )[0].value = 0;
			$( "#port2rxrate" )[0].value = 0;
			$( "#port3rxrate" )[0].value = 0;
			
			$("#dialog-form").dialog({
				autoOpen: false,
				resizable: false,
				show: "blind",
				hide: "explode",
				modal: true,
				height: 550,
				width: 700,
				buttons: {
					"Save": function() {
						var bValid = true;
						allFields.removeClass( "ui-state-error" );
						//验证
						if(proname.val() == ""){
							proname.addClass( "ui-state-error" );
							updateTips( "Template name can not be null! ");
							bValid = false;
						}
						bValid = bValid && checknum(cpuportrxrate,"Down global rate-limit")&& checknum(port0txrate,"ETH1 rate-limit")&& checknum(port1txrate,"ETH2 rate-limit")&& checknum(port2txrate,"ETH3 rate-limit")&& checknum(port3txrate,"ETH4 rate-limit")
							&& checknum(cpuporttxrate,"up global rate-limit")&& checknum(port0rxrate,"ETH1 rate-limit")&& checknum(port1rxrate,"ETH2 rate-limit")&& checknum(port2rxrate,"ETH3 rate-limit")&& checknum(port3rxrate,"ETH4 rate-limit"); 
							
						bValid = bValid&& checkvalue(vlanen,"Vlan en",1,2)&& checkvalue(vlan0id,"vlan0id",0,4095)&& checkvalue(vlan1id,"vlan1id",0,4095)
						&& checkvalue(vlan2id,"vlan2id",0,4095)&& checkvalue(vlan3id,"vlan3id",0,4095)&& checkvalue(rxlimitsts,"rxlimitsts",1,2)&& checkvalue(txlimitsts,"txlimitsts",1,2);
						
						if(bValid){
							var datastring = '{"proname":"'+proname.val()+'","authorization":"'+parseInt(authorization.val(),10)+'","vlanen":"'+vlanen.val()+
							'","vlan0id":"'+parseInt(vlan0id.val(),10)+'","vlan1id":"'+parseInt(vlan1id.val(),10)+'","vlan2id":"'+parseInt(vlan2id.val())+'","vlan3id":"'+parseInt(vlan3id.val(),10)+
							'","rxlimitsts":"'+parseInt(rxlimitsts.val(),10)+'","cpuportrxrate":"'+parseInt(cpuportrxrate.val(),10)+'","port0txrate":"'+parseInt(port0txrate.val(),10)+
							'","port1txrate":"'+parseInt(port1txrate.val(),10)+'","port2txrate":"'+parseInt(port2txrate.val(),10)+'","port3txrate":"'+parseInt(port3txrate.val(),10)+
							'","txlimitsts":"'+parseInt(txlimitsts.val(),10)+'","cpuporttxrate":"'+parseInt(cpuporttxrate.val(),10)+'","port0rxrate":"'+parseInt(port0rxrate.val(),10)+
							'","port1rxrate":"'+parseInt(port1rxrate.val(),10)+'","port2rxrate":"'+parseInt(port2rxrate.val(),10)+'","port3rxrate":"'+parseInt(port3rxrate.val(),10)+'","user":"'+user+'"}';
							
							socket.emit('profile_create',datastring);
							$("#dialog-form").dialog("close");						
							window.location.reload();
					}},
					"Cancel": function() {
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
	    	if(flag == "3"){
	    		  alert("Read-Only, permission denied!");
	    		  return;
	    	  }
	    	var anSelected = fnGetSelected( pTable );
	    	if(anSelected.length == 0){
	        	alert("Please choose template!");
	        	return;
	        }
	        var proid = anSelected[0].firstChild.textContent;
	        if(proid <5){
	        	alert("Default template can not modify!");
	        	return;
	        }
	        socket.emit('profile_isedit',proid);
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
			updateTips( n + "must be numberal!" );
			return false;
		}
		if(o.val()> max || o.val()<min)
		{
			o.addClass( "ui-state-error" );
			updateTips( n+"value must in the range from"+min + "to" + max );
			return false;
		}else{
			return true;
		}
	}
	
	function checknum( o, n){
		if(o.val()==""){
			o[0].value = 0;
		}
		if(isNaN(o.val()))
		{
			o.addClass( "ui-state-error" );
			updateTips( n + "must be numberal!");
			return false;
		}else{
			if(o.val()>102400 || o.val()<0){
				o.addClass( "ui-state-error" );
				updateTips( n + "value must between 0~102400!");
				return false;
			}
			return true;
		}
	}
	
	/* Get the rows which are currently selected */
	function fnGetSelected( oTableLocal )
	{
	    return oTableLocal.$('tr.row_selected');
	}
	
	function fun_Detail(tmpdata){
		if(tmpdata != ""){
			$( "#dialog:ui-dialog" ).dialog( "destroy" );
			if(tmpdata.vlanen=="1"){
				tmpdata.vlanen = "enable";
			}else{
				tmpdata.vlanen = "disable";
			}
			if(tmpdata.rxlimitsts=="1"){
				tmpdata.rxlimitsts = "enable";
			}else{
				tmpdata.rxlimitsts = "disable";
			}
			if(tmpdata.txlimitsts=="1"){
				tmpdata.txlimitsts = "enable";
			}else{
				tmpdata.txlimitsts = "disable";
			}
			if(tmpdata.authorization=="1"){
				tmpdata.authorization = "enable";
			}else{
				tmpdata.authorization = "disable";
			}
			//组装弹出窗口html	
			$("#dialog-detail").empty();
			$("#dialog-detail").append('<fieldset>'+
					'<legend>Basic configuration</legend>'+
					'<form>'+
						'<table>'+
							'<tr><td><label for="proname_e" >Template name:</label></td>'+
								'<td><label>'+tmpdata.proname+'</label></td>'+
								'<td><label for="authorization_e">&nbsp &nbsp &nbsp &nbsp Authorization:</label></td>'+
								'<td><label>'+tmpdata.authorization+
									'</td></tr>'+
							'<tr><td><label for="vlanen_e">Vlan_En:</label></td>'+
							'<td><label>'+tmpdata.vlanen+'</td></tr>'+
							'<tr>'+
							'<td><label for="vlan0id_e">Vlan0id:</label></td>'+
							'<td><label>'+tmpdata.vlan0id+ ' </label></td>'+
							'<td><label for="vlan1id_e">&nbsp &nbsp &nbsp &nbsp Vlan1id:</label></td>'+
							'<td><label>'+tmpdata.vlan1id+ '</label></td></tr>'+
							'<tr><td><label for="vlan2id_e">Vlan2id:</label></td>'+
							'<td><label>'+tmpdata.vlan2id+ '</label></td>'+
							'<td><label for="vlan3id_e">&nbsp &nbsp &nbsp &nbsp Vlan3id:</label></td>'+
							'<td><label>'+tmpdata.vlan3id+ '</label></td></tr>'+
						'</table>'+
					'</form></fieldset>'+
					'<fieldset><legend>Downstream configuration (KB)</legend>'+
					'<form><table>'+
						'<tr><td><label for="rxlimitsts_e">Down rate-limit en:</label></td>'+
						'<td><label>'+tmpdata.rxlimitsts+
						'</td>'+
						'<td><label for="cpuportrxrate_e">&nbsp &nbsp &nbsp &nbsp Down global rate-limit:</label></td>'+
						'<td><label >'+tmpdata.cpuportrxrate+ '</label></td></tr>'+
						'<tr><td><label for="port0txrate_e">ETH1 rate-limit:</label></td>'+
						'<td><label>'+tmpdata.port0txrate+ '</label></td>'+
						'<td><label for="port1txrate_e">&nbsp &nbsp &nbsp &nbsp ETH2 rate-limit:</label></td>'+
						'<td><label>'+tmpdata.port1txrate+ '</label></td></tr>'+
						'<tr><td><label for="port2txrate_e">ETH3 rate-limit:</label></td>'+
						'<td><label>'+tmpdata.port2txrate+ '</label></td>'+
						'<td><label for="port3txrate_e">&nbsp &nbsp &nbsp &nbsp ETH4 rate-limit:</label></td>'+
						'<td><label>'+tmpdata.port3txrate+ '</label></td></tr>'+
					'</table></form></fieldset>'+
					'<fieldset><legend>Upstream configuration (KB)</legend>'+
					'<form><table>'+
						'<tr><td><label for="txlimitsts_e">Up rate-limit en:</label></td>'+
						'<td><label>'+tmpdata.txlimitsts+						
						'</td>'+
						'<td><label for="cpuporttxrate_e">&nbsp &nbsp &nbsp &nbsp Up global rate-limit:</label></td>'+
						'<td><label>'+tmpdata.cpuporttxrate+ '</label></td></tr>'+
						'<tr><td><label for="port0rxrate_e">ETH1 rate-limit:</label></td>'+
						'<td><label>'+tmpdata.port0rxrate+ '</label></td>'+
						'<td><label for="port1txrate_e">&nbsp &nbsp &nbsp &nbsp ETH2 rate-limit:</label></td>'+
						'<td><label>'+tmpdata.port1rxrate+ '</label></td></tr>'+
						'<tr><td><label for="port2rxrate_e">ETH3 rate-limit:</label></td>'+
						'<td><label>'+tmpdata.port2rxrate+ '</label></td>'+
						'<td><label for="port3txrate_e">&nbsp &nbsp &nbsp &nbsp ETH4 rate-limit:</label></td>'+
						'<td><label>'+tmpdata.port3rxrate+ '</label></td></tr>'+
					'</table></form></fieldset>');
				
				
				//弹出窗口
				$("#dialog-detail").dialog({
				autoOpen: false,
				resizable: false,
				show: "blind",
				hide: "explode",
				modal: true,
				height: 500,
				width: 600,
				buttons: {					
					"Confirm": function() {
						$( "#dialog-detail" ).dialog("close");						
					}
				},
				close: function() {

				}
			});	
		
			$("#dialog-detail").dialog("open");
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
	
	function fun_Editprofile(tmpdata){
		if(tmpdata != ""){
			$( "#dialog:ui-dialog" ).dialog( "destroy" );
			
			var proname = $( "#proname" ),
			authorization = $( "#authorization" ),
			vlanen = $( "#vlanen" ),
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
			
			
			allFields = $( [] ).add( proname ).add( authorization ).add( vlanen ).add( vlan0id ).add( vlan1id ).add( vlan2id )
			.add( vlan3id ).add( rxlimitsts ).add( cpuportrxrate ).add( port0txrate ).add( port1txrate ).add( port2txrate )
			.add( port3txrate ).add( txlimitsts ).add( cpuporttxrate ).add( port0rxrate ).add( port1rxrate ).add( port2rxrate )
			.add( port3rxrate ),
			
			tips = $( ".validateTips" );
			//组装弹出窗口html	
			$("#dialog-edit").empty();
			$("#dialog-edit").append('<p class="validateTips">Every option can not be null. </p>'+	
				'<fieldset>'+
					'<legend>Basic configuration</legend>'+
					'<form>'+
						'<table>'+
							'<tr><td><label for="proname_e" >Template name:</label></td>'+
								'<td><input type="text" name="proname_e" id="proname_e" value='+tmpdata.proname+' class="text ui-widget-content ui-corner-all" /></td>'+
								'<td><label for="authorization_e">Authorization:</label></td>'+
								'<td><select name="authorization_e" id="authorization_e">'+
									'<option value="1">enable</option>'+
									'<option value="2">disable</option>'+
								'</select></td>	</tr>'+
							'<tr><td><label for="vlanen_e">Vlan_En:</label></td>'+
								'<td><select name="vlanen_e" id="vlanen_e">'+
									'<option value="1">enable</option>'+
									'<option value="2">disable</option>'+
								'</select></td></tr>'+
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
					'<fieldset><legend>Downstream configuration (KB)</legend>'+
					'<form><table>'+
						'<tr><td><label for="rxlimitsts_e">Down rate-limit en:</label></td>'+
						'<td><select name="rxlimitsts_e" id="rxlimitsts_e">'+
						'<option value="1">enable</option><option value="2">disable</option>'+
						'</select></td>'+
						'<td><label for="cpuportrxrate_e">Down global rate-limit:</label></td>'+
						'<td><input type="text" name="cpuportrxrate_e" id="cpuportrxrate_e" value='+tmpdata.cpuportrxrate+ ' class="text ui-widget-content ui-corner-all" /></td></tr>'+
						'<tr><td><label for="port0txrate_e">ETH1 rate-limit:</label></td>'+
						'<td><input type="text" name="port0txrate_e" id="port0txrate_e" value='+tmpdata.port0txrate+ ' class="text ui-widget-content ui-corner-all" /></td>'+
						'<td><label for="port1txrate_e">ETH2 rate-limit:</label></td>'+
						'<td><input type="text" name="port1txrate_e" id="port1txrate_e" value='+tmpdata.port1txrate+ ' class="text ui-widget-content ui-corner-all" /></td></tr>'+
						'<tr><td><label for="port2txrate_e">ETH3 rate-limit:</label></td>'+
						'<td><input type="text" name="port2txrate_e" id="port2txrate_e" value='+tmpdata.port2txrate+ ' class="text ui-widget-content ui-corner-all" /></td>'+
						'<td><label for="port3txrate_e">ETH4 rate-limit:</label></td>'+
						'<td><input type="text" name="port3txrate_e" id="port3txrate_e" value='+tmpdata.port3txrate+ ' class="text ui-widget-content ui-corner-all" /></td></tr>'+
					'</table></form></fieldset>'+
					'<fieldset><legend>Upwnstream configuration (KB)</legend>'+
					'<form><table>'+
						'<tr><td><label for="txlimitsts_e">Up rate-limit en:</label></td>'+
						'<td><select name="txlimitsts_e" id="txlimitsts_e">'+
						'<option value="1">enable</option><option value="2">disable</option>'+
						'</select></td>'+
						'<td><label for="cpuporttxrate_e">Up global rate-limit:</label></td>'+
						'<td><input type="text" name="cpuporttxrate_e" id="cpuporttxrate_e" value='+tmpdata.cpuporttxrate+ ' class="text ui-widget-content ui-corner-all" /></td></tr>'+
						'<tr><td><label for="port0rxrate_e">ETH1 rate-limit:</label></td>'+
						'<td><input type="text" name="port0rxrate_e" id="port0rxrate_e" value='+tmpdata.port0rxrate+ ' class="text ui-widget-content ui-corner-all" /></td>'+
						'<td><label for="port1txrate_e">ETH2 rate-limit:</label></td>'+
						'<td><input type="text" name="port1rxrate_e" id="port1rxrate_e" value='+tmpdata.port1rxrate+ ' class="text ui-widget-content ui-corner-all" /></td></tr>'+
						'<tr><td><label for="port2rxrate_e">ETH3 rate-limit:</label></td>'+
						'<td><input type="text" name="port2rxrate_e" id="port2rxrate_e" value='+tmpdata.port2rxrate+ ' class="text ui-widget-content ui-corner-all" /></td>'+
						'<td><label for="port3txrate_e">ETH4 rate-limit:</label></td>'+
						'<td><input type="text" name="port3rxrate_e" id="port3rxrate_e" value='+tmpdata.port3rxrate+ ' class="text ui-widget-content ui-corner-all" /></td></tr>'+
					'</table></form></fieldset>');
				document.getElementById('authorization_e').value = tmpdata.authorization;
				document.getElementById('vlanen_e').value = tmpdata.vlanen;
				document.getElementById('rxlimitsts_e').value = tmpdata.rxlimitsts;
				document.getElementById('txlimitsts_e').value = tmpdata.txlimitsts;
				
				proname = $( "#proname_e" ),
				authorization = $( "#authorization_e" ),
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
				width: 700,
				buttons: {
					"Save": function() {
						var bValid = true;
						allFields.removeClass( "ui-state-error" );
						//验证
						if(proname.val() == ""){
							proname.addClass( "ui-state-error" );
							updateTips( "Template name can not be null! ");
							bValid = false;
						}
						bValid = bValid && checknum(cpuportrxrate,"Down global rate-limit")&& checknum(port0txrate,"ETH1 rate-limit")&& checknum(port1txrate,"ETH2 rate-limit")&& checknum(port2txrate,"ETH3 rate-limit")&& checknum(port3txrate,"ETH4 rate-limit")
							&& checknum(cpuporttxrate,"Up global rate-limit")&& checknum(port0rxrate,"ETH1 rate-limit")&& checknum(port1rxrate,"ETH2 rate-limit")&& checknum(port2rxrate,"ETH3 rate-limit")&& checknum(port3rxrate,"ETH4 rate-limit"); 
							
						bValid = bValid&& checkvalue(vlanen,"Vlan en",1,2)&& checkvalue(vlan0id,"vlan0id",0,4095)&& checkvalue(vlan1id,"vlan1id",0,4095)
						&& checkvalue(vlan2id,"vlan2id",0,4095)&& checkvalue(vlan3id,"vlan3id",0,4095)&& checkvalue(rxlimitsts,"rxlimitsts",1,2)&& checkvalue(txlimitsts,"txlimitsts",1,2);
						
						if(bValid){
							var anSelected = fnGetSelected( pTable );
							var datastring = '{"proid":"'+anSelected[0].firstChild.textContent+'","proname":"'+proname.val()+'","authorization":"'+parseInt(authorization.val(),10)+'","vlanen":"'+vlanen.val()+
							'","vlan0id":"'+parseInt(vlan0id.val(),10)+'","vlan1id":"'+parseInt(vlan1id.val(),10)+'","vlan2id":"'+parseInt(vlan2id.val(),10)+'","vlan3id":"'+parseInt(vlan3id.val(),10)+
							'","rxlimitsts":"'+parseInt(rxlimitsts.val(),10)+'","cpuportrxrate":"'+parseInt(cpuportrxrate.val(),10)+'","port0txrate":"'+parseInt(port0txrate.val(),10)+
							'","port1txrate":"'+parseInt(port1txrate.val(),10)+'","port2txrate":"'+parseInt(port2txrate.val(),10)+'","port3txrate":"'+parseInt(port3txrate.val(),10)+
							'","txlimitsts":"'+parseInt(txlimitsts.val(),10)+'","cpuporttxrate":"'+parseInt(cpuporttxrate.val(),10)+'","port0rxrate":"'+parseInt(port0rxrate.val(),10)+
							'","port1rxrate":"'+parseInt(port1rxrate.val(),10)+'","port2rxrate":"'+parseInt(port2rxrate.val(),10)+'","port3rxrate":"'+parseInt(port3rxrate.val(),10)+'","user":"'+user+'"}';
							socket.emit('profile_edit',datastring);
							$("#dialog-edit").dialog("close");
							pTable=$('#proTable').dataTable();

							window.location.reload();
					}},
					"Cancel": function() {
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
				var item = [itemv.id,itemv.proname,itemv.vlanen,
				itemv.rxlimitsts,itemv.cpuportrxrate,
				itemv.txlimitsts,itemv.cpuporttxrate,itemv.id];
				groupval[groupval.length] = item; 				
								
		 	}); 
		 	
		 	pTable = $('#proTable').dataTable( {
			"bFilter": false,						//不使用过滤功能
			"bLengthChange": false,					//用户不可改变每页显示数量
			"iDisplayLength": 10,					//每页显示10条数据
			"aaData": groupval,
    		"bInfo": false,	
	        "sPaginationType": "full_numbers",
	        "oLanguage": {							//汉化
				"sLengthMenu": "Display _MENU_ strip record per page",
				"sZeroRecords": "No data has been dicoveryed",
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
	            	$('td:eq(2)', nRow).html( 'enable' );				               
	            }else{
	            	$('td:eq(2)', nRow).html( 'disable' );
	            }      
	            if ( aData[3] == "1" )
	            {
	            	$('td:eq(3)', nRow).html( 'enable' );				               
	            }else{
	            	$('td:eq(3)', nRow).html( 'disable' );
	            } 
	            if ( aData[5] == "1" )
	            {
	            	$('td:eq(5)', nRow).html( 'enable' );				               
	            }else{
	            	$('td:eq(5)', nRow).html( 'disable' );
	            }
	            if(aData[7] != ""){
	            	$('td:eq(7)', nRow).html( '<button id="pro_detail">view</button>' );
	            }
	            
	        },		
			"aoColumns": [							//设定各列宽度
			              { "sTitle": "ID" ,"sWidth":"50px"},
						  { "sTitle": "Template name" },
					      { "sTitle": "vlan en" },
					      { "sTitle": "Down rate-limit en" },
					      { "sTitle": "Down global rate-limit" },
					      { "sTitle": "Up rate-limit en" },
					      { "sTitle": "Up global rate-limit" },
					      { "sTitle": "Detail information" }
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
