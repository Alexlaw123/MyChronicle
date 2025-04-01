package com.openlattice.chronicle.models

import com.openlattice.chronicle.android.ChronicleSample
import java.time.OffsetDateTime

data class ExtendedChronicleUsageEvent(
    val appPackageName: String,
    val className: String?,  // ✅ 新增字段
    val interactionType: String,
    val timestamp: OffsetDateTime,
    val timezone: String,
    val user: String,
    val applicationLabel: String,
) : ChronicleSample
