package com.firebase.rest.api;

import com.firebase.rest.neli.FirebaseRestApi;
import com.firebase.rest.neli.auth.AccessTokenResponse;
import com.firebase.rest.neli.auth.LoginResponse;

import org.jetbrains.annotations.NotNull;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

class ExampleLogin {

    FirebaseRestApi firebaseRestApi;
    final String FIREBASE_DB_URL = "https://testdtse.firebaseio.com/";
    final String FIREBASE_API_KEY = "AIzaSyCmkwwpis6tIrVef_-TQzrMthfutuNPHDk";

    Continuation<LoginResponse>contLoginResponse = new Continuation<LoginResponse>() {
        @NotNull
        @Override
        public CoroutineContext getContext() {
            return EmptyCoroutineContext.INSTANCE;
        }

        @Override
        public void resumeWith(@NotNull Object result) {
            LoginResponse response = (LoginResponse)result;
            if (response.getIdToken().length() > 0){
                firebaseRestApi.doLoginWithAccessToken(response.getRefreshToken(),contAccessToken);
            }
        }
    };

    Continuation<AccessTokenResponse>contAccessToken = new Continuation<AccessTokenResponse>() {
        @NotNull
        @Override
        public CoroutineContext getContext() {
            return EmptyCoroutineContext.INSTANCE;
        }

        @Override
        public void resumeWith(@NotNull Object result) {
            //AccessTokenResponse response = (AccessTokenResponse) result;
            if (firebaseRestApi.isLogged()){
                getData();
            }
        }
    };

    Continuation<String> contGetData = new Continuation<String>() {
        @NotNull
        @Override
        public CoroutineContext getContext() {
            return EmptyCoroutineContext.INSTANCE;
        }

        @Override
        public void resumeWith(@NotNull Object result) {
            System.out.println("----read value: "+result);
        }
    };

    private void doLogin() {
        firebaseRestApi = new FirebaseRestApi(FIREBASE_DB_URL, FIREBASE_API_KEY);
        firebaseRestApi.doLogin("dtse.fra.sp@gmail.com", "fra8025!",
                true, contLoginResponse);
    }

    void init(){
            doLogin();
    }

    void getData(){
        firebaseRestApi.get("",contGetData);
    }
}
