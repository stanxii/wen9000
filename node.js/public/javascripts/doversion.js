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
				    
				    '<label>a665d478aa30df6b6a75178a621279a80b85fc80</label>'+
				    
				   '</td></tr>'+
				   '<tr><td><label>版本号:：</label></td><td>'+
				   
				    '<label>v1.2.7</label>'+
				    
				   '</td></tr>'+
				   '</td></tr>'+
				   '<tr><td><label>发布时间:：</label></td><td>'+
				   
				    '<label>2014-5-10</label>'+
				    
				   '</td></tr>'+
				   '</div>'
				);				
   }
////////////////////////function	
})(jQuery);
