package net.joinu.wirehair

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference

typealias WirehairCodec = Pointer

interface Wirehair : Library {
    companion object {
        val LIBRARY_NAME = "wirehair"
        var WRAPPER: Wirehair

        init {
            System.setProperty("jna.debug_load.jna", "true")
            val libFile = Native.extractFromResourcePath(LIBRARY_NAME)
            WRAPPER = Native.load(libFile.absolutePath, Wirehair::class.java)
        }
    }

    fun wirehair_init_(expected_version: Int = 2): Int
    fun wirehair_encoder_create(reuseOpt: WirehairCodec?, message: ByteArray, messageBytes: Int, blockBytes: Int): WirehairCodec?
    fun wirehair_encode(codec: WirehairCodec, blockId: Int, blockDataOut: ByteArray, outBytes: Int, dataBytesOut: IntByReference): Int
    fun wirehair_decoder_create(reuseOpt: WirehairCodec?, messageBytes: Int, blockBytes: Int): WirehairCodec?
    fun wirehair_decode(codec: WirehairCodec, blockId: Int, blockData: ByteArray, dataBytes: Int): Int
    fun wirehair_recover(codec: WirehairCodec, messageOut: ByteArray, messageBytes: Int): Int
    fun wirehair_recover_block(codec: WirehairCodec, blockId: Int, blockData: ByteArray, bytesOut: IntByReference): Int
    fun wirehair_decoder_becomes_encoder(codec: WirehairCodec): Int
    fun wirehair_free(codec: WirehairCodec)

    object WirehairResult {
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
