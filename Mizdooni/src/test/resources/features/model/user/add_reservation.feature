Feature: add reservation to user

    Scenario: add a reservation to client with no reservation
        Given a sample client with no reservation
        When a reservation is added
        Then the client should have 1 reservation

    Scenario: add a reservation to client with reservations
        Given a sample client with 2 reservations
        When a reservation is added
        Then the client should have 3 reservations

