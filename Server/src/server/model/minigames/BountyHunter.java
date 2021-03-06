package server.model.minigames;
 
import server.model.players.Client;
import server.model.players.Player;
import server.model.players.PlayerHandler;
import server.event.CycleEvent;
import server.event.CycleEventContainer;
import server.event.CycleEventHandler;
import server.util.Misc;
 
public class BountyHunter {
 
    private static Client c;
 
    public BountyHunter(Client c) {
        this.c = c;
    }
 
    public static int[] restricted = {
        995, 996
    };
 
    public static int attempts;
 
    private static int BROWN_SKULL = 2;
    private static int SILVER_SKULL = 3;
    private static int GREEN_SKULL = 4;
    private static int BLUE_SKULL = 5;
    private static int RED_SKULL = 6;
 
    public static int getPlayerSkull(Client c) {
        int carrying = c.getItems().getEquipmentNet(c) + c.getItems().getInventoryNet(c);
        return (carrying < 100000 ? BROWN_SKULL : carrying < 500000 && carrying > 100000 ? SILVER_SKULL : carrying < 1000000 && carrying > 500000 ? BLUE_SKULL : carrying < 5000000 && carrying > 1000000 ? GREEN_SKULL : carrying < 10000000 && carrying > 5000000 ? BLUE_SKULL : carrying > 10000000 || carrying < -1 ? RED_SKULL : -1);
    }
 
 
 
    public static boolean inBH(Client c) {
        if (safeArea(c)) return false;
        if (c.absX >= 3145 && c.absX <= 3197 && c.absY >= 3650 && c.absY <= 3667) return false;
        if (c.absX >= 3080 && c.absX <= 3193 && c.absY >= 3650 && c.absY <= 3770) {
            return true;
        }
        return false;
    }
    public static boolean safeArea(Client c) {
        if (c.absX >= 3140 && c.absX <= 3166 && c.absY >= 3653 && c.absY <= 3667 || c.absX >= 3150 && c.absX <= 3194 && c.absY >= 3668 && c.absY <= 3681 || c.absX >= 3158 && c.absX <= 3196 && c.absY >= 3682 && c.absY <= 3693 || c.absX == 3197 && c.absY == 3692 || c.absX >= 3177 && c.absX <= 3195 && c.absY >= 3694 && c.absY <= 3702 || c.absX >= 3191 && c.absX <= 3195 && c.absY >= 3703 && c.absY <= 3710) {
            return true;
        }
        return false;
    }
 
    public static int getActivePlayers(Client c) {
        for (int j = 0; j < PlayerHandler.players.length; j++) {
            if (PlayerHandler.players[j] != null) {
                Client c2 = (Client) PlayerHandler.players[j];
                if (inBH(c2) && c2.heightLevel == c.heightLevel && c.heightLevel == c2.heightLevel) {
                    int randomTarget = Misc.random(c2.playerId);
                    c.playerTarget = randomTarget;
                }
                if (c.playerTarget == c.playerId) {
                    c.playerTarget = 0;
                }
            }
        }
        return c.playerTarget;
    }
 
    public static void addTarget(Client c) {
        try {
            if (!inBH(c)) return;
            getActivePlayers(c);
            if (c.playerTarget == 0 && attempts <= 5) {
                attempts++;
                addTarget(c);
                return;
            }
            if (c.playerTarget == 0 && attempts >= 5) {
                handleNewTarget(c);
                c.targetName = "None";
                return;
            }
            Client target = (Client) PlayerHandler.players[c.playerTarget];
            if (!inBH(target)) {
                addTarget(c);
                return;
            }
            if (target.heightLevel != c.heightLevel) {
                addTarget(c);
                resetFlags(c);
                return;
            }
            if (target.playerId == c.killerId) {
                resetFlags(c);
                handleNewTarget(c);
                return;
            }
            c.targetName = Misc.formatPlayerName(target.playerName);
            c.sendMessage("You have been assigned to a target. Your current target is " + c.targetName + ".");
            c.getPA().createPlayerHints(10, c.playerTarget);
        } catch (Exception e) {
            addTarget(c);
        }
    }
 
    public static void enterBH(Client c, int object) {
        final int[][] coords = {
            {
                3138, 3669
            }, {
                3124, 3665
            }, {
                3108, 3670
            }, {
                3101, 3682
            }, {
                3096, 3692
            }, {
                3091, 3706
            }, {
                3086, 3717
            }, {
                3091, 3735
            }, {
                3110, 3747
            }, {
                3135, 3758
            }, {
                3147, 3758
            }, {
                3163, 3753
            }, {
                3170, 3746
            }, {
                3171, 3737
            }, {
                3181, 3720
            }, {
                3180, 3708
            }, {
                3171, 3701
            }, {
                3163, 3696
            }, {
                3146, 3681
            }
        };
        for (int i = 0; i < restricted.length; i++) {
            for (int equip = 0; equip < 14; equip++) {
                if (c.getItems().playerHasItem(restricted[i]) || c.playerEquipment[equip] == restricted[i]) {
                    c.getDH().sendStatement("You can not bring " + c.getItems().getItemName(restricted[i]) + " in Bounty Hunter.");
                    c.nextChat = 0;
                    return;
                }
            }
        }
        if (c.enterTime > 0) {
            c.getDH().sendStatement("You must wait " + getTime2(c) + " seconds before you can enter.");
            c.nextChat = 0;
            return;
        }
        switch (object) {
            case 28119:
                //low
                if (c.combatLevel >= 67) {
                    c.getDH().sendStatement("You cannot enter this crater! This crater is a low level crater.");
                    return;
                }
                break;
            case 28120:
                //mid
                if (c.combatLevel <= 60 || c.combatLevel >= 100) {
                    c.getDH().sendStatement("You cannot enter this crater! This crater is a medium level crater.");
                    return;
                }
                break;
            case 28121:
                //high
                if (c.combatLevel < 100) {
                    c.getDH().sendStatement("You cannot enter this crater! This crater is a high level crater.");
                    return;
                }
                break;
        }
        int coord = Misc.random(coords.length);
        int x = coord;
        try {
            c.teleportToX = coords[x][0];
            c.teleportToY = coords[x][1];
            c.heightLevel = playerHeight(c);
            c.startAnimation(7376);
            handleNewTarget(c);
        } catch (Exception e) {
            //it was causing a dc sometimes..
        }
    }
 
