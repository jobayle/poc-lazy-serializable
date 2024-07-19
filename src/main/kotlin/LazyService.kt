package org.example

import org.example.lazy.ref
import org.example.lazy.refOf

class LazyService {

    companion object {
        val INSTANCE = LazyService()
    }
    
    fun createLazyComposite(): LazyComposite {
        return LazyComposite(
            LazyCompositeInitializers(
                ref { "Hello" },
                ref { MyType("Hi", 2) },
                ref { listOf("foo", "bar", "baz") },
                ref { listOf(MyType("abc", 1), MyType("xyz", 26)) }
            )
        )
    }
    
    fun createNotLazyComposite(): LazyComposite {
        return LazyComposite(
            LazyCompositeInitializers(
                refOf( "Hello" ),
                refOf( MyType("Hi", 2) ),
                refOf( listOf("foo", "bar", "baz") ),
                refOf( listOf(MyType("abc", 1), MyType("xyz", 26)) )
            )
        )
    }
    
}
