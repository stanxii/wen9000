using System;
using System.Collections.Generic;
using System.Text;

using System.IO;
using System.Data;
using System.Data.OleDb;

namespace HfcAlarmServer
{
    public class CDatabaseEngine : System.IDisposable
    {
        private static string thisDatabaseFile = "WiseView_NMS_HFC.mdb";

        private static string thisConnectingString = @"Provider=Microsoft.Jet.OLEDB.4.0;" +
                        @"Jet OLEDB:Database Password=wiseview; " +
                        @"Data Source=" + thisDatabaseFile;
        
        private IDbConnection thisIConnection;

        #region CDatabaseEngine类的构造器代码。
        public CDatabaseEngine()
        {
            //处理数据库文件的属性。
            if (!File.Exists(thisDatabaseFile))
            {
                throw new Exception("DB File can not find,Program will stop!");
            }

            if (File.GetAttributes(thisDatabaseFile) != FileAttributes.Normal)
            {
                File.SetAttributes(thisDatabaseFile, FileAttributes.Normal);
            }

            thisIConnection = new  OleDbConnection(thisConnectingString);

            //打开数据库连接。
            thisIConnection.Open();
        }
        #endregion

        #region 释放该数据库引擎对象所占的资源。
        private bool IsDisposed = false;
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
            if (thisIConnection != null)
            {
                thisIConnection.Close();
            }
        }
        #endregion
      

        #region "UserGroupTable"表的操作函数集。

        public class UserGroupTableRow
        {
            public int UserGroupID;
            public string UserGroupName;
            public int ParentGroupID;

            public UserGroupTableRow()
            { 
            
            }

            public UserGroupTableRow(int thisid, string name, int parent)
            {
                UserGroupID = thisid;
                UserGroupName = name;
                ParentGroupID = parent;
            }
        }

