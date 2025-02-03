package org.smithe65.jlox;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    private final LoxClass definition;
    private final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(LoxClass definition) {
        this.definition = definition;
    }

    Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        LoxFunction method = definition.findMethod(name.lexeme);

        if (method != null) {
            return method.bind(this);
        }

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return definition.name + " instance";
    }
}
