package mizdooni.controllers;

import static mizdooni.controllers.ControllerUtils.TIME_FORMATTER;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;

import mizdooni.model.Restaurant;
import mizdooni.model.RestaurantSearchFilter;
import mizdooni.response.PagedList;
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

            mockMvc.perform(get("/restaurants/{restaurantId}", Integer.toString(restaurantId))
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

        @Test
        @DisplayName("should return not found response when restaurant is not existed")
        void shouldReturnNotFoundResponse_whenRestaurantIsNotExisted() throws Exception {
            int restaurantId = 111111;
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);

            mockMvc.perform(get("/restaurants/{restaurantId}", Integer.toString(restaurantId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", CoreMatchers.is(false)))
                .andExpect(jsonPath("$.message", CoreMatchers.is("restaurant not found")))
                .andDo(print());
        }

        @ParameterizedTest
        @DisplayName("should return not found response when restaurant is not existed")
        @ValueSource(strings = {"invalid"})
        void shouldReturnBadRequest_whenRestaurantIdIsInvalid(String restaurantId) throws Exception {
            mockMvc.perform(get("/restaurants/{restaurantId}", restaurantId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
        }
    }

    @Nested
    class GetRestaurants {

        static Object[][] getRestauarantsProvider() {
            List<Restaurant> restaurants = List.of(RestaurantFaker.createRestaurant(),
                RestaurantFaker.createRestaurant());
            return new Object[][] {
                {
                    1,
                    Map.of(
                        "name",  "The Krusty Krab",
                        "sort", "rating",
                        "order", "asc"
                    ),
                    restaurants,
                },
                {
                    5,
                    Map.of(
                        "type", "italian",
                        "sort", "reviews"
                    ),
                    restaurants
                },
                {
                    3,
                    Map.of(
                        "location", "bikini bottom"
                    ),
                    restaurants
                }
            };
        }
        @ParameterizedTest
        @MethodSource("getRestauarantsProvider")
        void shouldReturnCorrectListOfRestaurants(int page, Map<String, String> params,
            List<Restaurant> restaurants) throws Exception {
            RestaurantSearchFilter filters = new RestaurantSearchFilter();
            if (params.containsKey("name")) {
                filters.setName(params.get("name"));
            }
            if (params.containsKey("type")) {
                filters.setType(params.get("type"));
            }
            if (params.containsKey("location")) {
                filters.setLocation(params.get("location"));
            }
            if (params.containsKey("sort")) {
                filters.setSort(params.get("sort"));
            }
            if (params.containsKey("order")) {
                filters.setOrder(params.get("order"));
            }
            when(restaurantService.getRestaurants(page, filters))
                .thenReturn(new PagedList<>(restaurants, page, 10));

            mockMvc.perform(get("/restaurants")
                .param("page", Integer.toString(page))
                .params(new LinkedMultiValueMap<>() {{ params.forEach((key, value) -> add(key, value)); }})
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
        }

        static Object[][] getRestaurantsInvalidProvider() {
            return new Object[][] {
                {
                    Map.of(
                        "name", "feri kassif",
                        "location", "tehroon"
                    )
                },
                {
                    Map.of(
                        "page", "1",
                        "sort", "reviews",
                        "order", "invalid order"
                    )
                },
                {
                    Map.of(
                        "page", "5",
                        "sort", "invalid sort"
                    )
                },
                {
                    Map.of(
                        "page", "invalid page"
                    )
                }
            };
        }
        @ParameterizedTest
        @MethodSource("getRestaurantsInvalidProvider")
        void shouldReturnBadRequest_whenParametersIsMissedOrInvalid(Map<String, String> params) throws Exception {
            mockMvc.perform(get("/restaurants")
                .params(new LinkedMultiValueMap<>() {{ params.forEach((key, value) -> add(key, value)); }})
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
        }
    }
}

