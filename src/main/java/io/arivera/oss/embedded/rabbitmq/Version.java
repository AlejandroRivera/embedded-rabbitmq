package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;
import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * A class that provides information about a specific distribution artifact version of RabbitMQ.
 */
public interface Version {

  VersionComparator VERSION_COMPARATOR = new VersionComparator();

  /**
   * @return a String formatted like {@code "3.6.5"}
   *
   * @see #getVersionAsString(CharSequence)
   */
  String getVersionAsString();

  /**
   * @return a String formatted like {@code "3_6_5"} if given {@code "_"} as separator.
   */
  String getVersionAsString(CharSequence separator);


  /**
   * @return correct Archive Type for the given OS.
   */
  ArchiveType getArchiveType(OperatingSystem operatingSystem);

  /**
   * @return folder name under which the compressed application is extracted to.
   */
  String getExtractionFolder();

  /**
   * @return minimum version required to run this RabbitMQ version. Example: {@code "R16B3"} or {code null}
   */
  String getMinimumErlangVersion();

  /**
   * @return an Array like [3,6,5] for version 3.6.5
   */
  List<Integer> getVersionComponents();

  class VersionComparator implements Comparator<Version>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Version v1, Version v2) {
      for (int i = 0; i < v1.getVersionComponents().size(); i++) {
        int comp = v1.getVersionComponents().get(i).compareTo(v2.getVersionComponents().get(i));
        if (comp != 0) {
          return comp;
        }
      }
      return 0;
    }

  }

}
