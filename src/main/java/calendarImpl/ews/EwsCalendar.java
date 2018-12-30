package calendarImpl.ews;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import interfaces.AbstractEvent;
import interfaces.CalendarSource;
import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.autodiscover.exception.AutodiscoverLocalException;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.FolderTraversal;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.FolderSchema;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.FolderView;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;

public class EwsCalendar implements CalendarSource {
    static Logger log = LogManager.getLogger(EwsCalendar.class);

    private final String mailBox;
    private final FolderId folderId;
    private final ExchangeService _serviceInstance;

    public EwsCalendar(JSONObject settings) throws Exception {
        mailBox = settings.getString("mailBox");

        _serviceInstance = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        _serviceInstance.setUseDefaultCredentials(true);

        // Use Autodiscover to set the URL endpoint.
        // and using a AutodiscoverRedirectionUrlValidationCallback in case of https enabled clod account
        _serviceInstance.autodiscoverUrl(mailBox, new IAutodiscoverRedirectionUrl() {
            @Override
            public boolean autodiscoverRedirectionUrlValidationCallback(String serviceUrl)
                    throws AutodiscoverLocalException {
                return serviceUrl.toLowerCase().startsWith("https://");
            }
        });

        String folder = settings.has("folder") ? settings.getString("folder") : null;
        if (folder != null && !folder.isEmpty()) {
            folderId = GetFolder(folder);
        } else {
            folderId = new FolderId(WellKnownFolderName.Calendar);
        }
    }

    @Override
    public List<AbstractEvent> getEvents(Instant from, Instant to) throws Exception {
        FindItemsResults<Item> res = _serviceInstance.findItems(folderId,
                // new SearchFilter.Not(new SearchFilter.ContainsSubstring(ItemSchema.Subject, "#Mittag#")),
                new ItemView(15));

        List<AbstractEvent> events = new ArrayList<>();
        for (Item item : res) {
            if (item instanceof Appointment) {
                events.add(new EwsEvent((Appointment) item));
            } else {
                log.warn("Found item that isn't an appointment: " + item.getSubject());
            }
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
