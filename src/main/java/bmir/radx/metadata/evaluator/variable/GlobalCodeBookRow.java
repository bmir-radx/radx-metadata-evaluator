package bmir.radx.metadata.evaluator.variable;

public record GlobalCodeBookRow(int rowNumber,
                                String concept,
                                String radxGlobalPrompt,
                                String variable,
                                String responses) {
}
