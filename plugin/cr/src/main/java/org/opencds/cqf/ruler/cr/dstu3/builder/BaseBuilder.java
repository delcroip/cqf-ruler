package org.opencds.cqf.ruler.cr.dstu3.builder;

/*
    These builders are based off of work performed by Philips Healthcare.
    I simplified their work with this generic base class and added/expanded builders.

    Tip of the hat to Philips Healthcare developer nly98977
*/

public class BaseBuilder<T> {

    protected T complexProperty;

    public BaseBuilder(T complexProperty) {
        this.complexProperty = complexProperty;
    }

    public T build() {
        return complexProperty;
    }
}
