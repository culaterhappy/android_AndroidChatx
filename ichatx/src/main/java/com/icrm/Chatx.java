package com.icrm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.TelephonyManager;


import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ipworksws.Wsclient;
import ipworksws.WsclientConnectedEvent;
import ipworksws.WsclientConnectionStatusEvent;
import ipworksws.WsclientDisconnectedEvent;
import ipworksws.WsclientErrorEvent;
import ipworksws.WsclientReadyToSendEvent;

/**
 * Created by zhutong on 2016/5/26.
 */
public class Chatx {
    private Wsclient wsclient;
    private static final String TAG = "VidyoSampleActivity.Chatx";
    private String receivedData = "";
    private Vector _eventListeners = new Vector();
    private String ServerIP = "";
    private int ServerPort = 7080;

    private Context m_context;



    private static Geocoder geocoder;   //此对象能通过经纬度来获取相应的城市等信息
    private String _CallData = "";
    private String deviceid = "";
    private String phonenumber = "";
    private String softwareversion = "";
    private String operatorname = "";
    private String simcountrycode = "";
    private String simoperator = "";
    private String simserialno = "";
    private String subscriberid = "";
    private String providerName = "";
    private String networktype = "";
    private String phonetype = "";
    private String model = "";
    private String sdk = "";
    private String osRelease = "";
    private String manufacturer = "";
    public static String city = "";  //城市名

    private Boolean bDisconnect = false;

    private String vName = "";
    private String CurrentAgentID = "";
    private String CurrentChTag = "";
    private String FileUploadUrl = "";
    private String SvcCode = "";

    private Boolean IsChating = false;
    private static ArrayList<Emotj> Emotjs = new ArrayList<Emotj>();

    private String emotjStr = "<faces><face facecode=\"/::)\" facepath=\"0\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::~\" facepath=\"1\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::B\" facepath=\"2\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::|\" facepath=\"3\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/:8-)\" facepath=\"4\" show=\"1\">" +
            "</face>" +
            "" +
            "<face facecode=\"/::&lt;\" facepath=\"5\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::$\" facepath=\"6\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::X\" facepath=\"7\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::Z\" facepath=\"8\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::'(\" facepath=\"9\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::-|\" facepath=\"10\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::@\" facepath=\"11\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::P\" facepath=\"12\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::D\" facepath=\"13\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::O\" facepath=\"14\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::(\" facepath=\"15\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::+\" facepath=\"16\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/:--b\" facepath=\"17\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::Q\" facepath=\"18\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::T\" facepath=\"19\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/:,@P\" facepath=\"20\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/:,@-D\" facepath=\"21\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::d\" facepath=\"22\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/:,@o\" facepath=\"23\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::g\" facepath=\"24\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/:|-)\" facepath=\"25\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::!\" facepath=\"26\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::L\" facepath=\"27\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::>\" facepath=\"28\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/::,@\" facepath=\"29\" show=\"1\">" +
            "</face>" +
            "<face facecode=\"/:,@f\" facepath=\"30\" show=\"1\">" +
            "</face></faces>";

    public String getApplicationName() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = ((Activity)m_context).getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(((Activity)m_context).getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName =
                (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }

    public String getEmotjFaceCode(String facepath)
    {

        for(int i = 0;i<Emotjs.size();i++){
            if(("q" + Emotjs.get(i).facepath).equals(facepath)){
                return Emotjs.get(i).facecode;
            }
        }
        return "";

    }

    public String ProcessEmotj(String msg)
    {
        for(int i = 0;i<Emotjs.size();i++){
            msg = msg.replace(Emotjs.get(i).facecode,"<img src=\"q" + Emotjs.get(i).facepath + "\"></img>");
        }
        return msg;

    }

    private void InitEmotj()
    {
        try {
            Log.v(TAG,"InitEmotj Start...");
            StringReader rd = new StringReader(emotjStr);
            InputSource in = new InputSource(rd);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);

            NodeList nl = doc.getElementsByTagName("face");
            for(int i = 0;i<nl.getLength();i++){
                Node nd = nl.item(i);
                String facecode = nd.getAttributes().getNamedItem("facecode").getNodeValue();
                String facepath = nd.getAttributes().getNamedItem("facepath").getNodeValue();

                Log.v(TAG,"facecode->" + facecode + "...facepath->" + facepath);
                Emotj _em = new Emotj();
                _em.facecode = facecode;
                _em.facepath = facepath;
                Emotjs.add(_em);

            }
        }
        catch(Exception ex){
            Log.v(TAG,ex.getMessage());
            ex.printStackTrace();
        }

    }

    public void NotifyOnChatActivityQuit()
    {
        ((Activity)m_context).runOnUiThread(new Runnable()
        { public void run() {
            Iterator iterator = ((Vector) _eventListeners.clone()).iterator();

            while (iterator.hasNext()) {
                ChatxEventListener listener = (ChatxEventListener) iterator.next();
                listener.OnChatActivityQuit();
            }
        }});
    }

