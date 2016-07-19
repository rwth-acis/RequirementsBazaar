package de.rwth.dbis.acis.bazaar.service.dal.helpers;


import java.util.List;

public class PaginationResult<T> {

    private int total;
    private String filter;
    private Pageable pageable;
    private List<T> elements;

    public PaginationResult(int total, String filter, Pageable pageInfo, List<T> elements) {
        this.total = total;
        this.filter = filter;
        this.pageable = pageable;
        this.elements = elements;
    }

    public int getTotal() {
        return total;
    }

    public String getFilter() {
        return filter;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public List<T> getElements() {
        return elements;
    }
}
