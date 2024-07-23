package org.example.lazy

import kotlin.reflect.KProperty

/**
 * Ref holder with lazy initialization, but read/write.
 * Inspired by kotlin's Lazy in stdlib.
 *
 * @param V output type, type of value hold by this ref
 */
interface LazyRef<V> {
    /** A lazy initialized value, or a value, or an empty ref that can be set. */
    var value: V
}

/** This ref is used to implement the lateinit pattern */
internal object UNINITIALIZED_VALUE

/** This delegate is initialised when the getter method is called (pattern lazy). */
internal class LazyRefImpl<V>(private val initializer: (() -> V)): LazyRef<V> {

    private var _value: Any? = UNINITIALIZED_VALUE

    override var value: V
        get() {
            if (_value === UNINITIALIZED_VALUE) {
                _value = initializer()
            }
            @Suppress("UNCHECKED_CAST")
            return _value as V
        }
        set(o) {
            _value = o
        }

    override fun toString(): String {
        return if (_value === UNINITIALIZED_VALUE) "Lazy ref is no initialized yet" else _value.toString()
    }

}

/** This delegate is initialised later, using the setter method. */
internal class LateinitRefValueImpl<V>: LazyRef<V> {

    private var _value: Any? = UNINITIALIZED_VALUE

    override var value: V
        get() {
            if (_value === UNINITIALIZED_VALUE)
                throw UninitializedPropertyAccessException("lateinit property has not been initialized")
            @Suppress("UNCHECKED_CAST")
            return _value as V
        }
        set(o) {
            _value = o
        }

}

/** This delegate is initialized at creation. */
internal class RefValueImpl<V>(override var value: V) : LazyRef<V> {

    override fun toString(): String {
        return value.toString()
    }

}

// Helper functions
fun <V> refOf(value: V): LazyRef<V> = RefValueImpl(value)
fun <V> ref(initializer: () -> V): LazyRef<V> = LazyRefImpl(initializer)
fun <V> lateinitRef(): LazyRef<V> = LateinitRefValueImpl()

// Implementation of Delegate
@Suppress("kotlin:S1172")
inline operator fun <V> LazyRef<V>.getValue(thisRef: Any?, property: KProperty<*>): V = value
@Suppress("kotlin:S1172")
inline operator fun <V> LazyRef<V>.setValue(thisRef: Any?, property: KProperty<*>, v: V) { value = v }
