package mizdooni.utils;

import java.util.ArrayList;
import java.util.List;

import com.github.javafaker.Faker;

import mizdooni.model.Restaurant;
import mizdooni.model.Table;

public class TableFaker {

    private static Faker faker = new Faker();

    public static List<Table> createTables(Restaurant restaurant) {
        List<Table> tables = new ArrayList<>();

        int tableCounts = faker.number().numberBetween(2, 4);
        for (int i = 0; i < tableCounts; i++) {
            Table table = new Table(i + 1, restaurant.getId(), faker.number().numberBetween(2, 6));
            tables.add(table);
            restaurant.addTable(table);
        }

        return tables;
    }

    public static List<Table> createTables(Restaurant restaurant, int count) {
        List<Table> tables = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Table table = new Table(0, restaurant.getId(), faker.number().numberBetween(2, 6));
            tables.add(table);
            restaurant.addTable(table);
        }

        return tables;
    }

    public static Table createTable(Restaurant restaurant, int seatsNumber) {
        Table table = new Table(0, restaurant.getId(), seatsNumber);
        restaurant.addTable(table);
        return table;
    }

}

