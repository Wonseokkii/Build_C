import 'package:flutter/material.dart';

import 'sensors/sensor_manager.dart';
import 'sensors/polar_sensor.dart';

import 'widgets/live_line_chart.dart';
import 'widgets/acc_line_chart.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  SensorManager.registerSensor(PolarSensor("polar1"));

  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List<String> devices = [];
  String? connectedId;
  int? latestHr;

  @override
  void initState() {
    super.initState();

    final polar = SensorManager.get("polar1") as PolarSensor;
    polar.startListeningToEvents();

    polar.getStream("deviceFound").listen((id) {
      setState(() {
        if (!devices.contains(id)) {
          devices.add(id);
        }
      });
    });

    polar.getStream("deviceConnected").listen((id) async {
      setState(() {
        connectedId = id;
      });
    });

    polar.getStream("featureReady").listen((id) async {
      print("⭐ FEATURE READY FOR STREAMING: $id");

      await polar.startEcg(id);
      await polar.startAcc(id);

      print("⭐ ECG + ACC STARTED FOR $id");
    });

    // HR has its separate ble feature ready which works immediately after device is connected (educated guess)
    polar.getStream("hr").listen((hr) {
      setState(() {
        latestHr = hr;
      });
    });

  }

  @override
  Widget build(BuildContext context) {
    final polar = SensorManager.get("polar1") as PolarSensor;

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text("Modular Sensor Demo (Polar)")),
        body: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                ElevatedButton(
                  onPressed: () {
                    SensorManager.startScan("polar1");
                  },
                  child: const Text("Start Scan (Polar)"),
                ),

                const SizedBox(height: 12),
                const Text("Found devices:", style: TextStyle(fontWeight: FontWeight.bold)),

                ...devices.map((id) => ListTile(
                  title: Text(id),
                  trailing: ElevatedButton(
                    onPressed: () async {
                      await SensorManager.connect("polar1", id);

                    },
                    child: const Text("Connect"),
                  ),
                )),

                const SizedBox(height: 16),
                if (connectedId != null) Text("Connected to: $connectedId"),
                if (latestHr != null)
                  Text("HR: $latestHr bpm",
                      style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),

                const SizedBox(height: 30),

                const Text("ECG Stream", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                LiveLineChart(
                  stream: polar.getStream<int>("ecg"),
                  maxPoints: 500,
                  height: 200,
                  color: Colors.pink,
                ),

                const SizedBox(height: 30),

                const Text("Accelerometer Stream (x,y,z)",
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                AccLineChart(
                  stream: polar.getStream<List<int>>("acc"),
                  maxPoints: 300,
                  height: 200,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
