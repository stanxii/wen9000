
/**
 * Module dependencies.
 */

var express = require('express')
  , routes = require('./routes')
  , http = require('http')
  , querystring = require('querystring')
  , io = require('socket.io');

var MemStore = express.session.MemoryStore;
var app = express();
var redis = require('redis').createClient();
var publish = require('redis').createClient();
var jedis = require('redis').createClient();

app.configure(function(){
  app.set('views', __dirname + '/views');
  app.set('view engine', 'jade');
  app.use(express.favicon());
  app.use(express.logger('dev'));
  app.use(express.static(__dirname + '/public'));
  app.use(express.bodyParser());
  app.use(express.cookieParser("thissecretrocks"));
  app.use(express.methodOverride());  
  app.use(express.session({secret: 'alessios', store: MemStore({
	    reapInterval: 60000 * 10
  })}));
  app.use(app.router);
  app.use(express.errorHandler());
});

//app.configure('development', function(){
//  app.use(express.errorHandler());
//});

//app.get('/', routes.index);

app.get('/login', function (req, res) {
	console.log("-----------------old user====>>>"+req.session.user);
	
	res.render('login.jade', { title: 'Wen9000网路管理系统---登录' });
});

app.get('/register', function (req, res) {
	res.render('register.jade', { title: 'Wen9000网络管理系统---注册' });
});
//用户不存在
app.get('/101', function (req, res) {
	res.render('101.jade', { title: 'Wen9000网络管理系统---错误' });
});
//密码错误
app.get('/102', function (req, res) {
	res.render('102.jade', { title: 'Wen9000网络管理系统---错误' });
});

app.get('/validate', function (req, res) {
	if ((req.session.user != null)&&((req.session.user != "undefined"))) {
		//console.log("---------------------------------------->>>>>"+req.session.user);
//		jedis.get('global:isupdating',function(error,result){
//			if(result == "true"){
//				res.redirect('/103');
//			}
//		});
		jedis.exists('user:'+req.session.user, function(error, result) {
		    if(result){
		    	jedis.hget('user:'+req.session.user,"password", function(error, result) {
		    	    if(result == req.session.password){
		    	    	res.render('index', { title: 'Wen9000网络管理系统' });
		    	    }else{
		    	    	res.redirect('/102');
		    	    }
		    	});
		    }else{
		    	res.redirect('/101');
		    }
		});		
	} else {
		res.redirect('/login');
	}
});

app.get('/', function (req, res) {
	if ((req.session.user != null)&&((req.session.user != "undefined"))) {
		res.render('index', { title: 'Wen9000网络管理系统' });	
	} else {
		res.redirect('/login');
	}
});


//登陆post路由
app.post('/login', function (req, res) {
	var name = req.body.userName;	
	var password = req.body.password;	
	req.session.user = name;
	req.session.password = password;
	res.redirect('/validate');
});

//注册用户
app.post('/register', function (req, res) {
	var name = req.body.userName;	
	var password = req.body.password;
    jedis.hset("user:"+name,"password",password);
    jedis.hset("user:"+name,"flag","3");
	res.redirect('/login');
});

