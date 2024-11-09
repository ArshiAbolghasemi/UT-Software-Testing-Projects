package mizdooni.utils;

import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import mizdooni.model.Address;

@Component
public class AddressFaker {

    private static Faker faker = new Faker();

    public static Address createAddress() {
        return new Address(faker.country().name(), faker.address().city(), faker.address().streetName());
    }

    public static Address createAddress(String country, String city, String street) {
        return new Address(country, city, street);
    }

}
