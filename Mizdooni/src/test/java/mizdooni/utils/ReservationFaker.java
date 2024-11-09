package mizdooni.utils;

import java.time.LocalDateTime;

import mizdooni.model.Reservation;
import mizdooni.model.Restaurant;
import mizdooni.model.Table;
import mizdooni.model.User;

public class ReservationFaker {

    public static Reservation createReservation(User user, Restaurant restaurant, Table table) {
        return createReservation(user, restaurant, table, LocalDateTime.now());
    }

    public static Reservation createReservation(User user, Restaurant restaurant, Table table, LocalDateTime time) {
        Reservation reservation = new Reservation(user, restaurant, table, time);
        user.addReservation(reservation);
        table.addReservation(reservation);
        return reservation;
    }

}

