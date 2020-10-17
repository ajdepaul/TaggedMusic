package ajdepaul.taggedmusiccli.util

import java.io.File

/** Converts this string into a list of arguments. Returns null if the quotation formatting is bad. */
internal fun String.toArgs(): List<String>? {
    var args = listOf<String>()
    var inQuotes = false
    var textInQuotes = ""

    for (word in this.split(" ").filter { it != "" }) {

        if (!inQuotes) when {
            word.first() == '"' -> {
                inQuotes = true
                textInQuotes += word.drop(1)
            }
            word.contains("\"") -> return null
            else -> args += word

        } else when {
            word.last() == '"' -> {
                inQuotes = false
                args += "$textInQuotes ${word.dropLast(1)}"
            }
            word.contains("\"") -> return null
            else -> textInQuotes += word
        }
    }

    return if (inQuotes) null else args
}

/**
 * Replaces tilde with system home for the specified strings.
 * @param indices the indices of the list to transform (all indices transformed if left empty)
 * @return the same list
 */
internal fun List<String>.replaceSysHome(vararg indices: Int): List<String> {
    val itr = if (indices.isEmpty()) 0..this.lastIndex else indices.toList()
    for (i in itr)
        this.getOrNull(i)?.replaceFirst("~${File.separator}", System.getProperty("user.home") + File.separator)
    return this
}

/** Prints a bad arguments message. */
internal fun badArgs(command: String) = println("Bad arguments. Use \"help $command\" for correct usage.")

/**
 * Repeatedly asks [question] until a response from [trueResponses] or [falseResponses] is given.
 * @return true if the response is in [trueResponses], false if the response is in [falseResponses], and null if EOF
 */
internal fun binaryQuestion(question: String, trueResponses: List<String>, falseResponses: List<String>): Boolean? {
    print(question)
    val input = readLine()?.trim()?.toLowerCase()
    return when {
        trueResponses.contains(input) -> true
        falseResponses.contains(input) -> false
        input == null -> null
        else -> binaryQuestion(question, trueResponses, falseResponses)
    }
}

/**
 * Repeatedly asks [question] until a response from [responses] is given.
 * @return the user's response or null if EOF
 */
internal fun askUntilCorrect(question: String, vararg responses: String): String? {
    print("$question (")
    for (response in responses) if (response != responses.last()) print("$response, ")
    print("${responses.last()}): ")

    val input = readLine()?.trim()?.toLowerCase()
    return when {
        responses.contains(input) -> input
        input == "" || input == null -> null
        else -> askUntilCorrect(question, *responses)
    }
}

/**
 * Repeatedly asks the user for a well formed path.
 * @return the user's path response or null if EOF
 */
internal fun pathResponse(): String? {
    var path = readLine()
            ?.replaceFirst("~${File.separator}", System.getProperty("user.home") + File.separator)
            ?: return null

    if (path.first() == '"' && path.last() == '"') path = path.drop(1).dropLast(1)

    return if (path.count {it == '"'} == 0) path else {
        println("Bad path format. Either surround the path in quotes, or don't use any.")
        pathResponse()
    }
}