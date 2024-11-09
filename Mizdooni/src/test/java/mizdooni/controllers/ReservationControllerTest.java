package mizdooni.controllers;

import static mizdooni.controllers.ControllerUtils.DATETIME_FORMATTER;
import static mizdooni.controllers.ControllerUtils.DATE_FORMATTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import mizdooni.assertion.ResponseAssertion;
import mizdooni.exceptions.BadPeopleNumber;
import mizdooni.exceptions.DateTimeInThePast;
import mizdooni.exceptions.InvalidManagerRestaurant;
import mizdooni.exceptions.InvalidWorkingTime;
import mizdooni.exceptions.ManagerReservationNotAllowed;
import mizdooni.exceptions.ReservationNotFound;
import mizdooni.exceptions.ReservationNotInOpenTimes;
import mizdooni.exceptions.TableNotFound;
import mizdooni.exceptions.UserNoAccess;
import mizdooni.exceptions.UserNotFound;
import mizdooni.exceptions.UserNotManager;
import mizdooni.model.Reservation;
import mizdooni.model.Restaurant;
import mizdooni.model.Table;
import mizdooni.model.User;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.ReservationService;
import mizdooni.service.RestaurantService;
import mizdooni.utils.ReservationFaker;
import mizdooni.utils.RestaurantFaker;
import mizdooni.utils.TableFaker;
import mizdooni.utils.UserFaker;

@ExtendWith(MockitoExtension.class)
public class ReservationControllerTest {

    @Mock
    private RestaurantService restaurantService;
    @Mock
    private ReservationService reservationService;
    @InjectMocks
    private ReservationController reservationController;

