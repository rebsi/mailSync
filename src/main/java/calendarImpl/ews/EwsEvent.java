package calendarImpl.ews;

import java.time.Instant;

import interfaces.AbstractEvent;
import microsoft.exchange.webservices.data.core.service.item.Appointment;

public class EwsEvent extends AbstractEvent {
    private final String iCalUid;
    private final String title;
    private final String description;
    private final Instant start;
    private final Instant end;
    private final String location;
    private final boolean isAllDayEvent;

    public EwsEvent(Appointment appointment) throws Exception {
        iCalUid = appointment.getICalUid();
        title = appointment.getSubject();
        description = appointment.getBody().toString();
        start = appointment.getStart().toInstant();
        end = appointment.getEnd().toInstant();
        location = appointment.getLocation();
        isAllDayEvent = appointment.getIsAllDayEvent();
    }

    @Override
    public String getICalUID() {
        return iCalUid;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Instant getStart() {
        return start;
    }

    @Override
    public Instant getEnd() {
        return end;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public boolean isAllDayEvent() {
        return isAllDayEvent;
    }
}
