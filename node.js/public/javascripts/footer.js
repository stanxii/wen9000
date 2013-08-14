
/*lanrenzhijia*/
(function($){
	$(function(){
		 var socket = io.connect('http://localhost:3000');
         

			socket.on('countcbat', onCountCbat);
			socket.on('countcnu', onCountCnu);
			
			openDiv();
		
	});
	/////////////////////////////function
	function onCountCbat(data){		
		jQuery("#cbatcounts")[0].value= data;
	}

	function onCountCnu(data){
		jQuery("#cnucounts")[0].value= data;
	}

	function openDiv()
	{

		jQuery("#contraction").click(function(){
			jQuery(".box-lanrenzhijia").animate({
	        height: '30px',bottom:'0px'
	    }, 1000, 'linear', function() { jQuery(".pop_Content").hide(); });
		jQuery("#contraction").hide();
		jQuery("#open_window").show();
		});
		
		jQuery("#open_window").click(function(){
			jQuery(".box-lanrenzhijia").animate({
	        height: '100px'
	    }, 10, 'linear', function() { jQuery(".pop_Content").show(); });
		jQuery("#open_window").hide();
		jQuery("#contraction").show();
		});
		
		jQuery("#close_window").click(function(){
			jQuery(".box-lanrenzhijia").hide();
		});	
	}

	function closeDiv()
	 {
	  document.getElementById('pop').style.visibility='hidden';
	 }
	
})(jQuery);
	
	


