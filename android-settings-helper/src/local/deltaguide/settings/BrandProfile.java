package local.deltaguide.settings;
import java.util.Locale;
/** Manufacturer routing is separate from UI recognition. Candidate components are not public OEM APIs. */
public final class BrandProfile {
 public final String id,name; public final String[] packages,startupComponents;
 public final boolean manualLaunchManagement;
 private BrandProfile(String id,String name,String[] packages,String[] components,boolean manual){this.id=id;this.name=name;this.packages=packages;this.startupComponents=components;this.manualLaunchManagement=manual;}
 public static BrandProfile detect(String manufacturer,String brand){
 String value=((manufacturer==null?"":manufacturer)+" "+(brand==null?"":brand)).toLowerCase(Locale.ROOT);
 if(has(value,"honor"))return new BrandProfile("honor","荣耀 MagicOS（Android）",new String[]{"com.hihonor.systemmanager","com.huawei.systemmanager"},new String[]{"com.huawei.systemmanager/com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"},true);
 if(has(value,"huawei"))return new BrandProfile("huawei","华为 EMUI / 兼容 Android 的系统",new String[]{"com.huawei.systemmanager"},new String[]{"com.huawei.systemmanager/com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"},true);
 if(has(value,"xiaomi","redmi","poco"))return new BrandProfile("xiaomi","小米 / Redmi / POCO",new String[]{"com.miui.securitycenter","com.miui.powerkeeper"},new String[]{"com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity"},false);
 if(has(value,"oneplus","oppo","realme"))return new BrandProfile("oppo",has(value,"oneplus")?"一加 ColorOS / OxygenOS":has(value,"realme")?"realme UI":"OPPO ColorOS",new String[]{"com.coloros.safecenter","com.oppo.safe","com.oplus.safecenter","com.coloros.oppoguardelf","com.oplus.battery","com.oneplus.security"},new String[]{"com.coloros.safecenter/com.coloros.safecenter.permission.startup.StartupAppListActivity","com.coloros.safecenter/com.coloros.safecenter.startupapp.StartupAppListActivity","com.oppo.safe/com.oppo.safe.permission.startup.StartupAppListActivity","com.oneplus.security/com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"},false);
 if(has(value,"vivo","iqoo"))return new BrandProfile("vivo","vivo / iQOO",new String[]{"com.vivo.permissionmanager","com.iqoo.secure","com.vivo.abe"},new String[]{"com.vivo.permissionmanager/com.vivo.permissionmanager.activity.BgStartUpManagerActivity","com.iqoo.secure/com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"},false);
 return new BrandProfile("generic","Android 通用",new String[0],new String[0],false);
 }
 private static boolean has(String value,String... words){for(String word:words)for(String token:value.split("[^a-z0-9]+"))if(token.equals(word))return true;return false;}
 public boolean allowsPackage(String p){if("com.android.settings".equals(p))return true;for(String allowed:packages)if(allowed.equals(p))return true;return false;}
 public String validation(){return id.equals("xiaomi")?"已有小米用户试跑记录；不同系统版本仍需核对":"试验适配 · 尚无此品牌真机验证";}
}
