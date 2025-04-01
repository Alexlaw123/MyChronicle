package com.openlattice.chronicle.models

import com.openlattice.chronicle.android.ChronicleSample
import java.time.OffsetDateTime
import java.util.*

data class FullUsageEvent(
    val studyId: UUID,
    val participantId: String,
    val appPackageName: String,
    val className: String?, // ✅ 加入 className
    val interactionType: String,
    val timestamp: OffsetDateTime,
    val timezone: String,
    val user: String,
    val applicationLabel: String,
) : ChronicleSample
