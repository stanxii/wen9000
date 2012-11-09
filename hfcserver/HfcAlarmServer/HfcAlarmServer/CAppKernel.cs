using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Net.NetworkInformation;
using System.Text;
using adventnet.snmp.mibs;
using adventnet.snmp.snmp2;
using System.Threading;

namespace HfcAlarmServer
{
    class CAppKernel : adventnet.snmp.snmp2.SnmpSession, System.IDisposable, adventnet.snmp.snmp2.SnmpClient
    {
        #region Property MibOperObj

        private static MibOperations _MibOperObj;
        /// <summary>
        /// 获取MIB库操作对象。
        /// </summary>
        public static MibOperations MibOperObj
        {
            get { return _MibOperObj; }
        }

        #endregion

        #region Property ParamsHash

        private static Hashtable _ParamsHash;
        /// <summary>
        /// 获取MIB系统参数的哈希表对象。
        /// </summary>
        public static Hashtable ParamsHash
        {
            get { return _ParamsHash; }
        }

        #endregion

        #region Property AppKernel
        private static CAppKernel _CAppKernel = null;
        /// <summary>
        /// 获取应用程序内核对象的引用。
        /// 应用程序内核对象是所有资源、对象的总管理器。
        /// </summary>
        public static CAppKernel AppKernel
        {
            get { return CAppKernel._CAppKernel; }
        }
        #endregion

        #region Property DatabaseEngine
        private static CDatabaseEngine _DatabaseEngine = null;
        /// <summary>
        /// 获取数据库引擎对象的引用。
        /// </summary>
        public static CDatabaseEngine DatabaseEngine
        {
            get { return CAppKernel._DatabaseEngine; }
        }
        #endregion

        #region Property AsyncClientID
        private readonly int _AsyncClientID;
        /// <summary>
        /// 获取SNMP引擎的异步通信客户端ID。
        /// </summary>
        public int AsyncClientID
        {
            get { return _AsyncClientID; }
        }
        #endregion

        public CAppKernel()
            : base(new SnmpAPI(false))
        {
            //创建数据库引擎对象。
            CAppKernel._DatabaseEngine = new CDatabaseEngine();

            //创建MIB系统的参数哈希表。
            CAppKernel._ParamsHash = new Hashtable(800, 0.75F);

            #region 从数据库导入MIB系统参数到"ParamsHash"哈希表。

            List<CDatabaseEngine.ParmsTableRow> paramslist = CAppKernel.DatabaseEngine.ParmsTableGetAllRows();
            if (paramslist != null)
            {
                foreach (CDatabaseEngine.ParmsTableRow p in paramslist)
                {
                    CAppKernel.AddParamInfo(p);
                }
            }
            else
            {
                throw new Exception("DB Access Error!!");
            }

            #endregion

            #region 从文件系统加载系统所必需的MIB库。

            //创建MIB库操作对象。
            CAppKernel._MibOperObj = new MibOperations();
            CAppKernel.MibOperObj.loadMibModule(@"mibs\NSCRTV-FOBETMOC-WOS2000-MIB");
            CAppKernel.MibOperObj.loadMibModule(@"mibs\NSCRTV-HFCEMS-ALARMS-MIB");
            CAppKernel.MibOperObj.loadMibModule(@"mibs\NSCRTV-HFCEMS-COMMON-MIB");
            CAppKernel.MibOperObj.loadMibModule(@"mibs\NSCRTV-HFCEMS-PROPERTY-MIB");
            CAppKernel.MibOperObj.loadMibModule(@"mibs\NSCRTV-HFCEMS-OPTICALSWITCH-MIB");
            CAppKernel.MibOperObj.loadMibModule(@"mibs\NSCRTV-HFCEMS-OPTICALAMPLIFIER-MIB");
            CAppKernel.MibOperObj.loadMibModule(@"mibs\NSCRTV-HFCEMS-OPTICALTRANSMITTERDIRECTLY-MIB");
            CAppKernel.MibOperObj.loadMibModule(@"mibs\NSCRTV-HFCEMS-DOWNSTREAMOPTICALRECEIVER-MIB");
            CAppKernel.MibOperObj.loadMibModule(@"mibs\NSCRTV-HFCEMS-DOWNSTREAMOPTICALRECEIVER-MIB");
            CAppKernel.MibOperObj.loadMibModule(@"mibs\NSCRTV-HFCEMS-FIBERNODE-MIB");

            #endregion

            #region 创建SNMP协议通信引擎对象。
            //创建SNMP协议通信引擎对象。
            UDPProtocolOptions myUdpOpt = new UDPProtocolOptions();
            myUdpOpt.LocalPort = 2250;
            base.ProtocolOptions = myUdpOpt;
            base.IsBackground = true;
            base.Callbackthread = true;
            base.PacketBufferSize = 256 * 1024;
            base.TimeToWait = 0;
            base.Timeout = 300;
            base.Retries = 2;
            base.Community = "public";
            base.WriteCommunity = "public";
            base.AutoInformResponse = true;
            base.BroadcastFeatureEnable = true;
            //创建用于异步测试设备是否在线的SNMP通信客户端对象。
            this.myTestOnlineClient = new TestOnlineClient();
            this.myTestOnlineClient.TestOnlineMessageHandler = this.TestOnlineMessageCallback;
            //接收Trap包的SnmpClient对象
            base.AddSnmpClient(this);
            //接收Snmp异步数据包的SnmpClient对象
            this._AsyncClientID = base.AddSnmpClientWithID(this);
            this.TestOnlineClientID = base.AddSnmpClientWithID(this.myTestOnlineClient);
            //打开SNMP引擎的事务处理。
            this.Open();
            Console.WriteLine("Listenint Trap ...........");
            #endregion
    
        }

