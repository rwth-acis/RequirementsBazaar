package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Created by hugif on 26.12.2016.
 */
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class Statistic {

    private int numberOfProjects;
    private int numberOfCategories;
    private int numberOfRequirements;
    private int numberOfComments;
    private int numberOfAttachments;
    private int numberOfVotes;

    public String toJSON() throws JsonProcessingException {
        return new ObjectMapper().registerModule(new JavaTimeModule()).setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(this);
    }
}
