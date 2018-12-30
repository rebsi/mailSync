package merger;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import interfaces.AbstractEvent;
import interfaces.CalendarSource;
import interfaces.CalendarTarget;

public class Merger {
    static Logger log = LogManager.getLogger(Merger.class);

    private final CalendarSource source;
    private final CalendarTarget target;
    private Function<AbstractEvent, Boolean> filter;

    public Merger(CalendarSource source, CalendarTarget target) {
        this.source = source;
        this.target = target;
    }

    public void setFilter(Function<AbstractEvent, Boolean> filter) {
        this.filter = filter;
    }

    public void merge(Instant from, Instant to) throws Exception {
        log.debug(() -> String.format("merging %s -> %s from %s to %s%s", source, target, from, to,
                filter == null ? "" : " (with filter: " + filter + ")"));

        List<AbstractEvent> sourceList = source.getEvents(from, to);
        if (filter != null) {
            sourceList = sourceList.stream().filter(evt -> filter.apply(evt))
                    .collect(Collectors.toList());
        }

        Set<AbstractEvent> sourceEvents = new HashSet<AbstractEvent>(sourceList);
        List<AbstractEvent> targetEvents = target.getEvents(from, to);

        for (AbstractEvent tEvent : targetEvents) {
            if (sourceEvents.contains(tEvent)) {
                sourceEvents.remove(tEvent);
                log.debug(() -> String.format("Existing: %s", tEvent));
            } else {
                target.delete(tEvent);
                log.info(() -> String.format("Deleted: %s", tEvent));
            }
        }

        for (AbstractEvent event : sourceEvents) {
            target.create(event);
            log.info(() -> String.format("Created: %s", event));
        }
    }
}
