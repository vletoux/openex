package io.openex.contract.fields;

import io.openex.contract.ContractType;
import io.openex.model.LinkedFieldModel;

import java.util.ArrayList;
import java.util.List;

public abstract class ContractElement {

    private String key;

    private String label;

    private boolean mandatory = true;

    private boolean expectation = false;

    private List<LinkedFieldModel> linkedFields = new ArrayList<>();

    private List<String> linkedValues = new ArrayList<>();

    public ContractElement(String key, String label) {
        this.key = key;
        this.label = label;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public List<LinkedFieldModel> getLinkedFields() {
        return linkedFields;
    }

    public void setLinkedFields(List<ContractElement> linkedFields) {
        this.linkedFields = linkedFields.stream().map(LinkedFieldModel::fromField).toList();
    }

    public List<String> getLinkedValues() {
        return linkedValues;
    }

    public void setLinkedValues(List<String> linkedValues) {
        this.linkedValues = linkedValues;
    }

    public boolean isExpectation() {
        return expectation;
    }

    public void setExpectation(boolean expectation) {
        this.expectation = expectation;
    }

    public abstract ContractType getType();
}
