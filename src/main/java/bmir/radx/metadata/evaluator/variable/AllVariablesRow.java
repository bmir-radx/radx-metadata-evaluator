package bmir.radx.metadata.evaluator.variable;

import java.util.List;

public record AllVariablesRow(String radxProgram, String studyName, String phsId, String fileName, List<String> variables) {
}
