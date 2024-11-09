package mizdooni.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.github.javafaker.Faker;

import mizdooni.assertion.ResponseAssertion;
import mizdooni.exceptions.DuplicatedUsernameEmail;
import mizdooni.exceptions.InvalidEmailFormat;
import mizdooni.exceptions.InvalidUsernameFormat;
import mizdooni.model.Address;
import mizdooni.model.User;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.UserService;
import mizdooni.utils.AddressFaker;
import mizdooni.utils.UserFaker;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    class Login {

        private Map<String, String> prepareParams(String username, String password) {
            Map<String, String> params = new HashMap<>();
            params.put("username", username);
            params.put("password", password);
            return params;
        }

        @Test
        @DisplayName("Should login successfully when username and password are Valid")
        void shouldLoginSuccessfully_whenUsernameAndPasswordAreValid() {
            String password = Faker.instance().internet().password();
            User user = UserFaker.createClient(password);
            String username = user.getUsername();

            when(userService.login(username, password)).thenReturn(true);
            when(userService.getCurrentUser()).thenReturn(user);

            Response response = authenticationController.login(prepareParams(username, password));

            ResponseAssertion.checkResponse(response, HttpStatus.OK, true, user, null, "login successful");
        }

        @Test
        @DisplayName("should not login when user name or password are not valid")
        void shouldNotLogin_whenUsernameOrPasswordAreNotValid() {
            String username = Faker.instance().name().username();
            String password = Faker.instance().internet().password();

            when(userService.login(username, password))
                .thenReturn(false);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                authenticationController.login(prepareParams(username, password));
            });

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
            assertEquals("invalid username or password", exception.getMessage());
        }

     static Object[][] missingCredentialsProvider() {
            return new Object[][] {
                { Faker.instance().name().username(), null },
                { Faker.instance().name().username(), "" },
                { null, Faker.instance().internet().password() },
                { "", Faker.instance().internet().password() },
                { null, null },
                { "", "" },
            };
        }
        @ParameterizedTest
        @MethodSource("missingCredentialsProvider")
        @DisplayName("should get bad request if credentials are missed")
        void shouldNotLogin_whenUsernameOrPasswordAreMissed(String username, String password) {
            ResponseException exception = assertThrows(ResponseException.class, () -> {
                authenticationController.login(prepareParams(username, password));
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals("parameters missing", exception.getMessage());
        }
    }

    @Nested
    class CurrentUser {

        @Test
        @DisplayName("should return current user if user is logged in")
        void shouldReturnCurrentUser_whenUserIsLoggedIn() {
            User user = UserFaker.createClient();
            when(userService.getCurrentUser()).thenReturn(user);

            Response response = authenticationController.user();

            ResponseAssertion.checkResponse(response, HttpStatus.OK, true, user, null, "current user");
        }

        @Test
        void shouldGetUnauthorized_whenUserIsNotLoggedIn() {
            when(userService.getCurrentUser()).thenReturn(null);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                authenticationController.user(); 
            });

            assertEquals(exception.getStatus(), HttpStatus.UNAUTHORIZED);
            assertEquals(exception.getMessage(), "no user logged in");
        }
    }

    @Nested
    class SignUp {

        private Map<String, Object> prepareParams(String username, String password, String email, String city,
            String country, User.Role role) {
            return Map.of(
                "username", username,
                "password", password,
                "email", email,
                "address", Map.of(
                    "city", city,
                    "country", country
                ),
                "role", role.name()
            );
        }

        @Test
        void shouldSignUpUser_whenCredentialsAreValid() throws Exception {
            String password = Faker.instance().internet().password();
            Address address = AddressFaker.createAddress();
            User user = UserFaker.createClient(password, address);

            doNothing().when(userService).signup(eq(user.getUsername()), eq(password), eq(user.getEmail()), 
                any(Address.class), eq(user.getRole()));
            when(userService.login(user.getUsername(), password)).thenReturn(true);
            when(userService.getCurrentUser()).thenReturn(user);

            Map<String, Object> params = prepareParams(user.getUsername(), password, user.getEmail(), address.getCity(),
                address.getCountry(), user.getRole());
            Response response = authenticationController.signup(params);

            ResponseAssertion.checkResponse(response, HttpStatus.OK, true, user, null, "signup successful");
            verify(userService, times(1)).signup(eq(user.getUsername()), eq(password), eq(user.getEmail()), any(Address.class),
                eq(user.getRole()));
            verify(userService, times(1)).login(eq(user.getUsername()), eq(password));
        }

        static Object[][] sigUpMissingParametersPrvider() {
            return new Object[][] {
                {
                    Map.of(
                        "username", Faker.instance().name().username(),
                        "password", Faker.instance().internet().password(),
                        "email", Faker.instance().internet().emailAddress(),
                        "address", Map.of(
                            "city", Faker.instance().address().city(),
                            "country", Faker.instance().address().country()
                        )
                    )
                },
                {
                    Map.of(
                        "username", Faker.instance().name().username(),
                        "password", Faker.instance().internet().password(),
                        "email", "",
                        "address", Map.of(
                            "city", Faker.instance().address().city(),
                            "country", ""
                        ),
                        "role", User.Role.client.name()
                    )
                },
                {
                    Map.of(
                        "password", Faker.instance().internet().password(),
                        "email", "",
                        "address", Map.of(
                            "country", Faker.instance().address().city()
                        ),
                        "role", User.Role.manager.name()
                    )
                },
                {
                    Map.of(
                        "user", Faker.instance().name().username(),
                        "password", Faker.instance().internet().password(),
                        "email", Faker.instance().internet().emailAddress(),
                        "address", Map.of(
                            "country", Faker.instance().address().city(),
                            "city", Faker.instance().address().country()
                        ),
                        "role", User.Role.client.name()
                    )
                }
            };
        }
        @ParameterizedTest
        @MethodSource("sigUpMissingParametersPrvider")
        @DisplayName("Shoul throw bad request exception when parameters is missed")
        void shouldThrowBadRequestException_whenParametersIsMissed(Map<String, Object> params) {
            ResponseException exception = assertThrows(ResponseException.class, () -> {
                authenticationController.signup(params);
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals("parameters missing", exception.getMessage());
        }

        static Object[][] invalidSignupParameterProvider() {
            return new Object[][] {
                {
                    Map.of(
                        "username", Faker.instance().name().username(),
                        "password", Faker.instance().internet().password(),
                        "email", Faker.instance().internet().emailAddress(),
                        "address", "city address",
                        "role", User.Role.client.name()
                    )
                },
                {
                    Map.of(
                        "username", Faker.instance().name().username(),
                        "password", Faker.instance().internet().password(),
                        "email", Faker.instance().internet().emailAddress(),
                        "address", Map.of(
                            "city", Faker.instance().address().city(),
                            "country", Faker.instance().address().country()
                        ),
                        "role", "fake role"
                    )
                }
            };
        }
        @ParameterizedTest
        @MethodSource("invalidSignupParameterProvider")
        @DisplayName("Shoul throw bad request exception when parameters structure are invalid")
        void shouldThrowBadRequestException_whenParametersStructureAreNotValid(Map<String, Object> params) {
            ResponseException exception = assertThrows(ResponseException.class, () -> {
                authenticationController.signup(params);
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals("bad parameter type", exception.getMessage());
        }

        static Object[][] invalidCredentialsFormatProvider() {
            return new Object[][] {
                {
                    "user123",
                    Faker.instance().internet().password(),
                    Faker.instance().internet().emailAddress(),
                    AddressFaker.createAddress(),
                    User.Role.client,
                    InvalidUsernameFormat.class
                },
                {
                    Faker.instance().name().username(),
                    Faker.instance().internet().password(),
                    "fakeemail.com",
                    AddressFaker.createAddress(),
                    User.Role.manager,
                    InvalidEmailFormat.class
                }
            };
        }
        @ParameterizedTest
        @MethodSource("invalidCredentialsFormatProvider")
        @DisplayName("Should throw invalid username exception when credentials format are not valid")
        void shouldThrowInvalidUsernameException_whenFormatCredentialsAreNotValid(String username, String password, 
            String email, Address address, User.Role role, Class<? extends Throwable> ex) throws Exception {

            doThrow(ex).when(userService).signup(eq(username), eq(password), eq(email), any(Address.class), eq(role));

            Map<String, Object> params = prepareParams(username, password, email, address.getCity(),
                address.getCountry(), role);
            ResponseException exception = assertThrows(ResponseException.class, () -> { 
                authenticationController.signup(params); 
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals(ex.getSimpleName(), exception.getError());
        }

        @Test
        @DisplayName("Should throw duplicate exception when user is existed with same credential")
        void shouldThrowDuplicateException_whenUserIsExistedWithSameCredentials() throws Exception {
            String username = Faker.instance().name().username();
            String password = Faker.instance().internet().password();
            String email = Faker.instance().internet().emailAddress();
            Address address = AddressFaker.createAddress();
            User.Role role = User.Role.client;

            doThrow(DuplicatedUsernameEmail.class).when(userService)
                .signup(eq(username), eq(password), eq(email), any(Address.class), eq(role));

            Map<String, Object> params = prepareParams(username, password, email, address.getCity(),
                address.getCountry(), role);
            ResponseException exception = assertThrows(ResponseException.class, () -> {
                authenticationController.signup(params); 
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals(DuplicatedUsernameEmail.class.getSimpleName(), exception.getError());

        }
    }

    @Nested
    class LogOut {

        @Test
        @DisplayName("should logout successfully when user is logged in")
        void shouldLogOutSuccessFullt_whenUserIsLoggedIn() {
            when(userService.logout()).thenReturn(true);

            Response response = authenticationController.logout();

            ResponseAssertion.checkResponse(response, HttpStatus.OK, true, null, null, "logout successful");
            verify(userService, times(1)).logout();
        }

        @Test
        @DisplayName("should return unauthorized response when user is not logged in")
        void shouldReturnUnauthorizedResponse_whenUserIsNotLoggedIn() {
            when(userService.logout()).thenReturn(false);

            ResponseException exception = assertThrows(ResponseException.class, () -> { 
                authenticationController.logout(); 
            });

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
            assertEquals("no user logged in", exception.getMessage());
        }
    }

    @Nested
    class ValidateUsername {
       
        @Test
        @DisplayName("should return ok response when username is valid and available")
        void shouldReturnOkResponse_whenUsernameIsValidAndAvailable() {
            String username = "validusername";
            
            when(userService.usernameExists(username)).thenReturn(false);

            Response response = authenticationController.validateUsername(username);
            ResponseAssertion.checkResponse(response, HttpStatus.OK, true, null, null, "username is available");
        }

        @Test
        @DisplayName("should return bad request when username is not valid")
        void shoulReturnBadRequest_whenUsernameIsNotValid() {
            ResponseException exception = assertThrows(ResponseException.class, () -> { 
                authenticationController.validateUsername("user@123"); 
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals("invalid username format", exception.getMessage());
        }

        @Test
        @DisplayName("should return conflict response when username is not available")
        void shouldReturnConflictResponse_whenUsernameIsNotAvailable() {
            String username = "usernameValid";
        
            when(userService.usernameExists(username)).thenReturn(true);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                authenticationController.validateUsername(username);
            });

            assertEquals(HttpStatus.CONFLICT, exception.getStatus());
            assertEquals("username already exists", exception.getMessage());
        }

    }

    @Nested
    class ValidateEmail {
        @Test
        @DisplayName("should return ok response when email is valid and available")
        void shouldReturnOkResponse_whenEmailIsValidAndAvailable() {
            String email = Faker.instance().internet().emailAddress();

            when(userService.emailExists(email)).thenReturn(false);

            Response response = authenticationController.validateEmail(email);
            ResponseAssertion.checkResponse(response, HttpStatus.OK, true, null, null, "email not registered");
        }

        @Test
        @DisplayName("should return bad request when email is not valid")
        void shoulReturnBadRequest_whenEmailIsNotValid() {
            ResponseException exception = assertThrows(ResponseException.class, () -> { 
                authenticationController.validateEmail("fakeemail.com"); 
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals("invalid email format", exception.getMessage());
        }

        @Test
        @DisplayName("should return conflict response when email is not available")
        void shouldReturnConflictResponse_whenUsernameIsNotAvailable() {
            String email = Faker.instance().internet().emailAddress();

            when(userService.emailExists(email)).thenReturn(true);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                authenticationController.validateEmail(email);
            });

            assertEquals(HttpStatus.CONFLICT, exception.getStatus());
            assertEquals("email already registered", exception.getMessage());
        }
    }

}
