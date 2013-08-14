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
				    
				    '<label>b28569140ac871176e208718a4f299d6079051f9</label>'+
				    
				   '</td></tr>'+
				   '<tr><td><label>版本号:：</label></td><td>'+
				   
				    '<label>v1.1.18</label>'+
				    
				   '</td></tr>'+
				   '</td></tr>'+
				   '<tr><td><label>发布时间:：</label></td><td>'+
				   
				    '<label>2013-8-6</label>'+
				    
				   '</td></tr>'+
				   '</div>'
				);				
   }
////////////////////////function	
})(jQuery);
