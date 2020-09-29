package com.omarea.model;

public enum TaskAction {
    FSTRIM,             // fstrim
    STANDBY_MODE_ON,    // 待机模式
    STANDBY_MODE_OFF,   // 待机模式
    AIRPLANE_MODE_ON,   // 飞行模式开
    AIRPLANE_MODE_OFF,  // 飞行模式关
    WIFI_ON,            // wifi开
    WIFI_OFF,           // wifi关
    GPRS_ON,            // 数据开
    GPRS_OFF,           // 数据关
    GPS_ON,             // GPS开
    GPS_OFF,            // GPS关
    ZEN_MODE_ON,        // 勿扰模式开
    ZEN_MODE_OFF,       // 勿扰模式关
    COMPILE_SPEED,      // Speed模式编译
    COMPILE_EVERYTHING, // Everything模式编译
    POWER_REBOOT,       // 重启手机
    POWER_OFF,          // 关机手机
    MODE_POWERSAVE,     // 省电模式
    MODE_BALANCE,      // 均衡模式
    MODE_PERFORMANCE,  // 性能模式
    MODE_FAST,         // 极速模式
    FROZEN_APPS,       // 冻结偏见应用
}
