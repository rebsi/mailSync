package calendarImpl.pipedJson;

import interfaces.AbstractEvent;

import java.time.Instant;

public class PipedJsonEvent extends AbstractEvent {
    private final int bodySizeLimit;
    private final PipedJsonCalendar.ParsedJsonEvent event;
    private final String iCalUid;
    private final Instant start;
    private final Instant end;

    public PipedJsonEvent(PipedJsonCalendar.ParsedJsonEvent event, int bodySizeLimit) throws Exception {
        this.event = event;
        this.bodySizeLimit = bodySizeLimit;

        if (event.getIsRecurrent()) {
            iCalUid = event.getId() + DATE_ONLY_FORMAT.format(event.getStart());
        } else {
            iCalUid = event.getId();
        }

        if (isAllDayEvent()) {
            start = truncateTimeAndZone(event.getStart());
            end = truncateTimeAndZone(event.getEnd());
        } else {
            start = event.getStart().toInstant();
            end = event.getEnd().toInstant();
        }
    }

    @Override
    public String getICalUID() {
        return iCalUid;
    }

    @Override
    public String getTitle() {
        return event.getTitle();
    }

    @Override
    public String getDescription() {
        if (bodySizeLimit < 0 || event.getDescription() == null || event.getDescription().length() <= bodySizeLimit) {
            return event.getDescription();
        }
        return event.getDescription().substring(0, bodySizeLimit);
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
        return event.getLocation();
    }

    @Override
    public boolean isAllDayEvent() {
        return event.getIsAllDayEvent();
    }
}
