package hr.algebra.postagram.models.events;

import hr.algebra.postagram.models.EventTypeEnum;
import hr.algebra.postagram.models.User;

import java.time.LocalDateTime;

public class UserProfileUpdate implements IEvent {
    User user;
    LocalDateTime timestamp;

    public UserProfileUpdate(User user) {
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
        return user.getUsername() + " updated their profile";
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
