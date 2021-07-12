package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.UserVote;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotNull;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class Dashboard extends EntityBase {

    @NotNull
    private List<Project> projects;

    @NotNull
    private List<Category> categories;

    @NotNull
    private List<Requirement> requirements;

    @JsonIgnore
    @Override
    public int getId() {
        return 0;
    }
}