    public void NotifyNoAgents(String Reason)
    {
        final String sReason = Reason;
        ((Activity)m_context).runOnUiThread(new Runnable()
        { public void run() {
            Iterator iterator = ((Vector) _eventListeners.clone()).iterator();

            while (iterator.hasNext()) {
                ChatxEventListener listener = (ChatxEventListener) iterator.next();
                listener.OnNoAgents(sReason);
            }
        }});

    }

    public Chatx(Context context)
    {

        Log.v(TAG,"Chatx Start...");


        m_context = context;



        wsclient = new Wsclient(context);
        wsclient.setRuntimeLicense("31574739564230383731313336394544555A48584252000000000000000000000000000000000000464143443541345300004142533252464558525854340000");


        try{
            wsclient.setTimeout(10);
            wsclient.addWsclientEventListener(new ipworksws.DefaultWsclientEventListener(){
                public void dataIn(ipworksws.WsclientDataInEvent e){
                    OnDataIn(e);
                }

                @Override
                public void connected(WsclientConnectedEvent wsclientConnectedEvent) {
                    Log.v(TAG,"wsclient.connected...description->" + wsclientConnectedEvent.description);

                    if(wsclientConnectedEvent.statusCode == 0){
                        ((Activity)m_context).runOnUiThread(new Runnable()
                        { public void run() {
                            Iterator iterator = ((Vector) _eventListeners.clone()).iterator();

                            while (iterator.hasNext()) {
                                ChatxEventListener listener = (ChatxEventListener) iterator.next();
                                listener.OnChatEvtConnected();
                            }
                        }});

                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    //发送LOGIN消息，让服务器和本机id关联
                                    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><APP><Action>Login</Action>";
                                    xml += "<Data vTag=\"#vTag#\" ";
                                    xml += "providerName=\"#providerName#\" ";//运营商
                                    xml += "simserialno=\"#simserialno#\" ";//sim卡id
                                    xml += "simcountrycode=\"#simcountrycode#\" city=\"#city#\" ";//国家代码，城市信息
                                    xml += "OS=\"android\" osRelease=\"#osRelease#\" ";//操作系统信息
                                    xml += "manufacturer=\"#manufacturer#\" model=\"#model#\" appname=\"#appname#\" >";//厂商和型号
                                    xml += "</Data></APP>";

                                    xml = xml.replace("#vTag#", simserialno);
                                    xml = xml.replace("#providerName#", providerName);
                                    xml = xml.replace("#simserialno#", simserialno);
                                    xml = xml.replace("#simcountrycode#", simcountrycode);
                                    xml = xml.replace("#city#", city);
                                    xml = xml.replace("#osRelease#", osRelease);
                                    xml = xml.replace("#manufacturer#", manufacturer);
                                    xml = xml.replace("#model#", getModel());
                                    xml = xml.replace("#appname#", getApplicationName());

                                    Log.v(TAG, xml);
                                    wsclient.sendText(xml);


                                } catch (Exception ex) {
                                    Log.v(TAG, ex.getMessage());
                                    ex.printStackTrace();

                                }
                            }
                        }).start();;



                    }


                }

                @Override
                public void connectionStatus(WsclientConnectionStatusEvent wsclientConnectionStatusEvent) {
                    Log.v(TAG,"wsclient.connectionStatus...description->" + wsclientConnectionStatusEvent.description);
                }

                @Override
                public void disconnected(WsclientDisconnectedEvent wsclientDisconnectedEvent) {
                    Log.v(TAG,"wsclient.disconnected...description->" + wsclientDisconnectedEvent.description);

                    ((Activity)m_context).runOnUiThread(new Runnable()
                        { public void run(){
                            Iterator iterator = ((Vector) _eventListeners.clone()).iterator();

                            while (iterator.hasNext()) {
                                ChatxEventListener listener = (ChatxEventListener) iterator.next();
                                listener.OnChatEvtDisConnected();
                            }

                            if(bDisconnect == false) {
                                Log.v(TAG, "wsclient.disconnected...try to reconnect...");
                                Connect();
                            }


                        }}
                    );



                }

                @Override
                public void error(WsclientErrorEvent wsclientErrorEvent) {
                    Log.v(TAG,"wsclient.error...description->" + wsclientErrorEvent.description);
                }

