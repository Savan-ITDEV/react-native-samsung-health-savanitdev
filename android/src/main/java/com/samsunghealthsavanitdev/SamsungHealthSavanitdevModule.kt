package com.samsunghealthsavanitdev

import android.content.ContentValues.TAG
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.data.HealthDataPoint
import com.samsung.android.sdk.health.data.data.entries.SleepSession
import com.samsung.android.sdk.health.data.error.HealthDataException
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.IdFilter
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.LocalTimeGroup
import com.samsung.android.sdk.health.data.request.LocalTimeGroupUnit
import com.samsung.android.sdk.health.data.request.Ordering
import com.samsunghealthsavanitdev.Permissions.PERMISSIONS
import com.samsunghealthsavanitdev.utils.DailyIntakeCalories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale


class SamsungHealthSavanitdevModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    private var context = reactContext;
    private var healthMainViewModel = HealthDataService;
    override fun getName(): String {

        return NAME
    }

    companion object {
        const val NAME = "SamsungHealthSavanitdev"


    }

    @ReactMethod
    fun checkPermissions(promise: Promise) {
        // Ensure we have a valid activity
        val activity = currentActivity
        if (activity == null) {
            promise.reject(
                "ACTIVITY_ERROR",
                "Current activity is null. Ensure the module is used in an active context."
            )
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val healthDataStore = HealthDataService.getStore(activity)
                val grantedPermissions = healthDataStore.getGrantedPermissions(PERMISSIONS)
                if (grantedPermissions.containsAll(PERMISSIONS)) {
                    Log.i(
                        "grantedPermissions",
                        "grantedPermissions: Success ${grantedPermissions.toString()}"
                    )
                    promise.resolve("SUCCESS");
                } else {
                    val result = healthDataStore.requestPermissions(PERMISSIONS, activity)
                    Log.i(TAG, "requestPermissions: Success ${result.size}")
                    promise.resolve("REQUEST");
                }

//                promise.resolve(grantedPermissions)
            } catch (e: HealthDataException) {
                promise.reject("HEALTH_ERROR", e.errorMessage)
            }
        }
    }

    @ReactMethod
    fun askPermissionAsync(promise: Promise) {
        val activity = currentActivity
        if (activity == null) {
            promise.reject(
                "ACTIVITY_ERROR",
                "Current activity is null. Ensure the module is used in an active context."
            )
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val healthDataStore = HealthDataService.getStore(activity)
                val grantedPermissions = healthDataStore.getGrantedPermissions(PERMISSIONS)
                if (grantedPermissions.containsAll(PERMISSIONS)) {
                    Log.i(
                        "grantedPermissions",
                        "grantedPermissions: Success ${grantedPermissions.toString()}"
                    )
                    promise.resolve("SUCCESS");
                } else {
                    promise.resolve("REQUEST");
                }
            } catch (e: HealthDataException) {
                promise.reject("HEALTH_ERROR", e.errorMessage)
            }
        }
    }

    @ReactMethod
    fun readProfile(promise: Promise) {
        val activity = currentActivity
        if (activity == null) {
            promise.reject(
                "ACTIVITY_ERROR",
                "Current activity is null. Ensure the module is used in an active context."
            )
            return
        }
        val result = Arguments.createArray()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val healthDataStore = HealthDataService.getStore(activity)
                val grantedPermissions = healthDataStore.getGrantedPermissions(PERMISSIONS)
                if (grantedPermissions.containsAll(PERMISSIONS)) {
                    val readRequest = DataTypes.USER_PROFILE.readDataRequestBuilder.build()
                    val userProfileList = healthDataStore.readData(readRequest).dataList
                    val map = Arguments.createMap()
                    userProfileList.forEach { dataPoint ->
                        val gender = dataPoint.getValue(DataType.UserProfileDataType.GENDER)
                        val dateOfBirth =
                            dataPoint.getValue(DataType.UserProfileDataType.DATE_OF_BIRTH)
                        val height = dataPoint.getValue(DataType.UserProfileDataType.HEIGHT)
                        val weight = dataPoint.getValue(DataType.UserProfileDataType.WEIGHT)
                        val nickname = dataPoint.getValue(DataType.UserProfileDataType.NICKNAME)

                        map.putString("gender",gender.toString())
                        map.putString("dateOfBirth",dateOfBirth.toString())
                        map.putString("height",height.toString())
                        map.putString("weight",weight.toString())
                        map.putString("nickname",nickname.toString())

                    }

                    promise.resolve(result);
                } else {
                    promise.resolve("REQUEST");
                }

//                promise.resolve(grantedPermissions)
            } catch (e: HealthDataException) {
                promise.reject("HEALTH_ERROR", e.errorMessage)
            }
        }
    }

    @ReactMethod
    fun readSteps(promise: Promise) {
        // Ensure we have a valid activity
        val activity = currentActivity
        if (activity == null) {
            promise.reject(
                "ACTIVITY_ERROR",
                "Current activity is null. Ensure the module is used in an active context."
            )
            return
        }
        val result = Arguments.createArray()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val healthDataStore = HealthDataService.getStore(activity)
                val grantedPermissions = healthDataStore.getGrantedPermissions(PERMISSIONS)
                if (grantedPermissions.containsAll(PERMISSIONS)) {
                    Log.i(
                        "grantedPermissions",
                        "grantedPermissions: Success ${grantedPermissions.toString()}"
                    )
                    val map = Arguments.createMap()
                    val currentDate: LocalDateTime = LocalDateTime.now().with(LocalTime.MIDNIGHT)
                    val localtimeFilter = LocalTimeFilter.of(currentDate, currentDate.plusDays(1))
                    val localTimeGroup = LocalTimeGroup.of(LocalTimeGroupUnit.HOURLY, 1)
                    val aggregateRequest = DataType.StepsType.TOTAL.requestBuilder
                        .setLocalTimeFilterWithGroup(localtimeFilter, localTimeGroup)
                        .setOrdering(Ordering.ASC)
                        .build()
                    val results = healthDataStore.aggregateData(aggregateRequest)

                    val stepList: ArrayList<GroupedData> = ArrayList()

                    results.dataList.forEach { aggregatedData ->
                        var stepCount = 0L
                        aggregatedData.value?.let { stepCount = it }


                        val stepTime = "${aggregatedData.getStartLocalDateTime().toLocalTime()} - ${
                            aggregatedData.getEndLocalDateTime().toLocalTime()
                        }"
                        map.putString("stepTime",stepTime.toString())

                        val startTime = aggregatedData.startTime.atZone(ZoneId.systemDefault())
                        val groupedData = GroupedData(stepCount, startTime.toLocalDateTime())
                        stepList.add(groupedData)

                    }

                    val maxGroupedStepCount = stepList.maxByOrNull { it.count }?.count ?: 0
                    Log.d("stepList ", maxGroupedStepCount.toString());
                    map.putString("steps",maxGroupedStepCount.toString())
                    result.pushMap(map);
                    promise.resolve(result);
                } else {
                    promise.resolve("REQUEST");
                }

