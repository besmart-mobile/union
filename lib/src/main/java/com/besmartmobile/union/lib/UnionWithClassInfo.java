package com.besmartmobile.union.lib;

public abstract class UnionWithClassInfo {
    private String _actualClassName = this.getClass().getName();

    public String getActualClassName() {
        return _actualClassName;
    }
}
