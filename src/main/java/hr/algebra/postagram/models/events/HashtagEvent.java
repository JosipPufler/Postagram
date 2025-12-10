package hr.algebra.postagram.models.events;

import hr.algebra.postagram.models.EventTypeEnum;
import hr.algebra.postagram.models.Hashtag;
import hr.algebra.postagram.models.Post;
import hr.algebra.postagram.models.User;

import java.time.LocalDateTime;

public class HashtagEvent implements IEvent{
    User user;
    Hashtag hashtag;
    LocalDateTime eventTime;

    public HashtagEvent(User user, Hashtag hashtag) {
        this.user = user;
        this.hashtag = hashtag;
        eventTime = LocalDateTime.now();
    }

    @Override
    public EventTypeEnum getEventType() {
        return EventTypeEnum.HASHTAG;
    }

    @Override
    public String getDescription() {
        return "User " + user.getUsername() + "[id="+user.getId()+"] created hashtag \"#"+hashtag+"\"";
    }

    @Override
    public LocalDateTime getTimestamp() {
        return eventTime;
    }

    @Override
    public User getUser() {
        return user;
    }
}