    public static void ResetBountyHunter(Client c) {
        logout(c);
        resetFlags(c);
        decreaseEnterTime(c, 50);
        c.getPA().walkableInterface(-1);
        c.getPA().sendFrame126("", 25351);
        c.getCombat().resetPlayerAttack();
 
    }
 
 
    public static int playerHeight(Client c) {
        int h = 0;
        if (c.combatLevel <= 67) h = 4;
        if (c.combatLevel >= 60 && c.combatLevel <= 100) h = 8;
        if (c.combatLevel >= 100) h = 12;
        return h;
    }
 
 
    public static void leaveBH(Client c) {
        if (c.cantLeavePenalty > 0) {
            c.getDH().sendStatement("You need to wait " + getTime(c) + " seconds before you can leave!");
            return;
        }
        c.startAnimation(7376);
        logout(c);
        resetFlags(c);
        c.getCombat().resetPlayerAttack();
        decreaseEnterTime(c, 50);
        c.getPA().movePlayer(3163, 3685, 0);
        c.getPA().walkableInterface(-1);
    }
 
    public static int getTime2(Client c) {
        int alfa = c.enterTime;
        double bravo = (alfa * 0.6);
        int time = (int) bravo;
        return time;
    }
 
    public static int getTime(Client c) {
        int alfa = (c.pickupPenalty > 0 ? c.pickupPenalty : c.cantLeavePenalty > 0 ? c.cantLeavePenalty : 0);
        double bravo = (alfa * 0.6);
        int time = (int) bravo;
        return time;
    }
 
    public static void handlePickupPenalty(final Client c) {
        c.pickupPenalty = 300;
        CycleEventHandler.getSingleton().addEvent(c, new CycleEvent() {@Override
            public void execute(CycleEventContainer container) {
                getPickupPenalty(c);
                if (c.pickupPenalty <= 0) {
                    container.stop();
                }
            }@Override
            public void stop() {}
        }, 1);
    }
 
    public static void handleNewTarget(final Client plr) {
        CycleEventHandler.getSingleton().addEvent(plr, new CycleEvent() {
            int time = 5;@Override
            public void execute(CycleEventContainer container) {
                try {
                    time--;
                    if (time == 0) {
                        container.stop();
                        if (inBH(plr) && plr.playerTarget == 0) addTarget(plr);
                    }
                } catch (Exception e) {
                    container.stop();
                }
            }@Override
            public void stop() {}
        }, 1);
    }
 
    public static void handleCantLeave(final Client c) {
        c.pickupPenalty = 0;
        c.cantLeavePenalty = 300;
        CycleEventHandler.getSingleton().addEvent(c, new CycleEvent() {@Override
            public void execute(CycleEventContainer container) {
                getPickupPenalty(c);
                if (c.cantLeavePenalty <= 0) {
                    container.stop();
                }
            }@Override
            public void stop() {}
        }, 1);
    }
 
    public static void enterTimer(Client c) {
        c.enterTime--;
    }
 
    public static void decreaseEnterTime(final Client c, int time) {
        c.enterTime = time;
        CycleEventHandler.getSingleton().addEvent(c, new CycleEvent() {@Override
            public void execute(CycleEventContainer container) {
                enterTimer(c);
                if (c.enterTime <= 0) {
                    container.stop();
                }
            }@Override
            public void stop() {}
        }, 1);
    }
 
    public static void getPickupPenalty(Client c) {
        if (c.prayerActive[10]) {
            c.getCombat().activatePrayer(10);
        }
        //if (c.curseActive[0]) {
        //c.getCurse().activateCurse(0);
        //}
        if (c.pickupPenalty >= 0) {
            c.pickupPenalty--;
        }
        if (c.cantLeavePenalty >= 0) {
            c.cantLeavePenalty--;
        }
    }
 
    public static void resetFlags(Client c) {
        c.playerTarget = 0;
        c.targetName = "None";
        c.getPA().createPlayerHints(-1, -1);
        for (Player p: PlayerHandler.players) {
            if (p != null && ((Client) p).playerId == c.playerTarget) {
                resetFlags(((Client) p));
            }
        }
    }
 
    public static void targetTeleport(Client c) {
        Client target = (Client) PlayerHandler.players[c.playerTarget];
        c.getPA().movePlayer(target.absX + Misc.random(2), target.absY + Misc.random(2), target.heightLevel);
    }
 
    public static void logout(Client c) {
        for (int j = 0; j < PlayerHandler.players.length; j++) {
            if (PlayerHandler.players[j] != null) {
                Client c2 = (Client) PlayerHandler.players[j];
                if (c.playerId == c2.playerTarget) {
                    c2.sendMessage("Your target has left the crater. You shall be found a new target.");
                    resetFlags(c2);
                    handleNewTarget(c2);
                }
            }
        }
    }
 
 
 
 
}