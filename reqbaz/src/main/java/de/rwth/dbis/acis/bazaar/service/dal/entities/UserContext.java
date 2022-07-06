package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.UserVote;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotNull;

/**
 * Dynamic object to provide the context the currently logged in user has provided to the related object
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class UserContext extends EntityBase {

    @ApiModelProperty(
            value = "The role the user has within the project. Only returned when requesting project resources."
    )
    private ProjectRole userRole;

    @ApiModelProperty(
            value = "Only returned when requesting requirement resources."
    )
    private UserVote userVoted;

    @NotNull
    private Boolean isFollower;

    @ApiModelProperty(
            value = "Only returned when requesting requirement resources."
    )
    private Boolean isDeveloper;

    @ApiModelProperty(
            value = "Only returned when requesting requirement resources."
    )
    private Boolean isContributor;

    @ApiModelProperty(
            value = "Whether the user has privilege to move the requirement. Only returned when requesting requirement resources."
    )
    private Boolean isMoveAllowed;

    @JsonIgnore
    @Override
    public int getId() {
        return 0;
    }
}
