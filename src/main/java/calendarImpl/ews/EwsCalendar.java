package calendarImpl.ews;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.search.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import interfaces.AbstractEvent;
import interfaces.CalendarSource;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.FolderTraversal;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.core.service.schema.FolderSchema;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;

public class EwsCalendar implements CalendarSource {
    static Logger log = LogManager.getLogger(EwsCalendar.class);

    private final String mailBox;
    private final FolderId folderId;
    private final ExchangeService _serviceInstance;

    public EwsCalendar(JSONObject settings) throws Exception {
        mailBox = settings.getString("mailBox");

        _serviceInstance = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        //_serviceInstance.setCredentials(ExchangeCredentials.getExchangeCredentialsFromNetworkCredential("", "", ""));
        _serviceInstance.setUseDefaultCredentials(true);

        String url = getSettingOrNull(settings, "url");
        if (url != null && !url.isEmpty()) {
            _serviceInstance.setUrl(new URI(url));
        } else {
            // Use Autodiscover to set the URL endpoint.
            // and using a AutodiscoverRedirectionUrlValidationCallback in case of https enabled clod account
            _serviceInstance.autodiscoverUrl(mailBox, serviceUrl -> serviceUrl.toLowerCase().startsWith("https://"));
        }

        String folder = getSettingOrNull(settings, "folder");
        if (folder != null && !folder.isEmpty()) {
            folderId = GetFolder(folder);
        } else {
            folderId = new FolderId(WellKnownFolderName.Calendar);
        }
    }

    private static String getSettingOrNull(JSONObject settings, String key) {
        return settings.has(key) ? settings.getString(key) : null;
    }

    @Override
    public List<AbstractEvent> getEvents(Instant from, Instant to) throws Exception {
        CalendarView calendarView = new CalendarView(Date.from(from), Date.from(to));
        calendarView.setPropertySet(PropertySet.FirstClassProperties);
        FindItemsResults<Appointment> res = _serviceInstance.findAppointments(folderId, calendarView);
        _serviceInstance.loadPropertiesForItems(new ArrayList<>(res.getItems()), PropertySet.FirstClassProperties);

        List<AbstractEvent> events = new ArrayList<>();
        for (Appointment appointment : res) {
            events.add(new EwsEvent(appointment));
        }
        return events;
    }

    private FolderId GetFolder(String name) throws Exception {
        FolderView view = new FolderView(2);
        PropertySet propertySet = new PropertySet(BasePropertySet.IdOnly);
        propertySet.add(FolderSchema.DisplayName);
        view.setPropertySet(propertySet);
        view.setTraversal(FolderTraversal.Deep);

        SearchFilter filter = new SearchFilter.IsEqualTo(FolderSchema.DisplayName, name);
        FindFoldersResults findFoldersResults = _serviceInstance
                .findFolders(WellKnownFolderName.Root, filter, view);

        if (findFoldersResults.getTotalCount() < 1) {
            throw new Exception("Cannot find folder: " + name);
        }

        if (findFoldersResults.getTotalCount() > 1) {
            throw new Exception("Multiple [" + name + "] folders");
        }

        return findFoldersResults.getFolders().get(0).getId();
    }
}
