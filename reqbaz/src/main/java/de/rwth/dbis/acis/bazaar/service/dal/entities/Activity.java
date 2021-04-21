package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class Activity extends EntityBase {

    private final transient int id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone="Europe/Berlin")
    private final LocalDateTime creationDate;

    private final ActivityAction activityAction;
    private final String dataUrl;
    private final DataType dataType;
    private final String dataFrontendUrl;
    private final String parentDataUrl;
    private final DataType parentDataType;
    private final String userUrl;
    private final String origin;

    private AdditionalObject additionalObject;

    @Override
    @JsonIgnore
    public int getId() {
        return id;
    }

    public enum DataType {
        STATISTIC,
        PROJECT,
        CATEGORY,
        REQUIREMENT,
        COMMENT,
        ATTACHMENT,
        USER,
        FEEDBACK
    }

    public enum ActivityAction {
        RETRIEVE,
        RETRIEVE_CHILD,
        CREATE,
        UPDATE,
        DELETE,
        REALIZE,
        UNREALIZE,
        VOTE,
        UNVOTE,
        DEVELOP,
        UNDEVELOP,
        FOLLOW,
        UNFOLLOW,
        LEADDEVELOP,
        UNLEADDEVELOP
    }

    @Data
    public static class AdditionalObject {
        @JsonFilter("ActivityFilter")
        @NonNull
        private Project project;

        @JsonFilter("ActivityFilter")
        @NonNull
        private Category category;

        @JsonFilter("ActivityFilter")
        @NonNull
        private Requirement requirement;

        @JsonFilter("ActivityFilter")
        @NonNull
        private User user;
    }
}
