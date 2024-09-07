package org.smithe65.jlox;

public abstract class Statement {
    abstract <R> R accept(Visitor<R> visitor);

    public interface Visitor<R> {
        R visitExpressionStatement(Expression statement);

        R visitPrintStatement(Print statement);
    }

    public static class Expression extends Statement {
        final org.smithe65.jlox.Expression expression;

        Expression(org.smithe65.jlox.Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStatement(this);
        }
    }

    public static class Print extends Statement {
        final org.smithe65.jlox.Expression expression;

        Print(org.smithe65.jlox.Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStatement(this);
        }
    }
}
