package com.api.automation.utilities;

import com.github.javafaker.Faker;
import com.api.automation.models.request.CreateUserRequest;
import com.api.automation.models.common.Address;

import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class RandomDataGenerator {

    private static final Faker faker = new Faker(Locale.US);
    private static final Random random = new Random();

    private RandomDataGenerator() {
        throw new UnsupportedOperationException("RandomDataGenerator class cannot be instantiated");
    }

    public static CreateUserRequest createValidUser() {
        return CreateUserRequest.builder()
            .firstName(faker.name().firstName())
            .lastName(faker.name().lastName())
            .email(generateUniqueEmail())
            .username(generateUniqueUsername())
            .password(generatePassword())
            .phone(faker.phoneNumber().phoneNumber())
            .address(createAddress())
            .build();
    }

    public static Address createAddress() {
        return Address.builder()
            .street(faker.address().streetAddress())
            .city(faker.address().city())
            .state(faker.address().state())
            .zipCode(faker.address().zipCode())
            .country(faker.address().country())
            .build();
    }

    public static String generateUniqueEmail() {
        return String.format("test_%s_%d@automation.com",
            UUID.randomUUID().toString().substring(0, 8),
            System.currentTimeMillis());
    }

    public static String generateUniqueUsername() {
        return String.format("user_%s_%d",
            UUID.randomUUID().toString().substring(0, 8),
            System.currentTimeMillis());
    }

    public static String generatePassword() {
        return faker.internet().password(8, 16, true, true, true);
    }

    public static String generateRandomString(int length) {
        return faker.lorem().characters(length);
    }

    public static int generateRandomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static String generatePhoneNumber() {
        return faker.phoneNumber().phoneNumber();
    }

    public static String generateCompanyName() {
        return faker.company().name();
    }
}
