package mizdooni.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import mizdooni.utils.RatingFaker;
import mizdooni.utils.RestaurantFaker;
import mizdooni.utils.ReviewFaker;
import mizdooni.utils.TableFaker;
import mizdooni.utils.UserFaker;

@SpringBootTest
class RestaurantTest {

    @Nested
    class Tables {

        private static List<Restaurant> restaurants;
        private static List<Table> tables;

        @BeforeAll
        static void setup() {
            restaurants = List.of(RestaurantFaker.createRestaurant(), RestaurantFaker.createRestaurant());
            Restaurant restaurant1 = restaurants.get(0);
            tables = List.of(
                TableFaker.createTable(restaurant1, 2),
                TableFaker.createTable(restaurant1, 6),
                TableFaker.createTable(restaurant1, 4),
                TableFaker.createTable(restaurant1, 2)
            );
        }

        @AfterAll
        static void tearDown() {
            tables = null;
            restaurants = null;
        }

        static Object[][] restaurantTablesProvider() {
            return new Object[][] {
                {restaurants.get(0), tables.get(0).getTableNumber(), tables.get(0)},
                {restaurants.get(0), tables.get(2).getTableNumber(), tables.get(2)},
                {restaurants.get(1), tables.get(3).getTableNumber(), null},
            };
        }
        @ParameterizedTest
        @MethodSource("restaurantTablesProvider")
        @DisplayName("Should return equivalent table when is fetched from restaurant")
        void shouldReturnTable_whenTableIsAddedToRestaurant(Restaurant restaurant, int tableNumber, Table table) {
            assertEquals(table, restaurant.getTable(tableNumber));
        }

        @Test
        @DisplayName("Should assign correct table number to each table")
        void shouldAssignCorrectTableNumber_whenTableIsAddedToRestaurant() {
            for (int idx = 0; idx < tables.size(); idx++) {
                assertEquals(tables.get(idx).getTableNumber(), idx + 1);
            }
        }

        static Object[][] seatsNumberProvider() {
            return new Object[][] {
                {restaurants.get(0), 6},
                {restaurants.get(1), 0},
            };
        }
        @ParameterizedTest
        @MethodSource("seatsNumberProvider")
        @DisplayName("should return correct number of maximum sets number")
        void shouldReturnCorrectNumberOfSeatsNumber(Restaurant restaurant, int expectedMaximumSeatsNumber) {
            assertEquals(expectedMaximumSeatsNumber, restaurant.getMaxSeatsNumber());
        }

    }

    @Nested
    class Reviews {

        private static List<Restaurant> restaurants;
        private static List<User> clients;
        private static List<Review> reviews;

        @BeforeAll
        static void setup() {
            restaurants = List.of(RestaurantFaker.createRestaurant(), RestaurantFaker.createRestaurant());
            Restaurant restaurant1 = restaurants.get(0);
            clients = List.of(UserFaker.createClient(), UserFaker.createClient(), UserFaker.createClient());
            User client1 = clients.get(0);
            User client2 = clients.get(1);
            User client3 = clients.get(2);
            reviews = List.of(
                ReviewFaker.createReview(client1,
                    RatingFaker.createRating(4.5, 3.5, 4.0, 4.0)),
                ReviewFaker.createReview(client2,
                    RatingFaker.createRating(3.5, 4.0, 2.5, 3.0)),
                ReviewFaker.createReview(client1,
                    RatingFaker.createRating(4.5, 4.5, 4.0, 5.0)),
                ReviewFaker.createReview(client3,
                    RatingFaker.createRating(2.5, 3.5, 2.5, 2.5))
            );
            reviews.forEach(review -> restaurant1.addReview(review));
        }

        @AfterAll
        static void tearDown() {
            reviews = null;
            clients = null;
            restaurants = null;
        }

        static Object[][] reviewsSetProvider() {
            return new Object[][] {
                { restaurants.get(0), List.of(reviews.get(1), reviews.get(2), reviews.get(3)) },
                { restaurants.get(1), List.of() },
            };
        }
        @ParameterizedTest
        @MethodSource("reviewsSetProvider")
        @DisplayName("Should return correct sets of review for restaurant")
        void shouldReturnCrrectSetOfReview_whenReviewsAreAdded(Restaurant restaurant, List<Review> reviews) {
            assertEquals(reviews, restaurant.getReviews());
        }

        static Object[][] averageRatingProvider() {
            Rating averageRatingRestaurant1 = new Rating();
            averageRatingRestaurant1.food = 4.0;
            averageRatingRestaurant1.service = 3.5;
            averageRatingRestaurant1.ambiance = 3.0;
            averageRatingRestaurant1.overall = 3.5;
            return new Object[][] {
                { restaurants.get(0), averageRatingRestaurant1 },
                { restaurants.get(1), (new Rating()) }, // TODO: fix this return null instead of this
            };
        }
        @ParameterizedTest
        @MethodSource("averageRatingProvider")
        @DisplayName("Should return correct average rating")
        void shouldReturnCorrectAverageRating_whenReviewsAreAdded(Restaurant restaurant, Rating rating) {
            Rating averageRating = restaurant.getAverageRating();
            assertEquals(rating.ambiance, averageRating.ambiance);
            assertEquals(rating.food, averageRating.food);
            assertEquals(rating.service, averageRating.service);
            assertEquals(rating.overall, averageRating.overall);
        }

        static Object[][] starCountProvider() {
            return new Object[][] {
                { restaurants.get(0), 4 },
                { restaurants.get(1), 0 },
            };
        }
        @ParameterizedTest
        @MethodSource("starCountProvider")
        @DisplayName("Should return correct star count")
        void shouldReturnCorrectStartCount_whenReviewAreAdded(Restaurant restaurant, int starCount) {
            assertEquals(restaurant.getStarCount(), starCount);
        }
    }

}

