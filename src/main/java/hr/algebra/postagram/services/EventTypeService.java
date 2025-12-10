package hr.algebra.postagram.services;

import hr.algebra.postagram.models.EventType;
import hr.algebra.postagram.models.EventTypeEnum;
import hr.algebra.postagram.repositories.EventTypeRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EventTypeService extends GeneralCrudService<EventType, EventTypeRepo> {
    public EventTypeService(EventTypeRepo repository) {
        super(repository);
    }

    public Optional<EventType> findByName(String name) {
        return repository.findByName(name);
    }

    public EventType findByEnum(EventTypeEnum eventTypeEnum) {
        return repository.findByName(eventTypeEnum.name()).orElseGet(() -> save(new EventType(eventTypeEnum.name())));
    }
}
