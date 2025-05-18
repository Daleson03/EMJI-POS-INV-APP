package com.example.emjiposinv;

import java.util.List;

public interface ReturnListCallback {
    void onSuccess(List<ReturnItem> returnList);
    void onError(String error);
}