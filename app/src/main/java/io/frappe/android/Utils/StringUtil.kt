package io.frappe.android.Utils

import java.text.Normalizer

object StringUtil {

    fun slugify(word: String?, replacement: String = "_") = Normalizer
            .normalize(word, Normalizer.Form.NFD)
            .replace("[^\\p{ASCII}]".toRegex(), "")
            .replace("[^a-zA-Z0-9\\s]+".toRegex(), "").trim()
            .replace("\\s+".toRegex(), replacement)
            .toLowerCase()

}