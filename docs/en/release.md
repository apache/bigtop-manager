# Release
This document is written based on the release of Bigtop Manager 1.0.0.

## Prepare
If this is your first release, you need to do the following preparation work.

### GPG Key
The GPG key is used to sign your code during the release stage. You can check [Signing Releases](https://infra.apache.org/release-signing.html) or follow the instructions below to create your GPG key information.

#### Download the Software
Download and install the GPG software package from the [official website](https://www.gnupg.org/download/index.html). It is recommended to download the latest version. The following example is written based on version `2.4.7`.

#### Create the Key
```shell
$ gpg --full-gen-key

gpg (GnuPG) 2.4.7; Copyright (C) 2024 g10 Code GmbH
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

# Choose key info. Just fill in 1/4096/0/y in order.
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

# Fill in your personal information. It is recommended to fill in your Apache account information and then enter O to confirm.
GnuPG needs to construct a user ID to identify your key.

Real name: Zhiguo Wu
Email address: wuzhiguo@apache.org
Comment: CODE SIGNING KEY
You selected this USER-ID:
    "Zhiguo Wu (CODE SIGNING KEY) <wuzhiguo@apache.org>"

Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O

We need to generate a lot of random bytes. It is a good idea to perform some other action (type on the keyboard, move the mouse, utilize the disks) during the prime generation; this gives the random number generator a better chance to gain enough entropy.

# The key is successfully created. Please save it properly.
gpg: directory '/Users/wuzhiguo/.gnupg/openpgp-revocs.d' created
gpg: revocation certificate stored as '/Users/wuzhiguo/.gnupg/openpgp-revocs.d/D3D5CF25076873C9AA068C0D8F062A5450E25685.rev'
public and secret key created and signed.

pub   rsa4096 2023-02-21 [SC]
      D3D5CF25076873C9AA068C0D8F062A5450E25685
uid           [ultimate] Zhiguo Wu (CODE SIGNING KEY) <wuzhiguo@apache.org>
sub   rsa4096 2023-02-21 [E]
```

#### View the Key
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

#### Upload the Key
Visit the [public server](http://keyserver.ubuntu.com/) to upload the key or run the following command to upload it:
```shell
$ gpg --keyserver keyserver.ubuntu.com --send-key D3D5CF25076873C9AA068C0D8F062A5450E25685
```

#### View the Key
Visit the [public server](http://keyserver.ubuntu.com/) to search for your key or run the following command to view the upload result:
```shell
$ gpg --keyserver keyserver.ubuntu.com --search-keys D3D5CF25076873C9AA068C0D8F062A5450E25685
```

### SVN
Apache uses SVN to manage release packages.

#### Pull the Directory
```bash
$ mkdir -p ~/apache/dev
$ mkdir -p ~/apache/release
$ cd ~/apache/dev
$ svn co https://dist.apache.org/repos/dist/dev/bigtop
$ cd ~/apache/release
$ svn co https://dist.apache.org/repos/dist/release/bigtop
```

#### Upload the Key
Please note to change the name below to the one you used when generating the key:
```bash
$ cd ~/apache/release/bigtop
$ (gpg --list-sigs Zhiguo Wu && gpg --armor --export Zhiguo Wu) >> KEYS
$ svn commit -m "Adding Zhiguo Wu's code signing key"
```

## Start the Release
### Release Notes
Since the project is managed by `Apache Jira`, you can easily obtain the corresponding release notes.
* Go to the [Bigtop Jira](https://issues.apache.org/jira/projects/BIGTOP) page.
* Click the `Release` menu on the left.
* Click the version you want to release.
* There will be a `Release Notes` page at the top of the page. Just click to open it.

Before generating release notes, you should review if all JIRA issues addressed in this release have the correct `fixVersion` and unrelated issues don't have that `fixVersion`.

Now we have obtained the release notes page: https://issues.apache.org/jira/secure/ReleaseNote.jspa?projectId=12311420&version=12354831

### Upload Dependencies
Upload the Bigtop/Infra/Extra Stack service packages and the corresponding Tools packages to `repos.bigtop.apache.org` and obtain the corresponding URL: http://repos.bigtop.apache.org/releases/bigtop-manager/1.0.0

### Update the DDL Files
Update the DDL files for `MySQL` and `PostgreSQL` in the `bigtop-manager-server/src/main/resources/ddl` directory. Change the following content:
```sql
INSERT INTO repo (name, arch, base_url, type)
VALUES
('Service tarballs', 'x86_64', 'http://your-repo/'),
('Service tarballs', 'aarch64', 'http://your-repo/'),
```
to
```sql
INSERT INTO repo (name, arch, base_url, type)
VALUES
('Service tarballs', 'x86_64', 'http://repos.bigtop.apache.org/releases/bigtop-manager/1.0.0/'),
('Service tarballs', 'aarch64', 'http://repos.bigtop.apache.org/releases/bigtop-manager/1.0.0/'),
```

### Upgrade the Version Number
Upgrade the version number and create the corresponding branch and tag. Note that the following ISSUE number is usually the ISSUE number corresponding to the roadmap:
```shell
$ git checkout -b branch-1.0
$ mvn versions:set-property -Dproperty=revision -DnewVersion=1.0.0 -DgenerateBackupPoms=false
$ git commit -m "BIGTOP-4129: Preparing for release 1.0.0"
$ git tag release-1.0.0-rc0 -am "Bigtop Manager 1.0.0 RC0"

# Push directly to the Apache repository. Be cautious when operating.
$ git push upstream branch-1.0
$ git push upstream release-1.0.0-rc0
```

### Obtain the Source Package and Sign
Compile the code:
```shell
$ mvn clean install -DskipTests
```
After the compilation is completed, you can obtain the source package from `bigtop-manager-dist/target/apache-bigtop-manager-1.0.0-src.tar.gz`

Sign it:
```shell
$ gpg --armor --output apache-bigtop-manager-1.0.0-src.tar.gz.asc --detach-sig apache-bigtop-manager-1.0.0-src.tar.gz
$ sha512sum apache-bigtop-manager-1.0.0-src.tar.gz > apache-bigtop-manager-1.0.0-src.tar.gz.sha512
```

### Submit the Source Package and the Signature
Submit the three files to the SVN Dev repository:
```shell
$ mkdir -p ~/apache/dev/bigtop/bigtop-manager-1.0.0-RC0
$ mv apache-bigtop-manager-1.0.0-src* ~/apache/dev/bigtop/bigtop-manager-1.0.0-RC0/
$ cd ~/apache/dev/bigtop/
$ svn add bigtop-manager-1.0.0-RC0
$ svn commit -m "Preparing Release Bigtop Manager 1.0.0 RC0"
```

### Initiate a Vote
Initiate a vote according to the [VOTE](#vote) template
* If the vote is failed, rerun the above steps after making the required modifications. At the same time, increment the Release Candidate by +1. If according to the above example, the next Release Candidate will be RC1.
* If you like to cancel a vote due to some issue, you can use [CANCEL](#cancel) template.
* If the vote is passed, proceed to the [Vote Passes](#vote-passes) operation.

## Vote Passes
### Result Notification
If the release vote is passed, you need to send the voting result to the mailing list. Refer to the [RESULT](#result) template

### Submit the Source Package and the Signature
Move the source package and the signature file to the SVN Release repository:
```shell
$ cd ~/apache/dev/bigtop/
$ mkdir -p ~/apache/release/bigtop/bigtop-manager-1.0.0
$ mv bigtop-manager-1.0.0-RC0/apache-bigtop-manager-1.0.0-src* ~/apache/release/bigtop/bigtop-manager-1.0.0/

# Delete the Release Candidate directory under Dev
$ svn delete bigtop-manager-1.0.0-RC0
$ svn commit -m "Removing Release Bigtop Manager 1.0.0 RC0"

# Submit to the official Release repository
$ cd ~/apache/release/bigtop/
$ svn add bigtop-manager-1.0.0
$ svn commit -m "Committing Release Bigtop Manager 1.0.0"
```

### Update the Git Repository
Submit the corresponding tag to the remote repository. The corresponding commit hash is usually the same as that of the Release Candidate that passed the vote:
```shell
$ git tag release-1.0.0 -am 'Bigtop Manager 1.0.0'
$ git push upstream release-1.0.0
```

### Upgrade the Version Number
Upgrade the version number of the corresponding branch:
```shell
$ git checkout branch-1.0
$ mvn versions:set-property -Dproperty=revision -DnewVersion=1.0.1-SNAPSHOT -DgenerateBackupPoms=false
$ git commit -m "BIGTOP-4129: Bump version to 1.0.1-SNAPSHOT"
$ git push upstream
```

Upgrade the version number of the main branch. Here, you need to submit a PR instead of directly pushing:
```shell
$ git checkout main
$ mvn versions:set-property -Dproperty=revision -DnewVersion=1.1.0-SNAPSHOT -DgenerateBackupPoms=false
$ git commit -m "BIGTOP-4129: Bump version to 1.1.0-SNAPSHOT"
```

Note that the above two ISSUE numbers are still the ISSUE numbers corresponding to the roadmap.

### Email Announcement
So far, you have completed all the operations required for the release. Next, you just need to send an email announcement according to the [ANNOUNCE](#announce) template.

### Release Completed
Congratulations! You have completed the release process!

## Verification
In addition to performing the release operation, we also need to know how to verify other people's releases. There are many aspects to verify. Here we only select a few to illustrate. For detailed information, please refer to the [Release Policy](https://www.apache.org/legal/release-policy.html)

### Verify the Signature
Verify whether the signature is correct through the following commands:
```shell
$ curl https://downloads.apache.org/bigtop/KEYS -o KEYS.bigtop
$ gpg --import KEYS.bigtop
$ gpg --verify apache-bigtop-manager-1.0.0-src.tar.gz.asc
$ sha512sum --check apache-bigtop-manager-1.0.0-src.tar.gz.sha512
```

### License Verification
Usually, the license has been verified in the CI. However, just in case, we still need to verify the source package.

Our project uses `skywalking-eyes` instead of `apache-rat-plugin` for license management. Please first install the corresponding dependencies locally according to the [document](https://github.com/apache/skywalking-eyes)

Run the following commands to verify the license:
```shell
# Verify the License Header
license-eye -c.licenserc.yaml header check

# Verify Dependencies' License
license-eye -c.licenserc.yaml dep check
```

### Other Verifications
Other optional simple verifications include:
* Check whether the hash of the tag in the email is correct.
* Check whether the compilation `mvn clean install` passes.
* Check whether all the links in the email are valid.

## Templates
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