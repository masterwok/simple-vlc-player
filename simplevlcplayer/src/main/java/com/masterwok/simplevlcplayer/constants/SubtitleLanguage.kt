package com.masterwok.simplevlcplayer.constants

import com.masterwok.opensubtitlesandroid.SubtitleLanguage


/**
 * All of the subtitle languages supported by libVLC. When specifying the subtitle language,
 * it's also important to provide the [@see SubtitleEncoding] to the [@see VlcOptionsProvider].
 */
class SubtitleLanguage {

    companion object {

        const val Albanian = SubtitleLanguage.Albanian
        const val Arabic = SubtitleLanguage.Arabic
        const val Armenian = SubtitleLanguage.Armenian
        const val Basque = SubtitleLanguage.Basque
        const val Bengali = SubtitleLanguage.Bengali
        const val Bosnian = SubtitleLanguage.Bosnian
        const val Breton = SubtitleLanguage.Breton
        const val Bulgarian = SubtitleLanguage.Bulgarian
        const val Burmese = SubtitleLanguage.Burmese
        const val Catalan = SubtitleLanguage.Catalan
        const val Chinese = SubtitleLanguage.ChineseSimplified
        const val Croatian = SubtitleLanguage.Croatian
        const val Czech = SubtitleLanguage.Czech
        const val Danish = SubtitleLanguage.Danish
        const val Dutch = SubtitleLanguage.Dutch
        const val English = SubtitleLanguage.English
        const val Esperanto = SubtitleLanguage.Esperanto
        const val Estonian = SubtitleLanguage.Estonian
        const val Finnish = SubtitleLanguage.Finnish
        const val French = SubtitleLanguage.French
        const val Galician = SubtitleLanguage.Galician
        const val Georgian = SubtitleLanguage.Georgian
        const val German = SubtitleLanguage.German
        const val Greek = SubtitleLanguage.Greek
        const val Hebrew = SubtitleLanguage.Hebrew
        const val Hindi = SubtitleLanguage.Hindi
        const val Hungarian = SubtitleLanguage.Hungarian
        const val Icelandic = SubtitleLanguage.Icelandic
        const val Indonesian = SubtitleLanguage.Indonesian
        const val Italian = SubtitleLanguage.Italian
        const val Japanese = SubtitleLanguage.Japanese
        const val Kazakh = SubtitleLanguage.Kazakh
        const val Khmer = SubtitleLanguage.Khmer
        const val Korean = SubtitleLanguage.Korean
        const val Latvian = SubtitleLanguage.Latvian
        const val Lithuanian = SubtitleLanguage.Lithuanian
        const val Luxembourgish = SubtitleLanguage.Luxembourgish
        const val Macedonian = SubtitleLanguage.Macedonian
        const val Malay = SubtitleLanguage.Malay
        const val Malayalam = SubtitleLanguage.Malayalam
        const val Mongolian = SubtitleLanguage.Mongolian
        const val Norwegian = SubtitleLanguage.Norwegian
        const val Occitan = SubtitleLanguage.Occitan
        const val Persian = SubtitleLanguage.Persian
        const val Polish = SubtitleLanguage.Polish
        const val Portuguese = SubtitleLanguage.Portuguese
        const val Brazilian = SubtitleLanguage.PortugueseBr
        const val Romanian = SubtitleLanguage.Romanian
        const val Russian = SubtitleLanguage.Russian
        const val Serbian = SubtitleLanguage.Serbian
        const val Sinhalese = SubtitleLanguage.Sinhalese
        const val Slovak = SubtitleLanguage.Slovak
        const val Slovenian = SubtitleLanguage.Slovenian
        const val Spanish = SubtitleLanguage.Spanish
        const val Swahili = SubtitleLanguage.Swahili
        const val Swedish = SubtitleLanguage.Swedish
        const val Syriac = SubtitleLanguage.Syriac
        const val Tagalog = SubtitleLanguage.Tagalog
        const val Tamil = SubtitleLanguage.Tamil
        const val Telugu = SubtitleLanguage.Telugu
        const val Thai = SubtitleLanguage.Thai
        const val Turkish = SubtitleLanguage.Turkish
        const val Ukrainian = SubtitleLanguage.Ukrainian
        const val Urdu = SubtitleLanguage.Urdu
        const val Vietnamese = SubtitleLanguage.Vietnamese

        private val languageMapping: LinkedHashMap<String, String> by lazy {
            linkedMapOf(
                    "Albanian" to SubtitleLanguage.Albanian
                    , "Arabic" to SubtitleLanguage.Arabic
                    , "Armenian" to SubtitleLanguage.Armenian
                    , "Basque" to SubtitleLanguage.Basque
                    , "Bengali" to SubtitleLanguage.Bengali
                    , "Bosnian" to SubtitleLanguage.Bosnian
                    , "Breton" to SubtitleLanguage.Breton
                    , "Bulgarian" to SubtitleLanguage.Bulgarian
                    , "Burmese" to SubtitleLanguage.Burmese
                    , "Catalan" to SubtitleLanguage.Catalan
                    , "Chinese" to SubtitleLanguage.ChineseSimplified
                    , "Croatian" to SubtitleLanguage.Croatian
                    , "Czech" to SubtitleLanguage.Czech
                    , "Danish" to SubtitleLanguage.Danish
                    , "Dutch" to SubtitleLanguage.Dutch
                    , "English" to SubtitleLanguage.English
                    , "Esperanto" to SubtitleLanguage.Esperanto
                    , "Estonian" to SubtitleLanguage.Estonian
                    , "Finnish" to SubtitleLanguage.Finnish
                    , "French" to SubtitleLanguage.French
                    , "Galician" to SubtitleLanguage.Galician
                    , "Georgian" to SubtitleLanguage.Georgian
                    , "German" to SubtitleLanguage.German
                    , "Greek" to SubtitleLanguage.Greek
                    , "Hebrew" to SubtitleLanguage.Hebrew
                    , "Hindi" to SubtitleLanguage.Hindi
                    , "Hungarian" to SubtitleLanguage.Hungarian
                    , "Icelandic" to SubtitleLanguage.Icelandic
                    , "Indonesian" to SubtitleLanguage.Indonesian
                    , "Italian" to SubtitleLanguage.Italian
                    , "Japanese" to SubtitleLanguage.Japanese
                    , "Kazakh" to SubtitleLanguage.Kazakh
                    , "Khmer" to SubtitleLanguage.Khmer
                    , "Korean" to SubtitleLanguage.Korean
                    , "Latvian" to SubtitleLanguage.Latvian
                    , "Lithuanian" to SubtitleLanguage.Lithuanian
                    , "Luxembourgish" to SubtitleLanguage.Luxembourgish
                    , "Macedonian" to SubtitleLanguage.Macedonian
                    , "Malay" to SubtitleLanguage.Malay
                    , "Malayalam" to SubtitleLanguage.Malayalam
                    , "Mongolian" to SubtitleLanguage.Mongolian
                    , "Norwegian" to SubtitleLanguage.Norwegian
                    , "Occitan" to SubtitleLanguage.Occitan
                    , "Persian" to SubtitleLanguage.Persian
                    , "Polish" to SubtitleLanguage.Polish
                    , "Portuguese" to SubtitleLanguage.Portuguese
                    , "Brazilian Portuguese" to SubtitleLanguage.PortugueseBr
                    , "Romanian" to SubtitleLanguage.Romanian
                    , "Russian" to SubtitleLanguage.Russian
                    , "Serbian" to SubtitleLanguage.Serbian
                    , "Sinhalese" to SubtitleLanguage.Sinhalese
                    , "Slovak" to SubtitleLanguage.Slovak
                    , "Slovenian" to SubtitleLanguage.Slovenian
                    , "Spanish" to SubtitleLanguage.Spanish
                    , "Swahili" to SubtitleLanguage.Swahili
                    , "Swedish" to SubtitleLanguage.Swedish
                    , "Syriac" to SubtitleLanguage.Syriac
                    , "Tagalog" to SubtitleLanguage.Tagalog
                    , "Tamil" to SubtitleLanguage.Tamil
                    , "Telugu" to SubtitleLanguage.Telugu
                    , "Thai" to SubtitleLanguage.Thai
                    , "Turkish" to SubtitleLanguage.Turkish
                    , "Ukrainian" to SubtitleLanguage.Ukrainian
                    , "Urdu" to SubtitleLanguage.Urdu
                    , "Vietnamese" to SubtitleLanguage.Vietnamese
            )
        }

        /**
         * Get a list of all of the supported languages.
         */
        @JvmStatic
        val supportedLanguages: List<String>
            get() = languageMapping.keys.toList()

        /**
         * Get the language code of the associated [language].
         */
        @JvmStatic
        fun getCodeByLanguage(language: String): String? = languageMapping[language]

        /**
         * Get the language of the associated language [code].
         */
        @JvmStatic
        fun getLanguageByCode(code: String): String? = languageMapping
                .entries
                .firstOrNull { it.value == code }
                ?.key

    }
}