        #region 实现[adventnet.snmp.snmp2.SnmpClient]接口的代码。

        public bool Authenticate(adventnet.snmp.snmp2.SnmpPDU pdu, string community)
        {
            return true;
        }

        public bool Callback(adventnet.snmp.snmp2.SnmpSession session, adventnet.snmp.snmp2.SnmpPDU pdu, int requestID)
        {
            if (pdu == null) return false;

            if (SnmpAPI.TRP_REQ_MSG == pdu.Command)
            {
                ProcessTrapRequestPduHandler(pdu, requestID);
            }
            else if (SnmpAPI.GET_RSP_MSG == pdu.Command)
            {
                ProcessGetResponsePduHandler(pdu, requestID);
            }

            return true;
        }

        public void DebugPrint(string debugOutput)
        {

        }

        #endregion

        private void ProcessGetResponsePduHandler(adventnet.snmp.snmp2.SnmpPDU pdu, int requestID)
        {
            //this.ReceivedAgentSearchRequest(pdu);
        }

        #region 定义异步处理TrapRequest包和GetResponse包的函数。

        /// <summary>
        /// 当应用程序接收到SNMP协议的TRAP数据包时，会调用该函数。
        /// </summary>
        /// <param name="pdu"></param>
        /// <param name="requestID"></param>
        private void ProcessTrapRequestPduHandler(adventnet.snmp.snmp2.SnmpPDU pdu, int requestID)
        {
            try
            {
                if (pdu.TrapType != 6)//该字段不等于6，就说明是RFC标准TRAP定义。
                {
                    ProcessGenericTraps(pdu);
                }
                else if (pdu.Enterprise.Equals(SupportedTrapEnterprises.nscrtvHFCemsTree))
                {
                    ProcessHFCTraps(pdu);
                }
                else if (pdu.Enterprise.Equals(SupportedTrapEnterprises.wos2kIdent))
                {
                    ProcessWosTraps(pdu);
                }
                else if (pdu.Enterprise.Equals(SupportedTrapEnterprises.wos3kIdent))
                {
                    ProcessWos3kTraps(pdu);
                }
                else
                {
                    return;
                }
            }
            catch (Exception ex)
            {
                Console.Write(ex.Source);
            }
        }

