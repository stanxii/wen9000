
/**
 * Module dependencies.
 */

var express = require('express')
  , routes = require('./routes')
  , http = require('http')
  , io = require('socket.io');


var app = express();
var redis = require('redis').createClient();
var publish = require('redis').createClient();

app.configure(function(){
  app.set('views', __dirname + '/views');
  app.set('view engine', 'jade');
  app.use(express.favicon());
  app.use(express.logger('dev'));
  app.use(express.static(__dirname + '/public'));
  app.use(express.bodyParser());
  app.use(express.methodOverride());
  app.use(app.router);
});

app.configure('development', function(){
  app.use(express.errorHandler());
});

app.get('/', routes.index);
app.get('/profilemanager', function( request, response ) {
    response.render( 'profilemanager.jade', { title: 'Wen9000网路管理系统---模板管理' } );
});
app.get('/opt/accounts', function( request, response ) {
    response.render( 'opt/accounts.jade', { title: 'Wen9000网路管理系统---选择配置用户' } );
});
app.get('/opt/selectprofiles', function( request, response ) {
    response.render( 'opt/selectprofiles.jade', { title: 'Wen9000网路管理系统---选择模板' } );
});
app.get('/opt/confirm', function( request, response ) {
    response.render( 'opt/confirm.jade', { title: 'Wen9000网路管理系统---操作确认' } );
});
app.get('/opt/config_results', function( request, response ) {
    response.render( 'opt/config_results.jade', { title: 'Wen9000网路管理系统---结果查询' } );
});
app.get('/dis/search', function( request, response ) {
    response.render( 'discovery/discovery.jade', { title: 'Wen9000网路管理系统---设备搜索' } );
});
app.get('/dis/result', function( request, response ) {
    response.render( 'discovery/result.jade', { title: 'Wen9000网路管理系统---搜索结果' } );
});
app.get('/opt/global_opt', function( request, response ) {
    response.render( 'opt/global_opt.jade', { title: 'Wen9000网路管理系统---全局管理' } );
});
app.get('/opt/updatecbat', function( request, response ) {
    response.render( 'opt/updatecbat.jade', { title: 'Wen9000网路管理系统---局端升级' } );
});
app.get('/version', function( request, response ) {
    response.render( 'version.jade', { title: 'Wen9000网路管理系统---版本信息' } );
});
app.get('/historyalarm', function( request, response ) {
    response.render( 'historyalarm.jade', { title: 'Wen9000网路管理系统---历史告警' } );
});
app.get('/opt/pre_config', function( request, response ) {
    response.render( 'opt/pre_config.jade', { title: 'Wen9000网路管理系统---设备预开户' } );
});

var node = http.createServer(app).listen(3000);
var sio = io.listen(node);


redis.psubscribe('node.alarm.*');
redis.psubscribe('node.tree.*');
redis.psubscribe('node.pro.*');
redis.psubscribe('node.opt.*');
redis.psubscribe('node.dis.*');

