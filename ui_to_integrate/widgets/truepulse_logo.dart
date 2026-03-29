import 'package:flutter/material.dart';

class TruePulseLogo extends StatelessWidget {
  const TruePulseLogo({super.key});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: const [
        Icon(Icons.favorite, size: 48, color: Color(0xFF630436)),
        SizedBox(height: 8),
        Text(
          "TruePulse",
          style: TextStyle(
            fontSize: 22,
            fontWeight: FontWeight.bold,
            color: Color(0xFF630436),
          ),
        ),
      ],
    );
  }
}