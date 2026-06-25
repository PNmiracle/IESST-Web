package cn.iesst.demo.model;

public record Journal(
        Long id,
        String type,
        String title,
        String field,
        String indexType,
        String cycle,
        String description,
        String imageUrl,
        String journalLevel,
        String impactFactorLabel,
        Double impactFactorValue,
        String journalPartition,
        String acceptanceTime,
        String submissionDeadlineText,
        String submissionDeadlineDate,
        String disciplineCategory,
        String casZone,
        String jcrQuartile,
        Long viewCount,
        String documentName,
        String documentUrl,
        boolean published
) {}
