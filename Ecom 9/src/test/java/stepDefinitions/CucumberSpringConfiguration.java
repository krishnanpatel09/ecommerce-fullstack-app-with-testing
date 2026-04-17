package stepDefinitions;

import com.TDD.Ecom.EcomApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = EcomApplication.class)
public class CucumberSpringConfiguration {
    // This class should be empty
    // Its purpose is to enable Spring support in Cucumber tests
} 