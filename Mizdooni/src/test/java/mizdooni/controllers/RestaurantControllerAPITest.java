package mizdooni.controllers;

import static mizdooni.controllers.ControllerUtils.TIME_FORMATTER;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.CoreMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import mizdooni.model.Restaurant;
import mizdooni.service.RestaurantService;
import mizdooni.utils.RestaurantFaker;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class RestaurantControllerAPITest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RestaurantService restaurantService;

    @Nested
    class GetRestaurant {

        private Restaurant restaurant;

        @BeforeEach
        void setUp() {
            restaurant = RestaurantFaker.createRestaurant();
        }

        @AfterEach
        void tearDown() {
            restaurant = null;
        }

        @Test
        @DisplayName("should return restaurant when restaurant is existed")
        void shouldReturnRestaurant_whenRestuarantIsExisted() throws Exception {
            int restaurantId = restaurant.getId();
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

            mockMvc.perform(get("/restaurants/" + restaurantId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                .andExpect(jsonPath("$.message", CoreMatchers.is("restaurant found")))
                .andExpect(jsonPath("$.data.id", CoreMatchers.is(restaurantId)))
                .andExpect(jsonPath("$.data.name", CoreMatchers.is(restaurant.getName())))
                .andExpect(jsonPath("$.data.description", CoreMatchers.is(restaurant.getDescription())))
                .andExpect(jsonPath("$.data.startTime", CoreMatchers.is(restaurant.getStartTime().format(TIME_FORMATTER))))
                .andExpect(jsonPath("$.data.endTime", CoreMatchers.is(restaurant.getEndTime().format(TIME_FORMATTER))))
                .andExpect(jsonPath("$.data.address.country", CoreMatchers.is(restaurant.getAddress().getCountry())))
                .andExpect(jsonPath("$.data.address.city", CoreMatchers.is(restaurant.getAddress().getCity())))
                .andExpect(jsonPath("$.data.address.street", CoreMatchers.is(restaurant.getAddress().getStreet())))
                .andExpect(jsonPath("$.data.starCount", CoreMatchers.is(restaurant.getStarCount())))
                .andExpect(jsonPath("$.data.averageRating.food", CoreMatchers.is(restaurant.getAverageRating().food)))
                .andExpect(jsonPath("$.data.averageRating.service", CoreMatchers.is(restaurant.getAverageRating().service)))
                .andExpect(jsonPath("$.data.averageRating.ambiance", CoreMatchers.is(restaurant.getAverageRating().ambiance)))
                .andExpect(jsonPath("$.data.averageRating.overall", CoreMatchers.is(restaurant.getAverageRating().overall)))
                .andExpect(jsonPath("$.data.maxSeatsNumber", CoreMatchers.is(restaurant.getMaxSeatsNumber())))
                .andExpect(jsonPath("$.data.managerUsername", CoreMatchers.is(restaurant.getManager().getUsername())))
                .andExpect(jsonPath("$.data.image", CoreMatchers.is(restaurant.getImageLink())))
                .andExpect(jsonPath("$.data.totalReviews", CoreMatchers.is(restaurant.getReviews().size())))
                .andDo(print());
        }
    }
}

