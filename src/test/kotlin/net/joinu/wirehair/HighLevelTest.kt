package net.joinu.wirehair

import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import sun.nio.ch.DirectBuffer
import java.nio.ByteBuffer


object HighLevelTest {
    @RepeatedTest(100)
    fun `the high-level wrapper works fine`() {
        // initializing wirehair
        Wirehair.init()

        // setting sizes
        val kPacketSize = 1400
        val kMessageBytes = 1000 * 1000 + 333

        // creating the message
        val message = ByteBuffer.allocateDirect(kMessageBytes) as DirectBuffer

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

    @Test
    fun `encoder and decoder throw after close`() {
        Wirehair.init()

        val kPacketSize = 1400
        val kMessageBytes = 1000 * 1000 + 333

        val message = ByteBuffer.allocateDirect(kMessageBytes) as DirectBuffer

        val encoder = Wirehair.Encoder(message, kMessageBytes, kPacketSize)
        val decoder = Wirehair.Decoder(kMessageBytes, kPacketSize)

        encoder.close()
        assertThrows { encoder.encode(1, ByteArray(kPacketSize), kPacketSize) }
        assertThrows { encoder.close() }

        decoder.close()
        assertThrows {
            decoder.decode(1, ByteArray(1400), kPacketSize)
            assertThrows { decoder.close() }
        }
    }

    fun assertThrows(block: () -> Unit) {
        try {
            block()
            assert(false)
        } catch (e: Throwable) {
        }
    }
}
