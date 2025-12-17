package hr.algebra.postagram.models.events;

import hr.algebra.postagram.models.EventTypeEnum;
import hr.algebra.postagram.models.User;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AdminProfileUpdate implements IEvent {
    User admin;
    User user;
    LocalDateTime timestamp;

    public AdminProfileUpdate(User admin, User user) {
        this.admin = admin;
        this.user = user;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public EventTypeEnum getEventType() {
        return EventTypeEnum.USER_PROFILE_UPDATE;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getDescription() {
        return admin.getUsername() + " updated " + user.getUsername();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