        #endregion

        #region 处理接收到的可识别的TRAP信息。

        private void ProcessGenericTraps(SnmpPDU pdu)
        {
            string trapstring = string.Empty;

            switch (pdu.TrapType)
            {
                case 0:
                    trapstring = "Standard cold start";
                    break;
                case 1:
                    trapstring = "Standard warm start";
                    break;
                case 2:
                    trapstring = "Standard disconnect";
                    break;
                case 3:
                    trapstring = "Standard connected";
                    break;
                /*
                case 4:
                    trapstring = "标准签名错误";
                    break;
                 */
                case 5:
                    trapstring = "Standard target lost";
                    break;
                default:
                    return;
            }

            UdpSend(trapstring);
            //lock (CAppKernel.ViewTrapLog)
            //{
            //    CAppKernel.ViewTrapLog.InsertTrapLog(CDatabaseEngine.TrapLogTypes.GenericTrap, pdu.Address.ToString(), trapstring,
            //        DateTime.Now);
            //}
        }

        private void ProcessHFCTraps(SnmpPDU pdu)
        {
            switch (pdu.SpecificType)
            {
                case 0://hfcColdStart
                    ParseTrapHfcColdStart(pdu);
                    return;
                case 1://hfcAlarmEvent
                    ParseTrapHfcAlarmEvent(pdu);
                    return;
                case 2://hfcWarmStart
                    ParseTrapHfcWarmStart(pdu);
                    return;
                case 8686://osSwitchEvent
                    ParseTrapHfcOsSwitchEvent(pdu);
                    return;
            }
        }

        private void ProcessWosTraps(SnmpPDU pdu)
        {
            switch (pdu.SpecificType)
            {
                case 1://wosTrapRestart
                    ParseTrapWosTrapRestart(pdu);
                    return;
                case 2://wosTrapDeviceUp
                    ParseTrapWosTrapDeviceUp(pdu);
                    return;
                case 3://wosTrapDeviceDown
                    ParseTrapWosTrapDeviceDown(pdu);
                    return;
            }
        }
        private void ProcessWos3kTraps(SnmpPDU pdu)
        {
            switch (pdu.SpecificType)
            {
                case 1://wosTrapRestart
                    ParseTrapWosTrapRestart(pdu);
                    return;
                case 2://wosTrapDeviceUp
                    ParseTrapWosTrapDeviceUp(pdu);
                    return;
                case 3://wosTrapDeviceDown
                    ParseTrapWosTrapDeviceDown(pdu);
                    return;
                case 4:                             ///////////////  //修改
                    ParseTrapWos3kAlarmEvent(pdu);
                    return;
            }
        }

        #endregion

        #region 具体解析所识别的TRAP消息

        private void ParseTrapHfcColdStart(SnmpPDU pdu)
        {
            if (pdu.VariableBindings.Count < 2) return;
            string trapstring = "HFC cold start,";

            SnmpVar phy = pdu.GetVariable(0);
            //trapstring += "物理地址：" + CAppKernel.GetPhyAddressString(AdventNetUtil.ToByteArray(phy.ToBytes()));
            //trapstring += " 逻辑ID：" + pdu.GetVariable(1).ToString();
            trapstring = CAppKernel.GetPhyAddressString(AdventNetUtil.ToByteArray(phy.ToBytes())) + "|" + trapstring;
            UdpSend(trapstring);
            //lock (CAppKernel.ViewTrapLog)
            //{
            //    CAppKernel.ViewTrapLog.InsertTrapLog(CDatabaseEngine.TrapLogTypes.HfcColdStart, pdu.Address.ToString(), trapstring,
            //        DateTime.Now);
            //}
        }

