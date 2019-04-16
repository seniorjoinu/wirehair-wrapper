package net.joinu.wirehair

import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import sun.nio.ch.DirectBuffer
import java.io.Closeable


object Wirehair {
    /**
     * Initializes wirehair
     *
     * @param expectedVersion [Int]
     *
     * @throws [WirehairException]
     */
    fun init(expectedVersion: Int = 2) {
        val result = WirehairLib.INSTANCE.wirehair_init_(expectedVersion)

        isWirehairResultSuccess(result)
    }

    /**
     * Wirehair encoder
     * This structure is used to encode some message into infinite sequence of repair blocks
     * Rule: one encoder = one encoded message
     *
     * @param message [DirectBuffer] - message to encode
     * @param messageBytes [Int] - bytes in the message
     * @param blockBytes [Int] - bytes in an output block
     * @param reuseOpt [Encoder] - <optional> pointer to prior codec object
     *
     * @throws [IllegalStateException]
     */
    class Encoder(message: DirectBuffer, messageBytes: Int, blockBytes: Int, reuseOpt: Encoder? = null) : Closeable {
        val pointer: Pointer

        init {
            pointer = WirehairLib.INSTANCE.wirehair_encoder_create(reuseOpt?.pointer, message, messageBytes, blockBytes)
                ?: error("Unable to create encoder. Params: [reuseOpt: $reuseOpt, message: $message, messageBytes: $messageBytes, blockBytes: $blockBytes]")
        }

        /**
         * Encodes block of repair data
         * Use it to generate infinite number of repair blocks
         *
         * @param blockId [Int] - identifier of repair block to generate
         * @param blockDataOut [ByteArray] - output repair block
         * @param outBytes [Int] - bytes in the output repair block
         *
         * @return [Int] - number of bytes written <= blockBytes
         *
         * @throws [WirehairException]
         * @throws [IllegalStateException]
         */
        fun encode(blockId: Int, blockDataOut: ByteArray, outBytes: Int): Int {
            val dataBytesOut = IntByReference(0)
            val result = WirehairLib.INSTANCE.wirehair_encode(pointer, blockId, blockDataOut, outBytes, dataBytesOut)

            if (!isWirehairResultSuccess(result))
                error("Unable to encode. Params: [encoder: $this, blockId: $blockId, blockDataOut: $blockDataOut, outBytes: $outBytes]")

            return dataBytesOut.value
        }

        /**
         * Releases encoder
         * Use it when you don't need it anymore
         */
        override fun close() {
            WirehairLib.INSTANCE.wirehair_free(pointer)
        }
    }

    /**
     * Wirehair decoder
     * This structure is used to decode some message from N (sometimes more) repair blocks
     * Rule: one decoder = one decoded message
     *
     * @param messageBytes [Int] - bytes in the message
     * @param blockBytes [Int] - bytes in an output block
     * @param reuseOpt [Encoder] - <optional> pointer to prior codec object
     *
     * @throws [IllegalStateException]
     */
    class Decoder(messageBytes: Int, blockBytes: Int, reuseOpt: Decoder? = null) : Closeable {
        val pointer: Pointer

        init {
            pointer = WirehairLib.INSTANCE.wirehair_decoder_create(reuseOpt?.pointer, messageBytes, blockBytes)
                ?: error("Unable to create decoder. Params: [reuseOpt: $reuseOpt, messageBytes: $messageBytes, blockBytes: $blockBytes]")
        }

        /**
         * Decodes block of repair data
         * Use it to accumulate as much repair blocks as needed
         *
         * @param blockId [Int] - identifier of repair block to accumulate
         * @param blockData [ByteArray] - repair block data
         * @param dataBytes [Int] - bytes in the repair block data
         *
         * @return [Boolean] - you need more repair blocks for message recovery if this returns false, otherwise you did great!
         *
         * @throws [WirehairException]
         */
        fun decode(blockId: Int, blockData: ByteArray, dataBytes: Int): Boolean {
            val result = WirehairLib.INSTANCE.wirehair_decode(pointer, blockId, blockData, dataBytes)

            return isWirehairResultSuccess(result)
        }

        /**
         * Recovers message from accumulated repair blocks
         *
         * @param messageOut [ByteArray] - reconstructed from repair blocks message
         * @param messageBytes [Int] - bytes in reconstructed message
         *
         * @throws [WirehairException]
         * @throws [IllegalStateException]
         */
        fun recover(messageOut: ByteArray, messageBytes: Int) {
            val result = WirehairLib.INSTANCE.wirehair_recover(pointer, messageOut, messageBytes)

            if (!isWirehairResultSuccess(result))
                error("Unable to recover. Params: [decoder: $this, messageOut: $messageOut, messageBytes: $messageBytes]")
        }

        /**
         * Releases decoder
         * Use it when you don't need in anymore
         */
        override fun close() {
            WirehairLib.INSTANCE.wirehair_free(pointer)
        }
    }

    private fun isWirehairResultSuccess(result: Int): Boolean {
        return when (result) {
            Result.Success -> true
            Result.NeedMore -> false
            Result.InvalidInput -> throw WirehairException("Invalid input")
            Result.BadDenseSeed -> throw WirehairException("Bad dense seed")
            Result.BadPeelSeed -> throw WirehairException("Bad peel seed")
            Result.BadInputSmallN -> throw WirehairException("Bad input: small N")
            Result.BadInputLargeN -> throw WirehairException("Bad input: large N")
            Result.ExtraInsufficient -> throw WirehairException("Extra insufficient")
            Result.OOM -> throw WirehairException("Out of memory")
            Result.UnsupportedPlatform -> throw WirehairException("Unsupported platform")
            else -> throw WirehairException("Unexpected error")
        }
    }

    private object Result {
        const val Success = 0
        const val NeedMore = 1
        const val InvalidInput = 2
        const val BadDenseSeed = 3
        const val BadPeelSeed = 4
        const val BadInputSmallN = 5
        const val BadInputLargeN = 6
        const val ExtraInsufficient = 7
        const val Error = 8
        const val OOM = 9
        const val UnsupportedPlatform = 10
    }
}

class WirehairException(message: String) : RuntimeException(message)