app.get('/profilemanager', function( request, response ) {
	if ((request.session.user != null)&&((request.session.user != "undefined"))) {
		response.render( 'profilemanager.jade', { title: 'Wen9000网络管理系统---模板管理' } );
	} else {
		response.redirect('/login');
	}    
});
app.get('/opt/accounts', function( request, response ) {
	if ((request.session.user != null)&&((request.session.user != "undefined"))) {
		response.render( 'opt/accounts.jade', { title: 'Wen9000网络管理系统---选择配置用户' } );
	} else {
		response.redirect('/login');
	}    
});
app.get('/opt/selectprofiles', function( request, response ) {
	if ((request.session.user != null)&&((request.session.user != "undefined"))) {
		response.render( 'opt/selectprofiles.jade', { title: 'Wen9000网络管理系统---选择模板' } );
	} else {
		response.redirect('/login');
	}        
});
app.get('/opt/confirm', function( request, response ) {
	response.render( 'opt/confirm.jade', { title: 'Wen9000网络管理系统---操作确认' } );	        
});
app.get('/opt/config_results', function( request, response ) {
    response.render( 'opt/config_results.jade', { title: 'Wen9000网络管理系统---结果查询' } );
});
app.get('/dis/search', function( request, response ) {
	if ((request.session.user != null)&&((request.session.user != "undefined"))) {
		response.render( 'discovery/discovery.jade', { title: 'Wen9000网络管理系统---设备搜索' } );
	} else {
		response.redirect('/login');
	}     
});
app.get('/dis/result', function( request, response ) {
    response.render( 'discovery/result.jade', { title: 'Wen9000网络管理系统---搜索结果' } );
});
app.get('/global_opt', function( request, response ) {
	if ((request.session.user != null)&&((request.session.user != "undefined"))) {
		response.render( 'global_opt.jade', { title: 'Wen9000网络管理系统---全局管理' } );
	} else {
		response.redirect('/login');
	}      
});
app.get('/opt/updatecbat', function( request, response ) {
	if ((request.session.user != null)&&((request.session.user != "undefined"))) {
		response.render( 'opt/updatecbat.jade', { title: 'Wen9000网络管理系统---局端升级' } );
	} else {
		response.redirect('/login');
	}     
});
app.get('/version', function( request, response ) {
    response.render( 'version.jade', { title: 'Wen9000网络管理系统---版本信息' } );
});
app.get('/historyalarm', function( request, response ) {
	if ((request.session.user != null)&&((request.session.user != "undefined"))) {
		response.render( 'historyalarm.jade', { title: 'Wen9000网络管理系统---历史告警' } );
	} else {
		response.redirect('/login');
	}       
});
app.get('/opt/pre_config', function( request, response ) {
	if ((request.session.user != null)&&((request.session.user != "undefined"))) {
		response.render( 'opt/pre_config.jade', { title: 'Wen9000网络管理系统---设备预开户' } );
	} else {
		response.redirect('/login');
	}      
});
app.get('/userManager', function( request, response ) {
	if ((request.session.user != null)&&((request.session.user != "undefined"))) {
		response.render( 'userManager.jade', { title: 'Wen9000网络管理系统---用户管理' } );
	} else {
		response.redirect('/login');
	}     
});

//app.listen(3000);
var node = http.createServer(app).listen(3000);
var sio = io.listen(node);


//test
function PostCode() {
	  var date = new Date();
	  // Build the post string from an object
	  var post_data = querystring.stringify({
		  "type":2,    //0-cbat, 1-clt, 2-cnu
		  "mac":"F0:4D:A2:FD:6A:DF",
		  "vlanen":"1",  //0-disable, 1-enable
		  "vlan0id":1,
		  "vlan1id":1,
		  "vlan2id":1,
		  "vlan3id":1,
		  "txlimitsts":1,   //1-enable, 2-disable  if 1 then have follow param else no
		  "cpuporttxrate":1024,   //K
		  "port0rxrate":1024,
		  "port1rxrate":1024,
		  "port2rxrate":1024,
		  "port3rxrate":1024,
		  "rxlimitsts":1, //1-enable, 2-disable  if 1 then have follow param else no
		  "cpuportrxrate":1024,
		  "port0txrate":1024,
		  "port1txrate":1024,
		  "port2txrate":1024,
		  "port3txrate":1024,
		  "permit":1   //1-kaitong, 2-guanduan
		  });

	  // An object of options to indicate where to post to
	  var post_options = {
	      host: '192.168.1.194',
	      port: '80',
	      path: '/setcnu.json',
	      auth: 'support:support',
	      method: 'POST',
	      headers: {
	          'Content-Type': 'text/json',
	          'Content-Length': post_data.length
	      }
	  };

	  // Set up the request
	  var post_req = http.request(post_options, function(res) {
		  res.setEncoding('utf8');
		  //res.on('response', function (response) {
			  res.on('data', function (chunk) {
				date = new Date();
				console.log("end time ===================="+date.getSeconds()+"=====millsecends==="+date.getMilliseconds());
			    console.log('BODY: ' + chunk);
			  });
			//});		      
		  });
	      
	  post_req.on('error', function (error) {
		    console.log('server_http:Error: ' + error);
		});
	  console.log("first time ====================secends=="+date.getSeconds()+"=====millsecends==="+date.getMilliseconds());
	  post_req.write(post_data);
	  post_req.end();

	};


//PostCode();

redis.psubscribe('node.alarm.*');
redis.psubscribe('node.historyalarm.*');
redis.psubscribe('node.tree.*');
redis.psubscribe('node.pro.*');
redis.psubscribe('node.opt.*');
redis.psubscribe('node.dis.*');

