package na.komi.kodesh.ui.main

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.recyclerview_child_content_main.*
import kotlinx.coroutines.*
import na.komi.kodesh.Application
import na.komi.kodesh.Prefs
import na.komi.kodesh.R
import na.komi.kodesh.model.Bible
import na.komi.kodesh.util.page.Fonts
import na.komi.kodesh.util.page.Formatting
import na.komi.kodesh.util.page.MySpannableFactory
import na.komi.kodesh.util.text.spanBetweenEach
import na.komi.kodesh.util.text.withSpan
import na.komi.kodesh.util.tryy
import na.komi.kodesh.ui.widget.LayoutedTextView
import na.komi.kodesh.ui.widget.LeadingMarginSpan3
import kotlin.coroutines.CoroutineContext


class MainChildAdapter(
    private val vm: MainViewModel
) :
    RecyclerView.Adapter<MainChildAdapter.ViewHolder>(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private val job = SupervisorJob()
    private val list = mutableListOf<Bible>()
    private val cleanList by lazy { mutableListOf<String>()  }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = if (position == 0) 0.1.toLong() else list[position].id.toLong()

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recyclerview_child_content_main,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    fun setList(newList: MutableList<Bible>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
        coroutineContext.cancelChildren()
        /*launch(Dispatchers.IO) {
            cleanList.clear()
            var text:String
            list.mapTo(cleanList) {
                text = it.verseText!!.replace(periscopeDelimeter,"") .replace(italicDelimeter,"")
                if (vm.kjvStyling) {
                    val kjv = Formatting.kjvList[it.id - 1]
                    text =  Formatting.diffText(text, kjv)
                }
                text
            }
        }*/
    }

    private val spannableFactory by lazy { MySpannableFactory() }

    private val onlyAplhaNumericRegex by lazy { """\p{Alnum}""".toRegex() }
    private val periscopeDelimeter by lazy { """\\<.*?\\>""".toRegex() }
    private val italicDelimeter by lazy { """\\[|\\]""".toRegex() }

    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        var verseView: LayoutedTextView = child_tv_item
        private var origLeftPadding = -1
        private var origRightPadding = -1
        private var origTopPadding = -1
        private val leadingMarginSpan by lazy { LeadingMarginSpan3() }

        var p: Paint? = null
        var c = Canvas()
        private val redColor by lazy {
            val typedValue = TypedValue()
            verseView.context.theme.resolveAttribute(R.attr.redLetterColor, typedValue, true)
            typedValue.data
        }

        init {
            verseView.typeface = Fonts.GentiumPlus_R
            verseView.setTextSize(TypedValue.COMPLEX_UNIT_SP, Prefs.mainFontSize)
            verseView.setSpannableFactory(spannableFactory)
        }

        private fun sp(px: Float) = px / Application.instance.applicationContext.resources.displayMetrics.scaledDensity

        fun bind(item: Bible, position: Int) {
            //verseView.futureSet("${item.verseId!!} ${item.verseText!!}")
            //verseView.text = "${item.verseId!!} ${item.verseText!!}"

            //log d "position: $position | ${item.verseId!!} ${item.verseText!!}"
            if (origLeftPadding == -1)
                origLeftPadding = verseView.paddingLeft

            if (origRightPadding == -1)
                origRightPadding = verseView.paddingRight

            if (sp(verseView.textSize) != TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    Prefs.mainFontSize,
                    verseView.context.resources.displayMetrics
                )
            ) {
                if (vm.showDropCap && position == 0) {
                } else verseView.setTextSize(TypedValue.COMPLEX_UNIT_SP, Prefs.mainFontSize)
            }

            if (verseView.paddingLeft != origLeftPadding)
                verseView.updatePadding(origLeftPadding)

            if (verseView.paddingRight != origRightPadding)
                verseView.updatePadding(right = origRightPadding)

            if (verseView.paddingTop != origTopPadding)
                verseView.updatePadding(top = origTopPadding)


            val vt = if (vm.kjvStyling) {
                val kjv = Formatting.kjvList[item.id - 1]
                Formatting.diffText(item.verseText!!, kjv)
            } else item.verseText!!

            val finalText = SpannableStringBuilder()
            /**
             * numbers
             * red letter
             */

            var verseNumber = ""
            if (vm.showVerseNumbers) {
                run {
                    if (position == 0 && vm.showDropCap) return@run
                    finalText.withSpan(RelativeSizeSpan(1f / 1.9f)) {
                        append("${item.verseId!!}")
                    }
                    finalText.withSpan(ForegroundColorSpan(Formatting.NumColor))
                    finalText.append("  ")
                    verseNumber = finalText.toString()
                }
            }

            @Suppress("RedundantIf")
            if (vm.showDropCap && position == 0) {
                verseView.showDropCap = true
                verseView.dropCapText = vt.take(1)
                leadingMarginSpan.setMargin(verseView.tp.measureText(verseView.dropCapText).toInt() + verseView.MARGIN_PADDING)
                finalText.withSpan(leadingMarginSpan) {
                    append(vt.drop(1))
                }
                verseView.post {
                    if (verseView.lineCount == 1)
                        verseView.updatePadding(top = 200)
                    else
                        verseView.updatePadding(top = 0)
                }
            } else {

                verseView.showDropCap = false
                verseView.dropCapText = ""

                finalText.append(vt)
            }

            // Italics
            tryy {
                finalText.spanBetweenEach(StyleSpan(Typeface.ITALIC), "[", "]", true)
            }

            // Red Letters
            if (vm.showRedLetters && item.bookId!! >= 40) {
                //d{"Ran block"}
                val rIdx =
                    Formatting.redLetterList.indexOfFirst { str -> str.indexOf("${item.bookId}\t${item.chapterId}\t${item.verseId}\t") >= 0 }
                if (rIdx >= 0) {
                    Formatting.redLetterPositions(rIdx) { s, e, c ->
                        val it = Formatting.redLetterList[rIdx]
                        val redText =
                            it.substring(it.lastIndexOf('\t') + 1)//.replace("{","").replace("}","")
                        //val verseText = verse.toString().replace("\n","").replace("\t","").replace(ZSPACE,"")

                        // Safety bounds
                        val zSpace = finalText.length//verse.indexOf(ZSPACE)
                        var start = if (s < 0) 0 else s
                        if (vm.showVerseNumbers && position > 1 && c <= 1) start += verseNumber.length
                        var end = if (e > finalText.length) finalText.length else e
                        end += if (vm.showVerseNumbers && position > 1 && end + verseNumber.length <= finalText.length) verseNumber.length else 0

                        val startCharIndex = redText.indexOf("{", s - 1)
                        val endCharIndex = redText.indexOf("}", e - 1)

                        var spaceAfterStart = redText.indexOf(" ", startCharIndex + 1)
                        if (spaceAfterStart < 0) spaceAfterStart = zSpace

                        val spaceBeforeStart = redText.substring(0, startCharIndex).lastIndexOf(' ')
                        val spaceBeforeEnd = redText.substring(0, endCharIndex).lastIndexOf(' ')

                        val startWord = redText.substring(startCharIndex, spaceAfterStart)
                        val endWord = redText.substring(spaceBeforeEnd, endCharIndex)

                        val newStart =
                            finalText.indexOf(
                                startWord.replace(onlyAplhaNumericRegex, ""),
                                spaceBeforeStart,
                                false
                            )
                        val newEnd =
                            finalText.indexOf(
                                endWord.replace(onlyAplhaNumericRegex, ""),
                                spaceBeforeEnd,
                                true
                            )

                        if (newEnd > 0) end = newEnd + endWord.length
                        if (newStart in 0..(end - 1)) start = newStart
                        //if (i==0)
                        //log d "start :$start = $finalText"

                        if (vm.kjvStyling) {
                            end += vt.length - item.verseText!!.length

                        }
                        if (end > finalText.length) end = finalText.lastIndex

                        // Since we removed Punctuation we have to add the missing index back in
                        if (zSpace >= 0 && end in zSpace - 3..zSpace) end = zSpace


                        //log d finalText.substring(start,end)
                        try {
                            finalText.setSpan(
                                ForegroundColorSpan(redColor),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        } catch (ex: Exception) {
                            Log.wtf("RedLetter", ex)
                        }
                    }
                }
            }

            verseView.setText(finalText, TextView.BufferType.SPANNABLE)

            /*
                verseView.setTextFuture(
                PrecomputedTextCompat.getTextFuture(text,
                    TextViewCompat.getTextMetricsParams(verseView),
                   vm.executorDispatcher.executor))*/


        }
    }
}