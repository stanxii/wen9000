(function($){
	$(function(){
		var user = localStorage.getItem('username');
		socket = io.connect('http://localhost:3000');
		
		socket.emit('opt.global_opt',"globalopt");
		
		socket.on('opt.globalopt',fun_GlobalOpt);
		socket.on('opt.globalsave',fun_GlobalSave);
		socket.on('opt.saveredis',fun_SaveRedis);
		socket.on('opt.ImportHfcResult',fun_ImportHfcResult);
		var flag = getCookie("flag");
		$("#btn_gsub").click( function(){
			if(flag == "3"){
	    		  alert("只读用户，权限不足！");
	    		  return;
	    	  }
    		var ip = $("#trap_server")[0].value;
    		var port = $("#trap_serport")[0].value;
    		var data = '{"ip":"'+ip+'","port":"'+port+'","user":"'+user+'"}';
    		socket.emit('opt.save_global',data);
    	});
		
		$("#btn_gsave").click( function(){
			if(flag == "3"){
	    		  alert("只读用户，权限不足！");
	    		  return;
	    	  }
    		socket.emit('opt.saveredis',"save_redis");
    	});
		
		$("#btn_import").click( function(){
			if(flag == "3"){
	    		  alert("只读用户，权限不足！");
	    		  return;
	    	  }
			var data = '{"user":"'+user+'"}';
			socket.emit('opt.importhfcredis',data);
			$( "#dialog-message-proc" ).dialog({
				autoOpen: false,
				show: "blind",
				modal: true,
				resizable: false,
				hide: "explode"
			});
			$("#dialog-message-proc").dialog("open");
    	});
		
		$("#btn_tmpsub").click( function(){
			if(flag == "3"){
	    		  alert("只读用户，权限不足！");
	    		  return;
	    	  }
			var data = '{"val":"'+$("#tmpalarm")[0].value+'","user":"'+user+'"}';
			socket.emit('opt.alarmtmpset',data);
			
    	});
	});
	
	function fun_ImportHfcResult(data){
		$( "#dialog-message-proc" ).dialog("destroy");
		if(data == "0"){
			//failed
			$( "#dialog-message-failed" ).dialog({
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
  			$("#dialog-message-failed").dialog("open");
		}else{
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
	
	function fun_GlobalSave(data){
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
	
	function fun_GlobalOpt(data){
		if(data == ""){
			
		}else{
			$("#trap_server")[0].value = data.ip;
			$("#trap_serport")[0].value = data.port;
			$("#tmpalarm")[0].value = data.tmp;
		}
	}
	
	function fun_SaveRedis(data){
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
