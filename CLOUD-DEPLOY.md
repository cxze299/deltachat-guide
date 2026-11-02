# 云服务器容器部署

需要 Git、Docker Engine 和 Docker Compose 插件，并确保服务器能拉取 GitHub 仓库和 Nginx 镜像。

## 首次部署

```sh
git clone https://github.com/cxze299/deltachat-guide.git
cd deltachat-guide
sh deploy-docker.sh
```

如果当前用户无权访问 Docker，请使用 `sudo sh deploy-docker.sh`。

访问 `http://服务器公网IP:8080/`，在云安全组及主机防火墙放行 TCP 8080。默认绑定所有网卡，脚本不会自动修改防火墙。

自定义端口可在仓库根目录创建 `.env`：

```dotenv
PORT=8090
BIND_ADDRESS=0.0.0.0
```

已有宿主机反向代理时可将 `BIND_ADDRESS` 设为 `127.0.0.1`，由代理处理域名和 HTTPS。容器本身提供 HTTP，不自动申请证书。

## 更新与管理

```sh
git pull --ff-only
sh deploy-docker.sh
```

```sh
docker compose ps
docker compose logs --tail=30 web
docker compose down
```

停止容器使用最后一条命令；再次运行部署脚本恢复。容器异常退出或服务器重启后自动启动（前提是 Docker 服务已启动），日志限制为 3 个 10 MB 文件。

## 从原部署迁移

如果旧部署占用了 8080，先选择其他端口验证容器，或停止旧站点后再使用原端口。不要停掉承载其他站点的 Nginx。需要继续使用 `/deltachat-guide/` 路径时，可在现有反向代理中去掉该前缀后转发至容器。

容器只提供静态网页，无数据库及运行时数据卷；重新构建会发布仓库里的 `dist` 文件。浏览器进度仍保存在访问者本地。NAS APK、中继、App Store 和 GitHub 等外部链接仍指向原有服务。

旧的 `deploy-cloud.sh` 为宿主机 Nginx 安装脚本，不用于容器部署；`deploy-server.sh` 保留用于 NAS 静态发布。
