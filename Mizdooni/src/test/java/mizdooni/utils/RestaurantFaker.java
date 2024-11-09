package mizdooni.utils;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.javafaker.Faker;

import mizdooni.model.Restaurant;

public class RestaurantFaker {

    private static Faker faker = new Faker();

    public static Restaurant createRestaurant() {
        List<String> types = Arrays.asList("Italian", "Chinese", "Mexican", "Indian", "American", "French", "Thai",
            "Japanese", "Spanish", "Mediterranean");
        Collections.shuffle(types);

        return new Restaurant(faker.company().name(), UserFaker.createManager(), types.get(0), LocalTime.parse("08:30"),
            LocalTime.parse("22:00"), faker.lorem().sentence(), AddressFaker.createAddress(), faker.internet().image());
    }
}

