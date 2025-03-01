package org.kenspeckle.landlock;

import java.util.List;

/**
 * This enum should be a representation of https://github.com/torvalds/linux/blob/master/include/uapi/linux/landlock.h
 * If there are values missing, please create a pull request or an github issue.
 */
public enum Flag {

   LANDLOCK_ACCESS_FS_EXECUTE(1, 1 << 0),
   LANDLOCK_ACCESS_FS_WRITE_FILE(1, 1 << 1),
   LANDLOCK_ACCESS_FS_READ_FILE(1, 1 << 2),
   LANDLOCK_ACCESS_FS_READ_DIR(1, 1 << 3),
   LANDLOCK_ACCESS_FS_REMOVE_DIR(1, 1 << 4),
   LANDLOCK_ACCESS_FS_REMOVE_FILE(1, 1 << 5),
   LANDLOCK_ACCESS_FS_MAKE_CHAR(1, 1 << 6),
   LANDLOCK_ACCESS_FS_MAKE_DIR(1, 1 << 7),
   LANDLOCK_ACCESS_FS_MAKE_REG(1, 1 << 8),
   LANDLOCK_ACCESS_FS_MAKE_SOCK(1, 1 << 9),
   LANDLOCK_ACCESS_FS_MAKE_FIFO(1, 1 << 10),
   LANDLOCK_ACCESS_FS_MAKE_BLOCK(1, 1 << 11),
   LANDLOCK_ACCESS_FS_MAKE_SYM(1, 1 << 12),
   LANDLOCK_ACCESS_FS_REFER(2, 1 << 13),
   LANDLOCK_ACCESS_FS_TRUNCATE(3, 1 << 14),
   LANDLOCK_ACCESS_FS_IOCTL_DEV(5, 1 << 15),

   LANDLOCK_ACCESS_NET_BIND_TCP(4, 1 << 0),
   LANDLOCK_ACCESS_NET_CONNECT_TCP(4, 1 << 1);

   private final int abiVersion;
   private final int value;

   Flag(final int abiVersion, final int value) {

      this.abiVersion = abiVersion;
      this.value = value;
   }

   public int getValue() {
      return value;
   }

   /**
    * @param abiVersion
    * @return if the current Flag value is supported in the given abiVersion of Landlock
    */
   public boolean flagSupported(final int abiVersion) {
      return this.abiVersion <= abiVersion;
   }

   /**
    * Helper-arrays for for different use cases
    */
   public static Flag[] READ_EXECUTE_FILE = {
           LANDLOCK_ACCESS_FS_EXECUTE,
           LANDLOCK_ACCESS_FS_READ_FILE
   };

   public static Flag[] READ_EXECUTE_DIR = {
           LANDLOCK_ACCESS_FS_EXECUTE,
           LANDLOCK_ACCESS_FS_READ_FILE,
           LANDLOCK_ACCESS_FS_READ_DIR,
   };

   public static Flag[] READ_DIR = {
           LANDLOCK_ACCESS_FS_READ_FILE,
           LANDLOCK_ACCESS_FS_READ_DIR,
   };

   public static Flag[] READ_WRITE_DIR = {
           LANDLOCK_ACCESS_FS_READ_FILE,
           LANDLOCK_ACCESS_FS_READ_DIR,
           LANDLOCK_ACCESS_FS_MAKE_DIR,
           LANDLOCK_ACCESS_FS_REMOVE_DIR,
           LANDLOCK_ACCESS_FS_TRUNCATE,
           LANDLOCK_ACCESS_FS_WRITE_FILE,
           LANDLOCK_ACCESS_FS_REMOVE_FILE
   };

   public static Flag[] EVERYTHING_FS = {
           LANDLOCK_ACCESS_FS_EXECUTE,
           LANDLOCK_ACCESS_FS_WRITE_FILE,
           LANDLOCK_ACCESS_FS_READ_FILE,
           LANDLOCK_ACCESS_FS_READ_DIR,
           LANDLOCK_ACCESS_FS_REMOVE_DIR,
           LANDLOCK_ACCESS_FS_REMOVE_FILE,
           LANDLOCK_ACCESS_FS_MAKE_CHAR,
           LANDLOCK_ACCESS_FS_MAKE_DIR,
           LANDLOCK_ACCESS_FS_MAKE_REG,
           LANDLOCK_ACCESS_FS_MAKE_SOCK,
           LANDLOCK_ACCESS_FS_MAKE_FIFO,
           LANDLOCK_ACCESS_FS_MAKE_BLOCK,
           LANDLOCK_ACCESS_FS_MAKE_SYM,
           LANDLOCK_ACCESS_FS_REFER,
           LANDLOCK_ACCESS_FS_TRUNCATE
   };

   public static List<Flag> allFlags() {
      return List.of(Flag.values());
   }

}
