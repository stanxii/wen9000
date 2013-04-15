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
				"bLengthChange": false,					//用户不可改变每页显示数量
			"iDisplayLength": 10,					//每页显示10条数据
			"aaData": groupval,
    		"bInfo": false,	
	        "sPaginationType": "full_numbers",				        
	        "oLanguage": {							//汉化
				"sLengthMenu": "Display _MENU_ strip record per page",
				"sZeroRecords": "No data was discoveryed",
				"sInfo": "Current data is from _START_ to _END_ strip data; has _TOTAL_ strip record in all",
				"sInfoEmtpy": "No data",
				"sProcessing": "Data loading...",
				"sSearch": "Search device:",
				"oPaginate": {
					"sFirst": "Home",
					"sPrevious": "Previous",
					"sNext": "Next",
					"sLast": "End"
				}
			},
				"fnRowCallback": function( nRow, aData, iDisplayIndex ) {	    			
		        	
		        },		
		        "aaSorting": [[ 0, "desc" ]],
				"aoColumns" : [
					{"sTitle" : "Time","sWidth":"20%" , "sClass": "center"	},
					{"sTitle" : "User","sWidth":"15%" , "sClass": "center"	},
					{"sTitle" : "Description","sWidth":"*"		}
					]

			});

		}

	});
})(jQuery);
