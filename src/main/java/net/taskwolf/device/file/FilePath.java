package net.taskwolf.device.file;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public final class FilePath {
  private final String path;
  private final String name;

  public String compound() {
    var link = "";
    if (path.contains("/")) {
      link = "/";
    } else if (path.contains("//")) {
      link = "//";
    } else if (path.contains("\\")) {
      link = "\\";
    } else if (path.contains("\\\\")) {
      link = "\\\\";
    }
    return path + link + name;
  }
}
