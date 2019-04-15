package net.joinu.wirehair

import com.sun.jna.ptr.IntByReference
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature


object ReadmeTest: Spek({
    Feature("Wrapper works fine") {
        Scenario("Repeating the example test from wirehair repo") {
            val initResult = Wirehair.WRAPPER.wirehair_init_()

            assert(initResult == Wirehair.WirehairResult.Success)

            val kPacketSize = 1400
            val kMessageBytes = 1000 * 1000 + 333

            val message = ByteArray(kMessageBytes)

            val encoder = Wirehair.WRAPPER.wirehair_encoder_create(null, message, kMessageBytes, kPacketSize)
                ?: error("Encoder is not created")

            val decoder = Wirehair.WRAPPER.wirehair_decoder_create(null, kMessageBytes, kPacketSize)

            if (decoder == null) {
                Wirehair.WRAPPER.wirehair_free(encoder)
                error("Decoder is not created")
            }

            var blockId = 0
            var needed = 0

            while (true) {
                blockId++

                if (blockId % 10 == 0) continue

                ++needed

                val block = ByteArray(kPacketSize)
                val writeLen = IntByReference(0)

                val encodeResult = Wirehair.WRAPPER.wirehair_encode(
                    encoder,
                    blockId,
                    block,
                    kPacketSize,
                    writeLen
                )

                assert(encodeResult == Wirehair.WirehairResult.Success) { "Encode failed" }

                val decodeResult = Wirehair.WRAPPER.wirehair_decode(decoder, blockId, block, writeLen.value)

                if (decodeResult == Wirehair.WirehairResult.Success)
                    break

                assert(decodeResult == Wirehair.WirehairResult.NeedMore) { "Decode failed" }
            }

            val decoded = ByteArray(kMessageBytes)
            val recoverResult = Wirehair.WRAPPER.wirehair_recover(decoder, decoded, kMessageBytes)

            assert(recoverResult == Wirehair.WirehairResult.Success) { "Recover failed" }

            Wirehair.WRAPPER.wirehair_free(encoder)
            Wirehair.WRAPPER.wirehair_free(decoder)
        }
    }
})
