
package info.tol.gocd.task.qt;


public enum QtPlatform {

  ANDROID("android_armv7", "android-clang"),
  IOS("ios", "macx-ios-clang"),
  LINUX("gcc_64", "linux-g++"),
  WINDOS("msvc2017_64", "win32-msvc");

  public final String ARCH;
  public final String SPEC;

  private QtPlatform(String aRCH, String sPEC) {
    this.ARCH = aRCH;
    this.SPEC = sPEC;
  }
}
