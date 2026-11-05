# Brand workflows — v2.3

Routing uses Build.MANUFACTURER and Build.BRAND. Build.MODEL and Android release/API level are displayed locally. No IMEI, serial, phone number or upload is used. Device information selects a profile, but UI controls and states must still match before any action.

| Family | Behavior | Validation |
|---|---|---|
| Xiaomi / Redmi / POCO | MIUI / HyperOS 电池无限制单选 + 自启动列表勾选 | 已有实测 |
| OPPO / OnePlus / realme | ColorOS / OxygenOS 耗电管理“允许后台活动”/“唤醒前台” + 专属自启动列表与单项权限 | v2.3 精细化分支 |
| Huawei / Honor | EMUI / HarmonyOS / MagicOS 手动启动管理：自动管理切关 -> 校验 3 个子开关 -> 点击确定 | v2.3 精细化分支 |
| vivo / iQOO | OriginOS / FuntouchOS 耗电管理“允许后台高耗电”单选/开关 + 专属自启动列表与单项权限 | v2.3 精细化分支 |
| Other Android | AOSP 通用流程：系统通知开关 + 电池无限制/忽略优化 + Delta Chat 内部后台连接 | 通用兜底 |

Huawei/Honor identities are separate. Newer Honor uses `com.hihonor.systemmanager`, Huawei uses `com.huawei.systemmanager`. Newer OPPO/OnePlus/realme includes `com.oplus.battery.startupapp.view.StartupAppListActivity`.

## References
- Xiaomi developer autostart policy: https://dev.mi.com/xiaomihyperos/documentation/detail?pId=1624
- Huawei official background management: https://consumer.huawei.com/cn/support/content/zh-cn00428704/
- Honor notification and startup guidance: https://www.honor.com/cn/support/content/zh-cn00685514/
- OnePlus OxygenOS manual: https://service.oneplus.com/content/dam/support/user-manuals/common/OxygenOS_14.0_User_Manual.pdf
- OPPO battery guidance: https://support.oppo.com/en/answer/?aid=2171043
- vivo official background operation: https://www.vivo.com/tw/support/questionByTitle?title=%E8%83%8C%E6%99%AF%E9%81%8B%E8%A1%8C
- Android Settings API: https://developer.android.com/reference/android/provider/Settings
- Candidate native components: https://github.com/judemanutd/AutoStarter
