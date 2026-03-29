import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../widgets/graph_card.dart';
import '../widgets/section_header.dart';
import '../widgets/truepulse_logo.dart';
import '../models/timestamp_note.dart';

class PatientSessionScreen extends StatefulWidget {
  const PatientSessionScreen({super.key});

  @override
  State<PatientSessionScreen> createState() => _PatientSessionScreenState();
}

class _PatientSessionScreenState extends State<PatientSessionScreen> {
  final _notes = <TimestampNote>[];
  DateTime? _fileSavedTimestamp;

  void _addTimestampNote() {
    setState(() {
      _notes.add(TimestampNote(DateTime.now(), "Timestamp note"));
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Patient Session")),
      floatingActionButton: FloatingActionButton(
        onPressed: _addTimestampNote,
        child: const Icon(Icons.add_comment),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Center(child: TruePulseLogo()),

            sectionHeader("Physiological Signals"),
            const GraphCard(title: "ECG"),
            const GraphCard(title: "RR Intervals"),
            const GraphCard(title: "Accelerometer"),

            sectionHeader("Controls"),
            ElevatedButton(
              onPressed: () {},
              child: const Text("Start 30:15 Test"),
            ),

            if (_fileSavedTimestamp != null)
              Text(
                "Saved at ${DateFormat.yMd().add_jms().format(_fileSavedTimestamp!)}",
              ),
          ],
        ),
      ),
    );
  }
}