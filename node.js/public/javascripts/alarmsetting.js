(function($) {
	var pTable;
	$(function() {
		
		$("#alarmsettab").tabs();
		var socket = io.connect('http://localhost:3000');
        
		initAlarmSetting();
		
		socket.on('alarmsetting_init', onAlarmSettingInit);
		
		
		$("#btn_addlevelfilter").click(function(){
			var alarmlevel = $("#sel_alarmlevel option:selected").val();
			var datastring = '{"alarmlevel":"'+datastring+'"}';
			//add alarm filter event
			socket.emit('alarmfilter_add',datastring);
			
			
			if($(".talarmfilterclass").length< 10){
				//update and show flush ui
				$("#talarmfilter").append('<table class="talarmfilterclass"><tr><td style="width:200px"><label>告警等级</label></td>'+
				                                      '<td style="width:200px"><label>等于</label></td>'+
				                                      '<td style="width:200px"><label>'+alarmlevel+'</label></td></tr></table>');
			}else{
				alert("最多添加10条");
			}
		
		});
		
		$("#btn_addcodefilter").click(function(){
			var alarmcode = $("#sel_alarmcode option:selected").val();
			var datastring = '{"alarmcode":"'+datastring+'"}';
			//add alarm filter event
			socket.emit('alarmfilter_add',datastring);
			
			
			if($(".talarmfilterclass").length< 10){
				$("#talarmfilter").append('<table class="talarmfilterclass"><tbody><tr><td style="width:200px"><label>告警码</label></td>'+
                        '<td style="width:200px"><label>等于</label></td>'+
                        '<td style="width:200px"><label>'+alarmcode+'</label></td></tr></tbody></table>');				
			}else{
				alert("最多添加10条");
			}
			
		});
		
		$("#btn_clearallfilter").click(function(){
			$(".talarmfilterclass").remove();
			
		});
		
		
		//alarm directional
	
		$("#btn_addalarmdirectional").click(function(){
			
			
			if($(".talarmdirectionalclass").length< 10){
				var alarmcode = $("#sel_directionalalarmcode option:selected").val();
				var ip = $("#sipaddr").val();
				$("#talarmdirectional").append('<table class="talarmdirectionalclass"><tr><td style="width:200px"><label>告警码</label></td>'+
														'<td style="width:200px"><label>'+alarmcode+'</label></td>'+
														'<td style="width:200px"><label>只发往IP地址</label></td>'+
														'<td style="width:200px"><label>'+ip+'</label></td></tr></table>');
			}else{
				alert("最多添加10条");
			}
			
									
		});
	
		
		$("#btn_clearalldirectional").click(function(){
			$(".talarmdirectionalclass").remove();
		});
		
		
		function initAlarmSetting() {
			socket.emit('alarmsetting_init','{"alarmsetting":"init"}');
		}
		
		function onAlarmSettingInit(data){
			
		}
		
	});
})(jQuery);
