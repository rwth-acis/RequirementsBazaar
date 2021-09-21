package de.rwth.dbis.acis.bazaar.service.dal.entities;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

// import javax.ws.rs.core.Link;

/**
 * Created by Martin on 12.06.2017.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class RequirementContributors extends EntityBase {

    private final int id;

    private User creator;
    private User leadDeveloper;
    private List<User> developers;
    private List<User> commentCreator;
    private List<User> attachmentCreator;
}
