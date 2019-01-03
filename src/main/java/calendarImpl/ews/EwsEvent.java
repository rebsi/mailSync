package calendarImpl.ews;

import interfaces.AbstractEvent;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.Appointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

public class EwsEvent extends AbstractEvent {
    public static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat RFC_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

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

        String d = appointment.getBody().toString();
        if (d != null && d.isEmpty()) {
            d = null;
        }
        description = d;

        location = appointment.getLocation();
        isAllDayEvent = appointment.getIsAllDayEvent();

        if (isAllDayEvent) {
            start = truncateTimeAndZone(appointment.getStart());
            end = truncateTimeAndZone(appointment.getEnd());
        } else {
            start = appointment.getStart().toInstant();
            end = appointment.getEnd().toInstant();
        }
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

    private Instant truncateTimeAndZone(Date date) throws ParseException, ServiceLocalException {
        return RFC_DATE_FORMAT.parse(DATE_ONLY_FORMAT.format(date) + "T00:00:00.000+0000").toInstant();
    }
}
