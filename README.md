# radx-metadata-evaluator
# RADx Metadata Evaluator

The **RADx Metadata Evaluator** is a tool for evaluating the quality of metadata related to studies and data files. It identifies issues and generates detailed reports to ensure metadata compliance with standards.

---
## Evaluation Criteria for Metadata Instances

The evaluation criteria encompass several key aspects to ensure the quality and reliability of metadata. These include:
- **Completeness**: Percentage of fields with a non-empty value
- **Consistency**: Examine metadata across a collection to ensure that values expected to have the same content are identical.
- **Accuracy**: Assess the correctness of metadata values in accurately representing the actual data.
- **Validity**: Verify whether metadata conform to predefined schemas.
- **Accessibility**: Check if provided URLs are resolvable and accessible.
- **Controlled Vocabulary Consistency**: Ensures consistent use of controlled vocabularies.
- **Uniqueness**: Ensures that metadata instances are distinct and not duplicated across the system.
- **Linguistic Quality**: Ensures that values are both grammatically and lexically accurate.
- **Structural Quality**: Assesses whether the study description text is well-organized and follows a clear, logical structure, enabling easy comprehension and alignment with predefined templates or guidelines.

For a more detailed explanation of these criteria and their implementation, please refer to this [report](https://docs.google.com/document/d/1Z4uRYGnmJKZjeeHkicVIzZEIWuluOxIkUiyMtU0f78o/edit?tab=t.0#heading=h.7lcg48y7eqi9).

---

## Inputs

The evaluator requires the following inputs (optional as specified):

| Input Option  | Description                                                                                  | Required |
|---------------|----------------------------------------------------------------------------------------------|----------|
| `--o`         | Path to the output directory where evaluation reports will be generated                      | Yes      |
| `--s`         | Path to the study metadata spreadsheet                                                       | No       |
| `--d`         | Path to the data file metadata folder                                                        | No       | |
The tool utilizes the spreadsheet validator to validate study metadata (in spreadsheet format), including aspects such as number type, cardinality, and more. Please ensure the study metadata headers align with the [Study Metadata Template](https://openview.metadatacenter.org/templates/https:%2F%2Frepo.metadatacenter.org%2Ftemplates%2Faf3f6a0d-9f9f-4db2-898d-0a19d2dd0bb6).

---

## Usage

Run the evaluator by specifying the study metadata spreadsheet, data file metadata folder and the output directory.

### Example Command:
```bash
java -jar radx-metadata-evaluator.jar --s /path/to/study_metadata.xlsx --d /path/to/data_file_metadata --o /path/to/output
```

You can also choose to evaluate only the study metadata or the data file metadata by providing just the relevant inputs.
### Example: Evaluate Study Metadata Only
```bash
java -jar radx-metadata-evaluator.jar --s /path/to/study_metadata.xlsx --o /path/to/output
```
### Evaluate Data File Metadata Only
```bash
java -jar radx-metadata-evaluator.jar --d /path/to/data_file_metadata --o /path/to/output
```
