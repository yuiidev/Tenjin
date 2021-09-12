package net.demozo.tenjin;

public abstract class Model<T> {
    public Model() {
    }

    public void save() {
        Tenjin.save(this);
    }
}
