package com.olgun.kablosuzekran;

import android.app.*;import android.os.*;import android.content.*;import android.graphics.*;import android.view.*;import android.widget.*;
import android.net.wifi.p2p.WifiP2pManager;import android.content.pm.PackageManager;
import java.net.*;import java.io.*;import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends Activity {
 final Handler ui=new Handler(Looper.getMainLooper()); FrameLayout root; ImageView picture; LinearLayout home; TextView countdown; Button close; volatile boolean connected=false; volatile boolean paused=false; volatile long lastFrame=0; int seconds=0; int closeStep=0; long closeDeadline=0; final AtomicBoolean running=new AtomicBoolean(true);
 final Runnable tick=new Runnable(){public void run(){if(!running.get())return; if(connected&&!paused&&System.currentTimeMillis()-lastFrame>3500){connected=false;showHome();} if(paused&&connected){seconds--; if(seconds<=0){paused=false;showPicture();}else countdown.setText(String.valueOf(seconds));}if(closeStep==1&&System.currentTimeMillis()>closeDeadline){closeStep=0;close.setText("Kapat");}ui.postDelayed(this,1000);}};
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);root=new FrameLayout(this);root.setBackgroundColor(Color.WHITE);setContentView(root);picture=new ImageView(this);picture.setScaleType(ImageView.ScaleType.FIT_CENTER);root.addView(picture,new FrameLayout.LayoutParams(-1,-1));home=new LinearLayout(this);home.setOrientation(LinearLayout.VERTICAL);home.setGravity(Gravity.CENTER);root.addView(home,new FrameLayout.LayoutParams(-1,-1));countdown=label("",26);home.addView(countdown);home.addView(label("Kablosuz Monitör",32));TextView name=label("Muhammed Yusuf OLGUN",20);LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-2,-2);np.topMargin=36;name.setTextColor(Color.GRAY);home.addView(name,np);Button diagnostic=new Button(this);diagnostic.setText("Bağlantı Tanılama");home.addView(diagnostic);diagnostic.setOnClickListener(v->showDiagnostics());Button scan=new Button(this);scan.setText("Wi-Fi Direct Testi");home.addView(scan);scan.setOnClickListener(v->testWifiDirect());close=new Button(this);close.setText("Kapat");LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-2,-2);cp.topMargin=36;home.addView(close,cp);close.setOnClickListener(v->{if(closeStep==0){closeStep=1;closeDeadline=System.currentTimeMillis()+3000;close.setText("Kapatmayı onayla");}else if(System.currentTimeMillis()<=closeDeadline)finish();else{closeStep=0;close.setText("Kapat");}});picture.setOnTouchListener(new View.OnTouchListener(){long previous=0;public boolean onTouch(View v,android.view.MotionEvent e){if(e.getAction()==1){long now=System.currentTimeMillis();if(now-previous<350){paused=true;seconds=5;showHome();}previous=now;}return true;}});showHome();ui.post(tick);new Thread(this::receiveLoop,"screen-receiver").start();}
 void reportTest(String title,String message){ui.post(()->{if(!isFinishing()&&!isDestroyed())new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("Tamam",null).show();});}
 void testWifiDirect(){
  try{
   if(!getPackageManager().hasSystemFeature("android.hardware.wifi.direct")){reportTest("Wi-Fi Direct","Bu cihaz Wi-Fi Direct desteği bildirmiyor.");return;}
   android.net.wifi.WifiManager wifi=(android.net.wifi.WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
   if(wifi==null||!wifi.isWifiEnabled()){reportTest("Wi-Fi Direct","Önce Wi-Fi bağlantısını açın.");return;}
   String permission=(android.os.Build.VERSION.SDK_INT>=33&&getApplicationInfo().targetSdkVersion>=33)?"android.permission.NEARBY_WIFI_DEVICES":"android.permission.ACCESS_FINE_LOCATION";
   if(checkSelfPermission(permission)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{permission},501);return;}
   android.location.LocationManager lm=(android.location.LocationManager)getSystemService(Context.LOCATION_SERVICE);
   if(lm!=null&&!lm.isLocationEnabled()){reportTest("Wi-Fi Direct","Android konum hizmeti kapalı. Wi-Fi Direct cihaz araması için konum hizmetini açın.");return;}
   WifiP2pManager manager=(WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
   if(manager==null){reportTest("Wi-Fi Direct","Wi-Fi Direct sistem servisi bulunamadı.");return;}
   WifiP2pManager.Channel channel=manager.initialize(this,getMainLooper(),()->reportTest("Wi-Fi Direct","Sistem Wi-Fi Direct kanalını kapattı."));
   if(channel==null){reportTest("Wi-Fi Direct","Wi-Fi Direct kanalı başlatılamadı.");return;}
   manager.discoverPeers(channel,new WifiP2pManager.ActionListener(){
    public void onSuccess(){reportTest("Wi-Fi Direct","Arama başlatıldı. Bu işlem Windows + K / Miracast alıcısı desteğini sağlamaz.");}
    public void onFailure(int reason){reportTest("Wi-Fi Direct","Cihaz araması başarısız. Kod: "+reason+" (0=ERROR, 1=P2P_UNSUPPORTED, 2=BUSY)");}
   });
  }catch(SecurityException e){reportTest("İzin hatası",e.toString());}
   catch(RuntimeException e){reportTest("Wi-Fi Direct hatası",e.toString());}
 }
 @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
  super.onRequestPermissionsResult(requestCode,permissions,grantResults);
  if(requestCode==501){if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)testWifiDirect();else reportTest("İzin gerekli","Wi-Fi Direct testi için istenen izni verin.");}
 }

 void showDiagnostics(){boolean p2p=getPackageManager().hasSystemFeature("android.hardware.wifi.direct"); android.net.wifi.WifiManager wifi=(android.net.wifi.WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE); boolean wifiOn=wifi!=null&&wifi.isWifiEnabled();String message="Wi-Fi: "+(wifiOn?"Açık":"Kapalı")+"\nWi-Fi Direct donanımı: "+(p2p?"Mevcut":"Algılanmadı")+"\nAndroid: "+android.os.Build.VERSION.RELEASE+"\n\nBu sürüm Miracast alıcısı değildir. Windows + K üzerinden görünmez. Wi-Fi Direct donanımı bulunması tek başına Miracast alıcısı desteği sağlamaz.";new AlertDialog.Builder(this).setTitle("Monitor - Bağlantı Tanılama").setMessage(message).setPositiveButton("Tamam",null).show();}
 TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.BLACK);t.setTextSize(size);t.setGravity(Gravity.CENTER);return t;}
 void showHome(){ui.post(()->{home.setVisibility(View.VISIBLE);picture.setVisibility(View.GONE);countdown.setText(paused&&connected?String.valueOf(seconds):"");});}
 void showPicture(){ui.post(()->{home.setVisibility(View.GONE);picture.setVisibility(View.VISIBLE);});}
 void receiveLoop(){byte[] buf=new byte[65507];while(running.get()){try(DatagramSocket socket=new DatagramSocket(48620)){socket.setSoTimeout(1500);while(running.get()){try{DatagramPacket p=new DatagramPacket(buf,buf.length);socket.receive(p);String msg=new String(p.getData(),0,p.getLength(),"UTF-8");if(msg.startsWith("KABLOSUZ_EKRAN_V1:")){String host=p.getAddress().getHostAddress();int port=Integer.parseInt(msg.substring(18).trim());stream(host,port);}}catch(SocketTimeoutException ignored){}catch(Exception ignored){}}}catch(Exception ignored){try{Thread.sleep(1000);}catch(InterruptedException e){return;}}}}
 void stream(String host,int port){try(Socket socket=new Socket()){socket.connect(new InetSocketAddress(host,port),2000);socket.setSoTimeout(5000);DataInputStream in=new DataInputStream(new BufferedInputStream(socket.getInputStream()));while(running.get()){int len=in.readInt();if(len<1||len>8_000_000)break;byte[] data=new byte[len];in.readFully(data);Bitmap bmp=BitmapFactory.decodeByteArray(data,0,len);if(bmp!=null){lastFrame=System.currentTimeMillis();connected=true;ui.post(()->{picture.setImageBitmap(bmp);if(!paused)showPicture();});}}}catch(Exception ignored){}finally{connected=false;paused=false;showHome();}}
 @Override protected void onDestroy(){running.set(false);ui.removeCallbacksAndMessages(null);super.onDestroy();}
}
