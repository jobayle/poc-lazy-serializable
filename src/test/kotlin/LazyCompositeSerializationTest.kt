import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.netty.buffer.ByteBuf
import org.example.LazyComposite
import org.example.LazyService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.TestMethodOrder
import org.redisson.codec.Kryo5Codec
import kotlin.test.assertContains
import kotlin.test.assertIs

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(Lifecycle.PER_CLASS)
class LazyCompositeSerializationTest {

    private val svc = LazyService.INSTANCE
    private val lazyInstance = svc.createLazyComposite()
    private val notLazyInstance = svc.createNotLazyComposite()
    private lateinit var deserializedInstance: LazyComposite

    // JACKSON SERIALIZATION (use case: json payload of REST API)

    private val mapper = jacksonObjectMapper()
    private lateinit var json: String

    @Order(1)
    @Test
    fun testEquality() {
        assertTrue(lazyInstance == notLazyInstance)
    }

    @Order(10)
    @Test
    fun testSerializeJacksonLazy() {
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
    fun testSerializeJacksonNotLazy() {
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

    @Order(12)
    @Test
    fun testDeserializeJackson() {
        assertDoesNotThrow {
            deserializedInstance = mapper.readValue<LazyComposite>(json)
        }
        assertEquals(lazyInstance, deserializedInstance)
    }

    @Order(13)
    @Test
    fun testSerializeJacksonDeserializedInstance() {
        json = ""
        assertDoesNotThrow {
            json = mapper.writeValueAsString(deserializedInstance)
        }

        println("Serialization result:")
        println(json)
        println("################\n")

        assertContains(json, notLazyInstance.lazyStr)
        assertContains(json, notLazyInstance.lazyType.name)
        notLazyInstance.lazyList.forEach { assertContains(json, it) }
        notLazyInstance.lazyTypeList.forEach { assertContains(json, it.name) }
    }

    // KRYO SERIALIZATION (use case: redis via redisson)

    private val kryo = Kryo()
    init {
        // TODO see how Redisson configures this (de)serializer
        // see: https://github.com/redisson/redisson/blob/master/redisson/src/main/java/org/redisson/codec/Kryo5Codec.java
        kryo.isRegistrationRequired = false
    }

    private lateinit var kryoBin: ByteArray

    @Order(20)
    @Test
    fun testSerializeKryoLazy() {
        assertDoesNotThrow {
            val output = Output(1024)
            kryo.writeObject(output, lazyInstance)
            kryoBin = output.toBytes()
        }
        assertTrue(kryoBin.isNotEmpty())
    }

    @Order(21)
    @Test
    fun testDeserializeKryo() {
        assertDoesNotThrow {
            deserializedInstance = kryo.readObject(Input(kryoBin), LazyComposite::class.java)
        }
        assertEquals(lazyInstance, deserializedInstance)
    }

    // REDISSON Kryo CODEC

    private val codec = Kryo5Codec()
    private lateinit var redissonBuf: ByteBuf

    @Order(30)
    @Test
    fun testSerializeRedissonLazy() {
        assertDoesNotThrow {
            kryoBin = ByteArray(2048)
            redissonBuf = codec.valueEncoder.encode(lazyInstance)
        }
        assertTrue(kryoBin.isNotEmpty())
    }

    @Order(31)
    @Test
    fun testDeserializeRedisson() {
        assertDoesNotThrow {
            val decoded = codec.valueDecoder.decode(redissonBuf, null)
            assertNotNull(decoded)
            deserializedInstance = assertIs<LazyComposite>(decoded)
        }
        assertEquals(lazyInstance, deserializedInstance)
    }
}
