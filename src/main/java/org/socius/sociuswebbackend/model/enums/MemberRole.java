package org.socius.sociuswebbackend.model.enums;

import lombok.Getter;

@Getter
public enum MemberRole {
    ADMIN,
    MEMBER;

    private final String value;

    MemberRole() {
        this.value = name().toLowerCase();
    }
}
