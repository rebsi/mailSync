package calendarImpl.google;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import interfaces.AbstractEvent;

import java.time.Instant;

public class GoogleEvent extends AbstractEvent {
    private final Event event;

    public GoogleEvent(Event event) {
        this.event = event;
    }

    public String getId() {
        return event.getId();
    }

    @Override
    public String getICalUID() {
        return event.getICalUID();
    }

    @Override
    public String getTitle() {
        return event.getSummary();
    }

    @Override
    public String getDescription() {
        return event.getDescription();
    }

    @Override
    public Instant getStart() {
        return getInstant(event.getStart());
    }

    @Override
    public Instant getEnd() {
        return getInstant(event.getEnd());
    }

    @Override
    public String getLocation() {
        return event.getLocation();
    }

    @Override
    public boolean isAllDayEvent() {
        return event.getStart().getDate() != null;
    }

    private static Instant getInstant(EventDateTime edt) {
        DateTime dateTime = edt.getDateTime();
        if (dateTime == null) {
            dateTime = edt.getDate();
        }

        if (dateTime == null) {
            return null;
        }

        return Instant.ofEpochMilli(dateTime.getValue());
    }
}
