package hr.algebra.postagram.services;

import hr.algebra.postagram.models.events.IEvent;
import hr.algebra.postagram.models.events.PostEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class GlobalEventListener  {
    private final EventService eventService;
    private final Counter postCounter;

    public GlobalEventListener(EventService eventService, MeterRegistry registry) {
        this.eventService = eventService;
        this.postCounter = Counter.builder("app_events_total")
                .tag("type", "post")
                .description("Total number of post events")
                .register(registry);
    }

    @Async
    @EventListener
    @Transactional
    public void handleEvent(IEvent event) {
        eventService.save(event);

        if (event instanceof PostEvent) {
            postCounter.increment();
        }
    }
}
