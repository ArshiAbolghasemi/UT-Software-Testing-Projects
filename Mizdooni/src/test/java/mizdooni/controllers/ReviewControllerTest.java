package mizdooni.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import mizdooni.assertion.ResponseAssertion;
import mizdooni.model.Restaurant;
import mizdooni.model.Review;
import mizdooni.model.User;
import mizdooni.response.PagedList;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.RestaurantService;
import mizdooni.service.ReviewService;
import mizdooni.utils.RatingFaker;
import mizdooni.utils.RestaurantFaker;
import mizdooni.utils.ReviewFaker;
import mizdooni.utils.UserFaker;

@ExtendWith(MockitoExtension.class)
public class ReviewControllerTest {

    @Mock
    private RestaurantService restaurantService;
    @Mock
    private ReviewService reviewService;
    @InjectMocks
    private ReviewController reviewController;

    private Restaurant restaurant;
    private User client1;
    private User client2;
    private List<Review> reviews;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        restaurant = RestaurantFaker.createRestaurant();
        client1 = UserFaker.createClient();
        client2 = UserFaker.createClient();
        reviews = List.of(
            ReviewFaker.createReview(client1, RatingFaker.createRating()),
            ReviewFaker.createReview(client2, RatingFaker.createRating())
        );
    }

    @AfterEach
    void tearDown() {
        restaurant = null;
        client1 = null;
        client2 = null;
        reviews = null;
    }

    @Nested
    class GetReview {

        @Test
        @DisplayName("should return review when params are valid")
        void shouldReturnReview_whenParamsAreValid() throws Exception {
            int restaurantId = restaurant.getId();
            int page = 1;
            PagedList<Review> pagedListReview = new PagedList<>(reviews, page, reviews.size());

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(reviewService.getReviews(restaurantId, page)).thenReturn(pagedListReview);

            Response response = reviewController.getReviews(restaurantId, page);

            ResponseAssertion.checkResponse(response, HttpStatus.OK, true, pagedListReview, null,
                String.format("reviews for restaurant (%d): %s", restaurantId, restaurant.getName()));
        }

        @Test
        @DisplayName("should return review when there is no review for restaurant")
        void shouldReturnEmptyReview_whenThereIsNoReviewForRestaurant() throws Exception {
            int restaurantId = restaurant.getId();
            int page = 1;
            PagedList<Review> pagedListReview = new PagedList<>(Collections.emptyList(), page, reviews.size());

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(reviewService.getReviews(restaurantId, page)).thenReturn(pagedListReview);

            Response response = reviewController.getReviews(restaurantId, page);

            ResponseAssertion.checkResponse(response, HttpStatus.OK, true, pagedListReview, null,
                String.format("reviews for restaurant (%d): %s", restaurantId, restaurant.getName()));
        }


        @Test
        @DisplayName("Should throw not found exception when restaurant id is invalid")
        void shouldThrowNotFoundException_whenRestaurantIsIsInvalid() {
            int restaurantId = restaurant.getId();
            int page = 1;

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reviewController.getReviews(restaurantId, page);
            });

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
            assertEquals("restaurant not found", exception.getMessage());
        }
    }
}
