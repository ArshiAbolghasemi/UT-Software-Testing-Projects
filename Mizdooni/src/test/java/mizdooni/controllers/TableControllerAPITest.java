package mizdooni.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

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
        void shouldReturnBadRequest_whenRestauratIdIsNotExisted() throws Exception {
            int restaurantId = 111111;
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

}
