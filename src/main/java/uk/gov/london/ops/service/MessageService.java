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
import org.springframework.stereotype.Service;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.Message;
import uk.gov.london.ops.repository.MessageRepository;

import java.util.List;

@Service
public class MessageService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private Environment environment;

    public List<Message> getAll() {
        return messageRepository.findAll();
    }

    public Message find(String key) {
        Message response = null;
        if (key.equalsIgnoreCase("system-environment")) {
            response = environmentSummaryMessage(key);
        } else if (key.equalsIgnoreCase("system-hostname")) {
            response = environmentHostnameMessage(key);
        } else {
            response = messageRepository.findById(key).orElse(null);
        }
        if (response == null) {
            log.warn("Could not find requested message key: " + key);
        }
        return response;
    }

    public void update(Message message) {

        Message existing = messageRepository.getOne(message.getCode());
        existing.setEnabled(message.isEnabled());
        if (message.getText() == null) {
            existing.setText("");
        } else if (message.getText().equals("_NPE_")) {
            throw new NullPointerException("Fake Null Pointer Exception to test error handling");
        } else {
            existing.setText(message.getText());
        }

        messageRepository.save(existing);
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
