

package edu.ora.se.java.generics.examples ;


import java.util.List;

public class WildcardError {

    void foo(List<?> i) {
        i.set(0, i.get(0));
    }
}
