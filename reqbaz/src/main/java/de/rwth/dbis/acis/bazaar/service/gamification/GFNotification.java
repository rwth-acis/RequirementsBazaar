package de.rwth.dbis.acis.bazaar.service.gamification;

import java.util.Map;

import lombok.Value;

@Value
public class GFNotification {

    private Map<String, Object> data;
}
