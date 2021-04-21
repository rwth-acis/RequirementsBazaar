package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotNull;

/**
 * Abstracts the project membership data
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class ProjectMember extends EntityBase {

    private int id;

    @NotNull
    private int userId;

    @NotNull
    private ProjectRole role;

    @JsonIgnore
    private User user;

    @JsonGetter("userProfileImage")
    public String getProfileImage() {
        return user.getProfileImage();
    }

    @JsonGetter("userName")
    public String getUserName() {
        return user.getUserName();
    }
}
