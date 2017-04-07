package androks.simplywash.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androks.simplywash.R;
import androks.simplywash.models.entity.Schedule;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by androks on 4/1/2017.
 */

public class ScheduleDialog extends AppCompatDialogFragment {

    @BindView(R.id.weekdays)
    TextView mWeekdays;
    @BindView(R.id.saturday)
    TextView mSaturday;
    @BindView(R.id.sunday)
    TextView mSunday;

    private Schedule schedule;

    public ScheduleDialog() {
        // Empty constructor required for DialogFragment
    }

    public static ScheduleDialog newInstance(Schedule schedule) {
        ScheduleDialog dialog = new ScheduleDialog();
        dialog.setWasher(schedule);
        return dialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_schedule, container, false);
        ButterKnife.bind(this, view);
        Dialog dialog = getDialog();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        setData();
        return view;
    }


    private void setData() {
        mWeekdays.setText(schedule.getWeekdays());
        mSaturday.setText(schedule.getSaturday());
        mSunday.setText(schedule.getSunday());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @OnClick(R.id.close)
    public void close() {
        this.dismiss();
    }

    public void setWasher(Schedule schedule) {
        this.schedule = schedule;
    }
}
