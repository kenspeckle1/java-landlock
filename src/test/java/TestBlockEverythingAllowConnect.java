import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kenspeckle.landlock.Flag;
import org.kenspeckle.landlock.Landlock;
import org.kenspeckle.landlock.RuleSet;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
public class TestBlockEverythingAllowConnect {
	@Test
	void testBlockEverythingAllowConnect() {
		assertDoesNotThrow(Landlock::loadLandlock);

		int port = 12345;
		ServerSocket socketBeforeLandlock = null;
		try {
			socketBeforeLandlock = new ServerSocket(port);
		} catch (IOException e) {
			fail(e);
		}
		RuleSet ruleSet = new RuleSet();
		ruleSet.addNetRule(Flag.LANDLOCK_ACCESS_NET_CONNECT_TCP, 12345);
		ruleSet.restrictSelf();
		assertThrows(AccessDeniedException.class, () -> Files.createTempFile("", ""));
		assertThrows(BindException.class, () -> new ServerSocket(12346));
		assertDoesNotThrow(() -> new Socket("127.0.0.1", 12345));
		assertThrows(BindException.class, () -> new Socket("127.0.0.1", 12346));
		try {
			socketBeforeLandlock.close();
		} catch (IOException e) {
			fail(e);
		}
	}

}
