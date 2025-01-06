package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "ALERT")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    long gardenId;

    @Column
    private Instant dismissedUntil;

    @Column
    private int type;

    public static final int DO_NOT_WATER = 1;
    public static final int NEED_WATER = 2;
    /**
     * JPA (Java Persistence API) required no-args constructor, needed for DB to work
     */
    public Alert() {}

    public Alert(long gardenId, Instant dismissedUntil, int type) {
        this.gardenId = gardenId;
        this.dismissedUntil = dismissedUntil;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public long getGardenId() {
        return gardenId;
    }

    public int getType() {
        return type;
    }


    public void dismissUntilTomorrow() {
        dismissForFlooredTime(1, ChronoUnit.DAYS);
    }

    public void dismissForFlooredTime(int amount, ChronoUnit period) {
        dismissedUntil = Instant.now().plus(amount, period).truncatedTo(ChronoUnit.DAYS);
    }

    public void resetDismissal() {
        dismissedUntil = Instant.now();
    }
}
