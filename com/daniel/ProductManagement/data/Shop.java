package com.daniel.ProductManagement.data;

import com.daniel.file.service.ProductManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Shop {
    public static void main(String[] args) {
        ProductManager pm = ProductManager.getInstance();

        Comparator<Product> ratingSorter = (p1, p2) -> p2.getRating().ordinal() - p1.getRating().ordinal();
        Comparator<Product> priceSorter = (p1, p2) -> p2.getPrice().compareTo(p1.getPrice());
        Predicate<Product> filter = p -> !p.getName().contains("hey");
        
        pm.printProducts(filter, ratingSorter.thenComparing(priceSorter));

        AtomicInteger clientCount = new AtomicInteger();
        Callable<String> client = () -> {
            String clientID = "CLIENT No." + clientCount.incrementAndGet();
            String threadName = Thread.currentThread().getName();
            int productID = ThreadLocalRandom.current().nextInt(63)+101;
            // String languageTag = ProductManager.getSupportedLocales().stream().findFirst().isPresent().get();
            return clientID + "_" + threadName + "_";
        };

        List<Callable<String>> clients = Stream.generate(() -> client).limit(5).collect(Collectors.toList());
        ExecutorService service = Executors.newFixedThreadPool(5);

        try {
            List<Future<String>> results = service.invokeAll(clients);
            service.shutdown();
            results.forEach((r) -> {
                try {
                    System.out.println(r.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        } catch (InterruptedException e) {
            Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Unable to invoke clients: " + e.getMessage());
        }

        List<Product> productList = new ArrayList<>();
        productList.add(new Drink(BigDecimal.valueOf(1.20), 123, "Cola"));

        pm.printProductReport(productList);
    }
}
