package com.example.rossmaguire.beacontest;

public class Constants {

    public interface ACTION {
        public static String MAIN_ACTION = "com.example.rossmaguire.beacontest.action.main";
        public static String STARTFOREGROUND_ACTION = "com.example.rossmaguire.beacontest.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.example.rossmaguire.beacontest.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
