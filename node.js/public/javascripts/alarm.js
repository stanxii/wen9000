 var num =0;
 var oTable = null;
          $(function() {
		//init tab
               $("#tabs").tabs();
	       $(".tabs-bottom .ui-tabs-nav, .tabs-bottom .ui-tabs-nav > *").removeClass("ui-corner-all ui-corner-top").addClass("ui-corner-bottom");
	       		  
                  var socket = io.connect('http://localhost:3000');
                  socket.emit('lastalarms', "");

                  socket.on('news', function (data) {
                    console.log(data);
                    
                  });
                  socket.on('newAlarm', onGetNewAlarm);
                  addAlarmHead();                 
                  
                  historyAlarmTable();             
                  

                  function onGetNewAlarm(data) {
                        console.log(data);
                        if(num <15) {
                            addNewOne(data);
                            num++;
                        }else {
                            $('.ralarm:eq(14)').remove();

                            addNewOne(data);
	                }
                  }

                function addAlarmHead() {
                        
                        $( '<ul class="ralarmHead" style="width:100%">' +
                        		'<li style="width:80px;text-align:center"> 告警等级 </li>' +
                        		'<li style="width:200px;text-align:center"> 告警发生时间 </li>' +
                                '<li style="width:150px;text-align:center">MAC地址</li>' +
                               '<li style="width:110px;text-align:center">告警码</li>' +
                               '<li class="alarminfo" style="width:260px;text-align:center">告警详细信息</li>'+

                        	'</ul>').prependTo('#alarmHead');
                }


	        function addNewOne(data) {
                        var background= "";
                        
                         if(data.alarmlevel == 1 ) background = "http://localhost:3000/images/ball_red.png";
                         else if(data.alarmlevel == 2 ) background = "http://localhost:3000/images/ball_orange.png";
                         else if(data.alarmlevel == 3 ) background = "http://localhost:3000/images/ball_yellow.png";
                         else if(data.alarmlevel == 4 ) background = "http://localhost:3000/images/ball_hese.png";
                         else if(data.alarmlevel == 5 ) background = "http://localhost:3000/images/ball_blue.png";
                         else if(data.alarmlevel == 6 ) background = "http://localhost:3000/images/ball_green.png";
                         else if(data.alarmlevel == 7 ) background = "http://localhost:3000/images/ball_white.png";
                         else background = "http://localhost:3000/images/ball_black.png";


                          $( '<ul class="ralarm" style="height:24px"><li style="width:80px;text-align:center;"><img src='+  background +  ' /></li><li style="width:200px;text-align:center;">'+  data.salarmtime +  '</li><li style="width:150px;text-align:center;">' +
                                                       data.cbatmac +  '</li><li style="width:110px;text-align:center">' +
                                                       data.alarmcode+ '</li><li class="alarminfo" style="width:260px;text-align:center">' +
                                                       data.cnalarminfo +

                                 '</li></ul>').prependTo('#newAlarm');
                          
                          if(window.screen.width<=1024){
                          	  $(".ralarm").css("width","104%");
                            }else{
                          	  $(".ralarm").css("width","100%");
                            }
                }
	        
	        $(".ralarm").live('mouseover',function(){
	        	$(this).css("background-color","#ccc");
	        });
	        $(".ralarm").live('mouseout',function(){
	        	$(this).css("background-color","");	        	
	        });

          });


 	  //自定义数据获取函数
	function retrieveData( sSource, aoData, fnCallback ) {		
		$.ajax( {
			"type": "POST", 
			"contentType": "application/json",
			"url": sSource, 
			"dataType": "json",
			"data": JSON.stringify(aoData), 
			"success": function(resp) {
				fnCallback(resp);
			}
		});
	}

	  function historyAlarmTable(){
  	  	
  		if (oTable == null) { //仅第一次检索时初始化Datatable
			$("#alarmtable").show();
			oTable = $('#alarmtable').dataTable( {
				"bAutoWidth": false,	//不自动计算列宽度
				
				"aoColumns": [							//设定各列宽度
								{"sWidth": "15px"},
								{"sWidth": "15px"},	
								{"sWidth": "160px"},
								{"sWidth": "110px"},
								{"sWidth": "120px"},
								{"sWidth": "140px"},
								{"sWidth": "140px"},
								{"sWidth": "15px"},
								{"sWidth": "80px"},
								{"sWidth": "160px"},
								{"sWidth": "110px"},
								{"sWidth": "120px"},
								{"sWidth": "140px"},		
													
								{"sWidth": "*"}
							],
				"bProcessing": false,					//加载数据时显示正在加载信息
				"bServerSide": true,					//指定从服务器端获取数据
				"bFilter": false,						//不使用过滤功能
				"bLengthChange": false,					//用户不可改变每页显示数量
				"iDisplayLength": 8,					//每页显示8条数据
				"sAjaxSource": "http://localhost:8080/wen9000/alarm/historyalarm", //获取数据的url				
				"fnServerData": retrieveData,			//获取数据的处理函数
				"sPaginationType": "full_numbers",		//翻页界面类型
				"oLanguage": {							//汉化
					"sLengthMenu": "每页显示 _MENU_ 条记录",
					"sZeroRecords": "没有检索到数据",
					"sInfo": "当前数据为从第 _START_ 到第 _END_ 条数据；总共有 _TOTAL_ 条记录",
					"sInfoEmtpy": "没有数据",
					"sProcessing": "正在加载数据...",
					"oPaginate": {
						"sFirst": "首页",
						"sPrevious": "前页",
						"sNext": "后页",
						"sLast": "尾页"
					}
				}
			});
		}

		//刷新Datatable，会自动激发retrieveData
	//	oTable.fnDraw();
		
	}


