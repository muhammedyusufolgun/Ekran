package com.olgun.kablosuzekran;

import android.app.*;import android.os.*;import android.content.*;import android.graphics.*;import android.view.*;import android.widget.*;
import android.net.wifi.p2p.WifiP2pInfo;import android.net.wifi.p2p.WifiP2pManager;import android.net.wifi.p2p.WifiP2pDevice;import android.net.wifi.p2p.WifiP2pConfig;import android.content.pm.PackageManager;
import java.net.*;import java.io.*;import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends Activity {
 final Handler ui=new Handler(Looper.getMainLooper()); FrameLayout root; ImageView picture; LinearLayout home; TextView countdown; Button close; volatile boolean connected=false; volatile boolean paused=false; volatile long lastFrame=0; int seconds=0; int closeStep=0; long closeDeadline=0; final AtomicBoolean running=new AtomicBoolean(true); final AtomicBoolean streaming=new AtomicBoolean(false); WifiP2pManager p2pManager; WifiP2pManager.Channel p2pChannel; BroadcastReceiver p2pReceiver; boolean scanning=false; int scanGeneration=0; boolean p2pEnabled=false;
 final Runnable tick=new Runnable(){public void run(){if(!running.get())return; if(connected&&!paused&&System.currentTimeMillis()-lastFrame>3500){connected=false;showHome();} if(paused&&connected){seconds--; if(seconds<=0){paused=false;showPicture();}else countdown.setText(String.valueOf(seconds));}if(closeStep==1&&System.currentTimeMillis()>closeDeadline){closeStep=0;close.setText("Kapat");}ui.postDelayed(this,1000);}};
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);root=new FrameLayout(this);root.setBackgroundColor(Color.WHITE);setContentView(root);picture=new ImageView(this);picture.setScaleType(ImageView.ScaleType.FIT_CENTER);root.addView(picture,new FrameLayout.LayoutParams(-1,-1));home=new LinearLayout(this);home.setOrientation(LinearLayout.VERTICAL);home.setGravity(Gravity.CENTER);root.addView(home,new FrameLayout.LayoutParams(-1,-1));countdown=label("",26);home.addView(countdown);home.addView(label("Kablosuz Monitör",32));TextView name=label("Muhammed Yusuf OLGUN",20);LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-2,-2);np.topMargin=36;name.setTextColor(Color.GRAY);home.addView(name,np);Button diagnostic=new Button(this);diagnostic.setText("Bağlantı Tanılama");home.addView(diagnostic);diagnostic.setOnClickListener(v->showDiagnostics());Button scan=new Button(this);scan.setText("Wi-Fi Direct Bağlan");home.addView(scan);scan.setOnClickListener(v->testWifiDirect());close=new Button(this);close.setText("Kapat");LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-2,-2);cp.topMargin=36;home.addView(close,cp);close.setOnClickListener(v->{if(closeStep==0){closeStep=1;closeDeadline=System.currentTimeMillis()+3000;close.setText("Kapatmayı onayla");}else if(System.currentTimeMillis()<=closeDeadline)finish();else{closeStep=0;close.setText("Kapat");}});picture.setOnTouchListener(new View.OnTouchListener(){long previous=0;public boolean onTouch(View v,android.view.MotionEvent e){if(e.getAction()==1){long now=System.currentTimeMillis();if(now-previous<350){paused=true;seconds=5;showHome();}previous=now;}return true;}});showHome();setupP2pReceiver();ui.post(tick);new Thread(this::receiveLoop,"screen-receiver").start();}
 void setupP2pReceiver(){
  try{
   p2pManager=(WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
   if(p2pManager==null)return;
   p2pChannel=p2pManager.initialize(this,getMainLooper(),()->reportTest("Wi-Fi Direct","Bağlantı kanalı kapandı."));
   p2pReceiver=new BroadcastReceiver(){@Override public void onReceive(Context context,Intent intent){
    if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(intent.getAction())){p2pEnabled=intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1)==WifiP2pManager.WIFI_P2P_STATE_ENABLED;return;} if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(intent.getAction())){if(scanning)showP2pPeers();return;} if(!WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(intent.getAction()))return;
    try{
     android.net.NetworkInfo info=intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
     if(info!=null&&info.isConnected()&&p2pChannel!=null){
      p2pManager.requestConnectionInfo(p2pChannel,(WifiP2pInfo connection)->{
       if(connection.groupFormed)reportTest("Wi-Fi Direct bağlandı","Wi-Fi Direct grup bağlantısı kuruldu. Bu, Windows ekran görüntüsünü aktarmıyor; Miracast alıcı protokolü bu uygulamada bulunmuyor.");
      });
     }
    }catch(Exception e){reportTest("Bağlantı durumu",e.toString());}
   }};
   IntentFilter filter=new IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
   registerReceiver(p2pReceiver,filter);
  }catch(Exception e){reportTest("Wi-Fi Direct başlatma",e.toString());}
 }
 void reportTest(String title,String message){ui.post(()->{if(!isFinishing()&&!isDestroyed())new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("Tamam",null).show();});}
 void testWifiDirect(){
  try{
   if(!getPackageManager().hasSystemFeature("android.hardware.wifi.direct")){reportTest("Wi-Fi Direct","Bu cihaz Wi-Fi Direct desteği bildirmiyor.");return;}
   android.net.wifi.WifiManager wifi=(android.net.wifi.WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
   if(wifi==null||!wifi.isWifiEnabled()){reportTest("Wi-Fi Direct","Önce Wi-Fi bağlantısını açın.");return;}
   String permission=(android.os.Build.VERSION.SDK_INT>=33&&getApplicationInfo().targetSdkVersion>=33)?"android.permission.NEARBY_WIFI_DEVICES":"android.permission.ACCESS_FINE_LOCATION";
   if(checkSelfPermission(permission)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{permission},501);return;}
   android.location.LocationManager lm=(android.location.LocationManager)getSystemService(Context.LOCATION_SERVICE);
   if(lm!=null&&!lm.isLocationEnabled()){new AlertDialog.Builder(this).setTitle("Konum hizmeti gerekli").setMessage("Android Wi-Fi Direct keşfi için konum hizmetini açın. Bu işlem konumunuzu başka cihazlara göndermez.").setPositiveButton("Ayarları aç",(d,w)->startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))).setNegativeButton("İptal",null).show();return;}
   WifiP2pManager manager=(WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
   if(manager==null){reportTest("Wi-Fi Direct","Wi-Fi Direct sistem servisi bulunamadı.");return;}
   WifiP2pManager.Channel channel=p2pChannel!=null?p2pChannel:manager.initialize(this,getMainLooper(),()->reportTest("Wi-Fi Direct","Sistem Wi-Fi Direct kanalını kapattı."));
   if(channel==null){reportTest("Wi-Fi Direct","Wi-Fi Direct kanalı başlatılamadı.");return;}
   scanning=true;final int generation=++scanGeneration;p2pManager=manager;p2pChannel=channel;manager.discoverPeers(channel,new WifiP2pManager.ActionListener(){
    public void onSuccess(){ui.postDelayed(()->{if(scanning&&scanGeneration==generation)showP2pPeers();},5000);ui.postDelayed(()->{if(scanning&&scanGeneration==generation){scanning=false;reportTest("Cihaz bulunamadı","20 saniye içinde Wi-Fi Direct cihazı bulunamadı. Windows + K, yalnızca Miracast alıcılarını listeler; bu APK henüz Miracast alıcısı değildir.");}},20000);}
    public void onFailure(int reason){scanning=false;reportTest("Wi-Fi Direct","Cihaz araması başarısız. Kod: "+reason+" (0=ERROR, 1=P2P_UNSUPPORTED, 2=BUSY)");}
   });
  }catch(SecurityException e){reportTest("İzin hatası",e.toString());}
   catch(RuntimeException e){reportTest("Wi-Fi Direct hatası",e.toString());}
 }
 @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
  super.onRequestPermissionsResult(requestCode,permissions,grantResults);
  if(requestCode==501){if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)testWifiDirect();else reportTest("İzin gerekli","Wi-Fi Direct testi için istenen izni verin.");}
 }

 void showP2pPeers(){
  if(!scanning||p2pManager==null||p2pChannel==null||isFinishing())return;
  try{p2pManager.requestPeers(p2pChannel,peers->{
   if(!scanning||isFinishing()||isDestroyed())return;
   java.util.ArrayList<WifiP2pDevice> devices=new java.util.ArrayList<>(peers.getDeviceList());
   if(devices.isEmpty())return;
   scanning=false;
   String[] names=new String[devices.size()];
   for(int i=0;i<devices.size();i++)names[i]=devices.get(i).deviceName+" ("+devices.get(i).deviceAddress+")";
   new AlertDialog.Builder(this).setTitle("Wi-Fi Direct cihazları").setItems(names,(dialog,index)->{
    WifiP2pConfig config=new WifiP2pConfig();config.deviceAddress=devices.get(index).deviceAddress;
    try{p2pManager.connect(p2pChannel,config,new WifiP2pManager.ActionListener(){
     public void onSuccess(){reportTest("Bağlantı isteği","İstek gönderildi; gerçek bağlantı sonucu bekleniyor.");}
     public void onFailure(int reason){reportTest("Bağlantı hatası","Kod: "+reason);}
    });}catch(Exception e){reportTest("Bağlantı hatası",e.toString());}
   }).setNegativeButton("İptal",null).show();
  });}catch(Exception e){scanning=false;reportTest("Cihaz arama hatası",e.toString());}
 }
 void showDiagnostics(){boolean p2p=getPackageManager().hasSystemFeature("android.hardware.wifi.direct"); android.net.wifi.WifiManager wifi=(android.net.wifi.WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE); boolean wifiOn=wifi!=null&&wifi.isWifiEnabled();boolean wfdPermission=checkSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY")==PackageManager.PERMISSION_GRANTED;boolean controlPermission=checkSelfPermission("android.permission.CONTROL_WIFI_DISPLAY")==PackageManager.PERMISSION_GRANTED;String message="Wi-Fi: "+(wifiOn?"Açık":"Kapalı")+"\nWi-Fi Direct donanımı: "+(p2p?"Mevcut":"Algılanmadı")+"\nWi-Fi Direct sistem durumu: "+(p2pEnabled?"Etkin":"Etkin değil / henüz bildirilmedi")+"\nAndroid: "+android.os.Build.VERSION.RELEASE+"\nMiracast sistem izni (CONFIGURE_WIFI_DISPLAY): "+(wfdPermission?"Verilmiş":"Verilmemiş / sistem kısıtlaması")+"\nMiracast düşük seviye kontrol izni: "+(controlPermission?"Verilmiş":"Verilmemiş / sistem kısıtlaması")+"\n\nBu uygulama henüz Miracast alıcısı değildir. Windows + K listesinde görünmesi için Wi-Fi Display alıcı ilanı, RTSP ve RTP/H.264 alıcı bileşenleri gerekir. Standart APK, sistem iznini kendi kendine alamaz.";new AlertDialog.Builder(this).setTitle("Monitor - Bağlantı Tanılama").setMessage(message).setPositiveButton("Tamam",null).show();}
 TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.BLACK);t.setTextSize(size);t.setGravity(Gravity.CENTER);return t;}
 void showHome(){ui.post(()->{home.setVisibility(View.VISIBLE);picture.setVisibility(View.GONE);countdown.setText(paused&&connected?String.valueOf(seconds):"");});}
 void showPicture(){ui.post(()->{home.setVisibility(View.GONE);picture.setVisibility(View.VISIBLE);});}
 void receiveLoop(){byte[] buf=new byte[65507];while(running.get()){try(DatagramSocket socket=new DatagramSocket(48620)){socket.setSoTimeout(1500);while(running.get()){try{DatagramPacket p=new DatagramPacket(buf,buf.length);socket.receive(p);String msg=new String(p.getData(),0,p.getLength(),"UTF-8");if(msg.startsWith("KABLOSUZ_EKRAN_V1:")){String host=p.getAddress().getHostAddress();int port=Integer.parseInt(msg.substring(18).trim());if(port>0&&port<=65535&&streaming.compareAndSet(false,true)){new Thread(()->{try{stream(host,port);}finally{streaming.set(false);}},"monitor-stream").start();}}}catch(SocketTimeoutException ignored){}catch(Exception ignored){}}}catch(Exception ignored){try{Thread.sleep(1000);}catch(InterruptedException e){return;}}}}
 void stream(String host,int port){try(Socket socket=new Socket()){socket.connect(new InetSocketAddress(host,port),2000);socket.setSoTimeout(5000);DataInputStream in=new DataInputStream(new BufferedInputStream(socket.getInputStream()));while(running.get()){int len=in.readInt();if(len<1||len>8_000_000)break;byte[] data=new byte[len];in.readFully(data);BitmapFactory.Options options=new BitmapFactory.Options();options.inPreferredConfig=Bitmap.Config.RGB_565;Bitmap bmp=BitmapFactory.decodeByteArray(data,0,len,options);if(bmp!=null){lastFrame=System.currentTimeMillis();connected=true;ui.post(()->{picture.setImageBitmap(bmp);if(!paused)showPicture();});}}}catch(Exception ignored){}finally{connected=false;paused=false;if(running.get())showHome();}}
 @Override protected void onDestroy(){running.set(false);if(p2pReceiver!=null){try{unregisterReceiver(p2pReceiver);}catch(Exception ignored){}}ui.removeCallbacksAndMessages(null);super.onDestroy();}
}
