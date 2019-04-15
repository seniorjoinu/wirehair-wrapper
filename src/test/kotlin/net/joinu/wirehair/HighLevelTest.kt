package net.joinu.wirehair

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature


object HighLevelTest : Spek({
    Feature("High level wrapper works fine") {
        Scenario("Repeating ReadmeTest but in new syntax") {
            // initializing wirehair
            Wirehair.init()

            // setting sizes
            val kPacketSize = 1400
            val kMessageBytes = 1000 * 1000 + 333

            // creating the message
            val message = ByteArray(kMessageBytes)

            // creating encoder and decoder and releasing them after all stuff is done
            Wirehair.Encoder(message, kMessageBytes, kPacketSize).use { encoder ->
                Wirehair.Decoder(kMessageBytes, kPacketSize).use { decoder ->
                    var blockId = 0

                    while (true) {
                        // generating unique blockId
                        blockId++

                        // generating repair block
                        val block = ByteArray(kPacketSize)
                        val writeLen = encoder.encode(blockId, block, kPacketSize)

                        // imitating 10 percent packet drop
                        if (blockId % 10 == 0) continue

                        // accumulating repair blocks
                        val enough = decoder.decode(blockId, block, writeLen)

                        if (enough) break
                    }

                    // decoding message from accumulated repair blocks
                    val decoded = ByteArray(kMessageBytes)
                    decoder.recover(decoded, kMessageBytes)
                }
            }
        }
    }
})
