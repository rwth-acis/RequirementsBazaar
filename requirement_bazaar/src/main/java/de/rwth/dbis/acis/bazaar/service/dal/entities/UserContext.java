package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.UserVote;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

/**
 * Dynamic object to provide the context the currently logged in user has provided to the related object
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class UserContext extends EntityBase {

    private ProjectRole projectRole;

    private UserVote userVoted;

    private Boolean isFollower;
    private Boolean isDeveloper;
    private Boolean isContributor;

    @JsonIgnore
    @Override
    public int getId() {
        return 0;
    }
}
