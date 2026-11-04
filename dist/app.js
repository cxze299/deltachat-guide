const isGuideIPad=/iPad/i.test(navigator.userAgent)||(/Mac/i.test(navigator.platform+' '+navigator.userAgent)&&navigator.maxTouchPoints>1);
document.documentElement.classList.toggle('ipad',isGuideIPad);
function detectGuideDevice(nav){
 const ua=nav.userAgent||'',os=(nav.userAgentData&&nav.userAgentData.platform)||nav.platform||'';
 const apple=/iPhone|iPad|iPod/i.test(ua)||/iOS|iPadOS/i.test(os)||(/Mac/i.test(os+' '+ua)&&nav.maxTouchPoints>1);
 const android=!apple&&(/Android/i.test(ua+' '+os)||/HarmonyOS/i.test(ua));
 let brand=null;
 if(android){
  if(/HONOR|HUAWEI/i.test(ua))brand='huawei';
  else if(/Xiaomi|Redmi|POCO|MIUI|MiBrowser/i.test(ua))brand='xiaomi';
  else if(/OPPO|OnePlus|realme|HeyTap/i.test(ua))brand='oppo';
  else if(/vivo|iQOO/i.test(ua))brand='vivo';
  else if(/Samsung|SM-[A-Z0-9]+/i.test(ua))brand='samsung';
  else if(/Pixel/i.test(ua))brand='pixel';
 }
 return {platform:apple?'ios':'android',apple,brand};
}
const guideDevice=detectGuideDevice(navigator);
const appleDevice=guideDevice.apple;
const stepRoute=location.pathname.match(/\/([123])\/?$/);
const panel=document.querySelector('#panel');let current=stepRoute?Number(stepRoute[1])-1:0,platform=guideDevice.platform,done=[];try{const saved=JSON.parse(localStorage.getItem('delta-guide-progress')||'[]');if(Array.isArray(saved))done=saved.filter(x=>[0,1,2].includes(x));}catch{}
const shot=(n,alt)=>`<button class="screenshot" data-image="assets/image${n}.png" aria-label="放大${alt}"><img src="assets/image${n}.png" alt="${alt}"><span>点击看大图 <svg class="ui-icon" aria-hidden="true"><use href="#icon-external"></use></svg></span></button>`;
const bottom=()=>`<div class="panel-bottom"><span>${done.includes(current)?'✓ 已完成':''}</span><button class="primary" id="complete">${current===2?'我已完成设置':'已完成，下一步'} <span><svg class="ui-icon" aria-hidden="true"><use href="#icon-next"></use></svg></span></button></div>`;
function render(){if(stepRoute){const url=new URL(location.href);url.pathname=url.pathname.replace(/\/[123]\/?$/,`/${current+1}/`);history.replaceState(null,'',url);}document.querySelectorAll('.step').forEach((b,i)=>{b.classList.toggle('active',i===current);b.classList.toggle('done',done.includes(i));if(i===current)b.setAttribute('aria-current','step');else b.removeAttribute('aria-current');b.querySelector('.step-number').textContent=done.includes(i)?'✓':`0${i+1}`});
if(current===0){panel.innerHTML=`<div class="panel-heading"><h3>安装或更新 Delta Chat</h3><span class="badge">STEP 01 / 03</span></div><p class="description">请安装或更新至最新版本。</p><div class="tabs" role="tablist" aria-label="手机类型"><button role="tab" aria-selected="${platform==='android'}" class="tab ${platform==='android'?'selected':''}" data-platform="android">Android 安卓</button><button role="tab" aria-selected="${platform==='ios'}" class="tab ${platform==='ios'?'selected':''}" data-platform="ios">iPhone / iPad</button></div><div role="tabpanel">${platform==='android'?`<a class="secondary" target="_blank" rel="noopener noreferrer" href="https://mouss.synology.me:1050/delta.apk">下载 Android 安装包 <svg class="ui-icon" aria-hidden="true"><use href="#icon-download"></use></svg></a><button class="secondary notification-entry" id="open-notification-guide">收不到通知？查看设置攻略</button>`:`<div class="content-row"><div><p>按顺序操作，下载或更新应用。</p><button class="secondary" id="switch-store">1. 切换 App Store 账号</button><a class="secondary store-fallback" href="${appleDevice?'itms-apps://apps.apple.com/sg/app/delta-chat/id1459523234?l=zh-Hans-CN':'https://apps.apple.com/sg/app/delta-chat/id1459523234?l=zh-Hans-CN'}">2. 在 App Store 中打开 <svg class="ui-icon" aria-hidden="true"><use href="#icon-external"></use></svg></a><a class="secondary store-fallback" href="https://apps.apple.com/sg/app/delta-chat/id1459523234?l=zh-Hans-CN">备用：打开商店网页 <svg class="ui-icon" aria-hidden="true"><use href="#icon-external"></use></svg></a></div></div><div class="tip">请用 Safari 打开；失败时检查网络及商店地区。</div>`}</div>${bottom()}`}
if(current===1){panel.innerHTML=`<div class="panel-heading"><h3>配置账号或中继</h3><span class="badge">STEP 02 / 03</span></div><div class="content-row"><div><a class="secondary" href="DCACCOUNT:https://talk.cmsv.space/new">点击这里创建账号或添加中继</a></div></div>${bottom()}`}
if(current===2){panel.innerHTML=`<div class="panel-heading"><h3>添加好友</h3><span class="badge">STEP 03 / 03</span></div><a class="secondary" href="https://i.delta.chat/#A04EA3BFF772833E84995A05058F07D243571C73&amp;v=3&amp;i=mfl2aClXEQuz7R1-Q87ivSk-&amp;s=HhVW5NW1cxhEhNn3tRFk2kJZ&amp;a=jv5gth1cm%40talk.cmsv.space&amp;n=%E5%A5%BD%E5%8F%8B%E5%8A%A9%E6%89%8B" target="_blank" rel="noopener noreferrer">点击这里添加好友助手 <svg class="ui-icon" aria-hidden="true"><use href="#icon-external"></use></svg></a>${bottom()}`}
}
function toast(s){const el=document.querySelector('#toast');el.textContent=s;el.classList.add('visible');clearTimeout(window.toastTimer);window.toastTimer=setTimeout(()=>el.classList.remove('visible'),3500)}
document.addEventListener('click',async e=>{const step=e.target.closest('[data-step]');if(step){current=Number(step.dataset.step);render();return}const tab=e.target.closest('[data-platform]');if(tab){platform=tab.dataset.platform;render();panel.querySelector(`[data-platform="${platform}"]`).focus();return}const copy=e.target.closest('[data-copy]');if(copy){try{await navigator.clipboard.writeText(copy.dataset.copy);const label=copy.textContent;copy.textContent='已复制';setTimeout(()=>copy.textContent=label,2000);toast(copy.dataset.copyMessage||'已复制，可粘贴到 Delta Chat')}catch{toast('复制未成功，请手动选择并复制文字')}return}const img=e.target.closest('[data-image]');if(img){document.querySelector('#large-image').src=img.dataset.image;document.querySelector('#image-dialog').showModal();return}if(e.target.closest('#complete')){if(current===2){done=[];current=0;try{localStorage.removeItem('delta-guide-progress')}catch{}toast('设置已完成，进度已重置。')}else{if(!done.includes(current))done.push(current);try{localStorage.setItem('delta-guide-progress',JSON.stringify(done))}catch{}current++;}render();panel.focus();}if(e.target.closest('#reset')){done=[];current=0;try{localStorage.removeItem('delta-guide-progress')}catch{}render();toast('进度已重置')}if(e.target.closest('#close-image'))document.querySelector('#image-dialog').close()});
document.querySelector('#image-dialog').addEventListener('click',e=>{if(e.target===e.currentTarget)e.currentTarget.close()});render();

