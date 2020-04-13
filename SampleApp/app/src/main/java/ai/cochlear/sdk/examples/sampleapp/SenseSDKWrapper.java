package ai.cochlear.sdk.examples.sampleapp;

import android.content.Context;

import ai.cochlear.sdk.core.Cochl;
import ai.cochlear.sdk.models.Model;

public class SenseSDKWrapper {
    private static SenseSDKWrapper instance = null;
    private Model model = null;
    private Cochl cochl = null;

    public static SenseSDKWrapper getInstance() {
        if (instance == null) {
            synchronized (SenseSDKWrapper.class) {
                if (instance == null) {
                    instance = new SenseSDKWrapper();
                }
            }
        }
        return instance;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Model getModel() {
        return this.model;
    }

    public void setCochl(Cochl cochl) {
        this.cochl = cochl;
    }

    public Cochl getCochl() {
        return this.cochl;
    }

    public void init(Context context, String sdkKey) {
        cochl = Cochl.getInstance();
        cochl.init(context, sdkKey);
        model = cochl.getModel("emergency");
    }

    public void stop() {
        if (model != null)
            model.stopPredict();
    }
}
