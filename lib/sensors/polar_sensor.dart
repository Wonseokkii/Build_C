import 'dart:async';
import 'package:flutter/services.dart';
import 'sensor_interface.dart';

class PolarSensor extends SensorInterface {
  late final MethodChannel _method;
  late final EventChannel _events;

  final _deviceFoundController = StreamController<String>.broadcast();
  final _deviceConnectedController = StreamController<String>.broadcast();
  final _featureReadyController = StreamController<String>.broadcast();
  final _hrController = StreamController<int>.broadcast();
  final _ecgController = StreamController<int>.broadcast();
  final _accController = StreamController<List<int>>.broadcast();

  bool _listening = false;

  @override
  final String name;

  PolarSensor(this.name) {
    _method = MethodChannel("sensors/$name/methods");
    _events = EventChannel("sensors/$name/events");
  }

  void startListeningToEvents() {
    // there should only be 1 event listener
    if (_listening) return;
    _listening = true;

    _events.receiveBroadcastStream().listen((raw) {
      print("RAW NATIVE EVENT: $raw");

      final e = raw as Map<dynamic, dynamic>;

      if (e["sensor"] != "polar1") return; // safety

      switch (e["event"]) {
        case "deviceFound":
          _deviceFoundController.add(e["id"]);
          break;

        case "deviceConnected":
          _deviceConnectedController.add(e["id"]);
          break;

        case "featureReady":
          _featureReadyController.add(e["id"]);
          break;

        case "hr":
          _hrController.add(e["hr"] as int);
          break;

        case "ecg":
          _ecgController.add(e["sample"] as int);
          break;

        case "acc":
          _accController.add([
            e["x"] as int,
            e["y"] as int,
            e["z"] as int,
          ]);
          break;

      }
    });
  }

  // Convenience getters if you want:
  // Stream<String> get onDeviceFound => _deviceFoundController.stream;
  // Stream<String> get onDeviceConnected => _deviceConnectedController.stream;
  // Stream<int> get onHr => _hrController.stream;
  // Stream<List<int>> get onEcg => _ecgController.stream;
  // Stream<List<List<int>>> get onAcc => _accController.stream;

  @override
  Future<void> startScan() async {
    await _method.invokeMethod("startScan");
  }

  @override
  Future<void> connect(String id) async {
    await _method.invokeMethod("connect", {"id": id});
  }

  Future<void> startEcg(String id) async {
    await _method.invokeMethod("startEcg", {"id": id});
  }

  Future<void> startAcc(String id) async {
    await _method.invokeMethod("startAcc", {"id": id});
  }

  @override
  Stream<T> getStream<T>(String streamType) {
    switch (streamType) {
      case "deviceFound":
        return _deviceFoundController.stream as Stream<T>;
      case "deviceConnected":
        return _deviceConnectedController.stream as Stream<T>;
      case "featureReady":
        return _featureReadyController.stream as Stream<T>;
      case "hr":
        return _hrController.stream as Stream<T>;
      case "ecg":
        return _ecgController.stream as Stream<T>;
      case "acc":
        return _accController.stream as Stream<T>;
      default:
        throw Exception("Unknown stream type: $streamType");
    }
  }

  void dispose() {
    _deviceFoundController.close();
    _deviceConnectedController.close();
    _hrController.close();
    _ecgController.close();
    _accController.close();
  }
}
