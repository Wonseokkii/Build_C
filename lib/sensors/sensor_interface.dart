abstract class SensorInterface {
  /// Unique ID e.g. "polar", "garmin"
  String get name;

  Future<void> startScan();
  Future<void> connect(String id);

  /// Generic streaming API: "deviceFound", "deviceConnected", "hr", "ecg", "acc", etc.
  Stream<T> getStream<T>(String streamType);
}