redis.on('pmessage', function(pat,ch,data) {

   console.log('pmessage receive from redis with pubsub pat='+ pat + ' ch = ' + ch + ' data' + data);
   if(pat == 'node.alarm.*') {
       data = JSON.parse(data);
       sio.sockets.emit('newAlarm',data);
    }
    else if(ch == 'node.historyalarm') {
       data = JSON.parse(data);
       sio.sockets.emit('historyalarm_all',data);
    }
    else if(ch == 'node.tree.init') {
       data = JSON.parse(data);
       sio.sockets.emit('initDynatree',data);
    }

    else if(ch == 'node.tree.cbatdetail') {
        data = JSON.parse(data);
        sio.sockets.emit('cbatdetail',data);
    }else if(ch == 'node.tree.cnudetail') {
        data = JSON.parse(data);
        sio.sockets.emit('cnudetail',data);
    }else if(ch == 'node.tree.cbatmodify') {
        //data = JSON.parse(data);
        sio.sockets.emit('cbat_modify',data);
    }else if(ch == 'node.tree.cbatsync') {
    	if(data == ""){
    		sio.sockets.emit('cbat_sync',data);
    	}else{
    		data = JSON.parse(data);
            sio.sockets.emit('cbat_sync',data);
    	}        
    }else if(ch == 'node.tree.statuschange') {
        data = JSON.parse(data);
        sio.sockets.emit('statuschange',data);
    }else if(ch == 'node.pro.allprofiles') {
        data = JSON.parse(data);
        sio.sockets.emit('profileALL',data);
    }else if(ch == 'node.pro.delprofile') {
    	sio.sockets.emit('profileDEL',data);   
    }else if(ch == 'node.pro.get') {
    	data = JSON.parse(data);
    	sio.sockets.emit('profileGet',data);   
    }else if(ch == 'node.pro.detail') {
    	data = JSON.parse(data);
    	sio.sockets.emit('profiledetail',data);   
    }else if(ch == 'node.pro.isedit') {
    	if(data == ""){
    		sio.sockets.emit('profileisedit',data);
    	}else{
    		data = JSON.parse(data);
            sio.sockets.emit('profileisedit',data);
    	}      
    }else if(ch == 'node.tree.cnusync') {
    	if(data == ""){
    		sio.sockets.emit('cnusync',data);
    	}else{
    		data = JSON.parse(data);
            sio.sockets.emit('cnusync',data);
    	}        
    }else if(ch == 'node.tree.cnu_sub') {
    	sio.sockets.emit('cnu_sub',data);   
    }else if(ch == 'node.opt.cnus') {
    	data = JSON.parse(data);
    	sio.sockets.emit('opt.allcnus',data);   
    }else if(ch == 'node.opt.allcheckedcnus') {
    	if(data == ""){
    		sio.sockets.emit('opt.allcheckedcnus',data);
    	}else{
    		data = JSON.parse(data);
            sio.sockets.emit('opt.allcheckedcnus',data);
    	}        
    }else if(ch == 'node.opt.selectedpro') {
    	data = JSON.parse(data);
    	sio.sockets.emit('opt.selectedpro',data);   
    }else if(ch == 'node.opt.sendconfig') {
    	sio.sockets.emit('opt.sendconfig',data);   
    }else if(ch == 'node.opt.proc') {
    	sio.sockets.emit('opt.p_proc',data);   
    }else if(ch == 'node.opt.con_success') {
    	if(data == ""){
    		sio.sockets.emit('opt.con_success',data);
    	}else{
    		data = JSON.parse(data);
            sio.sockets.emit('opt.con_success',data);
    	}        
    }else if(ch == 'node.opt.con_failed') {
    	if(data == ""){
    		sio.sockets.emit('opt.con_failed',data);
    	}else{
    		data = JSON.parse(data);
            sio.sockets.emit('opt.con_failed',data);
    	}        
    }else if(ch == 'node.dis.validate') {
    	sio.sockets.emit('dis.validate',data);   
    }else if(ch == 'node.dis.proc') {
    	sio.sockets.emit('dis.proc',data);   
    }else if(ch == 'node.dis.searchtotal') {
    	data = JSON.parse(data);
    	sio.sockets.emit('dis.searchtotal',data);   
    }else if(ch == 'node.dis.findcbat') {
    	data = JSON.parse(data);
    	sio.sockets.emit('dis.findcbat',data);   
    }else if(ch == 'node.opt.globalopt') {
    	if(data == ""){
    		sio.sockets.emit('opt.globalopt',data);
    	}else{
    		data = JSON.parse(data);
            sio.sockets.emit('opt.globalopt',data);
    	}        
    }else if(ch == 'node.opt.globalsave') {
    	sio.sockets.emit('opt.globalsave',data);         
    }else if(ch == 'node.opt.saveredis') {
    	sio.sockets.emit('opt.saveredis',data);         
    }else if(ch == 'node.opt.onlinecbats') {
    	if(data == ""){
    		sio.sockets.emit('opt.onlinecbats',data);
    	}else{
    		data = JSON.parse(data);
            sio.sockets.emit('opt.onlinecbats',data);
    	}        
    }else if(ch == 'node.opt.ftpconnect') {
    	if(data == ""){
    		sio.sockets.emit('opt.ftpfilelist',data);
    	}else{
    		data = JSON.parse(data);
            sio.sockets.emit('opt.ftpfilelist',data);
    	}        
    }else if(ch == 'node.opt.updateproc') {
    	sio.sockets.emit('opt.updateproc',data);       
    }else if(ch == 'node.opt.updateinfo') {
    	sio.sockets.emit('opt.updateinfo',data);       
    }else if(ch == 'node.opt.preconfig_one') {
    	sio.sockets.emit('opt.preconfig_one',data);       
    }else if(ch == 'node.opt.preconfig_batch') {
    	data = JSON.parse(data);
    	sio.sockets.emit('opt.preconfig_batch',data);       
    }else if(ch == 'node.opt.preconfig_all') {
    	data = JSON.parse(data);
    	sio.sockets.emit('opt.preconfig_all',data);       
    }
});

