package org.socius.sociuswebbackend.model.enums;

public enum VoteType {
    positive,
    negative;

    private final String value;

    VoteType() {
        this.value = name().toLowerCase();
    }

    public String getValue() {
        return value;
    }
}
