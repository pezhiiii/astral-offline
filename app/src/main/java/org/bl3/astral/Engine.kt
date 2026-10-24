package org.bl3.astral
object Engine {
 init { System.loadLibrary("astral") }
 external fun answer(model: String, system: String, question: String, maxTokens: Int): ByteArray
 external fun snapshot(): ByteArray
 external fun release()
 external fun metrics(): ByteArray
 external fun cancel()
}
