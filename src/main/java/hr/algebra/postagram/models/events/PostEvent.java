package hr.algebra.postagram.models.events;

import hr.algebra.postagram.models.EventTypeEnum;
import hr.algebra.postagram.models.Post;
import hr.algebra.postagram.models.User;

import java.time.LocalDateTime;

public class PostEvent implements IEvent {
    User user;
    Post post;
    LocalDateTime eventTime;

    public PostEvent(Post post) {
        this.user = post.getUser();
        this.post = post;
        eventTime = LocalDateTime.now();
    }

    @Override
    public EventTypeEnum getEventType() {
        return EventTypeEnum.POST;
    }

    @Override
    public String getDescription() {
        return "User " + user.getUsername() + " created a post with ID " + post.getId();
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
