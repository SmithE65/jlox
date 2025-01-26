package org.smithe65.jlox;

public class Return extends RuntimeException {
    final Object value;

    Return(final Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
