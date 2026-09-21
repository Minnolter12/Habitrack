package com.minnolter.habitrack.domain.model

/**
 * The dashboard/statistics time filters from Section 10. [LIFETIME] is the
 * required default (Section 10) — a fresh install must never open on a
 * discouraging "0 hours today" view.
 */
enum class TimeRange {
    LIFETIME,
    TODAY,
    WEEK,
    MONTH,
    YEAR
}
