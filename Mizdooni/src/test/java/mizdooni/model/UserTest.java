package mizdooni.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import mizdooni.utils.ReservationFaker;
import mizdooni.utils.RestaurantFaker;
import mizdooni.utils.TableFaker;
import mizdooni.utils.UserFaker;

@SpringBootTest
public class UserTest {

    @Nested
    class CheckPassword {

        static Object[][] wrongPassword() {
            return new Object[][] {
                { UserFaker.createClient("JXrHN;k6te"), "JXrHN;k6t" },
                { UserFaker.createClient("JXrHN;k6te"), "XrHN;k6te" },
                { UserFaker.createManager("JXrHN;k6te"), "JXrN;k6te" },
                { UserFaker.createManager("JXrHN;k6te"), "JXrHNk6te" },
                { UserFaker.createManager("JXrHN;k6te"), "JXrHN_k6te" },
                { UserFaker.createClient("JXrHN;k6te"), "" },
                { UserFaker.createManager("JXrHN;k6te"), null },
            };
        }
        @ParameterizedTest(name = "{index} => password={0}, input={1}, expected=false")
        @MethodSource("wrongPassword")
        @DisplayName("Should return false for incorrect passwords")
        void shouldReturnFalse_whenPassIncorrectPasswordToCheckPassword(User user, String inputPassword) {
            assertFalse(user.checkPassword(inputPassword), "Password should be incorrect for: " + inputPassword);
        }

        static Object[][] correctPassword() {
            return new Object[][] {
                { UserFaker.createClient("JXrHN;k6te"), "JXrHN;k6te" },
                { UserFaker.createClient("naJ5@x.S8b"), "naJ5@x.S8b" },
                { UserFaker.createManager("d^G!6`+v_z"), "d^G!6`+v_z" },
                { UserFaker.createManager("Y`qu8}':Gd"), "Y`qu8}':Gd" },
            };
        }
        @ParameterizedTest(name = "{index} => password={0}, input={1}, expected=true")
        @MethodSource("correctPassword")
        @DisplayName("Should return true for correct passwords")
        void shouldReturnTrue_whenPassCorrectPasswordToCheckPassword(User user, String inputPassword) {
            assertTrue(user.checkPassword(inputPassword), "Password should be correct for: " + inputPassword);
        }
    }

    @Nested
    class ReservationTest {

        private static List<Restaurant> restaurants;
        private static List<User> clients;
        private static List<Reservation> reservations;

        @BeforeAll
        static void setup() {
            restaurants = List.of(RestaurantFaker.createRestaurant(), RestaurantFaker.createRestaurant());
            Restaurant restaurant1 = restaurants.get(0);
            List<Table> restaurant1Tables = TableFaker.createTables(restaurant1);
            Collections.shuffle(restaurant1Tables);
            Restaurant restaurant2 = restaurants.get(1);
            List<Table> restaurant2Tables = TableFaker.createTables(restaurant2);
            Collections.shuffle(restaurant2Tables);
            clients = List.of(UserFaker.createClient(), UserFaker.createClient(), UserFaker.createClient());
            User client1 = clients.get(0);
            User client2 = clients.get(1);
            User client3 = clients.get(2);
            reservations = List.of(
                ReservationFaker.createReservation(client1, restaurant1, restaurant1Tables.getFirst()),
                ReservationFaker.createReservation(client1, restaurant2, restaurant2Tables.getFirst()),
                ReservationFaker.createReservation(client2, restaurant2, restaurant2Tables.getLast()),
                ReservationFaker.createReservation(client3, restaurant1, restaurant1Tables.getLast())
            );
        }

        @AfterAll
        static void tearDown() {
            restaurants = null;
            clients = null;
            reservations = null;
        }

        static Object[][] dataProviderReservationExistence() {
            return new Object[][] {
                {clients.get(0), reservations.get(0).getReservationNumber(), reservations.get(0)},
                {clients.get(0), reservations.get(2).getReservationNumber(), null},
                {clients.get(1), reservations.get(1).getReservationNumber(), null},
                {clients.get(1), reservations.get(2).getReservationNumber(), reservations.get(2)},
                {clients.get(2), reservations.get(3).getReservationNumber(), reservations.get(3)},
            };
        }
        @ParameterizedTest
        @MethodSource("dataProviderReservationExistence")
        @DisplayName("Should return reservations when reservation is added otherwise return null")
        void shouldReturnCorrectReservation_whenReservationIsAdded(User client, String reservationNumber, 
            Reservation reservation) {
            assertEquals(reservation, client.getReservation(reservationNumber));
        }

        static Object[][] dataProviderRestaurantReservation() {
            return new Object[][] {
                {clients.get(0), restaurants.get(0), true},
                {clients.get(2), restaurants.get(1), false},
                {clients.get(1), restaurants.get(1), true},
                {clients.get(1), restaurants.get(0), false},
            };
        }
        @ParameterizedTest
        @MethodSource("dataProviderRestaurantReservation")
        @DisplayName("Should return restaurant is reserved when reservation for restaurant is added otherwise false")
        void shouldReturnCorrectRestaurantReservationStauts(User client, Restaurant restaurant,
            boolean isRestaurantReserved) {
            assertEquals(isRestaurantReserved, client.checkReserved(restaurant));
        }
    }
}

