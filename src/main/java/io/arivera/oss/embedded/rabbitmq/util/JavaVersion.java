package io.arivera.oss.embedded.rabbitmq.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Copied from Apache Commons Lang3 v3.4
 */
public enum JavaVersion {
  JAVA_0_9(1.5F, "0.9"),
  JAVA_1_1(1.1F, "1.1"),
  JAVA_1_2(1.2F, "1.2"),
  JAVA_1_3(1.3F, "1.3"),
  JAVA_1_4(1.4F, "1.4"),
  JAVA_1_5(1.5F, "1.5"),
  JAVA_1_6(1.6F, "1.6"),
  JAVA_1_7(1.7F, "1.7"),
  JAVA_1_8(1.8F, "1.8"),
  JAVA_1_9(1.9F, "1.9"),
  JAVA_RECENT(maxVersion(), Float.toString(maxVersion()));

  private final float value;
  private final String name;

  private JavaVersion(float value, String name) {
    this.value = value;
    this.name = name;
  }

  public boolean atLeast(JavaVersion requiredVersion) {
    return this.value >= requiredVersion.value;
  }

  static JavaVersion getJavaVersion(String nom) {
    return get(nom);
  }

  static JavaVersion get(String nom) {
    if("0.9".equals(nom)) {
      return JAVA_0_9;
    } else if("1.1".equals(nom)) {
      return JAVA_1_1;
    } else if("1.2".equals(nom)) {
      return JAVA_1_2;
    } else if("1.3".equals(nom)) {
      return JAVA_1_3;
    } else if("1.4".equals(nom)) {
      return JAVA_1_4;
    } else if("1.5".equals(nom)) {
      return JAVA_1_5;
    } else if("1.6".equals(nom)) {
      return JAVA_1_6;
    } else if("1.7".equals(nom)) {
      return JAVA_1_7;
    } else if("1.8".equals(nom)) {
      return JAVA_1_8;
    } else if("1.9".equals(nom)) {
      return JAVA_1_9;
    } else if(nom == null) {
      return null;
    } else {
      float v = toFloatVersion(nom);
      if((double)v - 1.0D < 1.0D) {
        int firstComma = Math.max(nom.indexOf(46), nom.indexOf(44));
        int end = Math.max(nom.length(), nom.indexOf(44, firstComma));
        if(Float.parseFloat(nom.substring(firstComma + 1, end)) > 0.9F) {
          return JAVA_RECENT;
        }
      }

      return null;
    }
  }

  public String toString() {
    return this.name;
  }

  private static float maxVersion() {
    float v = toFloatVersion(System.getProperty("java.version", "2.0"));
    return v > 0.0F?v:2.0F;
  }

  private static float toFloatVersion(String value) {
    String[] toParse = value.split("\\.");
    if(toParse.length >= 2) {
      try {
        return Float.parseFloat(toParse[0] + '.' + toParse[1]);
      } catch (NumberFormatException var3) {
        ;
      }
    }

    return -1.0F;
  }
}
