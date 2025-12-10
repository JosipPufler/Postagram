package hr.algebra.postagram.services;

import hr.algebra.postagram.models.Event;
import hr.algebra.postagram.models.EventType;
import hr.algebra.postagram.models.events.IEvent;
import hr.algebra.postagram.repositories.EventRepo;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class EventService extends GeneralCrudService<Event, EventRepo> {
    EventTypeService eventTypeService;

    public EventService(EventRepo repository, EventTypeService eventTypeService) {
        super(repository);
        this.eventTypeService = eventTypeService;
    }

    public Page<Event> findPaged(Integer pageNumber, Integer pageSize, String username){
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return repository.findPaged(username, pageable);
    }

    public List<Event> findByEventType(EventType eventType) {
        return repository.findByEventType(eventType);
    }

    public Event save(Event event) {
        if(findById(event.getId()).isEmpty()) {
            return repository.saveAndFlush(event);
        }
        return null;
    }

    public Event save(IEvent iEvent){
        return repository.saveAndFlush(Event.builder()
                        .eventType(eventTypeService.findByEnum(iEvent.getEventType()))
                        .time(iEvent.getTimestamp())
                        .description(iEvent.getDescription())
                        .user(iEvent.getUser())
                        .build());
    }
}
