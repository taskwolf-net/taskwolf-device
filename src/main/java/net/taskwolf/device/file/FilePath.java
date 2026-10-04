package net.taskwolf.device.file;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public final class FilePath {
  private final String path;
  private final String name;

  public String compound() {
    var correctedPath = path.replace("//", "/").replace("\\\\", "/")
      .replace("\\", "/");
    if (path.charAt(path.length() - 1) != '/') {
      correctedPath += "/";
    }
    var correctedName = name.replace("//", "").replace("\\\\", "")
      .replace("\\", "");
    return correctedPath + correctedName;
  }
}
