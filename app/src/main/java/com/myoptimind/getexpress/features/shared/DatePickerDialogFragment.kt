package com.myoptimind.suzuki_app.features.shared

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.shared.BaseDialogFragment
import com.myoptimind.getexpress.features.shared.toReadableDateTime
import kotlinx.android.synthetic.main.dialog_date_picker.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber


private const val ARGS_YEAR = "dp_args_year"
private const val ARGS_MONTH_OF_YEAR = "dp_args_month_of_year"
private const val ARGS_DAY_OF_MONTH = "dp_day_of_month"
private const val ARGS_MAX_DATE = "dp_max_date"
private const val ARGS_MIN_DATE = "dp_min_date"


class DatePickerDialogFragment(): BaseDialogFragment(){

    companion object {

        const val EXTRA_DATE = "date_data"

        fun newInstance(year: Int, monthOfYear: Int, dayOfMonth: Int, maxDate: Long? = null, minDate:Long? = null): DatePickerDialogFragment {
            val args = Bundle()

            args.putInt(ARGS_YEAR,year)
            args.putInt(ARGS_MONTH_OF_YEAR,monthOfYear)
            args.putInt(ARGS_DAY_OF_MONTH,dayOfMonth)

            if(maxDate != null){
                args.putLong(ARGS_MAX_DATE,maxDate)
            }

            if(minDate != null){
                args.putLong(ARGS_MIN_DATE,minDate)
            }

            val fragment = DatePickerDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_date_picker,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ib_close.setOnClickListener { this.dismiss() }
        arguments.let {
            if(it != null){
                if(it.containsKey(ARGS_MAX_DATE)){
                    date_picker.maxDate = it.getLong(ARGS_MAX_DATE)
                }
                if(it.containsKey(ARGS_MIN_DATE)){
                    date_picker.minDate = it.getLong(ARGS_MIN_DATE)
                }

                date_picker.init(it.getInt(ARGS_YEAR),
                        it.getInt(ARGS_MONTH_OF_YEAR),
                        it.getInt(ARGS_DAY_OF_MONTH)){ _, year, monthOfYear, dayOfMonth ->
                    Timber.v("Date picked: [ Month:$monthOfYear ][ Day:$dayOfMonth ][ Year:$year ]")
                    // add 1 to month of year because java.calendar month starts on 0 :)
                    val dt = DateTimeFormat.forPattern("yyyy M d").parseDateTime("$year ${monthOfYear + 1} $dayOfMonth")
                    targetFragment?.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(EXTRA_DATE,dt.toReadableDateTime()) // PARSE THIS ("yyyy M d")
                    )
                    this.dismiss()
                }
            }
        }

    }

}