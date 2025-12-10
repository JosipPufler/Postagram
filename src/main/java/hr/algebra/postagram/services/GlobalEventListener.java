package hr.algebra.postagram.services;

import hr.algebra.postagram.models.events.IEvent;
import jakarta.transaction.Transactional;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class GlobalEventListener  {
    private final EventService eventService;

    public GlobalEventListener(EventService eventService) {
        this.eventService = eventService;
    }

    @Async
    @EventListener
    @Transactional
    public void handleEvent(IEvent event) {
        eventService.save(event);
    }
}
