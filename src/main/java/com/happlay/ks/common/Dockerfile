# 基础镜像
FROM oraclelinux:7

# 安装依赖
RUN yum install -y wget tar && \
    yum clean all

# 下载并安装 Oracle OpenJDK 1.8.0_321
RUN wget -q --no-cookies --no-check-certificate \
    --header "Cookie: oraclelicense=accept-securebackup-cookie" \
    "https://download.oracle.com/otn/java/jdk/8u321-b31/jdk-8u321-linux-x64.tar.gz" -O /tmp/jdk-8u321-linux-x64.tar.gz && \
    tar -xzf /tmp/jdk-8u321-linux-x64.tar.gz -C /opt && \
    rm /tmp/jdk-8u321-linux-x64.tar.gz && \
    ln -s /opt/jdk1.8.0_321 /opt/jdk

# 设置环境变量
ENV JAVA_HOME /opt/jdk
ENV PATH $JAVA_HOME/bin:$PATH

# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 拷贝jar包
COPY docker-demo.jar /app.jar

# 入口
ENTRYPOINT ["java", "-jar", "/app.jar"]