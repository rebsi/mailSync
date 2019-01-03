package calendarImpl.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import interfaces.AbstractEvent;
import interfaces.CalendarTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GoogleCalendar implements CalendarTarget {
    static Logger log = LogManager.getLogger(GoogleCalendar.class);

    private final String calendarId;
    private final Calendar service;
    private String calendarName;

    public GoogleCalendar(JSONObject settings) throws GeneralSecurityException, IOException {
        calendarId = settings.getString("calendarId");
        service = getService();
    }

    @Override
    public List<AbstractEvent> getEvents(Instant from, Instant to) throws IOException {
        DateTime fromDate = new DateTime(from.toEpochMilli());
        DateTime endDate = new DateTime(to.toEpochMilli());

        Events events = service.events().list(calendarId).setTimeMin(fromDate).setTimeMax(endDate)
                .setOrderBy("startTime").setSingleEvents(true).execute();

        return events.getItems().stream().map(e -> (AbstractEvent) new GoogleEvent(e))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(AbstractEvent toDelete) throws IOException {
        if (!GoogleEvent.class.isInstance(toDelete)) {
            throw new IOException("Only GoogleEvents can be deleted from a google calendar.");
        }

        GoogleEvent gEvent = (GoogleEvent) toDelete;
        service.events().delete(calendarId, gEvent.getId()).execute();
    }

    @Override
    public void create(AbstractEvent toCreate) throws IOException {
        Event event = new Event().setSummary(toCreate.getTitle())
                .setLocation(toCreate.getLocation()).setDescription(toCreate.getDescription())
                .setStart(getEventDateTime(toCreate, e -> e.getStart()))
                .setEnd(getEventDateTime(toCreate, e -> e.getEnd()))
                .setICalUID(toCreate.getICalUID());

        service.events().calendarImport(calendarId, event).execute();
    }

    @Override
    public String toString() {
        try {
            calendarName = service.calendars().get(calendarId).execute().getSummary();
        } catch (IOException e) {
            log.error("Failed to retrieve calendar title.", e);
        }

        return calendarName;
    }

    private static EventDateTime getEventDateTime(AbstractEvent event,
                                                  Function<AbstractEvent, Instant> instantSelector) {
        EventDateTime edt = new EventDateTime();
        if (event.isAllDayEvent()) {
            DateTime endDateTime = new DateTime(true, instantSelector.apply(event).toEpochMilli(),
                    0);
            edt.setDate(endDateTime).setTimeZone("UTC");
        } else {
            DateTime endDateTime = new DateTime(instantSelector.apply(event).toEpochMilli());
            edt.setDateTime(endDateTime).setTimeZone("UTC");
        }
        return edt;
    }

    private static final String APPLICATION_NAME = "CalendarSync";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart. If modifying these scopes, delete your previously
     * saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/googleCredentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GoogleCalendar.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,
                JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(
                        new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private static Calendar getService() throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME).build();
    }
}
