# 云服务器一键部署

适用于 Ubuntu / Debian，使用 Nginx 托管引导网站，无需 Docker。

将同目录的 `deploy-cloud.sh` 上传到服务器，在服务器执行：

```sh
sudo sh deploy-cloud.sh
```

浏览器访问 `http://服务器公网IP:8080/`。在云平台安全组及服务器防火墙中放行 TCP 8080。脚本不会自动修改防火墙。

自定义端口：

```sh
sudo env PORT=8090 sh deploy-cloud.sh
```

再次运行会下载 GitHub main 分支并更新网站。默认目录为 `/var/www/deltachat-guide`，Nginx 配置为 `/etc/nginx/conf.d/deltachat-guide.conf`。使用的端口应未被其他服务占用。

默认提供 HTTP；HTTPS 需配置自己的域名和证书。网页中的 NAS 下载链接和账号服务仍使用原地址。
