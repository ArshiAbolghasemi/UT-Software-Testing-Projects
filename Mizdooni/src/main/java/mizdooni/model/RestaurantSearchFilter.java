package mizdooni.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RestaurantSearchFilter {
    private String name;
    private String type;
    private String location;
    private String sort;
    private String order;

    private static final String SORT_BY_RATING = "rating";
    private static final String SORT_BY_REVIEWS = "reviews";
    private static final String[] VALID_SORT_FIELDS = {SORT_BY_RATING, SORT_BY_REVIEWS};

    private static final String ORDER_ASC = "asc";
    private static final String ORDER_DESC = "desc";
    private static final String[] VALID_ORDER = {ORDER_ASC, ORDER_DESC};

    public void validate() {
        if (order != null && !order.isEmpty() && !Arrays.asList(VALID_ORDER).contains(order)) {
            throw new IllegalArgumentException("invalid order query param!");
        }

        if (sort != null && !sort.isEmpty() && !Arrays.asList(VALID_SORT_FIELDS).contains(sort)) {
            throw new IllegalArgumentException("invalid sort query param!");
        }
    }

    public List<Restaurant> filter(List<Restaurant> restaurants) {
        List<Restaurant> rest = new ArrayList<>(restaurants);
        if (name != null) {
            rest = rest.stream().filter(r -> r.getName().contains(name)).collect(Collectors.toList());
        }
        if (type != null) {
            rest = rest.stream().filter(r -> r.getType().equals(type)).collect(Collectors.toList());
        }
        if (location != null) {
            rest = rest.stream().filter(r -> r.getAddress().getCity().equals(location)).collect(Collectors.toList());
        }
        if (sort != null) {
            if (sort.equals(SORT_BY_RATING)) {
                Comparator<Restaurant> comparator = Comparator.comparing(r -> r.getAverageRating().overall);
                if (order != null && order.equals(ORDER_ASC)) {
                    comparator = comparator.reversed();
                }
                rest.sort(comparator.reversed());
            } else if (sort.equals(SORT_BY_REVIEWS)) {
                Comparator<Restaurant> comparator = Comparator.comparingInt(r -> r.getReviews().size());
                if (order != null && order.equals(ORDER_ASC)) {
                    comparator = comparator.reversed();
                }
                rest.sort(comparator.reversed());
            }
        }
        return rest;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}
