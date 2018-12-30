package interfaces;

import java.time.Instant;
import java.util.List;

public interface CalendarSource {
    List<AbstractEvent> getEvents(Instant from, Instant to) throws Exception;
}
