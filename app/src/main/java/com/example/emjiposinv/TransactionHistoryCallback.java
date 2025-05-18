package com.example.emjiposinv;

import java.util.List;

public interface TransactionHistoryCallback {
    void onSuccess(List<TransacHis> historyList);
    void onError(String error);
}