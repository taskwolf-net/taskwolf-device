package com.dulno.device.structure;

public enum DevicePlatform {
  ANDROID,
  IOS,
  WINDOWS,
  LINUX,
  MACOS;

  public boolean isAndroid() {
    return this == ANDROID;
  }

  public boolean isIOS() {
    return this == IOS;
  }

  public boolean isWindows() {
    return this == WINDOWS;
  }

  public boolean isLinux() {
    return this == LINUX;
  }

  public boolean isMacOS() {
    return this == MACOS;
  }

  public boolean isMobile() {
    return isAndroid() || isIOS();
  }

  public boolean isDesktop() {
    return isWindows() || isLinux() || isMacOS();
  }
}
