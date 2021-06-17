package com.daniel.ProductManagement.data;

public enum Rating {
    NULL("NO STAR ASSIGNED"),
    ONE_STAR("\u2605\u2606\u2606\u2606\u2606"),
    TWO_STAR("\u2605\u2605\u2606\u2606\u2606"),
    THREE_STAR("\u2605\u2605\u2605\u2606\u2606"),
    FOUR_STAR("\u2605\u2605\u2605\u2605\u2606"),
    FIVE_STAR("\u2605\u2605\u2605\u2605\u2605");

    private String stars;
    Rating(String in) {
        this.stars = in;
    }
    public String getStars() {
        return stars;
    }
}
