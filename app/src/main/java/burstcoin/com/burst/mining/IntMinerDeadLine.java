package burstcoin.com.burst.mining;

import java.math.BigInteger;

/**
 * Created by IceBurst on 8/15/2016.
 */
public interface IntMinerDeadLine {
    void foundDeadLine(long nonce, BigInteger deadline);
}
