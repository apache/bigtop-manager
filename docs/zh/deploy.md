# 安装与启动
## 安装包部署
```bash
# 解压安装包（注意正确解压参数）
tar zxvf apache-bigtop-manager-1.0.0-SNAPSHOT-server.tar.gz -C /opt

# 进入部署目录
cd /opt/bigtop-manager-server
```

## 配置文件修改
```bash
# 进入部署目录
cd /opt/bigtop-manager-server

# 修改配置文件（替换所有占位符）
sed -e "s|org.postgresql.Driver|com.mysql.cj.jdbc.Driver|g" \
    -e "s|jdbc:postgresql://localhost:5432|jdbc:mysql://YOUR_MYSQL_IP:3306|g" \
    -e "s|username: postgres|username: YOUR_USER_NAME|g" \
    -e "s|password: postgres|password: YOUR_PASSWORD|g" \
    -i.bak conf/application.yml  # 自动生成备份文件
```

### 服务端口配置（可选）
```yaml
# conf/application.yml 末尾添加
server:
  port: 8080  # 修改为实际需要的端口，默认为8080
```

## 创建MySQL用户及授权
```sql
-- 创建数据库（若需降低密码策略可先执行第二行）
CREATE DATABASE bigtop_manager;
-- SET GLOBAL validate_password_policy = LOW;  -- 测试环境可临时降低密码策略

-- 创建用户（替换YOUR_USER_NAME/YOUR_IP/YOUR_PASSWORD）
CREATE USER 'YOUR_USER_NAME'@'YOUR_IP' IDENTIFIED BY 'YOUR_PASSWORD';

-- 授权（建议根据实际需求缩小权限范围）
GRANT ALL PRIVILEGES ON bigtop_manager.* TO 'YOUR_USER_NAME'@'YOUR_IP';
FLUSH PRIVILEGES;
```

## 数据库初始化
```bash
# 进入部署目录
cd /opt/bigtop-manager-server

# 执行DDL脚本（注意密码参数格式）
mysql -h YOUR_MYSQL_IP -P 3306 -u YOUR_USER_NAME -pYOUR_PASSWORD < ddl/MySQL-DDL-CREATE.sql
```

## 下载MySQL驱动
```bash
# 进入部署目录
cd /opt/bigtop-manager-server

# 或官方源
wget https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar -O libs/mysql-connector-j-8.0.33.jar

# 如果你在中国大陆（推荐阿里云镜像）
wget https://maven.aliyun.com/repository/central/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar -O libs/mysql-connector-j-8.0.33.jar
```

## 启动服务
```bash
# 进入部署目录
cd /opt/bigtop-manager-server

# 启动服务
./bin/server.sh start

# 或者在后台启动服务
nohup bin/server.sh start > /dev/null 2>&1 &
```

## 访问管理页面
* 地址：`http://YOUR_IP:8080/`
* 用户名：`admin`
* 密码：`admin`