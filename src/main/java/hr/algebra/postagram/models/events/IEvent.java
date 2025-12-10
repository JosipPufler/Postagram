package hr.algebra.postagram.models.events;

import hr.algebra.postagram.models.EventTypeEnum;
import hr.algebra.postagram.models.User;

import java.time.LocalDateTime;

public interface IEvent {
    EventTypeEnum getEventType();
    User getUser();
    String getDescription();
    LocalDateTime getTimestamp();
}
