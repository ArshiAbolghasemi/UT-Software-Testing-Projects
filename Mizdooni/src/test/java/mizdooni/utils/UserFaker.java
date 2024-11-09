package mizdooni.utils;

import com.github.javafaker.Faker;

import mizdooni.model.Address;
import mizdooni.model.User;

public class UserFaker {

    private static Faker faker = Faker.instance();

    public static User createClient(String password) {
        return new User(faker.name().username(), password, faker.internet().emailAddress(), AddressFaker.createAddress(),
                User.Role.client);
    }

    public static User createClient() {
        return new User(faker.name().username(), faker.internet().password(), faker.internet().emailAddress(),
                AddressFaker.createAddress(), User.Role.client);
    }

    public static User createClient(String password, Address address) {
        return new User(faker.name().username(), faker.internet().password(), faker.internet().emailAddress(), address, 
            User.Role.client);
    }

    public static User createManager(String password) {
        return new User(faker.name().username(), password, faker.internet().emailAddress(), AddressFaker.createAddress(),
                User.Role.manager);
    }

    public static User createManager() {
        return new User(faker.name().username(), faker.internet().password(), faker.internet().emailAddress(),
                AddressFaker.createAddress(), User.Role.manager);
    }

}