        private void ParseTrapHfcAlarmEvent(SnmpPDU pdu)
        {
            if (pdu.VariableBindings.Count < 3) return;
            string trapstring = "HFC Parameter alarm,";

            SnmpVar phy = pdu.GetVariable(0);
            //trapstring += "物理地址：" + CAppKernel.GetPhyAddressString(AdventNetUtil.ToByteArray(phy.ToBytes()));
            //trapstring += " 逻辑ID：" + pdu.GetVariable(1).ToString();
            byte[] alarminfo = AdventNetUtil.ToByteArray(pdu.GetVariable(2).ToBytes());
            if (alarminfo.Length < 6) return;
            trapstring += " AlarmType:" + CAppKernel.GetAlarmEnumString(alarminfo[4]);
            byte[] alarmvb = new byte[alarminfo.Length - 6];
            Array.Copy(alarminfo, 6, alarmvb, 0, alarmvb.Length);
            SnmpOID oid;
            int val;
            if (CAppKernel.ParseAlarmInform(alarmvb, out oid, out val))
            {
                MibNode fnode = CAppKernel.MibOperObj.getNearestNode(oid);
                if (fnode != null)
                {
                    CDatabaseEngine.ParmsTableRow pararmrow = CAppKernel.GetParamInfo(fnode.label);
                    int[] nodeoid = fnode.getOID();
                    int[] oidarray = oid.ToIntArray();
                    string exstr = string.Empty;
                    if (oidarray.Length > nodeoid.Length)
                    {
                        for (int i = nodeoid.Length; i < oidarray.Length; i++)
                        {
                            exstr += '.' + oidarray[i].ToString();
                        }
                    }
                    if (pararmrow != null)
                    {
                        trapstring += " Parameter:" + pararmrow.ParamDispText + exstr;

                        if (pararmrow.IsFormatEnable)
                        {
                            if (pararmrow.ParamMibLabel == "fnReverseOpticalPower")
                            {
                                float tmpf = val * pararmrow.FormatCoff;
                                trapstring += " Value:" + tmpf.ToString(pararmrow.FormatText);// +pararmrow.FormatUnit;
                            }
                            else
                            {
                                float tmpf = val * pararmrow.FormatCoff;
                                trapstring += " Value" + tmpf.ToString(pararmrow.FormatText) + pararmrow.FormatUnit;
                            }
                        }
                        trapstring = CAppKernel.GetPhyAddressString(AdventNetUtil.ToByteArray(phy.ToBytes())) + "|" + trapstring;
                        UdpSend(trapstring);
                        //lock (CAppKernel.ViewTrapLog)
                        //{
                        //    CAppKernel.ViewTrapLog.InsertTrapLog(GetTrapLogType(alarminfo[4]), pdu.Address.ToString(), trapstring,
                        //        DateTime.Now);
                        //}
                    }
                    else
                    {
                        trapstring += " Parameter:" + fnode.label + exstr;
                        trapstring += " Value:" + val.ToString();
                        trapstring = CAppKernel.GetPhyAddressString(AdventNetUtil.ToByteArray(phy.ToBytes())) + "|" + trapstring;
                        UdpSend(trapstring);
                        //lock (CAppKernel.ViewTrapLog)
                        //{
                        //    CAppKernel.ViewTrapLog.InsertTrapLog(GetTrapLogType(alarminfo[4]), pdu.Address.ToString(), trapstring,
                        //        DateTime.Now);
                        //}
                    }
                }
                else
                {
                    trapstring += " Parameter:" + oid.ToString();
                    trapstring += " Value:" + val.ToString();
                    trapstring = CAppKernel.GetPhyAddressString(AdventNetUtil.ToByteArray(phy.ToBytes())) + "|" + trapstring;
                    UdpSend(trapstring);
                    //lock (CAppKernel.ViewTrapLog)
                    //{
                    //    CAppKernel.ViewTrapLog.InsertTrapLog(GetTrapLogType(alarminfo[4]), pdu.Address.ToString(), trapstring,
                    //        DateTime.Now);
                    //}
                }
            }
        }

