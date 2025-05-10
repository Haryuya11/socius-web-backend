package org.socius.sociuswebbackend.model.enums;

import lombok.Getter;

@Getter
public enum VoteType {
    positive,
    negative;

    private final String value;

    VoteType() {
        this.value = name().toLowerCase();
    }

}
