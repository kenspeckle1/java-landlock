import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kenspeckle.landlock.Flag;
import org.kenspeckle.landlock.Landlock;
import org.kenspeckle.landlock.RuleSet;

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled
public class TestBlockEverythingAllowBind {

	@Test
	void testBlockEverythingAllowBind() {
		assertDoesNotThrow(Landlock::loadLandlock);

		RuleSet ruleSet = new RuleSet();
		ruleSet.addNetRule(Flag.LANDLOCK_ACCESS_NET_BIND_TCP, 12345);
		ruleSet.restrictSelf();
		assertThrows(AccessDeniedException.class, () -> Files.createTempFile("", ""));
		assertDoesNotThrow(() -> new ServerSocket(12345));
		assertThrows(BindException.class, () -> new Socket("1.1.1.1", 53));
	}

}
