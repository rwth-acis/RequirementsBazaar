package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Ownable {
        @JsonIgnore
        boolean isOwner(User user);
}
