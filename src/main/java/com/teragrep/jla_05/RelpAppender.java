/*
   Log4j RELP Plugin
   Copyright (C) 2021  Suomen Kanuuna Oy

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.teragrep.jla_05;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.SDElement;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import com.teragrep.rlp_01.RelpBatch;
import com.teragrep.rlp_01.RelpConnection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public final class RelpAppender extends AppenderSkeleton {
    String appName;
    int connectionTimeout;
    String hostname;
    int readTimeout;
    String relpAddress;
    String useSD;
    boolean useSDBoolean;
    int relpPort;
    int writeTimeout;
    int reconnectInterval;
    boolean connected = false;
    private RelpConnection relpConnection = new RelpConnection();

    public int getReconnectInterval() {
        return reconnectInterval;
    }

    public void setReconnectInterval(int reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getRelpAddress() {
        return this.relpAddress;
    }

    public void setRelpAddress(String address) {
        this.relpAddress = address;
    }

    public int getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getWriteTimeout() {
        return this.writeTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getRelpPort() {
        return this.relpPort;
    }

    public void setRelpPort(int port) {
        this.relpPort = port;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String app) {
        this.appName = app;
    }

    public String getUseSD() {
        return this.useSD;
    }

    public void setUseSD(String useSD) {
        this.useSD = useSD;
        this.useSDBoolean = Boolean.parseBoolean(useSD);
    }

    public boolean getUseSDBoolean() {
        return this.useSDBoolean;
    }

    private void reconnect() throws IOException, TimeoutException {
        disconnect();
        connect();
    }

    private void disconnect() throws IOException, TimeoutException {
        if (!this.connected) {
            return;
        }
        try {
            this.relpConnection.disconnect();
        } catch (IllegalStateException | IOException | java.util.concurrent.TimeoutException e) {
            System.out.println("RelpAppender.disconnect> exception:");
            e.printStackTrace();
        }
        this.relpConnection.tearDown();
        this.connected = false;
    }

    private void connect() {
        while (!this.connected) {
            try {
                this.relpConnection.setConnectionTimeout(this.getConnectionTimeout());
                this.relpConnection.setReadTimeout(this.getReadTimeout());
                this.relpConnection.setWriteTimeout(this.getWriteTimeout());
                this.connected = this.relpConnection.connect(this.getRelpAddress(), this.getRelpPort());
            } catch (Exception e) {
                System.out.println("RelpAppender.connect> exception:");
                e.printStackTrace();
            }
            if(!this.connected) {
                try {
                    Thread.sleep(this.getReconnectInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public synchronized void close() {
        if (!this.connected) {
            return;
        }
        try {
            disconnect();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void append(final LoggingEvent event) {
        if (!isAsSevereAsThreshold(event.getLevel())) {
            return;
        }
        if (!this.connected) {
            connect();
        }

        // Craft syslog message
        SyslogMessage syslog = new SyslogMessage()
                .withTimestamp(new Date().getTime())
                .withSeverity(Severity.WARNING)
                .withAppName(this.getAppName())
                .withHostname(this.getHostname())
                .withFacility(Facility.USER)
                .withMsg(layout.format(event));

        // Add SD if enabled
        if (this.getUseSDBoolean()) {
            SDElement event_id_48577 = new SDElement("event_id@48577")
                    .addSDParam("hostname", this.getHostname())
                    .addSDParam("uuid", UUID.randomUUID().toString())
                    .addSDParam("source", "source")
                    .addSDParam("unixtime", Long.toString(System.currentTimeMillis()));
            SDElement origin_48577 = new SDElement("origin@48577")
                    .addSDParam("hostname", this.getHostname());
            syslog = syslog
                    .withSDElement(event_id_48577)
                    .withSDElement(origin_48577);
        }

        RelpBatch batch = new RelpBatch();
        batch.insert(syslog.toRfc5424SyslogMessage().getBytes(StandardCharsets.UTF_8));

        boolean allSent = false;
        while (!allSent) {
            try {
                this.relpConnection.commit(batch);
            } catch (IllegalStateException | IOException | java.util.concurrent.TimeoutException e) {
                System.out.println("RelpAppender.flush.commit> exception:");
                e.printStackTrace();
                this.relpConnection.tearDown();
                this.connected = false;
            }
            // Check if everything has been sent, retry and reconnect if not.
            if (!batch.verifyTransactionAll()) {
                batch.retryAllFailed();
                try {
                    reconnect();
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            } else {
                allSent = true;
            }
        }
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }
}
