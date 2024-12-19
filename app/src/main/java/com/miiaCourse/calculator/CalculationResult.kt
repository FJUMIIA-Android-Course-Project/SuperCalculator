package com.miiaCourse.calculator

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_results")
data class CalculationResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val result: String
)
