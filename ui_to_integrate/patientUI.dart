import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

class PatientSessionScreen extends StatefulWidget {
  const PatientSessionScreen({super.key});

  @override
  State<PatientSessionScreen> createState() => _PatientSessionScreenState();
}

class _PatientSessionScreenState extends State<PatientSessionScreen> {
  final _idController = TextEditingController();
  final _nameController = TextEditingController();
  final _historyController = TextEditingController();
  DateTime? _birthday;
  DateTime? _fileSavedTimestamp;

  final List<TimestampNote> _notes = [];

  void _addTimestampNote() {
    showDialog(
      context: context,
      builder: (_) {
        final noteController = TextEditingController();
        return AlertDialog(
          title: const Text("Add Timestamp Note"),
          content: TextField(
            controller: noteController,
            decoration: const InputDecoration(
              hintText: "Describe what is happening",
            ),
          ),
          actions: [
            TextButton(
              onPressed: () {
                setState(() {
                  _notes.add(
                    TimestampNote(DateTime.now(), noteController.text),
                  );
                });
                Navigator.pop(context);
              },
              child: const Text("Save"),
            ),
          ],
        );
      },
    );
  }

  void _saveDataFile() {
    setState(() {
      _fileSavedTimestamp = DateTime.now();
    });
  }

  void _start3015Test() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text("30:15 Test Started")),
    );
    // Hook physiological logic here later
  }

  @override
  Widget build(BuildContext context) {
    final dateFormat = DateFormat("yyyy-MM-dd HH:mm:ss");

    return Scaffold(
      appBar: AppBar(title: const Text("TruePulse – Patient Session")),
      floatingActionButton: FloatingActionButton(
        onPressed: _addTimestampNote,
        child: const Icon(Icons.add_comment),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _sectionTitle("Patient Information"),
            _patientInfoForm(),

            const SizedBox(height: 20),
            _sectionTitle("Physiological Signals"),
            _graphCard("ECG Signal"),
            _graphCard("RR Intervals"),
            _graphCard("Accelerometer"),

            const SizedBox(height: 20),
            _sectionTitle("Controls"),
            Row(
              children: [
                ElevatedButton(
                  onPressed: _start3015Test,
                  child: const Text("Start 30:15 Test"),
                ),
                const SizedBox(width: 12),
                ElevatedButton(
                  onPressed: _saveDataFile,
                  child: const Text("Save Data"),
                ),
              ],
            ),

            if (_fileSavedTimestamp != null)
              Padding(
                padding: const EdgeInsets.only(top: 12),
                child: Text(
                  "File saved at: ${dateFormat.format(_fileSavedTimestamp!)}",
                  style: const TextStyle(fontSize: 12, color: Colors.grey),
                ),
              ),

            const SizedBox(height: 20),
            _sectionTitle("Timestamp Notes"),
            ..._notes.map(
                  (note) => ListTile(
                leading: const Icon(Icons.flag),
                title: Text(note.note),
                subtitle: Text(dateFormat.format(note.timestamp)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _patientInfoForm() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          children: [
            TextField(
              controller: _idController,
              decoration: const InputDecoration(labelText: "Patient ID"),
            ),
            TextField(
              controller: _nameController,
              decoration: const InputDecoration(labelText: "Patient Name"),
            ),
            Row(
              children: [
                Expanded(
                  child: Text(
                    _birthday == null
                        ? "Birthday not selected"
                        : "Birthday: ${DateFormat.yMMMd().format(_birthday!)}",
                  ),
                ),
                TextButton(
                  onPressed: () async {
                    final picked = await showDatePicker(
                      context: context,
                      firstDate: DateTime(1900),
                      lastDate: DateTime.now(),
                      initialDate: DateTime(2000),
                    );
                    if (picked != null) {
                      setState(() => _birthday = picked);
                    }
                  },
                  child: const Text("Select Birthday"),
                ),
              ],
            ),
            TextField(
              controller: _historyController,
              maxLines: 3,
              decoration:
              const InputDecoration(labelText: "Patient History"),
            ),
          ],
        ),
      ),
    );
  }

  Widget _graphCard(String title) {
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 8),
      child: SizedBox(
        height: 150,
        child: Center(
          child: Text(
            "$title Graph Placeholder",
            style: const TextStyle(color: Colors.grey),
          ),
        ),
      ),
    );
  }

  Widget _sectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(
        title,
        style: const TextStyle(
          fontSize: 18,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }
}
