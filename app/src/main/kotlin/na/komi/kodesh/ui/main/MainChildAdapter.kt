package na.komi.kodesh.ui.main

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.AlignmentSpan
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
import na.komi.kodesh.ui.widget.ViewPager3
import na.komi.kodesh.util.log
import na.komi.kodesh.util.text.spanBetween
import kotlin.coroutines.CoroutineContext


class MainChildAdapter(
    private val vm: MainViewModel
) :
    RecyclerView.Adapter<MainChildAdapter.ViewHolder>(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private val job = SupervisorJob()
    private val list = mutableListOf<Bible>()
    private val cleanList by lazy { mutableListOf<String>() }

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

    var mRecyclerView: RecyclerView? = null
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }
    private val spannableFactory by lazy { MySpannableFactory() }

    private val onlyAplhaNumericRegex by lazy { """[^A-Za-z0-9 ]""".toRegex() }
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
                    if (position == 0 && (vm.showDropCap || vt.contains("<"))) return@run
                    finalText.withSpan(RelativeSizeSpan(1f / 1.9f)) {
                        append("${item.verseId!!}")
                    }
                    finalText.withSpan(ForegroundColorSpan(Formatting.NumColor))
                    finalText.append("  ")
                    verseNumber = finalText.toString()
                }
            }

            @Suppress("RedundantIf")
            if (position == 0) {
                var newVt = vt
                if (vt.contains("<")) {
                    //log d "mRecyclerView = ${mRecyclerView}"
                    //log d "mRecyclerView.parent = ${mRecyclerView?.parent}"
                    //log d "mRecyclerView.parent?.parent = ${mRecyclerView?.parent?.parent}"
                    verseView.hasPeriscope = true
                    val i = vt.lastIndexOf('>')+1
                    val s1 = vt.substring(0, i)
                    val s2 = vt.substring(i).trim()
                    newVt = s2
                    vt.lastIndexOf("<")
                    val span = SpannableStringBuilder(s1.replace("<", "").replace(">", ""))
                    tryy {
                        finalText.append(span)
                        finalText.spanBetweenEach(StyleSpan(Typeface.ITALIC), "[", "]", true)

                        finalText.withSpan(StyleSpan(Typeface.BOLD))
                        finalText.withSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER))

                        finalText.append("\n\n")

                    }
                } else verseView.hasPeriscope = false
                if (vm.showDropCap) {
                    verseView.showDropCap = true
                    verseView.dropCapText = newVt.take(1)
                    leadingMarginSpan.setMargin(verseView.tp.measureText(verseView.dropCapText).toInt() + verseView.MARGIN_PADDING)
                    finalText.withSpan(leadingMarginSpan) {
                        append(newVt.drop(1))
                    }
                    // Italics
                    tryy {
                        finalText.spanBetweenEach(StyleSpan(Typeface.ITALIC), "[", "]", true)
                    }
                    verseView.post {
                        if (verseView.lineCount == 1)
                            verseView.updatePadding(top = 200)
                        else
                            verseView.updatePadding(top = 0)
                    }
                } else {
                    // Add verse number
                    if (vt.contains("<")) {
                        val verseNum = "${item.verseId!!}"
                        finalText.withSpan(RelativeSizeSpan(1f / 1.9f)) {
                            append(verseNum)
                        }
                        finalText.setSpan(ForegroundColorSpan(Formatting.NumColor),finalText.length- verseNum.length,finalText.length ,SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                        finalText.append("  ")
                        verseNumber = "$verseNum  "
                    }

                    finalText.append(newVt)
                    // Italics
                    tryy {
                        finalText.spanBetweenEach(StyleSpan(Typeface.ITALIC), "[", "]", true)
                    }
                    verseView.showDropCap = true
                    verseView.dropCapText = ""
                }

            } else {
                if (item.verseId!! == list.size && vt.contains("<")) {
                    log d "POSITION ${item.verseId!!}"
                    if (vt.contains("<")) {
                        val i = vt.indexOf("<")
                        val s1 = vt.substring(0, i).trim()
                        val s2 = vt.substring(i)
                        val span = SpannableStringBuilder(s2.replace("<", "").replace(">", ""))
                        tryy {
                            finalText.append(s1)
                            finalText.spanBetweenEach(StyleSpan(Typeface.ITALIC), "[", "]", true)

                            span.withSpan(StyleSpan(Typeface.BOLD))
                            span.withSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER))

                            finalText.append("\n\n")
                            finalText.append(span)
                        }
                    }
                } else {
                    finalText.append(vt)

                    // Italics
                    tryy {
                        finalText.spanBetweenEach(StyleSpan(Typeface.ITALIC), "[", "]", true)
                    }
                }
                verseView.showDropCap = false
                verseView.hasPeriscope = false
                verseView.dropCapText = ""
            }


            // Red Letters
            if (vm.showRedLetters && item.bookId!! >= 40) {
                //d{"Ran block"}
                val rIdx =
                    Formatting.redLetterList.indexOfFirst { str -> str.indexOf("${item.bookId}\t${item.chapterId}\t${item.verseId}\t") >= 0 }
                if (rIdx >= 0) {
                    val it = Formatting.redLetterList[rIdx]
                    val _redText = it.substring(it.lastIndexOf('\t') + 1)
                    val redText = if (vm.kjvStyling) {
                        val kjv = Formatting.kjvList[item.id - 1]
                        Formatting.diffText(_redText, kjv)
                    } else _redText
                    Formatting.redLetterPositions(rIdx) { s, e, c ->

                        // Safety bounds
                        val zSpace = finalText.length//verse.indexOf(ZSPACE)
                        var start = s orr 0
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
                        if (start > end) start = s orr 0
                        if (start > end) start = 0

                        if (end > finalText.length) end = finalText.lastIndex

                        // Since we removed Punctuation we have to add the missing index back in
                        if (zSpace >= 0 && end in zSpace - 3..zSpace) end = zSpace

                        // If this is the last word, go to the end.
                        if (endCharIndex in redText.length - 2..redText.length + 2)
                            end = finalText.length

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

    private infix fun Int.orr(other: Int): Int {

        return if (this < 0) other else this
    }
}