import 'package:flutter/material.dart';

const Color truePulsePrimary = Color(0xFF630436);

final ThemeData truePulseTheme = ThemeData(
  useMaterial3: true,
  primaryColor: truePulsePrimary,
  scaffoldBackgroundColor: const Color(0xFFFAF7F8),

  colorScheme: ColorScheme.fromSeed(
    seedColor: truePulsePrimary,
    primary: truePulsePrimary,
    secondary: const Color(0xFFB23A48),
  ),

  appBarTheme: const AppBarTheme(
    backgroundColor: truePulsePrimary,
    foregroundColor: Colors.white,
    centerTitle: true,
  ),

  cardTheme: CardTheme(
    elevation: 2,
    shape: RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(16),
    ),
  ),

  inputDecorationTheme: InputDecorationTheme(
    filled: true,
    fillColor: Colors.white,
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(14),
      borderSide: BorderSide.none,
    ),
    labelStyle: const TextStyle(color: truePulsePrimary),
  ),

  elevatedButtonTheme: ElevatedButtonThemeData(
    style: ElevatedButton.styleFrom(
      backgroundColor: truePulsePrimary,
      foregroundColor: Colors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(14),
      ),
    ),
  ),

  floatingActionButtonTheme: const FloatingActionButtonThemeData(
    backgroundColor: truePulsePrimary,
    foregroundColor: Colors.white,
  ),
);