const helpDialog=document.querySelector('#help-dialog');
document.querySelector('#open-help').addEventListener('click',()=>helpDialog.showModal());
document.querySelector('#close-help').addEventListener('click',()=>helpDialog.close());
helpDialog.addEventListener('click',e=>{if(e.target===helpDialog)helpDialog.close()});

document.addEventListener('click',e=>{if(e.target.closest('#switch-store'))document.querySelector('#store-dialog').showModal();if(e.target.closest('#close-store'))document.querySelector('#store-dialog').close();});
document.querySelector('#store-dialog').addEventListener('click',e=>{if(e.target===e.currentTarget)e.currentTarget.close();});

const notificationDialog=document.querySelector('#notification-dialog');
document.addEventListener('click',e=>{if(e.target.closest('#open-notification-guide'))notificationDialog.showModal();if(e.target.closest('#close-notification-guide'))notificationDialog.close();});
notificationDialog.addEventListener('click',e=>{if(e.target===notificationDialog)notificationDialog.close()});
const brandGuides={xiaomi:'应用省电策略 → 无限制；允许后台自启动；在最近任务中锁定应用。',huawei:'应用启动管理 → 关闭自动管理，允许自启动、关联启动和后台活动。',oppo:'应用耗电管理 → 允许后台活动；允许自启动；在最近任务中锁定应用。',vivo:'后台耗电管理 → 允许后台高耗电或不限制；允许自启动。',samsung:'从休眠、深度休眠应用中移除，加入从不自动休眠的应用；电池选择不受限制（如有）。',pixel:'应用信息 → 应用电池用量 → 允许后台使用，选择不受限制（如有）。'};
document.querySelector('#phone-brand').addEventListener('change',e=>{document.querySelector('#brand-instructions').textContent=brandGuides[e.target.value]||'选择品牌后查看对应设置说明。'});


