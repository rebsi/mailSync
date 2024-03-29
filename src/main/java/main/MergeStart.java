package main;

import interfaces.CalendarSource;
import interfaces.CalendarTarget;
import merger.Merger;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class MergeStart {
    static Logger log = LogManager.getLogger(MergeStart.class);

    private CalendarSource calendarSource;
    private CalendarTarget calendarTarget;
    private int syncDayPeriod;

    public MergeStart() throws Exception {
        try {
            init();
        } catch (Exception ex) {
            log.fatal("Failed to merge.", ex);
            throw ex;
        }
    }

    public void merge() throws Exception {
        Merger merger = new Merger(calendarSource, calendarTarget);
        merger.setFilter(evt -> evt.getTitle() == null ||
                (!"#Mittag#".equals(evt.getTitle()) && !evt.getTitle().startsWith("##")));

        Instant now = OffsetDateTime.now(ZoneOffset.UTC).toInstant();
        Instant inOneMonth = now.plus(syncDayPeriod, ChronoUnit.DAYS);

        merger.merge(now, inOneMonth);
    }

    private void init() throws Exception {
        InputStream in = MergeStart.class.getResourceAsStream("/settings.json");
        JSONObject calendarConfig = new JSONObject(IOUtils.toString(in, StandardCharsets.UTF_8));

        syncDayPeriod = calendarConfig.getInt("syncDayPeriod");
        if (syncDayPeriod <= 0) {
            throw new Exception("syncDayPeriod has to be grater 0: " + syncDayPeriod);
        }

        JSONObject sourceConfig = calendarConfig.getJSONObject("sourceCalendar");
        calendarSource = createCalendar(sourceConfig);

        JSONObject targetConfig = calendarConfig.getJSONObject("targetCalendar");
        calendarTarget = createCalendar(targetConfig);
    }

    private static <T extends CalendarSource> T createCalendar(JSONObject settings)
            throws Exception {
        Class<?> clazz = Class.forName(settings.getString("implementation"));
        return (T) clazz.getDeclaredConstructor(JSONObject.class).newInstance(settings);
    }

    public static void main(String... args) throws Exception {
        MergeStart mergeStart = new MergeStart();
        mergeStart.merge();
    }
}
