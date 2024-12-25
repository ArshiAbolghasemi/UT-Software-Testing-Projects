package mizdooni.cucumber.scenarios.model.user;

import static org.junit.Assert.assertEquals;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import mizdooni.model.Restaurant;
import mizdooni.model.User;
import mizdooni.utils.ReservationFaker;
import mizdooni.utils.RestaurantFaker;
import mizdooni.utils.TableFaker;
import mizdooni.utils.UserFaker;

public class AddReservation {

    private User client;

    private void add_reservation(User client) {
        Restaurant restaurant = RestaurantFaker.createRestaurant();
        TableFaker.createTables(restaurant);
        ReservationFaker.createReservation(client, restaurant, restaurant.getTables().getFirst());
    }

    private void add_reservation(User client, int countReservations) {
        for (int i =0; i < countReservations; i++) {
            add_reservation(client);
        }
    }

    @Given("a sample client with no reservation")
    public void a_sample_client_with_no_resrvation() {
        client = UserFaker.createClient();
    }

    @Given("a sample client with 2 reservations")
    public void a_sample_client_with_2_reservations() {
        client = UserFaker.createClient();
        add_reservation(client, 2);
    }

    @When("a reservation is added")
    public void a_reservation_is_added() {
        add_reservation(client);
    }

    @Then("the client should have {int} reservation(s)")
    public void the_client_should_have_reservations(int expectedReservationCount) {
        assertEquals(expectedReservationCount, client.getReservations().size());
    }
}

