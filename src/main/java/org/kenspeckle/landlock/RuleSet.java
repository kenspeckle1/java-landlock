package org.kenspeckle.landlock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * This class is the representation of the linux landlock ruleset (https://www.man7.org/linux/man-pages/man2/landlock_create_ruleset.2.html)
 * Note that all methods follow a whitelist approach. That means, that everything will be forbidden, that is not allowed
 */
public class RuleSet {

	/**
	 * Internal linux ruleset descriptor
	 */
	private long ruleSetFd;

	/**
	 * saves if this RuleSet was already applied and thus is read-only
	 */
	private boolean closed;

	public RuleSet() {
		Landlock.getInstance().addRuleSet(this);
		ruleSetFd = createRuleSetNative(0, 0);
		if (ruleSetFd < 0) {
			throw new LandlockException("Landlock is not supported!");
		}
		closed = false;
	}

	public void addFsRule(final List<Flag> allowFlags, final Path path) {
		if (closed) {
			throw new LandlockException("Ruleset already closed, open a new one");
		}
		if (Files.isRegularFile(path) && containsDirFlag(allowFlags)) {
			throw new LandlockException("Directory flags are not allowed on files");
		}
		int allowedFsFlags = flagsToInt(allowFlags);
		if (allowedFsFlags == 0) {
			throw new LandlockException("Empty accesses is not allowed");
		}
		if (addFsRuleNative(ruleSetFd, allowedFsFlags, path.toAbsolutePath().toString()) == 0) {
			throw new LandlockException("Unable to add filesystem rule for path %s".formatted(path.toAbsolutePath().toString()));
		}
	}

	public void addFsRule(final List<Flag> allowFlags, final String path) {
		if (closed) {
			throw new LandlockException("Ruleset already closed, open a new one");
		}
		int allowedFsFlags = flagsToInt(allowFlags);
		if (allowedFsFlags == 0) {
			throw new LandlockException("A least one flag needs to be specified");
		}
		if (addFsRuleNative(ruleSetFd, allowedFsFlags, path) != 0) {
			throw new LandlockException("Unable to add filesystem rule for path %s".formatted(path));
		}
	}

	public void addFsRule(final String path, final Flag ... allowFlags) {
		addFsRule(List.of(allowFlags), path);
	}

	public void addNetRule(final List<Flag> allowFlags, final int port) {
		if (closed) {
			throw new LandlockException("Ruleset already closed, open a new one");
		}

		int allowedNetFlags = flagsToInt(allowFlags);
		if (allowedNetFlags == 0) {
			throw new LandlockException("At least one flag needs to be specified");
		}
		if (addNetRuleNative(ruleSetFd, allowedNetFlags, port) != 0) {
			throw new LandlockException("Unable to add net rule for port %d".formatted(port));
		}
	}

	public void addNetRule(final Flag allowFlag, final int port) {
		if (closed) {
			throw new LandlockException("Ruleset already closed, open a new one");
		}

		if (allowFlag == null) {
			throw new LandlockException("Empty accesses is not allowed");
		}
		int flagValue = allowFlag.getValue();
		if (addNetRuleNative(ruleSetFd, flagValue, port) != 0) {
			throw new LandlockException("Unable to add net rule for port %d".formatted(port));
		}
	}

	public void restrictSelf() {
		if (closed) {
			throw new LandlockException("Unable to restrict self as this ruleset is already enforced");
		}
		int err = restrictSelfNative(ruleSetFd);
		switch (err) {
			case 1:
				throw new LandlockException("Failed to restrict privileges");
			case 2:
				throw new LandlockException("Failed to enforce ruleset");
		}
		closed = true;
	}

	public boolean isClosed() {
		return closed;
	}

	/**
	 * Creates a new ruleset. Note that a flag might be removed, if the running kernel does not support it
	 * @param allowedFsFlags filesystem flags that should be allowed
	 * @param allowedNetFlags net flags that should be allowed
	 * @return the ruleset file descriptor
	 */
	private native int createRuleSetNative(final long allowedFsFlags, final long allowedNetFlags);

	/**
	 * This call prevents the current thread from gaining more privileges (e.g. by calling a SUID binary) and then enforces the ruleset
	 * @return  false/0 if there was no error (C-Style)
	 *          1 if it is not possible to restrict privileges
	 *          2 if it is not possible to enforce the specified ruleset
	 */
	private native int restrictSelfNative(final long ruleSetFd);

	/**
	 * @return false/0 if there was no error (C-Style)
	 */
	private native int addFsRuleNative(final long ruleSetFd, final long allowedFsFlags, final String path);

	/**
	 * @return false/0 if there was no error (C-Style)
	 * generally it forwards the return code of landlock_add_rule()
	 */
	private native int addNetRuleNative(final long ruleSetFd, final long allowedNetFlags, final int port);

	/**
	 * @return the by the landlock version supported by the current linux kernel
	 */
	public native int getLandlockAbiVersion();
	
	public static int flagsToInt(final List<Flag> flags) {
		int ret = 0;
		for (var flag : flags) {
			ret |= flag.getValue();
		}
		return ret;
	}

	public static boolean containsDirFlag(final List<Flag> flags) {
		return flags.stream().anyMatch(f -> f == Flag.LANDLOCK_ACCESS_FS_MAKE_DIR || f == Flag.LANDLOCK_ACCESS_FS_READ_DIR || f == Flag.LANDLOCK_ACCESS_FS_REMOVE_DIR);
	}
}