//redis初始化
publish.publish('servicecontroller.index.init', '');

redis.on('pmessage', function(pat,ch,data) {

   console.log('pmessage receive from redis with pubsub pat='+ pat + ' ch = ' + ch + ' data' + data);
   if(pat == 'node.alarm.*') {
       data = JSON.parse(data);
       sio.sockets.emit('newAlarm',data);
    }else if(ch == 'node.historyalarm.getall') {
       data = JSON.parse(data);
       sio.sockets.emit('historyalarmall',data);
    }else if(ch == 'node.historyalarm.gethistorypage') {
        data = JSON.parse(data);
        sio.sockets.emit('historypage',data);
    }else if(ch == 'node.historyalarm.gethistorynp') {
        sio.sockets.emit('historynp',data);
     }else if(ch == 'node.tree.init') {
       data = JSON.parse(data);
       sio.sockets.emit('initDynatree',data);
    }else if(ch == 'node.tree.cbatdetail') {
        data = JSON.parse(data);
        sio.sockets.emit('cbatdetail',data);
    }else if(ch == 'node.tree.cnudetail') {
        data = JSON.parse(data);
        sio.sockets.emit('cnudetail',data);
    }else if(ch == 'node.tree.hfcdetail') {
        data = JSON.parse(data);
        sio.sockets.emit('hfcdetail',data);
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
    	if(data == ""){
    		sio.sockets.emit('cnu_sub',data);
    	}else{
    		data = JSON.parse(data);
            sio.sockets.emit('cnu_sub',data);
    	}  
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
    }else if(ch == 'node.opt.checkedcbats') {
    	sio.sockets.emit('opt.checkedcbats',data);       
    }else if(ch == 'node.opt.updateproc') {
    	data = JSON.parse(data);
    	sio.sockets.emit('opt.updateproc',data);       
    }else if(ch == 'node.opt.updateinfo') {
    	sio.sockets.emit('opt.updateinfo',data);       
    }else if(ch == 'node.opt.preconfig_one') {
    	if(data == ""){
    		sio.sockets.emit('opt.preconfig_one',data);
    	}else{
    		data = JSON.parse(data);
            sio.sockets.emit('opt.preconfig_one',data);
    	}        
    }else if(ch == 'node.opt.preconfig_batch') {
    	data = JSON.parse(data);
    	sio.sockets.emit('opt.preconfig_batch',data);       
    }else if(ch == 'node.opt.preconfig_all') {
    	data = JSON.parse(data);
    	sio.sockets.emit('opt.preconfig_all',data);       
    }else if(ch == 'node.opt.cbatreset') {
    	sio.sockets.emit('cbatreset',data);       
    }else if(ch == 'node.opt.ftpinfo') {
    	data = JSON.parse(data);
    	sio.sockets.emit('ftpinfo',data);       
    }else if(ch == 'node.tree.hfcbase') {
    	sio.sockets.emit('hfcbase',data);       
    }else if(ch == 'node.tree.hfcrealtime') {
    	if(data == ""){
    		sio.sockets.emit('hfcrealtime',data);
    	}else{
    		data = JSON.parse(data);
            sio.sockets.emit('hfcrealtime',data);
    	}     
    }else if(ch == 'node.opt.userinfo') {
    	data = JSON.parse(data);
    	sio.sockets.emit('userinfo',data);       
    }else if(ch == 'node.opt.userlist') {
    	data = JSON.parse(data);
    	sio.sockets.emit('userlist',data);       
    }else if(ch == 'node.opt.pwdmodify') {
    	sio.sockets.emit('pwdmodify',data);       
    }else if(ch == 'node.opt.userres') {
    	sio.sockets.emit('userres',data);       
    }else if(ch == 'node.opt.getflag') {
    	sio.sockets.emit('getflag',data);       
    }else if(ch == 'node.opt.checkallcnusres') {
    	sio.sockets.emit('checkallcnusres',data);       
    }
});

