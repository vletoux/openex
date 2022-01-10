package io.openex.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.openex.database.audit.ModelBaseListener;
import io.openex.helper.MonoModelDeserializer;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "dryruns")
@EntityListeners(ModelBaseListener.class)
public class Dryrun implements Base {
    @Id
    @Column(name = "dryrun_id")
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonProperty("dryrun_id")
    private String id;

    @Column(name = "dryrun_speed")
    @JsonProperty("dryrun_speed")
    private int speed;

    @Column(name = "dryrun_date")
    @JsonProperty("dryrun_date")
    private Instant date;

    @Column(name = "dryrun_status")
    @JsonProperty("dryrun_status")
    private boolean status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dryrun_exercise")
    @JsonSerialize(using = MonoModelDeserializer.class)
    @JsonProperty("dryrun_exercise")
    private Exercise exercise;

    @OneToMany(mappedBy = "run", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DryInject<?>> injects = new ArrayList<>();

    // region transient
    @JsonProperty("dryrun_finished")
    public boolean isFinished() {
        List<DryInject<?>> injects = getInjects();
        return injects.stream().allMatch(dryInject -> {
            DryInjectStatus status = dryInject.getStatus();
            return status != null && Inject.STATUS.SUCCESS.name().equals(status.getName());
        });
    }
    // endregion

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public List<DryInject<?>> getInjects() {
        return injects;
    }

    public void setInjects(List<DryInject<?>> injects) {
        this.injects = injects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !Base.class.isAssignableFrom(o.getClass())) return false;
        Base base = (Base) o;
        return id.equals(base.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
