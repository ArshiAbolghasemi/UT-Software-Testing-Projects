package mizdooni.controllers;

import static mizdooni.controllers.ControllerUtils.TIME_FORMATTER;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;

import mizdooni.model.Restaurant;
import mizdooni.model.RestaurantSearchFilter;
import mizdooni.model.User;
import mizdooni.response.PagedList;
import mizdooni.service.RestaurantService;
import mizdooni.service.UserService;
import mizdooni.utils.RestaurantFaker;
import mizdooni.utils.UserFaker;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class RestaurantControllerAPITest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RestaurantService restaurantService;
    @MockBean
    private UserService userService;

    private void checkRestaurantResponse(ResultActions response, String jsonBasePath, Restaurant restaurant) 
        throws Exception {
        response
            .andExpect(jsonPath(jsonBasePath + ".id", CoreMatchers.is(restaurant.getId())))
            .andExpect(jsonPath(jsonBasePath + ".name", CoreMatchers.is(restaurant.getName())))
            .andExpect(jsonPath(jsonBasePath + ".description", CoreMatchers.is(restaurant.getDescription())))
            .andExpect(jsonPath(jsonBasePath + ".startTime", CoreMatchers.is(restaurant.getStartTime().format(TIME_FORMATTER))))
            .andExpect(jsonPath(jsonBasePath + ".endTime", CoreMatchers.is(restaurant.getEndTime().format(TIME_FORMATTER))))
            .andExpect(jsonPath(jsonBasePath + ".address.country", CoreMatchers.is(restaurant.getAddress().getCountry())))
            .andExpect(jsonPath(jsonBasePath + ".address.city", CoreMatchers.is(restaurant.getAddress().getCity())))
            .andExpect(jsonPath(jsonBasePath + ".address.street", CoreMatchers.is(restaurant.getAddress().getStreet())))
            .andExpect(jsonPath(jsonBasePath + ".starCount", CoreMatchers.is(restaurant.getStarCount())))
            .andExpect(jsonPath(jsonBasePath + ".averageRating.food", CoreMatchers.is(restaurant.getAverageRating().food)))
            .andExpect(jsonPath(jsonBasePath + ".averageRating.service", CoreMatchers.is(restaurant.getAverageRating().service)))
            .andExpect(jsonPath(jsonBasePath + ".averageRating.ambiance", CoreMatchers.is(restaurant.getAverageRating().ambiance)))
            .andExpect(jsonPath(jsonBasePath + ".averageRating.overall", CoreMatchers.is(restaurant.getAverageRating().overall)))
            .andExpect(jsonPath(jsonBasePath + ".maxSeatsNumber", CoreMatchers.is(restaurant.getMaxSeatsNumber())))
            .andExpect(jsonPath(jsonBasePath + ".managerUsername", CoreMatchers.is(restaurant.getManager().getUsername())))
            .andExpect(jsonPath(jsonBasePath + ".image", CoreMatchers.is(restaurant.getImageLink())))
            .andExpect(jsonPath(jsonBasePath + ".totalReviews", CoreMatchers.is(restaurant.getReviews().size())));
    }

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

            ResultActions response = mockMvc.perform(get("/restaurants/{restaurantId}", Integer.toString(restaurantId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                .andExpect(jsonPath("$.message", CoreMatchers.is("restaurant found")))
                .andExpect(jsonPath("$.data").isMap())
                .andDo(print());

            checkRestaurantResponse(response, "$.data", restaurant);
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
                    1,
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
            PagedList<Restaurant> listRestaurants = new PagedList<>(restaurants, page, 10);
            when(restaurantService.getRestaurants(eq(page), any(RestaurantSearchFilter.class)))
                .thenReturn(listRestaurants);

            ResultActions response = mockMvc.perform(get("/restaurants")
                .param("page", Integer.toString(page))
                .params(new LinkedMultiValueMap<>() {{ params.forEach((key, value) -> add(key, value)); }})
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                .andExpect(jsonPath("$.message", CoreMatchers.is("restaurants listed")))
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.page", CoreMatchers.is(page)))
                .andExpect(jsonPath("$.data.hasNext", CoreMatchers.is(listRestaurants.hasNext())))
                .andExpect(jsonPath("$.data.totalPages", CoreMatchers.is(listRestaurants.totalPages())))
                .andExpect(jsonPath("$.data.pageList").isArray())
                .andDo(print());
            for (int i = 0; i < listRestaurants.getPageList().size(); i++) {
                Restaurant restaurant = listRestaurants.getPageList().get(i);
                checkRestaurantResponse(response, "$.data.pageList[" + i + "]", restaurant);
            }
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

    @Nested
    class GetManagerRestaurants {

        static Object[][] getManagerRestaurantsValidManagerIdProvider() {
            return new Object[][] {
                {
                    UserFaker.createManager(),
                    List.of(RestaurantFaker.createRestaurant(), RestaurantFaker.createRestaurant())
                },
                {
                    UserFaker.createManager(),
                    Collections.emptyList()
                }
            };
        }
        @ParameterizedTest
        @MethodSource("getManagerRestaurantsValidManagerIdProvider")
        void shouldReturnMangerRestaurants_whenRestaurantMangerIdIsValid(User manager, List<Restaurant> restaurants) 
            throws Exception {
            int managerId = manager.getId();
            when(userService.getManager(eq(managerId))).thenReturn(manager);
            when(restaurantService.getManagerRestaurants(eq(managerId))).thenReturn(restaurants);

            ResultActions response = mockMvc.perform(get("/manager/{managerId}/restaurants", Integer.toString(managerId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", CoreMatchers.is("manager restaurants listed")))
                .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());
            for (int i = 0; i < restaurants.size(); i++) {
                Restaurant restaurant = restaurants.get(i);
                checkRestaurantResponse(response, "$.data[" + i + "]", restaurant);
            }
        }

        @Test
        void shouldReturnBadRequest_whenManagerIdIsInvalid() throws Exception {
            int managerId = 111111;
            when(userService.getManager(eq(managerId))).thenReturn(null);
            mockMvc.perform(get("/manager/{managerId}/restaurants", Integer.toString(managerId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
        }

        @Test
        void shouldReturnBadRequest_whenManagerIdIsInvalidFormat() throws Exception {
            mockMvc.perform(get("/manager/{managerId}/restaurants", "invalid manager id format")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
        }
    }

    @Nested
    class ValidateRestaurantName {

        @Test
        void shouldReturnConflictResponse_whenResturantWithChosenNameIsExisted() throws Exception {
            String name = "bikini bottom";
            when(restaurantService.restaurantExists(eq(name))).thenReturn(true);

            mockMvc.perform(get("/validate/restaurant-name")
                .contentType(MediaType.APPLICATION_JSON)
                .param("data", name))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", CoreMatchers.is("restaurant name is taken")))
                .andExpect(jsonPath("$.success", CoreMatchers.is(false)))
                .andDo(print());
        }

        @Test
        void shouldReturnOkResponse_whenRestaurantWithChosenNameIsNotExisted() throws Exception {
            String name = "feri kasif";
            when(restaurantService.restaurantExists(eq(name))).thenReturn(false);

            mockMvc.perform(get("/validate/restaurant-name")
                .contentType(MediaType.APPLICATION_JSON)
                .param("data", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", CoreMatchers.is("restaurant name is available")))
                .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                .andDo(print());
        }
    }

    @Nested
    class GetRestaurantTypes {

        @Test
        void shouldReturnRestaurantTypes() throws Exception {
            Set<String> types = Set.of("Italian", "Chinese", "Indian", "Mexican");
            when(restaurantService.getRestaurantTypes()).thenReturn(types);

            mockMvc.perform(get("/restaurants/types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                .andExpect(jsonPath("$.message", CoreMatchers.is("restaurant types")))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", containsInAnyOrder(types.toArray())))
                .andDo(print());
        }

        @Test
        public void shouldReturnBadRequest_whenExceptionIsThrown() throws Exception {
            when(restaurantService.getRestaurantTypes()).thenThrow(new RuntimeException("Some error"));

            mockMvc.perform(get("/restaurants/types")
                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.success", CoreMatchers.is(false)))
               .andExpect(jsonPath("$.message", CoreMatchers.is("Some error")))
               .andDo(print());
        }
    }

    @Nested
    class GetRestaurantLocations {

        @Test
        void shouldReturnRestauratnlocations() throws Exception {
            Map<String, Set<String>> locations = Map.of(
                "tehroon", Set.of("feri kasif","khanlari"),
                "bikini_bottom", Set.of("The Krusty Krab")
            );

            when(restaurantService.getRestaurantLocations()).thenReturn(locations);

            mockMvc.perform(get("/restaurants/locations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                .andExpect(jsonPath("$.message", CoreMatchers.is("restaurant locations")))
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.tehroon", containsInAnyOrder(locations.get("tehroon").toArray())))
                .andExpect(jsonPath("$.data.bikini_bottom", containsInAnyOrder(locations.get("bikini_bottom").toArray())))
                .andDo(print());
        }

        @Test
        void shouldReturnBadRequest_whenExceptionIsThrown() throws Exception {
            when(restaurantService.getRestaurantLocations()).thenThrow(RuntimeException.class);

            mockMvc.perform(get("/restaurants/locations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", CoreMatchers.is(false)))
                .andDo(print());
        }
    }
}

