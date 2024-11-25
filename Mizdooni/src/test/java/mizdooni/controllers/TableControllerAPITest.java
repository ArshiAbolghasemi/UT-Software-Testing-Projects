package mizdooni.controllers;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import mizdooni.exceptions.UserNotManager;
import mizdooni.exceptions.InvalidManagerRestaurant;
import mizdooni.model.Restaurant;
import mizdooni.model.Table;
import mizdooni.service.RestaurantService;
import mizdooni.service.TableService;
import mizdooni.utils.RestaurantFaker;
import mizdooni.utils.TableFaker;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class TableControllerAPITest {

    @MockBean
    private RestaurantService restaurantService;
    @MockBean
    private TableService tableService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class GetRestaaurantTables {

        static Object[][] getRestaurantsTableProvider() {
            Restaurant restaurant = RestaurantFaker.createRestaurant();
            return new Object[][] {
                {
                    restaurant,
                    TableFaker.createTables(restaurant)
                },
                {
                    restaurant,
                    Collections.emptyList()
                }
            };
        }
        @ParameterizedTest
        @MethodSource("getRestaurantsTableProvider")
        void shoudReturnRestaurantTables_whenRestaurabtIdIsValid(Restaurant restaurant, List<Table> tables) throws Exception{
            int restaurantId = restaurant.getId();
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            when(tableService.getTables(restaurantId)).thenReturn(tables);

            ResultActions response = mockMvc.perform(get("/restaurants/{restaurantId}/tables", restaurantId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", CoreMatchers.is("tables listed")))
                .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());

            for (int i = 0; i < tables.size(); i++) {
                String basePath = "$.data[" + i + "]";
                response
                    .andExpect(jsonPath(basePath + "tableNumber", CoreMatchers.is(tables.get(i).getTableNumber())))
                    .andExpect(jsonPath(basePath + "seatsNumber", CoreMatchers.is(tables.get(i).getSeatsNumber())));
            }
        }

        @Test
        void shouldReturnNotFound_whenRestauratIdIsNotExisted() throws Exception {
            int restaurantId = 111111;
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);
            mockMvc.perform(get("/restaurants/{restaurantId}/tables", Integer.toString(restaurantId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", CoreMatchers.is(false)))
                .andDo(print());
        }

        @Test
        void shouldReturnBadRequest_whenRestaurantIdIsInvalidFormat() throws Exception {
            mockMvc.perform(get("/restaurants/{restaurantId}/tables", "invalid id")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
        }
    }

    @Nested
    class AddRestaurantTable {

        @Test
        void shouldAddTable_whenParametersAreOk() throws Exception {
            Restaurant restaurant = RestaurantFaker.createRestaurant();
            int restaurantId = restaurant.getId();
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            int seatsNumber = 4;
            doNothing().when(tableService).addTable(restaurantId, seatsNumber);

            mockMvc.perform(post("/restaurants/{restaurantId}/tables", restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("seatsNumber", seatsNumber))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                .andExpect(jsonPath("$.message", CoreMatchers.is("table added")))
                .andDo(print());
        }

        @Test
        void shouldReturnNotFound_whenRestauratIdIsNotExisted() throws Exception {
            int restaurantId = 111111;
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);
            mockMvc.perform(get("/restaurants/{restaurantId}/tables", Integer.toString(restaurantId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("seatsNumber", 4))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", CoreMatchers.is(false)))
                .andDo(print());
        }

        @Test
        void shouldReturnBadRequest_whenSeatsNumberIsMissed() throws Exception {
            Restaurant restaurant = RestaurantFaker.createRestaurant();
            int restaurantId = restaurant.getId();
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

            mockMvc.perform(post("/restaurants/{restaurantId}/tables", restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("seats", 4))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", CoreMatchers.is(false)))
                .andDo(print());
        }

        static Object[][] addRestaurantInvalidSeatsNumberFormatProvider() {
            return new Object[][] {
                { 0 },
                { "" },
                { "invalid seats number" }
            };
        }
        @ParameterizedTest
        @MethodSource("addRestaurantInvalidSeatsNumberFormatProvider")
        void shouldReturnBadRequest_whenSeatsNumberIsInvalidFormat(Object seatsNumber) throws Exception {
            Restaurant restaurant = RestaurantFaker.createRestaurant();
            int restaurantId = restaurant.getId();
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

            mockMvc.perform(post("/restaurants/{restaurantId}/tables", restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("seatsNumber", seatsNumber))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", CoreMatchers.is(false)))
                .andDo(print());
        }

        static Object[][] addRestaurantsExceptionRaisedProvider() {
            return new Object[][] {
                { UserNotManager.class },
                { InvalidManagerRestaurant.class }
            };
        }
        @ParameterizedTest
        @MethodSource("addRestaurantsExceptionRaisedProvider")
        void shouldReturnBadRequest_whenExceptionIsRaised(Class<? extends Throwable> exception) throws Exception {
            Restaurant restaurant = RestaurantFaker.createRestaurant();
            int restaurantId = restaurant.getId();
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            int seatsNumber = 2;
            doThrow(exception).when(tableService).addTable(restaurantId, seatsNumber);

            mockMvc.perform(post("/restaurants/{restaurantId}/tables", restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("seatsNumber", seatsNumber))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", CoreMatchers.is(false)))
                .andDo(print());
        }
    }

}
