# 发版
本文档案例基于 Bigtop Manager 1.0.0 发版编写

## 准备工作
如果这是您的第一次发版，您需要做以下准备工作

### GPG 密钥
GPG 密钥是发版阶段用来对您的代码进行签名的，您可以可查看 [Signing Releases](https://infra.apache.org/release-signing.html) 或者跟随下面的指引来创建您的 GPG 密钥信息

#### 下载软件
从 [官方网站](https://www.gnupg.org/download/index.html) 下载并安装 GPG 软件包，推荐下载最新版本，下面的案例基于版本 `2.4.7` 编写

#### 创建密钥
```shell
$ gpg --full-gen-key

gpg (GnuPG) 2.4.7; Copyright (C) 2024 g10 Code GmbH
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

# 此处让您选择密钥类型等信息，按序填入 1/4096/0/y 即可
Please select what kind of key you want:
   (1) RSA and RSA
   (2) DSA and Elgamal
   (3) DSA (sign only)
   (4) RSA (sign only)
   (9) ECC (sign and encrypt) *default*
  (10) ECC (sign only)
  (14) Existing key from card
Your selection? 1
RSA keys may be between 1024 and 4096 bits long.
What keysize do you want? (3072) 4096
Requested keysize is 4096 bits
Please specify how long the key should be valid.
         0 = key does not expire
      <n>  = key expires in n days
      <n>w = key expires in n weeks
      <n>m = key expires in n months
      <n>y = key expires in n years
Key is valid for? (0) 0
Key does not expire at all
Is this correct? (y/N) y

# 此处让您填写个人信息，推荐填写 Apache 账号信息后输入 O 确认即可
GnuPG needs to construct a user ID to identify your key.

Real name: Zhiguo Wu
Email address: wuzhiguo@apache.org
Comment: CODE SIGNING KEY
You selected this USER-ID:
    "Zhiguo Wu (CODE SIGNING KEY) <wuzhiguo@apache.org>"

Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O

We need to generate a lot of random bytes. It is a good idea to perform some other action (type on the keyboard, move the mouse, utilize the disks) during the prime generation; this gives the random number generator a better chance to gain enough entropy.

# 创建成功，请妥善保存
gpg: directory '/Users/wuzhiguo/.gnupg/openpgp-revocs.d' created
gpg: revocation certificate stored as '/Users/wuzhiguo/.gnupg/openpgp-revocs.d/D3D5CF25076873C9AA068C0D8F062A5450E25685.rev'
public and secret key created and signed.

pub   rsa4096 2023-02-21 [SC]
      D3D5CF25076873C9AA068C0D8F062A5450E25685
uid           [ultimate] Zhiguo Wu (CODE SIGNING KEY) <wuzhiguo@apache.org>
sub   rsa4096 2023-02-21 [E]
```

#### 查看密钥
```shell
$ gpg --list-keys

gpg: checking the trustdb
gpg: marginals needed: 3  completes needed: 1  trust model: pgp
gpg: depth: 0  valid:   1  signed:   0  trust: 0-, 0q, 0n, 0m, 0f, 1u
[keyboxd]
---------
pub   rsa4096 2023-02-21 [SC]
      D3D5CF25076873C9AA068C0D8F062A5450E25685
uid           [ultimate] Zhiguo Wu (CODE SIGNING KEY) <wuzhiguo@apache.org>
sub   rsa4096 2023-02-21 [E]
```

#### 上传密钥
访问 [公共服务器](http://keyserver.ubuntu.com/) 上传密钥或者运行以下命令上传：
```shell
$ gpg --keyserver keyserver.ubuntu.com --send-key D3D5CF25076873C9AA068C0D8F062A5450E25685
```

#### 查看密钥
访问 [公共服务器](http://keyserver.ubuntu.com/) 搜索您的密钥或者运行以下命令查看上传结果：
```shell
$ gpg --keyserver keyserver.ubuntu.com --search-keys D3D5CF25076873C9AA068C0D8F062A5450E25685
```

### SVN
Apache 使用 SVN 来管理 Release 软件包

#### 拉取目录
```bash
$ mkdir -p ~/apache/dev
$ mkdir -p ~/apache/release
$ cd ~/apache/dev
$ svn co https://dist.apache.org/repos/dist/dev/bigtop
$ cd ~/apache/release
$ svn co https://dist.apache.org/repos/dist/release/bigtop
```

#### 上传密钥
请注意将下面的名字改为您自己生成密钥时使用的名字：
```bash
$ cd ~/apache/release/bigtop
$ (gpg --list-sigs Zhiguo Wu && gpg --armor --export Zhiguo Wu) >> KEYS
$ svn commit -m "Adding Zhiguo Wu's code signing key"
```

## 开始发版
### Release Notes
由于项目由 `Apache Jira` 管理，您可以很简单的获取对应的 Release Notes
* 进入 [Bigtop Jira](https://issues.apache.org/jira/projects/BIGTOP) 页面
* 点击左侧的 `Release` 菜单
* 点击您想要发布的版本
* 页面上方会有 `Release Notes` 页面，点开即可

在生成 Release Notes 之前，您需要检查所有 JIRA ISSUE 是否都已修复并且 `fixVersion` 是正确的，且相应的 `fixVersion` 下没有不相关的 ISSUE

现在我们获得了 Release Notes：https://issues.apache.org/jira/secure/ReleaseNote.jspa?projectId=12311420&version=12354831

### 上传依赖包
将 Bigtop/Infra/Extra Stack 服务软件包及对应的 Tools 软件包上传至 `repos.bigtop.apache.org`，获取对应的 URL：http://repos.bigtop.apache.org/releases/bigtop-manager/1.0.0

### 更新 DDL 文件
更新 `bigtop-manager-server/src/main/resources/ddl` 目录下的 `MySQL` 和 `PostgreSQL` 的 DDL 文件，将以下内容
```sql
INSERT INTO repo (name, arch, base_url, type)
VALUES
('Service tarballs', 'x86_64', 'http://your-repo/'),
('Service tarballs', 'aarch64', 'http://your-repo/'),
```
改为
```sql
INSERT INTO repo (name, arch, base_url, type)
VALUES
('Service tarballs', 'x86_64', 'http://repos.bigtop.apache.org/releases/bigtop-manager/1.0.0/'),
('Service tarballs', 'aarch64', 'http://repos.bigtop.apache.org/releases/bigtop-manager/1.0.0/'),
```

### 升级版本号
升级版本号并创建对应的分支和 Tag，注意下面的 ISSUE 编号通常为 Roadmap 对应的 ISSUE 编号：
```shell
$ git checkout -b branch-1.0
$ mvn versions:set-property -Dproperty=revision -DnewVersion=1.0.0 -DgenerateBackupPoms=false
$ git commit -m "BIGTOP-4129: Preparing for release 1.0.0"
$ git tag release-1.0.0-rc0 -am "Bigtop Manager 1.0.0 RC0"

# 直接推送至 Apache 仓库，操作时需谨慎
$ git push upstream branch-1.0
$ git push upstream release-1.0.0-rc0
```

### 获取源码包并签名
编译代码：
```shell
$ mvn clean install -DskipTests
```
编译完成后您便可从 `bigtop-manager-dist/target/apache-bigtop-manager-1.0.0-src.tar.gz` 获得源码包

签名：
```shell
$ gpg --armor --output apache-bigtop-manager-1.0.0-src.tar.gz.asc --detach-sig apache-bigtop-manager-1.0.0-src.tar.gz
$ sha512sum apache-bigtop-manager-1.0.0-src.tar.gz > apache-bigtop-manager-1.0.0-src.tar.gz.sha512
```

### 提交源码包及签名
将三个文件同时提交至 SVN 的 Dev 仓库中
```shell
$ mkdir -p ~/apache/dev/bigtop/bigtop-manager-1.0.0-RC0
$ mv apache-bigtop-manager-1.0.0-src* ~/apache/dev/bigtop/bigtop-manager-1.0.0-RC0/
$ cd ~/apache/dev/bigtop/
$ svn add bigtop-manager-1.0.0-RC0
$ svn commit -m "Preparing Release Bigtop Manager 1.0.0 RC0"
```

### 发起投票
根据 [VOTE 邮件模板](#vote) 发起投票
* 若投票未通过，则按要求修改完成后重复以上步骤，同时 Release Candidate +1，若根据以上案例，下一个 Release Candidate 即为 RC1
* 若由于某些问题你想要取消投票，则可以参考 [CANCEL 邮件模板](#cancel) 来取消
* 若投票通过，进入 [发版成功](#发版成功) 操作

## 发版成功
### 结果通知
若发版投票通过，您需要发送一封投票结果至邮件列表中，参考 [RESULT 邮件模板](#result)

### 提交源码包及签名
将源码包及签名文件移至 SVN 的 Release 仓库
```shell
$ cd ~/apache/dev/bigtop/
$ mkdir -p ~/apache/release/bigtop/bigtop-manager-1.0.0
$ mv bigtop-manager-1.0.0-RC0/apache-bigtop-manager-1.0.0-src* ~/apache/release/bigtop/bigtop-manager-1.0.0/

# 删除 Dev 下的 Release Candidate 目录
$ svn delete bigtop-manager-1.0.0-RC0
$ svn commit -m "Removing Release Bigtop Manager 1.0.0 RC0"

# 提交至正式 Release 仓库
$ cd ~/apache/release/bigtop/
$ svn add bigtop-manager-1.0.0
$ svn commit -m "Committing Release Bigtop Manager 1.0.0"
```

### 更新 Git 仓库
提交对应的 Tag 至远程仓库中，对应的 Commit Hash 通常与通过投票的 Release Candidate 一致
```shell
$ git tag release-1.0.0 -am 'Bigtop Manager 1.0.0'
$ git push upstream release-1.0.0
```

### 升级版本号
升级对应 Release 分支的版本号：
```shell
$ git checkout branch-1.0
$ mvn versions:set-property -Dproperty=revision -DnewVersion=1.0.1-SNAPSHOT -DgenerateBackupPoms=false
$ git commit -m "BIGTOP-4129: Bump version to 1.0.1-SNAPSHOT"
$ git push upstream
```

升级 main 分支的版本号，此处需提交 PR 而不能直接 Push：
```shell
$ git checkout main
$ mvn versions:set-property -Dproperty=revision -DnewVersion=1.1.0-SNAPSHOT -DgenerateBackupPoms=false
$ git commit -m "BIGTOP-4129: Bump version to 1.1.0-SNAPSHOT"
```

注意上面两个 ISSUE 编号依然是 Roadmap 对应的 ISSUE 编号

### 邮件公告
到此为止，您已经完成了所有发版所需要的操作，接下来只需要根据 [ANNOUNCE 邮件模板](#announce) 来发一封邮件通知即可！

### 完成发版
恭喜您，您已经完成了发版！

## 发版验证
我们除了操作发版之外，也需要懂得如何验证别人的发版，需要验证的地方很多，此处我们只挑选几个说明，详细信息请查阅 [Release Policy](https://www.apache.org/legal/release-policy.html)

### 验证签名
通过如下命令来验证签名是否正确：
```shell
$ curl https://downloads.apache.org/bigtop/KEYS -o KEYS.bigtop
$ gpg --import KEYS.bigtop
$ gpg --verify apache-bigtop-manager-1.0.0-src.tar.gz.asc
$ sha512sum --check apache-bigtop-manager-1.0.0-src.tar.gz.sha512
```

### License 校验
通常 License 已经在 CI 中校验过，不过为了以防万一我们还是需要对源码包进行校验

我们的项目用的不是 `apache-rat-plugin` 而是 `skywalking-eyes` 来进行 License 管理，请先根据 [文档](https://github.com/apache/skywalking-eyes) 在本地安装对应依赖

运行如下命令来校验 License：
```shell
# 校验 License Header
license-eye -c .licenserc.yaml header check

# 校验 Dependencies' License
license-eye -c .licenserc.yaml dep check
```

### 其他校验
其他可选的简单校验包括：
* 检查邮件中的 Tag Hash 是否正确
* 检查编译 `mvn clean install` 是否通过
* 检查邮件中的链接是否都可用

## 邮件模板
### VOTE
```
To: "Bigtop Developers List" <dev@bigtop.apache.org>
Subject: [VOTE] Release Apache Bigtop Manager 1.0.0 RC0

Hello Community,

This is a call for a vote to release Apache Bigtop Manager 1.0.0 RC0

Release note:
https://issues.apache.org/jira/secure/ReleaseNote.jspa?projectId=12311420&version=12354831

The release candidates:
https://dist.apache.org/repos/dist/dev/bigtop/bigtop-manager-1.0.0-RC0/

Git tag for the release:
https://github.com/apache/bigtop-manager/tree/release-1.0.0-rc0

Hash for the release tag:
2ea1fd91062a52b983210e38f50667ce11b8ed23

The artifacts have been signed with Key D3D5CF25076873C9AA068C0D8F062A5450E25685, which can be found in the KEYS file:
https://dist.apache.org/repos/dist/release/bigtop/KEYS

The vote will be open for at least 72 hours or until the necessary number of votes is reached.

Please vote accordingly:

[ ] +1 approve
[ ] +0 no opinion
[ ] -1 disapprove with the reason

Best Regards,
Zhiguo Wu
```

### CANCEL
```
To: "Bigtop Developers List" <dev@bigtop.apache.org>
Subject: [CANCEL] [VOTE] Release Apache Bigtop Manager 1.0.0 RC0

Hello Community,

Due to some license issues, I'd like to cancel the vote for release Apache Bigtop Manager 1.0.0 RC0, the next vote for RC1 will be sent out in a few days.

Best Regards,
Zhiguo Wu
```

### RESULT
```
To: "Bigtop Developers List" <dev@bigtop.apache.org>
Subject: [RESULT] [VOTE] Release Apache Bigtop Manager 1.0.0 RC0

Hello Community,

The release vote finished, We’ve received

Binding +1s:
Zhiguo Wu

Non-binding +1s:
Zhiguo Wu

The vote and result thread:
https://lists.apache.org/thread/q7j77x3p9qo20wrd41mq7pnzf53vgmq2
The vote passed. I am working on the further release process, thanks.

Best regards,
Zhiguo Wu
```

### ANNOUNCE
```
To: "Bigtop Developers List" <dev@bigtop.apache.org>
Subject: [ANNOUNCE] Apache Bigtop Manager 1.0.0 Released

Hello Community,

I am glad to announce that Apache Bigtop Manager 1.0.0 has been released.

The source release can be downloaded here:
https://www.apache.org/dyn/closer.cgi/bigtop/bigtop-manager-1.0.0/

Detailed release notes can be checked here:
https://issues.apache.org/jira/secure/ReleaseNote.jspa?projectId=12311420&version=12354831

Thank you to all the contributors that made the release possible.

Best regards,
Zhiguo Wu
```