package io.openex.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.openex.database.audit.ModelBaseListener;
import io.openex.database.repository.DryInjectReportingRepository;
import io.openex.helper.MonoModelDeserializer;
import io.openex.model.Execution;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;

import static java.util.Optional.ofNullable;

@Entity
@Table(name = "dryinjects")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dryinject_type")
@EntityListeners(ModelBaseListener.class)
public abstract class DryInject<T> extends Injection<T> implements Base {

    @Id
    @Column(name = "dryinject_id")
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonProperty("dryinject_id")
    private String id;

    @Column(name = "dryinject_title")
    @JsonProperty("dryinject_title")
    private String title;

    @Column(name = "dryinject_type", insertable = false, updatable = false)
    @JsonProperty("dryinject_type")
    private String type;

    @Column(name = "dryinject_date")
    @JsonProperty("dryinject_date")
    private Instant date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dryinject_dryrun")
    @JsonSerialize(using = MonoModelDeserializer.class)
    @JsonProperty("dryinject_dryrun")
    private Dryrun run;

    @OneToOne(mappedBy = "dryInject", fetch = FetchType.EAGER)
    @JsonProperty("dryinject_status")
    private DryInjectStatus status;

    @Override
    @JsonProperty("dryinject_exercise")
    @JsonSerialize(using = MonoModelDeserializer.class)
    public Exercise getExercise() {
        return getRun().getExercise();
    }

    @Override
    @JsonProperty("dryinject_audiences")
    public List<Audience> getAudiences() {
        return new ArrayList<>();
    }

    // region transient
    @Transient
    private DryInjectReportingRepository<T> statusRepository;

    @JsonIgnore
    public DryInject<T> setStatusRepository(DryInjectReportingRepository<T> statusRepository) {
        this.statusRepository = statusRepository;
        return this;
    }

    @Override
    public void report(Execution execution) {
        statusRepository.executionSave(execution, this);
    }
    // endregion

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Dryrun getRun() {
        return run;
    }

    public void setRun(Dryrun run) {
        this.run = run;
    }

    public Optional<Instant> getDate() {
        return ofNullable(date);
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public DryInjectStatus getStatus() {
        return status;
    }

    public void setStatus(DryInjectStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean isGlobalInject() {
        return false;
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
