package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.component.CustomAuthenticationProvider;
import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ServiceRequestService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ViewRequestSteps {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    MessageSource messageSource;
    @Autowired
    private GardenService gardenService;
    @Autowired
    private UserService userService;
    @Autowired
    ServiceRequestService serviceRequestService;

    @Autowired
    private CustomAuthenticationProvider customAuthProvider;

    private String requestUrl;

    private ResultActions mvcResult;

    final String VALID_TITLE = "title";
    final String VALID_DESCRIPTION = "description";
    final String VALID_DATE = LocalDate.now().plusDays(2).format(DateTimeFormatter.ofPattern("dd/MM/uuuu"));
    final String VALID_PRICE = "10";
    final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private final String VALID_IMAGE_PATH = "/images/default.jpg";

    @And("I own a service")
    public void iOwnAService() throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1")
                        .param("imagePath", VALID_IMAGE_PATH)
        );
    }


    @When("I click on the My Service Requests link")
    public void iClickOnTheMyServiceRequestsLink() throws Exception {
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/myServiceRequests")
                .with(csrf()));
    }

    @Then("I am shown the My Service Requests page")
    public void iShouldSeeTheMyServiceRequestsPage() throws Exception {
        mvcResult.andExpect(status().isOk())
                .andExpect(view().name("myServiceRequestsTemplate"));
    }




    @When("I attempt to open a Service Request Details page")
    public void iAttemptToOpenAServiceRequestDetailsPageWithId() {
        requestUrl = "/serviceRequest/1";
    }

    @When("I attempt to open a Service Request Details page of my request")
    public void iAttemptToOpenAServiceRequestDetailsPageOfMyRequest() {
        requestUrl = "/serviceRequest/1";
    }

    @Then("I am shown the Service Request Details page")
    public void iAmShownTheServiceRequestDetailsPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(requestUrl))
                .andExpect(status().isOk())
                .andExpect(view().name("serviceRequestDetailsTemplate"))
                .andReturn().getResponse();
    }

    @And("I can see min and max values for date")
    public void iCanSeeMinAndMaxValuesForDate() throws Exception {
        ServiceRequest serviceRequest = serviceRequestService.findById(1L).get();

        MvcResult result = mockMvc.perform(get(requestUrl)).andReturn();
        ModelAndView mv =  result.getModelAndView();

        assertNotNull(mv);
        assertNotNull(mv.getModel());
        assertNotNull(mv.getViewName());

        assertEquals("serviceRequestDetailsTemplate", mv.getViewName());

        ServiceRequest modelServiceRequest = (ServiceRequest) mv.getModel().get("serviceRequest");
        assertEquals(serviceRequest.getPriceMin(), modelServiceRequest.getPriceMin());
        assertEquals(serviceRequest.getPriceMax(), modelServiceRequest.getPriceMax());
    }


    @And("I can see min and max values for price")
    public void iCanSeeMinAndMaxValuesForPrice() throws Exception {
        ServiceRequest serviceRequest = serviceRequestService.findById(1L).get();

        MvcResult result = mockMvc.perform(get(requestUrl)).andReturn();
        ModelAndView mv =  result.getModelAndView();

        assertNotNull(mv);
        assertNotNull(mv.getModel());
        assertNotNull(mv.getViewName());

        assertEquals("serviceRequestDetailsTemplate", mv.getViewName());

        ServiceRequest modelServiceRequest = (ServiceRequest) mv.getModel().get("serviceRequest");
        assertNotNull(modelServiceRequest.getDateMin());
        assertEquals(serviceRequest.getDateMin(), modelServiceRequest.getDateMin());
        assertEquals(serviceRequest.getDateMax(), modelServiceRequest.getDateMax());
    }

    @And("I can see job title and description")
    public void iCanSeeJobTitleAndDescription() throws Exception {
        ServiceRequest serviceRequest = serviceRequestService.findById(1L).get();

        MvcResult result = mockMvc.perform(get(requestUrl)).andReturn();
        ModelAndView mv =  result.getModelAndView();

        assertNotNull(mv);
        assertNotNull(mv.getModel());
        assertNotNull(mv.getViewName());

        assertEquals("serviceRequestDetailsTemplate", mv.getViewName());

        ServiceRequest modelServiceRequest = (ServiceRequest) mv.getModel().get("serviceRequest");
        assertNotNull(modelServiceRequest.getDescription());
        assertEquals(serviceRequest.getDescription(), modelServiceRequest.getDescription());
        assertNotNull(modelServiceRequest.getTitle());
        assertEquals(serviceRequest.getTitle(), modelServiceRequest.getTitle());
    }

    @Then("I am redirected to my service requests")
    public void iAmRedirectedToHomepage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(requestUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests"))
                .andReturn().getResponse();
    }

    @Then("I see a button to edit the service request and there is no apply button")
    public void iSeeAButtonToEditTheServiceRequest() throws Exception {

        MvcResult result = mockMvc.perform(get(requestUrl)).andReturn();
        ModelAndView mv =  result.getModelAndView();

        assertNotNull(mv);
        assertNotNull(mv.getModel());
        assertNotNull(mv.getViewName());

        assertEquals("serviceRequestDetailsTemplate", mv.getViewName());

//        This is as close as we can get to checking for the presence of buttons.
//        The boolean isOwner controls the presence of buttons in the html.
//        It is checked again in manual testing.
        Boolean isOwner = (boolean) mv.getModel().get("isOwner");
        assertEquals(true, isOwner);
    }

    @Then("I can see a button to apply the service request and I cannot edit it")
    public void iCannotSeeAButtonToApplyTheServiceRequest() throws Exception {

        MvcResult result = mockMvc.perform(get(requestUrl)).andReturn();
        ModelAndView mv =  result.getModelAndView();

        assertNotNull(mv);
        assertNotNull(mv.getModel());
        assertNotNull(mv.getViewName());

        assertEquals("serviceRequestDetailsTemplate", mv.getViewName());

//        This is as close as we can get to checking for the presence of buttons.
//        The boolean isOwner controls the presence of buttons in the html.
//        It is checked again in manual testing.
        Boolean isOwner = (boolean) mv.getModel().get("isOwner");
        assertEquals(false, isOwner);
    }
}
