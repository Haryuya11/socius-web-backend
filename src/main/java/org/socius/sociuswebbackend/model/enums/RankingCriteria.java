package org.socius.sociuswebbackend.model.enums;

import lombok.Getter;

@Getter
public enum RankingCriteria {
    performance,
    peer_vote,
    attendance,
    task_completion;

    private final String value;

    RankingCriteria() {
        this.value = name().toLowerCase();
    }

}
