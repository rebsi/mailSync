package interfaces;

import java.time.Instant;

public abstract class AbstractEvent {
    public abstract String getICalUID();

    public abstract String getTitle();

    public abstract String getDescription();

    public abstract Instant getStart();

    public abstract Instant getEnd();

    public abstract String getLocation();

    public abstract boolean isAllDayEvent();

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
            if (other.getEnd() != null) {
                return false;
            }
        } else if (!getEnd().equals(other.getEnd())) {
            return false;
        }

        return true;
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
