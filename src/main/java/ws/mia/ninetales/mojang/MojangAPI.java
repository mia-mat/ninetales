package ws.mia.ninetales.mojang;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface MojangAPI {

	@Nullable
	UUID getUuid(String username);

	@Nullable
	String getUsername(UUID uuid);

}
