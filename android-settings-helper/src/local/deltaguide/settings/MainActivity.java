package local.deltaguide.settings;
import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.Intent;
import android.content.ComponentName;
import android.net.Uri;
import android.graphics.Color;
import android.widget.*;
import java.util.Locale;
public class MainActivity extends Activity {
 private BrandProfile profile; private String target; private TextView status; private ProgressBar progress; private TextView results; private Button optimize; private final android.os.Handler ui=new android.os.Handler(); private final Runnable refresh=new Runnable(){public void run(){refreshProgress();ui.postDelayed(this,500);}};
 @Override public void onCreate(Bundle b){super.onCreate(b);target=findDelta();profile=BrandProfile.detect(Build.MANUFACTURER,Build.BRAND);LinearLayout root=new LinearLayout(this);root.setOrientation(1);int p=dp(24);root.setPadding(p,dp(48),p,p);root.setBackgroundColor(Color.WHITE);ScrollView scroll=new ScrollView(this);scroll.addView(root);setContentView(scroll);
 TextView title=new TextView(this);title.setText("Delta 通知设置助手 v2.3");title.setTextSize(24);title.setTextColor(Color.rgb(54,78,89));root.addView(title);
 status=new TextView(this);status.setTextSize(15);status.setPadding(0,dp(16),0,dp(20));status.setText(profile.name+" · "+profile.validation());root.addView(status);
 progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(4);root.addView(progress,new LinearLayout.LayoutParams(-1,dp(12)));
 results=new TextView(this);results.setTextSize(15);results.setPadding(0,dp(12),0,dp(12));root.addView(results);
 optimize=new Button(this);optimize.setText("优化通知");optimize.setTextColor(Color.WHITE);optimize.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(54,78,89)));root.addView(optimize,new LinearLayout.LayoutParams(-1,dp(56)));optimize.setOnClickListener(new android.view.View.OnClickListener(){public void onClick(android.view.View v){requestOptimization();}});
 TextView note=new TextView(this);note.setText("识别设备："+profile.name+"\n型号："+Build.MODEL+" · Android "+Build.VERSION.RELEASE+"（API "+Build.VERSION.SDK_INT+"）\n"+profile.validation()+"\n仅操作 Delta Chat 的通知和后台设置，可能增加耗电。遇到未知页面会停止该项并标为待确认。完成后请锁屏测试。");note.setTextSize(13);note.setPadding(0,dp(12),0,dp(18));root.addView(note);
 TextView manual=new TextView(this);manual.setText("单独打开设置");manual.setTextSize(16);root.addView(manual);
 String[][] actions={{"打开 Delta Chat","app"},{"打开通知设置","notifications"},{"打开应用信息","details"},{"打开电池优化","battery"},{"打开自启动 / 后台管理","autostart"}};
 for(String[] item:actions){Button btn=new Button(this);btn.setText(item[0]);btn.setTextColor(Color.WHITE);btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(54,78,89)));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(56));lp.bottomMargin=dp(8);root.addView(btn,lp);btn.setOnClickListener(new android.view.View.OnClickListener(){public void onClick(android.view.View v){open(item[1]);}});}
 route(getIntent());}
 @Override protected void onResume(){super.onResume();ui.post(refresh);}
 @Override protected void onPause(){ui.removeCallbacks(refresh);super.onPause();}
 private void refreshProgress(){android.content.SharedPreferences prefs=getSharedPreferences("optimizer",0);boolean active=OptimizerService.instance!=null&&OptimizerService.instance.isRunning();int count=prefs.getInt("step",0);progress.setProgress(count,true);String[] titles={"系统通知","应用省电","后台自启动","后台连接"};StringBuilder text=new StringBuilder();for(int i=0;i<4;i++)text.append(i+1).append(". ").append(titles[i]).append("\n").append(prefs.getString("result"+i,"等待处理")).append("\n\n");results.setText(text.toString().trim());status.setText(prefs.getString("message",profile.name+" · "+profile.validation()));if(prefs.getBoolean("running",false)&&!active)status.setText("上次运行中断，请重新检查");optimize.setText(active?"停止优化":"优化通知");}
 private void requestOptimization(){if(target==null){status.setText("请先安装 Delta Chat。");return;}if(OptimizerService.instance!=null&&OptimizerService.instance.isRunning()){OptimizerService.instance.stop("你已停止优化");return;}
 if(OptimizerService.instance==null){new android.app.AlertDialog.Builder(this).setTitle("开启通知优化辅助服务").setMessage("需要你手动开启“Delta 通知优化”无障碍服务。它会在你点击优化后读取设置控件并点击通知、后台和省电开关，不保存或上传界面内容。运行时可随时停止，结束后自动停用。开启后返回这里，再点击优化通知。若系统提示受限，请按系统说明在本助手应用信息中允许受限设置。").setNegativeButton("取消",null).setPositiveButton("前往开启",new android.content.DialogInterface.OnClickListener(){public void onClick(android.content.DialogInterface d,int which){launch(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));}}).show();return;}
 new android.app.AlertDialog.Builder(this).setTitle("开始优化 Delta Chat 通知？").setMessage("设备："+profile.name+"\n"+profile.validation()+"\n将按识别到的界面处理通知、省电、自启动和后台连接。请保持手机解锁，运行时暂勿操作其他应用。未确认的项目不会算作成功。").setNegativeButton("取消",null).setPositiveButton("开始优化",new android.content.DialogInterface.OnClickListener(){public void onClick(android.content.DialogInterface d,int which){OptimizerService service=OptimizerService.instance;if(service!=null)service.begin(target);}}).show();}
 private int dp(int x){return (int)(getResources().getDisplayMetrics().density*x);}
 private String findDelta(){for(String p:new String[]{"chat.delta","com.b44t.messenger"})try{getPackageManager().getPackageInfo(p,0);return p;}catch(Exception e){}return null;}
 @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);route(i);}
 private void route(Intent i){Uri u=i.getData();if(u!=null&&"deltaguide".equals(u.getScheme())&&"settings".equals(u.getHost())){String action=u.getQueryParameter("action");if(action!=null&&!action.equals("optimize"))open(action);}}
 private boolean launch(Intent i){if(i==null)return false;try{startActivity(i);return true;}catch(Exception e){return false;}}
 private boolean component(String s){String[] p=s.split("/",2);return launch(new Intent().setComponent(new ComponentName(p[0],p[1])).putExtra("package_name",target).putExtra("packageName",target));}
 private boolean details(){return launch(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS",Uri.parse("package:"+target)));}
 private void open(String action){if(target==null){status.setText("未找到 Delta Chat，请先安装应用。");return;}boolean ok=false;
 switch(action){
 case "app":ok=launch(getPackageManager().getLaunchIntentForPackage(target));break;
 case "notifications":ok=launch(new Intent("android.settings.APP_NOTIFICATION_SETTINGS").putExtra("android.provider.extra.APP_PACKAGE",target));if(!ok)ok=details();break;
 case "details":ok=details();break;
 case "battery":if(profile.id.equals("xiaomi"))ok=launch(new Intent().setComponent(new ComponentName("com.miui.powerkeeper","com.miui.powerkeeper.ui.HiddenAppsConfigActivity")).putExtra("package_name",target).putExtra("package_label","Delta Chat"));else if(profile.id.equals("vivo"))ok=launch(new Intent().setComponent(new ComponentName("com.vivo.abe","com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity")));if(!ok)ok=launch(new Intent("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS"));if(!ok)ok=details();break;
 case "autostart":ok=autoStart();if(!ok){status.setText("此系统没有可用的专用入口，已尝试打开应用信息，请进入电池或后台管理。");details();return;}break;
 default:status.setText("不支持的操作。");return;}
 status.setText(ok?"已打开设置；完成后返回浏览器继续。":"未能打开设置，请从手机系统设置中操作。");}
 private boolean autoStart(){for(String c:profile.startupComponents)if(component(c))return true;return details();}
}
