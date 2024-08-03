From alpine:latest

LABEL vendor="Innospot Reserved."


RUN echo "https://mirrors.ustc.edu.cn/alpine/latest-stable/community" > /etc/apk/repositories
RUN echo "https://mirrors.ustc.edu.cn/alpine/latest-stable/main" >> /etc/apk/repositories
RUN echo "https://mirrors.ustc.edu.cn/alpine/edge/testing" >> /etc/apk/repositories

RUN apk update && \
    apk add openjdk21 nmon curl busybox tzdata wget busybox-extras iproute2 ethtool && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo Asia/Shanghai > /etc/timezone && \
    apk del tzdata && \
    rm -rf /tmp/* /var/cache/apk/* \
    mkdir /innospots

ENV JAVA_HOME=/usr/lib/jvm/default-jvm

ENV CLASSPATH=$JAVA_HOME/lib \
    PATH=$JAVA_HOME/bin:$PATH \
    LC_ALL=zh_CN.UTF-8 \
    LANG=zh_CN.UTF-8 \
    LANGUAGE=zh_CN.UTF-8 \
    TERM=xterm-256color

WORKDIR /innospots

COPY app_entrypoint.sh /innospots/bin/app_entrypoint.sh