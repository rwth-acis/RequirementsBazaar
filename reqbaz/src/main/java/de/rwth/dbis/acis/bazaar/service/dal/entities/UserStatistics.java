package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Statistics about the users of the Requirements Bazaar.
 *
 * The values may only represent a certain period of time (passed in the request).
 */
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class UserStatistics {

    /** The number of users who were active during the requested time period. */
    private int numberOfActiveUsers;

    /** The number of users who have registered in during the requested time period. */
    private int numberOfNewUsers;

    public String toJSON() throws JsonProcessingException {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(this);
    }
}