package calendarImpl.ews;

import interfaces.AbstractEvent;
import microsoft.exchange.webservices.data.core.enumeration.service.calendar.AppointmentType;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.Appointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.regex.Pattern;

public class EwsEvent extends AbstractEvent {

    public static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat RFC_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final Pattern replaceStyleRegex = Pattern.compile("style=\\\".+?\\\"");
    private final int bodySizeLimit;
    private final String iCalUid;
    private final String title;
    private final String description;
    private final Instant start;
    private final Instant end;
    private final String location;
    private final boolean isAllDayEvent;

    public EwsEvent(Appointment appointment) throws Exception {
        this(appointment, -1);
    }

    public EwsEvent(Appointment appointment, int bodySizeLimit) throws Exception {
        this.bodySizeLimit = bodySizeLimit;

        if (appointment.getAppointmentType() != AppointmentType.Single) {
            iCalUid = appointment.getICalUid() + DATE_ONLY_FORMAT.format(appointment.getStart());
        } else {
            iCalUid = appointment.getICalUid();
        }

        title = appointment.getSubject();
        description = getLimitedBody(appointment.getBody().toString());
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

    private String getLimitedBody(String d) {
        if (d == null) {
            return null;
        }

        if (d.isEmpty()) {
            return null;
        }

        if (bodySizeLimit < 0) {
            return d;
        }

        if (d.length() <= bodySizeLimit) {
            return d;
        }

        if (d.startsWith("<html xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\"")) {
            int headEnd = d.indexOf("</head>");
            if (headEnd > 0) {
                d = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">" + d.substring(headEnd);
            }
        }

        if (d.length() <= bodySizeLimit) {
            return d;
        }

        d = replaceStyleRegex.matcher(d).replaceAll("")
                .trim().replaceAll(" +", " ")
                .replaceAll("<span >", "<span>");

        if (d.length() <= bodySizeLimit) {
            return d;
        }

        return d.substring(0, bodySizeLimit);
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
