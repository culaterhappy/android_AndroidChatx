package com.icrm;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ichatx.R;
import com.jialin.chat.Message;
import com.jialin.chat.MessageAdapter;
import com.jialin.chat.MessageInputToolBox;
import com.jialin.chat.OnOperationListener;
import com.jialin.chat.Option;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by zhutong on 2016/5/31.
 */
public class ChatActivity extends FragmentActivity {

    private static final String TAG = "VidyoSampleActivity.ChatActivity";

    public static Chatx chatx;

    private MessageInputToolBox box;
    private ListView listView;
    private MessageAdapter adapter;

    public static final String broadcastaction = "chatx.msg";
    long temptime;

    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.chat);

        initMessageInputToolBox();

        initListView();

        IntentFilter filter = new IntentFilter(broadcastaction);
        registerReceiver(broadcastReceiver, filter);


        chatx.SendVisitorCallReq();


    }

    protected void onDestroy() {
        Log.v(TAG,"onDestory");
        chatx.CallEnd();
        //OnChatActivityQuit
        chatx.NotifyOnChatActivityQuit();
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private Boolean DownLoadFile(String urlStr,String path,String fileName)
    {
        Log.v(TAG,"DownLoadFileNew...url->" + urlStr + "...path->" + path + "...fileName->" + fileName + "...V2...");


        URL m;
        InputStream i = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream out =null;
        try {


            urlStr = URLEncoder.encode(urlStr,"UTF-8").replaceAll("\\+", "%20");
            urlStr = urlStr.replaceAll("%3A", ":").replaceAll("%2F", "/");

            Log.v(TAG,"DownLoadFileNew...url encode->" + urlStr);
            m = new URL(urlStr);
            i = (InputStream) m.getContent();
            bis = new BufferedInputStream(i,1024 * 8);
            out = new ByteArrayOutputStream();
            int len=0;
            byte[] buffer = new byte[1024];
            while((len = bis.read(buffer)) != -1){
                out.write(buffer, 0, len);
            }
            out.close();
            bis.close();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String SDCard = Environment.getExternalStorageDirectory() + "";
        String pathName = SDCard + "/" + path + "/" + fileName;//文件存储路径
        String dir = SDCard + "/" + path;
        File file = new File(pathName);
        byte[] data = out.toByteArray();

        if(isPicure(fileName)) {
            Log.v(TAG,fileName + " is Picture...will process Picture...");
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            try {
                new File(dir).mkdir();//新建文件夹
                file.createNewFile();//新建文件

                FileOutputStream fout = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                fout.flush();
                fout.close();
            } catch (Exception ex) {
                Log.v(TAG, ex.getMessage());
                ex.printStackTrace();
                return false;
            }
            return true;
        }
        else{
            Log.v(TAG,fileName + " is not Picture...will process as normal file...");
            try {
                FileOutputStream fops = new FileOutputStream(file);
                fops.write(data);
                fops.flush();
                fops.close();
            }
            catch (Exception ex) {
                Log.v(TAG, ex.getMessage());
                ex.printStackTrace();
                return false;
            }

            return true;
        }

    }

    private Boolean isPicure(String fileName)
    {
        if(fileName.toLowerCase().endsWith(".png") || fileName.toLowerCase().endsWith(".jpg") ||  fileName.toLowerCase().endsWith(".jpeg") || fileName.toLowerCase().endsWith(".bmp") ){
            return true;
        }
        else{
            return false;
        }
    }

    ;
    private Boolean DownLoadFileOld(String urlStr,String path,String fileName)
    {
        Log.v(TAG,"DownLoadFile...url->" + urlStr + "...path->" + path + "...fileName->" + fileName);
        OutputStream output=null;
        try {
                /*
                 * 通过URL取得HttpURLConnection
                 * 要网络连接成功，需在AndroidMainfest.xml中进行权限配置
                 * <uses-permission android:name="android.permission.INTERNET" />
                 */
            URL url=new URL(urlStr);
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            //取得inputStream，并将流中的信息写入SDCard

                /*
                 * 写前准备
                 * 1.在AndroidMainfest.xml中进行权限配置
                 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
                 * 取得写入SDCard的权限
                 * 2.取得SDCard的路径： Environment.getExternalStorageDirectory()
                 * 3.检查要保存的文件上是否已经存在
                 * 4.不存在，新建文件夹，新建文件
                 * 5.将input流中的信息写入SDCard
                 * 6.关闭流
                 */
            String SDCard=Environment.getExternalStorageDirectory()+"";
            String pathName=SDCard+"/"+path+"/"+fileName;//文件存储路径

            File file=new File(pathName);
            InputStream input=conn.getInputStream();
            if(file.exists()){
                Log.v(TAG,"DownLoadFile...Exists already...");
                return true;
            }else{
                String dir=SDCard+"/"+path;
                new File(dir).mkdir();//新建文件夹
                file.createNewFile();//新建文件
                output=new FileOutputStream(file);
                //读取大文件
                byte[] buffer=new byte[4*1024];
                while(input.read(buffer)!=-1){
                    output.write(buffer);
                }
                output.flush();


            }
        } catch (Exception e) {
            Log.v(TAG,"DownLoadFile...Download failed->" + e.getMessage());
            e.printStackTrace();
        }finally{
            try {
                if(output != null) {
                    output.close();
                }
                Log.v(TAG,"DownLoadFile...Download succ...");
                return true;

            } catch (IOException e) {
                Log.v(TAG,"DownLoadFile...Download failed->" + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }

    protected void ProcessMsg(String msg) {
        try {
            Log.v(TAG, "ProcessMsg..." + msg);

            StringReader rd = new StringReader(msg);
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

            if (action.equals("CallResult")) {


                nl = doc.getElementsByTagName("Data");
                nd = nl.item(0);
                String Result = nd.getAttributes().getNamedItem("Result").getNodeValue();
                String Reason = nd.getAttributes().getNamedItem("Reason").getNodeValue();

                if (Result.equals("-1")) {
                    String sReason = Reason;
                    if (Reason.equals("No Agent Avail")) {

                        sReason = "沒有空閒坐席";

                    } else {
                        sReason = Reason;
                    }

                    ShowStaticNotice("<font color='blue'>" + sReason + "</font>");
                    chatx.NotifyNoAgents(sReason);

                } else if (Result.equals("0")) {
                    String sReason = Reason;
                    ShowStaticNotice("<font color='blue'>" + sReason + "</font>");
                    chatx.setChating(true);
                }

            }
            else if(action.equals("VideoCallFromChatFail")){
                String sReason = "点对点视频失败.当前座席没有开启点对点视频功能";
                ShowStaticNotice("<font color='red'>" + sReason + "</font>");
            }
            else if(action.equals("VideoCallFromChatDeny")){
                String sReason = "点对点视频失败.当前座席拒绝点对点视频请求";
                ShowStaticNotice("<font color='red'>" + sReason + "</font>");
            }
            else if (action.equals("OperatorSay"))
            {
                //OperatorSay
                //<?xml version="1.0" encoding="UTF-8"?><APP><Action>OperatorSay</Action><Data vTag="898600910114f0548775" Msg="座席:admin 为您服务，请问有什么可以帮助您?" /></APP>
                nl = doc.getElementsByTagName("Data");
                nd = nl.item(0);
                String Msg = nd.getAttributes().getNamedItem("Msg").getNodeValue();
                String AgentID = nd.getAttributes().getNamedItem("AgentID").getNodeValue();
                chatx.setCurrentAgentID(AgentID);

                if(Msg.equals("ZRG")){
                    Log.v(TAG,"OperatorSay...ZRG...will Try to TransferToAgent...");
                    chatx.SendVisitorCallReq();
                }
                else {
                    ShowOperatorMsg(Msg);
                }




            }
            else if(action.equals("OperatorSendImage")){
                nl = doc.getElementsByTagName("Data");
                nd = nl.item(0);
                final String imageId = nd.getAttributes().getNamedItem("imageId").getNodeValue();

                final String url = chatx.getFileUploadUrl() + "\\WXData\\" + imageId;
                final String fileName = imageId;

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {

                            Boolean bResult = DownLoadFile(url,chatx.getApplicationName(),fileName);
                            if(bResult == true) {
                                String SDCard=Environment.getExternalStorageDirectory()+"";
                                final String pathName= SDCard+"/"+chatx.getApplicationName()+"/"+fileName;//文件存储路径


                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        //ShowOperatorMsg("收到对方发送的图片:");
                                        if(isPicure(pathName) == true) {
                                            ShowOperatorImage(pathName);
                                        }
                                        else{
                                            ShowOperatorFile(pathName);
                                        }
                                    }
                                });

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();



            }
            else if(action.equals("OperatorCloseCall")){
                //OperatorCloseCall
                //<?xml version="1.0" encoding="UTF-8"?><APP><Action>OperatorCloseCall</Action><Data vTag="898600910114f0548775" Reason="和座席的洽谈已经结束!" /></APP>

                nl = doc.getElementsByTagName("Data");
                nd = nl.item(0);
                String sReason = nd.getAttributes().getNamedItem("Reason").getNodeValue();

                ShowStaticNotice("<font color='red' bold='true'>" + sReason + "</font>");
                chatx.setChating(false);
            }


        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
            ex.printStackTrace();
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String data = intent.getExtras().getString("data");
            ProcessMsg(data);

        }
    };

    private void ShowOperatorMsg(String msg)
    {
        msg = chatx.ProcessEmotj(msg);
        Message message = new Message(Message.MSG_TYPE_TEXT, Message.MSG_STATE_SUCCESS, chatx.getCurrentAgentID(), "avatar", chatx.getvName(), "avatar", msg, false, true, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 8),"");
        adapter.getData().add(message);
        listView.setSelection(listView.getBottom());
    }

    private void ShowOperatorFile(String filePath)
    {
        Message message = new Message(Message.MSG_TYPE_LINK, Message.MSG_STATE_SUCCESS, chatx.getCurrentAgentID() , "avatar", chatx.getvName(), "avatar", filePath, false, true, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 8),"");
        adapter.getData().add(message);
        listView.setSelection(listView.getBottom());
    }

    private void ShowVisitorMsg(String msg)
    {
        msg = chatx.ProcessEmotj(msg);
        Message message = new Message(Message.MSG_TYPE_TEXT, Message.MSG_STATE_SUCCESS, chatx.getvName() , "avatar", chatx.getCurrentAgentID(), "avatar", msg, true, true, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 8),"");
        adapter.getData().add(message);
        listView.setSelection(listView.getBottom());
    }

    private void ShowVisitorVideo(String uri)
    {
        Log.v(TAG,"ShowVisitorVideo...uri->" + uri);
        Message message = new Message(Message.MSG_TYPE_PHOTO, Message.MSG_STATE_SUCCESS, chatx.getvName(), "avatar",chatx.getCurrentAgentID(), "avatar", uri.toString(), false, true, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 7),"");

        adapter.getData().add(message);
        listView.setSelection(listView.getBottom());
    }

    private void ShowOperatorImage(String uri)
    {
        Log.v(TAG,"ShowOperatorImage...uri->" + uri);
        Message message = new Message(Message.MSG_TYPE_PHOTO, Message.MSG_STATE_SUCCESS, chatx.getvName(), "avatar",chatx.getCurrentAgentID(), "avatar", uri.toString(), false, true, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 7),"");

        adapter.getData().add(message);
        listView.setSelection(listView.getBottom());
    }


    private void ShowVisitorImage(String uri)
    {
        Log.v(TAG,"ShowVisitorImage...uri->" + uri);
        Message message = new Message(Message.MSG_TYPE_PHOTO, Message.MSG_STATE_SUCCESS, chatx.getvName(), "avatar",chatx.getCurrentAgentID(), "avatar", uri.toString(), true, true, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 7),"");

        adapter.getData().add(message);
        listView.setSelection(listView.getBottom());
        //adapter.notifyDataSetChanged();
    }



    private void ShowStaticNotice(String msg)
    {
        Message message = new Message(0, 1, "", "", "", "", "", true, true, new Date(),msg);

        adapter.getData().add(message);
        listView.setSelection(listView.getBottom());

    }

    public static String getPath(Context context, Uri uri) {

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection,null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }

        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /* 上传文件至Server的方法 */
    private Boolean uploadFile(String actionUrl,String uploadFile,String newName)
    {
        Log.v(TAG,"uploadFile...actionUrl->" + actionUrl);
        Log.v(TAG,"uploadFile...uploadFile->" + uploadFile);


        String end ="\r\n";
        String twoHyphens ="--";
        String boundary = UUID.randomUUID().toString();;
        try
        {
            ContentResolver cr = this.getContentResolver();
            Uri u = Uri.parse(uploadFile);


            URL url =new URL(actionUrl);
            HttpURLConnection con=(HttpURLConnection)url.openConnection();
          /* 允许Input、Output，不使用Cache */
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
          /* 设置传送的method=POST */
            con.setRequestMethod("POST");
          /* setRequestProperty */
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");
            con.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary="+boundary);
          /* 设置DataOutputStream */
            DataOutputStream ds =
                    new DataOutputStream(con.getOutputStream());
            ds.writeBytes(twoHyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; "+
                    "name=\"File1\";filename=\""+
                    newName +"\""+ end);
            ds.writeBytes(end);
          /* 取得文件的FileInputStream */




            //FileInputStream fStream =new FileInputStream(uploadFile);
            InputStream fStream = cr.openInputStream(u);
          /* 设置每次写入1024bytes */
            int bufferSize =1024;
            byte[] buffer =new byte[bufferSize];
            int length =-1;
          /* 从文件读取数据至缓冲区 */
            while((length = fStream.read(buffer)) !=-1)
            {
            /* 将资料写入DataOutputStream中 */
                ds.write(buffer, 0, length);
            }
            ds.writeBytes(end);
            ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
          /* close streams */
            fStream.close();
            ds.flush();
          /* 取得Response内容 */
            int res = con.getResponseCode();
            Log.e(TAG, "response code:" + res);

            InputStream is = con.getInputStream();
            int ch;
            StringBuffer b =new StringBuffer();
            while( ( ch = is.read() ) !=-1 )
            {
                b.append( (char)ch );
            }
          /* 将Response显示于Dialog */
            Log.v(TAG,"上传成功:" + b.toString().trim());
          /* 关闭DataOutputStream */
            ds.close();

            return true;
        }
        catch(Exception e)
        {
            Log.v(TAG,"文件上传失败:" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {


            Uri uriBmp = data.getData();
            String path = "";
            if(uriBmp == null){
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
                if (data.getData() != null)
                {
                    uriBmp = data.getData();
                }
                else
                {
                    //uriBmp  = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null));

                    try {
                        String SDCard = Environment.getExternalStorageDirectory() + "";

                        String fileName = java.util.UUID.randomUUID().toString() + ".jpg";
                        path = SDCard + "/" + chatx.getApplicationName() + "/" + fileName;//文件存储路径
                        String dir = SDCard + "/" + chatx.getApplicationName() + "/";
                        File file = new File(path);

                        Boolean bResult = new File(dir).mkdirs();//新建文件夹
                        file.createNewFile();//新建文件

                        FileOutputStream fout = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);

                        uriBmp = Uri.fromFile(file);


                    }
                    catch(Exception ex){
                        Log.v(TAG,ex.getMessage());
                    }

                }

            }

            final Uri uri = uriBmp;

            Log.e(TAG, uri.toString());


            path = getPath(this, uri);
            Log.v(TAG,"path->" + path);

            File file = new File(path);
            final String newName = file.getName();
            Log.v(TAG,"fileSize->" + file.length() + "...newName->" + newName + "...requestCode->" + requestCode);


            final int frequestCode = requestCode;

            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {

                        if(uploadFile(chatx.getFileUploadUrl() + "FileUpload.aspx",uri.toString(),newName) == true) {
                            if(frequestCode == 1 || frequestCode == 3) {
                                chatx.VisitorSendImage(newName);
                            }
                            else{
                                chatx.VisitorSendVideo(newName);
                            }
                        }
                        else{
                            //Toast.makeText((Context)this, "图片发送失败!", 1000).show();
                            ShowStaticNotice("图片发送失败");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            if(requestCode == 1 || requestCode == 3) {
                ShowVisitorImage(uri.toString());
            }
            else if(requestCode == 2){
                ShowVisitorVideo(uri.toString());
            }

            //提交给Operator


			/*
			ContentResolver cr = this.getContentResolver();
			try {
				Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));


			} catch (FileNotFoundException e) {
				Log.e("Exception", e.getMessage(),e);
			}
			*/

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)//主要是对这个函数的复写
    {
        // TODO Auto-generated method stub
        if(chatx.getChating() == false){
            if((keyCode == KeyEvent.KEYCODE_BACK)&&(event.getAction() == KeyEvent.ACTION_DOWN)) {
                finish();
                return true;
            }
            else {
                return super.onKeyDown(keyCode, event);
            }

        }

        if((keyCode == KeyEvent.KEYCODE_BACK)&&(event.getAction() == KeyEvent.ACTION_DOWN))
        {

            if(System.currentTimeMillis() - temptime >2000) // 2s内再次选择back键有效
            {
                System.out.println(Toast.LENGTH_LONG);
                Toast.makeText(this, "退出将会结束聊天,请再按一次返回退出", Toast.LENGTH_LONG).show();
                temptime = System.currentTimeMillis();
            }
            else {
                finish();

            }

            return true;

        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * init MessageInputToolBox
     */
    @SuppressLint("ShowToast")
    private void initMessageInputToolBox(){
        box = (MessageInputToolBox) findViewById(R.id.messageInputToolBox);
        box.setOnOperationListener(new OnOperationListener() {

            @Override
            public void send(String content) {

                ShowVisitorMsg(content);

                chatx.VisitorSay(content);
                //Just demo
                //createReplayMsg(message);
            }

            @Override
            public void selectedFace(String content) {

                Log.v(TAG,"selectedFace...selectedFace->" + content);

                /*
                System.out.println("===============" + content);
                Message message = new Message(Message.MSG_TYPE_FACE, Message.MSG_STATE_SUCCESS, "Tomcat", "avatar", "Jerry", "avatar", content, true, true, new Date(),"");
                adapter.getData().add(message);
                listView.setSelection(listView.getBottom());
                */
                String facecode = chatx.getEmotjFaceCode(content);
                box.AddTextAtCurrentPos(facecode);
                //Just demo
                //createReplayMsg(message);

            }

            private void SelectImageOrCamera()
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setItems(new String[] { "拍照", "从手机相册里选择" }, new DialogInterface.OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (which == 0) // 拍照
                        {
                            Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(camera, 3);
                        }
                        else if (which == 1) // 从手机相册选择
                        {
                            Intent picture = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(picture, 1);
                        }
                    }
                });
                Dialog dialog = builder.create();
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.show();
            }

            @Override
            public void selectedFuncation(int index) {

                System.out.println("===============" + index);

                switch (index) {
                    case 0: {

                        SelectImageOrCamera();
                        break;

                        //do some thing
                        /*
                        Intent intent = new Intent();

                        intent.setType("image/*");
                	    intent.setAction(Intent.ACTION_GET_CONTENT);
                	    startActivityForResult(intent, 1);

                        break;
                        */

                    }

                    case 1: {
                        //do some thing
                        //video/*
                        //do some thing
                        Intent intent = new Intent();
                	    /* 开启Pictures画面Type设定为image */
                        intent.setType("video/*");
                	    /* 使用Intent.ACTION_GET_CONTENT这个Action */
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                	    /* 取得相片后返回本画面 */
                        startActivityForResult(intent, 2);

                        break;
                    }
                    case 2: {
                        //进行vidyo视频
                        chatx.VideoCallFromChat();
                        box.hide();
                        ShowStaticNotice("已经发出视频请求，等待对方回应.");
                        break;
                    }
                    default:
                        break;
                }
                //Toast.makeText(ChatActivity.this, "Do some thing here, index :" +index, 1000).show();

            }

        });

        ArrayList<String> faceNameList = new ArrayList<String>();
        for(int x = 0; x <= 30; x++){
            faceNameList.add("q"+x);
        }

        /*
        for(int x = 1; x <= 10; x++){
            faceNameList.add("big"+x);
        }
        for(int x = 1; x <= 10; x++){
            faceNameList.add("big"+x);
        }

        ArrayList<String> faceNameList1 = new ArrayList<String>();
        for(int x = 1; x <= 7; x++){
            faceNameList1.add("cig"+x);
        }


        ArrayList<String> faceNameList2 = new ArrayList<String>();
        for(int x = 1; x <= 24; x++){
            faceNameList2.add("dig"+x);
        }
        */

        Map<Integer, ArrayList<String>> faceData = new HashMap<Integer, ArrayList<String>>();
        //faceData.put(R.drawable.em_cate_magic, faceNameList2);
        //faceData.put(R.drawable.em_cate_rib, faceNameList1);
        faceData.put(R.drawable.em_cate_duck, faceNameList);
        box.setFaceData(faceData);


        List<Option> functionData = new ArrayList<Option>();
        Option takePhotoOption = new Option(this, "发视频", R.drawable.take_photo);
        Option galleryOption = new Option(this, "发图片", R.drawable.gallery);
        Option vidyoOption = new Option(this, "实时视频", R.drawable.vidyo);

        functionData.add(galleryOption);
        functionData.add(takePhotoOption);
        functionData.add(vidyoOption);

        box.setFunctionData(functionData);
    }



    private void initListView(){
        listView = (ListView) findViewById(R.id.messageListview);

        //create Data
        /*
        Message message = new Message(Message.MSG_TYPE_TEXT, Message.MSG_STATE_SUCCESS, "Tom", "avatar", "Jerry", "avatar", "Hi", false, true, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 8));
        Message message1 = new Message(Message.MSG_TYPE_TEXT, Message.MSG_STATE_SUCCESS, "Tom", "avatar", "Jerry", "avatar", "Hello World", true, true, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24)* 8));
        Message message2 = new Message(Message.MSG_TYPE_PHOTO, Message.MSG_STATE_SUCCESS, "Tom", "avatar", "Jerry", "avatar", "device_2014_08_21_215311", false, true, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 7));
        Message message3 = new Message(Message.MSG_TYPE_TEXT, Message.MSG_STATE_SUCCESS, "Tom", "avatar", "Jerry", "avatar", "Haha", true, true, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 7));
        Message message4 = new Message(Message.MSG_TYPE_FACE, Message.MSG_STATE_SUCCESS, "Tom", "avatar", "Jerry", "avatar", "big3", false, true, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 7));
        Message message5 = new Message(Message.MSG_TYPE_FACE, Message.MSG_STATE_SUCCESS, "Tom", "avatar", "Jerry", "avatar", "big2", true, true, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 6));
        Message message6 = new Message(Message.MSG_TYPE_TEXT, Message.MSG_STATE_FAIL, "Tom", "avatar", "Jerry", "avatar", "test send fail", true, false, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 6));
        Message message7 = new Message(Message.MSG_TYPE_TEXT, Message.MSG_STATE_SENDING, "Tom", "avatar", "Jerry", "avatar", "test sending", true, true, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 6));

        List<Message> messages = new ArrayList<Message>();
        messages.add(message);
        messages.add(message1);
        messages.add(message2);
        messages.add(message3);
        messages.add(message4);
        messages.add(message5);
        messages.add(message6);
        messages.add(message7);
        */

        Message message = new Message(Message.MSG_TYPE_TEXT, Message.MSG_STATE_SUCCESS, "", "", "", "", "", false, true, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 8),"转接座席中,请稍候");

        List<Message> messages = new ArrayList<Message>();
        messages.add(message);

        adapter = new MessageAdapter(this, messages);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();




    }



    private void createReplayMsg(Message message){

        final Message reMessage = new Message(message.getType(), 1, "Tom", "avatar", "Jerry", "avatar",
                message.getType() == 0 ? "Re:" + message.getContent() : message.getContent(),
                false, true, new Date(),""
        );
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * (new Random().nextInt(3) +1));
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            adapter.getData().add(reMessage);
                            listView.setSelection(listView.getBottom());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }




}
