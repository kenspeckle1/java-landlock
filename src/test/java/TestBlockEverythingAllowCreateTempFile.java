import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kenspeckle.landlock.Flag;
import org.kenspeckle.landlock.Landlock;
import org.kenspeckle.landlock.RuleSet;

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
class TestBlockEverythingAllowCreateTempFile {

	@Test
	void testBlockEverythingAllowCreateTempFile() {
		assertDoesNotThrow(Landlock::loadLandlock);

		RuleSet ruleSet = new RuleSet();
		ruleSet.addFsRule("/tmp", Flag.EVERYTHING_FS);
		ruleSet.restrictSelf();
		assertDoesNotThrow(() -> Files.createTempFile("", ""));
		assertThrows(BindException.class, () -> new ServerSocket(12345));
		assertThrows(BindException.class, () -> new Socket("1.1.1.1", 53));
	}
}