    private Restaurant restaurant;
    private List<Table> tables;
    private List<Reservation> reservations;
    private User client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        restaurant = RestaurantFaker.createRestaurant();
        tables = TableFaker.createTables(restaurant, 4);
        client = UserFaker.createClient();
        reservations = new ArrayList<>();
        reservations.add(ReservationFaker.createReservation(client, restaurant, tables.get(0),
            LocalDateTime.now().minusDays(1)));
        reservations.add(ReservationFaker.createReservation(client, restaurant, tables.get(0),
            LocalDateTime.now().plusDays(2)));
        reservations.add(ReservationFaker.createReservation(client, restaurant, tables.get(1),
            LocalDateTime.now().plusDays(1)));
    }

    @AfterEach
    void tearDown() {
        restaurant = null;
        tables = null;
        client = null;
        reservations = null;
    }

    @Nested
    class GetReservations {

        @Test
        @DisplayName("Should return in specific date reservations for restaurant when parameters are valid")
        void shouldReturnReservationsForTableOnSpecificDate_whenParamsAreValid() throws Exception {
            int restaurantId = restaurant.getId();
            int tableNumber = tables.get(0).getTableNumber();
            List<Reservation> tableReservations = List.of(reservations.get(1));
            LocalDateTime date = LocalDateTime.now().plusDays(2);
            String dateFormat = date.format(DATE_FORMATTER);

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(reservationService.getReservations(restaurantId, tableNumber,
                LocalDate.parse(dateFormat, DATE_FORMATTER))).thenReturn(tableReservations);

            Response response = reservationController.getReservations(restaurantId, tableNumber, dateFormat);
ResponseAssertion.checkResponse(response, HttpStatus.OK, true, tableReservations, null,
                "restaurant table reservations");
        }

        @Test
        @DisplayName("Should return all reservations for table when date is not defined")
        void shouldReturnAllReservationsForTable_whenDateIsNotDefined() throws Exception {
            int restaurantId = restaurant.getId();
            int tableNumber = tables.get(0).getTableNumber();
            List<Reservation> tableReservations = List.of(reservations.get(0), reservations.get(1));

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(reservationService.getReservations(restaurantId, tableNumber, null)).thenReturn(tableReservations);

            Response response = reservationController.getReservations(restaurantId, tableNumber, null);
            ResponseAssertion.checkResponse(response, HttpStatus.OK, true, tableReservations, null,
                "restaurant table reservations");
        }

        @Test
        @DisplayName("should return empty reservations when there is no reservation")
        void shouldReturnEmptyList_whenThereIsNoReservation() throws Exception {
            int restaurantId = restaurant.getId();
            int tableNumber = tables.get(2).getTableNumber();
            LocalDateTime date = LocalDateTime.now().minusDays(1);
            String dateFormat = date.format(DATE_FORMATTER);
            List<Reservation> tableReservations = Collections.emptyList();

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(reservationService.getReservations(restaurantId, tableNumber,
                LocalDate.parse(dateFormat, DATE_FORMATTER))).thenReturn(tableReservations);

            Response response = reservationController.getReservations(restaurantId, tableNumber, dateFormat);
            ResponseAssertion.checkResponse(response, HttpStatus.OK, true, tableReservations, null,
                "restaurant table reservations");
        }

        @Test
        @DisplayName("Should throw not found exception when restaurant id is invalid")
        void shouldThrowNotFoundException_whenRestaurantIdIsInvalid() {
            int restaurantId = 1;
            int tableNumber = tables.get(0).getTableNumber();
            LocalDateTime date = LocalDateTime.now().plusDays(2);
            String dateFormat = date.format(DATE_FORMATTER);

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.getReservations(restaurantId, tableNumber, dateFormat);
            });

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
            assertEquals("restaurant not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw not found exception when table number is invalid")
        void shouldThrowNotFoundException_whenTableNumberIsInvalid() throws Exception {
            int restaurantId = restaurant.getId();
            int tableNumber = 10;
            LocalDateTime date = LocalDateTime.now().plusDays(2);
            String dateFormat = date.format(DATE_FORMATTER);

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(reservationService.getReservations(restaurantId, tableNumber,
                LocalDate.parse(dateFormat, DATE_FORMATTER))).thenThrow(TableNotFound.class);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.getReservations(restaurantId, tableNumber, dateFormat);
            });

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
            assertEquals(TableNotFound.class.getSimpleName(), exception.getError());
        }

        static Object[][] invalidDateProvider() {
            return new Object[][] {
                { "2024-13-01" },
                { "invalid date" }
            };
        }
        @ParameterizedTest
        @MethodSource("invalidDateProvider")
        @DisplayName("Should throw Bad request exception when date is invalid")
        void shouldThrowBadRequestException_whenDateisInvalid(String date) {
            int restaurantId = restaurant.getId();
            int tableNumber = 10;

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);;

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.getReservations(restaurantId, tableNumber, date);
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals("bad parameter type", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw Forbidden exception when user that is logged in is not manager")
        void shouldThrowForbiddenException_whenLoggedInUserIsNotManger() throws Exception {
            int restaurantId = restaurant.getId();
            int tableNumber = 10;
            LocalDateTime date = LocalDateTime.now().plusDays(2);
            String dateFormat = date.format(DATE_FORMATTER);

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(reservationService.getReservations(restaurantId, tableNumber,
                LocalDate.parse(dateFormat, DATE_FORMATTER))).thenThrow(UserNotManager.class);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.getReservations(restaurantId, tableNumber, dateFormat);
            });

            assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
            assertEquals(UserNotManager.class.getSimpleName(), exception.getError());
        }

        @Test
        @DisplayName("Should throw Forbidden exception when user want to access another manger restaurant reservations")
        void shouldThrowForbiddenException_whenUserWantToAccessAnotherMangerRestaurantReservations() throws Exception {
            int restaurantId = restaurant.getId();
            int tableNumber = 10;
            LocalDateTime date = LocalDateTime.now().plusDays(2);
            String dateFormat = date.format(DATE_FORMATTER);

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(reservationService.getReservations(restaurantId, tableNumber,
                LocalDate.parse(dateFormat, DATE_FORMATTER))).thenThrow(InvalidManagerRestaurant.class);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.getReservations(restaurantId, tableNumber, dateFormat);
            });

            assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
            assertEquals(InvalidManagerRestaurant.class.getSimpleName(), exception.getError());
        }

        @Test
        @DisplayName("Should throw user not found exception when user is not logged in")
        void shouldThrowUserNotFoundException_whenUserIsNotLoggedIn() throws Exception {
            int restaurantId = restaurant.getId();
            int tableNumber = 10;
            LocalDateTime date = LocalDateTime.now().plusDays(2);
            String dateFormat = date.format(DATE_FORMATTER);

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(reservationService.getReservations(restaurantId, tableNumber,
                LocalDate.parse(dateFormat, DATE_FORMATTER))).thenThrow(UserNotFound.class);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.getReservations(restaurantId, tableNumber, dateFormat);
            });

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
            assertEquals(UserNotFound.class.getSimpleName(), exception.getError());
        }
    }

    @Nested
    class GetCustomerReservations {

        @Test
        @DisplayName("Should return customer reservations when there is no reservation")
        void shouldReturnCustomerReservationsWhenThereIsNoReservation() throws Exception {
            int customerId = client.getId();
            List<Reservation> customerReservations = client.getReservations();

            when(reservationService.getCustomerReservations(customerId)).thenReturn(customerReservations);

            Response response = reservationController.getCustomerReservations(customerId);

            ResponseAssertion.checkResponse(response, HttpStatus.OK, true, customerReservations, null,
                "user reservations");
        }

        @Test
        @DisplayName("Should throw user not found exception when user is not logged in")
        void shouldThrowUserNotFoundException_whenUserIsNotLoggedIn() throws Exception {
            int customerId = 10;

            when(reservationService.getCustomerReservations(customerId)).thenThrow(UserNotFound.class);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.getCustomerReservations(customerId);
            });

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
            assertEquals(UserNotFound.class.getSimpleName(), exception.getError());
        }

        @Test
        @DisplayName("Should throw user not access exception when user want to access other user reservation")
        void shouldThrowUserNotAccess_whenUserWantToAccessOtherUserReservations() throws Exception {
            int customerId = 10;

            when(reservationService.getCustomerReservations(customerId)).thenThrow(UserNoAccess.class);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.getCustomerReservations(customerId);
            });

            assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
            assertEquals(UserNoAccess.class.getSimpleName(), exception.getError());
        }
    }

    @Nested
    class GetAvailableTimes {

        @Test
        @DisplayName("Should return available times restaurant when params are valid")
        void shouldReturnAvailableTimesRestaurant_whenParamsAreValid() throws Exception {
            int restaurantId = restaurant.getId();
            int people = 2;
            LocalDate date = LocalDate.now().plusDays(1);
            String dateFormat = date.format(DATE_FORMATTER);
            List<LocalTime> availableTimes = List.of(
                LocalTime.now().plusHours(1),
                LocalTime.now().plusHours(2)
            );

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(reservationService.getAvailableTimes(restaurantId, people, date)).thenReturn(availableTimes);

            Response response = reservationController.getAvailableTimes(restaurantId, people, dateFormat);

            ResponseAssertion.checkResponse(response, HttpStatus.OK, true, availableTimes, null, "available times");
        }

        @Test
        @DisplayName("Should return empty list when there is no available times")
        void shouldReturnEmptyList_whenThereIsNoAvailableTime() throws Exception {
            int restaurantId = restaurant.getId();
            int people = 2;
            LocalDate date = LocalDate.now().plusDays(3);
            String dateFormat = date.format(DATE_FORMATTER);
            List<LocalTime> availableTimes = Collections.emptyList();

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(reservationService.getAvailableTimes(restaurantId, people, date)).thenReturn(availableTimes);

            Response response = reservationController.getAvailableTimes(restaurantId, people, dateFormat);

            ResponseAssertion.checkResponse(response, HttpStatus.OK, true, availableTimes, null, "available times");
        }

        @Test
        @DisplayName("Should throw not found exception when restaurant id is not valid")
        void shouldThrowNotFoundException_whenRestaurantIdIsNotValid() {
            int restaurantId = 10;
            int people = 2;
            LocalDate date = LocalDate.now().plusDays(1);
            String dateFormat = date.format(DATE_FORMATTER);

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.getAvailableTimes(restaurantId, people, dateFormat);
            });

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
            assertEquals("restaurant not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw bad request exception when people number is not valid")
        void shoulThrowBadRequestException_whenPeopleNumberIsNotValid() throws Exception {
            int restaurantId = 10;
            int people = -3;
            LocalDate date = LocalDate.now().plusDays(1);
            String dateFormat = date.format(DATE_FORMATTER);

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(reservationService.getAvailableTimes(restaurantId, people, date)).thenThrow(BadPeopleNumber.class);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.getAvailableTimes(restaurantId, people, dateFormat);
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals(BadPeopleNumber.class.getSimpleName(), exception.getError());
        }

        @Test
        @DisplayName("Should throw bad request exception when people number is not valid")
        void shoulThrowBadRequestException_whenDatetimeIsInPast() throws Exception {
            int restaurantId = 10;
            int people = 3;
            LocalDate date = LocalDate.now().minusDays(1);
            String dateFormat = date.format(DATE_FORMATTER);

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(reservationService.getAvailableTimes(restaurantId, people, date)).thenThrow(DateTimeInThePast.class);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.getAvailableTimes(restaurantId, people, dateFormat);
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals(DateTimeInThePast.class.getSimpleName(), exception.getError());
        }

        @Test
        @DisplayName("Should throw bad request exception when invalid format date")
        void shouldThrowBadRequestException_whenDateisInvalid() {
            int restaurantId = 10;
            int people = -3;

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.getAvailableTimes(restaurantId, people, "2023-11-43");
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals("bad parameter type", exception.getMessage());
        }

    }

    @Nested
    class AddReservation {

        private Map<String, String> prepareParams(int people, String datetime) {
            return Map.of(
                "people", Integer.toString(people),
                "datetime", datetime
            );
        }

        @Test
        @DisplayName("should add reservation when parameters are valid")
        void shouldAddReservation_whenParametersAreValid() throws Exception {
            int restaurantId = restaurant.getId();
            int people = 2;
            LocalDateTime datetime = LocalDateTime.now().plusDays(1);
            String datetimeFormat = datetime.format(DATETIME_FORMATTER);
            Reservation reservation = reservations.get(0);

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(reservationService.reserveTable(restaurantId, people,
                LocalDateTime.parse(datetimeFormat, DATETIME_FORMATTER))).thenReturn(reservation);

            Response response = reservationController.addReservation(restaurantId, 
                prepareParams(people, datetimeFormat));

            ResponseAssertion.checkResponse(response, HttpStatus.OK, true, reservation, null,
                "reservation done");
        }

        @Test
        @DisplayName("Should throw restaurant not found exception when restaurant id is not valid")
        void shouldThrowRestaurantNotFoundException_whenRestautanIdIsNotValid() {
            int restaurantId = 10;
            int people = 2;
            String datetime = LocalDateTime.now().plusDays(1).format(DATETIME_FORMATTER);

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);

            ResponseException  exception = assertThrows(ResponseException.class, () -> {
                reservationController.addReservation(restaurantId, prepareParams(people, datetime));
            });

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
            assertEquals("restaurant not found", exception.getMessage());
        }

        static Object[][] missingParametersProvider() {
            return new Object[][] {
                { Map.of("people", 2) },
                { Map.of("datetime", LocalDateTime.now().plusDays(1).format(DATETIME_FORMATTER)) }
            };
        }
        @ParameterizedTest
        @MethodSource("missingParametersProvider")
        @DisplayName("Should throw bad request exception when parameters are missed")
        void souldThrowBadRequestParametersMissin_whenPeopleOrDatetimeIsMissed(Map<String, String> params)
            throws Exception{
            int restaurantId = restaurant.getId();

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.addReservation(restaurantId, params);
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals("parameters missing", exception.getMessage());
        }

        static Object[][] invalidParametersProvider() {
            return new Object[][] {
                {
                    Map.of(
                        "people", "13temp",
                        "datetime", LocalDateTime.now().plusDays(1).format(DATETIME_FORMATTER)
                    )
                },
                {
                    Map.of(
                        "people", "3",
                        "datetime", "2024-13-05"
                    )
                },
                {
                    Map.of(
                        "people", "-3",
                        "datetime", LocalDateTime.now().plusDays(1).format(DATETIME_FORMATTER)
                    )
                }
            };
        }
        @ParameterizedTest
        @MethodSource("invalidParametersProvider")
        @DisplayName("Should throw bad requet exception when parameters are invalid")
        void shouldThrowBadRequestException_whenParametersAreInvalid(Map<String, String> params) {
            int restaurantId = restaurant.getId();

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.addReservation(restaurantId, params);
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals("bad parameter type", exception.getMessage());
        }

        static Object[][] failedToReserveTableProvider() {
            return new Object[][] {
                { UserNotFound.class },
                { ManagerReservationNotAllowed.class },
                { InvalidWorkingTime.class },
                { DateTimeInThePast.class },
                { ReservationNotInOpenTimes.class },
                { TableNotFound.class }
            };
        }
        @ParameterizedTest
        @MethodSource("failedToReserveTableProvider")
        @DisplayName("Should throw bad request exception when failed to reserved table")
        void shouldThrowBadRequestException_whenFailedToReservedTable(Class<? extends Throwable> failedException) 
            throws Exception {
            int restaurantId = restaurant.getId();
            int people = 2;
            String datetime = LocalDateTime.now().plusDays(1).format(DATETIME_FORMATTER);

            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(reservationService.reserveTable(restaurantId, people,
                LocalDateTime.parse(datetime, DATETIME_FORMATTER))).thenThrow(failedException);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.addReservation(restaurantId, prepareParams(people, datetime));
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals(failedException.getSimpleName(), exception.getError());
        }
    }

    @Nested
    class CancelReservation {

        @Test
        @DisplayName("Should cancel reservation when reservation number is valid")
        void shouldCancelReservation_whenReservationNumberIsValid() throws Exception {
            String reservationNumber = reservations.get(0).getReservationNumber();

            doNothing().when(reservationService).cancelReservation(eq(reservationNumber));

            Response response = reservationController.cancelReservation(reservationNumber);

            ResponseAssertion.checkResponse(response, HttpStatus.OK, true, null, null, "reservation cancelled");
            verify(reservationService, times(1)).cancelReservation(eq(reservationNumber));
        }

        @Test
        @DisplayName("Should throw user not found exception when user is not logged in")
        void shouldThrowUserNotFoundException_whenUserIsNotLoggedInd() throws Exception {
            String reservationNumber = reservations.get(2).getReservationNumber();

            doThrow(UserNotFound.class).when(reservationService).cancelReservation(reservationNumber);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.cancelReservation(reservationNumber);
            });

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
            assertEquals(UserNotFound.class.getSimpleName(), exception.getError());
        }

        @Test
        @DisplayName("should throw Reservation not found when reservation is not valid")
        void shouldThrowReservationNotFound_whenReservationNumberIsNotValid() throws Exception {
            String reservationNumber = "invalid reservation number";

            doThrow(ReservationNotFound.class).when(reservationService).cancelReservation(reservationNumber);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                reservationController.cancelReservation(reservationNumber);
            });

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
            assertEquals(ReservationNotFound.class.getSimpleName(), exception.getError());
        }
    }
}

