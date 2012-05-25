(function($){
	$(function(){
		socket = io.connect('http://localhost:3000');
		
		socket.emit('opt.global_opt',"globalopt");
		
		socket.on('opt.globalopt',fun_GlobalOpt);
		socket.on('opt.globalsave',fun_GlobalSave);
		socket.on('opt.saveredis',fun_SaveRedis);
		
		$("#btn_gsub").click( function(){
    		var ip = $("#trap_server")[0].value;
    		var port = $("#trap_serport")[0].value;
    		var data = '{"ip":"'+ip+'","port":"'+port+'"}';
    		socket.emit('opt.save_global',data);
    	});
		
		$("#btn_gsave").click( function(){
    		socket.emit('opt.saveredis',"save_redis");
    	});
	});
	
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
})(jQuery);
