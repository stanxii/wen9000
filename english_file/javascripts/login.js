(function($){
	$(function(){
		socket = io.connect('http://localhost:3000');
		
		socket.on('getflag',fun_flag);
		Login_init();

		$("#btn_sub").hover(function(){
			setCookie();
		});
		
		$(':input').keyup(function(){
			setCookie();
		});
	});
	
	function fun_flag(data){
		document.cookie ="flag="+data;
	}
	
	function Login_init(){
		var init_name = getCookie("userName");
		if(init_name == undefined){
			init_name = "";
		}
		$("#userName")[0].value = init_name;
		if(getCookie("checked")=="true"){
			$("#remember")[0].checked = true;			
			$("#password")[0].value = getCookie("password");
		}
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


	function setCookie()//设置cookie
	{		
		if($("#remember")[0].checked){
			document.cookie ="checked=true";
			document.cookie ="userName="+document.getElementById ('userName').value;
			document.cookie ="password="+document.getElementById ('password').value;
		}else{
			document.cookie ="checked=false";
			document.cookie ="userName="+document.getElementById ('userName').value;
			document.cookie ="password=";
		}
		socket.emit('getflag',document.getElementById ('userName').value);
		
	}
})(jQuery);