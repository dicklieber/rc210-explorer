package net.wa9nnn.rc210.util

/**
 * Convert ASCII control chars in a string to Unicode Control Pictures
 *
 * @see https://www.unicode.org/charts/PDF/U2400.pdf
 * @param in perhaps with ascii control chars like \r or \n
 * @return string like: ␍Hello␊lfBeforeThis
 */
def expandControlChars(in: String): String =
  in.map { ch =>
    if (ch.isControl) {
      val codepoint: Long = ch.toLong + 0x2400L // start of Range: 2400–243F
      codepoint.toChar
    } else {
      ch
    }
  }

