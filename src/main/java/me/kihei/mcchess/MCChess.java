package me.kihei.mcchess;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.logging.Logger;

public final class MCChess extends JavaPlugin implements Listener {
    public boolean blockRestore = true;

    public static String[][] board = new String[8][8];
    public static String playerW = "";
    public static String playerB = "";
    public int selR = -1;
    public int selC = -1;

    private boolean whiteCanKCastle = true;
    public boolean whiteCanQCastle = true;
    public boolean blackCanKCastle = true;
    public boolean blackCanQCastle = true;
    public static int[] lastDoublePawn = null;

    public static String turn = "W";
    public static String winner = "";

    public static Inventory gui;
    public static Object[] promotionData;
    public static String promoteChoice = "";
    public static boolean promoteDone = false;

    int fireworkCount = 0;
    String[] fireworkTypes = new String[] {"{LifeTime:35,FireworksItem:{id:firework_rocket,Count:1,tag:{Fireworks:{Flight:3,Explosions:[{Type:1,Flicker:0,Trail:1,Colors:[I;1973019,11743532]}]}}}}", "{LifeTime:35,FireworksItem:{id:firework_rocket,Count:1,tag:{Fireworks:{Flight:3,Explosions:[{Type:1,Flicker:0,Trail:1,Colors:[I;2651799,11250603]}]}}}}", "{LifeTime:35,FireworksItem:{id:firework_rocket,Count:1,tag:{Fireworks:{Flight:3,Explosions:[{Type:1,Flicker:0,Trail:1,Colors:[I;4312372,15435844]}]}}}}", "{LifeTime:35,FireworksItem:{id:firework_rocket,Count:1,tag:{Fireworks:{Flight:3,Explosions:[{Type:1,Flicker:0,Trail:1,Colors:[I;8073150,15790320]}]}}}}", "{LifeTime:35,FireworksItem:{id:firework_rocket,Count:1,tag:{Fireworks:{Flight:3,Explosions:[{Type:1,Flicker:0,Trail:1,Colors:[I;1973019,11743532,3887386,5320730,2437522,8073150,2651799,11250603,4408131,14188952,4312372,14602026,6719955,12801229,15435844,15790320]}]}}}}"};

    int minW;
    int secW;
    int minB;
    int secB;
    int inc;
    boolean resetWCalled = false;
    boolean resetBCalled = false;
    boolean timerWRunning = false;
    boolean timerBRunning = false;

    private Logger log;

    @Override
    public void onEnable() {
        log = getLogger();
        log.info("MCChess is enabled!");

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new MenuCloseHandler(this), this);

        board[0] = new String[]{"BR", "BN", "BB", "BQ", "BK", "BB", "BN", "BR"};
        board[1] = new String[]{"BP", "BP", "BP", "BP", "BP", "BP", "BP", "BP"};
        board[6] = new String[]{"WP", "WP", "WP", "WP", "WP", "WP", "WP", "WP"};
        board[7] = new String[]{"WR", "WN", "WB", "WQ", "WK", "WB", "WN", "WR"};
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            String cmd = command.getName();

