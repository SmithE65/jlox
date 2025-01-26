//package org.smithe65.jlox;
//
//class AstPrinter implements Expression.Visitor<String> {
//
//    String print(Expression expression) {
//        return expression.accept(this);
//    }
//
//    @Override
//    public String visitBinaryExpression(Expression.Binary expression) {
//        return parenthesize(expression.operator.lexeme, expression.left, expression.right);
//    }
//
//    @Override
//    public String visitGroupingExpression(Expression.Grouping expression) {
//        return parenthesize("group", expression.expression);
//    }
//
//    @Override
//    public String visitLiteralExpression(Expression.Literal expression) {
//        if (expression.value == null) return "nil";
//        return expression.value.toString();
//    }
//
//    @Override
//    public String visitUnaryExpression(Expression.Unary expression) {
//        return parenthesize(expression.operator.lexeme, expression.right);
//    }
//
//    @Override
//    public String visitVariableExpression(Expression.Variable expression) {
//        return expression.name.lexeme;
//    }
//
//    private String parenthesize(String name, Expression... expressions) {
//        StringBuilder builder = new StringBuilder();
//
//        builder.append("(");
//        builder.append(name);
//
//        for (Expression expression : expressions) {
//            builder.append(" ");
//            builder.append(expression.accept(this));
//        }
//
//        builder.append(")");
//
//        return builder.toString();
//    }
//}
