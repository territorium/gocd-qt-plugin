/*
 * Copyright (c) 2001-2019 Territorium Online Srl / TOL GmbH. All Rights Reserved.
 *
 * This file contains Original Code and/or Modifications of Original Code as defined in and that are
 * subject to the Territorium Online License Version 1.0. You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at http://www.tol.info/license/
 * and read it before using this file.
 *
 * The Original Code and all software distributed under the License are distributed on an 'AS IS'
 * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TERRITORIUM ONLINE HEREBY
 * DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT. Please see the License for
 * the specific language governing rights and limitations under the License.
 */

package cd.go.task.qt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link Qt} class.
 */
public class Qt {

  public static final String MODE_BUILD       = "BUILD";
  public static final String MODE_QMAKE       = "QMAKE";
  public static final String MODE_MAKE        = "MAKE";
  public static final String MODE_TEST        = "TEST";

  public static final String QT_HOME          = "QT_HOME";
  public static final String QT_ARCH          = "QT_ARCH";
  public static final String QT_SPEC          = "QT_SPEC";
  public static final String QT_CONFIG        = "QT_CONFIG";

  public static final String QT_REPO          = "QT_REPO";
  public static final String QT_BUILD         = "QT_BUILD";
  public static final String QT_PLUGIN_PATH   = "QT_PLUGIN_PATH";
  public static final String QML2_IMPORT_PATH = "QML2_IMPORT_PATH";

  public static final String QT_REPOSITORY    = "QT_REPOSITORY";
  public static final String QT_OVERLAY       = "QT_OVERLAY";
  public static final String RELEASE          = "RELEASE";


  private static final String ANDROID      = "ANDROID";
  private static final String IOS          = "IOS";
  private static final String LINUX        = "LINUX";
  private static final String WINDOS       = "WINDOWS";

  private static final String ARCH_ANDROID = "android_armv7";
  private static final String ARCH_IOS     = "ios";
  private static final String ARCH_LINUX   = "gcc_64";
  private static final String ARCH_WINDOWS = "msvc2017_64";

  private static final String SPEC_ANDROID = "android-clang";
  private static final String SPEC_IOS     = "macx-ios-clang";
  private static final String SPEC_LINUX   = "linux-g++";
  private static final String SPEC_WINDOWS = "win32-msvc";


  private static final Map<String, String> SPECS = new HashMap<>();
  private static final Map<String, String> ARCHS = new HashMap<>();

  static {
    SPECS.put(ANDROID, SPEC_ANDROID);
    SPECS.put(IOS, SPEC_IOS);
    SPECS.put(LINUX, SPEC_LINUX);
    SPECS.put(WINDOS, SPEC_WINDOWS);

    ARCHS.put(ANDROID, ARCH_ANDROID);
    ARCHS.put(IOS, ARCH_IOS);
    ARCHS.put(LINUX, ARCH_LINUX);
    ARCHS.put(WINDOS, ARCH_WINDOWS);
  }

  /**
   * Get the Qt Architecture.
   *
   * @param context
   * @param config
   */
  public static final String getArch(Context context, Config config) {
    // return ARCHS.get(config.getPlatform().toUpperCase());
    return context.getEnvironment().get(Qt.QT_ARCH);

  }

  /**
   * Get the Qt Specification.
   *
   * @param context
   * @param config
   */
  public static final String getSpec(Context context, Config config) {
    // return SPECS.get(config.getPlatform().toUpperCase());
    return context.getEnvironment().get(Qt.QT_SPEC);
  }

  /**
   * Get the Qt Specification.
   *
   * @param context
   * @param config
   */
  public static final List<String> getConfig(Context context, Config config) {
    List<String> configs = new ArrayList<>();
    String env = context.getEnvironment().get(Qt.QT_CONFIG);
    if (env != null && !env.trim().isEmpty()) {
      Arrays.asList(env.split(",")).forEach(c -> configs.add(c.trim()));
    }
    return configs;
  }
}
