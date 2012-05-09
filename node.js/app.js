
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
//app.get('/profilemanager', routes.profilemanager);
app.get('/profilemanager', function( request, response ) {
    response.render( 'profilemanager.jade', { title: 'Wen9000网路管理系统---模板管理' } );
});
app.get('/opt/accounts', function( request, response ) {
    response.render( 'opt/accounts.jade', { title: 'Wen9000网路管理系统---选择配置用户' } );
});
app.get('/opt/selprofiles', function( request, response ) {
    response.render( 'opt/selprofiles.jade', { title: 'Wen9000网路管理系统---选择模板' } );
});
app.get('/opt/confirm', function( request, response ) {
    response.render( 'opt/confirm.jade', { title: 'Wen9000网路管理系统---操作确认' } );
});
app.get('/opt/config_results', function( request, response ) {
    response.render( 'opt/config_results.jade', { title: 'Wen9000网路管理系统---结果查询' } );
});

var node = http.createServer(app).listen(3000);
var sio = io.listen(node);


redis.psubscribe('node.alarm.*');
redis.psubscribe('node.tree.*');
redis.psubscribe('node.pro.*');
redis.psubscribe('node.opt.*');

redis.on('pmessage', function(pat,ch,data) {

   console.log('pmessage receive from redis with pubsub pat='+ pat + ' ch = ' + ch + ' data' + data);
   if(pat == 'node.alarm.*') {
       data = JSON.parse(data);
       sio.sockets.emit('newAlarm',data);
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
    }
});

sio.sockets.on('connection', function (socket) {
  console.log('socket connected!' + socket.id);


  socket.on('initDynatree', function (data) {
     console.log('nodeserver: inittree');
     publish.publish('servicecontroller.treeinit', 'inittree');
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
  
  socket.on('profile_get', function (data) {
	  	 console.log('nodeserver: profile_get==='+data);
	     publish.publish('servicecontroller.profile_get', data);
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
  
  socket.on('my other event', function (data) {
    console.log(data);
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