package systems.carson

import java.util.*


fun <T> Optional<T>.ifNotPresent(var1: () -> Unit) {
    if (!this.isPresent) {
        var1.invoke()
    }
}

fun <A> MutableList<A>.mapInPlace(var1: (A) -> A){
    val i = this.listIterator()
    while(i.hasNext()){
        i.set(var1(i.next()))
    }
}

fun <A> MutableList<A>.mapInPlaceIndexed(var1: (A,Int) -> A){
    val i = this.listIterator()
    var index = 0
    while(i.hasNext()){
        i.set(var1(i.next(),index++))
    }
}

fun <A,B> MutableMap<A,B>.mapInPlace(var1: (Map.Entry<A,B>) -> B){
    val i = this.entries.iterator()
    while(i.hasNext()){
        val next = i.next()
        next.setValue(var1(next))
    }
}

fun <T,R> T.map(var1 :(T) -> R):R{
    return var1(this)
}

class Either<A,B>(private val a :A?, private val b :B?){
    companion object {
        fun <A,B> a(a :A):Either<A,B>{
            return Either(a = a,b = null)
        }
        fun <A,B> b(b :B):Either<A,B>{
            return Either(a = null,b = b)
        }
    }

    fun isA():Boolean = a != null
    fun isB():Boolean = b != null

    fun getA():A = a!!
    fun getB():B = b!!

    fun getAOptional():Optional<A> = Optional.ofNullable(a)
    fun getBOptional():Optional<B> = Optional.ofNullable(b)




}


class Split<T,R>(val t :T, val r: R){
    fun <E> mapT(mapper :(T) -> E):Split<E,R>{
        return Split(mapper(t),r)
    }
    fun runT(runner :(T) -> Unit):Split<T,R>{
        runner(t)
        return Split(t,r)
    }

    fun <E> mapR(mapper :(R) -> E):Split<T,E>{
        return Split(t,mapper(r))
    }
    fun runR(runner :(R) -> Unit):Split<T,R>{
        runner(r)
        return Split(t,r)
    }


    fun popT() :T{
        return t
    }

    fun popR() :R{
        return r
    }
    fun popPair():Pair<T,R>{
        return Pair(t,r)
    }
}

class Mapper<R,T>(private val initial :R, private val runner :(R) -> T){
    fun <E> map(mapper :(T) -> E):Mapper<R,E>{
        return Mapper(initial) { mapper(runner(it)) }
    }
    fun call():T = runner(initial)
    fun initial():R = initial
}

fun <T> T.mapper():Mapper<T,T> = Mapper(this) { it }

fun <T> T.split():Split<T,T> = Split(this,this)