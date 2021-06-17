package com.daniel.file.service;

import com.daniel.ProductManagement.data.Food;
import com.daniel.ProductManagement.data.Product;
import com.daniel.ProductManagement.data.Rating;
import com.daniel.ProductManagement.data.UserReview;

import java.io.*;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProductManager {
    private final static Logger logger = Logger.getLogger(ProductManager.class.getName());
    private final ResourceBundle config = ResourceBundle.getBundle("configurations");

    private volatile Map<Product, List<UserReview>> productListMap = new HashMap<>();

    private final static ResourceFormatter formatter = new ResourceFormatter(new Locale("en_US"));
    private final static Map<String, ResourceFormatter> formatters = Map.of(
            "en_US", new ResourceFormatter(Locale.US),
            "zh_US", new ResourceFormatter(Locale.CHINA)
    );

    private final static ProductManager pm = new ProductManager();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private final String userName = config.getString("user.name");

    private ProductManager() {}

    public static ProductManager getInstance() {
        // This ensures all the callers only get the same instance.
        return pm;
    }

    public boolean createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product product = new Food(price, rating, id , name);
        return addProduct(product);
    }

    public boolean addProduct(Product product) {
        try {
            writeLock.lock();
            productListMap.putIfAbsent(product, new ArrayList<>());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception caught while creating the product: " + e.getMessage());
            return false;
        } finally {
            writeLock.unlock();
        }
        return true;
    }

    public Product findProduct(int id) {
        try {
            readLock.lock();
            return productListMap
                .keySet()
                .stream()
                .filter(product -> product.getId() == id)
                .findFirst().orElse(null);
        } finally {
            readLock.unlock();
        }
    }

    public Product reviewProduct(Product product, Rating rating, String comments) {
        try {
            writeLock.lock();
            List<UserReview> reviews = productListMap.get(product);
            productListMap.remove(product, reviews);
            reviews.add(new UserReview(rating, comments));
            product.applyRating((int) Math.round(
                    reviews.stream()
                            .mapToInt(r -> r.getRating().ordinal())
                            .average()
                            .orElse(0)));
            // Decimal Stars!
            // product.applyRating();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception caught while reviewing the product: " + e.getMessage());
        } finally {
            writeLock.unlock();
        }
        Runnable o = () -> System.out.println("asd");
        o.run();

        return product;
    }

    public void printProducts(Predicate<Product> filter, Comparator<Product> cmp) {
        try {
            readLock.lock();

            if (productListMap.isEmpty()) {
                System.out.println(formatter.getText("no.product"));
                return;
            }
            productListMap.keySet()
                    .stream()
                    .sorted(cmp)
                    .filter(filter)
                    .map(formatter::formatProduct)
                    .forEachOrdered(System.out::println);
        } finally {
            readLock.unlock();
        }
    }

    public void printProductReport(List<Product> products) {
        StringBuilder builder = new StringBuilder();
        for (Product product : products) {
            builder.append("Product Name: ").append(product.getName())
                    .append("\n Price:").append(product.getPrice())
                    .append("\n Stars:").append(product.getRating().getStars())
                    .append("\n Reviews:");
            if (productListMap.get(product) != null) {
                builder.append(productListMap.get(product)
                        .stream()
                        .map(r -> formatter.formatReview(r) + "\n")
                        .collect(Collectors.joining())
                );
            } else {
                builder.append(formatter.getText("no.review"));
            }
        }

        MessageFormat format = new MessageFormat(config.getString("report.file"));
        String filePathName = format.format(new String[] {userName.replace(' ', '_'),
                LocalDateTime.now().format(formatter.dateFormat).replace(' ', '_')});

        try (FileWriter myWriter = new FileWriter(filePathName)) {
            myWriter.write(builder.toString());
            logger.log(Level.INFO, "File saved at " + filePathName);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to write to the file: ", e);
        }

    }

    public static Set<String> getSupportedLocales() {
        return formatters.keySet();
    }

    public Map<String, String> getDiscounts(String localeTag) {
        ResourceFormatter formatter = formatters.getOrDefault(localeTag, formatters.get("en_US"));
        return Map.of("Unavailable", "Unavailable");
    }


    /**
     * Save the productListMap to file to save the progress.
     *
     * It's thread-safe. Both saving and reading operations are multithreaded, preventing excessive time consumption.
     */
    public void saveProgress() {
        class SaveProgress implements Runnable {
            @Override
            public void run() {
                synchronized (productListMap) {
                    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("progress_products"))) {
                        out.writeObject(productListMap);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Failed to save a file ", e);
                        return;
                    }
                    logger.log(Level.INFO, "Progress Saved");
                }
            }
        }

        Thread saveProgress = new Thread(new SaveProgress(), "Save Products List Progress");
        saveProgress.start();
    }

    @SuppressWarnings("unchecked")
    public Map<Product, List<UserReview>> readProgress() {
        Map<Product, List<UserReview>> productListMap = new HashMap<>();
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream("progress_products"))) {
            productListMap = (Map<Product, List<UserReview>>) in.readObject();
        } catch (FileNotFoundException notFoundException) {
            logger.log(Level.SEVERE, "Unable to find the progress swap file ", notFoundException);
        } catch (IOException ioException) {
            logger.log(Level.SEVERE, "Failed to read from the file ", ioException);
        } catch (ClassNotFoundException classNotFoundException) {
            logger.log(Level.SEVERE, "Unable to locate the class ", classNotFoundException);
        }
        return productListMap;
    }

    private static class ResourceFormatter {
        // private Locale locale;
        private ResourceBundle resources;
        private DateTimeFormatter dateFormat;
        private NumberFormat currencyFormat;

        private ResourceFormatter(Locale locale) {
            // this.locale = locale;
            resources = ResourceBundle.getBundle("resources", locale);
            dateFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale);
            currencyFormat = NumberFormat.getCurrencyInstance(locale);
        }

        private String formatProduct(Product product) {
            return MessageFormat.format(resources.getString("product"),
                    product.getName(),
                    currencyFormat.format(product.getPrice()),
                    product.getRating().getStars(),
                    dateFormat.format(product.getBestBefore()));
        }

        private String formatReview(UserReview review) {
            return MessageFormat.format(resources.getString("review"),
                    review.getRating().getStars(),
                    review.getComments());
        }

        private String getText(String key) {
                return resources.getString(key);
        }

        private void changeLocale(String name) {
            final String[] localeInfo = name.split("_");
            changeLocale(new Locale(localeInfo[0], localeInfo[1]));
        }

        private void changeLocale(Locale locale) {
            // this.locale = locale;
            resources = ResourceBundle.getBundle("properties", locale);
            dateFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale);
            currencyFormat = NumberFormat.getCurrencyInstance(locale);
        }
    }
}
