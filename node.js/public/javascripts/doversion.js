/*
 * 
 * v1.3.2 add support to WR1004JLD
 * 
 * 
 * 
 */
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
				    
				    '<label>51a3c13b42ffbb357eb541e26c9e0b8ad1cb6788</label>'+
				    
				   '</td></tr>'+
				   '<tr><td><label>版本号:：</label></td><td>'+
				   
				    '<label>v1.3.2</label>'+
				    
				   '</td></tr>'+
				   '</td></tr>'+
				   '<tr><td><label>发布时间:：</label></td><td>'+
				   
				    '<label>2015-1-22</label>'+
				    
				   '</td></tr>'+
				   '</div>'
				);				
   }
////////////////////////function	
})(jQuery);
