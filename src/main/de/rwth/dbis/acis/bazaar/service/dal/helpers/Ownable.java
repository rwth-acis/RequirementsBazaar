package de.rwth.dbis.acis.bazaar.service.dal.helpers;


public interface Ownable {

    boolean isOwner(int userId);

}
