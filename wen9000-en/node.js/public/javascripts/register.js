(function($){
	$(function(){
		socket = io.connect('http://localhost:3000');
        
		//socket.on('register_err',fun_err);

		var username = $("#name");
		if(username[0].textLength==0){
			username.css("border","2px solid red");
		}		
		
		username.keyup(function(){
			if(this.textLength>0){
				$("#btn_sub").removeAttr("disabled");
				username.css("border","");
			}else{
				$("#btn_sub").attr("disabled","disable");
				username.css("border","2px solid red");
			}
		});
		
	});
	
	function fun_err(data){
		alert("Please input username!");
	}
})(jQuery);