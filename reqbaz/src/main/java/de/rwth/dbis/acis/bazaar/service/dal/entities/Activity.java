package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

import java.time.OffsetDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class Activity extends EntityBase {

    private final transient int id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Europe/Berlin")
    private final OffsetDateTime creationDate;

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
        USER_STATISTICS,
        PROJECT,
        CATEGORY,
        REQUIREMENT,
        COMMENT,
        ATTACHMENT,
        USER,
        FEEDBACK,
        TAG
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
    @lombok.Builder
    public static class AdditionalObject {
        @JsonFilter("ActivityFilter")
        @NonNull
        private Project project;

        @JsonFilter("ActivityFilter")
        @NonNull
        private User user;

        /* re-ordered properties because lombok enforces the constructor order:
         * first: required parameters (non-null)
         * then: other parameters
         */

        @JsonFilter("ActivityFilter")
        private Category category;

        @JsonFilter("ActivityFilter")
        private Requirement requirement;
    }
}
