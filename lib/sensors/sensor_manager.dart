import 'sensor_interface.dart';

class SensorManager {
  static final Map<String, SensorInterface> _sensors = {};

  static void registerSensor(SensorInterface sensor) {
    _sensors[sensor.name] = sensor;
  }

  static SensorInterface? get(String name) => _sensors[name];

  static Future<void> startScan(String name) async {
    await _sensors[name]?.startScan();
  }

  static Future<void> connect(String name, String id) async {
    await _sensors[name]?.connect(id);
  }

  static Stream<dynamic> on(String name, String streamType) {
    final sensor = _sensors[name];
    if (sensor == null) {
      throw Exception('Sensor $name not registered');
    }
    return sensor.getStream(streamType);
  }
}