        private void ParseTrapHfcWarmStart(SnmpPDU pdu)
        {
            if (pdu.VariableBindings.Count < 2) return;
            string trapstring = "HFC warm start";

            SnmpVar phy = pdu.GetVariable(0);
            //trapstring += "物理地址：" + CAppKernel.GetPhyAddressString(AdventNetUtil.ToByteArray(phy.ToBytes()));
            //trapstring += " 逻辑ID：" + pdu.GetVariable(1).ToString();
            trapstring = CAppKernel.GetPhyAddressString(AdventNetUtil.ToByteArray(phy.ToBytes())) + "|" + trapstring;
            UdpSend(trapstring);
            //lock (CAppKernel.ViewTrapLog)
            //{
            //    CAppKernel.ViewTrapLog.InsertTrapLog(CDatabaseEngine.TrapLogTypes.HfcWarmStart, pdu.Address.ToString(), trapstring,
            //        DateTime.Now);
            //}
        }








        private void ParseTrapWos3kAlarmEvent(SnmpPDU pdu)//////////////////////////修改
        {
            if (pdu.VariableBindings.Count < 4) return;

            string trapstring = "WOS3000 Optical PlatForm Alarm,";


            int slotnum = (int)pdu.GetVariable(1).ToValue();
            int subdevtype = Int32.Parse(pdu.GetVariable(2).ToString());
            if (subdevtype == 6 || subdevtype == 7)
                return;
            sbyte[] alarmtext = (pdu.GetVariable(3).ToBytes());
            string alarm = (string)pdu.GetVariable(3).ToValue();
            trapstring += slotnum.ToString() + " Slot's" + GetWosSubDevName(subdevtype);
            if (alarmtext.Length >= 24 && alarmtext.Length < 29)
            {
                trapstring += "   Alarmed";
                UdpSend(trapstring);
                //lock (CAppKernel.ViewTrapLog)
                //{
                //    CAppKernel.ViewTrapLog.InsertTrapLog(CDatabaseEngine.TrapLogTypes.WosTrapDeviceEvent, pdu.Address.ToString(), trapstring,
                //        DateTime.Now);
                //}
            }
            else if (alarmtext.Length >= 29)
            {
                trapstring += "   Alarm get right";
                UdpSend(trapstring);
                //lock (CAppKernel.ViewTrapLog)
                //{
                //    CAppKernel.ViewTrapLog.InsertTrapLog(CDatabaseEngine.TrapLogTypes.WosTrapDeviceNormal, pdu.Address.ToString(), trapstring,
                //        DateTime.Now);
                //}
            }
        }



        private void ParseTrapHfcOsSwitchEvent(SnmpPDU pdu)
        {
            if (pdu.VariableBindings.Count < 3) return;
            string trapstring = "Optical switch channel switching,";

            SnmpVar phy = pdu.GetVariable(0);
            //trapstring += "物理地址：" + CAppKernel.GetPhyAddressString(AdventNetUtil.ToByteArray(phy.ToBytes()));
            //trapstring += " 逻辑ID：" + pdu.GetVariable(1).ToString();
            trapstring += " switching to :" + ((int)pdu.GetVariable(2).ToValue() == 1 ? "A" : "B");
            trapstring = CAppKernel.GetPhyAddressString(AdventNetUtil.ToByteArray(phy.ToBytes())) + "|" + trapstring;
            UdpSend(trapstring);
            //lock (CAppKernel.ViewTrapLog)
            //{
            //    CAppKernel.ViewTrapLog.InsertTrapLog(CDatabaseEngine.TrapLogTypes.HfcOptSwitch, pdu.Address.ToString(), trapstring,
            //        DateTime.Now);
            //}
        }


