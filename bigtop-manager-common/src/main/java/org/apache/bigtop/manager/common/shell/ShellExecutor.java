/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.bigtop.manager.common.shell;

import org.apache.bigtop.manager.common.thread.TaskLogThreadDecorator;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * shell command executor.
 *
 * <code>ShellExecutor</code> should be used in cases where the output
 * of the command needs no explicit parsing and where the command, working
 * directory and the environment remains unchanged. The output of the command
 * is stored as-is and is expected to be small.
 */
@Slf4j
public class ShellExecutor {

    private final String[] command;

    private final File dir;

    /**
     * env for the command execution
     */
    private final Map<String, String> environment;

    /**
     * Time after which the executing script would be timed out
     */
    private final long timeoutInterval;

    private final Consumer<String> consumer;

    /**
     * Whether script timed out
     */
    private AtomicBoolean isTimeout;

    private Timer timeoutTimer;

    /**
     * sub process used to execute the command
     */
    private Process process;

    /**
     * Whether script finished executing
     */
    private AtomicBoolean completed;

    /**
     * Create a new instance of the ShellExecutor to execute a command.
     *
     * @param execString The command to execute with arguments
     * @param dir        If not-null, specifies the directory which should be set
     *                   as the current working directory for the command.
     *                   If null, the current working directory is not modified.
     * @param env        If not-null, environment of the command will include the
     *                   key-value pairs specified in the map. If null, the current
     *                   environment is not modified.
     * @param timeout    Specifies the time in milliseconds, after which the
     *                   command will be killed and the status marked as timedout.
     *                   If 0, the command will not be timed out.
     * @param consumer   the consumer to consume the output of the executed command.
     */
    private ShellExecutor(
            String[] execString, File dir, Map<String, String> env, long timeout, Consumer<String> consumer) {
        this.command = execString.clone();
        this.dir = dir;
        this.environment = env;
        this.timeoutInterval = timeout;
        this.consumer = consumer;
    }

    /**
     * Static method to execute a shell command.
     * Covers most of the simple cases for user.
     *
     * @param builderParameters shell command to execute.
     * @return the output of the executed command.
     * @throws IOException errors
     */
    public static ShellResult execCommand(List<String> builderParameters) throws IOException {
        return execCommand(builderParameters, s -> {});
    }

    /**
     * Static method to execute a shell command.
     * Covers most of the simple cases for user.
     *
     * @param builderParameters shell command to execute.
     * @param consumer the consumer to consume the output of the executed command.
     * @return the output of the executed command.
     * @throws IOException errors
     */
    public static ShellResult execCommand(List<String> builderParameters, Consumer<String> consumer)
            throws IOException {
        return execCommand(null, builderParameters, 0L, consumer);
    }

    /**
     * Static method to execute a shell command.
     * Covers most of the simple cases without requiring the user to implement
     * the <code>AbstractShell</code> interface.
     *
     * @param env the map of environment key=value
     * @param builderParameters shell command to execute.
     * @return the output of the executed command.
     * @throws IOException errors
     */
    public static ShellResult execCommand(Map<String, String> env, List<String> builderParameters) throws IOException {
        return execCommand(env, builderParameters, s -> {});
    }

    /**
     * Static method to execute a shell command.
     * Covers most of the simple cases without requiring the user to implement
     * the <code>AbstractShell</code> interface.
     *
     * @param env the map of environment key=value
     * @param builderParameters shell command to execute.
     * @param consumer the consumer to consume the output of the executed command.
     * @return the output of the executed command.
     * @throws IOException errors
     */
    public static ShellResult execCommand(
            Map<String, String> env, List<String> builderParameters, Consumer<String> consumer) throws IOException {
        return execCommand(env, builderParameters, 0L, consumer);
    }

    /**
     * Static method to execute a shell command.
     * Covers most of the simple cases without requiring the user to implement
     * the <code>AbstractShell</code> interface.
     *
     * @param env     the map of environment key=value
     * @param builderParameters shell command to execute.
     * @param timeout time in milliseconds after which script should be marked timeout
     * @return the output of the executed command.
     * @throws IOException errors
     */
    public static ShellResult execCommand(Map<String, String> env, List<String> builderParameters, long timeout)
            throws IOException {
        return execCommand(env, builderParameters, timeout, s -> {});
    }

