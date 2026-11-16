package com.example.util

/**
 * Générateur QR Code standard ISO/IEC 18004 (Version 4-L, 33x33 modules, jusqu'à 78 octets UTF-8)
 * sans aucune dépendance externe, afin d'intégrer un code QR vérifiable sur les reçus de paiement.
 */
object QrCodeGenerator {

  private const val VERSION = 4
  private const val SIZE = 33 // 17 + 4 * 4
  private const val DATA_CODEWORDS = 80 // Version 4-L: 80 data + 20 ECC = 100 total
  private const val ECC_CODEWORDS = 20

  // Format information strings for Level L (01) and masks 0..7 (precomputed BCH + XOR 0x5412)
  private val FORMAT_INFO_L = intArrayOf(
    0x77C4, 0x72F3, 0x7DAA, 0x789D, 0x662F, 0x6318, 0x6C41, 0x6976,
  )

  fun encode(text: String): Array<BooleanArray> {
    val bytes = text.toByteArray(Charsets.UTF_8).let {
      if (it.size > 78) it.copyOf(78) else it
    }

    // Build bit stream: mode=0100 (byte), count (8 bits), data, terminator, padding
    val bits = ArrayList<Int>(DATA_CODEWORDS * 8)
    fun appendBits(value: Int, count: Int) {
      for (i in (count - 1) downTo 0) {
        bits.add((value ushr i) and 1)
      }
    }

    appendBits(0b0100, 4) // Byte mode
    appendBits(bytes.size, 8)
    for (b in bytes) {
      appendBits(b.toInt() and 0xFF, 8)
    }
    val maxDataBits = DATA_CODEWORDS * 8
    val terminatorLen = minOf(4, maxDataBits - bits.size)
    appendBits(0, terminatorLen)
    while (bits.size % 8 != 0) bits.add(0)
    val padBytes = intArrayOf(0xEC, 0x11)
    var padIdx = 0
    while (bits.size < maxDataBits) {
      appendBits(padBytes[padIdx % 2], 8)
      padIdx++
    }

    val dataCw = IntArray(DATA_CODEWORDS) { i ->
      var v = 0
      for (b in 0 until 8) {
        v = (v shl 1) or bits[i * 8 + b]
      }
      v
    }

    val eccCw = computeReedSolomon(dataCw, ECC_CODEWORDS)
    val allCodewords = IntArray(DATA_CODEWORDS + ECC_CODEWORDS)
    dataCw.copyInto(allCodewords, 0)
    eccCw.copyInto(allCodewords, DATA_CODEWORDS)

    // Convert codewords + 7 remainder bits (Version 4) into a single bit sequence
    val finalBits = ArrayList<Int>(allCodewords.size * 8 + 7)
    for (cw in allCodewords) {
      for (i in 7 downTo 0) {
        finalBits.add((cw ushr i) and 1)
      }
    }
    repeat(7) { finalBits.add(0) }

    val baseModules = Array(SIZE) { BooleanArray(SIZE) }
    val isFunction = Array(SIZE) { BooleanArray(SIZE) }

    drawFunctionPatterns(baseModules, isFunction)
    placeDataBits(baseModules, isFunction, finalBits)

    // Try masks 0..7 and pick lowest penalty score
    var bestMask = 0
    var bestPenalty = Int.MAX_VALUE
    var bestGrid = baseModules
    for (mask in 0 until 8) {
      val candidate = Array(SIZE) { r -> baseModules[r].clone() }
      applyMask(candidate, isFunction, mask)
      drawFormatBits(candidate, mask)
      val score = penaltyScore(candidate)
      if (score < bestPenalty) {
        bestPenalty = score
        bestMask = mask
        bestGrid = candidate
      }
    }
    drawFormatBits(bestGrid, bestMask)
    return bestGrid
  }

  private fun drawFunctionPatterns(modules: Array<BooleanArray>, isFunc: Array<BooleanArray>) {
    fun setFunc(r: Int, c: Int, dark: Boolean) {
      if (r in 0 until SIZE && c in 0 until SIZE) {
        modules[r][c] = dark
        isFunc[r][c] = true
      }
    }

    fun drawFinder(top: Int, left: Int) {
      for (dr in -1..7) {
        for (dc in -1..7) {
          val r = top + dr
          val c = left + dc
          if (r !in 0 until SIZE || c !in 0 until SIZE) continue
          val inOuter = dr in 0..6 && dc in 0..6
          val dark = inOuter && (
            dr == 0 || dr == 6 || dc == 0 || dc == 6 ||
              (dr in 2..4 && dc in 2..4)
            )
          setFunc(r, c, dark)
        }
      }
    }

    drawFinder(0, 0)
    drawFinder(0, SIZE - 7)
    drawFinder(SIZE - 7, 0)

    // Alignment pattern for Version 4 at (26, 26)
    val alignCenter = 26
    for (dr in -2..2) {
      for (dc in -2..2) {
        val dark = maxOf(kotlin.math.abs(dr), kotlin.math.abs(dc)) != 1
        setFunc(alignCenter + dr, alignCenter + dc, dark)
      }
    }

    // Timing patterns
    for (i in 8 until SIZE - 8) {
      setFunc(6, i, i % 2 == 0)
      setFunc(i, 6, i % 2 == 0)
    }

    // Reserve format information areas (will be written after masking)
    for (i in 0..8) {
      if (i != 6) {
        isFunc[8][i] = true
        isFunc[i][8] = true
      }
    }
    for (i in 0..7) {
      isFunc[8][SIZE - 1 - i] = true
      isFunc[SIZE - 1 - i][8] = true
    }
    // Always-dark module at (4*version + 9, 8) = (25, 8)
    setFunc(SIZE - 8, 8, true)
  }

