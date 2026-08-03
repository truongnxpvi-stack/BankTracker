package com.banktracker.parser

object CategoryClassifier {
    private val CATEGORIES = mapOf(
        "Ăn uống"    to listOf("shopeefood","grabfood","baemin","highlands","phuc long","kfc","lotteria","pizza","com","bun","pho","cafe","tra sua","milk tea"),
        "Mua sắm"    to listOf("shopee","lazada","tiki","sendo","vinmart","coopmart","siêu thị","supermarket"),
        "Di chuyển"  to listOf("grab","be app","xanh sm","parking","xang dau","petrolimex","taxi","xe om"),
        "Giải trí"   to listOf("netflix","spotify","youtube","steam","game","cinema","cgv","bhd","lotte cinema"),
        "Y tế"       to listOf("hospital","benh vien","pharmacy","nha thuoc","vinmec","medical","clinic"),
        "Giáo dục"   to listOf("hoc phi","tuition","truong","school","university","khoa hoc","course"),
        "Tiện ích"   to listOf("evn","dien luc","nuoc","internet","viettel","vnpt","mobifone","vietnamobile"),
        "Chuyển tiền" to listOf("chuyen khoan","transfer","gui tien","ck")
    )

    fun classify(text: String): String {
        val lower = text.lowercase()
        for ((cat, keys) in CATEGORIES) {
            if (keys.any { lower.contains(it) }) return cat
        }
        return "Khác"
    }
}
