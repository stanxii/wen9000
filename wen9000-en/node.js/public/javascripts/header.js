(function($) {
$(function(){	  
	$("ul.subnav").parent().append("<span></span>");
		      
		        $("ul.topnav li span").click(function() {
		            $(this).parent().find("ul.subnav").slideDown('fast').show();		      
		            $(this).parent().hover(function() {  
		            }, function(){  
		                $(this).parent().find("ul.subnav").slideUp('slow'); //When the mouse hovers out of the subnav, move it back up  
		            }); 
		            }).hover(function() {  
		                $(this).addClass("subhover");
		            }, function(){
		                $(this).removeClass("subhover");
		        });  
		      
		
});

})(jQuery);

