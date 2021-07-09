package de.rwth.dbis.acis.bazaar.service.dal.entities;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreateValidation;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Requirement entity
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class Requirement extends EntityBase implements Ownable {

    private int id;

    @NotNull
    @Size(min = 1, max = 50, message = "name must be between 1 and 50 characters")
    private String name;

    @NotNull(message = "description should not be null", groups = CreateValidation.class)
    @Size(min = 1, message = "Description can't be empty")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Berlin")
    private LocalDateTime realized;

    @Min(value = 0)
    @NotNull(message = "A project id must be provided", groups = CreateValidation.class)
    private int projectId;

    private User creator;
    private User leadDeveloper;

    @NotNull(message = "categories should not be null", groups = CreateValidation.class)
    @Size(min = 1, groups = CreateValidation.class)
    private List<Integer> categories;

    // This field is not filled because attachments should be not included in requirements response.
    // But the API still allows to create a requirement with attachments at the same time.
    private List<Attachment> attachments;

    @lombok.Builder.Default
    private List<Tag> tags = new ArrayList<>();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Berlin")
    private LocalDateTime creationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Berlin")
    private LocalDateTime lastUpdatedDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Berlin")
    private LocalDateTime lastActivity;

    private Integer numberOfComments;
    private Integer numberOfAttachments;
    private Integer numberOfFollowers;

    private int upVotes;
    private int downVotes;

    private UserContext userContext;

    @ApiModelProperty(
            dataType = "java.util.Map"
    )
    private JsonNode additionalProperties;

    @JsonProperty("_context")
    private EntityContext context;

    @Override
    public boolean isOwner(User user) {
        return creator == user;
    }

    @Override
    public boolean isOwner(Integer userId) {
        return creator.getId() == userId;
    }
}
