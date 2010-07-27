package com.github.gikolipse.utils;

public class A {

    public A(String text, String url) {
	this.text = text;
	this.url = url;
    }

    public String text;
    public String url;

    @Override
    public int hashCode() {
	return super.hashCode();
    }

    @Override
    public String toString() {
	return this.text;
    }
}
