package com.daniel.ProductManagement.data;

import com.daniel.ProductManagement.service.Rateable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public abstract class Product implements Rateable<Product> {
    public static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.1);

    private BigDecimal price;
    private Rating rating = DEFAULT_RATING;
    private int counter = 0;
    private final int id;
    private final String name;

    public Product(BigDecimal price, Rating rating, int id, String name) {
        this.price = price;
        this.rating = rating;
        this.id = id;
        this.name = name;
    }


    public abstract boolean sell();
    public abstract boolean produce();

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public Rating getRating() {
        return rating;
    }

    public int getCounter() {
        return counter;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    public LocalDate getBestBefore() {
        return LocalDate.now();
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Product) {
            final Product o = (Product) obj;
            return this.id == o.id && Objects.equals(this.name, o.name);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "{" +
                ", name=" + name +
                ", rating=" + rating.name() +
                '}';
    }
}
