package com.daniel.ProductManagement.data;

import java.math.BigDecimal;

public final class Drink extends Product{

    public Drink(BigDecimal price, int id, String name) {
        super(price, Rating.NULL, id, name);
    }

    public Drink(BigDecimal price, Rating rating, int id, String name) {
        super(price, rating, id, name);
    }

    @Override
    public Product applyRating(Rating rating) {
        return null;
    }

    @Override
    public boolean sell() {
        System.out.println("Food sold");
        setCounter(getCounter()-1);
        return true;
    }

    @Override
    public boolean produce() {
        System.out.println("Food produced");
        setCounter(getCounter() + 1);
        return true;
    }
}
