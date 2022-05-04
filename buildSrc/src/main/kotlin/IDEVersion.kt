/**
 * Denotes support IDE product type
 */
enum class Product(val value: String) {
    IC("IC"), UI("UI"), AS("AS")
}

data class IDEVersion(
    val product: Product,
    val year: Int,
    val release: Int
) {
    init {
        require(year >= 2021) { "Invalid year, was $year" }
        require(release > 0) { "Invalid release number, was $release" }
    }
}

private val IDEVersionComparator = object : Comparator<IDEVersion> {
    override fun compare(
        o1: IDEVersion,
        o2: IDEVersion
    ): Int = when (val cmp = o1.year.compareTo(o2.year)) {
        0 -> o1.release.compareTo(o2.release)
        else -> cmp
    }
}

fun Iterable<IDEVersion>.latest(): IDEVersion = sortedWith(IDEVersionComparator).last()

fun Iterable<IDEVersion>.oldest(): IDEVersion = sortedWith(IDEVersionComparator).first()

val IDEVersion.versionName: String get() = "${product.value}-$year.$release"

val IDEVersion.buildNumber: String get() = "${year % 100}$release"
