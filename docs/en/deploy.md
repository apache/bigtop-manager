# Installation
## Package Deployment
```bash
# Extract the installation package (note correct extraction parameters)
tar zxvf apache-bigtop-manager-1.0.0-SNAPSHOT-server.tar.gz -C /opt

# Enter deployment directory
cd /opt/bigtop-manager-server
```

## Configuration
```bash
# Enter deployment directory
cd /opt/bigtop-manager-server

# Modify configuration file (replace all placeholders)
sed -e "s|org.postgresql.Driver|com.mysql.cj.jdbc.Driver|g" \
    -e "s|jdbc:postgresql://localhost:5432|jdbc:mysql://YOUR_MYSQL_IP:3306|g" \
    -e "s|username: postgres|username: YOUR_USER_NAME|g" \
    -e "s|password: postgres|password: YOUR_PASSWORD|g" \
    -i.bak conf/application.yml  # Automatically generate backup file
```

### Optional Service Port Configuration
```yaml
# Add to the end of conf/application.yml
server:
  port: 8080  # Modify to desired port, default is 8080
```

## Create MySQL User
```sql
-- Create database (execute the second line first if password policy needs adjustment)
CREATE DATABASE bigtop_manager;
-- SET GLOBAL validate_password_policy = LOW;  -- Temporarily lower password policy for testing environments

-- Create user (replace YOUR_USER_NAME/YOUR_IP/YOUR_PASSWORD)
CREATE USER 'YOUR_USER_NAME'@'YOUR_IP' IDENTIFIED BY 'YOUR_PASSWORD';

-- Grant privileges (recommend narrowing privileges based on requirements)
GRANT ALL PRIVILEGES ON bigtop_manager.* TO 'YOUR_USER_NAME'@'YOUR_IP';
FLUSH PRIVILEGES;
```

## Initialize Database
```bash
# Enter deployment directory
cd /opt/bigtop-manager-server

# Execute DDL script (note password parameter format)
mysql -h YOUR_MYSQL_IP -P 3306 -u YOUR_USER_NAME -pYOUR_PASSWORD < ddl/MySQL-DDL-CREATE.sql
```

## Download MySQL Driver
```bash
# Enter deployment directory
cd /opt/bigtop-manager-server

# Official repository
wget https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar -O libs/mysql-connector-j-8.0.33.jar

# For users in Mainland China (recommended Aliyun mirror)
wget https://maven.aliyun.com/repository/central/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar -O libs/mysql-connector-j-8.0.33.jar
```

## Start Service
```bash
# Enter deployment directory
cd /opt/bigtop-manager-server

# Start service
./bin/server.sh start

# Or run in background
nohup bin/server.sh start > /dev/null 2>&1 &
```

## 7. Admin Page
* Url: `http://YOUR_IP:8080/`
* Username: `admin`  
* Password: `admin`