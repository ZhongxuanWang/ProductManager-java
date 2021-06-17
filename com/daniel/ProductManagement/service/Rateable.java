package com.daniel.ProductManagement.service;

import com.daniel.ProductManagement.data.Rating;

@FunctionalInterface
public interface Rateable <T> {
    public static final Rating DEFAULT_RATING = Rating.NULL;

    T applyRating(Rating rating);

    public default Rating getRating() {
        return DEFAULT_RATING;
    }

    /**
     *
     * @param stars number of stars in int type
     * @return the Rating object converted from the int values.
     */
    public static Rating convert(int stars) {
        return Rating.values()[stars-1];
    }

    public default T applyRating(int stars) {
        return applyRating(convert(stars));
    }

    public static void ma() {

    }
}