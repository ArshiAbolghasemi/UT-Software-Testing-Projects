package mizdooni.cucumber.scenarios.model.restaurant;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import mizdooni.model.Rating;
import mizdooni.model.Restaurant;
import mizdooni.utils.RestaurantFaker;
import mizdooni.utils.ReviewFaker;
import mizdooni.utils.UserFaker;

public class GetAverageRating {

    private Restaurant restaurant;
    private Rating actualAverageRating;

    @Given("a sample restaurant")
    public void a_sample_restaurant() {
        restaurant = RestaurantFaker.createRestaurant();
    }

    @Given("the following review(s) rating for a restaurant:")
    public void the_following_reviews_rating_for_a_restaurant(DataTable reviewsRating) {
        List<Map<String, String>> ratings = reviewsRating.asMaps(String.class, String.class);
        for (Map<String, String> ratingScore : ratings) {
            Rating rating = new Rating();
            rating.food = Double.parseDouble(ratingScore.get("Food"));
            rating.service = Double.parseDouble(ratingScore.get("Service"));
            rating.ambiance = Double.parseDouble(ratingScore.get("Ambiance"));
            rating.overall = Double.parseDouble(ratingScore.get("Overall"));
            restaurant.addReview(ReviewFaker.createReview(UserFaker.createClient(), rating));
        }
    }

    @Given("no reviews for a restaurant")
    public void no_reviews_for_a_restaurant() {}

    @When("I calculate the average rating")
    public void when_i_calculate_average_rating() {
        actualAverageRating = restaurant.getAverageRating();
    }

    @Then("the average rating should be:")
    public void the_average_rating_should_be(DataTable expectedAverageRatingScore) {
        Map<String, String> scroes = expectedAverageRatingScore.asMaps(String.class, String.class).get(0);
        Rating expectedAverageRating = new Rating();
        expectedAverageRating.food = Double.parseDouble(scroes.get("Food"));
        expectedAverageRating.ambiance = Double.parseDouble(scroes.get("Ambiance"));
        expectedAverageRating.service = Double.parseDouble(scroes.get("Service"));
        expectedAverageRating.overall = Double.parseDouble(scroes.get("Overall"));

        double delta = 0.0001;
        assertEquals(expectedAverageRating.food, actualAverageRating.food, delta);
        assertEquals(expectedAverageRating.service, actualAverageRating.service, delta);
        assertEquals(expectedAverageRating.ambiance, actualAverageRating.ambiance, delta);
        assertEquals(expectedAverageRating.overall, actualAverageRating.overall, delta);
    }
}

