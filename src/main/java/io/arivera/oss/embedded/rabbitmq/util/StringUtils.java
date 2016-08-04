package io.arivera.oss.embedded.rabbitmq.util;

import java.util.Collection;

public class StringUtils {

  public static <T> String join(Collection<T> collection, CharSequence joinedBy){
    StringBuilder stringBuilder = new StringBuilder(256);
    for (T t : collection) {
      stringBuilder.append(t.toString()).append(joinedBy);
    }
    return stringBuilder.toString();
  }

}
