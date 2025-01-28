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

const SamsungHealth = {
  readProfile: async function () {
    return await SamsungHealthSavanitdev.readProfile();
  },
  readSteps: async function () {
    return await SamsungHealthSavanitdev.readSteps();
  },
  readSleeps: async function () {
    return await SamsungHealthSavanitdev.readSleeps();
  },
  readNutrition: async function () {
    return await SamsungHealthSavanitdev.readNutrition();
  },
  readHeartRate: async function () {
    return await SamsungHealthSavanitdev.readHeartRate();
  },
  checkPermissions: async function () {
    return await SamsungHealthSavanitdev.checkPermissions();
  },
  askPermissionAsync: async function () {
    return await SamsungHealthSavanitdev.askPermissionAsync();
  },
};
export default SamsungHealth;
