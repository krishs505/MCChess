# MCChess

**MCChess** is a Minecraft Spigot plugin that brings the game of chess into the Minecraft world. Players can enjoy full-featured chess matches on an 8×8 board rendered with in-game blocks and interact with it using simple in-game actions and commands.

---

## 🧱 Features

- **Interactive Chess Board**: An 8×8 board built with Minecraft blocks.
- **Standard Chess Rules Implementation**: Move pieces using block interactions, following official rules, including castling, en passant, check/mate detection, etc.
- **Pawn Promotion**: Promote to Queen, Rook, Bishop, or Knight via in-game GUI.
- **Chess Clock**: Optional game timer with increment support.
- **FEN Support**: Load or export board state with Forsyth-Edwards Notation.
- **Block Protection**: Optional toggle to prevent non-chess block destruction.
- **Fireworks Celebration**: A stunning fireworks show commences using a randomized algorithm at the conclusion of a game.
- **Utility Commands**: Includes tools like `/fly` and `/speed` for enhanced gameplay.

<img width="898" height="619" alt="Screenshot 2025-07-11 211727" src="https://github.com/user-attachments/assets/8dcabfe5-c071-4db4-a37c-87091dbcafa1" />

---

## ⌨️ Commands

| Command | Description |
|--------|-------------|
| `/blockRestore` | Toggle protection for non-chess blocks. |
| `/reset` | Resets the board and game state. |
| `/loadfen <FEN>` | Load a board position using FEN. |
| `/getfen` | Display and copy current FEN string. |
| `/clock <time>+<increment>` | Start a timed game (e.g., `/clock 5+3`). |
| `/resetClock` | Reset the chess clock. |
| `/resign` | Resign from the game. |
| `/fly` | Toggle flight mode. |
| `/white [player]` | Assign a player as White. |
| `/black [player]` | Assign a player as Black. |
| `/players` | View current White and Black players. |
| `/speed [0–10]` | Set or view your walking/flying speed. |

---

## 🕹️ How to Play

1. **Install the Plugin**
   - Download the `MCChess.jar` file from [here](https://www.spigotmc.org/resources/minecraft-chess.111884/) and place it into your server’s `plugins/` folder.
 
2. **Download Map**
   - Download the Minecraft Chess map [here](https://www.mediafire.com/file/rzosyep50bmcsmw/MCChess.zip/file) and install it to your server folder.
  
3. **Assign Players**
   - Use `/white` and `/black` to assign players.

4. **Move Pieces**
   - Select your piece by right-clicking or breaking the block (ensure /blockRestore is on).
   - Move to a legal square by interacting with it.
   - Illegal moves or wrong turns are blocked.

5. **Pawn Promotion**
   - When a pawn reaches the last rank, a GUI will appear to choose a promotion piece.

6. **Use the Clock (Optional)**
   - Start timed games using `/clock`.

7. **Game End**
   - The plugin will announce checkmate or stalemate.
   - Fireworks celebrate the game's conclusion.

8. **Reset**
   - Use `/reset` to start a new game.

---

## ⚙️ Development Notes

- Board visuals are managed using Minecraft’s `/clone` command.
- Pieces are internally represented as strings (e.g., `"WP"` for White Pawn).
- Minecraft block coordinates are mapped to a standard 8×8 array.
- Move validation and game rules (castling, check, en passant, etc.) are implemented in Java.
- GUI elements (like promotion) are built with Bukkit’s Inventory API.

---
