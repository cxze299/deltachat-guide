# Brand workflows — v2.2

Routing uses Build.MANUFACTURER and Build.BRAND. Build.MODEL and Android release/API level are displayed locally. No IMEI, serial, phone number or upload is used. Device information selects a profile, but UI controls and states must still match before any action.

| Family | Behavior | Validation |
|---|---|---|
| Xiaomi / Redmi / POCO | Existing MIUI battery and startup flow | Prior user run, v2.2 changes not device-verified |
| OPPO / OnePlus / realme | App details, background-activity labels and candidate startup pages | Experimental |
| Huawei / Honor | Manual launch management; automatic-management switch must be OFF, all three individual allow switches ON | Experimental |
| vivo / iQOO | Background power labels and candidate startup pages | Experimental |
| Other Android | Common settings only; unknown startup policy marked unverified | Generic fallback |

Huawei/Honor identities are separate. Old manufacturer component candidates may be unavailable on newer Honor; fall back to app details instead of inventing private API names. Only Android APK-compatible devices are covered. Native HarmonyOS apps require a separate implementation.

## References checked 2026-10-25

Official guidance describes setting semantics, not stable AccessibilityNode layouts or private Activity APIs. No public stable OEM automation API was found in the OPPO/vivo developer portal searches; official support and manuals were used instead.

- Xiaomi developer autostart policy: https://dev.mi.com/xiaomihyperos/documentation/detail?pId=1624
- Huawei official background management: https://consumer.huawei.com/cn/support/content/zh-cn00428704/
- Honor notification and startup guidance: https://www.honor.com/cn/support/content/zh-cn00685514/
- OnePlus OxygenOS 14 manual: https://service.oneplus.com/content/dam/support/user-manuals/common/OxygenOS_14.0_User_Manual.pdf
- OPPO battery guidance: https://support.oppo.com/en/answer/?aid=2171043
- vivo official background operation: https://www.vivo.com/tw/support/questionByTitle?title=%E8%83%8C%E6%99%AF%E9%81%8B%E8%A1%8C
- Android Settings API: https://developer.android.com/reference/android/provider/Settings
- Candidate native components, not OEM public API: https://github.com/judemanutd/AutoStarter

## Verification and release status

JVM tests pass for 12 device identities, Huawei/Honor inverted switch semantics, exact package allowlists and candidate containment. APK builds and signature verification pass. A combined documentation/install/open/browser-check command was rejected by automatic approval with only “blocked by policy”; no v2.2 device installation or deployment is claimed. The current NAS release remains v2.1 until a separate authorized release can be completed. Unknown layouts/timeouts are unverified, never successful. Reading the device model alone cannot certify a script or successful notification delivery.


Release update 2026-10-25: user explicitly requested NAS deployment. v2.2 APK and all four guide pages deployed and verified byte-for-byte over HTTPS. Phone installation and other-brand device verification remain unconfirmed.
