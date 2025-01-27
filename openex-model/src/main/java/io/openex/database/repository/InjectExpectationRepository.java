package io.openex.database.repository;

import io.openex.database.model.InjectExpectation;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Repository
public interface InjectExpectationRepository extends CrudRepository<InjectExpectation, String>, JpaSpecificationExecutor<InjectExpectation> {

    @NotNull
    Optional<InjectExpectation> findById(@NotNull String id);

    @Query(value = "select i from InjectExpectation i where i.exercise.id = :exerciseId")
    List<InjectExpectation> findAllForExercise(@Param("exerciseId") String exerciseId);

    @Query(value = "select i from InjectExpectation i where i.exercise.id = :exerciseId " +
            "and i.type = 'CHALLENGE' and i.audience.id IN (:audienceIds)")
    List<InjectExpectation> findChallengeExpectations(@Param("exerciseId") String exerciseId,
                                                     @Param("audienceIds") List<String> audienceIds);

    @Query(value = "select i from InjectExpectation i where i.exercise.id = :exerciseId " +
            "and i.challenge.id = :challengeId and i.audience.id IN (:audienceIds)")
    List<InjectExpectation> findChallengeExpectations(@Param("exerciseId") String exerciseId,
                                               @Param("audienceIds") List<String> audienceIds,
                                               @Param("challengeId") String challengeId);

    @Modifying
    @Query(value = "delete from InjectExpectation i where i.exercise.id = :exerciseId")
    void deleteAllForExercise(@Param("exerciseId") String exerciseId);
}
