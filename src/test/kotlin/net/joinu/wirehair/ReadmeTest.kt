package net.joinu.wirehair

import com.sun.jna.ptr.IntByReference
import org.junit.jupiter.api.Test


object ReadmeTest {
    @Test
    fun `the low-level wrapper works fine`() {
        val initResult = WirehairLib.INSTANCE.wirehair_init_(2)

        assert(initResult == 0)

        val kPacketSize = 1400
        val kMessageBytes = 1000 * 1000 + 333

        val message = ByteArray(kMessageBytes)

        val encoder = WirehairLib.INSTANCE.wirehair_encoder_create(null, message, kMessageBytes, kPacketSize)
            ?: error("Encoder is not created")

        val decoder = WirehairLib.INSTANCE.wirehair_decoder_create(null, kMessageBytes, kPacketSize)

        if (decoder == null) {
            WirehairLib.INSTANCE.wirehair_free(encoder)
            error("Decoder is not created")
        }

        var blockId = 0

        while (true) {
            blockId++

            val block = ByteArray(kPacketSize)
            val writeLen = IntByReference(0)

            val encodeResult = WirehairLib.INSTANCE.wirehair_encode(
                encoder,
                blockId,
                block,
                kPacketSize,
                writeLen
            )

            if (blockId % 10 == 0) continue

            assert(encodeResult == 0) { "Encode failed" }

            val decodeResult = WirehairLib.INSTANCE.wirehair_decode(decoder, blockId, block, writeLen.value)

            if (decodeResult == 0)
                break

            assert(decodeResult == 1) { "Decode failed" }
        }

        val decoded = ByteArray(kMessageBytes)
        val recoverResult = WirehairLib.INSTANCE.wirehair_recover(decoder, decoded, kMessageBytes)

        assert(recoverResult == 0) { "Recover failed" }

        WirehairLib.INSTANCE.wirehair_free(encoder)
        WirehairLib.INSTANCE.wirehair_free(decoder)
    }
}
