package mizdooni.utils;

import java.time.LocalDateTime;

import com.github.javafaker.Faker;

import mizdooni.model.Rating;
import mizdooni.model.Review;
import mizdooni.model.User;

public class ReviewFaker {

    private static Faker faker = new Faker();

    public static Review createReview(User client, Rating rating) {
        return new Review(client, rating, faker.lorem().sentence(), LocalDateTime.now());
    }
}

