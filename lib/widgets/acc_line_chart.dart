import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';

class AccLineChart extends StatefulWidget {
  final Stream<List<int>> stream; // batches of [x, y, z]
  final int maxPoints;
  final double height;

  const AccLineChart({
    super.key,
    required this.stream,
    this.maxPoints = 400,
    this.height = 200,
  });

  @override
  State<AccLineChart> createState() => _AccLineChartState();
}

class _AccLineChartState extends State<AccLineChart> {
  List<FlSpot> xBuf = [];
  List<FlSpot> yBuf = [];
  List<FlSpot> zBuf = [];
  double t = 0;

  @override
  void initState() {
    super.initState();

    widget.stream.listen((sample) {
      setState(() {
        final x = sample[0].toDouble();
        final y = sample[1].toDouble();
        final z = sample[2].toDouble();

        xBuf.add(FlSpot(t, x));
        yBuf.add(FlSpot(t, y));
        zBuf.add(FlSpot(t, z));

        t += 1;

        if (xBuf.length > widget.maxPoints) {
          xBuf.removeAt(0);
          yBuf.removeAt(0);
          zBuf.removeAt(0);
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: widget.height,
      child: LineChart(
        LineChartData(
          minY: -2000,
          maxY: 2000,
          titlesData: const FlTitlesData(show: false),
          gridData: const FlGridData(show: true),
          borderData: FlBorderData(show: true),
          lineBarsData: [
            LineChartBarData(
              spots: xBuf,
              dotData: const FlDotData(show: false),
              color: Colors.red,
              barWidth: 2,
            ),
            LineChartBarData(
              spots: yBuf,
              dotData: const FlDotData(show: false),
              color: Colors.green,
              barWidth: 2,
            ),
            LineChartBarData(
              spots: zBuf,
              dotData: const FlDotData(show: false),
              color: Colors.blue,
              barWidth: 2,
            ),
          ],
        ),
      ),
    );
  }
}
