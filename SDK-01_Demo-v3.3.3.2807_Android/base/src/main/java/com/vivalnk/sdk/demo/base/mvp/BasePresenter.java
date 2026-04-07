/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vivalnk.sdk.demo.base.mvp;

public class BasePresenter<M extends IModel, V extends IView> implements IPresenter {
    protected final String TAG = this.getClass().getSimpleName();

    protected M mModel;
    protected V mView;

    public BasePresenter(M model, V view) {
        this.mModel = model;
        this.mView = view;
        onStart();
    }

    public BasePresenter(V view) {
        this.mView = view;
        onStart();
    }

    public BasePresenter() {
        onStart();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onDestroy() {
        if (mModel != null) {
            mModel.onDestroy();
        }
        this.mModel = null;
        this.mView = null;
    }

}
