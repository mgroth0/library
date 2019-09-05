package com.cgogolin.library

import java.util.*
import java.util.regex.Pattern

object LatexPrettyPrinter {

    private val latexCommandMap = createLatexCommandMap()
    private val latexSubstitutionMap = createLatexSubstitutionMap()
    private val latexExpansionMap = createLatexExpansionMap()
    private fun createLatexCommandMap(): Map<String, String> {
        val map = linkedMapOf<String, String>()
        /*Most entries in this list com from a file published by the W3C under: http://www.w3.org/Math/characters/unicode.xml
         * We are not handling:
         * \d{o}
         * \b{o}
         * \t{oo}
         * See also https://en.wikibooks.org/wiki/LaTeX/Special_Characters
         */
        map["\\;"] = "\u2009-0200A-0200A"
        map["\\-"] = "\u00AD"
        map["\\&"] = "\u0026"
        map["\\%"] = "\u0025"
        map["\\#"] = "\u0023"
        map["\\\"{y}"] = "\u00FF"
        map["\\\"{u}"] = "\u00FC"
        map["\\\"{o}"] = "\u00F6"
        map["\\\"{e}"] = "\u00EB"
        map["\\\"{a}"] = "\u00E4"
//        map.put("\\\"{\\i}","\u00EF")
        map["\\\"{i}"] = "\u00EF"
        map["\\\"{Y}"] = "\u0178"
        map["\\\"{U}"] = "\u00DC"
        map["\\\"{O}"] = "\u00D6"
        map["\\\"{I}"] = "\u00CF"
        map["\\\"{E}"] = "\u00CB"
        map["\\\"{A}"] = "\u00C4"
        map["\\\""] = "\u0308"
//        map.put("\\\'{}{I}","\u038A")
//        map.put("\\\'{}O","\u038C")
        map["\\\'{z}"] = "\u017A"
        map["\\\'{y}"] = "\u00FD"
        map["\\\'{u}"] = "\u00FA"
        map["\\\'{s}"] = "\u015B"
        map["\\\'{r}"] = "\u0155"
        map["\\\'{o}"] = "\u00F3"
        map["\\\'{o}"] = "\u03CC"
        map["\\\'{n}"] = "\u0144"
        map["\\\'{l}"] = "\u013A"
        map["\\\'{g}"] = "\u01F5"
        map["\\\'{e}"] = "\u00E9"
        map["\\\'{c}"] = "\u0107"
        map["\\\'{a}"] = "\u00E1"
//        map.put("\\\'{\\i}","\u00ED")
        map["\\\'{i}"] = "\u00ED"
        map["\\\'{Z}"] = "\u0179"
        map["\\\'{Y}"] = "\u00DD"
        map["\\\'{U}"] = "\u00DA"
        map["\\\'{S}"] = "\u015A"
        map["\\\'{R}"] = "\u0154"
        map["\\\'{O}"] = "\u00D3"
        map["\\\'{N}"] = "\u0143"
        map["\\\'{L}"] = "\u0139"
        map["\\\'{I}"] = "\u00CD"
        map["\\\'{H}"] = "\u0389"
        map["\\\'{E}"] = "\u00C9"
        map["\\\'{E}"] = "\u0388"
        map["\\\'{C}"] = "\u0106"
        map["\\\'{A}"] = "\u00C1"
//        map.put("\\\'{A}","\u0386")
//        map.put("\\\'{$\\alpha$}","\u03AC")
        map["\\\'"] = "\u0301"
        map["\\^{}"] = "\u005E"
        map["\\^{y}"] = "\u0177"
        map["\\^{w}"] = "\u0175"
        map["\\^{u}"] = "\u00FB"
        map["\\^{s}"] = "\u015D"
        map["\\^{o}"] = "\u00F4"
        map["\\^{h}"] = "\u0125"
        map["\\^{g}"] = "\u011D"
        map["\\^{e}"] = "\u00EA"
        map["\\^{c}"] = "\u0109"
        map["\\^{a}"] = "\u00E2"
        // map.put("\\^{\\j}","\u0135")
        // map.put("\\^{\\i}","\u00EE")
        map["\\^{Y}"] = "\u0176"
        map["\\^{W}"] = "\u0174"
        map["\\^{U}"] = "\u00DB"
        map["\\^{S}"] = "\u015C"
        map["\\^{O}"] = "\u00D4"
        map["\\^{J}"] = "\u0134"
        map["\\^{I}"] = "\u00CE"
        map["\\^{H}"] = "\u0124"
        map["\\^{G}"] = "\u011C"
        map["\\^{E}"] = "\u00CA"
        map["\\^{C}"] = "\u0108"
        map["\\^{A}"] = "\u00C2"
        map["\\^"] = "\u0302"
        map["\\`{u}"] = "\u00F9"
        map["\\`{o}"] = "\u00F2"
        map["\\`{e}"] = "\u00E8"
        map["\\`{a}"] = "\u00E0"
//        map.put("\\`{\\i}","\u00EC")
        map["\\`{U}"] = "\u00D9"
        map["\\`{O}"] = "\u00D2"
        map["\\`{I}"] = "\u00CC"
        map["\\`{E}"] = "\u00C8"
        map["\\`{A}"] = "\u00C0"
        map["\\.{z}"] = "\u017C"
        map["\\.{g}"] = "\u0121"
        map["\\.{e}"] = "\u0117"
        map["\\.{c}"] = "\u010B"
        map["\\.{Z}"] = "\u017B"
        map["\\.{I}"] = "\u0130"
        map["\\.{G}"] = "\u0120"
        map["\\.{E}"] = "\u0116"
        map["\\.{C}"] = "\u010A"
        map["\\."] = "\u0307"
        map["\\~{u}"] = "\u0169"
        map["\\~{o}"] = "\u00F5"
        map["\\~{n}"] = "\u00F1"
        map["\\~{a}"] = "\u00E3"
//        map.put("\\~{\\i}","\u0129")
        map["\\~{U}"] = "\u0168"
        map["\\~{O}"] = "\u00D5"
        map["\\~{N}"] = "\u00D1"
        map["\\~{I}"] = "\u0128"
        map["\\~{A}"] = "\u00C3"
        map["\\~"] = "\u0303"
        map["\\={u}"] = "\u016B"
        map["\\={o}"] = "\u014D"
        map["\\={e}"] = "\u0113"
        map["\\={a}"] = "\u0101"
//        map.put("\\={\\i}","\u012B")
        map["\\={U}"] = "\u016A"
        map["\\={O}"] = "\u014C"
        map["\\={I}"] = "\u012A"
        map["\\={E}"] = "\u0112"
        map["\\={A}"] = "\u0100"
        map["\\="] = "\u0304"
        map["\\BibTeX"] = "BibTeX"
        map["\\TeX"] = "TeX"
        map["\\texteuro"] = "\u20AC"
        map["\\mbox"] = ""
        map["\\u{u}"] = "\u016D"
        map["\\u{o}"] = "\u014F"
        map["\\u{g}"] = "\u011F"
        map["\\u{e}"] = "\u0115"
        map["\\u{a}"] = "\u0103"
//        map.put("\\u{\\i}","\u012D")
        map["\\u{U}"] = "\u016C"
        map["\\u{O}"] = "\u014E"
        map["\\u{I}"] = "\u012C"
        map["\\u{G}"] = "\u011E"
        map["\\u{E}"] = "\u0114"
        map["\\u{A}"] = "\u0102"
        map["\\k{}"] = "\u02DB"
        map["\\k{u}"] = "\u0173"
        map["\\k{i}"] = "\u012F"
        map["\\k{e}"] = "\u0119"
        map["\\k{a}"] = "\u0105"
        map["\\k{U}"] = "\u0172"
        map["\\k{I}"] = "\u012E"
        map["\\k{E}"] = "\u0118"
        map["\\k{A}"] = "\u0104"
        map["\\zeta"] = "\u03B6"
        map["\\xi"] = "\u03BE"
        map["\\wr"] = "\u2240"
        map["\\wp"] = "\u2118"
        map["\\wedge"] = "\u2227"
        map["\\v{z}"] = "\u017E"
        map["\\v{t}"] = "\u0165"
        map["\\v{s}"] = "\u0161"
        map["\\v{r}"] = "\u0159"
        map["\\v{n}"] = "\u0148"
        map["\\v{l}"] = "\u013E"
        map["\\v{e}"] = "\u011B"
        map["\\v{d}"] = "\u010F"
        map["\\v{c}"] = "\u010D"
        map["\\v{Z}"] = "\u017D"
        map["\\v{T}"] = "\u0164"
        map["\\v{S}"] = "\u0160"
        map["\\v{R}"] = "\u0158"
        map["\\v{N}"] = "\u0147"
        map["\\v{L}"] = "\u013D"
        map["\\v{E}"] = "\u011A"
        map["\\v{D}"] = "\u010E"
        map["\\v{C}"] = "\u010C"
        map["_\\ast"] = "\u2217"
        map["\\volintegral"] = "\u2230"
        map["\\virgo"] = "\u264D"
        map["\\verymuchless"] = "\u22D8"
        map["\\verymuchgreater"] = "\u22D9"
        map["\\vert"] = "\u007C"
        map["\\venus"] = "\u2640"
        map["\\veebar"] = "\u22BB"
        map["\\vee"] = "\u2228"
        map["\\vdots"] = "\u22EE"
        map["\\vdash"] = "\u22A2"
        map["\\vartriangleright"] = "\u22B3"
        map["\\vartriangleleft"] = "\u22B2"
        map["\\vartriangle"] = "\u25B5"
        map["\\varsupsetneq"] = "\u228B-0FE00"
        map["\\varsubsetneqq"] = "\u228A-0FE00"
        map["\\varsigma"] = "\u03C2"
        map["\\varrho"] = "\u03F1"
        map["\\varpi"] = "\u03D6"
        map["\\varphi"] = "\u03C6"
        map["\\varnothing"] = "\u2205"
        map["\\varkappa"] = "\u03F0"
        map["\\varepsilon"] = "\u025B"
        map["\\v"] = "\u030C"
        map["\\urcorner"] = "\u231D"
        map["\\uranus"] = "\u2645"
        map["\\upuparrows"] = "\u21C8"
        map["\\upslopeellipsis"] = "\u22F0"
        map["\\upsilon"] = "\u03C5"
        map["\\uplus"] = "\u228E"
        map["\\upharpoonright"] = "\u21BE"
        map["\\upharpoonleft"] = "\u21BF"
        map["\\updownarrow"] = "\u2195"
        map["\\uparrow"] = "\u2191"
        map["\\ulcorner"] = "\u231C"
        map["\\u"] = "\u0306"
        map["\\twoheadrightarrow"] = "\u21A0"
        map["\\twoheadleftarrow"] = "\u219E"
        map["\\truestate"] = "\u22A7"
        map["\\trianglerighteq"] = "\u22B5"
        map["\\triangleright"] = "\u25B9"
        map["\\triangleq"] = "\u225C"
        map["\\trianglelefteq"] = "\u22B4"
        map["\\triangleleft"] = "\u25C3"
        map["\\triangledown"] = "\u25BF"
        map["\\top"] = "\u22A4"
        map["\\tone{55}"] = "\u02E5"
        map["\\tone{44}"] = "\u02E6"
        map["\\tone{33}"] = "\u02E7"
        map["\\tone{22}"] = "\u02E8"
        map["\\tone{11}"] = "\u02E9"
        map["\\tildetrpl"] = "\u224B"
        map["\\therefore"] = "\u2234"
        map["\\th"] = "\u00FE"
        map["\\textyen"] = "\u00A5"
        map["\\textvisiblespace"] = "\u2423"
        map["\\textvartheta"] = "\u03D1"
        map["\\textturnk"] = "\u029E"
        map["\\texttrademark"] = "\u2122"
        map["\\times"] = "\u00D7"
        map["\\texttimes"] = "\u00D7"
        map["\\texttildelow"] = "\u02DC"
        map["\\textthreequarters"] = "\u00BE"
        map["\\texttheta"] = "\u03B8"
        map["\\textsterling"] = "\u00A3"
        map["\\textsection"] = "\u00A7"
        map["\\textregistered"] = "\u00AE"
        map["\\textquotesingle"] = "\u0027"
        map["\\textquotedblright"] = "\u201D"
        map["\\textquotedblleft"] = "\u201C"
        map["\\textquestiondown"] = "\u00BF"
        map["\\textphi"] = "\u0278"
        map["\\textperthousand"] = "\u2030"
        map["\\textpertenthousand"] = "\u2031"
        map["\\textperiodcentered"] = "\u02D9"
        map["\\textparagraph"] = "\u00B6"
        map["\\textordmasculine"] = "\u00BA"
        map["\\textordfeminine"] = "\u00AA"
        map["\\textonequarter"] = "\u00BC"
        map["\\textonehalf"] = "\u00BD"
        map["\\textnrleg"] = "\u019E"
        map["\\texthvlig"] = "\u0195"
        map["\\textfrac{7}{8}"] = "\u215E"
        map["\\textfrac{5}{8}"] = "\u215D"
        map["\\textfrac{5}{6}"] = "\u215A"
        map["\\textfrac{4}{5}"] = "\u2158"
        map["\\textfrac{3}{8}"] = "\u215C"
        map["\\textfrac{3}{5}"] = "\u2157"
        map["\\textfrac{2}{5}"] = "\u2156"
        map["\\textfrac{2}{3}"] = "\u2154"
        map["\\textfrac{1}{8}"] = "\u215B"
        map["\\textfrac{1}{6}"] = "\u2159"
        map["\\textfrac{1}{5}"] = "\u2155"
        map["\\textfrac{1}{3}"] = "\u2153"
        map["\\frac{7}{8}"] = "\u215E"
        map["\\frac{5}{8}"] = "\u215D"
        map["\\frac{5}{6}"] = "\u215A"
        map["\\frac{4}{5}"] = "\u2158"
        map["\\frac{3}{8}"] = "\u215C"
        map["\\frac{3}{5}"] = "\u2157"
        map["\\frac{2}{5}"] = "\u2156"
        map["\\frac{2}{3}"] = "\u2154"
        map["\\frac{1}{8}"] = "\u215B"
        map["\\frac{1}{6}"] = "\u2159"
        map["\\frac{1}{5}"] = "\u2155"
        map["\\frac{1}{3}"] = "\u2153"
        map["\\textexclamdown"] = "\u00A1"
        map["\\textendash"] = "\u2013"
        map["\\textemdash"] = "\u2014"
        map["\\textdoublepipe"] = "\u01C2"
        map["\\textdollar"] = "\u0024"
        map["\\textdegree"] = "\u00B0"
        map["\\textdaggerdbl"] = "\u2021"
        map["\\textdagger"] = "\u2020"
        map["\\dagger"] = "\u2020"
        map["\\dag"] = "\u2020"
        map["\\textcurrency"] = "\u00A4"
        map["\\textcopyright"] = "\u00A9"
        map["\\textcent"] = "\u00A2"
        map["\\textbullet"] = "\u2022"
        map["\\textbrokenbar"] = "\u00A6"
//        map.put("\\textbackslash","\u005C") // very strange error!!!
        map["\\textasciitilde"] = "\u007E"
        map["\\textasciimacron"] = "\u00AF"
        map["\\textasciigrave"] = "\u0060"
        map["\\textasciidieresis"] = "\u00A8"
        map["\\textasciicaron"] = "\u02C7"
        map["\\textasciibreve"] = "\u02D8"
        map["\\textasciiacute"] = "\u00B4"
        map["\\textTheta"] = "\u03F4"
        map["\\taurus"] = "\u2649"
        map["\\tau"] = "\u03C4"
        map["\\swarrow"] = "\u2199"
        map["\\surfintegral"] = "\u222F"
        map["\\surd"] = "\u221A"
        map["\\supsetneqq"] = "\u2ACC"
        map["\\supsetneq"] = "\u228B"
        map["\\supseteqq"] = "\u2AC6"
        map["\\supseteq"] = "\u2287"
        map["\\supset"] = "\u2283"
        map["\\sum"] = "\u2211"
        map["\\succnsim"] = "\u22E9"
        map["\\succneqq"] = "\u2AB6"
        map["\\succnapprox"] = "\u2ABA"
        map["\\succeq"] = "\u2AB0"
        map["\\succcurlyeq"] = "\u227D"
        map["\\succapprox"] = "\u227F"
        map["\\succapprox"] = "\u2AB8"
        map["\\succ"] = "\u227B"
        map["\\subsetneqq"] = "\u2ACB"
        map["\\subsetneq"] = "\u228A"
        map["\\subseteqq"] = "\u2AC5"
        map["\\subseteq"] = "\u2286"
        map["\\subset"] = "\u2282"
        map["\\starequal"] = "\u225B"
        map["\\star"] = "\u22C6"
        map["\\stackrel{*}{=}"] = "\u2A6E"
        map["\\ss"] = "\u00DF"
        map["\\square"] = "\u25A1"
        map["\\sqsupseteq"] = "\u2292"
        map["\\sqsupset"] = "\u2290"
        map["\\sqsubseteq"] = "\u2291"
        map["\\sqsubset"] = "\u228F"
        map["\\sqrint"] = "\u2A16"
        map["\\sqcup"] = "\u2294"
        map["\\sqcap"] = "\u2293"
        map["\\sphericalangle"] = "\u2222"
        map["\\space"] = "\u0020"
        map["\\smile"] = "\u2323"
        map["\\simeq"] = "\u2243"
        map["\\sim\\joinrel\\leadsto"] = "\u27FF"
        map["\\sim"] = "\u223C"
        map["\\sigma"] = "\u03C3"
        map["\\sharp"] = "\u266F"
        map["\\setminus"] = "\u2216"
        map["\\searrow"] = "\u2198"
        map["\\scorpio"] = "\u264F"
        map["\\saturn"] = "\u2644"
        map["\\sagittarius"] = "\u2650"
        map["\\r{}"] = "\u02DA"
        map["\\r{u}"] = "\u016F"
        map["\\r{U}"] = "\u016E"
        map["\\rule{1em}{1pt}"] = "\u2015"
        map["\\rtimes"] = "\u22CA"
        map["\\rmoustache"] = "\u23B1"
        map["\\risingdotseq"] = "\u2253"
        map["\\rightthreetimes"] = "\u22CC"
        map["\\rightsquigarrow"] = "\u21DD"
        map["\\rightrightarrows"] = "\u21C9"
        map["\\rightmoon"] = "\u263E"
        map["\\rightleftharpoons"] = "\u21CC"
        map["\\rightleftarrows"] = "\u21C4"
        map["\\rightharpoonup"] = "\u21C0"
        map["\\rightharpoondown"] = "\u21C1"
        map["\\rightarrowtail"] = "\u21A3"
        map["\\rightarrow"] = "\u2192"
        map["\\rightanglearc"] = "\u22BE"
        map["\\rightangle"] = "\u221F"
        map["\\rho"] = "\u03C1"
        map["\\rfloor"] = "\u230B"
        map["\\recorder"] = "\u2315"
        map["\\rceil"] = "\u2309"
        map["\\rbrace"] = "\u007D"
        map["\\rangle"] = "\u232A"
        map["\\r"] = "\u030A"
        map["\\quarternote"] = "\u2669"
        map["\\psi"] = "\u03C8"
        map["\\propto"] = "\u221D"
        map["\\prod"] = "\u220F"
        map["\\precneqq"] = "\u2AB5"
        map["\\precnapprox"] = "\u2AB9"
        map["\\preceq"] = "\u2AAF"
        map["\\precedesnotsimilar"] = "\u22E8"
        map["\\preccurlyeq"] = "\u227C"
        map["\\precapprox"] = "\u227E"
        map["\\precapprox"] = "\u2AB7"
        map["\\prec"] = "\u227A"
        map["\\pm"] = "\u00B1"
        map["\\pluto"] = "\u2647"
        map["\\pitchfork"] = "\u22D4"
        map["\\pisces"] = "\u2653"
        map["\\pi"] = "\u03C0"
        map["\\phi"] = "\u03D5"
        map["\\perspcorrespond"] = "\u2306"
        map["\\perspcorrespond"] = "\u2A5E"
        map["\\perp"] = "\u22A5"
        map["\\partial"] = "\u2202"
        map["\\parallel"] = "\u2225"
        map["\\otimes"] = "\u2297"
        map["\\oslash"] = "\u2298"
        map["\\original"] = "\u22B6"
        map["\\oplus"] = "\u2295"
        map["\\openbracketright"] = "\u301B"
        map["\\openbracketleft"] = "\u301A"
        map["\\ominus"] = "\u2296"
        map["\\omega"] = "\u03C9"
        map["\\oint"] = "\u222E"
        map["\\oe"] = "\u0153"
        map["\\odot"] = "\u2299"
        map["\\o"] = "\u00F8"
        map["\\nwarrow"] = "\u2196"
        map["\\nvdash"] = "\u22AC"
        map["\\nvDash"] = "\u22AD"
        map["\\nu"] = "\u03BD"
        map["\\ntrianglerighteq"] = "\u22ED"
        map["\\ntriangleright"] = "\u22EB"
        map["\\ntrianglelefteq"] = "\u22EC"
        map["\\ntriangleleft"] = "\u22EA"
        map["\\nsupseteqq"] = "\u2AC6-00338"
        map["\\nsubseteqq"] = "\u2AC5-00338"
        map["\\nrightarrow"] = "\u219B"
        map["\\nparallel"] = "\u2226"
        map["\\notlessgreater"] = "\u2278"
        map["\\notgreaterless"] = "\u2279"
        map["\\not\\supseteq"] = "\u2289"
        map["\\not\\supset"] = "\u2285"
        map["\\not\\succeq"] = "\u2AB0-00338"
        map["\\not\\succ"] = "\u2281"
        map["\\not\\subseteq"] = "\u2288"
        map["\\not\\subset"] = "\u2284"
        map["\\not\\sqsupseteq"] = "\u22E3"
        map["\\not\\sqsubseteq"] = "\u22E2"
        map["\\not\\simeq"] = "\u2244"
        map["\\not\\sim"] = "\u2241"
        map["\\not\\preceq"] = "\u2AAF-00338"
        map["\\not\\prec"] = "\u2280"
        map["\\not\\ni"] = "\u220C"
        map["\\not\\leq"] = "\u2270"
        map["\\not\\kern-0.3em\\times"] = "\u226D"
        map["\\not\\in"] = "\u2209"
        map["\\not\\geq"] = "\u2271"
        map["\\not\\equiv"] = "\u2262"
        map["\\not\\doteq"] = "\u2250-00338"
        map["\\not\\cong"] = "\u2247"
        map["\\not\\approx"] = "\u2249"
        map["\\not\\apid"] = "\u224B-00338"
        map["\\not>"] = "\u226F"
        map["\\not<"] = "\u226E"
        map["\\not{=}"] = "\u2260"
        map["\\nolinebreak"] = "\u2060"
        map["\\nmid"] = "\u2224"
        map["\\nleqslant"] = "\u2A7D-00338"
        map["\\nleftrightarrow"] = "\u21AE"
        map["\\nleftarrow"] = "\u219A"
        map["\\ni"] = "\u220B"
        map["\\ngeqslant"] = "\u2A7E-00338"
        map["\\ng"] = "\u014B"
        map["\\nexists"] = "\u2204"
        map["\\neptune"] = "\u2646"
        map["\\nearrow"] = "\u2197"
        map["\\natural"] = "\u266E"
        map["\\nabla"] = "\u2207"
        map["\\nVdash"] = "\u22AE"
        map["\\nVDash"] = "\u22AF"
        map["\\nRightarrow"] = "\u21CF"
        map["\\nLeftrightarrow"] = "\u21CE"
        map["\\nLeftarrow"] = "\u21CD"
        map["\\multimap"] = "\u22B8"
        map["\\mu"] = "\u03BC"
        map["\\mp"] = "\u2213"
        map["\\mkern4mu"] = "\u205F"
        map["\\mkern1mu"] = "\u200A"
        map["\\mid"] = "\u2223"
        map["\\mho"] = "\u2127"
        map["\\mercury"] = "\u263F"
        map["\\measuredangle"] = "\u2221"
        map["\\mbox{\\texteuro}"] = "\u20AC"
        // map.put("\\mathscr{o}","\u2134")
        // map.put("\\mathscr{l}","\u2113")
        // map.put("\\mathscr{g}","\u210A")
        // map.put("\\mathscr{e}","\u212F")
        // map.put("\\mathscr{R}","\u211B")
        // map.put("\\mathscr{M}","\u2133")
        // map.put("\\mathscr{L}","\u2112")
        // map.put("\\mathscr{I}","\u2110")
        // map.put("\\mathscr{H}","\u210B")
        // map.put("\\mathscr{F}","\u2131")
        // map.put("\\mathscr{E}","\u2130")
        // map.put("\\mathscr{B}","\u212C")
        // map.put("\\mathrm{\\mu}","\u00B5")
        // map.put("\\mathrm{\\ddot{Y}}","\u03AB")
        // map.put("\\mathrm{\\ddot{I}}","\u03AA")
        // map.put("\\mathrm{\'\\Omega}","\u038F")
        // map.put("\\mathrm{\'Y}","\u038E")
        // map.put("\\mathfrak{Z}","\u2128")
        // map.put("\\mathfrak{R}","\u211C")
        // map.put("\\mathfrak{I}","\u2111")
        // map.put("\\mathfrak{H}","\u210C")
        // map.put("\\mathfrak{C}","\u212D")
        map["\\mathchar\"2208"] = "\u2316"
        map["\\mathbin{{:}\\!\\!{-}\\!\\!{:}}"] = "\u223A"
        // map.put("\\mathbb{Z}","\u2124")
        // map.put("\\mathbb{R}","\u211D")
        // map.put("\\mathbb{Q}","\u211A")
        // map.put("\\mathbb{P}","\u2119")
        // map.put("\\mathbb{N}","\u2115")
        // map.put("\\mathbb{H}","\u210D")
        // map.put("\\mathbb{C}","\u2102")
        map["\\mapsto"] = "\u21A6"
        map["\\male"] = "\u2642"
        map["\\lvertneqq"] = "\u2268-0FE00"
        map["\\ltimes"] = "\u22C9"
        map["\\lrcorner"] = "\u231F"
        map["\\lozenge"] = "\u25CA"
        map["\\looparrowright"] = "\u21AC"
        map["\\looparrowleft"] = "\u21AB"
        map["\\longrightarrow"] = "\u27F6"
        map["\\longmapsto"] = "\u27FC"
        map["\\longleftrightarrow"] = "\u27F7"
        map["\\longleftarrow"] = "\u27F5"
        map["\\lnsim"] = "\u22E6"
        map["\\lnot"] = "\u00AC"
        map["\\lneqq"] = "\u2268"
        map["\\lneq"] = "\u2A87"
        map["\\lnapprox"] = "\u2A89"
        map["\\lmoustache"] = "\u23B0"
        map["\\llcorner"] = "\u231E"
        map["\\ll"] = "\u226A"
        map["\\libra"] = "\u264E"
        map["\\lfloor"] = "\u230A"
        map["\\lessgtr"] = "\u2276"
        map["\\lessequivlnt"] = "\u2272"
        map["\\lesseqqgtr"] = "\u2A8B"
        map["\\lesseqgtr"] = "\u22DA"
        map["\\lessdot"] = "\u22D6"
        map["\\lessapprox"] = "\u2A85"
        map["\\leqslant"] = "\u2A7D"
        map["\\leqq"] = "\u2266"
        map["\\leq"] = "\u2264"
        map["\\leo"] = "\u264C"
        map["\\leftthreetimes"] = "\u22CB"
        map["\\leftrightsquigarrow"] = "\u21AD"
        map["\\leftrightharpoons"] = "\u21CB"
        map["\\leftrightarrows"] = "\u21C6"
        map["\\leftrightarrow"] = "\u2194"
        map["\\leftleftarrows"] = "\u21C7"
        map["\\leftharpoonup"] = "\u21BC"
        map["\\leftharpoondown"] = "\u21BD"
        map["\\leftarrowtail"] = "\u21A2"
        map["\\leftarrow"] = "\u2190"
        map["\\ldots"] = "\u2026"
        map["\\lceil"] = "\u2308"
        map["\\lbrace"] = "\u007B"
        map["\\lazysinv"] = "\u223E"
        map["\\langle"] = "\u2329"
        map["\\lambda"] = "\u03BB"
        map["\\l"] = "\u0142"
        map["\\kappa"] = "\u03BA"
        map["\\k"] = "\u0328"
        map["\\jupiter"] = "\u2643"
        map["\\iota"] = "\u03B9"
        map["\\intercal"] = "\u22BA"
        map["\\int\\!\\int\\!\\int"] = "\u222D"
        map["\\int\\!\\int"] = "\u222C"
        map["\\int"] = "\u222B"
        map["\\infty"] = "\u221E"
        map["\\in"] = "\u2208"
        map["\\image"] = "\u22B7"
        map["\\i"] = "\u0131"
        map["\\hslash"] = "\u210F"
        map["\\hphantom{0}"] = "\u2007"
        map["\\hphantom{,}"] = "\u2008"
        map["\\hookrightarrow"] = "\u21AA"
        map["\\hookleftarrow"] = "\u21A9"
        map["\\homothetic"] = "\u223B"
        map["\\hermitconjmatrix"] = "\u22B9"
        map["\\gvertneqq"] = "\u2269-0FE00"
        map["\\guilsinglright"] = "\u203A"
        map["\\guilsinglleft"] = "\u2039"
        map["\\guillemotright"] = "\u00BB"
        map["\\guillemotleft"] = "\u00AB"
        map["\\gtrless"] = "\u2277"
        map["\\gtreqqless"] = "\u2A8C"
        map["\\gtreqless"] = "\u22DB"
        map["\\gtrdot"] = "\u22D7"
        map["\\gtrapprox"] = "\u2A86"
        map["\\greaterequivlnt"] = "\u2273"
        map["\\gnsim"] = "\u22E7"
        map["\\gneqq"] = "\u2269"
        map["\\gneq"] = "\u2A88"
        map["\\gnapprox"] = "\u2A8A"
        map["\\gimel"] = "\u2137"
        map["\\gg"] = "\u226B"
        map["\\geqslant"] = "\u2A7E"
        map["\\geqq"] = "\u2267"
        map["\\geq"] = "\u2265"
        map["\\gemini"] = "\u264A"
        map["\\gamma"] = "\u03B3"
        map["\\frown"] = "\u2322"
        map["\\forcesextra"] = "\u22A8"
        map["\\forall"] = "\u2200"
        map["\\flat"] = "\u266D"
        map["\\fbox{~~}"] = "\u25AD"
        map["\\fallingdotseq"] = "\u2252"
        map["\\exists"] = "\u2203"
        map["\\eth"] = "\u01AA"
        map["\\eta"] = "\u03B7"
        map["\\estimates"] = "\u2259"
        map["\\equiv"] = "\u2261"
        map["\\eqslantless"] = "\u2A95"
        map["\\eqslantgtr"] = "\u2A96"
        map["\\eqcirc"] = "\u2256"
        map["\\epsilon"] = "\u03B5"
        map["\\ensuremath{\\Elzpes}"] = "\u20A7"
        map["\\eighthnote"] = "\u266A"
        map["\\downslopeellipsis"] = "\u22F1"
        map["\\downharpoonright"] = "\u21C2"
        map["\\downharpoonleft"] = "\u21C3"
        map["\\downdownarrows"] = "\u21CA"
        map["\\downarrow"] = "\u2193"
        map["\\dotplus"] = "\u2214"
        map["\\doteqdot"] = "\u2251"
        map["\\doteq"] = "\u2250"
        map["\\dj"] = "\u0111"
        map["\\divideontimes"] = "\u22C7"
        map["\\div"] = "\u00F7"
        map["\\digamma"] = "\u03DD"
        map["\\diamond"] = "\u22C4"
        map["\\diamond"] = "\u2662"
        map["\\diagup"] = "\u2571"
        map["\\dh"] = "\u00F0"
        map["\\delta"] = "\u03B4"
        // map.put("\\ddot{\\upsilon}","\u03CB")
        // map.put("\\ddot{\\iota}","\u03CA")
        map["\\dddot"] = "\u20DB"
        map["\\ddddot"] = "\u20DC"
        map["\\dblarrowupdown"] = "\u21C5"
        map["\\dashv"] = "\u22A3"
        map["\\daleth"] = "\u2138"
        map["\\c{}"] = "\u00B8"
        map["\\c{t}"] = "\u0163"
        map["\\c{s}"] = "\u015F"
        map["\\c{r}"] = "\u0157"
        map["\\c{n}"] = "\u0146"
        map["\\c{l}"] = "\u013C"
        map["\\c{k}"] = "\u0137"
        map["\\c{g}"] = "\u0123"
        map["\\c{c}"] = "\u00E7"
        map["\\c{T}"] = "\u0162"
        map["\\c{S}"] = "\u015E"
        map["\\c{R}"] = "\u0156"
        map["\\c{N}"] = "\u0145"
        map["\\c{L}"] = "\u013B"
        map["\\c{K}"] = "\u0136"
        map["\\c{G}"] = "\u0122"
        map["\\c{C}"] = "\u00C7"
        map["\\curvearrowright"] = "\u21B7"
        map["\\curvearrowleft"] = "\u21B6"
        map["\\curlywedge"] = "\u22CF"
        map["\\curlyvee"] = "\u22CE"
        map["\\curlyeqsucc"] = "\u22DF"
        map["\\curlyeqprec"] = "\u22DE"
        map["\\cup"] = "\u222A"
        map["\\coprod"] = "\u2210"
        map["\\cong"] = "\u2245"
        map["\\complement"] = "\u2201"
        map["\\clwintegral"] = "\u2231"
        map["\\clockoint"] = "\u2A0F"
        map["\\circleddash"] = "\u229D"
        map["\\circledcirc"] = "\u229A"
        map["\\circledast"] = "\u229B"
        map["\\circledS"] = "\u24C8"
        map["\\circlearrowright"] = "\u21BB"
        map["\\circlearrowleft"] = "\u21BA"
        map["\\circeq"] = "\u2257"
        map["\\circ"] = "\u2218"
        map["\\chi"] = "\u03C7"
        map["\\cdots"] = "\u22EF"
        map["\\cdot"] = "\u00B7"
        map["\\cdot"] = "\u22C5"
        map["\\capricornus"] = "\u2651"
        map["\\cap"] = "\u2229"
        map["\\cancer"] = "\u264B"
        map["\\c"] = "\u0327"
        map["\\bumpeq"] = "\u224F"
        map["\\bullet"] = "\u2219"
        map["\\boxtimes"] = "\u22A0"
        map["\\boxplus"] = "\u229E"
        map["\\boxminus"] = "\u229F"
        map["\\boxdot"] = "\u22A1"
        map["\\bowtie"] = "\u22C8"
        map["\\blacktriangleright"] = "\u25B8"
        map["\\blacktriangleleft"] = "\u25C2"
        map["\\blacktriangledown"] = "\u25BE"
        map["\\blacktriangle"] = "\u25B4"
        map["\\blacksquare"] = "\u25AA"
        map["\\blacklozenge"] = "\u29EB"
        map["\\bigtriangleup"] = "\u25B3"
        map["\\bigtriangledown"] = "\u25BD"
        map["\\bigcup"] = "\u22C3"
        map["\\bigcirc"] = "\u25CB"
        map["\\bigcirc"] = "\u25EF"
        map["\\bigcap"] = "\u22C2"
        map["\\between"] = "\u226C"
        map["\\beth"] = "\u2136"
        map["\\beta"] = "\u03B2"
        map["\\because"] = "\u2235"
        map["\\barwedge"] = "\u2305"
        map["\\backsimeq"] = "\u22CD"
        map["\\backsim"] = "\u223D"
        map["\\backprime"] = "\u2035"
        map["\\backepsilon"] = "\u03F6"
        map["\\asymp"] = "\u224D"
        map["\\ast"] = "\u002A"
        map["\\arrowwaveright"] = "\u219C"
        map["\\arrowwaveright"] = "\u219D"
        map["\\aries"] = "\u2648"
        map["\\aquarius"] = "\u2652"
        map["\\approxnotequal"] = "\u2246"
        map["\\approxeq"] = "\u224A"
        map["\\approx"] = "\u2248"
        map["\\angle"] = "\u2220"
        map["\\amalg"] = "\u2A3F"
        map["\\alpha"] = "\u03B1"
        map["\\allequal"] = "\u224C"
        map["\\aleph"] = "\u2135"
        map["\\ae"] = "\u00E6"
        // map.put("\\acute{\\upsilon}","\u03CD")
        // map.put("\\acute{\\omega}","\u03CE")
        // map.put("\\acute{\\iota}","\u03AF")
        // map.put("\\acute{\\eta}","\u03AE")
        // map.put("\\acute{\\epsilon}","\u03AD")
        // map.put("\\acute{\\ddot{\\upsilon}}","\u03B0")
        // map.put("\\acute{\\ddot{\\iota}}","\u0390")
        map["\\aa"] = "\u00E5"
        map["\\`"] = "\u0300"
        map["\\_"] = "\u005F"
        map["\\Zeta"] = "\u0396"
        map["\\Xi"] = "\u039E"
        map["\\Vvdash"] = "\u22AA"
        map["\\Vert"] = "\u2016"
        map["\\Vdash"] = "\u22A9"
        map["\\VDash"] = "\u22AB"
        map["\\Upsilon"] = "\u03A5"
        map["\\Upsilon"] = "\u03D2"
        map["\\Updownarrow"] = "\u21D5"
        map["\\Uparrow"] = "\u21D1"
        map["\\UpEquilibrium"] = "\u296E"
        map["\\UpArrowBar"] = "\u2912"
        map["\\Theta"] = "\u0398"
        map["\\Tau"] = "\u03A4"
        map["\\TH"] = "\u00DE"
        map["\\Supset"] = "\u22D1"
        map["\\Subset"] = "\u22D0"
        map["\\Stigma"] = "\u03DA"
        map["\\Sigma"] = "\u03A3"
        map["\\Sampi"] = "\u03E0"
        map["\\RuleDelayed"] = "\u29F4"
        map["\\Rsh"] = "\u21B1"
        map["\\Rrightarrow"] = "\u21DB"
        map["\\RoundImplies"] = "\u2970"
        map["\\Rightarrow"] = "\u21D2"
        map["\\RightVectorBar"] = "\u2953"
        map["\\RightUpVectorBar"] = "\u2954"
        map["\\RightUpTeeVector"] = "\u295C"
        map["\\RightUpDownVector"] = "\u294F"
        map["\\RightTriangleBar"] = "\u29D0"
        map["\\RightTeeVector"] = "\u295B"
        map["\\RightDownVectorBar"] = "\u2955"
        map["\\RightDownTeeVector"] = "\u295D"
        map["\\Rho"] = "\u03A1"
        map["\\ReverseUpEquilibrium"] = "\u296F"
        map["\\Psi"] = "\u03A8"
        map["\\Pisymbol{ppi022}{87}"] = "\u03D0"
        map["\\Pisymbol{ppi020}{117}"] = "\u2A9D"
        map["\\Pisymbol{ppi020}{105}"] = "\u2A9E"
        map["\\Pi"] = "\u03A0"
        map["\\Phi"] = "\u03A6"
        map["\\Omega"] = "\u03A9"
        map["\\Omega"] = "\u2126"
        map["\\OE"] = "\u0152"
        map["\\O"] = "\u00D8"
        map["\\NotSucceedsTilde"] = "\u227F-00338"
        map["\\NotSquareSuperset"] = "\u2290-00338"
        map["\\NotSquareSubset"] = "\u228F-00338"
        map["\\NotRightTriangleBar"] = "\u29D0-00338"
        map["\\NotPrecedesTilde"] = "\u227E-00338"
        map["\\NotNestedLessLess"] = "\u2AA1-00338"
        map["\\NotNestedGreaterGreater"] = "\u2AA2-00338"
        map["\\NotLessLess"] = "\u226A-00338"
        map["\\NotLeftTriangleBar"] = "\u29CF-00338"
        map["\\NotHumpEqual"] = "\u224F-00338"
        map["\\NotHumpDownHump"] = "\u224E-00338"
        map["\\NotGreaterGreater"] = "\u226B-00338"
        map["\\NotEqualTilde"] = "\u2242-00338"
        map["\\NestedLessLess"] = "\u2AA1"
        map["\\NestedGreaterGreater"] = "\u2AA2"
        map["\\NG"] = "\u014A"
        map["\\Lsh"] = "\u21B0"
        map["\\Longrightarrow"] = "\u27F9"
        map["\\Longleftrightarrow"] = "\u27FA"
        map["\\Longleftarrow"] = "\u27F8"
        map["\\Lleftarrow"] = "\u21DA"
        map["\\Leftrightarrow"] = "\u21D4"
        map["\\Leftarrow"] = "\u21D0"
        map["\\LeftVectorBar"] = "\u2952"
        map["\\LeftUpVectorBar"] = "\u2958"
        map["\\LeftUpTeeVector"] = "\u2960"
        map["\\LeftUpDownVector"] = "\u2951"
        map["\\LeftTriangleBar"] = "\u29CF"
        map["\\LeftTeeVector"] = "\u295A"
        map["\\LeftRightVector"] = "\u294E"
        map["\\LeftDownVectorBar"] = "\u2959"
        map["\\LeftDownTeeVector"] = "\u2961"
        map["\\Lambda"] = "\u039B"
        map["\\L"] = "\u0141"
        map["\\Koppa"] = "\u03DE"
        map["\\Kappa"] = "\u039A"
        map["\\Iota"] = "\u0399"
        map["\\H{}"] = "\u02DD"
        map["\\H{u}"] = "\u0171"
        map["\\H{o}"] = "\u0151"
        map["\\H{U}"] = "\u0170"
        map["\\H{O}"] = "\u0150"
        map["\\H"] = "\u030B"
        map["\\Gamma"] = "\u0393"
        map["\\Eta"] = "\u0397"
        map["\\Equal"] = "\u2A75"
        map["\\Epsilon"] = "\u0395"
        map["\\Elzyogh"] = "\u0292"
        map["\\Elzxrat"] = "\u211E"
        map["\\Elzxl"] = "\u0335"
        map["\\Elzxh"] = "\u0127"
        map["\\Elzvrecto"] = "\u25AF"
        map["\\Elzverts"] = "\u02C8"
        map["\\Elzverti"] = "\u02CC"
        map["\\Elztrny"] = "\u028E"
        map["\\Elztrnt"] = "\u0287"
        map["\\Elztrnsa"] = "\u0252"
        map["\\Elztrnrl"] = "\u027A"
        map["\\Elztrnr"] = "\u0279"
        map["\\Elztrnmlr"] = "\u0270"
        map["\\Elztrnm"] = "\u026F"
        map["\\Elztrnh"] = "\u0265"
        map["\\Elztrna"] = "\u0250"
        map["\\Elztfnc"] = "\u2980"
        map["\\Elztesh"] = "\u02A7"
        map["\\Elztdcol"] = "\u2AF6"
        map["\\Elzsqspne"] = "\u22E5"
        map["\\Elzsqfse"] = "\u25EA"
        map["\\Elzsqfr"] = "\u25E8"
        map["\\Elzsqfnw"] = "\u2519"
        map["\\Elzsqfl"] = "\u25E7"
        map["\\Elzschwa"] = "\u0259"
        map["\\Elzsbrhr"] = "\u02D2"
        map["\\Elzsblhr"] = "\u02D3"
        map["\\Elzsbbrg"] = "\u032A"
        map["\\Elzrvbull"] = "\u25D8"
        map["\\Elzrttrnr"] = "\u027B"
        map["\\Elzrtlz"] = "\u0290"
        map["\\Elzrtlt"] = "\u0288"
        map["\\Elzrtls"] = "\u0282"
        map["\\Elzrtlr"] = "\u027D"
        map["\\Elzrtln"] = "\u0273"
        map["\\Elzrtll"] = "\u026D"
        map["\\Elzrtld"] = "\u0256"
        map["\\Elzrl"] = "\u027C"
        map["\\Elzrh"] = "\u0322"
        map["\\Elzreglst"] = "\u0295"
        map["\\Elzreapos"] = "\u201B"
        map["\\Elzrarrx"] = "\u2947"
        map["\\Elzrais"] = "\u02D4"
        map["\\ElzrLarr"] = "\u2944"
        map["\\Elzpupsil"] = "\u028A"
        map["\\Elzpscrv"] = "\u028B"
        map["\\Elzpgamma"] = "\u0263"
        map["\\Elzpbgam"] = "\u0264"
        map["\\Elzpalh"] = "\u0321"
        map["\\Elzopeno"] = "\u0254"
        map["\\Elzminhat"] = "\u2A5F"
        map["\\Elzltln"] = "\u0272"
        map["\\Elzltlmr"] = "\u0271"
        map["\\Elzlpargt"] = "\u29A0"
        map["\\Elzlow"] = "\u02D5"
        map["\\Elzlmrk"] = "\u02D0"
        map["\\Elzinvw"] = "\u028D"
        map["\\Elzinvv"] = "\u028C"
        map["\\Elzinglst"] = "\u0296"
        map["\\Elzhlmrk"] = "\u02D1"
        map["\\Elzglst"] = "\u0294"
        map["\\Elzfhr"] = "\u027E"
        map["\\Elzesh"] = "\u0283"
        map["\\Elzdyogh"] = "\u02A4"
        map["\\Elzdshfnc"] = "\u2506"
        map["\\Elzdlcorn"] = "\u23A3"
        map["\\Elzdefas"] = "\u29CB"
        map["\\Elzddfnc"] = "\u2999"
        map["\\Elzclomeg"] = "\u0277"
        map["\\Elzcirfr"] = "\u25D1"
        map["\\Elzcirfl"] = "\u25D0"
        map["\\Elzcirfb"] = "\u25D2"
        map["\\Elzbtdl"] = "\u026C"
        map["\\Elzbar"] = "\u0336"
        map["\\ElzTimes"] = "\u2A2F"
        map["\\ElzThr"] = "\u2A05"
        map["\\ElzSup"] = "\u2A08"
        map["\\ElzRlarr"] = "\u2942"
        map["\\ElzOr"] = "\u2A54"
        map["\\ElzLap"] = "\u29CA"
        map["\\ElzInf"] = "\u2A07"
        map["\\ElzCint"] = "\u2A0D"
        map["\\ElzAnd"] = "\u2A53"
        map["\\Elxuplus"] = "\u2A04"
        map["\\Elxsqcup"] = "\u2A06"
        map["\\Elroang"] = "\u2986"
        map["\\Elorarr"] = "\u2941"
        map["\\Elolarr"] = "\u2940"
        map["\\ElOr"] = "\u2A56"
        map["\\Downarrow"] = "\u21D3"
        map["\\DownRightVectorBar"] = "\u2957"
        map["\\DownRightTeeVector"] = "\u295F"
        map["\\DownLeftVectorBar"] = "\u2956"
        map["\\DownLeftTeeVector"] = "\u295E"
        map["\\DownLeftRightVector"] = "\u2950"
        map["\\DownArrowUpArrow"] = "\u21F5"
        map["\\DownArrowBar"] = "\u2913"
        map["\\Digamma"] = "\u03DC"
        map["\\Delta"] = "\u0394"
        map["\\DJ"] = "\u0110"
        map["\\DH"] = "\u00D0"
        map["\\Cup"] = "\u22D3"
        map["\\Colon"] = "\u2237"
        map["\\Chi"] = "\u03A7"
        map["\\Cap"] = "\u22D2"
        map["\\Bumpeq"] = "\u224E"
        map["\\Beta"] = "\u0392"
        map["\\Angle"] = "\u299C"
        map["\\Alpha"] = "\u0391"
        map["\\AE"] = "\u00C6"
        map["\\AA"] = "\u00C5"
        map["\\AA"] = "\u212B"
        // map.put("\\ding{99}","\u2743")
        // map.put("\\ding{98}","\u2742")
        // map.put("\\ding{97}","\u2741")
        // map.put("\\ding{96}","\u2740")
        // map.put("\\ding{95}","\u273F")
        // map.put("\\ding{94}","\u273E")
        // map.put("\\ding{93}","\u273D")
        // map.put("\\ding{92}","\u273C")
        // map.put("\\ding{91}","\u273B")
        // map.put("\\ding{90}","\u273A")
        // map.put("\\ding{89}","\u2739")
        // map.put("\\ding{88}","\u2738")
        // map.put("\\ding{87}","\u2737")
        // map.put("\\ding{86}","\u2736")
        // map.put("\\ding{85}","\u2735")
        // map.put("\\ding{84}","\u2734")
        // map.put("\\ding{83}","\u2733")
        // map.put("\\ding{82}","\u2732")
        // map.put("\\ding{81}","\u2731")
        // map.put("\\ding{80}","\u2730")
        // map.put("\\ding{79}","\u272F")
        // map.put("\\ding{78}","\u272E")
        // map.put("\\ding{77}","\u272D")
        // map.put("\\ding{76}","\u272C")
        // map.put("\\ding{75}","\u272B")
        // map.put("\\ding{74}","\u272A")
        // map.put("\\ding{73}","\u2606")
        // map.put("\\ding{73}","\u2729")
        // map.put("\\ding{72}","\u2605")
        // map.put("\\ding{71}","\u2727")
        // map.put("\\ding{70}","\u2726")
        // map.put("\\ding{69}","\u2725")
        // map.put("\\ding{68}","\u2724")
        // map.put("\\ding{67}","\u2723")
        // map.put("\\ding{66}","\u2722")
        // map.put("\\ding{65}","\u2721")
        // map.put("\\ding{64}","\u2720")
        // map.put("\\ding{63}","\u271F")
        // map.put("\\ding{62}","\u271E")
        // map.put("\\ding{61}","\u271D")
        // map.put("\\ding{60}","\u271C")
        // map.put("\\ding{59}","\u271B")
        // map.put("\\ding{58}","\u271A")
        // map.put("\\ding{57}","\u2719")
        // map.put("\\ding{56}","\u2718")
        // map.put("\\ding{55}","\u2717")
        // map.put("\\ding{54}","\u2716")
        // map.put("\\ding{53}","\u2715")
        // map.put("\\ding{52}","\u2714")
        // map.put("\\ding{51}","\u2713")
        // map.put("\\ding{50}","\u2712")
        // map.put("\\ding{49}","\u2711")
        // map.put("\\ding{48}","\u2710")
        // map.put("\\ding{47}","\u270F")
        // map.put("\\ding{46}","\u270E")
        // map.put("\\ding{45}","\u270D")
        // map.put("\\ding{44}","\u270C")
        // map.put("\\ding{43}","\u261E")
        // map.put("\\ding{42}","\u261B")
        // map.put("\\ding{41}","\u2709")
        // map.put("\\ding{40}","\u2708")
        // map.put("\\ding{39}","\u2707")
        // map.put("\\ding{38}","\u2706")
        // map.put("\\ding{37}","\u260E")
        // map.put("\\ding{36}","\u2704")
        // map.put("\\ding{35}","\u2703")
        // map.put("\\ding{34}","\u2702")
        // map.put("\\ding{33}","\u2701")
        // map.put("\\ding{254}","\u27BE")
        // map.put("\\ding{253}","\u27BD")
        // map.put("\\ding{252}","\u27BC")
        // map.put("\\ding{251}","\u27BB")
        // map.put("\\ding{250}","\u27BA")
        // map.put("\\ding{249}","\u27B9")
        // map.put("\\ding{248}","\u27B8")
        // map.put("\\ding{247}","\u27B7")
        // map.put("\\ding{246}","\u27B6")
        // map.put("\\ding{245}","\u27B5")
        // map.put("\\ding{244}","\u27B4")
        // map.put("\\ding{243}","\u27B3")
        // map.put("\\ding{242}","\u27B2")
        // map.put("\\ding{241}","\u27B1")
        // map.put("\\ding{239}","\u27AF")
        // map.put("\\ding{238}","\u27AE")
        // map.put("\\ding{237}","\u27AD")
        // map.put("\\ding{236}","\u27AC")
        // map.put("\\ding{235}","\u27AB")
        // map.put("\\ding{234}","\u27AA")
        // map.put("\\ding{233}","\u27A9")
        // map.put("\\ding{232}","\u27A8")
        // map.put("\\ding{231}","\u27A7")
        // map.put("\\ding{230}","\u27A6")
        // map.put("\\ding{229}","\u27A5")
        // map.put("\\ding{228}","\u27A4")
        // map.put("\\ding{227}","\u27A3")
        // map.put("\\ding{226}","\u27A2")
        // map.put("\\ding{225}","\u27A1")
        // map.put("\\ding{224}","\u27A0")
        // map.put("\\ding{223}","\u279F")
        // map.put("\\ding{222}","\u279E")
        // map.put("\\ding{221}","\u279D")
        // map.put("\\ding{220}","\u279C")
        // map.put("\\ding{219}","\u279B")
        // map.put("\\ding{218}","\u279A")
        // map.put("\\ding{217}","\u2799")
        // map.put("\\ding{216}","\u2798")
        // map.put("\\ding{212}","\u2794")
        // map.put("\\ding{211}","\u2793")
        // map.put("\\ding{210}","\u2792")
        // map.put("\\ding{209}","\u2791")
        // map.put("\\ding{208}","\u2790")
        // map.put("\\ding{207}","\u278F")
        // map.put("\\ding{206}","\u278E")
        // map.put("\\ding{205}","\u278D")
        // map.put("\\ding{204}","\u278C")
        // map.put("\\ding{203}","\u278B")
        // map.put("\\ding{202}","\u278A")
        // map.put("\\ding{201}","\u2789")
        // map.put("\\ding{200}","\u2788")
        // map.put("\\ding{199}","\u2787")
        // map.put("\\ding{198}","\u2786")
        // map.put("\\ding{197}","\u2785")
        // map.put("\\ding{196}","\u2784")
        // map.put("\\ding{195}","\u2783")
        // map.put("\\ding{194}","\u2782")
        // map.put("\\ding{193}","\u2781")
        // map.put("\\ding{192}","\u2780")
        // map.put("\\ding{191}","\u277F")
        // map.put("\\ding{190}","\u277E")
        // map.put("\\ding{189}","\u277D")
        // map.put("\\ding{188}","\u277C")
        // map.put("\\ding{187}","\u277B")
        // map.put("\\ding{186}","\u277A")
        // map.put("\\ding{185}","\u2779")
        // map.put("\\ding{184}","\u2778")
        // map.put("\\ding{183}","\u2777")
        // map.put("\\ding{182}","\u2776")
        // map.put("\\ding{181}","\u2469")
        // map.put("\\ding{180}","\u2468")
        // map.put("\\ding{179}","\u2467")
        // map.put("\\ding{178}","\u2466")
        // map.put("\\ding{177}","\u2465")
        // map.put("\\ding{176}","\u2464")
        // map.put("\\ding{175}","\u2463")
        // map.put("\\ding{174}","\u2462")
        // map.put("\\ding{173}","\u2461")
        // map.put("\\ding{172}","\u2460")
        // map.put("\\ding{171}","\u2660")
        // map.put("\\ding{170}","\u2665")
        // map.put("\\ding{169}","\u2666")
        // map.put("\\ding{168}","\u2663")
        // map.put("\\ding{167}","\u2767")
        // map.put("\\ding{166}","\u2766")
        // map.put("\\ding{165}","\u2765")
        // map.put("\\ding{164}","\u2764")
        // map.put("\\ding{163}","\u2763")
        // map.put("\\ding{162}","\u2762")
        // map.put("\\ding{161}","\u2761")
        // map.put("\\ding{126}","\u275E")
        // map.put("\\ding{125}","\u275D")
        // map.put("\\ding{124}","\u275C")
        // map.put("\\ding{123}","\u275B")
        // map.put("\\ding{122}","\u275A")
        // map.put("\\ding{121}","\u2759")
        // map.put("\\ding{120}","\u2758")
        // map.put("\\ding{119}","\u25D7")
        // map.put("\\ding{118}","\u2756")
        // map.put("\\ding{117}","\u25C6")
        // map.put("\\ding{116}","\u25BC")
        // map.put("\\ding{115}","\u25B2")
        // map.put("\\ding{114}","\u2752")
        // map.put("\\ding{113}","\u2751")
        // map.put("\\ding{112}","\u2750")
        // map.put("\\ding{111}","\u274F")
        // map.put("\\ding{110}","\u25A0")
        // map.put("\\ding{109}","\u274D")
        // map.put("\\ding{108}","\u25CF")
        // map.put("\\ding{107}","\u274B")
        // map.put("\\ding{106}","\u274A")
        // map.put("\\ding{105}","\u2749")
        // map.put("\\ding{104}","\u2748")
        // map.put("\\ding{103}","\u2747")
        // map.put("\\ding{102}","\u2746")
        // map.put("\\ding{101}","\u2745")
        // map.put("\\ding{100}","\u2744")
        // map.put("\\ElsevierGlyph{E838}","\u233D")
        // map.put("\\ElsevierGlyph{E61B}","\u29B6")
        // map.put("\\ElsevierGlyph{E395}","\u2A10")
        // map.put("\\ElsevierGlyph{E381}","\u25B1")
        // map.put("\\ElsevierGlyph{E372}","\u29DC")
        // map.put("\\ElsevierGlyph{E36E}","\u2A55")
        // map.put("\\ElsevierGlyph{E30D}","\u2AEB")
        // map.put("\\ElsevierGlyph{E291}","\u2994")
        // map.put("\\ElsevierGlyph{E260}","\u29B5")
        // map.put("\\ElsevierGlyph{E25E}","\u2A34")
        // map.put("\\ElsevierGlyph{E25E}","\u2A35")
        // map.put("\\ElsevierGlyph{E25D}","\u2A2E")
        // map.put("\\ElsevierGlyph{E25C}","\u2A2D")
        // map.put("\\ElsevierGlyph{E25B}","\u2A2A")
        // map.put("\\ElsevierGlyph{E25A}","\u2A25")
        // map.put("\\ElsevierGlyph{E259}","\u2A3C")
        // map.put("\\ElsevierGlyph{E21D}","\u2933")
        // map.put("\\ElsevierGlyph{E21C}","\u2933")
        // map.put("\\ElsevierGlyph{E21A}","\u2936")
        // map.put("\\ElsevierGlyph{E219}","\u2937")
        // map.put("\\ElsevierGlyph{E215}","\u297D")
        // map.put("\\ElsevierGlyph{E214}","\u297C")
        // map.put("\\ElsevierGlyph{E212}","\u2905")
        // map.put("\\ElsevierGlyph{E211}","\u2927")
        // map.put("\\ElsevierGlyph{E210}","\u292A")
        // map.put("\\ElsevierGlyph{E20F}","\u2929")
        // map.put("\\ElsevierGlyph{E20E}","\u2928")
        // map.put("\\ElsevierGlyph{E20D}","\u2924")
        // map.put("\\ElsevierGlyph{E20C}","\u2923")
        // map.put("\\ElsevierGlyph{E20B}","\u2925")
        // map.put("\\ElsevierGlyph{E20A}","\u2926")
        // map.put("\\ElsevierGlyph{3019}","\u3019")
        // map.put("\\ElsevierGlyph{3018}","\u2985")
        // map.put("\\ElsevierGlyph{3018}","\u3018")
        // map.put("\\ElsevierGlyph{300B}","\u300B")
        // map.put("\\ElsevierGlyph{300A}","\u300A")
        // map.put("\\ElsevierGlyph{22C1}","\u22C1")
        // map.put("\\ElsevierGlyph{22C0}","\u22C0")
        // map.put("\\ElsevierGlyph{2275}","\u2275")
        // map.put("\\ElsevierGlyph{2274}","\u2274")
        // map.put("\\ElsevierGlyph{225F}","\u225F")
        // map.put("\\ElsevierGlyph{225A}","\u225A")
        // map.put("\\ElsevierGlyph{225A}","\u2A63")
        // map.put("\\ElsevierGlyph{2242}","\u2242")
        // map.put("\\ElsevierGlyph{2238}","\u2238")
        // map.put("\\ElsevierGlyph{2233}","\u2233")
        // map.put("\\ElsevierGlyph{2232}","\u2232")
        // map.put("\\ElsevierGlyph{21B3}","\u21B3")
        // map.put("\\ElsevierGlyph{2129}","\u2129")
        map["\\cyrchar{\\\'\\cyrk}"] = "\u045C"
        map["\\cyrchar{\\\'\\cyrg}"] = "\u0453"
        map["\\cyrchar{\\\'\\CYRK}"] = "\u040C"
        map["\\cyrchar{\\\'\\CYRG}"] = "\u0403"
        map["\\cyrchar\\textnumero"] = "\u2116"
        map["\\cyrchar\\cyrzhdsc"] = "\u0497"
        map["\\cyrchar\\cyrzh"] = "\u0436"
        map["\\cyrchar\\cyrzdsc"] = "\u0499"
        map["\\cyrchar\\cyrz"] = "\u0437"
        map["\\cyrchar\\cyryu"] = "\u044E"
        map["\\cyrchar\\cyryo"] = "\u0451"
        map["\\cyrchar\\cyryi"] = "\u0457"
        map["\\cyrchar\\cyryhcrs"] = "\u04B1"
        map["\\cyrchar\\cyrya"] = "\u044F"
        map["\\cyrchar\\cyry"] = "\u04AF"
        map["\\cyrchar\\cyrv"] = "\u0432"
        map["\\cyrchar\\cyrushrt"] = "\u045E"
        map["\\cyrchar\\cyruk"] = "\u0479"
        map["\\cyrchar\\cyru"] = "\u0443"
        map["\\cyrchar\\cyrtshe"] = "\u045B"
        map["\\cyrchar\\cyrthousands"] = "\u0482"
        map["\\cyrchar\\cyrtetse"] = "\u04B5"
        map["\\cyrchar\\cyrtdsc"] = "\u04AD"
        map["\\cyrchar\\cyrt"] = "\u0442"
        map["\\cyrchar\\cyrshha"] = "\u04BB"
        map["\\cyrchar\\cyrshch"] = "\u0449"
        map["\\cyrchar\\cyrsh"] = "\u0448"
        map["\\cyrchar\\cyrsftsn"] = "\u044C"
        map["\\cyrchar\\cyrsemisftsn"] = "\u048D"
        map["\\cyrchar\\cyrsdsc"] = "\u04AB"
        map["\\cyrchar\\cyrschwa"] = "\u04D9"
        map["\\cyrchar\\cyrs"] = "\u0441"
        map["\\cyrchar\\cyrrtick"] = "\u048F"
        map["\\cyrchar\\cyrr"] = "\u0440"
        map["\\cyrchar\\cyrpsi"] = "\u0471"
        map["\\cyrchar\\cyrphk"] = "\u04A7"
        map["\\cyrchar\\cyrp"] = "\u043F"
        map["\\cyrchar\\cyrotld"] = "\u04E9"
        map["\\cyrchar\\cyrot"] = "\u047F"
        map["\\cyrchar\\cyromegatitlo"] = "\u047D"
        map["\\cyrchar\\cyromegarnd"] = "\u047B"
        map["\\cyrchar\\cyromega"] = "\u0461"
        map["\\cyrchar\\cyro"] = "\u043E"
        map["\\cyrchar\\cyrnje"] = "\u045A"
        map["\\cyrchar\\cyrnhk"] = "\u04C8"
        map["\\cyrchar\\cyrng"] = "\u04A5"
        map["\\cyrchar\\cyrndsc"] = "\u04A3"
        map["\\cyrchar\\cyrn"] = "\u043D"
        map["\\cyrchar\\cyrmillions"] = "\u0489"
        map["\\cyrchar\\cyrm"] = "\u043C"
        map["\\cyrchar\\cyrlyus"] = "\u0467"
        map["\\cyrchar\\cyrlje"] = "\u0459"
        map["\\cyrchar\\cyrl"] = "\u043B"
        map["\\cyrchar\\cyrkvcrs"] = "\u049D"
        map["\\cyrchar\\cyrksi"] = "\u046F"
        map["\\cyrchar\\cyrkoppa"] = "\u0481"
        map["\\cyrchar\\cyrkhk"] = "\u04C4"
        map["\\cyrchar\\cyrkhcrs"] = "\u049F"
        map["\\cyrchar\\cyrkdsc"] = "\u049B"
        map["\\cyrchar\\cyrkbeak"] = "\u04A1"
        map["\\cyrchar\\cyrk"] = "\u043A"
        map["\\cyrchar\\cyrje"] = "\u0458"
        map["\\cyrchar\\cyrishrt"] = "\u0439"
        map["\\cyrchar\\cyriotlyus"] = "\u0469"
        map["\\cyrchar\\cyriote"] = "\u0465"
        map["\\cyrchar\\cyriotbyus"] = "\u046D"
        map["\\cyrchar\\cyrii"] = "\u0456"
        map["\\cyrchar\\cyrie"] = "\u0454"
        map["\\cyrchar\\cyri"] = "\u0438"
        map["\\cyrchar\\cyrhundredthousands"] = "\u0488"
        map["\\cyrchar\\cyrhrdsn"] = "\u044A"
        map["\\cyrchar\\cyrhdsc"] = "\u04B3"
        map["\\cyrchar\\cyrh"] = "\u0445"
        map["\\cyrchar\\cyrgup"] = "\u0491"
        map["\\cyrchar\\cyrghk"] = "\u0495"
        map["\\cyrchar\\cyrghcrs"] = "\u0493"
        map["\\cyrchar\\cyrg"] = "\u0433"
        map["\\cyrchar\\cyrf"] = "\u0444"
        map["\\cyrchar\\cyrery"] = "\u044B"
        map["\\cyrchar\\cyrerev"] = "\u044D"
        map["\\cyrchar\\cyre"] = "\u0435"
        map["\\cyrchar\\cyrdzhe"] = "\u045F"
        map["\\cyrchar\\cyrdze"] = "\u0455"
        map["\\cyrchar\\cyrdje"] = "\u0452"
        map["\\cyrchar\\cyrd"] = "\u0434"
        map["\\cyrchar\\cyrchvcrs"] = "\u04B9"
        map["\\cyrchar\\cyrchrdsc"] = "\u04B7"
        map["\\cyrchar\\cyrchldsc"] = "\u04CC"
        map["\\cyrchar\\cyrch"] = "\u0447"
        map["\\cyrchar\\cyrc"] = "\u0446"
        map["\\cyrchar\\cyrb"] = "\u0431"
        map["\\cyrchar\\cyrae"] = "\u04D5"
        map["\\cyrchar\\cyrabhha"] = "\u04A9"
        map["\\cyrchar\\cyrabhdze"] = "\u04E1"
        map["\\cyrchar\\cyrabhchdsc"] = "\u04BF"
        map["\\cyrchar\\cyrabhch"] = "\u04BD"
        map["\\cyrchar\\cyra"] = "\u0430"
        map["\\cyrchar\\CYRpalochka"] = "\u04C0"
        map["\\cyrchar\\CYRZHDSC"] = "\u0496"
        map["\\cyrchar\\CYRZH"] = "\u0416"
        map["\\cyrchar\\CYRZDSC"] = "\u0498"
        map["\\cyrchar\\CYRZ"] = "\u0417"
        map["\\cyrchar\\CYRYU"] = "\u042E"
        map["\\cyrchar\\CYRYO"] = "\u0401"
        map["\\cyrchar\\CYRYI"] = "\u0407"
        map["\\cyrchar\\CYRYHCRS"] = "\u04B0"
        map["\\cyrchar\\CYRYAT"] = "\u0462"
        map["\\cyrchar\\CYRYA"] = "\u042F"
        map["\\cyrchar\\CYRY"] = "\u04AE"
        map["\\cyrchar\\CYRV"] = "\u0412"
        map["\\cyrchar\\CYRUSHRT"] = "\u040E"
        map["\\cyrchar\\CYRUK"] = "\u0478"
        map["\\cyrchar\\CYRU"] = "\u0423"
        map["\\cyrchar\\CYRTSHE"] = "\u040B"
        map["\\cyrchar\\CYRTETSE"] = "\u04B4"
        map["\\cyrchar\\CYRTDSC"] = "\u04AC"
        map["\\cyrchar\\CYRT"] = "\u0422"
        map["\\cyrchar\\CYRSHHA"] = "\u04BA"
        map["\\cyrchar\\CYRSHCH"] = "\u0429"
        map["\\cyrchar\\CYRSH"] = "\u0428"
        map["\\cyrchar\\CYRSFTSN"] = "\u042C"
        map["\\cyrchar\\CYRSEMISFTSN"] = "\u048C"
        map["\\cyrchar\\CYRSDSC"] = "\u04AA"
        map["\\cyrchar\\CYRSCHWA"] = "\u04D8"
        map["\\cyrchar\\CYRS"] = "\u0421"
        map["\\cyrchar\\CYRRTICK"] = "\u048E"
        map["\\cyrchar\\CYRR"] = "\u0420"
        map["\\cyrchar\\CYRPSI"] = "\u0470"
        map["\\cyrchar\\CYRPHK"] = "\u04A6"
        map["\\cyrchar\\CYRP"] = "\u041F"
        map["\\cyrchar\\CYROTLD"] = "\u04E8"
        map["\\cyrchar\\CYROT"] = "\u047E"
        map["\\cyrchar\\CYROMEGATITLO"] = "\u047C"
        map["\\cyrchar\\CYROMEGARND"] = "\u047A"
        map["\\cyrchar\\CYROMEGA"] = "\u0460"
        map["\\cyrchar\\CYRO"] = "\u041E"
        map["\\cyrchar\\CYRNJE"] = "\u040A"
        map["\\cyrchar\\CYRNHK"] = "\u04C7"
        map["\\cyrchar\\CYRNG"] = "\u04A4"
        map["\\cyrchar\\CYRNDSC"] = "\u04A2"
        map["\\cyrchar\\CYRN"] = "\u041D"
        map["\\cyrchar\\CYRM"] = "\u041C"
        map["\\cyrchar\\CYRLYUS"] = "\u0466"
        map["\\cyrchar\\CYRLJE"] = "\u0409"
        map["\\cyrchar\\CYRL"] = "\u041B"
        map["\\cyrchar\\CYRKVCRS"] = "\u049C"
        map["\\cyrchar\\CYRKSI"] = "\u046E"
        map["\\cyrchar\\CYRKOPPA"] = "\u0480"
        map["\\cyrchar\\CYRKHK"] = "\u04C3"
        map["\\cyrchar\\CYRKHCRS"] = "\u049E"
        map["\\cyrchar\\CYRKDSC"] = "\u049A"
        map["\\cyrchar\\CYRKBEAK"] = "\u04A0"
        map["\\cyrchar\\CYRK"] = "\u041A"
        map["\\cyrchar\\CYRJE"] = "\u0408"
        map["\\cyrchar\\CYRIZH"] = "\u0474"
        map["\\cyrchar\\CYRISHRT"] = "\u0419"
        map["\\cyrchar\\CYRIOTLYUS"] = "\u0468"
        map["\\cyrchar\\CYRIOTE"] = "\u0464"
        map["\\cyrchar\\CYRIOTBYUS"] = "\u046C"
        map["\\cyrchar\\CYRII"] = "\u0406"
        map["\\cyrchar\\CYRIE"] = "\u0404"
        map["\\cyrchar\\CYRI"] = "\u0418"
        map["\\cyrchar\\CYRHRDSN"] = "\u042A"
        map["\\cyrchar\\CYRHDSC"] = "\u04B2"
        map["\\cyrchar\\CYRH"] = "\u0425"
        map["\\cyrchar\\CYRGUP"] = "\u0490"
        map["\\cyrchar\\CYRGHK"] = "\u0494"
        map["\\cyrchar\\CYRGHCRS"] = "\u0492"
        map["\\cyrchar\\CYRG"] = "\u0413"
        map["\\cyrchar\\CYRFITA"] = "\u0472"
        map["\\cyrchar\\CYRF"] = "\u0424"
        map["\\cyrchar\\CYRERY"] = "\u042B"
        map["\\cyrchar\\CYREREV"] = "\u042D"
        map["\\cyrchar\\CYRE"] = "\u0415"
        map["\\cyrchar\\CYRDZHE"] = "\u040F"
        map["\\cyrchar\\CYRDZE"] = "\u0405"
        map["\\cyrchar\\CYRDJE"] = "\u0402"
        map["\\cyrchar\\CYRD"] = "\u0414"
        map["\\cyrchar\\CYRCHVCRS"] = "\u04B8"
        map["\\cyrchar\\CYRCHRDSC"] = "\u04B6"
        map["\\cyrchar\\CYRCHLDSC"] = "\u04CB"
        map["\\cyrchar\\CYRCH"] = "\u0427"
        map["\\cyrchar\\CYRC"] = "\u0426"
        map["\\cyrchar\\CYRBYUS"] = "\u046A"
        map["\\cyrchar\\CYRB"] = "\u0411"
        map["\\cyrchar\\CYRAE"] = "\u04D4"
        map["\\cyrchar\\CYRABHHA"] = "\u04A8"
        map["\\cyrchar\\CYRABHDZE"] = "\u04E0"
        map["\\cyrchar\\CYRABHCHDSC"] = "\u04BE"
        map["\\cyrchar\\CYRABHCH"] = "\u04BC"
        map["\\cyrchar\\CYRA"] = "\u0410"
        map["\\cyrchar\\C"] = "\u030F"
        return Collections.unmodifiableMap(map)
    }

