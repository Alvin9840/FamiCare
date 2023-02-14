package com.ethan.FamiCare

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HealthStepsActivity : AppCompatActivity() {
    val myDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").withZone( ZoneId.systemDefault() )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_steps)

        val client = HealthConnectClient.getOrCreate(this)

        lifecycleScope.launch {
            val steps = getStepCount(
                client,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now()
            )
            val first: TextView = findViewById(R.id.today)
            first.text = myDateTimeFormatter.format(Instant.parse(steps[steps.size-1].endTime.toString()))+"  "+steps[steps.size-1].count.toString()+"步"
            val yesterday: TextView = findViewById(R.id.yesterday)
            yesterday.text = myDateTimeFormatter.format(Instant.parse(steps[steps.size-2].endTime.toString()))+"  "+steps[steps.size-2].count.toString()+"步"
            val twodaysago: TextView = findViewById(R.id.twodaysago)
            twodaysago.text = myDateTimeFormatter.format(Instant.parse(steps[steps.size-3].endTime.toString()))+"  "+steps[steps.size-3].count.toString()+"步"

            aggregateStepsIntoWeeks(
                client,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now())

            aggregateStepsIntoMonths(
                client,
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now())
        }

        this.findViewById<Button>(R.id.update).setOnClickListener {
            lifecycleScope.launch {
                val steps = getStepCount(
                    client,
                    LocalDateTime.now().minusDays(3),
                    LocalDateTime.now())
                val first: TextView = findViewById(R.id.today)
                first.text = myDateTimeFormatter.format(Instant.parse(steps[steps.size-1].endTime.toString()))+"  "+steps[steps.size-1].count.toString()+"步"
                val yesterday: TextView = findViewById(R.id.yesterday)
                yesterday.text = myDateTimeFormatter.format(Instant.parse(steps[steps.size-2].endTime.toString()))+"  "+steps[steps.size-2].count.toString()+"步"
                val twodaysago: TextView = findViewById(R.id.twodaysago)
                twodaysago.text = myDateTimeFormatter.format(Instant.parse(steps[steps.size-3].endTime.toString()))+"  "+steps[steps.size-3].count.toString()+"步"

                aggregateStepsIntoWeeks(
                    client,
                    LocalDateTime.now().minusDays(7),
                    LocalDateTime.now())

                aggregateStepsIntoMonths(
                    client,
                    LocalDateTime.now().minusDays(30),
                    LocalDateTime.now())
            }
        }
    }

    suspend fun getStepCount(
        client: HealthConnectClient,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<StepsRecord> {

        val request = client.readRecords(
            ReadRecordsRequest(
                StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )
        return request.records
    }

    suspend fun aggregateStepsIntoWeeks(
        healthConnectClient: HealthConnectClient,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ){
        val response =
            healthConnectClient.aggregateGroupByPeriod(
                AggregateGroupByPeriodRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    timeRangeSlicer = Period.ofWeeks(1)
                )
            )

        for (weeklyResult in response) {
            val totalSteps = weeklyResult.result[StepsRecord.COUNT_TOTAL]
            val week: TextView = findViewById(R.id.weekAvg)
            if (totalSteps != null) {
                week.text = totalSteps.div(response.size).toString()+"步"
            }
        }
    }

    suspend fun aggregateStepsIntoMonths(
        healthConnectClient: HealthConnectClient,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ) {
        val response =
            healthConnectClient.aggregateGroupByPeriod(
                AggregateGroupByPeriodRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    timeRangeSlicer = Period.ofMonths(1)
                )
            )

        for (monthlyResult in response) {
            val totalSteps = monthlyResult.result[StepsRecord.COUNT_TOTAL]
            val month: TextView = findViewById(R.id.monthAvg)
            if (totalSteps != null) {
                month.text = totalSteps.div(response.size).toString()+"步"
            }
        }
    }

}