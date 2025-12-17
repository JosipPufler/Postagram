package hr.algebra.postagram.models.events;

import hr.algebra.postagram.models.EventTypeEnum;
import hr.algebra.postagram.models.Post;
import hr.algebra.postagram.models.User;

import java.time.LocalDateTime;

public class UserPostUpdate implements IEvent{
    Post post;
    LocalDateTime timestamp;

    public UserPostUpdate(Post post){
        this.post = post;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public EventTypeEnum getEventType() {
        return EventTypeEnum.ADMIN_POST_UPDATE;
    }

    @Override
    public User getUser() {
        return post.getUser();
    }

    @Override
    public String getDescription() {
        return post.getUser().getUsername() + " has updated post with id: " + post.getId();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
