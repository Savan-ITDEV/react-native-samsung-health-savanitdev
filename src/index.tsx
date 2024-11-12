import { NativeModules, Platform } from "react-native";

const LINKING_ERROR =
  `The package 'react-native-samsung-health-savanitdev' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: "" }) +
  "- You rebuilt the app after installing the package\n" +
  "- You are not using Expo Go\n";

const SamsungHealthSavanitdev = NativeModules.SamsungHealthSavanitdev
  ? NativeModules.SamsungHealthSavanitdev
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function multiply(a: number, b: number): Promise<number> {
  return SamsungHealthSavanitdev.multiply(a, b);
}

type SamsungHealthAndroidType = {
  Types: Record<string, string>;
  createMetric(
    props: SamsungHealthAndroidMetricProps
  ): SamsungHealthAndroidMetric;
  connect(debug: boolean): Promise<boolean>;
  disconnect(): Promise<boolean>;
  askPermissionAsync(permissions: string[]): Promise<Record<string, boolean>>;
  getPermissionAsync(permissions: string[]): Promise<Record<string, boolean>>;
  readDataAsync(
    metric: SamsungHealthAndroidMetric
  ): Promise<Record<string, number>[]>;

  getStepCountDailie(options: options): Promise<Record<string, number>[]>;
};

type SamsungHealthAndroidMetric = {
  type: string;
  properties: string[];
  start: number;
  end: number;
};

type SamsungHealthAndroidMetricProps = {
  type: string;
  start: number;
  end: number;
};

type options = {
  startDate: string;
  endDate: string;
};

// console.log("SamsungHealthSavanitdev", SamsungHealthSavanitdev);

// const samsungHealth = NativeModules.RNSamsungHealth;

SamsungHealthSavanitdev.Types = SamsungHealthSavanitdev.getConstants();
SamsungHealthSavanitdev.createMetric = (
  props: SamsungHealthAndroidMetricProps
): SamsungHealthAndroidMetric => {
  const { type } = props;
  let properties: string[] = [];

  switch (type) {
    case SamsungHealthSavanitdev.Types.DailyTrend:
      properties = ["calorie", "count", "distance", "speed"];
      break;
    case SamsungHealthSavanitdev.Types.StepCount:
      properties = ["update_time", "calorie", "count", "distance", "speed"];
      break;
    case SamsungHealthSavanitdev.Types.Sleep:
      properties = ["start_time", "end_time", "custom", "comment"];
      break;
    case SamsungHealthSavanitdev.Types.SleepStage:
      properties = ["start_time", "end_time", "stage", "sleep_id"];
      break;
    case SamsungHealthSavanitdev.Types.CaffeineIntake:
      properties = ["update_time", "amount", "unit_amount"];
      break;
    case SamsungHealthSavanitdev.Types.BodyTemperature:
      properties = ["update_time", "temperature"];
      break;
    case SamsungHealthSavanitdev.Types.BloodPressure:
      properties = ["update_time", "diastolic", "mean", "pulse", "systolic"];
      break;
    case SamsungHealthSavanitdev.Types.Electrocardiogram:
      properties = [
        "data",
        "data_format",
        "max_heart_rate",
        "mean_heart_rate",
        "min_heart_rate",
        "sample_count",
        "sample_frequency",
        "update_time",
      ];
      break;
    case SamsungHealthSavanitdev.Types.HeartRate:
      properties = [
        "min",
        "max",
        "heart_rate",
        "heart_bate_count",
        "binning_data",
        "update_time",
      ];
      break;
    case SamsungHealthSavanitdev.Types.OxygenSaturation:
      properties = ["update_time", "heart_rate", "spo2"];
      break;
    case SamsungHealthSavanitdev.Types.AmbientTemperature:
      properties = [
        "accuracy",
        "altitude",
        "humidity",
        "latitude",
        "longitude",
        "temperature",
        "update_time",
      ];
      break;
    case SamsungHealthSavanitdev.Types.UvExposure:
      properties = [
        "accuracy",
        "altitude",
        "latitude",
        "longitude",
        "uv_index",
        "update_time",
      ];
      break;
    default:
      throw new Error(`Type ${type} is not supported`);
  }

  return {
    ...props,
    properties,
  };
};

SamsungHealthSavanitdev.getStepCountDailie = (options: options): any => {
  console.log("options", options);
  let startDate =
    options.startDate != undefined
      ? options.startDate
      : new Date().setHours(0, 0, 0, 0);
  let endDate =
    options.endDate != undefined ? options.endDate : new Date().valueOf();

  return new Promise((resolve, reject) => {
    SamsungHealthSavanitdev.readStepCountDailies(
      startDate,
      endDate,
      (msg: any) => reject(msg),
      (res: any) => resolve(res)
    );
  });
};
export default SamsungHealthSavanitdev as SamsungHealthAndroidType;
