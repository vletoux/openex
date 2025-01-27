package io.openex.contract;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ContractType {
    @JsonProperty("text")
    Text,
    @JsonProperty("number")
    Number,
    @JsonProperty("tuple")
    Tuple,
    @JsonProperty("checkbox")
    Checkbox,
    @JsonProperty("textarea")
    Textarea,
    @JsonProperty("select")
    Select,
    @JsonProperty("article")
    Article,
    @JsonProperty("challenge")
    Challenge,
    @JsonProperty("dependency-select")
    DependencySelect,
    @JsonProperty("attachment")
    Attachment,
    @JsonProperty("audience")
    Audience
}
