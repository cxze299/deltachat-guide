import local.deltaguide.settings.BrandProfile;
public class BrandProfileTest {
 static void check(boolean result,String note){if(!result)throw new AssertionError(note);}
 static void device(String maker,String brand,String expected){check(BrandProfile.detect(maker,brand).id.equals(expected),maker+" / "+brand);}
 public static void main(String[] args){
 device("Xiaomi","Redmi","xiaomi");device("POCO","POCO","xiaomi");device("HUAWEI","HONOR","honor");device("HONOR","HONOR","honor");device("HUAWEI","HUAWEI","huawei");device("OPPO","OnePlus","oppo");device("OnePlus","OnePlus","oppo");device("realme","realme","oppo");device("vivo","iQOO","vivo");device("BBK","vivo","vivo");device(null,null,"generic");device("NotXiaomi","unknown","generic");
 BrandProfile h=BrandProfile.detect("HUAWEI","HONOR");check(h.manualLaunchManagement,"Honor list switch must be automatic management, never treated as autostart enable");check(!BrandProfile.detect("vivo","vivo").manualLaunchManagement,"vivo keeps normal enable semantics");
 for(String brand:new String[]{"Xiaomi","HONOR","HUAWEI","OPPO","OnePlus","realme","vivo","iQOO","unknown"}){BrandProfile p=BrandProfile.detect(brand,brand);check(p.allowsPackage("com.android.settings"),"public settings missing");check(!p.allowsPackage("com.example.bank"),"unrelated UI allowed");check(!p.allowsPackage("com.android.settings.evil"),"package prefix matching is unsafe");for(String component:p.startupComponents)check(p.allowsPackage(component.split("/")[0]),"candidate outside profile allowlist");}
 check(BrandProfile.detect("OPPO","OnePlus").name.contains("一加"),"OnePlus identity lost");System.out.println("Brand profiles: 12 device routes, inverted Huawei semantics, candidate scopes and unrelated-package rejection passed.");
 }
}
