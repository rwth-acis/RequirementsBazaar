package de.rwth.dbis.acis.bazaar.service.dal.helpers;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;

/**
 * Does nothing but acts as a group for hibernate validation
 */
@GroupSequence(Default.class)
public interface CreateValidation {
}
