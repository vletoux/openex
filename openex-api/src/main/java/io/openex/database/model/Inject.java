package io.openex.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.openex.database.audit.ModelBaseListener;
import io.openex.database.repository.InjectReportingRepository;
import io.openex.helper.MonoModelDeserializer;
import io.openex.helper.MultiModelDeserializer;
import io.openex.model.Execution;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;

import static java.time.Instant.now;
import static java.util.Optional.ofNullable;

@Entity
@Table(name = "injects")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "inject_type")
@EntityListeners(ModelBaseListener.class)
public abstract class Inject<T> extends Injection<T> implements Base {

    public enum STATUS {
        SUCCESS
    }

    public static Comparator<Inject<?>> executionComparator = (o1, o2) -> {
        if (o1.getDate().isPresent() && o2.getDate().isPresent()) {
            return o1.getDate().get().compareTo(o2.getDate().get());
        }
        return o1.getId().compareTo(o2.getId());
    };

    @Id
    @Column(name = "inject_id")
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonProperty("inject_id")
    private String id;

    @Column(name = "inject_title")
    @JsonProperty("inject_title")
    private String title;

    @Column(name = "inject_description")
    @JsonProperty("inject_description")
    private String description;

    @Column(name = "inject_country")
    @JsonProperty("inject_country")
    private String country;

    @Column(name = "inject_city")
    @JsonProperty("inject_city")
    private String city;

    @Column(name = "inject_enabled")
    @JsonProperty("inject_enabled")
    private boolean enabled = true;

    @Column(name = "inject_type", insertable = false, updatable = false)
    @JsonProperty("inject_type")
    private String type;

    @Column(name = "inject_created_at")
    @JsonProperty("inject_created_at")
    private Instant createdAt = now();

    @Column(name = "inject_updated_at")
    @JsonProperty("inject_updated_at")
    private Instant updatedAt = now();

    @Column(name = "inject_all_audiences")
    @JsonProperty("inject_all_audiences")
    private boolean allAudiences;

    @ManyToOne
    @JoinColumn(name = "inject_exercise")
    @JsonSerialize(using = MonoModelDeserializer.class)
    @JsonProperty("inject_exercise")
    private Exercise exercise;

    @ManyToOne
    @JoinColumn(name = "inject_depends_from_another")
    @JsonSerialize(using = MonoModelDeserializer.class)
    @JsonProperty("inject_depends_on")
    private Inject<?> dependsOn;

    @Column(name = "inject_depends_duration")
    @JsonProperty("inject_depends_duration")
    private Long dependsDuration;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonSerialize(using = MonoModelDeserializer.class)
    @JoinColumn(name = "inject_user")
    @JsonProperty("inject_user")
    private User user;

    @OneToOne(mappedBy = "inject", fetch = FetchType.EAGER)
    @JsonProperty("inject_status")
    private InjectStatus status;

    @OneToOne(mappedBy = "inject", fetch = FetchType.EAGER)
    @JsonProperty("inject_outcome")
    private Outcome outcome;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "injects_tags",
            joinColumns = @JoinColumn(name = "inject_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @JsonSerialize(using = MultiModelDeserializer.class)
    @JsonProperty("inject_tags")
    @Fetch(FetchMode.SUBSELECT)
    private List<Tag> tags = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "injects_audiences",
            joinColumns = @JoinColumn(name = "inject_id"),
            inverseJoinColumns = @JoinColumn(name = "audience_id"))
    @JsonSerialize(using = MultiModelDeserializer.class)
    @JsonProperty("inject_audiences")
    @Fetch(FetchMode.SUBSELECT)
    private List<Audience> audiences = new ArrayList<>();

    @Transient
    private InjectReportingRepository<T> statusRepository;

    // region transient
    @JsonIgnore
    @Override
    public boolean isUserHasAccess(User user) {
        return getExercise().isUserHasAccess(user);
    }

    @JsonProperty("inject_users_number")
    public long getNumberOfTargetUsers() {
        if (allAudiences) {
            return getExercise().usersNumber();
        }
        return getAudiences().stream()
                .map(Audience::getUsersNumber)
                .reduce(Long::sum).orElse(0L);
    }

    @JsonIgnore
    private Instant computeInjectDate(Instant source, int speed) {
        Optional<Inject<?>> dependsOnInject = ofNullable(getDependsOn());
        long duration = ofNullable(getDependsDuration()).orElse(0L) / speed;
        Instant dependingStart = dependsOnInject
                .map(inject -> inject.computeInjectDate(source, speed))
                .orElse(source);
        Instant standardExecutionDate = dependingStart.plusSeconds(duration);
        long pauseDelay = getExercise().getPauses().stream()
                .filter(pause -> pause.getDate().isBefore(standardExecutionDate))
                .mapToLong(pause -> pause.getDuration().orElse(0L)).sum();
        return standardExecutionDate.plusSeconds(pauseDelay);
    }

    @JsonProperty("inject_date")
    public Optional<Instant> getDate() {
        return getExercise().getStart()
                .map(source -> computeInjectDate(source, 1));
    }

    @JsonIgnore
    public boolean isNotExecuted() {
        return getStatus().isEmpty();
    }

    @JsonIgnore
    public boolean isPastInject() {
        return getDate().map(date -> date.isBefore(now())).orElse(false);
    }

    @JsonIgnore
    public boolean isFutureInject() {
        return getDate().map(date -> date.isAfter(now())).orElse(false);
    }

    @JsonIgnore
    public Inject<T> setStatusRepository(InjectReportingRepository<T> statusRepository) {
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    @Override
    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public Optional<InjectStatus> getStatus() {
        return ofNullable(status);
    }

    public void setStatus(InjectStatus status) {
        this.status = status;
    }

    public boolean isAllAudiences() {
        return allAudiences;
    }

    public void setAllAudiences(boolean allAudiences) {
        this.allAudiences = allAudiences;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Inject<?> getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(Inject<?> dependsOn) {
        this.dependsOn = dependsOn;
    }

    public Long getDependsDuration() {
        return dependsDuration;
    }

    public void setDependsDuration(Long dependsDuration) {
        this.dependsDuration = dependsDuration;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    @JsonIgnore
    protected abstract DryInject<T> toDry();

    @JsonIgnore
    public DryInject<T> toDryInject(Dryrun run, int speed) {
        DryInject<T> dryInject = toDry();
        dryInject.setTitle(getTitle());
        dryInject.setType(getType());
        dryInject.setContent(getContent());
        dryInject.setRun(run);
        dryInject.setDate(computeInjectDate(run.getDate(), speed));
        return dryInject;
    }

    @Override
    @JsonProperty("inject_audiences")
    public List<Audience> getAudiences() {
        return audiences;
    }

    public void setAudiences(List<Audience> audiences) {
        this.audiences = audiences;
    }

    @Override
    @JsonIgnore
    public boolean isGlobalInject() {
        return isAllAudiences();
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
