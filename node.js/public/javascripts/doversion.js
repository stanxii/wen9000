(function($) {
	
	$(function() {

		 $("#doversion").click(function(){			
			 onShowVersion();
	      });

	});
	
	//////////////function
		
	function onDestoryVersion(){		
			
		
		
	}
	
	function onShowVersion() {
		$("#content").html();
		$("#content").html(
				'<h1><label>版本信息</label></h1>'+
				'<div style="margin-left:30px" >'+
				   '<table>'+
				    '<tr><td><label>版本hash值：</label></td><td>'+
				    
				    '<label>0fbc97af450a93dec440a4a7e870460e4da07658</label>'+
				    
				   '</td></tr>'+
				   '<tr><td><label>版本号:：</label></td><td>'+
				   
				    '<label>v1.2.9</label>'+
				    
				   '</td></tr>'+
				   '</td></tr>'+
				   '<tr><td><label>发布时间:：</label></td><td>'+
				   
				    '<label>2014-7-16</label>'+
				    
				   '</td></tr>'+
				   '</div>'
				);				
   }
////////////////////////function	
})(jQuery);
