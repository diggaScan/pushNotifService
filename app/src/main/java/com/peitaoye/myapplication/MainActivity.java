package com.peitaoye.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    TextView notif_text;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MqttService.connService(this);
//
//        String DEVICE_ID= Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
//        Log.d("deviceid",DEVICE_ID);
//
//        notif_text=(TextView)findViewById(R.id.textView);
//        String url_spec="tcp://"+HOST_NAME+":"+PORT_NUM;
//        try{
//            mqttClient=new MqttClient(url_spec,CLIENT_ID,new MqttDefaultFilePersistence(getCacheDir().getAbsolutePath()));
//        }catch(Exception e){
//            e.printStackTrace();
//            Log.d("info","fail to create client");
//        }
//        mqttClient.setCallback(new MqttCallback() {
//            @Override
//            public void connectionLost(Throwable cause) {
//
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                Log.d("info",topic+"from MQttcallback");
//                notif_text.setText(message.getPayload().toString()+"good one");
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken token) {
//
//            }
//        });
//        myOptions=new MqttConnectOptions();
//        myOptions.setConnectionTimeout(30);
//        myOptions.setAutomaticReconnect(false);
//        myOptions.setCleanSession(true);
//        myOptions.setKeepAliveInterval(60*2);
//        try{
//
//            mqttClient.connect(myOptions);
//        }catch (Exception e){
//            e.printStackTrace();
//            Log.d("info","fail to conn...");
//        }
//
//
//        Button publisher=(Button)findViewById(R.id.button);
//        Button subscriber=(Button)findViewById(R.id.button2);
//        publisher.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                p_message=new MqttMessage();
//                p_message.setId(100);
//                String payload="hello world from tom";
//                p_message.setPayload(payload.getBytes());
//                p_message.setQos(2);
//                try{
//                    mqttClient.publish(topic_Filter,p_message);
//                }catch (MqttException e){
//                    e.printStackTrace();
//                }
//
//            }
//        });
//
//        subscriber.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try{
//                    mqttClient.subscribe(topic_Filter, 2, new IMqttMessageListener() {
//                        @Override
//                        public void messageArrived(String topic, MqttMessage message) throws Exception {
//                            Log.d("info",topic+"from IMqttMessageListener");
//                            String messageg= new String(message.getPayload());
//                            Log.d("info",messageg);
//                            notif_text.setText(messageg);
//                            View view=(View)findViewById(R.id.textView);
//                            view.invalidate();
//                        }
//                    });
//                }catch(MqttException e){
//                    e.printStackTrace();
//                }
//
//            }
//        });
//
//        Button button=(Button)findViewById(R.id.button3);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try{
//                    mqttClient.unsubscribe(topic_Filter);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//
//            }
//        });
//
//        Button dic_conn=(Button)findViewById(R.id.button5);
//        dic_conn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try{
//                    mqttClient.disconnect();
//                }catch (MqttException e){
//                    e.printStackTrace();
//                }
//
//            }
//        });
//
//        final Button button1=(Button)findViewById(R.id.button6);
//        button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try{
//                    mqttClient.connect(myOptions);
//                }catch(Exception e){
//                    e.printStackTrace();
//                }
//
//            }
//        });
    }

}