//                promise.resolve(grantedPermissions)
            } catch (e: HealthDataException) {
                promise.reject("HEALTH_ERROR", e.errorMessage)
            }
        }
    }

    @ReactMethod
    fun readSleeps(promise: Promise) {
        // Ensure we have a valid activity
        val activity = currentActivity
        if (activity == null) {
            promise.reject(
                "ACTIVITY_ERROR",
                "Current activity is null. Ensure the module is used in an active context."
            )
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val results: WritableArray = Arguments.createArray()
                val healthDataStore = HealthDataService.getStore(activity)
                val grantedPermissions = healthDataStore.getGrantedPermissions(PERMISSIONS)
                if (grantedPermissions.containsAll(PERMISSIONS)) {

                    val currentDate: LocalDateTime = LocalDateTime.now().with(LocalTime.MIDNIGHT)
                    val readRequest = DataTypes.SLEEP.readDataRequestBuilder
                        .setLocalTimeFilter(
                            LocalTimeFilter.of(
                                currentDate,
                                currentDate.plusDays(1)
                            )
                        )
                        .setOrdering(Ordering.ASC)
                        .build()
                    val sleepDataList = healthDataStore.readData(readRequest).dataList

                    if (sleepDataList.isNotEmpty()) {
                        val ids = IdFilter.builder()
                        sleepDataList.forEach { healthDataPoint ->
                            val map = Arguments.createMap()
                            val sleep = healthDataPoint.getValue(DataType.SleepType.DURATION)?.let {
                                durationParsing(it.toString())
                            } ?: ""
                            map.putString("sleep", sleep.toString());

                            Log.d("sleep sleep ", sleep)
                            val sleepSessionList = healthDataPoint.getValue(DataType.SleepType.SESSIONS) as List<SleepSession>
                            val stage = SleepStage(0, 0, 0, 0)

                            val timeInBed = "${
                                LocalDateTime.ofInstant(healthDataPoint.startTime, healthDataPoint.zoneOffset).toLocalTime()
                            } - ${
                                LocalDateTime.ofInstant(healthDataPoint.endTime, healthDataPoint.zoneOffset).toLocalTime()
                            }"
                            Log.d("timeInBed ",timeInBed.toString())
                            map.putString("timeInBed", timeInBed.toString());

                            sleepSessionList.forEach { sleepSession ->
                                sleepSession.stages?.forEach {

                                    val startTime = it.startTime.epochSecond
                                    val endTime = it.endTime.epochSecond
                                    stage.apply {
                                        when (it.stage) {
                                            DataType.SleepType.StageType.UNDEFINED -> {
                                                stage.undefined += (endTime - startTime).toInt()
                                            }

                                            DataType.SleepType.StageType.AWAKE -> {
                                                stage.awake += (endTime - startTime).toInt()
                                            }

                                            DataType.SleepType.StageType.LIGHT -> {
                                                stage.light += (endTime - startTime).toInt()
                                            }

                                            DataType.SleepType.StageType.REM -> {
                                                stage.rem += (endTime - startTime).toInt()
                                            }

                                            DataType.SleepType.StageType.DEEP -> {
                                                stage.deep += (endTime - startTime).toInt()
                                            }
                                        }
                                    }
                                }

                                map.putString("undefined", stage.undefined.toString());
                                map.putString("awake", stage.awake.toString());
                                map.putString("light", stage.light.toString());
                                map.putString("rem", stage.rem.toString());
                                map.putString("deep", stage.deep.toString());

                                Log.d("stage", " $stage")
                                results.pushMap(map);
                            }

                        }

                    }
                    promise.resolve(results);
                } else {
                    val result = healthDataStore.requestPermissions(PERMISSIONS, activity)
                    Log.i(TAG, "requestPermissions: Success ${result.size}")
                    promise.resolve("REQUEST");
                }
            } catch (e: HealthDataException) {
                promise.reject("HEALTH_ERROR", e.errorMessage)
            }
        }
    }

    @ReactMethod
    fun readNutrition(promise: Promise) {
        // Ensure we have a valid activity
        val activity = currentActivity
        if (activity == null) {
            promise.reject(
                "ACTIVITY_ERROR",
                "Current activity is null. Ensure the module is used in an active context."
            )
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val healthDataStore = HealthDataService.getStore(activity)
                val grantedPermissions = healthDataStore.getGrantedPermissions(PERMISSIONS)
                val results: WritableArray = Arguments.createArray()
                if (grantedPermissions.containsAll(PERMISSIONS)) {
                    Log.i(
                        "grantedPermissions",
                        "grantedPermissions: Success ${grantedPermissions.toString()}"
                    )

                    val currentDate: LocalDateTime = LocalDateTime.now().with(LocalTime.MIDNIGHT)
                    val readRequest = DataTypes.NUTRITION.readDataRequestBuilder
                        .setLocalTimeFilter(
                            LocalTimeFilter.of(
                                currentDate,
                                currentDate.plusDays(1)
                            )
                        )
                        .setOrdering(Ordering.ASC)
                        .build()
                    val intakeList = healthDataStore.readData(readRequest).dataList

                    var calories = 0f
                    val dailyIntakeCalories = DailyIntakeCalories()

                    intakeList.forEach { nutritionData ->
                        calories += nutritionData.getValueOrDefault(
                            DataType.NutritionType.CALORIES,
                            0F
                        )
                        dailyIntakeCalories.addData(
                            nutritionData.getValueOrDefault(
                                DataType.NutritionType.MEAL_TYPE,
                                DataType.NutritionType.MealType.BREAKFAST
                            ),
                            nutritionData.getValueOrDefault(DataType.NutritionType.CALORIES, 0F),
                            nutritionData.getValueOrDefault(DataType.NutritionType.TITLE, ""),
                            nutritionData.getValueOrDefault(DataType.NutritionType.PROTEIN, 0F),
                            nutritionData.getValueOrDefault(DataType.NutritionType.TOTAL_FAT, 0F),
                            nutritionData.getValueOrDefault(
                                DataType.NutritionType.SATURATED_FAT,
                                0F
                            ),
                            nutritionData.getValueOrDefault(
                                DataType.NutritionType.POLYSATURATED_FAT,
                                0F
                            ),
                            nutritionData.getValueOrDefault(
                                DataType.NutritionType.MONOSATURATED_FAT,
                                0F
                            ),
                            nutritionData.getValueOrDefault(DataType.NutritionType.TRANS_FAT, 0F),
                            nutritionData.getValueOrDefault(
                                DataType.NutritionType.CARBOHYDRATE,
                                0F
                            ),
                            nutritionData.getValueOrDefault(
                                DataType.NutritionType.DIETARY_FIBER,
                                0F
                            ),
                            nutritionData.getValueOrDefault(DataType.NutritionType.SUGAR, 0F),
                            nutritionData.getValueOrDefault(DataType.NutritionType.CHOLESTEROL, 0F),
                            nutritionData.getValueOrDefault(DataType.NutritionType.SODIUM, 0F),
                            nutritionData.getValueOrDefault(DataType.NutritionType.POTASSIUM, 0F),
                            nutritionData.getValueOrDefault(DataType.NutritionType.VITAMIN_A, 0F),
                            nutritionData.getValueOrDefault(DataType.NutritionType.VITAMIN_C, 0F),
                            nutritionData.getValueOrDefault(DataType.NutritionType.CALCIUM, 0F),
                            nutritionData.getValueOrDefault(DataType.NutritionType.IRON, 0F),
                        )
                    }
                    val maps = Arguments.createMap()
                    maps.putString("totalCalories",calories.toString())
                    val lunch = dailyIntakeCalories.convertToWritableMap(dailyIntakeCalories.lunchNutritionInfo)
                    maps.putMap("lunch",lunch)
                    val dinner = dailyIntakeCalories.convertToWritableMap(dailyIntakeCalories.dinnerNutritionInfo)
                    maps.putMap("dinner",dinner)
                    val breakfast = dailyIntakeCalories.convertToWritableMap(dailyIntakeCalories.breakfastNutritionInfo)
                    maps.putMap("breakfast",breakfast)

                    val evening = dailyIntakeCalories.convertToWritableMap(dailyIntakeCalories.eveningSnackNutritionInfo)
                    maps.putMap("evening",evening)

                    val morning = dailyIntakeCalories.convertToWritableMap(dailyIntakeCalories.morningSnackNutritionInfo)
                    maps.putMap("morning",morning)

                    val afternoon = dailyIntakeCalories.convertToWritableMap(dailyIntakeCalories.afternoonSnackNutritionInfo)
                    maps.putMap("afternoon",afternoon)
                    results.pushMap(maps);
                    promise.resolve(results);

                } else {
                    val result = healthDataStore.requestPermissions(PERMISSIONS, activity)
                    Log.i(TAG, "requestPermissions: Success ${result.size}")
                    promise.resolve("REQUEST");
                }
            } catch (e: HealthDataException) {
                promise.reject("HEALTH_ERROR", e.errorMessage)
            }
        }
    }

    @ReactMethod
    fun readHeartRate(promise: Promise) {
        // Ensure we have a valid activity
        val activity = currentActivity
        if (activity == null) {
            promise.reject("ACTIVITY_ERROR", "Current activity is null. Ensure the module is used in an active context.")
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val healthDataStore = HealthDataService.getStore(activity)
                val grantedPermissions = healthDataStore.getGrantedPermissions(PERMISSIONS)
                if (grantedPermissions.containsAll(PERMISSIONS)) {
                    Log.i("grantedPermissions", "grantedPermissions: Success ${grantedPermissions.toString()}")

                    val currentDate: LocalDateTime = LocalDateTime.now().with(LocalTime.MIDNIGHT)
                    val localTimeFilter = LocalTimeFilter.of(currentDate, currentDate.plusDays(1))
                    val readRequest = DataTypes.HEART_RATE.readDataRequestBuilder
                        .setLocalTimeFilter(localTimeFilter)
                        .setOrdering(Ordering.DESC)
                        .build()
                    val heartRateList = healthDataStore.readData(readRequest).dataList
                    val hrOfFirstQuarter = HeartRate(1000f, 0f, 0f, "00:00", "06:00", 0)
                    val hrOfSecondQuarter = HeartRate(1000f, 0f, 0f, "06:00", "12:00", 0)
                    val hrOfThirdQuarter = HeartRate(1000f, 0f, 0f, "12:00", "18:00", 0)
                    val hrOfFourthQuarter = HeartRate(1000f, 0f, 0f, "18:00", "24:00", 0)

                    heartRateList.forEach { heartRateData ->
                        val time = LocalDateTime.ofInstant(heartRateData.startTime, heartRateData.zoneOffset)
                        when {
                            time.isBetween(0, 5) -> processHeartRateData(heartRateData, hrOfFirstQuarter)
                            time.isBetween(6, 11) -> processHeartRateData(heartRateData, hrOfSecondQuarter)
                            time.isBetween(12, 17) -> processHeartRateData(heartRateData, hrOfThirdQuarter)
                            time.isBetween(18, 23) -> processHeartRateData(heartRateData, hrOfFourthQuarter)
                        }
                    }
                    promise.resolve("DONE");
                } else {
                    val result = healthDataStore.requestPermissions(PERMISSIONS, activity)
                    Log.i(TAG, "requestPermissions: Success ${result.size}")
                    promise.resolve("REQUEST");
                }
            } catch (e: HealthDataException) {
                promise.reject("HEALTH_ERROR", e.errorMessage)
            }
        }
    }

    private fun durationParsing(s: String): String = s.substring(2)
        .lowercase(Locale.ROOT).replace(Regex("[hms](?!\$)")) { it.value + " " }
    private fun processHeartRateData(heartRateData: HealthDataPoint, hrQuarter: HeartRate) {
        hrQuarter.apply {
            heartRateData.getValue(DataType.HeartRateType.HEART_RATE)?.let {
                avg += it
                count++
            }
            heartRateData.getValue(DataType.HeartRateType.MAX_HEART_RATE)?.let {
                max = maxOf(max, it)
            }
            heartRateData.getValue(DataType.HeartRateType.MIN_HEART_RATE)?.let {
                if (min != 0f) {
                    min = minOf(min, it)
                }
            }
        }
    }
    private fun LocalDateTime.isBetween(fromHour: Int, toHour: Int) =
        this >= this.withHour(fromHour).withMinute(0).withSecond(0) &&
                this <= this.withHour(toHour).withMinute(59).withSecond(59)
}

data class GroupedData(
    val count: Long,
    val startTime: LocalDateTime,
)

data class StepData(
    val count: Long,
    var goal: Int,
    val hourly: ArrayList<GroupedData>,
)

data class HeartRate(
    var min: Float,
    var max: Float,
    var avg: Float,
    var startTime: String,
    var endTime: String,
    var count: Int,
)
data class SleepStage(
    var undefined: Int = 0,
    var awake: Int = 0,
    var light: Int = 0,
    var rem: Int = 0,
    var deep: Int = 0,
)