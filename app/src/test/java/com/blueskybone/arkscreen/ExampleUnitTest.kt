package com.blueskybone.arkscreen

import com.blueskybone.arkscreen.util.TimeUtils.getRemainTimeMinStr
import com.blueskybone.arkscreen.util.TimeUtils.getRemainTimeStr
import com.blueskybone.arkscreen.util.TimeUtils.getTimeStr
import org.junit.Test

import org.junit.Assert.*
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test(){

        println(getRemainTimeStr(-1000))
        println("getRemainTimeStr(-10)")
        println(getRemainTimeStr(200))
        println(getRemainTimeMinStr(-200))
        println(getTimeStr(1747183889000, "MM-dd"))
    }

    @Test
    fun testGzipToRawBytesMatchesErrorPattern() {
        // 1. 准备已知的正确字符串
        val originalString = "{\"code\":0,\"message\":\"OK\",\"timestamp\":\"1745943174\",\"data\":{\"ts\":\"1745943174\",\"awards\":[{\"resource\":{\"id\":\"4003\",\"type\":\"DIAMOND_SHD\",\"name\":\"合成玉\",\"rarity\":4,\"stageDropList\":[],\"otherSource\":[\"GIFT_PACKAGE\",\"SHOP\"],\"buildingProductList\":[],\"sortId\":10002,\"classifyType\":\"NORMAL\"},\"count\":120,\"type\":\"daily\"}],\"resourceInfoMap\":{\"4003\":{\"id\":\"4003\",\"type\":\"DIAMOND_SHD\",\"name\":\"合成玉\",\"rarity\":4,\"stageDropList\":[],\"otherSource\":[\"GIFT_PACKAGE\",\"SHOP\"],\"buildingProductList\":[],\"sortId\":10002,\"classifyType\":\"NORMAL\"}}}}\n"

        // 2. 压缩为 GZIP 字节数组
        val gzipBytes = originalString.toGzipByteArray()

        // 3. 直接以字符串形式读取 GZIP 字节（模拟错误读取方式）
        val corruptedString = String(gzipBytes, Charsets.UTF_8)

        // 4. 打印结果供对比
        println("原始字符串: $originalString")
        println("GZIP 字节（Hex）: ${bytesToHex(gzipBytes)}")
        println("错误读取的乱码: $corruptedString")

        // 5. 验证乱码是否以 GZIP 头开始（1F 8B 08）
        val gzipHeader = byteArrayOf(0x1F.toByte(), 0x8B.toByte(), 0x08.toByte())
        assert(gzipBytes.copyOf(3).contentEquals(gzipHeader)) {
            "GZIP 头不符合预期"
        }
    }

    // 扩展函数：字符串 → GZIP 字节数组
    private fun String.toGzipByteArray(): ByteArray {
        ByteArrayOutputStream().use { bos ->
            GZIPOutputStream(bos).bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(this)
            }
            return bos.toByteArray()
        }
    }

    // 工具函数：字节数组转 Hex 字符串
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString(" ") { "%02X".format(it) }
    }


}