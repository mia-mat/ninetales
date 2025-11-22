package ws.mia.ninetales.hypixel;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface HypixelAPI {

	@Nullable
	String getDiscord(UUID uuid);

}
