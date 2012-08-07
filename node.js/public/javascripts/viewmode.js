(function($){
	$(function(){
		socket = io.connect('http://localhost:3000');
		
		socket.emit('Viewmodeget',"");
		socket.on('Viewmodeshow',fun_Viewmodeget);
		
		$("#viewmode").live('change',function(){
			 var value = $(this)[0].value;
			 
			 socket.emit('Viewmodechange',value);
		 });
	});
	
	function fun_Viewmodeget(data){
		document.getElementById('viewmode').value = data;
	}
})(jQuery);