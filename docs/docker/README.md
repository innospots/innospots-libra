## Docker File

- 构建基础镜像，执行命令，在当前dockerfile目录下执行

```shell script
docker build -t innospots-base:latest . -f base.dockerfile
```


- 执行完成后查看镜像，查看是否有innospots-base镜像文件

```shell script
docker images
```


- 删除镜像

```shell script
docker rmi IMAGE ID
```

示例：docker rmi 30bb2eb88b1c

如果有运行中的容器和依赖则不能删除


```shell script
docker login -u innospot_dev -p Deployer1234 http://hbr.innospot.live
docker push innospots-base
```


### docker run 命令参数

- -d：容器后台运行
- -p: 执行端口映射，例如：-p 8686:8686
- --restart always , 退出重启
- --rm 启动退出后，清除容器运行container，和restart alyways不能同时使用
- --name 运行的容器名称
- -i交互执行
- -t分配虚拟终端
- -e 指定环境参数， -e CONSUL_HOST="127.0.0.1"

> 示例命令

```shell script
$ docker run -d --name=centos -e SERVER_PORT=80 --env APP_NAME=pkslow centos:7
```

进入容器命令，屏蔽容器默认entrypoint命令

```shell script
docker run --entrypoint=/bin/sh  -it innospots-base
```