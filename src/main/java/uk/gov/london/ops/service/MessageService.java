/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.london.ops.domain.Message;
import uk.gov.london.ops.repository.MessageRepository;
import uk.gov.london.ops.Environment;

@Service
public class MessageService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private Environment environment;

    public Message find(String key) {
        Message response = null;
        if (key.equalsIgnoreCase("system-environment")) {
            response = environmentSummaryMessage(key);
        } else if (key.equalsIgnoreCase("system-hostname")) {
            response = environmentHostnameMessage(key);
        } else {
            response = messageRepository.findOne(key);
        }
        if (response == null) {
            log.warn("Could not find requested message key: " + key);
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public void update(Message message) {
        if (message.getText() == null) {
            message.setText("");
        } else if (message.getText().equals("_NPE_")) {
            throw new NullPointerException("Fake Null Pointer Exception to test error handling");
        }

        messageRepository.save(message);
    }

    private Message environmentSummaryMessage(String key) {
        Message response = new Message(key);
        response.setText(environment.summary());
        return response;
    }

    private Message environmentHostnameMessage(String key) {
        Message response = new Message(key);
        response.setText(environment.hostName());
        return response;
    }
}
