package com.example.emjiposinv;

import java.util.List;

public interface NewReturnListCallback {
    void onSuccess(List<ReturnTB> returnList);
    void onError(String errorMessage);
}