package cn.iesst.demo.model;

import java.util.List;

public record DashboardSummary(
        long bannerCount,
        long enabledBannerCount,
        long journalCount,
        long publishedJournalCount,
        long submissionCount,
        long pendingSubmissionCount,
        List<Submission> latestSubmissions
) {
}
