package mizdooni.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
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
public class TableTest {

    @Nested
    class Reservation {

        private static Restaurant restaurant;
        private static List<Table> tables;
        private static User client;

        @BeforeAll
        static void setup() {
            restaurant = RestaurantFaker.createRestaurant();
            tables = TableFaker.createTables(restaurant, 4);
            client = UserFaker.createClient();
            ReservationFaker.createReservation(client, restaurant, tables.get(0),
                LocalDateTime.parse("2024-10-20T15:30:00"));
            ReservationFaker.createReservation(client, restaurant, tables.get(0),
                LocalDateTime.parse("2024-10-20T20:00:00"));
            ReservationFaker.createReservation(client, restaurant, tables.get(0),
                LocalDateTime.parse("2024-10-20T17:30:00")).cancel();
            ReservationFaker.createReservation(client, restaurant, tables.get(2),
                LocalDateTime.parse("2024-10-20T15:30:00")).cancel();
            ReservationFaker.createReservation(client, restaurant, tables.get(3),
                LocalDateTime.parse("2024-10-20T14:00:00"));
        }

        @AfterAll
        static void teardDown() {
            restaurant = null;
            client = null;
            tables = null;
        }

        static Object[][] tableReservationDataProvider() {
            return new Object[][] {
                {tables.get(0), LocalDateTime.parse("2024-10-20T15:30:00"), true},
                {tables.get(0), LocalDateTime.parse("2024-10-20T19:00:00"), false},
                {tables.get(0), LocalDateTime.parse("2024-10-20T17:30:00"), false},
                {tables.get(1), LocalDateTime.parse("2024-10-20T15:30:00"), false},
                {tables.get(2), LocalDateTime.parse("2024-10-20T15:30:00"), false},
                {tables.get(3), null, false},
            };
        }
        @ParameterizedTest
        @MethodSource("tableReservationDataProvider")
        @DisplayName("Should return true if table reserved at this time otherwise false")
        void shouldReturnCorrectReservationStautsForTable(Table table, LocalDateTime time, boolean isReserved) {
            assertEquals(isReserved, table.isReserved(time));
        }
    }
}