sio.sockets.on('connection', function (socket) {
  console.log('socket connected!' + socket.id);


  socket.on('initDynatree', function (data) {
     console.log('nodeserver: inittree');
     publish.publish('servicecontroller.treeinit', 'inittree');
  });
  
  socket.on('historyalarm_all', function (data) {
     console.log('nodeserver: historyalarm_all');
     publish.publish('servicecontroller.gethistoryalarm', '{istart:1, ilen: 1000}');
  });

  socket.on('cbatdetail', function (data) {
	     console.log('nodeserver: cbatmac==='+data);
	     publish.publish('servicecontroller.cbatdetail', data);
  });
  
  socket.on('cnudetail', function (data) {
	     console.log('nodeserver: cnumac==='+data);
	     publish.publish('servicecontroller.cnudetail', data);
  });
  
  socket.on('cbat_modify', function (data) {
	  	 console.log('nodeserver: cbatmodify==='+data);
	     publish.publish('servicecontroller.cbat_modify', data);
  });
  
  socket.on('cbat_sync', function (data) {
	  	 console.log('nodeserver: cbatsync==='+data);
	     publish.publish('servicecontroller.cbat_sync', data);
  });
  
  socket.on('profile_all', function (data) {
	  	 console.log('nodeserver: profile_all==='+data);
	     publish.publish('servicecontroller.profile_all', data);
  });

  socket.on('profile_del', function (data) {
	  	 console.log('nodeserver: profile_del==='+data);
	     publish.publish('servicecontroller.profile_del', data);
  });
  
  socket.on('profile_detail', function (data) {
	  	 console.log('nodeserver: profile_detail==='+data);
	     publish.publish('servicecontroller.profile_detail', data);
  });
  
  socket.on('profile_get', function (data) {
	  	 console.log('nodeserver: profile_get==='+data);
	     publish.publish('servicecontroller.profile_get', data);
  });
  
  socket.on('profile_isedit', function (data) {
	  	 console.log('nodeserver: profile_isedit==='+data);
	     publish.publish('servicecontroller.profile_isedit', data);
  });
  
  socket.on('profile_edit', function (data) {
	  	 console.log('nodeserver: profile_edit==='+data);
	     publish.publish('servicecontroller.profile_edit', data);
  });
  
  socket.on('profile_create', function (data) {
	  	 console.log('nodeserver: profile_create==='+data);
	     publish.publish('servicecontroller.profile_create', data);
  });
  
  socket.on('cnu_basesub', function (data) {
	  	 console.log('nodeserver: cnu_basesub==='+data);
	     publish.publish('servicecontroller.cnu_basesub', data);
  });
  
  socket.on('cnusync', function (data) {
	  	 console.log('nodeserver: cnusync==='+data);
	     publish.publish('servicecontroller.cnusync', data);
  });
  
  socket.on('cnu_sub', function (data) {
	  	 console.log('nodeserver: cnu_sub==='+data);
	     publish.publish('servicecontroller.cnu_sub', data);
  });
  
  socket.on('opt.cnus', function (data) {
	  	 console.log('nodeserver: opt.cnus==='+data);
	     publish.publish('servicecontroller.opt.cnus', data);
  });
  
  socket.on('opt.checkedcnus', function (data) {
	  	 console.log('nodeserver: opt.checkedcnus==='+data);
	     publish.publish('servicecontroller.opt.checkedcnus', data);
  });
  
  socket.on('opt.allcheckedcnus', function (data) {
	  	 console.log('nodeserver: opt.allcheckedcnus==='+data);
	     publish.publish('servicecontroller.opt.allcheckedcnus', data);
  });
  
  socket.on('opt.selectedpro', function (data) {
	  	 console.log('nodeserver: opt.selectedpro==='+data);
	     publish.publish('servicecontroller.opt.selectedpro', data);
  });
  
  socket.on('opt.send_config', function (data) {
	  	 console.log('nodeserver: opt.send_config==='+data);
	     publish.publish('servicecontroller.opt.send_config', data);
  });
  
  socket.on('opt.con_success', function (data) {
	  	 console.log('nodeserver: opt.con_success==='+data);
	     publish.publish('servicecontroller.opt.con_success', data);
  });
  
  socket.on('opt.con_failed', function (data) {
	  	 console.log('nodeserver: opt.con_failed==='+data);
	     publish.publish('servicecontroller.opt.con_failed', data);
  });
  
  socket.on('discovery.search', function (data) {
	  	 console.log('nodeserver: discovery.search==='+data);
	     publish.publish('servicecontroller.discovery.search', data);
  });
  
  socket.on('dis.searchtotal', function (data) {
	  	 console.log('nodeserver: discovery.searchtotal==='+data);
	     publish.publish('servicecontroller.discovery.searchtotal', data);
  });
  
  socket.on('opt.global_opt', function (data) {
	  	 console.log('nodeserver: opt.global_opt==='+data);
	     publish.publish('servicecontroller.opt.global_opt', data);
  });
  
  socket.on('opt.save_global', function (data) {
	  	 console.log('nodeserver: opt.save_global==='+data);
	     publish.publish('servicecontroller.opt.save_global', data);
  });
  
  socket.on('opt.saveredis', function (data) {
	  	 console.log('nodeserver: opt.saveredis==='+data);
	     publish.publish('servicecontroller.opt.saveredis', data);
  });
  
  socket.on('opt.onlinecbats', function (data) {
	  	 console.log('nodeserver: opt.onlinecbats==='+data);
	     publish.publish('servicecontroller.opt.onlinecbats', data);
  });
  
  socket.on('opt.ftpconnet', function (data) {
	  	 console.log('nodeserver: opt.ftpconnet==='+data);
	     publish.publish('servicecontroller.opt.ftpconnet', data);
  });
  //选择要升级的头端设备
  socket.on('opt.updatedcbats', function (data) {
	  	 console.log('nodeserver: opt.updatedcbats==='+data);
	     publish.publish('servicecontroller.opt.updatedcbats', data);
  });
  //升级头端设备
  socket.on('opt.ftpupdate', function (data) {
	  	 console.log('nodeserver: opt.ftpupdate==='+data);
	     publish.publish('servicecontroller.opt.ftpupdate', data);
  });
  //升级头端进度信息
  socket.on('opt.updateinfo', function (data) {
	  	 console.log('nodeserver: opt.updateinfo==='+data);
	     publish.publish('ServiceUpdateProcess.updateinfo', data);
  });
  //获取实时告警信息
  socket.on('lastalarms', function (data) {
	  console.log(data);
	  publish.publish('servicecontroller.opt.lastalarms', data);
  });
//单个预开户事件
  socket.on('opt.preconfig_one', function (data) {
	  console.log('nodeserver: opt.preconfig_one==='+data);
	  publish.publish('servicecontroller.opt.preconfig_one', data);
  });
//批量预开户事件
  socket.on('opt.preconfig_batch', function (data) {
	  console.log('nodeserver: opt.preconfig_batch==='+data);
	  publish.publish('servicecontroller.opt.preconfig_batch', data);
  });
//获取预开户列表
  socket.on('preconfig_all', function (data) {
	  console.log('nodeserver: preconfig_all==='+data);
	  publish.publish('servicecontroller.opt.preconfig_all', data);
  });
//删除预开户设备
  socket.on('pre_del', function (data) {
	  console.log('nodeserver: pre_del==='+data);
	  publish.publish('servicecontroller.opt.pre_del', data);
  });

  socket.on('channel', function(ch) {
      console.log('channel receive ch=='+ch);
        socket.join(ch);
   });

   socket.on('disconnect', function(){
        console.log('Socket dis connected: ' + socket.id);
   });


});



redis.on('error', function(err) {
        console.log('Error ' + err);
});

console.log("Express server listening on port 3000");