        public bool UserGroupTableInsertRow(UserGroupTableRow row)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "INSERT INTO UserGroupTable(UserGroupName,ParentGroupID) VALUES('" +
                            row.UserGroupName + "'," + row.ParentGroupID.ToString() + ')';
                        if (tmpCommand.ExecuteNonQuery() > 0)
                        {
                            tmpCommand.CommandText = "SELECT MAX(UserGroupID) FROM UserGroupTable";
                            row.UserGroupID = (int)tmpCommand.ExecuteScalar();
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
            }
            return false;
        }


        public bool UserGroupTableDeleteRow(int thisID)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "DELETE FROM UserGroupTable WHERE UserGroupID=" + thisID.ToString();
                        if (tmpCommand.ExecuteNonQuery() > 0)
                        {
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
            }
            return false;
        }


        public bool UserGroupTableUpdateRow(UserGroupTableRow row)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "UPDATE UserGroupTable SET UserGroupName='" + row.UserGroupName +
                            "',ParentGroupID=" + row.ParentGroupID.ToString() +
                            " WHERE UserGroupID=" + row.UserGroupID.ToString();
                        if (tmpCommand.ExecuteNonQuery() > 0)
                        {
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
            }
            return false;
        }

        public UserGroupTableRow UserGroupTableSearchRow(int thisID)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                IDataReader tmpReader = null;
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "SELECT * FROM UserGroupTable WHERE UserGroupID=" + thisID.ToString();
                        tmpReader = tmpCommand.ExecuteReader(CommandBehavior.SingleRow);
                        if (tmpReader.Read())
                        {
                            UserGroupTableRow row = new UserGroupTableRow((int)tmpReader[0],(string)tmpReader[1],(int)tmpReader[2]);
                            return row;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
                finally
                {
                    if (tmpReader != null) tmpReader.Close();
                }
            }
            return null;
        }

        public List<UserGroupTableRow> UserGroupTableGetAllRows()
        {
            List<UserGroupTableRow> retList = new List<UserGroupTableRow>();

            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                IDataReader tmpReader = null;
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "SELECT * FROM UserGroupTable";
                        tmpReader = tmpCommand.ExecuteReader();
                        while (tmpReader.Read())
                        {
                            UserGroupTableRow tmpRow = new UserGroupTableRow(
                                    (int)tmpReader[0],
                                    (string)tmpReader[1],
                                    (int)tmpReader[2]);
                            retList.Add(tmpRow);
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
                finally
                {
                    if (tmpReader != null) tmpReader.Close();
                }
            }
            return retList;
        }

        #endregion

        #region "OperLogTable"表的操作函数集。

        public class OperLogTableRow
        {
            public int OperLogID;
            public OperLogTypes OperLogType;
            public string OperLogContent;
            public DateTime OperLogTime;

            public OperLogTableRow()
            { 
            
            }

            public OperLogTableRow(OperLogTypes type, string content, DateTime time)
            {
                OperLogID = 0;
                OperLogType = type;
                OperLogContent = content;
                OperLogTime = time;
            }
        }

        /// <summary>
        /// 定义了系统操作日志的类型枚举。
        /// </summary>
        public enum OperLogTypes : int
        { 
            Unknown = 0,
            LogonSystem = 1,
            ExitSystem = 2,
            AddrRangeSearch = 3,
            BroadcastSearch = 4,
            CreateUserGroup = 5,
            RenameUserGroup = 6,
            DeleteUserGroup = 7,
            MoveUserGroup = 8,
            CreateDevice = 9,
            RenameDevice = 10,
            DeleteDevice = 11,
            MoveDevice = 12,
            RegisterDevice = 13,
            BackupOperLog = 14,
            BackupTrapLog = 15,
            SetRoCommunity = 16,
            SetRwCommunity = 17,
            ChangIPAddress
        }

        public bool OperLogTableInsertRow(OperLogTableRow row)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "INSERT INTO OperLogTable(OperLogType,OperLogContent,OperLogTime) VALUES(" +
                            ((int)row.OperLogType).ToString() + ",'" + row.OperLogContent + "','" + row.OperLogTime + "')";
                        if (tmpCommand.ExecuteNonQuery() > 0)
                        {
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
            }
            return false;
        }

        public List<OperLogTableRow> OperLogTableGetAllRows()
        {
            List<OperLogTableRow> retList = new List<OperLogTableRow>();

            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                IDataReader tmpReader = null;
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "SELECT * FROM OperLogTable";
                        tmpReader = tmpCommand.ExecuteReader();
                        while (tmpReader.Read())
                        {
                            OperLogTableRow tmpRow = new OperLogTableRow(
                                    (OperLogTypes)tmpReader[1],
                                    (string)tmpReader[2],
                                    (DateTime)tmpReader[3]);
                            tmpRow.OperLogID = (int)tmpReader[0];
                            retList.Add(tmpRow);
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
                finally
                {
                    if (tmpReader != null) tmpReader.Close();
                }
            }
            return retList;
        }

        public bool OperLogTableDelAllRows()
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "DELETE FROM OperLogTable";
                        if (tmpCommand.ExecuteNonQuery() >= 0)
                        {
                            return true;
                        }  
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
            }
            return false;
        }

        #endregion

        #region "TrapLogTable"表的操作函数集。

        public class TrapLogTableRow
        { 
            public int TrapLogID;
            public TrapLogTypes TrapLogType;
            public string TrapDevAddress;
            public string TrapLogContent;
            public DateTime TrapLogTime;

            public TrapLogTableRow()
            { 
            
            }

            public TrapLogTableRow(TrapLogTypes type, string addr, string content, DateTime time)
            {
                TrapLogID = 0;
                TrapLogType = type;
                TrapDevAddress = addr;
                TrapLogContent = content;
                TrapLogTime = time;
            }
        }

        /// <summary>
        /// 定义设备告警记录的类型枚举。
        /// </summary>
        public enum TrapLogTypes : int
        { 
            Default = 0,
            GenericTrap = 1,
            HfcColdStart = 2,
            HfcWarmStart = 3,
            HfcAlarmEventNormal = 4,
            HfcAlarmEventHiHi = 5,
            HfcAlarmEventHi = 6,
            HfcAlarmEventLo = 7,
            HfcAlarmEventLoLo = 8,
            HfcAlarmEventMajor = 9,
            HfcAlarmEventMinor = 10,
            WosTrapRestart = 11,
            WosTrapDeviceUp = 12,
            WosTrapDeviceDown = 13,
            TestOffline = 14,
            TestOnline = 15,
            TestHfcTypeChanged = 16,
            HfcOptSwitch = 17,
            WosTrapDeviceEvent = 18,
            WosTrapDeviceNormal = 19
        }

        public bool TrapLogTableInsertRow(TrapLogTableRow row)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "INSERT INTO TrapLogTable(TrapLogType,TrapDevAddress,TrapLogContent,TrapLogTime) VALUES(" +
                            ((int)row.TrapLogType).ToString() + ",'" + row.TrapDevAddress + "','" + row.TrapLogContent + "','" + row.TrapLogTime + "')";
                        if (tmpCommand.ExecuteNonQuery() > 0)
                        {
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
            }
            return false;
        }

        public List<TrapLogTableRow> TrapLogTableGetAllRows()
        {
            List<TrapLogTableRow> retList = new List<TrapLogTableRow>();

            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                IDataReader tmpReader = null;
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "SELECT * FROM TrapLogTable";
                        tmpReader = tmpCommand.ExecuteReader();
                        while (tmpReader.Read())
                        {
                            TrapLogTableRow tmpRow = new TrapLogTableRow(
                                    (TrapLogTypes)tmpReader[1],
                                    (string)tmpReader[2],
                                    (string)tmpReader[3],
                                    (DateTime)tmpReader[4]);
                            tmpRow.TrapLogID = (int)tmpReader[0];
                            retList.Add(tmpRow);
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
                finally
                {
                    if (tmpReader != null) tmpReader.Close();
                }
            }
            return retList;
        }

        public bool TrapLogTableDelAllRows()
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "DELETE FROM TrapLogTable";
                        if (tmpCommand.ExecuteNonQuery() >= 0)
                        {
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
            }
            return false;
        }

        #endregion

        #region "DeviceTable"表的操作函数集。

        #region "DeviceTableRow"类成员的枚举定义。
        /// <summary>
        /// "网络组织类型"的枚举。
        /// </summary>
        public enum NetTypes : int
        { 
            Unknown = 0,
            HeaderControl = 1,
            DeviceIP = 2,
            DeviceMAC = 3
        }
        
        #endregion

        #region "DeviceTableRow"类的定义。

        /// <summary>
        /// 代表设备表格一条记录的类。
        /// </summary>
        public class DeviceTableRow
        {
            private readonly string _NetAddress;
            /// <summary>
            /// 获取网管地址，包括ＩＰ地址或ＭＡＣ地址。
            /// </summary>
            public string NetAddress
            {
                get { return _NetAddress; }
            }

            private readonly NetTypes _NetType;
            /// <summary>
            /// 获取网络组织类型。
            /// </summary>
            public NetTypes NetType
            {
                get { return _NetType; }
            }
            
            private int _UserGroupID = 0;
            /// <summary>
            /// 获取或设置所在的用户分组ＩＤ。
            /// </summary>
            public int UserGroupID
            {
                get { return _UserGroupID; }
                set { _UserGroupID = value; }
            }
            private string _HeadAddress = string.Empty;
            /// <summary>
            /// 获取或设置所在的前端控制器地址。
            /// </summary>
            public string HeadAddress
            {
                get { return _HeadAddress; }
                set { _HeadAddress = value; }
            }
            private string _Name = string.Empty;
            /// <summary>
            /// 获取或设置设备名称。
            /// </summary>
            public string Name
            {
                get { return _Name; }
                set { _Name = value; }
            }
            
            private string _ROCommunity = "public";
            /// <summary>
            /// 获取或设置只读团体名。
            /// </summary>
            public string ROCommunity
            {
                get { return _ROCommunity; }
                set { _ROCommunity = value; }
            }
            private string _RWCommunity = "public";
            /// <summary>
            /// 获取或设置读写团体名。
            /// </summary>
            public string RWCommunity
            {
                get { return _RWCommunity; }
                set { _RWCommunity = value; }
            }

            private bool _IsRegister = false;
            /// <summary>
            /// 获取或设置该数据记录对象是否已经成功注册到数据库。
            /// </summary>
            public bool IsRegister
            {
                get { return _IsRegister; }
                set { _IsRegister = value; }
            }

            public DeviceTableRow(string netaddr, NetTypes nettype)
            {
                this._IsRegister = false;
                this._NetAddress = netaddr;
                this._NetType = nettype;
                this._Name = netaddr;
            }
        }

        #endregion

        public bool DeviceTableInsertRow(DeviceTableRow row)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "INSERT INTO DeviceTable(NetAddress,NetType," +
                            "UserGroupID,HeadAddress,Name,ROCommunity,RWCommunity) VALUES('" +
                            row.NetAddress + "'," + ((int)row.NetType).ToString() + ',' + 
                            row.UserGroupID.ToString() + ",'" + row.HeadAddress + "','" + row.Name + "','" + 
                            row.ROCommunity + "','" + row.RWCommunity + "')";

                        if (tmpCommand.ExecuteNonQuery() > 0)
                        {
                            row.IsRegister = true;
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
            }
            return false;
        }

        public bool DeviceTableDeleteRow(DeviceTableRow row)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "DELETE FROM DeviceTable WHERE NetAddress='" + row.NetAddress + '\'';
                        if (tmpCommand.ExecuteNonQuery() > 0)
                        {
                            row.IsRegister = false;
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
            }
            return false;
        }

        public bool DeviceTableUpdateRow(DeviceTableRow row)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "UPDATE DeviceTable SET NetType=" + ((int)row.NetType).ToString() + ",UserGroupID=" +
                            row.UserGroupID.ToString() + ",HeadAddress='" + row.HeadAddress + "',Name='" + row.Name +
                            "',ROCommunity='" + row.ROCommunity + "',RWCommunity='" + row.RWCommunity +
                            "' WHERE NetAddress='" + row.NetAddress + '\'';
                        if (tmpCommand.ExecuteNonQuery() > 0)
                        {
                            row.IsRegister = true;
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
            }
            return false;
        }

        public DeviceTableRow DeviceTableSearchRow(string netaddr)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                IDataReader tmpReader = null;
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "SELECT * FROM DeviceTable WHERE NetAddress='" + netaddr + '\'';
                        tmpReader = tmpCommand.ExecuteReader(CommandBehavior.SingleRow);
                        if (tmpReader.Read())
                        {
                            DeviceTableRow row;
                            row = new DeviceTableRow((string)tmpReader[0], (NetTypes)tmpReader[1]);
                            row.UserGroupID = (int)tmpReader[2];
                            row.HeadAddress = (string)tmpReader[3];
                            row.Name = (string)tmpReader[4];
                            row.ROCommunity = (string)tmpReader[5];
                            row.RWCommunity = (string)tmpReader[6];
                            row.IsRegister = true;
                            return row;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
                finally
                {
                    if (tmpReader != null) tmpReader.Close();
                }
            }
            return null;
        }


        public List<DeviceTableRow> DeviceTableGetAllRows()
        {
            List<DeviceTableRow> retList = new List<DeviceTableRow>();

            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                IDataReader tmpReader = null;
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "SELECT * FROM DeviceTable";
                        tmpReader = tmpCommand.ExecuteReader();
                        while (tmpReader.Read())
                        {
                            DeviceTableRow tmpRow = new DeviceTableRow(
                                (string)tmpReader[0], (NetTypes)tmpReader[1]);
                            tmpRow.UserGroupID = (int)tmpReader[2];
                            tmpRow.HeadAddress = (string)tmpReader[3];
                            tmpRow.Name = (string)tmpReader[4];
                            tmpRow.ROCommunity = (string)tmpReader[5];
                            tmpRow.RWCommunity = (string)tmpReader[6];
                            tmpRow.IsRegister = true;
                            retList.Add(tmpRow);
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
                finally
                {
                    if (tmpReader != null) tmpReader.Close();
                }
            }
            return retList;
        }


        public List<DeviceTableRow> DeviceTableGetAllRows(NetTypes nettype)
        {
            List<DeviceTableRow> retList = new List<DeviceTableRow>();

            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                IDataReader tmpReader = null;
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "SELECT * FROM DeviceTable WHERE NetType=" + ((int)nettype).ToString();
                        tmpReader = tmpCommand.ExecuteReader();
                        while (tmpReader.Read())
                        {
                            DeviceTableRow tmpRow = new DeviceTableRow(
                                (string)tmpReader[0], (NetTypes)tmpReader[1]);
                            tmpRow.UserGroupID = (int)tmpReader[2];
                            tmpRow.HeadAddress = (string)tmpReader[3];
                            tmpRow.Name = (string)tmpReader[4];
                            tmpRow.ROCommunity = (string)tmpReader[5];
                            tmpRow.RWCommunity = (string)tmpReader[6];
                            tmpRow.IsRegister = true;
                            retList.Add(tmpRow);
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
                finally
                {
                    if (tmpReader != null) tmpReader.Close();
                }
            }
            return retList;
        }

        public List<DeviceTableRow> DeviceTableGetAllRows(int usrgroup)
        {
            List<DeviceTableRow> retList = new List<DeviceTableRow>();

            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                IDataReader tmpReader = null;
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "SELECT * FROM DeviceTable WHERE UserGroupID=" + usrgroup.ToString();
                        tmpReader = tmpCommand.ExecuteReader();
                        while (tmpReader.Read())
                        {
                            DeviceTableRow tmpRow = new DeviceTableRow(
                                (string)tmpReader[0], (NetTypes)tmpReader[1]);
                            tmpRow.UserGroupID = (int)tmpReader[2];
                            tmpRow.HeadAddress = (string)tmpReader[3];
                            tmpRow.Name = (string)tmpReader[4];
                            tmpRow.ROCommunity = (string)tmpReader[5];
                            tmpRow.RWCommunity = (string)tmpReader[6];
                            tmpRow.IsRegister = true;
                            retList.Add(tmpRow);
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
                finally
                {
                    if (tmpReader != null) tmpReader.Close();
                }
            }
            return retList;
        }


        public List<DeviceTableRow> DeviceTableGetAllRows(string headaddr)
        {
            List<DeviceTableRow> retList = new List<DeviceTableRow>();

            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                IDataReader tmpReader = null;
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "SELECT * FROM DeviceTable WHERE HeadAddress='" + headaddr + '\'';
                        tmpReader = tmpCommand.ExecuteReader();
                        while (tmpReader.Read())
                        {
                            DeviceTableRow tmpRow = new DeviceTableRow(
                                (string)tmpReader[0], (NetTypes)tmpReader[1]);
                            tmpRow.UserGroupID = (int)tmpReader[2];
                            tmpRow.HeadAddress = (string)tmpReader[3];
                            tmpRow.Name = (string)tmpReader[4];
                            tmpRow.ROCommunity = (string)tmpReader[5];
                            tmpRow.RWCommunity = (string)tmpReader[6];
                            tmpRow.IsRegister = true;
                            retList.Add(tmpRow);
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
                finally
                {
                    if (tmpReader != null) tmpReader.Close();
                }
            }
            return retList;
        }


        #endregion

        #region "DevRemarkTable"表的操作函数集。

        public void DeleteDeviceRemarkText(string addr)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "DELETE FROM DevRemarkTable WHERE NetAddress='" + addr + '\'';
                        tmpCommand.ExecuteNonQuery();
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
            }
        }

        public bool SaveDeviceRemarkText(string addr, string remark)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                IDataReader tmpReader = null;
                bool isExist = false;
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "SELECT NetAddress FROM DevRemarkTable WHERE NetAddress='" + addr + '\'';
                        tmpReader = tmpCommand.ExecuteReader(CommandBehavior.SingleRow);
                        if (tmpReader.Read())
                        {
                            isExist = true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                    return false;
                }
                finally
                {
                    if (tmpReader != null) tmpReader.Close();
                }

                if (isExist)
                {
                    try
                    {
                        using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                        {
                            tmpCommand.CommandText = "UPDATE DevRemarkTable SET RemarkText='" + remark + "' WHERE NetAddress='" + addr + '\'';
                            if (tmpCommand.ExecuteNonQuery() > 0)
                                return true;
                        }
                    }
                    catch (Exception ex)
                    {
                        Console.Write(ex.Source);
                    }
                }
                else
                {
                    try
                    {
                        using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                        {
                            tmpCommand.CommandText = "INSERT INTO DevRemarkTable(NetAddress,RemarkText) VALUES('" + addr + "','" + remark + "')";
                            if (tmpCommand.ExecuteNonQuery() > 0)
                            {
                                return true;
                            }
                        }
                    }
                    catch (Exception ex)
                    {
                        Console.Write(ex.Source);
                    }
                }
            }
            return false;
        }

        public string LoadDeviceRemarkText(string addr)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                IDataReader tmpReader = null;
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "SELECT RemarkText FROM DevRemarkTable WHERE NetAddress='" + addr + '\'';
                        tmpReader = tmpCommand.ExecuteReader(CommandBehavior.SingleRow);
                        if (tmpReader.Read())
                        {
                            return (string)tmpReader[0];
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
                finally
                {
                    if (tmpReader != null) tmpReader.Close();
                }
            }
            return string.Empty;
        }

        #endregion

        #region "ParmsTable"表的操作函数集。

        public class ParmsTableRow
        {
            private readonly string _ParamMibLabel;
            public string ParamMibLabel
            {
                get { return _ParamMibLabel; }
            }

            private readonly string _ParamMibOID;
            public string ParamMibOID
            {
                get { return _ParamMibOID; }
            }

            private readonly string _ParamDispText;
            public string ParamDispText
            {
                get { return _ParamDispText; }
            }

            private readonly bool _IsFormatEnable;
            public bool IsFormatEnable
            {
                get { return _IsFormatEnable; }
            }

            private readonly float _FormatCoff;
            public float FormatCoff
            {
                get { return _FormatCoff; }
            }

            private readonly string _FormatText;
            public string FormatText
            {
                get { return _FormatText; }
            }

            private readonly string _FormatUnit;
            public string FormatUnit
            {
                get { return _FormatUnit; }
            } 

            public ParmsTableRow(string miblabel,string miboid, string disptxt, 
                bool fmten, float fmtcoff, string fmttxt, string fmtunit)
            {
                _ParamMibLabel = miblabel;
                _ParamMibOID = miboid;
                _ParamDispText = disptxt;
                _IsFormatEnable = fmten;
                _FormatCoff = fmtcoff;
                _FormatText = fmttxt;
                _FormatUnit = fmtunit;
            }
        }

        public List<ParmsTableRow> ParmsTableGetAllRows()
        {
            List<ParmsTableRow> retList = new List<ParmsTableRow>();

            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                IDataReader tmpReader = null;
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "SELECT * FROM ParmsTable";
                        tmpReader = tmpCommand.ExecuteReader();
                        while (tmpReader.Read())
                        {
                            ParmsTableRow tmpRow = new ParmsTableRow(
                                (string)tmpReader[0],
                                (string)tmpReader[1],
                                (string)tmpReader[2],
                                (bool)tmpReader[3],
                                (float)tmpReader[4],
                                (string)tmpReader[5],
                                (string)tmpReader[6]
                                );
                            retList.Add(tmpRow);
                        }
                        return retList;
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
                finally
                {
                    if (tmpReader != null) tmpReader.Close();
                }
            }
            return null;
        }

        #endregion

        #region "UserAuthorizeTable"类的定义。

        /// <summary>
        /// 代表设备表格一条记录的类。
        /// </summary>
        public class UserAuthorizeTableRow
        {

            private int _UserID;
            /// <summary>
            /// 获取用户名
            /// </summary>
            public int UserID
            {
                get { return _UserID; }
                set { _UserID = value; }
            }
            
            
            
            private  string _UserName;
            /// <summary>
            /// 获取用户名
            /// </summary>
            public string UserName
            {
                get { return _UserName; }
                set { _UserName = value; }
            }
            private string _PassWord;
            /// <summary>
            /// 密码
            /// </summary>
            public string PassWord
            {
                get { return _PassWord; }
                set { _PassWord = value; }
            }


            private byte _AuthTotal;
            /// <summary>
            /// 获取管理员权限
            /// </summary>
            public byte AuthTotal
            {
                get { return _AuthTotal; }
                set { _AuthTotal = value; }
            }


            private byte _AuthDelDevice;
            /// <summary>
            /// 获取删除设备权限
            /// </summary>
            public byte AuthDelDevice
            {
                get { return _AuthDelDevice; }
                set { _AuthDelDevice = value; }
            }


            private byte _AuthCreDevice;
            /// <summary>
            /// 获取创建设备权限
            /// </summary>
            public byte AuthCreDevice
            {
                get { return _AuthCreDevice; }
                set { _AuthCreDevice = value; }
            }


            private byte _AuthDelDeviceGroup;
            /// <summary>
            /// 获取删除设备组权限
            /// </summary>
            public byte AuthDelDeviceGroup
            {
                get { return _AuthDelDeviceGroup; }
                set { _AuthDelDeviceGroup = value; }
            }


            private byte _AuthCreDeviceGroup;
            /// <summary>
            /// 获取创建设备组权限
            /// </summary>
            public byte AuthCreDeviceGroup
            {
                get { return _AuthCreDeviceGroup; }
                set { _AuthCreDeviceGroup = value; }
            }


            private byte _AuthRegDevice;
            /// <summary>
            /// 获取注册设备权限
            /// </summary>
            public byte AuthRegDevice
            {
                get { return _AuthRegDevice; }
                set { _AuthRegDevice = value; }
            }


            private byte _AuthSetDevicePara;
            /// <summary>
            /// 获取设置参数权限
            /// </summary>
            public byte AuthSetDevicePara
            {
                get { return _AuthSetDevicePara; }
                set { _AuthSetDevicePara = value; }
            }

            private byte _AuthMoveDevice;
            /// <summary>
            /// 获取注册设备权限
            /// </summary>
            public byte AuthMoveDevice
            {
                get { return _AuthMoveDevice; }
                set { _AuthMoveDevice = value; }
            }


            private byte _AuthMoveDeviceGroup;
            /// <summary>
            /// 获取设置参数权限
            /// </summary>
            public byte AuthMoveDeviceGroup
            {
                get { return _AuthMoveDeviceGroup; }
                set { _AuthMoveDeviceGroup = value; }
            }
         


            public UserAuthorizeTableRow(int userid,string username,  byte authtotal, byte authdeldevice,byte authcredevice,
                byte authdeldevgroup, byte authcredevgroup, byte authregdevice, byte authsetdevicepara, byte movedevice, byte movedevgroup, string password)
            {
                this._UserID = userid;
                this._UserName = username;
                this._PassWord = password;
                this._AuthTotal = authtotal;
                
                this._AuthDelDevice = authdeldevice;
                
                this._AuthCreDevice = authcredevice;
                
                this._AuthDelDeviceGroup = authdeldevgroup;
                this._AuthCreDeviceGroup = authcredevgroup;
                this._AuthRegDevice = authregdevice;
                this._AuthSetDevicePara = authsetdevicepara;
                this._AuthMoveDevice = movedevice;
                this._AuthMoveDeviceGroup = movedevgroup;
            }
        }



        public bool UserAuthorizeTableInsertRow(UserAuthorizeTableRow row)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "INSERT INTO UserAuthorizeTable(UserName,password1," +
                            "AuthTotal,AuthDelDevice,AuthCreDevice,AuthDelDeviceGroup,AuthCreDeviceGroup,AuthRegDevice," +
                            "AuthSetDevicePara,AuthMoveDevice,AuthMoveDeviceGroup) VALUES('" +
                            row.UserName + "','" + row.PassWord + "'," +
                            row.AuthTotal.ToString() + ',' + row.AuthDelDevice.ToString() + ',' + row.AuthCreDevice.ToString() + ',' +
                            row.AuthDelDeviceGroup.ToString() + ',' + row.AuthCreDeviceGroup.ToString() + ',' + row.AuthRegDevice.ToString() + ','+
                            row.AuthSetDevicePara.ToString() + ',' + row.AuthMoveDevice.ToString() + ',' + row.AuthMoveDeviceGroup.ToString() + ")";

                        if (tmpCommand.ExecuteNonQuery() > 0)
                        {
                            //row.IsRegister = true;
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
            }
            return false;
        }

        public bool UserAuthorizeTableDeleteRow(UserAuthorizeTableRow row)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "DELETE FROM UserAuthorizeTable WHERE UserID=" + row.UserID.ToString() ;
                        if (tmpCommand.ExecuteNonQuery() > 0)
                        {
                            //row.IsRegister = false;
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
            }
            return false;
        }

        public bool UserAuthorizeTableUpdateRow(UserAuthorizeTableRow row)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "UPDATE UserAuthorizeTable SET AuthTotal =" + row.AuthTotal.ToString() 
                            + ",PassWord1='" + row.PassWord
                         
                                                      
                           + "',UserName='" + row.UserName
                           +  "',AuthDelDevice=" + row.AuthDelDevice.ToString() + ",AuthCreDevice=" + row.AuthCreDevice.ToString() 
                            +",AuthDelDeviceGroup=" + row.AuthDelDeviceGroup.ToString() + ",AuthCreDeviceGroup=" + row.AuthCreDeviceGroup.ToString() +
                            ",AuthRegDevice=" + row.AuthRegDevice.ToString() + ",AuthSetDevicePara=" + row.AuthSetDevicePara.ToString() +
                            ",AuthMoveDevice=" + row.AuthMoveDevice.ToString() + ",AuthMoveDeviceGroup=" + row.AuthMoveDeviceGroup.ToString()
                      //       + ",password='" + row.UserName 
                            + " WHERE UserID=" + row.UserID.ToString();
                        if (tmpCommand.ExecuteNonQuery() > 0)
                        {
                            //row.IsRegister = true;
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
            }
            return false;
        }

        public UserAuthorizeTableRow UserAuthorizeTableSearchRow(int userid)
        {
            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                IDataReader tmpReader = null;
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "SELECT * FROM UserAuthorizeTable WHERE UserID=" + userid.ToString() + '\'';
                        tmpReader = tmpCommand.ExecuteReader(CommandBehavior.SingleRow);
                        if (tmpReader.Read())
                        {
                            UserAuthorizeTableRow row;
                            row = new UserAuthorizeTableRow((int)tmpReader[0], (string)tmpReader[1], (byte)tmpReader[2], (byte)tmpReader[3], (byte)tmpReader[4],
                             (byte)tmpReader[5],(byte)tmpReader[6],(byte)tmpReader[7],(byte)tmpReader[8],(byte)tmpReader[9],
                             (byte)tmpReader[10],(string)tmpReader[11]);
                           
                            return row;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
                finally
                {
                    if (tmpReader != null) tmpReader.Close();
                }
            }
            return null;
        }


        public List<UserAuthorizeTableRow> UserAuthorizeTableGetAllRows()
        {
            List<UserAuthorizeTableRow> retList = new List<UserAuthorizeTableRow>();

            if ((thisIConnection.State & ConnectionState.Open) == ConnectionState.Open)
            {
                IDataReader tmpReader = null;
                try
                {
                    using (IDbCommand tmpCommand = thisIConnection.CreateCommand())
                    {
                        tmpCommand.CommandText = "SELECT * FROM UserAuthorizeTable";
                        tmpReader = tmpCommand.ExecuteReader();
                        while (tmpReader.Read())
                        {
                            UserAuthorizeTableRow tmpRow = new UserAuthorizeTableRow((int)tmpReader[0], (string)tmpReader[1], (byte)tmpReader[2], (byte)tmpReader[3], (byte)tmpReader[4],
                             (byte)tmpReader[5], (byte)tmpReader[6], (byte)tmpReader[7], (byte)tmpReader[8], (byte)tmpReader[9],
                             (byte)tmpReader[10], (string)tmpReader[11]);
                           
                            retList.Add(tmpRow);
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.Write(ex.Source);
                }
                finally
                {
                    if (tmpReader != null) tmpReader.Close();
                }
            }
            return retList;
        }



        
        #endregion


    }
}
