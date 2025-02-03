package org.smithe65.jlox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Statement.Function declaration;
    private final Environment closure;

    LoxFunction(Statement.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment);
    }

    @Override
    public int arity() { return declaration.parameters.size(); }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < arguments.size(); ++i) {
            environment.define(declaration.parameters.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name + ">";
    }
}
