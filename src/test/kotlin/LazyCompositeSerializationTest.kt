import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.example.LazyComposite
import org.example.LazyService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.assertContains

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(Lifecycle.PER_CLASS)
class LazyCompositeSerializationTest {

    private val mapper = jacksonObjectMapper()
    private val svc = LazyService.INSTANCE

    private val lazyInstance = svc.createLazyComposite()
    private val notLazyInstance = svc.createNotLazyComposite()

    private lateinit var json: String

    @Order(1)
    @Test
    fun testEquality() {
        assertTrue(lazyInstance == notLazyInstance)
    }

    @Order(10)
    @Test
    fun testSerializeLazy() {
        assertDoesNotThrow {
            json = mapper.writeValueAsString(lazyInstance)
        }

        println("Serialization result:")
        println(json)
        println("################\n")

        assertContains(json, lazyInstance.lazyStr)
        assertContains(json, lazyInstance.lazyType.name)
        lazyInstance.lazyList.forEach { assertContains(json, it) }
        lazyInstance.lazyTypeList.forEach { assertContains(json, it.name) }
    }

    @Order(11)
    @Test
    fun testDeserializeLazy() {
        val instance = mapper.readValue<LazyComposite>(json)
        assertEquals(lazyInstance, instance)
    }

    // ----

    @Order(20)
    @Test
    fun testSerializeNotLazy() {
        json = ""
        assertDoesNotThrow {
            json = mapper.writeValueAsString(notLazyInstance)
        }

        println("Serialization result:")
        println(json)
        println("################\n")

        assertContains(json, notLazyInstance.lazyStr)
        assertContains(json, notLazyInstance.lazyType.name)
        notLazyInstance.lazyList.forEach { assertContains(json, it) }
        notLazyInstance.lazyTypeList.forEach { assertContains(json, it.name) }

    }

    @Order(21)
    @Test
    fun testDeserializeNotLazy() {
        val instance = mapper.readValue<LazyComposite>(json)
        assertEquals(notLazyInstance, instance)
    }

}
