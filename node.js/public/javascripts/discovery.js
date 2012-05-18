(function($){
	$(function(){
		socket = io.connect('http://192.168.1.249:3000');
		
		socket.on('dis.validate',fun_Validate);
		
		$("#submit").click(function(){
			var sip = document.getElementById('startip').value;
			var enip = document.getElementById('stopip').value
			var exp=/^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
			var isip = sip.match(exp)&&enip.match(exp);
			if(!isip){
				$( "#dialog:ui-dialog" ).dialog( "destroy" );

				$( "#dialog-dis-warn" ).dialog({
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
				$("#dialog-dis-warn").dialog("open");
				return;
			}
			var datastring = '{"startip":"'+sip+'","stopip":"'+ enip+'"}';
			socket.emit('discovery.search',datastring);
		});
	});
	
	function fun_Validate(data){
		if(data == ""){
			$( "#dialog:ui-dialog" ).dialog( "destroy" );

			$( "#dialog-dis-warn" ).dialog({
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
			$("#dialog-dis-warn").dialog("open");
		}else{
			window.location.href="/dis/result"
		}
	}
})(jQuery);
