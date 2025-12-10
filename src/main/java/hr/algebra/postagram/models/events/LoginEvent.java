package hr.algebra.postagram.models.events;

import hr.algebra.postagram.models.EventTypeEnum;
import hr.algebra.postagram.models.User;

import java.time.LocalDateTime;

public class LoginEvent implements IEvent{
    User user;
    LocalDateTime eventTime;
    String ipAddress;

    public LoginEvent(User user, String ipAddress) {
        this.user = user;
        eventTime = LocalDateTime.now();
        this.ipAddress = ipAddress;
    }

    @Override
    public EventTypeEnum getEventType() {
        return EventTypeEnum.LOGIN;
    }

    @Override
    public String getDescription() {
        return "User " + user.getUsername() + " logged in from " + ipAddress + ".";
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
