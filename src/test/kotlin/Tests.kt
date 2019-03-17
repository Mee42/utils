
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import systems.carson.*
import java.lang.NullPointerException
import java.lang.StringBuilder
import java.util.*


class Tests {

    @Test
    fun hashTest() {
        assertAll(
            { assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824".toUpperCase(),"hello".hash(HashType.SHA256).string) },
            { assertEquals("59e1748777448c69de6b800d7a33bbfb9ff1b463e44354c3553bcdb9c666fa90125a3c79f90397bdf5f6a13de828684f".toUpperCase(),"hello".hash(HashType.SHA384).string) },
            { assertEquals("9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca72323c3d99ba5c11d7c7acc6e14b8c5da0c4663475c2e5c3adef46f73bcdec043".toUpperCase(),"hello".hash(HashType.SHA512).string) }
        )
    }

    @Test
    fun op(){
        var x = false
        var op = Optional.empty<Int>()
        op.ifNotPresent { x = true }
        assert(x)
        x = false
        op = Optional.of(1)
        op.ifPresent { x = true }
        assert(x)
    }

    @Test
    fun mapInPlaces(){
        val map = mutableListOf(1,2,3,4,5)
        map.mapInPlace { it * 2 }
        assertEquals(listOf(2,4,6,8,10),map)
    }

    @Test
    fun mapInPlaces2(){
        val map = mutableListOf(5,4,3,2,1)
        map.mapInPlaceIndexed { value, i -> value * i }
        assertEquals(listOf(0,4,6,6,4),map)
        //                  0 1 2 3 4
        //                  5 4 3 2 1
    }

    @Test
    fun mapInPlaceMap(){
        val map = mutableMapOf(1 to 1, 2 to 2, 3 to 3, 4 to 4, 5 to 5)
        map.mapInPlace { it.value * 2 }
        assertEquals(mapOf(1 to 2,2 to 4,3 to 6, 4 to 8, 5 to 10),map)
        map.mapInPlace { it.key }
        assertEquals(mapOf(1 to 1, 2 to 2, 3 to 3, 4 to 4, 5 to 5),map)
    }

    @Test
    fun mapGeneral(){
        assertEquals(2,(1).map { it * 2 })
        assertEquals("hello_world",("hello").map { it + "_" + "world"})
    }

    @Test
    fun mapConc(){
        class Box(var i :Int)
        val x = Box(2)
        assert(x.map { Box(it.i + 10) }.map { it.i == 12 })
        assertEquals(2,x.i)
    }

    @Test
    fun mapAsyc(){
        class Box(var i :Int =  0)
        val x = Box(10)
        val start = System.nanoTime()
        x.map { it.i = 20 }
        val time = (System.nanoTime() - start)/1_000_000
        assert(time < 1000) { "failed to complete in decent time (1000 ms)" }
    }

    @Test
    fun either(){
        val a = Either.a<Int,Int>(1)
        val b = Either.b<Int,Int>(1)
        assert(a.isA())
        assert(a.isB().not())

        assert(b.isB())
        assert(b.isA().not())
    }
    @Test
    fun either2(){
        val a = Either.a<Int,Int>(1)
        val b = Either.b<Int,Int>(1)
        var failed = false
        try{ a.getA() }catch(e :NullPointerException){ failed = true }
        assert(failed.not())

        try{ b.getB() }catch(e :NullPointerException){ failed = true }
        assert(failed.not())

        failed = false
        try{ a.getB() }catch(e :NullPointerException){ failed = false }
        assert(failed.not())

        failed = false
        try{ b.getA() }catch(e :NullPointerException){ failed = false }
        assert(failed.not())
    }

    @Test
    fun eitherOp(){
        val a = Either.a<Int,Int>(1)
        val b = Either.b<Int,Int>(1)
        assertEquals(Optional.of(1),a.getAOptional())
        assertEquals(Optional.empty<Int>(),a.getBOptional())
        assertEquals(Optional.of(1),b.getBOptional())
        assertEquals(Optional.empty<Int>(),b.getAOptional())
    }

    @Test
    fun quickTest1(){
        var test = ""
        val a = StringBuilder("_")
        val x = "hello".split()
            .mapR { test+="a";it.length }
            .mapR { test+="b";a.append(it) }
            .mapR { test+="c";"world" }
            .mapT { test+="d";a.append(it) }
            .popR()
        a.append(x)
        assertEquals("abcd",test)
        assertEquals("_5helloworld",a.toString())
    }

    @Test
    fun quickTest1point0(){
        var test = ""
        val a = StringBuilder("_")
        val x = "hello".split()
            .mapT { test+="a";it.length }
            .mapT { test+="b";a.append(it) }
            .mapT { test+="c";"world" }
            .mapR { test+="d";a.append(it) }
            .popT()
        a.append(x)
        assertEquals("abcd",test)
        assertEquals("_5helloworld",a.toString())
    }


    @Test
    fun quickTest2(){
        var test = ""
        var a = "_"
        val x = "hello".split()
            .mapR { test+="a";it.length }
            .mapR { test+="b";a += it }
            .mapR { test+="c";"world" }
            .mapT { test+="d";a += it }
            .popR()
        a += x
        assertEquals("abcd",test)
        assertEquals("_5helloworld",a)
    }

    @Test
    fun quickTest3(){
        data class Box(val x :Int){
            fun next() = Box(x + 5)
        }
        val box = Box(10)
        val newBox = box.split()
            .mapR { it.next().next().next() }
            .popT()
        assertEquals(box,newBox)
    }

    @Test
    fun quickTest4(){
        data class Box(var x :Int)
        val box = Box(20)
        val split = Box(10).split().runR { it.x = 20 }
        assertEquals(split.r,split.t)
        assertEquals(box,split.popT())
    }

    @Test
    fun quickTest5(){
        data class Box(var x :Int)
        val box = Box(20)
        val new = Box(10).split().runR { it.x = 20 }.popT()
        assertEquals(box,new)
    }

    @Test
    fun quickTest6(){
        data class Box(val x :Int)
        val new = Box(10).split().mapT { it.x + 5 }.mapR { Box(it.x + 10) }.mapT { Box(it + 5) }
        val r = new.popPair()
        assertEquals(r.first,r.second)
        assertEquals(Box(20),r.first)
        assertEquals(Box(20),r.second)
    }

    @Test
    fun mapperTest1(){
        assertEquals(10,(5).mapper().map { it + 5 }.call())
    }
    @Test
    fun mapperTest2(){
        var x = false
        "hello".mapper().map { x = true }
        assertEquals(false, x)
    }

    @Test
    fun mapperTest3(){
        var x = false
        val y = "hello".mapper().map { x = true }
        y.call()
        assertEquals(true,x)
    }

    @Test
    fun mapperTest4(){
        var x = false
        var x1 = false
        val y = "hello".mapper()
            .map { x = true }
            .map {
                if(x){
                    x1 = true
                }
            }
        assert(!x1)
        assert(!x)
        y.call()
        assert(x)
        assert(x1)
    }
    @Test
    fun mapperTest5(){
        var i = false
        val y = "hello".mapper().map { i = true;"world" }.initial()
        assertEquals("hello",y)
        assert(!i)
    }
    @Test
    fun mapperTest6(){
        var i = false
        val y = "world".mapper().map { i = true;"world" }.call()
        assertEquals("hello",y)
        assert(i)
    }



}
