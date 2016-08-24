package io.arivera.oss.embedded.rabbitmq.util;

import java.util.Collection;

public class StringUtils {

  /**
   * Joins the elements of the provided collection into a single String containing the provided list of elements.
   * <p>
   * No delimiter is added before or after the list.
   * <p>
   * Empty collections return an empty String.
   */
  public static <T> String join(Collection<T> collection, CharSequence joinedBy) {
    if (collection.isEmpty()) {
      return "";
    }
    StringBuilder stringBuilder = new StringBuilder(256);
    for (T t : collection) {
      stringBuilder.append(t.toString()).append(joinedBy);
    }
    return stringBuilder.substring(0, stringBuilder.length() - joinedBy.length());
  }

}
