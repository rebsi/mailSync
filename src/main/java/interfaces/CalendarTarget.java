package interfaces;

import java.io.IOException;

public interface CalendarTarget extends CalendarSource {
    // void update(Event toUpdate, Event source) throws IOException;;
    void delete(AbstractEvent toDelete) throws IOException;

    ;

    void create(AbstractEvent toCreate) throws IOException;

    ;

}