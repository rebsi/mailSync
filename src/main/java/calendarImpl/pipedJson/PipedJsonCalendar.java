package calendarImpl.pipedJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import interfaces.AbstractEvent;
import interfaces.CalendarSource;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class PipedJsonCalendar implements CalendarSource {
    private final String exePath;
    private final int bodySizeLimit;

    public PipedJsonCalendar(JSONObject settings) throws Exception {
        ParsedJsonEvent.JSON_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));

        exePath = settings.getString("exePath");

        if (settings.has("bodySizeLimit")) {
            bodySizeLimit = settings.getInt("bodySizeLimit");
        } else {
            bodySizeLimit = -1;
        }
    }

    @Override
    public List<AbstractEvent> getEvents(Instant from, Instant to) throws Exception {
        Process process = new ProcessBuilder(exePath, from.toString(), to.toString()).start();
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String json = br.lines().collect(Collectors.joining());

        Gson gson = new GsonBuilder().setFieldNamingStrategy(f -> {
            String n = f.getName();
            if (n.length() > 1) {
                return n.substring(0, 1).toUpperCase() + n.substring(1);
            }
            return n.substring(0, 1).toUpperCase();
        }).create();
        Type listType = new TypeToken<ArrayList<ParsedJsonEvent>>() {
        }.getType();
        List<ParsedJsonEvent> jsonEvents = gson.fromJson(json, listType);

        List<AbstractEvent> events = new ArrayList<>();
        for (ParsedJsonEvent e : jsonEvents) {
            events.add(new PipedJsonEvent(e, bodySizeLimit));
        }
        return events;
    }

    @Override
    public String toString() {
        return "PipedJsonCalendar [" + exePath + "]";
    }

    public static class ParsedJsonEvent {
        public static final SimpleDateFormat JSON_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        private String id;
        private String title;
        private String description;
        private String start;
        private String end;
        private String location;
        private boolean isAllDayEvent;
        private boolean isRecurrent;

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public Date getStart() throws Exception {
            return JSON_DATE_FORMAT.parse(start);
        }

        public Date getEnd() throws Exception {
            return JSON_DATE_FORMAT.parse(end);
        }

        public String getLocation() {
            return location;
        }

        public boolean getIsAllDayEvent() {
            return isAllDayEvent;
        }

        public boolean getIsRecurrent() {
            return isRecurrent;
        }
    }
}
