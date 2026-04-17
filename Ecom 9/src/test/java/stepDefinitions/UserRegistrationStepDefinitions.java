package stepDefinitions;

import com.TDD.Ecom.dto.LoginDto;
import com.TDD.Ecom.dto.SignupDto;
import com.TDD.Ecom.model.User;
import com.TDD.Ecom.repo.UserRepo;
import com.TDD.Ecom.security.JwtTokenProvider;
import com.TDD.Ecom.service.AuthService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
public class UserRegistrationStepDefinitions {

    private SignupDto signupDto;
    private LoginDto loginDto;
    private Map<String, String> response;
    private Exception thrownException;

    private AuthService authService;
    private UserRepo userRepo;
    private PasswordEncoder passwordEncoder;

    @Before
    public void init() {
        userRepo = Mockito.mock(UserRepo.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        JwtTokenProvider fakeProvider = new JwtTokenProvider() {
            @Override
            public String generateToken(User user) {
                return "fake-jwt-token";
            }
        };

        authService = new AuthService(userRepo, passwordEncoder, fakeProvider);
        thrownException = null;
    }

    @Given("a new user provides valid details:")
    public void aNewUserProvidesValidDetails(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);
        signupDto = new SignupDto(data.get("username"), data.get("email"), data.get("password"), data.get("role"));
    }

    @When("the user submits the registration form")
    public void theUserSubmitsTheRegistrationForm() {
        try {
            Mockito.when(userRepo.findByEmail(signupDto.getEmail())).thenReturn(Optional.empty());
            Mockito.when(userRepo.findByUsername(signupDto.getUserName())).thenReturn(Optional.empty());
            Mockito.when(passwordEncoder.encode(signupDto.getPassword())).thenReturn("encodedPassword");

            User savedUser = new User();
            savedUser.setRole(signupDto.getRole());
            Mockito.when(userRepo.save(Mockito.any(User.class))).thenReturn(savedUser);

            response = authService.signup(signupDto);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the system should create an account successfully")
    public void theSystemShouldCreateAccountSuccessfully() {
        assertNotNull(response);
    }

    @Then("the response should contain {string}")
    public void theResponseShouldContain(String expectedMessage) {
        assertTrue(response.values().stream().anyMatch(msg -> msg.contains(expectedMessage)));
    }

    @When("a new user tries to register with the same email:")
    public void aNewUserTriesToRegisterWithSameEmail(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);
        signupDto = new SignupDto(data.get("username"), data.get("email"), data.get("password"), data.get("role"));

        User existingUser = new User();
        existingUser.setEmail(data.get("email"));

        Mockito.when(userRepo.findByEmail(signupDto.getEmail())).thenReturn(Optional.of(existingUser));

        try {
            authService.signup(signupDto);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("a user tries to register with invalid email:")
    public void aUserTriesToRegisterWithInvalidEmail(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);
        signupDto = new SignupDto(data.get("username"), data.get("email"), data.get("password"), data.get("role"));

        try {
            authService.signup(signupDto);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("a user tries to register with empty password:")
    public void aUserTriesToRegisterWithEmptyPassword(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);
        signupDto = new SignupDto(data.get("username"), data.get("email"), "", data.get("role"));

        try {
            authService.signup(signupDto);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the registration should fail")
    public void theRegistrationShouldFail() {
        assertNotNull(thrownException);
    }

    @Then("the error message should be {string}")
    public void theErrorMessageShouldBe(String expectedMessage) {
        assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Given("a registered user exists:")
    public void aRegisteredUserExists(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);
        User user = new User();
        user.setUsername(data.get("username"));
        user.setEmail(data.get("email"));
        user.setPassword("encodedPassword");
        user.setRole(data.get("role"));

        Mockito.when(userRepo.findByEmail(data.get("email"))).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(data.get("password"), "encodedPassword")).thenReturn(true);

        loginDto = new LoginDto(data.get("email"), data.get("password"));
    }

    @When("the user attempts to login with correct credentials")
    public void theUserAttemptsToLoginWithCorrectCredentials() {
        try {
            response = authService.login(loginDto);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the login should be successful")
    public void theLoginShouldBeSuccessful() {
        assertNotNull(response);
        assertTrue(response.containsKey("token"));
    }

    @When("the user attempts to login with incorrect password")
    public void theUserAttemptsToLoginWithIncorrectPassword() {
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        try {
            authService.login(loginDto);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the login should fail")
    public void theLoginShouldFail() {
        assertNotNull(thrownException);
    }
}
