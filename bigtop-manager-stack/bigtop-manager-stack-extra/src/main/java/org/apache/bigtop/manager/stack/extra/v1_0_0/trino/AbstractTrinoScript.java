package org.apache.bigtop.manager.stack.extra.v1_0_0.trino;

import lombok.extern.slf4j.Slf4j;
import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.grpc.pojo.ClusterInfo;
import org.apache.bigtop.manager.grpc.pojo.PackageInfo;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.spi.script.AbstractServerScript;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.TarballUtils;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import java.text.MessageFormat;
import java.util.Properties;

/**
 *
 * @author dyw770
 * @since 2026-02-25
 */
@Slf4j
public abstract class AbstractTrinoScript extends AbstractServerScript {

    @Override
    public ShellResult add(Params params) {
        Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_SKIP_LEVELS, "1");
        installJdk((TrinoParams) params);
        return super.add(params, properties);
    }

    private void installJdk(TrinoParams params) {
        log.info("Setting up trino jdk25...");
        ClusterInfo clusterInfo = LocalSettings.cluster();
        String dependenciesHome = clusterInfo.getRootDir() + "/dependencies";
        String user = System.getProperty("user.name");
        LinuxFileUtils.createDirectories(dependenciesHome, user, user, Constants.PERMISSION_755, true);

        String jdkHome = dependenciesHome + "/jdk25";
        RepoInfo repoInfo = LocalSettings.repo("jdk25");
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.setName(repoInfo.getPkgName());
        packageInfo.setChecksum(repoInfo.getChecksum());
        TarballUtils.installPackage(repoInfo.getBaseUrl(), dependenciesHome, jdkHome, packageInfo, 1);
        LinuxFileUtils.createDirectories(jdkHome, user, user, Constants.PERMISSION_755, true);
    }

    @Override
    public ShellResult configure(Params params) {
        super.configure(params);
        return TrinoSetup.config((TrinoParams) params, getComponentName());
    }

    @Override
    public ShellResult start(Params params) {
        TrinoParams trinoParams = (TrinoParams) params;

        String cmd = MessageFormat.format(
                "JAVA_HOME={0} {1}/bin/launcher start", trinoParams.javaHome(), trinoParams.serviceHome());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, trinoParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult stop(Params params) {
        TrinoParams trinoParams = (TrinoParams) params;

        String cmd = MessageFormat.format(
                "JAVA_HOME={0} {1}/bin/launcher stop", trinoParams.javaHome(), trinoParams.serviceHome());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, trinoParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult status(Params params) {
        TrinoParams trinoParams = (TrinoParams) params;

        String cmd = MessageFormat.format(
                "JAVA_HOME={0} {1}/bin/launcher status", trinoParams.javaHome(), trinoParams.serviceHome());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, trinoParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }
}
