package hr.algebra.postagram.services;

import hr.algebra.postagram.models.Hashtag;
import hr.algebra.postagram.models.User;
import hr.algebra.postagram.models.events.HashtagEvent;
import hr.algebra.postagram.repositories.HashtagRepo;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class HashtagService extends GeneralCrudService<Hashtag, HashtagRepo> {
    private final ApplicationEventPublisher publisher;

    public HashtagService(HashtagRepo repository, ApplicationEventPublisher publisher) {
        super(repository);
        this.publisher = publisher;
    }

    public Hashtag findByNameOrCreate(String name, User user) {
        Optional<Hashtag> byName = repository.findByName(name.replaceAll("[^A-Za-z0-9]",""));
        return byName.orElseGet(() -> {
            Hashtag save = repository.save(new Hashtag("#" + name.trim()));
            publisher.publishEvent(new HashtagEvent(user, save));
            return save;
        });
    }
}
