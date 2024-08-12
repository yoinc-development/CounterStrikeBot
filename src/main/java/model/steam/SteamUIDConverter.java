package model.steam;

import java.math.BigInteger;

// Algorithm taken from https://forums.alliedmods.net/showthread.php?t=60899
public class SteamUIDConverter {

    public static long getSteamId64(String steamid) {
        if (steamid == null || steamid.isEmpty()) {
            return 0;
        }

        int iServer = 0;
        int iAuthID = 0;

        String[] tokens = steamid.split(":");
        if (tokens.length > 1) {
            try {
                iServer = Integer.parseInt(tokens[1]);
                iAuthID = Integer.parseInt(tokens[2]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                return 0;
            }
        }

        if (iAuthID == 0) {
            return 0;
        }

        long i64friendID = (long) iAuthID * 2;
        i64friendID += 76561197960265728L + iServer;
        return i64friendID;
    }

    public static String getSteamId(long steamid64) {
        BigInteger tempsteamid64 = BigInteger.valueOf(steamid64);
        String iServer = "1";
        BigInteger two = new BigInteger("2");
        BigInteger baseValue = new BigInteger("76561197960265728");

        if (tempsteamid64.mod(two).equals(BigInteger.ZERO)) {
            iServer = "0";
        }

        tempsteamid64 = tempsteamid64.subtract(new BigInteger(iServer));

        if (tempsteamid64.compareTo(baseValue) > 0) {
            tempsteamid64 = tempsteamid64.subtract(baseValue);
        }
        tempsteamid64 = tempsteamid64.divide(two);
        return "STEAM_1:" + iServer + ":" + tempsteamid64;
    }
}
