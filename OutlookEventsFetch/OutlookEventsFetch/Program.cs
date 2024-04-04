using System.Text.Json;
using Microsoft.Office.Interop.Outlook;

namespace OutlookEventsFetch;

public class Program
{
    private static void Main(string[] args)
    {
        DateTime startTime = DateTime.Parse(args[0]);
        DateTime endTime = DateTime.Parse(args[1]);

        Application outlook = new();
        Folder? calFolder = outlook.Session.GetDefaultFolder(OlDefaultFolders.olFolderCalendar) as Folder;

        if (calFolder == null)
        {
            Console.Error.WriteLine("Outlook calendar default folder not found.");
            return;
        }

        Items calItems = calFolder.Items;
        calItems.IncludeRecurrences = true;
        calItems.Sort("[Start]", Type.Missing);
        string filter = $"[Start] >= '{startTime:g}' AND [End] <= '{endTime:g}'";
        Items restrictItems = calItems.Restrict(filter);

        IList<EventJson> events = new List<EventJson>();
        foreach (AppointmentItem item in restrictItems)
        {
            if (item.Subject == "#Mittag#")
            {
                continue;
            }

            events.Add(new EventJson(item.GlobalAppointmentID, item.Subject, item.Body, item.StartUTC, item.EndUTC,
                item.Location, item.AllDayEvent, item.RecurrenceState != OlRecurrenceState.olApptNotRecurring));
        }

        string jsonString = JsonSerializer.Serialize(events);
        Console.WriteLine(jsonString);
    }

    public class EventJson
    {
        internal EventJson(string id, string title, string description, DateTime start, DateTime end, string location, bool isAllDayEvent, bool isRecurrent)
        {
            Id = id;
            Title = title;
            Description = description;
            Start = start;
            End = end;
            Location = location;
            IsAllDayEvent = isAllDayEvent;
            IsRecurrent = isRecurrent;
        }

        public string Id { get; }
        public string Title { get; }
        public string Description { get; }
        public DateTime Start { get; }
        public DateTime End { get; }
        public string Location { get; }
        public bool IsAllDayEvent { get; }
        public bool IsRecurrent { get; }

        public override string ToString()
        {
            return $"{Id}\n{Title}\n{Description}\n        start: {Start}\n          end: {End}\n     location: {Location}\nisAllDayEvent: {IsAllDayEvent}";
        }
    }
}