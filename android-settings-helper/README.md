# Delta notification settings helper

Small permission-free Android bridge for the locally hosted guide. The website opens a BROWSABLE `deltaguide://settings?action=...` URI; the helper calls Android Settings with an allowlisted action. It does not accept arbitrary component names or package names from the browser. It detects installed `chat.delta` / `com.b44t.messenger` versions.

Actions: app, notifications, details, battery, autostart. Vendor-specific launch failures fall back to application details. No background service, network permission, notification permission, accessibility service or settings modification is used.

Build: `python android-settings-helper/build.py`. Requires the Android Studio JBR, Android 35 platform, and official build tools in output/android-tools/android-15. Signing credentials are stored outside the repository under the current user .android/delta-guide-signing. Never deploy those credentials.

References:
- https://developer.android.com/reference/android/provider/Settings
- https://developer.chrome.com/docs/android/intents
- https://github.com/judemanutd/AutoStarter (OEM Activity identifiers)

Validation: APK signature verified; installed on connected Xiaomi M2007J1SC / Android 13. Deep-link invocations were issued. Foreground/browser inspection was blocked by automatic approval, so end-to-end website-to-settings verification remains pending. Other manufacturers are candidate native routes with fallback, not verified devices. Modern Honor variants may require the generic fallback. The app action opens Delta Chat itself, not its private notification preferences screen.


## Version 2.0

Adds a local, fixed AccessibilityService workflow for Xiaomi MIUI: notification permission, app battery policy, autostart and Delta Chat background connection. It does not execute downloaded scripts or arbitrary commands. The user must manually enable the service and explicitly start each run; deep links only open the dashboard. Service events are restricted to system Settings, MIUI security / power manager, and the two Delta Chat package variants. UI content is processed locally in memory, never logged or uploaded. Existing UI controls are matched conservatively by exact labels and target app identity. Unknown layouts time out and are reported as unverified. Internal Delta Chat preference navigation is restricted to recognized activity classes / toolbar resources.

The floating control displays completed-check progress and offers stop. Each step advances only when a checked state is observed or a timeout/error is explicitly recorded. A 100% check count does not imply all settings succeeded; the result list records verified versus unverified. Stopping preserves changes already applied. Screen-off stops the workflow. The service disables itself after a run. No permissions are enabled automatically, and system confirmation dialogs are left to the user.

The four-stage automatic flow is currently Xiaomi-only and awaits the connected device's user-authorized run; other manufacturers retain manual native jump routes. Notification channels, lock-screen previews, DND, and long-term delivery are not asserted to be verified. The app requests no INTERNET permission. Android 7+ is required. A notification bell/checkmark vector and adaptive launcher icon are included.


## Version 2.1 follow-up

The connected Xiaomi user reported the sequence completed but autostart and app settings kept searching after toggles were already on. v2.1 matches checkbox controls by label-row vertical overlap (rather than a fixed ancestor depth), accepts checked app background controls as evidence alongside the system battery API, waits for UI settling after actions, limits repeat clicks and stops scrolling when the visible page stops changing or reaches the bottom. Installed on the connected phone; the follow-up device run remains to be confirmed by the user. UI fingerprints are transient hashes used only to detect unchanged pages and are never persisted or transmitted.


## Version 2.2 — local build

Automatic brand routing and model/Android version display added. See BRANDS.md for primary references and validation limits. MainActivity and OptimizerService share BrandProfile. Device installation was blocked by automatic approval; NAS release remains v2.1.


Release update 2026-10-25: user explicitly requested NAS deployment. v2.2 APK and all four guide pages deployed and verified byte-for-byte over HTTPS. Phone installation and other-brand device verification remain unconfirmed.
