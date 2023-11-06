package eventstream.app

import eventstream.utilities.stringutils.StringUtils

import org.apache.commons.text.WordUtils

fun main() {

    /*

    Blank App in case you need to fork and Extend this Library

    */

    val tokens = StringUtils.split(MessageUtils.getMessage())
    val result = StringUtils.join(tokens)
    println(WordUtils.capitalize(result))
}