            if (cmd.equalsIgnoreCase("blockRestore")) {
                if (blockRestore) {
                    blockRestore = false;
                    p.sendMessage(ChatColor.GREEN + "blockRestore off!");
                } else {
                    blockRestore = true;
                    p.sendMessage(ChatColor.GREEN + "blockRestore on!");
                }

            }
            if (cmd.equalsIgnoreCase("reset")) {
                p.sendMessage("Loading..");
                //playerW = "";
                //playerB = "";
                turn = "W";
                winner = "";
                selR = -1;
                selC = -1;
                whiteCanKCastle = true;
                whiteCanQCastle = true;
                blackCanKCastle = true;
                blackCanQCastle = true;
                lastDoublePawn = null;
                board = new String[8][8];
                board[0] = new String[]{"BR", "BN", "BB", "BQ", "BK", "BB", "BN", "BR"};
                board[1] = new String[]{"BP", "BP", "BP", "BP", "BP", "BP", "BP", "BP"};
                board[6] = new String[]{"WP", "WP", "WP", "WP", "WP", "WP", "WP", "WP"};
                board[7] = new String[]{"WR", "WN", "WB", "WQ", "WK", "WB", "WN", "WR"};
                minW = 0;
                minB = 0;
                secW = 0;
                secB = 0;
                if (timerWRunning) {
                    resetWCalled = true;
                }
                if (timerBRunning) {
                    resetBCalled = true;
                }

                resetFloor();
                drawBoard();
                p.sendMessage("Done!");
            }
            /*if (cmd.equalsIgnoreCase("gmc")) {
                p.setGameMode(GameMode.CREATIVE);
                p.sendMessage(ChatColor.AQUA + "Gamemode set to creative.");
            }
            if (cmd.equalsIgnoreCase("gms")) {
                p.setGameMode(GameMode.SURVIVAL);
                p.sendMessage(ChatColor.AQUA + "Gamemode set to survival.");
            }
            if (cmd.equalsIgnoreCase("gmsp")) {
                p.setGameMode(GameMode.SPECTATOR);
                p.sendMessage(ChatColor.AQUA + "Gamemode set to spectator mode.");
            }
            if (cmd.equalsIgnoreCase("gma")) {
                p.setGameMode(GameMode.ADVENTURE);
                p.sendMessage(ChatColor.AQUA + "Gamemode set to adventure mode.");
            }*/
            if (cmd.equalsIgnoreCase("loadfen")) {
                if (args.length > 0) {
                    String fen = "";
                    for (int i = 0; i < args.length; i++) {
                        fen += args[i] + " ";
                    }
                    p.sendMessage("Loaded: " + fen);
                    loadFEN(fen);
                } else {
                    p.sendMessage("You didn't provide a FEN!");
                }
            }
            if (cmd.equalsIgnoreCase("clock")) {
                if (timerWRunning || timerBRunning) {
                    p.sendMessage(ChatColor.RED + "There is already a clock running! Please run /resetclock or /reset before starting a new timer.");
                } else {
                    if (args.length == 0) {
                        p.sendMessage("No time was provided!");
                    } else {
                        if (args[0].substring(1, 2).equalsIgnoreCase("+")) {
                            minW = Integer.parseInt(args[0].substring(0, 1));
                            minB = Integer.parseInt(args[0].substring(0, 1));
                            inc = Integer.parseInt(args[0].substring(2));
                        } else if (args[0].substring(2, 3).equalsIgnoreCase("+")) {
                            minW = Integer.parseInt(args[0].substring(0, 2));
                            minB = Integer.parseInt(args[0].substring(0, 2));
                            inc = Integer.parseInt(args[0].substring(3));
                        } else {
                            p.sendMessage("Invalid time control.");
                        }

                        secW = 0;
                        secB = 0;

                        updateClock();

                        timerWRunning = true;
                        timerBRunning = true;

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (resetWCalled) {
                                    resetWCalled = false;
                                    timerWRunning = false;
                                    this.cancel();
                                } else {
                                    if (turn.equalsIgnoreCase("W")) {
                                        if (secW == 0) {
                                            if (minW == 0) {
                                                winner = "B";
                                                broadcast(ChatColor.GOLD + "BLACK (" + playerB + ") HAS WON THE GAME BY TIMEOUT!");
                                                fireworks();
                                                timerWRunning = false;
                                                this.cancel();
                                            } else {
                                                minW -= 1;
                                                secW = 59;
                                            }
                                        } else {
                                            secW -= 1;
                                        }
                                        updateClock();
                                    }
                                }
                            }
                        }.runTaskTimer(this, 20, 20);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (resetBCalled) {
                                    resetBCalled = false;
                                    timerBRunning = false;
                                    this.cancel();
                                } else {
                                    if (turn.equalsIgnoreCase("B")) {
                                        if (secB == 0) {
                                            if (minB == 0) {
                                                winner = "W";
                                                broadcast(ChatColor.GOLD + "WHITE (" + playerW + ") HAS WON THE GAME BY TIMEOUT!");
                                                fireworks();
                                                timerBRunning = false;
                                                this.cancel();
                                            } else {
                                                minB -= 1;
                                                secB = 59;
                                            }
                                        } else {
                                            secB -= 1;
                                        }

                                        updateClock();

                                    }
                                }
                            }
                        }.runTaskTimer(this, 20, 20);
                    }
                }
            }
            if (cmd.equalsIgnoreCase("resetClock")) {
                minW = 0;
                minB = 0;
                secW = 0;
                secB = 0;
                if (timerWRunning) {
                    resetWCalled = true;
                }
                if (timerBRunning) {
                    resetBCalled = true;
                }
                updateClock();
            }
            if (cmd.equalsIgnoreCase("resign")) {
                if (playerB.equalsIgnoreCase(p.getName())) {
                    winner = "W";
                    broadcast(ChatColor.GOLD + "WHITE (" + playerW + ") HAS WON THE GAME BY RESIGNATION!");
                    fireworks();
                } else if (playerW.equalsIgnoreCase(p.getName())) {
                    winner = "B";
                    broadcast(ChatColor.GOLD + "BLACK (" + playerB + ") HAS WON THE GAME BY RESIGNATION!");
                    fireworks();
                } else {
                    p.sendMessage("bro you're not even playing");
                }
            }
            if (cmd.equalsIgnoreCase("getfen")) {
                String fen = getFEN();
                TextComponent msg = new TextComponent("FEN: " + fen);
                msg.setColor(net.md_5.bungee.api.ChatColor.GOLD);
                msg.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, fen));
                msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to Copy FEN").color(net.md_5.bungee.api.ChatColor.AQUA).create()));
                p.spigot().sendMessage(msg);
            }
            if (cmd.equalsIgnoreCase("fly")) {
                if (p.getAllowFlight()) {
                    p.setAllowFlight(false);
                    p.sendMessage(ChatColor.AQUA + "Fly turned off!");
                } else {
                    p.setAllowFlight(true);
                    p.sendMessage(ChatColor.AQUA + "Fly turned on!");
                }
            }
            if (cmd.equalsIgnoreCase("white")) {
                if (args.length == 0) {
                    playerW = p.getName();
                } else {
                    playerW = args[0];
                }

                broadcast(ChatColor.YELLOW + "White player set to " + playerW);

            }
            if (cmd.equalsIgnoreCase("black")) {
                if (args.length == 0) {
                    playerB = p.getName();
                } else {
                    playerB = args[0];
                }
                broadcast(ChatColor.YELLOW + "Black player set to " + playerB);
            }
            if (cmd.equalsIgnoreCase("players")) {
                p.sendMessage(ChatColor.YELLOW + "White: " + playerW);
                p.sendMessage(ChatColor.YELLOW + "Black: " + playerB);
            }
            if (cmd.equalsIgnoreCase("speed")) {
                if (args.length == 0) {
                    p.sendMessage(ChatColor.AQUA + "Walk speed: " + Float.toString(p.getWalkSpeed()));
                    p.sendMessage(ChatColor.AQUA + "Fly speed: " + Float.toString(p.getFlySpeed()));
                } else {
                    if (isInt(args[0]) && Integer.parseInt(args[0]) <= 10 && Integer.parseInt(args[0]) >= 0) {
                        if (p.isFlying()) {
                            p.setFlySpeed((float) Integer.parseInt(args[0]) / 10);
                            p.sendMessage(ChatColor.AQUA + "Fly speed set to " + args[0]);
                        } else {
                            p.setWalkSpeed((float) Integer.parseInt(args[0]) / 10);
                            p.sendMessage(ChatColor.AQUA + "Walk speed set to " + args[0]);
                        }
                    } else {
                        p.sendMessage(ChatColor.AQUA + "Please use a speed from 0-10.");
                    }
                }
            }
        }

        return true;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (blockRestore) {
            event.setCancelled(true);
        }

        Player p = event.getPlayer();
        String name = p.getName();
        Block block = event.getBlock();

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        if (y <= -1 && y >= -25 && x <= 96 && x >= 0 && z <= 96 && z >= 0) {
            if (winner.equalsIgnoreCase("")) {
                int[] javaCoord = coordToJava(x, z);

                if (name.equalsIgnoreCase(playerW) || name.equalsIgnoreCase(playerB)) {
                    if ((name.equalsIgnoreCase(playerW) && turn.equalsIgnoreCase("W")) || (name.equalsIgnoreCase(playerB) && turn.equalsIgnoreCase("B"))) {
                        if (selR == -1) {
                            if (board[javaCoord[0]][javaCoord[1]] != null && board[javaCoord[0]][javaCoord[1]].substring(0, 1).equalsIgnoreCase(turn)) {
                                selR = javaCoord[0];
                                selC = javaCoord[1];
                                p.sendMessage(ChatColor.AQUA + "Selected " + board[javaCoord[0]][javaCoord[1]].substring(1) + " on " + coordToChess(x, z) + ".");
                            } else {
                                p.sendMessage("You don't have a piece selected!");
                            }
                        } else {
                            if (selR == javaCoord[0] && selC == javaCoord[1]) { // it's a piece that's already selected
                                selR = -1;
                                selC = -1;
                                p.sendMessage(ChatColor.AQUA + "Unselected " + board[javaCoord[0]][javaCoord[1]].substring(1) + " on " + coordToChess(x, z) + ".");
                            } else {
                                makeMove(new int[]{selR, selC, javaCoord[0], javaCoord[1]}, p);
                                selR = -1;
                                selC = -1;
                            }
                        }

                    } else {
                        p.sendMessage("It's not your turn!");
                    }
                }
            } else {
                p.sendMessage("The game has ended. Enter /reset to start a new game.");
            }
        }

    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (e.getView().getTitle().equalsIgnoreCase("Select a promotion piece:")) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null) {
                String iN = e.getCurrentItem().getItemMeta().getDisplayName();
                if (iN.equalsIgnoreCase("Queen")) {
                    p.sendMessage("Selected Queen.");
                    promoteChoice = "Q";
                } else if (iN.equalsIgnoreCase("Rook")) {
                    p.sendMessage("Selected Rook.");
                    promoteChoice = "R";
                } else if (iN.equalsIgnoreCase("Bishop")) {
                    p.sendMessage("Selected Bishop.");
                    promoteChoice = "B";
                } else if (iN.equalsIgnoreCase("Knight")) {
                    p.sendMessage("Selected Knight.");
                    promoteChoice = "N";
                }

                if (!promoteChoice.equalsIgnoreCase("")) {
                    promoteDone = true;
                    p.closeInventory();

                    resetFloor();
                    int iR = (int) promotionData[0];
                    int iC = (int) promotionData[1];
                    int fR = (int) promotionData[2];
                    int fC = (int) promotionData[3];
                    board[fR][fC] = turn + promoteChoice;
                    setPiece(null, iR, iC);
                    setPiece(turn + promoteChoice, fR, fC);
                    highlightMove(iR, iC, fR, fC);
                    broadcast(ChatColor.GOLD + p.getName() + " played the move " + position(fR, fC) + "=" + promoteChoice + "!");

                    promoteChoice = "";

                    winnerCheck(iR, iC, fR, fC);
                }
            }
        }
    }

    private String coordToChess(int x, int z) {
        int nX = (x / 12) + 1;
        int nZ = (z / 12) + 1;
        String letter = "";
        switch (nZ) {
            case 1:
                letter = "a";
                break;
            case 2:
                letter = "b";
                break;
            case 3:
                letter = "c";
                break;
            case 4:
                letter = "d";
                break;
            case 5:
                letter = "e";
                break;
            case 6:
                letter = "f";
                break;
            case 7:
                letter = "g";
                break;
            case 8:
                letter = "h";
                break;
        }

        return letter + Integer.toString(nX);
    }

    private int[] coordToJava(int x, int z) {
        int nX = x / 12;
        int c = z / 12;

        int r = 7 - nX;

        return new int[]{r, c};
    }

    private void makeMove(int[] theMove, Player p) {
        int iR = theMove[0];
        int iC = theMove[1];
        int fR = theMove[2];
        int fC = theMove[3];

        ArrayList<int[]> thePossibleMoves = new ArrayList<int[]>();
        boolean possible = false;

        if (board[iR][iC] != null) { // if it's a piece
            if (board[iR][iC].substring(0, 1).equalsIgnoreCase(turn)) {
                thePossibleMoves = possibleMoves(board[iR][iC], iR, iC, board, true, checkForCheck(board, oppTurn(turn)), true);
                for (int i = 0; i < thePossibleMoves.size(); i++) {
                    if (fR == thePossibleMoves.get(i)[0] && fC == thePossibleMoves.get(i)[1]) {
                        board[fR][fC] = board[iR][iC];
                        board[iR][iC] = null;
                        possible = true;
                        break;
                    }
                }

                if (possible) {
                    if (board[fR][fC].substring(1).equalsIgnoreCase("P") && ((fR == 0 && board[fR][fC].substring(0, 1).equalsIgnoreCase("W")) || (fR == 7 && board[fR][fC].substring(0, 1).equalsIgnoreCase("B")))) {
                        gui = Bukkit.createInventory(p, 9, "Select a promotion piece:");
                        ItemStack queen = new ItemStack(Material.GOLD_INGOT); ItemMeta queen_meta = queen.getItemMeta(); queen_meta.setDisplayName("Queen"); queen.setItemMeta(queen_meta); gui.addItem(queen);
                        ItemStack rook = new ItemStack(Material.STONE_BRICKS); ItemMeta rook_meta = rook.getItemMeta(); rook_meta.setDisplayName("Rook"); rook.setItemMeta(rook_meta); gui.addItem(rook);
                        ItemStack bishop = new ItemStack(Material.CAMEL_SPAWN_EGG); ItemMeta bishop_meta = bishop.getItemMeta(); bishop_meta.setDisplayName("Bishop"); bishop.setItemMeta(bishop_meta); gui.addItem(bishop);
                        ItemStack knight = new ItemStack(Material.DIAMOND_HORSE_ARMOR); ItemMeta knight_meta = knight.getItemMeta(); knight_meta.setDisplayName("Knight"); knight.setItemMeta(knight_meta); gui.addItem(knight);

                        p.openInventory(gui);

                        promotionData = new Object[] {iR, iC, fR, fC};

                        /*
                        if (!promoteChoice.equalsIgnoreCase("")) {
                            board[fR][fC] = turn + promoteChoice;
                            setPiece(null, iR, iC);
                            setPiece(turn + promoteChoice, fR, fC);
                            broadcast(ChatColor.GOLD + p.getName() + " played the move " + position(fR, fC).substring(0, 1) + "=" + promoteChoice + "!");
                            promoteChoice = "";
                        }
                        */
                    } else {
                        resetFloor();
                        movePiece(iR, iC, fR, fC);
                        checkCastlingEPInfo(board[fR][fC], board, fR, fC, iR, iC, checkForCheck(board, oppTurn(turn)));
                        highlightMove(iR, iC, fR, fC);
                        broadcast(ChatColor.GOLD + p.getName() + " played the move " + moveStr(board[fR][fC], position(fR, fC)) + "!");

                        winnerCheck(iR, iC, fR, fC);
                    }
                } else {
                    p.sendMessage(moveStr(board[iR][iC], position(fR, fC)) + " is not a legal move!");
                }
            } else {
                broadcast("i don't think this should be possible UHH");
            }
        }
    }

    private String[][] cloneArray(String[][] arr) {
        String[][] newArr = new String[8][8];
        for (int i = 0; i < arr.length; i++)
            newArr[i] = arr[i].clone();

        return newArr;
    }

    private int[] intArr(int o, int t) {
        return new int[]{o, t};
    }

    public static boolean isE(String p) {
        return p == null;
    }

    public static boolean isW(String p) {
        return p != null && p.substring(0, 1).equals("W");
    }

    public static boolean isB(String p) {
        return p != null && p.substring(0, 1).equals("B");
    }

    private boolean inBoard(int r, int c) {
        return r >= 0 && r <= 7 && c >= 0 && c <= 7;
    }

    private void swapTurn() {
        if (turn.equalsIgnoreCase("W"))
            turn = "B";
        else
            turn = "W";
    }

    public static String oppTurn(String t) {
        if (t.equals("W"))
            return "B";
        else
            return "W";
    }

    // finds all possible moves for a single piece
    private ArrayList<int[]> possibleMoves(String piece, int sR, int sC, String[][] bd, boolean cfc, boolean iCheck, boolean careKing) {
        ArrayList<int[]> poss = new ArrayList<int[]>();

        String scol = piece.substring(0, 1); // color of selected piece

        int j = 1;

        switch (piece) {
            case "WP":
                if (inBoard(sR - 1, sC) && isE(bd[sR - 1][sC])) {
                    poss.add(intArr(sR - 1, sC));
                }
                if (sR == 6 && isE(bd[sR - 1][sC]) && isE(bd[sR - 2][sC])) { // first row of pawns -> double move
                    poss.add(intArr(sR - 2, sC));
                }
                // checking for diagonal capture
                if (inBoard(sR - 1, sC - 1) && isB(bd[sR - 1][sC - 1])) {
                    poss.add(intArr(sR - 1, sC - 1));
                }
                if (inBoard(sR - 1, sC + 1) && isB(bd[sR - 1][sC + 1])) {
                    poss.add(intArr(sR - 1, sC + 1));
                }
                if (sR == 3 && lastDoublePawn != null && sR == lastDoublePawn[0]) {
                    if (sC - 1 == lastDoublePawn[1]) {
                        String[][] tBoard = cloneArray(bd);
                        tBoard[sR][sC] = null;
                        tBoard[sR][sC - 1] = null;
                        tBoard[sR - 1][sC - 1] = "WP";
                        if (!checkForCheck(tBoard, "B")) {
                            poss.add(intArr(sR - 1, sC - 1));
                        }
                    } else if (sC + 1 == lastDoublePawn[1]) {
                        String[][] tBoard = cloneArray(bd);
                        tBoard[sR][sC] = null;
                        tBoard[sR][sC + 1] = null;
                        tBoard[sR - 1][sC + 1] = "WP";
                        if (!checkForCheck(tBoard, "B")) {
                            poss.add(intArr(sR - 1, sC + 1));
                        }
                    }
                }
                break;
            case "BP":
                if (inBoard(sR + 1, sC) && isE(bd[sR + 1][sC])) {
                    poss.add(intArr(sR + 1, sC));
                }
                if (sR == 1 && isE(bd[sR + 1][sC]) && isE(bd[sR + 2][sC])) { // first row of pawns -> double move
                    poss.add(intArr(sR + 2, sC));
                }
                // checking for diagonal capture
                if (inBoard(sR + 1, sC - 1) && isW(bd[sR + 1][sC - 1])) {
                    poss.add(intArr(sR + 1, sC - 1));
                }
                if (inBoard(sR + 1, sC + 1) && isW(bd[sR + 1][sC + 1])) {
                    poss.add(intArr(sR + 1, sC + 1));
                }
                if (sR == 4 && lastDoublePawn != null && sR == lastDoublePawn[0]) {
                    if (sC - 1 == lastDoublePawn[1]) {
                        String[][] tBoard = cloneArray(bd);
                        tBoard[sR][sC] = null;
                        tBoard[sR][sC - 1] = null;
                        tBoard[sR + 1][sC - 1] = "BP";
                        if (!checkForCheck(tBoard, "W")) {
                            poss.add(intArr(sR + 1, sC - 1));
                        }
                    } else if (sC + 1 == lastDoublePawn[1]) {
                        String[][] tBoard = cloneArray(bd);
                        tBoard[sR][sC] = null;
                        tBoard[sR][sC + 1] = null;
                        tBoard[sR + 1][sC + 1] = "BP";
                        if (!checkForCheck(tBoard, "W")) {
                            poss.add(intArr(sR + 1, sC + 1));
                        }
                    }
                }
                break;
            case "WR":
                for (int i = sR - 1; i >= 0; i--) {
                    if (!isW(bd[i][sC])) {
                        poss.add(intArr(i, sC));
                        if (isB(bd[i][sC])) break;
                    } else {
                        break;
                    }
                }
                for (int i = sR + 1; i < 8; i++) {
                    if (!isW(bd[i][sC])) {
                        poss.add(intArr(i, sC));
                        if (isB(bd[i][sC])) break;
                    } else {
                        break;
                    }
                }
                for (int i = sC - 1; i >= 0; i--) {
                    if (!isW(bd[sR][i])) {
                        poss.add(intArr(sR, i));
                        if (isB(bd[sR][i])) break;
                    } else {
                        break;
                    }
                }
                for (int i = sC + 1; i < 8; i++) {
                    if (!isW(bd[sR][i])) {
                        poss.add(intArr(sR, i));
                        if (isB(bd[sR][i])) break;
                    } else {
                        break;
                    }
                }
                break;
            case "BR":
                for (int i = sR - 1; i >= 0; i--) {
                    if (!isB(bd[i][sC])) {
                        poss.add(intArr(i, sC));
                        if (isW(bd[i][sC])) break;
                    } else {
                        break;
                    }
                }
                for (int i = sR + 1; i < 8; i++) {
                    if (!isB(bd[i][sC])) {
                        poss.add(intArr(i, sC));
                        if (isW(bd[i][sC])) break;
                    } else {
                        break;
                    }
                }
                for (int i = sC - 1; i >= 0; i--) {
                    if (!isB(bd[sR][i])) {
                        poss.add(intArr(sR, i));
                        if (isW(bd[sR][i])) break;
                    } else {
                        break;
                    }
                }
                for (int i = sC + 1; i < 8; i++) {
                    if (!isB(bd[sR][i])) {
                        poss.add(intArr(sR, i));
                        if (isW(bd[sR][i])) break;
                    } else {
                        break;
                    }
                }
                break;
            case "WN":
                poss.add(intArr(sR - 2, sC - 1));
                poss.add(intArr(sR - 2, sC + 1));

                poss.add(intArr(sR + 2, sC - 1));
                poss.add(intArr(sR + 2, sC + 1));

                poss.add(intArr(sR - 1, sC - 2));
                poss.add(intArr(sR + 1, sC - 2));

                poss.add(intArr(sR - 1, sC + 2));
                poss.add(intArr(sR + 1, sC + 2));

                break;
            case "BN":
                poss.add(intArr(sR - 2, sC - 1));
                poss.add(intArr(sR - 2, sC + 1));

                poss.add(intArr(sR + 2, sC - 1));
                poss.add(intArr(sR + 2, sC + 1));

                poss.add(intArr(sR - 1, sC - 2));
                poss.add(intArr(sR + 1, sC - 2));

                poss.add(intArr(sR - 1, sC + 2));
                poss.add(intArr(sR + 1, sC + 2));

                break;
            case "WB":
                j = 1;
                while (sR + j < 8 && sC + j < 8) {
                    if (!isW(bd[sR + j][sC + j])) {
                        poss.add(intArr(sR + j, sC + j));
                        if (isB(bd[sR + j][sC + j])) break;
                    } else {
                        break;
                    }
                    j++;
                }
                j = 1;
                while (sR - j >= 0 && sC - j >= 0) {
                    if (!isW(bd[sR - j][sC - j])) {
                        poss.add(intArr(sR - j, sC - j));
                        if (isB(bd[sR - j][sC - j])) break;
                    } else {
                        break;
                    }
                    j++;
                }
                j = 1;
                while (sR + j < 8 && sC - j >= 0) {
                    if (!isW(bd[sR + j][sC - j])) {
                        poss.add(intArr(sR + j, sC - j));
                        if (isB(bd[sR + j][sC - j])) break;
                    } else {
                        break;
                    }
                    j++;
                }
                j = 1;
                while (sR - j >= 0 && sC + j < 8) {
                    if (!isW(bd[sR - j][sC + j])) {
                        poss.add(intArr(sR - j, sC + j));
                        if (isB(bd[sR - j][sC + j])) break;
                    } else {
                        break;
                    }
                    j++;
                }
                break;
            case "BB":
                j = 1;
                while (sR + j < 8 && sC + j < 8) {
                    if (!isB(bd[sR + j][sC + j])) {
                        poss.add(intArr(sR + j, sC + j));
                        if (isW(bd[sR + j][sC + j])) break;
                    } else {
                        break;
                    }
                    j++;
                }
                j = 1;
                while (sR - j >= 0 && sC - j >= 0) {
                    if (!isB(bd[sR - j][sC - j])) {
                        poss.add(intArr(sR - j, sC - j));
                        if (isW(bd[sR - j][sC - j])) break;
                    } else {
                        break;
                    }
                    j++;
                }
                j = 1;
                while (sR + j < 8 && sC - j >= 0) {
                    if (!isB(bd[sR + j][sC - j])) {
                        poss.add(intArr(sR + j, sC - j));
                        if (isW(bd[sR + j][sC - j])) break;
                    } else {
                        break;
                    }
                    j++;
                }
                j = 1;
                while (sR - j >= 0 && sC + j < 8) {
                    if (!isB(bd[sR - j][sC + j])) {
                        poss.add(intArr(sR - j, sC + j));
                        if (isW(bd[sR - j][sC + j])) break;
                    } else {
                        break;
                    }
                    j++;
                }
                break;
            case "WQ":
                poss = possibleMoves("WR", sR, sC, bd, cfc, iCheck, careKing);
                poss.addAll(possibleMoves("WB", sR, sC, bd, cfc, iCheck, careKing));
                break;
            case "BQ":
                poss = possibleMoves("BR", sR, sC, bd, cfc, iCheck, careKing);
                poss.addAll(possibleMoves("BB", sR, sC, bd, cfc, iCheck, careKing));
                break;
            case "WK":
                poss.add(intArr(sR - 1, sC - 1));
                poss.add(intArr(sR - 1, sC));
                poss.add(intArr(sR - 1, sC + 1));
                poss.add(intArr(sR + 1, sC - 1));
                poss.add(intArr(sR + 1, sC));
                poss.add(intArr(sR + 1, sC + 1));
                poss.add(intArr(sR, sC - 1));
                poss.add(intArr(sR, sC + 1));

                if (careKing && !iCheck) {
                    if (whiteCanKCastle && sC + 3 < 8 && bd[sR][sC + 3] != null && bd[sR][sC + 3].equals("WR") && isE(bd[sR][sC + 1]) && isE(bd[sR][sC + 2])) {
                        String[][] tBoard = cloneArray(bd);
                        tBoard[sR][sC] = null;
                        tBoard[sR][sC + 1] = "WK";
                        if (!checkForCheck(tBoard, "B")) {
                            poss.add(intArr(sR, sC + 2));
                        }
                    }

                    if (whiteCanQCastle && sC - 4 >= 0 && bd[sR][sC - 4] != null && bd[sR][sC - 4].equals("WR") && isE(bd[sR][sC - 1]) && isE(bd[sR][sC - 2]) && isE(bd[sR][sC - 3])) {
                        String[][] tBoard = cloneArray(bd);
                        tBoard[sR][sC] = null;
                        tBoard[sR][sC - 1] = "WK";
                        if (!checkForCheck(tBoard, "B")) {
                            poss.add(intArr(sR, sC - 2));
                        }
                    }
                }

                break;
            case "BK":
                poss.add(intArr(sR - 1, sC - 1));
                poss.add(intArr(sR - 1, sC));
                poss.add(intArr(sR - 1, sC + 1));
                poss.add(intArr(sR + 1, sC - 1));
                poss.add(intArr(sR + 1, sC));
                poss.add(intArr(sR + 1, sC + 1));
                poss.add(intArr(sR, sC - 1));
                poss.add(intArr(sR, sC + 1));

                if (careKing && !iCheck) {
                    if (blackCanKCastle && sC + 3 < 8 && bd[sR][sC + 3] != null && bd[sR][sC + 3].equals("BR") && isE(bd[sR][sC + 1]) && isE(bd[sR][sC + 2])) {
                        String[][] tBoard = cloneArray(bd);
                        tBoard[sR][sC] = null;
                        tBoard[sR][sC + 1] = "BK";
                        if (!checkForCheck(tBoard, "W")) {
                            poss.add(intArr(sR, sC + 2));
                        }
                    }

                    if (blackCanQCastle && sC - 4 >= 0 && bd[sR][sC - 4] != null && bd[sR][sC - 4].equals("BR") && isE(bd[sR][sC - 1]) && isE(bd[sR][sC - 2]) && isE(bd[sR][sC - 3])) {
                        String[][] tBoard = cloneArray(bd);
                        tBoard[sR][sC] = null;
                        tBoard[sR][sC - 1] = "BK";
                        if (!checkForCheck(tBoard, "W")) {
                            poss.add(intArr(sR, sC - 2));
                        }
                    }
                }

                break;
        }


        // scol is color of selected piece

        for (int i = 0; i < poss.size(); i++) {
            // is it not in the board? then remove it from the possible moves
            // if it is on the board, check if it's the same color and remove
            if (!inBoard(poss.get(i)[0], poss.get(i)[1])) {
                poss.remove(i);
                i--;
            } else if (
                    (scol.equals("W") && isW(bd[poss.get(i)[0]][poss.get(i)[1]]))
                            || (scol.equals("B") && isB(bd[poss.get(i)[0]][poss.get(i)[1]]))
            ) {
                poss.remove(i);
                i--;
            }
        }

        if (cfc) {
            // check if you are "walking into check" - creates a temp board of each possible move of the selected piece and checks for check
            // if you are, remove that possible move
            for (int i = 0; i < poss.size(); i++) {
                String[][] tempBoard = cloneArray(bd);

                String temp = tempBoard[sR][sC];
                tempBoard[sR][sC] = null;
                tempBoard[poss.get(i)[0]][poss.get(i)[1]] = temp;

                boolean checkFound = checkForCheck(tempBoard, oppTurn(scol)); // is the opponent checking you if you move here?
                if (checkFound) {
                    poss.remove(i);
                    i--;
                }
            }
        }

        return poss;
    }

    // checks if this "color" is checking their opponent
    private boolean checkForCheck(String[][] bd, String color) {
        int[] king = new int[2];
        String kC = oppTurn(color);
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (bd[r][c] != null && bd[r][c].equals(kC + "K")) {
                    king[0] = r;
                    king[1] = c;
                    break;
                }
            }
        }

        ArrayList<int[]> poss = new ArrayList<int[]>();

        boolean checkFound = false;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (bd[r][c] != null && bd[r][c].substring(0, 1).equals(color)) {
                    poss = possibleMoves(bd[r][c], r, c, bd, false, false, false);

                    for (int i = 0; i < poss.size(); i++) {
                        if (poss.get(i)[0] == king[0] && poss.get(i)[1] == king[1]) {
                            checkFound = true;
                            break;
                        }
                    }
                    if (checkFound) {
                        break;
                    }
                }
            }
            if (checkFound) {
                break;
            }
        }

        return checkFound;
    }

    // checking if the "color" has no moves
    private String checkForMate(String[][] bd, String color) {
        boolean mate = true;
        ArrayList<int[]> poss = new ArrayList<int[]>();

        for (int r = 0; r < 8; r++) {
            if (mate == false) {
                break;
            }

            for (int c = 0; c < 8; c++) {
                if (mate == false) {
                    break;
                }

                if (bd[r][c] != null && bd[r][c].substring(0, 1).equals(color)) {
                    poss = possibleMoves(bd[r][c], r, c, bd, true, true, true);

                    if (poss.size() > 0) {
                        mate = false;
                    }
                }
            }
        }

        if (mate) {
            return oppTurn(color);
        } else {
            return "";
        }
    }

    // check if a king is trying to castle, disable castling when the king/rook moves, check for en passant
    private void checkCastlingEPInfo(String piece, String[][] bd, int r, int c, int selR, int selC, boolean checked) {
        // selR is INITIAL, r is FINAL

        // check for castle
        if (!checked) {
            if (piece.equals("WK")) {
                if (whiteCanQCastle && c == selC - 2) {
                    bd[r][selC - 4] = null;
                    bd[r][selC - 1] = "WR";
                    movePiece(r, selC - 4, r, selC - 1);
                    whiteCanKCastle = false;
                    whiteCanQCastle = false;
                } else if (whiteCanKCastle && c == selC + 2) {
                    bd[r][selC + 3] = null;
                    bd[r][selC + 1] = "WR";
                    movePiece(r, selC + 3, r, selC + 1);
                    whiteCanKCastle = false;
                    whiteCanQCastle = false;
                }
            } else if (piece.equals("BK")) {
                if (blackCanQCastle && c == selC - 2) {
                    bd[r][selC - 4] = null;
                    bd[r][selC - 1] = "BR";
                    movePiece(r, selC - 4, r, selC - 1);
                    blackCanKCastle = false;
                    blackCanQCastle = false;
                } else if (blackCanKCastle && c == selC + 2) {
                    bd[r][selC + 3] = null;
                    bd[r][selC + 1] = "BR";
                    movePiece(r, selC + 3, r, selC + 1);
                    blackCanKCastle = false;
                    blackCanQCastle = false;
                }
            }
        }

        // rook moves = disallow that side's castling
        if (piece.equals("WR")) {
            if (whiteCanQCastle && selC == 0) {
                whiteCanQCastle = false;
            } else if (whiteCanKCastle && selC == 7) {
                whiteCanKCastle = false;
            }
        } else if (piece.equals("BR")) {
            if (blackCanQCastle && selC == 0) {
                blackCanQCastle = false;
            } else if (blackCanKCastle && selC == 7) {
                blackCanKCastle = false;
            }
        }

        // king moves = disallow castling
        if ((whiteCanKCastle || whiteCanQCastle) && piece.equals("WK")) {
            whiteCanKCastle = false;
            whiteCanQCastle = false;
        } else if ((blackCanKCastle || blackCanQCastle) && piece.equals("BK")) {
            blackCanKCastle = false;
            blackCanQCastle = false;
        }

        // en passant
        if (lastDoublePawn != null && c == lastDoublePawn[1] && ((piece.equals("WP") && r == lastDoublePawn[0] - 1) || (piece.equals("BP") && r == lastDoublePawn[0] + 1))) {
            bd[lastDoublePawn[0]][lastDoublePawn[1]] = null;
            //int[] c1 = getMCCoord(lastDoublePawn[0], lastDoublePawn[1]);
            //getServer().dispatchCommand(getServer().getConsoleSender(), "clone 44 -50 88 55 -31 77 " + c1[0] + " " + c1[1] + " " + c1[2]);
            setPiece(null, lastDoublePawn[0], lastDoublePawn[1]);
        }

        // promotions
		/*if (piece.substring(1).equals("P") && (r == 0 || r == 7)) {
			bd[r][c] = piece.substring(0) + "Q";
		}*/
    }

    public static String position(int r, int c) {
        int R = 8 - r;
        int C = c;
        String rC = "";
        switch (C) {
            case 0:
                rC = "a";
                break;
            case 1:
                rC = "b";
                break;
            case 2:
                rC = "c";
                break;
            case 3:
                rC = "d";
                break;
            case 4:
                rC = "e";
                break;
            case 5:
                rC = "f";
                break;
            case 6:
                rC = "g";
                break;
            case 7:
                rC = "h";
                break;
        }

        return rC + R;
    }

    private String moveStr(String piece, String position) {
        if (piece.substring(1).equalsIgnoreCase("p")) {
            return position;
        } else {
            return piece.substring(1) + position;
        }
    }

    private void movePiece(int iR, int iC, int fR, int fC) {
        int[] c1 = getMCCoord(iR, iC);
        int[] c2 = getMCCoord(fR, fC);

        runCommand("clone " + c1[0] + " " + c1[1] + " " + c1[2] + " " + c1[3] + " " + c1[4] + " " + c1[5] + " " + c2[0] + " " + c2[1] + " " + c2[2] + " replace move");
    }

    private void highlightMove(int iR, int iC, int fR, int fC) {
        int[] c1 = getMCCoord(iR, iC);
        int[] c2 = getMCCoord(fR, fC);

        runCommand("clone " + " 0 -47 48 11 -47 59  " + c1[0] + " " + (c1[1] - 1) + " " + c1[2]);
        runCommand("clone " + " 0 -46 48 11 -46 59  " + c2[0] + " " + (c2[1] - 1) + " " + c2[2]);
        //HSI = new int[]{c1[0], (c1[1] - 1), c1[2]};
        //HSF = new int[]{c2[0], (c2[1] - 1), c2[2]};
        //HSCI = getSquareColor(iR, iC);
        //HSCF = getSquareColor(fR, fC);
    }

    private void resetFloor() {
        runCommand("clone 0 -51 0 95 -51 95 0 -25 0");
    }

    private void setPiece(String piece, int r, int c) {
        int[] c1 = getMCCoord(r, c);
        String loc = "";

        if (piece == null) {
            loc = "23 -50 47 12 -26 36";
        } else {
            switch (piece) {
                case "WP": loc = "36 -50 24 47 -26 35"; break;
                case "WR": loc = "36 -50 36 47 -26 47"; break;
                case "WN": loc = "36 -50 48 47 -26 59"; break;
                case "WB": loc = "36 -50 60 47 -26 71"; break;
                case "WK": loc = "24 -50 36 35 -26 47"; break;
                case "WQ": loc = "24 -50 24 35 -26 35"; break;
                case "BP": loc = "60 -50 24 71 -26 35"; break;
                case "BR": loc = "60 -50 36 71 -26 47"; break;
                case "BN": loc = "60 -50 48 71 -26 59"; break;
                case "BB": loc = "60 -50 60 71 -26 71"; break;
                case "BQ": loc = "48 -50 24 59 -26 35"; break;
                case "BK": loc = "48 -50 36 59 -26 47"; break;
            }
        }

        runCommand("clone " + loc + " " + c1[0] + " " + c1[1] + " " + c1[2]);
    }

    private void drawBoard() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                setPiece(board[r][c], r, c);
            }
        }
    }

    private int[] getMCCoord(int r, int c) {
        int newR = (7 - r) * 12;
        int newC = c * 12;

        return new int[]{newR, -24, newC, newR + 11, -1, newC + 11};
    }

    private String getFEN() {
        String FEN = "";
        int count = 0;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                String p = board[r][c];
                if (p == null) {
                    if (count > 0) {
                        count++;
                    } else {
                        count = 1;
                    }
                } else {
                    if (count > 0) {
                        FEN += String.valueOf(count);
                        count = 0;
                    }
                    FEN += pToFEN(p);
                }
            }

            if (count > 0) {
                FEN += String.valueOf(count);
            }

            FEN += "/";
            count = 0;
        }

        FEN = FEN.substring(0, FEN.length() - 1);

        FEN += " " + turn.toLowerCase() + " ";

        String c = "";
        if (whiteCanKCastle) c += "K";
        if (whiteCanQCastle) c += "Q";
        if (blackCanKCastle) c += "k";
        if (blackCanQCastle) c += "q";

        if (c.equals("")) { FEN += "-"; } else { FEN += c; }

        FEN += " - 0 1";

        return FEN;
    }

    private void loadFEN(String FEN) {
        int stage = 0;
        int r = 0;
        int c = 0;

        whiteCanKCastle = false; whiteCanQCastle = false; blackCanKCastle = false; blackCanQCastle = false;

        for (int i = 0; i < FEN.length(); i++) {
            String temp = FEN.substring(i, i+1);

            if (temp.equals(" ")) {
                stage++;
            } else {
                switch (stage) {
                    case 0:
                        if (temp.equals("/")) {
                            r++;
                            c = 0;
                        } else if (isInt(temp)) {

                            for (int j = 0; j < Integer.parseInt(temp); j++) {
                                board[r][c + j] = null;
                            }
                            c += Integer.parseInt(temp);

                        } else {
                            board[r][c] = FENToP(temp);
                            c++;
                        }
                        break;
                    case 1:
                        turn = temp.toUpperCase();
                        break;
                    case 2:
                        if (temp.equals("K")) {
                            whiteCanKCastle = true;
                        } else if (temp.equals("Q")) {
                            whiteCanQCastle = true;
                        } else if (temp.equals("k")) {
                            blackCanKCastle = true;
                        } else if (temp.equals("q")) {
                            blackCanQCastle = true;
                        }
                        break;
                }
            }
        }

        selR = -1;
        selC = -1;
        winner = "";
        lastDoublePawn = null;
        minW = 0;
        minB = 0;
        secW = 0;
        secB = 0;
        if (timerWRunning) {
            resetWCalled = true;
        }
        if (timerBRunning) {
            resetBCalled = true;
        }
        drawBoard();
    }

    private String pToFEN(String p) {
        if (isW(p)) {
            return p.substring(1);
        } else {
            return p.substring(1).toLowerCase();
        }
    }

    private String FENToP(String p) {
        char ch = p.charAt(0);
        if (Character.isUpperCase(ch)) {
            return "W" + p;
        } else {
            return "B" + p.toUpperCase();
        }
    }

    private boolean isInt(String s) {
        try {
            int d = Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }

    private void broadcast(String msg) {
        getServer().broadcastMessage(msg);
    }

    private void fireworks() {
        ArrayList<int[]> possCoords = new ArrayList<int[]>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int[] coo = getMCCoord(r, c);
                possCoords.add(coo);
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                int[] rCo = possCoords.get(randInt(0, possCoords.size() - 1));
                String rFirework = fireworkTypes[randInt(0, fireworkTypes.length - 1)];
                runCommand("summon firework_rocket " + rCo[0] + " " + rCo[1] + " "  + rCo[2] + " " + rFirework);
                fireworkCount++;
                if (fireworkCount >= 75) {
                    this.cancel();
                    fireworkCount = 0;
                }
            }
        }.runTaskTimer(this, 0, 4);
    }

    private void winnerCheck(int iR, int iC, int fR, int fC) {
        winner = checkForMate(board, oppTurn(turn));

        if (winner.equalsIgnoreCase("")) { // no winner
            if (inc > 0) {
                if (turn.equalsIgnoreCase("W")) {
                    if (secW + inc > 59) {
                        minW += (secW + inc) / 60;
                        secW = (secW + inc) % 60;
                    } else {
                        secW += inc;
                    }
                } else {
                    if (secB + inc > 59) {
                        minB += (secB + inc) / 60;
                        secB = (secB + inc) % 60;
                    } else {
                        secB += inc;
                    }
                }
            }
            swapTurn();
            if (turn.equalsIgnoreCase("W")) {
                broadcast(ChatColor.GOLD + "It is now " + playerW + "'s turn.");
            } else {
                broadcast(ChatColor.GOLD + "It is now " + playerB + "'s turn.");
            }

            if ((board[fR][fC].equals("WP") && iR == 6 && fR == 4) || (board[fR][fC].equals("BP") && iR == 1 && fR == 3)) {
                lastDoublePawn = new int[]{fR, fC};
            } else {
                lastDoublePawn = null;
            }
        } else {
            if (checkForCheck(board, turn)) {
                if (winner.equalsIgnoreCase("W")) {
                    broadcast(ChatColor.GOLD + "WHITE (" + playerW + ") HAS WON THE GAME BY CHECKMATE!");
                    fireworks();
                } else {
                    broadcast(ChatColor.GOLD + "BLACK (" + playerB + ") HAS WON THE GAME BY CHECKMATE!");
                    fireworks();
                }
            } else {
                broadcast(ChatColor.GOLD + "DRAW BY STALEMATE!");
                fireworks();
            }
        }
    }

    public int randInt(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    public void updateClock() {
        String minWSplit = Integer.toString(minW);
        String minBSplit = Integer.toString(minB);
        if (minWSplit.length() == 2) {
            buildNumber(Integer.parseInt(minWSplit.substring(0, 1)), 40,-19, 113);
            buildNumber(Integer.parseInt(minWSplit.substring(1)), 33, -19, 113);
        } else {
            buildNumber(0, 40, -19, 113);
            buildNumber(minW, 33, -19, 113);
        }
        if (minBSplit.length() == 2) {
            buildNumber(Integer.parseInt(minBSplit.substring(0, 1)), 75,-19, 113);
            buildNumber(Integer.parseInt(minBSplit.substring(1)), 68, -19, 113);
        } else {
            buildNumber(0, 75, -19, 113);
            buildNumber(minB, 68, -19, 113);
        }

        String secWSplit = Integer.toString(secW);
        String secBSplit = Integer.toString(secB);
        if (secWSplit.length() == 2) {
            buildNumber(Integer.parseInt(secWSplit.substring(0, 1)), 22, -19, 113);
            buildNumber(Integer.parseInt(secWSplit.substring(1)), 15, -19, 113);
        } else {
            buildNumber(0, 22, -19, 113);
            buildNumber(secW, 15, -19, 113);
        }
        if (secBSplit.length() == 2) {
            buildNumber(Integer.parseInt(secBSplit.substring(0, 1)), 57, -19, 113);
            buildNumber(Integer.parseInt(secBSplit.substring(1)), 50, -19, 113);
        } else {
            buildNumber(0, 57, -19, 113);
            buildNumber(secB, 50, -19, 113);
        }
    }

    public void buildNumber(int num, int x, int y, int z) {
        String coords = "";
        switch (num) {
            case 0: coords = "-2 -50 100 2 -38 100"; break;
            case 1: coords = "10 -50 100 14 -38 100"; break;
            case 2: coords = "22 -50 100 26 -38 100"; break;
            case 3: coords = "34 -50 100 38 -38 100"; break;
            case 4: coords = "46 -50 100 50 -38 100"; break;
            case 5: coords = "58 -50 100 62 -38 100"; break;
            case 6: coords = "70 -50 100 74 -38 100"; break;
            case 7: coords = "82 -50 100 86 -38 100"; break;
            case 8: coords = "-2 -50 92 2 -38 93"; break;
            case 9: coords = "94 -50 100 98 -38 100"; break;
        }
        runCommand("clone " + coords + " " + x + " " + y + " " + z);
    }

    public void runCommand(String cmd) {
        getServer().dispatchCommand(getServer().getConsoleSender(), cmd);
    }
}
