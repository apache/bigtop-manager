
# Environment Preparation
## 1. Operating System Preparation
### 1.1 System Requirements
Supported Architectures
* x86_64
* aarch64
Verified Distributions
* Rocky Linux 8.10
* Anolis OS 8.10
* openEuler 22.03
Filesystems
* ext4
* xfs
## 2. JDK Version
Requires `JDK ≥ 17` (e.g., `JDK17` or `JDK21`). LTS releases are recommended.

## 3. Database Options
Supported Databases:
* MySQL
* MariaDB
* PostgreSQL
Recommended versions:
* MySQL 8.0+
* PostgreSQL 16.4+
## 4. Project Build

```bash
git clone https://github.com/apache/bigtop-manager.git
cd bigtop-manager
mvn clean package -DskipTests
```
## 5. System Security Configuration
### 5.1 Firewall Configuration
If port connectivity issues occur, temporarily disable the firewall to confirm whether it is blocking traffic. Re-enable specific ports for bigtop-manager components if needed.

```bash
sudo systemctl stop firewalld.service
sudo systemctl disable firewalld.service
```
### 5.2 Time Synchronization
Ensure clock synchronization across all cluster nodes to prevent metadata inconsistencies.

#### For NTP
```bash
sudo systemctl start ntpd.service
sudo systemctl enable ntpd.service
```

#### For Chrony
```bash
sudo systemctl start chronyd.service
sudo systemctl enable chronyd.service
```
## 6. Hostname & Hosts Configuration
### 6.1 Set Hostname

```bash
hostnamectl set-hostname your-host-name  # Replace with actual hostname (e.g., bm1)
```
### 6.2 Unified Cluster Hosts
###  Configure on all nodes
```bash
echo "10.10.0.101 bm1" | sudo tee -a /etc/hosts
echo "10.10.0.102 bm2" | sudo tee -a /etc/hosts
echo "10.10.0.103 bm3" | sudo tee -a /etc/hosts
```
## 7. System Tuning
### 7.1 Resource Limits

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

Verification
```bash
ulimit -a
```
### 7.2 Memory Optimization
#### Disable Transparent Huge Pages (THP)
```bash
echo 'never' | sudo tee /sys/kernel/mm/transparent_hugepage/enabled
echo 'never' | sudo tee /sys/kernel/mm/transparent_hugepage/defrag
```
#### Adjust Swappiness
```bash
sudo sysctl vm.swappiness=1
sudo swapoff -a  # Optional (disable swap)
```
#### Verification
```bash
cat /sys/kernel/mm/transparent_hugepage/enabled
cat /sys/kernel/mm/transparent_hugepage/defrag

free -h | grep Swap
sysctl vm.swappiness
```
#### Persistence Configuration
```bash
# THP persistence
sudo tee -a /etc/rc.d/rc.local << EOF
if test -f /sys/kernel/mm/transparent_hugepage/enabled; then
  echo never > /sys/kernel/mm/transparent_hugepage/enabled
fi
if test -f /sys/kernel/mm/transparent_hugepage/defrag; then
  echo never > /sys/kernel/mm/transparent_hugepage/defrag
fi
EOF
sudo chmod +x /etc/rc.d/rc.local

# Swappiness persistence
echo 'vm.swappiness=1' | sudo tee -a /etc/sysctl.conf
```
## 8. SSH Mutual Trust
### 8.1 Generate SSH Keys
```bash
# Generate keys (run once on the management node)
sudo ssh-keygen -N '' -t rsa -b 2048 -f /etc/ssh/ssh_host_rsa_key
sudo ssh-keygen -N '' -t ecdsa -b 256 -f /etc/ssh/ssh_host_ecdsa_key
sudo ssh-keygen -N '' -t ed25519 -b 256 -f /etc/ssh/ssh_host_ed25519_key
```

### 8.2 Distribute Public Keys (Optional)
```bash
# Replace YOU_KEY.pub with your actual public key
for node in bm{1..3}; do
  ssh-copy-id -i ~/.ssh/YOU_KEY.pub $node
done
```