    private fun createLatexSubstitutionMap(): Map<String, String> {
        val map = LinkedHashMap<String, String>()
        map["---"] = "—"
        map["--"] = "–"
        map["^0"] = "\u2070"
        map["^1"] = "\u00B9"
        map["^2"] = "\u00B2"
        map["^3"] = "\u00B3"
        map["^4"] = "\u2074"
        map["^5"] = "\u2075"
        map["^6"] = "\u2076"
        map["^7"] = "\u2077"
        map["^8"] = "\u2078"
        map["^9"] = "\u2079"
        map["^+"] = "\u207A"
        map["^-"] = "\u207B"
        map["^="] = "\u207C"
        map["^A"] = "\u1D2C"
        map["^B"] = "\u1D2E"
        map["^D"] = "\u1D30"
        map["^E"] = "\u1D31"
        map["^F"] = "\u1D32"
        map["^G"] = "\u1D33"
        map["^H"] = "\u1D34"
        map["^I"] = "\u1D35"
        map["^J"] = "\u1D36"
        map["^K"] = "\u1D37"
        map["^L"] = "\u1D38"
        map["^M"] = "\u1D39"
        map["^N"] = "\u1D3A"
        map["^O"] = "\u1D3C"
        map["^P"] = "\u1D3E"
        map["^R"] = "\u1D3F"
        map["^T"] = "\u1D40"
        map["^U"] = "\u1D41"
        map["^W"] = "\u1D42"
        map["^a"] = "\u1D43"
        map["^b"] = "\u1D47"
        map["^c"] = "\u1D9C"
        map["^d"] = "\u1D48"
        map["^e"] = "\u1D49"
        map["^f"] = "\u1DA0"
        map["^g"] = "\u1D4D"
        map["^h"] = "\u02B0"
        map["^i"] = "\u2071"
        map["^j"] = "\u02B2"
        map["^k"] = "\u1D4F"
        map["^l"] = "\u02E1"
        map["^m"] = "\u1D50"
        map["^n"] = "\u207F"
        map["^o"] = "\u1D52"
        map["^p"] = "\u1D56"
        map["^r"] = "\u02B3"
        map["^s"] = "\u02E2"
        map["^t"] = "\u1D57"
        map["^u"] = "\u1D58"
        map["^v"] = "\u1D5B"
        map["^w"] = "\u02B7"
        map["^x"] = "\u02E3"
        map["^y"] = "\u02B8"
        map["^z"] = "\u1DBB"
        map["_0"] = "\u2080"
        map["_1"] = "\u2081"
        map["_2"] = "\u2082"
        map["_3"] = "\u2083"
        map["_4"] = "\u2084"
        map["_5"] = "\u2085"
        map["_6"] = "\u2086"
        map["_7"] = "\u2087"
        map["_8"] = "\u2088"
        map["_9"] = "\u2089"
        map["_+"] = "\u208A"
        map["_-"] = "\u208B"
        map["_="] = "\u208C"
        map["_a"] = "\u2090"
        map["_e"] = "\u2091"
        map["_i"] = "\u1D62"
        map["_j"] = "\u2C7C"
        map["_o"] = "\u2092"
        map["_r"] = "\u1D63"
        map["_u"] = "\u1D64"
        map["_v"] = "\u1D65"
        map["_x"] = "\u2093"
        map["{\'}"] = "\u2032"
        map["{\'\'}"] = "\u2033"
        map["{\'\'\'}"] = "\u2034"
        map["ij"] = "\u0133"
        map["fl"] = "\uFB02"
        map["fi"] = "\uFB01"
        // map.put("ffl","\uFB04") //Not supporte by the fonts of some devices
        // map.put("ffi","\uFB03")
        // map.put("ff","\uFB00")
        map["IJ"] = "\u0132"
        map["=:"] = "\u2255"
        map["<\\kern-0.58em("] = "\u2993"
        map[":="] = "\u2254"
        map[".."] = "\u2025"
        map[",,"] = "\u201E"
        map["\'n"] = "\u0149"
        map["\'\'\'\'"] = "\u2057"
        map["\\_"] = "_"
        map["\\{"] = "{"
        map["\\}"] = "}"
        map["\\$"] = ""
        map["{"] = ""
        map["}"] = ""
        return map
    }

