package encryption;

import com.github.windpapi4j.WinDPAPI;

import java.util.Base64;

public final class Encryption {
    private static final String charsetName = "UTF-8";

    private static WinDPAPI createWinDPAPIInstance() throws Exception {
        if (!WinDPAPI.isPlatformSupported()) {
            throw new Exception("Encryption API [WinDPAPI] not supported.");
        }

        return WinDPAPI.newInstance(WinDPAPI.CryptProtectFlag.CRYPTPROTECT_LOCAL_MACHINE);
    }

    public static String encrypt(String s) throws Exception {
        try {
            WinDPAPI winDPAPI = createWinDPAPIInstance();

            byte[] cipherTextBytes = winDPAPI.protectData(s.getBytes(charsetName));
            return Base64.getEncoder().encodeToString(cipherTextBytes);
        } catch (Exception ex) {
            throw new Exception("ERROR: encryption failed.", ex);
        }
    }

    public static String decrypt(String s) throws Exception {
        try {
            WinDPAPI winDPAPI = createWinDPAPIInstance();

            byte[] decoded = Base64.getDecoder().decode(s.getBytes());
            byte[] decryptedBytes = winDPAPI.unprotectData(decoded);
            return new String(decryptedBytes, charsetName);
        } catch (Exception ex) {
            throw new Exception("ERROR: encryption failed.", ex);
        }
    }
}
