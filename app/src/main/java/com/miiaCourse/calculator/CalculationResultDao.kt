package com.miiaCourse.calculator

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CalculationResultDao {
    @Insert
    suspend fun insertResult(result: CalculationResult)

    @Query("SELECT * FROM calculation_results")
    suspend fun getAllResults(): List<CalculationResult>
}