    private fun createLatexExpansionMap(): Map<String, String> {
        val map = LinkedHashMap<String, String>()
        map["\\$\\\\backslash\\$"] = "\\\\"
        map["\\\\backslash "] = "\\\\"
        map["\\~([[a-z][A-Z]])"] = "\\~{$1}"
        map["\\`([[a-z][A-Z]])"] = "\\`{$1}"
        map["\\^([[a-z][A-Z]])"] = "\\^{$1}"
        map["\\=([[a-z][A-Z]])"] = "\\={$1}"
        map["\\.([[a-z][A-Z]])"] = "\\.{$1}"
        map["\\\'([[a-z][A-Z]])"] = "\\\'{$1}"
        map["\\\"([[a-z][A-Z]])"] = "\\\"{$1}"
        return map
    }

    /*Match all latex commands starting with a \ and include their argument in case they have one and also include some special cases from the LatexCommandMap. Note: we are not actually caturing ALL cases!*/
    val latexCommandRegex = "(\\\\cyrchar\\\\[A-Z]+|\\\\not\\\\[^ \\\\{};%#-]+|\\\\textfrac\\{[0-9]\\}\\{[0-9]\\}|\\\\frac\\{[0-9]\\}\\{[0-9]\\}|\\\\[^ a-zA-Z\\\\{};%#-]\\{[^ \\\\{};%#-]+\\}|\\\\[^ a-zA-Z\\\\{};%#-]|\\\\[a-zA-Z]+\\{[^ \\\\{};%#-]+\\}|\\\\[a-zA-Z]+)"

