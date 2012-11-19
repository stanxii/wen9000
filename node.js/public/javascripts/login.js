(function($){
	$(function(){
		socket = io.connect('http://localhost:3000');
		if (!html5_storage_support) {
			  alert("您使用的浏览器版本过低，请升级浏览器！！");
		}
		socket.on('getflag',fun_flag);
		Login_init();

		$("#btn_sub").hover(function(){
			setStorage();
		});
		
		$(':input').keyup(function(){
			setStorage();
		});
		
		$("#remember").click(function(){
			if(localStorage.getItem('check')){
				localStorage.setItem('check','');
				//localStorage.setItem('password','');
				//$("#password")[0].value = "";
			}else{
				localStorage.setItem('check','true');
			}
		});
	});
	function html5_storage_support() {
		return !!window.localStorage;
	}
	
	function fun_flag(data){
		//document.cookie ="flag="+data;
		localStorage.setItem('flag',data);
	}
	
	function Login_init(){
		//OPEN AND OR CREATE THE DATABASE ON THE USERS MACHINE
		//db = openDatabase("Userdb", "1", "Login users", 1000);
		var username	= localStorage.getItem('username');
	    var password	= localStorage.getItem('password');
	    var check		= localStorage.getItem('check');
	    $("#userName")[0].value = username;
	    if(check){
	    	$("#remember")[0].checked = true;			
			$("#password")[0].value = password;
	    }else{
	    	$("#remember")[0].checked = false;			
			$("#password")[0].value = "";
	    }
//		var init_name = getCookie("userName");
//		if(init_name == undefined){
//			init_name = "";
//		}
//		$("#userName")[0].value = init_name;
//		if((getCookie("checked")=="true")&&(init_name != "")){
//			$("#remember")[0].checked = true;			
//			$("#password")[0].value = getCookie("password");
//		}
	}
	
	function getCookie(objName)//获取指定名称的cookie的值
	{    
	    var arrStr = document.cookie.split(";");
	    
	        for(var i = 0;i < arrStr.length;i++)
	            {
	                var temp = arrStr[i].split("=");
	                if(objName.trim()==temp[0].trim()) 
	                {                
	                	return temp[1];
	                }                            
	            }
	}


	function setStorage()//设置localStorage
	{		
		if(localStorage.getItem('check')){
			localStorage.setItem('username',document.getElementById ('userName').value);
			localStorage.setItem('password',document.getElementById ('password').value);
			localStorage.setItem('check','true');
		}else{
			localStorage.setItem('username',document.getElementById ('userName').value);
			localStorage.setItem('password','');
			localStorage.setItem('check','');
		}
//		if($("#remember")[0].checked){
//			document.cookie ="checked=true";
//			document.cookie ="userName="+document.getElementById ('userName').value;
//			document.cookie ="password="+document.getElementById ('password').value;
//		}else{
//			document.cookie ="checked=false";
//			document.cookie ="userName="+document.getElementById ('userName').value;
//			document.cookie ="password=";
//		}
		socket.emit('getflag',document.getElementById ('userName').value);
		
	}
})(jQuery);