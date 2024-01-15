package com.azarasi.pgsharphelper.utils

import android.util.Xml
import android.util.Xml.Encoding
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.io.StringWriter

object SPParser {
    private const val TAG_MAP = "map"
    private const val TAG_STRING = "string"
    private const val TAG_LONG = "long"
    private const val TAG_INT = "int"
    private const val TAG_BOOLEAN = "boolean"
    private const val TAG_FLOAT = "float"
    private const val TAG_SET = "set"
    private const val ATTRIBUTE_NAME = "name"
    private const val ATTRIBUTE_VALUE = "value"
    private const val VALUE_NULL = "null"

    fun parseXmlText(xmlText: String): Map<String, Any?> {
        val xmlPullParser: XmlPullParser = Xml.newPullParser()
        xmlPullParser.setInput(StringReader(xmlText))

        val items: MutableMap<String, Any?> = mutableMapOf()
        var key = ""
        var stringSet: MutableSet<String>? = null

        while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
            when (xmlPullParser.eventType) {
                XmlPullParser.START_TAG -> {
                    val tagName = xmlPullParser.name ?: continue

                    if (tagName == TAG_SET) {
                        stringSet = mutableSetOf()
                    } else if (stringSet != null && tagName == TAG_STRING) {
                        stringSet.add(xmlPullParser.nextText())
                    }

                    val value = xmlPullParser.getAttributeValue(null, ATTRIBUTE_VALUE)
                    key = xmlPullParser.getAttributeValue(null, ATTRIBUTE_NAME) ?: continue

                    val itemValue = when (tagName) {
                        TAG_BOOLEAN -> value.toBooleanStrictOrNull()
                        TAG_FLOAT -> value.toFloatOrNull()
                        TAG_INT -> value.toIntOrNull()
                        TAG_LONG -> value.toLongOrNull()
                        TAG_STRING -> xmlPullParser.nextText() ?: VALUE_NULL
                        else -> null
                    }
                    if (itemValue != null) {
                        items[key] = itemValue
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (xmlPullParser.name == TAG_SET) {
                        if (stringSet != null) {
                            items[key] = stringSet
                        }
                    }
                    stringSet = null
                }
            }
        }
        return items
    }

    fun createXmlText(items: Map<String, Any?>): String {
        val stringWriter = StringWriter()
        val xmlSerializer = Xml.newSerializer()
        xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
        xmlSerializer.setOutput(stringWriter)
        xmlSerializer.startDocument(Encoding.UTF_8.name, true)
        xmlSerializer.startTag(null, TAG_MAP)
        items.forEach { (key, value) ->
            when (value) {
                is Boolean -> {
                    xmlSerializer.startTag(null, TAG_BOOLEAN)
                    xmlSerializer.attribute(null, ATTRIBUTE_NAME, key)
                    xmlSerializer.attribute(null, ATTRIBUTE_VALUE, value.toString())
                    xmlSerializer.endTag(null, TAG_BOOLEAN)
                }

                is Float -> {
                    xmlSerializer.startTag(null, TAG_FLOAT)
                    xmlSerializer.attribute(null, ATTRIBUTE_NAME, key)
                    xmlSerializer.attribute(null, ATTRIBUTE_VALUE, value.toString())
                    xmlSerializer.endTag(null, TAG_FLOAT)
                }

                is Int -> {
                    xmlSerializer.startTag(null, TAG_INT)
                    xmlSerializer.attribute(null, ATTRIBUTE_NAME, key)
                    xmlSerializer.attribute(null, ATTRIBUTE_VALUE, value.toString())
                    xmlSerializer.endTag(null, TAG_INT)
                }

                is Long -> {
                    xmlSerializer.startTag(null, TAG_LONG)
                    xmlSerializer.attribute(null, ATTRIBUTE_NAME, key)
                    xmlSerializer.attribute(null, ATTRIBUTE_VALUE, value.toString())
                    xmlSerializer.endTag(null, TAG_LONG)
                }

                is String?, String -> {
                    xmlSerializer.startTag(null, TAG_STRING)
                    xmlSerializer.attribute(null, ATTRIBUTE_NAME, key)
                    xmlSerializer.text(value?.toString() ?: VALUE_NULL)
                    xmlSerializer.endTag(null, TAG_STRING)
                }

                is Set<*> -> {
                    xmlSerializer.startTag(null, TAG_SET)
                    xmlSerializer.attribute(null, ATTRIBUTE_NAME, key)
                    value.forEach {
                        it?.let { safeString ->
                            xmlSerializer.startTag(null, TAG_STRING)
                            xmlSerializer.text(safeString.toString())
                            xmlSerializer.endTag(null, TAG_STRING)
                        }
                    }
                    xmlSerializer.endTag(null, TAG_SET)
                }
            }
        }
        xmlSerializer.endTag(null, TAG_MAP)
        xmlSerializer.endDocument()
        return stringWriter.toString()
    }
}