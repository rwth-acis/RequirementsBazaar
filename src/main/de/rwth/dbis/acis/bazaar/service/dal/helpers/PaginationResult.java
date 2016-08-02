package de.rwth.dbis.acis.bazaar.service.dal.helpers;


import java.util.List;

public class PaginationResult<T> {

    private int total;
    private String filter;
    private Pageable pageable;
    private List<T> elements;

    public PaginationResult(int total, String filter, Pageable pageable, List<T> elements) {
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
}