                @Override
                public void readyToSend(WsclientReadyToSendEvent wsclientReadyToSendEvent) {
                    Log.v(TAG,"wsclient.readyToSend...");
                }
            });
        }
        catch(Exception ex)
        {
            Log.v(TAG,"Error adding event handler to WSClient: " + ex.getMessage());

        }

        GetTelephoneInfo();
        InitEmotj();
    }

    private void GetTelephoneInfo()
    {
        DeviceUuidFactory df = new DeviceUuidFactory(m_context);

        TelephonyManager tm = (TelephonyManager) m_context.getSystemService(Context.TELEPHONY_SERVICE);

        //Get the IMEI code
        //setDeviceid(tm.getDeviceId());
        setDeviceid(df.getDeviceUuid().toString());

        //Get  the phone number string for line 1,For ex: the MSISDN for a GSM phone
        if(tm.getDeviceId() != null) {
            phonenumber = tm.getLine1Number();

            //Get  the software version number for the device, For ex: the IMEI/SV for GSM phones
            softwareversion = tm.getDeviceSoftwareVersion();

            //Get  the alphabetic name of current registered operator.
            operatorname = tm.getNetworkOperatorName();
            //Get  the ISO country code equivalent for the SIM provider's country code.
            simcountrycode = tm.getSimCountryIso();
            //Get  the Service Provider Name (SPN).
            simoperator = tm.getSimOperatorName();
            //Get  the serial number of the SIM, if applicable. Return null if it is unavailable.
            simserialno = tm.getSimSerialNumber();
            //Get  the unique subscriber ID, for example, the IMSI for a GSM phone
            subscriberid = tm.getSubscriberId();
            //Get the type indicating the radio technology (network type)
            //currently in use on the device for data transmission.EDGE,GPRS,UMTS  etc
            networktype = getNetworkTypeString(tm.getNetworkType());
            //This indicates the type of radio used to transmit voice calls
            //GSM,CDMA etc
            phonetype = getPhoneTypeString(tm.getPhoneType());
        }

        if(simserialno == "" || simserialno == null) {
            simserialno = getDeviceid();
        }

        getCNBylocation(m_context);
        setModel(android.os.Build.MODEL);
        sdk = android.os.Build.VERSION.SDK;
        osRelease = android.os.Build.VERSION.RELEASE;
        manufacturer = android.os.Build.MANUFACTURER;

        providerName = getProvidersName(subscriberid);


        Log.v(TAG,"GetTelephoneInfo...deviceid->" + getDeviceid());
        Log.v(TAG,"GetTelephoneInfo...phonenumber->" + phonenumber);
        Log.v(TAG,"GetTelephoneInfo...softwareversion->" + softwareversion);
        Log.v(TAG,"GetTelephoneInfo...operatorname->" + operatorname);
        Log.v(TAG,"GetTelephoneInfo...simcountrycode->" + simcountrycode);
        Log.v(TAG,"GetTelephoneInfo...simoperator->" + simoperator);
        Log.v(TAG,"GetTelephoneInfo...simserialno->" + simserialno);
        Log.v(TAG,"GetTelephoneInfo...subscriberid->" + subscriberid + "...providerName->" + providerName);
        Log.v(TAG,"GetTelephoneInfo...networktype->" + networktype);
        Log.v(TAG,"GetTelephoneInfo...phonetype->" + phonetype);


        Log.v(TAG,"GetTelephoneInfo...city->" + city);

        // 获取Android手机型号和OS的版本号


        Log.v(TAG,"GetTelephoneInfo...model->" + getModel());
        Log.v(TAG,"GetTelephoneInfo...sdk->" + sdk);
        Log.v(TAG,"GetTelephoneInfo...osRelease->" + osRelease);
        Log.v(TAG,"GetTelephoneInfo...manufacturer->" + manufacturer);




    }

    public String getProvidersName(String subscriberid) {
        // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。

        try {
            String ProvidersName = "";
            if (subscriberid.startsWith("46000") || subscriberid.startsWith("46002")) {
                ProvidersName = "中国移动";
            } else if (subscriberid.startsWith("46001")) {
                ProvidersName = "中国联通";
            } else if (subscriberid.startsWith("46003")) {
                ProvidersName = "中国电信";
            }
            return ProvidersName;
        }
        catch (Exception ex){
            return "";
        }
    }

    private String getNetworkTypeString(int type){
        String typeString = "Unknown";
        switch(type)
        {
            case TelephonyManager.NETWORK_TYPE_EDGE:
                typeString = "EDGE"; break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                typeString = "GPRS"; break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                typeString = "UMTS"; break;
            default:
                typeString = "UNKNOWN"; break;
        }
        return typeString;
    }

    private String getPhoneTypeString(int type){
        String typeString = "Unknown";
        switch(type)
        {
            case TelephonyManager.PHONE_TYPE_GSM:   typeString = "GSM"; break;
            case TelephonyManager.PHONE_TYPE_NONE:  typeString = "UNKNOWN"; break;
            default:
                typeString = "UNKNOWN"; break;
        }
        return typeString;
    }

    public static void getCNBylocation(Context context){

        try {
            geocoder = new Geocoder(context);
            //用于获取Location对象，以及其他
            LocationManager locationManager;
            String serviceName = Context.LOCATION_SERVICE;
            //实例化一个LocationManager对象
            locationManager = (LocationManager) context.getSystemService(serviceName);
            //provider的类型
            String provider = LocationManager.NETWORK_PROVIDER;

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);   //高精度
            criteria.setAltitudeRequired(false);    //不要求海拔
            criteria.setBearingRequired(false); //不要求方位
            criteria.setCostAllowed(false); //不允许有话费
            criteria.setPowerRequirement(Criteria.POWER_LOW);   //低功耗

            //通过最后一次的地理位置来获得Location对象
            Location location = locationManager.getLastKnownLocation(provider);

            String queryed_name = updateWithNewLocation(location);
            if ((queryed_name != null) && (0 != queryed_name.length())) {

                city = queryed_name;
            }
        }
        catch(Exception ex){

        }


    }

    /**
     * 更新location
     * @param location
     * @return cityName
     */
    private static String updateWithNewLocation(Location location) {
        String mcityName = "";
        double lat = 0;
        double lng = 0;
        List<Address> addList = null;
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
        } else {

            Log.v(TAG,"无法获取地理信息");
        }

        try {

            addList = geocoder.getFromLocation(lat, lng, 1);    //解析经纬度

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (addList != null && addList.size() > 0) {
            for (int i = 0; i < addList.size(); i++) {
                Address add = addList.get(i);
                mcityName += add.getLocality();
            }
        }
        if(mcityName.length()!=0){

            return mcityName.substring(0, (mcityName.length()-1));
        } else {
            return mcityName;
        }
    }


    public synchronized void addEventListener(ChatxEventListener l){
        _eventListeners.addElement(l);

    }

    public synchronized void removeEventListener(ChatxEventListener l){
        _eventListeners.removeElement(l);

    }

    private void OnDataIn(ipworksws.WsclientDataInEvent e)
    {
        receivedData = new String(e.text);
        Log.v(TAG,"OnDataIn...Received: " + receivedData);


        ((Activity)m_context).runOnUiThread(new Runnable()
        { public void run() {
            Iterator iterator = ((Vector) _eventListeners.clone()).iterator();

            while (iterator.hasNext()) {
                ChatxEventListener listener = (ChatxEventListener) iterator.next();
                listener.OnChatEvtNewMsg(receivedData);
            }
        }});


        Intent intentbrd = new Intent(ChatActivity.broadcastaction);
        intentbrd.putExtra("data", receivedData);
        ((Activity)m_context).sendBroadcast(intentbrd);


        try {
            StringReader rd = new StringReader(receivedData);
            InputSource in = new InputSource(rd);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);

            NodeList nl = doc.getElementsByTagName("Action");
            if (nl == null) {
                Log.v(TAG, "OnChatEvtNewMsg...Action not found...ignore and return...");
                return;
            }

            Node nd = nl.item(0);
            String action = nd.getTextContent();

            if (action.equals("LoginResponse")) {
                nl = doc.getElementsByTagName("Data");
                nd = nl.item(0);
                String FileUploadUrl = nd.getAttributes().getNamedItem("FileUploadUrl").getNodeValue();
                setFileUploadUrl(FileUploadUrl);
            }
            else if(action.equals("VideoGuestJoin")){
                nl = doc.getElementsByTagName("Data");
                nd = nl.item(0);
                final String Provider = nd.getAttributes().getNamedItem("Provider").getNodeValue();

                if(Provider.toLowerCase().equals("agora")){
                    //VENDORKEY
                    //RoomName

                    final String VENDORKEY = nd.getAttributes().getNamedItem("VENDORKEY").getNodeValue();
                    final String RoomName = nd.getAttributes().getNamedItem("RoomName").getNodeValue();

                    ((Activity)m_context).runOnUiThread(new Runnable()
                    { public void run() {
                        Iterator iterator = ((Vector) _eventListeners.clone()).iterator();

                        while (iterator.hasNext()) {
                            ChatxEventListener listener = (ChatxEventListener) iterator.next();
                            listener.OnVideoGuestJoin("agora",VENDORKEY,RoomName,"","");

                        }
                    }});

                }
                else if(Provider.toLowerCase().equals("vidyo")) {
                    String VidyoUrl = nd.getAttributes().getNamedItem("VidyoUrl").getNodeValue();

                    VidyoUrl = VidyoUrl.replace("http://","");

                    final String host = VidyoUrl;

                    final String RoomKey = nd.getAttributes().getNamedItem("RoomKey").getNodeValue();
                    final String RoomPin = nd.getAttributes().getNamedItem("RoomPin").getNodeValue();

                    final String userName = getvName();

                    ((Activity)m_context).runOnUiThread(new Runnable()
                    { public void run() {
                        Iterator iterator = ((Vector) _eventListeners.clone()).iterator();

                        while (iterator.hasNext()) {
                            ChatxEventListener listener = (ChatxEventListener) iterator.next();
                            listener.OnVideoGuestJoin("vidyo",RoomKey,RoomPin,userName,host);

                        }
                    }});

                }
                else{
                    VideoGuestJoinFailed("视频平台不一致");
                    ((Activity)m_context).runOnUiThread(new Runnable()
                    { public void run() {
                        Toast.makeText((Activity)m_context, "坐席的视频厂商为:" + Provider + ",和您的APP不符合.", Toast.LENGTH_LONG).show();
                    }});


                }
            }
            else if(action.equals("VideoQuit")){
                ((Activity)m_context).runOnUiThread(new Runnable()
                { public void run() {
                    Iterator iterator = ((Vector) _eventListeners.clone()).iterator();

                    while (iterator.hasNext()) {
                        ChatxEventListener listener = (ChatxEventListener) iterator.next();
                        listener.OnVideoQuit();

                    }
                }});
            }
        }
        catch(Exception ex){
            Log.v(TAG,ex.getMessage());

        }


    }

    public void Disconnect()
    {
        bDisconnect = true;
        if(wsclient.isConnected()) {
            try {
                wsclient.disconnect();
            }
            catch(Exception ex){
                Log.v(TAG,"Disconnect..." + ex.getMessage());
                ex.printStackTrace();
            }
        }

    }

    public Boolean getIsConnected(){
        return wsclient.isConnected();
    }

    public String getServerIP() {
        return ServerIP;
    }

    public void setServerIP(String serverIP) {
        this.ServerIP = serverIP;
    }


    public Boolean Connect(){
        Log.v(TAG,"Connect Start..ServerIP->" + ServerIP + "...ServerPort->" + ServerPort);
        bDisconnect = false;
        try{
            if (wsclient.isConnected() != true) {
                String url = "ws://" + ServerIP + ":" + ServerPort;
                wsclient.connect(url);

                if(wsclient.isConnected() == false) {

                    ((Activity)m_context).runOnUiThread(new Runnable()
                    { public void run() {
                        Iterator iterator = ((Vector) _eventListeners.clone()).iterator();

                        while (iterator.hasNext()) {
                            ChatxEventListener listener = (ChatxEventListener) iterator.next();
                            listener.OnChatEvtConnectServerFailed();
                        }
                    }});

                    return false;
                }

            }

            return true;

        }
        catch(Exception ex){
            Log.v(TAG,"Connect Start...Failed..." + ex.getMessage());
            ex.printStackTrace();

            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                        Connect();
                    }
                    catch(Exception ex){
                        Log.v(TAG,"ReConnect " + ex.getMessage());
                    }
                }
            }).start();;

            return false;

        }
    }

    public Boolean SendMsg(String msg)
    {
        Log.v(TAG,"SendMsg..msg->" + msg);

        try{
            if (wsclient.isConnected() != true) {
                Log.v(TAG,"SendMsg...not Connected to Server...return false...");
                return false;
            }

            wsclient.sendText(msg);


            return true;
        }
        catch(Exception ex){
            Log.v(TAG,"SendMsg...Failed..." + ex.getMessage());
            ex.printStackTrace();
            return false;
        }

    }

    public Boolean VideoGuestQuit()
    {
        Log.v(TAG,"VideoGuestQuit Start..");
        try{
            if (wsclient.isConnected() != true) {
                Log.v(TAG,"VideoGuestQuit...not Connected to Server...return false...");
                return false;
            }

            //发送呼叫信息
            //<?xml version="1.0" encoding="UTF-8"?><APP><Action>VisitorCall</Action><Data vTag="#vTag#" StartTime="#StartTime#" DATA="0"/></APP>
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><APP><Action>VideoGuestQuit</Action>";
            xml += "<Data vTag=\"#vTag#\" chTag=\"#chTag#\"  >";
            xml += "</Data></APP>";

            //chTag标志这个呼叫
            xml = xml.replace("#vTag#", simserialno);
            xml = xml.replace("#chTag#",CurrentChTag);


            //vTag标志这个访客
            //xml = SetXMLProperty(xml,"DATA",_CallData) ;
            SendMsg(xml);

            return true;
        }
        catch(Exception ex){
            Log.v(TAG,"VideoGuestQuit...Failed..." + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public Boolean VideoGuestJoinSucc()
    {
        Log.v(TAG,"VideoGuestJoinSucc Start..");
        try{
            if (wsclient.isConnected() != true) {
                Log.v(TAG,"VideoGuestJoinSucc...not Connected to Server...return false...");
                return false;
            }

            //发送呼叫信息
            //<?xml version="1.0" encoding="UTF-8"?><APP><Action>VisitorCall</Action><Data vTag="#vTag#" StartTime="#StartTime#" DATA="0"/></APP>
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><APP><Action>VideoGuestJoinSucc</Action>";
            xml += "<Data vTag=\"#vTag#\" chTag=\"#chTag#\"  >";
            xml += "</Data></APP>";

            //chTag标志这个呼叫
            xml = xml.replace("#vTag#", simserialno);
            xml = xml.replace("#chTag#",CurrentChTag);


            //vTag标志这个访客
            //xml = SetXMLProperty(xml,"DATA",_CallData) ;
            SendMsg(xml);

            return true;
        }
        catch(Exception ex){
            Log.v(TAG,"VideoGuestJoinSucc...Failed..." + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public Boolean VideoGuestJoinFailed(String Reason)
    {
        Log.v(TAG,"VideoGuestJoinFailed Start..");
        try{
            if (wsclient.isConnected() != true) {
                Log.v(TAG,"VideoGuestJoinFailed...not Connected to Server...return false...");
                return false;
            }

            //发送呼叫信息
            //<?xml version="1.0" encoding="UTF-8"?><APP><Action>VisitorCall</Action><Data vTag="#vTag#" StartTime="#StartTime#" DATA="0"/></APP>
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><APP><Action>VideoGuestJoinFailed</Action>";
            xml += "<Data vTag=\"#vTag#\" chTag=\"#chTag#\" Reason=\"#Reason#\" >";
            xml += "</Data></APP>";

            //chTag标志这个呼叫
            xml = xml.replace("#vTag#", simserialno);
            xml = xml.replace("#chTag#",CurrentChTag);
            xml = xml.replace("#Reason#",Reason);

            //vTag标志这个访客
            //xml = SetXMLProperty(xml,"DATA",_CallData) ;
            SendMsg(xml);

            return true;
        }
        catch(Exception ex){
            Log.v(TAG,"VideoGuestJoinFailed...Failed..." + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }


    public Boolean VisitorSendVideo(String fileName)
    {
        Log.v(TAG,"VisitorSendVideo Start..fileName->" + fileName);

        try {

            if (wsclient.isConnected() != true) {
                Log.v(TAG,"VisitorSendVideo Start...not Connected to Server...return false...");
                return false;
            }

            //发送呼叫信息
            //<?xml version="1.0" encoding="UTF-8"?><APP><Action>VisitorCall</Action><Data vTag="#vTag#" StartTime="#StartTime#" DATA="0"/></APP>
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><APP><Action>VisitorSay</Action>";
            xml += "<Data vTag=\"#vTag#\" chTag=\"#chTag#\" MsgType=\"video\" DATA=\"#DATA#\" >";
            xml += "</Data></APP>";

            //chTag标志这个呼叫
            xml = xml.replace("#vTag#", simserialno);
            xml = xml.replace("#chTag#",CurrentChTag);

            //vTag标志这个访客
            xml = SetXMLProperty(xml,"DATA",fileName) ;
            SendMsg(xml);

            return true;
        }
        catch(Exception ex){
            Log.v(TAG,"VisitorSendVideo Start...Failed..." + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public Boolean VisitorSendImage(String fileName)
    {
        //<?xml version=\"1.0\" encoding=\"UTF-8\"?><WX><Action>VisitorSay</Action><Data OpenID=\"{0}\" NickName=\"{1}\" Oid=\"{2}\" MsgType=\"image\"  SubDir=\"{4}\"></Data><Content><![CDATA[{3}]]></Content></WX>
        Log.v(TAG,"VisitorSendImage Start..fileName->" + fileName);

        try {

            if (wsclient.isConnected() != true) {
                Log.v(TAG,"VisitorSendImage Start...not Connected to Server...return false...");
                return false;
            }

            //发送呼叫信息
            //<?xml version="1.0" encoding="UTF-8"?><APP><Action>VisitorCall</Action><Data vTag="#vTag#" StartTime="#StartTime#" DATA="0"/></APP>
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><APP><Action>VisitorSay</Action>";
            xml += "<Data vTag=\"#vTag#\" chTag=\"#chTag#\" MsgType=\"image\" DATA=\"#DATA#\" >";
            xml += "</Data></APP>";

            //chTag标志这个呼叫
            xml = xml.replace("#vTag#", simserialno);
            xml = xml.replace("#chTag#",CurrentChTag);

            //vTag标志这个访客
            xml = SetXMLProperty(xml,"DATA",fileName) ;
            SendMsg(xml);

            return true;
        }
        catch(Exception ex){
            Log.v(TAG,"VisitorSendImage Start...Failed..." + ex.getMessage());
            ex.printStackTrace();
            return false;
        }

    }
    public Boolean VisitorSay(String msg)
    {
        Log.v(TAG,"VisitorSay Start..msg->" + msg);

        try {

            if (wsclient.isConnected() != true) {
                Log.v(TAG,"VisitorSay Start...not Connected to Server...return false...");
                return false;
            }

            //发送呼叫信息
            //<?xml version="1.0" encoding="UTF-8"?><APP><Action>VisitorCall</Action><Data vTag="#vTag#" StartTime="#StartTime#" DATA="0"/></APP>
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><APP><Action>VisitorSay</Action>";
            xml += "<Data vTag=\"#vTag#\" chTag=\"#chTag#\" MsgType=\"text\" ";
            xml += " DATA=\"0\">"; //随路数据
            xml += "</Data></APP>";

            //chTag标志这个呼叫
            xml = xml.replace("#vTag#", simserialno);

            xml = xml.replace("#chTag#",CurrentChTag);


            //vTag标志这个访客
            xml = SetXMLProperty(xml,"DATA",msg) ;
            SendMsg(xml);

            return true;
        }
        catch(Exception ex){
            Log.v(TAG,"VisitorSay Start...Failed..." + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }


    public Boolean VideoCallFromChat()
    {
        Log.v(TAG,"VideoCallFromChat Start..");

        try {

            if (wsclient.isConnected() != true) {
                Log.v(TAG,"VideoCallFromChat...not Connected to Server...return false...");
                return false;
            }

            //发送呼叫信息
            //<?xml version="1.0" encoding="UTF-8"?><APP><Action>VisitorCall</Action><Data vTag="#vTag#" StartTime="#StartTime#" DATA="0"/></APP>
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><APP><Action>VideoCallFromChat</Action>";
            xml += "<Data vTag=\"#vTag#\" chTag=\"#chTag#\" >";
            xml += "</Data></APP>";

            //chTag标志这个呼叫
            xml = xml.replace("#vTag#", simserialno);
            xml = xml.replace("#chTag#",CurrentChTag);

            //vTag标志这个访客
            //xml = SetXMLProperty(xml,"DATA",_CallData) ;
            SendMsg(xml);

            return true;
        }
        catch(Exception ex){
            Log.v(TAG,"VideoCallFromChat...Failed..." + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /*
    发起Video呼叫
     */
    public Boolean VideoCall(String svc,String CallData) {
        Log.v(TAG,"VideoCall Start..svc->" + svc);

        try {

            if (wsclient.isConnected() != true) {
                Log.v(TAG,"VideoCall Start..svc->" + svc + "...not Connected to Server...return false...");
                return false;
            }

            //发送呼叫信息
            //<?xml version="1.0" encoding="UTF-8"?><APP><Action>VisitorCall</Action><Data vTag="#vTag#" StartTime="#StartTime#" DATA="0"/></APP>
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><APP><Action>VideoCall</Action>";
            xml += "<Data vTag=\"#vTag#\" chTag=\"#chTag#\" SVC=\"#SVC#\" ";
            xml += " DATA=\"" + CallData + "\">"; //随路数据
            xml += "</Data></APP>";

            //chTag标志这个呼叫
            CurrentChTag = java.util.UUID.randomUUID().toString();
            xml = xml.replace("#vTag#", simserialno);
            xml = xml.replace("#chTag#",CurrentChTag);
            xml = xml.replace("#SVC#",svc);

            //vTag标志这个访客
            //xml = SetXMLProperty(xml,"DATA",_CallData) ;
            SendMsg(xml);

            return true;
        }
        catch(Exception ex){
            Log.v(TAG,"Call Start..svc->" + svc + "...Failed..." + ex.getMessage());
            ex.printStackTrace();
            return false;
        }


    }

    public Boolean CallEnd()
    {
        Log.v(TAG,"CallEnd Start...");
        try {

            if (wsclient.isConnected() != true) {
                Log.v(TAG, "CallEnd...not Connected to Server...return false...");
                return false;
            }

            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><APP><Action>EndCall</Action>";
            xml += "<Data vTag=\"#vTag#\" chTag=\"#chTag#\"  ";
            xml += " vName=\"#vName#\" >";
            xml += "</Data></APP>";

            //chTag标志这个呼叫
            xml = xml.replace("#vTag#",simserialno);
            xml = xml.replace("#chTag#",CurrentChTag);
            if(vName.isEmpty()){
                vName = deviceid;
            }
            xml = xml.replace("#vName#",vName);

            //vTag标志这个访客
            SendMsg(xml);

            return true;

        }
        catch(Exception ex){
            Log.v(TAG,"CallEnd...Failed..." + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /*
    发出VisitorCall请求消息,是会通过ChatActivity的创建调用
    真正的发起呼叫是StartChat-->ChatActivity-->SendVisitorCallReq
     */
    public Boolean SendVisitorCallReq() {
        String svc = SvcCode;
        Log.v(TAG,"SendVisitorCallReq Start..svc->" + svc);

        try {

            if (wsclient.isConnected() != true) {
                Log.v(TAG,"SendVisitorCallReq Start..svc->" + svc + "...not Connected to Server...return false...");
                return false;
            }

            //发送呼叫信息
            //<?xml version="1.0" encoding="UTF-8"?><APP><Action>VisitorCall</Action><Data vTag="#vTag#" StartTime="#StartTime#" DATA="0"/></APP>
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><APP><Action>VisitorCall</Action>";
            xml += "<Data vTag=\"#vTag#\" chTag=\"#chTag#\" svc=\"#svc#\" ";
            xml += " vName=\"#vName#\" ";
            xml += " DATA=\"0\">"; //随路数据
            xml += "</Data></APP>";

            //chTag标志这个呼叫
            xml = xml.replace("#vTag#",simserialno);
            xml = xml.replace("#svc#",svc);
            CurrentChTag = java.util.UUID.randomUUID().toString();
            xml = xml.replace("#chTag#",CurrentChTag);
            if(vName.isEmpty()){
                vName = deviceid;
            }
            xml = xml.replace("#vName#",vName);

            //vTag标志这个访客
            xml = SetXMLProperty(xml,"DATA",_CallData) ;
            SendMsg(xml);


            return true;
        }
        catch(Exception ex){
            Log.v(TAG,"SendVisitorCallReq Start..svc->" + svc + "...Failed..." + ex.getMessage());
            ex.printStackTrace();
            return false;
        }


    }

    public void DoSetAssociatedData(String orgStr,String DataName, String DataValue)
    {
        Log.v(TAG,"DoSetAssociatedData...DataName->" + DataName + "...");
        Log.v(TAG,"DoSetAssociatedData...DataValue->" + DataValue + "...");

        _CallData = _CallData.trim();
        if(_CallData.length() == 0){
            _CallData = "<CALLDATA></CALLDATA>";
        }

        String CallDataTmp = SetXMLPropertyForAssoc(_CallData,DataName,DataValue);
        _CallData = CallDataTmp;

        Log.v(TAG,"DoSetAssociatedData..._CallData->" + _CallData + "...");

    }

    private String SetXMLPropertyForAssoc(String xml,String DataName,String DataValue)
    {
        //首先需要判断本PropertyName是否已经存在；
        //如果已经存在，则覆盖
        //如果没有存在，则创建

        try{
            StringReader rd=new StringReader(xml);
            InputSource in=new InputSource(rd);

            DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
            DocumentBuilder builder=factory.newDocumentBuilder();
            Document doc = builder.parse(in);

            //NodeList nl = doc.getElementsByTagName("Data");
            NodeList nl = doc.getChildNodes();
            Node nd = nl.item(0);
            if(nd == null){
                Log.v(TAG,"SetXMLPropertyForAssoc...Src=" + xml + "...PrpName=" + DataName + "...PrpValue=" + DataValue + "...");
                return xml;
            }

            Node nd1 = nd.getAttributes().getNamedItem(DataName);
            if(nd1 != null){
                //存在,更新数值
                nd1.setNodeValue(DataValue);
                String xmlString = documentToString(doc);
                //System.out.println(xmlString);
                Log.v(TAG,"SetXMLPropertyForAssoc...Step1 xmlString->" + xmlString);
                return xmlString;

            }

            //不存在，创建之
            Node nd2 = doc.createAttribute(DataName);
            nd2.setNodeValue(DataValue);
            //nd.getAttributes().setNamedItem(nd2);
            nd.getAttributes().setNamedItem(nd2);


            String xmlString = documentToString(doc);
            Log.v(TAG,"SetXMLPropertyForAssoc...Step2 xmlString->" + xmlString);
            //System.out.println(xmlString);
            return xmlString;



        }
        catch(Exception e){
            Log.v(TAG,"SetXMLPropertyForAssoc..." + e.toString() + "...");
        }

        return xml;

    }


    private String SetXMLProperty(String xml,String DataName,String DataValue)
    {
        //首先需要判断本PropertyName是否已经存在；
        //如果已经存在，则覆盖
        //如果没有存在，则创建

        try{
            StringReader rd=new StringReader(xml);
            InputSource in=new InputSource(rd);

            DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
            DocumentBuilder builder=factory.newDocumentBuilder();
            Document doc = builder.parse(in);

            NodeList nl = doc.getElementsByTagName("Data");
            //NodeList nl = doc.getChildNodes();
            Node nd = nl.item(0);
            if(nd == null){
                Log.v(TAG,"SetXMLAttribute...Src=" + xml + "...PrpName=" + DataName + "...PrpValue=" + DataValue + "...");
                return xml;
            }

            Node nd1 = nd.getAttributes().getNamedItem(DataName);
            if(nd1 != null){
                //存在,更新数值
                nd1.setNodeValue(DataValue);
                String xmlString = documentToString(doc);
                //System.out.println(xmlString);
                Log.v(TAG,"SetXMLAttribute...Step1 xmlString->" + xmlString);
                return xmlString;

            }

            //不存在，创建之
            Node nd2 = doc.createAttribute(DataName);
            nd2.setNodeValue(DataValue);
            //nd.getAttributes().setNamedItem(nd2);
            nd.getAttributes().setNamedItem(nd2);


            String xmlString = documentToString(doc);
            Log.v(TAG,"SetXMLAttribute...Step2 xmlString->" + xmlString);
            //System.out.println(xmlString);
            return xmlString;



        }
        catch(Exception e){
            Log.v(TAG,"SetXMLAttribute..." + e.toString() + "...");
        }

        return xml;

    }

    private String documentToString(org.w3c.dom.Document doc) throws TransformerException {

        // Create dom source for the document
        DOMSource domSource=new DOMSource(doc);

        // Create a string writer
        StringWriter stringWriter=new StringWriter();

        // Create the result stream for the transform
        StreamResult result = new StreamResult(stringWriter);

        // Create a Transformer to serialize the document
        TransformerFactory tFactory =TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty("indent","no");

        Properties props = new Properties();
        props.put(OutputKeys.METHOD, "xml");
        props.put(OutputKeys.OMIT_XML_DECLARATION, "yes");

        transformer.setOutputProperties(props);


        // Transform the document to the result stream
        transformer.transform(domSource, result);
        return stringWriter.toString();
    }


    public int getServerPort() {
        return ServerPort;
    }

    public void setServerPort(int serverPort) {
        ServerPort = serverPort;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getvName() {
        return vName;
    }

    public void setvName(String vName) {
        this.vName = vName;
    }

    public String getCurrentAgentID() {
        return CurrentAgentID;
    }

    public void setCurrentAgentID(String m_CurrentAgentID) {
        this.CurrentAgentID = m_CurrentAgentID;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFileUploadUrl() {
        return FileUploadUrl;
    }

    public void setFileUploadUrl(String fileUploadUrl) {
        FileUploadUrl = fileUploadUrl;
        Log.v(TAG,"FileUploadUrl->" + FileUploadUrl);
    }

    public String getSvcCode() {
        return SvcCode;
    }

    public void setSvcCode(String svcCode) {
        SvcCode = svcCode;
    }



    public Boolean getChating() {
        return IsChating;
    }

    public void setChating(Boolean chating) {
        IsChating = chating;
    }

    public  void StartChat(){
        ChatActivity.chatx = this;
        final Intent intent = new Intent(m_context, ChatActivity.class);
        m_context.startActivity(intent);
    }

}
