Feature: Calculate average rating

    Scenario: Calculate average rating for a list of reviews
        Given a restaurant
        And the following reviews rating for a restaurant:
            | Food | Service | Ambiance | Overall |
            | 4.0  | 5.0     | 3.0      | 4.0     |
            | 5.0  | 4.0     | 4.0      | 5.0     |
            | 3.0  | 4.0     | 5.0      | 4.0     |
        When I calculate the average rating
        Then the average rating should be:
            | Food | Service | Ambiance | Overall |
            | 4.0  | 4.3333  | 4.0      | 4.3333  |

    Scenario: Calculate average rating one reviews
        Given a restaurant
        And the following reviews rating for a restaurant:
            | Food | Service | Ambiance | Overall |
            | 4.0  | 5.0     | 3.0      | 4.0     |
        When I calculate the average rating
        Then the average rating should be:
            | Food | Service | Ambiance | Overall |
            | 4.0  | 5.0     | 3.0      | 4.0     |

    Scenario: Calculate average rating for an empty list of reviews
        Given a restaurant
        And no reviews for a restaurant
        When I calculate the average rating
        Then the average rating should be:
            | Food | Service | Ambiance | Overall |
            | 0.0  | 0.0     | 0.0      | 0.0     |
