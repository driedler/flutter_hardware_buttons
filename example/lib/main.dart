import 'dart:async';

import 'package:flutter/material.dart';
import 'package:hardware_buttons/hardware_buttons.dart' as HardwareButtons;

void main() => runApp(MaterialApp(
    home: MyApp()));

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              Text('Value'),
            ],
          ),
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: () {
            Navigator.push(context, MaterialPageRoute(builder: (context)
            =>SecondPage()
            ));
          },
          child: Icon(Icons.add),
          backgroundColor: Colors.green,
        ),
    );
  }
}

class SecondPage extends StatefulWidget {
  @override
  SecondPageState createState() => SecondPageState();
}

class SecondPageState extends State<SecondPage> with WidgetsBindingObserver{
  String _latestHardwareButtonEvent;

  StreamSubscription<
      HardwareButtons.VolumeButtonEvent> _volumeButtonSubscription;
  StreamSubscription<HardwareButtons.HomeButtonEvent> _homeButtonSubscription;
  StreamSubscription<HardwareButtons.LockButtonEvent> _lockButtonSubscription;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance..addObserver(this)..addPostFrameCallback((timeStamp) {
      _volumeButtonSubscription =
          HardwareButtons.volumeButtonEvents.listen((event) {
            setState(() {
              _latestHardwareButtonEvent = event.toString();
            });
          });

      _homeButtonSubscription = HardwareButtons.homeButtonEvents.listen((event) {
        setState(() {
          _latestHardwareButtonEvent = 'HOME_BUTTON';
        });
      });

      _lockButtonSubscription = HardwareButtons.lockButtonEvents.listen((event) {
        setState(() {
          _latestHardwareButtonEvent = 'LOCK_BUTTON';
        });
      });
    });
  }

  @override
  void dispose() {
    super.dispose();
    _volumeButtonSubscription?.cancel();
    _homeButtonSubscription?.cancel();
    _lockButtonSubscription?.cancel();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              Text('Value: $_latestHardwareButtonEvent\n'),
            ],
          ),
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: () {
            Navigator.pop(context);
          },
          child: Icon(Icons.add),
          backgroundColor: Colors.green,
        ),
      ),
    );
  }
}
