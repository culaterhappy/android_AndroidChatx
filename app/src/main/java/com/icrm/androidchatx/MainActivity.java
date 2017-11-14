package com.icrm.androidchatx;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.icrm.ChatActivity;
import com.icrm.Chatx;
import com.icrm.ChatxEventListener;

import com.vidyo.vidyosample.activity.VidyoEventListener;
import com.vidyo.vidyosample.activity.VidyoSampleActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import io.agora.demo.agora.AgoraVideoActivity;
import io.agora.demo.agora.AgoraVideoEventListener;



public class MainActivity extends AppCompatActivity implements ChatxEventListener ,AgoraVideoEventListener,VidyoEventListener {
    private static final String TAG = "ChatXSampleActivity";
    private Chatx chatx;
    private String videoVendor = "";

    static class RecentUseComparator implements Comparator<UsageStats>
    {

        @Override
        public int compare(UsageStats lhs,UsageStats rhs) {

                 return (lhs.getLastTimeUsed()> rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed()== rhs.getLastTimeUsed()) ? 0 : 1;

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chatx = new Chatx(this);
        chatx.addEventListener(this);
        chatx.setServerIP("121.199.10.37");
        //chatx.setServerIP("kf.ehomepay.com.cn");
        chatx.setServerPort(7080);
        chatx.Connect();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goChat();

            }
        });
    }

    private void goChat()
    {

        chatx.setvName("APP用户" + chatx.getModel());
        chatx.setSvcCode("投诉");
        chatx.DoSetAssociatedData("","MyData","MyValue");
        chatx.StartChat();

    }

    private void goAgora(String VENDORKEY,String RoomName)
    {
        AgoraVideoActivity.removeEventListener(this);
        AgoraVideoActivity.addEventListener(this);
        Intent intent = new Intent(this, AgoraVideoActivity.class);
        intent.putExtra(AgoraVideoActivity.EXTRA_CALLING_TYPE, AgoraVideoActivity.CALLING_TYPE_VIDEO);
        intent.putExtra(AgoraVideoActivity.EXTRA_VENDOR_KEY, VENDORKEY);
        intent.putExtra(AgoraVideoActivity.EXTRA_CHANNEL_ID, RoomName);
        startActivity(intent);

    }

    private void goVidyo(String RoomKey,String RoomPin,String userName,String host)
    {
        VidyoSampleActivity.removeEventListener(this);
        VidyoSampleActivity.addEventListener(this);

        //String host = "vidyochina.cn";
        //String host = "api.byshang.com";
        String port = "80";

        final Intent intent = new Intent(this, VidyoSampleActivity.class);
        intent.putExtra("host", host);
        intent.putExtra("port",port);
        intent.putExtra("key", RoomKey);
        intent.putExtra("userName", userName);
        intent.putExtra("pin", RoomPin);
        intent.putExtra("CallType", "GUEST_JOIN");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);



    }

    @Override
    public void OnChatEvtConnectServerFailed(){
        Log.v(TAG,"chatx OnChatEvtConnectServerFailed");
    }


    @Override
    public void OnChatEvtConnected() {
        Log.v(TAG,"chatx connected");
    }

    @Override
    public void OnChatEvtDisConnected() {
        Log.v(TAG,"chatx disconnected");
    }

    @Override
    public void OnChatEvtNewMsg(String msg) {
        //解析msg

    }

    @Override
    public void OnChatActivityQuit()
    {
        Log.v(TAG,"chatx OnChatActivityQuit");
    }


    @Override
    public void OnNoAgents(String reason){

    }

    @Override
    public void OnVideoGuestJoin(String vendor,String param1,String param2,String param3,String param4 )
    {
        Log.v(TAG,"MainActivity.OnVideoGuestJoin vendor->" + vendor);
        videoVendor = vendor;
        if(vendor.equals("agora")){


            String VENDORKEY = param1;
            String RoomName = param2;

            Log.v(TAG,"MainActivity.OnVideoGuestJoin VENDORKEY->" + VENDORKEY);
            Log.v(TAG,"MainActivity.OnVideoGuestJoin RoomName->" + RoomName);
            goAgora(VENDORKEY,RoomName);

            chatx.VideoGuestJoinSucc();

        }
        else if(vendor.equals("vidyo")){



            String RoomKey = param1;
            String RoomPin = param2;
            String userName = param3;
            String host = param4;

            Log.v(TAG,"MainActivity.OnVideoGuestJoin RoomKey->" + RoomKey);
            Log.v(TAG,"MainActivity.OnVideoGuestJoin RoomPin->" + RoomPin);
            Log.v(TAG,"MainActivity.OnVideoGuestJoin userName->" + userName);
            Log.v(TAG,"MainActivity.OnVideoGuestJoin host->" + host);

            goVidyo(RoomKey,RoomPin,userName,host);

            chatx.VideoGuestJoinSucc();

        }

    }

    @Override
    public void OnVideoQuit()
    {

        if(videoVendor.equals("agora")){
            AgoraVideoActivity.SendMsg(AgoraVideoActivity.MSG_BYE);
        }
        else if(videoVendor.equals("vidyo")){
            VidyoSampleActivity.SendMsg(VidyoSampleActivity.MSG_BYE);
        }
    }

    @Override
    public void OnEvtVideoQuit()
    {
        chatx.VideoGuestQuit();
    }

    /*
    private String getRunningActivityName(){
        //ActivityManager activityManager=(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        //String runningActivity=activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        String runningActivity = getTopPackage();
        return runningActivity;

    }


    private String getTopPackage() {

        Log.d(TAG, "===getTopPackage=");

        long ts = System.currentTimeMillis();

        RecentUseComparator mRecentComp = new RecentUseComparator();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");

        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST, ts - 10000, ts);  //查询ts-10000 到ts这段时间内的UsageStats，由于要设定时间限制，所以有可能获取不到
        if (usageStats == null)
            return "";

        if (usageStats.size() == 0)
            return "";

        Collections.sort(usageStats, mRecentComp);

        Log.d(TAG, "====usageStats.get(0).getPackageName()" + usageStats.get(0).getPackageName());
        return usageStats.get(0).getPackageName();

    }
    */

    @Override
    protected void onDestroy() {
        Log.v(TAG,"MainActivity.onDestroy start...");
        super.onDestroy();
        if(chatx != null){
            if(chatx.getIsConnected() == true){
                chatx.Disconnect();
            }
        }
    }



}
