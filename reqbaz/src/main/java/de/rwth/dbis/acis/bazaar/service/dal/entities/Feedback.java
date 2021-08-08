package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreateValidation;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class Feedback extends EntityBase {
    private int id;

    @NotNull(message = "feedback needs an associated project", groups = CreateValidation.class)
    @Min(value = 0, groups = CreateValidation.class)
    private int projectId;

    @NotNull(message = "feedback can not be null", groups = CreateValidation.class)
    private String feedback;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Europe/Berlin")
    private OffsetDateTime creationDate;

    @JsonProperty("email")
    private String eMail;

    private Integer requirementId;
}
