(function($){
	 var username;
	 var password;
	$(function(){
		 socket = io.connect('http://localhost:3000');
		 
		 username = getCookie("userName");
		 
		 socket.emit('userinfo',username);
		 
		 socket.on('userinfo',fun_userinfo);
		 socket.on('pwdmodify',fun_pwdmodify);
		 socket.on('userlist',fun_userlist);
		 socket.on('userres',fun_userres);
		 
		 $("#btn_create").live('click',function(){
			 $( "#dialog:ui-dialog" ).dialog( "destroy" );
			 $("#name")[0].value = "";
			 $("#password")[0].value = "";
				$( "#dialog-user-create" ).dialog({
					autoOpen: false,
					show: "blind",
					modal: true,
					resizable: false,
					hide: "explode",
					buttons: {
						"保存": function() {
							var name = $("#name")[0].value;
							var password = $("#password")[0].value;
							var datastring = '{"username":"'+name+'","password":"'+password+'"}';
							socket.emit('usercreate',datastring);
							$( this ).dialog("close");
						},
						"取消": function() {
							$( this ).dialog("close");
						}
					}
				});
				$("#dialog-user-create").dialog("open");		 
			 
		 });
		 
		 $("#btn_del").live('click',function(){
			 var name = $(this)[0].parentNode.parentElement.firstChild.textContent;
			 socket.emit('userdel',name);			 
			 
		 });
		 
		 $("#btn_modify").live('click',function(){
			 document.getElementById("old_pwd").value = "";
			 document.getElementById("new_pwd").value = "";
			 document.getElementById("rep_pwd").value = "";
			 $( "#pwdmodify" ).dialog({
					autoOpen: false,
					show: "blind",
					modal: true,
					resizable: false,
					hide: "explode",
					buttons: {
						Ok: function() {
							var oldpwd = document.getElementById("old_pwd").value;
							if(oldpwd != password){
								$("#old_pwd").css("border","2px solid red");
								alert("旧密码输入错误！");
								return;
							}
							$("#old_pwd").css("");
							var newpwd = document.getElementById("new_pwd").value;
							var reppwd = document.getElementById("rep_pwd").value;
							if(newpwd != reppwd){
								$("#new_pwd").css("border","2px solid red");
								$("#rep_pwd").css("border","2px solid red");
								alert("两次输入的密码不一致！");
								return;
							}
							$("#new_pwd").css("");
							$("#rep_pwd").css("");
							var datastring = '{"username":"'+username+'","password":"'+newpwd+'"}';
							socket.emit('pwd_modify',datastring);
							$( this ).dialog( "close" );
						}
					}
				});
				$("#pwdmodify").dialog("open");
		 });
	});
	
	function fun_userres(data){
		if(data == "2"){
			alert("创建失败，用户已存在！");
		}
		//用户局部刷新
		socket.emit('userinfo',username);
	}
	
	function fun_userlist(data){
		$( '<ul class="userHeads" style="width:400px;list-style:none">' +
        		'<li style="width:100px;float:left;text-align:center;background-color:#ccc"> 用户名 </li>' +
        		'<li style="width:80px;float:left;text-align:center;background-color:#ccc"> 用户等级 </li>' +
                '<li style="width:150px;float:left;text-align:center;background-color:#ccc">管理</li>' +
                '</ul>').prependTo('#userheads');
		$.each(data, function(key, itemv) {  
				if(itemv.flag == "1"){
					itemv.flag = "管理员";
				}else{
					itemv.flag = "一般用户";
				}
				$( '<ul class="usersinfos" style="width:400px;list-style:none;margin-top:5px">' +
	            		'<li style="width:100px;text-align:center;float:left"> <lable>'+itemv.username+'</lable></li>' +
	            		'<li style="width:80px;text-align:center;float:left">  <lable>'+itemv.flag+'</lable> </li>' +
	                    '<li style="width:150px;text-align:center;float:left"><button id="btn_del">删除</button></li>' +
	                    '</ul>').prependTo('#userlists');		
		 	});
	}
	
	function fun_pwdmodify(data){
		if(data == ""){
			alert("修改失败!!");
		}else{
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
	}
	
	function fun_userinfo(data){	
		$("#usercontent").empty();
		if(data.flag == "1"){			 
			//超级用户
			$("#usercontent").append('<h3>您好'+username+'!,您是管理员！</h3>'+
					'<p style="margin-left:30px">管理员的权限除了能对网管系统的正常使用外，还能对其它一般用户进行管理操作,'+
					'例如可以删除一般用户，添加新用户等。<br/>如需修改您的登录密码请点击<button id="btn_modify">修改密码</button>.<br/>'+
					'如果要创建新用户，请点击<button id="btn_create">创建用户</button>.</p>'+
					'<div id="userheads"></div>'+
					'<div id="userlists"></div>');
			socket.emit('userlist',"");			
			
		}else{
			//一般用户
			password = data.password;
			$("#usercontent").append('<h3>您好'+username+'!,您是一般用户！</h3>'+
					'<p style="margin-left:30px">一般用户的权限仅限于对网管系统的正常使用，和对用户本身的常规操作,'+
					'不能对其它用户进行管理。如需修改您的登录密码请点击<button id="btn_modify">这里</button></p>');
		}
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
})(jQuery);