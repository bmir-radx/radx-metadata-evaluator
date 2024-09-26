package bmir.radx.metadata.evaluator.study;

import java.util.Date;

public record StudyMetadataRow(
    //TODO change data type of URLs from string to URL
    Integer rowNumber,
    String studyProgram,
    String studyPHS,
    String studyTitle,
    String description,
    String radxAcknowledgements,
    String nihGrantNumber,
    String rapidsLink,
    Date studyStartDate,
    Date studyEndDate,
    Date studyReleaseDate,
    Date updatedAt,
    String foaNumber,
    String foaUrl,
    String contactPiProjectLeader,
    String studyDoi,
    String cdccProvidedPublicationUrls,
    String clinicalTrialsGovUrl,
    String studyWebsiteUrl,
    String studyDesign,
    String dataTypes,
    String studyDomain,
    String nihInstituteOrCenter,
    Boolean multiCenterStudy,
    String multiCenterSites,
    String keywords,
    String dataCollectionMethod,
    Integer estimatedCohortSize,
    String studyPopulationFocus,
    String species,
    String consentDataUseLimitations,
    String studyStatus,
    Boolean hasDataFiles
) {
}
