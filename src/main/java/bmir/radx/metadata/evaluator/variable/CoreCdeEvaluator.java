package bmir.radx.metadata.evaluator.variable;

import bmir.radx.metadata.evaluator.EvaluationCriterion;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.SpreadsheetReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.ACCURACY;
import static bmir.radx.metadata.evaluator.EvaluationCriterion.BASIC_INFO;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;

@Component
public class CoreCdeEvaluator {
  private final String globalCodeBookUrl = "https://docs.google.com/spreadsheets/d/1famf1rpRpLz3Q-rLJ5t-pEbetPWasxsK/export?format=xlsx&id=1famf1rpRpLz3Q-rLJ5t-pEbetPWasxsK&gid=200816038";
  private final SpreadsheetReader spreadsheetReader;

  public CoreCdeEvaluator(SpreadsheetReader spreadsheetReader) {
    this.spreadsheetReader = spreadsheetReader;
  }

  public void evaluate(List<VariableMetadataRow> rows, Consumer<EvaluationResult> consumer) throws IOException {
    int coreCde = 0;
    int totalVar = 0;
    int incorrectCoreCdeCount = 0;
    var incorrectCoreCdes = new ArrayList<Integer>();
    var allCdes = getAllCodeCdes();

    for(var row: rows){
      if(row.isTier1CDE()){
        coreCde++;
      }
      if(!isCorrectCoreCde(row, allCdes)){
        incorrectCoreCdeCount++;
        incorrectCoreCdes.add(row.rowNumber());
      }
      totalVar++;
    }

    consumer.accept(new EvaluationResult(BASIC_INFO, TOTAL_NUMBER_OF_VARIABLES, String.valueOf(totalVar)));
    consumer.accept(new EvaluationResult(BASIC_INFO, NUMBER_OF_TIER_1_CDES, String.valueOf(coreCde)));
    consumer.accept(new EvaluationResult(ACCURACY, NUMBER_OF_INCORRECT_CORE_CDES, String.valueOf(incorrectCoreCdeCount)));
    if(incorrectCoreCdeCount !=0 ){
      consumer.accept(new EvaluationResult(ACCURACY, INCORRECT_CORE_CDES_ROWS, incorrectCoreCdes.toString()));
    }
  }

  private List<String> getAllCodeCdes() throws IOException {
    var globalCodeBook = spreadsheetReader.readGlobalCodeBook(new URL(globalCodeBookUrl).openStream());
    var cdes = new ArrayList<String>();
    for(var cde: globalCodeBook){
      cdes.add(cde.variable());
    }
    return cdes;
  }

  private boolean isCorrectCoreCde(VariableMetadataRow row, List<String> allCdes){
    return row.isTier1CDE()==allCdes.contains(row.dataVariable());
  }
}