sio.sockets.on('connection', function (socket) {
  console.log('socket connected!' + socket.id);

  socket.on('initDynatree', function (data) {
     console.log('nodeserver: inittree');
     publish.publish('servicecontroller.treeinit', 'inittree');
  });
  
  socket.on('historyalarmall', function (data) {
     console.log('from client: nodeserver: historyalarmall');
     publish.publish('servicecontroller.gethistoryalarm', '{"istart":1, "ilen": 1000}');
  });
  
//历史告警导航
  socket.on('historypage', function (data) {
	  console.log('nodeserver: historypage==='+data);
	  publish.publish('servicecontroller.historypage', data);
  });
  
//历史告警导航下一页
  socket.on('historynext', function (data) {
	  console.log('nodeserver: historynext==='+data);
	  publish.publish('servicecontroller.historynext', data);
  });
  
//历史告警导航上一页
  socket.on('historypre', function (data) {
	  console.log('nodeserver: historypre==='+data);
	  publish.publish('servicecontroller.historypre', data);
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
  
  socket.on('opt.checkallcnus', function (data) {
	  	 console.log('nodeserver: opt.checkallcnus==='+data);
	     publish.publish('servicecontroller.opt.checkallcnus', data);
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
//获得要升级的头端设备
  socket.on('opt.checkedcbats', function (data) {
	  	 console.log('nodeserver: opt.checkedcbats==='+data);
	     publish.publish('servicecontroller.opt.checkedcbats', data);
  });
  //升级头端设备
  socket.on('opt.ftpupdate', function (data) {
	  	 console.log('nodeserver: opt.ftpupdate==='+data);
	     publish.publish('servicecontroller.opt.ftpupdate', data);
  });
  //查询升级进度
  socket.on('opt.updateproc', function (data) {
	  	 console.log('nodeserver: opt.updateproc==='+data);
	     publish.publish('servicecontroller.opt.updateproc', data);
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

//头端恢复出厂设置
  socket.on('cbatreset', function (data) {
	  console.log('nodeserver: cbatreset==='+data);
	  publish.publish('servicecontroller.opt.cbatreset', data);
  });
//FTP信息
  socket.on('opt.ftpinfo', function (data) {
	  console.log('nodeserver: opt.ftpinfo==='+data);
	  publish.publish('servicecontroller.opt.ftpinfo', data);
  });
//删除设备节点
  socket.on('delnode', function (data) {
	  console.log('nodeserver: delnode==='+data);
	  publish.publish('servicecontroller.delnode', data);
  });
//初始化升级信息
  socket.on('opt.updatereset', function (data) {
	  console.log('nodeserver: updatereset==='+data);
	  publish.publish('servicecontroller.opt.updatereset', data);
  });
//HFC详细信息
  socket.on('hfcdetail', function (data) {
	  console.log('nodeserver: hfcdetail==='+data);
	  publish.publish('servicecontroller.hfcdetail', data);
  });
//HFC基本信息提交
  socket.on('hfc_baseinfo', function (data) {
	  console.log('nodeserver: hfc_baseinfo==='+data);
	  publish.publish('servicecontroller.hfc_baseinfo', data);
  });
//HFC实时参数获取
  socket.on('hfcrealtime', function (data) {
	  console.log('nodeserver: hfcrealtime==='+data);
	  publish.publish('servicecontroller.hfcrealtime', data);
  });
//一般用户信息获取
  socket.on('userinfo', function (data) {
	  console.log('nodeserver: userinfo==='+data);
	  publish.publish('servicecontroller.userinfo', data);
  });
//用户列表获取
  socket.on('userlist', function (data) {
	  console.log('nodeserver: userlist==='+data);
	  publish.publish('servicecontroller.userlist', data);
  });
//用户密码修改
  socket.on('pwd_modify', function (data) {
	  console.log('nodeserver: pwd_modify==='+data);
	  publish.publish('servicecontroller.pwd_modify', data);
  });
//用户删除
  socket.on('userdel', function (data) {
	  console.log('nodeserver: userdel==='+data);
	  publish.publish('servicecontroller.userdel', data);
  });
//创建用户
  socket.on('usercreate', function (data) {
	  console.log('nodeserver: usercreate==='+data);
	  publish.publish('servicecontroller.usercreate', data);
  });
//获取用户权限
  socket.on('getflag', function (data) {
	  console.log('nodeserver: getflag==='+data);
	  publish.publish('servicecontroller.getflag', data);
  });
//用户权限修改
  socket.on('PermissionChange', function (data) {
	  console.log('nodeserver: PermissionChange==='+data);
	  publish.publish('servicecontroller.PermissionChange', data);
  });
  
  socket.on('channel', function(ch) {
      //console.log('channel receive ch=='+ch);
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
