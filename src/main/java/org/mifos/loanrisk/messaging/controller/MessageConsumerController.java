package org.mifos.loanrisk.messaging.controller;

import lombok.AllArgsConstructor;
import org.mifos.loanrisk.messaging.event.EventMessageDTO;
import org.mifos.loanrisk.messaging.service.MessageConsumerService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@AllArgsConstructor
public class MessageConsumerController {

    private final MessageConsumerService service;

    @GetMapping("/consumer/getAllMessages")
    public Flux<EventMessageDTO> getMessages() {
        return service.getMessages();
    }

    @GetMapping("/consumer/getMessagesByEventType/{eventType}")
    public Flux<EventMessageDTO> getMessagesByType(@PathVariable("eventType") String eventType) {
        return service.getMessagesByType(eventType);
    }

    @DeleteMapping("/consumer/deleteAllMessages")
    public void deleteMessages() {
        service.deleteMessages();
    }

}
