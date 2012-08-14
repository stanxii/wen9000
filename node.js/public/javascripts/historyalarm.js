(function($) {
	var pTable;
	$(function() {

		var socket = io.connect('http://localhost:3000');

		socket.emit('historyalarmall', 'historyalarmall');
		socket.emit('historypage', 'historypage');

		socket.on('historyalarmall', fun_AllHistoryAlarms);
		socket.on('historypage', fun_HistoryPage);
		socket.on('historynp', fun_HistoryNP);
		
		$("#btn_next").click(function(){
			socket.emit('historynext', 'historynext');
		});
		
		$("#btn_pre").click(function(){
			socket.emit('historypre', 'historypre');
		});

		function fun_HistoryNP(data){
			window.location.reload();
		}
		
		function fun_HistoryPage(data){
			if(data.haspre == "1"){
				$("#btn_pre").removeAttr("disabled");
			}else{
				$("#btn_pre").attr("disabled","disable");
			}
			if(data.hasnext == "1"){
				$("#btn_next").removeAttr("disabled");
			}else{
				$("#btn_next").attr("disabled","disable");
			}
			$("#from")[0].textContent = data.from;
			$("#end")[0].textContent = data.end;
			$("#total")[0].textContent = data.total;
		}
		
		function fun_AllHistoryAlarms(data) {
			var groupval = [];

			$.each(data, function(key, itemv) {
				var item = [itemv.alarmlevel,itemv.salarmtime, itemv.alarmcode,itemv.cbatmac,itemv.runingtime,itemv.cnalarminfo];
				groupval[groupval.length] = item;
			});
			
			pTable = $('#historyAlarm').dataTable({
				"bLengthChange": false,	
				"bFilter" : true,
				"iDisplayLength" : 10,
				"aaData" : groupval,
				"bInfo" : false,
				"sPaginationType" : "full_numbers",
				"oLanguage" : {
					"sLengthMenu" : "每页显示 _MENU_ 条记录",
					"sZeroRecords" : "没有检索到数据",
					"sInfo" : "当前数据为从第 _START_ 到第 _END_ 条数据；总共有 _TOTAL_ 条记录",
					"sSearch": "搜索:",
					"oPaginate" : {
						"sFirst" : "首页",
						"sPrevious" : "前页",
						"sNext" : "后页",
						"sLast" : "尾页"
					}
				},
				"fnRowCallback": function( nRow, aData, iDisplayIndex ) {	    			
		        	if ( aData[0] == "1" ){
		        		$('td:eq(0)', nRow).html( '<img src="http://localhost:8080/wen9000/images/ball_red.png" />' );				               
		            }else if(aData[0] == "2"){
		            	$('td:eq(0)', nRow).html( '<img src="http://localhost:8080/wen9000/images/ball_orange.png" />' );
		            }else if(aData[0] == "3"){
		            	$('td:eq(0)', nRow).html( '<img src="http://localhost:8080/wen9000/images/ball_yellow.png" />' );
		            }else if(aData[0] == "4"){
		            	$('td:eq(0)', nRow).html( '<img src="http://localhost:8080/wen9000/images/ball_hese.png" />' );
		            }else if(aData[0] == "5"){
		            	$('td:eq(0)', nRow).html( '<img src="http://localhost:8080/wen9000/images/ball_blue.png" />' );
		            }else if(aData[0] == "6"){
		            	$('td:eq(0)', nRow).html( '<img src="http://localhost:8080/wen9000/images/ball_green.png" />' );
		            }else if(aData[0] == "7"){
		            	$('td:eq(0)', nRow).html( '<img src="http://localhost:8080/wen9000/images/ball_white.png" />' );
		            }     
		            
		        },		
		        "aaSorting": [[ 1, "desc" ]],
				"aoColumns" : [
					{"sTitle" : "告警级别","sWidth":"8%"	},
					{"sTitle" : "告警时间","sWidth":"12%"	},
					{"sTitle" : "告警码","sWidth":"10%"		},
					{"sTitle" : "MAC地址","sWidth":"15%"	},
					{"sTitle" : "连续运行时间","sWidth":"15%"	},
					{"sTitle" : "告警信息","sWidth":"*"	}
					]

			});

		}

	});
})(jQuery);
