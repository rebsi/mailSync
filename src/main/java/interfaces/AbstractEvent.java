package interfaces;

import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public abstract class AbstractEvent {
    public static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat RFC_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public abstract String getICalUID();

    public abstract String getTitle();

    public abstract String getDescription();

    public abstract Instant getStart();

    public abstract Instant getEnd();

    public abstract String getLocation();

    public abstract boolean isAllDayEvent();

    protected Instant truncateTimeAndZone(Date date) throws ParseException, ServiceLocalException {
        return RFC_DATE_FORMAT.parse(DATE_ONLY_FORMAT.format(date) + "T00:00:00.000+0000").toInstant();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (getICalUID() == null ? 0 : getICalUID().hashCode());
        result = prime * result + (getTitle() == null ? 0 : getTitle().hashCode());
        result = prime * result + (getDescription() == null ? 0 : getDescription().hashCode());
        result = prime * result + (getStart() == null ? 0 : getStart().hashCode());
        result = prime * result + (getEnd() == null ? 0 : getEnd().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractEvent)) {
            return false;
        }

        AbstractEvent other = (AbstractEvent) obj;

        if (getICalUID() == null) {
            if (other.getICalUID() != null) {
                return false;
            }
        } else if (!getICalUID().equals(other.getICalUID())) {
            return false;
        }

        if (getTitle() == null) {
            if (other.getTitle() != null) {
                return false;
            }
        } else if (!getTitle().equals(other.getTitle())) {
            return false;
        }

        if (getDescription() == null) {
            if (other.getDescription() != null) {
                return false;
            }
        } else if (!getDescription().equals(other.getDescription())) {
            return false;
        }

        if (getStart() == null) {
            if (other.getStart() != null) {
                return false;
            }
        } else if (!getStart().equals(other.getStart())) {
            return false;
        }

        if (getEnd() == null) {
            return other.getEnd() == null;
        } else return getEnd().equals(other.getEnd());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Event [Title: ");
        sb.append(getTitle());
        sb.append(" Start: ");
        sb.append(getStart());
        sb.append(" End: ");
        sb.append(getEnd());
        if (isAllDayEvent()) {
            sb.append(" (all Day)");
        }
        // sb.append(" ICalUID: ");
        // sb.append(getICalUID());
        sb.append("]");

        return sb.toString();
    }
}
