## Compass Bar v1.0.3-mc26.1.2

### Changes

- Compass HUD is now enabled by default when joining a game.
- Reduced player position synchronization from 20 Hz to 10 Hz while retaining client-side interpolation.
- Moved player marker color assignment to the server.
- Player marker colors are now persisted per UUID in world SavedData and remain stable across reconnects and server restarts.
- Exact RGB color reuse is prevented for newly assigned players.
- New colors are selected from a broader visible color range using OKLab distance so they remain visually distinct from existing colors while avoiding colors that are too dark or overly washed out.
- Added dedicated server-to-client synchronization for persistent player colors.
- Updated the network protocol for the new color synchronization payload.

### Compatibility

- Minecraft 26.1.2
- NeoForge 26.1.2.97
- Java 25
