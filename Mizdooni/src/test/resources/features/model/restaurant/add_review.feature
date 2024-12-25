Feature: add review for restaurant

    Background:
        Given a restaurant
        And a client

    Scenario: adding first review to restaurant
        When client adding a review
        Then restaurant should have 1 review that contain this review

    Scenario: adding review to restaurant that already has review
        Given 3 reviews for restaurant
        When client adding a review
        Then restaurant should have 4 reviews that contain this review

    Scenario: adding another review from client to restaurant that already add review
        Given 1 review for restaurant
        And already a review from client for restaurant
        When client adding a review
        Then restaurant should have 2 review that contain this review

