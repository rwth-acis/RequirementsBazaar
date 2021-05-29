package de.rwth.dbis.acis.bazaar.service.dal.entities;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

@EqualsAndHashCode(callSuper = true)
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class RequirementTag extends EntityBase {
    private final int id;
    private final int requirementId;

    private int tagId;
}
