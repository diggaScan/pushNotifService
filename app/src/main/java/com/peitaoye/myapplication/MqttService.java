package com.peitaoye.myapplication;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;


public class MqttService extends Service {

    //广播是否已被注册的标签
    private boolean receive_tag=false;
    private final String TAG=this.getClass().getCanonicalName();
    private static final String SERVIC_START="com.peitaoye.myapplication.START";
    private static final String SERVICE_RECONN="com.peitaoye.myapplication.RECONN";
    private static final String SERVICE_KEEP_ALIVE="com.peitaoye.myapplication.KEEP_ALIVE";
    private static final String SERVICE_DISCONN="com.peitaoye.myapplication.DISCONN";

    private final String PRE_KEY="mqtt_conn";
    private final String DEVICE_ID_KEY="DeviceID";
    private final String IS_STARTED="isSTARTED";
    private final String PRR_INTERNAL="retry_internal";
    private MqttConn myConn;

    // whether the service is running
    private boolean isStarted=false;

    private SharedPreferences myPres=null;
    private ConnectivityManager conMan=null;
    private NotificationManager notiMan=null;

    public static void connService(Context context){
        Intent intent=new Intent(context,MqttService.class);
        intent.setAction(SERVIC_START);
        context.startService(intent);
    }
    public static void disConnService(Context context){
        Intent intent=new Intent(context,MqttService.class);
        intent.setAction(SERVICE_DISCONN);
        context.startService(intent);
    }

    public static void reconnService(Context context){
        Intent intent=new Intent(context,MqttService.class);
        intent.setAction(SERVICE_RECONN);
        context.startService(intent);
    }
    public MqttService() {
        super();
    }

    BroadcastReceiver conn_receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("info","get netWork info");



            if(isNetworkAvailable()){
                if(myConn!=null){
                    myConn.conn();
                }else{
                    connect();
                }
            }else{
                Log.d("info","no network found in receiver");
                cancelAlive();
            }

        }
    };
    private void setisStarted(boolean isStart){
        this.isStarted=isStart;

    }
    private boolean getisStart(){
        return isStarted;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        //initialize all the managers
        myPres=getSharedPreferences(PRE_KEY, MODE_PRIVATE);
        conMan=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        if(getisStart()){
            handleServiceCrash();
        }
    }

    private void handleServiceCrash(){
        stop();
        connect();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action=intent.getAction();
        switch(action){
            case SERVIC_START:
                Log.d("info","start to connect Mqtt");
                connect();
                break;
            case SERVICE_RECONN:
                Log.d("info","Start to reconn...");
                connect();
                break;
            case SERVICE_KEEP_ALIVE:
                Log.d("info","start to send alive packet");
                myConn.sendAlivePacket();
                break;
            case SERVICE_DISCONN:
                Log.d("info","start to disconn..");
                stop();
                stopSelf();


        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void stop(){
        cancelReconn();
        cancelAlive();
        if(myConn!=null){
            myConn.disconnect();
            myConn=null;
        }
    }
    private void cancelAlive(){
        Intent intent=new Intent(this,MqttService.class);
        intent.setAction(SERVICE_KEEP_ALIVE);
        AlarmManager alarmMgr=(AlarmManager)getSystemService(ALARM_SERVICE);
        PendingIntent pi=PendingIntent.getService(this,1,intent,0);
        alarmMgr.cancel(pi);
    }
    private void connect(){
        if(getisStart()&&myConn!=null){
            Log.d("info","connection has already been established.");
            return;
        }
        if(!receive_tag){
            registerReceiver(conn_receiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            receive_tag=true;
        }
        if(isNetworkAvailable()){
                myConn=new MqttConn();
        }else{
            Log.d("info","No network is available");
        }

    }

    private void cancelReconn(){
        Intent intent=new Intent(this,MqttService.class);
        intent.setAction(SERVICE_RECONN);
        PendingIntent pi=PendingIntent.getService(this,1,intent,0);
        AlarmManager alarmMgr=(AlarmManager)getSystemService(ALARM_SERVICE);
        alarmMgr.cancel(pi);
    }

    private void scheduleRecpnnect(){
        Intent intent=new Intent(this,MqttService.class);
        intent.setAction(SERVICE_RECONN);
        PendingIntent pi=PendingIntent.getService(this,1,intent,0);
        AlarmManager alarmMgr=(AlarmManager)getSystemService(ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP,1000*10,pi);

    }

    public boolean isNetworkAvailable(){
        NetworkInfo netInfo=conMan.getActiveNetworkInfo();
        if(netInfo!=null){
            Log.d("info","idConnected:"+netInfo.isConnected());
            return netInfo.isConnected();
        }else{
            return false;
        }
    }

    public void keepAlivePacket(){
        Intent intent=new Intent(this,MqttService.class);
        intent.setAction(SERVICE_KEEP_ALIVE);
        AlarmManager alarmMgr=(AlarmManager)getSystemService(ALARM_SERVICE);
        PendingIntent pi=PendingIntent.getService(this,1,intent,0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+1000*1,1000*1,pi);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(receive_tag){
            unregisterReceiver(conn_receiver);
            receive_tag=false;
        }
        myPres=null;
        notiMan=null;
        conMan=null;
        setisStarted(false);
        myConn.disconnect();
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class MqttConn{
        final String HOST_NAME="192.168.2.143";
        final String PORT_NUM="1883";
        String CLIENT_ID="MQTT_TEST_CLIENT";

        MqttClientPersistence myPersis=null;

        MqttClient mqttClient;

        MqttConnectOptions myOptions;

        //publishing configuration
        MqttMessage p_message=null;
        final String TOPIC="my_local_Topic";

        //subscriber configuration
        final String topic_Filter="my_local_filter";
        public MqttConn() {
            String url_spec="tcp://"+HOST_NAME+":"+PORT_NUM;

                myPersis=new MqttDefaultFilePersistence(getFilesDir().getAbsolutePath());
                try{
                    mqttClient=new MqttClient(url_spec,CLIENT_ID,myPersis);
                }catch(MqttException e){
                e.printStackTrace();
            }

                myOptions=new MqttConnectOptions();
                myOptions.setCleanSession(true);
                myOptions.setConnectionTimeout(5);
                myOptions.setKeepAliveInterval(60);
                myOptions.setAutomaticReconnect(true);
                mqttClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        //使用broadcastreceiver
//                        Log.d("info","connectionLost（）");
//                        setisStarted(false);
//                        cancelAlive();
//                        if(isNetworkAvailable()){
//                                connect();
//                        }else{
//                            Log.d("info","No Network Availabel !!!");
//                        }
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        Log.d("info","messageArrived()");
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        Log.d("info","deliveryComplete()");
                    }
                });
            conn();

        }

        private void sendAlivePacket(){
            try{
                mqttClient.publish("alive",new byte[]{0},0,false);
            }catch (MqttException e){
                Log.d("info",e.getMessage()+"   "+e.getCause());
            }
        }
        private void conn(){
            try {
                mqttClient.connect(myOptions);
                Log.d("info","connection succeed");
                setisStarted(true);
                keepAlivePacket();
            }catch(MqttException e){
                Log.d("info",e.getMessage()+"  "+ e.getCause());
                scheduleRecpnnect();
            }
        }
        private void disconnect(){
            try{
                mqttClient.disconnect();
            }catch(MqttException e){
                e.printStackTrace();











































































































































































































































































































































































































































































































































            }

            mqttClient=null;
        }
    }
}
