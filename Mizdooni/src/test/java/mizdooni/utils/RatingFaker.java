package mizdooni.utils;

import com.github.javafaker.Faker;

import mizdooni.model.Rating;

public class RatingFaker {

    private static Faker faker = new Faker();

    public static Rating createRating() {
        Rating rating = new Rating();
        rating.food = faker.number().numberBetween(1, 10);
        rating.ambiance = faker.number().numberBetween(1, 10);
        rating.service = faker.number().numberBetween(1, 10);
        rating.overall = faker.number().numberBetween(1, 10);
        return rating;
    }

    public static Rating createRating(double overall) {
        Rating rating = new Rating();
        rating.food = faker.number().numberBetween(1, 10);
        rating.ambiance = faker.number().numberBetween(1, 10);
        rating.service = faker.number().numberBetween(1, 10);
        rating.overall = overall;
        return rating;
    }

    public static Rating createRating(double overall, double food, double ambiance, double service) {
        Rating rating = new Rating();
        rating.food = food;
        rating.ambiance = ambiance;
        rating.service = service;
        rating.overall = overall;
        return rating;
    }
}

