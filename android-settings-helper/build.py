from pathlib import Path
import subprocess,os,secrets,zipfile
root=Path(__file__).resolve().parent.parent;j=Path('C:/Program Files/Android/Android Studio/jbr/bin');t=root/'output/android-tools/android-15';sdk=Path('D:/Android/Sdk/platforms/android-35/android.jar');out=root/'output/helper-build';out.mkdir(exist_ok=True);(out/'classes').mkdir(exist_ok=True);(out/'dex').mkdir(exist_ok=True)
env=os.environ.copy();env['JAVA_HOME']=str(j.parent);env['PATH']=str(j)+';'+env['PATH']
def run(args):subprocess.run([str(x) for x in args],check=True,env=env)
run([j/'javac.exe','-encoding','UTF-8','-source','8','-target','8','-bootclasspath',sdk,'-d',out/'classes',*list((root/'android-settings-helper/src').rglob('*.java'))])
run([t/'d8.bat','--lib',sdk,'--output',out/'dex',*list((out/'classes').rglob('*.class'))])
run([t/'aapt2.exe','compile','--dir','android-settings-helper/res','-o',out/'resources.zip'])
run([t/'aapt2.exe','link','-I',sdk,'--manifest',root/'android-settings-helper/AndroidManifest.xml',out/'resources.zip','-o',out/'unsigned.apk'])
with zipfile.ZipFile(out/'unsigned.apk','a') as z:z.write(out/'dex/classes.dex','classes.dex')
run([t/'zipalign.exe','-f','4',out/'unsigned.apk',out/'aligned.apk'])
keys=Path.home()/'.android/delta-guide-signing';keys.mkdir(exist_ok=True);pw=keys/'password.txt';key=keys/'release.jks'
if not pw.exists():pw.write_text(secrets.token_urlsafe(32))
if not key.exists():run([j/'keytool.exe','-genkeypair','-keystore',key,'-storepass:file',pw,'-keypass:file',pw,'-alias','delta-guide','-keyalg','RSA','-keysize','2048','-validity','10000','-dname','CN=Delta Guide Settings Helper'])
(root/'dist/downloads').mkdir(exist_ok=True)
run([t/'apksigner.bat','sign','--ks',key,'--ks-pass','file:'+str(pw),'--out',root/'dist/downloads/delta-settings-helper.apk',out/'aligned.apk'])
run([t/'apksigner.bat','verify',root/'dist/downloads/delta-settings-helper.apk'])
