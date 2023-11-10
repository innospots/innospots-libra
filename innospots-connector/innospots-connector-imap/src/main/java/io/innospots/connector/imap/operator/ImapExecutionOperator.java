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

package io.innospots.connector.imap.operator;

import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.data.request.ItemRequest;
import io.innospots.base.utils.BeanUtils;
import jakarta.mail.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/12
 */
@Slf4j
public class ImapExecutionOperator implements IExecutionOperator {

    private final Store imapStore;

    private static final String KEY_FOLDER = "mail_folder";

    public ImapExecutionOperator(Store imapStore) {
        this.imapStore = imapStore;
    }


    @Override
    public DataBody<Map<String, Object>> execute(ItemRequest itemRequest) {

        String folderName = (String) itemRequest.getBody().getOrDefault(KEY_FOLDER, "INBOX");
        DataBody<Map<String, Object>> dataBody = new DataBody<>();
        try {
            Folder folder = imapStore.getFolder(folderName);
            // set message to read
            folder.open(Folder.READ_WRITE);
            if (folder.getUnreadMessageCount() == 0) {
                return dataBody;
            }
            Message m = folder.getMessage(folder.getMessageCount() + 1 - folder.getUnreadMessageCount());
            MailMessage mailMessage = new MailMessage();

            Address[] a;
            // FROM
            if ((a = m.getFrom()) != null) {
                List<String> from = new ArrayList<>();
                for (int j = 0; j < a.length; j++) {
                    from.add(a[j].toString());
                }
                mailMessage.setFrom(from);
            }

            // TO
            if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
                List<String> to = new ArrayList<>();
                for (int j = 0; j < a.length; j++) {
                    to.add(a[j].toString());
                }
                mailMessage.setTo(to);
            }

            // REPLY TO
            if ((a = m.getReplyTo()) != null) {
                List<String> replyTo = new ArrayList<>();

                for (int j = 0; j < a.length; j++) {
                    replyTo.add(a[j].toString());
                }
                mailMessage.setReplyTo(replyTo);
            }

            // SUBJECT
            mailMessage.setSubject(m.getSubject());

            // content
            mailMessage.setContent(String.valueOf(m.getContent()));

            // FLAGS
            List<String> messageFlags = new ArrayList<>();
            Flags flags = m.getFlags();
            String[] uf = flags.getUserFlags(); // get the user flag strings
            messageFlags.addAll(Arrays.asList(uf));
            mailMessage.setFlags(messageFlags);

            // ReceiveDate
            mailMessage.setReceiveDate(m.getReceivedDate());
            // SendDate
            mailMessage.setSendDate(m.getSentDate());
            // messageId
            mailMessage.setMessageId(String.valueOf(m.getMessageNumber()));
            // ContentType
            mailMessage.setContentType(m.getContentType());
            // encoding TODO

            folder.close();
            dataBody.setBody(BeanUtils.toMap(mailMessage));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        dataBody.end();
        //https://iowiki.com/javamail_api/javamail_api_imap_servers.html
        return dataBody;
    }

    @Getter
    @Setter
    private static class MailMessage {
        private List<String> from;
        private List<String> to;
        private List<String> replyTo;
        private String subject;
        private String content;
        private List<String> flags;
        private Date receiveDate;
        private Date sendDate;
        private String messageId;
        private String contentType;
        private String encoding;

    }
}
