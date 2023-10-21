/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.connector.imap.minder;

import io.innospots.base.data.minder.BaseDataConnectionMinder;
import io.innospots.base.data.operator.IOperator;
import io.innospots.base.data.schema.ConnectionCredential;
import io.innospots.connector.imap.operator.ImapExecutionOperator;
import jakarta.mail.*;
import jakarta.mail.event.ConnectionEvent;
import jakarta.mail.event.ConnectionListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/12
 */
@Slf4j
public class ImapConnectionMinder extends BaseDataConnectionMinder {


    public static final String FIELD_USER = "mail.user";
    public static final String FIELD_PASSWORD = "mail.pass";
    public static final String FIELD_HOST = "mail.imap.host";
    public static final String FIELD_PORT = "mail.imap.port";
    public static final String FIELD_SSL = "mail.imap.ssl";
    public static final String FIELD_AUTH = "mail.imap.auth";
    public static final String FIELD_PROTOCOL = "mail.store.protocol";

    private Session session;

    private Store imapStore;

    @Override
    public void open() {
        //https://jakarta.ee/specifications/mail/2.1/jakarta-mail-spec-2.1.html#example-showing-a-message
        Properties props = new Properties();
        props.putAll(this.connectionCredential().getConfig());
        //props.put("mail.store.protocol", "imap");
        props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(props.getProperty(FIELD_USER), props.getProperty(FIELD_PASSWORD));
            }
        });

        try {
            imapStore = session.getStore("imap");
            fillListener(imapStore);
            imapStore.connect(props.getProperty(FIELD_HOST),
                    Integer.parseInt(props.getProperty(FIELD_PORT, "993")),
                    props.getProperty(FIELD_USER), props.getProperty(FIELD_PASSWORD));

        } catch (MessagingException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void fillListener(Store imapStore) {
        imapStore.addStoreListener(storeEvent ->
                log.debug("storeEvent, message:{}, msgType:{}", storeEvent.getMessage(), storeEvent.getMessageType()));
        imapStore.addConnectionListener(new ConnectionListener() {
            @Override
            public void opened(ConnectionEvent connectionEvent) {
                log.info("opened: {}", connectionEvent);
            }

            @Override
            public void disconnected(ConnectionEvent connectionEvent) {
                log.info("disconnected: {}", connectionEvent);
            }

            @Override
            public void closed(ConnectionEvent connectionEvent) {
                log.info("closed: {}", connectionEvent);
            }
        });
    }


    @Override
    public Object test(ConnectionCredential connectionCredential) {
        Properties props = new Properties();
        props.putAll(connectionCredential.getConfig());
        Session session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(props.getProperty(FIELD_USER), props.getProperty(FIELD_PASSWORD));
            }
        });
        try {
            Store store = session.getStore();
            fillListener(store);
            store.connect(props.getProperty(FIELD_HOST),
                    Integer.parseInt(props.getProperty(FIELD_PORT, "993")),
                    props.getProperty(FIELD_USER), props.getProperty(FIELD_PASSWORD));
            return true;
        } catch (MessagingException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void close() {
        if (imapStore != null) {
            try {
                imapStore.close();
            } catch (MessagingException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public String connector() {
        return "email-imap";
    }

    @Override
    public Object fetchSample(ConnectionCredential connectionCredential, String tableName) {
        return null;
    }


    @Override
    public IOperator buildOperator() {
        return new ImapExecutionOperator(imapStore);
    }
}
