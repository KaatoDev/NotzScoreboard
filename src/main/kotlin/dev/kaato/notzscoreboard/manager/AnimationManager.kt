package dev.kaato.notzscoreboard.manager

import dev.kaato.notzscoreboard.NotzScoreboard.Companion.af
import dev.kaato.notzscoreboard.utils.MessageUtil.set

object AnimationManager {
    val animations = hashMapOf<String, List<String>>()
    val animationsLive = hashMapOf<String, Int>()
    fun hasAnimation(animation: String) = animations.containsKey(animation)

    fun getAnimation(animation: String): String {
        val animLines = animations[animation] ?: return "[$animation]"
        var index = animationsLive[animation]?.inc() ?: 0
        val animLine = try {
            animLines[index]
        } catch (e: IndexOutOfBoundsException) {
            index = 0
            animLines[index]
        }
        animationsLive[animation] = index
        return animLine
    }

    fun autoAnimate(text: String): List<String> {
        val formatedText = set(text)
        var color = ""
        var onceColor = ""
        var colorIndex = 0

        val reg1 = Regex("""(?:<[^>]+>)+""")

        val lines = mutableListOf(formatedText)
        var altLine = formatedText

        do {
            if (altLine[0] == '<')
                reg1.find(altLine).let {
                    if (it != null) {
                        if (colorIndex == 0) {
                            if (!it.value.contains("\\"))
                                color = it.value
                            onceColor = it.value
                            colorIndex = it.range.count()
                        }

                        altLine = altLine.replaceFirst(it.value, "")
                    }
                }

            if (colorIndex > 0) colorIndex--
            if (colorIndex < 1 || color.contains("\\")) {
                color = ""
                colorIndex = 0
            }

            if (altLine.indexOf(" <") == 0) {
                color = ""
//                println(altLine)
                colorIndex = 0
            }

            altLine = altLine.substring(1) + onceColor + altLine[0]
            lines.add(color + altLine)

            if (onceColor.isNotBlank()) onceColor = ""

            if (lines.size == 25) return lines
        } while (lines.last() != formatedText)

        return lines
    }
    
    fun loadAnimations() {
        val normalList = af.get().getMapList("normal")
        val autoList = af.get().getMapList("auto")

        normalList.flatMap { it.entries.map { entry -> entry.key.toString() to entry.value as List<*> } }.forEach {
            animations[it.first] = it.second.map { str -> str.toString() }
        }

        autoList.flatMap { it.entries.map { entry -> entry.key.toString() to entry.value.toString() } }.forEach {
            animations[it.first] = autoAnimate(it.second)
        }
    }
}