        private void ParseTrapWosTrapRestart(SnmpPDU pdu)
        {
            if (pdu.VariableBindings.Count < 2) return;
            string trapstring = "WOS Optical PlatForm Reboot";

            SnmpVar phy = pdu.GetVariable(0);
            //trapstring += "物理地址：" + CAppKernel.GetPhyAddressString(AdventNetUtil.ToByteArray(phy.ToBytes()));
            trapstring += " SoftVersion：" + ((float)(((int)pdu.GetVariable(1).ToValue()) / 100.0f)).ToString("F");
            trapstring = CAppKernel.GetPhyAddressString(AdventNetUtil.ToByteArray(phy.ToBytes())) + "|" + trapstring;
            UdpSend(trapstring);
            //lock (CAppKernel.ViewTrapLog)
            //{
            //    CAppKernel.ViewTrapLog.InsertTrapLog(CDatabaseEngine.TrapLogTypes.WosTrapRestart, pdu.Address.ToString(), trapstring,
            //        DateTime.Now);
            //}
        }
        private void ParseTrapWosTrapDeviceUp(SnmpPDU pdu)
        {
            if (pdu.VariableBindings.Count < 1) return;
            string trapstring = "Detecting device online,";

            int[] oidarray = pdu.GetVariableBinding(0).ObjectID.ToIntArray();
            int slotnum = oidarray[oidarray.Length - 1];
            int subdevtype = oidarray[oidarray.Length - 2];

            trapstring +=slotnum.ToString() + " slot's" + GetWosSubDevName(subdevtype);
            UdpSend(trapstring);
            //lock (CAppKernel.ViewTrapLog)
            //{
            //    CAppKernel.ViewTrapLog.InsertTrapLog(CDatabaseEngine.TrapLogTypes.WosTrapDeviceUp, pdu.Address.ToString(), trapstring,
            //        DateTime.Now);
            //}
        }

        private void ParseTrapWosTrapDeviceDown(SnmpPDU pdu)
        {
            if (pdu.VariableBindings.Count < 1) return;
            string trapstring = "Detecting device offline,";

            int[] oidarray = pdu.GetVariableBinding(0).ObjectID.ToIntArray();
            int slotnum = oidarray[oidarray.Length - 1];
            int subdevtype = oidarray[oidarray.Length - 2];

            trapstring += slotnum.ToString() + " slot's" + GetWosSubDevName(subdevtype);
            UdpSend(trapstring);
            //lock (CAppKernel.ViewTrapLog)
            //{
            //    CAppKernel.ViewTrapLog.InsertTrapLog(CDatabaseEngine.TrapLogTypes.WosTrapDeviceDown, pdu.Address.ToString(), trapstring,
            //        DateTime.Now);
            //}
        }

        private static string GetWosSubDevName(int typenum)
        {
            switch (typenum)
            {
                case 2:
                    return "Power";
                case 3:
                    return "Optical Transmitter";
                case 4:
                    return "Optical Receiver";
                case 5:
                    return "Reverse Optical Receiver";
                case 6:
                    return "Optical Switch";
                case 7:
                    return "RF Switch";
                case 9:
                    return "EDFA";

                case 10:
                    return "Double Backup Optical Receiver";

                case 61:
                    return "Forward Optical Receiver";
                case 63:
                    return "Reverse Optical Receiver";
                case 65:
                    return "Optical Transmitter";
                default:
                    return "Unknown";
            }
        }

        #endregion

        #region 定义一些操作"ParamsHash"的函数。

        public static void AddParamInfo(CDatabaseEngine.ParmsTableRow param)
        {
            if (!ParamsHash.Contains(param.ParamMibLabel))
            {

                ParamsHash.Add(param.ParamMibLabel, param);

            }
        }

        public static CDatabaseEngine.ParmsTableRow GetParamInfo(string miblabel)
        {
            return (CDatabaseEngine.ParmsTableRow)ParamsHash[miblabel];
        }

