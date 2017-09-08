package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by hugif on 26.12.2016.
 */
public class Statistic {

    private int numberOfProjects;
    private int numberOfCategories;
    private int numberOfRequirements;
    private int numberOfComments;
    private int numberOfAttachments;
    private int numberOfVotes;


    protected Statistic(Builder builder) {
        this.numberOfProjects = builder.numberOfProjects;
        this.numberOfCategories = builder.numberOfCategories;
        this.numberOfRequirements = builder.numberOfRequirements;
        this.numberOfComments = builder.numberOfComments;
        this.numberOfAttachments = builder.numberOfAttachments;
        this.numberOfVotes = builder.numberOfVotes;
    }

    public int getNumberOfProjects() {
        return numberOfProjects;
    }

    public void setNumberOfProjects(int numberOfProjects) {
        this.numberOfProjects = numberOfProjects;
    }

    public int getNumberOfCategories() {
        return numberOfCategories;
    }

    public void setNumberOfCategories(int numberOfCategories) {
        this.numberOfCategories = numberOfCategories;
    }

    public int getNumberOfRequirements() {
        return numberOfRequirements;
    }

    public void setNumberOfRequirements(int numberOfRequirements) {
        this.numberOfRequirements = numberOfRequirements;
    }

    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public int getNumberOfAttachments() {
        return numberOfAttachments;
    }

    public void setNumberOfAttachments(int numberOfAttachments) {
        this.numberOfAttachments = numberOfAttachments;
    }

    public int getNumberOfVotes() {
        return numberOfVotes;
    }

    public void setNumberOfVotes(int numberOfVotes) {
        this.numberOfVotes = numberOfVotes;
    }

    public String toJSON() throws JsonProcessingException {
        return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(this);
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {

        protected int numberOfProjects;
        protected int numberOfCategories;
        protected int numberOfRequirements;
        protected int numberOfComments;
        protected int numberOfAttachments;
        protected int numberOfVotes;

        public Builder numberOfProjects(int numberOfProjects) {
            this.numberOfProjects = numberOfProjects;
            return this;
        }

        public Builder numberOfCategories(int numberOfCategories) {
            this.numberOfCategories = numberOfCategories;
            return this;
        }

        public Builder numberOfRequirements(int numberOfRequirements) {
            this.numberOfRequirements = numberOfRequirements;
            return this;
        }

        public Builder numberOfComments(int numberOfComments) {
            this.numberOfComments = numberOfComments;
            return this;
        }

        public Builder numberOfAttachments(int numberOfAttachments) {
            this.numberOfAttachments = numberOfAttachments;
            return this;
        }

        public Builder numberOfVotes(int numberOfVotes) {
            this.numberOfVotes = numberOfVotes;
            return this;
        }

        public Statistic build() {
            Statistic created = new Statistic(this);
            return created;
        }
    }

}
