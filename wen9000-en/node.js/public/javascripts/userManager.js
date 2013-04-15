(function($){
	 var username;
	 var password;
	 var flag;
	 var user;
	$(function(){
		user = localStorage.getItem('username');
		 socket = io.connect('http://localhost:3000');
		 
		 username = localStorage.getItem('username');//getCookie("userName");
		 
		 socket.emit('userinfo',username);
		 
		 socket.on('userinfo',fun_userinfo);
		 socket.on('pwdmodify',fun_pwdmodify);
		 socket.on('userlist',fun_userlist);
		 socket.on('userres',fun_userres);
		 
		 $("#s_select").live('change',function(){
			 var xx = $(this);
			 var value = $(this)[0].value;
			 var cname = $(this)[0].parentNode.parentElement.firstChild.textContent;
			 var datastring = '{"username":"'+cname+'","flag":"'+value+'","user":"'+user+'"}';
			 var ccccc = $(this)[0].parentNode.nextSibling.firstChild;
			 if(parseInt(flag)< parseInt(value)){
				 var attr = $(this)[0].parentNode.nextSibling.firstChild.attributes.getNamedItem("disabled");
				 if(attr != null){
					 $(this)[0].parentNode.nextSibling.firstChild.attributes.removeNamedItem("disabled");
				 }
				 
			 }else{
				 var attr = document.createAttribute ("disabled");
		         attr.value = "disable";
				 $(this)[0].parentNode.nextSibling.firstChild.attributes.setNamedItem(attr);
			 }
			 socket.emit('PermissionChange',datastring);
		 });
		 
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
						"Save": function() {
							var name = $("#name")[0].value;
							var password = $("#password")[0].value;
							var flag = $("#flag")[0].options[$("#flag")[0].options.selectedIndex].value;
							var datastring = '{"username":"'+name+'","password":"'+password+'","flag":"'+flag+'","user":"'+user+'"}';
							socket.emit('usercreate',datastring);
							$( this ).dialog("close");
						},
						"Cancel": function() {
							$( this ).dialog("close");
						}
					}
				});
				$("#dialog-user-create").dialog("open");		 
			 
		 });
		 
		 $("#btn_del").live('click',function(){
			 var name = $(this)[0].parentNode.parentElement.firstChild.textContent;
			 var datastring = '{"username":"'+name+'","user":"'+user+'"}';
			 socket.emit('userdel',datastring);			 
			 
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
								alert("Input old password error!");
								return;
							}
							$("#old_pwd").css("");
							var newpwd = document.getElementById("new_pwd").value;
							var reppwd = document.getElementById("rep_pwd").value;
							if(newpwd != reppwd){
								$("#new_pwd").css("border","2px solid red");
								$("#rep_pwd").css("border","2px solid red");
								alert("The two passwords you typed do not match!");
								return;
							}
							password = newpwd;
							document.cookie ="password="+password;
							$("#new_pwd").css("");
							$("#rep_pwd").css("");
							var datastring = '{"username":"'+username+'","password":"'+newpwd+'","user":"'+user+'"}';
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
			alert("Create failed, user already exist!");
		}
		//用户局部刷新
		socket.emit('userinfo',username);
	}
	
	function fun_userlist(data){
		$( '<ul class="userHeads" style="width:400px;list-style:none">' +
        		'<li style="width:100px;float:left;text-align:center;background-color:#ccc"> User Name </li>' +
        		'<li style="width:80px;float:left;text-align:center;background-color:#ccc"> Level </li>' +
                '<li style="width:150px;float:left;text-align:center;background-color:#ccc">Delete</li>' +
                '</ul>').prependTo('#userheads');
		$.each(data, function(key, itemv) {  
			if(username == itemv.username){
				return;
			}
			if(flag == "0"){
				$( '<ul class="usersinfos" style="width:400px;list-style:none;margin-top:5px">' +
	            		'<li style="width:100px;text-align:center;float:left"> <lable>'+itemv.username+'</lable></li>' +
	            		'<li style="width:80px;text-align:center;float:left"> <select id="s_select">'+
	                    '<option value = "1">Administrator</option><option value = "2">Normal</option><option value = "3">ReadOnly</option></select> </li>' +
	                    '<li style="width:150px;text-align:center;float:left"><button id="btn_del">Delete</button></li>' +
	                    '</ul>').prependTo('#userlists');
				if(itemv.flag == "1"){						
					document.getElementById('s_select').value = 1;
				}else if(itemv.flag == "2"){
					document.getElementById('s_select').value = 2;
				}else if(itemv.flag == "3"){
					document.getElementById('s_select').value = 3;
				}
			}else if(flag == "1"){
				$( '<ul class="usersinfos" style="width:400px;list-style:none;margin-top:5px">' +
	            		'<li style="width:100px;text-align:center;float:left"> <lable>'+itemv.username+'</lable></li>' +
	            		'<li style="width:80px;text-align:center;float:left"> <select id="s_select">'+
	                    '<option value = "1">Administrator</option><option value = "2">Normal</option><option value = "3">ReadOnly</option></select> </li>' +
	                    '<li style="width:150px;text-align:center;float:left"><button id="btn_del">Delete</button></li>' +
	                    '</ul>').prependTo('#userlists');
				if(itemv.flag == "1"){						
					document.getElementById('s_select').value = 1;
					$("#btn_del").attr("disabled","disable");
				}else if(itemv.flag == "2"){
					document.getElementById('s_select').value = 2;
				}else if(itemv.flag == "3"){
					document.getElementById('s_select').value = 3;
				}
			}else{
				$( '<ul class="usersinfos" style="width:400px;list-style:none;margin-top:5px">' +
	            		'<li style="width:100px;text-align:center;float:left"> <lable>'+itemv.username+'</lable></li>' +
	            		'<li style="width:80px;text-align:center;float:left">  <lable>'+itemv.flag+'</lable> </li>' +
	                    '<li style="width:150px;text-align:center;float:left"><button id="btn_del">Delete</button></li>' +
	                    '</ul>').prependTo('#userlists');
				if(itemv.flag == "1"){
					itemv.flag = "Administrator";							
					$("#btn_del").attr("disabled","disable");
				}else if(itemv.flag == "2"){
					itemv.flag = "Normal";
					$("#btn_del").attr("disabled","disable");
				}else if(itemv.flag == "3"){
					itemv.flag = "ReadOnly";
					$("#btn_del").attr("disabled","disable");
				}
			}
		
		 });
	}
	
	function fun_pwdmodify(data){
		if(data == ""){
			alert("Modify fail!!");
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
		flag = data.flag;
		password = data.password;
		$("#usercontent").empty();
		if(data.flag == "1"){			 
			//管理员			
			$("#usercontent").append('<h3>Hi '+username+'!,you are the Administrator!</h3>'+
					'<p style="margin-left:30px">As an administrator, you can use the NMS normally, and also can manage other normal users. '+
					'e.g. Can delete normal user, add new user, etc.<br/>If need to modify the password, please click<button id="btn_modify">Modify</button>.<br/>'+
					'If you want to create a new user, please click<button id="btn_create">Create</button>.</p>'+
					'<div id="userheads"></div>'+
					'<div id="userlists"></div>');
			socket.emit('userlist',"");			
			
		}else if(data.flag == "0"){
			//超级管理员
			$("#usercontent").append('<h3>Hi '+username+'!,you are the Super Administrator！</h3>'+
					'<p style="margin-left:30px">As an administrator, you can use the NMS normally, and also can manage other normal users. '+
					'e.g. Can delete normal user, add new user, etc.<br/>If need to modify the password, please click<button id="btn_modify">Modify</button>.<br/>'+
					'If you want to create a new user, please click<button id="btn_create">Create</button>.</p>'+
					'<div id="userheads"></div>'+
					'<div id="userlists"></div>');
			socket.emit('userlist',"");			
		}else if(data.flag == "2"){
			//一般用户
			$("#usercontent").append('<h3>Hi '+username+'!,you are the normal user!</h3>'+
					'<p style="margin-left:30px">Normal user can only do the normal operation, and use the NMS normally.'+
					'Can not manage other users. If you need to modify your password, please click<button id="btn_modify">Modify</button></p>');
		}else if(data.flag == "3"){
			//只读用户
			$("#usercontent").append('<h3>Hi '+username+'!, you are the Read-Only User!</h3>'+
					'<p style="margin-left:30px">Read-Only User only can read the information of the NMS, can not operate on the NMS.'+
					'Can not manage other users.If you need to modify your password, please click<button id="btn_modify">Modify</button></p>');
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