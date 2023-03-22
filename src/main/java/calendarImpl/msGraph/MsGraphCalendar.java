package calendarImpl.msGraph;

import interfaces.AbstractEvent;
import interfaces.CalendarSource;
import org.json.JSONObject;

import java.time.Instant;
import java.util.List;

public class MsGraphCalendar implements CalendarSource {
    // https://learn.microsoft.com/de-de/azure/active-directory/develop/sample-v2-code

    public MsGraphCalendar(JSONObject settings) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AbstractEvent> getEvents(Instant from, Instant to) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "MsGraphCalendar [" + "]";
    }
}
