package de.rwth.dbis.acis.bazaar.dal.entities;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public interface IdentifiedById {
    /**
     * @return Returns the identifier of the implementer. All entities should implement this.
     */
    public Integer getId();
}
