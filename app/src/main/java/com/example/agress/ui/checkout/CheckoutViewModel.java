package com.example.agress.ui.checkout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CheckoutViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CheckoutViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is checkout fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}