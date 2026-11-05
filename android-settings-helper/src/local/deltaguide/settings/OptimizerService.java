package local.deltaguide.settings;
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.*;
import android.view.*;
import android.widget.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.PixelFormat;
import java.util.*;

/** Fixed, local workflow. No downloaded scripts, shell, logging of UI contents or networking. */
public class OptimizerService extends AccessibilityService {
 public static OptimizerService instance;
 private final Handler handler=new Handler();
 private boolean running=false;
 private int step=0,ticks=0,scrolls=0;
 private String target="",activity="";
 private BrandProfile profile;
 private boolean manualOptionsObserved=false,manualSaveClicked=false;
 private String[] results=new String[4];
 private long settleUntil=0;
 private String lastScreen="";
 private int unchanged=0;
 private final Map<String,Integer> attempts=new HashMap<>();
 private LinearLayout overlay;
 private TextView caption;
 private ProgressBar bar;
 private final String[] titles={"允许系统通知","放开应用省电","允许后台自启动","开启后台连接"};
 private final Runnable loop=new Runnable(){
  public void run(){
   if(!running)return;
   try{tick();}catch(Exception e){complete(false,"界面无法识别，待手动确认");}
   if(running)handler.postDelayed(this,900);
  }
 };

 @Override protected void onServiceConnected(){instance=this;}
 @Override public void onAccessibilityEvent(AccessibilityEvent e){
  if(e.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED&&e.getClassName()!=null){
   String c=e.getClassName().toString();
   if(c.endsWith("Activity"))activity=c;
  }
 }
 @Override public void onInterrupt(){stop("操作被中断");}
 @Override public void onDestroy(){
  if(running)save("运行已中断，请重新检查");
  removeOverlay();instance=null;super.onDestroy();
 }
 public boolean isRunning(){return running;}

 public void begin(String pkg){
  if(running)return;
  target=pkg;
  profile=BrandProfile.detect(Build.MANUFACTURER,Build.BRAND);
  android.accessibilityservice.AccessibilityServiceInfo info=getServiceInfo();
  List<String> observed=new ArrayList<>();
  observed.add("com.android.settings");
  observed.add("android");
  observed.add("com.android.systemui");
  observed.add(target);
  for(String p:profile.packages)observed.add(p);
  info.packageNames=observed.toArray(new String[0]);
  setServiceInfo(info);
  step=0;
  results=new String[4];
  running=true;
  showOverlay();
  save("正在优化通知");
  startStep();
  handler.postDelayed(loop,900);
 }

