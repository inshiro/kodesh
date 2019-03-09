package na.komi.kodesh

import android.text.SpannableStringBuilder
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import na.komi.kodesh.util.text.count
import org.junit.Ignore

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleUnitTest {
    @Ignore("Default test")
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    fun SpannableStringBuilder.deleteAll(str: String): SpannableStringBuilder{
        var i = 0
        while(i >=0 && indexOf(str,i).also { i = it } >= 0 )
            delete(i,i+str.length)
        return this
    }
    fun SpannableStringBuilder.spanBetween(begin: String, end: String, removeDelimiters: Boolean): SpannableStringBuilder {

        val e = indexOf(end)
        if (removeDelimiters)
            delete(e, e + end.length)

        val s = indexOf(begin)
        if (removeDelimiters)
            delete(s, s + begin.length)

        println(this.substring(s,e-begin.length))
        //println("s:$s ${this.get(s)}| e:$e |${this.get(if (e>=this.length) this.lastIndex else e-1)}")

        return this

    }

    fun SpannableStringBuilder.spanBetweenEach(begin: String, end: String, removeDelimiters: Boolean): SpannableStringBuilder {
        while (indexOf(begin) >= 0) spanBetween(begin, end, removeDelimiters)
        return this
    }

    fun SpannableStringBuilder.capitalizeUntil(search: String): SpannableStringBuilder {
        val found = indexOf(search)
        if (found>=0) {
            val newStr = substring(0, found).toUpperCase()
            delete(0, found)
            insert(0, newStr)
        }
        return this
    }

    @Ignore
    fun spanTest() {
        val str = SpannableStringBuilder("qwrte <<To the letter>>otc [and he]")
        str.spanBetweenEach("<<", ">>", true)

        //println(str.toString())
        assertEquals("qwrte To the letterotc [and he]", str.toString())
    }

    @Ignore
    fun insertTest() {
        val str = SpannableStringBuilder("<<To the letter>>")
        str.capitalizeUntil(" ")
        str.insert(str.indexOf("<<")+"<<".length+1, "\n")
        //println(str)
        assertEquals("<<T\nO the letter>>", str.toString())

    }

    @Ignore
    fun deleteSpanTest(){

        val a = SpannableStringBuilder("\n\t123wqd[d]123we4098<<oqiwue>>i[eowqij]jnh123\n\t\t\t")
        a.deleteAll("123").deleteAll("<<").deleteAll(">>").deleteAll("[").deleteAll("]").deleteAll("\n").deleteAll("\t")
        println(a)
        assertEquals("wqddwe4098oqiwueieowqijjnh", a.toString())
    }
    @Test
    fun redLetterTest(){

        val redText = "But the Lord said unto him, {Go thy way: for he is a chosen vessel unto me, to bear my name before the Gentiles, and kings, and the children of Israel:}"
        val verse = "\n\t12_But the Lord said unto him, Go thy way: for he is a chosen vessel unto me, to bear my name before the Gentiles, and kings, and the children of Israel:\n\t\t\t"

        val istart = 10
        val endStart = 20
        var start = if (istart < 0) 0 else istart
        var end = if (endStart > verse.length) verse.length else endStart
        val startCharIndex = redText.indexOf("{",istart-1)
        var startPrevSpaceIndex = redText.substring(0,startCharIndex).lastIndexOf(' ')

        var startNextSpaceIndex = redText.indexOf(" ",startCharIndex+1)
        if(startNextSpaceIndex < 0) startNextSpaceIndex = redText.length
        val startWord = redText.substring(startCharIndex,startNextSpaceIndex).replace("{","").replace("}","")

        val endCharIndex = redText.indexOf("}",endStart-1)
        val endStr = redText.substring(startCharIndex,endCharIndex)
        var endPrevSpaceIndex = endStr.lastIndexOf(' ')
        if(endPrevSpaceIndex < 0) endPrevSpaceIndex = 0
        val endWord = endStr.substring(endPrevSpaceIndex).replace(" ","").replace("{","").replace("}","")

        val newStart = verse.indexOf(startWord, startPrevSpaceIndex)
        val newEnd = verse.indexOf(endWord, endPrevSpaceIndex)

        if (newStart >=0 ) start = newStart
        if (newEnd >0 ) end = newEnd+endWord.length

        println(startCharIndex)
        println(startPrevSpaceIndex)
        println(startWord)
        println()

        println(endWord)
        println(startPrevSpaceIndex)
        println(verse.substring(newStart,newEnd))
        println(verse.substring(start,end))

    }

}
