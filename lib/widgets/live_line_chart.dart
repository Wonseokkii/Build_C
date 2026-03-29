import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';

class LiveLineChart extends StatefulWidget {
  final Stream<int> stream;  // e.g. ECG batches
  final int maxPoints;             // keep last N points
  final double height;
  final Color color;

  const LiveLineChart({
    super.key,
    required this.stream,
    this.maxPoints = 500,
    this.height = 200,
    this.color = Colors.green,
  });

  @override
  State<LiveLineChart> createState() => _LiveLineChartState();
}

class _LiveLineChartState extends State<LiveLineChart> {
  final List<FlSpot> _buffer = [];
  double _x = 0;

  @override
  void initState() {
    super.initState();

    widget.stream.listen((sample) {
      setState(() {
        _buffer.add(FlSpot(_x, sample.toDouble()));
        _x += 1;

        if (_buffer.length > widget.maxPoints) {
          _buffer.removeAt(0);
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
          gridData: const FlGridData(show: true),
          titlesData: const FlTitlesData(show: false),
          borderData: FlBorderData(show: true),
          minY: -500,
          maxY: 500,
          lineBarsData: [
            LineChartBarData(
              spots: _buffer,
              isCurved: false,
              color: widget.color,
              barWidth: 2,
            )
          ],
        ),
      ),
    );
  }
}