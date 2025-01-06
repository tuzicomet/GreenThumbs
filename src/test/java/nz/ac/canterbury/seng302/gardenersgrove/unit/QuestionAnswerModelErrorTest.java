package nz.ac.canterbury.seng302.gardenersgrove.unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.ui.Model;
import java.util.HashMap;
import java.util.Map;
import static nz.ac.canterbury.seng302.gardenersgrove.controller.MyServiceRequestsController.updateModelWithQAErrors;
import static org.mockito.Mockito.*;

class QuestionAnswerModelErrorTest {

    @Mock
    private Model model;

    @BeforeEach
    void setup(){
        model = Mockito.mock(Model.class);
    }


    @Test
    void updateModel_QuestionErrorExists_ErrorAndInputAreAdded() {
        Map<String, Object> flashAttributes = new HashMap<>();
        flashAttributes.put("question", "question");
        flashAttributes.put("errorQuestion", "question error");

        updateModelWithQAErrors(flashAttributes, model);

        verify(model).addAttribute("question", "question");
        verify(model).addAttribute("errorQuestion", "question error");
        verify(model, never()).addAttribute("answer", "answer");
        verify(model, never()).addAttribute("errorAnswer", "answer error");
    }
    @Test
    void updateModel_AnswerErrorExists_ErrorAndInputAreAdded() {
        Map<String, Object> flashAttributes = new HashMap<>();
        flashAttributes.put("answer", "answer");
        flashAttributes.put("errorAnswer", "answer error");

        updateModelWithQAErrors(flashAttributes, model);

        verify(model, never()).addAttribute("question", "Sample question");
        verify(model, never()).addAttribute("errorQuestion", "Sample question error");
        verify(model).addAttribute("answer", "answer");
        verify(model).addAttribute("errorAnswer", "answer error");
    }
    @Test
    void updateModel_NoErrors_NotAdded() {
        Map<String, Object> flashAttributes = new HashMap<>();

        updateModelWithQAErrors(flashAttributes, model);

        verify(model, never()).addAttribute("question", "Sample question");
        verify(model, never()).addAttribute("errorQuestion", "Sample question error");
        verify(model, never()).addAttribute("answer", "Sample answer");
        verify(model, never()).addAttribute("errorAnswer", "Sample answer error");
    }
}