package com.stan.wen9000.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.snmp4j.smi.OID;
import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.stan.wen9000.domain.Alarm;
import com.stan.wen9000.domain.Cbat;
import com.stan.wen9000.domain.Cbatinfo;
import com.stan.wen9000.domain.Cnu;
import com.stan.wen9000.reference.EocDeviceType;
import com.stan.wen9000.service.AlarmService;
import com.stan.wen9000.service.CbatService;
import com.stan.wen9000.service.CbatinfoService;
import com.stan.wen9000.service.CnuService;


public class ServiceAlarmProcessor {

	private static final String PERSIST_CBAT_QUEUE_NAME = "alarm_queue";
	private static SnmpUtil util = new SnmpUtil();
	
	@Autowired
	AlarmService alarmsr;
	
	@Autowired
	CbatService cbatsr;
	
	@Autowired
	CbatinfoService cbatinfosr;
	
	@Autowired
	CnuService cnusr;
	
	EocDeviceType devicetype;

	public void dostart() {

		//log.info("[#3] ..... service alarm");
		System.out.println("[#3] ..... service Alarm starting");
		servicestart();

	}

	public void servicestart() {

		QueueingConsumer consumer = null;
		Channel channel = null;
		try {

			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("localhost");
			Connection connection = factory.newConnection();
			channel = connection.createChannel();

			channel.queueDeclare(PERSIST_CBAT_QUEUE_NAME, true, false, false,
					null);
			System.out
					.println(" [*] Waiting for messages. To exit press CTRL+C");

			channel.basicQos(1);

			consumer = new QueueingConsumer(channel);
			channel.basicConsume(PERSIST_CBAT_QUEUE_NAME, false, consumer);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		while (true) {

			String message = null;
			QueueingConsumer.Delivery delivery;
			try {

				delivery = consumer.nextDelivery();

				message = new String(delivery.getBody());

			} catch (Exception e) {
				// e.printStackTrace();
				continue;
			}

			System.out.println(" [x] Alarm_service Received '" + message + "'");
			savealarm(message);
			// System.out.println(" [x] Alarm_service Done");

			try {
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			} catch (Exception e) {
				// e.printStackTrace();
				continue;
			}
		}

	}

	public void savealarm(String message) {
		int code = Integer.parseInt(message.substring(0, 6));
		if (code == 200002) {
//			HeartBean heart = new HeartBean();
//			int index1 = 0;
//			int index2 = 0;
//			try {
//				index1 = message.indexOf(";");
//				heart.setCode(Integer.parseInt(message.substring(0, index1)));
//
//				index2 = message.indexOf(";", index1 + 1);
//				heart.setCbatsys(message.substring(index1 + 1, index2));
//
//				index1 = index2;
//				index2 = message.indexOf(";", index1 + 1);
//				heart.setCnusys(message.substring(index1 + 1, index2));
//
//				index1 = index2;
//				heart.setInfo(message.substring(index1 + 1));
//
//				doheart(heart);
//				return;
//			} catch (Exception e) {
//				e.printStackTrace();
//				return;
//			}
		} else {
			Alarm alarm = new Alarm();
			DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			int index1 = 0;
			int index2 = 0;

			try {
				index1 = message.indexOf("|");
				alarm.setAlarmcode(Integer.parseInt(message
						.substring(0, index1)));

				index2 = message.indexOf("|", index1 + 1);
				alarm.setAlarmtype(Integer.parseInt(message.substring(
						index1 + 1, index2)));

				index1 = index2;
				index2 = message.indexOf("|", index1 + 1);
				alarm.setCltindex(Integer.parseInt(message.substring(
						index1 + 1, index2)));

				index1 = index2;
				index2 = message.indexOf("|", index1 + 1);
				alarm.setCnuindex(Integer.parseInt(message.substring(
						index1 + 1, index2)));

				index1 = index2;
				index2 = message.indexOf("|", index1 + 1);
				alarm.setItemnumber(Integer.parseInt(message.substring(
						index1 + 1, index2)));

				index1 = index2;
				index2 = message.indexOf("|", index1 + 1);
				alarm.setCbatmac(message.substring(index1 + 1, index2));

				index1 = index2;
				index2 = message.indexOf("|", index1 + 1);
				alarm.setAlarmvalue(Integer.parseInt(message.substring(
						index1 + 1, index2)));

				index1 = index2;
				index2 = message.indexOf("|", index1 + 1);
				alarm.setTrapinfo(message.substring(index1 + 1, index2));

				index1 = index2;
				index2 = message.indexOf("|", index1 + 1);
				alarm.setRealtime(format1.parse(message.substring(index1 + 1,
						index2)));

				index1 = index2;
				alarm.setTimeticks(message.substring(index1 + 1));
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			switch (alarm.getAlarmcode()) {
			case 200901:
			case 200902:
			case 200909:
				// �¼�
				alarm.setAlarmlevel(7);
				break;
			case 200903:
				alarm.setTrapinfo("环境温度告警");
				alarm.setAlarmlevel(1);
				break;
			case 200904:
				alarm.setTrapinfo("CBAT管理CPU负载过高告警以及恢复");
				alarm.setAlarmlevel(1);
				break;
			case 200905:
				alarm.setTrapinfo("CBAT内存利用率过高告警");
				alarm.setAlarmlevel(1);
				break;
			case 200910:
			case 200911:
				alarm.setAlarmlevel(1);
				break;
			case 200906:
				alarm.setTrapinfo("噪声过高告警");
				alarm.setAlarmlevel(2);
				break;
			case 200907:
				alarm.setTrapinfo("链路层速率告警");
				alarm.setAlarmlevel(2);
				break;
			case 200908:
				alarm.setTrapinfo("物理层速率告警");
				alarm.setAlarmlevel(2);
				break;
			case 200913:
				// ����
				alarm.setTrapinfo("用户数量超限");
				alarm.setAlarmlevel(2);
				break;
			case 200914:
				if(alarm.getAlarmvalue()==1)
				{
					alarm.setTrapinfo("阻止 CNU注册 成功");
					alarm.setAlarmlevel(6);
				}
				else
				{
					alarm.setTrapinfo("阻止 CNU注册 失败");
					alarm.setAlarmlevel(3);
				}
				break;
			case 200915:
			case 200916:
				alarm.setAlarmlevel(3);
				break;
			case 200918:
				if(alarm.getAlarmvalue()==1)
				{
					alarm.setTrapinfo("KICK OFF CNU 成功");
					alarm.setAlarmlevel(6);
				}
				else
				{
					alarm.setTrapinfo("KICK OFF CNU 失败");
					alarm.setAlarmlevel(3);
				}
				break;
			case 200919:
				alarm.setAlarmlevel(6);
				alarm.setTrapinfo("CNU强制重新注册");
				break;
			case 200920:			
				// 告警
				alarm.setAlarmlevel(6);
				break;
			case 200921:
				alarm.setAlarmlevel(2);
				break;
			case 200912:
				alarm.setAlarmlevel(5);
				alarm.setTrapinfo("非法用户试图注册");
				break;
			}

			doalarm(alarm);
			
			if ((alarm.getAlarmcode() == 200000)
					|| (alarm.getAlarmcode() == 200001)) {
				return;
			}

			alarmsr.saveAlarm(alarm);

			
		}

	}

	public void doalarm(Alarm alarm) {
		switch (alarm.getAlarmcode()) {
		case 200001:
		case 200902: { // cnu status alarm
			//docnualarm(alarm);
			break;
		}
		case 200000:
		case 200920:{ // cbat online alarm

			//docbatalarm(alarm);
			break;
		}
		case 200909: {
			//doupgrade(alarm);
			
			break;
		}
		default:
			//dosetip(alarm);
			break;
		}
	}
	
	public void docbatalarm(Alarm alarm) {
		int index1 = 0;
		int index2 = 0;
		long cbattype = 0;
		String cbatip = "";

		try {
			index1 = alarm.getTrapinfo().indexOf("]", 1);
			cbatip = alarm.getTrapinfo().substring(1, index1);
			index2 = alarm.getTrapinfo().indexOf("]", index1 + 1);
			cbattype = Long.parseLong(alarm.getTrapinfo().substring(index1 + 2,
					index2));
		} catch (Exception e) {
			// System.out.println(" Parse cnu messerge error");
			return;
		}
		Cbat cbat = cbatsr.findByMac(alarm.getCbatmac());
		if(cbat != null)
		{
			 // 已发现头端上线，active状态置1，刷新设备ip；
			if ((cbat.getIp().equalsIgnoreCase(cbatip))
					&& (alarm.getAlarmcode() != 200920)) {				
				return;
			} else { // 头端信息有变更				
				try {
					cbat.getCbatinfo().setAppVer(util.getStrPDU(cbatip, "161",
							new OID(new int[] { 1, 3, 6, 1, 4, 1, 36186, 8,
									4, 4, 0 })));
				} catch (Exception e) {
				}
				cbat.setActive(alarm.getAlarmvalue()==1?true:false);
				cbat.setIp(cbatip);
				cbatsr.saveCbat(cbat);
			}
			alarm.setTrapinfo("标识为"+cbat.getLabel() +"的头端上线了");
			
		
		}else{
			// 新头端上线
			Cbat newcbat = new Cbat();
			newcbat.setActive(true);
			switch ((int) cbattype) {
			case 1:
				// WEC-3501I X7
				newcbat.setDeviceType(devicetype.WEC_3501I_E31);
				break;
			case 2:
				// WEC-3501I E31
				newcbat.setDeviceType(devicetype.WEC_3501I_E31);
				break;
			case 3:
				// WEC-3501I Q31
				newcbat.setDeviceType(devicetype.WEC_3501I_E31);
				break;
			case 4:
				// WEC-3501I C22
				newcbat.setDeviceType(devicetype.WEC_3501I_E31);
				break;
			case 5:
				// WEC-3501I S220
				newcbat.setDeviceType(devicetype.WEC_3501I_E31);
				break;
			case 6:
				// WEC-3501I S60
				newcbat.setDeviceType(devicetype.WEC_3501I_E31);
				break;
			default:
				newcbat.setDeviceType(devicetype.WEC_3501I_E31);
				break;
			}
			newcbat.setIp(cbatip);
			newcbat.setMac(alarm.getCbatmac().toUpperCase());
			newcbat.setLabel(alarm.getCbatmac().toUpperCase());
			
			Cbatinfo cbatinfo = new Cbatinfo();
			cbatinfo.setLabel(alarm.getCbatmac().toUpperCase());
			cbatinfo.setBootVer("cml-boot-v1.1.0 for linux sdk");			
			try {
				cbatinfo.setAgentPort(util.getINT32PDU(cbatip, "161", new OID(new int[] {
						1,3,6,1,4,1,36186,8,2,7,0})));
				cbatinfo.setAppVer(util.getStrPDU(cbatip, "161", new OID(new int[] {
						1, 3, 6, 1, 4, 1, 36186, 8, 4, 4, 0 })));			
				cbatinfo.setMvId((long) util.getINT32PDU(cbatip, "161", new OID(
						new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 5, 0 })));
				cbatinfo.setMvStatus(util.getINT32PDU(cbatip, "161", new OID(
						new int[] { 1, 3, 6, 1, 4, 1, 36186, 8, 5, 4, 0 }))==1?true:false);
			} catch (Exception e) {
			}
			cbatinfosr.saveCbatinfo(cbatinfo);
			
			newcbat.setCbatinfo(cbatinfo);			
			cbatsr.saveCbat(newcbat);
		}
	}
	
	public void docnualarm(Alarm alarm) {
		int isOnline = -1;
		String cnumac = "";
		int cnutype = 0;
		int index1 = 0;
		int index2 = 0;
		// savedb
		try {
			index1 = alarm.getTrapinfo().indexOf("[");
			index2 = alarm.getTrapinfo().indexOf("]");
			cnumac = alarm.getTrapinfo().substring(0, index1).toUpperCase()
					.trim();
			cnutype = Integer.parseInt(alarm.getTrapinfo().substring(
					index1 + 1, index2));

			isOnline = alarm.getAlarmvalue();
		} catch (Exception e) {
			// e.printStackTrace();
			return;
		}

		// CNU上线
		if (1 == isOnline) {
			
			doOnline(alarm, cnumac, cnutype);

		} else // CNU offline
		{
			//doOffline(alarm, cnumac, cnutype);

		}
	}
	
	public void doOnline(Alarm alarm, String cnumac, int cnutype) {
		Cbat cbat = null;
		Cnu cnu = cnusr.findByMac(cnumac);

		int result = 0;
		if(cnu == null)
		{
			result = 1;
		}
		
		if (1 == result) {
			// add new cnu
			//addNewCnu(alarm, cnumac, cnutype);
			return;
		}


		if (cnu.getCbat().getMac()
				.equalsIgnoreCase(alarm.getCbatmac().toUpperCase()) == false) {
			System.out.println("========>>>发现CNU移机操作CNU MAC:" + cnumac);
			Alarm alarm1 = new Alarm();
			try {
				cbat = cbatsr.findByMac(alarm.getCbatmac());
				alarm1.setTrapinfo("头端标识为"+cnu.getCbat().getLabel()+"下的 CNU[" + cnumac+"]移动到标识为"+cbat.getLabel()+"的头端下");
				cnu.setCbat(cbat);
			} catch (Exception e) {
				return;
			}			
			// 写入告警			
			alarm1.setAlarmcode(200931);
			alarm1.setAlarmlevel(5);
			alarm1.setAlarmvalue(0);
			alarm1.setAlarmtype(6);
			alarm1.setCbatip(cbat.getIp());
			alarm1.setCbatmac(alarm.getCbatmac().toUpperCase());
			alarm1.setCnumac(cnumac);
			alarmsr.saveAlarm(alarm1);
		}
		alarm.setCbatip(cnu.getCbat().getIp());
		alarm.setCnumac(cnumac);
		alarm.setTrapinfo("头端标识为"+cnu.getCbat().getLabel()+"下的CNU["+ cnumac+"]上线了" );
		//cnu.setDevcnuId((long) alarm.getCnuindex());
		//cnu.setActive(true);
		cnusr.saveCnu(cnu);

	}
	
	
}
