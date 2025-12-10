package hr.algebra.postagram.models.events;

import hr.algebra.postagram.models.EventTypeEnum;
import hr.algebra.postagram.models.User;

import java.time.LocalDateTime;

public class LogoutEvent implements IEvent{
    User user;
    LocalDateTime eventTime;

    public LogoutEvent(User user) {
        this.user = user;
        eventTime = LocalDateTime.now();
    }

    @Override
    public EventTypeEnum getEventType() {
        return EventTypeEnum.LOGOUT;
    }

    @Override
    public String getDescription() {
        return "User " + user.getUsername() + " logged out.";
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
