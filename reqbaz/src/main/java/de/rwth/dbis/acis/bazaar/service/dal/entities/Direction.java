package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

public class Direction {
    @Getter
    @Setter
    private VoteDirection direction;

    @JsonIgnore
    public boolean isUpVote() {
        return direction == VoteDirection.up;
    }
}
