package com.masterwok.simplevlcplayer.constants


/**
 * All of the subtitle encodings supported by libVLC. It's important to provide the correct
 * encoding to the [@see VlcOptionsProvider] for the selected subtitle language. If the
 * correct encoding is not chosen, then the subtitles will appear as gibberish.
 */
@Suppress("MemberVisibilityCanBePrivate")
class SubtitleEncoding private constructor() {

    companion object {
        const val DefaultWindows1252 = ""
        const val SystemCodeset =  "system"
        const val UniversalUTF8= "UTF-8"
        const val UniversalUTF16 = "UTF-16"
        const val UniversalBigEndianUTF16 = "UTF-16BE"
        const val UniversalLittleEndianUTF16 = "UTF-16LE"
        const val UniversalChineseGB18030 = "GB18030"
        const val WesternEuropeanLatin9 = "ISO-8859-15"
        const val WesternEuropeanWindows1252 = "Windows-1252"
        const val WesternEuropeanIBM00850 = "IBM850"
        const val EasternEuropeanLatin2 = "ISO-8859-2"
        const val EasternEuropeanWindows1250 = "Windows-1250"
        const val EsperantoLatin3 = "ISO-8859-3"
        const val NordicLatin6 = "ISO-8859-10"
        const val CyrillicWindows1251 = "Windows-1251"
        const val RussianKOI8R = "KOI8-R"
        const val UkrainianKOI8U = "KOI8-U"
        const val ArabicISO88596 = "ISO-8859-6"
        const val ArabicWindows1256 = "Windows-1256"
        const val GreekISO88597 = "ISO-8859-7"
        const val GreekWindows1253 = "Windows-1253"
        const val HebrewISO88598 = "ISO-8859-8"
        const val HebrewWindows1255 = "Windows-1255"
        const val TurkishISO88599 = "ISO-8859-9"
        const val TurkishWindows1254 = "Windows-1254"
        const val ThaiTIS6202533ISO885911 = "ISO-8859-11"
        const val ThaiWindows874 = "Windows-874"
        const val BalticLatin7 = "ISO-8859-13"
        const val BalticWindows1257 = "Windows-1257"
        const val CelticLatin8 = "ISO-8859-14"
        const val SouthEasternEuropeanLatin10 = "ISO-8859-16"
        const val SimplifiedChineseISO2022CNEXT = "ISO-2022-CN-EXT"
        const val SimplifiedChineseUnixEUCCN = "EUC-CN"
        const val Japanese7bitsJISISO2022JP2 = "ISO-2022-JP-2"
        const val JapaneseUnixEUCJP = "EUC-JP"
        const val JapaneseShiftJIS = "Shift_JIS"
        const val KoreanEUCKRCP949 = "CP949"
        const val KoreanISO2022KR = "ISO-2022-KR"
        const val TraditionalChineseBig5 = "Big5"
        const val TraditionalChineseUnixEUCTW = "ISO-2022-TW"
        const val HongKongSupplementaryHKSCS = "Big5-HKSCS"
        const val VietnameseVISCII = "VISCII"
        const val VietnameseWindows1258 = "Windows-1258"

        private val subtitleEncodingMapping: LinkedHashMap<String, String> by lazy {
            linkedMapOf(
                    "Default (Windows-1252)" to DefaultWindows1252
                    , "System codeset" to SystemCodeset
                    , "Universal (UTF-8)" to UniversalUTF8
                    , "Universal (UTF-16)" to UniversalUTF16
                    , "Universal (big endian UTF-16)" to UniversalBigEndianUTF16
                    , "Universal (little endian UTF-16)" to UniversalLittleEndianUTF16
                    , "Universal, Chinese (GB18030)" to UniversalChineseGB18030
                    , "Western European (Latin-9)" to WesternEuropeanLatin9
                    , "Western European (Windows-1252)" to WesternEuropeanWindows1252
                    , "Western European (IBM 00850)" to WesternEuropeanIBM00850
                    , "Eastern European (Latin-2)" to EasternEuropeanLatin2
                    , "Eastern European (Windows-1250)" to EasternEuropeanWindows1250
                    , "Esperanto (Latin-3)" to EsperantoLatin3
                    , "Nordic (Latin-6)" to NordicLatin6
                    , "Cyrillic (Windows-1251)" to CyrillicWindows1251
                    , "Russian (KOI8-R)" to RussianKOI8R
                    , "Ukrainian (KOI8-U)" to UkrainianKOI8U
                    , "Arabic (ISO 8859-6)" to ArabicISO88596
                    , "Arabic (Windows-1256)" to ArabicWindows1256
                    , "Greek (ISO 8859-7)" to GreekISO88597
                    , "Greek (Windows-1253)" to GreekWindows1253
                    , "Hebrew (ISO 8859-8)" to HebrewISO88598
                    , "Hebrew (Windows-1255)" to HebrewWindows1255
                    , "Turkish (ISO 8859-9)" to TurkishISO88599
                    , "Turkish (Windows-1254)" to TurkishWindows1254
                    , "Thai (TIS 620-2533/ISO 8859-11)" to ThaiTIS6202533ISO885911
                    , "Thai (Windows-874)" to ThaiWindows874
                    , "Baltic (Latin-7)" to BalticLatin7
                    , "Baltic (Windows-1257)" to BalticWindows1257
                    , "Celtic (Latin-8)" to CelticLatin8
                    , "South-Eastern European (Latin-10)" to SouthEasternEuropeanLatin10
                    , "Simplified Chinese (ISO-2022-CN-EXT)" to SimplifiedChineseISO2022CNEXT
                    , "Simplified Chinese Unix (EUC-CN)" to SimplifiedChineseUnixEUCCN
                    , "Japanese (7-bits JIS/ISO-2022-JP-2)" to Japanese7bitsJISISO2022JP2
                    , "Japanese Unix (EUC-JP)" to JapaneseUnixEUCJP
                    , "Japanese (Shift JIS)" to JapaneseShiftJIS
                    , "Korean (EUC-KR/CP949)" to KoreanEUCKRCP949
                    , "Korean (ISO-2022-KR)" to KoreanISO2022KR
                    , "Traditional Chinese (Big5)" to TraditionalChineseBig5
                    , "Traditional Chinese Unix (EUC-TW)" to TraditionalChineseUnixEUCTW
                    , "Hong-Kong Supplementary (HKSCS)" to HongKongSupplementaryHKSCS
                    , "Vietnamese (VISCII)" to VietnameseVISCII
                    , "Vietnamese (Windows-1258)" to VietnameseWindows1258
            )
        }

        /**
         * Get a [List] of display names of supported encodings.
         */
        @JvmStatic
        val supportedSubtitleEncodings: List<String>
            get() = subtitleEncodingMapping
                    .keys
                    .toList()

        /**
         * Get the encoding value by its display name.
         */
        @JvmStatic
        fun getEncodingByName(name: String): String? = subtitleEncodingMapping[name]

        /**
         * Get the display name of the encoding by the encoding value.
         */
        @JvmStatic
        fun getNameByEncoding(encoding: String): String? = subtitleEncodingMapping
                .entries
                .firstOrNull { it.value == encoding }
                ?.key

    }
}


