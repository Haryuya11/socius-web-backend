package org.socius.sociuswebbackend.model.enums;

public enum RankingCriteria {
    performance,
    peer_vote,
    attendance,
    task_completion;

    private final String value;

    RankingCriteria() {
        this.value = name().toLowerCase();
    }

    public String getValue() {
        return value;
    }
}