        #endregion

        #region 这里定义本系统所支持的TRAP包的企业OID。

        public static class SupportedTrapEnterprises
        {
            public static SnmpOID wos2kIdent = new SnmpOID(".1.3.6.1.4.1.17409.8888.1");
            public static SnmpOID nscrtvHFCemsTree = new SnmpOID(".1.3.6.1.4.1.17409.1");
            public static SnmpOID wos3kIdent = new SnmpOID(".1.3.6.1.4.1.2000.1.3000");
        }

        #endregion

        #region 实现[System.IDisposable]接口的代码。
        private bool IsDisposed = false;
        /// <summary>
        /// 关闭应用程序内核。在应用程序退出时调用该方法来
        /// 结束所有终止内核有关的事务。
        /// </summary>
        public void Dispose()
        {
            if (IsDisposed)
            {
                return;
            }
            else
            {
                IsDisposed = true;
            }

            try
            {
                base.RemoveAllSnmpClients();
                SnmpAPI tmpAPI = this.SnmpAPI;
                base.Close();
                tmpAPI.Close();
            }
            catch (Exception ex)
            {
                Console.Write(ex.Source);
            }
            finally
            {
                if (CAppKernel.DatabaseEngine != null)
                {
                    CAppKernel.DatabaseEngine.Dispose();
                }
            }
        }
        #endregion

        #region 检测已注册和未注册设备在线状态的后台线程。

        public delegate void TestOnlineMessage(SnmpPDU pdu);
        private readonly int TestOnlineClientID;
        private readonly TestOnlineClient myTestOnlineClient;

        public class TestOnlineClient : adventnet.snmp.snmp2.SnmpClient
        {
            public TestOnlineMessage TestOnlineMessageHandler = null;

            public bool Authenticate(adventnet.snmp.snmp2.SnmpPDU pdu, string community)
            {
                return true;
            }

            public bool Callback(adventnet.snmp.snmp2.SnmpSession session, adventnet.snmp.snmp2.SnmpPDU pdu, int requestID)
            {
                if (pdu == null) return false;

                if (pdu.Command == SnmpAPI.GET_RSP_MSG)
                {
                    if (TestOnlineMessageHandler != null)
                        TestOnlineMessageHandler.Invoke(pdu);
                }

                return true;
            }

            public void DebugPrint(string debugOutput)
            {

            }
        }

        private void TestOnlineMessageCallback(SnmpPDU pdu)
        {
            
        }

        private Thread TestThreadObj = null;

        private void CreateOnlineThread()
        {
            TestThreadObj = new Thread(this.OnlineTestThread);
            TestThreadObj.Priority = ThreadPriority.Normal;
            TestThreadObj.IsBackground = true;
            TestThreadObj.Start();
        }

