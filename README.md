# Delta Chat 使用引导

一个中文静态引导网站，帮助用户完成 **安装或更新 → 配置账号或中继 → 添加好友**。支持电脑、iPhone / iPad 和 Android，可部署到普通云服务器或群晖 Web Station。

[在线使用](https://mouss.synology.me:1050/deltachat-guide/) · [云服务器部署说明](CLOUD-DEPLOY.md) · [Android 通知助手](android-settings-helper/README.md)

> 本项目为非官方使用指南。Delta Chat 官方中文网站：https://delta.chat/zh_CN/

## 功能

- 三步引导、步骤切换和本地进度保存，完成最后一步后重置进度。
- 自动识别 Android / iOS，选择对应下载入口；提供 App Store 账号切换快捷指令入口。
- 使用按钮打开账号配置、添加中继及好友助手。
- 老帐号继续使用说明：更新软件、添加中继、清理多余服务器、双方测试，必要时重新互加好友。
- Android 通知攻略及通知设置助手下载入口。
- 桌面端采用紧凑说明页布局，手机端保留适合触摸操作的引导界面。

## 本地运行

需要 Python 3，无需安装 Node.js、数据库或容器。

```sh
git clone https://github.com/cxze299/deltachat-guide.git
cd deltachat-guide
python -m http.server 8917 --bind 127.0.0.1 --directory dist
```

打开 **http://127.0.0.1:8917/**。Windows 也可双击 `start-local.cmd`。

保持终端运行，按 `Ctrl+C` 停止。此命令仅允许本机访问，外部下载和账号服务仍需联网。

## 云服务器一键部署

适用于 **Ubuntu / Debian**，需要 sudo 权限及访问 GitHub、系统软件源的网络连接。

```sh
curl -fsSL https://raw.githubusercontent.com/cxze299/deltachat-guide/main/deploy-cloud.sh -o deploy-cloud.sh
sudo sh deploy-cloud.sh
```

完成后访问 **http://服务器公网IP:8080/**，在云平台安全组和服务器防火墙中放行 **TCP 8080**。

脚本会安装 Git 和 Nginx，下载仓库默认分支的网站文件，写入独立 Nginx 配置，检查配置后启动或重新加载 Nginx。

| 项目 | 默认值 |
| --- | --- |
| 访问端口 | `8080` |
| 网站目录 | `/var/www/deltachat-guide` |
| Nginx 配置 | `/etc/nginx/conf.d/deltachat-guide.conf` |
| 更新方式 | 再次执行部署脚本 |

自定义端口：

```sh
sudo env PORT=8090 sh deploy-cloud.sh
```

请使用未被其他服务占用的端口。脚本不自动修改防火墙，不申请证书；如需 HTTPS，请配置域名、证书和对应 Nginx 服务。部署会覆盖同名网站文件，但不会清理旧的多余文件。已有该站点配置也会被脚本替换。

更多说明见 [CLOUD-DEPLOY.md](CLOUD-DEPLOY.md)。

## 群晖 NAS / 已有 Web 服务器

将 **`dist` 目录里的内容**上传到网站根目录或子目录。例如现有站点：

- 目录：`/volume1/web/poems/deltachat-guide/`
- 地址：https://mouss.synology.me:1050/deltachat-guide/

群晖使用 Web Station 配置静态站点和 HTTPS 即可，无需 Docker。

服务器已有 Git 仓库与目标目录时，也可在仓库根目录执行：

```sh
git pull --ff-only
sh deploy-server.sh /volume1/web/poems/deltachat-guide
```

`deploy-server.sh` 只发布当前 Git 提交中的 `dist` 文件，目标目录必须已存在且可写。它不会安装 Nginx、拉取更新或配置 HTTPS，未提交的本地修改不会被发布。

## 直接打开指定步骤

在部署地址后添加路径：

| 路径 | 内容 |
| --- | --- |
| `/1/` | 安装或更新 |
| `/2/` | 配置账号或中继 |
| `/3/` | 添加好友 |

例如：https://mouss.synology.me:1050/deltachat-guide/2/

## 目录与维护

| 文件 / 目录 | 用途 |
| --- | --- |
| `dist/index.html` | 页面结构及说明弹窗 |
| `dist/style.css` | 桌面及移动端样式 |
| `dist/app.js` | 步骤、设备识别、链接及交互逻辑 |
| `dist/1/`、`dist/2/`、`dist/3/` | 指定步骤的静态入口 |
| `dist/assets/` | 网站资源 |
| `dist/downloads/` | Android 设置助手安装包 |
| `android-settings-helper/` | Android 设置助手源码及品牌适配说明 |
| `start-local.cmd` | Windows 本地启动入口 |
| `deploy-cloud.sh` | Ubuntu / Debian 云服务器部署 |
| `deploy-server.sh` | 已有网站目录发布 |

本项目直接维护 `dist` 内的静态源码，无额外打包步骤。修改根 `index.html` 后，应同步三个步骤入口，并保留它们的 `<base href="../">`，以正确引用公共资源。修改 CSS / JS 后，更新 HTML 中的资源版本参数，避免浏览器继续使用旧缓存。

部署时只上传 `dist` 内容，不要将本地工具、临时文件或密钥放到公开网站目录。

## 外部服务与 Android 助手

页面中的 Android 主程序下载、中继服务器、好友助手、App Store 和快捷指令均依赖外部服务。自行部署网页不会同时部署这些服务；更换服务地址时需检查 `dist/app.js` 和 `dist/index.html` 中的对应链接。

Android 设置助手部分自动化需要用户在手机上手动授权无障碍服务。不同系统和品牌的设置界面可能不同，新增品牌流程并不代表已经逐一完成真机验证。详见 [助手说明](android-settings-helper/README.md) 和 [品牌适配说明](android-settings-helper/BRANDS.md)。

## 常见问题

- **服务器无法访问**：确认 Nginx 正常运行、端口未冲突，安全组和防火墙均已放行。
- **GitHub 下载失败**：检查服务器 DNS、网络和 GitHub 连通性。
- **更新后仍显示旧样式**：强制刷新页面，检查资源版本参数，并确认更新的是当前站点目录。
- **按钮没有打开应用**：先安装或更新 Delta Chat，使用系统浏览器打开，并允许跳转到应用。
- **第三方下载或账号入口不可用**：检查对应外部服务状态；重新部署静态网站不会修复外部服务。

## APK 下载

[国内下载：NAS](https://mouss.synology.me:1050/deltachat-guide/downloads/delta-settings-helper.apk) · [备用下载：GitHub](https://github.com/cxze299/deltachat-guide/releases/download/settings-helper-v2.2/delta-settings-helper.apk) · [Release 说明](https://github.com/cxze299/deltachat-guide/releases/tag/settings-helper-v2.2)

此安装包为 Android 通知设置助手，不是 Delta Chat 主程序。
