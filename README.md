## Wirehair Kotlin Wrapper

Kotlin wrapper for amazing `https://github.com/catid/wirehair`

### Example
```kotlin
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
```

### Install
Use [Jitpack](https://jitpack.io/)

### Help
Feel free to open an issue or suggest a PR

### TODO
* Bundle `jar` with binaries for major platforms
* Add other functions to the high level wrapper
