package com.example.emjiposinv;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SettingFragment extends Fragment {

    private Button btnCreateAccount, btnChangePassword, btnChangePin;

    private TableLayout tableLayout;
    private SupabaseAuthApi supabaseAuthApi;

    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIs" +
            "InJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA" +
            "1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdX" +
            "BhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczO" +
            "TA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        btnCreateAccount = view.findViewById(R.id.btncreate);
        btnChangePassword = view.findViewById(R.id.btnchangepass);
        btnChangePin = view.findViewById(R.id.btnchangepin);

        btnChangePin.setOnClickListener(V -> {
            Intent intent = new Intent(requireContext(), PinSettingsActivity.class);
            startActivity(intent);
        });

        tableLayout = view.findViewById(R.id.tableLayout);
        supabaseAuthApi = RetrofitClient.getClient().create(SupabaseAuthApi.class);

        String role = AuthManager.Role;

        hideViews(btnCreateAccount, tableLayout);

        if (role.equals("Admin")) {
           showViews(btnCreateAccount, tableLayout);
        }

        btnCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SignupActivity.class);
            startActivity(intent);
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ResetPasswordEmailActivity.class);
            startActivity(intent);
        });

        fetchData();

        return view;
    }

    private void fetchData() {
        supabaseAuthApi.getAllAccountLog(API_KEY, AUTH_TOKEN).enqueue(new Callback<List<AccountLog>>() {
            @Override
            public void onResponse(Call<List<AccountLog>> call, Response<List<AccountLog>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateTable(response.body());
                } else {
                    Log.e("Supabase", "Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<AccountLog>> call, Throwable t) {
                Log.e("Supabase", "Failed to fetch data", t);
            }
        });
    }

    private void populateTable(List<AccountLog> accountlogs) {
        tableLayout.removeAllViews();
        addTableHeader();

        for (AccountLog accountlog : accountlogs) {
            TableRow row = new TableRow(requireContext());
            row.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView emailText = createStyledCell(accountlog.getEmail());
            TextView UserText = createStyledCell(accountlog.getUser());
            TextView dateText = createStyledCell(accountlog.getDate());
            TextView timeText = createStyledCell(accountlog.getTime());
            TextView actionText = createStyledCell(accountlog.getAction());



            row.addView(emailText);
            row.addView(UserText);
            row.addView(dateText);
            row.addView(timeText);
            row.addView(actionText);


            tableLayout.addView(row);
        }
    }


    private void addTableHeader() {
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headerRow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue));

        String[] headers = {"Email", "User", "Date", "Time", "Action"};
        for (String header : headers) {
            TextView textView = createStyledHeader(header);
            headerRow.addView(textView);
        }

        tableLayout.addView(headerRow);
    }

    private TextView createStyledCell(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setPadding(16, 8, 16, 8);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);
        textView.setBackgroundResource(R.drawable.table_cell_border);
        textView.setTextColor(getResources().getColor(R.color.table_text));
        textView.setBackgroundColor(getResources().getColor(R.color.table_background));
        return textView;
    }

    private TextView createStyledHeader(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setPadding(16, 8, 16, 8);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextColor(getResources().getColor(android.R.color.white));
        return textView;
    }



    private void hideViews(View... views) {
        for (View view : views) {
            view.setVisibility(View.GONE);
        }
    }

    // Helper function to show multiple views
    private void showViews(View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }
}