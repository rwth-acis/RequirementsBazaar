package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Created by Martin on 15.06.2017.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class ProjectContributors extends EntityBase {

    private final int id;

    private User leader;
    private List<User> categoryLeader;
    private List<User> requirementCreator;
    private List<User> leadDeveloper;
    private List<User> developers;
    private List<User> commentCreator;
    private List<User> attachmentCreator;

    @Override
    @JsonIgnore
    public int getId() {
        return id;
    }
}
