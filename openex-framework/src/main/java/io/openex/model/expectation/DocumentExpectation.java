package io.openex.model.expectation;

import io.openex.database.model.InjectExpectation;
import io.openex.model.Expectation;

import java.util.Objects;

public class DocumentExpectation implements Expectation {
    private Integer score;

    public DocumentExpectation(Integer score) {
        setScore(Objects.requireNonNullElse(score, 100));
    }

    @Override
    public InjectExpectation.EXPECTATION_TYPE type() {
        return InjectExpectation.EXPECTATION_TYPE.DOCUMENT;
    }

    @Override
    public Integer score() {
        return score;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
