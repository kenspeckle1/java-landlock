package org.kenspeckle.landlock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a wrapper around the landlock binding.
 * It loads the library and holds a list of all {@link RuleSet}s.
 * Note that the constructor of {@link RuleSet} will call {@link Landlock#addRuleSet} and thus all {@link RuleSet}s
 * should automatically have a reference in the class.
 */
public class Landlock {

	/**
	 * This needs to be called before any of the {@link RuleSet} native functions are called.
	 *
	 * Tries to load the JNI Library from the classpath by copying it to a temporary file.
	 * I see the irony in using a library to restrict access to the filesystem and the first thing this library does
	 * is create a file, but this was the "most elegant" solution I found.
	 *
	 * @throws IOException if we are unable to create the temp file, remove it or open the 'libjavalandlock.so' file from the classpath
	 */
	public static void loadLandlock() throws IOException {
		Path tempFile = Files.createTempFile("libjavalandlock_", "");
		try (var inStream = Landlock.class.getResourceAsStream("/libjavalandlock.so")) {
			assert inStream != null;
			Files.copy(inStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
		}

		System.load(tempFile.toAbsolutePath().toString());
		Files.delete(tempFile);
	}

	private static Landlock instance = null;

	public static Landlock getInstance() {
		if (instance == null) {
			instance = new Landlock();
		}
		return instance;
	}

	private final List<RuleSet> ruleSets;

	private Landlock() {
		ruleSets = new ArrayList<>();
	}

	public void addRuleSet(final RuleSet ruleSet) {
		ruleSets.add(ruleSet);
	}

	public List<RuleSet> getRuleSets() {
		return ruleSets;
	}

	public List<RuleSet> getActivatedRuleSets() {
		return ruleSets.stream().filter(RuleSet::isClosed).toList();
	}
}
