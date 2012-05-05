
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

var node = http.createServer(app).listen(3000);
var sio = io.listen(node);


redis.psubscribe('node.alarm.*');
redis.psubscribe('node.tree.*');

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
