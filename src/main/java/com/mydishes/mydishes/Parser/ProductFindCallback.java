package com.mydishes.mydishes.Parser;

public interface ProductFindCallback {
    void onSuccess();

    void onError(Exception e);
}
