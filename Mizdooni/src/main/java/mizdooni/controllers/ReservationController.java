package mizdooni.controllers;

import mizdooni.exceptions.BadPeopleNumber;
import mizdooni.exceptions.InvalidManagerRestaurant;
import mizdooni.exceptions.ReservationNotFound;
import mizdooni.exceptions.TableNotFound;
import mizdooni.exceptions.UserNoAccess;
import mizdooni.exceptions.UserNotFound;
import mizdooni.exceptions.UserNotManager;
import mizdooni.filters.LoginRequired;
import mizdooni.model.Reservation;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.ReservationService;
import mizdooni.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static mizdooni.controllers.ControllerUtils.*;

@RestController
@LoginRequired
public class ReservationController {
    @Autowired
    private RestaurantService restaurantService;
    @Autowired
    private ReservationService reserveService;

    @GetMapping("/reserves/{restaurantId}")
    public Response getReservations(@PathVariable int restaurantId,
                                    @RequestParam int table,
                                    @RequestParam(required = false) String date) {
        ControllerUtils.checkRestaurant(restaurantId, restaurantService);
        LocalDate localDate = null;

        if (date != null) {
            try {
                localDate = LocalDate.parse(date, DATE_FORMATTER);
            } catch (Exception ex) {
                throw new ResponseException(HttpStatus.BAD_REQUEST, PARAMS_BAD_TYPE);
            }
        }
        try {
            List<Reservation> reservations = reserveService.getReservations(restaurantId, table, localDate);
            return Response.ok("restaurant table reservations", reservations);
        } catch (UserNotFound ex) {
            throw new ResponseException(HttpStatus.UNAUTHORIZED, ex);
        } catch (UserNotManager | InvalidManagerRestaurant ex ) {
            throw new ResponseException(HttpStatus.FORBIDDEN, ex);
        } catch (TableNotFound ex) {
            throw new ResponseException(HttpStatus.NOT_FOUND, ex);
        } catch (Exception ex) {
            throw new ResponseException(HttpStatus.BAD_REQUEST, ex);
        }
    }

    @GetMapping("/reserves/customer/{customerId}")
    public Response getCustomerReservations(@PathVariable int customerId) {
        try {
            List<Reservation> reservations = reserveService.getCustomerReservations(customerId);
            return Response.ok("user reservations", reservations);
        } catch (UserNotFound ex) {
            throw new ResponseException(HttpStatus.UNAUTHORIZED, ex);
        } catch (UserNoAccess ex) {
            throw new ResponseException(HttpStatus.FORBIDDEN, ex);
        } catch (Exception ex) {
            throw new ResponseException(HttpStatus.BAD_REQUEST, ex);
        }
    }

    @GetMapping("/reserves/{restaurantId}/available")
    public Response getAvailableTimes(@PathVariable int restaurantId,
                                      @RequestParam int people,
                                      @RequestParam String date) {
        ControllerUtils.checkRestaurant(restaurantId, restaurantService);
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date, DATE_FORMATTER);
        } catch (Exception ex) {
            throw new ResponseException(HttpStatus.BAD_REQUEST, PARAMS_BAD_TYPE);
        }

        try {
            List<LocalTime> availableTimes = reserveService.getAvailableTimes(restaurantId, people, localDate);
            return Response.ok("available times", availableTimes);
        } catch (Exception ex) {
            throw new ResponseException(HttpStatus.BAD_REQUEST, ex);
        }
    }

    @PostMapping("/reserves/{restaurantId}")
    public Response addReservation(@PathVariable int restaurantId, @RequestBody Map<String, String> params) {
        ControllerUtils.checkRestaurant(restaurantId, restaurantService);
        if (!ControllerUtils.containsKeys(params, "people", "datetime")) {
            throw new ResponseException(HttpStatus.BAD_REQUEST, PARAMS_MISSING);
        }

        int people;
        LocalDateTime datetime;

        try {
            people = Integer.parseInt(params.get("people"));
            if (people <= 0) {
                throw new BadPeopleNumber();
            }
            datetime = LocalDateTime.parse(params.get("datetime"), DATETIME_FORMATTER);
        } catch (Exception ex) {
            throw new ResponseException(HttpStatus.BAD_REQUEST, PARAMS_BAD_TYPE);
        }

        try {
            Reservation reservation = reserveService.reserveTable(restaurantId, people, datetime);
            return Response.ok("reservation done", reservation);
        } catch (Exception ex) {
            throw new ResponseException(HttpStatus.BAD_REQUEST, ex);
        }
    }

    @PostMapping("/reserves/cancel/{reservationNumber}")
    public Response cancelReservation(@PathVariable String reservationNumber) {
        try {
            reserveService.cancelReservation(reservationNumber);
            return Response.ok("reservation cancelled");
        } catch (UserNotFound ex) {
            throw new ResponseException(HttpStatus.UNAUTHORIZED, ex);
        } catch (ReservationNotFound ex) {
            throw new ResponseException(HttpStatus.NOT_FOUND, ex);
        } catch (Exception ex) {
            throw new ResponseException(HttpStatus.BAD_REQUEST, ex);
        }
    }
}
