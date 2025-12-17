package hr.algebra.postagram.models.events;

import hr.algebra.postagram.models.EventTypeEnum;
import hr.algebra.postagram.models.Post;
import hr.algebra.postagram.models.User;

import java.time.LocalDateTime;

public class AdminPostUpdate implements IEvent{
    User admin;
    Post post;
    LocalDateTime timestamp;

    public AdminPostUpdate(User admin, Post post){
        this.admin = admin;
        this.post = post;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public EventTypeEnum getEventType() {
        return EventTypeEnum.ADMIN_POST_UPDATE;
    }

    @Override
    public User getUser() {
        return admin;
    }

    @Override
    public String getDescription() {
        return admin.getUsername() + " has updated post with id: " + post.getId();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
