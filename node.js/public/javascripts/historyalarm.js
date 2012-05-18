(function($){
        var pTable;
        $(function(){

                var socket = io.connect('http://localhost:3000');


                socket.emit('historyalarmall','historyalarmall');

                socket.on('historyalarmall',fun_AllHistoryAlarms);


                                function fun_AllHistoryAlarms(data){
                                    var groupval=[];

                                                $.each(data, function(key, itemv) {
                                                                var item = [itemv.alarmcode,itemv.alarmlevel,itemv.cnalarminfo];
                                                                groupval[groupval.length] = item;
			});

		 $('#historyAlarm').show();
                                                pTable = $('#historyAlarm').dataTable( {
                        "bFilter": false,                                               //²»Ê¹ÓÃ¹ýÂË¹¦ÄÜ
                        "iDisplayLength": 10,                                   //Ã¿Ò³ÏÔÊ¾10ÌõÊý¾Ý
                        "aaData": groupval,
                "bInfo": false,
                "sPaginationType": "full_numbers",
                "oLanguage": {           
			                                                        "sLengthMenu": "每页显示 _MENU_ 条记录",
                                                        "sZeroRecords": "没有检索到数据",
                                                        "sInfo": "当前数据为从第 _START_ 到第 _END_ 条数据；总共有 _TOTAL_ 条记录",
                                                        "oPaginate": {
                                                                "sFirst": "首页",
                                                                "sPrevious": "前页",
                                                                "sNext": "后页",
                                                                "sLast": "尾页"
                                                        }
                                                },
		  "aoColumns": [

                                                        {"sTitle" : "告警码"},
                                                              { "sTitle": "告警信息" },
                                                              { "sTitle": "告警级别"}
                                                                ]

                                });


                                                        oTable.fnDraw();

                         }


 } );
})(jQuery);
