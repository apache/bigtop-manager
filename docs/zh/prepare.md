# 环境准备
## 操作系统准备
### 系统要求
支持架构
* `x86_64`
* `aarch64`

已验证发行版
* `Rocky Linux 8.10`
* `Anolis OS 8.10`
* `openEuler 24.03`

文件系统
* `ext4` 
* `xfs`

## JDK 版本
`JDK` 版本需要`≥ 17`，例如 `JDK17` 或者 `JDK21`，建议使用LTS版本。

## 数据库选型
支持的数据库
* `MySQL`
* `PostgreSQL`

推荐版本
* `MySQL` 8.0+
* `PostgreSQL` 16.4+

## 项目构建
```bash
git clone https://github.com/apache/bigtop-manager.git
cd bigtop-manager
mvn clean package -DskipTests
```

## 系统安全配置
### 检测和关闭系统防火墙
如果发现端口不通，可以试着关闭防火墙，确认是否是本机防火墙造成。如果是防火墙造成，可以根据配置的 bigtop-manager 各组件端口打开相应的端口通信。

```bash
sudo systemctl stop firewalld.service
sudo systemctl disable firewalld.service
```

### 配置时间同步服务
所有集群机器要进行时钟同步，避免因为时钟问题引发的元数据不一致导致服务出现异常。

#### NTP
```bash
sudo systemctl start ntpd.service
sudo systemctl enable ntpd.service
```

#### Chrony
```bash
sudo systemctl start chronyd.service
sudo systemctl enable chronyd.service
```

## Hosts 配置
```bash
# 设置主机名
hostnamectl set-hostname your-host-name

# 统一集群 Hosts
echo "10.10.0.101 bm1" | sudo tee -a /etc/hosts
echo "10.10.0.102 bm2" | sudo tee -a /etc/hosts
echo "10.10.0.103 bm3" | sudo tee -a /etc/hosts
```

## 系统参数调优
### 资源限制配置
```bash
cat >> /etc/security/limits.conf << EOF
*            soft    fsize           unlimited
*            hard    fsize           unlimited
*            soft    cpu             unlimited
*            hard    cpu             unlimited
*            soft    as              unlimited
*            hard    as              unlimited
*            soft    nofile          1048576
*            hard    nofile          1048576
*            soft    nproc           unlimited
*            hard    nproc           unlimited
EOF
```

验证
```base
ulimit -a
```

### 内存参数优化
#### 禁用透明大页
```bash
echo 'never' > /sys/kernel/mm/transparent_hugepage/enabled
echo 'never' > /sys/kernel/mm/transparent_hugepage/defrag
```

#### 调整swappiness
```bash
sysctl vm.swappiness=1
swapoff -a
```

#### 验证参数
```bash
cat /sys/kernel/mm/transparent_hugepage/enabled
cat /sys/kernel/mm/transparent_hugepage/defrag

free -h | grep Swap
sysctl vm.swappiness
```

#### 配置持久化
```bash
# 关闭透明大页
cat >> /etc/rc.d/rc.local << EOF
if test -f /sys/kernel/mm/transparent_hugepage/enabled; then
  echo never > /sys/kernel/mm/transparent_hugepage/enabled
fi
if test -f /sys/kernel/mm/transparent_hugepage/defrag; then
  echo never > /sys/kernel/mm/transparent_hugepage/defrag
fi
EOF
chmod +x /etc/rc.d/rc.local

# 设置 swappiness
echo 'vm.swappiness=1' | sudo tee -a /etc/sysctl.conf
```

## SSH 配置
### 生成密钥
生成ssh秘钥后分发秘钥，也可以在安装后在管理界面配置账密连接。

```bash
# 生成密钥（在主节点上运行一次）
ssh-keygen -N '' -t rsa -b 2048 -f /etc/ssh/ssh_host_rsa_key
ssh-keygen -N '' -t ecdsa -b 256 -f /etc/ssh/ssh_host_ecdsa_key
ssh-keygen -N '' -t ed25519 -b 256 -f /etc/ssh/ssh_host_ed25519_key
```

### 分发密钥(可选)
```bash
# 替换YOU_KEY.pub为你上方生成的
for node in bm{1..3}; do
  ssh-copy-id -i ~/.ssh/YOU_KEY.pub $node
done
```