    /**
     * Static method to execute a shell command.
     * Covers most of the simple cases without requiring the user to implement
     * the <code>AbstractShell</code> interface.
     *
     * @param env     the map of environment key=value
     * @param builderParameters shell command to execute.
     * @param timeout time in milliseconds after which script should be marked timeout
     * @param consumer the consumer to consume the output of the executed command.
     * @return the output of the executed command.
     * @throws IOException errors
     */
    public static ShellResult execCommand(
            Map<String, String> env, List<String> builderParameters, long timeout, Consumer<String> consumer)
            throws IOException {
        String[] cmd = builderParameters.toArray(new String[0]);

        ShellExecutor shellExecutor = new ShellExecutor(cmd, null, env, timeout, consumer);
        return shellExecutor.execute();
    }

    /**
     * Execute the shell command
     *
     * @throws IOException errors
     */
    private ShellResult execute() throws IOException {
        int exitCode;
        ProcessBuilder builder = new ProcessBuilder(command);
        isTimeout = new AtomicBoolean(false);
        completed = new AtomicBoolean(false);

        if (environment != null) {
            builder.environment().putAll(this.environment);
        }
        if (dir != null) {
            builder.directory(this.dir);
        }

        process = builder.start();
        ProcessContainer.putProcess(process);

        if (timeoutInterval > 0) {
            scheduleTimeoutTimer();
        }

        // read error and input streams as this would free up the buffers
        // free the error stream buffer
        BufferedReader errReader = createBufferedReader(process.getErrorStream());
        StringBuilder errMsg = new StringBuilder();
        Thread errThread = createReaderThread(errReader, errMsg);

        BufferedReader inReader = createBufferedReader(process.getInputStream());
        StringBuilder inMsg = new StringBuilder();
        Thread inThread = createReaderThread(inReader, inMsg);

        try {
            errThread.start();
            inThread.start();
        } catch (IllegalStateException ise) {
            log.warn("Illegal while starting the error and in thread", ise);
        }

        try {
            exitCode = process.waitFor();
            try {
                errThread.join();
                inThread.join();
            } catch (InterruptedException ie) {
                log.warn("Interrupted while reading the error and in stream", ie);
            }

            completed.compareAndSet(false, true);
        } catch (InterruptedException ie) {
            throw new IOException(ie.toString());
        } finally {
            if ((timeoutTimer != null) && !isTimeout.get()) {
                timeoutTimer.cancel();
            }

            // close the input stream
            try {
                inReader.close();
            } catch (IOException ioe) {
                log.warn("Error while closing the input stream", ioe);
            }

            if (!completed.get()) {
                errThread.interrupt();
            }

            try {
                errReader.close();
            } catch (IOException ioe) {
                log.warn("Error while closing the error stream", ioe);
            }

            ProcessContainer.removeProcess(process);
            process.destroy();
        }

        return new ShellResult(exitCode, inMsg.toString(), errMsg.toString());
    }

    /**
     * Returns the commands of this instance.
     * Arguments with spaces in are presented with quotes round; other
     * arguments are presented raw
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (String s : command) {
            if (s.indexOf(' ') >= 0) {
                builder.append('"').append(s).append('"');
            } else {
                builder.append(s);
            }

            builder.append(' ');
        }

        return builder.toString();
    }

    private BufferedReader createBufferedReader(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream));
    }

    private Thread createReaderThread(BufferedReader reader, StringBuilder msg) {
        TaskLogThreadDecorator decorator = new TaskLogThreadDecorator();
        return decorator.decorate(() -> {
            try {
                String line = reader.readLine();
                while ((line != null)) {
                    consumer.accept(line);
                    msg.append(line);
                    msg.append(System.lineSeparator());
                    line = reader.readLine();
                }
            } catch (IOException ioe) {
                log.warn("Error reading the stream", ioe);
            }
        });
    }

    private void scheduleTimeoutTimer() {
        this.timeoutTimer = new Timer();
        timeoutTimer.schedule(
                new TimerTask() {

                    @Override
                    public void run() {
                        try {
                            process.exitValue();
                        } catch (Exception e) {
                            // Process has not terminated.
                            // So check if it has completed
                            // if not just destroy it.
                            if (process != null && !completed.get()) {
                                isTimeout.set(true);
                                process.destroy();
                            }
                        }
                    }
                },
                timeoutInterval);
    }
}
