package com.blueskybone.arkscreen.ui.common

import com.blueskybone.arkscreen.R
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

/** Reads actual default resources so JVM tests exercise placeholders without an Android Context. */
object TestUiStrings : UiStrings {
    private val values by lazy {
        val root = generateSequence(File(requireNotNull(System.getProperty("user.dir")))) { it.parentFile }
            .flatMap { sequenceOf(File(it, "src/main/res/values"), File(it, "app/src/main/res/values")) }
            .first { it.isDirectory }
        val result = mutableMapOf<Int, String>()
        root.listFiles().orEmpty().filter { it.extension == "xml" }.forEach { file ->
            val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getElementsByTagName("string")
            for (i in 0 until nodes.length) {
                val node = nodes.item(i)
                val name = node.attributes.getNamedItem("name").nodeValue
                val id = R.string::class.java.getField(name).getInt(null)
                result[id] = node.textContent.replace("\\n", "\n").replace("\\'", "'")
            }
        }
        result
    }
    override fun get(id: Int, vararg args: Any): String = String.format(Locale.CHINA, values.getValue(id), *args)
}
