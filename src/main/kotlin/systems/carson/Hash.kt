package systems.carson

import java.security.MessageDigest

enum class HashType(val str :String){
    SHA256("SHA-256"),
    SHA384("SHA-384"),
    SHA512("SHA-512")
}

fun ByteArray.hash(type :HashType = HashType.SHA256) :Hash = Hash(MessageDigest.getInstance(type.str).digest(this))

fun String.hash(type :HashType = HashType.SHA256) :Hash = this.toByteArray().hash(type)

class Hash(val bytes :ByteArray){
    val string :String
        get() = bytes.bytesToHex()
}

private val hexArray = "0123456789ABCDEF".toCharArray()

private fun ByteArray.bytesToHex(): String {
    val hexChars = CharArray(size * 2)
    for (j in indices) {
        val v = this[j].toInt() and 0xFF
        hexChars[j * 2] = hexArray[v.ushr(4)]
        hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
}
