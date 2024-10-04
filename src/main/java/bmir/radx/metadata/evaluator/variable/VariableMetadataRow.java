package bmir.radx.metadata.evaluator.variable;

import bmir.radx.metadata.evaluator.sharedComponents.MetadataRow;

import java.util.List;

public record VariableMetadataRow(
    Integer rowNumber,
    String dataVariable,
    Boolean isTier1CDE,
    Integer fileCount,
    Integer studyCount,
    List<String> dbGaPIDs,
    List<String> filesPerStudy,
    List<String> radxProgram,
    String label,
    String concept,
    String responses,
    String radxGlobalPrompt
) implements MetadataRow {
}
