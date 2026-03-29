import 'package:flutter/material.dart';
import 'theme/truepulse_theme.dart';
import 'screens/patient_session_screen.dart';

void main() {
  runApp(const TruePulseApp());
}

class TruePulseApp extends StatelessWidget {
  const TruePulseApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'TruePulse',
      theme: truePulseTheme,
      home: const PatientSessionScreen(),
    );
  }
}

//   runApp(const MyApp());
// }
//
// class MyApp extends StatelessWidget {
//   const MyApp({super.key});
//
//   @override
//   Widget build(BuildContext context) {
//     return MaterialApp(
//       title: 'Patient Monitoring App',
//       theme: ThemeData(
//         useMaterial3: true,
//         colorSchemeSeed: Colors.blue,
//       ),
//       home: const PatientInputScreen(),
//     );
//   }
//}