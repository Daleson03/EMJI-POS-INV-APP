package com.example.emjiposinv;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

public class AddUpdateSupFragment extends Fragment {

    private Spinner ddSupplier;
    private List<Supplier> supplierList = new ArrayList<>();
    private ArrayAdapter<Supplier> adapter;
    private DatabaseHelper databaseHelper;

    private EditText SupplierName, ContactPerson, Phone, Email;
    private TextView tvTime;
    private Button btnPickTime, btnAddSup, btnUpSup;
    private String selectedTime;  // Variable to store selected time

    private SupabaseAuthApi supabaseAuthApi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_update_sup, container, false);

        // Pass 'this' (the current fragment instance) to DatabaseHelper
        databaseHelper = new DatabaseHelper(requireContext(), this);

        ddSupplier = view.findViewById(R.id.ddsupplierid);
        SupplierName = view.findViewById(R.id.txtsuppliername);
        ContactPerson = view.findViewById(R.id.txtcontactperson);
        Phone = view.findViewById(R.id.txtphone);
        Email = view.findViewById(R.id.txtemail);

        tvTime = view.findViewById(R.id.txtcutofftime);
        btnPickTime = view.findViewById(R.id.btnPickTime);
        btnAddSup = view.findViewById(R.id.btnadd3);
        btnUpSup = view.findViewById(R.id.btnupdate3);

        btnPickTime.setOnClickListener(v -> showTimePicker());
        btnAddSup.setOnClickListener(v -> insertSupplier());
        btnUpSup.setOnClickListener(v -> updateSupplier());

        supabaseAuthApi = RetrofitClient.getClient().create(SupabaseAuthApi.class);

        // Fetch supplier data
        fetchSuppliers();

        btnUpSup.setEnabled(false);

        return view;
    }

    private int selectedSupplierID = -1; // Store selected SupplierID

    void fetchSuppliers() {
        supabaseAuthApi.getSuppliers().enqueue(new Callback<List<Supplier>>() {
            @Override
            public void onResponse(Call<List<Supplier>> call, Response<List<Supplier>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    supplierList.clear();
                    supplierList.add(new Supplier(0, "Select a Supplier", "", "", "", "")); // Blank first item
                    supplierList.addAll(response.body());

                    adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, supplierList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    ddSupplier.setAdapter(adapter);

                    ddSupplier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position > 0) { // Ignore first blank item
                                Supplier selectedSupplier = supplierList.get(position);
                                selectedSupplierID = selectedSupplier.getSupplierID(); // Store SupplierID

                                SupplierName.setText(selectedSupplier.getSupplierName());
                                Phone.setText(selectedSupplier.getPhone());
                                ContactPerson.setText(selectedSupplier.getContactPerson());
                                Email.setText(selectedSupplier.getEmail());
                                tvTime.setText(selectedSupplier.getCutoffTime());

                                btnUpSup.setEnabled(true);
                                btnAddSup.setEnabled(false);
                            } else {
                                selectedSupplierID = -1; // Reset if "Select a Supplier" is chosen
                                SupplierName.setText("");
                                Phone.setText("");
                                ContactPerson.setText("");
                                Email.setText("");
                                tvTime.setText("");

                                btnUpSup.setEnabled(false);
                                btnAddSup.setEnabled(true);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // Do nothing
                        }
                    });
                } else {
                    try {
                        Log.e("SupabaseError", "Response Error: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e("SupabaseError", "Error reading response body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Supplier>> call, Throwable t) {
                Log.e("SupabaseError", "Error fetching suppliers: " + t.getMessage());
            }
        });
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minuteOfHour) -> {
                    String amPm;
                    int hourFormat;

                    if (hourOfDay >= 12) {
                        amPm = "PM";
                        hourFormat = (hourOfDay == 12) ? 12 : hourOfDay - 12;
                    } else {
                        amPm = "AM";
                        hourFormat = (hourOfDay == 0) ? 12 : hourOfDay;
                    }

                    String formattedTime = String.format("%02d:%02d %s", hourFormat, minuteOfHour, amPm);
                    tvTime.setText(formattedTime);
                },
                hour, minute, false // false -> 12-hour format
        );

        timePickerDialog.show();
    }
    private void insertSupplier() {
        String suppliername = SupplierName.getText().toString();
        String contactperson = ContactPerson.getText().toString();
        String phone = Phone.getText().toString();
        String email = Email.getText().toString();
        String cutofftime = tvTime.getText().toString();

        if (suppliername.isEmpty() || contactperson.isEmpty() || phone.isEmpty() || email.isEmpty() || cutofftime.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }



        databaseHelper.insertSupplier(suppliername, contactperson, phone, email, cutofftime);

        clearFields();
        fetchSuppliers();
        Toast.makeText(getContext(), "Supplier Adding...", Toast.LENGTH_SHORT).show();
    }

    private void updateSupplier() {
        String suppliername = SupplierName.getText().toString();
        String contactperson = ContactPerson.getText().toString();
        String phone = Phone.getText().toString();
        String email = Email.getText().toString();
        String cutofftime = tvTime.getText().toString();

        if (suppliername.isEmpty() || contactperson.isEmpty() || phone.isEmpty() || email.isEmpty() || cutofftime.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedSupplierID == -1) {
            Toast.makeText(getContext(), "No supplier selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseHelper.updateSupplier(selectedSupplierID, suppliername, contactperson, phone, email, cutofftime);

        clearFields();
        fetchSuppliers();
        Toast.makeText(getContext(), "Supplier Updating...", Toast.LENGTH_SHORT).show();
    }

    private void clearFields() {
        SupplierName.setText("");
        ContactPerson.setText("");
        Phone.setText("");
        Email.setText("");


        btnUpSup.setEnabled(false); // Disable update button after updating
    }
}
