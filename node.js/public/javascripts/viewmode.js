(function($){
	$(function(){
		var user = localStorage.getItem('username');
		socket = io.connect('http://localhost:3000');
		
		socket.emit('Viewmodeget',"");
		socket.on('Viewmodeshow',fun_Viewmodeget);
		
		$("#viewmode").live('change',function(){
			 var value = $(this)[0].value;
			 var datastring = '{"value":"'+value+'","user":"'+user+'"}';
			 socket.emit('Viewmodechange',datastring);
		 });
	});
	
	function fun_Viewmodeget(data){
		document.getElementById('viewmode').value = data;
	}
})(jQuery);