        private void OnlineTestThread()
        {
            //List<CDeviceBase> testdevlist = null;

            //Thread.Sleep(300);

            //SnmpPDU outpdu = new SnmpPDU();
            //outpdu.ClientID = this.TestOnlineClientID;
            //outpdu.Command = SnmpAPI.GET_REQ_MSG;
            //outpdu.AddNull(new SnmpOID(".1.3.6.1.2.1.1.2.0"));    //.2.0
            //outpdu.AddNull(new SnmpOID(".1.3.6.1.4.1.17409.1.3.1.1.0"));//HFC的commonNELogicalID
            //outpdu.AddNull(new SnmpOID(".1.3.6.1.4.1.17409.1.3.1.3.0"));//HFC的commonNEModelNumber
            //outpdu.AddNull(new SnmpOID(".1.3.6.1.4.1.17409.1.3.1.4.0"));//HFC的commonNESerialNumber
            //outpdu.AddNull(new SnmpOID(".1.3.6.1.4.1.17409.1.3.1.18.0"));
            ////需要实验确定

            //while (true)
            //{
            //    try
            //    {
            //        testdevlist = CAppKernel.GetAllDevices();

            //        if (testdevlist == null)
            //        {
            //            Thread.Sleep(100);
            //            continue;
            //        }

            //        for (int i = 0; i < 6; i++)
            //        {
            //            foreach (CDeviceBase dev in testdevlist)
            //            {
            //                if ((!dev.IsRegister) || (dev.NetType != CDatabaseEngine.NetTypes.DeviceIP))
            //                    continue;

            //                if (!dev.InTestMode)
            //                {
            //                    dev.OnlineCount = 0;
            //                    dev.InTestMode = true;
            //                }
            //                outpdu.ProtocolOptions = new UDPProtocolOptions(IPAddress.Parse(dev.NetAddress), 161);
            //                outpdu.Community = dev.ROCommunity;
            //                outpdu.Reqid = 0;
            //                AyncSendSnmpPdu(outpdu);
            //                Thread.Sleep(40);  // 5
            //            }
            //        }

            //        Thread.Sleep(40);

            //        foreach (CDeviceBase dev in testdevlist)
            //        {
            //            if (dev.InTestMode)
            //            {
            //                lock (dev)
            //                {
            //                    dev.InTestMode = false;
            //                    if (dev.OnlineCount <= 0)
            //                    {
            //                        dev.SetOnline(false);
            //                    }
            //                }
            //            }
            //        }
            //    }
            //    catch (Exception ex)
            //    {
            //        Console.Write(ex.Source);
            //    }

            //    Thread.Sleep(100);
            //}
        }

        #endregion

        public static string GetPhyAddressString(byte[] phyarray)
        {
            if (phyarray.Length != 6)
                return "000000000000";

            PhysicalAddress phyobj = new PhysicalAddress(phyarray);
            return phyobj.ToString();
        }

        public static string GetAlarmEnumString(byte num)
        {
            switch (num)
            {
                case 1:
                    return "NORMAL";
                case 2:
                    return "HIHI";
                case 3:
                    return "HI";
                case 4:
                    return "LO";
                case 5:
                    return "LOLO";
                case 6:
                    return "Discrete Major";
                case 7:
                    return "Discrete Minor";
                default:
                    return "Unkown Alarm";
            }
        }

        public static bool ParseAlarmInform(byte[] data, out SnmpOID oid, out int val)
        {
            oid = null; val = 0;
            if (data.Length < 2) return false;
            if (data[0] != 0x06) return false;

            int oidindex = 1, oidlen;
            if ((data[1] & 0x80) == 0)
            {
                oidlen = data[1] + 1;
            }
            else
            {
                int arrayindex = 2 + (data[1] & 0x7f);
                oidlen = 0;
                for (int i = 2; i < arrayindex; i++)
                {
                    oidlen = (oidlen << 8) + data[i];
                }
                oidlen += arrayindex - 1;
            }
            byte[] arrayoid = new byte[oidlen];
            Array.Copy(data, oidindex, arrayoid, 0, oidlen);
            oid = new SnmpOID(new ASN1Parser(AdventNetUtil.ToSByteArray(arrayoid)).DecodeOID());
            oidindex += oidlen;
            if (data[oidindex++] != 0x02) return false;
            byte[] arrayval = new byte[data.Length - oidindex];
            Array.Copy(data, oidindex, arrayval, 0, arrayval.Length);
            val = new ASN1Parser(AdventNetUtil.ToSByteArray(arrayval)).DecodeInteger();
            return true;
        }

        private void UdpSend(String str)
        {
            byte[] bytes = new byte[1024];  
            IPEndPoint ip = new IPEndPoint(IPAddress.Parse("127.0.0.1"), 5000);
            Socket server = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
            bytes = System.Text.Encoding.UTF8.GetBytes(str);
            server.SendTo(bytes, ip);  
        }
    }
}
