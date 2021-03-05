package de.rwth.dbis.acis.bazaar.service.dal.entities;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreateValidation;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.UserVote;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Requirement entity
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class Requirement extends EntityBase {

    private int id;

    @Size(min = 1, max = 50, message = "name must be between 1 and 50 characters")
    private String name;

    @NotNull(message = "description should not be null", groups = CreateValidation.class)
    @Size(min = 1, message = "Description can't be empty")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Berlin")
    private LocalDateTime realized;

    @Min(value = 0)
    private int projectId;

    private User creator;
    private User leadDeveloper;

    @NotNull(message = "categories should not be null", groups = CreateValidation.class)
    @Size(min = 1, groups = CreateValidation.class)
    private List<Category> categories;

    // This field is not filled because attachments should be not included in requirements response.
    // But the API still allows to create a requirement with attachments at the same time.
    private List<Attachment> attachments;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Berlin")
    private LocalDateTime creationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Berlin")
    private LocalDateTime lastUpdatedDate;

    private Integer numberOfComments;
    private Integer numberOfAttachments;
    private Integer numberOfFollowers;

    private int upVotes;
    private int downVotes;
    private UserVote userVoted;

    private Boolean isFollower;
    private Boolean isDeveloper;
    private Boolean isContributor;

    @JsonProperty("_context")
    private EntityContext context;

    @JsonProperty("isFollower")
    public Boolean isFollower() {
        return isFollower;
    }

    @JsonProperty("isDeveloper")
    public Boolean isDeveloper() {
        return isDeveloper;
    }

    @JsonProperty("isContributor")
    public Boolean isContributor() {
        return isContributor;
    }
}