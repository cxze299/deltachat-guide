# Delta Chat 使用引导（本地部署）

双击 start-local.cmd 启动，然后打开 http://127.0.0.1:8917/ 。

保持启动窗口打开，按 Ctrl+C 停止服务。电脑重启后需重新启动。

网站源码位于 dist 目录，修改后刷新浏览器即可。依赖本机 Python，无需 Sites 或云端构建。仅允许本机访问；网页内的外部下载与账号配置入口仍需联网。

## NAS 部署

访问地址：https://mouss.synology.me:1050/deltachat-guide/

NAS 目录：/volume1/web/poems/deltachat-guide/

使用现有 Web Station HTTPS（1050 端口）托管 dist 静态文件，无容器、无需单独的应用进程。发布时仅上传 dist 中的文件，不上传 reference、源码工具或本地配置。
