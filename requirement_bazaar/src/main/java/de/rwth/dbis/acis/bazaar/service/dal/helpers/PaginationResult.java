package de.rwth.dbis.acis.bazaar.service.dal.helpers;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

public class PaginationResult<T> {

    private int total;
    private Pageable pageable;
    private List<T> elements;

    public PaginationResult(int total, Pageable pageable, List<T> elements) {
        this.total = total;
        this.pageable = pageable;
        this.elements = elements;
    }

    public int getTotal() {
        return total;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public List<T> getElements() {
        return elements;
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) this.getTotal() / (double) this.getPageable().getPageSize());
    }

    public int getPrevPage() {
        if (this.getPageable().getOffset() > 0) {
            return this.getPageable().getPageNumber() - 1;
        } else {
            return -1;
        }
    }

    public int getNextPage() {
        if (this.getPageable().getOffset() + this.getElements().size() < this.getTotal()) {
            return this.getPageable().getPageNumber() + 1;
        } else {
            return -1;
        }
    }

    public String toJSON() throws JsonProcessingException {
        return new ObjectMapper().registerModule(new JavaTimeModule())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writerWithView(SerializerViews.Public.class)
                .writeValueAsString(this.getElements());
    }
}