 private boolean launch(Intent i){
  try{i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);return true;}
  catch(Exception e){return false;}
 }

 private void startStep(){
  ticks=0;scrolls=0;activity="";manualOptionsObserved=false;manualSaveClicked=false;
  settleUntil=SystemClock.uptimeMillis()+1600;lastScreen="";unchanged=0;attempts.clear();
  save("正在处理："+titles[step]);updateOverlay();boolean opened=false;
  if(step==0){
   Intent i=new Intent("android.settings.APP_NOTIFICATION_SETTINGS");
   i.putExtra("android.provider.extra.APP_PACKAGE",target);
   i.putExtra("app_package",target);
   opened=launch(i);
   if(!opened)opened=appDetails();
  }
  if(step==1){
   if(profile.id.equals("xiaomi")){
    opened=launch(new Intent().setComponent(new ComponentName("com.miui.powerkeeper","com.miui.powerkeeper.ui.HiddenAppsConfigActivity")).putExtra("package_name",target).putExtra("package_label","Delta Chat"));
   } else if(profile.id.equals("vivo")){
    opened=launch(new Intent().setComponent(new ComponentName("com.vivo.abe","com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity")));
   }
   if(!opened)opened=appDetails();
  }
  if(step==2){
   for(String component:profile.startupComponents){
    if(launch(new Intent().setComponent(ComponentName.unflattenFromString(component)).putExtra("package_name",target).putExtra("packageName",target))){
     opened=true;break;
    }
   }
   if(!opened)opened=appDetails();
  }
  if(step==3){
   Intent i=getPackageManager().getLaunchIntentForPackage(target);
   opened=i!=null&&launch(i);
  }
  if(!opened)complete(false,"设置入口不可用，待手动确认");
 }

 private void tick(){
  if(SystemClock.uptimeMillis()<settleUntil)return;
  ticks++;updateOverlay();
  if(!((PowerManager)getSystemService(POWER_SERVICE)).isInteractive()){
   stop("已锁屏，优化已停止");return;
  }
  if(ticks>26){
   complete(false,"未能验证，请手动确认");return;
  }
  AccessibilityNodeInfo root=getRootInActiveWindow();
  if(root==null)return;
  String pkg=String.valueOf(root.getPackageName());
  if(!profile.allowsPackage(pkg)&&!pkg.equals(target))return;

  if(step==0){
   notificationStep(root);
   return;
  }
  if(step==1){
   batteryStep(root);
   return;
  }
  if(step==2){
   startupStep(root);
   return;
  }
  if(step==3&&pkg.equals(target)){
   delta(root);
  }
 }

 private boolean appDetails(){
  return launch(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS",android.net.Uri.parse("package:"+target)));
 }

 private boolean navigate(AccessibilityNodeInfo root,String[] labels){
  AccessibilityNodeInfo n=exact(root,labels);
  return n!=null&&click(n);
 }

 /** STEP 0: Master notification toggle */
 private void notificationStep(AccessibilityNodeInfo root){
  String[] allowLabels={"允许通知","显示通知","开启通知","所有 Delta Chat 通知","All Delta Chat notifications","Allow notifications","Show notifications","应用通知"};
  AccessibilityNodeInfo n=toggle(root,allowLabels);
  if(n!=null){
   if(n.isChecked()){
    complete(true,"系统通知已开启");
   }else{
    click(n);
   }
   return;
  }
  searchScroll(root,3);
 }

 /** STEP 1: Battery / power optimization */
 private void batteryStep(AccessibilityNodeInfo root){
  switch(profile.id){
   case "xiaomi":xiaomiBattery(root);break;
   case "oppo":oppoBattery(root);break;
   case "vivo":vivoBattery(root);break;
   case "huawei":case "honor":huaweiBattery(root);break;
   default:genericBattery(root);break;
  }
 }

 private void xiaomiBattery(AccessibilityNodeInfo root){
  AccessibilityNodeInfo choice=exact(root,new String[]{"无限制","不限制","不受限制","Unrestricted","No restrictions"});
  if(choice!=null){
   AccessibilityNodeInfo check=nearCheck(choice);
   if((check!=null&&check.isChecked())||choice.isSelected()){
    complete(true,"小米：后台电池策略已设为无限制");
   } else {
    click(check!=null?check:choice);
   }
   return;
  }
  if(navigate(root,new String[]{"省电策略","电池和性能","电量和性能","应用电池用量","Battery usage"}))return;
  searchScroll(root,5);
 }

 private void oppoBattery(AccessibilityNodeInfo root){
  AccessibilityNodeInfo check=toggle(root,new String[]{"允许后台活动","允许完全后台行为","允许后台运行","Allow background activity","Allow background usage"});
  if(check!=null){
   if(check.isChecked()){
    AccessibilityNodeInfo wake=toggle(root,new String[]{"允许唤醒前台","允许前台唤醒"});
    if(wake!=null&&!wake.isChecked())click(wake);
    complete(true,"OPPO/一加：允许后台活动已开启");
   } else {
    click(check);
   }
   return;
  }
  AccessibilityNodeInfo choice=exact(root,new String[]{"允许完全后台行为","无限制","不限制","不受限制","Don't optimize"});
  if(choice!=null){
   AccessibilityNodeInfo r=nearCheck(choice);
   if((r!=null&&r.isChecked())||choice.isSelected()){
    complete(true,"OPPO/一加：后台耗电策略已放开");
   } else {
    click(r!=null?r:choice);
   }
   return;
  }
  if(navigate(root,new String[]{"耗电管理","应用耗电管理","电池用量","电池","耗电保护","Battery usage","App battery usage","Battery"}))return;
  searchScroll(root,6);
 }

 private void vivoBattery(AccessibilityNodeInfo root){
  if(hasDelta(root)){
   AccessibilityNodeInfo name=exact(root,new String[]{"Delta Chat"});
   AccessibilityNodeInfo sw=nearCheck(name);
   if(sw!=null){
    if(sw.isChecked()){complete(true,"vivo/iQOO：后台高耗电已开启");return;}
    else{click(sw);return;}
   }
  }
  AccessibilityNodeInfo toggle=toggle(root,new String[]{"允许后台高耗电","后台高耗电","允许高耗电","允许后台运行","Allow background high power"});
  if(toggle!=null){
   if(toggle.isChecked()){complete(true,"vivo/iQOO：允许后台高耗电已开启");return;}
   else{click(toggle);return;}
  }
  AccessibilityNodeInfo choice=exact(root,new String[]{"允许后台高耗电","允许高耗电","后台高耗电","不限制","No restrictions"});
  if(choice!=null){
   AccessibilityNodeInfo r=nearCheck(choice);
   if((r!=null&&r.isChecked())||choice.isSelected()){
    complete(true,"vivo/iQOO：已设置为允许后台高耗电");return;}
   else{click(r!=null?r:choice);return;}
  }
  if(navigate(root,new String[]{"后台耗电管理","后台高耗电","电池","耗电管理","后台耗电","Battery"}))return;
  searchScroll(root,6);
 }

 private void huaweiBattery(AccessibilityNodeInfo root){
  AccessibilityNodeInfo choice=exact(root,new String[]{"不允许","不限制","无限制","不受限制","Don't optimize"});
  if(choice!=null){
   AccessibilityNodeInfo r=nearCheck(choice);
   if((r!=null&&r.isChecked())||choice.isSelected()){
    AccessibilityNodeInfo confirm=exact(root,new String[]{"确定","完成","OK","Done"});
    if(confirm!=null){click(confirm);return;}
    complete(true,"华为/荣耀：已解除后台电池限制");return;
   }else{
    click(r!=null?r:choice);return;
   }
  }
  AccessibilityNodeInfo check=toggle(root,new String[]{"允许后台活动","后台活动","允许后台运行","Run in background"});
  if(check!=null){
   if(check.isChecked()){complete(true,"华为/荣耀：已允许后台活动");return;}
   else{click(check);return;}
  }
  if(navigate(root,new String[]{"耗电详情","耗电排行","电池","应用启动管理","Battery"}))return;
  searchScroll(root,6);
 }

 private void genericBattery(AccessibilityNodeInfo root){
  AccessibilityNodeInfo choice=exact(root,new String[]{"无限制","不限制","不受限制","Unrestricted","No restrictions","Don't optimize"});
  if(choice!=null){
   AccessibilityNodeInfo r=nearCheck(choice);
   if((r!=null&&r.isChecked())||choice.isSelected()){
    complete(true,"后台电池策略已设为无限制");return;
   }else{
    click(r!=null?r:choice);return;
   }
  }
  AccessibilityNodeInfo check=toggle(root,new String[]{"允许后台活动","允许后台运行","Allow background activity","Allow background usage"});
  if(check!=null){
   if(check.isChecked()){complete(true,"已允许应用后台活动");return;}
   else{click(check);return;}
  }
  if(navigate(root,new String[]{"耗电管理","应用耗电管理","应用电池用量","电池用量","电池","Battery","Battery usage"}))return;
  searchScroll(root,5);
 }

 /** STEP 2: Startup / autostart management */
 private void startupStep(AccessibilityNodeInfo root){
  switch(profile.id){
   case "xiaomi":xiaomiStartup(root);break;
   case "oppo":oppoStartup(root);break;
   case "vivo":vivoStartup(root);break;
   case "huawei":case "honor":huaweiStartup(root);break;
   default:genericStartup(root);break;
  }
 }

 private void xiaomiStartup(AccessibilityNodeInfo root){
  if(hasDelta(root)){
   AccessibilityNodeInfo name=exact(root,new String[]{"Delta Chat"});
   AccessibilityNodeInfo toggle=nearCheck(name);
   if(toggle==null){
    boolean detail=exact(root,new String[]{"应用信息","应用详情","App info","自启动","Autostart"})!=null;
    if(detail)toggle=toggle(root,new String[]{"允许自启动","自启动","后台自启动","Autostart","Auto-launch"});
   }
   if(toggle!=null){
    if(toggle.isChecked()){complete(true,"小米：自启动已开启");return;}
    else{click(toggle);return;}
   }
   if(navigate(root,new String[]{"自启动管理","后台自启动","自启动","应用启动管理","Autostart"}))return;
  }
  searchScroll(root,10);
 }

 private void oppoStartup(AccessibilityNodeInfo root){
  if(hasDelta(root)){
   AccessibilityNodeInfo name=exact(root,new String[]{"Delta Chat"});
   AccessibilityNodeInfo sw=nearCheck(name);
   if(sw!=null){
    if(sw.isChecked()){complete(true,"OPPO/一加：自启动已开启");return;}
    else{click(sw);return;}
   }
  }
  AccessibilityNodeInfo toggle=toggle(root,new String[]{"允许自启动","自启动","应用自启动","允许应用自启动","Allow auto-launch","Auto-launch"});
  if(toggle!=null){
   if(toggle.isChecked()){complete(true,"OPPO/一加：自启动已开启");return;}
   else{click(toggle);return;}
  }
  if(navigate(root,new String[]{"自启动管理","自启动","应用启动管理","权限管理","Auto-launch","Autostart"}))return;
  searchScroll(root,10);
 }

 private void vivoStartup(AccessibilityNodeInfo root){
  if(hasDelta(root)){
   AccessibilityNodeInfo name=exact(root,new String[]{"Delta Chat"});
   AccessibilityNodeInfo sw=nearCheck(name);
   if(sw!=null){
    if(sw.isChecked()){complete(true,"vivo/iQOO：自启动已开启");return;}
    else{click(sw);return;}
   }
  }
  AccessibilityNodeInfo toggle=toggle(root,new String[]{"允许自启动","自启动","后台启动","允许后台自启动","Auto-launch"});
  if(toggle!=null){
   if(toggle.isChecked()){complete(true,"vivo/iQOO：自启动已开启");return;}
   else{click(toggle);return;}
  }
  if(navigate(root,new String[]{"自启动管理","自启动","权限管理","单项权限设置","后台启动","Auto-launch"}))return;
  searchScroll(root,10);
 }

 private void huaweiStartup(AccessibilityNodeInfo root){
  String[][] labels={
   {"允许自启动","Allow auto-launch"},
   {"允许关联启动","Allow secondary launch"},
   {"允许后台活动","Run in background","Allow background activity"}
  };
  AccessibilityNodeInfo[] switches=new AccessibilityNodeInfo[3];
  int found=0;
  for(int i=0;i<3;i++){
   switches[i]=toggle(root,labels[i]);
   if(switches[i]!=null)found++;
  }
  if(found>=2){
   for(AccessibilityNodeInfo n:switches){
    if(n!=null&&!n.isChecked()){click(n);return;}
   }
   manualOptionsObserved=true;
   AccessibilityNodeInfo confirm=exact(root,new String[]{"确定","完成","OK","Done"});
   if(confirm!=null){
    if(click(confirm))manualSaveClicked=true;
    return;
   }
   complete(false,"华为/荣耀：三个子开关已开启，但确定按钮未响应");
   return;
  }

  if(hasDelta(root)){
   AccessibilityNodeInfo name=exact(root,new String[]{"Delta Chat"});
   AccessibilityNodeInfo row=nearCheck(name);
   boolean launchPage=exact(root,new String[]{"应用启动管理","启动管理","App launch","Launch management","自动管理","Manage automatically"})!=null;
   if(launchPage&&row!=null){
    if(row.isChecked()){
     click(row);
     return;
    }
    if(manualSaveClicked){
     complete(true,"华为/荣耀：手动管理已保存，自启动/关联启动/后台活动全允许");
     return;
    }
    click(name);
    return;
   }
   if(navigate(root,new String[]{"应用启动管理","启动管理","App launch"}))return;
  }
  searchScroll(root,8);
 }

 private void genericStartup(AccessibilityNodeInfo root){
  if(hasDelta(root)){
   AccessibilityNodeInfo toggle=toggle(root,new String[]{"允许自启动","自启动","后台自启动","Auto-launch","Autostart"});
   if(toggle!=null){
    if(toggle.isChecked()){complete(true,"自启动已开启");return;}
    else{click(toggle);return;}
   }
   if(navigate(root,new String[]{"自启动管理","后台自启动","自启动","应用启动管理","Auto-launch","Autostart"}))return;
  }
  complete(false,"未识别此品牌的专用自启动页面，请手动检查");
 }

 /** STEP 3: Delta Chat in-app background connection */
 private void delta(AccessibilityNodeInfo root){
  if(activity.endsWith("ConversationListActivity")||activity.endsWith("RoutingActivity")){
   for(AccessibilityNodeInfo n:root.findAccessibilityNodeInfosByViewId(target+":id/menu_settings"))if(n.isVisibleToUser()){click(n);return;}
   AccessibilityNodeInfo toolbar=null;
   for(AccessibilityNodeInfo n:root.findAccessibilityNodeInfosByViewId(target+":id/toolbar"))if(n.isVisibleToUser())toolbar=n;
   if(toolbar!=null){
    AccessibilityNodeInfo more=description(toolbar,new String[]{"更多选项","更多","More options"});
    if(more!=null){click(more);return;}
   }
   for(String id:new String[]{"android:id/title",target+":id/title"}){
    for(AccessibilityNodeInfo n:root.findAccessibilityNodeInfosByViewId(id)){
     if(n.isVisibleToUser()&&matches(n.getText(),new String[]{"设置","Settings"})){click(n);return;}
    }
   }
   return;
  }
  if(!activity.endsWith("ApplicationPreferencesActivity"))return;
  AccessibilityNodeInfo force=toggle(root,new String[]{"强制后台连接","Force Background Connection","强制后台连接服务"});
  if(force!=null){
   if(!force.isChecked()){click(force);return;}
   boolean ignored=((PowerManager)getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(target);
   AccessibilityNodeInfo bg=toggle(root,new String[]{"使用后台连接","Use Background Connection"});
   if(ignored||(bg!=null&&bg.isChecked())){
    complete(true,"后台连接已开启");return;
   }
   if(bg!=null){
    click(bg);save("请确认系统弹出的电池优化请求");
   }else complete(false,"强制后台连接已开启；电池豁免待确认");
   return;
  }
  AccessibilityNodeInfo notification=exact(root,new String[]{"通知","Notifications"});
  if(notification!=null&&ticks<8){click(notification);return;}
  searchScroll(root,10);
 }

 private boolean hasDelta(AccessibilityNodeInfo n){
  if(n==null)return false;
  return containsSubstr(n,"Delta Chat")||containsSubstr(n,"chat.delta")||(target!=null&&target.length()>0&&containsSubstr(n,target));
 }

 private boolean containsSubstr(AccessibilityNodeInfo n,String query){
  if(n==null)return false;
  if(n.isVisibleToUser()){
   CharSequence t=n.getText();
   if(t!=null&&t.toString().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))return true;
   CharSequence d=n.getContentDescription();
   if(d!=null&&d.toString().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))return true;
  }
  for(int i=0;i<n.getChildCount();i++){
   if(containsSubstr(n.getChild(i),query))return true;
  }
  return false;
 }

 private boolean matches(CharSequence text,String[] labels){
  if(text==null)return false;
  String s=text.toString().trim();
  for(String l:labels)if(l.equalsIgnoreCase(s))return true;
  return false;
 }

 private AccessibilityNodeInfo exact(AccessibilityNodeInfo n,String[] labels){
  if(n==null)return null;
  if(n.isVisibleToUser()&&(matches(n.getText(),labels)||matches(n.getContentDescription(),labels)))return n;
  for(int i=0;i<n.getChildCount();i++){
   AccessibilityNodeInfo found=exact(n.getChild(i),labels);
   if(found!=null)return found;
  }
  return null;
 }

 private AccessibilityNodeInfo description(AccessibilityNodeInfo n,String[] labels){
  if(n==null)return null;
  if(n.isVisibleToUser()&&matches(n.getContentDescription(),labels))return n;
  for(int i=0;i<n.getChildCount();i++){
   AccessibilityNodeInfo found=description(n.getChild(i),labels);
   if(found!=null)return found;
  }
  return null;
 }

 private void checks(AccessibilityNodeInfo n,List<AccessibilityNodeInfo> out){
  if(n==null)return;
  if(n.isVisibleToUser()&&n.isCheckable())out.add(n);
  for(int i=0;i<n.getChildCount();i++)checks(n.getChild(i),out);
 }

 private AccessibilityNodeInfo nearCheck(AccessibilityNodeInfo label){
  if(label==null)return null;
  if(label.isCheckable())return label;
  android.graphics.Rect labelBounds=new android.graphics.Rect();
  label.getBoundsInScreen(labelBounds);
  AccessibilityNodeInfo row=label;
  for(int depth=0;depth<5&&row!=null;depth++,row=row.getParent()){
   List<AccessibilityNodeInfo> candidates=new ArrayList<>();
   checks(row,candidates);
   AccessibilityNodeInfo match=null;int count=0;
   for(AccessibilityNodeInfo c:candidates){
    android.graphics.Rect bounds=new android.graphics.Rect();
    c.getBoundsInScreen(bounds);
    if(bounds.bottom>labelBounds.top&&bounds.top<labelBounds.bottom){match=c;count++;}
   }
   if(count==1)return match;
   if(count>1)return null;
  }
  return null;
 }

 private AccessibilityNodeInfo toggle(AccessibilityNodeInfo root,String[] labels){
  AccessibilityNodeInfo label=exact(root,labels);
  return label==null?null:nearCheck(label);
 }

 private boolean click(AccessibilityNodeInfo n){
  for(int d=0;d<4&&n!=null;d++,n=n.getParent()){
   if(n.isVisibleToUser()&&n.isEnabled()&&n.isClickable()){
    android.graphics.Rect bounds=new android.graphics.Rect();
    n.getBoundsInScreen(bounds);
    String key=step+":"+n.getViewIdResourceName()+":"+bounds.toShortString();
    int tries=attempts.containsKey(key)?attempts.get(key):0;
    if(tries>=3){complete(false,"已尝试设置，状态未刷新；请手动确认");return false;}
    boolean clicked=n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    if(clicked){attempts.put(key,tries+1);settleUntil=SystemClock.uptimeMillis()+1800;}
    return clicked;
   }
  }
  return false;
 }

 private void fingerprint(AccessibilityNodeInfo n,StringBuilder out){
  if(n==null)return;
  if(n.isVisibleToUser()){
   out.append(n.getViewIdResourceName()).append(n.getText()).append(n.isChecked());
  }
  for(int i=0;i<n.getChildCount();i++)fingerprint(n.getChild(i),out);
 }

 private void searchScroll(AccessibilityNodeInfo root,int limit){
  StringBuilder data=new StringBuilder();
  fingerprint(root,data);
  String current=Integer.toHexString(data.toString().hashCode());
  unchanged=current.equals(lastScreen)?unchanged+1:0;
  lastScreen=current;
  if(unchanged>=2||scrolls>=limit){
   complete(false,"页面未找到可验证的开关，已停止搜索");
   return;
  }
  if(scroll(root)){
   scrolls++;settleUntil=SystemClock.uptimeMillis()+1600;
  }else complete(false,"已到页面底部，未找到可验证的开关");
 }

 private boolean scroll(AccessibilityNodeInfo n){
  if(n==null)return false;
  if(n.isScrollable()&&n.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD))return true;
  for(int i=0;i<n.getChildCount();i++)if(scroll(n.getChild(i)))return true;
  return false;
 }

 private void complete(boolean ok,String note){
  if(!running)return;
  results[step]=(ok?"已确认 · ":"待确认 · ")+note;
  step++;
  if(step==4){
   int verified=0;
   for(String s:results)if(s.startsWith("已确认"))verified++;
   running=false;
   save(verified==4?"设置优化完成，请锁屏测试收信":"检查完成："+verified+" / 4 项已确认，其余需手动检查");
   finishRun();
  }else startStep();
 }

 public void stop(String reason){
  if(!running)return;
  running=false;
  save(reason+"；已完成的修改会保留");
  finishRun();
 }

 private void save(String message){
  android.content.SharedPreferences.Editor e=getSharedPreferences("optimizer",0).edit()
   .putString("message",message).putInt("step",step).putBoolean("running",running);
  for(int i=0;i<4;i++)e.putString("result"+i,results[i]==null?"等待处理":results[i]);
  e.apply();
 }

 private int dp(int n){return (int)(getResources().getDisplayMetrics().density*n);}

 private void showOverlay(){
  try{
   overlay=new LinearLayout(this);
   overlay.setOrientation(1);
   overlay.setPadding(dp(12),dp(8),dp(12),dp(8));
   overlay.setBackgroundColor(Color.rgb(54,78,89));
   caption=new TextView(this);
   caption.setTextColor(Color.WHITE);
   caption.setTextSize(14);
   overlay.addView(caption);
   bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
   bar.setMax(4);
   overlay.addView(bar,new LinearLayout.LayoutParams(-1,dp(8)));
   Button cancel=new Button(this);
   cancel.setText("停止优化");
   cancel.setOnClickListener(new View.OnClickListener(){public void onClick(View v){stop("你已停止优化");}});
   overlay.addView(cancel,new LinearLayout.LayoutParams(-1,dp(44)));
   WindowManager.LayoutParams lp=new WindowManager.LayoutParams(dp(250),-2,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
   lp.gravity=Gravity.BOTTOM|Gravity.END;
   lp.y=dp(30);
   ((WindowManager)getSystemService(WINDOW_SERVICE)).addView(overlay,lp);
  }catch(Exception e){overlay=null;}
 }

 private void updateOverlay(){
  if(overlay!=null){
   caption.setText((step+1)+" / 4 · "+titles[step]+" · 检查中");
   bar.setProgress(step,true);
  }
 }

 private void removeOverlay(){
  if(overlay!=null){
   try{((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(overlay);}catch(Exception e){}
   overlay=null;
  }
 }

 private void finishRun(){
  handler.removeCallbacks(loop);
  removeOverlay();
  Intent i=new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
  launch(i);
  disableSelf();
 }
}
