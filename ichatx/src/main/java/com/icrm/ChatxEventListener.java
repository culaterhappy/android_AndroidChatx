package com.icrm;

import java.util.EventListener;
import java.util.Vector;

/**
 * Created by zhutong on 2016/5/26.
 */
public interface ChatxEventListener extends EventListener {
    public void OnChatEvtNewMsg(String msg);
    public void OnChatEvtConnectServerFailed();
    public void OnChatActivityQuit();
    public void OnChatEvtConnected();
    public void OnChatEvtDisConnected();
    public void OnNoAgents(String reason);

    public void OnVideoGuestJoin(String vendor,String param1,String param2,String param3,String param4 );
    public void OnVideoQuit();
}