// The browser opens our BROWSABLE bridge; only the native bridge opens system Settings.
const helperFallback=new URL(location.href);helperFallback.hash='android-settings-fallback';
document.querySelectorAll('[data-settings-action]').forEach(a=>{a.href='intent://settings?action='+a.dataset.settingsAction+'#Intent;scheme=deltaguide;package=local.deltaguide.settings;S.browser_fallback_url='+encodeURIComponent(helperFallback.href)+';end';});
document.addEventListener('click',e=>{const a=e.target.closest('[data-settings-action]');if(!a)return;const status=document.querySelector('#settings-launch-status');if(!/Android/i.test(navigator.userAgent)){e.preventDefault();status.textContent='请在 Android 手机上安装助手后使用。';return;}status.textContent='允许浏览器打开“Delta 通知设置助手”，即可进入设置。';});
function settingsFallback(){if(location.hash!=='#android-settings-fallback')return;if(!notificationDialog.open)notificationDialog.showModal();document.querySelector('#settings-launch-status').textContent='请先安装设置助手；已安装时，请允许浏览器打开外部应用。';}
settingsFallback();window.addEventListener('hashchange',settingsFallback);

// Browser privacy settings can hide the model; only preselect a brand when explicitly identified.
if(guideDevice.brand){const brandSelect=document.querySelector('#phone-brand');brandSelect.value=guideDevice.brand;document.querySelector('#brand-instructions').textContent=brandGuides[guideDevice.brand];}

const existingDialog=document.querySelector('#existing-dialog');
document.querySelector('#open-existing').addEventListener('click',()=>existingDialog.showModal());
document.querySelector('#close-existing').addEventListener('click',()=>existingDialog.close());
existingDialog.addEventListener('click',e=>{if(e.target===existingDialog)existingDialog.close();});
document.querySelector('#existing-update').addEventListener('click',()=>{existingDialog.close();current=0;render();panel.focus();});
