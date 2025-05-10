package org.socius.sociuswebbackend.model.enums;

import lombok.Getter;

@Getter
public enum PasswordChangeResult {
    SUCCESS,
    NOT_AUTHENTICATED,
    USER_NOT_FOUND,
    ACCOUNT_NOT_FOUND,
    INCORRECT_PASSWORD,
    GENERAL_ERROR;

    private final String value;

    PasswordChangeResult() {
        this.value = name().toLowerCase();
    }
}
