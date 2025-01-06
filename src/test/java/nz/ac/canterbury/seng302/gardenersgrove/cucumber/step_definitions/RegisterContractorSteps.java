package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class RegisterContractorSteps {

    @Autowired
    MockMvc mockMvc;
    private String aboutMe = "Default valid about me";
    private String location;
    private String country;
    private String city;
    private String suburb;
    private String street;
    private String postcode;
    private List<MockMultipartFile> files = new ArrayList<>();
    private MvcResult result;

    private MvcResult postRegisterContractorRequest() throws Exception {
        MockMultipartHttpServletRequestBuilder multipartRequest = (MockMultipartHttpServletRequestBuilder) MockMvcRequestBuilders.multipart("/profile/contractor")
                .with(csrf())
                .param("description", aboutMe)
                .param("location", location)
                .param("country", country)
                .param("city", city)
                .param("suburb", suburb)
                .param("street", street)
                .param("postcode", postcode);
        if (!files.isEmpty()) {
            for (MockMultipartFile file : files) {
                multipartRequest.file(file);
            }
        }
        return mockMvc.perform(multipartRequest).andReturn();

    }

    @When("I enter an about me with content {string}")
    public void iEnterAnAboutMeWithContent(String aboutMeInput) {
        aboutMe = aboutMeInput;
    }

    @And("I click the contractor Submit button")
    public void iClickTheContractorSubmitButton() throws Exception {
        result = postRegisterContractorRequest();
    }

    @Then("I remain on the contractor page, and I see the error {string} from {string}")
    public void iRemainOnTheContractorPageAndISeeTheAboutMeError(String errorMessage, String elementName) {
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("contractorRegisterTemplate", mv.getViewName());
        Assertions.assertEquals(errorMessage, mv.getModel().get(elementName));
    }

    @And("I am on the contractor registration form")
    public void iAmOnTheContractorRegistrationForm() throws Exception {
        mockMvc.perform(get("/profile/contractor"))
                .andExpect(status().isOk());
    }

    @And("I enter a location {string}, country {string}, city {string}, suburb {string}, street {string} and postcode {string}")
    public void iEnterALocationCountryCitySuburbStreetAndPostcode(String location, String country, String city, String suburb, String street, String postcode) {
        this.location = location;
        this.country = country;
        this.city = city;
        this.suburb = suburb;
        this.street = street;
        this.postcode = postcode;
    }

    @And("I submit {int} valid image and one with type {string} and size {int} MB")
    public void iSubmitValidImageWithTypeAndSizeMB(int numOfImages, String fileType, int size) {
        files.clear(); //Empty previous files if any
        for (int i = 0; i < numOfImages; i++) {
            MockMultipartFile file = new MockMultipartFile("validUpload", i + "file.jpg", "image/jpeg", new byte[1]);
            files.add(file);
        }
        byte[] content = new byte[size * 1024 * 1024];

        MockMultipartFile file = new MockMultipartFile("validUpload", "file.jpg", fileType, content);
        files.add(file);
    }

    @Then("I am redirect to profile page")
    public void iAmRedirectToProfilePage() {
        result.getResponse().getRedirectedUrl();
        Assertions.assertEquals("/profile",result.getResponse().getRedirectedUrl());
    }

}
