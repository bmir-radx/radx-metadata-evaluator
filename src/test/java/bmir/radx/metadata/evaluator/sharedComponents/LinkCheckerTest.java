package bmir.radx.metadata.evaluator.sharedComponents;

import bmir.radx.metadata.evaluator.dataFile.FieldsCollector;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LinkCheckerTest {
  @Mock
  private FieldsCollector fieldsCollector;

  @InjectMocks
  private LinkChecker linkChecker;
  private URLCount urlCount;
  private List<SpreadsheetValidationResult> validationResults;

  @BeforeEach
  public void setUp(){
    MockitoAnnotations.openMocks(this);
    urlCount = spy(new URLCount(0,0,0));
    validationResults = new ArrayList<>();
  }

  @Test
  public void testCheckUrlResolvable_AllValidUrls() {
    String urlString = "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC9229995/";
    linkChecker.checkUrlResolvable(urlString, 1, "TestField", urlCount, validationResults);

    verify(urlCount, times(1)).incrementTotalURL();
    verify(urlCount, times(1)).incrementResolvableURL();
    verify(urlCount, never()).incrementUnresolvableURL();
    assertTrue(validationResults.isEmpty());
  }

}
