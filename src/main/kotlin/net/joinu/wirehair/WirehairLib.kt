package net.joinu.wirehair

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import sun.nio.ch.DirectBuffer


interface WirehairLib : Library {
    companion object {
        val LIBRARY_NAME = "wirehair"
        var INSTANCE: WirehairLib

        init {
            val libFile = Native.extractFromResourcePath(LIBRARY_NAME)
            INSTANCE = Native.load(libFile.absolutePath, WirehairLib::class.java)
        }
    }

    fun wirehair_init_(expected_version: Int): Int
    fun wirehair_encoder_create(reuseOpt: Pointer?, message: DirectBuffer, messageBytes: Int, blockBytes: Int): Pointer?
    fun wirehair_encode(
        codec: Pointer,
        blockId: Int,
        blockDataOut: ByteArray,
        outBytes: Int,
        dataBytesOut: IntByReference
    ): Int

    fun wirehair_decoder_create(reuseOpt: Pointer?, messageBytes: Int, blockBytes: Int): Pointer?
    fun wirehair_decode(codec: Pointer, blockId: Int, blockData: ByteArray, dataBytes: Int): Int
    fun wirehair_recover(codec: Pointer, messageOut: ByteArray, messageBytes: Int): Int
    fun wirehair_recover_block(codec: Pointer, blockId: Int, blockData: ByteArray, bytesOut: IntByReference): Int
    fun wirehair_decoder_becomes_encoder(codec: Pointer): Int
    fun wirehair_free(codec: Pointer)
}
