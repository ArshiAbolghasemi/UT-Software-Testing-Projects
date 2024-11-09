package mizdooni.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import mizdooni.utils.RatingFaker;

@SpringBootTest
public class RatingTest {

    static Object[][] starCountProvider() {
        return new Object[][] {
            { RatingFaker.createRating(3.5), 4 },
            { RatingFaker.createRating(4.5), 5 },
            { RatingFaker.createRating(5.0), 5 },
            { RatingFaker.createRating(6.5), 5 },
            { RatingFaker.createRating(0.0), 0 },
            { RatingFaker.createRating(Double.NaN), 0 },
        };
    }
    @ParameterizedTest
    @MethodSource("starCountProvider")
    @DisplayName("Should return correct starcount")
    void shouldReturnStarCount_whenSetOverallToRating(Rating rating, int starCount) {
        assertEquals(rating.getStarCount(), starCount);
    }
}

