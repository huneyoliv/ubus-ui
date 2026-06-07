fun testMask(it: String) {
    val clean = it.replace("-", "").uppercase().take(7)
    val formatted = if (clean.length > 3) {
        clean.substring(0, 3) + "-" + clean.substring(3)
    } else {
        clean
    }
    println("Input: $it -> Clean: $clean -> Formatted: $formatted")
}

testMask("ABC-123")
testMask("ABC-1234")
testMask("ABC-12345")
