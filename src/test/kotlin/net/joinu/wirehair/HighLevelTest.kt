@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")
package net.joinu.wirehair

import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import sun.nio.ch.DirectBuffer
import java.nio.ByteBuffer
import java.util.*


object HighLevelTest {
    init {
        System.setProperty("jna.debug_load", "true")
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
    }

    @RepeatedTest(100)
    fun `the high-level wrapper works fine`() {
        // initializing wirehair
        Wirehair.init()

        // setting sizes
        val kPacketSize = 1400
        val kMessageBytes = 1000 * 1000 + 333

        // creating the message
        val message = ByteBuffer.allocateDirect(kMessageBytes)
        val messageBytes = ByteArray(kMessageBytes)
        Random().nextBytes(messageBytes)

        message.put(messageBytes)
        message.rewind()

        val decodedMessage = ByteBuffer.allocateDirect(kMessageBytes)

        // creating encoder and decoder and releasing them after all stuff is done
        Wirehair.Encoder(message as DirectBuffer, kMessageBytes, kPacketSize).use { encoder ->
            Wirehair.Decoder(kMessageBytes, kPacketSize).use { decoder ->
                var blockId = 0

                while (true) {
                    // generating unique blockId
                    blockId++

                    // generating repair block
                    val block = ByteBuffer.allocateDirect(kPacketSize)
                    val writeLen = encoder.encode(blockId, block as DirectBuffer, kPacketSize)

                    // imitating 10 percent packet drop
                    if (blockId % 10 == 0) continue

                    // accumulating repair blocks
                    val enough = decoder.decode(blockId, block, writeLen)

                    if (enough) break
                }

                // decoding message from accumulated repair blocks
                decoder.recover(decodedMessage as DirectBuffer, kMessageBytes)
            }
        }

        val messageArray = ByteArray(kMessageBytes)
        message.get(messageArray)

        val decodedMessageArray = ByteArray(kMessageBytes)
        decodedMessage.get(decodedMessageArray)

        assert(messageArray.contentEquals(decodedMessageArray))
    }

    @Test
    fun `encoder and decoder throw after close`() {
        Wirehair.init()

        val kPacketSize = 1400
        val kMessageBytes = 1000 * 1000 + 333

        val message = ByteBuffer.allocateDirect(kMessageBytes) as DirectBuffer

        val encoder = Wirehair.Encoder(message, kMessageBytes, kPacketSize)
        val decoder = Wirehair.Decoder(kMessageBytes, kPacketSize)

        val blockDataOut = ByteBuffer.allocateDirect(kPacketSize)

        encoder.close()
        assertThrows { encoder.encode(1, blockDataOut as DirectBuffer, kPacketSize) }
        assertThrows { encoder.close() }

        decoder.close()
        assertThrows {
            decoder.decode(1, blockDataOut as DirectBuffer, kPacketSize)
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
