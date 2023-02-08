package models

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class OrderExecutionLogs    (
    val orderExecutionId: Long,
    val orderExecutionPrice: Long,
    val orderExecutionQuantity: Long
) {
    val orderExecutionTime: String = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
        .withZone(ZoneOffset.UTC)
        .format(Instant.now())
}