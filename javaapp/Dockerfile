FROM openjdk:8-jre-slim
MAINTAINER husen <1178515826@qq.com>
WORKDIR /data/app
# 设置时区
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
# 安装netcat
RUN mkdir -p /etc/apt
RUN touch /etc/apt/source.list
RUN echo "deb-src http://archive.ubuntu.com/ubuntu xenial main restricted #Added by software-properties" >> /etc/apt/source.list
RUN echo "deb http://mirrors.aliyun.com/ubuntu/ xenial main restricted" >> /etc/apt/source.list
RUN echo "deb-src http://mirrors.aliyun.com/ubuntu/ xenial main restricted multiverse universe #Added by software-properties" >> /etc/apt/source.list
RUN echo "deb http://mirrors.aliyun.com/ubuntu/ xenial-updates main restricted" >> /etc/apt/source.list
RUN echo "deb-src http://mirrors.aliyun.com/ubuntu/ xenial-updates main restricted multiverse universe #Added by software-properties" >> /etc/apt/source.list
RUN echo "deb http://mirrors.aliyun.com/ubuntu/ xenial universe" >> /etc/apt/source.list
RUN echo "deb http://mirrors.aliyun.com/ubuntu/ xenial-updates universe" >> /etc/apt/source.list
RUN echo "deb http://mirrors.aliyun.com/ubuntu/ xenial multiverse" >> /etc/apt/source.list
RUN echo "deb http://mirrors.aliyun.com/ubuntu/ xenial-updates multiverse" >> /etc/apt/source.list
RUN echo "deb http://mirrors.aliyun.com/ubuntu/ xenial-backports main restricted universe multiverse" >> /etc/apt/source.list
RUN echo "deb-src http://mirrors.aliyun.com/ubuntu/ xenial-backports main restricted universe multiverse #Added by software-properties" >> /etc/apt/source.list
RUN echo "deb http://archive.canonical.com/ubuntu xenial partner" >> /etc/apt/source.list
RUN echo "deb-src http://archive.canonical.com/ubuntu xenial partner" >> /etc/apt/source.list
RUN echo "deb http://mirrors.aliyun.com/ubuntu/ xenial-security main restricted" >> /etc/apt/source.list
RUN echo "deb-src http://mirrors.aliyun.com/ubuntu/ xenial-security main restricted multiverse universe #Added by software-properties" >> /etc/apt/source.list
RUN echo "deb http://mirrors.aliyun.com/ubuntu/ xenial-security universe" >> /etc/apt/source.list
RUN echo "deb http://mirrors.aliyun.com/ubuntu/ xenial-security multiverse" >> /etc/apt/source.list
RUN apt-get update && apt-get install netcat iputils-ping -y