  private fun placeDataBits(
    modules: Array<BooleanArray>,
    isFunc: Array<BooleanArray>,
    bits: List<Int>,
  ) {
    var bitIdx = 0
    var right = SIZE - 1
    var upward = true
    while (right >= 1) {
      if (right == 6) right--
      for (step in 0 until SIZE) {
        val row = if (upward) SIZE - 1 - step else step
        for (dx in 0..1) {
          val col = right - dx
          if (!isFunc[row][col]) {
            modules[row][col] = if (bitIdx < bits.size) bits[bitIdx++] == 1 else false
          }
        }
      }
      upward = !upward
      right -= 2
    }
  }

  private fun applyMask(modules: Array<BooleanArray>, isFunc: Array<BooleanArray>, mask: Int) {
    for (r in 0 until SIZE) {
      for (c in 0 until SIZE) {
        if (isFunc[r][c]) continue
        val invert = when (mask) {
          0 -> (r + c) % 2 == 0
          1 -> r % 2 == 0
          2 -> c % 3 == 0
          3 -> (r + c) % 3 == 0
          4 -> (r / 2 + c / 3) % 2 == 0
          5 -> ((r * c) % 2) + ((r * c) % 3) == 0
          6 -> (((r * c) % 2) + ((r * c) % 3)) % 2 == 0
          else -> (((r + c) % 2) + ((r * c) % 3)) % 2 == 0
        }
        if (invert) modules[r][c] = !modules[r][c]
      }
    }
  }

  private fun drawFormatBits(modules: Array<BooleanArray>, mask: Int) {
    val data = FORMAT_INFO_L[mask]
    for (i in 0 until 15) {
      val bit = ((data ushr i) and 1) == 1
      // Vertical strip
      when {
        i < 6 -> modules[i][8] = bit
        i < 8 -> modules[i + 1][8] = bit
        else -> modules[SIZE - 15 + i][8] = bit
      }
      // Horizontal strip
      when {
        i < 8 -> modules[8][SIZE - 1 - i] = bit
        i < 9 -> modules[8][15 - i] = bit
        else -> modules[8][15 - i - 1] = bit
      }
    }
  }

  private fun penaltyScore(m: Array<BooleanArray>): Int {
    var score = 0
    // Run lengths horizontal and vertical
    for (r in 0 until SIZE) {
      var run = 1
      for (c in 1 until SIZE) {
        if (m[r][c] == m[r][c - 1]) {
          run++
          if (run == 5) score += 3 else if (run > 5) score += 1
        } else {
          run = 1
        }
      }
    }
    for (c in 0 until SIZE) {
      var run = 1
      for (r in 1 until SIZE) {
        if (m[r][c] == m[r - 1][c]) {
          run++
          if (run == 5) score += 3 else if (run > 5) score += 1
        } else {
          run = 1
        }
      }
    }
    // 2x2 blocks
    for (r in 0 until SIZE - 1) {
      for (c in 0 until SIZE - 1) {
        val v = m[r][c]
        if (m[r][c + 1] == v && m[r + 1][c] == v && m[r + 1][c + 1] == v) {
          score += 3
        }
      }
    }
    return score
  }

  private fun computeReedSolomon(data: IntArray, eccCount: Int): IntArray {
    val gen = IntArray(eccCount)
    gen[eccCount - 1] = 1
    var root = 1
    for (i in 0 until eccCount) {
      for (j in 0 until eccCount) {
        gen[j] = gfMul(gen[j], root)
        if (j + 1 < eccCount) {
          gen[j] = gen[j] xor gen[j + 1]
        }
      }
      root = gfMul(root, 0x02)
    }

    val rem = IntArray(eccCount)
    for (b in data) {
      val factor = b xor rem[0]
      for (i in 0 until eccCount - 1) rem[i] = rem[i + 1]
      rem[eccCount - 1] = 0
      for (i in 0 until eccCount) {
        rem[i] = rem[i] xor gfMul(gen[i], factor)
      }
    }
    return rem
  }

  private fun gfMul(x: Int, y: Int): Int {
    var a = x
    var b = y
    var z = 0
    for (i in 0 until 8) {
      if ((b and 1) != 0) z = z xor a
      val hi = a and 0x80
      a = (a shl 1) and 0xFF
      if (hi != 0) a = a xor 0x1D
      b = b ushr 1
    }
    return z
  }
}
