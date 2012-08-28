(function($) {
	var pTable;
	$(function() {

		var socket = io.connect('http://localhost:3000');

		socket.emit('optlogall', 'optlogall');
		socket.emit('optlogpage', 'historypage');

		socket.on('optlogall', fun_AllOptlogs);
		socket.on('getoptlogpage', fun_OptlogPage);
		socket.on('getoptlognp', fun_OptlogNP);
		
		$("#btn_next").click(function(){
			socket.emit('optlognext', 'historynext');
		});
		
		$("#btn_pre").click(function(){
			socket.emit('optlogpre', 'historypre');
		});

		function fun_OptlogNP(data){
			window.location.reload();
		}
		
		function fun_OptlogPage(data){
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
		
		function fun_AllOptlogs(data) {
			var groupval = [];

			$.each(data, function(key, itemv) {
				var item = [itemv.logtime,itemv.user, itemv.desc];
				groupval[groupval.length] = item;
			});
			
			pTable = $('#optlog').dataTable({
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
		        	
		        },		
		        "aaSorting": [[ 0, "desc" ]],
				"aoColumns" : [
					{"sTitle" : "时间","sWidth":"20%" , "sClass": "center"	},
					{"sTitle" : "操作用户","sWidth":"15%" , "sClass": "center"	},
					{"sTitle" : "描述","sWidth":"*"		}
					]

			});

		}

	});
})(jQuery);
