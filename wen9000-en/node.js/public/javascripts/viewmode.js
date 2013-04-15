(function($){
	$(function(){
		socket = io.connect('http://localhost:3000');
		var user = localStorage.getItem('username');
		socket.emit('Viewmodeget',"");
		socket.emit('Devmodeget','{"value":"'+$("#devmode")[0].value+'","user":"'+user+'"}');
		socket.on('Viewmodeshow',fun_Viewmodeget);
		socket.on('Devmodeshow',fun_Devmodeshow);
		
		$("#viewmode").live('change',function(){
			 var value = $(this)[0].value;
			 var datastring = '{"value":"'+value+'","user":"'+user+'"}';
			 socket.emit('Viewmodechange',datastring);
		 });
		 
		 $("#devmode").live('change',function(){
			 var value = $(this)[0].value;
			 var datastring = '{"value":"'+value+'","user":"'+user+'"}';
			 socket.emit('Devmodechange',datastring);
		 });
		
		$("#btn_submit").click(function(){
			var devmode = $("#devmode")[0].value;
			var value = $("#dev_view")[0].value;
			var datastring = '{"devmode":"'+devmode+'","value":"'+value+'","user":"'+user+'"}';
			socket.emit('Devmodeset',datastring);
		});
		
	});
	
	function fun_Viewmodeget(data){
		document.getElementById('viewmode').value = data;
	}
	
	function fun_Devmodeshow(data){
		document.getElementById('dev_view').value = data;
	}
})(jQuery);