package burstcoin.com.burst.plotting;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;
import fr.cryptohash.Shabal256;


/**
 * Copied from pocminer_v1
 *
 */
public class SinglePlot {
    public static int HASH_SIZE = 32;
    public static int HASHES_PER_SCOOP = 2;
    public static int SCOOP_SIZE = HASHES_PER_SCOOP * HASH_SIZE;    // 64
    public static int SCOOPS_PER_PLOT = 4096;                       // original 1MB/plot = 16384
    public static int PLOT_SIZE = SCOOPS_PER_PLOT * SCOOP_SIZE;     // 4096*64 = 262144 ; 256Kb
    public static int HASH_CAP = 4096;

    public byte[] data = new byte[PLOT_SIZE];
    public long nonce;

    // Plotting Design based on URL: https://web.burst-team.us/index.php/about/
    public SinglePlot(long addr, long mNonce) {
        nonce = mNonce;
        ByteBuffer base_buffer = ByteBuffer.allocate(16);
        base_buffer.putLong(addr);
        base_buffer.putLong(nonce);
        byte[] base = base_buffer.array();
        Shabal256 md = new Shabal256();
        byte[] gendata = new byte[PLOT_SIZE + base.length];
        System.arraycopy(base, 0, gendata, PLOT_SIZE, base.length);
        for(int i = PLOT_SIZE; i > 0; i -= HASH_SIZE) {
            md.reset();
            int len = PLOT_SIZE + base.length - i;
            if(len > HASH_CAP) {
                len = HASH_CAP;
            }
            md.update(gendata, i, len);
            md.digest(gendata, i - HASH_SIZE, HASH_SIZE);
        }
        md.reset();
        md.update(gendata);
        byte[] mFinalHash = md.digest();

        for(int i = 0; i < PLOT_SIZE; i++) {
            data[i] = (byte) (gendata[i] ^ mFinalHash[i % HASH_SIZE]);
        }
    }
}
