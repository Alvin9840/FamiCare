package com.ethan.FamiCare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class DiaryFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public DiaryFragment() {
        // Required empty public constructor
    }

    public static DiaryFragment newInstance(String param1, String param2) {
        DiaryFragment fragment = new DiaryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private int selected_date;
    //Layout 元素
    private CalendarView calender;
    private TextView date;
    private Button title;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        calender = view.findViewById(R.id.calender);
        date = view.findViewById(R.id.date);
        title = view.findViewById(R.id.title);

        //監聽選擇到的日期，改變date，抓取資料庫對應日期的資料，顯示資料庫標題到title
        calender.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                //變更日期
                date.setText((month + 1) + "/" + dayOfMonth);
                selected_date = getSelected_date(year, month, dayOfMonth);
            }
        });

        //點擊日記標題，紀錄選擇的日期(20230101)，跳轉到DiaryContent
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!date.getText().equals("日期")) {

                    DiaryContentFragment diaryContentFragment = new DiaryContentFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", selected_date);
                    diaryContentFragment.setArguments(bundle);//把日期送到要跳轉的Fragment
                    FragmentManager fm = getActivity().getSupportFragmentManager();

                    fm.beginTransaction().addToBackStack(null).replace(R.id.Diary_layout, diaryContentFragment).commit();
                }
            }
        });

        return view;
    }

    public int getSelected_date(int year, int month, int dayOfMonth) {
        String s = String.format("%4d%02d%02d", year, month + 1, dayOfMonth);
        return Integer.parseInt(s);
    }
}