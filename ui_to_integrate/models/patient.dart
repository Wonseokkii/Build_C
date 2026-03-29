class Patient {
  final String id;
  final String name;
  final DateTime birthday;
  final String history;

  Patient({
    required this.id,
    required this.name,
    required this.birthday,
    required this.history,
  });

  int get age {
    final now = DateTime.now();
    return now.year - birthday.year -
        ((now.month < birthday.month ||
            (now.month == birthday.month && now.day < birthday.day))
            ? 1
            : 0);
  }
}