    val pattern = Pattern.compile(latexCommandRegex)

    fun parse(string: String): String {
        var str = string
        val foundCommandsList = ArrayList<String>()

        //We first treat the LaTeX commands that start with a \
        if (str.contains("\\")) {
            //Expand some commands to unify the LaTeX code
            for (entry in latexExpansionMap.entries) {
                str = str.replace(Regex(entry.key), entry.value)
            }

            //Find all appearing latex commands (see latexCommandRegex above)
            val matcher = pattern.matcher(str)
            while (matcher.find()) {
                foundCommandsList.add(str.substring(matcher.start(), matcher.end()))
            }

            //Expand all known commands
            for (command in foundCommandsList) {
                if (latexCommandMap.containsKey(command)) {
                    str = str.replace(command, latexCommandMap[command]!!)
                } else {
                    val indexOfArgument = command.indexOf('{')
                    if (indexOfArgument != -1) {
                        val commandWithoutArument = command.substring(0, indexOfArgument - 1)
                        if (latexCommandMap.containsKey(commandWithoutArument)) {
                            str = str.replace(commandWithoutArument, latexCommandMap[commandWithoutArument]!!)
                        }
                    }
                }
            }
        }

        //Finally make some replacements that are not commands starting with a \
        for (entry in latexSubstitutionMap.entries) {
            str = str.replace(entry.key, entry.value)
        }

        return str
    }
}
