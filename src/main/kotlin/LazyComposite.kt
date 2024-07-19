package org.example

import com.fasterxml.jackson.annotation.JsonCreator
import org.example.lazy.*

/**
 * This class implements the lazy composite pattern as done in LetReco Phoenix.
 */
class LazyComposite(initializers: LazyCompositeInitializers) {
    var lazyStr: String by initializers.strInitializer
    var lazyType: MyType by initializers.typeInitializer
    var lazyNull: String? by initializers.nullInitializer
    var lazyList: List<String> by initializers.listInitializer
    var lazyTypeList: List<MyType> by initializers.listTypeInitializer

    /** Secondary ctor for deserialization */
    @JsonCreator
    constructor() : this (
        LazyCompositeInitializers(
            lateinitRef(),
            lateinitRef(),
            lateinitRef(),
            lateinitRef(),
            lateinitRef(),
        )
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LazyComposite

        if (lazyStr != other.lazyStr) return false
        if (lazyType != other.lazyType) return false
        if (lazyNull != other.lazyNull) return false
        if (!lazyList.containsAll(other.lazyList)) return false
        if (!lazyTypeList.containsAll(other.lazyTypeList)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lazyStr.hashCode()
        result = 31 * result + lazyType.hashCode()
        result = 31 * result + lazyNull.hashCode()
        result = 31 * result + lazyList.hashCode()
        result = 31 * result + lazyTypeList.hashCode()
        return result
    }
}

/**
 * A non-basic type.
 */
data class MyType (
    val name: String,
    val size: Long,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MyType

        if (name != other.name) return false
        if (size != other.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + size.hashCode()
        return result
    }
}

/**
 * Initializers for various use cases.
 */
data class LazyCompositeInitializers (
    // Basic type
    val strInitializer: LazyRef<String>,
    // Complex type
    val typeInitializer: LazyRef<MyType>,
    // Nullable type
    val nullInitializer: LazyRef<String?>,
    // Collection of basic type
    val listInitializer: LazyRef<List<String>>,
    // Collection of complex type
    val listTypeInitializer: LazyRef<List<MyType>>,
)
