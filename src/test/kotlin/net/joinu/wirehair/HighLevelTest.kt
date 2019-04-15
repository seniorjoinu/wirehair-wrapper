package net.joinu.wirehair

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature


object HighLevelTest : Spek({
    Feature("High level wrapper works fine") {
        Scenario("Repeating ReadmeTest but in new terms") {
            Wirehair.init()

            val kPacketSize = 1400
            val kMessageBytes = 1000 * 1000 + 333

            val message = ByteArray(kMessageBytes)

            Wirehair.Encoder(message, kMessageBytes, kPacketSize).use { encoder ->
                Wirehair.Decoder(kMessageBytes, kPacketSize).use { decoder ->
                    var blockId = 0
                    var needed = 0

                    while (true) {
                        blockId++
                        needed++

                        val block = ByteArray(kPacketSize)

                        val writeLen = encoder.encode(blockId, block, kPacketSize)

                        if (blockId % 10 == 0) continue

                        val enough = decoder.decode(blockId, block, writeLen)

                        if (enough) break
                    }

                    val decoded = ByteArray(kMessageBytes)

                    decoder.recover(decoded, kMessageBytes)
                }
            }
        }
    }
})
