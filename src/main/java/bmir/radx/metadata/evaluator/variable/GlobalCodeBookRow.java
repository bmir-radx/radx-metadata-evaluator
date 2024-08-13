package bmir.radx.metadata.evaluator.variable;

public record GlobalCodeBookRow(Integer rowNumber,
                                String concept,
                                String radxGlobalPrompt,
                                String variable,
                                String responses) {
}
