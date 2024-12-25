package mizdooni.cucumber.scenarios.model.restaurant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import mizdooni.model.Restaurant;
import mizdooni.model.Review;
import mizdooni.model.User;
import mizdooni.utils.RatingFaker;
import mizdooni.utils.RestaurantFaker;
import mizdooni.utils.ReviewFaker;
import mizdooni.utils.UserFaker;

public class AddReview {

    private Restaurant restaurant;
    private User client;
    private Review clientReview;

    @Given("a restaurant")
    public void a_restaurant() {
        restaurant = RestaurantFaker.createRestaurant();
    }

    @Given("a client")
    public void a_client() {
        client = UserFaker.createClient();
    }

    @Given("{int} review(s) for restaurant")
    public void reviews_for_restaurant(int reviewsCount) {
        for (int i = 0; i < reviewsCount; i++) {
            restaurant.addReview(ReviewFaker.createReview(UserFaker.createClient(), RatingFaker.createRating()));
        }
    }

    @Given("already a review from client for restaurant")
    public void already_a_review_from_client_for_restaurant() {
        restaurant.addReview(ReviewFaker.createReview(client, RatingFaker.createRating()));
    }

    @When("client adding a review")
    public void client_adding_a_review() {
        clientReview = ReviewFaker.createReview(client, RatingFaker.createRating());
        restaurant.addReview(clientReview);
    }

    @Then("restaurant should have {int} review(s) that contain this review")
    public void restaurant_should_have_expected_count_review_that_contain_this_review(int expectedCountReview) {
        assertEquals(expectedCountReview, restaurant.getReviews().size());
        assertTrue(restaurant.getReviews().contains(clientReview));
    }

}

